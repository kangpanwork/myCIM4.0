package com.fa.cim.service.fmc.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.BondingGroupMethod;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.exceptions.NotFoundRecordException;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.cjpj.IControlJobProcessJobService;
import com.fa.cim.service.fmc.IFMCInqService;
import com.fa.cim.service.fmc.IFMCService;
import com.fa.cim.service.recipe.IRecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            lightyh                create file
 *
 * @author: lightyh
 * @date: 2020/9/8 17:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class FMCServiceImpl implements IFMCService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IObjectLockMethod lockMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IMachineRecipeMethod machineRecipeMethod;

    @Autowired
    private IRecipeService recipeService;

    @Autowired
    private IControlJobProcessJobService controlJobProcessJobService;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IEquipmentContainerPositionMethod equipmentContainerPositionMethod;

    @Autowired
    private ISLMMethod slmMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;


    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IEquipmentContainerMethod equipmentContainerMethod;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IFMCInqService fmcInqService;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private BondingGroupMethod bondingGroupMethod;


    /**
     * This function is start lots reservation function of SLM.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/28 11:16
     */
    @Override
    public ObjectIdentifier sxSLMStartLotsReservationReq(Infos.ObjCommon objCommon,
                                                         Params.SLMStartLotsReservationReqInParams slmStartLotsReservationReqInParams,
                                                         AtomicReference<String> APCIFControlStatus) {
        Validations.check(null == slmStartLotsReservationReqInParams || null == APCIFControlStatus, retCodeConfig.getInvalidParameter());

        ObjectIdentifier equipmentID = slmStartLotsReservationReqInParams.getEquipmentID();
        String portGroupID = slmStartLotsReservationReqInParams.getPortGroupID();
        ObjectIdentifier controlJobID = slmStartLotsReservationReqInParams.getControlJobID();
        List<Infos.StartCassette> strStartCassette = slmStartLotsReservationReqInParams.getStartCassetteList();
        List<Infos.MtrlOutSpec> strMtrlOutSpecSeq = slmStartLotsReservationReqInParams.getMtrlOutSpecList();


        List<Infos.StartCassette> tmpStartCassette = new ArrayList<>(strStartCassette);


        log.info("InParam [equipmentID] :" + ObjectIdentifier.fetchValue(equipmentID));
        log.info("InParam [portGroupID] :" + portGroupID);
        log.info("InParam [controlJobID]:" + ObjectIdentifier.fetchValue(controlJobID));
        Validations.check(CimArrayUtils.isEmpty(strStartCassette), retCodeConfig.getInvalidDataValue());

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("Check Transaction ID and equipment Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        //Input parameter check.
        //Check every lot has at least one wafer with processJobExecFlag == TRUE
        log.info("Check every lot has at least one wafer with processJobExecFlag == TRUE.");
        lotMethod.lotProcessJobExecFlagValidCheck(objCommon, tmpStartCassette);

        //-------------------------------------------------
        //  Check Scrap Wafer Exist In Carrier
        //-------------------------------------------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        Optional.of(tmpStartCassette).ifPresent(list -> list.forEach(data -> cassetteIDs.add(data.getCassetteID())));

        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
        if (CimArrayUtils.getSize(lotWaferMaps) > 0) {
            log.error("ScrapWafer Found ");
            throw new ServiceException(retCodeConfig.getFoundScrap());
        }

        /*-----------------------------------------*/
        /*   call txAPCRunTimeCapabilityInq        */
        /*-----------------------------------------*/
        // todo: txAPCRunTimeCapabilityInq

        /*------------------------------------------------*/
        /*   call txAPCRecipeParameterAdjustInq           */
        /*------------------------------------------------*/
        // todo: txAPCRecipeParameterAdjustInq

        //APC result check.
        //Check every lot has at least one wafer with processJobExecFlag == TRUE
        // todo: lot_processJobExecFlag_ValidCheck


        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(ThreadContextHolder.getTransactionId());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        Long lockMode = objLockModeOut.getLockMode();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());

            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

            /*------------------------------*/
            /*   Lock Dispatcher Object     */
            /*------------------------------*/
            lockMethod.objectLock(objCommon, CimDispatcher.class, equipmentID);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            /*--------------------------------------------*/
            /*      Machine Object Lock Process           */
            /*--------------------------------------------*/
            lockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        /*--------------------------------------------*/
        /*        Port Object Lock Process            */
        /*--------------------------------------------*/
        log.info("#### Port Object Lock ");
        /*--------------------------------------------------------*/
        /*   Get All Ports being in the same Port Group as ToPort */
        /*--------------------------------------------------------*/
        Infos.EqpPortInfo eqpPortInfo = portMethod.portResourceAllPortsInSameGroupGet(objCommon, equipmentID, strStartCassette.get(0).getLoadPortID());

        /*---------------------------------------------------------*/
        /* Lock All Ports being in the same Port Group as ToPort   */
        /*---------------------------------------------------------*/
        if (null != eqpPortInfo) {
            Optional.ofNullable(eqpPortInfo.getEqpPortStatuses()).ifPresent(list -> list.forEach(data -> {
                lockMethod.objectLockForEquipmentResource(objCommon, equipmentID, data.getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                log.info("Locked port object  : " + data.getPortID().getValue());
            }));
        }

        /*----------------------------------------------------------------------*/
        /*       Object Lock Source Cassettes and Destination Cassettes         */
        /*----------------------------------------------------------------------*/
        log.info("#### Source Cassettes and Destination Cassettes Object Lock ");
        Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrlOutSpec -> {
            Optional.ofNullable(mtrlOutSpec.getSourceMapList()).ifPresent(list ->
                    list.forEach(data -> lockMethod.objectLock(objCommon, CimCassette.class, data.getCassetteID())));
            Optional.ofNullable(mtrlOutSpec.getDestinationMapList()).ifPresent(list ->
                    list.forEach(data -> lockMethod.objectLock(objCommon, CimCassette.class, data.getCassetteID())));
        }));

        /*--------------------------------------------*/
        /*                                            */
        /*       Cassette Object Lock Process         */
        /*                                            */
        /*--------------------------------------------*/
        log.info("#### Cassette Object Lock ");
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        Optional.of(tmpStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
            Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(list -> list.forEach(data -> lotIDs.add(data.getLotID())));
        }));

        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/
        lockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        lockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        /*-----------------------------------------------*/
        /*   Equipment Container Position Lock Process   */
        /*-----------------------------------------------*/
        Inputs.ObjObjectLockForEquipmentContainerPositionIn positionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
        positionIn.setEquipmentID(equipmentID);
        positionIn.setStrStartCassette(tmpStartCassette);
        List<ObjectIdentifier> containerPositions = lockMethod.objectLockForEquipmentContainerPosition(objCommon, positionIn);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        //-------------------------------------------------
        //  Check Contamination (pr flag and contamination level)
        //-------------------------------------------------
        List<ObjectIdentifier> moveInLotIds = tmpStartCassette.parallelStream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().parallelStream()
                        .filter(Infos.LotInCassette::getMoveInFlag).map(Infos.LotInCassette::getLotID))
                .collect(Collectors.toList());
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepIn(moveInLotIds, slmStartLotsReservationReqInParams.getEquipmentID(),"");
        List<Params.ContaminationAllLotCheckParams> allLots = new ArrayList<>();
        for (Infos.StartCassette tempStartCassettes : strStartCassette){
            List<Infos.LotInCassette> lotInCassetteList = tempStartCassettes.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                Params.ContaminationAllLotCheckParams checkParams = new Params.ContaminationAllLotCheckParams();
                allLots.add(checkParams);
                checkParams.setLotID(lotInCassette.getLotID());
                checkParams.setMoveInFlag(lotInCassette.getMoveInFlag());
            }
        }
        contaminationMethod.contaminationLvlCheckAmongLots(allLots);
        //check usage type
        ObjectIdentifier desCarrierId = ObjectIdentifier.emptyIdentifier();
        for (Infos.StartCassette startCassette : tmpStartCassette){
            String loadPurposeType = startCassette.getLoadPurposeType();
            if (CimStringUtils.equals(loadPurposeType,BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)){
                desCarrierId = startCassette.getCassetteID();
            }
        }
        for (Infos.StartCassette startCassette : tmpStartCassette){
            if (ObjectIdentifier.isNotEmpty(desCarrierId)){
                String loadPurposeType = startCassette.getLoadPurposeType();
                if (!CimStringUtils.equals(loadPurposeType,BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)){
                    List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                    for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                        contaminationMethod.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(),lotInCassette.getLotID(),desCarrierId);
                    }
                }
            }
        }

        //check capability
        equipmentMethod.capabilityCheck(objCommon,moveInLotIds,slmStartLotsReservationReqInParams.getEquipmentID());

        // 【step 1-8】check port (top or base) whether match carrier
        slmStartLotsReservationReqInParams.getStartCassetteList()
                .parallelStream()
                .forEach(startCassette ->
                        bondingGroupMethod.portWaferBondingCheck(objCommon, startCassette.getLoadPortID(), slmStartLotsReservationReqInParams.getEquipmentID(), startCassette.getCassetteID())
                );
        //-------------------------------------------------
        //  Check scrap wafer existence in carrier
        //-------------------------------------------------
        cassetteIDs.clear();
        Optional.of(tmpStartCassette).ifPresent(list -> list.forEach(data -> cassetteIDs.add(data.getCassetteID())));
        List<Infos.LotWaferMap> lotWaferMaps1 = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
        if (CimArrayUtils.getSize(lotWaferMaps1) > 0) {
            log.info("ScrapWafer Found ");
            throw new ServiceException(retCodeConfig.getFoundScrap());
        }

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Cassette                                          */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - controlJobID                                                      */
        /*   - multiLotType                                                      */
        /*   - transferState                                                     */
        /*   - transferReserved                                                  */
        /*   - dispatchState                                                     */
        /*   - maxBatchSize                                                      */
        /*   - minBatchSize                                                      */
        /*   - emptyCassetteCount                                                */
        /*   - cassette'sloadingSequenceNomber                                   */
        /*   - eqp's multiRecipeCapability and recipeParameter                   */
        /*   - Upper/Lower Limit for RecipeParameterChange                       */
        /*   - MonitorLotCount or OperationStartLotCount                         */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        cassetteMethod.cassetteCheckConditionForOperation(objCommon, equipmentID, portGroupID, tmpStartCassette, BizConstant.SP_OPERATION_STARTRESERVATION);

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - controlJobID                                                      */
        /*   - lot's equipmentID                                                 */
        /*   - lotHoldState                                                      */
        /*   - lotProcessState                                                   */
        /*   - lotInventoryState                                                 */
        /*   - entityInhibition                                                  */
        /*   - minWaferCount                                                     */
        /*   - equipment's availability for specified lot                        */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        lotMethod.lotCheckConditionForOperation(objCommon, equipmentID, portGroupID, tmpStartCassette, BizConstant.SP_OPERATION_STARTRESERVATION);

        /*-----------------------------------------------------------------------------*/
        /*                                                                             */
        /*   Check Equipment Port for Start Reservation                                */
        /*                                                                             */
        /*   The following conditions are checked by this object                       */
        /*                                                                             */
        /*   1. In-parm's portGroupID must not have controlJobID.                      */
        /*   2. All of ports' loadMode must be CIMFW_PortRsc_Input or _InputOutput.    */
        /*   3. All of port, which is registered as in-parm's portGroup, must be       */
        /*      _LoadAvail or _LoadReq when equipment is online.                       */
        /*      As exceptional case, if equipment's takeOutInTransferFlag is True,     */
        /*      _UnloadReq is also OK for start reservation when equipment is Online.  */
        /*   4. All of port, which is registered as in-parm's portGroup,               */
        /*      must not have loadedCassetteID.                                        */
        /*   5. strStartCassette[].loadPortID's portGroupID must be same               */
        /*      as in-parm's portGroupID.                                              */
        /*   6. strStartCassette[].loadPurposeType must be match as specified port's   */
        /*      loadPutposeType.                                                       */
        /*   7. strStartCassette[].loadSequenceNumber must be same as specified port's */
        /*      loadSequenceNumber.                                                    */
        /*                                                                             */
        /*-----------------------------------------------------------------------------*/
        equipmentMethod.equipmentPortStateCheckForStartReservation(objCommon, equipmentID, portGroupID, tmpStartCassette,false);

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for FlowBatch                                         */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   1. whether in-parm's equipment has reserved flowBatchID or not      */
        /*      fill  -> all of flowBatch member and in-parm's lot must be       */
        /*               same perfectly.                                         */
        /*      blank -> no check                                                */
        /*                                                                       */
        /*   2. whether lot is in flowBatch section or not                       */
        /*      in    -> lot must have flowBatchID, and flowBatch must have      */
        /*               reserved equipmentID.                                   */
        /*               if lot is on target operation, flowBatch's reserved     */
        /*               equipmentID and in-parm's equipmentID must be same.     */
        /*      out   -> no check                                                */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn = new Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(equipmentID);
        operationStartIn.setPortGroupID(portGroupID);
        operationStartIn.setStartCassetteList(tmpStartCassette);
        equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommon, operationStartIn);

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Process Durable                                   */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   1. Whether equipment requires process durable or not                */
        /*      If no-need, return OK;                                           */
        /*                                                                       */
        /*   2. At least one of reticle / fixture for each reticleGroup /        */
        /*      fixtureGroup is in the equipment or not.                         */
        /*      Even if required reticle is in the equipment, its status must    */
        /*      be _Available or _InUse.                                         */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/

        /*-----------------------------------------*/
        /*   Check Process Durable Required Flag   */
        /*-----------------------------------------*/
        try {
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
        } catch (ServiceException e) {
            if (Validations.isEquals(e.getCode(), retCodeConfig.getEquipmentProcessDurableReticleRequired())
                    || Validations.isEquals(e.getCode(), retCodeConfig.getEquipmentProcessDurableFixtRequired())) {
                log.info("code == EQP_PROCDRBL_RTCL_REQD || code == EQP_PROCDRBL_FIXT_REQD");
                for (Infos.StartCassette cassette : tmpStartCassette) {
                    if (CimStringUtils.equals(cassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                        continue;
                    }
                    Optional.ofNullable(cassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                        for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                            if (!lotInCassette.getMoveInFlag()) {
                                continue;
                            }
                            /*--------------------------------------------------*/
                            /*   Check Process Durable Condition for OpeStart   */
                            /*--------------------------------------------------*/
                            Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                            Outputs.ObjProcessDurableCheckConditionForOperationStartOut out = processMethod.processDurableCheckConditionForOpeStart(objCommon,
                                    equipmentID,
                                    startRecipe.getLogicalRecipeID(),
                                    startRecipe.getMachineRecipeID(),
                                    lotInCassette.getLotID());

                            /*---------------------------------------*/
                            /*   Set Available Reticles / Fixtures   */
                            /*---------------------------------------*/
                            startRecipe.setStartReticleList(out.getStartReticleList());
                            startRecipe.setStartFixtureList(out.getStartFixtureList());
                        }
                    });
                }
            } else if (Validations.isEquals(e.getCode(), retCodeConfig.getEquipmentProcessDurableNotRequired())) {
                log.info("code == EQP_PROCDRBL_NOT_REQD, Do nothing.");
            } else {
                throw e;
            }
        }

        /*----------------------------------------------------------------------*/
        /*                                                                      */
        /*   Confirmation for Uploaded Recipe Body and Eqp's Recipe Body        */
        /*                                                                      */
        /*   When the following conditions are all matched, recipe body         */
        /*   confirmation request is sent to TCS.                               */
        /*                                                                      */
        /*   1. Equipment's onlineMode is Online                                */
        /*   2. Equipment's recipe body manage flag is TRUE.                    */
        /*   3. Machine recipe's recipe body confirm flag is TRUE.              */
        //
        //   Force Down Load Flag  Recipe Body Confirm Flag  Conditional Down Load Flag
        //           Yes                      No                       No                      -> Download it without confirmation.
        //           No                       Yes                      No                      -> Confirm only.
        //           No                       Yes                      Yes                     -> If confirmation is NG, download it.
        //           No                       No                       No                      -> No action.
        /*                                                                      */
        /*----------------------------------------------------------------------*/

        //---------------------------------------------------
        // Get Machine Recipe List for Recipe Body Mangement
        //---------------------------------------------------
        List<Infos.RecipeBodyManagement> recipeBodyManagementsOut = machineRecipeMethod.machineRecipeGetListForRecipeBodyManagement(objCommon, equipmentID, tmpStartCassette);
        if (CimArrayUtils.isEmpty(recipeBodyManagementsOut)) {
            log.info("Recipe for Recipe Body Management does not exist.");
        } else {
            for (Infos.RecipeBodyManagement recipeBody : recipeBodyManagementsOut) {
                log.info(" Machine Recipe ID          : " + recipeBody.getMachineRecipeId().getValue());
                log.info(" Force Down Load Flag       : " + recipeBody.getForceDownLoadFlag());
                log.info(" Recipe Body Confirm Flag   : " + recipeBody.getRecipeBodyConfirmFlag());
                log.info(" Conditional Down Load Flag : " + recipeBody.getConditionalDownLoadFlag());

                //-------------------
                // Force Down Load
                //-------------------
                boolean downLoadFlag = false;
                if (recipeBody.getForceDownLoadFlag()) {
                    log.info("downLoadFlag turns to True.");
                    downLoadFlag = true;
                } else {
                    if (recipeBody.getRecipeBodyConfirmFlag()) {
                        //---------------------
                        // Recipe Confirmation
                        //---------------------
                        Params.RecipeCompareReqParams params = new Params.RecipeCompareReqParams();
                        params.setEquipmentID(equipmentID);
                        params.setMachineRecipeID(recipeBody.getMachineRecipeId());
                        params.setPhysicalRecipeID(recipeBody.getPhysicalRecipeId());
                        params.setFileLocation(recipeBody.getFileLocation());
                        params.setFileName(recipeBody.getFileName());
                        params.setFormatFlag(false);
                        try {
                            recipeService.sxRecipeCompareReq(objCommon, params);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(e.getCode(), retCodeConfigEx.getTcsMMTapPPConfirmError())) {
                                if (recipeBody.getConditionalDownLoadFlag()) {
                                    //--------------------------
                                    // Conditional Down Load
                                    //--------------------------
                                    log.info("downLoadFlag turns to True.");
                                    downLoadFlag = true;
                                } else {
                                    //Recipe Body Confirmation error. the Recipe Body differs between Uploaded it to system and the owned it by equipment.
                                    log.error("Recipe Body Confirmation error. the Recipe Body differs between Uploaded it to system and the owned it by equipment.");
                                    throw new ServiceException(retCodeConfigEx.getRecipeConfirmError());
                                }
                            } else {
                                throw e;
                            }
                        }


                    } else {
                        log.info("Recipe Body management .. no action.");
                        // no action
                    }
                }

                log.info(" Recipe Down Load : " + downLoadFlag);
                if (downLoadFlag) {
                    //---------------------
                    // Recipe Down Load
                    //---------------------
                    Params.RecipeDownloadReqParams recipeDownloadReqParams = new Params.RecipeDownloadReqParams();
                    recipeDownloadReqParams.setEquipmentID(equipmentID);
                    recipeDownloadReqParams.setMachineRecipeID(recipeBody.getMachineRecipeId());
                    recipeDownloadReqParams.setPhysicalRecipeID(recipeBody.getPhysicalRecipeId());
                    recipeDownloadReqParams.setFileLocation(recipeBody.getFileLocation());
                    recipeDownloadReqParams.setFileName(recipeDownloadReqParams.getFileName());
                    recipeDownloadReqParams.setFormatFlag(false);
                    recipeService.sxRecipeDownloadReq(objCommon, recipeDownloadReqParams);
                }
            }
        }

        /*---------------------------------------------------------------------------*/
        /*                                                                           */
        /*   Check Category for Copper/Non Copper                                    */
        /*                                                                           */
        /*   It is checked in the following method whether it is the condition       */
        /*   that Lot of the object is made of OpeStart.                             */
        /*                                                                           */
        /*   1. It is checked whether CassetteCategory of RequiredCassetteCategory   */
        /*      of PosLot and PosCassette is the same.                               */
        /*                                                                           */
        /*   2. It is checked whether CassetteCategoryCapability of CassetteCategory */
        /*      of PosCassette and PosPortResource is the same.                      */
        /*                                                                           */
        /*   3. It is proper condition if CassetteCategoryCapability is the same     */
        /*      as RequiredCassetteCategory and CassetteCategory.                    */
        /*                                                                           */
        /*---------------------------------------------------------------------------*/
        Optional.of(tmpStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
            if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon, null, startCassette.getCassetteID(), equipmentID, startCassette.getLoadPortID());
            } else {
                Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(list -> list.forEach(data -> {
                    lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon, data.getLotID(), startCassette.getCassetteID(), equipmentID, startCassette.getLoadPortID());
                }));
            }
        }));

        /*---------------------------------------------------------------------------*/
        /*   Check Carrier Type for next operation of Empty carrier.             */
        /*---------------------------------------------------------------------------*/
        cassetteMethod.emptyCassetteCheckCategoryForOperation(objCommon, tmpStartCassette);

        /*----------------------------------------*/
        /*   Check Condition for SLM.             */
        /*----------------------------------------*/
        // SLM_CheckConditionForOperation
        slmMethod.slmCheckConditionForOperation(objCommon, equipmentID, portGroupID, null,
                tmpStartCassette, strMtrlOutSpecSeq, BizConstant.SP_OPERATION_STARTRESERVATIONFORSLM);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process                                                        */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        /*----------------------------------------------*/
        /*   Change Cassette's Dispatch State to TRUE   */
        /*----------------------------------------------*/
        Optional.of(tmpStartCassette).ifPresent(list -> list.forEach(data -> {
            cassetteMethod.cassetteDispatchStateChange(objCommon, data.getCassetteID(), true);
        }));

        /*-------------------------------------------------------------*/
        /*                                                             */
        /*   Create Control Job and Assign to Each Cassettes / Lots    */
        /*                                                             */
        /*   - Create new controlJob                                   */
        /*   - Set created controlJobID to each cassettes / lots       */
        /*   - Set created controlJobID to equipment                   */
        /*                                                             */
        /*-------------------------------------------------------------*/
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_CREATE);
        cjStatusChangeReqParams.setClaimMemo(slmStartLotsReservationReqInParams.getOpeMemo());

        Infos.ControlJobCreateRequest controlJobCreateRequest = new Infos.ControlJobCreateRequest();
        controlJobCreateRequest.setEquipmentID(equipmentID);
        controlJobCreateRequest.setPortGroup(portGroupID);
        controlJobCreateRequest.setStartCassetteList(tmpStartCassette);
        cjStatusChangeReqParams.setControlJobCreateRequest(controlJobCreateRequest);

        Results.CJStatusChangeReqResult cjStatusChangeReqResult = controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        /*----------------------------------------------------*/
        /*                                                    */
        /*   Set Start Reserved Control Job into Equipment    */
        /*                                                    */
        /*----------------------------------------------------*/
        equipmentMethod.equipmentReservedControlJobIDSet(objCommon, equipmentID, cjStatusChangeReqResult.getControlJobID());

        /*----------------------------------------------------*/
        /*                                                    */
        /*   Get Start Information for each Cassette / Lot    */
        /*                                                    */
        /*   - strStartCassette information is not filled     */
        /*     perfectlly. By this object function, it will   */
        /*     be filled.                                     */
        /*                                                    */
        /*----------------------------------------------------*/
        Outputs.ObjProcessStartReserveInformationGetOut processStartReserveInformationGetOut = processMethod.processStartReserveInformationGet(objCommon, tmpStartCassette, equipmentID, false);

        String fpcFlagStr = StandardProperties.OM_DOC_ENABLE_FLAG.getValue();
        if (CimStringUtils.isEmpty(fpcFlagStr)) {
            throw new ServiceException("Cannot found the Environment[SP_FPC_ADAPTATION_FLAG].");
        }
        int tmpFPCAdoptFlag = Integer.parseInt(fpcFlagStr);
        if (tmpFPCAdoptFlag == 1) {
            List<Infos.StartCassette> exchangeFPCStartCassetteInfoExchangeOut = fpcMethod.fpcStartCassetteInfoExchange(objCommon,
                    BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEREQ,
                    equipmentID,
                    processStartReserveInformationGetOut.getStartCassetteList());
            processStartReserveInformationGetOut.setStartCassetteList(exchangeFPCStartCassetteInfoExchangeOut);
        } else {
            fpcMethod.fpcStartCassetteProcessConditionCheck(objCommon, equipmentID,
                    processStartReserveInformationGetOut.getStartCassetteList(), false, true);
        }

        /*----------------------------------------------------*/
        /*                                                    */
        /*   Set Start Reservation Info to Each Lots' PO      */
        /*                                                    */
        /*   - Set created controlJobID into each cassette.   */
        /*   - Set created controlJobID into each lot.        */
        /*   - Set control job info (StartRecipe, DCDefs,     */
        /*     DCSpecs, Parameters, ...) into each lot's      */
        /*     cunrrent PO.                                   */
        /*                                                    */
        /*----------------------------------------------------*/
        // process_startReserveInformation_Set__090
        processMethod.processStartReserveInformationSet(objCommon,
                equipmentID,
                portGroupID,
                cjStatusChangeReqResult.getControlJobID(),
                processStartReserveInformationGetOut.getStartCassetteList(),
                false);

        /*---------------------------------------------*/
        /*   Write Equipment Container Position Info   */
        /*---------------------------------------------*/
        // equipmentContainerPosition_reservation_Create
        Inputs.ObjEquipmentContainerPositionReservationCreateIn reservationCreateIn = new Inputs.ObjEquipmentContainerPositionReservationCreateIn();
        reservationCreateIn.setContainerPositionIDs(containerPositions);
        reservationCreateIn.setEquipmentID(equipmentID);
        reservationCreateIn.setControlJobID(cjStatusChangeReqResult.getControlJobID());
        reservationCreateIn.setStrStartCassette(processStartReserveInformationGetOut.getStartCassetteList());
        reservationCreateIn.setStrMtrlOutSpecSeq(strMtrlOutSpecSeq);
        log.info("call equipmentContainerPosition_reservation_Create()");
        equipmentContainerPositionMethod.equipmentContainerPositionReservationCreate(objCommon, reservationCreateIn);

        //-----------------------------------------------------------//
        //  Wafer Stacking Operation                                 //
        //    If Equipment Category is SP_Mc_Category_WaferBonding,  //
        //    update Bonding Group Information                       //
        //-----------------------------------------------------------//
        // lot_bondingGroup_UpdateByOperation
        lotMethod.lotBondingGroupUpdateByOperation(objCommon,
                equipmentID,
                cjStatusChangeReqResult.getControlJobID(),
                processStartReserveInformationGetOut.getStartCassetteList(),
                BizConstant.SP_OPERATION_STARTRESERVATIONFORSLM);

        /*-------------------------------------------*/
        /*                                           */
        /*   Send TxStartLotsReservationReq to TCS   */
        /*                                           */
        /*-------------------------------------------*/

        /*----------------------------------------*/
        /*   Send Start Lots Reservation to TCS   */
        /*----------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue = CimStringUtils.isEmpty(tmpSleepTimeValue) ? BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : Long.parseLong(tmpSleepTimeValue);
        long retryCountValue = CimStringUtils.isEmpty(tmpRetryCountValue) ? BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : Long.parseLong(tmpRetryCountValue);

        log.info("env value of OM_EAP_CONNECT_SLEEP_TIME  = " + sleepTimeValue);
        log.info("env value of OM_EAP_CONNECT_RETRY_COUNT = " + retryCountValue);

        for (long i = 0; i < (retryCountValue + 1); i++) {
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            // todo: TCSMgr_SendSLMStartLotsReservationReq
        }

        /*-------------------------------------------------*/
        /*   call APCRuntimeCapability_RegistDR            */
        /*-------------------------------------------------*/
        // todo: APCRuntimeCapability_RegistDR

        /*-------------------------------------------------*/
        /*   call cassette_APCInformation_GetDR            */
        /*-------------------------------------------------*/
        // todo: cassette_APCInformation_GetDR

        /*-------------------------------------------------*/
        /*   call APCMgr_SendControlJobInformationDR       */
        /*-------------------------------------------------*/
        // todo: APCMgr_SendControlJobInformationDR


        /*--------------------*/
        /*   Return to Main   */
        /*--------------------*/
        return cjStatusChangeReqResult.getControlJobID();
    }

    @Override
    public Results.SLMWaferRetrieveCassetteReserveReqResult sxSLMWaferRetrieveCassetteReserveReq(Infos.ObjCommon objCommon, Params.SLMWaferRetrieveCassetteReserveReqInParams slmWaferRetrieveCassetteReserveReqInParams){

        //---------------------------------------------------------//
        // Put Input parameter                                     //
        //---------------------------------------------------------//
        ObjectIdentifier dummy = new ObjectIdentifier();
        ObjectIdentifier IN_cassetteID = slmWaferRetrieveCassetteReserveReqInParams.getCassetteID();
        ObjectIdentifier IN_controlJobID = slmWaferRetrieveCassetteReserveReqInParams.getControlJobID();
        ObjectIdentifier IN_destPortID = slmWaferRetrieveCassetteReserveReqInParams.getDestPortID();
        ObjectIdentifier IN_equipmentID = slmWaferRetrieveCassetteReserveReqInParams.getEquipmentID();
        ObjectIdentifier IN_lotID = slmWaferRetrieveCassetteReserveReqInParams.getLotID();
        String IN_actionCode = slmWaferRetrieveCassetteReserveReqInParams.getActionCode();
        List<Infos.MtrlOutSpec> IN_strMtrlOutSpecSeq = slmWaferRetrieveCassetteReserveReqInParams.getMtrlOutSpecList();

        List<Infos.SlmSlotMap> IN_srcMapSeq = new ArrayList<>();
        List<Infos.SlmSlotMap> IN_dstMapSeq = new ArrayList<>();
        for (Infos.MtrlOutSpec mtrlOutSpec : IN_strMtrlOutSpecSeq){
            //Source Cassette
            List<Infos.SlmSlotMap> strSourceMapSeq = mtrlOutSpec.getSourceMapList();
            IN_srcMapSeq.addAll(strSourceMapSeq);
            //Destination Cassette
            List<Infos.SlmSlotMap> strDestinationMapSeq = mtrlOutSpec.getDestinationMapList();
            IN_dstMapSeq.addAll(strDestinationMapSeq);
        }

        //--------------------------------------------------------//
        // Object Lock                                            //
        //--------------------------------------------------------//
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(IN_equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.FMC_WAFER_RETRIEVE_CARRIER_RESERVE_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        log.info("lockMode : ",lockMode);
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Advanced Mode
            // Lock Equipment Main Object
            List<String> dummySeq = new ArrayList<>();
            Inputs.ObjAdvanceLockIn strAdvanced_object_Lock_in = new Inputs.ObjAdvanceLockIn();
            strAdvanced_object_Lock_in.setObjectID(IN_equipmentID);
            strAdvanced_object_Lock_in.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvanced_object_Lock_in.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvanced_object_Lock_in.setKeyList(dummySeq);
            objectLockMethod.advancedObjectLock(objCommon,strAdvanced_object_Lock_in);
        }else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //-------------------------------//
            //   Object Lock for Equipment   //
            //-------------------------------//
            objectLockMethod.objectLock(objCommon, CimMachine.class, IN_equipmentID);
        }
        //----------------------------------------//
        //   Object Lock for Equipment Container  //
        //----------------------------------------//
        log.info(" # Call object_LockForEquipmentResource (All Equipment Container)");
        try {
            objectLockMethod.objectLockForEquipmentResource(objCommon,IN_equipmentID,dummy,BizConstant.SP_CLASSNAME_POSMACHINECONTAINER);
        } catch (ServiceException e) {
            if (e.getCode() == 923){
                //OK,do nothing
            }
        }

        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            log.info("IN_actionCode is CassetteReserve");
            //-----------------------------------------------------------//
            //   Object Lock for Source/Dest Cassette in MaterialOutSpec //
            //-----------------------------------------------------------//
            if (!CimObjectUtils.isEmpty(IN_strMtrlOutSpecSeq)){
                log.info("Object Lock source Cassettes and destination Cassettes");
                for (Infos.MtrlOutSpec mtrlOutSpec : IN_strMtrlOutSpecSeq){
                    List<Infos.SlmSlotMap> strSourceMapSeq = mtrlOutSpec.getSourceMapList();
                    for (Infos.SlmSlotMap slmSlotMap : strSourceMapSeq){
                        log.info("Object Lock (Src-Cassette)",slmSlotMap.getCassetteID().getValue());
                        objectLockMethod.objectLock(objCommon, CimCassette.class, slmSlotMap.getCassetteID());
                    }
                    List<Infos.SlmSlotMap> strDestinationMapSeq = mtrlOutSpec.getDestinationMapList();
                    for (Infos.SlmSlotMap slmSlotMap : strDestinationMapSeq){
                        log.info("Object Lock (Dest-Cassette)",slmSlotMap.getCassetteID().getValue());
                        objectLockMethod.objectLock(objCommon, CimCassette.class, slmSlotMap.getCassetteID());
                    }
                }
            }
            if (!ObjectIdentifier.isEmpty(IN_controlJobID)){
                log.info("Object Lock previous destination Cassettes");
                Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentMethod.equipmentContainerPositionInfoGet(objCommon, IN_equipmentID, IN_controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
                List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerPositionInfo.getEqpContainerPositionList();
                for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositionList){
                    if (!ObjectIdentifier.isEmpty(eqpContainerPosition.getDestCassetteID())){
                        log.info("Object Lock (Previous-Dest-Cassette)");
                        objectLockMethod.objectLock(objCommon, CimCassette.class, eqpContainerPosition.getDestCassetteID());
                    }
                }
            }
        }

        //---------------------------------------------------------//
        // Check Logic                                             //
        //---------------------------------------------------------//
        //-----------------------------------------
        // Inpara wafers duplication check
        //-----------------------------------------
        log.info("Check : Inpara wafers duplication");
        int IN_dstMapLen = IN_dstMapSeq.size();
        for (int i = 0; i < IN_dstMapLen; i++){
            for (int j = i+1; j < IN_dstMapLen; j++){
                if (ObjectIdentifier.equalsWithValue(IN_dstMapSeq.get(i).getWaferID(), IN_dstMapSeq.get(j).getWaferID())){
                    log.info(" ###!!!!! Error Because Duplicate between Inpara wafers.");
                    throw new ServiceException(retCodeConfigEx.getSlmInvalidParameterForWaferDuplicate());
                }
            }
        }

        //----------------------------------------------//
        // Action Code                                  //
        //  SP_SLM_ActionCode_CassetteReserve           //
        //  SP_SLM_ActionCode_PortReserve               //
        //  SP_SLM_ActionCode_PortReserveCancel         //
        //----------------------------------------------//
        log.info("# Check : Inpara Action Code",IN_actionCode);
        if (!CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)
                &&  !CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)
                &&  !CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVECANCEL)){
            log.info(" # !!!!! Error Because the Action Code is invalid.", IN_actionCode);
            throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidSlmActionCode(),IN_actionCode));
        }
        //----------------------------------------------//
        // Check input parameters                       //
        //----------------------------------------------//
        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)){
            if (ObjectIdentifier.isEmpty(IN_cassetteID) || ObjectIdentifier.isEmpty(IN_destPortID)){
                log.info(" # return RC_SLM_INVALID_PARAMETER_FOR_PORTRSV");
                throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidParameterForPortrsv(),IN_cassetteID.getValue(),IN_destPortID.getValue(),IN_controlJobID.getValue()));
            }
        }else if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVECANCEL)){
            if (ObjectIdentifier.isEmpty(IN_cassetteID) || !ObjectIdentifier.isEmpty(IN_destPortID)){
                log.info(" # return RC_SLM_INVALID_PARAMETER_FOR_PORTRSV");
                throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidParameterForPortrsv(),IN_cassetteID.getValue(),IN_destPortID.getValue(),IN_controlJobID.getValue()));
            }
        }

        List<Infos.EqpContainerPosition> CHG_strEqpContainerPositionSeq = new ArrayList<>();
        List<Infos.EqpContainer> NOCHG_strEqpContainerSeq = new ArrayList<>();

        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            //---------------------------------
            // Action Code  CassetteReserve
            //---------------------------------
            log.info("# Action Code is CassetteReserve");
            // cassetteID and destPortID are blank?
            if (!ObjectIdentifier.isEmpty(IN_cassetteID) || !ObjectIdentifier.isEmpty(IN_destPortID)){
                log.info(" # !!!!! Error Because cassetteID or destPortID are specified for ActionCode CassetteReserve.");
                throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidParameterForPortrsv(),IN_cassetteID.getValue(),IN_destPortID.getValue(),IN_controlJobID.getValue()));
            }
            //---------------------------------------------------
            //if lotID is blank , MtrlOutSpec is specified?
            //--------------------------------------------------
            if (ObjectIdentifier.isEmpty(IN_lotID) && CimObjectUtils.isEmpty(IN_strMtrlOutSpecSeq)){
                log.info(" # !!!!! Error Because lotID is blank but MtrlOutSpec is not specified for ActionCode CassetteReserve.");
                throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidParameterForPortrsv(),dummy.getValue(),dummy.getValue(),IN_controlJobID.getValue()));
            }
        }
        //----------------------------------------------
        //   Get equipment BR Infromation
        //----------------------------------------------
        log.info("# Get Equipment BR Information");
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, IN_equipmentID);

        //----------------------------------------------
        // The Equipment's SLM Capability is TRUE ?
        //----------------------------------------------
        log.info("# Check : The equipment's SLM Capability is TRUE ?");
        if (!eqpBrInfo.isFmcCapabilityFlag()){
            log.info(" # !!!!! Error Because the equipment's SLM Capability is False.");
            throw new ServiceException(new OmCode(retCodeConfigEx.getEqpSlmCapabilityOff(),IN_equipmentID.getValue()));
        }

        //------------------------------------------------
        // Get EqpContainerPosition Seq for object lock
        //------------------------------------------------
        List<Infos.EqpContainerPosition> Lock_strEqpContainerPositionSeq = new ArrayList<>();
        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            //---------------------------------------------------------
            // Get wafer sequence for getting EqpContainerPositions
            //---------------------------------------------------------
            List<ObjectIdentifier> Locked_waferSeq = new ArrayList<>();
            if (!ObjectIdentifier.isEmpty(IN_lotID)){
                log.info(" # lotID specified case. Lock EqpContainerPosition Lot's wafers are in.");
                Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
                objLotWaferIDListGetDRIn.setLotID(IN_lotID);
                objLotWaferIDListGetDRIn.setScrapCheckFlag(false);
                List<ObjectIdentifier> waferIDList = lotMethod.lotWaferIDListGetDR(objCommon, objLotWaferIDListGetDRIn);
                for (ObjectIdentifier waferID : waferIDList){
                    log.info(" ## Locked_wafer",waferID.getValue());
                    Locked_waferSeq.add(waferID);
                }
            }else {
                log.info(" # lotID not specified case.");
                if (IN_srcMapSeq.size() != IN_dstMapSeq.size()){
                    log.info(" # Destination cassette cancel exist. Lock EqpContainerPosition where wafers in IN_srcMap are included.");
                    for (Infos.SlmSlotMap slmSlotMap : IN_srcMapSeq){
                        Locked_waferSeq.add(slmSlotMap.getWaferID());
                    }
                }else {
                    log.info(" # No destination cassette cancel exist. Lock EqpContainerPosition where wafers in IN_dstMap are.");
                    for (Infos.SlmSlotMap slmSlotMap : IN_dstMapSeq){
                        Locked_waferSeq.add(slmSlotMap.getWaferID());
                    }
                }
            }
            //---------------------------------------------------------
            //   Get EqpContainerPositions from Locked_waferSeq
            //---------------------------------------------------------
            for (ObjectIdentifier Locked_waferID : Locked_waferSeq){
                Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn = new Inputs.ObjEquipmentContainerPositionInfoGetIn();
                positionInfoGetIn.setEquipmentID(IN_equipmentID);
                positionInfoGetIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_WAFER);
                positionInfoGetIn.setKey(Locked_waferID);
                Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, positionInfoGetIn);
                Infos.EqpContainerPosition eqpContainerPosition = null;
                if (!CimObjectUtils.isEmpty(eqpContainerPositionInfo.getEqpContainerPositionList())) {
                    eqpContainerPosition = eqpContainerPositionInfo.getEqpContainerPositionList().get(0);
                    Lock_strEqpContainerPositionSeq.add(eqpContainerPosition);
                }
            }
        }else {
            log.info(" # IN_actionCode is PortReserve or portReserveCancel.");
            //---------------------------------------------------------
            //   Get EqpContainerPositions from IN_cassetteID
            //---------------------------------------------------------
            log.info(" # Get Equipment Container Information.");
            Lock_strEqpContainerPositionSeq = equipmentContainerPositionMethod.equipmentContainerPositionInfoGetByDestCassette(objCommon, IN_equipmentID, IN_cassetteID);
        }

        for (Infos.EqpContainerPosition eqpContainerPosition : Lock_strEqpContainerPositionSeq){
            //-------------------------------------------------------
            //   Object Lock for EqpContainerPosition (First time)
            //-------------------------------------------------------
            Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
            objObjectLockForEquipmentContainerPositionIn.setControlJobID(IN_controlJobID);
            objObjectLockForEquipmentContainerPositionIn.setEquipmentID(IN_equipmentID);
            objectLockMethod.objectLockForEquipmentContainerPosition(objCommon,objObjectLockForEquipmentContainerPositionIn);
            //---------------------------------------------------
            //   Object Lock for Previous destination cassette
            //---------------------------------------------------
            if (!ObjectIdentifier.isEmpty(eqpContainerPosition.getDestCassetteID())){
                log.info(" ## Object Lock (Previous-Dest-Cassette)",eqpContainerPosition.getDestCassetteID());
                objectLockMethod.objectLock(objCommon, CimCassette.class, eqpContainerPosition.getDestCassetteID());
            }
            if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
                //--------------------------------------------------
                //   Object Lock for source cassette
                //--------------------------------------------------
                if (!ObjectIdentifier.isEmpty(eqpContainerPosition.getDestCassetteID())){
                    log.info(" ## Object Lock (Previous-Dest-Cassette)",eqpContainerPosition.getDestCassetteID());
                    objectLockMethod.objectLock(objCommon, CimCassette.class, eqpContainerPosition.getSrcCassetteID());
                }
            }
        }

        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            log.info(" # IN_actionCode is CassetteReserve");
            //----------------------------------//
            // Check Condition                  //
            // Action Code  CassetteReserve     //
            //--------------------------------- //
            Results.SLMCheckConditionForCassetteReserveResult slmCheckConditionForCassetteReserveResult =
                    slmMethod.slmCheckConditionForCassetteReserve(objCommon, IN_equipmentID, IN_lotID, IN_controlJobID, IN_dstMapSeq, IN_srcMapSeq);
            //The change information of the specified equipment's Container Information.
            CHG_strEqpContainerPositionSeq = slmCheckConditionForCassetteReserveResult.getStrEqpContainerPositionInfo().getEqpContainerPositionList();
            //The other  information of the specified equipment's Container Information.
            NOCHG_strEqpContainerSeq = slmCheckConditionForCassetteReserveResult.getStrEqpContainerInfo().getEqpContainerList();
        }else if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)
                || CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVECANCEL)){
            //---------------------------------
            // Action Code  PortReserve
            // Action Code  PortReserveCancel
            //---------------------------------
            log.info("# Action Code is PortReserve/PortReserveCancel.");
            //----------------------------------//
            // Check Condition                  //
            // Action Code  PortReserve         //
            // Action Code  PortReserveCancel   //
            //--------------------------------- //
            Results.SLMCheckConditionForPortReserveResult slmCheckConditionForPortReserveResult =
                    slmMethod.slmCheckConditionForPortReserve(objCommon, IN_actionCode, IN_equipmentID, IN_cassetteID, IN_destPortID);
            //The change information of the specified equipment's Container Information.
            CHG_strEqpContainerPositionSeq = slmCheckConditionForPortReserveResult.getStrEqpContainerPositionInfo().getEqpContainerPositionList();
            NOCHG_strEqpContainerSeq = slmCheckConditionForPortReserveResult.getStrEqpContainerInfo().getEqpContainerList();
        }
        //----------------------------------------------------------------------------------
        //   Object Lock Container Position (Second time). CHG_strEqpContainerPositionSeq
        //----------------------------------------------------------------------------------
        log.info("# Object Lock Container Position");
        for (Infos.EqpContainerPosition eqpContainerPosition : CHG_strEqpContainerPositionSeq){
            Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
            objObjectLockForEquipmentContainerPositionIn.setWaferID(eqpContainerPosition.getWaferID());
            objObjectLockForEquipmentContainerPositionIn.setEquipmentID(IN_equipmentID);
            objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, objObjectLockForEquipmentContainerPositionIn);
        }
        //-----------------------------------------------------------//
        // Preparing MtrlOutSpec for Combination Check               //
        //-----------------------------------------------------------//
        List<ObjectIdentifier> PRV_destCastIDs =new ArrayList<>();
        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            if (!ObjectIdentifier.isEmpty(IN_lotID)){
                //------------------------------------------------------
                //Pickup Previous destination cassette.  (no duplicate)
                //------------------------------------------------------
                List<ObjectIdentifier> PRVTMP_destCastIDs =new ArrayList<>();
                for (Infos.EqpContainerPosition eqpContainerPosition : CHG_strEqpContainerPositionSeq){
                    //Previous destination cassette for SLM Reserve cancel.
                    Infos.EqpContainerPositionInfo strEquipmentContainerPosition_info_Get_out = equipmentMethod.equipmentContainerPositionInfoGet(objCommon, IN_equipmentID,
                            eqpContainerPosition.getWaferID(), BizConstant.SP_SLM_KEYCATEGORY_WAFER);
                    List<Infos.EqpContainerPosition> eqpContPosSeq = strEquipmentContainerPosition_info_Get_out.getEqpContainerPositionList();
                    if (CimObjectUtils.isEmpty(eqpContPosSeq)){
                        log.info("The wafer isn't found in machine container.");
                        throw new ServiceException(new OmCode(retCodeConfig.getSlmWaferNotFoundInPosition(),eqpContainerPosition.getWaferID().getValue()));
                    }
                    if (!ObjectIdentifier.isEmpty(eqpContPosSeq.get(0).getDestCassetteID())){
                        boolean notAddFlag = false;
                        for (ObjectIdentifier PRVTMP_destCastID : PRVTMP_destCastIDs){
                            if (ObjectIdentifier.equalsWithValue(PRVTMP_destCastID, eqpContPosSeq.get(0).getDestCassetteID())){
                                log.info(" ### Cassette is found in PRVTMP_destCastIDs.");
                                notAddFlag = true;
                                break;
                            }
                        }
                        if (!notAddFlag){
                            log.info(" ## Add cassette to PRVTMP_destCastIDs.");
                            PRVTMP_destCastIDs.add(eqpContPosSeq.get(0).getDestCassetteID());
                        }
                    }
                }
                //-------------------------------------------------------//
                // SEARCH  Destination cassette !                        //
                //-------------------------------------------------------//
                //----------------------------------------------------------
                // Inquire to RTD returns Candidate cassettes.
                //----------------------------------------------------------
                Params.SLMCandidateCassetteForRetrievingInqInParams retrievingInqInParams = new Params.SLMCandidateCassetteForRetrievingInqInParams();
                retrievingInqInParams.setEquipmentID(IN_equipmentID);
                retrievingInqInParams.setLotID(IN_lotID);
                retrievingInqInParams.setPortID(IN_destPortID);
                Results.SLMCandidateCassetteForRetrievingInqResult retrievingInqResult = fmcInqService.sxSLMCandidateCassetteForRetrievingInq(objCommon,retrievingInqInParams);
                boolean destCastFoundFlag = false;
                boolean searchFlag =false;
                List<ObjectIdentifier> RTD_castIDs = retrievingInqResult.getCassetteIDs();
                int RTD_castIDsLen = RTD_castIDs.size();
                int RTD_castCnt = 0;
                while (!searchFlag){
                    ObjectIdentifier CHG_destCastID =new ObjectIdentifier();
                    if (RTD_castCnt < RTD_castIDsLen){
                        log.info("## Check : Cassettes which return from RTD are suitable for destination cassette.");
                        //RTD returns some empty cassettes.
                        CHG_destCastID = RTD_castIDs.get(RTD_castCnt);
                        RTD_castCnt++;
                    }else {
                        //RTD empty cassettes are not usefull for destination cassette.
                        //----------------------------------
                        // Search from Stocker all around.
                        //----------------------------------
                        ObjectIdentifier cassetteID = cassetteMethod.cassetteEmptyCassetteForRetrievingGet(objCommon, IN_equipmentID, IN_lotID);
                        if (ObjectIdentifier.isEmpty(cassetteID)){
                            log.info(" ## !!!!! Error Because destination cassette is not found.");
                            throw new ServiceException(retCodeConfigEx.getNotEnoughEmptycastFromAllaround());
                        }
                        log.info("## Check : Cassette which is picked up from all around aren't suitable for destination cassette.\"");
                        CHG_destCastID = cassetteID;
                        searchFlag = true;
                    }
                    //--------------------------------------------------------
                    //   Object Lock New Target Destination cassette
                    //--------------------------------------------------------
                    if (!ObjectIdentifier.isEmpty(CHG_destCastID)){
                        objectLockMethod.objectLock(objCommon, CimCassette.class, CHG_destCastID);
                    }
                    //--------------------------------------------------------
                    // Cassette is suitable for destination cassette ???
                    //--------------------------------------------------------
                    try {
                        cassetteMethod.cassetteCheckConditionForSLMDestCassette(objCommon, IN_equipmentID, IN_controlJobID, CHG_destCastID, IN_lotID);
                    } catch (ServiceException e) {
                        if (e.getCode()==retCodeConfigEx.getSlmDstcastReservedAnotherCtrljob().getCode()
                                ||  e.getCode()==retCodeConfig.getAlreadyReservedCassetteSlm().getCode()
                                ||  e.getCode()==retCodeConfigEx.getExistSorterjobForCassette().getCode()
                                ||  e.getCode()==retCodeConfig.getCassetteInPostProcess().getCode()
                                ||  e.getCode()==retCodeConfig.getInvalidCassetteState().getCode()
                                ||  e.getCode()==retCodeConfig.getAlreadyDispatchReservedCassette().getCode()
                                ||  e.getCode()==retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest().getCode()
                                ||  e.getCode()==retCodeConfigEx.getMismatchDestCastCategory().getCode()){
                            log.info(" ## Cannot use the cassette. next cassette...");
                            continue;
                        }else {
                            throw e;
                        }
                    }
                    //------------------------------
                    // Get Wafer Map in Cassette
                    //------------------------------
                    List<Infos.WaferMapInCassetteInfo> castSlotMap = cassetteMethod.cassetteGetWaferMapDR(objCommon, CHG_destCastID);
                    //Count Empty Slot
                    int emptySlotCnt = 0;
                    for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo : castSlotMap){
                        if (ObjectIdentifier.isEmpty(waferMapInCassetteInfo.getWaferID())){
                            log.info(" ## Slot is empty.");
                            emptySlotCnt++;
                        }
                    }
                    //-----------------------------------------------------------------------------------
                    //   Get EqpContianerPosition Information whose destCassetteID is CHG_destCastID.
                    //-----------------------------------------------------------------------------------
                    List<Infos.EqpContainerPosition> castCtnPosSeq = equipmentContainerPositionMethod.equipmentContainerPositionInfoGetByDestCassette(objCommon, IN_equipmentID, CHG_destCastID);
                    //Count Retrieved wafer
                    int retrievedCnt = 0;
                    for (Infos.EqpContainerPosition castCtnPos : castCtnPosSeq){
                        for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo : castSlotMap){
                            if (ObjectIdentifier.equalsWithValue(castCtnPos.getWaferID(), waferMapInCassetteInfo.getWaferID())){
                                log.info(" #### Wafer was retrieved.");
                                retrievedCnt++;
                            }
                        }
                    }
                    //-----------------------------------------------------------------------------------
                    //   Calculate enable slot count.
                    //       enableCount = emptySlotCount - ( SLMReservedCount - retrievedCount )
                    //-----------------------------------------------------------------------------------
                    int enableCount = emptySlotCnt - (castCtnPosSeq.size() - retrievedCnt);
                    if (CHG_strEqpContainerPositionSeq.size() > enableCount){
                        log.info(" ## !!!!! Error Because cannot assign SlotNumber of the cassette.");
                        continue;
                    }
                    //-----------------------------
                    // Set Destination Cassette
                    // Set Destination Slot Map
                    // Set Port
                    //-----------------------------
                    int slotNo =1;
                    ObjectIdentifier tmpPort = new ObjectIdentifier();
                    for(Infos.EqpContainerPosition eqpContainerPosition : CHG_strEqpContainerPositionSeq){
                        if (ObjectIdentifier.equalsWithValue(CHG_destCastID, eqpContainerPosition.getDestCassetteID())){
                            log.info("### Get the same cassette's PortID");
                            tmpPort = eqpContainerPosition.getDestPortID();
                        }
                        eqpContainerPosition.setDestCassetteID(CHG_destCastID);
                        eqpContainerPosition.setDestPortID(tmpPort);
                        boolean slotFoundFlag =false;
                        while (!slotFoundFlag && slotNo <= castSlotMap.size()){
                            slotFoundFlag = true;
                            //-----------------------------------------------------
                            // the slot in destination cassette should be empty
                            //-----------------------------------------------------
                            for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo : castSlotMap){
                                if (slotNo == waferMapInCassetteInfo.getSlotNumber()){
                                    log.info("##### Wafer already exist in this slot.");
                                    slotFoundFlag = false;
                                }else {
                                    log.info("##### Slot is blank. OK");
                                    break;
                                }
                            }
                            if (slotFoundFlag){
                                //-----------------------------------------------------
                                // the slot in destination cassette should be reserved
                                //-----------------------------------------------------
                                for (Infos.EqpContainerPosition castCtnPos : castCtnPosSeq){
                                    if (slotNo == castCtnPos.getDestSlotNo()){
                                        log.info("##### This slot is SLM reserved.");
                                        slotFoundFlag =false;
                                        break;
                                    }
                                }
                            }
                            if (!slotFoundFlag){
                                log.info("#### Slot is unabled.");
                            }else {
                                log.info("#### Slot is enabled.");
                                eqpContainerPosition.setDestSlotNo(slotNo);
                            }
                            slotNo++;
                        }
                        if (!slotFoundFlag){
                            log.info(" ### Cannot use this cassette. next cassette...");
                            continue;
                        }
                    }
                    //------------------------------------------------------------
                    // Check combination Source Map and Destination Map
                    //------------------------------------------------------------
                    Infos.EqpContainerPositionInfo strSLM_materialOutSpec_CombinationCheck_in = new Infos.EqpContainerPositionInfo();
                    strSLM_materialOutSpec_CombinationCheck_in.setEquipmentID(IN_equipmentID);
                    strSLM_materialOutSpec_CombinationCheck_in.setEqpContainerPositionList(CHG_strEqpContainerPositionSeq);
                    try {
                        slmMethod.slmMaterialOutSpecCombinationCheck(objCommon,strSLM_materialOutSpec_CombinationCheck_in);
                    } catch (ServiceException e) {
                        if (e.getCode() == retCodeConfigEx.getMtrlOutSpecCombinationError().getCode()){
                            log.info("##### Cannot use the cassette. next cassette...");
                            continue;
                        }
                    }
                    destCastFoundFlag = true;
                    break;
                }
                if (!destCastFoundFlag){
                    log.info(" ## !!!!! Error Because destination cassette is not found.");
                    throw new ServiceException(retCodeConfigEx.getNotEnoughEmptycastFromAllaround());
                }
                //----------------------------------------------
                // Check Previous destination Cassette
                //----------------------------------------------
                for (ObjectIdentifier PRVTMP_destCastID : PRVTMP_destCastIDs){
                    if (!ObjectIdentifier.isEmpty(PRVTMP_destCastID)){
                        boolean cassetteDispatchState = false;
                        cassetteDispatchState = cassetteMethod.cassetteDispatchStateGet(objCommon, PRVTMP_destCastID);
                        if (cassetteDispatchState){
                            Outputs.CassetteReservationInfoGetDROut objCassette_reservationInfo_GetDR_out = cassetteMethod.cassetteReservationInfoGetDR(objCommon, PRVTMP_destCastID);
                            if (CimStringUtils.equals(objCassette_reservationInfo_GetDR_out.getNPWLoadPurposeType(),BizConstant.SP_LOADPURPOSETYPE_SLMRETRIEVING)){
                                throw new ServiceException(retCodeConfig.getAlreadyDispatchReservedCassette());
                            }
                        }
                    }
                }
                //-----------------------------
                // ReSet Previous Cassettes.
                //-----------------------------
                for (ObjectIdentifier PRVTMP_destCastID : PRVTMP_destCastIDs){
                    boolean preCastFound = false;
                    for (Infos.EqpContainerPosition eqpContainerPosition : CHG_strEqpContainerPositionSeq){
                        if (ObjectIdentifier.equalsWithValue(PRVTMP_destCastID, eqpContainerPosition.getDestCassetteID())){
                            log.info("CassetteID is found in new destination cassette.");
                            preCastFound = true;
                            break;
                        }
                    }
                    if (!preCastFound){
                        PRV_destCastIDs.add(PRVTMP_destCastID);
                    }
                }
            }else if (ObjectIdentifier.isEmpty(IN_lotID)){
                //--------------------------------------------------------
                //Pickup specified destination cassettes. (no duplicate)
                //--------------------------------------------------------
                List<ObjectIdentifier> CHG_destCastIDs = new ArrayList<>();
                for (Infos.EqpContainerPosition eqpContainerPosition : CHG_strEqpContainerPositionSeq){
                    boolean castFoundFlag = false;
                    for (ObjectIdentifier CHG_destCastID : CHG_destCastIDs){
                        if (!ObjectIdentifier.isEmpty(eqpContainerPosition.getDestCassetteID())){
                            if (ObjectIdentifier.equalsWithValue(eqpContainerPosition.getDestCassetteID(), CHG_destCastID)){
                                castFoundFlag = true;
                                break;
                            }
                        }
                    }
                    if (!castFoundFlag){
                        CHG_destCastIDs.add(eqpContainerPosition.getDestCassetteID());
                    }
                }
                //------------------------------------------------------
                //Pickup Previous destination cassette.  (no duplicate)
                //------------------------------------------------------
                List<ObjectIdentifier> PRVTMP_destCastIDs = new ArrayList<>();
                List<ObjectIdentifier> PRVTMP_destPortIDs = new ArrayList<>();
                for (Infos.EqpContainerPosition eqpContainerPosition : CHG_strEqpContainerPositionSeq){
                    //Previous destination cassette for SLM Reserve cancel.
                    Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn = new Inputs.ObjEquipmentContainerPositionInfoGetIn();
                    positionInfoGetIn.setEquipmentID(IN_equipmentID);
                    positionInfoGetIn.setKey(eqpContainerPosition.getWaferID());
                    positionInfoGetIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_WAFER);
                    Infos.EqpContainerPositionInfo strEquipmentContainerPosition_info_Get_out = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, positionInfoGetIn);
                    List<Infos.EqpContainerPosition> eqpContPosSeq = strEquipmentContainerPosition_info_Get_out.getEqpContainerPositionList();
                    if (CimObjectUtils.isEmpty(eqpContPosSeq)){
                        log.info("The wafer isn't found in machine container. ");
                        throw new ServiceException(new OmCode(retCodeConfig.getSlmWaferNotFoundInPosition(),eqpContainerPosition.getWaferID().getValue()));
                    }
                    if (!ObjectIdentifier.isEmpty(eqpContPosSeq.get(0).getDestCassetteID())){
                        boolean preCastFlag = false;
                        for (ObjectIdentifier PRVTMP_destCastID : PRVTMP_destCastIDs){
                            if (ObjectIdentifier.equalsWithValue(PRVTMP_destCastID, eqpContPosSeq.get(0).getDestCassetteID())){
                                preCastFlag = true;
                                break;
                            }
                        }
                        if (!preCastFlag){
                            PRVTMP_destCastIDs.add(eqpContPosSeq.get(0).getDestCassetteID());
                            PRVTMP_destPortIDs.add(eqpContPosSeq.get(0).getDestPortID());
                        }
                    }
                }
                //----------------------------------------------------------------
                //Pickup Inpara(overwritten) destination cassette. (no duplicate)
                //----------------------------------------------------------------
                List<ObjectIdentifier> IN_destCastIDs = new ArrayList<>();
                for (Infos.SlmSlotMap slmSlotMap: IN_dstMapSeq){
                    boolean InCastFlag = false;
                    for (ObjectIdentifier IN_destCastID : IN_destCastIDs){
                        if (ObjectIdentifier.equalsWithValue(IN_destCastID, slmSlotMap.getCassetteID())){
                            InCastFlag = true;
                        }
                    }
                    if (!InCastFlag){
                        IN_destCastIDs.add(slmSlotMap.getCassetteID());
                    }
                }
                for (ObjectIdentifier IN_destCastID : IN_destCastIDs){
                    ObjectIdentifier tmpLotID = new ObjectIdentifier();
                    for (Infos.EqpContainerPosition CHG_strEqpContainerPosition : CHG_strEqpContainerPositionSeq){
                        if (ObjectIdentifier.equalsWithValue(CHG_strEqpContainerPosition.getDestCassetteID(), IN_destCastID)){
                            tmpLotID = waferMethod.waferLotGet(objCommon, CHG_strEqpContainerPosition.getWaferID());
                            break;
                        }
                    }
                    //----------------------------
                    // Cassette is suitable ???
                    //----------------------------
                    cassetteMethod.cassetteCheckConditionForSLMDestCassette(objCommon,IN_equipmentID,IN_controlJobID,IN_destCastID,tmpLotID);
                }
                //----------------------------------------------
                // Check Previous destination Cassette
                //----------------------------------------------
                for (ObjectIdentifier PRVTMP_destCastID : PRVTMP_destCastIDs){
                    boolean strCassette_dispatchState_Get_out =false;
                    strCassette_dispatchState_Get_out = cassetteMethod.cassetteDispatchStateGet(objCommon, PRVTMP_destCastID);
                    if (strCassette_dispatchState_Get_out){
                        Outputs.CassetteReservationInfoGetDROut cassetteReservationInfoGetDROut = cassetteMethod.cassetteReservationInfoGetDR(objCommon, PRVTMP_destCastID);
                        if (CimStringUtils.equals(cassetteReservationInfoGetDROut.getNPWLoadPurposeType(),BizConstant.SP_LOADPURPOSETYPE_SLMRETRIEVING)){
                            log.info("return RC_ALREADY_DISPATCH_RESVED_CST");
                            throw new ServiceException(retCodeConfig.getAlreadyDispatchReservedCassette());
                        }
                    }
                }
                //-------------------------------//
                // Set Destination Cassette      //
                // Set Destination SlotNo        //
                // Set Destination Port          //
                //-------------------------------//
                //Change to same Port for the destination cassette.
                for (Infos.EqpContainerPosition CHG_strEqpContainerPosition : CHG_strEqpContainerPositionSeq){
                    boolean waferFoundFlag = false;
                    for (Infos.SlmSlotMap IN_dstMap : IN_dstMapSeq){
                        if (ObjectIdentifier.equalsWithValue(CHG_strEqpContainerPosition.getWaferID(), IN_dstMap.getWaferID())){
                            waferFoundFlag = true;
                            //------------------------------------------------
                            //Over written destination cassette and slotNo!
                            //------------------------------------------------
                            CHG_strEqpContainerPosition.setDestCassetteID(IN_dstMap.getCassetteID());
                            CHG_strEqpContainerPosition.setDestSlotNo(IN_dstMap.getSlotNumber());
                            //One cassette can use one Port.
                            boolean cassetteFoundFlag =false;
                            for (ObjectIdentifier PRVTMP_destCastID : PRVTMP_destCastIDs){
                                if (ObjectIdentifier.equalsWithValue(CHG_strEqpContainerPosition.getDestCassetteID(), PRVTMP_destCastID)){
                                    cassetteFoundFlag = true;
                                    log.info("##### Change to the same cassette's PortID");
                                    CHG_strEqpContainerPosition.setDestPortID(PRVTMP_destPortIDs.get(PRVTMP_destCastIDs.indexOf(PRVTMP_destCastID)));
                                    break;
                                }
                            }
                            if (!cassetteFoundFlag){
                                CHG_strEqpContainerPosition.setDestPortID(dummy);
                            }
                        }
                    }
                    if (!waferFoundFlag){
                        CHG_strEqpContainerPosition.setDestSlotNo(0);
                    }
                }
                //-----------------------------
                // ReSet Previous Cassettes.
                //-----------------------------
                int PRV_dstCnt = 0;
                for (ObjectIdentifier PRVTMP_destCastID : PRVTMP_destCastIDs){
                    boolean preCastFound = false;
                    for (Infos.EqpContainerPosition CHG_strEqpContainerPosition : CHG_strEqpContainerPositionSeq){
                        if (ObjectIdentifier.equalsWithValue(PRVTMP_destCastID, CHG_strEqpContainerPosition.getDestCassetteID())){
                            log.info("CassetteID is found in new destination cassette.");
                            preCastFound = true;
                            break;
                        }
                    }
                    if (!preCastFound){
                        PRV_destCastIDs.set(PRV_dstCnt,PRVTMP_destCastID);
                        PRV_dstCnt++;
                    }
                }
                //------------------------------------------------------------
                // Check combination Source Map and Destination Map
                //------------------------------------------------------------
                Infos.EqpContainerPositionInfo strSLM_materialOutSpec_CombinationCheck_in = new Infos.EqpContainerPositionInfo();
                strSLM_materialOutSpec_CombinationCheck_in.setEquipmentID(IN_equipmentID);
                strSLM_materialOutSpec_CombinationCheck_in.setEqpContainerPositionList(CHG_strEqpContainerPositionSeq);
                slmMethod.slmMaterialOutSpecCombinationCheck(objCommon,strSLM_materialOutSpec_CombinationCheck_in);
            }
        }
        Inputs.ObjEquipmentContainerReservationUpdateIn objEquipmentContainerReservationUpdateIn = new Inputs.ObjEquipmentContainerReservationUpdateIn();

        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            //--------------------------------------------------------------------------//
            // Update for Cassette Reserve                                              //
            //--------------------------------------------------------------------------//
            objEquipmentContainerReservationUpdateIn.setActionCode(BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE);
        }else if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)){
            //--------------------------------------------------------------------------//
            // Update for Port Reserve                                                  //
            //--------------------------------------------------------------------------//
            objEquipmentContainerReservationUpdateIn.setActionCode(BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE);
        }else if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVECANCEL)){
            //--------------------------------------------------------------------------//
            // Update for Port Reserve                                                  //
            //--------------------------------------------------------------------------//
            objEquipmentContainerReservationUpdateIn.setActionCode(BizConstant.SP_SLM_ACTIONCODE_PORTRESERVECANCEL);
        }
        objEquipmentContainerReservationUpdateIn.setEquipmentID(IN_equipmentID);
        objEquipmentContainerReservationUpdateIn.setCassetteID(IN_cassetteID);
        objEquipmentContainerReservationUpdateIn.setDesPortID(IN_destPortID);
        objEquipmentContainerReservationUpdateIn.setStrEqpContainerPositionSeq(CHG_strEqpContainerPositionSeq);
        objEquipmentContainerReservationUpdateIn.setPreCassetteIDs(PRV_destCastIDs);
        equipmentContainerMethod.equipmentContainerReservationUpdate(objCommon,objEquipmentContainerReservationUpdateIn);

        //--------------------------------------//
        // Create MtrlOutSpec for Out Put       //
        //--------------------------------------//
        //--------------------------------------------------------
        //Pickup specified Source Cassette. (no duplicate)
        //--------------------------------------------------------
        List<ObjectIdentifier> CHG_srcCastIDs = new ArrayList<>();
        for (Infos.EqpContainerPosition eqpContainerPosition : CHG_strEqpContainerPositionSeq){
            if (!ObjectIdentifier.isEmpty(eqpContainerPosition.getSrcCassetteID())){
                boolean castFoundFlag = false;
                for (ObjectIdentifier CHG_srcCastID : CHG_srcCastIDs){
                    if (ObjectIdentifier.equalsWithValue(CHG_srcCastID, eqpContainerPosition.getSrcCassetteID())){
                        castFoundFlag =true;
                    }
                }
                if (!castFoundFlag){
                    CHG_srcCastIDs.add(eqpContainerPosition.getSrcCassetteID());
                }
            }
        }

        List<Infos.MtrlOutSpec> OUT_MtrlOutSpecSeq =new ArrayList<>();
        for (ObjectIdentifier CHG_srcCastID : CHG_srcCastIDs){
            Infos.MtrlOutSpec mtrlOutSpec = new Infos.MtrlOutSpec();
            List<Infos.SlmSlotMap> strDestinationMapSeq = new ArrayList<>();
            List<Infos.SlmSlotMap> strSourceMapSeq = new ArrayList<>();
            mtrlOutSpec.setDestinationMapList(strDestinationMapSeq);
            mtrlOutSpec.setSourceMapList(strSourceMapSeq);
            for (Infos.EqpContainerPosition CHG_strEqpContainerPosition : CHG_strEqpContainerPositionSeq){
                if (ObjectIdentifier.equalsWithValue(CHG_srcCastID, CHG_strEqpContainerPosition.getSrcCassetteID())){
                    Infos.SlmSlotMap srcMap = new Infos.SlmSlotMap();
                    srcMap.setWaferID(CHG_strEqpContainerPosition.getWaferID());
                    srcMap.setCassetteID(CHG_strEqpContainerPosition.getSrcCassetteID());
                    srcMap.setSlotNumber(CHG_strEqpContainerPosition.getSrcSlotNo());
                    strSourceMapSeq.add(srcMap);
                    if (!ObjectIdentifier.isEmpty(CHG_strEqpContainerPosition.getDestCassetteID())){
                        Infos.SlmSlotMap destMap = new Infos.SlmSlotMap();
                        destMap.setWaferID(CHG_strEqpContainerPosition.getWaferID());
                        destMap.setCassetteID(CHG_strEqpContainerPosition.getDestCassetteID());
                        destMap.setSlotNumber(CHG_strEqpContainerPosition.getDestSlotNo());
                        strDestinationMapSeq.add(destMap);
                    }else {
                        continue;
                    }
                }
            }
            //Resize
            OUT_MtrlOutSpecSeq.add(mtrlOutSpec);
        }

        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            //--------------------------------------------------------
            // Check destination Cassette condition
            //--------------------------------------------------------
            slmMethod.slmCheckConditionForOperation(objCommon, IN_equipmentID, null, IN_controlJobID,
                    null, OUT_MtrlOutSpecSeq, BizConstant.SP_OPERATION_SLMWAFERRETRIEVECASSETTERESERVE);

        }
        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)){
            // -----------------------
            // Report to TCS .
            // -----------------------
            String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
            String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
            long sleepTimeValue;
            long retryCountValue;

            if (CimStringUtils.isEmpty(tmpSleepTimeValue)) {
                sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
            } else {
                sleepTimeValue = Long.valueOf(tmpSleepTimeValue) ;
            }

            if (CimStringUtils.isEmpty(tmpRetryCountValue)) {
                retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
            } else {
                retryCountValue = Integer.valueOf(tmpRetryCountValue);
            }
            Inputs.TCSMgrSendSLMWaferRetrieveCassetteReserveReqIn tcsMgrSendSLMWaferRetrieveCassetteReserveReqIn = new Inputs.TCSMgrSendSLMWaferRetrieveCassetteReserveReqIn();
            Params.SLMWaferRetrieveCassetteReserveReqInParams strSLMWaferRetrieveCassetteReserveReqInParm;
            strSLMWaferRetrieveCassetteReserveReqInParm = slmWaferRetrieveCassetteReserveReqInParams;
            strSLMWaferRetrieveCassetteReserveReqInParm.setMtrlOutSpecList(OUT_MtrlOutSpecSeq);
            tcsMgrSendSLMWaferRetrieveCassetteReserveReqIn.setStrSLMWaferRetrieveCassetteReserveReqInParm(strSLMWaferRetrieveCassetteReserveReqInParm);
            tcsMgrSendSLMWaferRetrieveCassetteReserveReqIn.setObjCommonIn(objCommon);

            for (int retryNum = 0 ; retryNum < (retryCountValue + 1) ; retryNum++ ){
                /*--------------------------*/
                /*    Send Request to TCS   */
                /*--------------------------*/
                try {
                    Inputs.SLMWaferRetrieveCassetteReserveReqIn slmWaferRetrieveCassetteReserveReqIn = new Inputs.SLMWaferRetrieveCassetteReserveReqIn();
                    slmWaferRetrieveCassetteReserveReqIn.setControlJobID(IN_controlJobID);
                    slmWaferRetrieveCassetteReserveReqIn.setLotID(IN_lotID);
                    slmWaferRetrieveCassetteReserveReqIn.setDestPortID(IN_destPortID);
                    slmWaferRetrieveCassetteReserveReqIn.setCassetteID(IN_cassetteID);
                    slmWaferRetrieveCassetteReserveReqIn.setEquipmentID(IN_equipmentID);
                    slmWaferRetrieveCassetteReserveReqIn.setClaimMemo(slmWaferRetrieveCassetteReserveReqInParams.getClaimMemo());
                    slmWaferRetrieveCassetteReserveReqIn.setStrMtrlOutSpecSeq(OUT_MtrlOutSpecSeq);
                    tcsMethod.sendTCSReq(TCSReqEnum.sendSLMWaferRetrieveCassetteReserveReq,slmWaferRetrieveCassetteReserveReqIn);
                    break;
                } catch (ServiceException e) {
                    if (e.getCode() == retCodeConfig.getExtServiceBindFail().getCode()
                            || e.getCode() == retCodeConfig.getExtServiceNilObj().getCode()
                            || e.getCode() == retCodeConfig.getTcsNoResponse().getCode() ){
                        log.info("TCS subsystem has return NO_RESPONSE!! just retry now!!");
                        log.info("now sleeping... ");
                        try {
                            Thread.sleep(sleepTimeValue);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            ex.printStackTrace();
                        }
                    }else {
                        throw e;
                    }
                }
            }
        }
        Results.SLMWaferRetrieveCassetteReserveReqResult reserveReqResult = new Results.SLMWaferRetrieveCassetteReserveReqResult();
        reserveReqResult.setLotID(IN_lotID);
        reserveReqResult.setControlJobID(IN_controlJobID);
        reserveReqResult.setStrMtrlOutSpecSeq(OUT_MtrlOutSpecSeq);
        return reserveReqResult;
    }

    @Override
    public void sxSLMProcessJobStatusRpt(Infos.ObjCommon objCommon, Params.SLMProcessJobStatusRptInParams slmProcessJobStatusRptInParams){
        //----------------------------------------------------------------
        //  Pre Process
        //----------------------------------------------------------------
        String actionCode = slmProcessJobStatusRptInParams.getActionCode();
        ObjectIdentifier controlJobID = slmProcessJobStatusRptInParams.getControlJobID();
        ObjectIdentifier equipmentID = slmProcessJobStatusRptInParams.getEquipmentID();
        String processJobID = slmProcessJobStatusRptInParams.getProcessJobID();
        List<ObjectIdentifier> waferSeq = slmProcessJobStatusRptInParams.getWaferSeq();
        //----------------------------------------------------------------
        //  object_Lock for Equipment Container Position by ControlJob
        //----------------------------------------------------------------
        // Step1 - object_LockForEquipmentContainerPosition
        Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
        objObjectLockForEquipmentContainerPositionIn.setEquipmentID(equipmentID);
        objObjectLockForEquipmentContainerPositionIn.setStrStartCassette(new ArrayList<>());
        objObjectLockForEquipmentContainerPositionIn.setControlJobID(controlJobID);
        objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, objObjectLockForEquipmentContainerPositionIn);

        //----------------------------------------------------------------
        //  Get SLM Capability of Equipment BR info
        //----------------------------------------------------------------
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        // Check SLM Capability
        if (!eqpBrInfo.isFmcCapabilityFlag()) {
            log.info("SLM Capability is OFF");
            throw new ServiceException(new OmCode(retCodeConfigEx.getEqpSlmCapabilityOff(),equipmentID.getValue()));
        }

        //----------------------------------------------------------------
        //  Get Equipment Container Position info by Wafer
        //----------------------------------------------------------------
        int nWaferLen = CimArrayUtils.getSize(waferSeq);
        Boolean nonSLMOpeFlag = false;

        List<Infos.EqpContainerPosition> tmpStrEqpContainerPositionSeq = new ArrayList<>();
        log.info("nWaferLen : {}",nWaferLen);
        for(int nCnt1 = 0; nCnt1 < nWaferLen; nCnt1++ ) {
            log.info("nCnt1 : {}",nCnt1);
            log.info("call equipmentContainerPosition_info_Get()");
            Infos.EqpContainerPositionInfo containerPositionInfo = equipmentMethod.equipmentContainerPositionInfoGet(objCommon, equipmentID,
                    waferSeq.get(nCnt1), BizConstant.SP_SLM_KEYCATEGORY_WAFER);

            int lenEqpContPos = CimArrayUtils.getSize(containerPositionInfo.getEqpContainerPositionList());
            log.info("lenEqpContPos : {}", lenEqpContPos);

            // Check combination of control job & process job / wafer in equipment container
            Validations.check(lenEqpContPos < 1 ,  retCodeConfig.getSlmWaferNotFoundInPosition());


            log.info( "SLMState", containerPositionInfo.getEqpContainerPositionList().get(0).getFmcState());
            if(CimStringUtils.equals(containerPositionInfo.getEqpContainerPositionList().get(0).getFmcState(), BizConstant.SP_SLMSTATE_NONSLMOPE)) {
                log.info("SLMState = SP_SLMState_NonSLMOpe");
                nonSLMOpeFlag = true;
            }

            // check controlJobID
            log.info("controlJobID : {}", containerPositionInfo.getEqpContainerPositionList().get(0).getControlJobID());
            Validations.check(!ObjectIdentifier.equalsWithValue(containerPositionInfo.getEqpContainerPositionList().get(0).getControlJobID(), controlJobID),
                    retCodeConfig.getCtrljobEqpctnpstUnmatch());


            //check processJobID
            log.info("processJobID : {}", containerPositionInfo.getEqpContainerPositionList().get(0).getProcessJobID() );
            Validations.check(!CimStringUtils.equals(containerPositionInfo.getEqpContainerPositionList().get(0).getProcessJobID(), processJobID),
                    retCodeConfig.getCtrljobEqpctnpstUnmatch());

            tmpStrEqpContainerPositionSeq.add(containerPositionInfo.getEqpContainerPositionList().get(0));
        }

        //----------------------------------------------------------------------
        //
        //  Main Process
        //
        //----------------------------------------------------------------------
        //----------------------------------------------------------------------
        //  Set process job status information to each Equipment Container Position
        //----------------------------------------------------------------------
        Inputs.EquipmentContainerPositionProcessJobStatusSetIn strEquipmentContainerPositionProcessJobStatusSetIn = new Inputs.EquipmentContainerPositionProcessJobStatusSetIn();
        strEquipmentContainerPositionProcessJobStatusSetIn.setEquipmentID(equipmentID);
        strEquipmentContainerPositionProcessJobStatusSetIn.setWaferSeq(waferSeq);
        strEquipmentContainerPositionProcessJobStatusSetIn.setActionCode(actionCode);
        log.info("call equipmentContainerPosition_processJobStatus_Set()");
        equipmentMethod.equipmentContainerPositionProcessJobStatusSet(objCommon, strEquipmentContainerPositionProcessJobStatusSetIn );

        if (!nonSLMOpeFlag && CimStringUtils.equals(actionCode,BizConstant.SP_SLM_ACTIONCODE_PROCESSINGCOMP)){
            log.info("actionCode = SP_SLM_ActionCode_ProcessingComp. ");

            ObjectIdentifier tempLotID = new ObjectIdentifier();
            ObjectIdentifier tmpDestCassetteID = new ObjectIdentifier();
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);

            //message queue put only if equipment is online
            if (!CimObjectUtils.isEmpty(eqpPortInfo.getEqpPortStatuses())){
                if (!CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode(),BizConstant.SP_EQP_ONLINEMODE_OFFLINE)){
                    log.info("equipment is online");
                    // sort tmpStrEqpContainerPositionSeq
                    Infos.EqpContainerPosition tmpStrEqpContainerPosition;
                    nWaferLen = tmpStrEqpContainerPositionSeq.size();

                    for (int nCnt1 = 0 ; nCnt1 < nWaferLen ; nCnt1++ ){
                        log.info("lotID = ",tmpStrEqpContainerPositionSeq.get(nCnt1).getLotID().getValue());
                        if (nCnt1 == 0){
                            log.info("First Loop");
                            continue;
                        }
                        if (!CimStringUtils.equals(tmpStrEqpContainerPositionSeq.get(nCnt1).getLotID().getValue(),
                                tmpStrEqpContainerPositionSeq.get(nCnt1-1).getLotID().getValue())){
                            log.info("# Different lot, Go Swap Loop");
                            for (int nCnt2 = nCnt1+1; nCnt2 < nWaferLen; nCnt2++ ){
                                log.info("## Loop[nCnt2]", nCnt2,tmpStrEqpContainerPositionSeq.get(nCnt2).getLotID().getValue());
                                if (CimStringUtils.equals(tmpStrEqpContainerPositionSeq.get(nCnt2).getLotID().getValue(),
                                        tmpStrEqpContainerPositionSeq.get(nCnt1-1).getLotID().getValue())){
                                    log.info("## Same Lot, Swap position");
                                    tmpStrEqpContainerPosition = tmpStrEqpContainerPositionSeq.get(nCnt1);
                                    tmpStrEqpContainerPositionSeq.set(nCnt1,tmpStrEqpContainerPositionSeq.get(nCnt2));
                                    tmpStrEqpContainerPositionSeq.set(nCnt2,tmpStrEqpContainerPosition);
                                    nCnt1++;
                                }
                            }
                        }
                    }

                    // create message queue by lot
                    for (int nCnt2 = 0; nCnt2 < nWaferLen; nCnt2++ ){
                        if (!CimStringUtils.equals(tmpStrEqpContainerPositionSeq.get(nCnt2).getLotID().getValue(), tempLotID.getValue())){
                            tempLotID = tmpStrEqpContainerPositionSeq.get(nCnt2).getLotID();
                            tmpDestCassetteID = tempLotID = tmpStrEqpContainerPositionSeq.get(nCnt2).getDestCassetteID();

                            boolean messagePutFlag =true;
                            String tmpMessageID = "";

                            if (ObjectIdentifier.isEmpty(tmpDestCassetteID)){
                                log.info("destCassette is NOT assigned for lot ", tempLotID.getValue());
                                tmpMessageID = BizConstant.SP_SLM_MSG_DESTUNKNOWN;
                            }else {
                                log.info("destCassette is assigned for lot ",tempLotID.getValue());
                                tmpMessageID = BizConstant.SP_SLM_MSG_DESTNOTACCESSIBLE;
                                // check if destination cassette is loaded on port or reserved for port
                                List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
                                int nPortLen = eqpPortStatuses.size();
                                for (int nCnt3 = 0; nCnt3 < nPortLen; nCnt3++ ){
                                    log.info("Loop ", nCnt3);
                                    if (CimStringUtils.equals(eqpPortStatuses.get(nCnt3).getLoadedCassetteID().getValue(),tmpDestCassetteID.getValue())){
                                        // already loaded, no need message queue put
                                        log.info("destCassette is loaded on port ",eqpPortStatuses.get(nCnt3).getPortID().getValue());
                                        messagePutFlag = false;
                                        break;
                                    }else if (CimStringUtils.equals(eqpPortStatuses.get(nCnt3).getDispatchLoadCassetteID().getValue(),tmpDestCassetteID.getValue())){
                                        // already NPW reserved, no need message queue put
                                        log.info("destCassette is reserved to port ",eqpPortStatuses.get(nCnt3).getPortID().getValue());
                                        messagePutFlag = false;
                                        break;
                                    }
                                }
                            }

                            if (messagePutFlag){
                                log.info("SLM message queue  put" );
                                Infos.StrSLMMsgQueueRecord strSLMMsgQueueRecord = new Infos.StrSLMMsgQueueRecord();
                                strSLMMsgQueueRecord.setEventName(BizConstant.SP_SLM_EVENTNAME_PROCESSINGCOMP);
                                strSLMMsgQueueRecord.setEquipmentID(equipmentID);
                                strSLMMsgQueueRecord.setPortID(new ObjectIdentifier(""));
                                strSLMMsgQueueRecord.setCassetteID(tmpDestCassetteID);
                                strSLMMsgQueueRecord.setControlJobID(controlJobID);
                                strSLMMsgQueueRecord.setLotID(tempLotID);
                                strSLMMsgQueueRecord.setMessageID(tmpMessageID);
                                messageMethod.slmMessageQueuePutDR(objCommon, strSLMMsgQueueRecord);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sxFMCWaferStoreRpt(Infos.ObjCommon objCommon, Params.SLMWaferStoreRptInParams slmWaferStoreRptInParams) {
        Validations.check(null == objCommon || null == slmWaferStoreRptInParams, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier equipmentID = slmWaferStoreRptInParams.getEquipmentID();
        ObjectIdentifier controlJobID = slmWaferStoreRptInParams.getControlJobID();
        String processJobID = slmWaferStoreRptInParams.getProcessJobID();
        List<Infos.SlmSlotMap> strSLMSrcSlotMapSeq = slmWaferStoreRptInParams.getSlmSrcSlotMapList();

        log.info("InParam [equipmentID] :" + ObjectIdentifier.fetchValue(equipmentID));
        log.info("InParam [controlJobID]:" + ObjectIdentifier.fetchValue(controlJobID));
        log.info("InParam [processJobID]:" + processJobID);

        //----------------------------------------------------------------
        //
        // Pre Process
        //
        //----------------------------------------------------------------
        Validations.check(ObjectIdentifier.isEmptyWithValue(controlJobID), retCodeConfig.getInvalidInputParam());

        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setUserDataUpdateFlag(false);
        objLockModeIn.setFunctionCategory(ThreadContextHolder.getTransactionId());
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        long lockMode = objLockModeOut.getLockMode();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //----------------------------------------------------------------
            // object_Lock for Equipment
            //----------------------------------------------------------------
            lockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        //----------------------------------------------------------------
        // Get SLM Capability of Equipment BR info
        //----------------------------------------------------------------
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        log.info("SLMCapabilityFlag : " + eqpBrInfo.isFmcCapabilityFlag());
        // Check SLM Capability
        if (!eqpBrInfo.isFmcCapabilityFlag()) {
            log.error("##### SLM Capability is OFF.");
            Validations.check(retCodeConfigEx.getEqpSlmCapabilityOff(), equipmentID.getValue());
        }

        //----------------------------------------------------------------
        // Get cassette list without duplication
        //----------------------------------------------------------------
        Set<ObjectIdentifier> cassetteIDSeq = new HashSet<>();
        //----------------------------------------------------------------
        // Get lot list without duplication
        //----------------------------------------------------------------
        Set<ObjectIdentifier> lotIDSeq = new HashSet<>();
        Optional.ofNullable(strSLMSrcSlotMapSeq).ifPresent(slmSlotMaps -> slmSlotMaps.forEach(slmSlotMap -> {
            cassetteIDSeq.add(slmSlotMap.getCassetteID());
            lotIDSeq.add(waferMethod.waferLotGet(objCommon, slmSlotMap.getWaferID()));
        }));
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment InProcessingLot Element (Write)
            List<String> procLotSeq = new ArrayList<>();
            Optional.of(lotIDSeq).ifPresent(list -> list.forEach(data -> procLotSeq.add(data.getValue())));

            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT);
            objAdvanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
            objAdvanceLockIn.setKeyList(procLotSeq);
            try {
                lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
            } catch (NotFoundRecordException e) {
                throw Validations.buildException(retCodeConfig.getNotFoundInProcessingLot());
            }

            // Lock Equipment LoadCassette Element (Write)
            List<String> loadCastSeq = new ArrayList<>();
            Optional.of(cassetteIDSeq).ifPresent(list -> list.forEach(data -> loadCastSeq.add(data.getValue())));
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
            objAdvanceLockIn.setKeyList(loadCastSeq);
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

            /*------------------------------*/
            /*   Lock ControlJob Object     */
            /*------------------------------*/
            lockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }

        //----------------------------------------------------------------
        // object_Lock for Cassette
        //----------------------------------------------------------------
        log.info("object_Lock for Cassette");
        Optional.of(cassetteIDSeq).ifPresent(list -> list.forEach(data -> lockMethod.objectLock(objCommon, CimCassette.class, data)));

        //----------------------------------------------------------------
        // object_Lock for Lot
        //----------------------------------------------------------------
        log.info("object_Lock for Lot");
        Optional.of(lotIDSeq).ifPresent(list -> list.forEach(data -> lockMethod.objectLock(objCommon, CimLot.class, data)));

        //----------------------------------------------------------------
        // object_Lock for Equipment Container Position by ControlJob
        //----------------------------------------------------------------
        Inputs.ObjObjectLockForEquipmentContainerPositionIn positionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
        positionIn.setEquipmentID(equipmentID);
        positionIn.setControlJobID(controlJobID);
        lockMethod.objectLockForEquipmentContainerPosition(objCommon, positionIn);

        //----------------------------------------------------------------
        // Get ControlJob of Equipment
        //----------------------------------------------------------------
        Infos.EqpInprocessingControlJobInfo eqpInprocessingControlJobInfo = equipmentMethod.equipmentInprocessingControlJobInfoGet(objCommon, equipmentID);

        //----------------------------------------------------------------
        // Check combination of control job / equipment
        //----------------------------------------------------------------
        Optional.ofNullable(eqpInprocessingControlJobInfo).ifPresent(controlJobInfo -> {
            AtomicBoolean bFoundCJ = new AtomicBoolean(false);
            Optional.ofNullable(controlJobInfo.getStrEqpInprocessingControlJob()).ifPresent(eqpInprocessingControlJobs -> {
                for (Infos.EqpInprocessingControlJob controlJob : eqpInprocessingControlJobs) {
                    if (ObjectIdentifier.equalsWithValue(controlJobID, controlJob.getControlJobID())) {
                        log.info("controlJob is found.");
                        bFoundCJ.set(true);
                        break;
                    }
                }
            });
            Validations.check(!bFoundCJ.get(), retCodeConfig.getNotFoundInProcessingLot());
        });

        //----------------------------------------------------------------
        // Get Equipment Container Information
        //----------------------------------------------------------------
        Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn = new Inputs.ObjEquipmentContainerPositionInfoGetIn();
        positionInfoGetIn.setEquipmentID(equipmentID);
        if (CimStringUtils.isNotEmpty(processJobID)) {
            positionInfoGetIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_PROCESSJOB);
            positionInfoGetIn.setKey(ObjectIdentifier.buildWithValue(processJobID));
        } else if (ObjectIdentifier.isNotEmptyWithValue(controlJobID)) {
            positionInfoGetIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
            positionInfoGetIn.setKey(controlJobID);
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, positionInfoGetIn);
        if (null == eqpContainerPositionInfo || CimArrayUtils.getSize(eqpContainerPositionInfo.getEqpContainerPositionList()) == 0) {
            Validations.check(retCodeConfig.getNotFoundEquipmentContainerPosition());
        }

        //************************************/
        //*   Check SLMState                 */
        //************************************/
        assert eqpContainerPositionInfo != null;
        if (CimStringUtils.equals(eqpContainerPositionInfo.getEqpContainerPositionList().get(0).getFmcState(), BizConstant.SP_SLMSTATE_NONSLMOPE)) {
            // No SLM operation is required.
            log.error("SLMState = SP_SLMState_NonSLMOpe");
            Validations.check(retCodeConfigEx.getNoneSlmOpe());
        }

        //----------------------------------------------------------------
        // Check Input parameter
        //----------------------------------------------------------------
        log.info("Check Input parameter");
        Optional.ofNullable(strSLMSrcSlotMapSeq).ifPresent(slmSlotMaps -> slmSlotMaps.forEach(slmSlotMap -> {
            if (ObjectIdentifier.isEmptyWithValue(slmSlotMap.getWaferID())) {
                Validations.check(retCodeConfig.getInvalidInputParam());
            }

            AtomicBoolean bFoundWafer = new AtomicBoolean(false);
            Optional.ofNullable(eqpContainerPositionInfo.getEqpContainerPositionList()).ifPresent(eqpContainerPositions -> {
                for (Infos.EqpContainerPosition strCtnPstInfo : eqpContainerPositions) {
                    log.info("containerPositionID..." + strCtnPstInfo.getContainerPositionID().getValue());
                    log.info("  controlJobID........" + strCtnPstInfo.getControlJobID().getValue());
                    log.info("  processJobID........" + strCtnPstInfo.getProcessJobID());
                    log.info("  lotID..............." + strCtnPstInfo.getLotID().getValue());
                    log.info("  waferID............." + strCtnPstInfo.getWaferID().getValue());
                    log.info("  SLMState............" + strCtnPstInfo.getFmcState());
                    //--------------------------------------------
                    // Check SLMState = Reserved
                    // Check controlJobID
                    // Check processJobID
                    // Check wafer existence
                    // -------------------------------------------

                    if (ObjectIdentifier.equalsWithValue(strCtnPstInfo.getWaferID(), slmSlotMap.getWaferID())) {
                        log.info("Found Wafer");
                        // SLM State should be "Reserved"
                        if (!CimStringUtils.equals(strCtnPstInfo.getFmcState(), BizConstant.SP_SLMSTATE_RESERVED)) {
                            Validations.check(retCodeConfigEx.getInvalidSlmStateReserved());
                        }
                        if (ObjectIdentifier.equalsWithValue(strCtnPstInfo.getControlJobID(), controlJobID)
                                || CimStringUtils.equals(strCtnPstInfo.getProcessJobID(), processJobID)) {
                            bFoundWafer.set(true);
                            log.info("bFoundWafer = TRUE");
                        }
                        break;
                    }

                }
            });
            if (!bFoundWafer.get()) {
                Validations.check(retCodeConfig.getWaferNotInEqp());
            }
        }));

        //----------------------------------------------------------------
        // Check Cassette TransferState
        //----------------------------------------------------------------
        Optional.of(cassetteIDSeq).ifPresent(list -> list.forEach(data -> {
            //  Get Cassette TransferState
            String transferStateGet = cassetteMethod.cassetteTransferStateGet(objCommon, data);
            log.info("transferState : " + transferStateGet);
            Validations.check(!CimStringUtils.equals(transferStateGet, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), retCodeConfig.getInvalidCassetteTransferState());
        }));

        //----------------------------------------------------------------
        // Get ControlJob Status
        //----------------------------------------------------------------
        log.info("Get ControlJob Status");
        Outputs.ObjControlJobStatusGetOut controlJobStatusGet = controlJobMethod.controlJobStatusGet(objCommon, controlJobID);
        log.info("controlJobStatus : " + controlJobStatusGet.getControlJobStatus());
        // Check ControlJob Status
        if (!CimStringUtils.equals(controlJobStatusGet.getControlJobStatus(), BizConstant.SP_CONTROLJOBSTATUS_QUEUED)
                && !CimStringUtils.equals(controlJobStatusGet.getControlJobStatus(), BizConstant.SP_CONTROLJOBSTATUS_EXECUTING)) {
            Validations.check(retCodeConfigEx.getInvalidCjstatus());
        }

        //----------------------------------------------------------------
        //
        // Main Process
        //
        //----------------------------------------------------------------
        log.info("Main Process");
        //----------------------------------------------------------------
        // Store wafer in Equipment Container
        //----------------------------------------------------------------
        Inputs.ObjEquipmentContainerWaferStoreIn waferStoreIn = new Inputs.ObjEquipmentContainerWaferStoreIn();
        waferStoreIn.setEquipmentID(equipmentID);
        waferStoreIn.setControlJobID(controlJobID);
        waferStoreIn.setProcessJobID(processJobID);
        waferStoreIn.setStrSLMSlotMapSeq(strSLMSrcSlotMapSeq);
        log.info("call equipmentContainer_wafer_Store()");
        equipmentContainerMethod.equipmentContainerWaferStore(objCommon, waferStoreIn);

        //----------------------------------------------------------------
        // Update Cassette's MultiLotType
        //----------------------------------------------------------------
        log.info("Update Cassette's MultiLotType");
        Optional.of(cassetteIDSeq).ifPresent(list -> list.forEach(data -> cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, data)));

        //----------------------------------------------------------------
        // Message Queue Put for SLM
        //----------------------------------------------------------------
        log.info("Message Queue Put for FMC");
        Infos.EqpContainerInfo eqpContainerInfo = equipmentContainerMethod.equipmentContainerInfoGet(objCommon, equipmentID);
        if (null == eqpContainerInfo || CimArrayUtils.getSize(eqpContainerInfo.getEqpContainerList()) == 0) {
            Validations.check(retCodeConfig.getNotFoundEquipmentContainerPosition());
        }

        Optional.of(cassetteIDSeq).ifPresent(cassetteIDs -> {
            for (ObjectIdentifier cassetteID : cassetteIDs) {
                ObjectIdentifier searchCJID = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                AtomicBoolean bStoredAllWaferOfCJ = new AtomicBoolean(true);
                assert eqpContainerInfo != null;
                List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerInfo.getEqpContainerList().get(0).getEqpContainerPosition();
                Optional.ofNullable(eqpContainerPositionList).ifPresent(eqpContainerPositions -> {
                    for (Infos.EqpContainerPosition strCtnPstInfo : eqpContainerPositions) {
                        if (ObjectIdentifier.equalsWithValue(strCtnPstInfo.getControlJobID(), searchCJID)
                                && ObjectIdentifier.equalsWithValue(strCtnPstInfo.getSrcCassetteID(), cassetteID)
                                && !CimStringUtils.equals(strCtnPstInfo.getFmcState(), BizConstant.SP_SLMSTATE_STORED)
                                && !CimStringUtils.equals(strCtnPstInfo.getFmcState(), BizConstant.SP_SLMSTATE_RETRIEVED)) {
                            log.info("bStoredAllWaferOfCJ = FALSE");
                            bStoredAllWaferOfCJ.set(false);
                            break;
                        }
                    }
                });
                if (!bStoredAllWaferOfCJ.get()) {
                    log.info("Not all wafers of ControlJob in this Cassette are in EQPContainer");
                    continue;
                }
                AtomicReference<ObjectIdentifier> loadedPortID = new AtomicReference<>();
                // search loaded portID
                Optional.ofNullable(eqpContainerPositionList).ifPresent(eqpContainerPositions -> {
                    for (Infos.EqpContainerPosition strCtnPstInfo : eqpContainerPositions) {
                        if (ObjectIdentifier.equalsWithValue(strCtnPstInfo.getSrcCassetteID(), cassetteID)) {
                            loadedPortID.set(strCtnPstInfo.getSrcPortID());
                            break;
                        }
                    }
                });

                log.info("call SLM_messageQueue_PutDR()");
                // SLM_messageQueue_PutDR
                Infos.StrSLMMsgQueueRecord strSLMMsgQueueRecord = new Infos.StrSLMMsgQueueRecord();
                strSLMMsgQueueRecord.setEventName(BizConstant.SP_SLM_EVENTNAME_WAFERSTORE);
                strSLMMsgQueueRecord.setEquipmentID(equipmentID);
                strSLMMsgQueueRecord.setLotID(loadedPortID.get());
                strSLMMsgQueueRecord.setControlJobID(controlJobID);
                strSLMMsgQueueRecord.setCassetteID(cassetteID);
                messageMethod.slmMessageQueuePutDR(objCommon, strSLMMsgQueueRecord);
            }
        });
    }

    @Override
    public void sxFMCWaferRetrieveRpt (Infos.ObjCommon objCommon, Params.SLMWaferRetrieveRptInParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        String processJobID = params.getProcessJobID();
        List<Infos.SlmSlotMap> destSlotMapSeq = params.getSlmDestSlotMapList();

        //-------------------------------//
        //   Object Lock for Equipment   //
        //-------------------------------//
        Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
        objAdvanceLockIn.setObjectID(equipmentID);
        objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
        objAdvanceLockIn.setKeyList(Collections.emptyList());

        objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

        int size = CimNumberUtils.intValue(CimNumberUtils.doubleValue(destSlotMapSeq.size()) * 1.6);
        Set<ObjectIdentifier> retrieveCastIDs = new HashSet<>(size);
        Set<ObjectIdentifier> retrieveLotIDs = new HashSet<>(size);
        if (CimArrayUtils.isNotEmpty(destSlotMapSeq)) {
            destSlotMapSeq.forEach(map -> {
                //------------------------------//
                //   Object Lock for Cassette   //
                //------------------------------//
                retrieveCastIDs.add(map.getCassetteID());

                //-------------------------//
                //   Object Lock for Lot   //
                //-------------------------//
                ObjectIdentifier mapLotID = waferMethod.waferLotGet(objCommon, map.getWaferID());
                retrieveLotIDs.add(mapLotID);
            });
        }

        String controlJobId = ObjectIdentifier.fetchValue(controlJobID);
        if (CimStringUtils.isNotEmpty(controlJobId)) {
            List<String> procLotSeq = new ArrayList<>(retrieveLotIDs.size());
            retrieveLotIDs.forEach(id -> procLotSeq.add(ObjectIdentifier.fetchValue(id)));
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT);
            objAdvanceLockIn.setKeyList(procLotSeq);
            objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

            List<String> loadCastSeq = new ArrayList<>(retrieveCastIDs.size());
            retrieveCastIDs.forEach(id -> loadCastSeq.add(ObjectIdentifier.fetchValue(id)));
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
            objAdvanceLockIn.setKeyList(loadCastSeq);
            try {
                objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
            } catch (NotFoundRecordException e) {
                Validations.check(retCodeConfig.getInvalidTransferState());
            }

            /*------------------------------*/
            /*   Lock ControlJob Object     */
            /*------------------------------*/
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }
        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, retrieveCastIDs);
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, retrieveLotIDs);

        //-----------------------------------------//
        //   Object Lock for Equipment Container   //
        //-----------------------------------------//
        Inputs.ObjObjectLockForEquipmentContainerPositionIn eqpCntnrPstnIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
        eqpCntnrPstnIn.setEquipmentID(equipmentID);
        eqpCntnrPstnIn.setControlJobID(controlJobID);
        objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, eqpCntnrPstnIn);

        //-----------------//
        //   Check Logic   //
        //-----------------//
        //------------------------------//
        //   Check for SLM Capability   //
        //------------------------------//
        String equipmentId = ObjectIdentifier.fetchValue(equipmentID);
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        Validations.check(!eqpBrInfo.isFmcCapabilityFlag(), retCodeConfigEx.getEqpSlmCapabilityOff(), equipmentId);

        //------------------------------------------------------//
        //   Hold the equipment container info before updated   //
        //------------------------------------------------------//
        Infos.EqpContainerInfo eqpContainerInfo = equipmentContainerMethod.equipmentContainerInfoGet(objCommon, equipmentID);

        //------------------------------------------------------//
        //   Check input parameter                              //
        //------------------------------------------------------//
        List<Infos.EqpContainer> eqpContainerList = eqpContainerInfo.getEqpContainerList();
        Validations.check(CimArrayUtils.isEmpty(eqpContainerList), retCodeConfig.getNotFoundEquipmentContainer());
        destSlotMapSeq.forEach(map ->  {
            Optional<Infos.EqpContainerPosition> tmpPositionOpt = Optional.empty();
            String waferId = ObjectIdentifier.fetchValue(map.getWaferID());
            for (Infos.EqpContainer eqpContainer : eqpContainerList) {
                List<Infos.EqpContainerPosition> eqpContainerPosition = eqpContainer.getEqpContainerPosition();
                tmpPositionOpt = this.findEqpContainerPositionByWaferId(eqpContainerPosition, waferId);
                if(tmpPositionOpt.isPresent()) {
                    break;
                }
            }
            Validations.check(!tmpPositionOpt.isPresent() ||
                            !CimStringUtils.equals(ObjectIdentifier.fetchValue(tmpPositionOpt.get().getControlJobID()), controlJobId),
                    retCodeConfig.getInvalidInputParam());
        });

        if (CimStringUtils.isNotEmpty(controlJobId)) {
            //----------------------------------------------------------------------------------------//
            //   Check if the equipment has controlJob (Checked if input controlJobID isn't blank).   //
            //--------------------------------------------------------------------------------------- //
            Infos.EqpInprocessingControlJobInfo inProcessingControlJobInfo = equipmentMethod.equipmentInprocessingControlJobInfoGet(objCommon, equipmentID);
            List<Infos.EqpInprocessingControlJob> strEqpInprocessingControlJob = inProcessingControlJobInfo.getStrEqpInprocessingControlJob();
            boolean foundFlag = false;
            for (Infos.EqpInprocessingControlJob eqpInprocessingControlJob : strEqpInprocessingControlJob) {
                if (controlJobID.equals(eqpInprocessingControlJob.getControlJobID())) {
                    foundFlag = true;
                    break;
                }
            }
            Validations.check(!foundFlag, retCodeConfig.getControlJobEqpUnmatch(), controlJobId, equipmentId);

            //-------------------------------------------------------------------------//
            //   When in-parameter controlJobID isn't blank, bellow checks are done.   //
            //-------------------------------------------------------------------------//
            Inputs.ObjEquipmentContainerPositionInfoGetIn equipmentContainerPositionInfoGetIn =
                    new Inputs.ObjEquipmentContainerPositionInfoGetIn(equipmentID, controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
            Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, equipmentContainerPositionInfoGetIn);
            List<Infos.EqpContainerPosition> contPosSeq = eqpContainerPositionInfo.getEqpContainerPositionList();
            Validations.check(CimArrayUtils.isEmpty(contPosSeq), retCodeConfig.getNotFoundEquipmentContainerPosition());

            //----------------------------------//
            //   Check SLMState                 //
            //----------------------------------//
            Validations.check(CimStringUtils.equals(contPosSeq.get(0).getFmcState(), BizConstant.SP_SLMSTATE_NONSLMOPE),
                    retCodeConfigEx.getNoneSlmOpe());

            destSlotMapSeq.forEach(map -> findEqpContainerPositionByWaferId(contPosSeq, ObjectIdentifier.fetchValue(map.getWaferID()))
                    .orElseThrow(() -> new ServiceException(retCodeConfig.getNotFoundEquipmentContainerPosition())));
        }
        //---------------------------------------------//
        //   Check for destination cassette's state.   //
        //---------------------------------------------//
        retrieveCastIDs.forEach(id -> {
            String xferState = cassetteMethod.cassetteTransferStateGet(objCommon, id);
            Validations.check(!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, xferState), retCodeConfig.getInvalidTransferState());
        });

        //--------------------------------------------------------------------------------//
        //   Checks for machine container position.                                       //
        //       1.Consistency Check between slot map and equipment container position.   //
        //           - destination cassette                                               //
        //           - slot No                                                            //
        //       2.If destination cassette are the same.                                  //
        //       3.If all wafers SLMState are "Stored".                                   //
        //--------------------------------------------------------------------------------//
        destSlotMapSeq.forEach(map -> {
            Inputs.ObjEquipmentContainerPositionInfoGetIn equipmentContainerPositionInfoGetIn =
                    new Inputs.ObjEquipmentContainerPositionInfoGetIn(equipmentID, map.getWaferID(), BizConstant.SP_SLM_KEYCATEGORY_WAFER);
            Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, equipmentContainerPositionInfoGetIn);
            List<Infos.EqpContainerPosition> eqpContPosSeq = eqpContainerPositionInfo.getEqpContainerPositionList();

            String waferId = ObjectIdentifier.fetchValue(map.getWaferID());
            Validations.check(CimArrayUtils.isEmpty(eqpContPosSeq), retCodeConfigEx.getSlmWaferNotFoundInPosition(), waferId, equipmentId);

            Infos.EqpContainerPosition eqpContPos = eqpContPosSeq.get(0);
            Validations.check(!map.getCassetteID().equals(eqpContPos.getDestCassetteID()), retCodeConfigEx.getWrongCastForRetrievingWafer(),
                    ObjectIdentifier.fetchValue(map.getCassetteID()),
                    waferId);

            Validations.check(map.getSlotNumber() != CimNumberUtils.intValue(eqpContPos.getDestSlotNo()),
                    retCodeConfigEx.getSlotNoMismatchSlotmapEqpctnpst(), waferId);

            String slmState = eqpContPos.getFmcState();
            Validations.check(!CimStringUtils.equals(slmState, BizConstant.SP_SLMSTATE_STORED),
                    retCodeConfigEx.getInvalidSlmStateStored(), waferId, slmState);
        });

        //------------------------------------------------------------------//
        //   The empty situation of destination cassette is investigated.   //
        //   However, basically, This check is not required.                //
        //   So, this check exists for insurance.                           //
        //------------------------------------------------------------------//
        List<Infos.MtrlOutSpec> strMtrlOutSpecSeq = new ArrayList<>(retrieveCastIDs.size());
        retrieveCastIDs.forEach(id -> {
            Infos.MtrlOutSpec mtrlOutSpec = new Infos.MtrlOutSpec();
            strMtrlOutSpecSeq.add(mtrlOutSpec);
            List<Infos.SlmSlotMap> strDestinationMapSeq = new ArrayList<>();
            mtrlOutSpec.setDestinationMapList(strDestinationMapSeq);
            strDestinationMapSeq.forEach(map -> {
                if (CimStringUtils.equals(ObjectIdentifier.fetchValue(map.getCassetteID()), ObjectIdentifier.fetchValue(id))) {
                    strDestinationMapSeq.add(map);
                }
            });
            mtrlOutSpec.setSourceMapList(Collections.emptyList());
        });
        slmMethod.slmCheckConditionForOperation(objCommon, equipmentID, CimStringUtils.EMPTY, controlJobID,
                Collections.emptyList(), strMtrlOutSpecSeq, BizConstant.SP_OPERATION_SLMWAFERRETRIEVE);

        //----------------------//
        //   Retrieve Wafers.   //
        //----------------------//
        Inputs.ObjEquipmentContainerWaferRetrieveIn objEquipmentContainerWaferRetrieveIn = new Inputs.ObjEquipmentContainerWaferRetrieveIn();
        objEquipmentContainerWaferRetrieveIn.setEquipmentID(equipmentID);
        objEquipmentContainerWaferRetrieveIn.setControlJobID(controlJobID);
        objEquipmentContainerWaferRetrieveIn.setProcessJobID(processJobID);
        objEquipmentContainerWaferRetrieveIn.setStrSLMSlotMapSeq(destSlotMapSeq);
        //@TODO
        equipmentContainerMethod.equipmentContainerWaferRetrieve(objCommon, objEquipmentContainerWaferRetrieveIn);

        //------------------------------------------------------------//
        //  If all wafers are wafers are retrieved for the cassette   //
        //  Clear the Cassette's SLMReserved equipment information.   //
        //------------------------------------------------------------//
        Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn = CimStringUtils.isNotEmpty(controlJobId) ?
                new Inputs.ObjEquipmentContainerPositionInfoGetIn(equipmentID, controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB) :
                new Inputs.ObjEquipmentContainerPositionInfoGetIn(equipmentID, equipmentID, BizConstant.SP_SLM_KEYCATEGORY_EQUIPMENT);
        Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, positionInfoGetIn);
        List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerPositionInfo.getEqpContainerPositionList();
        retrieveCastIDs.forEach(castID -> {
            String castId = ObjectIdentifier.fetchValue(castID);
            ObjectIdentifier emptyEqp = ObjectIdentifier.emptyIdentifier();
            cassetteMethod.cassetteSLMReserveEquipmentSet(objCommon, castID, emptyEqp);
            //------------------------------------//
            //   Update Cassette's MultiLotType   //
            //------------------------------------//
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, castID);
        });
    }

    private Optional<Infos.EqpContainerPosition> findEqpContainerPositionByWaferId (List<Infos.EqpContainerPosition> eqpContainerPosition, String targetWaferId) {
        if (CimArrayUtils.isEmpty(eqpContainerPosition)) {
            return Optional.empty();
        }
        for (Infos.EqpContainerPosition containerPosition : eqpContainerPosition) {
            if (CimStringUtils.equals(targetWaferId, ObjectIdentifier.fetchValue(containerPosition.getWaferID()))) {
                return Optional.of(containerPosition);
            }
        }
        return Optional.empty();
    }

    private Optional<Infos.EqpContainerPosition> findEqpContainerPositionByDestCarrierId (List<Infos.EqpContainerPosition> eqpContainerPosition, String targetWaferId) {
        if (CimArrayUtils.isEmpty(eqpContainerPosition)) {
            return Optional.empty();
        }
        for (Infos.EqpContainerPosition containerPosition : eqpContainerPosition) {
            if (CimStringUtils.equals(targetWaferId, ObjectIdentifier.fetchValue(containerPosition.getDestCassetteID()))) {
                return Optional.of(containerPosition);
            }
        }
        return Optional.empty();
    }


    @Override
    public void sxSLMCassetteDetachFromCJReq(Infos.ObjCommon objCommon, Params.SLMCassetteDetachFromCJReqInParams slmCassetteDetachFromCJReqInParams){
        //----------------------------------------------------------------
        //  Pre Process
        //----------------------------------------------------------------
        ObjectIdentifier cassetteID = slmCassetteDetachFromCJReqInParams.getCassetteID();
        ObjectIdentifier controlJobID = slmCassetteDetachFromCJReqInParams.getControlJobID();
        ObjectIdentifier equipmentID = slmCassetteDetachFromCJReqInParams.getEquipmentID();
        //step1 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.FMC_CARRIER_REMOVE_FROM_CJ_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // The equipment information do not change, therefore, the object lock for the equipment object is not necessary
            // The control job object will be changed, therefore it should be locked to keep the consistency
            /*------------------------------*/
            /*   Lock ConstrolJob Object     */
            /*------------------------------*/
            try {
                objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
            } catch (NotFoundRecordException e) {
                throw new ServiceException(retCodeConfig.getNotFoundControlJob());
            }
        }else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //-------------------------------//
            //   Object Lock for Equipment   //
            //-------------------------------//
            try {
                objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
            } catch (NotFoundRecordException e) {
                throw new ServiceException(retCodeConfig.getNotFoundEqp());
            }
        }
        //----------------------------------------------------------------
        //  object_Lock for Cassette
        //----------------------------------------------------------------
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        //----------------------------------------------------------------
        //  Get SLM Capability & SLM Switch of Equipment BR info
        //----------------------------------------------------------------
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        // Check SLM Capability
        if (!eqpBrInfo.isFmcCapabilityFlag()) {
            log.info("SLM Capability is OFF");
            throw new ServiceException(new OmCode(retCodeConfigEx.getEqpSlmCapabilityOff(),equipmentID.getValue()));
        }

        //----------------------------------------------------------------
        //  Get Cassette's ControlJob
        //----------------------------------------------------------------
        ObjectIdentifier strCassette_controlJobID_Get_out = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
        // Check combination of control job / cassette
        if (!CimStringUtils.equals(strCassette_controlJobID_Get_out.getValue(),controlJobID.getValue())){
            throw new ServiceException(new OmCode(retCodeConfigEx.getCtrljobCastUnmatch(),controlJobID.getValue(),equipmentID.getValue()));
        }

        //  Get equipment container position list by controlJob
        Infos.EqpContainerPositionInfo containerPositionInfo = equipmentMethod.equipmentContainerPositionInfoGet(objCommon, equipmentID,
                controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
        List<Infos.EqpContainerPosition> eqpContainerPositionList = containerPositionInfo.getEqpContainerPositionList();
        if (!CimObjectUtils.isEmpty(eqpContainerPositionList)){
            //------------------------------------------------------------------------------------
            // Check Source Cassette in position
            // If SLMState is SP_SLMState_NonSLMOpe, this cassette is not allowed for detaching
            //------------------------------------------------------------------------------------
            if (CimStringUtils.equals(eqpContainerPositionList.get(0).getFmcState(),BizConstant.SP_SLMSTATE_NONSLMOPE)){
                log.info("SLMState = SP_SLMState_NonSLMOpe");
                throw new ServiceException(retCodeConfigEx.getNoneSlmOpe());
            }else {
                log.info("SLMState", eqpContainerPositionList.get(0).getFmcState());
            }
        }else {
            throw new ServiceException(retCodeConfig.getNotFoundEquipmentContainerPosition());
        }

        //----------------------------------------------------------------
        //  Get Wafer List in Cassette
        //----------------------------------------------------------------
        List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfos = cassetteMethod.cassetteGetWaferMapDR(objCommon, cassetteID);
        if (CimObjectUtils.isEmpty(waferMapInCassetteInfos)){
            throw new ServiceException(retCodeConfig.getNotFoundWafer());
        }else {
            //----------------------------------------------------------------
            // Check Wafer Location
            // Wafers in cassette should not inside container position
            //----------------------------------------------------------------
            for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo : waferMapInCassetteInfos){
                ObjectIdentifier waferID = waferMapInCassetteInfo.getWaferID();
                for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositionList){
                    ObjectIdentifier waferInContainerID = eqpContainerPosition.getWaferID();
                    if (ObjectIdentifier.equalsWithValue(waferID, waferInContainerID)){
                        log.info("Wafers in cassette are inside container position");
                        throw new ServiceException(retCodeConfigEx.getCjRelatedWfInCast());
                    }
                }
            }
        }

        //----------------------------------------------------------------
        // Check Wafer Location
        // If there are wafers that has been retrieved to cassette,
        // Detaching is not allowed
        //----------------------------------------------------------------

        List<Infos.EqpContainerPosition> eqpContainerPositions = equipmentContainerPositionMethod.equipmentContainerPositionInfoGetByDestCassette(objCommon, equipmentID, cassetteID);
        for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
            log.info("waferID in position =" ,eqpContainerPosition.getWaferID().getValue());
            if (CimStringUtils.equals(eqpContainerPosition.getFmcState(),BizConstant.SP_SLMSTATE_RETRIEVED)){
                log.info("## Retrieved Wafer Found" , eqpContainerPosition.getWaferID().getValue());
                throw new ServiceException(new OmCode(retCodeConfigEx.getWfRetrievedInCast(),eqpContainerPosition.getWaferID().getValue()));
            }
        }

        //----------------------------------------------------------------
        //  Main Process
        //----------------------------------------------------------------
        //----------------------------------------------------------------
        //  Remove Cassette information from Control Job
        //  Remove Control Job information from Cassette
        //----------------------------------------------------------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        controlJobMethod.controlJobCassetteInfoDelete(objCommon, cassetteIDs);

        //----------------------------------------------------------------
        //  Remove PortGroup information from Control Job
        //----------------------------------------------------------------
        log.info("Set PortGroup as empty");
        controlJobMethod.controlJobPortGroupSet(objCommon,controlJobID,new ObjectIdentifier());
    }

    @Override
    public void sxFMCCassetteUnclampReq(Infos.ObjCommon objCommon, Params.SLMCassetteUnclampReqInParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier cassetteID = params.getCassetteID();

        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(equipmentID),
                retCodeConfig.getInvalidParameterWithMsg(), "equipment ID is required");
        String equipmentId = ObjectIdentifier.fetchValue(equipmentID);
        Validations.check(ObjectIdentifier.isEmpty(portID),
                retCodeConfig.getInvalidParameterWithMsg(), "port ID is required");
        Validations.check(ObjectIdentifier.isEmpty(cassetteID),
                retCodeConfig.getInvalidParameterWithMsg(), "carrier ID is required");
        String cassetteId = ObjectIdentifier.fetchValue(cassetteID);

        //----------------------------------------------------------------
        //  Get SLM Capability of Equipment BR info
        //----------------------------------------------------------------
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        Validations.check(!eqpBrInfo.isFmcCapabilityFlag(), retCodeConfigEx.getEqpSlmCapabilityOff(), equipmentId);

        //----------------------------------------------------------------
        //  Get equipment port information
        //----------------------------------------------------------------
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();

        //------------------------------------------------------------------
        //  Common Consistency Check
        //     1. equipment OnlineMode should be Online
        //     2. equipment AccessMode should be Auto
        //     3. requested cassette should be loaded (EI) on requested port
        //------------------------------------------------------------------
        CimArrayUtils.get(eqpPortStatuses, 0).ifPresent(eqpPortStatus
                -> Validations.check(CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, eqpPortStatus.getOnlineMode()),
                retCodeConfigEx.getEqpOffline(), equipmentId));
        Infos.EqpPortStatus eqpPortStatus = eqpPortStatuses.stream().parallel()
                .filter(portInfo -> cassetteID.equals(portInfo.getLoadedCassetteID())).findFirst()
                .orElseThrow(() -> Validations.buildException(retCodeConfig.getCassetteNotInLoader(), cassetteId));
        Validations.check(portID.equals(eqpPortStatus.getPortID()), retCodeConfig.getInvalidCassettePortCombination());

        //------------------------------------------------------------------
        //  SLM Consistency Check
        //     1. the cassette should not be related to any controlJob
        //     2. the cassette should not contain any retrieved wafer
        //------------------------------------------------------------------

        /*---------------------------------*/
        /*   Get Cassette's ControlJobID   */
        /*---------------------------------*/
        ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
        Validations.check(!ObjectIdentifier.isEmpty(controlJobID), retCodeConfig.getCassetteControlJobFilled());

        List<Infos.EqpContainerPosition> eqpContainerPositions = equipmentContainerPositionMethod
                .equipmentContainerPositionInfoGetByDestCassette(objCommon, equipmentID, cassetteID);
        if (CimArrayUtils.isNotEmpty(eqpContainerPositions)) {
            boolean anySlmRetrieved = eqpContainerPositions.stream().parallel()
                    .anyMatch(position -> BizConstant.SP_SLMSTATE_RETRIEVED.equals(position.getFmcState()));
            Validations.check(anySlmRetrieved, retCodeConfigEx.getInvalidSlmStateRetrieved());
        }

        //----------------------------------------------------------------------
        //
        //  Main Process
        //
        //----------------------------------------------------------------------
        //----------------------------------------------------------------------
        //  Request Unclamp to TCS
        //----------------------------------------------------------------------
        Inputs.SendSLMCassetteUnclampReqIn tcsIn = new Inputs.SendSLMCassetteUnclampReqIn();
        tcsIn.setObjCommonIn(objCommon);
        tcsIn.setSlmCassetteUnclampReqInParams(params);
        tcsMethod.sendTCSReq(TCSReqEnum.sendSLMCassetteUnclampReq, tcsIn);
    }


    @Override
    public void sxFMCModeChangeReq(Infos.ObjCommon objCommon, Params.SLMSwitchUpdateReqInParams slmSwitchUpdateReqInParams) {
        Validations.check(null == objCommon || null == slmSwitchUpdateReqInParams, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier equipmentID = slmSwitchUpdateReqInParams.getEquipmentID();
        String SLMSwitch = slmSwitchUpdateReqInParams.getFMCMode();
        log.info("in-parm equipmentID     : " + ObjectIdentifier.fetchValue(equipmentID));
        log.info("in-parm FMCMode       : " + SLMSwitch);

        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(ThreadContextHolder.getTransactionId());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        long lockMode = objLockModeOut.getLockMode();

        log.info("lockMode : " + lockMode);
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------//
            //   Lock objects to be updated   //
            //--------------------------------//
            lockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        //-------------------------------e---------------------------------
        //  Get SLM Capability of Equipment BR info
        //----------------------------------------------------------------
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);

        log.info("SLMCapabilityFlag : " + eqpBrInfo.isFmcCapabilityFlag());
        // SLMSwitch should be "ON" or "OFF"
        if (!CimStringUtils.equals(SLMSwitch, BizConstant.SP_SLM_SWITCH_ON) && !CimStringUtils.equals(SLMSwitch, BizConstant.SP_SLM_SWITCH_OFF)) {
            Validations.check(retCodeConfigEx.getSlmInvalidParameterSLMSwitch(), SLMSwitch);
        }

        // Check SLM Capability
        if (!eqpBrInfo.isFmcCapabilityFlag()) {
            Validations.check(retCodeConfigEx.getEqpSlmCapabilityOff(), equipmentID.getValue());
        }

        // Get original SLMSwitch
        // original SLMSwitch should be different from requested one
        String orgSLMSwitch = eqpBrInfo.getFmcSwitch();
        log.info("orgSLMSwitch: " + orgSLMSwitch);
        if (CimStringUtils.equals(orgSLMSwitch, SLMSwitch)) {
            Validations.check(retCodeConfigEx.getSlmSLMSwitchSame(), SLMSwitch);
        }

        //----------------------------------------------------------------------
        //
        //  Main Process
        //
        //----------------------------------------------------------------------
        //----------------------------------------------------------------------
        //  Set SLMSwitch to equipment
        //----------------------------------------------------------------------
        equipmentMethod.equipmentSLMSwitchSet(objCommon, equipmentID, SLMSwitch);

        //----------------------------------------------------------------------
        //  Make history event
        //----------------------------------------------------------------------
        eventMethod.equipmentSLMSwitchChangeEventMake(objCommon, objCommon.getTransactionID(), equipmentID, SLMSwitch, slmSwitchUpdateReqInParams.getClaimMemo());
    }

    @Override
    public void sxFMCRsvMaxCountUpdateReq(Infos.ObjCommon objCommon,
                                          Params.FmcRsvMaxCountUpdateReqInParams fmcRsvMaxCountUpdateReqInParams){
        ObjectIdentifier equipmentID = fmcRsvMaxCountUpdateReqInParams.getEquipmentID();
        ObjectIdentifier equipmentContainerID = fmcRsvMaxCountUpdateReqInParams.getEquipmentContainerID();
        Integer maxRsvCount = fmcRsvMaxCountUpdateReqInParams.getMaxRsvCount();
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        long lockMode = objLockModeOut.getLockMode();

        log.info("lockMode : " + lockMode);
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------//
            //   Lock objects to be updated   //
            //--------------------------------//
            lockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, equipmentContainerID, BizConstant.SP_CLASSNAME_POSMACHINECONTAINER);
        //------------------------//
        //  Check SLM capability  //
        //------------------------//
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        if (!eqpBrInfo.isFmcCapabilityFlag()){
            throw new ServiceException(new OmCode(retCodeConfigEx.getEqpSlmCapabilityOff(),equipmentID.getValue()));
        }
        //------------------------------------//
        //  Update the SLM max reserve count  //
        //------------------------------------//
        equipmentContainerMethod.equipmentContainerMaxRsvCountUpdate(objCommon,equipmentID,equipmentContainerID,maxRsvCount);
        //------------------//
        //    Make Event    //
        //------------------//
        eventMethod.equipmentContainerMaxRsvCountUpdateEventMake(objCommon,fmcRsvMaxCountUpdateReqInParams);
    }
}
