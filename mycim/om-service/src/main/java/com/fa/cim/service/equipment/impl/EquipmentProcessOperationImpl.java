package com.fa.cim.service.equipment.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.cjpj.IControlJobProcessJobService;
import com.fa.cim.service.equipment.IEquipmentProcessOperation;
import com.fa.cim.service.equipment.IEquipmentService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.pcs.IProcessControlScriptService;
import com.fa.cim.service.plan.IPlanInqService;
import com.fa.cim.service.plan.IPlanService;
import com.fa.cim.service.season.ISeasoningService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@OmService
public class EquipmentProcessOperationImpl implements IEquipmentProcessOperation {

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IVirtualOperationMethod virtualOperationMethod;

    @Autowired
    private ISeasonMethod seasonMethod;

    @Autowired
    private ISeasoningService seasoningService;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IBinSummaryMethod binSummaryMethod;

    @Autowired
    private IFlowBatchMethod flowBatchMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IFixtureMethod fixtureMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private IEquipmentService equipmentService;

    @Autowired
    private IControlJobProcessJobService controlJobProcessJobService;

    @Autowired
    private IEquipmentContainerPositionMethod eqpContainerPosMethod;

    @Autowired
    private IEAPMethod eapMethod;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Autowired
    private IPilotRunMethod pilotRunMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private IProcessControlScriptService processControlScriptService;

    @Autowired
    private IMinQTimeMethod minQTimeMethod;

    @Autowired
    private IAPCMethod apcMethod;

    @Autowired
    private IEquipmentContainerPositionMethod equipmentContainerPositionMethod;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IPlanInqService planInqService;

    @Autowired
    private IPlanService planService;

    @Autowired
    private ITCSMethod tcsMethod;

    @Override
    public Results.MoveOutReqResult sxMoveOutReq(Infos.ObjCommon objCommon, Params.OpeComWithDataReqParams param) {
        User user = param.getUser();
        ObjectIdentifier equipmentID = param.getEquipmentID();
        ObjectIdentifier controlJobID = param.getControlJobID();
        String transactionID = objCommon.getTransactionID();
        String requestTimeStr = CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp());

        if (log.isDebugEnabled()) {
            log.debug("step1 - Check Process");
        }
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, param.getEquipmentID());

        if (log.isDebugEnabled()) {
            log.debug("step2 - Get Started lot information which is specified with controljob ID");
        }
        Outputs.ObjControlJobStartReserveInformationOut cjStartRsrvInfo =
                controlJobMethod.controlJobStartReserveInformationGet(objCommon, controlJobID, false);

        if (log.isDebugEnabled()) {
            log.debug("step3 - get lock mode by equipment 【{}】", equipmentID);
        }
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        long lockMode = CimNumberUtils.longValue(objLockModeOut.getLockMode());
        if (log.isTraceEnabled()) {
            log.trace("lockMode is 【{}】", lockMode);
        }
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (log.isDebugEnabled()) {
                log.debug("step4 - lock equipment main object");
            }
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(Collections.emptyList());
            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("step5 - Lock objects to be updated ");
            }
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        List<Infos.StartCassette> startCassetteList = cjStartRsrvInfo.getStartCassetteList();

        if (log.isTraceEnabled()) {
            startCassetteList.stream()
                    .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                    .forEach(lotInCassette -> log.trace("moveInFlag is 【{}】", lotInCassette.getMoveInFlag()));
        }

        List<Infos.StartCassette> startCassettes = new ArrayList<>(startCassetteList);

        List<ObjectIdentifier> cassetteIDs = startCassettes.stream().peek(startCassette -> {
            if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                try {
                    equipmentMethod.equipmentMonitorCreationFlagGet(objCommon, equipmentID);
                } catch (ServiceException ex) {
                    //---------------------------------------------------------------------------------------------
                    //    if monitor creationflag was TRUE, at least one Lot should be existed in Empty Cassete.
                    //---------------------------------------------------------------------------------------------
                    if (Validations.isEquals(retCodeConfigEx.getMonitorCreatReqd(), ex.getCode())) {
                        if (CimObjectUtils.isEmpty(startCassette.getLotInCassetteList())) {
                            throw ex;
                        }
                    }
                }
            }
        }).map(Infos.StartCassette::getCassetteID).collect(Collectors.toList());

        List<ObjectIdentifier> lotIDsToLock = startCassettes.stream()
                // ignore the lots that is in a carrier with EMPTY as load purpose type
                .filter(startCassette -> !CimStringUtils.equals(startCassette.getLoadPurposeType(),
                        BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE))
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(Infos.LotInCassette::getMoveInFlag)
                .map(Infos.LotInCassette::getLotID)
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("step7 - virtual operation check by start cassette");
        }
        boolean virtualOperationFlag = virtualOperationMethod.virtualOperationCheckByStartCassette(objCommon, startCassettes);

        if (log.isDebugEnabled()) {
            log.debug("step8 - get equipment brinfo");
        }
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);

        if (log.isTraceEnabled()) {
            log.trace("lockMode is 【{}】", lockMode);
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (log.isDebugEnabled()) {
                log.debug("get equipment online mode");
            }
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            if (log.isTraceEnabled()) {
                log.trace("onlineMode is 【{}】", onlineMode);
            }
            if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                if (log.isDebugEnabled()) {
                    log.debug("step9 - lock advanced object");
                }
                Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvanceLockIn.setObjectID(equipmentID);
                objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT);
                objAdvanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_COUNT));
                objAdvanceLockIn.setKeyList(Collections.emptyList());
                objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
            }
            if (log.isDebugEnabled()) {
                log.debug("step11 - Lock eqp ProcLot Element");
            }
            List<String> procLotSeq = lotIDsToLock.stream().map(ObjectIdentifier::fetchValue).collect(Collectors.toList());
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT);
            objAdvanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
            objAdvanceLockIn.setKeyList(procLotSeq);
            objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

            // For wafer stacking, lock load cassette
            // removed by Yuri: postProcForLotFlag is obsolete, it would be always considered as 1, which would enable
            // the moveOutReq to user pattern 22 as the post process pattern and it is included as an post process task

            //*------------------------------*//
            //*   Lock controljob Object     *//
            //*------------------------------*//
            if (log.isDebugEnabled()) {
                log.debug("step13 - Lock controljob Object");
            }
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }
        /*------------------------------*/
        /*   Lock cassette/lot Object   */
        /*------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step14 - Lock cassette/lot Object");
        }
        cassetteIDs.removeIf(ObjectIdentifier::isEmpty);
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        if (log.isDebugEnabled()) {
            log.debug("step15 - lock sequence object");
        }
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDsToLock);

        if (log.isDebugEnabled()) {
            log.debug("step16 - add season by ho");
        }
        seasoningService.sxSeasonForMoveOut(objCommon, equipmentID);

        if (log.isDebugEnabled()) {
            log.debug("step17 - update used achine recipe time");
        }
        seasonMethod.updateMachineRecipeUsedTime(objCommon, startCassettes, equipmentID);

        /*-------------------------------------------------*/
        /*   call cassette_APCInformation_GetDR            */
        /*-------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step18 - call cassetteAPCInformationGetDR");
        }
        try {
            cassetteMethod.cassetteAPCInformationGetDR(objCommon, equipmentID, startCassettes);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())) {
                throw e;
            }
        }

        /*---------------------------*/
        /*   Collect move-out lots   */
        /*---------------------------*/
        List<Infos.LotInCassette> moveOutLots = startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                .collect(Collectors.toList());

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check Process                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        boolean slmCapabilityFlag = CimBooleanUtils.isTrue(eqpBrInfo.isFmcCapabilityFlag());

        if (log.isTraceEnabled()) {
            log.trace("SLM Capability is " + slmCapabilityFlag + ".");
        }
        boolean eqpCtnrInfoNotEmptyFlag = false;
        List<ObjectIdentifier> carrierIDsForFmc = new ArrayList<>();
        boolean isPartialMoveOutReq = CimStringUtils.equalsIn(transactionID,
                TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),
                TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
        if (log.isTraceEnabled()) {
            log.trace("The transaction is partial move out -> {}", isPartialMoveOutReq);
        }
        if (slmCapabilityFlag) {
            if (log.isDebugEnabled()) {
                log.debug("step19 - lock for equipment container position");
            }
            Inputs.ObjObjectLockForEquipmentContainerPositionIn EqpContainerPosLockIn = new Inputs
                    .ObjObjectLockForEquipmentContainerPositionIn();
            EqpContainerPosLockIn.setEquipmentID(equipmentID);
            EqpContainerPosLockIn.setControlJobID(controlJobID);
            objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, EqpContainerPosLockIn);

            if (log.isDebugEnabled()) {
                log.debug("step20 - get equipment container position info");
            }
            Inputs.ObjEquipmentContainerPositionInfoGetIn resCreateIn = new Inputs.ObjEquipmentContainerPositionInfoGetIn();
            resCreateIn.setEquipmentID(equipmentID);
            resCreateIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
            resCreateIn.setKey(controlJobID);
            Infos.EqpContainerPositionInfo eqpContainerPositionInfo = eqpContainerPosMethod
                    .equipmentContainerPositionInfoGet(objCommon, resCreateIn);
            List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerPositionInfo.getEqpContainerPositionList();
            if (log.isTraceEnabled()) {
                log.trace("TransactionID is 【{}】", transactionID);
            }
            if (isPartialMoveOutReq) {

                //----------------------------------------------------------
                //  Filtering for Partial OpeComp
                //----------------------------------------------------------
                List<Infos.EqpContainerPosition> strEqpContainerPositionSeq = eqpContainerPositionList.stream()
                        .filter(p -> ObjectIdentifier.isNotEmpty(p.getControlJobID()))
                        .collect(Collectors.toList());
                eqpContainerPositionInfo.setEqpContainerPositionList(strEqpContainerPositionSeq);
            }
            eqpCtnrInfoNotEmptyFlag = CimArrayUtils.isNotEmpty(eqpContainerPositionInfo.getEqpContainerPositionList());
            log.trace("eqpContainerPositionList is not empty【{}】", eqpCtnrInfoNotEmptyFlag);
            if (eqpCtnrInfoNotEmptyFlag) {
                List<ObjectIdentifier> carrierIDs = eqpContainerPositionInfo.getEqpContainerPositionList()
                        .stream().peek(info -> {
                            /*-----------------------------------------------------------------------*/
                            /*   Check SLM condition for EqpContainerPosition                        */
                            /*   The following conditions are checked by this object                 */
                            /*   - All SLM Status of Equipment container position which has relation */
                            /*     with ControlJob has NOT to be 'Stored'.                           */
                            /*-----------------------------------------------------------------------*/
                            if (log.isTraceEnabled()) {
                                log.trace("FmcState is 【{}】", info.getFmcState());
                            }
                            Validations.check(CimStringUtils.equals(info.getFmcState(), BizConstant.SP_SLMSTATE_STORED),
                                    new OmCode(retCodeConfig.getInvalidSLMStatusOfContainerPosition(),
                                            ObjectIdentifier.fetchValue(info.getContainerPositionID()), info.getFmcState()));
                        }).map(Infos.EqpContainerPosition::getDestCassetteID)
                        // --------------------------------------------------------------
                        // Get Destination Cassette List.
                        // These cassettes should be locked before update
                        // --------------------------------------------------------------
                        .filter(carrierID -> {
                            if (log.isTraceEnabled()) {
                                log.trace("DestCassetteID is 【{}】", carrierID);
                            }
                            return ObjectIdentifier.isNotEmpty(carrierID);
                        }).distinct()
                        .peek(carrierID -> objectLockMethod.objectLock(objCommon, CimCassette.class, carrierID))
                        .collect(Collectors.toList());
                if (log.isDebugEnabled()) {
                    log.debug("step21 - lock the cassette");
                }
                carrierIDsForFmc.addAll(carrierIDs);
            }
        }

        /*----------------------------------------*/
        /*   Get eqp'requestTimeStr Online Mode   */
        /*----------------------------------------*/
        if (log.isTraceEnabled()) {
            log.trace("startCassettes size is 【{}】", CimArrayUtils.isEmpty(startCassettes));
        }
        if (startCassettes.size() <= 0) {
            if (log.isErrorEnabled()) {
                log.error("(There is no started cassette.)");
            }
            throw new ServiceException(retCodeConfig.getNotFoundCassette());
        }

        // 检查并设置Min Q-Time最小时间限制
        minQTimeMethod.checkAndSetRestrictions(objCommon, startCassettes);

        Outputs.ObjPortResourceCurrentOperationModeGetOut portCurrentOperationModeOut;
        if (log.isTraceEnabled()) {
            log.trace("virtualOperationFlag is 【{}】", virtualOperationFlag);
        }
        if (!virtualOperationFlag) {
            if (log.isDebugEnabled()) {
                log.debug("step22 - get port resource current operation mode");
            }
            ObjectIdentifier unloadPortID = startCassettes.get(0).getUnloadPortID();
            portCurrentOperationModeOut = portMethod.portResourceCurrentOperationModeGet(objCommon, equipmentID, unloadPortID);

            /*----------------------------------------------------------------------------*/
            /*   If AccessMode is Auto or OnlineMode is Offline, do the following check.  */
            /*----------------------------------------------------------------------------*/
            Infos.OperationMode operationMode = portCurrentOperationModeOut.getOperationMode();
            if (log.isTraceEnabled()) {
                log.trace("OnlineMode is 【{}】，AccessMode is 【{}】", operationMode.getOnlineMode(),
                        operationMode.getAccessMode());
            }
            if (CimStringUtils.equals(operationMode.getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                    || CimStringUtils.equals(operationMode.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                /*-----------------------------------------------------------------------*/
                /*   Check Process for cassette                                          */
                /*   The following conditions are checked by this object                 */
                /*   - transferState                                                     */
                /*-----------------------------------------------------------------------*/

                if (log.isDebugEnabled()) {
                    log.debug("step23 - check cassette condition for operation Comp");
                }
                cassetteMethod.cassetteCheckConditionForOpeComp(objCommon, startCassettes);

                /*-----------------------------------------------------------------------*/
                /*                                                                       */
                /*   Check eqp port for OpeComp                                    */
                /*                                                                       */
                /*   The following conditions are checked by this object                 */
                /*                                                                       */
                /*   - All of cassette, which is contained in controlJob, must be on     */
                /*     the eqp'requestTimeStr unloadingPort.                             */
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                if (log.isDebugEnabled()) {
                    log.debug("step24 - check equipment port state for operation comp");
                }
                equipmentMethod.equipmentPortStateCheckForOpeComp(objCommon, equipmentID, startCassettes);

            }
        } else {
            //init empty
            portCurrentOperationModeOut = new Outputs.ObjPortResourceCurrentOperationModeGetOut();

            //【bug-2307】virtual operation ：move Out 未成功，出现报错提示
            Infos.OperationMode operationMode = new Infos.OperationMode();
            portCurrentOperationModeOut.setOperationMode(operationMode);
        }
        if (log.isTraceEnabled()) {
            log.trace("portCurrentOperationModeOut is 【{}】", portCurrentOperationModeOut);
        }
        /*-----------------------------------------------------------------------*/
        /*   Check Process for lot                                               */
        /*   The following conditions are checked by this object                 */
        /*   - lotProcessState                                                   */
        /*-----------------------------------------------------------------------*/

        if (log.isDebugEnabled()) {
            log.debug("step25 - check lot condition for operation comp");
        }
        lotMethod.lotCheckConditionForOpeComp(objCommon, startCassettes);

        /*-----------------------------------------------------------------------*/
        /*   Check Process for eqp                                         */
        /*   The following conditions are checked by this object                 */
        /*   - All of lot which is contained in controlJob must be existing      */
        /*     in the eqp'requestTimeStr processing information.                        */
        /*-----------------------------------------------------------------------*/
        if (log.isTraceEnabled()) {
            log.trace("virtualOperationFlag is 【{}】", virtualOperationFlag);
        }
        if (!virtualOperationFlag) {
            if (log.isDebugEnabled()) {
                log.debug("step26 - check equipment condition operation comp");
            }
            equipmentMethod.equipmentCheckConditionForOpeComp(objCommon, equipmentID, startCassettes);
        }

        /*--------------------------------*/
        /*   CP Test Function Procedure   */
        /*--------------------------------*/
        moveOutLots.stream().map(Infos.LotInCassette::getLotID).forEach(lotID -> {
            ObjectIdentifier lotTestTypeID = ObjectIdentifier.emptyIdentifier();
            try {
                lotTestTypeID = lotMethod.lotTestTypeIDGet(objCommon, lotID);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundTestType(), e.getCode())) {
                    if (log.isInfoEnabled()) {
                        log.error("lot_testTypeID_Get() != RC_OK");
                    }
                    throw e;
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("objLotTestTypeIDGetOut is 【{}】", lotTestTypeID);
            }
            if (ObjectIdentifier.isNotEmpty(lotTestTypeID)) {
                /*---------------------------------------------------------------*/
                /*   Gather Bin Summary Information based on lotID & testTypeID  */
                /*---------------------------------------------------------------*/
                log.debug("step29 - get bin summary by test type");
                List<Infos.WaferBinSummary> waferBinSummaryList = binSummaryMethod.binSummaryGetByTestTypeDR(objCommon,
                        lotID, lotTestTypeID);

                /*----------------------------------------------------------*/
                /*   Update wafer Die quantity based on the input parameter */
                /*----------------------------------------------------------*/
                if (CimArrayUtils.isNotEmpty(waferBinSummaryList)) {
                    List<Infos.LotWaferAttributes> lotWaferAttributes = waferBinSummaryList.stream()
                            .filter(waferBinSummary -> waferBinSummary.getBinReportCount() > 0)
                            .map(waferBinSummary -> {
                                Infos.LotWaferAttributes lotWaferAttribute = new Infos.LotWaferAttributes();
                                lotWaferAttribute.setWaferID(waferBinSummary.getWaferId());
                                lotWaferAttribute.setGoodUnitCount(waferBinSummary.getGoodUnitCount());
                                lotWaferAttribute.setRepairUnitCount(waferBinSummary.getRepairUnitCount());
                                lotWaferAttribute.setFailUnitCount(waferBinSummary.getFailUnitCount());
                                return lotWaferAttribute;
                            }).collect(Collectors.toList());
                    if (log.isDebugEnabled()) {
                        log.debug("step30 - change lot wafer");
                    }
                    lotMethod.lotWaferChangeDie(objCommon, lotID, lotWaferAttributes);
                }
            }
        });

        /*------------------------------------------------------*/
        /*                                                      */
        /*     flowbatch Related Information Update Procedure   */
        /*                                                      */
        /*------------------------------------------------------*/
        /*------------------------------------------------------*/
        /*   Update flowbatch Information of eqp          */
        /*------------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step31 - update flow batch information by operation comp");
        }
        flowBatchMethod.flowBatchInformationUpdateByOpeComp(objCommon, equipmentID, startCassettes);


        /*----------------------------------------------*/
        /*   Process Operation Update Procedure         */
        /*----------------------------------------------*/
        /*----------------------------------------------*/
        /*  Update Process Operation (actual comp xxxx) */
        /*----------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step32 - set process actual comp information");
        }
        processMethod.processActualCompInformationSet(objCommon, startCassettes);

        /*------------------------------------------------------------*/
        /*   Reticle / Fixture Related Information Update Procedure   */
        /*------------------------------------------------------------*/
        /*--------------------------------------------------------*/
        /*   Check ProcessDurable was Used for Operation or Not   */
        /*--------------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step38 - get equipment process durable required flag");
        }
        try {
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
        } catch (ServiceException e) {
            Integer errorCode = e.getCode();
            if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), errorCode)) {
                if (log.isTraceEnabled()) {
                    log.trace("equipment_processDurableRequiredFlag_Get() == RC_EQP_PROCDRBL_NOT_REQD");
                }
            } else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), errorCode)
                    || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), errorCode)) {
                if (log.isTraceEnabled()) {
                    log.trace("equipment_processDurableRequiredFlag_Get() == RC_EQP_PROCDRBL_RTCL_REQD || RC_EQP_PROCDRBL_FIXT_REQD");
                }
                moveOutLots.forEach(lotInCassette -> {
                    if (Validations.isEquals(errorCode, retCodeConfig.getEquipmentProcessDurableReticleRequired())) {
                        /*-------------------------------*/
                        /*   Get Used Reticles for lot   */
                        /*-------------------------------*/
                        if (log.isDebugEnabled()) {
                            log.debug("step39 - get process assigned reticle");
                        }
                        processMethod.processAssignedReticleGet(objCommon, lotInCassette.getLotID()).stream()
                                .peek(startReticle -> {
                                    /*---------------------------------------------*/
                                    /*  Set last used time stamp for used reticle  */
                                    /*---------------------------------------------*/
                                    if (log.isDebugEnabled()) {
                                        log.debug("step42 - Set last used time stamp for used reticle");
                                    }
                                    reticleMethod.reticleLastUsedTimeSet(startReticle.getReticleID(),
                                            objCommon.getTimeStamp().getReportTimeStamp());
                                })
                                .map(startReticle -> reticleMethod.reticleUsageLimitationCheck(objCommon,
                                        startReticle.getReticleID()))
                                .filter(usageCheck -> {
                                    if (log.isTraceEnabled()) {
                                        log.trace("UsageLimitOverFlag is 【{}】", usageCheck.isUsageLimitOverFlag());
                                    }
                                    return usageCheck.isUsageLimitOverFlag();
                                })
                                .map(Outputs.ObjReticleUsageLimitationCheckOut::getMessageText)
                                .forEach(messageText -> {
                                    if (log.isInfoEnabled()) {
                                        log.info("strFixture_usageLimitation_Check_out.usageLimitOverFlag == TRUE");
                                    }
                                    /*-------------------------*/
                                    /*   Call System Message   */
                                    /*-------------------------*/
                                    if (log.isDebugEnabled()) {
                                        log.debug("step41- Call System Message");
                                    }
                                    callSxAlertMessageRpt(objCommon, requestTimeStr, messageText,
                                            BizConstant.SP_SYSTEMMSGCODE_RTCLUSAGELIMITOVER);
                                });
                    } else {
                        /*------------------------------*/
                        /*   Get Used Fixture for lot   */
                        /*------------------------------*/
                        log.debug("step43 - Get Used Fixture for lot");
                        processMethod.processAssignedFixtureGet(objCommon, lotInCassette.getLotID()).stream()
                                .map(startFixture -> fixtureMethod.fixtureUsageLimitationCheck(objCommon,
                                        lotInCassette.getLotID()))
                                .filter(usageCheck -> {
                                    if (log.isTraceEnabled()) {
                                        log.trace("UsageLimitOverFlag is 【{}】", usageCheck.isUsageLimitOverFlag());
                                    }
                                    return usageCheck.isUsageLimitOverFlag();
                                })
                                .map(Outputs.objFixtureUsageLimitationCheckOut::getMessageText)
                                .forEach(messageText -> {
                                    if (log.isInfoEnabled()) {
                                        log.info("strFixture_usageLimitation_Check_out.usageLimitOverFlag == TRUE");
                                    }
                                    /*-------------------------*/
                                    /*   Call System Message   */
                                    /*-------------------------*/
                                    if (log.isDebugEnabled()) {
                                        log.debug("step41- Call System Message");
                                    }
                                    callSxAlertMessageRpt(objCommon, requestTimeStr, messageText,
                                            BizConstant.SP_SYSTEMMSGCODE_RTCLUSAGELIMITOVER);
                                });
                    }
                });
            } else {
                log.trace("equipment_processDurableRequiredFlag_Get() != RC_OK");
                throw e;
            }
        }

        /*-----------------------------------------*/
        /*   Change lot Process State to Waiting   */
        /*-----------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step50 - Change lot Process State to Waiting");
        }
        lotMethod.lotProcessStateMakeWaiting(objCommon, startCassettes);

        /*--------------------------------------------*/
        /*   Change lot Pending Move Next Flag true   */
        /*--------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step51 - set lot's pending move next flag to true");
        }
        moveOutLots.stream().map(Infos.LotInCassette::getLotID)
                .forEach(lotID -> lotMethod.setLotMoveNextRequired(objCommon, lotID, true));

        /*------------------------------------------------------------------------------------*/
        /*   Prepare for PO Moving                                                            */
        /*   see {@link com.fa.cim.frameworks.pprocess.executor.impl.LotProcessMoveExecutor}  */
        /*------------------------------------------------------------------------------------*/
        List<ObjectIdentifier> operationStartLotIds = moveOutLots.stream().map(Infos.LotInCassette::getLotID)
                .peek(lotID -> {
                    /*-------------------------------------*/
                    /*   Set Contamination Level for Lot   */
                    /*-------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step51 - update contamination flag");
                    }
                    contaminationMethod.lotContaminationLevelAndPrFlagSet(lotID);
                })
                .peek(lotID -> {
                    /*-----------------------------------------*/
                    /*   Set Contamination Level for Carrier   */
                    /*-----------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step55 - lotCassetteCategoryUpdateForContaminationControl");
                    }
                    lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);
                }).collect(Collectors.toList());

        /*----------------------------------------------------*/
        /*   eqp Related Information Update Procedure   */
        /*----------------------------------------------------*/
        if (log.isTraceEnabled()) {
            log.trace("virtualOperationFlag is 【{}】", virtualOperationFlag);
        }
        if (!virtualOperationFlag) {
            /*-----------------------------------------------------------------------------*/
            /*   Remove ControlJobLot from EqpInfo'requestTimeStr ProcessingLot Sequence   */
            /*-----------------------------------------------------------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("step65 - delete equipment processing lot");
            }
            equipmentMethod.equipmentProcessingLotDelete(objCommon, param.getEquipmentID(), startCassettes);
        }

        /*----------------------------------------------------------*/
        /*   Maintain Eqp'requestTimeStr Status when OFF-LINE Mode  */
        /*----------------------------------------------------------*/
        String onlineMode = portCurrentOperationModeOut.getOperationMode().getOnlineMode();
        if (log.isTraceEnabled()) {
            log.trace("OnlineMode is 【{}】", onlineMode);
        }
        if (BizConstant.SP_EQP_ONLINEMODE_OFFLINE.equals(onlineMode)) {
            /*---------------------------------------------------------------*/
            /*   Change eqp'requestTimeStr Status to 'STANDBY' if necessary  */
            /*---------------------------------------------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("step66 - Change eqp'requestTimeStr Status to 'STANDBY' if necessary");
            }
            Boolean eqpCurrentStateCheck = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon, equipmentID);

            if (log.isTraceEnabled()) {
                log.trace("eqpCurrentStateCheck is 【{}】", eqpCurrentStateCheck);
            }

            if (CimBooleanUtils.isTrue(eqpCurrentStateCheck)) {
                /*===== get Default Status CimCode for Productive / Standby ===*/
                if (log.isDebugEnabled()) {
                    log.debug("step67 - get equipment recover state");
                }
                ObjectIdentifier eqpRecoverStateGetMfg = equipmentMethod.equipmentRecoverStateGetManufacturing(objCommon,
                        equipmentID);

                if (log.isDebugEnabled()) {
                    log.debug("step68 - change equipment status");
                }
                try {
                    equipmentService.sxEqpStatusChangeReq(objCommon, equipmentID, eqpRecoverStateGetMfg,
                            param.getOpeMemo());
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getInvalidStateTrans(), e.getCode())) {
                        throw e;
                    }
                }
            }
        }

        /*--------------------------------------*/
        /*   eqp Usage Limitation Check   */
        /*--------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step69 - eqp Usage Limitation Check");
        }
        Outputs.ObjEquipmentUsageLimitationCheckOut eqpUsageLimitCheck = equipmentMethod
                .equipmentUsageLimitationCheck(objCommon, equipmentID);

        if (log.isTraceEnabled()) {
            log.trace("UsageLimitOverFlag is 【{}】", eqpUsageLimitCheck.isUsageLimitOverFlag());
        }
        if (CimBooleanUtils.isTrue(eqpUsageLimitCheck.isUsageLimitOverFlag())) {
            /*-------------------------*/
            /*   Call System Message   */
            /*-------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("step70 - Call System Message ");
            }
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EQPUSAGELIMITOVER);
            alertMessageRptParams.setSystemMessageText(eqpUsageLimitCheck.getMessageText());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(param.getEquipmentID());
            alertMessageRptParams.setSystemMessageTimeStamp(requestTimeStr);
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }

        List<ObjectIdentifier> topLotIDs = findTopLotIDs(objCommon, equipmentID, controlJobID);
        moveOutLots.stream()
                .map(Infos.LotInCassette::getLotID)
                .filter(lotID -> topLotIDs.stream().noneMatch(topLotID -> topLotID.equals(lotID)))
                .forEach(lotID -> {
                    Params.ProcessControlScriptRunReqParams scriptParams = new Params.ProcessControlScriptRunReqParams();
                    scriptParams.setEquipmentId(equipmentID);
                    scriptParams.setLotId(lotID);
                    scriptParams.setPhase(BizConstant.SP_BRSCRIPT_POST);
                    scriptParams.setUser(objCommon.getUser());
                    processControlScriptService.sxProcessControlScriptRunReq(objCommon, scriptParams);
                });

        /*-----------------------------------------------------------------------*/
        /*                          Clear container position                     */
        /*-----------------------------------------------------------------------*/
        if (eqpCtnrInfoNotEmptyFlag) {
            Inputs.ObjEquipmentContainerPositionInfoClearIn eqpContainerPosIn = new Inputs.ObjEquipmentContainerPositionInfoClearIn();
            eqpContainerPosIn.setEquipmentID(param.getEquipmentID());
            eqpContainerPosIn.setKey(param.getControlJobID());
            eqpContainerPosIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
            if (log.isDebugEnabled()) {
                log.debug("step71 - equipmentContainerPositionInfoClear");
            }
            eqpContainerPosMethod.equipmentContainerPositionInfoClear(objCommon, eqpContainerPosIn);
        }

        /*-----------------------------------------------------------------------*/
        /*   Delete SLM reservation from container position                      */
        /*-----------------------------------------------------------------------*/
        if (log.isTraceEnabled()) {
            log.trace("destCassetteLength is 【{}】", CimArrayUtils.getSize(carrierIDsForFmc));
        }
        if (CimArrayUtils.isNotEmpty(carrierIDsForFmc)) {
            if (log.isDebugEnabled()) {
                log.debug("step72 - carrierFmcReserveEquipmentSet");
            }
            carrierIDsForFmc.forEach(carrierID -> cassetteMethod.cassetteSLMReserveEquipmentSet(objCommon,
                    carrierID, null));
        }

        /*---------------------------------------------------*/
        /*   cassette Related Information Update Procedure   */
        /*---------------------------------------------------*/
        startCassetteList.stream()
                .map(Infos.StartCassette::getCassetteID)
                .filter(ObjectIdentifier::isNotEmpty)
                .forEach(carrierID -> {
                    if (log.isDebugEnabled()) {
                        log.debug("step74 - carrier Usage Limitation Check");
                    }
                    Outputs.ObjCassetteUsageLimitationCheckOut limitCheckOut = cassetteMethod
                            .cassetteUsageLimitationCheck(objCommon, carrierID);

                    if (limitCheckOut.isUsageLimitOverFlag()) {
                        if (log.isDebugEnabled()) {
                            log.debug("step75 - alert message");
                        }
                        callSxAlertMessageRpt(objCommon, requestTimeStr, limitCheckOut.getMessageText(),
                                BizConstant.SP_SYSTEMMSGCODE_CASTUSAGELIMITOVER);
                    }

                    /*----------------------------------------------------*/
                    /*   Update cassette'requestTimeStr MultiLotType      */
                    /*----------------------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step76 - Update cassette'requestTimeStr MultiLotType");
                    }
                    cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, carrierID);
                });

        /*--------------------------*/
        /*   Event Make Procedure   */
        /*--------------------------*/
        String eventId = isPartialMoveOutReq ? transactionID : TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue();

        startCassetteList.stream().flatMap(startCassette -> {
                    ObjectIdentifier carrierID = startCassette.getCassetteID();
                    return startCassette.getLotInCassetteList().stream()
                            .filter(Infos.LotInCassette::getMoveInFlag)
                            .map(lotInCassette -> {
                                Inputs.LotOperationMoveEventMakeOpeComp moveInEvnet = new Inputs.LotOperationMoveEventMakeOpeComp();
                                moveInEvnet.setTransactionID(eventId);
                                moveInEvnet.setEquipmentID(param.getEquipmentID());
                                ObjectIdentifier operationModeID = portCurrentOperationModeOut.getOperationMode().getOperationMode();
                                moveInEvnet.setOperationMode(ObjectIdentifier.fetchValue(operationModeID));
                                moveInEvnet.setControlJobID(param.getControlJobID());
                                moveInEvnet.setCassetteID(carrierID);
                                moveInEvnet.setLotInCassette(lotInCassette);
                                moveInEvnet.setClaimMemo(param.getOpeMemo());
                                return moveInEvnet;
                            });
                })
                .forEach(event -> eventMethod.lotOperationMoveEventMakeOpeComp(objCommon, event));

        //-------------------------------------------------------------------
        // remove the cj info
        // only delete the cj when the transaction is not partial move out
        //-------------------------------------------------------------------
        if (!isPartialMoveOutReq) {
            if (log.isDebugEnabled()) {
                log.debug("step84 - change CJ status,remove the cj info from EQP");
            }
            if (ObjectIdentifier.isNotEmpty(controlJobID)) {
                Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
                cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP);
                cjStatusChangeReqParams.setControlJobID(controlJobID);
                cjStatusChangeReqParams.setClaimMemo(param.getOpeMemo());
                controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);
            }
        }

        if (!isPartialMoveOutReq) {
            //---------------------------------------------//
            //   Delete Control Job From Lot and Cassette  //
            //---------------------------------------------//
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobID(controlJobID);
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE);
            cjStatusChangeReqParams.setClaimMemo(param.getOpeMemo());

            if (log.isDebugEnabled()) {
                log.debug("step85 - change CJ status,remove the cj form Lot and Cassette");
            }
            controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);
        }

        /*------------------------------------------------------------------------*/
        /*   Send OpeComp Request to TCS Procedure                                */
        /*                                                                        */
        /*   - If specified portGroup'requestTimeStr operationCompMode is Manual, OpeComp trx  */
        /*     must be sent to TCS. In TCS, if "ProcessEnd" report is not come    */
        /*     from eqp yet, OpeComp trx from OMS is rejected by TCS.             */
        /*------------------------------------------------------------------------*/
        if (CimStringUtils.equals(BizConstant.SP_EQP_COMPMODE_MANUAL,
                portCurrentOperationModeOut.getOperationMode().getMoveOutMode())) {
            if (isPartialMoveOutReq) {
                try {
                    Optional.ofNullable(eapMethod.eapRemoteManager(objCommon, user, equipmentID, null, true))
                            .ifPresent(eapRemoteManager -> eapRemoteManager.sendMoveOutReq(param));
                } catch (CimIntegrationException e) {
                    throw new ServiceException(new OmCode(CimNumberUtils.intValue(e.getCode()), e.getMessage()));
                }
            }
        }

        /*---------------------------------------------------*/
        /*  Update Reticle'requestTimeStr LastUsedTimestamp  */
        /*---------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step95 - Update Reticle'requestTimeStr LastUsedTimestamp");
        }
        reticleMethod.reticleLastUsedTimeStampUpdate(objCommon, startCassetteList);

        if (log.isDebugEnabled()) {
            log.debug("step96 - change constraint exception lot for operation comp");
        }
        List<ObjectIdentifier> lotIDList = new ArrayList<>(operationStartLotIds);
        constraintMethod.constraintExceptionLotChangeForOpeComp(objCommon, lotIDList, controlJobID);

        /*----------------------*/
        /*                      */
        /*   Return to Caller   */
        /*                      */
        /*----------------------*/
        Results.MoveOutReqResult moveOutReqResult = new Results.MoveOutReqResult();
        // Check this is need to EDC check
        final boolean specCheckRequiredFlag = startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(Infos.LotInCassette::getMoveInFlag)
                .anyMatch(lotInCassette -> lotInCassette.getStartRecipe().getDataCollectionFlag());
        if (specCheckRequiredFlag && BizConstant.SP_EQP_ONLINEMODE_OFFLINE.equals(onlineMode)) {
            // Get EDC check result for Lots.
            final Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut edcInformationGets =
                    lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon,
                            equipmentID,
                            operationStartLotIds);

            // Get the MoveOut Lot
            moveOutReqResult = equipmentMethod.equipmentFillInTxTRC004(objCommon,
                    edcInformationGets.getStrStartCassette(),
                    null);
        } else {
            moveOutReqResult.setMoveOutLot(operationStartLotIds.stream()
                    .map(loID -> {
                        Infos.OpeCompLot opeCompLot = new Infos.OpeCompLot();
                        opeCompLot.setLotID(loID);
                        return opeCompLot;
                    }).collect(Collectors.toList()));
        }

        if (StandardProperties.OM_RTMS_CHECK_ACTIVE.isTrue()){
            //report RTMS
            for (Infos.StartCassette startCassette : startCassetteList){
                List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                    if (lotInCassette.getMoveInFlag()){
                        List<String> reticleList = new ArrayList<>();
                        Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                        List<Infos.StartReticleInfo> startReticleList = startRecipe.getStartReticleList();
                        if (CimArrayUtils.isNotEmpty(startReticleList)) {
                            for (Infos.StartReticleInfo startReticleInfo : startReticleList){
                                reticleList.add(startReticleInfo.getReticleID().getValue());
                            }
                        }
                        if (CimArrayUtils.isNotEmpty(reticleList)) {
                            reticleMethod.reticleUsageCheckByRTMS(objCommon,BizConstant.RTMS_RETICLE_CHECK_ACTION_MOVE_OUT,
                                    reticleList,lotInCassette.getLotID(),equipmentID);
                        }
                    }
                }
            }
        }




        if (log.isDebugEnabled()) {
            log.debug("step97 - check contamination level and pr flag after move out");
        }
        List<ObjectIdentifier> lotIds = startCassettes.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream()
                        .map(Infos.LotInCassette::getLotID))
                .collect(Collectors.toList());
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepOut(objCommon, lotIds, equipmentID);

        if (log.isDebugEnabled()) {
            log.debug("step98 - add pilot run");
        }
        pilotRunMethod.checkPilotRunCompleted(objCommon, startCassettes);
        return moveOutReqResult;
    }

    private void callSxAlertMessageRpt(Infos.ObjCommon objCommon, String requestTimeStr,
                                       String messageText, String systemMessageCode) {
        Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
        alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
        alertMessageRptParams.setSystemMessageCode(systemMessageCode);
        alertMessageRptParams.setSystemMessageText(messageText);
        alertMessageRptParams.setNotifyFlag(true);
        alertMessageRptParams.setSystemMessageTimeStamp(requestTimeStr);
        systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
    }

    private List<ObjectIdentifier> findTopLotIDs(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                 ObjectIdentifier controlJobID) {
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        if (log.isTraceEnabled()) {
            log.trace("::EquipmentCategory == {}", eqpBrInfo.getEquipmentCategory());
        }
        if (log.isTraceEnabled()) {
            log.trace("::EquipmentCategory == {}", eqpBrInfo.getEquipmentCategory());
        }
        boolean bondingEqpFlag = BizConstant.SP_MC_CATEGORY_WAFERBONDING.equals(eqpBrInfo.getEquipmentCategory());
        if (bondingEqpFlag && log.isTraceEnabled()) {
            log.trace("eqp Category is SP_Mc_Category_WaferBonding.");
        }

        return Optional.of(bondingEqpFlag).filter(CimBooleanUtils::isTrue)
                .map(bondingFlag -> findBondingInfo(objCommon, equipmentID, controlJobID))
                .map(Outputs.ObjBondingGroupInfoByEqpGetDROut::getTopLotIDSeq)
                .orElseGet(Collections::emptyList);
    }

    private Outputs.ObjBondingGroupInfoByEqpGetDROut findBondingInfo(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                                     ObjectIdentifier controlJobID) {
        if (log.isDebugEnabled()) {
            log.debug("Step 3 - get bonding group info ");
        }
        Outputs.ObjBondingGroupInfoByEqpGetDROut bondingGrpInfo = bondingGroupMethod.bondingGroupInfoByEqpGetDR(
                objCommon, equipmentID, controlJobID, true);

        List<Infos.BondingGroupInfo> bondingGroupInfoList = bondingGrpInfo.getBondingGroupInfoList();
        if (CimArrayUtils.isNotEmpty(bondingGroupInfoList)) {
            bondingGroupInfoList.stream()
                    .peek(info -> Validations.check(!CimStringUtils.equals(info.getBondingGroupState(),
                                    BizConstant.SP_BONDINGGROUPSTATE_PROCESSED),
                            new OmCode(retCodeConfigEx.getBondgrpStateInvalid(), "MoveOut")))
                    .flatMap(info -> info.getBondingMapInfoList().stream())
                    .forEach(mapInfo -> Validations.check(!CimStringUtils.equals(mapInfo.getBondingProcessState(),
                                    BizConstant.SP_BONDINGPROCESSSTATE_COMPLETED),
                            new OmCode(retCodeConfigEx.getBondmapStateInvalid(),
                                    mapInfo.getBondingProcessState(),
                                    ObjectIdentifier.fetchValue(mapInfo.getBaseWaferID()))));
        }
        return bondingGrpInfo;
    }

    @Override
    public Results.MoveOutReqResult sxMoveOutForIBReq(Infos.ObjCommon objCommon, Params.MoveOutForIBReqParams params) {
        if (log.isDebugEnabled()) {
            log.debug("【Method Entry】sxMoveOutForIBReq()");
        }
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();

        if (log.isDebugEnabled()) {
            log.debug("step1 - Transaction ID and eqp Category Consistency Check");
        }
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        if (log.isDebugEnabled()) {
            log.debug("step2 - Get Started lot information which is sepcified with controljob ID");
        }
        Outputs.ObjControlJobStartReserveInformationOut controlJobStartReserveInformationOut =
                controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                        controlJobID,
                        false);

        if (log.isDebugEnabled()) {
            log.debug("step3 - Get required equipment lock mode");
        }

        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        long lockMode = objLockModeOut.getLockMode();
        if (log.isTraceEnabled()) {
            log.trace("lockMode is {}", lockMode);
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (log.isDebugEnabled()) {
                log.debug("step4 - add required equipment lock");
            }
            objectLockMethod.advancedObjectLock(objCommon,
                    new Inputs.ObjAdvanceLockIn(equipmentID,
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(),
                            new ArrayList<>()));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("step5 - add object lock of equipment");
            }
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        controlJobStartReserveInformationOut.getStartCassetteList().forEach(startCassette -> {
            if (log.isDebugEnabled()) {
                log.debug("LoadPurposeType is {}", startCassette.getLoadPurposeType());
            }
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassette.getLoadPurposeType())) {
                //equipment_monitorCreationFlag_Get();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("step6 - check equipment monitor creation flag");
                    }
                    equipmentMethod.equipmentMonitorCreationFlagGet(objCommon, equipmentID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getMonitorCreatReqd(), e.getCode())) {
                        //---------------------------------------------------------------------------------------------
                        //if monitor creationflag was TRUE, at least one lot should be existed in Empty Cassete;
                        //---------------------------------------------------------------------------------------------
                        if (CimArrayUtils.isEmpty(startCassette.getLotInCassetteList())) {
                            log.error("object_Lock() == RC_MONITOR_CREAT_REQD");
                            Validations.check(retCodeConfigEx.getMonitorCreatReqd());
                        }
                    }
                }
                cassetteIDs.add(startCassette.getCassetteID());
                return;
            }
            cassetteIDs.add(startCassette.getCassetteID());
            final List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            lotInCassetteList.stream()
                    .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                    .forEach(lotInCassette -> lotIDs.add(lotInCassette.getLotID()));
        });

        if (log.isTraceEnabled()) {
            log.trace("lockMode is {}", lockMode);
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (log.isDebugEnabled()) {
                log.debug("step7 - get equipment online mode from equipment {}", equipmentID);
            }
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            if (log.isTraceEnabled()) {
                log.trace("onlineMode is {}", onlineMode);
            }
            if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                if (log.isDebugEnabled()) {
                    log.debug("step8 - Lock eqp ProcLot Element (Count)");
                }
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_COUNT, new ArrayList<>()));
            }
            if (log.isDebugEnabled()) {
                log.debug("step9 - Lock eqp ProcLot Element (Write)");
            }
            List<String> procLotSeq = lotIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, procLotSeq));
        }
        // lockMode == SP_EQP_LOCK_MODE_WRITE
        else {
            if (log.isDebugEnabled()) {
                log.debug("lockMode == SP_EQP_LOCK_MODE_WRITE");
                log.debug("step10 - Lock All port Object for internal Buffer eqp;");
            }
            Infos.EqpPortInfo portInfoRetCode = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon,
                    equipmentID);

            if (log.isDebugEnabled()) {
                log.debug("step11 - lock equipment resource");
            }
            int lenPortInfo = CimArrayUtils.getSize(portInfoRetCode.getEqpPortStatuses());
            for (int i = 0; i < lenPortInfo; i++) {
                Infos.EqpPortStatus eqpPortStatus = portInfoRetCode.getEqpPortStatuses().get(i);
                objectLockMethod.objectLockForEquipmentResource(objCommon,
                        equipmentID,
                        eqpPortStatus.getPortID(),
                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (log.isDebugEnabled()) {
                log.debug("step12 - Lock Material Location");
            }
            for (Infos.StartCassette startCassette : controlJobStartReserveInformationOut.getStartCassetteList()) {
                Inputs.ObjAdvancedObjectLockForEquipmentResourceIn inputs =
                        new Inputs.ObjAdvancedObjectLockForEquipmentResourceIn();
                inputs.setEquipmentID(equipmentID);
                inputs.setClassName(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_BYCJ);
                inputs.setObjectID(controlJobID);
                inputs.setObjectLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                inputs.setBufferResourceName(startCassette.getLoadPurposeType());
                inputs.setBufferResourceLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ);
                objectLockMethod.advancedObjectLockForEquipmentResource(objCommon, inputs);
            }

            if (log.isDebugEnabled()) {
                log.debug("step13 - Lock controljob Object");
            }
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }

        if (log.isDebugEnabled()) {
            log.debug("step14 - lock cassette object");
        }
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        if (log.isDebugEnabled()) {
            log.debug("step15 - lock lot object");
        }
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        //---------------------------
        // Object lock end.
        //---------------------------

        // Get start cassette info
        List<Infos.StartCassette> startCassetteList = controlJobStartReserveInformationOut.getStartCassetteList();

        // Gets all actual move lot
        final List<Infos.LotInCassette> moveOutLots = startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                .collect(Collectors.toList());


        if (log.isDebugEnabled()) {
            log.debug("step16 - update season of the move out");
        }
        seasoningService.sxSeasonForMoveOut(objCommon, equipmentID);

        if (log.isDebugEnabled()) {
            log.debug("step17 - update machine recipe used time");
        }
        seasonMethod.updateMachineRecipeUsedTime(objCommon, startCassetteList, equipmentID);

        if (log.isDebugEnabled()) {
            log.debug("step18 - call cassetteAPCInformationGetDR");
        }
        List<Infos.ApcBaseCassette> apcBaseCassettes;
        try {
            apcBaseCassettes = cassetteMethod.cassetteAPCInformationGetDR(objCommon,
                    equipmentID,
                    startCassetteList);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())) {
                throw e;
            } else {
                apcBaseCassettes = Collections.emptyList();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("step19 - Get and Check cassette on port");
        }
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);

        // Check length of eqp port
        Validations.check(CimArrayUtils.isEmpty(eqpPortInfo.getEqpPortStatuses()), retCodeConfig.getNotFoundPort());
        ObjectIdentifier portID = eqpPortInfo.getEqpPortStatuses().get(0).getPortID();

        if (log.isDebugEnabled()) {
            log.debug("step20 - get equipment's online mode");
        }
        Outputs.ObjPortResourceCurrentOperationModeGetOut operationModeRetCode =
                portMethod.portResourceCurrentOperationModeGet(objCommon, equipmentID, portID);

        // If AccessMode is Auto or OnlineMode is Offline, do the following check;
        final Infos.OperationMode operationMode = operationModeRetCode.getOperationMode();
        if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, operationMode.getOnlineMode())
                || CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, operationMode.getAccessMode())) {
            if (log.isDebugEnabled()) {
                log.debug("step21 - Check Process for cassette");
            }
            cassetteMethod.cassetteCheckConditionForOpeComp(objCommon, startCassetteList);

            if (log.isDebugEnabled()) {
                log.debug("step22 - Check eqp port for OpeComp");
            }
            equipmentMethod.equipmentPortStateCheckForOpeCompForInternalBuffer(objCommon,
                    equipmentID,
                    startCassetteList);
        }

        if (log.isDebugEnabled()) {
            log.debug("step23 - Check Process for lot: The following conditions are checked by this object");
        }
        lotMethod.lotCheckConditionForOpeComp(objCommon, startCassetteList);

        if (log.isDebugEnabled()) {
            log.debug("step24 - Check Process for eqp: The following conditions are checked by this object");
        }
        // - All lof lot, which is contained in controlJob, must be existing in the eqp's processing information.
        equipmentMethod.equipmentCheckConditionForOpeComp(objCommon, equipmentID, startCassetteList);

        if (log.isDebugEnabled()) {
            log.debug("step28 - CP Test Function Procedure");
        }
        moveOutLots.forEach(lotInCassette -> {
            ObjectIdentifier lotTestTypeRetCode = null;
            try {
                lotTestTypeRetCode = lotMethod.lotTestTypeIDGet(objCommon, lotInCassette.getLotID());
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundTestType(), e.getCode())) {
                    throw e;
                }
            }

            if (ObjectIdentifier.isNotEmptyWithValue(lotTestTypeRetCode)) {
                if (log.isDebugEnabled()) {
                    log.debug("step30 - Gather Bin Summary Information based on lotID & testTypeID");
                }
                List<Infos.WaferBinSummary> waferBinSummaryList = binSummaryMethod.binSummaryGetByTestTypeDR(objCommon,
                        lotInCassette.getLotID(),
                        lotTestTypeRetCode);

                if (log.isDebugEnabled()) {
                    log.debug("step31- Update wafer Die quantity based on the input parameter");
                }
                List<Infos.LotWaferAttributes> strLotWaferAttributes = waferBinSummaryList.stream()
                        .map(waferBinSummary -> {
                            Infos.LotWaferAttributes lotWaferAttributes = new Infos.LotWaferAttributes();
                            lotWaferAttributes.setWaferID(waferBinSummary.getWaferId());
                            lotWaferAttributes.setGoodUnitCount(waferBinSummary.getGoodUnitCount());
                            lotWaferAttributes.setRepairUnitCount(waferBinSummary.getRepairUnitCount());
                            lotWaferAttributes.setFailUnitCount(waferBinSummary.getFailUnitCount());
                            return lotWaferAttributes;
                        })
                        .collect(Collectors.toList());
                lotMethod.lotWaferChangeDie(objCommon, lotInCassette.getLotID(), strLotWaferAttributes);
            }
        });


        if (log.isDebugEnabled()) {
            log.debug("step32 - Update flowbatch Information of eqp");
        }
        flowBatchMethod.flowBatchInformationUpdateByOpeComp(objCommon, equipmentID, startCassetteList);

        if (log.isDebugEnabled()) {
            log.debug("step33 - Process Operation Update Procedure");
        }
        processMethod.processActualCompInformationSet(objCommon, startCassetteList);

        /*------------------------------------------------------------*/
        /*                                                            */
        /*   Reticle / Fixture Related Information Update Procedure   */
        /*                                                            */
        /*------------------------------------------------------------*/
        try {
            if (log.isDebugEnabled()) {
                log.debug("step38 - Check ProcessDurable was Used for Operation or Not");
            }
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())) {
                log.info("{}", e.getMessage());
            }
            // retCode == RC_EQP_PROCDRBL_RTCL_REQD || RC_EQP_PROCDRBL_FIXT_REQD
            else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())) {
                moveOutLots.forEach(lotInCassette -> {
                    if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())) {
                        if (log.isDebugEnabled()) {
                            log.debug("step39 - Get Used Reticles for lot");
                        }
                        processMethod.processAssignedReticleGet(objCommon, lotInCassette.getLotID()).forEach(startReticle -> {
                            if (log.isDebugEnabled()) {
                                log.debug("step40 - check reticle usage limitation for reticle");
                            }
                            Outputs.ObjReticleUsageLimitationCheckOut reticleUsedLimitRetCode =
                                    reticleMethod.reticleUsageLimitationCheck(objCommon, startReticle.getReticleID());
                            if (log.isTraceEnabled()) {
                                log.trace("UsageLimitOverFlag is {}", reticleUsedLimitRetCode.isUsageLimitOverFlag());
                            }
                            if (CimBooleanUtils.isTrue(reticleUsedLimitRetCode.isUsageLimitOverFlag())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("step41 - Call System Message");
                                }
                                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTCLUSAGELIMITOVER);
                                alertMessageRptParams.setSystemMessageText(reticleUsedLimitRetCode.getMessageText());
                                alertMessageRptParams.setNotifyFlag(true);
                                alertMessageRptParams.setSystemMessageTimeStamp(
                                        CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                            }

                            log.debug("step42 - Set last used time stamp for used reticle");
                            reticleMethod.reticleLastUsedTimeSet(startReticle.getReticleID(),
                                    objCommon.getTimeStamp().getReportTimeStamp());
                        });

                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("step43 - Get Used Fixture for lot");
                        }
                        processMethod.processAssignedFixtureGet(objCommon, lotInCassette.getLotID()).forEach(startFixture -> {
                            if (log.isDebugEnabled()) {
                                log.debug("step44 - Fixture Usage Limitation Check");
                            }
                            Outputs.objFixtureUsageLimitationCheckOut objFixtureUsageLimitationCheckOut =
                                    fixtureMethod.fixtureUsageLimitationCheck(objCommon, startFixture.getFixtureID());

                            if (log.isDebugEnabled()) {
                                log.debug("step45 - Call System Message");
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_FIXTUSAGELIMITOVER);
                            alertMessageRptParams.setSystemMessageText(objFixtureUsageLimitationCheckOut.getMessageText());
                            alertMessageRptParams.setNotifyFlag(true);
                            alertMessageRptParams.setSystemMessageTimeStamp(
                                    CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        });
                    }
                });
            }
            // else throw exception
            else {
                throw e;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("step49 - Check and set the min Q-Time");
        }
        // 检查并设置Min Q-Time最小时间限制
        minQTimeMethod.checkAndSetRestrictions(objCommon, startCassetteList);

        if (log.isDebugEnabled()) {
            log.debug("step50 - Change lot Process State to Waiting");
        }
        lotMethod.lotProcessStateMakeWaiting(objCommon, startCassetteList);


        /*--------------------------------------------*/
        /*   Change lot Pending Move Next Flag true   */
        /*--------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step51 - set lot's pending move next flag to true");
        }
        moveOutLots.stream()
                .map(Infos.LotInCassette::getLotID)
                .forEach(lotID -> lotMethod.setLotMoveNextRequired(objCommon, lotID, true));

        /*------------------------------------------------------------------------------------*/
        /*   Prepare for PO Moving                                                            */
        /*------------------------------------------------------------------------------------*/
        List<ObjectIdentifier> operationStartLotIds = moveOutLots.stream()
                .map(Infos.LotInCassette::getLotID)
                .peek(lotID -> {
                    /*-------------------------------------*/
                    /*   Set Contamination Level for Lot   */
                    /*-------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step52 - update contamination flag");
                    }
                    contaminationMethod.lotContaminationLevelAndPrFlagSet(lotID);
                })
                .peek(lotID -> {
                    /*-----------------------------------------*/
                    /*   Set Contamination Level for Carrier   */
                    /*-----------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step55 - lotCassetteCategoryUpdateForContaminationControl");
                    }
                    lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);
                }).collect(Collectors.toList());


        /*----------------------------------------------------*/
        /*                                                    */
        /*   Equipment Related Information Update Procedure   */
        /*                                                    */
        /*----------------------------------------------------*/

        /*----------------------------------------------------------------*/
        /*   Remove ControlJobLot from EqpInfo's ProcessingLot Sequence   */
        /*----------------------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step66 - Remove ControlJobLot from EqpInfo's ProcessingLot Sequence");
        }
        equipmentMethod.equipmentProcessingLotDelete(objCommon, equipmentID, startCassetteList);

        /*---------------------------------------------*/
        /*   Maintain Eqp's Status when OFF-LINE Mode  */
        /*---------------------------------------------*/
        if (log.isTraceEnabled()) {
            log.trace("OnlineMode is {}", operationMode.getOnlineMode());
        }
        if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, operationMode.getOnlineMode())) {
            if (log.isDebugEnabled()) {
                log.debug("step67 - Change eqp's Status to 'STANDBY' if necessary");
            }
            boolean checkEqpStateOut = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon,
                    equipmentID);
            if (log.isTraceEnabled()) {
                log.trace("checkEqpStateOut is {}", checkEqpStateOut);
            }
            if (CimBooleanUtils.isTrue(checkEqpStateOut)) {
                if (log.isDebugEnabled()) {
                    log.debug("step68 - get Defaclt Status CimCode for Productive / Standby");
                }
                ObjectIdentifier recoverStateOut = equipmentMethod.equipmentRecoverStateGetManufacturing(objCommon,
                        equipmentID);

                try {
                    if (log.isDebugEnabled()) {
                        log.debug("step69 - call sxEqpStatusChangeReq");
                    }
                    equipmentService.sxEqpStatusChangeReq(objCommon, equipmentID,
                            recoverStateOut, params.getOpeMemo());
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getInvalidStateTransition(), e.getCode())) {
                        throw e;
                    }
                }
            }
        }

        /*--------------------------------------*/
        /*   Equipment Usage Limitation Check   */
        /*--------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step70 - eqp Usage Limitation Check");
        }
        Outputs.ObjEquipmentUsageLimitationCheckOut checkEqpUseLimitOut =
                equipmentMethod.equipmentUsageLimitationCheck(objCommon, equipmentID);

        if (log.isTraceEnabled()) {
            log.trace("UsageLimitOverFlag is {}", checkEqpUseLimitOut.isUsageLimitOverFlag());
        }
        if (CimBooleanUtils.isTrue(checkEqpUseLimitOut.isUsageLimitOverFlag())) {
            if (log.isDebugEnabled()) {
                log.debug("step71 - Call System Message");
            }
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EQPUSAGELIMITOVER);
            alertMessageRptParams.setSystemMessageText(checkEqpUseLimitOut.getMessageText());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(equipmentID);
            alertMessageRptParams.setSystemMessageTimeStamp
                    (CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }

        /*----------------------------------------------------*/
        /*                                                    */
        /*   ControlJob Related Information Update Proceure   */
        /*                                                    */
        /*----------------------------------------------------*/
        log.trace("TransactionID is {}", objCommon.getTransactionID());
        final boolean isNotPartialMoveOut = CimStringUtils.unEqualIn(objCommon.getTransactionID(),
                TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),
                TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
        if (isNotPartialMoveOut) {
            //----------------------------------------------------------------------------------
            // Delete ControlJob from EQP.
            //----------------------------------------------------------------------------------
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobID(controlJobID);
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP);
            cjStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
            if (log.isDebugEnabled()) {
                log.debug("step72 - Delete controljob from EQP.");
            }
            controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);
        }

        if (isNotPartialMoveOut) {
            //---------------------------------------------//
            //   Delete Control Job From Lot and Cassette  //
            //---------------------------------------------//
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobID(controlJobID);
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE);
            cjStatusChangeReqParams.setClaimMemo(params.getOpeMemo());

            if (log.isDebugEnabled()) {
                log.debug("step72 - change CJ status,remove the cj form Lot and Cassette");
            }
            controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);
        }

        /*---------------------------------------------------*/
        /*                                                   */
        /*   Cassette Related Information Update Procedure   */
        /*                                                   */
        /*---------------------------------------------------*/
        startCassetteList.forEach(startCassette -> {

            /*-------------------------------------*/
            /*   Cassette Usage Limitation Check   */
            /*-------------------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("step73 - cassette Usage Limitation Check");
            }
            Outputs.ObjCassetteUsageLimitationCheckOut checkCassetteUseLimitOut =
                    cassetteMethod.cassetteUsageLimitationCheck(objCommon, startCassette.getCassetteID());

            if (checkCassetteUseLimitOut.isUsageLimitOverFlag()) {
                if (log.isDebugEnabled()) {
                    log.debug("step74 - Call System Message");
                }
                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_CASTUSAGELIMITOVER);
                alertMessageRptParams.setSystemMessageText(checkCassetteUseLimitOut.getMessageText());
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setSystemMessageTimeStamp(
                        CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            }

            /*---------------------------------------*/
            /*   Update Cassette's MultiLotType      */
            /*---------------------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("step75 - Update cassette's MultiLotType");
            }
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, startCassette.getCassetteID());
        });


        /*--------------------------*/
        /*                          */
        /*   Event Make Procedure   */
        /*                          */
        /*--------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step78 - Event Make Procedure");
        }
        String eventTxID;
        if (isNotPartialMoveOut) {
            eventTxID = TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue();
        } else {
            eventTxID = objCommon.getTransactionID();
        }
        startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream()
                        .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                        .map(lotInCassette -> {
                            Inputs.LotOperationMoveEventMakeOpeComp event = new Inputs.LotOperationMoveEventMakeOpeComp();
                            event.setTransactionID(eventTxID);
                            event.setEquipmentID(equipmentID);
                            event.setOperationMode(ObjectIdentifier.fetchValue(operationMode.getOperationMode()));
                            event.setCassetteID(startCassette.getCassetteID());
                            event.setLotInCassette(lotInCassette);
                            event.setClaimMemo(params.getOpeMemo());
                            event.setControlJobID(controlJobID);
                            return event;
                        })).forEach(event -> {
                            if (log.isDebugEnabled()) {
                                log.debug("step79 - Event Make Procedure");
                            }
                            eventMethod.lotOperationMoveEventMakeOpeComp(objCommon, event);
                        }
                );


        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   BR Script Procedure                                                  */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
        moveOutLots.forEach(lotInCassette -> {
            /*-------------------------*/
            /*   Execute Post BRScript */
            /*-------------------------*/
            Params.ProcessControlScriptRunReqParams runReqParams = new Params.ProcessControlScriptRunReqParams();
            runReqParams.setEquipmentId(equipmentID);
            runReqParams.setLotId(lotInCassette.getLotID());
            runReqParams.setPhase(BizConstant.SP_BRSCRIPT_POST);
            try {
                if (log.isDebugEnabled()) {
                    log.debug("step92 - BR Script Procedure - Execute BRScript");
                }
                processControlScriptService.sxProcessControlScriptRunReq(objCommon, runReqParams);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundScript(), e.getCode())) {
                    throw e;
                }
            }
        });

        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   Send OpeComp Request to TCS Procedure                                */
        /*                                                                        */
        /*   - If specified portGroup's operationCompMode is Manual, OpeComp trx  */
        /*     must be sent to TCS. In TCS, if "ProcessEnd" report is not come    */
        /*     from eqp yet, OpeComp trx from MM is rejected by TCS.              */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("MoveOutMode is {}", operationMode.getMoveOutMode());
        }
        if (CimStringUtils.equals(BizConstant.SP_EQP_COMPMODE_MANUAL, operationMode.getMoveOutMode())) {
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            if (isNotPartialMoveOut) {
                if (log.isDebugEnabled()) {
                    log.debug("transactionID != OEQPW012 && OEQPW024");
                    log.debug("step96 - Send Request to EAP");
                }
                /*--------------------------*/
                /*    Send Request to EAP   */
                /*--------------------------*/
                IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,
                        params.getUser(),
                        equipmentID,
                        null,
                        true);
                if (null == eapRemoteManager) {
                    if (log.isWarnEnabled()) {
                        log.warn("MES not configure EAP host");
                    }
                } else {
                    eapRemoteManager.sendMoveOutForIBReq(params);
                    if (log.isInfoEnabled()) {
                        log.info("Now EAP subSystem is alive!! Go ahead");
                    }
                }
            }
        }

        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   Send OpeComp Report to DCS Procedure                                 */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
        //TODO-NOTIMPL:【Step69】Send OpeComp Report to DCS Procedure;

        /*-----------------------------------------------------*/
        /*   Call controlJobAPCRunTimeCapabilityGetDR          */
        /*-----------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step98 - Call controlJobAPCRunTimeCapabilityGetDR");
        }
        List<Infos.APCRunTimeCapabilityResponse> apcRunTimeCapabilityResponseList =
                controlJobMethod.controlJobAPCRunTimeCapabilityGetDR(objCommon, controlJobID);
        boolean bActionTriggerFlag = apcRunTimeCapabilityResponseList.stream()
                .flatMap(apcRunTimeCapabilityResponse -> apcRunTimeCapabilityResponse
                        .getStrAPCRunTimeCapability()
                        .stream()
                        .flatMap(apcRunTimeCapability -> apcRunTimeCapability
                                .getStrAPCBaseAPCSystemFunction1()
                                .stream())
                )
                .anyMatch(function1 -> CimStringUtils.equals(function1.getType(),
                        BizConstant.SP_APCFUNCTIONTYPE_PRODUCTDISPOSITION));

        if (log.isTraceEnabled()) {
            log.trace("bActionTriggerFlag is {}", bActionTriggerFlag);
        }
        if (CimBooleanUtils.isFalse(bActionTriggerFlag)) {
            /*-----------------------------------------------------*/
            /*   Call APCRuntimeCapability_DeleteDR                */
            /*-----------------------------------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("step99 - Call apcRuntimeCapabilityDeleteDR");
            }
            apcMethod.apcRuntimeCapabilityDeleteDR(objCommon, controlJobID);
        }

        /*-------------------------------------------------*/
        /*   Call APCMgrSendControlJobInformationDR        */
        /*-------------------------------------------------*/
        if (isNotPartialMoveOut) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("step100 - call APCMgrSendControlJobInformationDR");
                }
                apcMethod.APCMgrSendControlJobInformationDR(objCommon,
                        equipmentID,
                        controlJobID,
                        BizConstant.SP_APC_CONTROLJOBSTATUS_COMPLETED,
                        apcBaseCassettes);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())) {
                    throw e;
                }
            }
        }

        /*--------------------------------------*/
        /*  Update Reticle's LastUsedTimestamp  */
        /*--------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step101 - Update Reticle's LastUsedTimestamp");
        }
        reticleMethod.reticleLastUsedTimeStampUpdate(objCommon, startCassetteList);

        if (log.isDebugEnabled()) {
            log.debug("step102 - entity inhibit exception lot to do Change For Operation Complete");
        }
        constraintMethod.constraintExceptionLotChangeForOpeComp(objCommon, operationStartLotIds, controlJobID);

        if (log.isDebugEnabled()) {
            log.debug("step103 - check contamination level and pr flag");
        }
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepOut(objCommon, lotIDs, equipmentID);

        /*----------------------*/
        /*                      */
        /*   Return to Caller   */
        /*                      */
        /*----------------------*/
        log.debug("step104 - Return to Caller");
        Results.MoveOutReqResult retVal = new Results.MoveOutReqResult();
        // Check this is need to EDC check
        final boolean specCheckRequiredFlag = startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(Infos.LotInCassette::getMoveInFlag)
                .anyMatch(lotInCassette -> lotInCassette.getStartRecipe().getDataCollectionFlag());
        if (specCheckRequiredFlag && BizConstant.SP_EQP_ONLINEMODE_OFFLINE.equals(operationMode.getOnlineMode())) {
            // Get EDC check result for Lots.
            final Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut edcInformationGets =
                    lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon,
                            equipmentID,
                            operationStartLotIds);

            // Get the MoveOut Lot
            retVal = equipmentMethod.equipmentFillInTxTRC004(objCommon,
                    edcInformationGets.getStrStartCassette(),
                    null);
        } else {
            retVal.setMoveOutLot(operationStartLotIds.stream()
                    .map(loID -> {
                        Infos.OpeCompLot opeCompLot = new Infos.OpeCompLot();
                        opeCompLot.setLotID(loID);
                        return opeCompLot;
                    }).collect(Collectors.toList()));
        }

        log.debug("【Method Exit】sxMoveOutForIBReq()");
        return retVal;
    }


    @Override
    public Results.ForceMoveOutReqResult sxForceMoveOutReq(Infos.ObjCommon objCommon,
                                                           ObjectIdentifier equipmentID,
                                                           ObjectIdentifier controlJobID,
                                                           Boolean spcResultRequiredFlag,
                                                           String claimMemo) {
        if (log.isInfoEnabled()) {
            log.info(">>> sxForceMoveOutReq entry");
        }
        //---------------------------------
        //   Check Process
        //---------------------------------
        //【step1】equipment_categoryVsTxID_CheckCombination
        if (log.isDebugEnabled()) {
            log.debug("Check Transaction ID and equipment Category combination.");
        }
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        /*-----------------------------------------------------------------------*/
        /*   Get Started Lot information which is specified with ControlJob ID   */
        /*-----------------------------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("Get Started Lot information which is specified with ControlJob ID");
        }
        Outputs.ObjControlJobStartReserveInformationOut controlJobStartReserveInformationOut =
                controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                        controlJobID,
                        false);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        // step 3 object_lockMode_Get
        if (log.isDebugEnabled()) {
            log.debug("Get Lock Mode");
        }
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        long lockMode = objLockModeOut.getLockMode();
        if (log.isDebugEnabled()) {
            log.debug("Lock Mode : {}", lockMode);
        }
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            // step 4 advanced_object_Lock
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            // step 5 object_Lock
            /*--------------------------------*/
            /*   Lock Machine object          */
            /*--------------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        /*--------------------------------*/
        /*   Lock Cassette / Lot Object   */
        /*--------------------------------*/
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        controlJobStartReserveInformationOut.getStartCassetteList().forEach(startCassette -> {
            if (log.isDebugEnabled()) {
                log.debug("LoadPurposeType is {}", startCassette.getLoadPurposeType());
            }
            if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                //equipment_monitorCreationFlag_Get();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("step6 - check equipment monitor creation flag");
                    }
                    equipmentMethod.equipmentMonitorCreationFlagGet(objCommon, equipmentID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getMonitorCreatReqd(), e.getCode())) {
                        //---------------------------------------------------------------------------------------------
                        //if monitor creationflag was TRUE, at least one lot should be existed in Empty Cassete;
                        //---------------------------------------------------------------------------------------------
                        if (CimArrayUtils.isEmpty(startCassette.getLotInCassetteList())) {
                            log.error("object_Lock() == RC_MONITOR_CREAT_REQD");
                            Validations.check(retCodeConfigEx.getMonitorCreatReqd());
                        }
                    }
                }
                cassetteIDs.add(startCassette.getCassetteID());
                return;
            }
            cassetteIDs.add(startCassette.getCassetteID());
            startCassette.getLotInCassetteList().stream()
                    /*---------------------------*/
                    /*   Omit Not-OpeStart Lot   */
                    /*---------------------------*/
                    .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                    .forEach(lotInCassette -> lotIDs.add(lotInCassette.getLotID()));
        });

        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            //【step8】 equipment_onlineMode_Get
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                // Lock Equipment ProcLot Element (Count)
                // step9 advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_COUNT, new ArrayList<>()));
            }
            // step10 advanced_object_Lock
            List<String> procLotSeq = lotIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, procLotSeq));
            /*------------------------------*/
            /*   Lock ControlJob Object     */
            /*------------------------------*/
            // step12 object_Lock
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }

        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/
        // step13 objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        // step14 objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        //【step7】 equipment_brInfo_GetDR__120
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        /*--------------------------------------*/
        /*   Get SLM Switch for the equipment   */
        /*--------------------------------------*/
        int containerPositionLen = 0;
        List<ObjectIdentifier> destCassetteList = Collections.emptyList();
        if (CimBooleanUtils.isTrue(eqpBrInfo.isFmcCapabilityFlag())) {
            if (log.isInfoEnabled()) {
                log.info("FMC Capability is TRUE.");
            }
            //----------------------------------------------------------------
            //  object_Lock for Equipment Container Position by ControlJob
            //----------------------------------------------------------------
            // step16 object_LockForEquipmentContainerPosition
            Inputs.ObjObjectLockForEquipmentContainerPositionIn positionIn =
                    new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
            positionIn.setEquipmentID(equipmentID);
            positionIn.setControlJobID(controlJobID);
            if (log.isDebugEnabled()) {
                log.debug("Object lock for Equipment Container Position by ControlJob");
            }
            objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, positionIn);

            //----------------------------------------------------------
            //  Get equipmentContainer position objects by controlJobID
            //----------------------------------------------------------
            //【step17】equipmentContainerPosition_info_Get
            Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn =
                    new Inputs.ObjEquipmentContainerPositionInfoGetIn();
            positionInfoGetIn.setEquipmentID(equipmentID);
            positionInfoGetIn.setKey(controlJobID);
            positionInfoGetIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
            Infos.EqpContainerPositionInfo equipmentContainerPositionInfoOut =
                    equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, positionInfoGetIn);

            containerPositionLen = CimArrayUtils.getSize(equipmentContainerPositionInfoOut.getEqpContainerPositionList());
            destCassetteList = equipmentContainerPositionInfoOut.getEqpContainerPositionList()
                    .stream()
                    .map(eqpContainerPosition -> {
                        /*-----------------------------------------------------------------------*/
                        /*   Check SLM condition for EqpContainerPosition                        */
                        /*   The following conditions are checked by this object                 */
                        /*   - All SLM Status of Equipment container position which has relation */
                        /*     with controljob has NOT to be 'Stored'.                           */
                        /*-----------------------------------------------------------------------*/
                        if (CimStringUtils.equals(BizConstant.SP_SLMSTATE_STORED, eqpContainerPosition.getFmcState())) {
                            log.info("# SLMState = SP_SLMState_Stored");
                            throw new ServiceException(retCodeConfig.getInvalidSLMStatusOfContainerPosition());
                        }
                        return eqpContainerPosition.getDestCassetteID();
                    })
                    .filter(ObjectIdentifier::isNotEmptyWithValue)
                    .distinct()
                    .collect(Collectors.toList());
            // --------------------------------------------------------------
            // Get Destination Cassette List.
            // These cassettes should be locked before update
            // --------------------------------------------------------------
            destCassetteList.forEach(destCassette -> {
                if (log.isDebugEnabled()) {
                    log.debug("Lock Dest Cassette : {}", ObjectIdentifier.fetchValue(destCassette));
                }
                objectLockMethod.objectLock(objCommon, CimCassette.class, destCassette);
            });
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process End                                             */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        // Get start cassette info
        List<Infos.StartCassette> startCassetteList = controlJobStartReserveInformationOut.getStartCassetteList();

        // Gets all actual move lot
        final List<Infos.LotInCassette> moveOutLots = startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                /*---------------------------*/
                /*   Omit Not-OpeStart Lot   */
                /*---------------------------*/
                .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("update season of the move out");
        }
        //  add season by ho
        seasoningService.sxSeasonForForceMoveOut(objCommon, equipmentID);

        /*-------------------------------------------------*/
        /*   call cassette_APCInformation_GetDR            */
        /*-------------------------------------------------*/
        List<Infos.ApcBaseCassette> apcBaseCassettes;
        try {
            if (log.isDebugEnabled()) {
                log.debug("call cassetteAPCInformationGetDR");
            }
            apcBaseCassettes = cassetteMethod.cassetteAPCInformationGetDR(objCommon,
                    equipmentID,
                    startCassetteList);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())) {
                throw e;
            } else {
                apcBaseCassettes = Collections.emptyList();
            }
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        /*---------------------------------*/
        /*   Get Equipment's Online Mode   */
        /*---------------------------------*/
        //【step19】portResource_currentOperationMode_Get
        Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeOut =
                portMethod.portResourceCurrentOperationModeGet(objCommon,
                        equipmentID,
                        startCassetteList.get(0).getUnloadPortID());
        // Port Operation Mode
        final Infos.OperationMode operationMode = portResourceCurrentOperationModeOut.getOperationMode();

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - lotProcessState                                                   */
        /*   - lotHoldState                                                      */
        /*-----------------------------------------------------------------------*/
        moveOutLots.forEach(lotInCassette -> {
            //【step20】  lot_allState_Get
            Outputs.ObjLotAllStateGetOut lotAllStateGetOut = lotMethod.lotAllStateGet(objCommon,
                    lotInCassette.getLotID());
            if (CimStringUtils.unEqual(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotAllStateGetOut.getProcessState())) {
                log.info("lot_allState_Get() strLot_allState_Get_out.processState == SP_Lot_ProcState_Processing");
                throw new ServiceException(retCodeConfig.getInvalidLotProcstat());
            }
            if (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD, lotAllStateGetOut.getHoldState())) {
                log.info("HoldState == NotOnHold");
                throw new ServiceException(retCodeConfig.getInvalidLotHoldStat());
            }
        });

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Equipment                                         */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - All of lot which is contained in controlJob must be existing      */
        /*     in the equipment's processing information.                        */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        //【step21】equipment_CheckConditionForOpeComp
        equipmentMethod.equipmentCheckConditionForOpeComp(objCommon, equipmentID, startCassetteList);

        /*--------------------------------*/
        /*                                */
        /*   CP Test Function Procedure   */
        /*                                */
        /*--------------------------------*/
        moveOutLots.forEach(lotInCassette -> {
            /*-----------------------------------------*/
            /*   Check Test Type of Current Process    */
            /*-----------------------------------------*/
            ObjectIdentifier testTypeID = null;
            try {
                testTypeID = lotMethod.lotTestTypeIDGet(objCommon, lotInCassette.getLotID());
            } catch (ServiceException ex) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundTestType(), ex.getCode())) {
                    throw ex;
                }
            }

            if (ObjectIdentifier.isNotEmpty(testTypeID)) {
                /*---------------------------------------------------------------*/
                /*   Gather Bin Summary Information based on lotID & testTypeID  */
                /*---------------------------------------------------------------*/
                if (log.isDebugEnabled()) {
                    log.debug("step30 - Gather Bin Summary Information based on lotID & testTypeID");
                }
                List<Infos.WaferBinSummary> waferBinSummaries = binSummaryMethod.binSummaryGetByTestTypeDR(objCommon,
                        lotInCassette.getLotID(),
                        testTypeID);
                /*----------------------------------------------------------*/
                /*   Update Wafer Die quantity based on the input parameter  */
                /*-----------------------------------------------------------*/
                List<Infos.LotWaferAttributes> lotWaferAttributes = waferBinSummaries.stream()
                        .map(waferBinSummary -> {
                            if (waferBinSummary.getBinReportCount() > 0) {
                                Infos.LotWaferAttributes lotWaferAttribute = new Infos.LotWaferAttributes();
                                lotWaferAttribute.setWaferID(waferBinSummary.getWaferId());
                                lotWaferAttribute.setGoodUnitCount(waferBinSummary.getGoodUnitCount());
                                lotWaferAttribute.setRepairUnitCount(waferBinSummary.getRepairUnitCount());
                                lotWaferAttribute.setFailUnitCount(waferBinSummary.getFailUnitCount());
                                return lotWaferAttribute;
                            } else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (log.isDebugEnabled()) {
                    log.debug("step31- Update wafer Die quantity based on the input parameter");
                }
                lotMethod.lotWaferChangeDie(objCommon, lotInCassette.getLotID(), lotWaferAttributes);
            }
        });

        /*------------------------------------------------------*/
        /*                                                      */
        /*     FlowBatch Related Information Update Procedure   */
        /*                                                      */
        /*------------------------------------------------------*/
        /*----------------------------------------------------------*/
        /*   Update FlowBatch Information of Equipment              */
        /*----------------------------------------------------------*/
        //【step26】 flowBatch_Information_UpdateByOpeComp
        flowBatchMethod.flowBatchInformationUpdateByOpeComp(objCommon, equipmentID, startCassetteList);

        /*----------------------------------------------*/
        /*                                              */
        /*   Process Operation Update Procedure         */
        /*                                              */
        /*----------------------------------------------*/
        /*-------------------------------------------------*/
        /*   Update Process Operation (actual comp xxxx)   */
        /*-------------------------------------------------*/
        //【step27】 process_actualCompInformation_Set
        processMethod.processActualCompInformationSet(objCommon, startCassetteList);

        /*--------------------------------------------------*/
        /*                                                  */
        /*  Bonding Map Condition Check for Wafer Stacking  */
        /*                                                  */
        /*--------------------------------------------------*/
        boolean bondingEqpFlag = false;
        if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERBONDING, eqpBrInfo.getEquipmentCategory())) {
            log.info("Equipment Category is Wafer Bonding");
            bondingEqpFlag = true;
        }
        List<ObjectIdentifier> tmpBondingTopLots = Collections.emptyList();
        if (bondingEqpFlag) {
            //----------------------------------------------------------------
            // Bonding Map Info Get
            //----------------------------------------------------------------
            Outputs.ObjBondingGroupInfoByEqpGetDROut bondingGroupInfoByEqpGetDR =
                    bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon,
                            equipmentID,
                            controlJobID,
                            true);
            tmpBondingTopLots = bondingGroupInfoByEqpGetDR.getTopLotIDSeq();

            bondingGroupInfoByEqpGetDR.getBondingGroupInfoList().forEach(bondingGroupInfo -> {
                boolean flag = CimStringUtils.unEqual(bondingGroupInfo.getBondingGroupState(),
                        BizConstant.SP_BONDINGGROUPSTATE_PROCESSED);
                Validations.check(flag, retCodeConfigEx.getBondgrpStateInvalid(),
                        bondingGroupInfo.getBondingGroupState(), "OpeComp");

                /*------------------------------------*/
                /*   Update Bonding Group State       */
                /*------------------------------------*/
                bondingGroupMethod.bondingGroupStateUpdateDR(objCommon,
                        bondingGroupInfo.getBondingGroupID(),
                        BizConstant.SP_BONDINGGROUPSTATE_ERROR,
                        null,
                        null);
            });
        }
        List<ObjectIdentifier> bondingTopLots = tmpBondingTopLots;

        /*------------------------------------------------------------*/
        /*   Reticle / Fixture Related Information Update Procedure   */
        /*------------------------------------------------------------*/
        //【step34】 - equipment_processDurableRequiredFlag_Get
        //Check ProcessDurable was Used for Operation or Not
        try {
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())) {
                log.info("{}", e.getMessage());
            } else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())) {
                log.info("equipmentProcessDurableRequiredFlagGet() == EQP_PROCDRBL_RTCL_REQD || EQP_PROCDRBL_FIXT_REQD");
                moveOutLots.forEach(lotInCassette -> {
                    if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Get Used Reticles for lot");
                        }
                        processMethod.processAssignedReticleGet(objCommon, lotInCassette.getLotID()).forEach(startReticle -> {
                            if (log.isDebugEnabled()) {
                                log.debug("check reticle usage limitation for reticle");
                            }
                            Outputs.ObjReticleUsageLimitationCheckOut reticleUsedLimitRetCode =
                                    reticleMethod.reticleUsageLimitationCheck(objCommon, startReticle.getReticleID());
                            if (log.isTraceEnabled()) {
                                log.trace("UsageLimitOverFlag is {}", reticleUsedLimitRetCode.isUsageLimitOverFlag());
                            }
                            if (CimBooleanUtils.isTrue(reticleUsedLimitRetCode.isUsageLimitOverFlag())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Call System Message");
                                }
                                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTCLUSAGELIMITOVER);
                                alertMessageRptParams.setSystemMessageText(reticleUsedLimitRetCode.getMessageText());
                                alertMessageRptParams.setNotifyFlag(true);
                                alertMessageRptParams.setSystemMessageTimeStamp(
                                        CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /*-------------------------*/
                                /*   Call System Message   */
                                /*-------------------------*/
                                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                            }

                            /*---------------------------------------------*/
                            /*  Set last used time stamp for used reticle  */
                            /*---------------------------------------------*/
                            log.debug("Set last used time stamp for used reticle");
                            reticleMethod.reticleLastUsedTimeSet(startReticle.getReticleID(),
                                    objCommon.getTimeStamp().getReportTimeStamp());
                        });
                    } else {
                        /*------------------------------*/
                        /*   Get Used Fixture for Lot   */
                        /*------------------------------*/
                        if (log.isDebugEnabled()) {
                            log.debug("Get Used Fixture for lot");
                        }
                        processMethod.processAssignedFixtureGet(objCommon, lotInCassette.getLotID()).forEach(startFixture -> {
                            /*------------------------------------*/
                            /*   Fixture Usage Limitation Check   */
                            /*------------------------------------*/
                            if (log.isDebugEnabled()) {
                                log.debug("Fixture Usage Limitation Check");
                            }
                            Outputs.objFixtureUsageLimitationCheckOut objFixtureUsageLimitationCheckOut =
                                    fixtureMethod.fixtureUsageLimitationCheck(objCommon, startFixture.getFixtureID());

                            /*-------------------------*/
                            /*   Call System Message   */
                            /*-------------------------*/
                            if (log.isDebugEnabled()) {
                                log.debug("Call System Message");
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_FIXTUSAGELIMITOVER);
                            alertMessageRptParams.setSystemMessageText(objFixtureUsageLimitationCheckOut.getMessageText());
                            alertMessageRptParams.setNotifyFlag(true);
                            alertMessageRptParams.setSystemMessageTimeStamp(
                                    CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        });
                    }
                });
            } else {
                log.info("equipment_processDurableRequiredFlag_Get() != RC_OK");
                throw e;
            }
        }

        /*----------------------------------------------*/
        /*                                              */
        /*   Lot Related Information Update Procedure   */
        /*                                              */
        /*----------------------------------------------*/
        // 检查并设置Min Q-Time最小时间限制
        minQTimeMethod.checkAndSetRestrictions(objCommon, startCassetteList);

        //Change Lot Process State to Waiting
        //【step46】 - lot_processState_MakeWaiting
        lotMethod.lotProcessStateMakeWaiting(objCommon, startCassetteList);


        /*--------------------------------------------*/
        /*   Change lot Pending Move Next Flag true   */
        /*--------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step51 - set lot's pending move next flag to true");
        }
        moveOutLots.stream()
                .map(Infos.LotInCassette::getLotID)
                .forEach(lotID -> lotMethod.setLotMoveNextRequired(objCommon, lotID, true));

        /*------------------------------------------------------------------------------------*/
        /*   Prepare for PO Moving                                                            */
        /*------------------------------------------------------------------------------------*/
        List<ObjectIdentifier> operationStartLotIds = moveOutLots.stream()
                .map(Infos.LotInCassette::getLotID)
                .peek(lotID -> {
                    /*-------------------------------------*/
                    /*   Set Contamination Level for Lot   */
                    /*-------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step52 - update contamination flag");
                    }
                    contaminationMethod.lotContaminationLevelAndPrFlagSet(lotID);
                })
                .peek(lotID -> {
                    /*-----------------------------------------*/
                    /*   Set Contamination Level for Carrier   */
                    /*-----------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step55 - lotCassetteCategoryUpdateForContaminationControl");
                    }
                    lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);
                }).collect(Collectors.toList());


        /*---------------------------------------------------------------*/
        /*   Auto Hold after force complete                              */
        /*---------------------------------------------------------------*/
        moveOutLots.forEach(lotInCassette -> {
            //【step51】 - sxHoldLotListInq --调用接口
            List<Infos.LotHoldListAttributes> holdListAttributes = lotInqService.sxHoldLotListInq(objCommon,
                    lotInCassette.getLotID());

            holdListAttributes.forEach(lotHoldListAttributes -> {
                if (ObjectIdentifier.equalsWithValue(BizConstant.SP_REASON_RUNNINGHOLD, lotHoldListAttributes.getReasonCodeID())) {
                    log.info("Find Running Hold Record!!");
                    List<Infos.LotHoldReq> strLotHoldReleaseReqList = new ArrayList<>();
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    strLotHoldReleaseReqList.add(lotHoldReq);
                    lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
                    lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
                    lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());
                    lotHoldReq.setResponsibleOperationMark(lotHoldListAttributes.getResponsibleOperationMark());
                    lotHoldReq.setRouteID(lotHoldListAttributes.getResponsibleRouteID());
                    lotHoldReq.setOperationNumber(lotHoldListAttributes.getResponsibleOperationNumber());
                    lotHoldReq.setRelatedLotID(lotHoldListAttributes.getRelatedLotID());
                    lotHoldReq.setClaimMemo("Auto Hold after force MoveOut");
                    ObjectIdentifier aReasonCodeId = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_RUNNINGHOLDRELEASE);

                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotInCassette.getLotID());
                    holdLotReleaseReqParams.setReleaseReasonCodeID(aReasonCodeId);
                    holdLotReleaseReqParams.setHoldReqList(strLotHoldReleaseReqList);
                    //【step52】 - txHoldLotReleaseReq
                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                }
            });

            //Execute Lot Hold
            ObjectIdentifier reasonCodeId = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_FORCECOMPHOLD);
            List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            strHoldListSeq.add(lotHoldReq);
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
            lotHoldReq.setHoldReasonCodeID(reasonCodeId);
            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(ObjectIdentifier.emptyIdentifier());
            lotHoldReq.setOperationNumber("");
            lotHoldReq.setRelatedLotID(ObjectIdentifier.emptyIdentifier());
            lotHoldReq.setClaimMemo("Auto Hold after force MoveOut");

            //【step53】 - txHoldLotReq  --调用接口
            lotService.sxHoldLotReq(objCommon, lotInCassette.getLotID(), strHoldListSeq);
        });


        /*----------------------------------------------------*/
        /*                                                    */
        /*   Equipment Related Information Update Procedure   */
        /*                                                    */
        /*----------------------------------------------------*/

        /*----------------------------------------------------------------*/
        /*   Remove ControlJobLot from EqpInfo's ProcessingLot Sequence   */
        /*----------------------------------------------------------------*/
        //【step62】 - equipment_processingLot_Delete
        equipmentMethod.equipmentProcessingLotDelete(objCommon, equipmentID, startCassetteList);


        /*---------------------------------------------*/
        /*   Maintain Eqp's Status when OFF-LINE Mode  */
        /*---------------------------------------------*/
        if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, operationMode.getOnlineMode())) {
            if (log.isInfoEnabled()) {
                log.info("OnlineMode == Off-Line");
            }
            /*--------------------------------------------------------*/
            /*   Change Equipment's Status to 'STANDBY' if necessary  */
            /*--------------------------------------------------------*/
            //【step63】 - equipment_currentState_CheckToManufacturing
            boolean changeFlag = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon, equipmentID);
            if (changeFlag) {
                if (log.isInfoEnabled()) {
                    log.info("ManufacturingStateChangeableFlag == true");
                }
                //【step64】 - equipment_recoverState_GetManufacturing
                ObjectIdentifier equipmentStatus =
                        equipmentMethod.equipmentRecoverStateGetManufacturing(objCommon, equipmentID);
                //【step65】 - txEqpStatusChangeReq --调用接口
                try {
                    equipmentService.sxEqpStatusChangeReq(objCommon, equipmentID, equipmentStatus, claimMemo);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getInvalidStateTransition(), e.getCode())) {
                        throw e;
                    }
                }

            }
        }
        /*--------------------------------------*/
        /*   Equipment Usage Limitation Check   */
        /*--------------------------------------*/
        //【step66】 - equipment_usageLimitation_Check
        Outputs.ObjEquipmentUsageLimitationCheckOut equipmentUsageLimitationCheckOut =
                equipmentMethod.equipmentUsageLimitationCheck(objCommon, equipmentID);
        if (CimBooleanUtils.isTrue(equipmentUsageLimitationCheckOut.isUsageLimitOverFlag())) {
            log.info("strEquipment_usageLimitation_Check_out.usageLimitOverFlag == TRUE");
            //step67 -  txAlertMessageRpt
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EQPUSAGELIMITOVER);
            alertMessageRptParams.setSystemMessageText(equipmentUsageLimitationCheckOut.getMessageText());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(equipmentID);
            alertMessageRptParams.setSystemMessageTimeStamp(
                    CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }

        /*----------------------------------------------------*/
        /*                                                    */
        /*   ControlJob Related Information Update Procedure  */
        /*                                                    */
        /*----------------------------------------------------*/

        /*-----------------------------------------------------------------------*/
        /*   Clear all  container positions                                      */
        /*-----------------------------------------------------------------------*/
        if (containerPositionLen > 0) {
            Inputs.ObjEquipmentContainerPositionInfoClearIn clearIn = new Inputs.ObjEquipmentContainerPositionInfoClearIn();
            clearIn.setEquipmentID(equipmentID);
            clearIn.setKey(controlJobID);
            clearIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);

            //【Step68】 - equipmentContainerPosition_info_Clear
            equipmentContainerPositionMethod.equipmentContainerPositionInfoClear(objCommon, clearIn);
        }

        /*-----------------------------------------------------------------------*/
        /*   Delete SLM reservation from container position                      */
        /*-----------------------------------------------------------------------*/
        destCassetteList.forEach(destCassette -> {
            if (log.isDebugEnabled()) {
                log.debug("Delete SLM reservation from container position");
            }
            cassetteMethod.cassetteSLMReserveEquipmentSet(objCommon, destCassette, null);
        });

        /*------------------------*/
        /*   Delete Control Job   */
        /*------------------------*/
        boolean isNotPartialMoveOut = CimStringUtils.unEqualIn(objCommon.getTransactionID(),
                TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),
                TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
        if (isNotPartialMoveOut) {
            //----------------------------------------------------------------------------------
            // Delete ControlJob from EQP.
            //----------------------------------------------------------------------------------
            if (log.isInfoEnabled()) {
                log.info("Delete ControlJob from EQP.");
            }
            //【Step70】 - txCJStatusChangeReq  --接口调用
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP);
            cjStatusChangeReqParams.setControlJobID(controlJobID);
            controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        }

        if (isNotPartialMoveOut) {
            //---------------------------------------------//
            //   Delete Control Job From Lot and Cassette  //
            //---------------------------------------------//
            if (log.isInfoEnabled()) {
                log.info("Delete controlJob from Lot and Cassette.");
            }
            //【Step81】 - txCJStatusChangeReq  --接口调用
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE);
            cjStatusChangeReqParams.setControlJobID(controlJobID);
            controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        }

        /*---------------------------------------------------*/
        /*                                                   */
        /*   Cassette Related Information Update Procedure   */
        /*                                                   */
        /*---------------------------------------------------*/
        startCassetteList.forEach(startCassette -> {
            /*-------------------------------------*/
            /*   Cassette Usage Limitation Check   */
            /*-------------------------------------*/
            //【step71】 - cassette_usageLimitation_Check
            Outputs.ObjCassetteUsageLimitationCheckOut cassetteUsageLimitationCheckOut =
                    cassetteMethod.cassetteUsageLimitationCheck(objCommon, startCassette.getCassetteID());

            if (CimBooleanUtils.isTrue(cassetteUsageLimitationCheckOut.isUsageLimitOverFlag())) {
                /*-------------------------*/
                /*   Call System Message   */
                /*-------------------------*/
                log.info("strCassette_usageLimitation_Check_out.usageLimitOverFlag == TRUE");
                // step72 - txAlertMessageRpt
                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_CASTUSAGELIMITOVER);
                alertMessageRptParams.setSystemMessageText(cassetteUsageLimitationCheckOut.getMessageText());
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setSystemMessageTimeStamp(
                        CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            }
            /*---------------------------------------*/
            /*   Update Cassette's MultiLotType      */
            /*---------------------------------------*/
            //【step73】 - cassette_multiLotType_Update
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, startCassette.getCassetteID());
        });


        /*--------------------------*/
        /*                          */
        /*   Event Make Procedure   */
        /*                          */
        /*--------------------------*/
        startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream()
                        .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                        .map(lotInCassette -> {
                            Inputs.LotOperationMoveEventMakeOpeComp event = new Inputs.LotOperationMoveEventMakeOpeComp();
                            event.setTransactionID(TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue());
                            event.setEquipmentID(equipmentID);
                            event.setOperationMode(ObjectIdentifier.fetchValue(operationMode.getOperationMode()));
                            event.setControlJobID(controlJobID);
                            event.setCassetteID(startCassette.getCassetteID());
                            event.setLotInCassette(lotInCassette);
                            event.setClaimMemo(claimMemo);
                            return event;
                        })).forEach(event -> {
                            if (log.isDebugEnabled()) {
                                log.debug(" Event Make Procedure");
                            }
                            eventMethod.lotOperationMoveEventMakeOpeComp(objCommon, event);
                        }
                );

        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   BR Script Procedure                                                  */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
        moveOutLots.stream()
                .filter(lotInCassette -> {
                    for (ObjectIdentifier bondingTopLot : bondingTopLots) {
                        if (ObjectIdentifier.equalsWithValue(lotInCassette.getLotID(), bondingTopLot)) {
                            return false;
                        }
                    }
                    return true;
                })
                .forEach(lotInCassette -> {
                    /*-------------------------*/
                    /*   Execute Post Script   */
                    /*-------------------------*/
                    if (log.isInfoEnabled()) {
                        log.info("Execute POST Script");
                    }
                    Params.ProcessControlScriptRunReqParams scriptParams = new Params.ProcessControlScriptRunReqParams();
                    scriptParams.setEquipmentId(equipmentID);
                    scriptParams.setLotId(lotInCassette.getLotID());
                    scriptParams.setPhase(BizConstant.SP_BRSCRIPT_POST);
                    scriptParams.setUser(objCommon.getUser());
                    processControlScriptService.sxProcessControlScriptRunReq(objCommon, scriptParams);
                });

        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   Send OpeComp Report to DCS Procedure                                 */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
        // todo: DCSMgr_SendOperationCompletedRpt

        /*-----------------------------------------------------*/
        /*   Call APCRuntimeCapability_DeleteDR                */
        /*-----------------------------------------------------*/
        log.info("call APCRuntimeCapability_DeleteDR");
        apcMethod.apcRuntimeCapabilityDeleteDR(objCommon, controlJobID);

        /*-------------------------------------------------*/
        /*   Call APCMgr_SendControlJobInformationDR       */
        /*-------------------------------------------------*/
        log.info("call APCMgr_SendControlJobInformationDR()");
        try {
            apcMethod.APCMgrSendControlJobInformationDR(objCommon,
                    equipmentID,
                    controlJobID,
                    BizConstant.SP_APC_CONTROLJOBSTATUS_COMPLETED,
                    apcBaseCassettes);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())) {
                throw e;
            }
        }

        /*--------------------------------------*/
        /*  Update Reticle's LastUsedTimestamp  */
        /*--------------------------------------*/
        //【step91】 - reticle_lastUsedTimeStamp_Update
        reticleMethod.reticleLastUsedTimeStampUpdate(objCommon, startCassetteList);

        /*--------------------------------------*/
        /*  Constraint exception lot change     */
        /*--------------------------------------*/
        //【step92】 - entityInhibitExceptionLot_ChangeForOpeComp
        constraintMethod.constraintExceptionLotChangeForOpeComp(objCommon, operationStartLotIds, controlJobID);

        /*--------------------------------------------*/
        /*  check contamination level and pr flag     */
        /*--------------------------------------------*/
        // 【step 93】 check contamination level and pr flag
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepOut(objCommon, lotIDs, equipmentID);

        /*--------------------------*/
        /*                          */
        /*   Set Return Structure   */
        /*                          */
        /*--------------------------*/
        Results.ForceMoveOutReqResult retVal = new Results.ForceMoveOutReqResult();
        // Check this is need to EDC check
        final boolean specCheckRequiredFlag = startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(Infos.LotInCassette::getMoveInFlag)
                .anyMatch(lotInCassette -> lotInCassette.getStartRecipe().getDataCollectionFlag());
        if (specCheckRequiredFlag && BizConstant.SP_EQP_ONLINEMODE_OFFLINE.equals(operationMode.getOnlineMode())) {
            // Get EDC check result for Lots.
            final Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut edcInformationGets =
                    lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon,
                            equipmentID,
                            operationStartLotIds);

            // Get the MoveOut Lot
            final Results.MoveOutReqResult moveOutReqResult = equipmentMethod.equipmentFillInTxTRC004(objCommon,
                    edcInformationGets.getStrStartCassette(),
                    null);
            retVal.setStrOpeCompLot(moveOutReqResult.getMoveOutLot());
        } else {
            retVal.setStrOpeCompLot(operationStartLotIds.stream()
                    .map(loID -> {
                        Infos.OpeCompLot opeCompLot = new Infos.OpeCompLot();
                        opeCompLot.setLotID(loID);
                        return opeCompLot;
                    }).collect(Collectors.toList()));
        }

        if (log.isInfoEnabled()) {
            log.info(">>> sxForceMoveOutReq exit");
        }
        return retVal;
    }

    @Override
    public Results.ForceMoveOutReqResult sxForceMoveOutForIBReq(Infos.ObjCommon objCommon,
                                                                ObjectIdentifier equipmentID,
                                                                ObjectIdentifier controlJobID,
                                                                Boolean spcResultRequiredFlag,
                                                                String claimMemo) {
        if (log.isInfoEnabled()) {
            log.info(">>> sxForceMoveOutForIBReq entry");
        }
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

        log.info("Check Transaction ID and equipment Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        /*-----------------------------------------------------------------------*/
        /*   Get Started Lot information which is sepcified with ControlJob ID   */
        /*-----------------------------------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("Get Started Lot information which is specified with ControlJob ID");
        }
        Outputs.ObjControlJobStartReserveInformationOut startReserveInformationOut =
                controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                        controlJobID,
                        false);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        // Get required equipment lock mode
        log.info("calling object_lockMode_Get() ： {}", equipmentID);
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        if (log.isDebugEnabled()) {
            log.debug("Get Lock Mode");
        }
        Outputs.ObjLockModeOut objectLockModeGet = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        int lockMode = objectLockModeGet.getLockMode().intValue();
        if (log.isDebugEnabled()) {
            log.debug("Lock Mode : {}", lockMode);
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(equipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objectLockModeGet.getRequiredLockForMainObject());
            log.info("calling advanced_object_Lock() : {} ", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //   Lock Macihne object
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        /*--------------------------------*/
        /*   Lock Cassette / Lot Object   */
        /*--------------------------------*/
        List<Infos.StartCassette> strStartCassette = startReserveInformationOut.getStartCassetteList();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        strStartCassette.forEach(startCassette -> {
            if (log.isDebugEnabled()) {
                log.debug("LoadPurposeType is {}", startCassette.getLoadPurposeType());
            }
            if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                //equipment_monitorCreationFlag_Get();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("step6 - check equipment monitor creation flag");
                    }
                    equipmentMethod.equipmentMonitorCreationFlagGet(objCommon, equipmentID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getMonitorCreatReqd(), e.getCode())) {
                        //---------------------------------------------------------------------------------------------
                        //if monitor creationflag was TRUE, at least one lot should be existed in Empty Cassete;
                        //---------------------------------------------------------------------------------------------
                        if (CimArrayUtils.isEmpty(startCassette.getLotInCassetteList())) {
                            log.error("object_Lock() == RC_MONITOR_CREAT_REQD");
                            Validations.check(retCodeConfigEx.getMonitorCreatReqd());
                        }
                    }
                }
                cassetteIDs.add(startCassette.getCassetteID());
                return;
            }
            cassetteIDs.add(startCassette.getCassetteID());
            startCassette.getLotInCassetteList().stream()
                    /*---------------------------*/
                    /*   Omit Not-OpeStart Lot   */
                    /*---------------------------*/
                    .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                    .forEach(lotInCassette -> lotIDs.add(lotInCassette.getLotID()));
        });

        if (log.isInfoEnabled()) {
            log.info("cassetteIDCnt : {}", CimArrayUtils.getSize(cassetteIDs));
            log.info("lotIDCnt : {}", CimArrayUtils.getSize(lotIDs));
        }

        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            String onlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            if (CimStringUtils.equals(onlineModeGet, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                // Lock Equipment ProcLot Element (Count)
                Inputs.ObjAdvanceLockIn advanceLockIn = new Inputs.ObjAdvanceLockIn();
                advanceLockIn.setObjectID(equipmentID);
                advanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                advanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT);
                advanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_COUNT));
                advanceLockIn.setKeyList(new ArrayList<>());
                objectLockMethod.advancedObjectLock(objCommon, advanceLockIn);
            }

            // Lock Equipment ProcLot Element (Write)
            List<String> procLotSeq = lotIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, procLotSeq));
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //**********************************************************//*
            //*  Lock All Port Object for internal Buffer Equipment.   *//*
            //**********************************************************//*
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
            eqpPortInfo.getEqpPortStatuses().forEach(eqpPortStatus -> {
                log.info("Locked port object : {}", eqpPortStatus.getPortID());
                objectLockMethod.objectLockForEquipmentResource(objCommon,
                        equipmentID,
                        eqpPortStatus.getPortID(),
                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            });
        }
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            //*------------------------------*//*
            //*   Lock Material Location     *//*
            //*------------------------------*//*
            strStartCassette.forEach(startCassette -> {
                Inputs.ObjAdvancedObjectLockForEquipmentResourceIn equipmentResourceIn =
                        new Inputs.ObjAdvancedObjectLockForEquipmentResourceIn();
                equipmentResourceIn.setEquipmentID(equipmentID);
                equipmentResourceIn.setClassName(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_BYCJ);
                equipmentResourceIn.setObjectID(controlJobID);
                equipmentResourceIn.setObjectLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                equipmentResourceIn.setBufferResourceName(startCassette.getLoadPurposeType());
                equipmentResourceIn.setBufferResourceLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ);
                objectLockMethod.advancedObjectLockForEquipmentResource(objCommon, equipmentResourceIn);
            });

            //*------------------------------*//*
            //*   Lock ControlJob Object     *//*
            //*------------------------------*//*
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }

        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/

        log.info("calling objectSequence_Lock() : {}", BizConstant.SP_CLASSNAME_POSCASSETTE);
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        log.info("calling objectSequence_Lock() : {}", BizConstant.SP_CLASSNAME_POSLOT);
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process End                                             */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/


        // Gets all actual move lot
        final List<Infos.LotInCassette> moveOutLots = strStartCassette.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                /*---------------------------*/
                /*   Omit Not-OpeStart Lot   */
                /*---------------------------*/
                .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                .collect(Collectors.toList());

        /*-------------------------------------------------*/
        /*   call cassette_APCInformation_GetDR            */
        /*-------------------------------------------------*/
        List<Infos.ApcBaseCassette> apcBaseCassettes;
        try {
            if (log.isDebugEnabled()) {
                log.debug("call cassetteAPCInformationGetDR");
            }
            apcBaseCassettes = cassetteMethod.cassetteAPCInformationGetDR(objCommon,
                    equipmentID,
                    strStartCassette);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())) {
                throw e;
            } else {
                apcBaseCassettes = Collections.emptyList();
            }
        }

        // add season by ho
        seasoningService.sxSeasonForForceMoveOut(objCommon, equipmentID);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        /*---------------------------------*/
        /*   Get Equipment's Online Mode   */
        /*---------------------------------*/
        Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeGetOut =
                portMethod.portResourceCurrentOperationModeGet(objCommon,
                        equipmentID,
                        strStartCassette.get(0).getUnloadPortID());
        // Port Operation Mode
        final Infos.OperationMode operationMode = portResourceCurrentOperationModeGetOut.getOperationMode();

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - lotProcessState                                                   */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        moveOutLots.forEach(lotInCassette -> {
            Outputs.ObjLotAllStateGetOut lotAllStateGetOut = lotMethod.lotAllStateGet(objCommon,
                    lotInCassette.getLotID());
            if (CimStringUtils.unEqual(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotAllStateGetOut.getProcessState())) {
                log.info("lot_allState_Get() strLot_allState_Get_out.processState == SP_Lot_ProcState_Processing");
                throw new ServiceException(retCodeConfig.getInvalidLotProcstat());
            }
            if (CimStringUtils.equals(BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD, lotAllStateGetOut.getHoldState())) {
                log.info("HoldState == NotOnHold");
                throw new ServiceException(retCodeConfig.getInvalidLotHoldStat());
            }
        });


        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Equipment                                         */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - All lof lot, which is contained in controlJob, must be existing   */
        /*     in the equipment's processing information.                        */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        equipmentMethod.equipmentCheckConditionForOpeComp(objCommon, equipmentID, strStartCassette);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process (Reverse Order of OpeStart Procedure)                  */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        /*--------------------------------*/
        /*                                */
        /*   CP Test Function Procedure   */
        /*                                */
        /*--------------------------------*/
        moveOutLots.forEach(lotInCassette -> {
            /*-----------------------------------------*/
            /*   Check Test Type of Current Process    */
            /*-----------------------------------------*/
            ObjectIdentifier testTypeID = null;
            try {
                testTypeID = lotMethod.lotTestTypeIDGet(objCommon, lotInCassette.getLotID());
            } catch (ServiceException ex) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundTestType(), ex.getCode())) {
                    throw ex;
                }
            }

            if (ObjectIdentifier.isNotEmpty(testTypeID)) {
                /*---------------------------------------------------------------*/
                /*   Gather Bin Summary Information based on lotID & testTypeID  */
                /*---------------------------------------------------------------*/
                if (log.isDebugEnabled()) {
                    log.debug("step30 - Gather Bin Summary Information based on lotID & testTypeID");
                }
                List<Infos.WaferBinSummary> waferBinSummaries = binSummaryMethod.binSummaryGetByTestTypeDR(objCommon,
                        lotInCassette.getLotID(),
                        testTypeID);
                /*----------------------------------------------------------*/
                /*   Update Wafer Die quantity based on the input parameter  */
                /*-----------------------------------------------------------*/
                List<Infos.LotWaferAttributes> lotWaferAttributes = waferBinSummaries.stream()
                        .map(waferBinSummary -> {
                            if (waferBinSummary.getBinReportCount() > 0) {
                                Infos.LotWaferAttributes lotWaferAttribute = new Infos.LotWaferAttributes();
                                lotWaferAttribute.setWaferID(waferBinSummary.getWaferId());
                                lotWaferAttribute.setGoodUnitCount(waferBinSummary.getGoodUnitCount());
                                lotWaferAttribute.setRepairUnitCount(waferBinSummary.getRepairUnitCount());
                                lotWaferAttribute.setFailUnitCount(waferBinSummary.getFailUnitCount());
                                return lotWaferAttribute;
                            } else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (log.isDebugEnabled()) {
                    log.debug("step31- Update wafer Die quantity based on the input parameter");
                }
                lotMethod.lotWaferChangeDie(objCommon, lotInCassette.getLotID(), lotWaferAttributes);
            }
        });

        /*------------------------------------------------------*/
        /*                                                      */
        /*     FlowBatch Related Information Update Procedure   */
        /*                                                      */
        /*------------------------------------------------------*/
        /*----------------------------------------------------------*/
        /*   Update FlowBatch Information of Equipment              */
        /*----------------------------------------------------------*/
        flowBatchMethod.flowBatchInformationUpdateByOpeComp(objCommon, equipmentID, strStartCassette);

        /*----------------------------------------------*/
        /*                                              */
        /*   Process Operation Update Procedure         */
        /*                                              */
        /*----------------------------------------------*/
        /*-------------------------------------------------*/
        /*   Update Process Operation (actual comp xxxx)   */
        /*-------------------------------------------------*/
        processMethod.processActualCompInformationSet(objCommon, strStartCassette);


        /*------------------------------------------------------------*/
        /*                                                            */
        /*   Reticle / Fixture Related Information Update Procedure   */
        /*                                                            */
        /*------------------------------------------------------------*/

        /*--------------------------------------------------------*/
        /*   Check ProcessDurable was Used for Operation or Not   */
        /*--------------------------------------------------------*/
        try {
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
        } catch (ServiceException e) {
            if (Validations.isEquals(e.getCode(), retCodeConfig.getEquipmentProcessDurableNotRequired())) {
                log.info("equipment_processDurableRequiredFlag_Get() == RC_EQP_PROCDRBL_NOT_REQD");
            } else if (Validations.isEquals(e.getCode(), retCodeConfig.getEquipmentProcessDurableReticleRequired())
                    || Validations.isEquals(e.getCode(), retCodeConfig.getEquipmentProcessDurableFixtRequired())) {
                moveOutLots.forEach(lotInCassette -> {
                    if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Get Used Reticles for lot");
                        }
                        processMethod.processAssignedReticleGet(objCommon, lotInCassette.getLotID()).forEach(startReticle -> {
                            if (log.isDebugEnabled()) {
                                log.debug("check reticle usage limitation for reticle");
                            }
                            Outputs.ObjReticleUsageLimitationCheckOut reticleUsedLimitRetCode =
                                    reticleMethod.reticleUsageLimitationCheck(objCommon, startReticle.getReticleID());
                            if (log.isTraceEnabled()) {
                                log.trace("UsageLimitOverFlag is {}", reticleUsedLimitRetCode.isUsageLimitOverFlag());
                            }
                            if (CimBooleanUtils.isTrue(reticleUsedLimitRetCode.isUsageLimitOverFlag())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Call System Message");
                                }
                                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTCLUSAGELIMITOVER);
                                alertMessageRptParams.setSystemMessageText(reticleUsedLimitRetCode.getMessageText());
                                alertMessageRptParams.setNotifyFlag(true);
                                alertMessageRptParams.setSystemMessageTimeStamp(
                                        CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /*-------------------------*/
                                /*   Call System Message   */
                                /*-------------------------*/
                                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                            }

                            /*---------------------------------------------*/
                            /*  Set last used time stamp for used reticle  */
                            /*---------------------------------------------*/
                            log.debug("Set last used time stamp for used reticle");
                            reticleMethod.reticleLastUsedTimeSet(startReticle.getReticleID(),
                                    objCommon.getTimeStamp().getReportTimeStamp());
                        });
                    } else {
                        /*------------------------------*/
                        /*   Get Used Fixture for Lot   */
                        /*------------------------------*/
                        if (log.isDebugEnabled()) {
                            log.debug("Get Used Fixture for lot");
                        }
                        processMethod.processAssignedFixtureGet(objCommon, lotInCassette.getLotID()).forEach(startFixture -> {
                            /*------------------------------------*/
                            /*   Fixture Usage Limitation Check   */
                            /*------------------------------------*/
                            if (log.isDebugEnabled()) {
                                log.debug("Fixture Usage Limitation Check");
                            }
                            Outputs.objFixtureUsageLimitationCheckOut objFixtureUsageLimitationCheckOut =
                                    fixtureMethod.fixtureUsageLimitationCheck(objCommon, startFixture.getFixtureID());

                            /*-------------------------*/
                            /*   Call System Message   */
                            /*-------------------------*/
                            if (log.isDebugEnabled()) {
                                log.debug("Call System Message");
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_FIXTUSAGELIMITOVER);
                            alertMessageRptParams.setSystemMessageText(objFixtureUsageLimitationCheckOut.getMessageText());
                            alertMessageRptParams.setNotifyFlag(true);
                            alertMessageRptParams.setSystemMessageTimeStamp(
                                    CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        });
                    }
                });
            } else {
                throw e;
            }
        }


        /*----------------------------------------------*/
        /*                                              */
        /*   Lot Related Information Update Procedure   */
        /*                                              */
        /*----------------------------------------------*/

        // 检查并设置Min Q-Time最小时间限制
        minQTimeMethod.checkAndSetRestrictions(objCommon, strStartCassette);

        /*-----------------------------------------*/
        /*   Change Lot Process State to Waiting   */
        /*-----------------------------------------*/
        lotMethod.lotProcessStateMakeWaiting(objCommon, strStartCassette);

        /*--------------------------------------------*/
        /*   Change lot Pending Move Next Flag true   */
        /*--------------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("step51 - set lot's pending move next flag to true");
        }
        moveOutLots.stream()
                .map(Infos.LotInCassette::getLotID)
                .forEach(lotID -> lotMethod.setLotMoveNextRequired(objCommon, lotID, true));

        /*------------------------------------------------------------------------------------*/
        /*   Prepare for PO Moving                                                            */
        /*------------------------------------------------------------------------------------*/
        List<ObjectIdentifier> operationStartLotIds = moveOutLots.stream()
                .map(Infos.LotInCassette::getLotID)
                .peek(lotID -> {
                    /*-------------------------------------*/
                    /*   Set Contamination Level for Lot   */
                    /*-------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step52 - update contamination flag");
                    }
                    contaminationMethod.lotContaminationLevelAndPrFlagSet(lotID);
                })
                .peek(lotID -> {
                    /*-----------------------------------------*/
                    /*   Set Contamination Level for Carrier   */
                    /*-----------------------------------------*/
                    if (log.isDebugEnabled()) {
                        log.debug("step55 - lotCassetteCategoryUpdateForContaminationControl");
                    }
                    lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);
                }).collect(Collectors.toList());


        /*---------------------------------------------------------------*/
        /*   Auto Hold after force complete                              */
        /*---------------------------------------------------------------*/
        moveOutLots.forEach(lotInCassette -> {
            //【step51】 - sxHoldLotListInq --调用接口
            List<Infos.LotHoldListAttributes> holdListAttributes = lotInqService.sxHoldLotListInq(objCommon,
                    lotInCassette.getLotID());

            holdListAttributes.forEach(lotHoldListAttributes -> {
                if (ObjectIdentifier.equalsWithValue(BizConstant.SP_REASON_RUNNINGHOLD, lotHoldListAttributes.getReasonCodeID())) {
                    log.info("Find Running Hold Record!!");
                    List<Infos.LotHoldReq> strLotHoldReleaseReqList = new ArrayList<>();
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    strLotHoldReleaseReqList.add(lotHoldReq);
                    lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
                    lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
                    lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());
                    lotHoldReq.setResponsibleOperationMark(lotHoldListAttributes.getResponsibleOperationMark());
                    lotHoldReq.setRouteID(lotHoldListAttributes.getResponsibleRouteID());
                    lotHoldReq.setOperationNumber(lotHoldListAttributes.getResponsibleOperationNumber());
                    lotHoldReq.setRelatedLotID(lotHoldListAttributes.getRelatedLotID());
                    lotHoldReq.setClaimMemo("Auto Hold after force MoveOut");
                    ObjectIdentifier aReasonCodeId = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_RUNNINGHOLDRELEASE);

                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotInCassette.getLotID());
                    holdLotReleaseReqParams.setReleaseReasonCodeID(aReasonCodeId);
                    holdLotReleaseReqParams.setHoldReqList(strLotHoldReleaseReqList);
                    //【step52】 - txHoldLotReleaseReq
                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                }
            });

            //Execute Lot Hold
            ObjectIdentifier reasonCodeId = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_FORCECOMPHOLD);
            List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            strHoldListSeq.add(lotHoldReq);
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
            lotHoldReq.setHoldReasonCodeID(reasonCodeId);
            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(ObjectIdentifier.emptyIdentifier());
            lotHoldReq.setOperationNumber("");
            lotHoldReq.setRelatedLotID(ObjectIdentifier.emptyIdentifier());
            lotHoldReq.setClaimMemo("Auto Hold after force MoveOut For IB");

            //【step53】 - txHoldLotReq  --调用接口
            lotService.sxHoldLotReq(objCommon, lotInCassette.getLotID(), strHoldListSeq);
        });


        /*----------------------------------------------------*/
        /*                                                    */
        /*   Equipment Related Information Update Procedure   */
        /*                                                    */
        /*----------------------------------------------------*/

        /*----------------------------------------------------------------*/
        /*   Remove ControlJobLot from EqpInfo's ProcessingLot Sequence   */
        /*----------------------------------------------------------------*/
        equipmentMethod.equipmentProcessingLotDelete(objCommon, equipmentID, strStartCassette);

        /*---------------------------------------------*/
        /*   Maintain Eqp's Status when OFF-LINE Mode  */
        /*---------------------------------------------*/
        if (CimStringUtils.equals(operationMode.getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            log.info("strPortResource_currentOperationMode_Get_out.strOperationMode.onlineMode == SP_Eqp_OnlineMode_Offline");

            /*--------------------------------------------------------*/
            /*   Change Equipment's Status to 'STANDBY' if necessary  */
            /*--------------------------------------------------------*/

            /*===== get StateChangeableFlag ===*/
            Boolean changeFlag = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon, equipmentID);
            if (CimBooleanUtils.isTrue(changeFlag)) {
                log.info("strEquipment_currentState_CheckToManufacturing_out.ManufacturingStateChangeableFlag == TRUE");

                /*===== get Defaclt Status Code for Productive / Standby ===*/
                ObjectIdentifier equipmentStatus =
                        equipmentMethod.equipmentRecoverStateGetManufacturing(objCommon, equipmentID);

                /*---------------------------------*/
                /*   Call txEqpStatusChangeReq()   */
                /*---------------------------------*/
                equipmentService.sxEqpStatusChangeReq(objCommon, equipmentID, equipmentStatus, claimMemo);
            }
        }

        /*--------------------------------------*/
        /*   Equipment Usage Limitation Check   */
        /*--------------------------------------*/
        Outputs.ObjEquipmentUsageLimitationCheckOut usageLimitationCheckOut =
                equipmentMethod.equipmentUsageLimitationCheck(objCommon, equipmentID);
        if (CimBooleanUtils.isTrue(usageLimitationCheckOut.isUsageLimitOverFlag())) {
            log.info("strEquipment_usageLimitation_Check_out.usageLimitOverFlag == TRUE");

            /*-------------------------*/
            /*   Call System Message   */
            /*-------------------------*/
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EQPUSAGELIMITOVER);
            alertMessageRptParams.setSystemMessageText(usageLimitationCheckOut.getMessageText());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(equipmentID);
            alertMessageRptParams.setSystemMessageTimeStamp(
                    CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }


        /*----------------------------------------------------*/
        /*                                                    */
        /*   ControlJob Related Information Update Proceure   */
        /*                                                    */
        /*----------------------------------------------------*/

        /*------------------------*/
        /*   Delete Control Job   */
        /*------------------------*/
        boolean isNotPartialMoveOut = CimStringUtils.unEqualIn(objCommon.getTransactionID(),
                TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),
                TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue());
        if (isNotPartialMoveOut) {
            log.info("strObjCommonIn.transactionID is not OEQPW012 and OEQPW024");

            //----------------------------------------------------------------------------------
            // Delete ControlJob from EQP.
            // The relation of it with Lot and Cassette is deleted in txEDCWithSpecCheckActionReq.
            //----------------------------------------------------------------------------------
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobID(controlJobID);
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP);
            cjStatusChangeReqParams.setClaimMemo(claimMemo);
            controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);
        }

        if (isNotPartialMoveOut) {
            //---------------------------------------------//
            //   Delete Control Job From Lot and Cassette  //
            //---------------------------------------------//
            log.info("Delete controlJob from Lot and Cassette.");
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE);
            cjStatusChangeReqParams.setControlJobID(controlJobID);
            cjStatusChangeReqParams.setClaimMemo(claimMemo);
            controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);
        }


        /*---------------------------------------------------*/
        /*                                                   */
        /*   Cassette Related Information Update Procedure   */
        /*                                                   */
        /*---------------------------------------------------*/
        strStartCassette.forEach(startCassette -> {
            /*-------------------------------------*/
            /*   Cassette Usage Limitation Check   */
            /*-------------------------------------*/
            //【step71】 - cassette_usageLimitation_Check
            Outputs.ObjCassetteUsageLimitationCheckOut cassetteUsageLimitationCheckOut =
                    cassetteMethod.cassetteUsageLimitationCheck(objCommon, startCassette.getCassetteID());

            if (CimBooleanUtils.isTrue(cassetteUsageLimitationCheckOut.isUsageLimitOverFlag())) {
                /*-------------------------*/
                /*   Call System Message   */
                /*-------------------------*/
                log.info("strCassette_usageLimitation_Check_out.usageLimitOverFlag == TRUE");
                // step72 - txAlertMessageRpt
                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_CASTUSAGELIMITOVER);
                alertMessageRptParams.setSystemMessageText(cassetteUsageLimitationCheckOut.getMessageText());
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setSystemMessageTimeStamp(
                        CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            }
            /*---------------------------------------*/
            /*   Update Cassette's MultiLotType      */
            /*---------------------------------------*/
            //【step73】 - cassette_multiLotType_Update
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, startCassette.getCassetteID());
        });

        /*--------------------------*/
        /*                          */
        /*   Event Make Procedure   */
        /*                          */
        /*--------------------------*/
        strStartCassette.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream()
                        .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                        .map(lotInCassette -> {
                            Inputs.LotOperationMoveEventMakeOpeComp event = new Inputs.LotOperationMoveEventMakeOpeComp();
                            event.setTransactionID(TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
                            event.setEquipmentID(equipmentID);
                            event.setOperationMode(ObjectIdentifier.fetchValue(operationMode.getOperationMode()));
                            event.setControlJobID(controlJobID);
                            event.setCassetteID(startCassette.getCassetteID());
                            event.setLotInCassette(lotInCassette);
                            event.setClaimMemo(claimMemo);
                            return event;
                        })).forEach(event -> {
                            if (log.isDebugEnabled()) {
                                log.debug(" Event Make Procedure");
                            }
                            eventMethod.lotOperationMoveEventMakeOpeComp(objCommon, event);
                        }
                );

        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   BR Script Procedure                                                  */
        /*                                                                        */
        /*------------------------------------------------------------------------*/

        /*-----------------------*/
        /*   Execute BRScript    */
        /*-----------------------*/
        moveOutLots.forEach(lotInCassette -> {
            /*-------------------------*/
            /*   Execute Post Script   */
            /*-------------------------*/
            if (log.isInfoEnabled()) {
                log.info("Execute POST Script");
            }
            Params.ProcessControlScriptRunReqParams scriptParams = new Params.ProcessControlScriptRunReqParams();
            scriptParams.setEquipmentId(equipmentID);
            scriptParams.setLotId(lotInCassette.getLotID());
            scriptParams.setPhase(BizConstant.SP_BRSCRIPT_POST);
            scriptParams.setUser(objCommon.getUser());
            processControlScriptService.sxProcessControlScriptRunReq(objCommon, scriptParams);
        });


        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   Send OpeComp Report to DCS Procedure                                 */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
        // todo: DCSMgr_SendOperationCompletedRpt

        /*-----------------------------------------------------*/
        /*   Call APCRuntimeCapability_DeleteDR                */
        /*-----------------------------------------------------*/
        log.info("call APCRuntimeCapability_DeleteDR");
        apcMethod.apcRuntimeCapabilityDeleteDR(objCommon, controlJobID);

        /*-------------------------------------------------*/
        /*   Call APCMgr_SendControlJobInformationDR       */
        /*-------------------------------------------------*/
        log.info("call APCMgr_SendControlJobInformationDR()");
        try {
            apcMethod.APCMgrSendControlJobInformationDR(objCommon,
                    equipmentID,
                    controlJobID,
                    BizConstant.SP_APC_CONTROLJOBSTATUS_COMPLETED,
                    apcBaseCassettes);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())) {
                throw e;
            }
        }

        /*--------------------------------------*/
        /*  Update Reticle's LastUsedTimestamp  */
        /*--------------------------------------*/
        reticleMethod.reticleLastUsedTimeStampUpdate(objCommon, startReserveInformationOut.getStartCassetteList());

        /*--------------------------------------*/
        /*  Constraint exception lot change     */
        /*--------------------------------------*/
        constraintMethod.constraintExceptionLotChangeForOpeComp(objCommon, operationStartLotIds, controlJobID);


        // check contamination level and pr flag
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepOut(objCommon, lotIDs, equipmentID);

        /*--------------------------*/
        /*                          */
        /*   Set Return Structure   */
        /*                          */
        /*--------------------------*/
        Results.ForceMoveOutReqResult retVal = new Results.ForceMoveOutReqResult();
        // Check this is need to EDC check
        final boolean specCheckRequiredFlag = strStartCassette.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(Infos.LotInCassette::getMoveInFlag)
                .anyMatch(lotInCassette -> lotInCassette.getStartRecipe().getDataCollectionFlag());
        if (specCheckRequiredFlag && BizConstant.SP_EQP_ONLINEMODE_OFFLINE.equals(operationMode.getOnlineMode())) {
            // Get EDC check result for Lots.
            final Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut edcInformationGets =
                    lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon,
                            equipmentID,
                            operationStartLotIds);

            // Get the MoveOut Lot
            final Results.MoveOutReqResult moveOutReqResult = equipmentMethod.equipmentFillInTxTRC004(objCommon,
                    edcInformationGets.getStrStartCassette(),
                    null);
            retVal.setStrOpeCompLot(moveOutReqResult.getMoveOutLot());
        } else {
            retVal.setStrOpeCompLot(operationStartLotIds.stream()
                    .map(loID -> {
                        Infos.OpeCompLot opeCompLot = new Infos.OpeCompLot();
                        opeCompLot.setLotID(loID);
                        return opeCompLot;
                    }).collect(Collectors.toList()));
        }

        if (log.isInfoEnabled()) {
            log.info(">>> sxForceMoveOutForIBReq exit");
        }
        return retVal;
    }

    @Override
    public Results.PartialMoveOutReqResult sxPartialMoveOutReq(Infos.ObjCommon objCommon,
                                                               Params.PartialMoveOutReqParams partialMoveOutReqParams,
                                                               String apcIFControlStatus,
                                                               String dcsIFControlStatus) {
        if (log.isInfoEnabled()) {
            log.info(">>> sxPartialMoveOutReq entry");
        }

        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------
        Results.PartialMoveOutReqResult retVal = new Results.PartialMoveOutReqResult();
        ObjectIdentifier equipmentID = partialMoveOutReqParams.getEquipmentID();
        ObjectIdentifier controlJobID = partialMoveOutReqParams.getControlJobID();

        retVal.setEquipmentID(equipmentID);
        retVal.setControlJobID(controlJobID);
        //------------------------------------------------------
        // step1 - Check Function Availability
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Step1 - Check Function Availability");
        }
        String pjControlEnable = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();
        Validations.check(CimStringUtils.unEqual(pjControlEnable, BizConstant.SP_FUNCTION_AVAILABLE_TRUE),
                retCodeConfig.getFunctionNotAvailable());

        // step2 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        if (log.isDebugEnabled()) {
            log.debug("Get Lock Mode.");
        }
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);
            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        // step5 controlJob_startLotWaferInfo_Get
        if (log.isDebugEnabled()) {
            log.debug("Step5 - controlJob_startLotWaferInfo_Get");
        }
        Inputs.ControlJobStartLotWaferInfoGetIn controlJobStartLotWaferInfoGetIn =
                new Inputs.ControlJobStartLotWaferInfoGetIn();
        controlJobStartLotWaferInfoGetIn.setControlJobID(controlJobID);
        controlJobStartLotWaferInfoGetIn.setStartLotOnlyFlag(false);
        Infos.ControlJobInformation controlJobStartLotWaferInfoGet =
                controlJobMethod.controlJobStartLotWaferInfoGet(objCommon, controlJobStartLotWaferInfoGetIn);


        boolean slmCapabilityFlag = false;
        /*--------------------------------------*/
        /*   Get SLM Switch for the equipment   */
        /*--------------------------------------*/
        if (log.isDebugEnabled()) {
            log.debug("Step6 - equipmentBRInfoGetDR(...)");
        }
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        // Check SLM Capability
        if (CimBooleanUtils.isTrue(eqpBrInfo.isFmcCapabilityFlag())) {
            slmCapabilityFlag = true;
        }
        List<Infos.EqpContainerPosition> eqpContainerPositionList = null;
        if (slmCapabilityFlag) {
            if (log.isInfoEnabled()) {
                log.info("slmCapabilityFlag = true");
            }
            Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn =
                    new Inputs.ObjEquipmentContainerPositionInfoGetIn();
            positionInfoGetIn.setEquipmentID(equipmentID);
            positionInfoGetIn.setKey(controlJobID);
            positionInfoGetIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);

            if (log.isDebugEnabled()) {
                log.debug("Step7 - equipmentContainerPositionInfoGet(...)");
            }
            Infos.EqpContainerPositionInfo equipmentContainerPositionInfoResult =
                    equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, positionInfoGetIn);
            eqpContainerPositionList = equipmentContainerPositionInfoResult.getEqpContainerPositionList();
        }

        boolean bEquipmentOnLineFlag = true;
        /*--------------------------------*/
        /*   Lock Cassette / Lot Object   */
        /*--------------------------------*/
        List<Infos.ControlJobCassetteInfo> controlJobCassetteInfoList =
                controlJobStartLotWaferInfoGet.getControlJobCassetteInfoList();
        Set<ObjectIdentifier> cassetteIDs = new HashSet<>();
        Set<ObjectIdentifier> orgLotIDs = new HashSet<>();
        AtomicInteger totalStartWaferCount = new AtomicInteger(0);

        controlJobCassetteInfoList.stream()
                .flatMap(controlJobCassetteInfo -> {
                    cassetteIDs.add(controlJobCassetteInfo.getCassetteID());
                    return controlJobCassetteInfo.getControlJobCassetteLotList().stream();
                })
                .filter(controlJobCassetteLot -> CimBooleanUtils.isTrue(controlJobCassetteLot.isOperationStartFlag()))
                .forEach(controlJobCassetteLot -> {
                    orgLotIDs.add(controlJobCassetteLot.getLotID());
                    totalStartWaferCount.addAndGet(CimArrayUtils.getSize(controlJobCassetteLot.getWaferIDs()));
                });

        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (log.isInfoEnabled()) {
                log.info("lockMode != 0");
            }
            // Step8 - equipment_onlineMode_Get
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                // Step9 - advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon,
                        new Inputs.ObjAdvanceLockIn(equipmentID,
                                BizConstant.SP_CLASSNAME_POSMACHINE,
                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                                (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_COUNT,
                                Collections.emptyList()));
            }
            // Lock eqp ProcLot Element
            // Step10 - advanced_object_Lock
            List<String> procLotSeq = orgLotIDs.stream()
                    .map(ObjectIdentifier::getValue)
                    .collect(Collectors.toList());
            objectLockMethod.advancedObjectLock(objCommon,
                    new Inputs.ObjAdvanceLockIn(equipmentID,
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                            (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                            procLotSeq));

            // Lock Equipment LoadCassette Element (Write)
            List<String> loadCastSeq = cassetteIDs.stream()
                    .map(ObjectIdentifier::getValue)
                    .collect(Collectors.toList());
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                    loadCastSeq));

            //*------------------------------*//
            //*   Lock controljob Object     *//
            //*------------------------------*//
            // Step12 - object_Lock
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }
        /*------------------------------*/
        /*   Lock cassette/lot Object   */
        /*------------------------------*/
        // Step13 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        // step14 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, orgLotIDs);

        //------------------------------------------------------
        // Check Equipment
        //------------------------------------------------------
        // The "PJ Level Control Flag" of the equipment should be "True"
        // Equipment online mode should be "On-Line Remote"
        // Multiple recipe capability of the equipment should be "MultipleRecipe"
        if (log.isDebugEnabled()) {
            log.debug("Step15 - equipmentProcessJobLevelControlCheck(...)");
        }
        try {
            equipmentMethod.equipmentProcessJobLevelControlCheck(objCommon,
                    equipmentID,
                    true,
                    true,
                    true,
                    false);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getEqpOnlineMode(), e.getCode())) {
                bEquipmentOnLineFlag = false;
            } else {
                throw e;
            }
        }

        //---------------------------
        // Check Actions
        //---------------------------
        //     Action should be one of the followings
        //         a.   SP_PartialOpeComp_Action_OpeComp
        //         b.   SP_PartialOpeComp_Action_OpeCompWithHold
        //         c.   SP_PartialOpeComp_Action_OpeStartCancel
        //         d.   SP_PartialOpeComp_Action_OpeStartCancelWithHold
        List<Infos.PartialOpeCompAction> partialOpeCompActions = partialMoveOutReqParams.getPartialOpeCompActionList();
        if (log.isDebugEnabled()) {
            log.debug("Step16 - Check Actions");
        }
        partialOpeCompActions.forEach(partialOpeCompAction -> {
            final String actionCode = partialOpeCompAction.getActionCode();
            final boolean unEqualIn = CimStringUtils.unEqualIn(actionCode,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD);
            Validations.check(unEqualIn,
                    new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Invalid Action Code"));
        });


        //------------------------------------------------------
        // Check controlJob status if equipment is Online
        //------------------------------------------------------
        //   controlJob should be "Executing"
        if (log.isDebugEnabled()) {
            log.debug("Step17 - equipmentOnlineModeGet(...)");
        }
        String operationMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
        if (CimStringUtils.unEqual(operationMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            Outputs.ObjControlJobStatusGetOut controlJobStatusResult =
                    controlJobMethod.controlJobStatusGet(objCommon, controlJobID);
            // Check ControlJob Status
            if (CimStringUtils.unEqual(controlJobStatusResult.getControlJobStatus(),
                    BizConstant.SP_CONTROLJOBSTATUS_EXECUTING)) {
                Validations.check(new OmCode(retCodeConfigEx.getInvalidCjstatus(),
                        controlJobStatusResult.getControlJobStatus()));
            }
        }

        //------------------------------
        // Check waferIDs
        //------------------------------
        // All specified waferIDs should be inside the start lot of the controlJob.
        if (log.isDebugEnabled()) {
            log.debug("Step18 - Check waferIDs");
        }
        long requestedWaferCount = partialOpeCompActions
                .stream()
                .flatMap(partialOpeCompAction -> partialOpeCompAction.getWaferIDs().stream())
                .distinct()
                .count();
        // Get waferID from controlJob
        if (requestedWaferCount != totalStartWaferCount.get()) {
            // request wafer count is not correct
            Validations.check(new OmCode(retCodeConfig.getInvalidParameterWithMsg(),
                    "All started lot wafer should be selected"));
        }

        //-------------------------------------------------------------------------------------------------
        // All wafers in start lot should be specified in input waferIDs (including non-sampled wafers).
        //-------------------------------------------------------------------------------------------------
        // Create result structure
        List<Infos.PartialOpeCompLot> partialOpeCompLotList = new ArrayList<>();
        for (Infos.PartialOpeCompAction partialOpeCompAction : partialOpeCompActions) {
            final List<ObjectIdentifier> waferIDs = partialOpeCompAction.getWaferIDs();
            final String actionCode = partialOpeCompAction.getActionCode();

            for (ObjectIdentifier tmpWaferID : waferIDs) {
                boolean bFoundWaferFlag = false;
                for (Infos.ControlJobCassetteInfo controlJobCassetteInfo : controlJobCassetteInfoList) {
                    List<Infos.ControlJobCassetteLot> controlJobCassetteLotList =
                            controlJobCassetteInfo.getControlJobCassetteLotList();
                    for (Infos.ControlJobCassetteLot controlJobCassetteLot : controlJobCassetteLotList) {
                        if (CimBooleanUtils.isFalse(controlJobCassetteLot.isOperationStartFlag())) {
                            continue;
                        }
                        for (ObjectIdentifier waferID : controlJobCassetteLot.getWaferIDs()) {
                            if (ObjectIdentifier.equalsWithValue(tmpWaferID, waferID)) {
                                bFoundWaferFlag = true;
                                // found the wafer
                                ObjectIdentifier tmpLotID = controlJobCassetteLot.getLotID();
                                // find tmpLotID in strPartialOpeCompLotSeq
                                boolean bFoundCompLotFlag = false;
                                for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                                    if (ObjectIdentifier.equalsWithValue(tmpLotID, partialOpeCompLot.getLotID())) {
                                        // Check if action is the same
                                        if (CimStringUtils.equals(actionCode, partialOpeCompLot.getActionCode())) {
                                            // add this wafer to the lot
                                            partialOpeCompLot.getWaferIDs().add(tmpWaferID);
                                            bFoundCompLotFlag = true;
                                            break;
                                        } else {
                                            // same lot ID, different action -> this lot needs to be split
                                            if (log.isInfoEnabled()) {
                                                log.info("same lot ID, different action -> this lot needs to be split");
                                            }
                                        }
                                    }
                                }
                                if (CimBooleanUtils.isFalse(bFoundCompLotFlag)) {
                                    // create this lot to new strPartialOpeCompLotSeq
                                    Infos.PartialOpeCompLot partialOpeCompLot = new Infos.PartialOpeCompLot();
                                    partialOpeCompLot.setLotID(tmpLotID);
                                    partialOpeCompLot.setActionCode(actionCode);

                                    List<ObjectIdentifier> waferIds = new ArrayList<>();
                                    waferIds.add(tmpWaferID);
                                    partialOpeCompLot.setWaferIDs(waferIds);
                                    partialOpeCompLotList.add(partialOpeCompLot);
                                }
                                break;
                            }
                        }
                        if (bFoundWaferFlag) {
                            break;
                        }
                    }
                    if (bFoundWaferFlag) {
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bFoundWaferFlag)) {
                    Validations.check(new OmCode(retCodeConfig.getInvalidParameterWithMsg(),
                            "tmpWaferID is not found"));
                }
                boolean isMoveOut = CimStringUtils.equalsIn(actionCode,
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP,
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD);

                if (slmCapabilityFlag && isMoveOut) {
                    for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositionList) {
                        if (ObjectIdentifier.equalsWithValue(tmpWaferID, eqpContainerPosition.getWaferID())) {
                            boolean fmcStateInFlag = CimStringUtils.equalsIn(eqpContainerPosition.getFmcState(),
                                    BizConstant.SP_SLMSTATE_RESERVED, BizConstant.SP_SLMSTATE_STORED);
                            Validations.check(fmcStateInFlag, retCodeConfig.getInvalidSLMStatusOfContainerPosition(),
                                    ObjectIdentifier.fetchValue(eqpContainerPosition.getContainerPositionID()),
                                    eqpContainerPosition.getFmcState());
                            break;
                        }
                    }
                }
            }
        }

        // todo: DCSMgr_SendPartialOperationCompletedRpt

        //------------------------------------------------------------------------------------------
        //  Lot needs to be split if wafers within the lot are selected for different actions
        //------------------------------------------------------------------------------------------
        for (ObjectIdentifier orgLotID : orgLotIDs) {
            List<String> actionCodes = new ArrayList<>();
            partialOpeCompLotList.forEach(partialOpeCompLot -> {
                if (ObjectIdentifier.equalsWithValue(orgLotID, partialOpeCompLot.getLotID())) {
                    actionCodes.add(partialOpeCompLot.getActionCode());
                }
            });

            if (CimArrayUtils.getSize(actionCodes) > 1) {
                // This lot need to split
                //  Decide parent Lot: Parent lot should be decided from following priorities. If wafer sampling is
                //  used, non-sampled wafers should be in the same lot
                //      a.   OpeComp lot
                //      b.   OpeCompWithHold Lot
                //      c.   OpeStartCancel Lot
                //      d.   OpeStartCancelWithHold Lot
                boolean bParentLotFoundFlag = false;
                String parentLotActionCode = null;
                // First look for OpeComp lot
                for (String actionCode : actionCodes) {
                    if (CimStringUtils.equals(actionCode, BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP)) {
                        bParentLotFoundFlag = true;
                        parentLotActionCode = actionCode;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bParentLotFoundFlag)) {
                    // Continue to look for OpeCompWithHold Lot
                    for (String actionCode : actionCodes) {
                        if (CimStringUtils.equals(actionCode, BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD)) {
                            // Parent lot found
                            bParentLotFoundFlag = true;
                            parentLotActionCode = actionCode;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(bParentLotFoundFlag)) {
                    // Continue to look for OpeStartCancel Lot
                    for (String actionCode : actionCodes) {
                        if (CimStringUtils.equals(actionCode, BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL)) {
                            // Parent lot found
                            bParentLotFoundFlag = true;
                            parentLotActionCode = actionCode;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(bParentLotFoundFlag)) {
                    if (log.isDebugEnabled()) {
                        log.debug("This is not going to happen, do nothing");
                    }
                }

                for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                    if (ObjectIdentifier.equalsWithValue(orgLotID, partialOpeCompLot.getLotID())) {
                        if (CimStringUtils.unEqual(partialOpeCompLot.getActionCode(), parentLotActionCode)) {
                            // split lot
                            Params.SplitLotReqParams splitLotReqParams = new Params.SplitLotReqParams();
                            splitLotReqParams.setParentLotID(orgLotID);
                            splitLotReqParams.setChildWaferIDs(partialOpeCompLot.getWaferIDs());
                            splitLotReqParams.setFutureMergeFlag(false);
                            splitLotReqParams.setMergedRouteID(ObjectIdentifier.emptyIdentifier());
                            splitLotReqParams.setMergedOperationNumber("");
                            splitLotReqParams.setBranchingRouteSpecifyFlag(false);
                            splitLotReqParams.setSubRouteID(ObjectIdentifier.emptyIdentifier());
                            splitLotReqParams.setReturnOperationNumber("");

                            //--------------------------------
                            // Split Lot
                            //--------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("Step20 - sxSplitLotReq(...)");
                            }
                            Results.SplitLotReqResult splitLotReqResultRetCode =
                                    lotService.sxSplitLotReq(objCommon, splitLotReqParams);

                            partialOpeCompLot.setLotID(splitLotReqResultRetCode.getChildLotID());
                            // Transfer following PO data from parent lot to child lot by waferID if child lot
                            // is going to perform opeComp
                            //     a.   Wafer chamber process
                            //     b.   Inside Chamber wafer position
                            //     c.   Processed wafers (Sampling wafers)
                            //     d.   Assigned Recipe parameters
                            //     e.   Collected data
                            //     f.   Assigned reticles
                            //     g.   Assigned fixtures
                            // Copy following PO data from parent PO to child lot
                            //     a.   Assigned equipment
                            //     b.   Assigned port group
                            //     c.   Assigned logical recipe
                            //     d.   Assigned recipe
                            //     e.   Assigned physical recipe
                            //     f.   Assigned recipe parameter change type
                            //     g.   Assigned dc flag
                            if (log.isDebugEnabled()) {
                                log.debug("Step21 - lotProcessOperationDataTransferInProcessing(...)");
                            }
                            lotMethod.lotProcessOperationDataTransferInProcessing(objCommon,
                                    orgLotID,
                                    partialOpeCompLot.getLotID());

                            if (CimStringUtils.equalsIn(partialOpeCompLot.getActionCode(),
                                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP,
                                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD)) {
                                // Copy schedule change reservation information to child lot for OpeComp and OpeCompWithHold lot
                                //     A.   Get schedule change reservation (object type = Lot) of the original lot (parent lot = opeComp lot)
                                //     B.   Copy the schedule change reservation to newly created child lots
                                Params.LotPlanChangeReserveListInqParams params =
                                        new Params.LotPlanChangeReserveListInqParams();
                                params.setObjectID(orgLotID.getValue());
                                params.setObjectType(BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT);
                                params.setLotInfoChangeFlag(0);
                                if (log.isDebugEnabled()) {
                                    log.debug("Step22 - sxLotPlanChangeReserveListInq(...)");
                                }
                                Results.LotPlanChangeReserveListInqResult lotPlanChangeReserveListInqResult =
                                        planInqService.sxLotPlanChangeReserveListInq(objCommon, params);
                                lotPlanChangeReserveListInqResult.getSchdlChangeReservations().forEach(data -> {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Step23 - sxLotPlanChangeReserveCreateReq(...)");
                                    }
                                    planService.sxLotPlanChangeReserveCreateReq(objCommon, data, "");
                                });
                            }
                        }
                    }
                }
            }
        }


        // Notify external subsystems (TCS)
        //     a.   Notify Conditions (No action needs to be taken if condition does not match)
        //         i.   Equipment should be under manual Comp mode
        //     b.   Handle Result
        //         i.   If returned "actionResult" is not "SP_ActionResult_OK", MMS should rollback the transaction
        //              and return the result to caller.
        Outputs.SendPartialMoveOutReqOut sendPartialMoveOutReqOut = null;
        if (bEquipmentOnLineFlag) {
            //step24 - TCSMgr_SendPartialMoveOutReq
            Inputs.SendPartialMoveOutReqIn sendPartialMoveOutReqIn = new Inputs.SendPartialMoveOutReqIn();
            sendPartialMoveOutReqIn.setEquipmentID(equipmentID);
            sendPartialMoveOutReqIn.setObjCommonIn(objCommon);
            sendPartialMoveOutReqIn.setControlJobID(controlJobID);
            sendPartialMoveOutReqIn.setStrPartialOpeCompLotSeq(partialOpeCompLotList);
            sendPartialMoveOutReqOut = (Outputs.SendPartialMoveOutReqOut)
                    tcsMethod.sendTCSReq(TCSReqEnum.sendPartialMoveOutReq, sendPartialMoveOutReqIn);
        }
        if (sendPartialMoveOutReqOut != null && null != sendPartialMoveOutReqOut.getPartialMoveOutReqResult()) {
            final List<Infos.PartialOpeCompLot> opeCompLotList = sendPartialMoveOutReqOut
                    .getPartialMoveOutReqResult()
                    .getPartialOpeCompLotList();
            opeCompLotList.forEach(partialOpeCompLot -> {
                if (CimStringUtils.unEqual(BizConstant.SP_ACTIONRESULT_OK, partialOpeCompLot.getActionResult())) {
                    Validations.check(retCodeConfigEx.getRequestRejectByTcs());
                }
            });

            // Merge result from TCS
            opeCompLotList.forEach(tmpPartialOpeCompLot -> {
                for (Infos.PartialOpeCompLot opeCompLot : partialOpeCompLotList) {
                    if (ObjectIdentifier.equalsWithValue(tmpPartialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                        if (log.isInfoEnabled()) {
                            log.info("Found the Lot, set actionResult ");
                        }
                        //found the lot
                        opeCompLot.setActionResult(tmpPartialOpeCompLot.getActionResult());
                        break;
                    }
                }
            });
        }
        retVal.setPartialOpeCompLotList(partialOpeCompLotList);


        List<Infos.ApcBaseCassette> strAPCBaseCassetteListForOpeComp = new ArrayList<>();
        List<Infos.ApcBaseCassette> strAPCBaseCassetteListForOpeStartCancel = new ArrayList<>();
        // Get updated controlJob information
        Inputs.ControlJobStartLotWaferInfoGetIn controlJobStartLotWaferInfoGetIn2 =
                new Inputs.ControlJobStartLotWaferInfoGetIn();
        controlJobStartLotWaferInfoGetIn2.setControlJobID(controlJobID);
        controlJobStartLotWaferInfoGetIn2.setStartLotOnlyFlag(true);

        if (log.isDebugEnabled()) {
            log.debug("Step25 - controlJobStartLotWaferInfoGet(...)");
        }
        Infos.ControlJobInformation controlJobStartLotWaferInfoGetOutResult2 =
                controlJobMethod.controlJobStartLotWaferInfoGet(objCommon, controlJobStartLotWaferInfoGetIn2);

        //--------------------------------------------------------------------
        // For OpeComp and OpeCompWithHold Lot
        //     A.   Update controlJob information with involved lots
        //     B.   Call txMoveOutReq (txMoveOutForIBReq)
        //--------------------------------------------------------------------

        // Create OpeComp and OpeCompWithHold Lot list => opeCompLotInfoList
        List<Infos.PartialOpeCompLot> partialOpeCompLotSeqForOpeComp = partialOpeCompLotList.stream()
                .filter(partialOpeCompLot -> CimStringUtils.equalsIn(partialOpeCompLot.getActionCode(),
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP,
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD))
                .collect(Collectors.toList());
        if (CimArrayUtils.isNotEmpty(partialOpeCompLotSeqForOpeComp)) {
            if (log.isDebugEnabled()) {
                log.debug("Step26 - controlJobUpdateForPartialOpeComp(...)");
            }
            //---------------------------------------
            // Control Job update
            //---------------------------------------
            controlJobMethod.controlJobUpdateForPartialOpeComp(objCommon,
                    controlJobStartLotWaferInfoGetOutResult2,
                    partialOpeCompLotSeqForOpeComp);

            //---------------------------------------
            // Move Out
            //---------------------------------------
            Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
            opeComWithDataReqParams.setEquipmentID(equipmentID);
            opeComWithDataReqParams.setControlJobID(controlJobID);
            opeComWithDataReqParams.setSpcResultRequiredFlag(partialMoveOutReqParams.isSpcResultRequiredFlag());
            opeComWithDataReqParams.setOpeMemo(partialMoveOutReqParams.getClaimMemo());
            opeComWithDataReqParams.setApcBaseCassetteListForOpeComp(strAPCBaseCassetteListForOpeComp);
            opeComWithDataReqParams.setApcifControlStatus(apcIFControlStatus);
            opeComWithDataReqParams.setDcsifControlStatus(dcsIFControlStatus);
            if (log.isDebugEnabled()) {
                log.debug("Step27 - sxMoveOutReq(...)");
            }
            Results.MoveOutReqResult moveOutReqResultRetCode = this.sxMoveOutReq(objCommon, opeComWithDataReqParams);

            retVal.setHoldReleasedLotIDs(moveOutReqResultRetCode.getHoldReleasedLotIDs());
            List<Infos.OpeCompLot> operationCompleteLot = moveOutReqResultRetCode.getMoveOutLot();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                for (Infos.OpeCompLot opeCompLot : operationCompleteLot) {
                    if (ObjectIdentifier.equalsWithValue(partialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                        // found the lot, set result
                        partialOpeCompLot.setLotStatus(opeCompLot.getLotStatus());
                        partialOpeCompLot.setSpecCheckResult(opeCompLot.getSpcCheckResult());
                        partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpcCheckResult());
                        break;
                    }
                }
            }
        }

        //-----------------------------------------------------------------
        // For OpeStartCancel and OpeStartCancelWithHold Lot
        //     A.   Update controlJob information with involved lots
        //     B.   Call txMoveInCancelReq (txMoveInCancelForIBReq)
        //-----------------------------------------------------------------
        List<Infos.PartialOpeCompLot> partialOpeCompLotSeqForOpeStartCancel = partialOpeCompLotList.stream()
                .filter(partialOpeCompLot -> CimStringUtils.equalsIn(partialOpeCompLot.getActionCode(),
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL,
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD))
                .collect(Collectors.toList());

        if (CimArrayUtils.isNotEmpty(partialOpeCompLotSeqForOpeStartCancel)) {
            if (log.isDebugEnabled()) {
                log.debug("Step28 - controlJobUpdateForPartialOpeComp(...)");
            }
            //---------------------------------------
            // Control Job update
            //---------------------------------------
            controlJobMethod.controlJobUpdateForPartialOpeComp(objCommon,
                    controlJobStartLotWaferInfoGetOutResult2,
                    partialOpeCompLotSeqForOpeStartCancel);
            //---------------------------------------
            // Move In Cancel
            //---------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Step29 - sxMoveInCancelReq(...)");
            }
            equipmentService.sxMoveInCancelReq(objCommon,
                    equipmentID,
                    controlJobID,
                    "",
                    strAPCBaseCassetteListForOpeStartCancel,
                    apcIFControlStatus,
                    dcsIFControlStatus);
        }

        //------------------------------------------------
        // Update controlJob as actual data
        //------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Step30 - controlJobUpdateForPartialOpeComp(...)");
        }
        controlJobMethod.controlJobUpdateForPartialOpeComp(objCommon,
                controlJobStartLotWaferInfoGetOutResult2,
                partialOpeCompLotList);

        //-------------------------------
        // Delete controlJob
        //-------------------------------
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        cjStatusChangeReqParams.setControlJobID(controlJobID);
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE);
        if (log.isDebugEnabled()) {
            log.debug("Step31 - sxCJStatusChangeReqService(...)");
        }
        controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        // Notify external subsystems (APC)
        //     a.   Notify Conditions (No action needs to be taken if condition does not match)
        //         i.   APC system is configured
        //     b.   Handle Result
        //         i.   If the return code is not RC_OK, MMS should rollback the transaction and
        //              return the result to caller.

        //TODO Step32 - APCMgr_SendControlJobInfoForPartialOpeCompDR

        if (log.isInfoEnabled()) {
            log.info(">>> sxPartialMoveOutReq exit");
        }
        return retVal;
    }

    @Override
    public Results.PartialMoveOutReqResult sxMoveOutWithRunningSplitForIBReq(Infos.ObjCommon objCommon,
                                                                             Params.PartialMoveOutReqParams partialMoveOutReqParams,
                                                                             String apcIFControlStatus,
                                                                             String dcsIFControlStatus) {
        if (log.isInfoEnabled()) {
            log.info(">>> sxMoveOutWithRunningSplitForIBReq entry");
        }

        Results.PartialMoveOutReqResult retVal = new Results.PartialMoveOutReqResult();
        ObjectIdentifier equipmentID = partialMoveOutReqParams.getEquipmentID();
        ObjectIdentifier controlJobID = partialMoveOutReqParams.getControlJobID();

        retVal.setEquipmentID(equipmentID);
        retVal.setControlJobID(controlJobID);


        //------------------------------------------------------
        // step1 - Check Function Availability
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Step1 - Check Function Availability");
        }
        String processJobControlEnable = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();
        Validations.check(CimStringUtils.unEqual(processJobControlEnable, BizConstant.SP_FUNCTION_AVAILABLE_TRUE),
                retCodeConfig.getFunctionNotAvailable());

        /*-----------------------------------------*/
        /*                                         */
        /*               Object Lock               */
        /*                                         */
        /*-----------------------------------------*/

        // step2 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        // // Get required equipment lock mode
        if (log.isDebugEnabled()) {
            log.debug("Get object mode.");
        }
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        // step3 - advanced_object_Lock
        long lockMode = objLockModeOut.getLockMode();
        if (log.isDebugEnabled()) {
            log.debug("lockMode = {}", lockMode);
        }
        Inputs.ObjAdvanceLockIn advanceLockIn = new Inputs.ObjAdvanceLockIn();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            // Lock Equipment Main Object
            advanceLockIn.setObjectID(equipmentID);
            advanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            advanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            advanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            advanceLockIn.setKeyList(Collections.emptyList());
            // step3 - advanced_object_Lock
            if (log.isDebugEnabled()) {
                log.debug("step3 - advancedObjectLock(...)");
            }
            objectLockMethod.advancedObjectLock(objCommon, advanceLockIn);
        } else {
            /*--------------------------------*/
            /*   Lock Machine object          */
            /*--------------------------------*/
            // step4 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        // step5 controlJob_startLotWaferInfo_Get
        if (log.isDebugEnabled()) {
            log.debug("Step5 - controlJobStartLotWaferInfoGet(...)");
        }
        Inputs.ControlJobStartLotWaferInfoGetIn startLotWaferInfoGetIn = new Inputs.ControlJobStartLotWaferInfoGetIn();
        startLotWaferInfoGetIn.setControlJobID(controlJobID);
        startLotWaferInfoGetIn.setStartLotOnlyFlag(false);
        Infos.ControlJobInformation controlJobStartLotWaferInfoGet =
                controlJobMethod.controlJobStartLotWaferInfoGet(objCommon, startLotWaferInfoGetIn);

        /*--------------------------------------*/
        /*   Get SLM Switch for the equipment   */
        /*--------------------------------------*/

        boolean bEquipmentOnLineFlag = true;
        /*--------------------------------*/
        /*   Lock Cassette / Lot Object   */
        /*--------------------------------*/
        List<Infos.ControlJobCassetteInfo> controlJobCassetteInfoList =
                controlJobStartLotWaferInfoGet.getControlJobCassetteInfoList();

        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        List<ObjectIdentifier> orgLotIDs = new ArrayList<>();
        AtomicInteger totalStartWaferCount = new AtomicInteger(0);

        controlJobCassetteInfoList.stream()
                .flatMap(controlJobCassetteInfo -> {
                    cassetteIDs.add(controlJobCassetteInfo.getCassetteID());
                    return controlJobCassetteInfo.getControlJobCassetteLotList().stream();
                })
                .filter(controlJobCassetteLot -> CimBooleanUtils.isTrue(controlJobCassetteLot.isOperationStartFlag()))
                .forEach(controlJobCassetteLot -> {
                    orgLotIDs.add(controlJobCassetteLot.getLotID());
                    totalStartWaferCount.addAndGet(CimArrayUtils.getSize(controlJobCassetteLot.getWaferIDs()));
                });

        // Step8 - equipment_onlineMode_Get

        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            // Step8 - equipment_onlineMode_Get
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                // Step9 - advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                        (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_COUNT,
                        Collections.emptyList()));
            }

            //-----------------------------
            // Lock eqp ProcLot Element
            //-----------------------------
            // Step10 - advanced_object_Lock
            List<String> procLotSeq = orgLotIDs.stream()
                    .map(ObjectIdentifier::getValue)
                    .collect(Collectors.toList());
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                    procLotSeq));

            //------------------------------------------------
            // Lock Equipment LoadCassette Element (Write)
            //------------------------------------------------
            List<String> loadCastSeq = cassetteIDs.stream()
                    .map(ObjectIdentifier::getValue)
                    .collect(Collectors.toList());
            // Step11 - advanced_object_Lock
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                    loadCastSeq));

            /*------------------------------*/
            /*   Lock Material Location     */
            /*------------------------------*/
            controlJobCassetteInfoList.forEach(controlJobCassetteInfo -> {
                Inputs.ObjAdvancedObjectLockForEquipmentResourceIn equipmentResourceIn = new
                        Inputs.ObjAdvancedObjectLockForEquipmentResourceIn();
                equipmentResourceIn.setEquipmentID(equipmentID);
                equipmentResourceIn.setClassName(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_BYCJ);
                equipmentResourceIn.setObjectID(controlJobID);
                equipmentResourceIn.setObjectLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                equipmentResourceIn.setBufferResourceName(controlJobCassetteInfo.getLoadPurposeType());
                equipmentResourceIn.setBufferResourceLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ);
                objectLockMethod.advancedObjectLockForEquipmentResource(objCommon, equipmentResourceIn);
            });

            /*------------------------------*/
            /*   Lock ControlJob Object     */
            /*------------------------------*/
            // Step12 - object_Lock
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }
        /*------------------------------*/
        /*   Lock cassette/lot Object   */
        /*------------------------------*/
        // Step13 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        // step14 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, orgLotIDs);

        // Lock End

        //------------------------------------------------------
        // Check Equipment
        //------------------------------------------------------
        // The "PJ Level Control Flag" of the equipment should be "True"
        // Equipment online mode should be "On-Line Remote"
        // Multiple recipe capability of the equipment should be "MultipleRecipe"
        if (log.isDebugEnabled()) {
            log.debug("Step15 - equipmentProcessJobLevelControlCheck(...)");
        }
        try {
            equipmentMethod.equipmentProcessJobLevelControlCheck(objCommon,
                    equipmentID,
                    true,
                    true,
                    true,
                    false);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getEqpOnlineMode(), e.getCode())) {
                bEquipmentOnLineFlag = false;
            } else {
                throw e;
            }
        }

        //-------------------------
        // Check Actions
        //-------------------------
        //     Action should be one of the followings
        //         a.   SP_PartialOpeComp_Action_OpeComp
        //         b.   SP_PartialOpeComp_Action_OpeCompWithHold
        //         c.   SP_PartialOpeComp_Action_OpeStartCancel
        //         d.   SP_PartialOpeComp_Action_OpeStartCancelWithHold
        List<Infos.PartialOpeCompAction> partialOpeCompActions = partialMoveOutReqParams.getPartialOpeCompActionList();
        if (log.isDebugEnabled()) {
            log.debug("Step16 - Check Actions");
        }
        partialOpeCompActions.forEach(partialOpeCompAction -> {
            final String actionCode = partialOpeCompAction.getActionCode();
            final boolean unEqualIn = CimStringUtils.unEqualIn(actionCode,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL,
                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD);
            Validations.check(unEqualIn, new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Invalid Action Code"));
        });

        //--------------------------------------------------
        // Check controlJob status if equipment is Online
        //--------------------------------------------------
        //   controlJob should be "Executing"
        if (log.isDebugEnabled()) {
            log.debug("Step17 - equipmentOnlineModeGet(...)");
        }
        String operationMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
        if (CimStringUtils.unEqualIn(operationMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            Outputs.ObjControlJobStatusGetOut controlJobStatusResult =
                    controlJobMethod.controlJobStatusGet(objCommon, controlJobID);
            // Check ControlJob Status
            if (CimStringUtils.unEqualIn(controlJobStatusResult.getControlJobStatus(),
                    BizConstant.SP_CONTROLJOBSTATUS_EXECUTING)) {
                Validations.check(new OmCode(retCodeConfigEx.getInvalidCjstatus(),
                        controlJobStatusResult.getControlJobStatus()));
            }
        }

        //---------------------------
        // Check waferIDs
        //---------------------------
        // All specified waferIDs should be inside the start lot of the controlJob.
        if (log.isDebugEnabled()) {
            log.debug("Step18 - Check waferIDs");
        }
        int requestedWaferCount = 0;
        for (Infos.PartialOpeCompAction partialOpeCompAction : partialOpeCompActions) {
            requestedWaferCount += CimArrayUtils.getSize(partialOpeCompAction.getWaferIDs());
        }

        // Get waferID from controlJob
        if (requestedWaferCount != totalStartWaferCount.get()) {
            // request wafer count is not correct
            Validations.check(retCodeConfig.getInvalidParameterWithMsg(),
                    "All started lot wafer should be selected");
        }


        if (log.isDebugEnabled()) {
            log.debug("All wafers in start lot should be specified in input waferIDs (including non-sampled wafers).");
        }
        // All wafers in start lot should be specified in input waferIDs (including non-sampled wafers).
        // Create result structure
        List<Infos.PartialOpeCompLot> partialOpeCompLotList = new ArrayList<>();
        for (Infos.PartialOpeCompAction partialOpeCompAction : partialOpeCompActions) {
            final List<ObjectIdentifier> waferIDs = partialOpeCompAction.getWaferIDs();
            final String actionCode = partialOpeCompAction.getActionCode();

            for (ObjectIdentifier tmpWaferID : waferIDs) {
                boolean bFoundWaferFlag = false;
                for (Infos.ControlJobCassetteInfo controlJobCassetteInfo : controlJobCassetteInfoList) {
                    List<Infos.ControlJobCassetteLot> controlJobCassetteLotList =
                            controlJobCassetteInfo.getControlJobCassetteLotList();
                    for (Infos.ControlJobCassetteLot controlJobCassetteLot : controlJobCassetteLotList) {
                        if (CimBooleanUtils.isFalse(controlJobCassetteLot.isOperationStartFlag())) {
                            continue;
                        }
                        for (ObjectIdentifier waferID : controlJobCassetteLot.getWaferIDs()) {
                            if (ObjectIdentifier.equalsWithValue(tmpWaferID, waferID)) {
                                bFoundWaferFlag = true;
                                // found the wafer
                                ObjectIdentifier tmpLotID = controlJobCassetteLot.getLotID();
                                // find tmpLotID in strPartialOpeCompLotSeq
                                boolean bFoundCompLotFlag = false;
                                for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                                    if (ObjectIdentifier.equalsWithValue(tmpLotID, partialOpeCompLot.getLotID())) {
                                        // Check if action is the same
                                        if (CimStringUtils.equals(actionCode, partialOpeCompLot.getActionCode())) {
                                            // add this wafer to the lot
                                            partialOpeCompLot.getWaferIDs().add(tmpWaferID);
                                            bFoundCompLotFlag = true;
                                            break;
                                        } else {
                                            // same lot ID, different action -> this lot needs to be split
                                            if (log.isInfoEnabled()) {
                                                log.info("same lot ID, different action -> this lot needs to be split");
                                            }
                                        }
                                    }
                                }
                                if (CimBooleanUtils.isFalse(bFoundCompLotFlag)) {
                                    // create this lot to new strPartialOpeCompLotSeq
                                    Infos.PartialOpeCompLot partialOpeCompLot = new Infos.PartialOpeCompLot();
                                    partialOpeCompLot.setLotID(tmpLotID);
                                    partialOpeCompLot.setActionCode(actionCode);

                                    List<ObjectIdentifier> waferIDs3 = new ArrayList<>();
                                    waferIDs3.add(tmpWaferID);
                                    partialOpeCompLot.setWaferIDs(waferIDs3);
                                    partialOpeCompLotList.add(partialOpeCompLot);
                                }
                                break;
                            }
                        }
                        if (bFoundWaferFlag) {
                            break;
                        }
                    }
                    if (bFoundWaferFlag) {
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bFoundWaferFlag)) {
                    Validations.check(retCodeConfig.getInvalidParameterWithMsg(), "tmpWaferID is not found");
                }
            }
        }

        // Notify to DCS if SP_DCS_AVAILABLE = 1
        //     a.  If notify to DCS returns error, ignore the error if SP_DCS_IGNORE_OPECOMP_RESULT is configured as "1".
        // todo: DCSMgr_SendPartialOperationCompletedRpt

        //-----------------------------------------------------------------------------------------
        //  Lot needs to be split if wafers within the lot are selected for different actions
        //-----------------------------------------------------------------------------------------
        for (ObjectIdentifier orgLotID : orgLotIDs) {
            List<String> actionCodes = new ArrayList<>();
            partialOpeCompLotList.forEach(partialOpeCompLot -> {
                if (ObjectIdentifier.equalsWithValue(orgLotID, partialOpeCompLot.getLotID())) {
                    actionCodes.add(partialOpeCompLot.getActionCode());
                }
            });

            if (CimArrayUtils.getSize(actionCodes) > 1) {
                // This lot need to split
                //  Decide parent Lot: Parent lot should be decided from following priorities. If wafer sampling is
                //  used, non-sampled wafers should be in the same lot
                //      a.   OpeComp lot
                //      b.   OpeCompWithHold Lot
                //      c.   OpeStartCancel Lot
                //      d.   OpeStartCancelWithHold Lot
                boolean bParentLotFoundFlag = false;
                String parentLotActionCode = null;
                // First look for OpeComp lot
                for (String actionCode : actionCodes) {
                    if (CimStringUtils.equals(actionCode, BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP)) {
                        bParentLotFoundFlag = true;
                        parentLotActionCode = actionCode;
                        break;
                    }
                }

                if (CimBooleanUtils.isFalse(bParentLotFoundFlag)) {
                    // Continue to look for OpeCompWithHold Lot
                    for (String actionCode : actionCodes) {
                        if (CimStringUtils.equals(actionCode, BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD)) {
                            // Parent lot found
                            bParentLotFoundFlag = true;
                            parentLotActionCode = actionCode;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(bParentLotFoundFlag)) {
                    // Continue to look for OpeStartCancel Lot
                    for (String actionCode : actionCodes) {
                        if (CimStringUtils.equals(actionCode, BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL)) {
                            // Parent lot found
                            bParentLotFoundFlag = true;
                            parentLotActionCode = actionCode;
                            break;
                        }
                    }
                }

                if (CimBooleanUtils.isFalse(bParentLotFoundFlag)) {
                    if (log.isDebugEnabled()) {
                        log.debug("This is not going to happen, do nothing");
                    }
                }

                for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                    if (ObjectIdentifier.equalsWithValue(orgLotID, partialOpeCompLot.getLotID())) {
                        if (CimStringUtils.unEqualIn(partialOpeCompLot.getActionCode(), parentLotActionCode)) {
                            // split lot
                            Params.SplitLotReqParams splitLotReqParams = new Params.SplitLotReqParams();
                            splitLotReqParams.setParentLotID(orgLotID);
                            splitLotReqParams.setChildWaferIDs(partialOpeCompLot.getWaferIDs());
                            splitLotReqParams.setFutureMergeFlag(false);
                            splitLotReqParams.setMergedRouteID(ObjectIdentifier.emptyIdentifier());
                            splitLotReqParams.setMergedOperationNumber("");
                            splitLotReqParams.setBranchingRouteSpecifyFlag(false);
                            splitLotReqParams.setSubRouteID(ObjectIdentifier.emptyIdentifier());
                            splitLotReqParams.setReturnOperationNumber("");


                            //--------------------------------
                            // Split Lot
                            //--------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("Step20 - sxSplitLotReq(...)");
                            }
                            Results.SplitLotReqResult splitLotReqResultRetCode =
                                    lotService.sxSplitLotReq(objCommon, splitLotReqParams);

                            partialOpeCompLot.setLotID(splitLotReqResultRetCode.getChildLotID());
                            // Transfer following PO data from parent lot to child lot by waferID if child lot is
                            // going to perform opeComp
                            //     a.   Wafer chamber process
                            //     b.   Inside Chamber wafer position
                            //     c.   Processed wafers (Sampling wafers)
                            //     d.   Assigned Recipe parameters
                            //     e.   Collected data
                            //     f.   Assigned reticles
                            //     g.   Assigned fixtures
                            // Copy following PO data from parent PO to child lot
                            //     a.   Assigned equipment
                            //     b.   Assigned port group
                            //     c.   Assigned logical recipe
                            //     d.   Assigned recipe
                            //     e.   Assigned physical recipe
                            //     f.   Assigned recipe parameter change type
                            //     g.   Assigned dc flag
                            if (log.isDebugEnabled()) {
                                log.debug("Step21 - lotProcessOperationDataTransferInProcessing(...)");
                            }
                            lotMethod.lotProcessOperationDataTransferInProcessing(objCommon,
                                    orgLotID,
                                    partialOpeCompLot.getLotID());

                            if (CimStringUtils.equalsIn(partialOpeCompLot.getActionCode(),
                                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP,
                                    BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD)) {
                                // Copy schedule change reservation information to child lot for OpeComp and OpeCompWithHold lot
                                //     A.   Get schedule change reservation (object type = Lot) of the original lot (parent lot = opeComp lot)
                                //     B.   Copy the schedule change reservation to newly created child lots
                                Params.LotPlanChangeReserveListInqParams params = new Params.LotPlanChangeReserveListInqParams();
                                params.setObjectID(orgLotID.getValue());
                                params.setObjectType(BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT);
                                params.setLotInfoChangeFlag(0);

                                if (log.isDebugEnabled()) {
                                    log.debug("Step22 - sxLotPlanChangeReserveListInq(...)");
                                }
                                Results.LotPlanChangeReserveListInqResult lotPlanChangeReserveListInqResult =
                                        planInqService.sxLotPlanChangeReserveListInq(objCommon, params);

                                lotPlanChangeReserveListInqResult.getSchdlChangeReservations().forEach(reservation -> {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Step23 - sxLotPlanChangeReserveCreateReq(...)");
                                    }
                                    planService.sxLotPlanChangeReserveCreateReq(objCommon,
                                            reservation,
                                            "");
                                });
                            }
                        }
                    }
                }
            }
        }

        //----------------------------------------
        // Notify external subsystems (EAP)
        //----------------------------------------
        //     a.   Notify Conditions (No action needs to be taken if condition does not match)
        //         i.   Equipment should be under manual Comp mode
        //     b.   Handle Result
        //         i.   If returned "actionResult" is not "SP_ActionResult_OK", MMS should rollback the transaction
        //              and return the result to caller.
        Outputs.SendPartialMoveOutReqOut sendPartialMoveOutReqOut = null;
        if (bEquipmentOnLineFlag) {
            //step24 - TCSMgr_SendPartialMoveOutForIBReq
            Inputs.SendPartialMoveOutReqIn sendPartialMoveOutReqIn = new Inputs.SendPartialMoveOutReqIn();
            sendPartialMoveOutReqIn.setEquipmentID(equipmentID);
            sendPartialMoveOutReqIn.setObjCommonIn(objCommon);
            sendPartialMoveOutReqIn.setControlJobID(controlJobID);
            sendPartialMoveOutReqIn.setStrPartialOpeCompLotSeq(partialOpeCompLotList);
            if (log.isDebugEnabled()) {
                log.debug("sendTCSReq(...)");
            }
            sendPartialMoveOutReqOut = (Outputs.SendPartialMoveOutReqOut)
                    tcsMethod.sendTCSReq(TCSReqEnum.sendPartialMoveOutForInternalBufferReq, sendPartialMoveOutReqIn);
        }

        // Merge result from EAP
        if (null != sendPartialMoveOutReqOut && null != sendPartialMoveOutReqOut.getPartialMoveOutReqResult()) {
            final List<Infos.PartialOpeCompLot> opeCompLotList = sendPartialMoveOutReqOut
                    .getPartialMoveOutReqResult()
                    .getPartialOpeCompLotList();
            opeCompLotList.forEach(partialOpeCompLot -> {
                if (CimStringUtils.unEqual(BizConstant.SP_ACTIONRESULT_OK, partialOpeCompLot.getActionResult())) {
                    Validations.check(retCodeConfigEx.getRequestRejectByTcs());
                }
            });

            // Merge result from TCS
            opeCompLotList.forEach(tmpPartialOpeCompLot -> {
                for (Infos.PartialOpeCompLot opeCompLot : partialOpeCompLotList) {
                    if (ObjectIdentifier.equalsWithValue(tmpPartialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                        if (log.isInfoEnabled()) {
                            log.info("Found the Lot, set actionResult ");
                        }
                        //found the lot
                        opeCompLot.setActionResult(tmpPartialOpeCompLot.getActionResult());
                        break;
                    }
                }
            });
        }
        retVal.setPartialOpeCompLotList(partialOpeCompLotList);


        List<Infos.ApcBaseCassette> strAPCBaseCassetteListForOpeComp = new ArrayList<>();
        List<Infos.ApcBaseCassette> strAPCBaseCassetteListForOpeStartCancel = new ArrayList<>();
        // Get updated controlJob information
        Inputs.ControlJobStartLotWaferInfoGetIn controlJobStartLotWaferInfoGetIn2 =
                new Inputs.ControlJobStartLotWaferInfoGetIn();
        controlJobStartLotWaferInfoGetIn2.setControlJobID(controlJobID);
        controlJobStartLotWaferInfoGetIn2.setStartLotOnlyFlag(true);

        if (log.isDebugEnabled()) {
            log.debug("Step25 - controlJobStartLotWaferInfoGet(...)");
        }
        Infos.ControlJobInformation objControlJobStartLotWaferInfoGetOutResult2 =
                controlJobMethod.controlJobStartLotWaferInfoGet(objCommon, controlJobStartLotWaferInfoGetIn2);

        //------------------------------------------------------------------
        // For OpeComp and OpeCompWithHold Lot
        //     A.   Update controlJob information with involved lots
        //     B.   Call txMoveOutReq (txMoveOutForIBReq)
        //------------------------------------------------------------------

        // Create OpeComp and OpeCompWithHold Lot list => opeCompLotInfoList
        List<Infos.PartialOpeCompLot> partialOpeCompLotSeqForOpeComp = partialOpeCompLotList.stream()
                .filter(partialOpeCompLot -> CimStringUtils.equalsIn(partialOpeCompLot.getActionCode(),
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP,
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD))
                .collect(Collectors.toList());

        if (CimArrayUtils.isNotEmpty(partialOpeCompLotSeqForOpeComp)) {
            //---------------------------------------
            // Control Job update
            //---------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Step26 - controlJobUpdateForPartialOpeComp(...)");
            }
            controlJobMethod.controlJobUpdateForPartialOpeComp(objCommon,
                    objControlJobStartLotWaferInfoGetOutResult2,
                    partialOpeCompLotSeqForOpeComp);

            Params.MoveOutForIBReqParams opeComWithDataReqParams = new Params.MoveOutForIBReqParams();
            opeComWithDataReqParams.setEquipmentID(equipmentID);
            opeComWithDataReqParams.setControlJobID(controlJobID);
            opeComWithDataReqParams.setSpcResultRequiredFlag(partialMoveOutReqParams.isSpcResultRequiredFlag());
            opeComWithDataReqParams.setOpeMemo(partialMoveOutReqParams.getClaimMemo());

            if (log.isDebugEnabled()) {
                log.debug("Step27 - sxMoveOutForIBReq(...)");
            }
            final Results.MoveOutReqResult moveOutReqResult = this.sxMoveOutForIBReq(objCommon, opeComWithDataReqParams);

            List<Infos.OpeCompLot> operationCompleteLot = moveOutReqResult.getMoveOutLot();
            for (Infos.PartialOpeCompLot partialOpeCompLot : partialOpeCompLotList) {
                for (Infos.OpeCompLot opeCompLot : operationCompleteLot) {
                    if (ObjectIdentifier.equalsWithValue(partialOpeCompLot.getLotID(), opeCompLot.getLotID())) {
                        // found the lot, set result
                        partialOpeCompLot.setLotStatus(opeCompLot.getLotStatus());
                        partialOpeCompLot.setSpecCheckResult(opeCompLot.getSpcCheckResult());
                        partialOpeCompLot.setSpcCheckResult(opeCompLot.getSpcCheckResult());
                        break;
                    }
                }
            }
        }

        // For OpeStartCancel and OpeStartCancelWithHold Lot
        //     A.   Update controlJob information with involved lots
        //     B.   Call txMoveInCancelReq (txMoveInCancelForIBReq)
        List<Infos.PartialOpeCompLot> partialOpeCompLotSeqForOpeStartCancel = partialOpeCompLotList.stream()
                .filter(partialOpeCompLot -> CimStringUtils.equalsIn(partialOpeCompLot.getActionCode(),
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL,
                        BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD))
                .collect(Collectors.toList());

        if (CimArrayUtils.isNotEmpty(partialOpeCompLotSeqForOpeStartCancel)) {
            //---------------------------------------
            // Control Job update
            //---------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Step28 - controlJobUpdateForPartialOpeComp(...)");
            }
            controlJobMethod.controlJobUpdateForPartialOpeComp(objCommon,
                    objControlJobStartLotWaferInfoGetOutResult2,
                    partialOpeCompLotSeqForOpeStartCancel);

            //---------------------------------------
            // Move In Cancel
            //---------------------------------------
            if (log.isDebugEnabled()) {
                log.debug("Step29 - sxMoveInCancelForIBReq(...)");
            }
            equipmentService.sxMoveInCancelForIBReq(objCommon,
                    equipmentID,
                    controlJobID,
                    strAPCBaseCassetteListForOpeStartCancel,
                    apcIFControlStatus,
                    dcsIFControlStatus);
        }

        //------------------------------------------------
        // Update controlJob as actual data
        //------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("Step30 - controlJobUpdateForPartialOpeComp(...)");
        }
        controlJobMethod.controlJobUpdateForPartialOpeComp(objCommon,
                objControlJobStartLotWaferInfoGetOutResult2,
                partialOpeCompLotList);

        // Delete controlJob
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        cjStatusChangeReqParams.setControlJobID(controlJobID);
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE);
        if (log.isDebugEnabled()) {
            log.debug("Step31 - sxCJStatusChangeReqService(...)");
        }
        controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        // Notify external subsystems (APC)
        //     a.   Notify Conditions (No action needs to be taken if condition does not match)
        //         i.   APC system is configured
        //     b.   Handle Result
        //         i.   If the return code is not RC_OK, MMS should rollback the transaction and return the result to caller.

        //TODO Step32 - APCMgr_SendControlJobInfoForPartialOpeCompDR

        if (log.isInfoEnabled()) {
            log.info(">>> sxMoveOutWithRunningSplitForIBReq exit");
        }
        return retVal;
    }
}
