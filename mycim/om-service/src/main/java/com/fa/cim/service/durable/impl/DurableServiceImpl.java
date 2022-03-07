package com.fa.cim.service.durable.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.idp.tms.api.TmsService;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimDurableControlJob;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimProcessResource;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IRTMSRemoteManager;
import com.fa.cim.rtms.ReticleRTMSJustInOutReqParams;
import com.fa.cim.service.bank.IBankInqService;
import com.fa.cim.service.durable.IDurableInqService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.equipment.IEquipmentInqService;
import com.fa.cim.service.equipment.IEquipmentService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.recipe.IRecipeService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;

import static com.fa.cim.common.constant.BizConstant.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 18:52
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class DurableServiceImpl implements IDurableService {

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IEquipmentInqService equipmentInqService;

    @Autowired
    private IProcessForDurableMethod processForDurableMethod;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IEquipmentForDurableMethod equipmentForDurableMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IMachineRecipeMethod machineRecipeMethod;

    @Autowired
    private IRecipeService recipeService;

    @Autowired
    private IDurableInqService durableInqService;

    @Autowired
    private IDurableService durableService;

    @Autowired
    private IBankForDurableMethod bankForDurableMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IDurableControlJobMethod durableControlJobMethod;

    @Autowired
    private IObjectLockMethod lockMethod;

    @Autowired
    private IEquipmentService equipmentService;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IPostProcessMethod postProcessMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ICodeMethod codeMethod;

    @Autowired
    private IStockerMethod stockerMethod;

    @Autowired
    private ISorterMethod sorterMethod;

    @Autowired
    private IBankInqService bankInqService;

    @Autowired
    private TmsService tmsService;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IRTMSRemoteManager rtmsRemote;

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strStartDurablesReservationCancelReqInParam
     * @param claimMemo                                   -
     * @return com.fa.cim.dto.Results.StartDurablesReservationCancelReqResult
     * @author ho
     * @date 2020/7/6 15:03
     */
    @Override
    public Results.StartDurablesReservationCancelReqResult sxDrbMoveInReserveCancelReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam, String claimMemo) {
        Results.StartDurablesReservationCancelReqResult strStartDurablesReservationCancelReqResult = new Results.StartDurablesReservationCancelReqResult();
        log.info("PPTManager_i::txStartDurablesReservationCancelReq");
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------

        Params.StartDurablesReservationCancelReqInParam strInParm = strStartDurablesReservationCancelReqInParam;

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        //Trace InParameters
        log.info("{} {}", "in-parm equipmentID           ", ObjectIdentifier.fetchValue(strInParm.getEquipmentID()));
        log.info("{} {}", "in-parm durableControlJobID   ", ObjectIdentifier.fetchValue(strInParm.getDurableControlJobID()));

        if (0 >= CimStringUtils.length(ObjectIdentifier.fetchValue(strInParm.getDurableControlJobID()))) {
            log.info("{}", "durableControlJobID is empty.");
            Validations.check(true, retCodeConfig.getDurableControlJobBlank());
        }

        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(
                strObjCommonIn,
                strInParm.getEquipmentID());

        // Get required equipment lock mode
        Outputs.ObjLockModeOut strObjectlockModeGetout;
        Inputs.ObjLockModeIn strObjectlockModeGetin = new Inputs.ObjLockModeIn();
        strObjectlockModeGetin.setObjectID(strInParm.getEquipmentID());
        strObjectlockModeGetin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        strObjectlockModeGetin.setFunctionCategory("ODRBW042");
        strObjectlockModeGetin.setUserDataUpdateFlag(false);
        strObjectlockModeGetout = objectMethod.objectLockModeGet(
                strObjCommonIn,
                strObjectlockModeGetin);

        Inputs.ObjAdvanceLockIn strAdvancedobjectLockin = new Inputs.ObjAdvanceLockIn();

        Long lockMode = strObjectlockModeGetout.getLockMode();
        log.info("{} {}", "lockMode", lockMode);
        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            log.info("{}", "lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjectLockin.setObjectID(strInParm.getEquipmentID());
            strAdvancedobjectLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjectLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjectLockin.setLockType(strObjectlockModeGetout.getRequiredLockForMainObject());
            strAdvancedobjectLockin.setKeyList(dummySeq);

            log.info("{} {}", "calling advanced_object_Lock()", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objectLockMethod.advancedObjectLock(
                    strObjCommonIn,
                    strAdvancedobjectLockin);
        } else {
            log.info("{}", "lockMode == SP_EQP_LOCK_MODE_WRITE");
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            log.info("{}", "#### Machine Object Lock ");
            objectLockMethod.objectLock(
                    strObjCommonIn,
                    CimMachine.class,
                    strInParm.getEquipmentID());
        }

        Infos.DurableControlJobStartReserveInformationGetOut strDurableControlJobstartReserveInformationGetout;
        Infos.DurableControlJobStartReserveInformationGetIn strDurableControlJobstartReserveInformationGetin = new Infos.DurableControlJobStartReserveInformationGetIn();
        strDurableControlJobstartReserveInformationGetin.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobstartReserveInformationGetout = durableMethod.durableControlJobStartReserveInformationGet(
                strObjCommonIn,
                strDurableControlJobstartReserveInformationGetin);
        String durableCategory = strDurableControlJobstartReserveInformationGetout.getDurableCategory();
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", durableCategory);
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), durableCategory);
        }

        List<Infos.StartDurable> strStartDurables;
        strStartDurables = strDurableControlJobstartReserveInformationGetout.getStrStartDurables();
        int durableLen = CimArrayUtils.getSize(strStartDurables);
        int durableCnt = 0;

        int portCnt;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Infos.EqpPortInfo strPortResourceallPortsInSameGroupGetout;
            strPortResourceallPortsInSameGroupGetout = portMethod.portResourceAllPortsInSameGroupGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID(),
                    strStartDurables.get(0).getStartDurablePort().getLoadPortID());

            /*---------------------------------------------------------*/
            /* Lock All Ports being in the same Port Group as ToPort   */
            /*---------------------------------------------------------*/
            int lenToPort = CimArrayUtils.getSize(strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses());
            for (portCnt = 0; portCnt < lenToPort; portCnt++) {
                log.info("{} {}", "loop to strPortResourceallPortsInSameGroupGetout.strEqpPortInfo.strEqpPortStatus.length()", portCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }

            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                /*-----------------------------------*/
                /*  Check cassette transfer status   */
                /*-----------------------------------*/
                log.info("{}", "Check cassette transfer status");
                String strCassettetransferStateGetout;
                strCassettetransferStateGetout = cassetteMethod.cassetteTransferStateGet(
                        strObjCommonIn,
                        strStartDurables.get(durableCnt).getDurableId());

                if (CimStringUtils.equals(strCassettetransferStateGetout, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    /*------------------------------------*/
                    /*   Get Cassette Info in Equipment   */
                    /*------------------------------------*/
                    log.info("{}", "Get Cassette Info in Equipment");
                    Outputs.ObjCassetteEquipmentIDGetOut strCassetteequipmentIDGetout;
                    strCassetteequipmentIDGetout = cassetteMethod.cassetteEquipmentIDGet(
                            strObjCommonIn,
                            strStartDurables.get(durableCnt).getDurableId());

                    Infos.EqpPortInfo strEquipmentportInfoGetout;
                    strEquipmentportInfoGetout = equipmentMethod.equipmentPortInfoGet(
                            strObjCommonIn,
                            strCassetteequipmentIDGetout.getEquipmentID());

                    /*----------------------------------------------------------*/
                    /*  Lock port object which has the specified cassette.      */
                    /*----------------------------------------------------------*/
                    int lenFromPort = CimArrayUtils.getSize(strEquipmentportInfoGetout.getEqpPortStatuses());
                    for (portCnt = 0; portCnt < lenFromPort; portCnt++) {
                        log.info("{} {}", "loop to strEquipmentportInfoGetout.strEqpPortInfo.strEqpPortStatus.length()", portCnt);
                        if (ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getDurableId(),
                                strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getLoadedCassetteID())) {
                            objectLockMethod.objectLockForEquipmentResource(
                                    strObjCommonIn,
                                    strCassetteequipmentIDGetout.getEquipmentID(),
                                    strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                                    BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                        }
                    }
                }
            }
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                        BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

            }
        }

        objectLockMethod.objectLock(
                strObjCommonIn,
                CimDurableControlJob.class,
                strInParm.getDurableControlJobID());

        List<ObjectIdentifier> durableIDs;
        durableIDs = new ArrayList<>(durableLen);
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableIDs.add(strStartDurables.get(durableCnt).getDurableId());
        }

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimCassette.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimReticlePod.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            log.info("{] {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimProcessDurable.class,
                    durableIDs);
        }

        String portGroupID;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Outputs.ObjEquipmentPortGroupIDGetOut strEquipmentportGroupIDGetout;
            Inputs.ObjEquipmentPortGroupIDGetIn strEquipmentportGroupIDGetin = new Inputs.ObjEquipmentPortGroupIDGetIn();
            strEquipmentportGroupIDGetin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentportGroupIDGetin.setPortId(strStartDurables.get(0).getStartDurablePort().getLoadPortID());
            strEquipmentportGroupIDGetout = portMethod.equipmentPortGroupIDGet(
                    strObjCommonIn,
                    strEquipmentportGroupIDGetin);
            portGroupID = strEquipmentportGroupIDGetout.getPortGroupId();
        } else {
            log.info("{}", "durableCategory != SP_DurableCat_Cassette");
            portGroupID = "";
        }

        Inputs.ObjDurableCheckConditionForOperationIn strDurableCheckConditionForOperationin = new Inputs.ObjDurableCheckConditionForOperationIn();
        strDurableCheckConditionForOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL);
        strDurableCheckConditionForOperationin.setEquipmentId(strInParm.getEquipmentID());
        strDurableCheckConditionForOperationin.setDurableCategory(durableCategory);
        strDurableCheckConditionForOperationin.setStartDurables(strStartDurables);
        strDurableCheckConditionForOperationin.setDurableStartRecipe(strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe());
        durableMethod.durableCheckConditionForOperation(
                strObjCommonIn,
                strDurableCheckConditionForOperationin);

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableMethod.durableStatusCheckForOperation(
                    strObjCommonIn,
                    BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL,
                    strStartDurables.get(durableCnt).getDurableId(),
                    durableCategory);
        }

        Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn strEquipmentandportStateCheckForDurableOperationin = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
        strEquipmentandportStateCheckForDurableOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL);
        strEquipmentandportStateCheckForDurableOperationin.setEquipmentId(strInParm.getEquipmentID());
        strEquipmentandportStateCheckForDurableOperationin.setPortGroupId(portGroupID);
        strEquipmentandportStateCheckForDurableOperationin.setDurableCategory(durableCategory);
        strEquipmentandportStateCheckForDurableOperationin.setStartDurables(strStartDurables);
        equipmentMethod.equipmentAndPortStateCheckForDurableOperation(
                strObjCommonIn,
                strEquipmentandportStateCheckForDurableOperationin);

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                int basePGNo = 0;

                Infos.EqpPortInfo strEquipmentportInfoGetout;
                strEquipmentportInfoGetout = equipmentMethod.equipmentPortInfoGet(
                        strObjCommonIn,
                        strInParm.getEquipmentID());

                int lenPortInfo = CimArrayUtils.getSize(strEquipmentportInfoGetout.getEqpPortStatuses());
                log.info("{} {}", "strEqpPortStatus.length", lenPortInfo);

                for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {

                    if (!ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                            strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID())) {
                        log.info("{}", "Not same portID, continue...");
                        continue;
                    }

                    if (CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)) {
                        log.info("{}", "dispatchState == Dispatched");

                        if (CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                                || CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                                || CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                            log.info("{}", "portState == LoadReq or UnloadReq or '-'");

                            /*------------------------*/
                            /*   change to Required   */
                            /*------------------------*/
                            ObjectIdentifier dummyOI = null;
                            equipmentMethod.equipmentDispatchStateChange(
                                    strObjCommonIn,
                                    strInParm.getEquipmentID(),
                                    strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                                    BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                                    dummyOI,
                                    dummyOI,
                                    dummyOI,
                                    dummyOI);

                        }

                        basePGNo = portCnt;
                        break;
                    }
                }

                /*--------------------------*/
                /*   find Same Port Group   */
                /*--------------------------*/
                for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {
                    log.info("{] {}", "loop to strEqpPortStatus", portCnt);

                    /*===== Omit Base Port =====*/
                    if (portCnt == basePGNo) {
                        log.info("{}", "portCnt == basePGNo, continue...");
                        continue;
                    }
                    /*===== Omit Different Group's Port =====*/
                    if (!CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortGroup(),
                            strEquipmentportInfoGetout.getEqpPortStatuses().get(basePGNo).getPortGroup())) {
                        log.info("{}", "Not same portGroup, continue...");
                        continue;
                    }

                    /*===== Check portState =====*/
                    if (!CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                            && !CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                            && !CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                        log.info("{}", "portState != LoadReq or UnloadReq or '-', continue...");
                        continue;
                    }

                    /*===== Check dispatchState =====*/
                    if (CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)
                            || CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_NOTDISPATCHED)) {
                        log.info("{}", "Same PortGroup == Dispatched or NotDispatched");

                        /*------------------------*/
                        /*   change to Required   */
                        /*------------------------*/
                        ObjectIdentifier dummyOI = null;
                        equipmentMethod.equipmentDispatchStateChange(
                                strObjCommonIn,
                                strInParm.getEquipmentID(),
                                strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                                BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                                dummyOI,
                                dummyOI,
                                dummyOI,
                                dummyOI);

                    }
                }
            }
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut strEquipmentreticlePodPortInfoGetDRout;
                strEquipmentreticlePodPortInfoGetDRout = equipmentMethod.equipmentReticlePodPortInfoGetDR(
                        strObjCommonIn,
                        strInParm.getEquipmentID());

                int lenPortInfo = CimArrayUtils.getSize(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList());
                log.info("{} {}", "strReticlePodPortInfo.length", lenPortInfo);

                for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {
                    log.info("{} {}", "loop to strEquipmentreticlePodPortInfoGetDRout.strReticlePodPortInfo.length()", portCnt);
                    if (!ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                            strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID())) {
                        log.info("{}", "Not same portID, continue...");
                        continue;
                    }

                    if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)) {
                        log.info("{}", "dispatchStatus == Dispatched");

                        if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                                || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                                || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                            log.info("{}", "portStatus == LoadReq or UnloadReq or '-'");

                            equipmentForDurableMethod.machineReticlePodPortReserveCancel(
                                    strObjCommonIn,
                                    strInParm.getEquipmentID(),
                                    strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID());

                        }

                        break;
                    }
                }
            }
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                Results.ReticleDetailInfoInqResult strReticledetailInfoGetDRout;                                               //DSN000101569
                strReticledetailInfoGetDRout = reticleMethod.reticleDetailInfoGetDR(strObjCommonIn, strStartDurables.get(durableCnt).getDurableId(),
                        false,
                        false);
                Infos.ReticleStatusInfo reticleStatusInfo = strReticledetailInfoGetDRout.getReticleStatusInfo();

                if (0 != CimStringUtils.length(ObjectIdentifier.fetchValue(reticleStatusInfo.getReticlePodID()))) {
                    log.info("{}", "Reticle is in ReticlePod");
                    Outputs.ObjEquipmentReticlePodPortInfoGetDROut strEquipmentreticlePodPortInfoGetDRout;
                    strEquipmentreticlePodPortInfoGetDRout = equipmentMethod.equipmentReticlePodPortInfoGetDR(
                            strObjCommonIn,
                            strInParm.getEquipmentID());

                    int lenPortInfo = CimArrayUtils.getSize(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList());
                    log.info("{} {}", "strReticlePodPortInfo.length", lenPortInfo);

                    for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {
                        log.info("{} {}", "loop to strEquipmentreticlePodPortInfoGetDRout.strReticlePodPortInfo.length()", portCnt);
                        if (!ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                                strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID())) {
                            log.info("{}", "Not same portID, continue...");
                            continue;
                        }

                        if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)) {
                            log.info("{}", "dispatchStatus == Dispatched");

                            if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                                    || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                                    || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                                log.info("{}", "portStatus == LoadReq or UnloadReq or '-'");

                                equipmentForDurableMethod.machineReticlePodPortReserveCancel(
                                        strObjCommonIn,
                                        strInParm.getEquipmentID(),
                                        strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID());

                            }

                            break;
                        }
                    }
                }
            }
        }

        Infos.ProcessStartDurablesReserveInformationClearIn strProcessstartDurablesReserveInformationClearin = new Infos.ProcessStartDurablesReserveInformationClearIn();
        strProcessstartDurablesReserveInformationClearin.setDurableCategory(durableCategory);
        strProcessstartDurablesReserveInformationClearin.setStrStartDurables(strStartDurables);
        processForDurableMethod.processStartDurablesReserveInformationClear(
                strObjCommonIn,
                strProcessstartDurablesReserveInformationClearin);

        ObjectIdentifier strDurableControlJobManageReqResult;
        Params.DurableControlJobManageReqInParam strDurableControlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        strDurableControlJobManageReqInParam.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_DELETE);
        Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        strDurableControlJobCreateRequest.setEquipmentID(strInParm.getEquipmentID());
        strDurableControlJobCreateRequest.setDurableCategory(durableCategory);
        strDurableControlJobCreateRequest.setStrStartDurables(strStartDurables);
        strDurableControlJobManageReqInParam.setStrDurableControlJobCreateRequest(strDurableControlJobCreateRequest);
        strDurableControlJobManageReqResult = sxDrbCJStatusChangeReq(
                strObjCommonIn,
                strDurableControlJobManageReqInParam);

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                /*-----------------------------------------------*/
                /*   Change Cassette's Dispatch State to FALSE   */
                /*-----------------------------------------------*/
                cassetteMethod.cassetteDispatchStateChange(
                        strObjCommonIn,
                        strStartDurables.get(durableCnt).getDurableId(),
                        false);
            }
        }

        /*-----------------------------------------------------*/
        /*   Send StartDurablesReservationCancelReq() to TCS   */
        /*-----------------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        Long sleepTimeValue = 0L;
        Long retryCountValue = 0L;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        log.info("{} {}", "env value of OM_EAP_CONNECT_SLEEP_TIME  = ", sleepTimeValue);
        log.info("{} {}", "env value of OM_EAP_CONNECT_RETRY_COUNT = ", retryCountValue);

        Results.StartDurablesReservationCancelReqResult strTCSMgrSendStartDurablesReservationCancelReqout;

        //'retryCountValue + 1' means first try plus retry count
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            try {
                strTCSMgrSendStartDurablesReservationCancelReqout = tcsMethod.sendStartDurablesReservationCancelReq(
                        strObjCommonIn,
                        strInParm);
                log.info("{}", "Now TCS subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "TCS subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    try {
                        Thread.sleep(sleepTimeValue);
                        continue;
                    } catch (InterruptedException e) {
                        ex.addSuppressed(e);
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                } else {
                    log.info("{}", "TCSMgr_SendStartDurablesReservationCancelReq() != RC_OK");
                    throw ex;
                }
            }

        }

        // Set Return Structure
        strStartDurablesReservationCancelReqResult.setDurableCategory(durableCategory);
        strStartDurablesReservationCancelReqResult.setStrStartDurables(strStartDurables);

        // Return to caller
        log.info("PPTManager_i::txStartDurablesReservationCancelReq");
        return strStartDurablesReservationCancelReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strStartDurablesReservationCancelReqInParam
     * @param claimMemo
     * @return com.fa.cim.dto.Results.StartDurablesReservationCancelReqResult
     * @throws
     * @author ho
     * @date 2020/7/6 15:03
     */
    @Override
    public Results.StartDurablesReservationCancelReqResult sxDrbMoveInReserveCancelForIBReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam, String claimMemo) {
        Results.StartDurablesReservationCancelReqResult strStartDurablesReservationCancelReqResult = new Results.StartDurablesReservationCancelReqResult();
        log.info("PPTManager_i::txStartDurablesReservationCancelReq");
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------

        Params.StartDurablesReservationCancelReqInParam strInParm = strStartDurablesReservationCancelReqInParam;

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        //Trace InParameters
        log.info("{} {}", "in-parm equipmentID           ", ObjectIdentifier.fetchValue(strInParm.getEquipmentID()));
        log.info("{} {}", "in-parm durableControlJobID   ", ObjectIdentifier.fetchValue(strInParm.getDurableControlJobID()));

        if (0 >= CimStringUtils.length(ObjectIdentifier.fetchValue(strInParm.getDurableControlJobID()))) {
            log.info("{}", "durableControlJobID is empty.");
            Validations.check(true, retCodeConfig.getDurableControlJobBlank());
        }

        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(
                strObjCommonIn,
                strInParm.getEquipmentID());

        // Get required equipment lock mode
        Outputs.ObjLockModeOut strObjectlockModeGetout;
        Inputs.ObjLockModeIn strObjectlockModeGetin = new Inputs.ObjLockModeIn();
        strObjectlockModeGetin.setObjectID(strInParm.getEquipmentID());
        strObjectlockModeGetin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        strObjectlockModeGetin.setFunctionCategory("ODRBW042");
        strObjectlockModeGetin.setUserDataUpdateFlag(false);
        strObjectlockModeGetout = objectMethod.objectLockModeGet(
                strObjCommonIn,
                strObjectlockModeGetin);

        Inputs.ObjAdvanceLockIn strAdvancedobjectLockin = new Inputs.ObjAdvanceLockIn();

        Long lockMode = strObjectlockModeGetout.getLockMode();
        log.info("{} {}", "lockMode", lockMode);
        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            log.info("{}", "lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjectLockin.setObjectID(strInParm.getEquipmentID());
            strAdvancedobjectLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjectLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjectLockin.setLockType(strObjectlockModeGetout.getRequiredLockForMainObject());
            strAdvancedobjectLockin.setKeyList(dummySeq);

            log.info("{} {}", "calling advanced_object_Lock()", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objectLockMethod.advancedObjectLock(
                    strObjCommonIn,
                    strAdvancedobjectLockin);
        } else {
            log.info("{}", "lockMode == SP_EQP_LOCK_MODE_WRITE");
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            log.info("{}", "#### Machine Object Lock ");
            objectLockMethod.objectLock(
                    strObjCommonIn,
                    CimMachine.class,
                    strInParm.getEquipmentID());
        }

        Infos.DurableControlJobStartReserveInformationGetOut strDurableControlJobstartReserveInformationGetout;
        Infos.DurableControlJobStartReserveInformationGetIn strDurableControlJobstartReserveInformationGetin = new Infos.DurableControlJobStartReserveInformationGetIn();
        strDurableControlJobstartReserveInformationGetin.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobstartReserveInformationGetout = durableMethod.durableControlJobStartReserveInformationGet(
                strObjCommonIn,
                strDurableControlJobstartReserveInformationGetin);
        String durableCategory = strDurableControlJobstartReserveInformationGetout.getDurableCategory();
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", durableCategory);
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), durableCategory);
        }

        List<Infos.StartDurable> strStartDurables;
        strStartDurables = strDurableControlJobstartReserveInformationGetout.getStrStartDurables();
        int durableLen = CimArrayUtils.getSize(strStartDurables);
        int durableCnt = 0;

        int portCnt;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Infos.EqpPortInfo strPortResourceallPortsInSameGroupGetout;
            strPortResourceallPortsInSameGroupGetout = portMethod.portResourceAllPortsInSameGroupGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID(),
                    strStartDurables.get(0).getStartDurablePort().getLoadPortID());

            /*---------------------------------------------------------*/
            /* Lock All Ports being in the same Port Group as ToPort   */
            /*---------------------------------------------------------*/
            int lenToPort = CimArrayUtils.getSize(strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses());
            for (portCnt = 0; portCnt < lenToPort; portCnt++) {
                log.info("{} {}", "loop to strPortResourceallPortsInSameGroupGetout.strEqpPortInfo.strEqpPortStatus.length()", portCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }

            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                /*-----------------------------------*/
                /*  Check cassette transfer status   */
                /*-----------------------------------*/
                log.info("{}", "Check cassette transfer status");
                String strCassettetransferStateGetout;
                strCassettetransferStateGetout = cassetteMethod.cassetteTransferStateGet(
                        strObjCommonIn,
                        strStartDurables.get(durableCnt).getDurableId());

                if (CimStringUtils.equals(strCassettetransferStateGetout, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    /*------------------------------------*/
                    /*   Get Cassette Info in Equipment   */
                    /*------------------------------------*/
                    log.info("{}", "Get Cassette Info in Equipment");
                    Outputs.ObjCassetteEquipmentIDGetOut strCassetteequipmentIDGetout;
                    strCassetteequipmentIDGetout = cassetteMethod.cassetteEquipmentIDGet(
                            strObjCommonIn,
                            strStartDurables.get(durableCnt).getDurableId());

                    Infos.EqpPortInfo strEquipmentportInfoGetout;
                    strEquipmentportInfoGetout = equipmentMethod.equipmentPortInfoGet(
                            strObjCommonIn,
                            strCassetteequipmentIDGetout.getEquipmentID());

                    /*----------------------------------------------------------*/
                    /*  Lock port object which has the specified cassette.      */
                    /*----------------------------------------------------------*/
                    int lenFromPort = CimArrayUtils.getSize(strEquipmentportInfoGetout.getEqpPortStatuses());
                    for (portCnt = 0; portCnt < lenFromPort; portCnt++) {
                        log.info("{} {}", "loop to strEquipmentportInfoGetout.strEqpPortInfo.strEqpPortStatus.length()", portCnt);
                        if (ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getDurableId(),
                                strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getLoadedCassetteID())) {
                            objectLockMethod.objectLockForEquipmentResource(
                                    strObjCommonIn,
                                    strCassetteequipmentIDGetout.getEquipmentID(),
                                    strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                                    BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                        }
                    }
                }
            }
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                        BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

            }
        }

        objectLockMethod.objectLock(
                strObjCommonIn,
                CimDurableControlJob.class,
                strInParm.getDurableControlJobID());

        List<ObjectIdentifier> durableIDs;
        durableIDs = new ArrayList<>(durableLen);
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableIDs.add(strStartDurables.get(durableCnt).getDurableId());
        }

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimCassette.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimReticlePod.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            log.info("{] {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimProcessDurable.class,
                    durableIDs);
        }

        String portGroupID;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Outputs.ObjEquipmentPortGroupIDGetOut strEquipmentportGroupIDGetout;
            Inputs.ObjEquipmentPortGroupIDGetIn strEquipmentportGroupIDGetin = new Inputs.ObjEquipmentPortGroupIDGetIn();
            strEquipmentportGroupIDGetin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentportGroupIDGetin.setPortId(strStartDurables.get(0).getStartDurablePort().getLoadPortID());
            strEquipmentportGroupIDGetout = portMethod.equipmentPortGroupIDGet(
                    strObjCommonIn,
                    strEquipmentportGroupIDGetin);
            portGroupID = strEquipmentportGroupIDGetout.getPortGroupId();
        } else {
            log.info("{}", "durableCategory != SP_DurableCat_Cassette");
            portGroupID = "";
        }

        Inputs.ObjDurableCheckConditionForOperationIn strDurableCheckConditionForOperationin = new Inputs.ObjDurableCheckConditionForOperationIn();
        strDurableCheckConditionForOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL);
        strDurableCheckConditionForOperationin.setEquipmentId(strInParm.getEquipmentID());
        strDurableCheckConditionForOperationin.setDurableCategory(durableCategory);
        strDurableCheckConditionForOperationin.setStartDurables(strStartDurables);
        strDurableCheckConditionForOperationin.setDurableStartRecipe(strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe());
        durableMethod.durableCheckConditionForOperation(
                strObjCommonIn,
                strDurableCheckConditionForOperationin);

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableMethod.durableStatusCheckForOperation(
                    strObjCommonIn,
                    BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL,
                    strStartDurables.get(durableCnt).getDurableId(),
                    durableCategory);
        }

        Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn strEquipmentandportStateCheckForDurableOperationin = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
        strEquipmentandportStateCheckForDurableOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL);
        strEquipmentandportStateCheckForDurableOperationin.setEquipmentId(strInParm.getEquipmentID());
        strEquipmentandportStateCheckForDurableOperationin.setPortGroupId(portGroupID);
        strEquipmentandportStateCheckForDurableOperationin.setDurableCategory(durableCategory);
        strEquipmentandportStateCheckForDurableOperationin.setStartDurables(strStartDurables);
        equipmentMethod.equipmentAndPortStateCheckForDurableOperationForInternalBuffer(
                strObjCommonIn,
                strEquipmentandportStateCheckForDurableOperationin);

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                int basePGNo = 0;

                Infos.EqpPortInfo strEquipmentportInfoGetout;
                strEquipmentportInfoGetout = equipmentMethod.equipmentPortInfoGet(
                        strObjCommonIn,
                        strInParm.getEquipmentID());

                int lenPortInfo = CimArrayUtils.getSize(strEquipmentportInfoGetout.getEqpPortStatuses());
                log.info("{} {}", "strEqpPortStatus.length", lenPortInfo);

                for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {

                    if (!ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                            strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID())) {
                        log.info("{}", "Not same portID, continue...");
                        continue;
                    }

                    if (CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)) {
                        log.info("{}", "dispatchState == Dispatched");

                        if (CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                                || CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                                || CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                            log.info("{}", "portState == LoadReq or UnloadReq or '-'");

                            /*------------------------*/
                            /*   change to Required   */
                            /*------------------------*/
                            ObjectIdentifier dummyOI = null;
                            equipmentMethod.equipmentDispatchStateChange(
                                    strObjCommonIn,
                                    strInParm.getEquipmentID(),
                                    strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                                    BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                                    dummyOI,
                                    dummyOI,
                                    dummyOI,
                                    dummyOI);

                        }

                        basePGNo = portCnt;
                        break;
                    }
                }

                /*--------------------------*/
                /*   find Same Port Group   */
                /*--------------------------*/
                for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {
                    log.info("{] {}", "loop to strEqpPortStatus", portCnt);

                    /*===== Omit Base Port =====*/
                    if (portCnt == basePGNo) {
                        log.info("{}", "portCnt == basePGNo, continue...");
                        continue;
                    }
                    /*===== Omit Different Group's Port =====*/
                    if (!CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortGroup(),
                            strEquipmentportInfoGetout.getEqpPortStatuses().get(basePGNo).getPortGroup())) {
                        log.info("{}", "Not same portGroup, continue...");
                        continue;
                    }

                    /*===== Check portState =====*/
                    if (!CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                            && !CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                            && !CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                        log.info("{}", "portState != LoadReq or UnloadReq or '-', continue...");
                        continue;
                    }

                    /*===== Check dispatchState =====*/
                    if (CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)
                            || CimStringUtils.equals(strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_NOTDISPATCHED)) {
                        log.info("{}", "Same PortGroup == Dispatched or NotDispatched");

                        /*------------------------*/
                        /*   change to Required   */
                        /*------------------------*/
                        ObjectIdentifier dummyOI = null;
                        equipmentMethod.equipmentDispatchStateChange(
                                strObjCommonIn,
                                strInParm.getEquipmentID(),
                                strEquipmentportInfoGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                                BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                                dummyOI,
                                dummyOI,
                                dummyOI,
                                dummyOI);

                    }
                }
            }
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut strEquipmentreticlePodPortInfoGetDRout;
                strEquipmentreticlePodPortInfoGetDRout = equipmentMethod.equipmentReticlePodPortInfoGetDR(
                        strObjCommonIn,
                        strInParm.getEquipmentID());

                int lenPortInfo = CimArrayUtils.getSize(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList());
                log.info("{} {}", "strReticlePodPortInfo.length", lenPortInfo);

                for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {
                    log.info("{} {}", "loop to strEquipmentreticlePodPortInfoGetDRout.strReticlePodPortInfo.length()", portCnt);
                    if (!ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                            strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID())) {
                        log.info("{}", "Not same portID, continue...");
                        continue;
                    }

                    if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)) {
                        log.info("{}", "dispatchStatus == Dispatched");

                        if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                                || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                                || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                            log.info("{}", "portStatus == LoadReq or UnloadReq or '-'");

                            equipmentForDurableMethod.machineReticlePodPortReserveCancel(
                                    strObjCommonIn,
                                    strInParm.getEquipmentID(),
                                    strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID());

                        }

                        break;
                    }
                }
            }
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                Results.ReticleDetailInfoInqResult strReticledetailInfoGetDRout;                                               //DSN000101569
                strReticledetailInfoGetDRout = reticleMethod.reticleDetailInfoGetDR(strObjCommonIn, strStartDurables.get(durableCnt).getDurableId(),
                        false,
                        false);
                Infos.ReticleStatusInfo reticleStatusInfo = strReticledetailInfoGetDRout.getReticleStatusInfo();

                if (0 != CimStringUtils.length(ObjectIdentifier.fetchValue(reticleStatusInfo.getReticlePodID()))) {
                    log.info("{}", "Reticle is in ReticlePod");
                    Outputs.ObjEquipmentReticlePodPortInfoGetDROut strEquipmentreticlePodPortInfoGetDRout;
                    strEquipmentreticlePodPortInfoGetDRout = equipmentMethod.equipmentReticlePodPortInfoGetDR(
                            strObjCommonIn,
                            strInParm.getEquipmentID());

                    int lenPortInfo = CimArrayUtils.getSize(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList());
                    log.info("{} {}", "strReticlePodPortInfo.length", lenPortInfo);

                    for (portCnt = 0; portCnt < lenPortInfo; portCnt++) {
                        log.info("{} {}", "loop to strEquipmentreticlePodPortInfoGetDRout.strReticlePodPortInfo.length()", portCnt);
                        if (!ObjectIdentifier.equalsWithValue(strStartDurables.get(durableCnt).getStartDurablePort().getLoadPortID(),
                                strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID())) {
                            log.info("{}", "Not same portID, continue...");
                            continue;
                        }

                        if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)) {
                            log.info("{}", "dispatchStatus == Dispatched");

                            if (CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                                    || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                                    || CimStringUtils.equals(strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                                log.info("{}", "portStatus == LoadReq or UnloadReq or '-'");

                                equipmentForDurableMethod.machineReticlePodPortReserveCancel(
                                        strObjCommonIn,
                                        strInParm.getEquipmentID(),
                                        strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList().get(portCnt).getReticlePodPortID());

                            }

                            break;
                        }
                    }
                }
            }
        }

        equipmentMethod.equipmentStartReserveCancelForDurableInternalBuffer(strObjCommonIn, strInParm.getEquipmentID(), strInParm.getDurableControlJobID());

        Infos.ProcessStartDurablesReserveInformationClearIn strProcessstartDurablesReserveInformationClearin = new Infos.ProcessStartDurablesReserveInformationClearIn();
        strProcessstartDurablesReserveInformationClearin.setDurableCategory(durableCategory);
        strProcessstartDurablesReserveInformationClearin.setStrStartDurables(strStartDurables);
        processForDurableMethod.processStartDurablesReserveInformationClear(
                strObjCommonIn,
                strProcessstartDurablesReserveInformationClearin);

        ObjectIdentifier strDurableControlJobManageReqResult;
        Params.DurableControlJobManageReqInParam strDurableControlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        strDurableControlJobManageReqInParam.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_DELETE);
        Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        strDurableControlJobCreateRequest.setEquipmentID(strInParm.getEquipmentID());
        strDurableControlJobCreateRequest.setDurableCategory(durableCategory);
        strDurableControlJobCreateRequest.setStrStartDurables(strStartDurables);
        strDurableControlJobManageReqInParam.setStrDurableControlJobCreateRequest(strDurableControlJobCreateRequest);
        strDurableControlJobManageReqResult = sxDrbCJStatusChangeReq(
                strObjCommonIn,
                strDurableControlJobManageReqInParam);

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                /*-----------------------------------------------*/
                /*   Change Cassette's Dispatch State to FALSE   */
                /*-----------------------------------------------*/
                cassetteMethod.cassetteDispatchStateChange(
                        strObjCommonIn,
                        strStartDurables.get(durableCnt).getDurableId(),
                        false);
            }
        }

        /*-----------------------------------------------------*/
        /*   Send StartDurablesReservationCancelReq() to TCS   */
        /*-----------------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        Long sleepTimeValue = 0L;
        Long retryCountValue = 0L;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        log.info("{} {}", "env value of OM_EAP_CONNECT_SLEEP_TIME  = ", sleepTimeValue);
        log.info("{} {}", "env value of OM_EAP_CONNECT_RETRY_COUNT = ", retryCountValue);

        Results.StartDurablesReservationCancelReqResult strTCSMgrSendStartDurablesReservationCancelReqout;

        //'retryCountValue + 1' means first try plus retry count
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            try {
                strTCSMgrSendStartDurablesReservationCancelReqout = tcsMethod.sendStartDurablesReservationCancelReq(
                        strObjCommonIn,
                        strInParm);
                log.info("{}", "Now TCS subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "TCS subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    try {
                        Thread.sleep(sleepTimeValue);
                        continue;
                    } catch (InterruptedException e) {
                        ex.addSuppressed(e);
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                } else {
                    log.info("{}", "TCSMgr_SendStartDurablesReservationCancelReq() != RC_OK");
                    throw ex;
                }
            }

        }

        // Set Return Structure
        strStartDurablesReservationCancelReqResult.setDurableCategory(durableCategory);
        strStartDurablesReservationCancelReqResult.setStrStartDurables(strStartDurables);

        // Return to caller
        log.info("PPTManager_i::txStartDurablesReservationCancelReq");
        return strStartDurablesReservationCancelReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strStartDurablesReservationReqInParam
     * @param claimMemo
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @throws
     * @author ho
     * @date 2020/7/2 10:57
     */
    @Override
    public ObjectIdentifier sxDrbMoveInReserveReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam, String claimMemo) {
        log.info("PPTManager_i::txStartDurablesReservationReq");
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------

        Params.StartDurablesReservationReqInParam strInParm = strStartDurablesReservationReqInParam;

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------

        String durableCategory = strInParm.getDurableCategory();
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", durableCategory);
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), durableCategory);
        }

        int durableLen = CimArrayUtils.getSize(strInParm.getStrStartDurables());
        if (0 >= durableLen) {
            log.info("{}", "0 > = CIMFWStrLen(strStartDurables.length())");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(
                strObjCommonIn,
                strInParm.getEquipmentID());

        // Get required equipment lock mode
        Outputs.ObjLockModeOut strObjectlockModeGetout;
        Inputs.ObjLockModeIn strObjectlockModeGetin = new Inputs.ObjLockModeIn();
        strObjectlockModeGetin.setObjectID(strInParm.getEquipmentID());
        strObjectlockModeGetin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        strObjectlockModeGetin.setFunctionCategory("ODRBW043"); // TxStartDurablesReservationReq
        strObjectlockModeGetin.setUserDataUpdateFlag(false);
        strObjectlockModeGetout = objectMethod.objectLockModeGet(
                strObjCommonIn,
                strObjectlockModeGetin);

        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();

        Long lockMode = strObjectlockModeGetout.getLockMode();
        log.info("{} {}", "lockMode", lockMode);
        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(strInParm.getEquipmentID());
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(strObjectlockModeGetout.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(
                    strObjCommonIn,
                    strAdvancedobjecLockin);
        } else {
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            objectLockMethod.objectLock(
                    strObjCommonIn,
                    CimMachine.class,
                    strInParm.getEquipmentID());
        }

        /*------------------------------*/
        /*   Lock Dispatcher Object     */
        /*------------------------------*/
        objectLockMethod.objectLock(
                strObjCommonIn,
                CimDispatcher.class,
                strInParm.getEquipmentID());

        int durableCnt = 0;
        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Infos.EqpPortInfo strPortResourceallPortsInSameGroupGetout;
            strPortResourceallPortsInSameGroupGetout = portMethod.portResourceAllPortsInSameGroupGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID(),
                    strInParm.getStrStartDurables().get(0).getStartDurablePort().getLoadPortID());

            /*---------------------------------------------------------*/
            /* Lock All Ports being in the same Port Group as ToPort   */
            /*---------------------------------------------------------*/
            int lenToPort = CimArrayUtils.getSize(strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses());
            for (int portCnt = 0; portCnt < lenToPort; portCnt++) {
                log.info("{} {}", "loop to strPortResourceallPortsInSameGroupGetout.strEqpPortInfo.strEqpPortStatus.length()", portCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strInParm.getStrStartDurables().get(durableCnt).getStartDurablePort().getLoadPortID(),
                        BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

            }
        }

        List<ObjectIdentifier> durableIDs;
        durableIDs = new ArrayList<>(durableLen);
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
            durableIDs.add(strInParm.getStrStartDurables().get(durableCnt).getDurableId());
        }

        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimCassette.class,
                    durableIDs);
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimReticlePod.class,
                    durableIDs);
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimProcessDurable.class,
                    durableIDs);
        }

        boolean bOnRouteFlag = false;
        boolean bOffRouteFlag = false;
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
            try {
                durableMethod.durableOnRouteCheck(strObjCommonIn, strInParm.getDurableCategory(),
                        strInParm.getStrStartDurables().get(durableCnt).getDurableId());
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getDurableOnroute())) {
                    log.info("{}", "##### durable is on  route");
                    bOnRouteFlag = true;
                } else {
                    log.info("{}", "##### durable is off route");
                    bOffRouteFlag = true;
                }
            }
        }
        if (bOnRouteFlag && bOffRouteFlag) {
            log.info("{}", "all durable OnRoute state is not same");
            Validations.check(true, retCodeConfigEx.getDurableOnRouteStatNotSame());
        }

        if (bOnRouteFlag && 0 == CimStringUtils.length(ObjectIdentifier.fetchValue(strInParm.getStrDurableStartRecipe().getLogicalRecipeId()))) {
            log.info("{}", "logicalRecipeID is blank");
            Validations.check(true, retCodeConfigEx.getDurableCannotOffrouteReserve());
        }

        String portGroupID;
        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            Outputs.ObjEquipmentPortGroupIDGetOut strEquipmentportGroupIDGetout;
            Inputs.ObjEquipmentPortGroupIDGetIn strEquipmentportGroupIDGetin = new Inputs.ObjEquipmentPortGroupIDGetIn();
            strEquipmentportGroupIDGetin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentportGroupIDGetin.setPortId(strInParm.getStrStartDurables().get(0).getStartDurablePort().getLoadPortID());
            strEquipmentportGroupIDGetout = portMethod.equipmentPortGroupIDGet(
                    strObjCommonIn,
                    strEquipmentportGroupIDGetin);
            portGroupID = strEquipmentportGroupIDGetout.getPortGroupId();
        } else {
            portGroupID = "";
        }

        Inputs.ObjDurableCheckConditionForOperationIn strDurableCheckConditionForOperationin = new Inputs.ObjDurableCheckConditionForOperationIn();
        strDurableCheckConditionForOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
        strDurableCheckConditionForOperationin.setEquipmentId(strInParm.getEquipmentID());
        strDurableCheckConditionForOperationin.setDurableCategory(strInParm.getDurableCategory());
        strDurableCheckConditionForOperationin.setStartDurables(strInParm.getStrStartDurables());
        strDurableCheckConditionForOperationin.setDurableStartRecipe(strInParm.getStrDurableStartRecipe());
        durableMethod.durableCheckConditionForOperation(
                strObjCommonIn,
                strDurableCheckConditionForOperationin);

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            durableMethod.durableStatusCheckForOperation(
                    strObjCommonIn,
                    BizConstant.SP_OPERATION_STARTRESERVATION,
                    strInParm.getStrStartDurables().get(durableCnt).getDurableId(),
                    strInParm.getDurableCategory());
        }

        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)
                || CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette || SP_DurableCat_ReticlePod");
            Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn strEquipmentandportStateCheckForDurableOperationin = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
            strEquipmentandportStateCheckForDurableOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
            strEquipmentandportStateCheckForDurableOperationin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentandportStateCheckForDurableOperationin.setPortGroupId(portGroupID);
            strEquipmentandportStateCheckForDurableOperationin.setDurableCategory(strInParm.getDurableCategory());
            strEquipmentandportStateCheckForDurableOperationin.setStartDurables(strInParm.getStrStartDurables());
            equipmentMethod.equipmentAndPortStateCheckForDurableOperation(
                    strObjCommonIn,
                    strEquipmentandportStateCheckForDurableOperationin);
        }

        List<Infos.RecipeBodyManagement> strMachineRecipeGetListForRecipeBodyManagementForDurableout;
        Infos.MachineRecipeGetListForRecipeBodyManagementForDurableIn strMachineRecipeGetListForRecipeBodyManagementForDurablein = new Infos.MachineRecipeGetListForRecipeBodyManagementForDurableIn();
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setEquipmentID(strInParm.getEquipmentID());
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setDurableCategory(strInParm.getDurableCategory());
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setStrStartDurables(strInParm.getStrStartDurables());
        List<Infos.DurableStartRecipe> strDurableStartRecipes;
        strDurableStartRecipes = new ArrayList<>(1);
        strDurableStartRecipes.add(strInParm.getStrDurableStartRecipe());
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setStrDurableStartRecipes(strDurableStartRecipes);
        strMachineRecipeGetListForRecipeBodyManagementForDurableout = machineRecipeMethod.machineRecipeGetListForRecipeBodyManagementForDurable(
                strObjCommonIn,
                strMachineRecipeGetListForRecipeBodyManagementForDurablein);

        int targetRecipeLen = CimArrayUtils.getSize(strMachineRecipeGetListForRecipeBodyManagementForDurableout);
        for (int targetRecipeCnt = 0; targetRecipeCnt < targetRecipeLen; targetRecipeCnt++) {
            log.info("{} {}", "loop to strMachineRecipeGetListForRecipeBodyManagementForDurableout.strRecipeBodyManagementSeq.length()", targetRecipeCnt);
            log.info("{} {}", " Machine Recipe ID          ", ObjectIdentifier.fetchValue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId()));
            log.info("{} {}", " Force Down Load Flag       ", (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getForceDownLoadFlag()) ? "True" : "False"));
            log.info("{} {}", " Recipe Body Confirm Flag   ", (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getRecipeBodyConfirmFlag()) ? "True" : "False"));
            log.info("{} {}", " Conditional Down Load Flag ", (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getConditionalDownLoadFlag()) ? "True" : "False"));

            //-------------------
            // Force Down Load
            //-------------------
            boolean downLoadFlag = false;
            if (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getForceDownLoadFlag())) {
                log.info("", "downLoadFlag turns to True.");
                downLoadFlag = true;
            } else {
                if (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getRecipeBodyConfirmFlag())) {
                    log.info("{}", "recipeBodyConfirmFlag == TRUE");
                    //---------------------
                    // Recipe Confirmation
                    //---------------------
                    log.info("{}", "Call txRecipeConfirmationReq");
                    Params.RecipeCompareReqParams recipeCompareReqParams = new Params.RecipeCompareReqParams();
                    recipeCompareReqParams.setEquipmentID(strInParm.getEquipmentID());
                    recipeCompareReqParams.setMachineRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId());
                    recipeCompareReqParams.setPhysicalRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getPhysicalRecipeId());
                    recipeCompareReqParams.setFileLocation(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileLocation());
                    recipeCompareReqParams.setFileName(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileName());
                    recipeCompareReqParams.setFormatFlag(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFormatFlag());
                    recipeCompareReqParams.setClaimMemo("");

                    try {
                        recipeService.sxRecipeCompareReq(
                                strObjCommonIn,
                                recipeCompareReqParams);
                    } catch (ServiceException ex) {
                        if (!Validations.isEquals(ex.getCode(), retCodeConfigEx.getTcsMMTapPPConfirmError())) {
                            throw ex;
                        }


                        log.info("{}", "rc == RC_TCS_MM_TAP_PP_CONFIRM_ERROR");
                        if (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getConditionalDownLoadFlag())) {
                            //--------------------------
                            // Conditional Down Load
                            //--------------------------
                            log.info("{}", "downLoadFlag turns to True.");
                            downLoadFlag = true;
                        } else {
                            //Recipe Body Confirmation error. the Recipe Body differs between Uploaded it to system and the owned it by equipment.
                            Validations.check(true, retCodeConfigEx.getRecipeConfirmError(),
                                    ObjectIdentifier.fetchValue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId()));
                        }
                    }
                } else {
                    log.info("{}", "Recipe Body management .. no action.");
                }
            }

            log.info("{} {}", "Recipe Down Load ??", (downLoadFlag ? "True" : "False"));
            if (downLoadFlag) {
                //---------------------
                // Recipe Down Load
                //---------------------
                log.info("{}", "Call txRecipeDownloadReq");
                Params.RecipeDownloadReqParams recipeDownloadReqParams = new Params.RecipeDownloadReqParams();
                recipeDownloadReqParams.setEquipmentID(strInParm.getEquipmentID());
                recipeDownloadReqParams.setMachineRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId());
                recipeDownloadReqParams.setPhysicalRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getPhysicalRecipeId());
                recipeDownloadReqParams.setFileLocation(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileLocation());
                recipeDownloadReqParams.setFileName(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileName());
                recipeDownloadReqParams.setFormatFlag(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFormatFlag());
                recipeDownloadReqParams.setClaimMemo("");

                recipeService.sxRecipeDownloadReq(strObjCommonIn, recipeDownloadReqParams);
            }
        }

        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                durableMethod.durableCassetteCategoryCheckForContaminationControl(
                        strObjCommonIn,
                        strInParm.getStrStartDurables().get(durableCnt).getDurableId(),
                        strInParm.getEquipmentID(),
                        strInParm.getStrStartDurables().get(durableCnt).getStartDurablePort().getLoadPortID());

                cassetteMethod.cassetteDispatchStateChange(
                        strObjCommonIn,
                        strInParm.getStrStartDurables().get(durableCnt).getDurableId(),
                        true);
            }
        }

        ObjectIdentifier strDurableControlJobManageReqResult;
        Params.DurableControlJobManageReqInParam strDurableControlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        ObjectIdentifier dummyDurableControlJobID;
        dummyDurableControlJobID = ObjectIdentifier.buildWithValue("");
        strDurableControlJobManageReqInParam.setDurableControlJobID(dummyDurableControlJobID);
        strDurableControlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_CREATE);
        Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        strDurableControlJobCreateRequest.setEquipmentID(strInParm.getEquipmentID());
        strDurableControlJobCreateRequest.setDurableCategory(strInParm.getDurableCategory());
        strDurableControlJobCreateRequest.setStrStartDurables(strInParm.getStrStartDurables());
        strDurableControlJobManageReqInParam.setStrDurableControlJobCreateRequest(strDurableControlJobCreateRequest);
        strDurableControlJobManageReqInParam.setClaimMemo(claimMemo);
        strDurableControlJobManageReqResult = sxDrbCJStatusChangeReq(
                strObjCommonIn,
                strDurableControlJobManageReqInParam);

        Inputs.ProcessStartDurablesReserveInformationSetIn strProcessstartDurablesReserveInformationSetin = new Inputs.ProcessStartDurablesReserveInformationSetIn();
        strProcessstartDurablesReserveInformationSetin.setEquipmentID(strInParm.getEquipmentID());
        strProcessstartDurablesReserveInformationSetin.setPortGroupID(portGroupID);
        strProcessstartDurablesReserveInformationSetin.setDurableControlJobID(strDurableControlJobManageReqResult);
        strProcessstartDurablesReserveInformationSetin.setDurableCategory(strInParm.getDurableCategory());
        strProcessstartDurablesReserveInformationSetin.setStrStartDurables(strInParm.getStrStartDurables());
        strProcessstartDurablesReserveInformationSetin.setStrDurableStartRecipe(strInParm.getStrDurableStartRecipe());
        processForDurableMethod.processStartDurablesReserveInformationSet(
                strObjCommonIn,
                strProcessstartDurablesReserveInformationSetin);

        /*--------------------------------------------*/
        /*   Send Start Durables Reservation to TCS   */
        /*--------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue = 0;
        long retryCountValue = 0;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        log.info("{} {}", "env value of OM_EAP_CONNECT_SLEEP_TIME  = ", sleepTimeValue);
        log.info("{} {}", "env value of OM_EAP_CONNECT_RETRY_COUNT = ", retryCountValue);

        ObjectIdentifier strTCSMgrSendStartDurablesReservationReqout;
        Infos.SendStartDurablesReservationReqIn strTCSMgrSendStartDurablesReservationReqin = new Infos.SendStartDurablesReservationReqIn();
        strTCSMgrSendStartDurablesReservationReqin.setStrStartDurablesReservationReqInParam(strInParm);
        strTCSMgrSendStartDurablesReservationReqin.setClaimMemo(claimMemo);

        //'retryCountValue + 1' means first try plus retry count
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            try {
                strTCSMgrSendStartDurablesReservationReqout = tcsMethod.sendStartDurablesReservationReq(
                        strObjCommonIn,
                        strTCSMgrSendStartDurablesReservationReqin);
                log.info("{}", "Now TCS subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "TCS subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    try {
                        Thread.sleep(sleepTimeValue*1000);
                        continue;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ServiceException(e.getMessage());
                    }
                } else {
                    throw ex;
                }
            }
        }

        // Return to caller
        return strDurableControlJobManageReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strStartDurablesReservationReqInParam
     * @param claimMemo
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @throws
     * @author ho
     * @date 2020/7/2 10:57
     */
    @Override
    public ObjectIdentifier sxDrbMoveInReserveForIBReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam, String claimMemo) {
        log.info("PPTManager_i::txStartDurablesReservationReq");
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------

        Params.StartDurablesReservationReqInParam strInParm = strStartDurablesReservationReqInParam;

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------

        String durableCategory = strInParm.getDurableCategory();
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", durableCategory);
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), durableCategory);
        }

        int durableLen = CimArrayUtils.getSize(strInParm.getStrStartDurables());
        if (0 >= durableLen) {
            log.info("{}", "0 > = CIMFWStrLen(strStartDurables.length())");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(
                strObjCommonIn,
                strInParm.getEquipmentID());

        // Get required equipment lock mode
        Outputs.ObjLockModeOut strObjectlockModeGetout;
        Inputs.ObjLockModeIn strObjectlockModeGetin = new Inputs.ObjLockModeIn();
        strObjectlockModeGetin.setObjectID(strInParm.getEquipmentID());
        strObjectlockModeGetin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        strObjectlockModeGetin.setFunctionCategory("ODRBW043"); // TxStartDurablesReservationReq
        strObjectlockModeGetin.setUserDataUpdateFlag(false);
        strObjectlockModeGetout = objectMethod.objectLockModeGet(
                strObjCommonIn,
                strObjectlockModeGetin);

        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();

        Long lockMode = strObjectlockModeGetout.getLockMode();
        log.info("{} {}", "lockMode", lockMode);
        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(strInParm.getEquipmentID());
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(strObjectlockModeGetout.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(
                    strObjCommonIn,
                    strAdvancedobjecLockin);
        } else {
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            objectLockMethod.objectLock(
                    strObjCommonIn,
                    CimMachine.class,
                    strInParm.getEquipmentID());
        }

        /*------------------------------*/
        /*   Lock Dispatcher Object     */
        /*------------------------------*/
        objectLockMethod.objectLock(
                strObjCommonIn,
                CimDispatcher.class,
                strInParm.getEquipmentID());

        int durableCnt = 0;
        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Infos.EqpPortInfo strPortResourceallPortsInSameGroupGetout;
            strPortResourceallPortsInSameGroupGetout = portMethod.portResourceAllPortsInSameGroupGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID(),
                    strInParm.getStrStartDurables().get(0).getStartDurablePort().getLoadPortID());

            /*---------------------------------------------------------*/
            /* Lock All Ports being in the same Port Group as ToPort   */
            /*---------------------------------------------------------*/
            int lenToPort = CimArrayUtils.getSize(strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses());
            for (int portCnt = 0; portCnt < lenToPort; portCnt++) {
                log.info("{} {}", "loop to strPortResourceallPortsInSameGroupGetout.strEqpPortInfo.strEqpPortStatus.length()", portCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strPortResourceallPortsInSameGroupGetout.getEqpPortStatuses().get(portCnt).getPortID(),
                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
                objectLockMethod.objectLockForEquipmentResource(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strInParm.getStrStartDurables().get(durableCnt).getStartDurablePort().getLoadPortID(),
                        BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

            }
        }

        List<ObjectIdentifier> durableIDs;
        durableIDs = new ArrayList<>(durableLen);
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
            durableIDs.add(strInParm.getStrStartDurables().get(durableCnt).getDurableId());
        }

        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimCassette.class,
                    durableIDs);
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimReticlePod.class,
                    durableIDs);
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimProcessDurable.class,
                    durableIDs);
        }

        boolean bOnRouteFlag = false;
        boolean bOffRouteFlag = false;
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strInParm.strStartDurables.length()", durableCnt);
            try {
                durableMethod.durableOnRouteCheck(strObjCommonIn, strInParm.getDurableCategory(),
                        strInParm.getStrStartDurables().get(durableCnt).getDurableId());
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getDurableOnroute())) {
                    log.info("{}", "##### durable is on  route");
                    bOnRouteFlag = true;
                } else {
                    log.info("{}", "##### durable is off route");
                    bOffRouteFlag = true;
                }
            }
        }
        if (bOnRouteFlag && bOffRouteFlag) {
            log.info("{}", "all durable OnRoute state is not same");
            Validations.check(true, retCodeConfigEx.getDurableOnRouteStatNotSame());
        }

        if (bOnRouteFlag && 0 == CimStringUtils.length(ObjectIdentifier.fetchValue(strInParm.getStrDurableStartRecipe().getLogicalRecipeId()))) {
            log.info("{}", "logicalRecipeID is blank");
            Validations.check(true, retCodeConfigEx.getDurableCannotOffrouteReserve());
        }

        String portGroupID;
        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            Outputs.ObjEquipmentPortGroupIDGetOut strEquipmentportGroupIDGetout;
            Inputs.ObjEquipmentPortGroupIDGetIn strEquipmentportGroupIDGetin = new Inputs.ObjEquipmentPortGroupIDGetIn();
            strEquipmentportGroupIDGetin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentportGroupIDGetin.setPortId(strInParm.getStrStartDurables().get(0).getStartDurablePort().getLoadPortID());
            strEquipmentportGroupIDGetout = portMethod.equipmentPortGroupIDGet(
                    strObjCommonIn,
                    strEquipmentportGroupIDGetin);
            portGroupID = strEquipmentportGroupIDGetout.getPortGroupId();
        } else {
            portGroupID = "";
        }

        Inputs.ObjDurableCheckConditionForOperationIn strDurableCheckConditionForOperationin = new Inputs.ObjDurableCheckConditionForOperationIn();
        strDurableCheckConditionForOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
        strDurableCheckConditionForOperationin.setEquipmentId(strInParm.getEquipmentID());
        strDurableCheckConditionForOperationin.setDurableCategory(strInParm.getDurableCategory());
        strDurableCheckConditionForOperationin.setStartDurables(strInParm.getStrStartDurables());
        strDurableCheckConditionForOperationin.setDurableStartRecipe(strInParm.getStrDurableStartRecipe());
        durableMethod.durableCheckConditionForOperationForInternalBuffer(
                strObjCommonIn,
                strDurableCheckConditionForOperationin);

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            durableMethod.durableStatusCheckForOperation(
                    strObjCommonIn,
                    BizConstant.SP_OPERATION_STARTRESERVATION,
                    strInParm.getStrStartDurables().get(durableCnt).getDurableId(),
                    strInParm.getDurableCategory());
        }

        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)
                || CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette || SP_DurableCat_ReticlePod");
            Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn strEquipmentandportStateCheckForDurableOperationin = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
            strEquipmentandportStateCheckForDurableOperationin.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
            strEquipmentandportStateCheckForDurableOperationin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentandportStateCheckForDurableOperationin.setPortGroupId(portGroupID);
            strEquipmentandportStateCheckForDurableOperationin.setDurableCategory(strInParm.getDurableCategory());
            strEquipmentandportStateCheckForDurableOperationin.setStartDurables(strInParm.getStrStartDurables());
            equipmentMethod.equipmentAndPortStateCheckForDurableOperationForInternalBuffer(
                    strObjCommonIn,
                    strEquipmentandportStateCheckForDurableOperationin);
        }

        List<Infos.RecipeBodyManagement> strMachineRecipeGetListForRecipeBodyManagementForDurableout;
        Infos.MachineRecipeGetListForRecipeBodyManagementForDurableIn strMachineRecipeGetListForRecipeBodyManagementForDurablein = new Infos.MachineRecipeGetListForRecipeBodyManagementForDurableIn();
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setEquipmentID(strInParm.getEquipmentID());
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setDurableCategory(strInParm.getDurableCategory());
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setStrStartDurables(strInParm.getStrStartDurables());
        List<Infos.DurableStartRecipe> strDurableStartRecipes;
        strDurableStartRecipes = new ArrayList<>(1);
        strDurableStartRecipes.add(strInParm.getStrDurableStartRecipe());
        strMachineRecipeGetListForRecipeBodyManagementForDurablein.setStrDurableStartRecipes(strDurableStartRecipes);
        strMachineRecipeGetListForRecipeBodyManagementForDurableout = machineRecipeMethod.machineRecipeGetListForRecipeBodyManagementForDurable(
                strObjCommonIn,
                strMachineRecipeGetListForRecipeBodyManagementForDurablein);

        int targetRecipeLen = CimArrayUtils.getSize(strMachineRecipeGetListForRecipeBodyManagementForDurableout);
        for (int targetRecipeCnt = 0; targetRecipeCnt < targetRecipeLen; targetRecipeCnt++) {
            log.info("{} {}", "loop to strMachineRecipeGetListForRecipeBodyManagementForDurableout.strRecipeBodyManagementSeq.length()", targetRecipeCnt);
            log.info("{} {}", " Machine Recipe ID          ", ObjectIdentifier.fetchValue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId()));
            log.info("{} {}", " Force Down Load Flag       ", (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getForceDownLoadFlag()) ? "True" : "False"));
            log.info("{} {}", " Recipe Body Confirm Flag   ", (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getRecipeBodyConfirmFlag()) ? "True" : "False"));
            log.info("{} {}", " Conditional Down Load Flag ", (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getConditionalDownLoadFlag()) ? "True" : "False"));

            //-------------------
            // Force Down Load
            //-------------------
            boolean downLoadFlag = false;
            if (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getForceDownLoadFlag())) {
                log.info("", "downLoadFlag turns to True.");
                downLoadFlag = true;
            } else {
                if (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getRecipeBodyConfirmFlag())) {
                    log.info("{}", "recipeBodyConfirmFlag == TRUE");
                    //---------------------
                    // Recipe Confirmation
                    //---------------------
                    log.info("{}", "Call txRecipeConfirmationReq");
                    Params.RecipeCompareReqParams recipeCompareReqParams = new Params.RecipeCompareReqParams();
                    recipeCompareReqParams.setEquipmentID(strInParm.getEquipmentID());
                    recipeCompareReqParams.setMachineRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId());
                    recipeCompareReqParams.setPhysicalRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getPhysicalRecipeId());
                    recipeCompareReqParams.setFileLocation(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileLocation());
                    recipeCompareReqParams.setFileName(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileName());
                    recipeCompareReqParams.setFormatFlag(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFormatFlag());
                    recipeCompareReqParams.setClaimMemo("");

                    try {
                        recipeService.sxRecipeCompareReq(strObjCommonIn, recipeCompareReqParams);
                    } catch (ServiceException ex) {
                        if (!Validations.isEquals(ex.getCode(), retCodeConfigEx.getTcsMMTapPPConfirmError())) {
                            throw ex;
                        }


                        log.info("{}", "rc == RC_TCS_MM_TAP_PP_CONFIRM_ERROR");
                        if (CimBooleanUtils.isTrue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getConditionalDownLoadFlag())) {
                            //--------------------------
                            // Conditional Down Load
                            //--------------------------
                            log.info("{}", "downLoadFlag turns to True.");
                            downLoadFlag = true;
                        } else {
                            //Recipe Body Confirmation error. the Recipe Body differs between Uploaded it to system and the owned it by equipment.
                            Validations.check(true, retCodeConfigEx.getRecipeConfirmError(),
                                    ObjectIdentifier.fetchValue(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId()));
                        }
                    }
                } else {
                    log.info("{}", "Recipe Body management .. no action.");
                }
            }

            log.info("{} {}", "Recipe Down Load ??", (downLoadFlag ? "True" : "False"));
            if (downLoadFlag) {
                //---------------------
                // Recipe Down Load
                //---------------------
                log.info("{}", "Call txRecipeDownloadReq");
                Params.RecipeDownloadReqParams recipeDownloadReqParams = new Params.RecipeDownloadReqParams();
                recipeDownloadReqParams.setEquipmentID(strInParm.getEquipmentID());
                recipeDownloadReqParams.setMachineRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getMachineRecipeId());
                recipeDownloadReqParams.setPhysicalRecipeID(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getPhysicalRecipeId());
                recipeDownloadReqParams.setFileLocation(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileLocation());
                recipeDownloadReqParams.setFileName(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFileName());
                recipeDownloadReqParams.setFormatFlag(strMachineRecipeGetListForRecipeBodyManagementForDurableout.get(targetRecipeCnt).getFormatFlag());
                recipeDownloadReqParams.setClaimMemo("");

                recipeService.sxRecipeDownloadReq(
                        strObjCommonIn,
                        recipeDownloadReqParams);
            }
        }

        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                durableMethod.durableCassetteCategoryCheckForContaminationControl(
                        strObjCommonIn,
                        strInParm.getStrStartDurables().get(durableCnt).getDurableId(),
                        strInParm.getEquipmentID(),
                        strInParm.getStrStartDurables().get(durableCnt).getStartDurablePort().getLoadPortID());

                cassetteMethod.cassetteDispatchStateChange(
                        strObjCommonIn,
                        strInParm.getStrStartDurables().get(durableCnt).getDurableId(),
                        true);
            }
        }

        ObjectIdentifier strDurableControlJobManageReqResult;
        Params.DurableControlJobManageReqInParam strDurableControlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        ObjectIdentifier dummyDurableControlJobID;
        dummyDurableControlJobID = ObjectIdentifier.buildWithValue("");
        strDurableControlJobManageReqInParam.setDurableControlJobID(dummyDurableControlJobID);
        strDurableControlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_CREATE);
        Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        strDurableControlJobCreateRequest.setEquipmentID(strInParm.getEquipmentID());
        strDurableControlJobCreateRequest.setDurableCategory(strInParm.getDurableCategory());
        strDurableControlJobCreateRequest.setStrStartDurables(strInParm.getStrStartDurables());
        strDurableControlJobManageReqInParam.setStrDurableControlJobCreateRequest(strDurableControlJobCreateRequest);
        strDurableControlJobManageReqInParam.setClaimMemo(claimMemo);
        strDurableControlJobManageReqResult = sxDrbCJStatusChangeReq(
                strObjCommonIn,
                strDurableControlJobManageReqInParam);

        Inputs.ProcessStartDurablesReserveInformationSetIn strProcessstartDurablesReserveInformationSetin = new Inputs.ProcessStartDurablesReserveInformationSetIn();
        strProcessstartDurablesReserveInformationSetin.setEquipmentID(strInParm.getEquipmentID());
        strProcessstartDurablesReserveInformationSetin.setPortGroupID(portGroupID);
        strProcessstartDurablesReserveInformationSetin.setDurableControlJobID(strDurableControlJobManageReqResult);
        strProcessstartDurablesReserveInformationSetin.setDurableCategory(strInParm.getDurableCategory());
        strProcessstartDurablesReserveInformationSetin.setStrStartDurables(strInParm.getStrStartDurables());
        strProcessstartDurablesReserveInformationSetin.setStrDurableStartRecipe(strInParm.getStrDurableStartRecipe());
        processForDurableMethod.processStartDurablesReserveInformationSet(
                strObjCommonIn,
                strProcessstartDurablesReserveInformationSetin);

        for (Infos.StartDurable startDurable : strInParm.getStrStartDurables()) {

            //step38 - equipment_allocatedMaterial_Add
            equipmentMethod.equipmentAllocatedMaterialAddForDrbIB(strObjCommonIn, strInParm.getEquipmentID(), startDurable.getDurableId(), startDurable.getStartDurablePort().getLoadPortID(),
                    startDurable.getStartDurablePort().getLoadPurposeType(), strDurableControlJobManageReqResult);
        }

        /*--------------------------------------------*/
        /*   Send Start Durables Reservation to TCS   */
        /*--------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue = 0;
        long retryCountValue = 0;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        log.info("{} {}", "env value of OM_EAP_CONNECT_SLEEP_TIME  = ", sleepTimeValue);
        log.info("{} {}", "env value of OM_EAP_CONNECT_RETRY_COUNT = ", retryCountValue);

        ObjectIdentifier strTCSMgrSendStartDurablesReservationReqout;
        Infos.SendStartDurablesReservationReqIn strTCSMgrSendStartDurablesReservationReqin = new Infos.SendStartDurablesReservationReqIn();
        strTCSMgrSendStartDurablesReservationReqin.setStrStartDurablesReservationReqInParam(strInParm);
        strTCSMgrSendStartDurablesReservationReqin.setClaimMemo(claimMemo);

        //'retryCountValue + 1' means first try plus retry count
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            try {
                strTCSMgrSendStartDurablesReservationReqout = tcsMethod.sendStartDurablesReservationReq(
                        strObjCommonIn,
                        strTCSMgrSendStartDurablesReservationReqin);
                log.info("{}", "Now TCS subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "TCS subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    try {
                        Thread.sleep(sleepTimeValue*1000);
                        continue;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ServiceException(e.getMessage());
                    }
                } else {
                    throw ex;
                }
            }
        }

        // Return to caller
        return strDurableControlJobManageReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableBankInByPostProcReqInParam
     * @param claimMemo
     * @return boolean
     * @throws
     * @author ho
     * @date 2020/7/23 15:58
     */
    @Override
    public boolean sxDrbBankInByPostTaskReq(Infos.ObjCommon strObjCommonIn, Params.DurableBankInByPostProcReqInParam strDurableBankInByPostProcReqInParam, String claimMemo) {
        log.info("PPTManager_i::txDurableBankInByPostProcReq");

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Params.DurableBankInByPostProcReqInParam strInParm = strDurableBankInByPostProcReqInParam;

        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", strInParm.getDurableCategory());
            Validations.check(true,
                    retCodeConfig.getInvalidDurableCategory(),
                    strInParm.getDurableCategory());
        }

        //--------------------------------------------------
        // Trace and check input parameter
        //--------------------------------------------------
        if (CimStringUtils.length(ObjectIdentifier.fetchValue(strInParm.getDurableID())) > 0) {
            log.info("{} {}", "The input parameter durableID : ", ObjectIdentifier.fetchValue(strInParm.getDurableID()));
        } else {
            log.info("{}", "The input parameter is not specified.");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        /*---------------------------------------------------------------*/
        /*   Auto-Bank-In Procedure                                      */
        /*---------------------------------------------------------------*/
        boolean strDurableBankInByPostProcReqResult = false;

        //---------------------------------------
        // Check Durable Hold State
        //---------------------------------------
        String strDurableholdStateGetout;
        strDurableholdStateGetout = durableMethod.durableHoldStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (!CimStringUtils.equals(strDurableholdStateGetout, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
            log.info("{}", "durableHoldState != SP_Durable_HoldState_OnHold");

            // Check Condition
            boolean strDurableCheckConditionForAutoBankInout;
            Infos.DurableCheckConditionForAutoBankInIn strDurableCheckConditionForAutoBankInin = new Infos.DurableCheckConditionForAutoBankInIn();
            strDurableCheckConditionForAutoBankInin.setDurableCategory(strInParm.getDurableCategory());
            strDurableCheckConditionForAutoBankInin.setDurableID(strInParm.getDurableID());

            strDurableCheckConditionForAutoBankInout = durableMethod.durableCheckConditionForAutoBankIn(
                    strObjCommonIn,
                    strDurableCheckConditionForAutoBankInin);
            strDurableBankInByPostProcReqResult = strDurableCheckConditionForAutoBankInout;

            if (strDurableBankInByPostProcReqResult) {
                log.info("{}", "autoBankInFlag == TRUE");

                /*---------------------------------------------*/
                /*  Call txDurableBankInReq() for Auto-Bank-In */
                /*---------------------------------------------*/
                Results.DurableBankInReqResult strDurableBankInReqResult = new Results.DurableBankInReqResult();
                strDurableBankInReqResult.setStrBankInDurableResults(new ArrayList<>(1));
                Params.DurableBankInReqInParam strDurableBankInReqInParam = new Params.DurableBankInReqInParam();
                strDurableBankInReqInParam.setDurableCategory(strInParm.getDurableCategory());
                strDurableBankInReqInParam.setDurableIDs(new ArrayList<>(1));
                strDurableBankInReqInParam.getDurableIDs().add(strInParm.getDurableID());
                strDurableBankInReqInParam.setBankID(ObjectIdentifier.buildWithValue(""));

                strDurableBankInReqResult = durableService.sxDrbBankInReq(strDurableBankInReqResult, strObjCommonIn, 0, strDurableBankInReqInParam, claimMemo);
            }
        }

        //---------------------------------------
        // Return
        //---------------------------------------
        log.info("PPTManager_i::txDurableBankInByPostProcReq");
        return strDurableBankInByPostProcReqResult;
    }

    @Override
    public void sxDrbBankInCancelReq(Infos.ObjCommon objCommon, Params.DurableBankInCancelReqInParam durableBankInCancelReqInParam, String claimMemo) {
        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        String durableCategory = durableBankInCancelReqInParam.getDurableCategory();
        ObjectIdentifier durableID = durableBankInCancelReqInParam.getDurableID();
        if (!CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)
                && !CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)
                && !CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
            Validations.check(new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }
        //---------------------------------------
        // Object Lock
        //---------------------------------------
        if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)) {
            objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)) {
            objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
        }
        //---------------------------------------
        // Call durable_status_CheckForOperation
        //---------------------------------------
        durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATIONCATEGORY_BANKIN, durableID, durableCategory);
        //---------------------------------------
        // Check Durable OnRoute
        //---------------------------------------
        try {
            durableMethod.durableOnRouteCheck(objCommon, durableCategory, durableID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                log.info("durable_OnRoute_Check() == RC_DURABLE_ONROUTE");
                Infos.DurableSubStateGetOut durableSubState = durableMethod.durableSubStateGet(objCommon, durableCategory, durableID);
                if (ObjectIdentifier.isEmpty(durableSubState.getDurableSubStatus())
                        && CimStringUtils.equals(durableSubState.getDurableStatus(), BizConstant.CIMFW_DURABLE_AVAILABLE)
                        || CimStringUtils.equals(durableSubState.getDurableStatus(), BizConstant.CIMFW_DURABLE_INUSE)) {
                    throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidDurableStatBankincancel(), durableID.getValue()));
                }
                //---------------------------------------
                // Check Durable Hold State
                //---------------------------------------
                String durableHoldStateGetOut = durableMethod.durableHoldStateGet(objCommon, durableCategory, durableID);
                if (!CimStringUtils.equals(durableHoldStateGetOut, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidDurableHoldStat(), durableID.getValue(), durableHoldStateGetOut));
                }
                //---------------------------------------
                // Check Durable Process State
                //---------------------------------------
                String durableProcessStateGetOut = durableMethod.durableProcessStateGet(objCommon, durableCategory, durableID);
                if (!CimStringUtils.equals(durableProcessStateGetOut, BizConstant.SP_DURABLE_PROCSTATE_PROCESSED)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidDurableProcStat(), durableID.getValue(), durableProcessStateGetOut));
                }
            }
        }
        //---------------------------------------
        // Check Durable Inventory State
        //---------------------------------------
        String durableinventoryState = durableMethod.durableInventoryStateGet(objCommon, durableCategory, durableID);
        if (!CimStringUtils.equals(durableinventoryState, BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidDurableInventoryStat(), durableID.getValue(), durableinventoryState));
        }
        //-----------------------------------------------------------
        // Check cassette interFabXferState
        //-----------------------------------------------------------
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            String cassetteInterFabXferStateGetOut = cassetteMethod.cassetteInterFabXferStateGet(objCommon, durableID);
            if (CimStringUtils.equals(cassetteInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), durableID.getValue(), cassetteInterFabXferStateGetOut));
            }
        }
        //-----------------------------------------------------------
        // Call Durable BankInCancel
        //-----------------------------------------------------------
        bankForDurableMethod.durableBankInCancel(objCommon, durableCategory, durableID);
        //-----------------------------------------------------------
        // Create Durable Bank Move Event
        //-----------------------------------------------------------
        Infos.DurablebankmoveeventMakeIn strDurableBankMoveEvent_Make_in = new Infos.DurablebankmoveeventMakeIn();
        strDurableBankMoveEvent_Make_in.setClaimMemo(claimMemo);
        strDurableBankMoveEvent_Make_in.setTransactionID(TransactionIDEnum.DURABLE_BANK_IN_CANCEL_REQ.getValue());
        strDurableBankMoveEvent_Make_in.setDurableCategory(durableCategory);
        strDurableBankMoveEvent_Make_in.setDurableID(durableID);
        eventMethod.durableBankMoveEventMake(objCommon, strDurableBankMoveEvent_Make_in);
    }

    @Override
    public Results.DurableBankMoveReqResult sxDrbBankMoveReq(Results.DurableBankMoveReqResult durableBankMoveReqResult, Infos.ObjCommon objCommon, int seqIndex, Params.DurableBankMoveReqParam strInParm, String claimMemo) {
        List<Results.BankMoveDurableResult> strBankMoveDurableResults = durableBankMoveReqResult.getStrBankMoveDurableResults();
        String durableCategory = strInParm.getDurableCategory();
        List<ObjectIdentifier> durableIDs = strInParm.getDurableIDs();
        ObjectIdentifier durableID = durableIDs.get(seqIndex);
        ObjectIdentifier toBankID = strInParm.getToBankID();
        Results.BankMoveDurableResult bankMoveDurableResult = new Results.BankMoveDurableResult();
        strBankMoveDurableResults.add(bankMoveDurableResult);
        bankMoveDurableResult.setDurableID(durableID);

        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)
                && !CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)
                && !CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
            Validations.check(true, new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }
        //---------------------------------------
        // Object Lock
        //---------------------------------------
        try {
            if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)) {
                objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
            } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)) {
                objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
            } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
            }
        } catch (ServiceException e) {
            bankMoveDurableResult.setErrorCode(e.getCode());
            bankMoveDurableResult.setErrorMessage(e.getMessage());
            return durableBankMoveReqResult;
        }
        //---------------------------------------
        // Call durable_status_CheckForOperation
        //---------------------------------------
        try {
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATIONCATEGORY_BANKIN, durableID, durableCategory);
        } catch (ServiceException e) {
            log.info("durable_status_CheckForOperation() != RC_OK");
            bankMoveDurableResult.setErrorCode(e.getCode());
            bankMoveDurableResult.setErrorMessage(e.getMessage());
            return durableBankMoveReqResult;
        }
        //---------------------------------------
        // Check Durable Hold State
        //---------------------------------------
        String durableHoldStateGetOut;
        try {
            durableHoldStateGetOut = durableMethod.durableHoldStateGet(objCommon, durableCategory, durableID);
        } catch (ServiceException ex) {
            bankMoveDurableResult.setErrorCode(ex.getCode());
            bankMoveDurableResult.setErrorMessage(ex.getMessage());
            return durableBankMoveReqResult;
        }
        if (CimStringUtils.isNotEmpty(durableHoldStateGetOut)
                && !CimStringUtils.equals(durableHoldStateGetOut, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD)
                && !CimStringUtils.equals(durableHoldStateGetOut, BizConstant.SP_UNDEFINED_STATE)) {
            bankMoveDurableResult.setErrorCode(retCodeConfig.getInvalidDurableHoldStat().getCode());
            bankMoveDurableResult.setErrorMessage(retCodeConfig.getInvalidDurableHoldStat().getMessage());
            return durableBankMoveReqResult;
        }
        //---------------------------------------
        // Check Durable Inventory State
        //---------------------------------------
        String durableinventoryState = null;
        try {
            durableinventoryState = durableMethod.durableInventoryStateGet(objCommon, durableCategory, durableID);
        } catch (ServiceException e) {
            log.info("durable_inventoryState_Get() != RC_OK");
            bankMoveDurableResult.setErrorCode(e.getCode());
            bankMoveDurableResult.setErrorMessage(e.getMessage());
            return durableBankMoveReqResult;
        }

        if (!CimStringUtils.equals(durableinventoryState, BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK)) {
            log.info("durableInventoryState != SP_Durable_InventoryState_OnFloor");
            bankMoveDurableResult.setErrorCode(retCodeConfig.getInvalidDurableInventoryStat().getCode());
            bankMoveDurableResult.setErrorMessage(retCodeConfig.getInvalidDurableInventoryStat().getMessage());
            return durableBankMoveReqResult;
        }
        //-----------------------------------------------------------
        // Check cassette interFabXferState
        //-----------------------------------------------------------
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            String cassetteInterFabXferStateGetResult = null;
            try {
                cassetteInterFabXferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommon, durableID);
            } catch (ServiceException e) {
                bankMoveDurableResult.setErrorCode(e.getCode());
                bankMoveDurableResult.setErrorMessage(e.getMessage());
                return durableBankMoveReqResult;
            }
            if (CimStringUtils.equals(cassetteInterFabXferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
                bankMoveDurableResult.setErrorCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest().getCode());
                bankMoveDurableResult.setErrorMessage(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest().getMessage());
                return durableBankMoveReqResult;
            }
        }

        //-----------------------------------------------------------
        // Call Durable BankMove
        //-----------------------------------------------------------
        try {
            bankForDurableMethod.durableBankMove(objCommon, durableCategory, durableID, toBankID);
        } catch (ServiceException e) {
            bankMoveDurableResult.setErrorCode(e.getCode());
            bankMoveDurableResult.setErrorMessage(e.getMessage());
            return durableBankMoveReqResult;
        }
        //-----------------------------------------------------------
        // Create Durable Bank Move Event
        //-----------------------------------------------------------
        Infos.DurablebankmoveeventMakeIn strDurableBankMoveEvent_Make_in = new Infos.DurablebankmoveeventMakeIn();
        strDurableBankMoveEvent_Make_in.setClaimMemo(claimMemo);
        strDurableBankMoveEvent_Make_in.setTransactionID(TransactionIDEnum.DURABLE_BANK_MOVE_REQ.getValue());
        strDurableBankMoveEvent_Make_in.setDurableCategory(durableCategory);
        strDurableBankMoveEvent_Make_in.setDurableID(durableID);
        try {
            eventMethod.durableBankMoveEventMake(objCommon, strDurableBankMoveEvent_Make_in);
        } catch (ServiceException e) {
            bankMoveDurableResult.setErrorCode(e.getCode());
            bankMoveDurableResult.setErrorMessage(e.getMessage());
            return durableBankMoveReqResult;
        }
        bankMoveDurableResult.setErrorCode(retCodeConfig.getSucc().getCode());
        bankMoveDurableResult.setErrorMessage("Sucess");
        return durableBankMoveReqResult;
    }

    /**
     * This function changes the specified Durable Control Job's Status.
     *
     * <p> When this function is claimed with "create" or "delete", this
     * function create a Durable Control Job /delete the specified Durable Control Job.
     *
     * <p> Pseudo code:
     * <p> 1. Check Environmental Variable (OM_CJ_STATUS_HISTORY_SET) is set as specification.
     * <p> 2. Check claimed DCJ Action.
     * <p>       At the same time, set 2 flags(whether to make Event, whether to check current CJ status).
     * <p>       bNeedtoMakeEvent = false;
     * <p>       bNeedtoCheckCJStatus = false;
     * <p>       +------------------------------------------------------------------+
     * <p>       | Claimed CJ  | EventMake | CJ Status check      | Call TCS method |
     * <p>       | Action      |           | Status should be...  |                 |
     * <p>       | -----------------------------------------------------------------|
     * <p>       | CJ create   | Depend on | N/A                  | No              |
     * <p>       | CJ queue    | Depend on | Not needed           | No              |
     * <p>       | CJ execute  | Depend on | Not needed           | No              |
     * <p>       | CJ complete | Depend on | Not needed           | No              |
     * <p>       | CJ delete   | Depend on | Not needed           | No              |
     * <p>       | CJ abort    | Necessary | "Queued"/"Executing" | Yes             |
     * <p>       | CJ stop     | Necessary | "Queued"/"Executing" | Yes             |
     * <p>       +------------------------------------------------------------------+
     * <p>       * "Depend on" the OM_CJ_STATUS_HISTORY_SET
     * <p> 3. If current CJ Status check is needed, get current CJ status and check it.
     * <p>    If the result of "CJ Status check" is OK, call TCS method.
     * <p> 4. Change CJ status
     * <p> 5. Create event if EventMake is needed.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/28 18:18
     */
    @Override
    public ObjectIdentifier sxDrbCJStatusChangeReq(Infos.ObjCommon objCommon, Params.DurableControlJobManageReqInParam paramIn) {
        ObjectIdentifier retID = new ObjectIdentifier();
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        String controlJobAction = paramIn.getControlJobAction();
        ObjectIdentifier durableControlJobID = paramIn.getDurableControlJobID();
        Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest = paramIn.getStrDurableControlJobCreateRequest();
        String durableCategory = strDurableControlJobCreateRequest.getDurableCategory();
        ObjectIdentifier equipmentID = strDurableControlJobCreateRequest.getEquipmentID();
        List<Infos.StartDurable> strStartDurables = strDurableControlJobCreateRequest.getStrStartDurables();

        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE) &&
                !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD) &&
                !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }

        //---------------------------------------
        // Check Durable ControlJob ID
        //---------------------------------------
        if (!CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_CREATE)) {
            if (ObjectIdentifier.isEmpty(durableControlJobID)) {
                log.info("Request of DurableControlJobID is null");
                throw new ServiceException(retCodeConfig.getNotFoundDctrljob());
            }
        }
        objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);

        //---------------------------------------
        // Lock durable objects for update.
        //---------------------------------------
        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        for (Infos.StartDurable startDurable : strStartDurables) {
            durableIDs.add(startDurable.getDurableId());
        }
        if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)) {
            objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, durableIDs);
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)) {
            objectLockMethod.objectSequenceLock(objCommon, CimReticlePod.class, durableIDs);
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
            objectLockMethod.objectSequenceLock(objCommon, CimProcessDurable.class, durableIDs);
        }
        //---------------------------------------
        // Call durable_CheckConditionForOperation
        //---------------------------------------
        Inputs.ObjDurableCheckConditionForOperationIn conditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
        conditionForOperationIn.setOperation(BizConstant.SP_OPERATION_DURABLECONTROLJOBMANAGE);
        conditionForOperationIn.setEquipmentId(equipmentID);
        conditionForOperationIn.setDurableCategory(durableCategory);
        conditionForOperationIn.setStartDurables(strStartDurables);
        durableMethod.durableCheckConditionForOperationForInternalBuffer(objCommon, conditionForOperationIn);

        //---------------------------------------
        // Call durable_status_CheckForOperation
        //---------------------------------------
        for (Infos.StartDurable startDurable : strStartDurables) {
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATION_DURABLECONTROLJOBMANAGE, startDurable.getDurableId(), durableCategory);
        }

        //-------------------------------------------------------
        // Get Environmental Variables for Event_Make
        //-------------------------------------------------------
        String tmpControlJobStatusChangeHistoryMask = StandardProperties.OM_CJ_STATUS_HISTORY_SET.getValue();
        int counter = 0;
        if (tmpControlJobStatusChangeHistoryMask.length() == 5) {
            for (counter = 0; counter < 5; counter++) {
                if (CimStringUtils.equals(tmpControlJobStatusChangeHistoryMask.charAt(counter), "0") || CimStringUtils.equals(tmpControlJobStatusChangeHistoryMask.charAt(counter), "1")) {
                    continue;
                } else {
                    break;
                }
            }
            if (counter == 5) {
                log.info("Environment variable was set to the default value of 00000 because of the invalid input.");
                tmpControlJobStatusChangeHistoryMask = "00000";
            }
        } else {
            log.info("Environment variable was set to the default value of 00000 because of the invalid input.");
            tmpControlJobStatusChangeHistoryMask = "00000";
        }
        //-------------------------------------------------------
        //
        // Check Input Parameter
        //
        //-------------------------------------------------------
        boolean bNeedtoCheckCJStatus = false;
        boolean bNeedtoMakeEvent = false;
        String controlJobStatus;
        ObjectIdentifier saveControlJobID;

        if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_CREATE)) {
            if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.DURABLE_CONTROL_JOB_MANAGE_REQ.getValue())) {
                log.info(String.format("Request of ControlJobAction type : create is not supported to the specified transaction : %s", objCommon.getTransactionID()));
                throw new ServiceException(new OmCode(retCodeConfig.getCalledFromInvalidTransaction(), objCommon.getTransactionID()));
            }
            //Call  durableControlJob_Create
            ObjectIdentifier strDurableControlJob_Create_out = durableControlJobMethod.durableControlJobCreate(objCommon, equipmentID, durableCategory, strStartDurables);

            if (CimStringUtils.equals(tmpControlJobStatusChangeHistoryMask.charAt(0), "1")) {
                bNeedtoMakeEvent = true;
            } else {
                bNeedtoMakeEvent = false;
            }
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_CREATED;
            retID = strDurableControlJob_Create_out;
            saveControlJobID = strDurableControlJob_Create_out;

            for (Infos.StartDurable startDurable : strStartDurables) {
                durableControlJobMethod.durableDurableControlJobIDSet(objCommon, startDurable.getDurableId(), strDurableControlJob_Create_out, durableCategory);
            }
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_QUEUE)) {
            if (CimStringUtils.equals(tmpControlJobStatusChangeHistoryMask.charAt(1), "1")) {
                bNeedtoMakeEvent = true;
            } else {
                bNeedtoMakeEvent = false;
            }
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_QUEUED;
            saveControlJobID = durableControlJobID;
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_EXECUTE)) {
            if (CimStringUtils.equals(tmpControlJobStatusChangeHistoryMask.charAt(2), "1")) {
                bNeedtoMakeEvent = true;
            } else {
                bNeedtoMakeEvent = false;
            }
            controlJobStatus = BizConstant.SP_CONTROLJOBACTION_TYPE_EXECUTE;
            saveControlJobID = durableControlJobID;
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_COMPLETE)) {
            if (CimStringUtils.equals(tmpControlJobStatusChangeHistoryMask.charAt(3), "1")) {
                bNeedtoMakeEvent = true;
            } else {
                bNeedtoMakeEvent = false;
            }
            controlJobStatus = BizConstant.SP_CONTROLJOBACTION_TYPE_COMPLETE;
            saveControlJobID = durableControlJobID;
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE)) {
            if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.DURABLE_CONTROL_JOB_MANAGE_REQ.getValue())) {
                throw new ServiceException(retCodeConfig.getCalledFromInvalidTransaction(), objCommon.getTransactionID());
            }
            if (CimStringUtils.equals(tmpControlJobStatusChangeHistoryMask.charAt(4), "1")) {
                bNeedtoMakeEvent = true;
            } else {
                bNeedtoMakeEvent = false;
            }
            controlJobStatus = BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE;
            saveControlJobID = durableControlJobID;
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_STOP)) {
            log.info("ControlJobAction type : stop");
            bNeedtoMakeEvent = true;
            bNeedtoCheckCJStatus = true;
            controlJobStatus = BizConstant.SP_CONTROLJOBACTION_TYPE_STOP;
            saveControlJobID = durableControlJobID;
        } else if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_ABORT)) {
            log.info("ControlJobAction type : abort");
            bNeedtoMakeEvent = true;
            bNeedtoCheckCJStatus = true;
            controlJobStatus = BizConstant.SP_CONTROLJOBACTION_TYPE_ABORT;
            saveControlJobID = durableControlJobID;
        } else {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidControlJobActionType(), controlJobAction));
        }
        //-------------------------------------------------------
        //   Call TCSMgr's method for "Abort"/"Stop".
        //   Event_Make is at the same time ( if needed ).
        //-------------------------------------------------------
        if (bNeedtoCheckCJStatus || bNeedtoMakeEvent) {
            //---------------------------------------------------------------------------
            // Get common information between calling TCSMgr's method and Event_Make
            //---------------------------------------------------------------------------
            Infos.DurableControlJobStartReserveInformationGetIn durableCJSRInformationGetIn = new Infos.DurableControlJobStartReserveInformationGetIn();
            durableCJSRInformationGetIn.setDurableControlJobID(saveControlJobID);
            Infos.DurableControlJobStartReserveInformationGetOut durableControlJobStartReserveInformationGetOut = durableMethod.durableControlJobStartReserveInformationGet(objCommon, durableCJSRInformationGetIn);
            //-------------------------------------------------------
            // Check current CJ status to before calling TCSMgr's method
            //-------------------------------------------------------
            if (bNeedtoCheckCJStatus) {
                //-------------------------------------------------------
                // Get current status of the specified CJ
                //-------------------------------------------------------
                Infos.DurableControlJobStatusGet strDurableControlJobStatusGetOut = durableMethod.durableControlJobStatusGet(objCommon, saveControlJobID);
                String durableControlJobStatus = strDurableControlJobStatusGetOut.getDurableControlJobStatus();
                //-------------------------------------------------------
                // Current status vs. required CJ Action
                //-------------------------------------------------------
                if (CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_CONTROLJOBSTATUS_QUEUED)
                        || CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_CONTROLJOBSTATUS_EXECUTING)) {
                    // OK. Nothing to do.
                } else {
                    throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidDctrljobactionForDcjstatus(), durableControlJobStatus, saveControlJobID.getValue()));
                }
                //-----------------------------------------------------------
                // Get Eqp Port Information
                //-----------------------------------------------------------
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, durableControlJobStartReserveInformationGetOut.getEquipmentID());
                //-----------------------------------------------------------
                // Check On-line mode of EQP to call TCSMgr's method
                //-----------------------------------------------------------
                if (CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                    log.info("onlineMode == SP_Eqp_OnlineMode_Offline");
                    Validations.check(retCodeConfig.getInvalidEquipmentMode(),
                            ObjectIdentifier.fetchValue(durableControlJobStartReserveInformationGetOut.getEquipmentID()),
                            eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode());
                }
                //-------------------------------------------------------
                // Call Tx  ControlJobActionReq of TCSMgr
                //-------------------------------------------------------
                /*--------------------------*/
                /*    Send Request to TCS   */
                /*--------------------------*/
                Inputs.SendDurableControlJobActionReqIn sendDurableControlJobActionReqIn = new Inputs.SendDurableControlJobActionReqIn();
                sendDurableControlJobActionReqIn.setEquipmentID(durableControlJobStartReserveInformationGetOut.getEquipmentID());
                sendDurableControlJobActionReqIn.setDurableControlJobID(saveControlJobID);
                sendDurableControlJobActionReqIn.setActionCode(controlJobAction);
                sendDurableControlJobActionReqIn.setClaimMemo(paramIn.getClaimMemo());
                tcsMethod.sendTCSReq(TCSReqEnum.sendDurableControlJobActionReq, sendDurableControlJobActionReqIn);
            }
            //-------------------------------------------------------
            // Event Make
            //-------------------------------------------------------
            if (bNeedtoMakeEvent) {
                Infos.DurableControlJobStatusChangeEventMake strDurableControlJobStatusChangeEvent_Make_in = new Infos.DurableControlJobStatusChangeEventMake();
                strDurableControlJobStatusChangeEvent_Make_in.setTransactionID(TransactionIDEnum.DURABLE_CONTROL_JOB_MANAGE_REQ.getValue());
                strDurableControlJobStatusChangeEvent_Make_in.setClaimMemo(paramIn.getClaimMemo());
                strDurableControlJobStatusChangeEvent_Make_in.setDurableCategory(durableCategory);
                strDurableControlJobStatusChangeEvent_Make_in.setDurableControlJobID(saveControlJobID);
                strDurableControlJobStatusChangeEvent_Make_in.setDurableControlJobStatus(controlJobStatus);
                strDurableControlJobStatusChangeEvent_Make_in.setStrStartDurables(durableControlJobStartReserveInformationGetOut.getStrStartDurables());
                eventMethod.durableControlJobStatusChangeEventMake(objCommon, strDurableControlJobStatusChangeEvent_Make_in);
            }
        }
        //-------------------------------------------------------
        //
        // Change ControlJob Status
        //
        //-------------------------------------------------------
        durableControlJobMethod.durableControlJobStatusChange(objCommon, saveControlJobID, controlJobStatus);
        //-------------------------------------------------------
        //
        //   Delete ControlJob when deletion of CJ is claimed
        //   Method call for deleting ControlJob should be at the end.
        //   ControlJob needs to be alive during above procedure.
        //
        //-------------------------------------------------------
        if (CimStringUtils.equals(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE)) {
            durableControlJobMethod.durableControlJobDelete(objCommon, saveControlJobID, controlJobAction);
        }
        return retID;
    }

    @Override
    public Results.DurableDeleteReqResult sxDrbDeleteReq(Results.DurableDeleteReqResult durableDeleteReqResult, Infos.ObjCommon objCommon, ObjectIdentifier durableID, String className, String claimMemo) {
        List<Results.DurableDeleteResult> durableDeleteResults = durableDeleteReqResult.getStrDurableDeleteResults();
        Results.DurableDeleteResult durableDeleteResult = new Results.DurableDeleteResult();
        durableDeleteResults.add(durableDeleteResult);
        durableDeleteResult.setDurableID(durableID);
        //------------------------------------------------------------
        // Lock the target object
        //------------------------------------------------------------
        String lockClass, durableAdministrator;
        if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, className)) {
            lockClass = BizConstant.SP_CLASSNAME_POSCASSETTE;
            durableAdministrator = StandardProperties.OM_CARRIER_ADMIN.getValue();
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, className)) {
            lockClass = BizConstant.SP_CLASSNAME_POSRETICLEPOD;
            durableAdministrator = StandardProperties.OM_RTCLPOD_ADMIN.getValue();
        } else {
            durableDeleteResult.setErrorCode(retCodeConfig.getInvalidInputParam().getCode());
            durableDeleteResult.setErrorMessage(retCodeConfig.getInvalidInputParam().getMessage());
            return durableDeleteReqResult;
        }

        //------------------------------------------------------------
        // Check the administrator of durables
        //------------------------------------------------------------
        if (!CimStringUtils.equals(durableAdministrator, BizConstant.SP_SUBSYSTEMID_MM)) {
            durableDeleteResult.setErrorCode(retCodeConfig.getAdministrationNotAuthrize().getCode());
            durableDeleteResult.setErrorMessage(retCodeConfig.getAdministrationNotAuthrize().getMessage());
            return durableDeleteReqResult;
        }

        try {
            if (CimStringUtils.equals(lockClass, BizConstant.SP_CLASSNAME_POSCASSETTE)) {
                objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
            } else if (CimStringUtils.equals(lockClass, BizConstant.SP_CLASSNAME_POSRETICLEPOD)) {
                objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
            }
        } catch (ServiceException e) {
            durableDeleteResult.setErrorCode(e.getCode());
            durableDeleteResult.setErrorMessage(e.getMessage());
            return durableDeleteReqResult;
        }
        //------------------------------------------------------------
        // Check whether the target object can be deleted or not
        //------------------------------------------------------------
        try {
            durableMethod.durableCheckForDeletion(objCommon, className, durableID);
        } catch (ServiceException e) {
            durableDeleteResult.setErrorCode(e.getCode());
            durableDeleteResult.setErrorMessage(e.getMessage());
            return durableDeleteReqResult;
        }
        //------------------------------------------------------------
        // Delete the target object
        //------------------------------------------------------------
        Infos.DurableAttribute deleteAttribute = new Infos.DurableAttribute();
        try {
            deleteAttribute = durableMethod.durableDelete(objCommon, className, durableID);
        } catch (ServiceException e) {
            durableDeleteResult.setErrorCode(e.getCode());
            durableDeleteResult.setErrorMessage(e.getMessage());
            return durableDeleteReqResult;
        }
        //------------------------------------------------------------
        // Create the target object's deletion event
        //------------------------------------------------------------
        try {
            eventMethod.durableRegistEventMake(objCommon, objCommon.getTransactionID(), BizConstant.SP_DURABLE_ACTION_DELETE, className, deleteAttribute, claimMemo);
        } catch (ServiceException e) {
            durableDeleteResult.setErrorCode(e.getCode());
            durableDeleteResult.setErrorMessage(e.getMessage());
            return durableDeleteReqResult;
        }

        durableDeleteResult.setErrorCode(retCodeConfig.getSucc().getCode());
        durableDeleteResult.setErrorMessage("Sucess");
        return durableDeleteReqResult;
    }

    @Override
    public void sxDrbForceSkipReq(Infos.ObjCommon strObjCommonIn, Params.DurableForceOpeLocateReqInParam strDurableForceOpeLocateReqInParam, String claimMemo) {
        log.info("PPTManager_i:: txDurableForceOpeLocateReq ");

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Params.DurableForceOpeLocateReqInParam strInParm = strDurableForceOpeLocateReqInParam;
        log.info("{} {}", "in-parm durableCategory        ", strInParm.getDurableCategory());
        log.info("{} {}", "in-parm durableID              ", ObjectIdentifier.fetchValue(strInParm.getDurableID()));
        log.info("{} {}", "in-parm locateDirection        ", strInParm.getLocateDirection());
        log.info("{} {}", "in-parm currentRouteID         ", ObjectIdentifier.fetchValue(strInParm.getCurrentRouteID()));
        log.info("{} {}", "in-parm currentOperationNumber ", strInParm.getCurrentOperationNumber());
        log.info("{} {}", "in-parm routeID                ", ObjectIdentifier.fetchValue(strInParm.getRouteID()));
        log.info("{} {}", "in-parm operationID            ", ObjectIdentifier.fetchValue(strInParm.getOperationID()));
        log.info("{} {}", "in-parm operationNumber        ", strInParm.getOperationNumber());
        log.info("{} {}", "in-parm seqno                  ", strInParm.getSeqno());

        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "Invalid durable category", strInParm.getDurableCategory());
            Validations.check(true,
                    retCodeConfig.getInvalidDurableCategory(),
                    strInParm.getDurableCategory());
        }

        //---------------------------------------
        //  Check Durable Hold State
        //---------------------------------------
        String strDurableholdStateGetout;
        strDurableholdStateGetout = durableMethod.durableHoldStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (!CimStringUtils.equals(strDurableholdStateGetout, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
            log.info("{}", "durableHoldState != SP_Durable_HoldState_OnHold");
            Validations.check(true,
                    retCodeConfigEx.getInvalidDurableStat(),
                    strDurableholdStateGetout);
        }

        //---------------------------------------
        //  Check LOCK Hold
        //---------------------------------------
        Infos.DurableCheckLockHoldConditionForOperationIn strDurableCheckLockHoldConditionForOperationin = new Infos.DurableCheckLockHoldConditionForOperationIn();
        strDurableCheckLockHoldConditionForOperationin.setDurableCategorySeq(new ArrayList<>());
        strDurableCheckLockHoldConditionForOperationin.setDurableIDSeq(new ArrayList<>());
        strDurableCheckLockHoldConditionForOperationin.getDurableCategorySeq().add(strInParm.getDurableCategory());
        strDurableCheckLockHoldConditionForOperationin.getDurableIDSeq().add(strInParm.getDurableID());
        durableMethod.durableCheckLockHoldConditionForOperation(
                strObjCommonIn,
                strDurableCheckLockHoldConditionForOperationin);

        //-----------------------------------------------------------
        // Call txDurableOpeLocateReq
        //-----------------------------------------------------------
        Params.DurableOpeLocateReqInParam strDurableOpeLocateReqInParam = new Params.DurableOpeLocateReqInParam();
        strDurableOpeLocateReqInParam.setLocateDirection(strInParm.getLocateDirection());
        strDurableOpeLocateReqInParam.setDurableCategory(strInParm.getDurableCategory());
        strDurableOpeLocateReqInParam.setDurableID(strInParm.getDurableID());
        strDurableOpeLocateReqInParam.setCurrentRouteID(strInParm.getCurrentRouteID());
        strDurableOpeLocateReqInParam.setCurrentOperationNumber(strInParm.getCurrentOperationNumber());
        strDurableOpeLocateReqInParam.setRouteID(strInParm.getRouteID());
        strDurableOpeLocateReqInParam.setOperationID(strInParm.getOperationID());
        strDurableOpeLocateReqInParam.setOperationNumber(strInParm.getOperationNumber());
        strDurableOpeLocateReqInParam.setProcessRef(strInParm.getProcessRef());
        strDurableOpeLocateReqInParam.setSeqno(strInParm.getSeqno());
        sxDrbSkipReq(strObjCommonIn, strDurableOpeLocateReqInParam, claimMemo);

        //---------------------------------------
        // Return
        //---------------------------------------
        log.info("PPTManager_i:: txDurableForceOpeLocateReq ");
    }

    @Override
    public Results.DurableProcessLagTimeUpdateReqResult sxDrbLagTimeActionReq(Infos.ObjCommon objCommonIn, User user, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm) {
        Results.DurableProcessLagTimeUpdateReqResult durableProcessLagTimeUpdateReqResult = new Results.DurableProcessLagTimeUpdateReqResult();
        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE) &&
                !CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD) &&
                !CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {

            Validations.check(true, retCodeConfig.getInvalidDurableCategory());
        }

        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            //-----------------------------------------------------------
            // Check cassette interFabXferState
            //-----------------------------------------------------------
            String cassetteInterFabXferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommonIn, durableProcessLagTimeUpdateReqInParm.getDurableID());
            //-----------------------------------------------------------
            // "Transferring"
            //-----------------------------------------------------------

            Validations.check(CimStringUtils.equals(cassetteInterFabXferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING), retCodeConfig.getInterfabInvalidLotXferstateForReq());


        }
        //-----------------------------------------------------------
        //   Action : SP_ProcessLagTime_Action_Set
        //    1. Set processLagTime information to duralbe
        //    2. Make "DLTH" durable hold by txHoldDurableReq()
        //-----------------------------------------------------------

        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getAction(), BizConstant.SP_PROCESSLAGTIME_ACTION_SET)) {
            log.info("", "in-parm's action = SP_PROCESSLAGTIME_ACTION_SET...");
            //-----------------------------------------------------------
            //   Get ProcessLagTime Information of previous operation
            //-----------------------------------------------------------
            log.info("", "Get ProcessLagTime Information of previous operation......");

            Outputs.ObjProcessLagTimeGetOut lagTimeGetOutRetCode = processForDurableMethod.processDurableProcessLagTimeGet(objCommonIn, durableProcessLagTimeUpdateReqInParm);
            //-----------------------------------------------------------
            //   Set ProcessLagTime Information to Durable
            //-----------------------------------------------------------
            processForDurableMethod.DurableProcessLagTimeSet(objCommonIn, durableProcessLagTimeUpdateReqInParm, lagTimeGetOutRetCode);

            if (CimNumberUtils.longValue(lagTimeGetOutRetCode.getExpriedTimeDuration()) > 0) {
                log.info("", "expiredTimeDuration > 0.........");
                //-----------------------------------------------------------
                //   Prepare for txHoldDurableReq's Input Parameter
                //-----------------------------------------------------------
                log.info("", "Prepare for txHoldDurableReq's Input Parameter...........");


                ObjectIdentifier holdReasonCodeID;
                ObjectIdentifier holdUserID;
                ObjectIdentifier dummyID = null;

                holdReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLEPROCESSLAGTIMEHOLD);
                holdUserID = ObjectIdentifier.buildWithValue(BizConstant.SP_PPTSVCMGR_PERSON);

                List<Infos.DurableHoldList> strDurableHoldList = new ArrayList<>();
                strDurableHoldList.add(new Infos.DurableHoldList());
                strDurableHoldList.get(0).setHoldType(BizConstant.SP_HOLDTYPE_DURABLEHOLD);
                strDurableHoldList.get(0).setHoldReasonCodeID(holdReasonCodeID);
                strDurableHoldList.get(0).setHoldUserID(holdUserID);
                strDurableHoldList.get(0).setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                strDurableHoldList.get(0).setRouteID(dummyID);
                strDurableHoldList.get(0).setOperationNumber("");
                strDurableHoldList.get(0).setRelatedDurableID(dummyID);
                strDurableHoldList.get(0).setRelatedDurableCategory("");
                strDurableHoldList.get(0).setClaimMemo(durableProcessLagTimeUpdateReqInParm.getClaimMemo());

                Params.HoldDurableReqInParam strHoldDurableReqInParam = new Params.HoldDurableReqInParam();
                strHoldDurableReqInParam.setDurableCategory(durableProcessLagTimeUpdateReqInParm.getDurableCategory());
                strHoldDurableReqInParam.setDurableID(durableProcessLagTimeUpdateReqInParm.getDurableID());
                strHoldDurableReqInParam.setDurableHoldLists(strDurableHoldList);

                //-----------------------------------------------------------
                //   Call txHoldDurableReq()
                //-----------------------------------------------------------
                try {
                    sxHoldDrbReq(objCommonIn, strHoldDurableReqInParam, durableProcessLagTimeUpdateReqInParm.getClaimMemo());
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(ex.getCode(), retCodeConfig.getExistSameHold())) {
                        throw ex;
                    }
                }

            }
        } else {
            //-----------------------------------------------------------
            //   Clear ProcessLagTime Information of Durable
            //-----------------------------------------------------------
            Outputs.ObjProcessLagTimeGetOut lagTimeGetOutRetCode = new Outputs.ObjProcessLagTimeGetOut();
            lagTimeGetOutRetCode.setProcessLagTimeStamp(Timestamp.valueOf(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING));
            processForDurableMethod.DurableProcessLagTimeSet(objCommonIn, durableProcessLagTimeUpdateReqInParm, lagTimeGetOutRetCode);

            //-----------------------------------------------------------
            //   Prepare for txHoldDurableReleaseReq's Input Parameter
            //-----------------------------------------------------------
            log.info("{}", "Prepare for txHoldDurableReleaseReq's Input Parameter...");
            ObjectIdentifier holdReasonCodeID;
            ObjectIdentifier holdUserID;
            ObjectIdentifier releaseReasonCodeID;
            ObjectIdentifier dummyID = null;

            holdReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLEPROCESSLAGTIMEHOLD);
            holdUserID = ObjectIdentifier.buildWithValue(BizConstant.SP_PPTSVCMGR_PERSON);
            releaseReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLEPROCESSLAGTIMEHOLDRELEASE);

            List<Infos.DurableHoldList> strDurableHoldList = new ArrayList<>();
            strDurableHoldList.add(new Infos.DurableHoldList());
            strDurableHoldList.get(0).setHoldType(BizConstant.SP_HOLDTYPE_DURABLEHOLD);
            strDurableHoldList.get(0).setHoldReasonCodeID(holdReasonCodeID);
            strDurableHoldList.get(0).setHoldUserID(holdUserID);
            strDurableHoldList.get(0).setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            strDurableHoldList.get(0).setRouteID(dummyID);
            strDurableHoldList.get(0).setOperationNumber("");
            strDurableHoldList.get(0).setRelatedDurableID(dummyID);
            strDurableHoldList.get(0).setRelatedDurableCategory("");
            strDurableHoldList.get(0).setClaimMemo(durableProcessLagTimeUpdateReqInParm.getClaimMemo());

            Infos.HoldDurableReleaseReqInParam strHoldDurableReleaseReqInParam = new Infos.HoldDurableReleaseReqInParam();
            strHoldDurableReleaseReqInParam.setDurableCategory(durableProcessLagTimeUpdateReqInParm.getDurableCategory());
            strHoldDurableReleaseReqInParam.setDurableID(durableProcessLagTimeUpdateReqInParm.getDurableID());
            strHoldDurableReleaseReqInParam.setReleaseReasonCodeID(releaseReasonCodeID);
            strHoldDurableReleaseReqInParam.setStrDurableHoldList(strDurableHoldList);

            //-----------------------------------------------------------
            //   Call txHoldDurableReleaseReq()
            //-----------------------------------------------------------
            log.info("{}", "Call txHoldLotReleaseReq()...");
            try {
                sxHoldDrbReleaseReq(
                        objCommonIn,
                        strHoldDurableReleaseReqInParam,
                        durableProcessLagTimeUpdateReqInParm.getClaimMemo());
            } catch (ServiceException ex) {
                if (!Validations.isEquals(ex.getCode(), retCodeConfigEx.getInvalidDurableStat()) &&
                        !Validations.isEquals(ex.getCode(), retCodeConfig.getNotExistHold())) {
                    throw ex;
                }
            }
        }
        return durableProcessLagTimeUpdateReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationStartCancelReqInParam
     * @param claimMemo
     * @return com.fa.cim.dto.Results.DurableOperationStartCancelReqResult
     * @throws
     * @author ho
     * @date 2020/6/23 15:05
     */
    @Override
    public Results.DurableOperationStartCancelReqResult sxDrbMoveInCancelForIBReq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationStartCancelReqInParam strDurableOperationStartCancelReqInParam, String claimMemo) {
        Results.DurableOperationStartCancelReqResult strDurableOperationStartCancelReqResult = new Results.DurableOperationStartCancelReqResult();
        log.info("PPTManager_i::txDurableOperationStartCancelReq");
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Params.DurableOperationStartCancelReqInParam strInParm = strDurableOperationStartCancelReqInParam;
        log.info("{} {}", "in-parm equipmentID          ", strInParm.getEquipmentID().getValue());
        log.info("{} {}", "in-parm durableControlJobID  ", strInParm.getDurableControlJobID().getValue());

        if (0 >= CimStringUtils.length(strInParm.getDurableControlJobID().getValue())) {
            log.info("{}", "durableControlJobID is empty.");
            Validations.check(true, retCodeConfig.getDurableControlJobBlank());
        }

        // equipment_SpecialControlVsTxID_CheckCombination
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(
                strObjCommonIn,
                strInParm.getEquipmentID());

        // Get required equipment lock mode
        Outputs.ObjLockModeOut strObjectlockModeGetout;
        Inputs.ObjLockModeIn strObjectlockModeGetin = new Inputs.ObjLockModeIn();
        strObjectlockModeGetin.setObjectID(strInParm.getEquipmentID());
        strObjectlockModeGetin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        strObjectlockModeGetin.setFunctionCategory("ODRBW028");
        strObjectlockModeGetin.setUserDataUpdateFlag(false);
        strObjectlockModeGetout = objectMethod.objectLockModeGet(
                strObjCommonIn,
                strObjectlockModeGetin);

        Inputs.ObjAdvanceLockIn strAdvancedobjectLockin = new Inputs.ObjAdvanceLockIn();

        Long lockMode = CimNumberUtils.longValue(strObjectlockModeGetout.getLockMode());
        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            log.info("{}", "lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjectLockin.setObjectID(strInParm.getEquipmentID());
            strAdvancedobjectLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjectLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjectLockin.setLockType(strObjectlockModeGetout.getRequiredLockForMainObject());
            strAdvancedobjectLockin.setKeyList(dummySeq);

            log.info("{} {}", "calling advanced_object_Lock()", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            lockMethod.advancedObjectLock(
                    strObjCommonIn,
                    strAdvancedobjectLockin);
        } else {
            log.info("{}", "lockMode == SP_EQP_LOCK_MODE_WRITE");
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            log.info("{}", "#### Machine Object Lock ");
            lockMethod.objectLock(
                    strObjCommonIn,
                    CimMachine.class,
                    strInParm.getEquipmentID());
        }

        Infos.DurableControlJobStartReserveInformationGetOut strDurableControlJobstartReserveInformationGetout;
        Infos.DurableControlJobStartReserveInformationGetIn strDurableControlJobstartReserveInformationGetin = new Infos.DurableControlJobStartReserveInformationGetIn();
        strDurableControlJobstartReserveInformationGetin.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobstartReserveInformationGetout = durableMethod.durableControlJobStartReserveInformationGet(
                strObjCommonIn,
                strDurableControlJobstartReserveInformationGetin);

        String durableCategory = strDurableControlJobstartReserveInformationGetout.getDurableCategory();
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", durableCategory);
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), durableCategory);
        }

        List<Infos.StartDurable> strStartDurables;
        strStartDurables = strDurableControlJobstartReserveInformationGetout.getStrStartDurables();
        Infos.DurableStartRecipe strDurableStartRecipe;
        strDurableStartRecipe = strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe();
        int durableLen = CimArrayUtils.getSize(strStartDurables);
        int durableCnt = 0;

        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            log.info("{}", "lockMode != SP_EQP_LOCK_MODE_WRITE");
            if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
                log.info("{}", "durableCategory == SP_DurableCat_Cassette");
                int cassetteIDCnt = 0;
                List<String> loadCastSeq;
                loadCastSeq = new ArrayList<>(durableLen);

                for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                    log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                    Infos.LotLocationInfo strCassetteLocationInfoGetDRout;
                    strCassetteLocationInfoGetDRout = cassetteMethod.cassetteLocationInfoGetDR(
                            strStartDurables.get(durableCnt).getDurableId());

                    log.info("{} {}", "transferStatus", strCassetteLocationInfoGetDRout.getTransferStatus());
                    if (CimStringUtils.equals(strCassetteLocationInfoGetDRout.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                            && CimStringUtils.equals(strCassetteLocationInfoGetDRout.getEquipmentID().getValue(), strInParm.getEquipmentID().getValue())) {
                        log.info("{} {}", "This cassette is load on equipment ", strStartDurables.get(durableCnt).getDurableId().getValue());
                        // This cassette is load on equipment
                        loadCastSeq.add(strStartDurables.get(durableCnt).getDurableId().getValue());
                        cassetteIDCnt++;
                    }
                }

                // Lock Equipment LoadCassette Element (Read)
                if (0 < CimArrayUtils.getSize(loadCastSeq)) {
                    log.info("{} {}", "loadCastSeq.length", CimArrayUtils.getSize(loadCastSeq));
                    strAdvancedobjectLockin.setObjectID(strInParm.getEquipmentID());
                    strAdvancedobjectLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    strAdvancedobjectLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                    strAdvancedobjectLockin.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ * 1L);
                    strAdvancedobjectLockin.setKeyList(loadCastSeq);

                    log.info("{} {}", "calling advanced_object_Lock()", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                    lockMethod.advancedObjectLock(
                            strObjCommonIn,
                            strAdvancedobjectLockin);
                }
            }
        }

        lockMethod.objectLock(
                strObjCommonIn,
                CimDurableControlJob.class,
                strInParm.getDurableControlJobID());

        List<ObjectIdentifier> durableIDs;
        durableIDs = new ArrayList<>(durableLen);
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableIDs.add(strStartDurables.get(durableCnt).getDurableId());
        }

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            lockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimCassette.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            lockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimReticlePod.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            lockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimProcessDurable.class,
                    durableIDs);
        }

        boolean onRouteFlag = true;
        boolean tmpOnRouteFlag = true;

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "durable loop index", durableCnt);

            //---------------------------------------
            //  Check Durable OnRoute
            //---------------------------------------
            ServiceException exception = null;
            try {
                durableMethod.durableOnRouteCheck(strObjCommonIn, durableCategory, strStartDurables.get(durableCnt).getDurableId());
            } catch (ServiceException ex) {
                exception = ex;
            }
            if (durableCnt == 0) {
                if (exception != null && !Validations.isEquals(retCodeConfig.getDurableOnroute(), exception.getCode())) {
                    onRouteFlag = false;
                }
                log.info("{} {}", "the first durable OnRoute state",
                        (onRouteFlag ? "TRUE" : "FALSE"),
                        strStartDurables.get(durableCnt).getDurableId().getValue());
            } else {
                if (exception != null && !Validations.isEquals(retCodeConfig.getDurableOnroute(), exception.getCode())) {
                    log.info("{} {}", "durable OnRoute state is FALSE", strStartDurables.get(durableCnt).getDurableId().getValue());
                    tmpOnRouteFlag = false;
                } else {
                    log.info("{} {}", "durable OnRoute state is TRUE", strStartDurables.get(durableCnt).getDurableId().getValue());
                    tmpOnRouteFlag = true;
                }

                if (tmpOnRouteFlag != onRouteFlag) {
                    log.info("{}", "all durable OnRoute state is not same");
                    Validations.check(true, retCodeConfigEx.getDurableOnRouteStatNotSame());
                }
            }
        }

        String portGroupID;
        String onlineMode;
        String accessMode = null;
        ObjectIdentifier operationMode = null;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Outputs.ObjEquipmentPortGroupIDGetOut strEquipmentportGroupIDGetout;
            Inputs.ObjEquipmentPortGroupIDGetIn strEquipmentportGroupIDGetin = new Inputs.ObjEquipmentPortGroupIDGetIn();
            strEquipmentportGroupIDGetin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentportGroupIDGetin.setPortId(strStartDurables.get(0).getStartDurablePort().getUnloadPortID());
            strEquipmentportGroupIDGetout = portMethod.equipmentPortGroupIDGet(
                    strObjCommonIn,
                    strEquipmentportGroupIDGetin);
            portGroupID = strEquipmentportGroupIDGetout.getPortGroupId();

            /*---------------------------------*/
            /*   Get Equipment's Online Mode   */
            /*---------------------------------*/
            Outputs.ObjPortResourceCurrentOperationModeGetOut strPortResourcecurrentOperationModeGetout;
            strPortResourcecurrentOperationModeGetout = portMethod.portResourceCurrentOperationModeGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID(),
                    strStartDurables.get(0).getStartDurablePort().getUnloadPortID());

            onlineMode = strPortResourcecurrentOperationModeGetout.getOperationMode().getOnlineMode();
            accessMode = strPortResourcecurrentOperationModeGetout.getOperationMode().getAccessMode();
            operationMode = strPortResourcecurrentOperationModeGetout.getOperationMode().getOperationMode();
        } else {
            log.info("{}", "durableCategory != SP_DurableCat_Cassette");
            portGroupID = ("");

            String strEquipmentonlineModeGetout;
            strEquipmentonlineModeGetout = equipmentMethod.equipmentOnlineModeGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID());

            onlineMode = strEquipmentonlineModeGetout;

            if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");

                Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut strReticlePodPortResourcecurrentAccessModeGetout;
                strReticlePodPortResourcecurrentAccessModeGetout = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strStartDurables.get(0).getStartDurablePort().getUnloadPortID());

                accessMode = strReticlePodPortResourcecurrentAccessModeGetout.getAccessMode();
            }
        }

        /*---------------------------------------------------*/
        /*                                                   */
        /*   If AccessMode is Auto, do the following check.  */
        /*                                                   */
        /*---------------------------------------------------*/
        log.info("{}", "accessMode = Auto");

        Inputs.ObjDurableCheckConditionForOperationIn strDurableCheckConditionForOperationin = new Inputs.ObjDurableCheckConditionForOperationIn();
        strDurableCheckConditionForOperationin.setOperation(BizConstant.SP_OPERATION_OPESTARTCANCEL);
        strDurableCheckConditionForOperationin.setEquipmentId(strInParm.getEquipmentID());
        strDurableCheckConditionForOperationin.setDurableCategory(durableCategory);
        strDurableCheckConditionForOperationin.setStartDurables(strStartDurables);
        strDurableCheckConditionForOperationin.setDurableStartRecipe(strDurableStartRecipe);
        durableMethod.durableCheckConditionForOperationForInternalBuffer(
                strObjCommonIn,
                strDurableCheckConditionForOperationin);

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableMethod.durableStatusCheckForOperation(
                    strObjCommonIn,
                    BizConstant.SP_OPERATION_OPESTARTCANCEL,
                    strStartDurables.get(durableCnt).getDurableId(),
                    durableCategory);
        }

        /*---------------------------------------------------*/
        /*                                                   */
        /*   If AccessMode is Auto, do the following check.  */
        /*                                                   */
        /*---------------------------------------------------*/
        if (CimStringUtils.equals(accessMode, BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
            log.info("{}", "accessMode = Auto");

            Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn strEquipmentandportStateCheckForDurableOperationin = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
            strEquipmentandportStateCheckForDurableOperationin.setOperation(BizConstant.SP_OPERATION_OPESTARTCANCEL);
            strEquipmentandportStateCheckForDurableOperationin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentandportStateCheckForDurableOperationin.setPortGroupId(portGroupID);
            strEquipmentandportStateCheckForDurableOperationin.setDurableCategory(durableCategory);
            strEquipmentandportStateCheckForDurableOperationin.setStartDurables(strStartDurables);
            equipmentMethod.equipmentAndPortStateCheckForDurableOperationForInternalBuffer(
                    strObjCommonIn,
                    strEquipmentandportStateCheckForDurableOperationin);
        }

        if (onRouteFlag) {
            log.info("{}", "onRouteFlag == TRUE");
            Infos.DurableProcessStateMakeWaitingIn strDurableprocessStateMakeWaitingin = new Infos.DurableProcessStateMakeWaitingIn();
            strDurableprocessStateMakeWaitingin.setDurableCategory(durableCategory);
            strDurableprocessStateMakeWaitingin.setStrStartDurables(strStartDurables);
            durableMethod.durableProcessStateMakeWaiting(
                    strObjCommonIn,
                    strDurableprocessStateMakeWaitingin);
        }

        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            log.info("{}", "onlineMode == SP_Eqp_OnlineMode_Offline");

            /*-----------------------------------------------*/
            /*   Change Equipment's Status to 'PRODUCTIVE'   */
            /*-----------------------------------------------*/

            /*===== get StateChageableFlag ===*/
            Boolean strEquipmentcurrentStateCheckToManufacturingout;
            strEquipmentcurrentStateCheckToManufacturingout = equipmentMethod.equipmentCurrentStateCheckToManufacturing(
                    strObjCommonIn,
                    strInParm.getEquipmentID());

            if (CimBooleanUtils.isTrue(strEquipmentcurrentStateCheckToManufacturingout)) {
                log.info("{}", "ManufacturingStateChangeableFlag == TRUE");

                /*===== get Defaclt Status Code for Productive / Standby ===*/
                ObjectIdentifier strEquipmentRecoverStateGetManufacturingForDurableout;
                strEquipmentRecoverStateGetManufacturingForDurableout = equipmentForDurableMethod.equipmentRecoverStateGetManufacturingForDurable(
                        strObjCommonIn,
                        BizConstant.SP_OPERATION_OPESTARTCANCEL,
                        strInParm.getEquipmentID(),
                        strInParm.getDurableControlJobID());

                /*---------------------------------*/
                /*   Call txEqpStatusChangeReq()   */
                /*---------------------------------*/
                Results.EqpStatusChangeReqResult strEqpStatusChangeReqResult;
                try {
                    strEqpStatusChangeReqResult = equipmentService.sxEqpStatusChangeReq(
                            strObjCommonIn,
                            strInParm.getEquipmentID(),
                            strEquipmentRecoverStateGetManufacturingForDurableout,
                            claimMemo);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(ex.getCode(), retCodeConfig.getCurrentStateSame())) {
                        throw ex;
                    }
                }
            }
        }

        Infos.ProcessStartDurablesReserveInformationClearIn strProcessstartDurablesReserveInformationClearin = new Infos.ProcessStartDurablesReserveInformationClearIn();
        strProcessstartDurablesReserveInformationClearin.setDurableCategory(durableCategory);
        strProcessstartDurablesReserveInformationClearin.setStrStartDurables(strStartDurables);
        processForDurableMethod.processStartDurablesReserveInformationClear(
                strObjCommonIn,
                strProcessstartDurablesReserveInformationClearin);

        ObjectIdentifier strDurableControlJobManageReqResult;
        Params.DurableControlJobManageReqInParam strDurableControlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        strDurableControlJobManageReqInParam.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_DELETE);
        Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        strDurableControlJobCreateRequest.setEquipmentID(strInParm.getEquipmentID());
        strDurableControlJobCreateRequest.setDurableCategory(durableCategory);
        strDurableControlJobCreateRequest.setStrStartDurables(strStartDurables);
        strDurableControlJobManageReqInParam.setStrDurableControlJobCreateRequest(strDurableControlJobCreateRequest);
        strDurableControlJobManageReqResult = sxDrbCJStatusChangeReq(
                strObjCommonIn,
                strDurableControlJobManageReqInParam);

        Infos.DurableOperationStartEventMakeOpeStartCancelIn strDurableOperationStartEventMakeOpeStartCancelin = new Infos.DurableOperationStartEventMakeOpeStartCancelIn();
        strDurableOperationStartEventMakeOpeStartCancelin.setEquipmentID(strInParm.getEquipmentID());
        strDurableOperationStartEventMakeOpeStartCancelin.setOperationMode(operationMode.getValue());
        strDurableOperationStartEventMakeOpeStartCancelin.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableOperationStartEventMakeOpeStartCancelin.setDurableCategory(durableCategory);
        strDurableOperationStartEventMakeOpeStartCancelin.setStrStartDurables(strStartDurables);
        strDurableOperationStartEventMakeOpeStartCancelin.setClaimMemo(claimMemo);
        eventMethod.durableOperationStartEventMakeOpeStartCancel(
                strObjCommonIn,
                strDurableOperationStartEventMakeOpeStartCancelin);

        /*--------------------------------------------*/
        /*   Send DurableOpeStartReq() to TCS         */
        /*--------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        int sleepTimeValue = 0;
        int retryCountValue = 0;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.intValue(BizConstant.SP_DEFAULT_SLEEP_TIME_TCS);
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.intValue(BizConstant.SP_DEFAULT_RETRY_COUNT_TCS);
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.intValue((tmpRetryCountValue));
        }

        log.info("{} {}", "env value of OM_EAP_CONNECT_SLEEP_TIME  = ", sleepTimeValue);
        log.info("{} {}", "env value of OM_EAP_CONNECT_RETRY_COUNT = ", retryCountValue);

        Results.DurableOperationStartCancelReqResult strTCSMgr_SendDurableOpeStartCancelReq_out;
        Infos.SendDurableOpeStartCancelReqIn strTCSMgrSendDurableOpeStartCancelReqin = new Infos.SendDurableOpeStartCancelReqIn();
        strTCSMgrSendDurableOpeStartCancelReqin.setStrDurableOperationStartCancelReqInParam(strInParm);
        strTCSMgrSendDurableOpeStartCancelReqin.setClaimMemo(claimMemo);

        //'retryCountValue + 1' means first try plus retry count
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            try {
                tcsMethod.sendDurableOpeStartCancelReq(
                        strObjCommonIn,
                        strTCSMgrSendDurableOpeStartCancelReqin);

                log.info("{}", "Now TCS subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "TCS subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    try {
                        Thread.sleep(sleepTimeValue * 1000L);
                        continue;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error(e.getMessage());
                    }
                } else {
                    log.info("{}", "TCSMgr_SendDurableOpeStartReq() != RC_OK");
                    throw ex;
                }
            }
        }

        // Set Return Structure
        strDurableOperationStartCancelReqResult.setDurableCategory(durableCategory);
        strDurableOperationStartCancelReqResult.setStrStartDurables(strStartDurables);

        // Return to caller
        return strDurableOperationStartCancelReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationStartCancelReqInParam
     * @param claimMemo
     * @return com.fa.cim.dto.Results.DurableOperationStartCancelReqResult
     * @throws
     * @author ho
     * @date 2020/6/23 15:05
     */
    @Override
    public Results.DurableOperationStartCancelReqResult sxDrbMoveInCancelReq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationStartCancelReqInParam strDurableOperationStartCancelReqInParam, String claimMemo) {
        Results.DurableOperationStartCancelReqResult strDurableOperationStartCancelReqResult = new Results.DurableOperationStartCancelReqResult();
        log.info("PPTManager_i::txDurableOperationStartCancelReq");
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Params.DurableOperationStartCancelReqInParam strInParm = strDurableOperationStartCancelReqInParam;
        log.info("{} {}", "in-parm equipmentID          ", strInParm.getEquipmentID().getValue());
        log.info("{} {}", "in-parm durableControlJobID  ", strInParm.getDurableControlJobID().getValue());

        if (0 >= CimStringUtils.length(strInParm.getDurableControlJobID().getValue())) {
            log.info("{}", "durableControlJobID is empty.");
            Validations.check(true, retCodeConfig.getDurableControlJobBlank());
        }

        // equipment_SpecialControlVsTxID_CheckCombination
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(
                strObjCommonIn,
                strInParm.getEquipmentID());

        // Get required equipment lock mode
        Outputs.ObjLockModeOut strObjectlockModeGetout;
        Inputs.ObjLockModeIn strObjectlockModeGetin = new Inputs.ObjLockModeIn();
        strObjectlockModeGetin.setObjectID(strInParm.getEquipmentID());
        strObjectlockModeGetin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        strObjectlockModeGetin.setFunctionCategory("ODRBW028");
        strObjectlockModeGetin.setUserDataUpdateFlag(false);
        strObjectlockModeGetout = objectMethod.objectLockModeGet(
                strObjCommonIn,
                strObjectlockModeGetin);

        Inputs.ObjAdvanceLockIn strAdvancedobjectLockin = new Inputs.ObjAdvanceLockIn();

        Long lockMode = CimNumberUtils.longValue(strObjectlockModeGetout.getLockMode());
        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            log.info("{}", "lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjectLockin.setObjectID(strInParm.getEquipmentID());
            strAdvancedobjectLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjectLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjectLockin.setLockType(strObjectlockModeGetout.getRequiredLockForMainObject());
            strAdvancedobjectLockin.setKeyList(dummySeq);

            log.info("{} {}", "calling advanced_object_Lock()", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            lockMethod.advancedObjectLock(
                    strObjCommonIn,
                    strAdvancedobjectLockin);
        } else {
            log.info("{}", "lockMode == SP_EQP_LOCK_MODE_WRITE");
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            log.info("{}", "#### Machine Object Lock ");
            lockMethod.objectLock(
                    strObjCommonIn,
                    CimMachine.class,
                    strInParm.getEquipmentID());
        }

        Infos.DurableControlJobStartReserveInformationGetOut strDurableControlJobstartReserveInformationGetout;
        Infos.DurableControlJobStartReserveInformationGetIn strDurableControlJobstartReserveInformationGetin = new Infos.DurableControlJobStartReserveInformationGetIn();
        strDurableControlJobstartReserveInformationGetin.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobstartReserveInformationGetout = durableMethod.durableControlJobStartReserveInformationGet(
                strObjCommonIn,
                strDurableControlJobstartReserveInformationGetin);

        String durableCategory = strDurableControlJobstartReserveInformationGetout.getDurableCategory();
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", durableCategory);
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), durableCategory);
        }

        List<Infos.StartDurable> strStartDurables;
        strStartDurables = strDurableControlJobstartReserveInformationGetout.getStrStartDurables();
        Infos.DurableStartRecipe strDurableStartRecipe;
        strDurableStartRecipe = strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe();
        int durableLen = CimArrayUtils.getSize(strStartDurables);
        int durableCnt = 0;

        if (!Objects.equals(lockMode, SP_EQP_LOCK_MODE_WRITE)) {
            log.info("{}", "lockMode != SP_EQP_LOCK_MODE_WRITE");
            if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
                log.info("{}", "durableCategory == SP_DurableCat_Cassette");
                int cassetteIDCnt = 0;
                List<String> loadCastSeq;
                loadCastSeq = new ArrayList<>(durableLen);

                for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
                    log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
                    Infos.LotLocationInfo strCassetteLocationInfoGetDRout;
                    strCassetteLocationInfoGetDRout = cassetteMethod.cassetteLocationInfoGetDR(
                            strStartDurables.get(durableCnt).getDurableId());

                    log.info("{} {}", "transferStatus", strCassetteLocationInfoGetDRout.getTransferStatus());
                    if (CimStringUtils.equals(strCassetteLocationInfoGetDRout.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                            && CimStringUtils.equals(strCassetteLocationInfoGetDRout.getEquipmentID().getValue(), strInParm.getEquipmentID().getValue())) {
                        log.info("{} {}", "This cassette is load on equipment ", strStartDurables.get(durableCnt).getDurableId().getValue());
                        // This cassette is load on equipment
                        loadCastSeq.add(strStartDurables.get(durableCnt).getDurableId().getValue());
                        cassetteIDCnt++;
                    }
                }

                // Lock Equipment LoadCassette Element (Read)
                if (0 < CimArrayUtils.getSize(loadCastSeq)) {
                    log.info("{} {}", "loadCastSeq.length", CimArrayUtils.getSize(loadCastSeq));
                    strAdvancedobjectLockin.setObjectID(strInParm.getEquipmentID());
                    strAdvancedobjectLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    strAdvancedobjectLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                    strAdvancedobjectLockin.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ * 1L);
                    strAdvancedobjectLockin.setKeyList(loadCastSeq);

                    log.info("{} {}", "calling advanced_object_Lock()", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                    lockMethod.advancedObjectLock(
                            strObjCommonIn,
                            strAdvancedobjectLockin);
                }
            }
        }

        lockMethod.objectLock(
                strObjCommonIn,
                CimDurableControlJob.class,
                strInParm.getDurableControlJobID());

        List<ObjectIdentifier> durableIDs;
        durableIDs = new ArrayList<>(durableLen);
        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableIDs.add(strStartDurables.get(durableCnt).getDurableId());
        }

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            lockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimCassette.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            lockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimReticlePod.class,
                    durableIDs);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Reticle");
            log.info("{} {}", "calling objectSequence_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            lockMethod.objectSequenceLock(
                    strObjCommonIn,
                    CimProcessDurable.class,
                    durableIDs);
        }

        boolean onRouteFlag = true;
        boolean tmpOnRouteFlag = true;

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "durable loop index", durableCnt);

            //---------------------------------------
            //  Check Durable OnRoute
            //---------------------------------------
            ServiceException exception = null;
            try {
                durableMethod.durableOnRouteCheck(strObjCommonIn, durableCategory, strStartDurables.get(durableCnt).getDurableId());
            } catch (ServiceException ex) {
                exception = ex;
            }
            if (durableCnt == 0) {
                if (exception != null && !Validations.isEquals(retCodeConfig.getDurableOnroute(), exception.getCode())) {
                    onRouteFlag = false;
                }
                log.info("{} {}", "the first durable OnRoute state",
                        (onRouteFlag ? "TRUE" : "FALSE"),
                        strStartDurables.get(durableCnt).getDurableId().getValue());
            } else {
                if (exception != null && !Validations.isEquals(retCodeConfig.getDurableOnroute(), exception.getCode())) {
                    log.info("{} {}", "durable OnRoute state is FALSE", strStartDurables.get(durableCnt).getDurableId().getValue());
                    tmpOnRouteFlag = false;
                } else {
                    log.info("{} {}", "durable OnRoute state is TRUE", strStartDurables.get(durableCnt).getDurableId().getValue());
                    tmpOnRouteFlag = true;
                }

                if (tmpOnRouteFlag != onRouteFlag) {
                    log.info("{}", "all durable OnRoute state is not same");
                    Validations.check(true, retCodeConfigEx.getDurableOnRouteStatNotSame());
                }
            }
        }

        String portGroupID;
        String onlineMode;
        String accessMode = null;
        ObjectIdentifier operationMode = null;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory == SP_DurableCat_Cassette");
            Outputs.ObjEquipmentPortGroupIDGetOut strEquipmentportGroupIDGetout;
            Inputs.ObjEquipmentPortGroupIDGetIn strEquipmentportGroupIDGetin = new Inputs.ObjEquipmentPortGroupIDGetIn();
            strEquipmentportGroupIDGetin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentportGroupIDGetin.setPortId(strStartDurables.get(0).getStartDurablePort().getUnloadPortID());
            strEquipmentportGroupIDGetout = portMethod.equipmentPortGroupIDGet(
                    strObjCommonIn,
                    strEquipmentportGroupIDGetin);
            portGroupID = strEquipmentportGroupIDGetout.getPortGroupId();

            /*---------------------------------*/
            /*   Get Equipment's Online Mode   */
            /*---------------------------------*/
            Outputs.ObjPortResourceCurrentOperationModeGetOut strPortResourcecurrentOperationModeGetout;
            strPortResourcecurrentOperationModeGetout = portMethod.portResourceCurrentOperationModeGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID(),
                    strStartDurables.get(0).getStartDurablePort().getUnloadPortID());

            onlineMode = strPortResourcecurrentOperationModeGetout.getOperationMode().getOnlineMode();
            accessMode = strPortResourcecurrentOperationModeGetout.getOperationMode().getAccessMode();
            operationMode = strPortResourcecurrentOperationModeGetout.getOperationMode().getOperationMode();
        } else {
            log.info("{}", "durableCategory != SP_DurableCat_Cassette");
            portGroupID = ("");

            String strEquipmentonlineModeGetout;
            strEquipmentonlineModeGetout = equipmentMethod.equipmentOnlineModeGet(
                    strObjCommonIn,
                    strInParm.getEquipmentID());

            onlineMode = strEquipmentonlineModeGetout;

            if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                log.info("{}", "durableCategory == SP_DurableCat_ReticlePod");

                Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut strReticlePodPortResourcecurrentAccessModeGetout;
                strReticlePodPortResourcecurrentAccessModeGetout = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(
                        strObjCommonIn,
                        strInParm.getEquipmentID(),
                        strStartDurables.get(0).getStartDurablePort().getUnloadPortID());

                accessMode = strReticlePodPortResourcecurrentAccessModeGetout.getAccessMode();
            }
        }

        /*---------------------------------------------------*/
        /*                                                   */
        /*   If AccessMode is Auto, do the following check.  */
        /*                                                   */
        /*---------------------------------------------------*/
        log.info("{}", "accessMode = Auto");

        Inputs.ObjDurableCheckConditionForOperationIn strDurableCheckConditionForOperationin = new Inputs.ObjDurableCheckConditionForOperationIn();
        strDurableCheckConditionForOperationin.setOperation(BizConstant.SP_OPERATION_OPESTARTCANCEL);
        strDurableCheckConditionForOperationin.setEquipmentId(strInParm.getEquipmentID());
        strDurableCheckConditionForOperationin.setDurableCategory(durableCategory);
        strDurableCheckConditionForOperationin.setStartDurables(strStartDurables);
        strDurableCheckConditionForOperationin.setDurableStartRecipe(strDurableStartRecipe);
        durableMethod.durableCheckConditionForOperation(
                strObjCommonIn,
                strDurableCheckConditionForOperationin);

        for (durableCnt = 0; durableCnt < durableLen; durableCnt++) {
            log.info("{} {}", "loop to strStartDurables.length()", durableCnt);
            durableMethod.durableStatusCheckForOperation(
                    strObjCommonIn,
                    BizConstant.SP_OPERATION_OPESTARTCANCEL,
                    strStartDurables.get(durableCnt).getDurableId(),
                    durableCategory);
        }

        /*---------------------------------------------------*/
        /*                                                   */
        /*   If AccessMode is Auto, do the following check.  */
        /*                                                   */
        /*---------------------------------------------------*/
        if (CimStringUtils.equals(accessMode, BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
            log.info("{}", "accessMode = Auto");

            Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn strEquipmentandportStateCheckForDurableOperationin = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
            strEquipmentandportStateCheckForDurableOperationin.setOperation(BizConstant.SP_OPERATION_OPESTARTCANCEL);
            strEquipmentandportStateCheckForDurableOperationin.setEquipmentId(strInParm.getEquipmentID());
            strEquipmentandportStateCheckForDurableOperationin.setPortGroupId(portGroupID);
            strEquipmentandportStateCheckForDurableOperationin.setDurableCategory(durableCategory);
            strEquipmentandportStateCheckForDurableOperationin.setStartDurables(strStartDurables);
            equipmentMethod.equipmentAndPortStateCheckForDurableOperation(
                    strObjCommonIn,
                    strEquipmentandportStateCheckForDurableOperationin);
        }

        if (onRouteFlag) {
            log.info("{}", "onRouteFlag == TRUE");
            Infos.DurableProcessStateMakeWaitingIn strDurableprocessStateMakeWaitingin = new Infos.DurableProcessStateMakeWaitingIn();
            strDurableprocessStateMakeWaitingin.setDurableCategory(durableCategory);
            strDurableprocessStateMakeWaitingin.setStrStartDurables(strStartDurables);
            durableMethod.durableProcessStateMakeWaiting(
                    strObjCommonIn,
                    strDurableprocessStateMakeWaitingin);
        }

        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            log.info("{}", "onlineMode == SP_Eqp_OnlineMode_Offline");

            /*-----------------------------------------------*/
            /*   Change Equipment's Status to 'PRODUCTIVE'   */
            /*-----------------------------------------------*/

            /*===== get StateChageableFlag ===*/
            Boolean strEquipmentcurrentStateCheckToManufacturingout;
            strEquipmentcurrentStateCheckToManufacturingout = equipmentMethod.equipmentCurrentStateCheckToManufacturing(
                    strObjCommonIn,
                    strInParm.getEquipmentID());

            if (CimBooleanUtils.isTrue(strEquipmentcurrentStateCheckToManufacturingout)) {
                log.info("{}", "ManufacturingStateChangeableFlag == TRUE");

                /*===== get Defaclt Status Code for Productive / Standby ===*/
                ObjectIdentifier strEquipmentRecoverStateGetManufacturingForDurableout;
                strEquipmentRecoverStateGetManufacturingForDurableout = equipmentForDurableMethod.equipmentRecoverStateGetManufacturingForDurable(
                        strObjCommonIn,
                        BizConstant.SP_OPERATION_OPESTARTCANCEL,
                        strInParm.getEquipmentID(),
                        strInParm.getDurableControlJobID());

                /*---------------------------------*/
                /*   Call txEqpStatusChangeReq()   */
                /*---------------------------------*/
                Results.EqpStatusChangeReqResult strEqpStatusChangeReqResult;
                try {
                    strEqpStatusChangeReqResult = equipmentService.sxEqpStatusChangeReq(
                            strObjCommonIn,
                            strInParm.getEquipmentID(),
                            strEquipmentRecoverStateGetManufacturingForDurableout,
                            claimMemo);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(ex.getCode(), retCodeConfig.getCurrentStateSame())) {
                        throw ex;
                    }
                }
            }
        }

        Infos.ProcessStartDurablesReserveInformationClearIn strProcessstartDurablesReserveInformationClearin = new Infos.ProcessStartDurablesReserveInformationClearIn();
        strProcessstartDurablesReserveInformationClearin.setDurableCategory(durableCategory);
        strProcessstartDurablesReserveInformationClearin.setStrStartDurables(strStartDurables);
        processForDurableMethod.processStartDurablesReserveInformationClear(
                strObjCommonIn,
                strProcessstartDurablesReserveInformationClearin);

        ObjectIdentifier strDurableControlJobManageReqResult;
        Params.DurableControlJobManageReqInParam strDurableControlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        strDurableControlJobManageReqInParam.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableControlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_DELETE);
        Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        strDurableControlJobCreateRequest.setEquipmentID(strInParm.getEquipmentID());
        strDurableControlJobCreateRequest.setDurableCategory(durableCategory);
        strDurableControlJobCreateRequest.setStrStartDurables(strStartDurables);
        strDurableControlJobManageReqInParam.setStrDurableControlJobCreateRequest(strDurableControlJobCreateRequest);
        strDurableControlJobManageReqResult = sxDrbCJStatusChangeReq(
                strObjCommonIn,
                strDurableControlJobManageReqInParam);

        Infos.DurableOperationStartEventMakeOpeStartCancelIn strDurableOperationStartEventMakeOpeStartCancelin = new Infos.DurableOperationStartEventMakeOpeStartCancelIn();
        strDurableOperationStartEventMakeOpeStartCancelin.setEquipmentID(strInParm.getEquipmentID());
        strDurableOperationStartEventMakeOpeStartCancelin.setOperationMode(operationMode.getValue());
        strDurableOperationStartEventMakeOpeStartCancelin.setDurableControlJobID(strInParm.getDurableControlJobID());
        strDurableOperationStartEventMakeOpeStartCancelin.setDurableCategory(durableCategory);
        strDurableOperationStartEventMakeOpeStartCancelin.setStrStartDurables(strStartDurables);
        strDurableOperationStartEventMakeOpeStartCancelin.setClaimMemo(claimMemo);
        eventMethod.durableOperationStartEventMakeOpeStartCancel(
                strObjCommonIn,
                strDurableOperationStartEventMakeOpeStartCancelin);

        /*--------------------------------------------*/
        /*   Send DurableOpeStartReq() to TCS         */
        /*--------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        int sleepTimeValue = 0;
        int retryCountValue = 0;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.intValue(BizConstant.SP_DEFAULT_SLEEP_TIME_TCS);
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpSleepTimeValue)");
            sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            log.info("{}", "0 == CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.intValue(BizConstant.SP_DEFAULT_RETRY_COUNT_TCS);
        } else {
            log.info("{}", "0 != CIMFWStrLen(tmpRetryCountValue)");
            retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
        }

        log.info("{} {}", "env value of OM_EAP_CONNECT_SLEEP_TIME  = ", sleepTimeValue);
        log.info("{} {}", "env value of OM_EAP_CONNECT_RETRY_COUNT = ", retryCountValue);

        Results.DurableOperationStartCancelReqResult strTCSMgr_SendDurableOpeStartCancelReq_out;
        Infos.SendDurableOpeStartCancelReqIn strTCSMgrSendDurableOpeStartCancelReqin = new Infos.SendDurableOpeStartCancelReqIn();
        strTCSMgrSendDurableOpeStartCancelReqin.setStrDurableOperationStartCancelReqInParam(strInParm);
        strTCSMgrSendDurableOpeStartCancelReqin.setClaimMemo(claimMemo);

        //'retryCountValue + 1' means first try plus retry count
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            try {
                tcsMethod.sendDurableOpeStartCancelReq(
                        strObjCommonIn,
                        strTCSMgrSendDurableOpeStartCancelReqin);

                log.info("{}", "Now TCS subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceBindFail())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getExtServiceNilObj())
                        || Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "TCS subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    try {
                        Thread.sleep(sleepTimeValue * 1000L);
                        continue;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error(e.getMessage());
                    }
                } else {
                    log.info("{}", "TCSMgr_SendDurableOpeStartReq() != RC_OK");
                    throw ex;
                }
            }
        }

        // Set Return Structure
        strDurableOperationStartCancelReqResult.setDurableCategory(durableCategory);
        strDurableOperationStartCancelReqResult.setStrStartDurables(strStartDurables);

        // Return to caller
        return strDurableOperationStartCancelReqResult;
    }

    /**
     * This function notifies system that Durable is actual Operation has started on the Equipment.
     * <p> - Input parameter check( durableControlJobID is not empty )
     * <p> - Consistency check of Transaction ID and SpecialControl of Equipment Category
     * <p> - Check condition of durable( PFX is not Nil, Cassette's durableControlJob exists in Eqp, Transfer Status... )
     * <p> - Check status of durable( Should be NotAvailable )
     * <p> - Check equipment and port state for cassette and reticle pod( Input EqpID and durableControlJob's EqpID should match... )
     * <p> - Check cassette category
     * <p> - Change equipment status if needed
     * <p> - Change cassette dispatch reserved status
     * <p> - Make event
     * <p> - Send durable operation start notification request to TCS
     * <p> - Update durable controlJob status to Queued
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 17:02
     */
    @Override
    public Results.DurableOperationStartReqResult sxDrbMoveInForIBReq(Infos.ObjCommon objCommon, Params.DurableOperationStartReqInParam param) {
        Validations.check(null == objCommon || null == param, retCodeConfig.getInvalidInputParam());

        Results.DurableOperationStartReqResult retVal = new Results.DurableOperationStartReqResult();

        String durableCategory = param.getDurableCategory();
        Infos.DurableStartRecipe durableStartRecipe = param.getStrDurableStartRecipe();
        List<Infos.StartDurable> startDurables = param.getStrStartDurables();
        String claimMemo = param.getClaimMemo();
        log.info("in-parm equipmentID          :" + ObjectIdentifier.fetchValue(param.getEquipmentID()));
        log.info("in-parm durableControlJobID  :" + ObjectIdentifier.fetchValue(param.getDurableControlJobID()));
        log.info("in-parm durableCategory      :" + durableCategory);

        if (ObjectIdentifier.isEmpty(param.getDurableControlJobID())) {
            Validations.check(retCodeConfig.getDurableControlJobBlank());
        }

        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        if (CimArrayUtils.isEmpty(startDurables)) {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        //---------------------------------------------------------------------------------
        // Consistency check of Transaction ID and SpecialControl of Equipment Category
        //---------------------------------------------------------------------------------
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(objCommon, param.getEquipmentID());

        // Get required equipment lock mode
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(param.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        assert null != objLockModeOut;

        //  Lock EQP
        long lockMode = objLockModeOut.getLockMode();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(param.getEquipmentID());
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info("lockMode == SP_EQP_LOCK_MODE_WRITE");
            /*--------------------------------------------*/
            /*      Machine Object Lock Process           */
            /*--------------------------------------------*/
            lockMethod.objectLock(objCommon, CimMachine.class, param.getEquipmentID());
        }

        // Lock Carrier
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (CimStringUtils.equals(param.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
                List<String> keys = new ArrayList<>();
                Optional.of(param).flatMap(data ->
                        Optional.ofNullable(data.getStrStartDurables())).ifPresent(list ->
                        list.forEach(durable -> keys.add(durable.getDurableId().getValue())));
                // Lock Cassette Element (Read)
                Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvanceLockIn.setObjectID(param.getEquipmentID());
                objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ));
                objAdvanceLockIn.setKeyList(keys);
                lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
            }
        }

        // Lock Durable Control Job
        lockMethod.objectLock(objCommon, CimDurableControlJob.class, param.getDurableControlJobID());

        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        Optional.of(param).flatMap(data ->
                Optional.ofNullable(data.getStrStartDurables())).ifPresent(list ->
                list.forEach(durable -> durableIDs.add(durable.getDurableId())));
        switch (durableCategory) {
            case BizConstant.SP_DURABLECAT_CASSETTE:
                lockMethod.objectSequenceLock(objCommon, CimCassette.class, durableIDs);
                break;
            case BizConstant.SP_DURABLECAT_RETICLEPOD:
                lockMethod.objectSequenceLock(objCommon, CimReticlePod.class, durableIDs);
                break;
            case BizConstant.SP_DURABLECAT_RETICLE:
                lockMethod.objectSequenceLock(objCommon, CimProcessDurable.class, durableIDs);
                break;
            default:
                // do nothing.
                break;
        }

        //-------------------------------------------
        // Durable Process Flow Control
        //-------------------------------------------
        boolean onRouteFlag = true;
        boolean tmpOnRouteFlag = true;

        //---------------------------------------
        //  Check Durable OnRoute
        //---------------------------------------
        int loop = 0;
        for (Infos.StartDurable startDurable : startDurables) {
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
            } catch (ServiceException e) {
                if (loop == 0) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                        onRouteFlag = false;
                    }
                } else {
                    tmpOnRouteFlag = Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute());
                    if (tmpOnRouteFlag != onRouteFlag) {
                        log.error("all durable OnRoute state is not same");
                        Validations.check(retCodeConfigEx.getDurableOnRouteStatNotSame());
                    }
                }
            }
            loop++;
        }

        if (onRouteFlag) {
            // todo:ZQI
            // txRunDurableBRScriptReq
        }

        String portGroupID = null;
        String onlineMode;
        ObjectIdentifier operationMode = null;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            ObjectIdentifier loadPortID = startDurables.get(0).getStartDurablePort().getLoadPortID();
            Inputs.ObjEquipmentPortGroupIDGetIn objEquipmentPortGroupIDGetIn = new Inputs.ObjEquipmentPortGroupIDGetIn();
            objEquipmentPortGroupIDGetIn.setEquipmentId(param.getEquipmentID());
            objEquipmentPortGroupIDGetIn.setPortId(loadPortID);
            Outputs.ObjEquipmentPortGroupIDGetOut groupIDGetOut = portMethod.equipmentPortGroupIDGet(objCommon, objEquipmentPortGroupIDGetIn);
            portGroupID = groupIDGetOut.getPortGroupId();

            /*---------------------------------*/
            /*   Get Equipment's Online Mode   */
            /*---------------------------------*/
            Outputs.ObjPortResourceCurrentOperationModeGetOut operationModeGetOut = portMethod.portResourceCurrentOperationModeGet(objCommon, param.getEquipmentID(), loadPortID);
            onlineMode = operationModeGetOut.getOperationMode().getOnlineMode();
            operationMode = operationModeGetOut.getOperationMode().getOperationMode();
        } else {
            log.info("durableCategory != SP_DurableCat_Cassette");
            onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, param.getEquipmentID());
        }

        //-----------------------------------------
        // Call durable_CheckConditionForOperation
        //-----------------------------------------
        Inputs.ObjDurableCheckConditionForOperationIn conditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
        conditionForOperationIn.setOperation(BizConstant.SP_OPERATION_OPESTART);
        conditionForOperationIn.setEquipmentId(param.getEquipmentID());
        conditionForOperationIn.setDurableCategory(durableCategory);
        conditionForOperationIn.setStartDurables(startDurables);
        conditionForOperationIn.setDurableStartRecipe(durableStartRecipe);
        durableMethod.durableCheckConditionForOperationForInternalBuffer(objCommon, conditionForOperationIn);

        for (Infos.StartDurable startDurable : startDurables) {
            // durable_status_CheckForOperation
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATION_OPESTART, startDurable.getDurableId(), durableCategory);
        }

        //----------------------------------------------------------
        // call equipment_and_portState_CheckForDurableOperation
        //----------------------------------------------------------
        Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn checkForDurableOperationIn = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
        checkForDurableOperationIn.setOperation(BizConstant.SP_OPERATION_OPESTART);
        checkForDurableOperationIn.setEquipmentId(param.getEquipmentID());
        checkForDurableOperationIn.setPortGroupId(portGroupID);
        checkForDurableOperationIn.setDurableCategory(durableCategory);
        checkForDurableOperationIn.setStartDurables(startDurables);
        equipmentMethod.equipmentAndPortStateCheckForDurableOperationForInternalBuffer(objCommon, checkForDurableOperationIn);

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            for (Infos.StartDurable startDurable : startDurables) {
                durableMethod.durableCassetteCategoryCheckForContaminationControl(objCommon, startDurable.getDurableId(), param.getEquipmentID(), startDurable.getStartDurablePort().getLoadPortID());
            }
        }

        //------------------------------------------------------------
        // call process_startDurablesReserveInformation_Set
        //------------------------------------------------------------
        Inputs.ProcessStartDurablesReserveInformationSetIn reserveInformationSetIn = new Inputs.ProcessStartDurablesReserveInformationSetIn();
        reserveInformationSetIn.setEquipmentID(param.getEquipmentID());
        reserveInformationSetIn.setPortGroupID(portGroupID);
        reserveInformationSetIn.setDurableControlJobID(param.getDurableControlJobID());
        reserveInformationSetIn.setDurableCategory(durableCategory);
        reserveInformationSetIn.setStrStartDurables(startDurables);
        reserveInformationSetIn.setStrDurableStartRecipe(durableStartRecipe);
        processForDurableMethod.processStartDurablesReserveInformationSet(objCommon, reserveInformationSetIn);


        /*-----------------------------------------------*/
        /*   Change Equipment's Status to 'PRODUCTIVE'   */
        /*-----------------------------------------------*/
        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            log.info("onlineMode == SP_Eqp_OnlineMode_Offline");

            /*===== get StateChageableFlag ===*/
            Boolean manufacturingStateChangeableFlag = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon, param.getEquipmentID());

            if (manufacturingStateChangeableFlag) {
                log.info("ManufacturingStateChangeableFlag == TRUE");
                /*===== get Defaclt Status Code for Productive / Standby ===*/
                // call equipment_recoverState_GetManufacturingForDurable
                ObjectIdentifier equipmentStatusCode = equipmentForDurableMethod.equipmentRecoverStateGetManufacturingForDurable(objCommon,
                        BizConstant.SP_OPERATION_OPESTART,
                        param.getEquipmentID(),
                        param.getDurableControlJobID());

                /*---------------------------------*/
                /*   Call txEqpStatusChangeReq()   */
                /*---------------------------------*/
                try {
                    equipmentService.sxEqpStatusChangeReq(objCommon, param.getEquipmentID(), equipmentStatusCode, claimMemo);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getCurrentStateSame())) {
                        throw e;
                    }
                }
            }
        }

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            Optional.of(startDurables).ifPresent(list -> list.forEach(data -> cassetteMethod.cassetteDispatchStateChange(objCommon, data.getDurableId(), false)));
        }

        if (onRouteFlag) {
            durableMethod.durableProcessStateMakeProcessing(objCommon, durableCategory, startDurables);
        }

        //---------------------------
        // Make Event
        //---------------------------
        Infos.DurableOperationStartEventMakeOpeStartIn eventMakeOpeStartIn = new Infos.DurableOperationStartEventMakeOpeStartIn();
        eventMakeOpeStartIn.setEquipmentID(param.getEquipmentID());
        eventMakeOpeStartIn.setOperationMode(ObjectIdentifier.fetchValue(operationMode));
        eventMakeOpeStartIn.setDurableControlJobID(param.getDurableControlJobID());
        eventMakeOpeStartIn.setDurableCategory(durableCategory);
        eventMakeOpeStartIn.setStrStartDurables(startDurables);
        eventMakeOpeStartIn.setStrDurableStartRecipe(durableStartRecipe);
        eventMakeOpeStartIn.setClaimMemo(claimMemo);
        eventMethod.durableOperationStartEventMakeOpeStart(objCommon, eventMakeOpeStartIn);

        /*-----------------------------------------------*/
        /*   Send DurableOpeStartReq() to TCS            */
        /*-----------------------------------------------*/
//        Inputs.SendDurableOpeStartReqIn durableOpeStartReqIn = new Inputs.SendDurableOpeStartReqIn();
//        durableOpeStartReqIn.setStrDurableOperationStartReqInParam(param);
//        tcsMethod.sendTCSReq(TCSReqEnum.sendDurableMoveInReq, durableOpeStartReqIn);

        /*----------------------------------------------------*/
        /*    Durable ControlJob Manage TYPE_QUEUE            */
        /*----------------------------------------------------*/
        Params.DurableControlJobManageReqInParam controlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        controlJobManageReqInParam.setDurableControlJobID(param.getDurableControlJobID());
        controlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_QUEUE);

        Infos.DurableControlJobCreateRequest durableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        durableControlJobCreateRequest.setEquipmentID(param.getEquipmentID());
        durableControlJobCreateRequest.setDurableCategory(param.getDurableCategory());
        durableControlJobCreateRequest.setStrStartDurables(startDurables);
        controlJobManageReqInParam.setStrDurableControlJobCreateRequest(durableControlJobCreateRequest);
        controlJobManageReqInParam.setClaimMemo(param.getClaimMemo());
        controlJobManageReqInParam.setUser(param.getUser());
        sxDrbCJStatusChangeReq(objCommon, controlJobManageReqInParam);

        /*-----------------------------------------------*/
        /*    Set Return Structure                       */
        /*-----------------------------------------------*/
        retVal.setDurableControlJobID(param.getDurableControlJobID());
        retVal.setDurableCategory(durableCategory);
        retVal.setStrStartDurables(startDurables);
        retVal.setStrDurableStartRecipe(durableStartRecipe);
        return retVal;
    }


    /**
     * This function cancels the Operation Start for the started Durables.
     * <p>- Input parameter check( durableControlJobID is not empty )
     * <p>- Consistency check of Transaction ID and SpecialControl of Equipment Category
     * <p>- Get startDurablesReserve information
     * <p>- Check condition of durable( PFX is not Nil, Transfer Status... )
     * <p>- Check status of durable
     * <p>- Check equipment and port state for cassette and reticle pod( Input EqpID and durableControlJob's EqpID should match )
     * <p>- Set the value of ActualCompTimeStamp for actual comp information
     * <p>- Move process operation of Durable
     * <p>- Change equipment status if needed
     * <p>- Equipment Usage Limitation Check
     * <p>- Delete durable controlJob
     * <p>- Make event
     * <p>- Send durable operation completion notification request to TCS
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/28 15:35
     */
    @Override
    public Results.DurableOpeCompReqResult sxDrbMoveOutForIBReq(Infos.ObjCommon objCommon, Params.DurableOpeCompReqInParam paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        log.info("in-parm equipmentID          :" + ObjectIdentifier.fetchValue(paramIn.getEquipmentID()));
        log.info("in-parm durableControlJobID  :" + ObjectIdentifier.fetchValue(paramIn.getDurableControlJobID()));

        Results.DurableOpeCompReqResult retVal = new Results.DurableOpeCompReqResult();

        /*-----------------------------------------------*/
        /* Input parameter check                         */
        /*-----------------------------------------------*/
        Validations.check(ObjectIdentifier.isEmptyWithValue(paramIn.getDurableControlJobID()), retCodeConfig.getDurableControlJobBlank());

        /*------------------------------------------------------------------------------*/
        /* Consistency check of Transaction ID and SpecialControl of Equipment Category */
        /*------------------------------------------------------------------------------*/
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(objCommon, paramIn.getEquipmentID());

        /*----------------*/
        /* Object Lock    */
        /*----------------*/
        // Get required equipment lock mode
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(paramIn.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        long lockMode = objLockModeOut.getLockMode();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(paramIn.getEquipmentID());
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            lockMethod.objectLock(objCommon, CimMachine.class, paramIn.getEquipmentID());
        }

        /*------------------------------------------------------*/
        /* Get StartDurablesReserve information                 */
        /*------------------------------------------------------*/
        Outputs.DurableControlJobStartReserveInformationGetOut reserveInformationGetOut = durableControlJobMethod.durableControlJobStartReserveInformationGet(objCommon, paramIn.getDurableControlJobID());
        assert null != reserveInformationGetOut;
        String durableCategory = reserveInformationGetOut.getDurableCategory();

        if (!CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            log.error("Invalid durable category : " + durableCategory);
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        // Lock DurableControlJob
        lockMethod.objectLock(objCommon, CimDurableControlJob.class, paramIn.getDurableControlJobID());

        List<Infos.StartDurable> startDurables = reserveInformationGetOut.getStrStartDurables();
        assert null != startDurables;
        Infos.DurableStartRecipe durableStartRecipe = reserveInformationGetOut.getStrDurableStartRecipe();
        assert null != durableStartRecipe;

        // Lock Durable
        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        Optional.of(startDurables).ifPresent(list -> list.forEach(data -> durableIDs.add(data.getDurableId())));
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                lockMethod.objectSequenceLock(objCommon, CimCassette.class, durableIDs);
                break;
            case SP_DURABLECAT_RETICLEPOD:
                lockMethod.objectSequenceLock(objCommon, CimReticlePod.class, durableIDs);
                break;
            case SP_DURABLECAT_RETICLE:
                lockMethod.objectSequenceLock(objCommon, CimProcessDurable.class, durableIDs);
                break;
        }

        /*---------------------------------------*/
        /*  Check Durable OnRoute                */
        /*---------------------------------------*/
        boolean onRouteFlag = true;
        int loop = 0;
        for (Infos.StartDurable startDurable : startDurables) {
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
            } catch (ServiceException e) {
                if (loop == 0) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                        onRouteFlag = false;
                    }
                } else {
                    boolean tmpOnRouteFlag = Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute());
                    if (tmpOnRouteFlag != onRouteFlag) {
                        log.error("all durable OnRoute state is not same");
                        Validations.check(retCodeConfigEx.getDurableOnRouteStatNotSame());
                    }
                }
            }
            loop++;
        }

        /*-------------------------------------------------------*/
        /* Get Equipment's PortGroup\Online Mode\Access Mode     */
        /*-------------------------------------------------------*/
        String portGroupID = null;
        String onlineMode = null;
        String accessMode = null;
        ObjectIdentifier operationMode = null;
        ObjectIdentifier loadPortID = startDurables.get(0).getStartDurablePort().getLoadPortID();
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            Inputs.ObjEquipmentPortGroupIDGetIn portGroupIDGetIn = new Inputs.ObjEquipmentPortGroupIDGetIn();
            portGroupIDGetIn.setEquipmentId(paramIn.getEquipmentID());
            portGroupIDGetIn.setPortId(loadPortID);
            Outputs.ObjEquipmentPortGroupIDGetOut portGroupIDGetOut = portMethod.equipmentPortGroupIDGet(objCommon, portGroupIDGetIn);

            portGroupID = portGroupIDGetOut.getPortGroupId();

            Outputs.ObjPortResourceCurrentOperationModeGetOut currentOperationModeGetOut = portMethod.portResourceCurrentOperationModeGet(objCommon, paramIn.getEquipmentID(), loadPortID);
            Infos.OperationMode operationModeGet = currentOperationModeGetOut.getOperationMode();
            onlineMode = operationModeGet.getOnlineMode();
            accessMode = operationModeGet.getAccessMode();
            operationMode = operationModeGet.getOperationMode();
        } else {
            log.info("durableCategory != SP_DurableCat_Cassette");
            onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, paramIn.getEquipmentID());

            if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut modeGetOut = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(objCommon, paramIn.getEquipmentID(), loadPortID);
                accessMode = modeGetOut.getAccessMode();
            }
        }

        /*---------------------------------------------*/
        /* Check Condition For Operation               */
        /*---------------------------------------------*/
        Inputs.ObjDurableCheckConditionForOperationIn checkConditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
        checkConditionForOperationIn.setOperation(BizConstant.SP_OPERATION_OPERATIONCOMP);
        checkConditionForOperationIn.setEquipmentId(paramIn.getEquipmentID());
        checkConditionForOperationIn.setPortGroupId(portGroupID);
        checkConditionForOperationIn.setDurableCategory(durableCategory);
        checkConditionForOperationIn.setStartDurables(startDurables);
        checkConditionForOperationIn.setDurableStartRecipe(durableStartRecipe);
        durableMethod.durableCheckConditionForOperationForInternalBuffer(objCommon, checkConditionForOperationIn);

        /*---------------------------------------------------*/
        /* Durable Status Check For Operation                */
        /*---------------------------------------------------*/
        for (ObjectIdentifier durableID : durableIDs) {
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATION_OPERATIONCOMP, durableID, durableCategory);
        }

        /*----------------------------------------------------------------------------*/
        /* If AccessMode is Auto or OnlineMode is Offline, do the following check.    */
        /*----------------------------------------------------------------------------*/
        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                || CimStringUtils.equals(accessMode, BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
            log.info(String.format("EQP[%s] onlineMode = %s or accessMode = %s", paramIn.getEquipmentID().getValue(), onlineMode, accessMode));
            Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn equipmentAndPortStateCheckForDurableOperationIn = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
            equipmentAndPortStateCheckForDurableOperationIn.setOperation(BizConstant.SP_OPERATION_OPERATIONCOMP);
            equipmentAndPortStateCheckForDurableOperationIn.setEquipmentId(paramIn.getEquipmentID());
            equipmentAndPortStateCheckForDurableOperationIn.setPortGroupId(portGroupID);
            equipmentAndPortStateCheckForDurableOperationIn.setDurableCategory(durableCategory);
            equipmentAndPortStateCheckForDurableOperationIn.setStartDurables(startDurables);
            equipmentMethod.equipmentAndPortStateCheckForDurableOperationForInternalBuffer(objCommon, equipmentAndPortStateCheckForDurableOperationIn);
        }

        /*----------------------------------------------------------------------*/
        /* Set the value of ActualCompTimeStamp for actual comp information     */
        /*----------------------------------------------------------------------*/
        //  call durableProcess_actualCompInformation_Set
        processForDurableMethod.durableProcessActualCompInformationSet(objCommon, durableCategory, startDurables);

        /*----------------------------------------------------------------------*/
        /* Set the Durable Process State to "Waiting"                           */
        /*----------------------------------------------------------------------*/
        if (onRouteFlag) {
            log.info("onRouteFlag == TRUE");
            Infos.DurableProcessStateMakeWaitingIn processStateMakeWaitingIn = new Infos.DurableProcessStateMakeWaitingIn();
            processStateMakeWaitingIn.setDurableCategory(durableCategory);
            processStateMakeWaitingIn.setStrStartDurables(startDurables);
            durableMethod.durableProcessStateMakeWaiting(objCommon, processStateMakeWaitingIn);
        }

        /*-----------------------------------------*/
        /* Move process operation of Durable       */
        /*-----------------------------------------*/
        for (Infos.StartDurable startDurable : startDurables) {
            try {
                processForDurableMethod.durableProcessMove(objCommon, durableCategory, startDurable.getDurableId());
            } catch (ServiceException e) {
                if (Validations.isEquals(e.getCode(), retCodeConfig.getAddToQueueFail())) {
                    Outputs.ObjDurableCurrentOperationInfoGetOut operationInfoGetOut = durableMethod.durableCurrentOperationInfoGet(objCommon, durableCategory, startDurable.getDurableId());

                    List<Infos.DurableHoldList> durableHoldLists = new ArrayList<>();
                    durableHoldLists.add(new Infos.DurableHoldList());
                    durableHoldLists.get(0).setHoldType(BizConstant.SP_HOLDTYPE_ADDTOQUEUEERRHOLD);
                    durableHoldLists.get(0).setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_ADDTOQUEUEERRHOLD));
                    durableHoldLists.get(0).setHoldUserID(objCommon.getUser().getUserID());
                    durableHoldLists.get(0).setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    durableHoldLists.get(0).setRouteID(operationInfoGetOut.getRouteID());
                    durableHoldLists.get(0).setOperationNumber(operationInfoGetOut.getOperationNumber());

                    Params.HoldDurableReqInParam holdDurableReqInParam = new Params.HoldDurableReqInParam();
                    holdDurableReqInParam.setDurableCategory(durableCategory);
                    holdDurableReqInParam.setDurableID(startDurable.getDurableId());
                    holdDurableReqInParam.setDurableHoldLists(durableHoldLists);
                    sxHoldDrbReq(objCommon, holdDurableReqInParam, paramIn.getClaimMemo());
                }
            }
        }

        /*-----------------------------------------------*/
        /* Change Equipment's Status to 'PRODUCTIVE'     */
        /*-----------------------------------------------*/
        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {

            /*===== get StateChageableFlag ===*/
            Boolean manufacturingStateChangeableFlag = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon, paramIn.getEquipmentID());
            if (manufacturingStateChangeableFlag) {
                log.info("ManufacturingStateChangeableFlag == TRUE");
                /*===== get Defaclt Status Code for Productive / Standby ===*/
                // call equipment_recoverState_GetManufacturingForDurable
                ObjectIdentifier equipmentStatusCode = equipmentForDurableMethod.equipmentRecoverStateGetManufacturingForDurable(objCommon,
                        SP_OPERATION_OPERATIONCOMP,
                        paramIn.getEquipmentID(),
                        paramIn.getDurableControlJobID());

                /*---------------------------------*/
                /*   Call txEqpStatusChangeReq()   */
                /*---------------------------------*/
                try {
                    equipmentService.sxEqpStatusChangeReq(objCommon, paramIn.getEquipmentID(),
                            equipmentStatusCode, paramIn.getClaimMemo());
                } catch (ServiceException e) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getCurrentStateSame())) {
                        throw e;
                    }
                }
            }
        }

        /*--------------------------------------*/
        /* Equipment Usage Limitation Check     */
        /*--------------------------------------*/
        Outputs.ObjEquipmentUsageLimitationCheckOut usageLimitationCheckOut = equipmentMethod.equipmentUsageLimitationCheck(objCommon, paramIn.getEquipmentID());

        if (usageLimitationCheckOut.isUsageLimitOverFlag()) {
            /*-------------------------*/
            /*   Call System Message   */
            /*-------------------------*/
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EQPUSAGELIMITOVER);
            alertMessageRptParams.setSystemMessageText(usageLimitationCheckOut.getMessageText());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(paramIn.getEquipmentID());
            alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }


        /*---------------------------------------------------------*/
        /* Delete the Durable Control Job                          */
        /*---------------------------------------------------------*/
        Params.DurableControlJobManageReqInParam controlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        controlJobManageReqInParam.setDurableControlJobID(paramIn.getDurableControlJobID());
        controlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_DELETE);

        Infos.DurableControlJobCreateRequest durableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        durableControlJobCreateRequest.setEquipmentID(paramIn.getEquipmentID());
        durableControlJobCreateRequest.setDurableCategory(durableCategory);
        durableControlJobCreateRequest.setStrStartDurables(startDurables);
        controlJobManageReqInParam.setStrDurableControlJobCreateRequest(durableControlJobCreateRequest);
        controlJobManageReqInParam.setUser(paramIn.getUser());
        controlJobManageReqInParam.setClaimMemo(paramIn.getClaimMemo());
        sxDrbCJStatusChangeReq(objCommon, controlJobManageReqInParam);


        /*---------------------------------*/
        /* Make Event                      */
        /*---------------------------------*/
        Infos.DurableOperationCompleteEventMakeOpeCompIn completeEventMakeOpeCompIn = new Infos.DurableOperationCompleteEventMakeOpeCompIn();
        completeEventMakeOpeCompIn.setEquipmentID(paramIn.getEquipmentID());
        completeEventMakeOpeCompIn.setOperationMode(ObjectIdentifier.fetchValue(operationMode));
        completeEventMakeOpeCompIn.setDurableControlJobID(paramIn.getDurableControlJobID());
        completeEventMakeOpeCompIn.setDurableCategory(durableCategory);
        completeEventMakeOpeCompIn.setStrStartDurables(startDurables);
        completeEventMakeOpeCompIn.setStrDurableStartRecipe(durableStartRecipe);
        completeEventMakeOpeCompIn.setClaimMemo(paramIn.getClaimMemo());
        eventMethod.durableOperationCompleteEventMakeOpeComp(objCommon, completeEventMakeOpeCompIn);

        /*-------------------------------*/
        /* Durable PCS                   */
        /*-------------------------------*/
        if (onRouteFlag) {
            // todo:ZQI txRunDurableBRScriptReq
        }


        /*--------------------------------------------*/
        /*   Send DurableOpeCompReq() to TCS          */
        /*--------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue = CimStringUtils.isEmpty(tmpSleepTimeValue) ? BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : Long.parseLong(tmpSleepTimeValue);
        long retryCountValue = CimStringUtils.isEmpty(tmpRetryCountValue) ? BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : Long.parseLong(tmpRetryCountValue);
        // todo:ZQI TCSMgr_SendDurableOpeCompReq

        // Set Return Structure
        retVal.setDurableCategory(durableCategory);
        retVal.setStrStartDurables(startDurables);
        return retVal;
    }

    /**
     * This function cancels the Operation Start for the started Durables.
     * <p>- Input parameter check( durableControlJobID is not empty )
     * <p>- Consistency check of Transaction ID and SpecialControl of Equipment Category
     * <p>- Get startDurablesReserve information
     * <p>- Check condition of durable( PFX is not Nil, Transfer Status... )
     * <p>- Check status of durable
     * <p>- Check equipment and port state for cassette and reticle pod( Input EqpID and durableControlJob's EqpID should match )
     * <p>- Set the value of ActualCompTimeStamp for actual comp information
     * <p>- Move process operation of Durable
     * <p>- Change equipment status if needed
     * <p>- Equipment Usage Limitation Check
     * <p>- Delete durable controlJob
     * <p>- Make event
     * <p>- Send durable operation completion notification request to TCS
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/28 15:35
     */
    @Override
    public Results.DurableOpeCompReqResult sxDrbMoveOutReq(Infos.ObjCommon objCommon, Params.DurableOpeCompReqInParam paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        log.info("in-parm equipmentID          :" + ObjectIdentifier.fetchValue(paramIn.getEquipmentID()));
        log.info("in-parm durableControlJobID  :" + ObjectIdentifier.fetchValue(paramIn.getDurableControlJobID()));

        Results.DurableOpeCompReqResult retVal = new Results.DurableOpeCompReqResult();

        /*-----------------------------------------------*/
        /* Input parameter check                         */
        /*-----------------------------------------------*/
        Validations.check(ObjectIdentifier.isEmptyWithValue(paramIn.getDurableControlJobID()), retCodeConfig.getDurableControlJobBlank());

        /*------------------------------------------------------------------------------*/
        /* Consistency check of Transaction ID and SpecialControl of Equipment Category */
        /*------------------------------------------------------------------------------*/
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(objCommon, paramIn.getEquipmentID());

        /*----------------*/
        /* Object Lock    */
        /*----------------*/
        // Get required equipment lock mode
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(paramIn.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        long lockMode = objLockModeOut.getLockMode();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(paramIn.getEquipmentID());
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            lockMethod.objectLock(objCommon, CimMachine.class, paramIn.getEquipmentID());
        }

        /*------------------------------------------------------*/
        /* Get StartDurablesReserve information                 */
        /*------------------------------------------------------*/
        Outputs.DurableControlJobStartReserveInformationGetOut reserveInformationGetOut = durableControlJobMethod.durableControlJobStartReserveInformationGet(objCommon, paramIn.getDurableControlJobID());
        assert null != reserveInformationGetOut;
        String durableCategory = reserveInformationGetOut.getDurableCategory();

        if (!CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            log.error("Invalid durable category : " + durableCategory);
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        // Lock DurableControlJob
        lockMethod.objectLock(objCommon, CimDurableControlJob.class, paramIn.getDurableControlJobID());

        List<Infos.StartDurable> startDurables = reserveInformationGetOut.getStrStartDurables();
        assert null != startDurables;
        Infos.DurableStartRecipe durableStartRecipe = reserveInformationGetOut.getStrDurableStartRecipe();
        assert null != durableStartRecipe;

        // Lock Durable
        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        Optional.of(startDurables).ifPresent(list -> list.forEach(data -> durableIDs.add(data.getDurableId())));
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                lockMethod.objectSequenceLock(objCommon, CimCassette.class, durableIDs);
                break;
            case SP_DURABLECAT_RETICLEPOD:
                lockMethod.objectSequenceLock(objCommon, CimReticlePod.class, durableIDs);
                break;
            case SP_DURABLECAT_RETICLE:
                lockMethod.objectSequenceLock(objCommon, CimProcessDurable.class, durableIDs);
                break;
        }

        /*---------------------------------------*/
        /*  Check Durable OnRoute                */
        /*---------------------------------------*/
        boolean onRouteFlag = true;
        int loop = 0;
        for (Infos.StartDurable startDurable : startDurables) {
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
            } catch (ServiceException e) {
                if (loop == 0) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                        onRouteFlag = false;
                    }
                } else {
                    boolean tmpOnRouteFlag = Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute());
                    if (tmpOnRouteFlag != onRouteFlag) {
                        log.error("all durable OnRoute state is not same");
                        Validations.check(retCodeConfigEx.getDurableOnRouteStatNotSame());
                    }
                }
            }
            loop++;
        }

        /*-------------------------------------------------------*/
        /* Get Equipment's PortGroup\Online Mode\Access Mode     */
        /*-------------------------------------------------------*/
        String portGroupID = null;
        String onlineMode = null;
        String accessMode = null;
        ObjectIdentifier operationMode = null;
        ObjectIdentifier loadPortID = startDurables.get(0).getStartDurablePort().getLoadPortID();
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            Inputs.ObjEquipmentPortGroupIDGetIn portGroupIDGetIn = new Inputs.ObjEquipmentPortGroupIDGetIn();
            portGroupIDGetIn.setEquipmentId(paramIn.getEquipmentID());
            portGroupIDGetIn.setPortId(loadPortID);
            Outputs.ObjEquipmentPortGroupIDGetOut portGroupIDGetOut = portMethod.equipmentPortGroupIDGet(objCommon, portGroupIDGetIn);

            portGroupID = portGroupIDGetOut.getPortGroupId();

            Outputs.ObjPortResourceCurrentOperationModeGetOut currentOperationModeGetOut = portMethod.portResourceCurrentOperationModeGet(objCommon, paramIn.getEquipmentID(), loadPortID);
            Infos.OperationMode operationModeGet = currentOperationModeGetOut.getOperationMode();
            onlineMode = operationModeGet.getOnlineMode();
            accessMode = operationModeGet.getAccessMode();
            operationMode = operationModeGet.getOperationMode();
        } else {
            log.info("durableCategory != SP_DurableCat_Cassette");
            onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, paramIn.getEquipmentID());

            if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut modeGetOut = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(objCommon, paramIn.getEquipmentID(), loadPortID);
                accessMode = modeGetOut.getAccessMode();
            }
        }

        /*---------------------------------------------*/
        /* Check Condition For Operation               */
        /*---------------------------------------------*/
        Inputs.ObjDurableCheckConditionForOperationIn checkConditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
        checkConditionForOperationIn.setOperation(BizConstant.SP_OPERATION_OPERATIONCOMP);
        checkConditionForOperationIn.setEquipmentId(paramIn.getEquipmentID());
        checkConditionForOperationIn.setPortGroupId(portGroupID);
        checkConditionForOperationIn.setDurableCategory(durableCategory);
        checkConditionForOperationIn.setStartDurables(startDurables);
        checkConditionForOperationIn.setDurableStartRecipe(durableStartRecipe);
        durableMethod.durableCheckConditionForOperation(objCommon, checkConditionForOperationIn);

        /*---------------------------------------------------*/
        /* Durable Status Check For Operation                */
        /*---------------------------------------------------*/
        for (ObjectIdentifier durableID : durableIDs) {
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATION_OPERATIONCOMP, durableID, durableCategory);
        }

        /*----------------------------------------------------------------------------*/
        /* If AccessMode is Auto or OnlineMode is Offline, do the following check.    */
        /*----------------------------------------------------------------------------*/
        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                || CimStringUtils.equals(accessMode, BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
            log.info(String.format("EQP[%s] onlineMode = %s or accessMode = %s", paramIn.getEquipmentID().getValue(), onlineMode, accessMode));
            Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn equipmentAndPortStateCheckForDurableOperationIn = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
            equipmentAndPortStateCheckForDurableOperationIn.setOperation(BizConstant.SP_OPERATION_OPERATIONCOMP);
            equipmentAndPortStateCheckForDurableOperationIn.setEquipmentId(paramIn.getEquipmentID());
            equipmentAndPortStateCheckForDurableOperationIn.setPortGroupId(portGroupID);
            equipmentAndPortStateCheckForDurableOperationIn.setDurableCategory(durableCategory);
            equipmentAndPortStateCheckForDurableOperationIn.setStartDurables(startDurables);
            equipmentMethod.equipmentAndPortStateCheckForDurableOperation(objCommon, equipmentAndPortStateCheckForDurableOperationIn);
        }

        /*----------------------------------------------------------------------*/
        /* Set the value of ActualCompTimeStamp for actual comp information     */
        /*----------------------------------------------------------------------*/
        //  call durableProcess_actualCompInformation_Set
        processForDurableMethod.durableProcessActualCompInformationSet(objCommon, durableCategory, startDurables);

        /*----------------------------------------------------------------------*/
        /* Set the Durable Process State to "Waiting"                           */
        /*----------------------------------------------------------------------*/
        if (onRouteFlag) {
            log.info("onRouteFlag == TRUE");
            Infos.DurableProcessStateMakeWaitingIn processStateMakeWaitingIn = new Infos.DurableProcessStateMakeWaitingIn();
            processStateMakeWaitingIn.setDurableCategory(durableCategory);
            processStateMakeWaitingIn.setStrStartDurables(startDurables);
            durableMethod.durableProcessStateMakeWaiting(objCommon, processStateMakeWaitingIn);
        }

        /*-----------------------------------------*/
        /* Move process operation of Durable       */
        /*-----------------------------------------*/
        for (Infos.StartDurable startDurable : startDurables) {
            try {
                processForDurableMethod.durableProcessMove(objCommon, durableCategory, startDurable.getDurableId());
            } catch (ServiceException e) {
                if (Validations.isEquals(e.getCode(), retCodeConfig.getAddToQueueFail())) {
                    Outputs.ObjDurableCurrentOperationInfoGetOut operationInfoGetOut = durableMethod.durableCurrentOperationInfoGet(objCommon, durableCategory, startDurable.getDurableId());

                    List<Infos.DurableHoldList> durableHoldLists = new ArrayList<>();
                    durableHoldLists.add(new Infos.DurableHoldList());
                    durableHoldLists.get(0).setHoldType(BizConstant.SP_HOLDTYPE_ADDTOQUEUEERRHOLD);
                    durableHoldLists.get(0).setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_ADDTOQUEUEERRHOLD));
                    durableHoldLists.get(0).setHoldUserID(objCommon.getUser().getUserID());
                    durableHoldLists.get(0).setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    durableHoldLists.get(0).setRouteID(operationInfoGetOut.getRouteID());
                    durableHoldLists.get(0).setOperationNumber(operationInfoGetOut.getOperationNumber());

                    Params.HoldDurableReqInParam holdDurableReqInParam = new Params.HoldDurableReqInParam();
                    holdDurableReqInParam.setDurableCategory(durableCategory);
                    holdDurableReqInParam.setDurableID(startDurable.getDurableId());
                    holdDurableReqInParam.setDurableHoldLists(durableHoldLists);
                    sxHoldDrbReq(objCommon, holdDurableReqInParam, paramIn.getClaimMemo());
                }
            }
        }

        /*-----------------------------------------------*/
        /* Change Equipment's Status to 'PRODUCTIVE'     */
        /*-----------------------------------------------*/
        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {

            /*===== get StateChageableFlag ===*/
            Boolean manufacturingStateChangeableFlag = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon, paramIn.getEquipmentID());
            if (manufacturingStateChangeableFlag) {
                log.info("ManufacturingStateChangeableFlag == TRUE");
                /*===== get Defaclt Status Code for Productive / Standby ===*/
                // call equipment_recoverState_GetManufacturingForDurable
                ObjectIdentifier equipmentStatusCode = equipmentForDurableMethod.equipmentRecoverStateGetManufacturingForDurable(objCommon,
                        SP_OPERATION_OPERATIONCOMP,
                        paramIn.getEquipmentID(),
                        paramIn.getDurableControlJobID());

                /*---------------------------------*/
                /*   Call txEqpStatusChangeReq()   */
                /*---------------------------------*/
                try {
                    equipmentService.sxEqpStatusChangeReq(objCommon, paramIn.getEquipmentID(),
                            equipmentStatusCode, paramIn.getClaimMemo());
                } catch (ServiceException e) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getCurrentStateSame())) {
                        throw e;
                    }
                }
            }
        }

        /*--------------------------------------*/
        /* Equipment Usage Limitation Check     */
        /*--------------------------------------*/
        Outputs.ObjEquipmentUsageLimitationCheckOut usageLimitationCheckOut = equipmentMethod.equipmentUsageLimitationCheck(objCommon, paramIn.getEquipmentID());

        if (usageLimitationCheckOut.isUsageLimitOverFlag()) {
            /*-------------------------*/
            /*   Call System Message   */
            /*-------------------------*/
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_EQPUSAGELIMITOVER);
            alertMessageRptParams.setSystemMessageText(usageLimitationCheckOut.getMessageText());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(paramIn.getEquipmentID());
            alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }


        /*---------------------------------------------------------*/
        /* Delete the Durable Control Job                          */
        /*---------------------------------------------------------*/
        Params.DurableControlJobManageReqInParam controlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        controlJobManageReqInParam.setDurableControlJobID(paramIn.getDurableControlJobID());
        controlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_DELETE);

        Infos.DurableControlJobCreateRequest durableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        durableControlJobCreateRequest.setEquipmentID(paramIn.getEquipmentID());
        durableControlJobCreateRequest.setDurableCategory(durableCategory);
        durableControlJobCreateRequest.setStrStartDurables(startDurables);
        controlJobManageReqInParam.setStrDurableControlJobCreateRequest(durableControlJobCreateRequest);
        controlJobManageReqInParam.setUser(paramIn.getUser());
        controlJobManageReqInParam.setClaimMemo(paramIn.getClaimMemo());
        sxDrbCJStatusChangeReq(objCommon, controlJobManageReqInParam);


        /*---------------------------------*/
        /* Make Event                      */
        /*---------------------------------*/
        Infos.DurableOperationCompleteEventMakeOpeCompIn completeEventMakeOpeCompIn = new Infos.DurableOperationCompleteEventMakeOpeCompIn();
        completeEventMakeOpeCompIn.setEquipmentID(paramIn.getEquipmentID());
        completeEventMakeOpeCompIn.setOperationMode(ObjectIdentifier.fetchValue(operationMode));
        completeEventMakeOpeCompIn.setDurableControlJobID(paramIn.getDurableControlJobID());
        completeEventMakeOpeCompIn.setDurableCategory(durableCategory);
        completeEventMakeOpeCompIn.setStrStartDurables(startDurables);
        completeEventMakeOpeCompIn.setStrDurableStartRecipe(durableStartRecipe);
        completeEventMakeOpeCompIn.setClaimMemo(paramIn.getClaimMemo());
        eventMethod.durableOperationCompleteEventMakeOpeComp(objCommon, completeEventMakeOpeCompIn);

        /*-------------------------------*/
        /* Durable PCS                   */
        /*-------------------------------*/
        if (onRouteFlag) {
            // todo:ZQI txRunDurableBRScriptReq
        }


        /*--------------------------------------------*/
        /*   Send DurableOpeCompReq() to TCS          */
        /*--------------------------------------------*/
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue = CimStringUtils.isEmpty(tmpSleepTimeValue) ? BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : Long.parseLong(tmpSleepTimeValue);
        long retryCountValue = CimStringUtils.isEmpty(tmpRetryCountValue) ? BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : Long.parseLong(tmpRetryCountValue);
        // todo:ZQI TCSMgr_SendDurableOpeCompReq

        // Set Return Structure
        retVal.setDurableCategory(durableCategory);
        retVal.setStrStartDurables(startDurables);
        return retVal;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strDurableGatePassReqResult
     * @param strObjCommonIn
     * @param seqIndex
     * @param strDurableGatePassReqInParam
     * @return com.fa.cim.dto.Results.DurableGatePassResult
     * @throws
     * @author ho
     * @date 2020/7/8 12:53
     */
    @Override
    public Results.DurableGatePassResult sxDrbPassThruReq(Results.DurableGatePassResult strDurableGatePassReqResult, Infos.ObjCommon strObjCommonIn, int seqIndex, Params.DurableGatePassReqInParam strDurableGatePassReqInParam) {
        log.info("PPTManager_i:: txDurableGatePassReq ");
        Params.DurableGatePassReqInParam strInParm = strDurableGatePassReqInParam;

        //---------------------------------------
        //  Check Parameter
        //---------------------------------------
        if (seqIndex >= CimArrayUtils.getSize(strInParm.getStrGatePassDurableInfo())) {
            log.info("", "input-param check NG");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Infos.GatePassDurableInfo strGatePassDurableInfo = strInParm.getStrGatePassDurableInfo().get(seqIndex);
        log.info("{} {}", "in-parm durableCategory        ", strGatePassDurableInfo.getDurableCategory());
        log.info("{} {}", "in-parm durableID              ", ObjectIdentifier.fetchValue(strGatePassDurableInfo.getDurableID()));
        log.info("{} {}", "in-parm currentOperationNumber ", strGatePassDurableInfo.getCurrentOperationNumber());
        log.info("{} {}", "in-parm currentRouteID         ", ObjectIdentifier.fetchValue(strGatePassDurableInfo.getCurrentRouteID()));

        // ****This initialisation is common for all.****
        strDurableGatePassReqResult.getStrGatePassDurablesResults().add(new Results.DurableGatePass());
        strDurableGatePassReqResult.getStrGatePassDurablesResults().get(seqIndex).setDurableID(strGatePassDurableInfo.getDurableID());

        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(strGatePassDurableInfo.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(strGatePassDurableInfo.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(strGatePassDurableInfo.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", strGatePassDurableInfo.getDurableCategory());
            Validations.check(retCodeConfig.getInvalidDurableCategory(), strGatePassDurableInfo.getDurableCategory());
        }

        //---------------------------------------
        //  Object Lock
        //---------------------------------------
        if (CimStringUtils.equals(strGatePassDurableInfo.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectLock(strObjCommonIn, CimCassette.class, strGatePassDurableInfo.getDurableID());
        } else if (CimStringUtils.equals(strGatePassDurableInfo.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectLock(strObjCommonIn, CimReticlePod.class, strGatePassDurableInfo.getDurableID());
        } else if (CimStringUtils.equals(strGatePassDurableInfo.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectLock(strObjCommonIn, CimProcessDurable.class, strGatePassDurableInfo.getDurableID());
        }

        //--------------------------------------------------------------------------
        //  Check whether Durable is on the specified Route/Operation or Not
        //--------------------------------------------------------------------------
        if (CimStringUtils.length(ObjectIdentifier.fetchValue(strGatePassDurableInfo.getCurrentRouteID())) > 0
                && CimStringUtils.length(strGatePassDurableInfo.getCurrentOperationNumber()) > 0) {
            log.info("{}", "durable is on the specified Route/Operation");

            Outputs.ObjDurableCurrentOperationInfoGetOut strDurablecurrentOperationInfoGetout;
            strDurablecurrentOperationInfoGetout = durableMethod.durableCurrentOperationInfoGet(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                    strGatePassDurableInfo.getDurableID());

            if (!ObjectIdentifier.equalsWithValue(strGatePassDurableInfo.getCurrentRouteID(), strDurablecurrentOperationInfoGetout.getRouteID())
                    || !CimStringUtils.equals(strGatePassDurableInfo.getCurrentOperationNumber(), strDurablecurrentOperationInfoGetout.getOperationNumber())) {
                log.info("{}", "Route/Operation check NG.");
                Validations.check(retCodeConfig.getNotSameRoute(), "Input parameter's currentRouteID/currentOperationNumber",
                        "Durables' current currentRouteID/currentOperationNumber");
            }
        }

        //---------------------------------------
        //  Check Durable OnRoute
        //---------------------------------------
        try {
            durableMethod.durableOnRouteCheck(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                    strGatePassDurableInfo.getDurableID());
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getDurableOnroute())) {
                throw ex;
            }
        }

        //---------------------------------------
        //  Call durable_status_CheckForOperation
        //---------------------------------------
        durableMethod.durableStatusCheckForOperation(strObjCommonIn, BizConstant.SP_OPERATIONCATEGORY_GATEPASS,
                strGatePassDurableInfo.getDurableID(),
                strGatePassDurableInfo.getDurableCategory()
        );

        //---------------------------------------
        //  Check Durable Hold State
        //---------------------------------------
        String strDurableholdStateGetout;
        strDurableholdStateGetout = durableMethod.durableHoldStateGet(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                strGatePassDurableInfo.getDurableID());
        if (CimStringUtils.length(strDurableholdStateGetout) > 0
                && !CimStringUtils.equals(strDurableholdStateGetout, BizConstant.SP_UNDEFINED_STATE)
                && !CimStringUtils.equals(strDurableholdStateGetout, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
            log.info("{}", "durableHoldState != SP_Durable_HoldState_OnHold");
            Validations.check(retCodeConfigEx.getInvalidDurableStat(), strDurableholdStateGetout);
        }

        //---------------------------------------
        //  Check Durable Process State
        //---------------------------------------
        String strDurableprocessStateGetout;
        strDurableprocessStateGetout = durableMethod.durableProcessStateGet(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                strGatePassDurableInfo.getDurableID());
        if (CimStringUtils.length(strDurableprocessStateGetout) > 0
                && !CimStringUtils.equals(strDurableprocessStateGetout, BizConstant.SP_UNDEFINED_STATE)
                && !CimStringUtils.equals(strDurableprocessStateGetout, BizConstant.SP_DURABLE_PROCSTATE_WAITING)) {
            log.info("{}", "durableProcessState != SP_Durable_ProcState_Waiting");
            Validations.check(retCodeConfig.getInvalidDurableProcStat(), ObjectIdentifier.fetchValue(strGatePassDurableInfo.getDurableID()),
                    strDurableprocessStateGetout);
        }

        //---------------------------------------
        //  Check Durable Inventory State
        //---------------------------------------
        String strDurableinventoryStateGetout;
        strDurableinventoryStateGetout = durableMethod.durableInventoryStateGet(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                strGatePassDurableInfo.getDurableID());
        if (CimStringUtils.length(strDurableinventoryStateGetout) > 0
                && !CimStringUtils.equals(strDurableinventoryStateGetout, BizConstant.SP_UNDEFINED_STATE)
                && !CimStringUtils.equals(strDurableinventoryStateGetout, BizConstant.SP_DURABLE_INVENTORYSTATE_ONFLOOR)) {
            log.info("{}", "durableInventoryState != SP_Durable_InventoryState_OnFloor");
            Validations.check(retCodeConfig.getInvalidDurableInventoryStat(), ObjectIdentifier.fetchValue(strGatePassDurableInfo.getDurableID()),
                    strDurableinventoryStateGetout);
        }

        //----------------------------------------------
        //  If Durable is in post process, returns error
        //----------------------------------------------
        Boolean strDurableinPostProcessFlagGetout;
        strDurableinPostProcessFlagGetout = durableMethod.durableInPostProcessFlagGet(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                strGatePassDurableInfo.getDurableID());
        if (CimBooleanUtils.isTrue(strDurableinPostProcessFlagGetout)) {
            log.info("{}", "isPostProcessFlagOn == TRUE");

            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            List<ObjectIdentifier> strPersonuserGroupListGetDRout;
            strPersonuserGroupListGetDRout = personMethod.personUserGroupListGetDR(
                    strObjCommonIn,
                    strObjCommonIn.getUser().getUserID());

            int userGroupLen = CimArrayUtils.getSize(strPersonuserGroupListGetDRout);
            log.info("{} {}", "userGroupIDs.length", userGroupLen);
            int nCnt = 0;
            for (nCnt = 0; nCnt < userGroupLen; nCnt++) {
            }

            if (nCnt == userGroupLen) {
                log.info("{}", "NOT External Post Process User!");
                Validations.check(retCodeConfig.getDurableInPostProcess(), ObjectIdentifier.fetchValue(strGatePassDurableInfo.getDurableID()));
            }
        }

        //---------------------------------------
        //  Check Durable ControlJob ID
        //---------------------------------------
        ObjectIdentifier strDurabledurableControlJobIDGetout;
        strDurabledurableControlJobIDGetout = durableMethod.durableDurableControlJobIDGet(strObjCommonIn,
                strGatePassDurableInfo.getDurableID(),
                strGatePassDurableInfo.getDurableCategory());
        if (CimStringUtils.length(ObjectIdentifier.fetchValue(strDurabledurableControlJobIDGetout)) > 0) {
            log.info("{}", "CIMFWStrLen(durableControlJobID.identifier) > 0");
            Validations.check(retCodeConfig.getDurableControlJobFilled());
        }

        //---------------------------------------
        //  Check Durable EndBank In
        //---------------------------------------
        try {
            durableMethod.durableCheckEndBankIn(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                    strGatePassDurableInfo.getDurableID());
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getNotBankInOperation())) {
                throw ex;
            }
        }

        //---------------------------------------
        //  Check Gate Pass
        //---------------------------------------
        Infos.ProcessCheckGatePassForDurableIn strProcessCheckGatePassForDurablein = new Infos.ProcessCheckGatePassForDurableIn();
        strProcessCheckGatePassForDurablein.setDurableCategory(strGatePassDurableInfo.getDurableCategory());
        strProcessCheckGatePassForDurablein.setDurableID(strGatePassDurableInfo.getDurableID());
        processForDurableMethod.processCheckGatePassForDurable(strObjCommonIn, strProcessCheckGatePassForDurablein);

        //---------------------------------------
        //  Call Process Move
        //---------------------------------------
        Boolean strDurableProcessMoveout;
        strDurableProcessMoveout = processForDurableMethod.durableProcessMove(strObjCommonIn, strGatePassDurableInfo.getDurableCategory(),
                strGatePassDurableInfo.getDurableID());

        //---------------------------------------
        //  Make History
        //---------------------------------------
        Infos.DurableOperationMoveEventMakeGatePassIn strDurableOperationMoveEventMakeGatePassin = new Infos.DurableOperationMoveEventMakeGatePassIn();
        strDurableOperationMoveEventMakeGatePassin.setDurableCategory(strGatePassDurableInfo.getDurableCategory());
        strDurableOperationMoveEventMakeGatePassin.setDurableID(strGatePassDurableInfo.getDurableID());
        strDurableOperationMoveEventMakeGatePassin.setTransactionID("ODRBW025");
        strDurableOperationMoveEventMakeGatePassin.setClaimMemo(strDurableGatePassReqInParam.getClaimMemo());
        eventMethod.durableOperationMoveEventMakeGatePass(strObjCommonIn, strDurableOperationMoveEventMakeGatePassin);

        //---------------------------------------
        //  Check Auto BankIn
        //---------------------------------------
        boolean strDurableCheckConditionForAutoBankInout;
        Infos.DurableCheckConditionForAutoBankInIn strDurableCheckConditionForAutoBankInin = new Infos.DurableCheckConditionForAutoBankInIn();
        strDurableCheckConditionForAutoBankInin.setDurableCategory(strGatePassDurableInfo.getDurableCategory());
        strDurableCheckConditionForAutoBankInin.setDurableID(strGatePassDurableInfo.getDurableID());
        strDurableCheckConditionForAutoBankInout = durableMethod.durableCheckConditionForAutoBankIn(strObjCommonIn, strDurableCheckConditionForAutoBankInin);
        if (strDurableCheckConditionForAutoBankInout) {
            log.info("{}", "autoBankInFlag == TRUE");

            Results.DurableBankInReqResult strDurableBankInReqResult = new Results.DurableBankInReqResult();
            strDurableBankInReqResult.setStrBankInDurableResults(new ArrayList<>(1));
            Params.DurableBankInReqInParam strDurableBankInReqInParam = new Params.DurableBankInReqInParam();
            strDurableBankInReqInParam.setDurableCategory(strGatePassDurableInfo.getDurableCategory());
            strDurableBankInReqInParam.setDurableIDs(new ArrayList<>(1));
            strDurableBankInReqInParam.getDurableIDs().add(strGatePassDurableInfo.getDurableID());
            try {
                strDurableBankInReqResult = durableService.sxDrbBankInReq(strDurableBankInReqResult, strObjCommonIn, 0, strDurableBankInReqInParam, strDurableGatePassReqInParam.getClaimMemo());
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidDurableHoldStat())) {
                    log.info("{}", "txDurableBankInReq() == RC_INVALID_DURABLE_HOLDSTAT", retCodeConfig.getInvalidDurableHoldStat());
                } else {
                    throw ex;
                }
            }
        }

        //---------------------------------------
        // Return
        //---------------------------------------
        strDurableGatePassReqResult.getStrGatePassDurablesResults().get(seqIndex).setDurableID(strGatePassDurableInfo.getDurableID());
        log.info("PPTManager_i:: txDurableGatePassReq ");
        return strDurableGatePassReqResult;
    }

    /**
     * The function registers the Post Processing Action Queue with the Queue Table,
     * in accordance with the Pattern Table defining the sequence of transactions.
     *
     * <p> Each action in the queue to be registered is specified with its Tx, DurableID, and D_key.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/30 10:45
     */
    @Override
    public Results.DurablePostProcessActionRegistReqResult sxDrbPostTaskRegistReq(Infos.ObjCommon objCommon, Params.DurablePostProcessActionRegistReqInParam paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        Results.DurablePostProcessActionRegistReqResult retVal = new Results.DurablePostProcessActionRegistReqResult();

        //------------------------------------------
        //  Call durablePostProcessQueue_Make
        //------------------------------------------
        Inputs.DurablePostProcessQueueMakeIn postProcessQueueMakeIn = new Inputs.DurablePostProcessQueueMakeIn();
        postProcessQueueMakeIn.setTxID(paramIn.getTxID());
        postProcessQueueMakeIn.setPatternID(paramIn.getPatternID());
        postProcessQueueMakeIn.setKey(paramIn.getKey());
        postProcessQueueMakeIn.setSeqNo(paramIn.getSeqNo());
        postProcessQueueMakeIn.setStrDurablePostProcessRegistrationParm(paramIn.getStrDurablePostProcessRegistrationParm());
        postProcessQueueMakeIn.setClaimMemo(paramIn.getClaimMemo());
        Outputs.DurablePostProcessQueueMakeOut durablePostProcessQueueMakeOut = postProcessMethod.durablePostProcessQueueMake(objCommon, postProcessQueueMakeIn);
        assert durablePostProcessQueueMakeOut != null;


        //------------------------------------------
        //  Get CassetteID
        //------------------------------------------
        Map<String, ObjectIdentifier> cassetteIDs = new HashMap<>();
        Optional.of(durablePostProcessQueueMakeOut).flatMap(data -> Optional.ofNullable(data.getStrActionInfoSeq())).ifPresent(list -> list.forEach(actionInfo -> {
            ObjectIdentifier cassetteID = actionInfo.getPostProcessTargetObject().getCassetteID();
            if (ObjectIdentifier.isNotEmptyWithValue(cassetteID)) {
                cassetteIDs.put(ObjectIdentifier.fetchValue(cassetteID), cassetteID);
            }
        }));
        List<ObjectIdentifier> tmpCasseIDList = new ArrayList<>(cassetteIDs.values());

        // Get environment variables
        long lockHoldUseFlag = StandardProperties.OM_LOCK_HOLD_MODE.getLongValue();
        long postProcFlagUseFlag = StandardProperties.OM_PP_FLAG_USE_FLAG.getLongValue();
        if (1 == lockHoldUseFlag || 1 == postProcFlagUseFlag) {
            String durableCategory = paramIn.getStrDurablePostProcessRegistrationParm().getDurableCategory();
            for (ObjectIdentifier durableID : tmpCasseIDList) {
                boolean onRouteFlag = false;
                try {
                    //Check Durable OnRoute
                    durableMethod.durableOnRouteCheck(objCommon, durableCategory, durableID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                        log.info("durable OnRoute state is TRUE");
                        onRouteFlag = true;
                    }
                }
                if (onRouteFlag) {
                    log.info("durable OnRoute state is TRUE");
                    if (lockHoldUseFlag == 1) {
                        log.info("lockHoldUseFlag is 1");
                        Params.HoldDurableReqInParam holdDurableReqInParam = new Params.HoldDurableReqInParam();
                        holdDurableReqInParam.setDurableCategory(durableCategory);
                        holdDurableReqInParam.setDurableID(durableID);

                        List<Infos.DurableHoldList> durableHoldLists = new ArrayList<>();
                        durableHoldLists.add(new Infos.DurableHoldList());
                        durableHoldLists.get(0).setHoldType(BizConstant.SP_HOLDTYPE_DURABLEHOLD);
                        durableHoldLists.get(0).setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLELOCK));
                        durableHoldLists.get(0).setHoldUserID(ObjectIdentifier.buildWithValue(BizConstant.SP_POSTPROC_PERSON));
                        durableHoldLists.get(0).setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                        holdDurableReqInParam.setDurableHoldLists(durableHoldLists);


                        try {
                            sxHoldDrbReq(objCommon, holdDurableReqInParam, paramIn.getClaimMemo());
                        } catch (ServiceException e) {
                            if (Validations.isEquals(e.getCode(), retCodeConfigEx.getInvalidDurableStat())) {
                                log.info("Error Code : " + e.getCode() + " rc = RC_INVALID_DURABLE_STAT .Do nothing...");
                            } else {
                                throw e;
                            }
                        }
                    }

                    if (postProcFlagUseFlag == 1) {
                        log.info("postProcFlagUseFlag is 1");
                        durableMethod.durableInPostProcessFlagSet(objCommon, durableCategory, durableID, true);
                    }
                }
            }
        }

        //------------------------------------------
        //  Put Action informations to queue
        //------------------------------------------
        postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADD, durablePostProcessQueueMakeOut.getStrActionInfoSeq());

        retVal.setStrPostProcessActionInfoSeq(durablePostProcessQueueMakeOut.getStrActionInfoSeq());
        retVal.setDKey(durablePostProcessQueueMakeOut.getDKey());
        retVal.setKeyTimeStamp(durablePostProcessQueueMakeOut.getKeyTimeStamp());
        return retVal;
    }

    /**
     * This function creates Durable PFX of specified Durable.
     * <p> - Check condition of durable( PFX is null )
     * <p> - Check status of durable( Should be NotAvailable )
     * <p> - Create Durable PFX
     * <p> - Make event
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/23 11:02
     */
    @Override
    public void sxDrbPrfCxCreateReq(Infos.ObjCommon objCommon, Params.DurablePFXCreateReqInParam paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());

        //*******************************************//
        //  Check Durable category                   //
        //*******************************************//
        String durableCategory = paramIn.getDurableCategory();
        log.info("in-parm durableCategory  : " + durableCategory);
        if (!CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        //*******************************************//
        // Object Lock                               //
        //*******************************************//
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                lockMethod.objectSequenceLock(objCommon, CimCassette.class, paramIn.getDurableIDs());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                lockMethod.objectSequenceLock(objCommon, CimReticlePod.class, paramIn.getDurableIDs());
                break;
            case SP_DURABLECAT_RETICLE:
                lockMethod.objectSequenceLock(objCommon, CimProcessResource.class, paramIn.getDurableIDs());
                break;
            default:
                break;
        }

        //*******************************************//
        // Check condition for operation             //
        //*******************************************//
        List<Infos.StartDurable> startDurables = new ArrayList<>();
        Optional.ofNullable(paramIn.getDurableIDs()).ifPresent(list -> list.forEach(data -> {
            Infos.StartDurable durable = new Infos.StartDurable();
            durable.setDurableId(data);
            startDurables.add(durable);
        }));

        Inputs.ObjDurableCheckConditionForOperationIn checkConditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
        checkConditionForOperationIn.setOperation(BizConstant.SP_OPERATION_PFXCREATE);
        checkConditionForOperationIn.setDurableCategory(durableCategory);
        checkConditionForOperationIn.setStartDurables(startDurables);
        durableMethod.durableCheckConditionForOperation(objCommon, checkConditionForOperationIn);

        //*******************************************//
        // Durable status check                      //
        //*******************************************//
        Optional.ofNullable(paramIn.getDurableIDs()).ifPresent(list -> list.forEach(data -> {
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATION_PFXCREATE, data, durableCategory);
        }));

        //*******************************************//
        // Durable STB check for Bank                //
        //*******************************************//
        if (ObjectIdentifier.isNotEmptyWithValue(paramIn.getRouteID())) {
            bankForDurableMethod.bankDurableSTBCheck(objCommon, paramIn.getRouteID(), durableCategory, paramIn.getDurableIDs());
        }

        //*******************************************//
        // Durable PFX Create                        //
        //*******************************************//
        Optional.ofNullable(paramIn.getDurableIDs()).ifPresent(list -> list.forEach(durableID -> {
            durableMethod.durablePFXCreate(objCommon, durableCategory, durableID, paramIn.getRouteID());

            //-----------------------
            // Make Event
            //-----------------------
            Infos.DurablePFXCreateEventMakeIn eventMakeIn = new Infos.DurablePFXCreateEventMakeIn();
            eventMakeIn.setDurableCategory(durableCategory);
            eventMakeIn.setDurableID(durableID);
            eventMakeIn.setRouteID(paramIn.getRouteID());
            eventMakeIn.setClaimMemo(paramIn.getClaimMemo());
            eventMethod.durablePFXCreateEventMake(objCommon, eventMakeIn);
        }));
    }

    /**
     * This function deletes Durable PFX of specified Durable.
     * <p> - Check condition of durable( PFX is not Nil, must not have durableControlJob )
     * <p> - Check status of durable( Should be NotAvailable )
     * <p> - Delete Durable PFX
     * <p> - Make event
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/24 13:17
     */
    @Override
    public void sxDrbPrfCxDeleteReq(Infos.ObjCommon objCommon, Params.DurablePFXDeleteReqInParam paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        //*******************************************//
        //  Check Durable category                   //
        //*******************************************//
        String durableCategory = paramIn.getDurableCategory();
        log.info("in-parm durableCategory  : " + durableCategory);
        if (!CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        //*******************************************//
        // Object Lock                               //
        //*******************************************//
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                lockMethod.objectSequenceLock(objCommon, CimCassette.class, paramIn.getDurableIDs());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                lockMethod.objectSequenceLock(objCommon, CimReticlePod.class, paramIn.getDurableIDs());
                break;
            case SP_DURABLECAT_RETICLE:
                lockMethod.objectSequenceLock(objCommon, CimProcessResource.class, paramIn.getDurableIDs());
                break;
        }

        //*******************************************//
        // Check condition for operation             //
        //*******************************************//
        List<Infos.StartDurable> startDurables = new ArrayList<>();
        Optional.ofNullable(paramIn.getDurableIDs()).ifPresent(list -> list.forEach(data -> {
            Infos.StartDurable durable = new Infos.StartDurable();
            startDurables.add(durable);
            durable.setDurableId(data);
        }));

        Inputs.ObjDurableCheckConditionForOperationIn checkConditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
        checkConditionForOperationIn.setOperation(BizConstant.SP_OPERATION_PFXDELETE);
        checkConditionForOperationIn.setDurableCategory(durableCategory);
        checkConditionForOperationIn.setStartDurables(startDurables);
        durableMethod.durableCheckConditionForOperation(objCommon, checkConditionForOperationIn);

        //*******************************************//
        // Durable status check                      //
        //*******************************************//
        Optional.ofNullable(paramIn.getDurableIDs()).ifPresent(list -> list.forEach(data -> {
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATION_PFXDELETE, data, durableCategory);

            //---------------------------------------
            // OnRoute Check
            //---------------------------------------
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, data);
            } catch (ServiceException e) {
                if (Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                    String holdStateGet = durableMethod.durableHoldStateGet(objCommon, durableCategory, data);
                    if (CimStringUtils.equals(holdStateGet, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                        log.error("durableHoldState == SP_Durable_HoldState_OnHold");
                        Validations.check(retCodeConfigEx.getInvalidDurableStat(), holdStateGet);
                    }

                    //---------------------------------------
                    //  Check Durable Process State
                    //---------------------------------------
                    String processStateGet = durableMethod.durableProcessStateGet(objCommon, durableCategory, data);
                    if (CimStringUtils.equals(processStateGet, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING)) {
                        log.error("durableProcessState == SP_Durable_ProcState_Processing");
                        Validations.check(retCodeConfig.getInvalidDurableProcStat(), processStateGet);
                    }
                }
            }
        }));

        //*******************************************//
        // Durable PFX Delete                        //
        //*******************************************//
        Optional.ofNullable(paramIn.getDurableIDs()).ifPresent(list -> list.forEach(durableID -> {
            durableMethod.durablePFXDelete(objCommon, durableCategory, durableID);

            //-----------------------
            // Make Event
            //-----------------------
            Infos.DurablePFXDeleteEventMakeIn deleteEventMakeIn = new Infos.DurablePFXDeleteEventMakeIn();
            deleteEventMakeIn.setDurableCategory(durableCategory);
            deleteEventMakeIn.setDurableID(durableID);
            deleteEventMakeIn.setClaimMemo(paramIn.getClaimMemo());
            eventMethod.durablePFXDeleteEventMake(objCommon, deleteEventMakeIn);
        }));
    }

    @Override
    public void sxDrbReworkCancelReq(Infos.ObjCommon objCommon, Infos.ReworkDurableCancelReqInParam strInParm, String claimMemo) {
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        String durableCategory = strInParm.getDurableCategory();
        ObjectIdentifier durableID = strInParm.getDurableID();
        ObjectIdentifier currentRouteID = strInParm.getCurrentRouteID();
        String currentOperationNumber = strInParm.getCurrentOperationNumber();
        ObjectIdentifier reasonCodeID = strInParm.getReasonCodeID();
        log.info("in-parm durableCategory        {}", durableCategory);
        log.info("in-parm durableID              {}", durableID);
        log.info("in-parm currentRouteID         {}", currentRouteID);
        log.info("in-parm currentOperationNumber {}", currentOperationNumber);
        log.info("in-parm reasonCodeID           {}", reasonCodeID);

        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        Validations.check(!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE), retCodeConfig.getInvalidDurableCategory(), durableCategory);

        //---------------------------------------
        //  Object Lock
        //---------------------------------------
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("calling object_Lock() {}", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("calling object_Lock() {}", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("calling object_Lock() {}", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
        }

        //--------------------------------------------------------------------------
        //  Check whether Durable is on the specified Route/Operation or Not
        //--------------------------------------------------------------------------
        if (!ObjectIdentifier.isEmpty(currentRouteID) && !CimObjectUtils.isEmpty(currentOperationNumber)) {
            log.info("durable is on the specified Route/Operation");
            Outputs.ObjDurableCurrentOperationInfoGetOut objDurableCurrentOperationInfoGetOut = durableMethod.durableCurrentOperationInfoGet(objCommon, durableCategory, durableID);

            Validations.check(!ObjectIdentifier.equalsWithValue(currentRouteID, objDurableCurrentOperationInfoGetOut.getRouteID())
                            || !CimStringUtils.equals(currentOperationNumber, objDurableCurrentOperationInfoGetOut.getOperationNumber()), retCodeConfig.getNotSameRoute(),
                    "Input parameter's currentRouteID/currentOperationNumber", "Durables' current currentRouteID/currentOperationNumber");
        }

        //---------------------------------------
        //  Check Durable OnRoute
        //---------------------------------------
        try {
            durableMethod.durableOnRouteCheck(objCommon, durableCategory, durableID);
        } catch (ServiceException ex) {
            if( !Validations.isEquals(ex.getCode(),retCodeConfig.getDurableOnroute()) ) {
                Validations.check(true, retCodeConfig.getDurableNotOnroute(), ObjectIdentifier.fetchValue(strInParm.getDurableID()));
            }
        }

        //---------------------------------------
        //  Call durable_status_CheckForOperation
        //---------------------------------------
        durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATIONCATEGORY_REWORKCANCEL, durableID, durableCategory);

        //---------------------------------------
        //  Check Durable Hold State
        //---------------------------------------
        String durableHoldState = durableMethod.durableHoldStateGet(objCommon, durableCategory, durableID);
        Validations.check(CimStringUtils.equals(durableHoldState, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD), retCodeConfigEx.getInvalidDurableStat(), durableID, durableHoldState);

        //---------------------------------------
        //  Check Durable Process State
        //---------------------------------------
        String durableProcessState = durableMethod.durableProcessStateGet(objCommon, durableCategory, durableID);
        Validations.check(CimStringUtils.equals(durableProcessState, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING), retCodeConfig.getInvalidDurableProcStat(), durableID, durableProcessState);

        //---------------------------------------
        //  Check Durable Production State
        //---------------------------------------
        String durableProductionState = durableMethod.durableProductionStateGet(objCommon, durableCategory, durableID);
        Validations.check(!CimStringUtils.equals(durableProductionState, BizConstant.SP_DURABLE_PRODUCTIONSTATE_INREWORK), retCodeConfigEx.getInvalidDurableProdstat(), durableID, durableProductionState);

        //---------------------------------------
        //  Check Durable Inventory State
        //---------------------------------------
        String durableInventoryState = durableMethod.durableInventoryStateGet(objCommon, durableCategory, durableID);
        Validations.check(!CimStringUtils.equals(durableInventoryState, SP_DURABLE_INVENTORYSTATE_ONFLOOR), retCodeConfig.getInvalidDurableInventoryStat(), durableID, durableInventoryState);

        //----------------------------------------------
        //  If Durable is in post process, returns error
        //----------------------------------------------
        Boolean isPostProcessFlagOn = durableMethod.durableInPostProcessFlagGet(objCommon, durableCategory, durableID);
        if (CimBooleanUtils.isTrue(isPostProcessFlagOn)) {
            log.info("isPostProcessFlagOn == TRUE");

            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

            int nCnt;
            int userGroupLen = CimArrayUtils.getSize(userGroupIDs);
            for (nCnt = 0; nCnt < userGroupLen; nCnt++) {
                ObjectIdentifier userGroupID = userGroupIDs.get(nCnt);
                log.info("# Loop[nCnt]/userID {} {}", nCnt, userGroupID);
            }
            Validations.check(nCnt == userGroupLen, retCodeConfig.getDurableInPostProcess());
        }

        //---------------------------------------
        //  Check Durable ControlJob ID
        //---------------------------------------
        ObjectIdentifier durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, durableID, durableCategory);
        Validations.check(!ObjectIdentifier.isEmpty(durableControlJobID), retCodeConfig.getDurableControlJobFilled());

        //------------------------------------------
        //  Call process_CheckBranchCancelForDurable
        //------------------------------------------
        durableMethod.processCheckBranchCancelForDurable(objCommon, durableCategory, durableID); //TODO

        //-----------------------------------------------------------------------
        //   Change State
        //-----------------------------------------------------------------------
        durableMethod.processDurableReworkCountDecrement(objCommon, durableCategory, durableID);
        Inputs.OldCurrentPOData oldCurrentPOData = durableMethod.processCancelBranchRouteForDurable(objCommon, durableCategory, durableID);

        //-----------------------------------------------------------------------
        //   Make History
        //-----------------------------------------------------------------------
        Infos.DurableReworkEventMakeIn durableReworkEventMakeIn = new Infos.DurableReworkEventMakeIn();
        durableReworkEventMakeIn.setDurableCategory(durableCategory);
        durableReworkEventMakeIn.setDurableID(durableID);
        durableReworkEventMakeIn.setStrOldCurrentPOData(oldCurrentPOData);
        durableReworkEventMakeIn.setReasonCodeID(reasonCodeID);
        durableReworkEventMakeIn.setTransactionID(TransactionIDEnum.REWORK_DURABLE_CANCEL_REQ.getValue());
        durableReworkEventMakeIn.setClaimMemo(claimMemo);
        eventMethod.durableReworkEventMake(objCommon, durableReworkEventMakeIn);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strReworkDurableReqInParam
     * @param claimMemo
     * @return void
     * @throws
     * @author ho
     * @date 2020/7/10 10:42
     */
    @Override
    public void sxDrbReworkReq(Infos.ObjCommon strObjCommonIn, Params.ReworkDurableReqInParam strReworkDurableReqInParam, String claimMemo) {
        log.info("PPTManager_i:: txReworkDurableReq ");

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Params.ReworkDurableReqInParam strInParm = strReworkDurableReqInParam;
        log.info("{} {}", "in-parm durableCategory        ", strInParm.getDurableCategory());
        log.info("{} {}", "in-parm durableID              ", ObjectIdentifier.fetchValue(strInParm.getDurableID()));
        log.info("{} {}", "in-parm currentRouteID         ", ObjectIdentifier.fetchValue(strInParm.getCurrentRouteID()));
        log.info("{} {}", "in-parm currentOperationNumber ", strInParm.getCurrentOperationNumber());
        log.info("{} {}", "in-parm subRouteID             ", ObjectIdentifier.fetchValue(strInParm.getSubRouteID()));
        log.info("{} {}", "in-parm returnOperationNumber  ", strInParm.getReturnOperationNumber());
        log.info("{} {}", "in-parm reasonCodeID           ", ObjectIdentifier.fetchValue(strInParm.getReasonCodeID()));
        log.info("{} {}", "in-parm bForceRework           ", strInParm.getBForceRework());
        log.info("{} {}", "in-parm bDynamicRoute          ", strInParm.getBDynamicRoute());

        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "Invalid durable category", strInParm.getDurableCategory());
            Validations.check(true, retCodeConfig.getInvalidDurableCategory());
        }

        //---------------------------------------
        //  Object Lock
        //---------------------------------------
        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectLock(strObjCommonIn, CimCassette.class, strInParm.getDurableID());
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectLock(strObjCommonIn, CimReticlePod.class, strInParm.getDurableID());
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectLock(strObjCommonIn, CimProcessDurable.class, strInParm.getDurableID());
        }

        //--------------------------------------------------------------------------
        //  Check whether Durable is on the specified Route/Operation or Not
        //--------------------------------------------------------------------------
        Outputs.ObjDurableCurrentOperationInfoGetOut strDurablecurrentOperationInfoGetout;
        strDurablecurrentOperationInfoGetout = durableMethod.durableCurrentOperationInfoGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());

        if (CimStringUtils.length(ObjectIdentifier.fetchValue(strInParm.getCurrentRouteID())) > 0 && CimStringUtils.length(strInParm.getCurrentOperationNumber()) > 0) {
            if (!ObjectIdentifier.equalsWithValue(strInParm.getCurrentRouteID(), strDurablecurrentOperationInfoGetout.getRouteID())
                    || !CimStringUtils.equals(strInParm.getCurrentOperationNumber(), strDurablecurrentOperationInfoGetout.getOperationNumber())) {
                log.info("{}", "Route/Operation check NG.");
                Validations.check(true, retCodeConfig.getNotSameRoute(),
                        "Input parameter's currentRouteID/currentOperationNumber",
                        "Durables' current currentRouteID/currentOperationNumber");
            }
        }

        //---------------------------------------
        //  Check Durable OnRoute
        //---------------------------------------
        try {
            durableMethod.durableOnRouteCheck(strObjCommonIn, strInParm.getDurableCategory(),
                    strInParm.getDurableID());
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfig.getDurableOnroute())) {
                Validations.check(true, retCodeConfig.getDurableNotOnroute(), ObjectIdentifier.fetchValue(strInParm.getDurableID()));
            }
        }

        //---------------------------------------
        //  Call durable_status_CheckForOperation
        //---------------------------------------
        durableMethod.durableStatusCheckForOperation(strObjCommonIn, BizConstant.SP_OPERATIONCATEGORY_REWORK,
                strInParm.getDurableID(),
                strInParm.getDurableCategory());

        //---------------------------------------
        //  Check Durable Hold State
        //---------------------------------------
        String strDurableholdStateGetout;
        strDurableholdStateGetout = durableMethod.durableHoldStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (CimStringUtils.equals(strDurableholdStateGetout, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
            log.info("{}", "durableHoldState == SP_Durable_HoldState_OnHold");
            Validations.check(true, retCodeConfigEx.getInvalidDurableStat(),
                    strDurableholdStateGetout);
        }

        //---------------------------------------
        //  Check Durable Process State
        //---------------------------------------
        String strDurableprocessStateGetout;
        strDurableprocessStateGetout = durableMethod.durableProcessStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (CimStringUtils.equals(strDurableprocessStateGetout, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING)) {
            log.info("{}", "durableProcessState == SP_Durable_ProcState_Processing");
            Validations.check(true, retCodeConfig.getInvalidDurableProcStat(),
                    ObjectIdentifier.fetchValue(strInParm.getDurableID()),
                    strDurableprocessStateGetout);
        }

        //---------------------------------------
        //  Check Durable Production State
        //---------------------------------------
        String strDurableproductionStateGetout;
        strDurableproductionStateGetout = durableMethod.durableProductionStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (CimStringUtils.equals(strDurableproductionStateGetout, BizConstant.SP_DURABLE_PRODUCTIONSTATE_INREWORK)) {
            log.info("{}", "durableProductionState == SP_Durable_ProductionState_InRework");
            Validations.check(true, retCodeConfig.getInvalidDurableProcStat(),
                    ObjectIdentifier.fetchValue(strInParm.getDurableID()),
                    strDurableproductionStateGetout);
        }

        //---------------------------------------
        //  Check Durable Inventory State
        //---------------------------------------
        String strDurableinventoryStateGetout;
        strDurableinventoryStateGetout = durableMethod.durableInventoryStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (!CimStringUtils.equals(strDurableinventoryStateGetout, BizConstant.SP_DURABLE_INVENTORYSTATE_ONFLOOR)) {
            log.info("{}", "durableInventoryState != SP_Durable_InventoryState_OnFloor");
            Validations.check(true, retCodeConfig.getInvalidDurableInventoryStat(),
                    ObjectIdentifier.fetchValue(strInParm.getDurableID()),
                    strDurableinventoryStateGetout);
        }

        //----------------------------------------------
        //  If Durable is in post process, returns error
        //----------------------------------------------
        Boolean strDurableinPostProcessFlagGetout;
        strDurableinPostProcessFlagGetout = durableMethod.durableInPostProcessFlagGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (CimBooleanUtils.isTrue(strDurableinPostProcessFlagGetout)) {
            log.info("{}", "isPostProcessFlagOn == TRUE");

            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            List<ObjectIdentifier> strPersonuserGroupListGetDRout;
            strPersonuserGroupListGetDRout = personMethod.personUserGroupListGetDR(
                    strObjCommonIn,
                    strObjCommonIn.getUser().getUserID());

            int userGroupLen = CimArrayUtils.getSize(strPersonuserGroupListGetDRout);
            log.info("{} {}", "userGroupIDs.length", userGroupLen);


            int nCnt = 0;
            for (nCnt = 0; nCnt < userGroupLen; nCnt++) {
            }

            if (nCnt == userGroupLen) {
                log.info("{}", "NOT External Post Process User!");
                Validations.check(true, retCodeConfig.getDurableInPostProcess(),
                        ObjectIdentifier.fetchValue(strInParm.getDurableID()));
            }
        }

        //---------------------------------------
        //  Check Durable ControlJob ID
        //---------------------------------------
        ObjectIdentifier strDurabledurableControlJobIDGetout;
        strDurabledurableControlJobIDGetout = durableMethod.durableDurableControlJobIDGet(strObjCommonIn, strInParm.getDurableID(),
                strInParm.getDurableCategory());
        if (CimStringUtils.length(ObjectIdentifier.fetchValue(strDurabledurableControlJobIDGetout)) > 0) {
            log.info("{}", "CIMFWStrLen(durableControlJobID.identifier) > 0");
            ;
            Validations.check(true, retCodeConfig.getDurableControlJobFilled());
        }

        //-------------------------------------------------------
        //  Converts input strReworkDurableReqInParam.subrouteID
        //-------------------------------------------------------
        ObjectIdentifier aSubRouteID;
        ObjectIdentifier strProcessactiveIDGetout;
        strProcessactiveIDGetout = processMethod.processActiveIDGet(strObjCommonIn, strInParm.getSubRouteID());
        aSubRouteID = strProcessactiveIDGetout;
        log.info("{} {}", "active subRouteID = ", ObjectIdentifier.fetchValue(aSubRouteID));

        //-----------------------------------
        //   Check first operation or not
        //-----------------------------------
        Results.DurableOperationListInqResult strProcessOperationListForDurableDRout;
        Params.ProcessOperationListForDurableDRInParam strProcessOperationListForDurableDRin = new Params.ProcessOperationListForDurableDRInParam();
        strProcessOperationListForDurableDRin.setDurableCategory(strInParm.getDurableCategory());
        strProcessOperationListForDurableDRin.setDurableID(strInParm.getDurableID());
        strProcessOperationListForDurableDRin.setSearchDirection(false);
        strProcessOperationListForDurableDRin.setPosSearchFlag(true);
        strProcessOperationListForDurableDRin.setCurrentFlag(true);
        strProcessOperationListForDurableDRin.setSearchCount(2);
        strProcessOperationListForDurableDRin.setSearchRouteID(ObjectIdentifier.buildWithValue(""));
        strProcessOperationListForDurableDRin.setSearchOperationNumber("");
        strProcessOperationListForDurableDRout = processForDurableMethod.processOperationListForDurableDR(strObjCommonIn, strProcessOperationListForDurableDRin);
        if (CimArrayUtils.getSize(strProcessOperationListForDurableDRout.getStrDurableOperationNameAttributes()) < 2
                || !ObjectIdentifier.equalsWithValue(strProcessOperationListForDurableDRout.getStrDurableOperationNameAttributes().get(0).getRouteID(),
                strProcessOperationListForDurableDRout.getStrDurableOperationNameAttributes().get(1).getRouteID())) {
            log.info("{}", "strOperationNameAttributes[0].routeID is not same as strOperationNameAttributes[1].routeID");
            Validations.check(true, retCodeConfigEx.getInvalidDurableReworkOperation());
        }

        //------------------------------------------------------------------------
        //  Check Input return operation number is exist in connected route list
        //------------------------------------------------------------------------
        Boolean bReturnOperationFlag = false;
        Boolean bConnectedRouteReturnOperationFlag = false;
        Boolean bSameReturnOperationExistFlag = false;
        String returnOperationNumberVar = "";

        if (CimStringUtils.length(strInParm.getReturnOperationNumber()) > 0) {
            log.info("{}", "Set bReturnOperationFlag = TRUE");
            ;
            bReturnOperationFlag = true;
        }

        Infos.ProcessGetReturnOperationForDurableOut strProcessGetReturnOperationForDurableout = null;
        Infos.ProcessGetReturnOperationForDurableIn strProcessGetReturnOperationForDurablein = new Infos.ProcessGetReturnOperationForDurableIn();
        strProcessGetReturnOperationForDurablein.setDurableCategory(strInParm.getDurableCategory());
        strProcessGetReturnOperationForDurablein.setDurableID(strInParm.getDurableID());
        strProcessGetReturnOperationForDurablein.setSubRouteID(aSubRouteID);
        try {
            strProcessGetReturnOperationForDurableout = processForDurableMethod.processGetReturnOperationForDurable(strObjCommonIn, strProcessGetReturnOperationForDurablein);
            log.info("{}", "Set bConnectedRouteReturnOperationFlag = TRUE");
            bConnectedRouteReturnOperationFlag = true;

            if (CimStringUtils.equals(strProcessGetReturnOperationForDurableout.getOperationNumber(), strInParm.getReturnOperationNumber())) {
                log.info("{}", "Set bSameReturnOperationExistFlag = TRUE");
                bSameReturnOperationExistFlag = true;
            } else if (CimStringUtils.length(strInParm.getReturnOperationNumber()) > 0) {
                log.info("{}", "return RC_INVALID_INPUT_PARM");
                Validations.check(true, retCodeConfig.getInvalidInputParam());
            }
        } catch (ServiceException ex) {
            if (Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundSubRoute())) {
                log.info("process_GetReturnOperationForDurable() == RC_NOT_FOUND_SUBROUTE");
            } else {
                throw ex;
            }
        }

        //-------------------------------------------------
        // Decide return operation number using all flags
        //-------------------------------------------------
        if (CimBooleanUtils.isTrue(bConnectedRouteReturnOperationFlag)) {
            log.info("{}", "bConnectedRouteReturnOperationFlag == TRUE");
            if (CimBooleanUtils.isTrue(bReturnOperationFlag)) {
                log.info("{}", "bReturnOperationFlag == TRUE");
                returnOperationNumberVar = strInParm.getReturnOperationNumber();
            } else {
                log.info("{}", "bReturnOperationFlag == FALSE");
                returnOperationNumberVar = strProcessGetReturnOperationForDurableout.getOperationNumber();
            }
        } else {
            log.info("{}", "bConnectedRouteReturnOperationFlag == FALSE");
            Validations.check(true, retCodeConfig.getInvalidRouteId());
        }

        //----------------------------------------------------
        //   Check ProcessDefinitionType is 'REWORK' or not
        //----------------------------------------------------
        if (!CimStringUtils.equals(strProcessGetReturnOperationForDurableout.getProcessDefinitionType(), BizConstant.SP_MAINPDTYPE_DURABLEREWORK)) {
            log.info("{}", "processDefinitionType != SP_MAINPDTYPE_DURABLEREWORK");
            ;
            Validations.check(true, retCodeConfig.getInvalidRouteType());
        }

        //-----------------------------------------------------------
        //   Check decided return operation is exist on current route
        //-----------------------------------------------------------
        List<Infos.OperationNameAttributes> strRouteOperationListForDurableInqResult;
        Params.RouteOperationListForDurableInqInParam strRouteOperationListForDurableInqInParam = new Params.RouteOperationListForDurableInqInParam();
        strRouteOperationListForDurableInqInParam.setDurableCategory(strInParm.getDurableCategory());
        strRouteOperationListForDurableInqInParam.setDurableID(strInParm.getDurableID());
        strRouteOperationListForDurableInqResult = durableInqService.sxProcessFlowOperationListForDrbInq(strObjCommonIn, strRouteOperationListForDurableInqInParam);

        log.info("{} {}", "returnOperationNumberVar : ", returnOperationNumberVar);
        int opeLen = CimArrayUtils.getSize(strRouteOperationListForDurableInqResult);
        for (int opeCnt = 0; opeCnt < opeLen; opeCnt++) {
            log.info("{} {}", "opeCnt : ", opeCnt);
            log.info("{} {}",
                    "strOperationNameAttributes[opeCnt].operationNumber : ",
                    strRouteOperationListForDurableInqResult.get(opeCnt).getOperationNumber());

            if (CimStringUtils.equals(strRouteOperationListForDurableInqResult.get(opeCnt).getOperationNumber(),
                    returnOperationNumberVar)) {
                log.info("{}", "return operation is exist on current route");
                break;
            } else if (opeCnt == (opeLen - 1)) {
                log.info("{}", "opeCnt == opeLen - 1");
                Validations.check(true, retCodeConfig.getNotFoundOperation(), returnOperationNumberVar);
            }
        }

        // -----------------------------------------------------------
        // Check routeID confliction
        //   return RC_INVALID_BRANCH_ROUTEID,
        //   when the same routeID is used in the following case
        //       ex) SubRoute --> The same SubRoute in the course
        // -----------------------------------------------------------
        if (ObjectIdentifier.equalsWithValue(aSubRouteID, strDurablecurrentOperationInfoGetout.getRouteID())) {
            log.info("{}", "currentRouteID = aSubRouteID");
            Validations.check(true, retCodeConfig.getInvalidBranchRouteId());
        }

        Infos.DurableOriginalRouteListGetOut strDurableoriginalRouteListGetout;
        Infos.DurableOriginalRouteListGetIn strDurableoriginalRouteListGetin = new Infos.DurableOriginalRouteListGetIn();
        strDurableoriginalRouteListGetin.setDurableCategory(strInParm.getDurableCategory());
        strDurableoriginalRouteListGetin.setDurableID(strInParm.getDurableID());
        strDurableoriginalRouteListGetout = durableMethod.durableOriginalRouteListGet(strObjCommonIn, strDurableoriginalRouteListGetin);

        int orgRouteLen = CimArrayUtils.getSize(strDurableoriginalRouteListGetout.getOriginalRouteID());
        for (int orgRouteCnt = 0; orgRouteCnt < orgRouteLen; orgRouteCnt++) {

            if (ObjectIdentifier.equalsWithValue(aSubRouteID, strDurableoriginalRouteListGetout.getOriginalRouteID().get(orgRouteCnt))) {
                log.info("{} {}", "orgRouteCnt                         = ", orgRouteCnt);
                Validations.check(true, retCodeConfig.getInvalidBranchRouteId());
            }
        }

        //---------------------------------
        //   Check Max Rework Count
        //---------------------------------
        if (CimBooleanUtils.isFalse(strReworkDurableReqInParam.getBForceRework())) {
            log.info("{}", "bForceRework == FALSE");

            Infos.ProcessDurableReworkCountCheckIn strProcessdurableReworkCountCheckin = new Infos.ProcessDurableReworkCountCheckIn();
            strProcessdurableReworkCountCheckin.setDurableCategory(strInParm.getDurableCategory());
            strProcessdurableReworkCountCheckin.setDurableID(strInParm.getDurableID());
            processForDurableMethod.processDurableReworkCountCheck(strObjCommonIn, strProcessdurableReworkCountCheckin);
        }

        //-----------------------------------------------------------------------
        //   Change State
        //-----------------------------------------------------------------------
        Infos.ProcessDurableReworkCountIncrementIn strProcessdurableReworkCountIncrementin = new Infos.ProcessDurableReworkCountIncrementIn();
        strProcessdurableReworkCountIncrementin.setDurableCategory(strInParm.getDurableCategory());
        strProcessdurableReworkCountIncrementin.setDurableID(strInParm.getDurableID());
        processForDurableMethod.processDurableReworkCountIncrement(strObjCommonIn, strProcessdurableReworkCountIncrementin);

        Inputs.OldCurrentPOData strProcessBranchRouteForDurableout;
        Infos.ProcessBranchRouteForDurableIn strProcessBranchRouteForDurablein = new Infos.ProcessBranchRouteForDurableIn();
        strProcessBranchRouteForDurablein.setDurableCategory(strInParm.getDurableCategory());
        strProcessBranchRouteForDurablein.setDurableID(strInParm.getDurableID());
        strProcessBranchRouteForDurablein.setSubRouteID(aSubRouteID);
        strProcessBranchRouteForDurablein.setReturnOperationNumber(returnOperationNumberVar);
        strProcessBranchRouteForDurableout = processForDurableMethod.processBranchRouteForDurable(strObjCommonIn, strProcessBranchRouteForDurablein);

        //-----------------------------------------------------------------------
        //   Make History
        //-----------------------------------------------------------------------
        Infos.DurableReworkEventMakeIn strDurableReworkEventMakein = new Infos.DurableReworkEventMakeIn();
        strDurableReworkEventMakein.setDurableCategory(strInParm.getDurableCategory());
        strDurableReworkEventMakein.setDurableID(strInParm.getDurableID());
        strDurableReworkEventMakein.setStrOldCurrentPOData(strProcessBranchRouteForDurableout);
        strDurableReworkEventMakein.setReasonCodeID(strInParm.getReasonCodeID());
        strDurableReworkEventMakein.setTransactionID("ODRBW038");
        strDurableReworkEventMakein.setClaimMemo(claimMemo);
        eventMethod.durableReworkEventMake(strObjCommonIn, strDurableReworkEventMakein);

        //---------------------------------------
        // Return
        //---------------------------------------
        log.info("PPTManager_i:: txReworkDurableReq");
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOpeLocateReqInParam
     * @param claimMemo
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/28 13:31
     */
    @Override
    public void sxDrbSkipReq(Infos.ObjCommon strObjCommonIn, Params.DurableOpeLocateReqInParam strDurableOpeLocateReqInParam, String claimMemo) {
        log.info("PPTManager_i:: txDurableOpeLocateReq ");

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Params.DurableOpeLocateReqInParam strInParm = strDurableOpeLocateReqInParam;
        log.info("{} {}", "in-parm durableCategory        ", strInParm.getDurableCategory());
        log.info("{} {}", "in-parm durableID              ", ObjectIdentifier.fetchValue(strInParm.getDurableID()));
        log.info("{} {}", "in-parm locateDirection        ", strInParm.getLocateDirection());
        log.info("{} {}", "in-parm currentRouteID         ", ObjectIdentifier.fetchValue(strInParm.getCurrentRouteID()));
        log.info("{} {}", "in-parm currentOperationNumber ", strInParm.getCurrentOperationNumber());
        log.info("{} {}", "in-parm routeID                ", ObjectIdentifier.fetchValue(strInParm.getRouteID()));
        log.info("{} {}", "in-parm operationID            ", ObjectIdentifier.fetchValue(strInParm.getOperationID()));
        log.info("{} {}", "in-parm operationNumber        ", strInParm.getOperationNumber());
        log.info("{} {}", "in-parm seqno                  ", strInParm.getSeqno());

        Infos.ProcessRef aProcessRef = new Infos.ProcessRef();
        aProcessRef.setProcessFlow(strDurableOpeLocateReqInParam.getProcessRef().getProcessFlow());
        aProcessRef.setProcessOperationSpecification(strDurableOpeLocateReqInParam.getProcessRef().getProcessOperationSpecification());
        aProcessRef.setMainProcessFlow(strDurableOpeLocateReqInParam.getProcessRef().getMainProcessFlow());
        aProcessRef.setModuleNumber(strDurableOpeLocateReqInParam.getProcessRef().getModuleNumber());
        aProcessRef.setModuleProcessFlow(strDurableOpeLocateReqInParam.getProcessRef().getModuleProcessFlow());
        aProcessRef.setModulePOS(strDurableOpeLocateReqInParam.getProcessRef().getModulePOS());

        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), strInParm.getDurableCategory());
        }

        //---------------------------------------
        //  Object Lock
        //---------------------------------------
        if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectLock(strObjCommonIn, CimCassette.class, strInParm.getDurableID());
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectLock(strObjCommonIn, CimReticlePod.class, strInParm.getDurableID());
        } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{} {}", "calling object_Lock()", BizConstant.SP_CLASSNAME_POSPROCESSDURABLE);
            objectLockMethod.objectLock(strObjCommonIn, CimProcessDurable.class, strInParm.getDurableID());
        }

        //--------------------------------------------------------------------------
        //  Check whether Durable is on the specified Route/Operation or Not
        //--------------------------------------------------------------------------
        if (CimStringUtils.length(ObjectIdentifier.fetchValue(strInParm.getCurrentRouteID())) > 0
                && CimStringUtils.length(strInParm.getCurrentOperationNumber()) > 0) {
            log.info("{}", "durable is on the specified Route/Operation");
            Outputs.ObjDurableCurrentOperationInfoGetOut strDurablecurrentOperationInfoGetout;
            strDurablecurrentOperationInfoGetout = durableMethod.durableCurrentOperationInfoGet(strObjCommonIn, strInParm.getDurableCategory(),
                    strInParm.getDurableID());

            if (!CimStringUtils.equals(ObjectIdentifier.fetchValue(strInParm.getCurrentRouteID()), ObjectIdentifier.fetchValue(strDurablecurrentOperationInfoGetout.getRouteID()))
                    || !CimStringUtils.equals(strInParm.getCurrentOperationNumber(), strDurablecurrentOperationInfoGetout.getOperationNumber())) {
                Validations.check(true, retCodeConfig.getNotSameRoute(), "Input parameter's currentRouteID/currentOperationNumber",
                        "Durables' current currentRouteID/currentOperationNumber");
            }
        }

        //---------------------------------------
        //  Check Durable OnRoute
        //---------------------------------------
        try {
            durableMethod.durableOnRouteCheck(strObjCommonIn, strInParm.getDurableCategory(), strInParm.getDurableID());
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                throw e;
            }
        }

        //---------------------------------------
        //  Call durable_status_CheckForOperation
        //---------------------------------------
        durableMethod.durableStatusCheckForOperation(strObjCommonIn, BizConstant.SP_OPERATION_LOCATE,
                strInParm.getDurableID(),
                strInParm.getDurableCategory());

        //---------------------------------------
        //  Check Durable Hold State
        //---------------------------------------
        String strDurableholdStateGetout;
        strDurableholdStateGetout = durableMethod.durableHoldStateGet(strObjCommonIn, strInParm.getDurableCategory(), strInParm.getDurableID());
        if (CimStringUtils.equals(strDurableholdStateGetout, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)
                && !CimStringUtils.equals(strObjCommonIn.getTransactionID(), "ODRBW024")) {
            Validations.check(true, retCodeConfigEx.getInvalidDurableStat(), strDurableholdStateGetout);
        }

        //---------------------------------------
        //  Check Durable Process State
        //---------------------------------------
        String strDurableprocessStateGetout;
        strDurableprocessStateGetout = durableMethod.durableProcessStateGet(strObjCommonIn, strInParm.getDurableCategory(), strInParm.getDurableID());
        if (!CimStringUtils.equals(strDurableprocessStateGetout, BizConstant.SP_DURABLE_PROCSTATE_WAITING)) {
            log.info("{}", "durableProcessState != SP_Durable_ProcState_Waiting");
            Validations.check(true, retCodeConfig.getInvalidDurableProcStat(),
                    strInParm.getDurableID().getValue(),
                    strDurableprocessStateGetout);
        }

        //---------------------------------------
        //  Check Durable Inventory State
        //---------------------------------------
        String strDurableinventoryStateGetout;
        strDurableinventoryStateGetout = durableMethod.durableInventoryStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());
        if (!CimStringUtils.equals(strDurableinventoryStateGetout, BizConstant.SP_DURABLE_INVENTORYSTATE_ONFLOOR)) {
            log.info("{}", "durableInventoryState != SP_Durable_InventoryState_OnFloor");
            Validations.check(true, retCodeConfig.getInvalidDurableInventoryStat(),
                    strInParm.getDurableID().getValue(),
                    strDurableinventoryStateGetout);
        }

        //----------------------------------------------
        //  If Durable is in post process, returns error
        //----------------------------------------------
        Boolean strDurableinPostProcessFlagGetout;
        strDurableinPostProcessFlagGetout = durableMethod.durableInPostProcessFlagGet(strObjCommonIn, strInParm.getDurableCategory(), strInParm.getDurableID());
        if (CimBooleanUtils.isTrue(strDurableinPostProcessFlagGetout)) {
            log.info("{}", "isPostProcessFlagOn == TRUE");

            /*---------------------------*/
            /* Get UserGroupID By UserID */
            /*---------------------------*/
            List<ObjectIdentifier> strPersonuserGroupListGetDRout;
            strPersonuserGroupListGetDRout = personMethod.personUserGroupListGetDR(
                    strObjCommonIn,
                    strObjCommonIn.getUser().getUserID());

            int userGroupLen = CimArrayUtils.getSize(strPersonuserGroupListGetDRout);
            log.info("{} {}", "userGroupIDs.length", userGroupLen);


            int nCnt = 0;
            for (nCnt = 0; nCnt < userGroupLen; nCnt++) {

            }

            if (nCnt == userGroupLen) {
                log.info("{}", "NOT External Post Process User!");
                Validations.check(true, retCodeConfig.getDurableInPostProcess(),
                        ObjectIdentifier.fetchValue(strInParm.getDurableID()));
            }
        }

        if (CimStringUtils.length(aProcessRef.getModulePOS()) == 0 || CimStringUtils.equals(aProcessRef.getModulePOS(), "*")) {
            log.info("{}", "processRef.modulePOS is blank or *");
            Infos.ProcessRef strProcessGetTargetOperationForDurableout;
            Infos.ProcessGetTargetOperationForDurableIn strProcessGetTargetOperationForDurablein = new Infos.ProcessGetTargetOperationForDurableIn();
            strProcessGetTargetOperationForDurablein.setDurableCategory(strInParm.getDurableCategory());
            strProcessGetTargetOperationForDurablein.setDurableID(strInParm.getDurableID());
            strProcessGetTargetOperationForDurablein.setLocateDirection(strInParm.getLocateDirection());
            strProcessGetTargetOperationForDurablein.setRouteID(strInParm.getRouteID());
            strProcessGetTargetOperationForDurablein.setOperationNumber(strInParm.getOperationNumber());

            strProcessGetTargetOperationForDurableout = processForDurableMethod.processGetTargetOperationForDurable(
                    strObjCommonIn,
                    strProcessGetTargetOperationForDurablein);

            aProcessRef = strProcessGetTargetOperationForDurableout;
            log.info("{} {}", "aProcessRef.processFlow", aProcessRef.getProcessFlow());
            log.info("{} {}", "aProcessRef.processOperationSpecification", aProcessRef.getProcessOperationSpecification());
            log.info("{} {}", "aProcessRef.mainProcessFlow", aProcessRef.getMainProcessFlow());
            log.info("{} {}", "aProcessRef.moduleNumber", aProcessRef.getModuleNumber());
            log.info("{} {}", "aProcessRef.moduleProcessFlow", aProcessRef.getModuleProcessFlow());
            log.info("{} {}", "aProcessRef.modulePOS", aProcessRef.getModulePOS());
        }

        //---------------------------------------
        //  Check Durable ControlJob ID
        //---------------------------------------
        ObjectIdentifier strDurabledurableControlJobIDGetout;
        strDurabledurableControlJobIDGetout = durableMethod.durableDurableControlJobIDGet(strObjCommonIn,
                strInParm.getDurableID(),
                strInParm.getDurableCategory());
        if (CimStringUtils.length(ObjectIdentifier.fetchValue(strDurabledurableControlJobIDGetout)) > 0) {
            log.info("{}", "durableControlJobID is not blank");
            Validations.check(retCodeConfig.getDurableControlJobFilled());
        }

        //-----------------------------------------------------------
        // Call Locate Durable
        //-----------------------------------------------------------
        Infos.ProcessLocateForDurableOut strProcessLocateForDurableout;
        Infos.ProcessLocateForDurableIn strProcessLocateForDurablein = new Infos.ProcessLocateForDurableIn();
        strProcessLocateForDurablein.setDurableCategory(strInParm.getDurableCategory());
        strProcessLocateForDurablein.setDurableID(strInParm.getDurableID());
        strProcessLocateForDurablein.setStrProcessRef(aProcessRef);
        strProcessLocateForDurablein.setSeqno(strInParm.getSeqno());
        strProcessLocateForDurableout = processForDurableMethod.processLocateForDurable(strObjCommonIn, strProcessLocateForDurablein);

        //-----------------------------------------------------------
        // Create Durable Operation Move Event
        //-----------------------------------------------------------
        Infos.DurableOperationMoveEventMakeLocateIn strDurableOperationMoveEventMakeLocatein = new Infos.DurableOperationMoveEventMakeLocateIn();
        strDurableOperationMoveEventMakeLocatein.setDurableCategory(strInParm.getDurableCategory());
        strDurableOperationMoveEventMakeLocatein.setDurableID(strInParm.getDurableID());
        strDurableOperationMoveEventMakeLocatein.setLocateDirection(strInParm.getLocateDirection());
        strDurableOperationMoveEventMakeLocatein.setStrOldCurrentPOData(strProcessLocateForDurableout.getStrOldCurrentPOData());
        strDurableOperationMoveEventMakeLocatein.setClaimMemo(claimMemo);
        if (CimStringUtils.equals(strObjCommonIn.getTransactionID(), "ODRBW024")) {
            log.info("{} {}", "transactionID is ODRBW024", strObjCommonIn.getTransactionID());
            strDurableOperationMoveEventMakeLocatein.setTransactionID("ODRBW024");
        } else {
            log.info("{} {}", "transactionID is ODRBW027", strObjCommonIn.getTransactionID());
            strDurableOperationMoveEventMakeLocatein.setTransactionID("ODRBW027");
        }
        eventMethod.durableOperationMoveEventMakeLocate(
                strObjCommonIn,
                strDurableOperationMoveEventMakeLocatein);

        /*------------------------------------------------*/
        /*   Call txDurableBankInReq() for Auto-Bank-In   */
        /*------------------------------------------------*/
        if (CimBooleanUtils.isTrue(strProcessLocateForDurableout.getAutoBankInFlag())) {
            log.info("{}", "autoBankInFlag == TRUE");

            Results.DurableBankInReqResult strDurableBankInReqResult = new Results.DurableBankInReqResult();
            strDurableBankInReqResult.setStrBankInDurableResults(new ArrayList<>(1));
            Params.DurableBankInReqInParam strDurableBankInReqInParam = new Params.DurableBankInReqInParam();
            strDurableBankInReqInParam.setDurableCategory(strInParm.getDurableCategory());
            strDurableBankInReqInParam.setDurableIDs(new ArrayList<>(1));
            strDurableBankInReqInParam.getDurableIDs().add(strInParm.getDurableID());
            try {
                strDurableBankInReqResult = durableService.sxDrbBankInReq(strDurableBankInReqResult, strObjCommonIn, 0, strDurableBankInReqInParam, claimMemo);
            } catch (ServiceException ex) {
                if (!Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidDurableHoldStat())) {
                    throw ex;
                }
            }
        }

        //---------------------------------------
        // Return
        //---------------------------------------
        log.info("PPTManager_i:: txDurableOpeLocateReq ");
    }

    @Override
    public void sxDurableSetReq(Infos.ObjCommon objCommon, boolean updateFlag, String className, Infos.DurableAttribute strDurableAttribute, String claimMemo) {
        //Check the input parameters
        Validations.check(ObjectIdentifier.isEmpty(strDurableAttribute.getDurableID()) || CimStringUtils.isEmpty(strDurableAttribute.getCategory()), retCodeConfig.getInvalidInputParam());

        String lockClass = null;
        String codeCategory = null;
        String durableAdministrator = null;
        int capacity = Integer.MIN_VALUE;
        int nominalSize = Integer.MIN_VALUE;

        if (CimStringUtils.equals(className, BizConstant.SP_DURABLECAT_CASSETTE)) {
            lockClass = BizConstant.SP_CLASSNAME_POSCASSETTE;
            codeCategory = BizConstant.SP_CATEGORY_CARRIERCATEGORY;
            durableAdministrator = StandardProperties.OM_CARRIER_ADMIN.getValue();
            capacity = StandardProperties.OM_CARRIER_STORE_SIZE.getIntValue();
            nominalSize = StandardProperties.OM_CARRIER_WAFER_SIZE.getIntValue();
        } else if (CimStringUtils.equals(className, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            lockClass = BizConstant.SP_CLASSNAME_POSRETICLEPOD;
            codeCategory = BizConstant.SP_CATEGORY_RETICLEPODCATEGORY;
            durableAdministrator = StandardProperties.OM_RTCLPOD_ADMIN.getValue();
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        if (!CimStringUtils.equals(durableAdministrator, BizConstant.SP_SUBSYSTEMID_MM)) {
            Validations.check(retCodeConfig.getAdministrationNotAuthrize());
        }
        String eventAction;
        Infos.DurableAttribute registAttribute = new Infos.DurableAttribute();
        BeanUtils.copyProperties(strDurableAttribute, registAttribute);

        if (CimBooleanUtils.isTrue(updateFlag)) {
            //Step1 - Lock the target object - object_Lock
            if (CimStringUtils.equals(lockClass, BizConstant.SP_CLASSNAME_POSRETICLEPOD)) {
                objectLockMethod.objectLock(objCommon, CimReticlePod.class, registAttribute.getDurableID());
            } else {
                objectLockMethod.objectLock(objCommon, CimCassette.class, registAttribute.getDurableID());
            }

            //Step2 - Check whether the target object can be updated or not - durable_CheckForUpdate
            durableMethod.durableCheckForUpdate(objCommon, className, registAttribute);

            //Step3 - Check the target object's settings - durable_settingCheck
            durableMethod.durableSettingCheck(objCommon, className, registAttribute);

            eventAction = BizConstant.SP_DURABLE_ACTION_UPDATE;
        } else {
            //------------------------------------------------------------
            // Check whether the target object can be created or not
            //------------------------------------------------------------
            objectMethod.objectCheckForCreation(objCommon, className, registAttribute.getDurableID());

            eventAction = BizConstant.SP_DURABLE_ACTION_CREATE;
        }

        //------------------------------------------------------------
        // Check the target cassette's settings
        //------------------------------------------------------------
        if (capacity >= 0 && nominalSize >= 0)    //"Cassette" is specified as the parameter className.
        {
            if (CimBooleanUtils.isTrue(registAttribute.getUsageCheckFlag())) {
                long maxRunTime = 0;
                //--------------------------------------------
                // maximum RunTime range check ( 0-999999 )
                //--------------------------------------------
                if (CimStringUtils.isNotEmpty(registAttribute.getMaximumRunTime())) {
//                    maxRunTime = BaseStaticMethod.parseToLong(registAttribute.getMaximumRunTime());
                    maxRunTime = CimNumberUtils.longValue(Long.valueOf(registAttribute.getMaximumRunTime()));
                }
                if (maxRunTime < BizConstant.SP_CASSETTE_MINIMUM_RUNTIME || BizConstant.SP_CASSETTE_MAXIMUM_RUNTIME < maxRunTime) {
                    throw new ServiceException(new OmCode(retCodeConfig.getValueRangeExceed(),
                            BizConstant.SP_DURABLE_ITEM_MAXRUNTIME, BizConstant.SP_CASSETTE_MINIMUM_RUNTIME, BizConstant.SP_CASSETTE_MAXIMUM_RUNTIME));
                }
                //-------------------------------------------------------
                // maximumOperationStartCount  range check( 0 - 999999 )
                //-------------------------------------------------------
                if (registAttribute.getMaximumOperationStartCount() < BizConstant.SP_CASSETTE_MINIMUM_OPERATIONSTARTCOUNT || BizConstant.SP_CASSETTE_MAXIMUM_OPERATIONSTARTCOUNT < registAttribute.getMaximumOperationStartCount()) {
                    throw new ServiceException(new OmCode(retCodeConfig.getValueRangeExceed(),
                            BizConstant.SP_DURABLE_ITEM_MAXOPESTARTCOUNT, BizConstant.SP_CASSETTE_MINIMUM_OPERATIONSTARTCOUNT, BizConstant.SP_CASSETTE_MAXIMUM_OPERATIONSTARTCOUNT));
                }
                //-------------------------------------------------------
                // intervalBetweenPM  range check( 0 - 999999 )
                //-------------------------------------------------------
                if (registAttribute.getIntervalBetweenPM() < BizConstant.SP_CASSETTE_MINIMUM_INTERVALBETWEENPM || BizConstant.SP_CASSETTE_MAXIMUM_INTERVALBETWEENPM < registAttribute.getIntervalBetweenPM()) {
                    throw new ServiceException(new OmCode(retCodeConfig.getValueRangeExceed(),
                            BizConstant.SP_DURABLE_ITEM_INTERVALBETWEENPM, BizConstant.SP_CASSETTE_MINIMUM_INTERVALBETWEENPM, BizConstant.SP_CASSETTE_MAXIMUM_INTERVALBETWEENPM));
                }
            }
            //-------------------------
            // usageCheckFlag is False
            //-------------------------
            else {
                //----------------------
                // maximum RunTime
                //----------------------
                int maxRunTime = 0;
                if (!CimStringUtils.isEmpty(registAttribute.getMaximumRunTime())) {
                    maxRunTime = CimNumberUtils.intValue(registAttribute.getMaximumRunTime());
                }
                Validations.check(maxRunTime != 0, new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_MAXRUNTIME, "0"));
                //---------------------------------
                // maximum OperationStartCount
                //---------------------------------
                Validations.check(registAttribute.getMaximumOperationStartCount() != 0, new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_MAXOPESTARTCOUNT, "0"));
                //-----------------------
                // intervalBetweenPM
                //-----------------------
                Validations.check(registAttribute.getIntervalBetweenPM() != 0, new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_INTERVALBETWEENPM, "0"));
            }
            //----------------------------
            // Check for creation
            //----------------------------
            if (!updateFlag) {
                if (registAttribute.getCapacity() == 0) {  //"0" is the default value of long type variables.
                    if (capacity > 0) {
                        registAttribute.setCapacity(capacity);
                    } else {
                        registAttribute.setCapacity(BizConstant.SP_CASSETTE_DEFAULT_CAPACITY);
                    }
                } else if (registAttribute.getCapacity() != capacity) {
                    throw new ServiceException(new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_CAPACITY, capacity));
                }
                //----- Nominal Size -------//
                if (registAttribute.getNominalSize() == 0) {
                    if (nominalSize > 0) {
                        registAttribute.setNominalSize(nominalSize);
                    } else {
                        registAttribute.setNominalSize(BizConstant.SP_CASSETTE_DEFAULT_NOMINALSIZE);
                    }
                } else if (registAttribute.getNominalSize() != nominalSize) {
                    Validations.check(new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_NOMINALSIZE, nominalSize));
                }
            }
        } else if (capacity == Long.MIN_VALUE && nominalSize == Long.MIN_VALUE) {     //"ReticlePod" is specified as the parameter className.
            //---------------------------
            // usageCheckFlag
            //---------------------------
            Validations.check(registAttribute.getUsageCheckFlag(), new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_USAGECHECKFLAG, "No"));
            //---------------------------
            // maximumRunTime
            //---------------------------
            int maxRunTime = 0;
            if (!CimStringUtils.isEmpty(registAttribute.getMaximumRunTime())) {
                maxRunTime = CimNumberUtils.intValue(registAttribute.getMaximumRunTime());
            }
            Validations.check(maxRunTime != 0, new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_MAXRUNTIME, "0"));
            //----------------------------------
            // maximum OperationStartCount
            //----------------------------------
            Validations.check(registAttribute.getMaximumOperationStartCount() != 0, new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_MAXOPESTARTCOUNT, "0"));
            //-------------------------------------------------------
            // intervalBetweenPM  range check( 0 - 999999 )
            //-------------------------------------------------------
            if (registAttribute.getIntervalBetweenPM() < BizConstant.SP_CASSETTE_MINIMUM_INTERVALBETWEENPM || BizConstant.SP_CASSETTE_MAXIMUM_INTERVALBETWEENPM < registAttribute.getIntervalBetweenPM()) {
                throw new ServiceException(new OmCode(retCodeConfig.getValueRangeExceed(),
                        BizConstant.SP_DURABLE_ITEM_INTERVALBETWEENPM, BizConstant.SP_CASSETTE_MINIMUM_INTERVALBETWEENPM, BizConstant.SP_CASSETTE_MAXIMUM_INTERVALBETWEENPM));
            }
            //-----------------------------
            // nominalSize
            //-----------------------------
            Validations.check(registAttribute.getNominalSize() != 0, new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_NOMINALSIZE, "0"));
            //-----------------------------
            // capacity
            //-----------------------------
            if (registAttribute.getCapacity() < BizConstant.SP_RETICLEPOD_MINIMUM_CAPACITY || registAttribute.getCapacity() > BizConstant.SP_RETICLEPOD_MAXIMUM_CAPACITY) {
                throw new ServiceException(new OmCode(retCodeConfig.getValueRangeExceed(), BizConstant.SP_DURABLE_ITEM_CAPACITY, BizConstant.SP_RETICLEPOD_MINIMUM_CAPACITY, BizConstant.SP_RETICLEPOD_MAXIMUM_CAPACITY));
            }
            //------------------------
            // contents
            //------------------------
            Validations.check(!CimStringUtils.isEmpty(registAttribute.getContents()), new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_CONTENTS, "Blank"));
        }
        if (!updateFlag) {
            //----- Instance Name -------//
            if (!CimStringUtils.isEmpty(registAttribute.getInstanceName())) {
                String instanceName = StandardProperties.OM_INSTANCE_ID.getValue();
                if (!CimStringUtils.equals(registAttribute.getInstanceName(), instanceName)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getReferenceValueDiffer(), BizConstant.SP_DURABLE_ITEM_INSTANCENAME, instanceName));
                }
            }
        }
        //-------------------------------------------------
        //  Get User Defined Data Attribute Information
        //-------------------------------------------------
        List<Infos.UserDefinedData> definedUdataSeq = new ArrayList<>();
        String classID = null;
        if (CimStringUtils.equals(lockClass, BizConstant.SP_CLASSNAME_POSCASSETTE)) {
            classID = BizConstant.SP_UDATA_POSCASSETTE;
        } else if (CimStringUtils.equals(lockClass, BizConstant.SP_CLASSNAME_POSRETICLEPOD)) {
            classID = BizConstant.SP_UDATA_POSRETICLEPOD;
        }
        Outputs.ObjUserDefinedAttributeInfoGetDROut objUserDefinedAttributeInfoGetDROut = personMethod.userDefinedAttributeInfoGetDR(objCommon, classID);
        if (objUserDefinedAttributeInfoGetDROut != null && !CimArrayUtils.isEmpty(objUserDefinedAttributeInfoGetDROut.getStrUserDefinedDataSeq())) {
            definedUdataSeq = objUserDefinedAttributeInfoGetDROut.getStrUserDefinedDataSeq();
        }
        //------------------------------------------
        // Check about Udata.
        //------------------------------------------
        Infos.DurableAttribute strRegistAttributeWithUdata = new Infos.DurableAttribute();
        BeanUtils.copyProperties(registAttribute, strRegistAttributeWithUdata);
        strRegistAttributeWithUdata.setDurableID(registAttribute.getDurableID());
        strRegistAttributeWithUdata.setDescription(registAttribute.getDescription());
        strRegistAttributeWithUdata.setCategory(registAttribute.getCategory());
        strRegistAttributeWithUdata.setUsageCheckFlag(registAttribute.getUsageCheckFlag());
        strRegistAttributeWithUdata.setMaximumRunTime(registAttribute.getMaximumRunTime());
        strRegistAttributeWithUdata.setMaximumOperationStartCount(registAttribute.getMaximumOperationStartCount());
        strRegistAttributeWithUdata.setIntervalBetweenPM(registAttribute.getIntervalBetweenPM());
        strRegistAttributeWithUdata.setCapacity(registAttribute.getCapacity());
        strRegistAttributeWithUdata.setNominalSize(registAttribute.getNominalSize());
        strRegistAttributeWithUdata.setContents(registAttribute.getContents());
        strRegistAttributeWithUdata.setInstanceName(registAttribute.getInstanceName());

        int inUdataLen = CimArrayUtils.getSize(strDurableAttribute.getUserDatas());
        int definedUdataLen = CimArrayUtils.getSize(definedUdataSeq);
        log.info("The specified Udata. count {}", inUdataLen);
        log.info("The defined   Udata. count {}", definedUdataLen);
        //------------------------------
        // Inpara vs Defined Data
        //------------------------------
        if (inUdataLen != definedUdataLen) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "The specified Udata count differs from the defined data count. Please refresh and retry ."));
        }
        //Check Name and Type.
        int i = 0;
        int j = 0;
        for (i = 0; i < inUdataLen; i++) {
            boolean findFlag = false;
            for (j = 0; j < definedUdataLen; j++) {
                Infos.UserData userData = strDurableAttribute.getUserDatas().get(i);
                log.info("Udata Name {} /  Udata Type {}", userData.getName(), userData.getType());
                if (CimStringUtils.equals(userData.getName(), definedUdataSeq.get(j).getName())
                        && CimStringUtils.equals(userData.getType(), definedUdataSeq.get(j).getType())) {
                    //------------------------------------------
                    // Check format each data type.
                    //------------------------------------------
                    //---------------------------------
                    // Integer
                    // Range : -9999999 - 9999999
                    //---------------------------------
                    String errorMsg = null;
                    if (CimStringUtils.equals(BizConstant.SP_UDATA_TYPE_INTEGER, userData.getType())) {
                        long inValCheck = 0;
                        try {
                            inValCheck = Long.parseLong(userData.getValue());
                        } catch (NumberFormatException e) {
                            errorMsg = "The specified Udata value is underflow/overflow. Please check the value of data name " + userData.getName();
                        }
                        if (BizConstant.SP_UDATA_MINIMUMVALUE_INTEGER > inValCheck) {
                            errorMsg = "The specified Udata value is underflow. Please check the value of data name " + userData.getName();
                        }
                        if (inValCheck > BizConstant.SP_UDATA_MAXIMUMVALUE_INTEGER) {
                            errorMsg = "The specified Udata value is overflow. Please check the value of data name " + userData.getName();
                        }
                    }
                    //-------------------------------------------------
                    // Float
                    // Range : -9999999.9999999 - 9999999.9999999
                    //-------------------------------------------------
                    else if (CimStringUtils.equals(BizConstant.SP_UDATA_TYPE_FLOAT, userData.getType())) {
                        double inValCheck = 0D;
                        try {
                            inValCheck = Double.parseDouble(userData.getValue());
                        } catch (NumberFormatException e) {
                            errorMsg = "The specified Udata value is underflow/overflow. Please check the value of data name " + userData.getName();
                        }
                        if (BizConstant.SP_UDATA_MINIMUMVALUE_FLOAT > inValCheck) {
                            errorMsg = "The specified Udata value is underflow. Please check the value of data name " + userData.getName();
                        }
                        if (inValCheck > BizConstant.SP_UDATA_MAXIMUMVALUE_FLOAT) {
                            errorMsg = "The specified Udata value is overflow. Please check the value of data name " + userData.getName();
                        }
                    }
                    //---------------------------------
                    // Boolean
                    // Range : 0 or 1
                    //---------------------------------
                    else if (CimStringUtils.equals(userData.getType(), BizConstant.SP_UDATA_TYPE_BOOLEAN)) {
                        if (!CimStringUtils.equals(userData.getValue(), BizConstant.SP_UDATA_TRUE)
                                && !CimStringUtils.equals(userData.getValue(), BizConstant.SP_UDATA_FALSE)) {
                            errorMsg = "The specified Udata value is overflow. Please check the value of data name " + userData.getName();
                        }
                    }
                    Validations.check(!CimStringUtils.isEmpty(errorMsg), new OmCode(retCodeConfig.getUdataCheckError(), errorMsg));
                    findFlag = true;
                    break;
                }
            }
            if (!findFlag) {
                String errorMsg = "The specified udata name and type is not found. Please refresh and retry." +
                        " name: " + strDurableAttribute.getUserDatas().get(i).getName() +
                        " type: " + strDurableAttribute.getUserDatas().get(i).getType();
                throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg(), errorMsg);
            }
        }
        //--------------------------------------
        // Set Udata (Inpara vs Current Data )
        //--------------------------------------
        int regUdataCnt = 0;
        List<Infos.UserData> tmpUserDatas = new ArrayList<>();
        strRegistAttributeWithUdata.setUserDatas(tmpUserDatas);
        for (i = 0; i < inUdataLen; i++) {
            if (CimStringUtils.equals(strDurableAttribute.getUserDatas().get(i).getOriginator(), BizConstant.SP_USERDATA_ORIG_SM)) {
                //Set Udata Item.
                tmpUserDatas.add(strDurableAttribute.getUserDatas().get(i));
            }
        }
        //Check whether the specified category exists or not
        List<ObjectIdentifier> codeList = new ArrayList<>();
        codeList.add(new ObjectIdentifier(registAttribute.getCategory()));

        //Step6 - code_CheckExistanceDR
        codeMethod.codeCheckExistanceDR(objCommon, codeCategory, codeList);

        //Step7 - Register the target object - durable_Regist__090
        durableMethod.durableRegist(objCommon, updateFlag, className, registAttribute);

        // Step8 - durable_registEvent_Make
        eventMethod.durableRegistEventMake(objCommon, objCommon.getTransactionID(), eventAction, className, registAttribute, claimMemo);
    }

    @Override
    public void sxDurableTransferJobStatusRpt(Infos.ObjCommon objCommon, Params.DurableTransferJobStatusRptParams params) {
        //---------------------------------
        // Make transfer job event
        //---------------------------------
        Inputs.DurableXferJobStatusChangeEventMakeIn durableXferJobStatusChangeEventMakeIn = new Inputs.DurableXferJobStatusChangeEventMakeIn();
        durableXferJobStatusChangeEventMakeIn.setDurableType(params.getDurableType());
        durableXferJobStatusChangeEventMakeIn.setEventTime(params.getEventTime());
        durableXferJobStatusChangeEventMakeIn.setOperationCategory(params.getOperationCategory());
        durableXferJobStatusChangeEventMakeIn.setJobID(params.getJobID());
        durableXferJobStatusChangeEventMakeIn.setJobStatus(params.getJobStatus());
        durableXferJobStatusChangeEventMakeIn.setTransportType(params.getTransportType());
        durableXferJobStatusChangeEventMakeIn.setClaimUserID(params.getClaimUserID());
        durableXferJobStatusChangeEventMakeIn.setStrCarrierJobResult(params.getStrCarrierJobResult());
        durableXferJobStatusChangeEventMakeIn.setClaimMemo(params.getClaimMemo());
        // durableXferJobStatusChangeEvent_Make()
        eventMethod.durableXferJobStatusChangeEventMake(objCommon, durableXferJobStatusChangeEventMakeIn);
    }

    /**
     * This function releases the specified DurableHold record.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/7/18 12:17
     */
    @Override
    public void sxHoldDrbReleaseReq(Infos.ObjCommon objCommon, Infos.HoldDurableReleaseReqInParam paramsIn, String claimMemo) {
        Validations.check(null == objCommon || null == paramsIn, retCodeConfig.getInvalidInputParam());
        String durableCategory = paramsIn.getDurableCategory();
        ObjectIdentifier durableID = paramsIn.getDurableID();
        ObjectIdentifier releaseReasonCodeID = paramsIn.getReleaseReasonCodeID();
        List<Infos.DurableHoldList> strDurableHoldList = paramsIn.getStrDurableHoldList();
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        int holdListLen = CimArrayUtils.getSize(strDurableHoldList);
        log.info("in-parm durableCategory             {}", durableCategory);
        log.info("in-parm durableID                   {}", durableID);
        log.info("in-parm releaseReasonCodeID         {}", releaseReasonCodeID);
        log.info("in-parm strDurableHoldList.length() {}", holdListLen);

        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        Validations.check(!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE), retCodeConfig.getInvalidDurableCategory(), durableCategory);

        //---------------------------------------
        // Check Durable Hold List
        //---------------------------------------
        Validations.check(holdListLen == 0, retCodeConfig.getInvalidInputParam());

        //---------------------------------------
        // Object Lock
        //---------------------------------------
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("calling object_Lock() {}", BizConstant.SP_CLASSNAME_POSCASSETTE);
            objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("calling object_Lock() {}", BizConstant.SP_CLASSNAME_POSRETICLEPOD);
            objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("calling object_Lock() {}", BizConstant.SP_CLASSNAME_POSRETICLE);
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
        }

        //---------------------------------------------
        // Check Release Season Code for LOCR
        //---------------------------------------------
        boolean bDurableLockReleaseFlag = false;
        if (ObjectIdentifier.equalsWithValue(releaseReasonCodeID, BizConstant.SP_REASON_DURABLELOCKRELEASE)) {
            log.info("releaseReasonCodeID == SP_Reason_DurableLockRelease");
            bDurableLockReleaseFlag = true;

            for (Infos.DurableHoldList durableHoldList : strDurableHoldList) {
                Validations.check(!ObjectIdentifier.equalsWithValue(durableHoldList.getHoldReasonCodeID(), BizConstant.SP_REASON_DURABLELOCK), retCodeConfigEx.getDurableCannotHoldReleaseWithLocr());
            }
        } else {
            for (Infos.DurableHoldList durableHoldList : strDurableHoldList) {
                Validations.check(ObjectIdentifier.equalsWithValue(durableHoldList.getHoldReasonCodeID(), BizConstant.SP_REASON_DURABLELOCK), retCodeConfigEx.getDurableCannotHoldReleaseForLock());
            }
        }

        if (!bDurableLockReleaseFlag) {
            log.info("bDurableLockReleaseFlag = FALSE");

            //---------------------------------------
            // Call durable_status_CheckForOperation
            //---------------------------------------
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE, durableID, durableCategory);
        }

        //---------------------------------------
        // Check Durable Hold State
        //---------------------------------------
        String durableHoldState = durableMethod.durableHoldStateGet(objCommon, durableCategory, durableID);
        Validations.check(CimStringUtils.equals(durableHoldState, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD), retCodeConfigEx.getInvalidDurableStat(), durableHoldState);

        //----------------------------------
        //  Get InPostProcessFlag of durable
        //----------------------------------
        boolean isPostProcessFlagOn = durableMethod.durableInPostProcessFlagGet(objCommon, durableCategory, durableID);

        //----------------------------------------------
        //  If Durable is in post process, returns error
        //----------------------------------------------
        if (isPostProcessFlagOn && CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("Durable is in post process.");

            //-----------------------------------------------------------
            // Check cassette interFabXferState
            //-----------------------------------------------------------
            String cassetteInterFabXferState = cassetteMethod.cassetteInterFabXferStateGet(objCommon, durableID);

            if (!CimStringUtils.equals(cassetteInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
                log.info("interFabXferState == SP_InterFab_XferState_Required");

                /*---------------------------*/
                /* Get UserGroupID By UserID */
                /*---------------------------*/
                List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());


                int nCnt;
                int userGroupLen = CimArrayUtils.getSize(userGroupIDs);
                for (nCnt = 0; nCnt < userGroupLen; nCnt++) {
                    ObjectIdentifier userGroupID = userGroupIDs.get(nCnt);
                    log.info("# Loop[nCnt]/userID {} {}", nCnt, userGroupID);
                }
                Validations.check(nCnt == userGroupLen, retCodeConfig.getDurableInPostProcess());
            }
        }

        /*-----------------------------------------------------------*/
        /*   Check PosCode                                           */
        /*-----------------------------------------------------------*/
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_DURABLEHOLDRELEASE, Collections.singletonList(releaseReasonCodeID));

        //-----------------------------------------------------------
        // Call Durable Hold Release
        //-----------------------------------------------------------
        List<Infos.DurableHoldHistory> durableHoldHistories = durableMethod.durableHoldRelease(objCommon, durableCategory, durableID, strDurableHoldList, releaseReasonCodeID);

        //-----------------------------------------------------------
        // Create Durable Hold Release Event
        //-----------------------------------------------------------
        if (!ObjectIdentifier.equalsWithValue(releaseReasonCodeID, BizConstant.SP_REASON_DURABLELOCKRELEASE)) {
            for (Infos.DurableHoldHistory durableHoldHistory : durableHoldHistories) {
                durableHoldHistory.setReleaseClaimMemo(claimMemo);
            }

            log.info("input-param releaseReasonCodeID != SP_Reason_DurableLockRelease {}", releaseReasonCodeID);
            Infos.DurableHoldEventMakeIn durableHoldEventMakeIn = new Infos.DurableHoldEventMakeIn();
            durableHoldEventMakeIn.setDurableCategory(durableCategory);
            durableHoldEventMakeIn.setDurableID(durableID);
            durableHoldEventMakeIn.setTransactionID(TransactionIDEnum.HOLD_DURABLE_RELEASE_REQ.getValue());
            durableHoldEventMakeIn.setStrHoldHistoryList(durableHoldHistories);
            eventMethod.durableHoldEventMake(objCommon, durableHoldEventMakeIn);
        }
    }

    @Override
    public void sxHoldDrbReq(Infos.ObjCommon objCommon, Params.HoldDurableReqInParam holdDurableReqInParam, String claimMemo) {
        String durableCategory = holdDurableReqInParam.getDurableCategory();
        ObjectIdentifier durableID = holdDurableReqInParam.getDurableID();

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        List<Infos.DurableHoldList> durableHoldLists = holdDurableReqInParam.getDurableHoldLists();

        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            Validations.check(true, retCodeConfig.getInvalidDurableCategory());
        }
        //---------------------------------------
        // Check Durable Hold List
        //---------------------------------------
        if (durableHoldLists.size() == 0) {
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        //---------------------------------------
        // Object Lock
        //---------------------------------------

        //---------------------------------------
        // Object Lock
        //---------------------------------------
        if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)) {
            objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)) {
            objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
        }

        //---------------------------------------
        // Check Durable OnRoute
        //---------------------------------------
        try {
            durableMethod.durableOnRouteCheck(objCommon, durableCategory, durableID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                throw e;
            }
        }
        //------------------------------------------------------
        // Judgement of Hold Reason of LOCK
        //------------------------------------------------------
        boolean bDurableLockFlag = false;
        for (Infos.DurableHoldList durableHoldList : durableHoldLists) {
            if (CimStringUtils.equals(BizConstant.SP_REASON_DURABLELOCK, durableCategory)) {
                log.info("Hold Reason Code is LOCK!");
                bDurableLockFlag = true;
                if (!CimStringUtils.equals(objCommon.getTransactionID(), "TXTRC074") &&
                        !CimStringUtils.equals(objCommon.getTransactionID(), "TXTRC085") &&
                        !CimStringUtils.equals(objCommon.getTransactionID(), "ODRBW032")) {
                    if (BizConstant.SP_REASON_RUNNINGHOLD.equals(durableHoldList.getHoldReasonCodeID().getValue())) {
                        Validations.check(retCodeConfig.getInvalidReasonCodeFromClient());
                    }

                }

            }
        }


        if (!bDurableLockFlag) {
            log.info("{}", "bDurableLockFlag = FALSE");
            //---------------------------------------
            // Call durable_status_CheckForOperation
            //---------------------------------------
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATIONCATEGORY_BANKIN, durableID, durableCategory);

        }
        //---------------------------------------
        // Check Durable Process State
        //---------------------------------------
        String durableProcessStateGetOut = durableMethod.durableProcessStateGet(objCommon, durableCategory, durableID);
        if (CimStringUtils.equals(durableProcessStateGetOut, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING) && !bDurableLockFlag) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidDurableProcStat(), durableID.getValue(), durableProcessStateGetOut));
        }

        //----------------------------------------------
        //  If Durable is in post process, returns error
        //----------------------------------------------
        Boolean strDurableinPostProcessFlagGetout;
        strDurableinPostProcessFlagGetout = durableMethod.durableInPostProcessFlagGet(objCommon, durableCategory,
                durableID);
        if (CimBooleanUtils.isTrue(strDurableinPostProcessFlagGetout) && CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "isPostProcessFlagOn == TRUE");

            String strCassetteInterFabXferStateGetOut = cassetteMethod.cassetteInterFabXferStateGet(objCommon, durableID);
            if (CimStringUtils.equals(strCassetteInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
                log.info("{}", "interFabXferState != SP_InterFab_XferState_Required");
                /*---------------------------*/
                /* Get UserGroupID By UserID */
                /*---------------------------*/
                List<ObjectIdentifier> strPersonuserGroupListGetDRout;
                strPersonuserGroupListGetDRout = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());

                int userGroupLen = CimArrayUtils.getSize(strPersonuserGroupListGetDRout);
                log.info("{} {}", "userGroupIDs.length", userGroupLen);

                int nCnt = 0;
                for (nCnt = 0; nCnt < userGroupLen; nCnt++) {

                }
                Validations.check(nCnt == userGroupLen, retCodeConfig.getDurableInPostProcess(), ObjectIdentifier.fetchValue(durableID));
            }
        }


        /*-----------------------------------------------------------*/
        /*   Check PosCode                                           */
        /*-----------------------------------------------------------*/
        for (Infos.DurableHoldList durableHoldList : durableHoldLists) {
            List<ObjectIdentifier> strCheckedCodes = new ArrayList<>();
            strCheckedCodes.add(durableHoldList.getHoldReasonCodeID());
            try {
                codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_DURABLEHOLD, strCheckedCodes);
            } catch (ServiceException e) {
                if (CimStringUtils.equals(TransactionIDEnum.HOLD_DURABLE_REQ.getValue(), objCommon.getTransactionID())) {
                    throw e;
                } else {
                    codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_FUTUREHOLD, strCheckedCodes);
                }
            }
        }

        //-----------------------------------------------------------
        // Call Durable Hold
        //-----------------------------------------------------------
        Infos.DurableHoldIn durableHoldIn = new Infos.DurableHoldIn();
        durableHoldIn.setDurableID(holdDurableReqInParam.getDurableID());
        durableHoldIn.setDurableCategory(holdDurableReqInParam.getDurableCategory());
        durableHoldIn.setStrDurableHoldList(holdDurableReqInParam.getDurableHoldLists());
        List<Infos.DurableHoldHistory> durableHoldHistories = durableMethod.durableHold(objCommon, durableHoldIn);

        //-----------------------------------------------------------
        // Create Durable Hold Event
        //-----------------------------------------------------------
        Infos.DurableHoldEventMakeIn durableHoldEventMakeIn = new Infos.DurableHoldEventMakeIn();
        durableHoldEventMakeIn.setDurableCategory(durableCategory);
        durableHoldEventMakeIn.setDurableID(durableID);
        durableHoldEventMakeIn.setTransactionID(TransactionIDEnum.HOLD_DURABLE_REQ.getValue());
        durableHoldEventMakeIn.setStrHoldHistoryList(durableHoldHistories);
        eventMethod.durableHoldEventMake(objCommon, durableHoldEventMakeIn);
    }

    @Override
    public void sxMultiDurableStatusChangeReq(Infos.ObjCommon objCommon, Params.MultiDurableStatusChangeReqParams params) {
        log.info("MultiDurableStatusChangeReqParams = {}", params);

        Infos.MultiDurableStatusChangeReqInParm parm = params.getParm();
        String durableCategory = parm.getDurableCategory();
        String durableStatus = parm.getDurableStatus();
        ObjectIdentifier durableSubStatus = parm.getDurableSubStatus();
        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        log.info("Check Durable Category");
        if (!BizConstant.SP_DURABLECAT_CASSETTE.equals(durableCategory)
                && !BizConstant.SP_DURABLECAT_RETICLE.equals(durableCategory)
                && !BizConstant.SP_DURABLECAT_RETICLEPOD.equals(durableCategory)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }
        //-----------------------------------------------------------------------//
        //   Decide TX_ID                                                        //
        //-----------------------------------------------------------------------//
        String txID = null;
        if (CimStringUtils.equals(parm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            txID = TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_CARRIER.getValue();
        } else if (CimStringUtils.equals(parm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            txID = TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_RETICLE.getValue();
        } else if (CimStringUtils.equals(parm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            txID = TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_RETICLE_POD.getValue();
        }
        //---------------------------------------
        // ObjectSequence Lock
        //---------------------------------------
        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos = parm.getStatusChangeDurableInfos();
        int durableLen = CimArrayUtils.getSize(statusChangeDurableInfos);
        for (int i = 0; i < durableLen; i++) {
            durableIDs.add(statusChangeDurableInfos.get(i).getDurableID());
        }
        if (CimStringUtils.equals(parm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, durableIDs);
        } else if (CimStringUtils.equals(parm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            objectLockMethod.objectSequenceLock(objCommon, CimReticlePod.class, durableIDs);
        } else if (CimStringUtils.equals(parm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            objectLockMethod.objectSequenceLock(objCommon, CimProcessDurable.class, durableIDs);
        }
        if (!CimObjectUtils.isEmpty(statusChangeDurableInfos)) {
            for (Infos.StatusChangeDurableInfo statusChangeDurableInfo : statusChangeDurableInfos) {
                ObjectIdentifier durableID = statusChangeDurableInfo.getDurableID();
                String currentDurableStatus = statusChangeDurableInfo.getDurableStatus();
                ObjectIdentifier currentDurableSubStatus = statusChangeDurableInfo.getDurableSubStatus();
                if (BizConstant.SP_DURABLECAT_CASSETTE.equals(durableCategory)) {
                    log.info("durableCategory is Cassette");
                    //-----------------------------------------------------------
                    // Check cassette interFabXferState
                    //-----------------------------------------------------------
                    log.info("Check cassette interFabXferState");
                    //-----------------------------------------------------------
                    // "Transferring"
                    //-----------------------------------------------------------
                    String interFabXferState = cassetteMethod.cassetteInterFabXferStateGet(objCommon, durableID);
                    Validations.check(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING.equals(interFabXferState),
                            new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), durableID.getValue(), interFabXferState));
                }

                if (CIMStateConst.CIM_DURABLE_AVAILABLE.equals(durableStatus) || CIMStateConst.CIM_DURABLE_INUSE.equals(durableStatus)) {
                    log.info("Change to Available or InUse");

                    Infos.DurableSubStateGetOut durableSubStateGet = durableMethod.durableSubStateGet(objCommon, durableCategory, durableID);

                    if (null == durableSubStateGet || ObjectIdentifier.isEmptyWithValue(durableSubStateGet.getDurableSubStatus())) {
                        log.info("durableSubStatus is blank");
                        //---------------------------------------
                        // Check Durable OnRoute
                        //---------------------------------------
                        log.info("Check Durable OnRoute");
                        try {
                            durableMethod.durableOnRouteCheck(objCommon, durableCategory, durableID);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                                log.info("durableOnRouteCheck returnCode == RC_DURABLE_ONROUTE");
                                //---------------------------------------
                                // Check Durable Inventory State
                                //---------------------------------------
                                log.info("Check Durable Inventory State");
                                String durableInventoryStateGet = durableMethod.durableInventoryStateGet(objCommon, durableCategory, durableID);
                                if (CimStringUtils.equals(durableInventoryStateGet, BizConstant.SP_DURABLE_INVENTORYSTATE_ONFLOOR)) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getDurableCannotStatechangeOnfloor(), ObjectIdentifier.fetchValue(durableID)));
                                }
                            }
                        }
                    }
                }

                //---------------------------------------
                // Check Durable Sub Status transition
                //---------------------------------------
                log.info("Check Durable Sub Status transition");
                Inputs.ObjDurableCurrentStateCheckTransitionIn transitionIn = new Inputs.ObjDurableCurrentStateCheckTransitionIn();
                transitionIn.setDurableCategory(durableCategory);
                transitionIn.setDurableID(durableID);
                transitionIn.setDurableStatus(durableStatus);
                transitionIn.setDurableSubStatus(durableSubStatus);
                transitionIn.setCurrentDurableStatus(currentDurableStatus);
                transitionIn.setCurrentDurableSubStatus(currentDurableSubStatus);
                durableMethod.durableCurrentStateCheckTransition(objCommon, transitionIn);


                //---------------------------------------
                // Change durable status & sub-status
                //---------------------------------------
                log.info("Change durable status & sub-status");
                Inputs.ObjDurableCurrentStateChangeIn changeIn = new Inputs.ObjDurableCurrentStateChangeIn();
                changeIn.setDurableCategory(durableCategory);
                changeIn.setDurableID(durableID);
                changeIn.setDurableStatus(durableStatus);
                changeIn.setDurableSubStatus(durableSubStatus);
                changeIn.setReticleLocation(parm.getReticleLocation());
                durableMethod.durableCurrentStateChange(objCommon, changeIn);


                //---------------------------------------
                // Create Durable Change Event
                //---------------------------------------
                eventMethod.durableChangeEventMake(objCommon, txID, statusChangeDurableInfo.getDurableID(), parm.getDurableCategory(), BizConstant.SP_DURABLEEVENT_ACTION_STATECHANGE, params.getClaimMemo());
            }
        }
    }

    @Override
    public void sxCarrierDispatchAttrChgReq(Infos.ObjCommon objCommon, Params.CarrierDispatchAttrChgReqParm carrierTransferJobEndRptParams) {
        //-----------------------------------------------------------
        //    Object Lock for Cassette
        //-----------------------------------------------------------
        objectLockMethod.objectLock(objCommon, CimCassette.class, carrierTransferJobEndRptParams.getCassetteID());

        //-----------------------------------------------------------
        //    Update cassette's dispatching attribute
        //-----------------------------------------------------------

        cassetteMethod.cassetteDispatchAttributeUpdate(objCommon, carrierTransferJobEndRptParams.getCassetteID(), carrierTransferJobEndRptParams.getSetFlag(), carrierTransferJobEndRptParams.getActionCode());
        //-----------------------------------------------------------
        //    Return to Caller
        //-----------------------------------------------------------

    }


    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param user             user
     * @param reticlePodStatus reticlePodStatus
     * @param reticlePodIDList reticlePodIDList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/29 11:24:44
     */
    @Override
    public void sxMultipleReticlePodStatusChangeRpt(Infos.ObjCommon objCommon, User user, String reticlePodStatus, List<ObjectIdentifier> reticlePodIDList) {
        if (CimArrayUtils.isEmpty(reticlePodIDList)) {
            throw new ServiceException(new ErrorCode("reticlePodIDList is empty!"));
        }
        for (ObjectIdentifier reticlePodID : reticlePodIDList) {
            if (CimStringUtils.equals(reticlePodStatus, CIMStateConst.CIM_DURABLE_AVAILABLE)
                    || CimStringUtils.equals(reticlePodStatus, CIMStateConst.CIM_DURABLE_INUSE)) {
                //Check durable OnRoute
                //Step1 - durable_OnRoute_Check
                try {
                    durableMethod.durableOnRouteCheck(objCommon, CIMStateConst.SP_DURABLE_CAT_RETICLE_POD, reticlePodID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                        //Check durable Inventory State
                        //Step2 - durable_inventoryState_Get
                        String durableInventoryStateGetOut = durableMethod.durableInventoryStateGet(objCommon, CIMStateConst.SP_DURABLE_CAT_RETICLE_POD, reticlePodID);
                        Validations.check(CimStringUtils.equals(durableInventoryStateGetOut, CIMStateConst.CIM_LOT_INVENTORY_STATE_ONFLOOR), retCodeConfig.getDurableCannotStatechangeOnfloor());
                    } else {
                        throw e;
                    }

                }
            }
            //Step3 - durable_subState_Get
            Infos.DurableSubStateGetOut durableSubStateGetOut = durableMethod.durableSubStateGet(objCommon, CIMStateConst.SP_DURABLE_CAT_RETICLE_POD, reticlePodID);

            if (!ObjectIdentifier.isEmpty(durableSubStateGetOut.getDurableSubStatus())) {
                throw new ServiceException(retCodeConfig.getDurableSubstateNotBlank());
            }
            // Check Destination Reticle Pod
            //Step4 - reticlePod_status_Change
            reticleMethod.reticlePodStatusChange(objCommon, reticlePodStatus, reticlePodID);

            // Step5 - Create durable Change Event , durableChangeEvent_Make
            eventMethod.durableChangeEventMake(objCommon, TransactionIDEnum.RETICLE_POD_MULTI_STATUS_CHANGE_RPT.getValue(), reticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD, BizConstant.SP_DURABLEEVENT_ACTION_STATECHANGE, "");
        }
    }

    @Override
    public void sxReticleAllInOutRpt(Infos.ObjCommon objCommon, Params.ReticleAllInOutRptParams params) {
        //Step1 - Check input parameter - justInOut_reticleTransferInfo_Verify
        reticleMethod.justInOutReticleTransferInfoVerify(objCommon, params.getMoveDirection(), params.getReticlePodID(), params.getMoveReticles());

        for (int i = 0; i < params.getMoveReticles().size(); i++) {
            Infos.MoveReticles moveReticles = params.getMoveReticles().get(i);
            Integer slotNumber = moveReticles.getSlotNumber();
            Validations.check(CimObjectUtils.isEmpty(slotNumber)|| CimNumberUtils.eq(0,slotNumber),retCodeConfig.getInvalidInputParam());
            if (BizConstant.equalsIgnoreCase(BizConstant.SP_MOVEDIRECTION_JUSTIN, params.getMoveDirection())) {
                //Step2 - reticle_materialContainer_JustIn
                reticleMethod.reticleMaterialContainerJustIn(objCommon, params.getReticlePodID(), moveReticles.getReticleID(), slotNumber);
                // make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, moveReticles.getReticleID());
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_JUST_IN);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                reticleOperationEventMakeParams.setReticlePodID(params.getReticlePodID().getValue());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            } else if (BizConstant.equalsIgnoreCase(BizConstant.SP_MOVEDIRECTION_JUSTOUT, params.getMoveDirection())) {
                //Step3 - reticle_materialContainer_JustOut
                reticleMethod.reticleMaterialContainerJustOut(objCommon, params.getReticlePodID(), moveReticles.getReticleID(), slotNumber);
                // make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, moveReticles.getReticleID());
                reticleOperationEventMakeParams.setOpeCategory(SP_RETICLE_JUST_OUT);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                reticleOperationEventMakeParams.setReticlePodID(params.getReticlePodID().getValue());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            }
        }
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param requestUserID
     * @param stockerID
     * @param equipmentID
     * @param strInventoryReticlePodInfo
     * @param claimMemo
     * @return com.fa.cim.dto.Results.ReticlePodInvUpdateRptResult
     * @throws
     * @author ho
     * @date 2020/3/19 12:49
     */
    @Override
    public Results.ReticlePodInvUpdateRptResult sxReticlePodInvUpdateRpt(
            Infos.ObjCommon strObjCommonIn,
            User requestUserID,
            ObjectIdentifier stockerID,
            ObjectIdentifier equipmentID,
            List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo,
            String claimMemo,
            Infos.ShelfPosition shelfPosition) {
        int nLenInventoryReticlePodInfo = CimArrayUtils.getSize(strInventoryReticlePodInfo);

        String tmpARMS = StandardProperties.OM_ARHS_FLAG.getValue();
        log.info("ARMS switch on / off  tmpARMS = {}", tmpARMS);

        if (!CimStringUtils.equals(tmpARMS, BizConstant.SP_ARMS_SWITCH_ON)) {
            log.info("ARMS switch off !! ==> return !!");
            Validations.check(true, retCodeConfig.getFunctionNotAvailable(), strObjCommonIn.getTransactionID());
        }

        OmCode chkResult = null;

        List<Infos.InventoryReticlePodInfo> tmpInventoryReticlePodInfo = new ArrayList<>();
        List<Results.InventoriedReticlePodInfo> strInventoriedReticlePodInfo = null;
        /*---------------------------------*/
        /*   Report from Reticle Stocker   */
        /*---------------------------------*/
        Boolean NotFoundRpodFlg = FALSE;

        if (CimStringUtils.length(stockerID.getValue()) != 0) {
            /*------------------------*/
            /*   Check Stocker Type   */
            /*------------------------*/
            Outputs.ObjStockerTypeGetDROut strStockerTypeGetDROut;
            strStockerTypeGetDROut = stockerMethod.stockerTypeGet(
                    strObjCommonIn,
                    stockerID);
            String stockerType = strStockerTypeGetDROut.getStockerType();

            if (!CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLESHELF)
                    && !CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.info("stockerType != SP_Stocker_Type_ReticleShelf, SP_Stocker_Type_ReticlePod");
                Validations.check(true, retCodeConfigEx.getNotReticleStocker(), stockerID.getValue());
            }

            /*-------------------------------*/
            /*   Update Reticles' Position   */
            /*-------------------------------*/
            // if there are reticles in reticlePod, and the transfer/durable status are changed
            // when reticlePod_position_UpdateByStockerInventoryDR calls reticlePod_transferState_Change

            int invRtclPodLen = 0;
            OmCode rc;
            for (int i = 0; i < nLenInventoryReticlePodInfo; i++) {
                /*-----------------------*/
                /*   Check Port Status   */
                /*-----------------------*/
                // Return to RXM reticlePodID. Check portStatus Loaded case
                Infos.ReticlePodCurrentMachineGetOut strReticlePodCurrentMachineGetOut = new Infos.ReticlePodCurrentMachineGetOut();
                try {
                    // reticlePod_currentMachine_Get
                    strReticlePodCurrentMachineGetOut = reticleMethod.reticlePodCurrentMachineGet(
                            strObjCommonIn,
                            strInventoryReticlePodInfo.get(i).getReticlePodID());
                } catch (ServiceException ex) {
                    if (Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidStockerType())) {
                        Outputs.ObjReticlePodFillInTxPDQ013DROut strReticlePodFillInTxPDQ013DROut;
                        Boolean durableOperationInfoFlag = FALSE;
                        Boolean durableWipOperationInfoFlag = FALSE;
                        Params.ReticlePodDetailInfoInqParams params = new Params.ReticlePodDetailInfoInqParams();
                        params.setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                        params.setDurableOperationInfoFlag(durableOperationInfoFlag);
                        params.setDurableWipOperationInfoFlag(durableWipOperationInfoFlag);

                        // reticlePod_FillInTxPDQ013DR__170
                        strReticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(
                                strObjCommonIn,
                                params);

                        if (CimStringUtils.length(strReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStockerID().getValue()) != 0) {
                            //Outputs.ObjStockerTypeGetDROut strStockerTypeGetDROut;

                            // stocker_type_GetDR
                            strStockerTypeGetDROut = stockerMethod.stockerTypeGet(
                                    strObjCommonIn,
                                    strReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStockerID());

                            if (CimStringUtils.equals(strStockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLESHELF)) {
                                log.info("stockerType is SP_Stocker_Type_ReticleShelf");
                                rc = retCodeConfig.getSucc();
                            } else {
                                log.info("stockerType is not SP_Stocker_Type_ReticleShelf");
                                rc = retCodeConfig.getInvalidStockerType();
                            }
                        } else {
                            rc = retCodeConfig.getInvalidStockerType();
                        }
                    }


                    if (!Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundReticlePod())) {
                        throw ex;
                    } else if (Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundReticlePod())) {
                        NotFoundRpodFlg = TRUE;
                        continue;
                    }
                }

                int machineType = 0;
                if (CimStringUtils.length(strReticlePodCurrentMachineGetOut.getCurrentMachineID().getValue()) != 0) {
                    Outputs.ObjMachineTypeGetOut strMachineTypeGetOut;

                    // machine_type_Get
                    strMachineTypeGetOut = equipmentMethod.machineTypeGet(
                            strObjCommonIn,
                            strReticlePodCurrentMachineGetOut.getCurrentMachineID());

                    if (!strMachineTypeGetOut.isBStorageMachineFlag()) {
                        machineType = 1;
                    } else if (CimStringUtils.equals(strMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                        machineType = 2;
                    }
                }

                Results.ReticlePodDetailInfoInqResult strReticlePodDetailInfoInqResult;
                Boolean durableOperationInfoFlag = FALSE;
                Boolean durableWipOperationInfoFlag = FALSE;

                Params.ReticlePodDetailInfoInqParams params = new Params.ReticlePodDetailInfoInqParams();
                params.setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                params.setDurableOperationInfoFlag(durableOperationInfoFlag);
                params.setDurableWipOperationInfoFlag(durableWipOperationInfoFlag);

                // txReticlePodDetailInfoInq__170
                strReticlePodDetailInfoInqResult = durableInqService.sxReticlePodDetailInfoInq(
                        strObjCommonIn,
                        params);

                if (machineType != 1 && machineType != 2) {
                    log.info("Return to RXM reticlePodID is in reticlePodStocker");
                    tmpInventoryReticlePodInfo.add(new Infos.InventoryReticlePodInfo());
                    tmpInventoryReticlePodInfo.get(invRtclPodLen).setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                    invRtclPodLen++;
                } else if (CimStringUtils.equals(strReticlePodDetailInfoInqResult.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    ObjectIdentifier strDurableDurableControlJobIDGetOut;

                    // durable_durableControlJobID_Get
                    strDurableDurableControlJobIDGetOut = durableMethod.durableDurableControlJobIDGet(
                            strObjCommonIn,
                            strInventoryReticlePodInfo.get(i).getReticlePodID(),
                            BizConstant.SP_DURABLECAT_RETICLEPOD);

                    if (CimStringUtils.length(strDurableDurableControlJobIDGetOut.getValue()) > 0) {
                        Infos.DurableControlJobStatusGet strDurableControlJobStatusGetOut;

                        // durableControlJob_status_Get
                        strDurableControlJobStatusGetOut = durableMethod.durableControlJobStatusGet(
                                strObjCommonIn,
                                strDurableDurableControlJobIDGetOut);

                        if (!CimStringUtils.equals(strDurableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED) &&
                                !CimStringUtils.equals(strDurableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                            Validations.check(true, retCodeConfig.getInvalidDcjstatus(), strDurableControlJobStatusGetOut.getDurableControlJobStatus());
                        }
                    }

                    // equipment_reticlePod_Unload__160
                    equipmentMethod.equipmentReticlePodUnload(
                            strObjCommonIn,
                            strReticlePodCurrentMachineGetOut.getCurrentMachineID(),
                            strReticlePodCurrentMachineGetOut.getCurrentReticlePodPortID(),
                            strInventoryReticlePodInfo.get(i).getReticlePodID(),
                            claimMemo);

                    tmpInventoryReticlePodInfo.add(new Infos.InventoryReticlePodInfo());
                    tmpInventoryReticlePodInfo.get(invRtclPodLen).setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                    invRtclPodLen++;
                } else if (CimStringUtils.equals(strReticlePodDetailInfoInqResult.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_STATIONIN)
                        && machineType == 2) {

                    // stocker_reticlePod_Unload__160
                    stockerMethod.stockerReticlePodUnload(
                            strObjCommonIn,
                            strReticlePodCurrentMachineGetOut.getCurrentMachineID(),
                            strReticlePodCurrentMachineGetOut.getCurrentReticlePodPortID(),
                            strInventoryReticlePodInfo.get(i).getReticlePodID(),
                            claimMemo, shelfPosition);
                    tmpInventoryReticlePodInfo.add(new Infos.InventoryReticlePodInfo());
                    tmpInventoryReticlePodInfo.get(invRtclPodLen).setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                    invRtclPodLen++;
                } else {
                    tmpInventoryReticlePodInfo.add(new Infos.InventoryReticlePodInfo());
                    tmpInventoryReticlePodInfo.get(invRtclPodLen).setReticlePodID(strInventoryReticlePodInfo.get(i).getReticlePodID());
                    invRtclPodLen++;
                }
            }

            List<Results.InventoriedReticlePodInfo> strReticlePodPositionUpdateByStockerInventoryDROut = null;

            try {
                // reticlePod_position_UpdateByStockerInventoryDR
                strReticlePodPositionUpdateByStockerInventoryDROut = reticleMethod.reticlePodPositionUpdateByStockerInventoryDR(
                        strObjCommonIn,
                        stockerID,
                        tmpInventoryReticlePodInfo, shelfPosition);
            } catch (ServiceException ex) {
                if (!Validations.isEquals(ex.getCode(), retCodeConfigEx.getSomertclinvDataError())) {
                    throw ex;
                }
                chkResult = retCodeConfigEx.getSomertclinvDataError();
            }


            /*-----------------------------*/
            /*   Change Inventory Status   */
            /*-----------------------------*/
            stockerMethod.stockerInventoryStateChange(
                    strObjCommonIn,
                    stockerID,
                    FALSE);
            strInventoriedReticlePodInfo = strReticlePodPositionUpdateByStockerInventoryDROut;
        } else if (CimStringUtils.length(equipmentID.getValue()) != 0) {
            /*---------------------------*/
            /*   Report from Equipment   */
            /*---------------------------*/
            log.info("Input Parameter Error");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        } else {
            /*---------------------------*/
            /*   Input Parameter Error   */
            /*---------------------------*/
            log.info("Input Parameter Error");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        Results.ReticlePodInvUpdateRptResult strReticlePodInvUpdateRptResult = new Results.ReticlePodInvUpdateRptResult();

        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        strReticlePodInvUpdateRptResult.setStockerID(stockerID);
        strReticlePodInvUpdateRptResult.setEquipmentID(equipmentID);
        strReticlePodInvUpdateRptResult.setStrInventoriedReticlePodInfo(strInventoriedReticlePodInfo);


        if (chkResult != null) {
            strReticlePodInvUpdateRptResult.setSiInfo(chkResult);
            return strReticlePodInvUpdateRptResult;
        }

        if (CimBooleanUtils.isTrue(TRUE)) {
            log.info("Inventory Upload was executed, ReticlePod does not exist in MM.");
            Validations.check(true, retCodeConfigEx.getReportedNotMmdurable());
        }

        return (strReticlePodInvUpdateRptResult);
    }


    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon    objCommon
     * @param user         user
     * @param reticlePodID reticlePodID
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/28 21:33:54
     */
    @Override
    public void sxReticlePodMaintInfoUpdateReq(Infos.ObjCommon objCommon, User user, ObjectIdentifier reticlePodID) {
        //Step1 -  Set TimeStamp and User, reticlePod_timeStamp_Set
        reticleMethod.reticlePodTimeStampSet(objCommon, reticlePodID);
        //Create durable Change Event
        // durableChangeEvent_Make
        eventMethod.durableChangeEventMake(objCommon, TransactionIDEnum.RETICLE_POD_PMINFO_RESET_REQ.getValue(), reticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD, BizConstant.SP_DURABLEEVENT_ACTION_PMRESET, "");

    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/9                          Wind
     *
     * @param objCommon
     * @param params
     * @return RetCode<Results.ReticlePodTransferStatusChangeRptResult>
     * @author Wind
     * @date 2018/11/9 13:59
     */
    @Override
    public Results.ReticlePodTransferStatusChangeRptResult sxReticlePodTransferStatusChangeRpt(Infos.ObjCommon objCommon, Params.ReticlePodTransferStatusChangeRptParams params, String claimMemo) {

        Results.ReticlePodTransferStatusChangeRptResult reticlePodTransferStatusChangeRptResult = new Results.ReticlePodTransferStatusChangeRptResult();
        int i;
        int nLenXferReticlePod = CimObjectUtils.isEmpty(params.getXferReticlePodList()) ? 0 : params.getXferReticlePodList().size();

        boolean bEquipmentFlag = false;
        String stockerType;
        /*================================================================================================*/
        /*   Get stocker type or eqp type                                                           */
        /*================================================================================================*/
        if (ObjectIdentifier.isEmpty(params.getStockerID()) ? false : CimStringUtils.isNotEmpty(params.getStockerID().getValue())) {
            /*-------------*/
            /*   stocker   */
            /*-------------*/

            //step1 - stocker_type_GetDR
            Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, params.getStockerID());
            stockerType = objStockerTypeGetDROut.getStockerType();

            /*================================================================================================*/
            /*   Get environment variable for reticlePodXferStatusChange limitation.                          */
            /*================================================================================================*/
            String envReticlePodXferStatusChangeLimitFlag = StandardProperties.OM_RTCLPOD_TRANSFER_STATUS_RPT_MODE.getValue();
            if (CimStringUtils.equals(envReticlePodXferStatusChangeLimitFlag, CIMStateConst.SP_FUNCTION_AVAILABLE_TRUE) &&
                    (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_AUTO) ||
                            CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_SHELF) ||
                            CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTERM) ||
                            CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_FIXTURE))) {

                throw new ServiceException(retCodeConfig.getInvalidStockerType());

            } else {

                // step2 - object_Lock
                objectLockMethod.objectLock(objCommon, CimStorageMachine.class, params.getStockerID());
            }
        }
        if (ObjectIdentifier.isEmpty(params.getEquipmentID()) ? false : CimStringUtils.isNotEmpty(params.getEquipmentID().getValue())) {
            /*---------------*/
            /*   eqp   */
            /*---------------*/
            bEquipmentFlag = true;

            //step3 - equipment_state_GetDR
            ObjectIdentifier strEquipmentStateGetDROut = equipmentMethod.equipmentStateGetDR(objCommon, params.getEquipmentID());
            Inputs.ObjectLockModeGetIn strObjectLockModeGetIn = new Inputs.ObjectLockModeGetIn();
            strObjectLockModeGetIn.setObjectID(params.getEquipmentID());
            strObjectLockModeGetIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strObjectLockModeGetIn.setFunctionCategory(TransactionIDEnum.RETICLE_POD_XFER_STAUES_CHANGE_RPT.getValue());
            strObjectLockModeGetIn.setUserDataUpdateFlag(false);

            // step4 - object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.RETICLE_POD_XFER_STAUES_CHANGE_RPT.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            Long lockMode = objLockModeOut.getLockMode();
            if (lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                // step5 - object_Lock
                objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
            }
        }
        /*================================================================================================*/
        /*   Make transferStatus and Change Transfer Status                                               */
        /*================================================================================================*/
        String transferStatus;
        for (i = 0; i < nLenXferReticlePod; i++) {

            //----------------------------------------------------------
            //BUG-985
            //if (bEquipmentFlag) {
            if (!bEquipmentFlag) {
                // modified by HuangHao
                //----------------------------------------------------------
                /*-------------*/
                /*   stocker   */
                /*-------------*/
                transferStatus = params.getXferReticlePodList().get(i).getTransferStatus();
            } else {
                /*---------------*/
                /*   eqp   */
                /*---------------*/
                transferStatus = params.getXferReticlePodList().get(i).getTransferStatus();
                ObjectIdentifier equipmentID = params.getEquipmentID();

                //step6 - equipment_brInfo_GetDR__120
                Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
                RetCode<Infos.EqpBrInfo> strEquipmentBrInfoGetDROut = new RetCode<>();

                strEquipmentBrInfoGetDROut.setObject(eqpBrInfo);

                if (!strEquipmentBrInfoGetDROut.getObject().isReticleUseFlag()) {
                    throw new ServiceException(new OmCode(retCodeConfig.getEqpNotRequiredReticle(), equipmentID.getValue()));
                }
                transferStatus = params.getXferReticlePodList().get(i).getTransferStatus();
            }
            if (CimStringUtils.equals(transferStatus, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_OUT)) {

                Inputs.ObjDurableControlJobIDGet objDurableControlJobIDGet = new Inputs.ObjDurableControlJobIDGet();
                //step7 - durable_durableControlJobID_Get
                ObjectIdentifier strDurableControlJobIDGetOut = durableMethod.durableDurableControlJobIDGet(objCommon, params.getXferReticlePodList().get(i).getReticlePodID(), CIMStateConst.SP_DURABLE_CAT_RETICLE_POD);
                if (ObjectIdentifier.isEmpty(strDurableControlJobIDGetOut) ? false : CimStringUtils.isNotEmpty(strDurableControlJobIDGetOut.getValue())) {
                    ObjectIdentifier durableControlJobID = strDurableControlJobIDGetOut;

                    //step8 - durableControlJob_status_Get
                    Infos.DurableControlJobStatusGet objDurableControlJobStatusGetOutRetCode = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);

                    if (!CimStringUtils.equals(objDurableControlJobStatusGetOutRetCode.getDurableControlJobStatus(), BizConstant.SP_APC_CONTROLJOBSTATUS_CREATED) &&
                            !CimStringUtils.equals(objDurableControlJobStatusGetOutRetCode.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                        throw new ServiceException(retCodeConfig.getInvalidDcjstatus());
                    }
                }
            }
            /*----------------------------*/
            /*   Change Transfer Status   */
            /*----------------------------*/

            //step9 - reticlePod_transferState_Change__160
            reticleMethod.reticlePodTransferStateChange(objCommon, params.getStockerID(),
                    params.getEquipmentID(),
                    params.getXferReticlePodList().get(i).getReticlePodID(),
                    transferStatus,
                    params.getXferReticlePodList().get(i).getTransferStatusChangeTimeStamp(),
                    claimMemo, params.getShelfPosition());

//            /*-------------------------------------*/
//            /*   Get Reticle List for reticlepod   */
//            /*-------------------------------------*/
//            Infos.XferReticlePod xferReticlePod = params.getXferReticlePodList().get(i);
//            Outputs.ObjReticlePodReticleListGetDROut objReticlePodReticleListGetDR = reticleMethod.reticlePodReticleListGetDR(objCommon, params.getXferReticlePodList().get(i).getReticlePodID());
//            List<ObjectIdentifier> reticleIDList = objReticlePodReticleListGetDR.getReticleID();
//            List<Infos.XferReticle> strXferReticle = new ArrayList<>();
//            for (ObjectIdentifier reticleID : reticleIDList){
//                Infos.XferReticle xferReticle = new Infos.XferReticle();
//                xferReticle.setReticleID(reticleID);
//                xferReticle.setTransferStatus(xferReticlePod.getTransferStatus());
//                xferReticle.setTransferStatusChangeTimeStamp(DateUtils.getCurrentTimeStamp());
//                strXferReticle.add(xferReticle);
//            }
//            durableService.sxReticleTransferStatusChangeRpt(objCommon, params.getStockerID(),
//                    params.getEquipmentID(),strXferReticle,claimMemo,false);
//            for (ObjectIdentifier reticleID : reticleIDList){
//                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
//                reticleMethod.reticleInfoSet(reticleOperationEventMakeParams, reticleID);
//                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_XFERCHG);
//                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
//                reticleOperationEventMakeParams.setEqpID(ObjectIdentifier.fetchValue(params.getEquipmentID()));
//                reticleOperationEventMakeParams.setStockerID(ObjectIdentifier.fetchValue(params.getStockerID()));
//                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
//            }
        }

        return reticlePodTransferStatusChangeRptResult;
    }

    @Override
    public void sxReticleSortRpt(Infos.ObjCommon objCommon, List<Infos.ReticleSortInfo> strReticleSortInfos, String claimMemo) {
        //---------------------------------------
        // Check input parameter for
        // reticle transfer (Sorter operation)
        //---------------------------------------
        log.info("Check input parameter for reticle transfer (Sorter operation)");
        sorterMethod.sorterReticleTransferInfoVerify(objCommon, strReticleSortInfos);

        //---------------------------------------
        // At first, all relation between
        // reticlePod-reticle should be remove relation (reticleID is null specified)
        // works inside of reticle_materialContainer_Change()
        //---------------------------------------
        log.info("At first, all relation between reticlePod-reticle should be remove relation (reticleID is null specified) works inside of reticle_materialContainer_Change()");
        strReticleSortInfos.forEach(x -> {
            reticleMethod.reticleMaterialContainerChange(objCommon, x, false, true);
        });

        //---------------------------------------
        // next time, add relation
        // works inside of reticle_materialContainer_Change()
        //---------------------------------------
        log.info("next time, add relation works inside of reticle_materialContainer_Change()");
        strReticleSortInfos.forEach(x -> {
            reticleMethod.reticleMaterialContainerChange(objCommon, x, true, false);
        });
    }

    @Override
    public void sxCarrierStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String cassetteStatus, String claimMemo) {
        // step1 - object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        // step2 - cassette_interFabXferState_Get
        String cassetteInterFabXferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommon, cassetteID);
        Validations.check(CIMStateConst.equals(CIMStateConst.SP_INTERFAB_XFER_STATE_TRANSFERRING, cassetteInterFabXferStateGetResult), retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());

        if(CimArrayUtils.binarySearch(new String[]{
                CIMStateConst.CIM_DURABLE_AVAILABLE,
                CIMStateConst.CIM_DURABLE_INUSE
        },cassetteStatus)){
            // step3 - durable_OnRoute_Check
            try {
                durableMethod.durableOnRouteCheck(objCommon, CIMStateConst.SP_DURABLE_CAT_CASSETTE, cassetteID);
            } catch (ServiceException e) {
                if(Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())){
                    // step4 - durable_inventoryState_Get
                    String durableInventoryStateGetResult = durableMethod.durableInventoryStateGet(objCommon, CIMStateConst.SP_DURABLE_CAT_CASSETTE, cassetteID);
                    Validations.check(CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR,durableInventoryStateGetResult), retCodeConfig.getDurableCannotStatechangeOnfloor());
                } else {
                    throw e;
                }
            }
        }

        // step5 - durable_subState_Get
        Infos.DurableSubStateGetOut  durableSubStateGetResult = durableMethod.durableSubStateGet(objCommon, CIMStateConst.SP_DURABLE_CAT_CASSETTE, cassetteID);
        Validations.check(!ObjectIdentifier.isEmpty(durableSubStateGetResult.getDurableSubStatus()), retCodeConfig.getDurableSubstateNotBlank());


        // step6 - cassette_state_Change
        cassetteMethod.cassetteStateChange(objCommon, cassetteID, cassetteStatus);

        // step7 durableChangeEvent_Make
        eventMethod.durableChangeEventMake(objCommon, TransactionIDEnum.CASSETTE_STATUS_CHANGE_RPT.getValue(), cassetteID,
                BizConstant.SP_DURABLECAT_CASSETTE, BizConstant.SP_DURABLEEVENT_ACTION_STATECHANGE, claimMemo);
    }

    @Override
    public ObjectIdentifier sxCarrierUsageCountResetReq(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String claimMemo) {
        // step1 -  object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        String lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        if (CimStringUtils.equals("1",lotOperationEIcheck)) {
            String strCassetteXferState;

            // step2 - cassette_transferState_Get
            String strCassette_transferState_Get_out = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);

            strCassetteXferState = strCassette_transferState_Get_out;

            if( CimStringUtils.equals(strCassetteXferState, CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN) )
            {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                        strCassetteXferState,cassetteID.getValue()));
            }
        }


        // step3 - cassette_controlJobID_Get
        ObjectIdentifier strCassette_controlJobID_Get_out = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);

        if ( !ObjectIdentifier.isEmpty(strCassette_controlJobID_Get_out))
        {
            throw new ServiceException(retCodeConfig.getCassetteControlJobFilled());
        }


        // step4 - cassette_interFabXferState_Get
        String strCassette_interFabXferState_Get_out = cassetteMethod.cassetteInterFabXferStateGet(objCommon, cassetteID);
        Validations.check(CIMStateConst.equals( CIMStateConst.SP_INTERFAB_XFER_STATE_TRANSFERRING, strCassette_interFabXferState_Get_out), retCodeConfig.getInterfabInvalidLotXferstateForReq());
        // step5 - cassette_usageInfo_Reset
        cassetteMethod.cassetteUsageInfoReset(objCommon, cassetteID);

        // step6 -durableChangeEvent_Make
        eventMethod.durableChangeEventMake(objCommon, TransactionIDEnum.CASSETTE_USAGE_COUNT_RESET_REQ.getValue(), cassetteID,
                BizConstant.SP_DURABLECAT_CASSETTE, BizConstant.SP_DURABLEEVENT_ACTION_PMRESET, claimMemo);

        return(cassetteID);
    }

    @Override
    public void sxMultipleCarrierStatusChangeRpt(Infos.ObjCommon objCommon, String cassetteStatus, List<ObjectIdentifier> cassetteID, String claimMemo) {
        int nLen = cassetteID.size();

        for ( int i=0 ; i<nLen ; i++ ) {
            sxCarrierStatusChangeRpt(objCommon, cassetteID.get(i), cassetteStatus, claimMemo);
        }
    }

    @Override
    public void sxMultipleReticleStatusChangeRpt(Infos.ObjCommon objCommon, String reticleStatus, List<ObjectIdentifier> reticleID, String claimMemo) {

        int nLen = reticleID == null ? 0 : CimArrayUtils.getSize(reticleID);
        for (int i = 0; i < nLen; i++) {

            // step1 - txReticleStatusChangeRpt
            sxReticleStatusChangeRpt(objCommon, reticleID.get(i), reticleStatus, claimMemo);
        }
    }

    @Override
    public Results.ReticleStatusChangeRptResult sxReticleStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String reticleStatus, String claimMemo) {
        // step1 - reticle_detailInfo_GetDR__170
        Results.ReticleDetailInfoInqResult strReticle_detailInfo_GetDR_out = reticleMethod.reticleDetailInfoGetDR(objCommon, reticleID, false, false);

        if (CimStringUtils.equals(reticleStatus, CIMStateConst.CIM_DURABLE_AVAILABLE)
                || CimStringUtils.equals(reticleStatus, CIMStateConst.CIM_DURABLE_INUSE)) {

            // strDurable_OnRoute_Check_in.durableID       = reticleID;
            // step2 - durable_OnRoute_Check
            try {
                durableMethod.durableOnRouteCheck(objCommon, BizConstant.SP_DURABLECAT_RETICLE, reticleID);
            } catch (ServiceException e) {

                if (Validations.isEquals(retCodeConfig.getDurableOnroute(),e.getCode())) {
                    // strDurable_inventoryState_Get_in.durableID       = reticleID;
                    // step3 - durable_inventoryState_Get
                    String strDurable_inventoryState_Get_out = durableMethod.durableInventoryStateGet(objCommon, BizConstant.SP_DURABLECAT_RETICLE, reticleID);
                    Validations.check(CimStringUtils.equals(strDurable_inventoryState_Get_out, CIMStateConst.CIM_LOT_INVENTORY_STATE_ONFLOOR), retCodeConfig.getDurableCannotStatechangeOnfloor());
                }

            }
        }


        // strDurable_subState_Get_in.durableID       = reticleID;
        // step4 - durable_subState_Get
        Infos.DurableSubStateGetOut  strDurableSubStateGetOut = null;
        try {
            strDurableSubStateGetOut = durableMethod.durableSubStateGet(objCommon, BizConstant.SP_DURABLECAT_RETICLE, reticleID);
        } catch (ServiceException e) {
            Validations.check((!CimObjectUtils.isEmpty(strDurableSubStateGetOut) && !ObjectIdentifier.isEmpty(strDurableSubStateGetOut.getDurableSubStatus())), retCodeConfig.getDurableSubstateNotBlank());
        }

        // step5 - reticle_state_Change
        reticleMethod.reticleStateChange(objCommon, reticleID, reticleStatus);

        //step6 - reticle_FillInTxPDR005
        Results.ReticleStatusChangeRptResult strReticle_FillInTxPDR005_out = reticleMethod.reticleFillInTxPDR005(objCommon, reticleID);

        // step7 - durableChangeEvent_Make
        eventMethod.durableChangeEventMake(objCommon, TransactionIDEnum.RETICLE_STATUS_CHANGE_RPT.getValue(), reticleID, BizConstant.SP_DURABLECAT_RETICLE, BizConstant.SP_DURABLEEVENT_ACTION_STATECHANGE, claimMemo);

        return (strReticle_FillInTxPDR005_out);

    }

    @Override
    public Results.ReticleTransferStatusChangeRptResult sxReticleTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, List<Infos.XferReticle> strXferReticle, String claimMemo,boolean byUrl) {
        Results.ReticleTransferStatusChangeRptResult strReticleTransferStatusChangeRptResult = new Results.ReticleTransferStatusChangeRptResult();

        int nLen = CimArrayUtils.getSize(strXferReticle);

        if(!ObjectIdentifier.isEmpty(stockerID) && !ObjectIdentifier.isEmpty(equipmentID)){
            Validations.check(retCodeConfig.getInvalidDataContents());
        }

        if (!ObjectIdentifier.isEmpty(stockerID)) {

            // step1 - stocker_type_GetDR 注意 此处为ObjectIdentifier
            Outputs.ObjStockerTypeGetDROut strStockerTypeGetDROut = stockerMethod.stockerTypeGet(objCommon, stockerID);

            int aSwitchNumber ;
            if (CimStringUtils.equals(strStockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLE) ||
                    CimStringUtils.equals(strStockerTypeGetDROut.getStockerType(),BizConstant.SP_STOCKER_TYPE_RETICLESHELF)) {
                aSwitchNumber = 0 ;
            }
            else {
                aSwitchNumber = 1 ;
            }

            switch (aSwitchNumber) {
                case 0 :
                    for(int i=0; i<nLen ; i++) {
                        String transferStatus = strXferReticle.get(i).getTransferStatus();
                        if ((CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_MANUALIN)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_BAYIN)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_BAYOUT)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALIN)) ||
                                (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT))) {
                            continue ;
                        } else {
                            throw new ServiceException(retCodeConfig.getInvalidDataCombinAtion());
                        }
                    }
                    break;
                default :
                    throw new ServiceException(retCodeConfig.getInvalidStockerType());
            }
        }


        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            // strEqpInfoInqResult.equipmentBRInfo.reticleUseFlag = FALSE;
            Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
            eqpInfoInqParams.setEquipmentID(equipmentID);
            eqpInfoInqParams.setRequestFlagForBasicInfo(true);
            eqpInfoInqParams.setRequestFlagForStatusInfo(true);
            eqpInfoInqParams.setRequestFlagForPMInfo(false);
            eqpInfoInqParams.setRequestFlagForPortInfo(false);
            eqpInfoInqParams.setRequestFlagForChamberInfo(false);
            eqpInfoInqParams.setRequestFlagForStockerInfo(false);
            eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(false);
            eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(false);
            eqpInfoInqParams.setRequestFlagForRSPPortInfo(false);
            eqpInfoInqParams.setRequestFlagForEqpContainerInfo(false);

            // step2 - txEqpInfoInq__160
            Results.EqpInfoInqResult eqpInfoInqResult = equipmentInqService.sxEqpInfoInq(objCommon, eqpInfoInqParams);
            for(int i=0 ; i < nLen ; i++) {
                String transferStatus = strXferReticle.get(i).getTransferStatus();
                if(!((CimStringUtils.equals(transferStatus,BizConstant.SP_TRANSSTATE_EQUIPMENTIN )) ||
                        (CimStringUtils.equals(transferStatus,BizConstant.SP_TRANSSTATE_EQUIPMENTOUT )))) {
                    throw new ServiceException(retCodeConfig.getInvalidDataCombinAtion());
                }

                if( ( CimStringUtils.equals(transferStatus,BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) &&
                        (CimBooleanUtils.isFalse(eqpInfoInqResult.getEquipmentBasicInfo().isReticleUseFlag()))) {
                    throw new ServiceException(new OmCode(retCodeConfig.getEqpNotRequiredReticle(),equipmentID.getValue()));
                }
            }
        }

        List<String> preTransferStatus = new ArrayList<>();
        if (byUrl){
            for(int i=0 ;i<nLen ; i++) {
                // step3 - reticle_detailInfo_GetDR__170
                Results.ReticleDetailInfoInqResult strReticleFillInTxPDQ005DROut = reticleMethod.reticleDetailInfoGetDR(objCommon, strXferReticle.get(i).getReticleID(), false, false);

                Infos.ReticleStatusInfo reticleStatusInfo = strReticleFillInTxPDQ005DROut.getReticleStatusInfo();
                preTransferStatus.add(reticleStatusInfo.getTransferStatus());

                if (!ObjectIdentifier.isEmpty(reticleStatusInfo.getReticlePodID())){
                    throw new ServiceException(new OmCode(retCodeConfig.getReticleIsInReticlepod(),strXferReticle.get(i).getReticleID().getValue(),reticleStatusInfo.getReticlePodID().getValue()));
                }
            }
        }
        // step4 - reticle_ChangeTransportState
        Infos.ReticleChangeTransportState strReticleChangeTransportStateOut = reticleMethod.reticleChangeTransportState(objCommon, stockerID, equipmentID, strXferReticle);

        strReticleTransferStatusChangeRptResult.setStockerID(strReticleChangeTransportStateOut.getStockerID());
        strReticleTransferStatusChangeRptResult.setEquipmentID(strReticleChangeTransportStateOut.getEquipmentID());
        strReticleTransferStatusChangeRptResult.setStrXferReticle(strReticleChangeTransportStateOut.getStrXferReticle());

        Timestamp recordTimeStamp = null;

        for(int i=0 ;i<nLen ; i++) {
            ObjectIdentifier machineID = null;
            Infos.XferReticle xferReticle = strXferReticle.get(i);
            if(!ObjectIdentifier.isEmpty(stockerID)) {
                machineID = stockerID;
            }
            if(!ObjectIdentifier.isEmpty(equipmentID)) {
                machineID = equipmentID;
            }

            if(xferReticle.getTransferStatusChangeTimeStamp()==null ) {
                recordTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
            } else {
                recordTimeStamp = xferReticle.getTransferStatusChangeTimeStamp();
            }

            // step5 - durableXferStatusChangeEvent_Make
            Inputs.DurableXferStatusChangeEventMakeParams durableXferStatusChangeEventMakeParams = new Inputs.DurableXferStatusChangeEventMakeParams();
            durableXferStatusChangeEventMakeParams.setTransactionID(TransactionIDEnum.RETICLE_XFER_STATUS_CHANGE_RPT.getValue());
            durableXferStatusChangeEventMakeParams.setDurableID(xferReticle.getReticleID());
            durableXferStatusChangeEventMakeParams.setDurableType(BizConstant.SP_DURABLECAT_RETICLE);
            durableXferStatusChangeEventMakeParams.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
            durableXferStatusChangeEventMakeParams.setDurableStatus("");
            durableXferStatusChangeEventMakeParams.setXferStatus(xferReticle.getTransferStatus());
            durableXferStatusChangeEventMakeParams.setTransferStatusChangeTimeStamp(CimDateUtils.convertToSpecString(recordTimeStamp));
            durableXferStatusChangeEventMakeParams.setLocation(ObjectIdentifier.fetchValue(machineID));
            durableXferStatusChangeEventMakeParams.setClaimMemo(claimMemo);
            eventMethod.durableXferStatusChangeEventMake(objCommon, durableXferStatusChangeEventMakeParams);

            Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
            reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, xferReticle.getReticleID());
            reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_XFERCHG);
            reticleOperationEventMakeParams.setClaimMemo(claimMemo);
            reticleOperationEventMakeParams.setEqpID(ObjectIdentifier.fetchValue(equipmentID));
            reticleOperationEventMakeParams.setStockerID(ObjectIdentifier.fetchValue(stockerID));;
            eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);

        }
        return strReticleTransferStatusChangeRptResult;
    }

    @Override
    public ObjectIdentifier sxReticleUsageCountResetReq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, String claimMemo) {

        // step1 - reticle_usageInfo_Reset
        reticleMethod.reticleUsageInfoReset(objCommon, reticleID);

        // step2 - durableChangeEvent_Make
        eventMethod.durableChangeEventMake(objCommon, TransactionIDEnum.RETICLE_USAGE_COUNT_RESET_REQ.getValue(), reticleID, BizConstant.SP_DURABLECAT_RETICLE, BizConstant.SP_DURABLEEVENT_ACTION_PMRESET, claimMemo);


        return reticleID;

    }

    @Override
    public Results.DurableBankInReqResult sxDrbBankInReq(Results.DurableBankInReqResult strDurableBankInReqResult, Infos.ObjCommon objCommon, int seqIndex, Params.DurableBankInReqInParam strInParm, String claimMemo) {
        // Common Initialisation done here itself for all the cases.
        List<Results.BankInDurableResult> strBankInDurableResults = strDurableBankInReqResult.getStrBankInDurableResults();
        Results.BankInDurableResult bankInDurableResult = new Results.BankInDurableResult();
        ObjectIdentifier durableID = strInParm.getDurableIDs().get(seqIndex);
        String durableCategory = strInParm.getDurableCategory();
        ObjectIdentifier bankID = strInParm.getBankID();
        strBankInDurableResults.add(seqIndex, bankInDurableResult);
        bankInDurableResult.setDurableID(durableID);
        bankInDurableResult.setDurableCategory(durableCategory);
        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        if (!CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)
                && !CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)
                && !CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
            Validations.check(true, new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }

        //---------------------------------------
        // Object Lock
        //---------------------------------------
        try {
            if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)) {
                objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
            } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)) {
                objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
            } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
            }
        } catch (ServiceException e) {
            bankInDurableResult.setErrorCode(e.getCode());
            bankInDurableResult.setErrorMessage(e.getMessage());
            return strDurableBankInReqResult;
        }
        //---------------------------------------
        // Call durable_status_CheckForOperation
        //---------------------------------------
        try {
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATIONCATEGORY_BANKIN, durableID, durableCategory);
        } catch (ServiceException e) {
            log.info("durable_status_CheckForOperation() != RC_OK");
            bankInDurableResult.setErrorCode(e.getCode());
            bankInDurableResult.setErrorMessage(e.getMessage());
            return strDurableBankInReqResult;
        }

        //---------------------------------------
        // Check Durable Inventory State
        //---------------------------------------
        String durableinventoryState = null;
        try {
            durableinventoryState = durableMethod.durableInventoryStateGet(objCommon, durableCategory, durableID);
        } catch (ServiceException e) {
            log.info("durable_inventoryState_Get() != RC_OK");
            bankInDurableResult.setErrorCode(e.getCode());
            bankInDurableResult.setErrorMessage(e.getMessage());
            return strDurableBankInReqResult;
        }

        if (CimStringUtils.isNotEmpty(durableinventoryState)
                && !CimStringUtils.equals(durableinventoryState, BizConstant.SP_UNDEFINED_STATE)
                && !CimStringUtils.equals(durableinventoryState, BizConstant.SP_DURABLE_INVENTORYSTATE_ONFLOOR)) {
            log.info("durableInventoryState != SP_Durable_InventoryState_OnFloor");
            bankInDurableResult.setErrorCode(retCodeConfig.getInvalidDurableInventoryStat().getCode());
            bankInDurableResult.setErrorMessage(retCodeConfig.getInvalidDurableInventoryStat().getMessage());
            Validations.check(retCodeConfig.getInvalidDurableInventoryStat(), ObjectIdentifier.fetchValue(durableID), durableinventoryState);
        }
        boolean onRouteFlag = false;
        //---------------------------------------
        // Check Durable OnRoute
        //---------------------------------------
        try {
            durableMethod.durableOnRouteCheck(objCommon, durableCategory, durableID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                log.info("durable_OnRoute_Check() == RC_DURABLE_ONROUTE");
                onRouteFlag = true;
                //---------------------------------------
                // Check Durable Hold State
                //---------------------------------------
                String durableHoldStateGetOut = null;
                String durableProcessStateGetOut = null;
                try {
                    durableHoldStateGetOut = durableMethod.durableHoldStateGet(objCommon, durableCategory, durableID);
                } catch (ServiceException ex) {
                    bankInDurableResult.setErrorCode(ex.getCode());
                    bankInDurableResult.setErrorMessage(ex.getMessage());
                    return strDurableBankInReqResult;
                }
                if (!CimStringUtils.equals(durableHoldStateGetOut, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                    bankInDurableResult.setErrorCode(retCodeConfig.getInvalidDurableHoldStat().getCode());
                    bankInDurableResult.setErrorMessage(retCodeConfig.getInvalidDurableHoldStat().getMessage());
                    return strDurableBankInReqResult;
                }
                //---------------------------------------
                // Check Durable Process State
                //---------------------------------------
                try {
                    durableProcessStateGetOut = durableMethod.durableProcessStateGet(objCommon, durableCategory, durableID);
                } catch (ServiceException ex) {
                    bankInDurableResult.setErrorCode(ex.getCode());
                    bankInDurableResult.setErrorMessage(ex.getMessage());
                    return strDurableBankInReqResult;
                }
                if (!CimStringUtils.equals(durableProcessStateGetOut, BizConstant.SP_DURABLE_PROCSTATE_WAITING)) {
                    bankInDurableResult.setErrorCode(retCodeConfig.getInvalidDurableProcStat().getCode());
                    bankInDurableResult.setErrorMessage(retCodeConfig.getInvalidDurableProcStat().getMessage());
                    return strDurableBankInReqResult;
                }

                if (!ObjectIdentifier.isEmpty(bankID)) {
                    log.info("in-parm bankID is not blank");
                    bankInDurableResult.setErrorCode(retCodeConfig.getInvalidParameterWithMsg().getCode());
                    bankInDurableResult.setErrorMessage(String.format(retCodeConfig.getInvalidParameterWithMsg().getMessage(),
                            DURABLE_ON_ROUTE));
                    return strDurableBankInReqResult;
                }

                try {
                    durableMethod.durableCheckEndBankIn(objCommon, durableCategory, durableID);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(retCodeConfig.getBankinOperation(), ex.getCode())){
                        bankInDurableResult.setErrorCode(ex.getCode());
                        bankInDurableResult.setErrorMessage(ex.getMessage());
                        return strDurableBankInReqResult;
                    }
                }
            } else if (Validations.isEquals(retCodeConfig.getDurableNotOnroute(), e.getCode())) {
                log.info("durable_OnRoute_Check() == RC_DURABLE_NOT_ONROUTE");
                onRouteFlag = false;
                if (ObjectIdentifier.isEmpty(bankID)) {
                    log.info("BankID should be specified on BankIn operation, When durable is off route.");
                    bankInDurableResult.setErrorCode(retCodeConfig.getInvalidParameterWithMsg().getCode());
                    bankInDurableResult.setErrorMessage(retCodeConfig.getInvalidParameterWithMsg().getMessage() + "BankID should be specified on BankIn operation, When durable is off route.");
                    return strDurableBankInReqResult;
                }

                try {
                    bankForDurableMethod.bankCheckStartBankForDurableRouteDR(objCommon, bankID);
                } catch (ServiceException ex) {
                    bankInDurableResult.setErrorCode(ex.getCode());
                    bankInDurableResult.setErrorMessage(ex.getMessage());
                    return strDurableBankInReqResult;

                }
            } else {
                bankInDurableResult.setErrorCode(e.getCode());
                bankInDurableResult.setErrorMessage(e.getMessage());
                return strDurableBankInReqResult;
            }
        }
        //---------------------------------------
        //  Check Durable ControlJob ID
        //---------------------------------------
        ObjectIdentifier durableControlJobID = null;
        try {
            durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, durableID, durableCategory);
        } catch (ServiceException e) {
            bankInDurableResult.setErrorCode(e.getCode());
            bankInDurableResult.setErrorMessage(e.getMessage());
            return strDurableBankInReqResult;
        }
        if (!ObjectIdentifier.isEmpty(durableControlJobID)) {
            bankInDurableResult.setErrorCode(retCodeConfig.getDurableControlJobFilled().getCode());
            bankInDurableResult.setErrorMessage(retCodeConfig.getDurableControlJobFilled().getMessage());
            return strDurableBankInReqResult;
        }
        //-----------------------------------------------------------
        // Check cassette interFabXferState
        //-----------------------------------------------------------
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            String cassetteInterFabXferStateGetResult = null;
            try {
                cassetteInterFabXferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommon, durableID);
            } catch (ServiceException e) {
                bankInDurableResult.setErrorCode(e.getCode());
                bankInDurableResult.setErrorMessage(e.getMessage());
                return strDurableBankInReqResult;
            }
            if (CimStringUtils.equals(cassetteInterFabXferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
                bankInDurableResult.setErrorCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest().getCode());
                bankInDurableResult.setErrorMessage(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest().getMessage());
                return strDurableBankInReqResult;
            }
        }

        //-----------------------------------------------------------
        // Call Durable BankIn
        //-----------------------------------------------------------
        try {
            bankForDurableMethod.durableBankIn(objCommon, onRouteFlag, durableCategory, durableID, bankID);
        } catch (ServiceException e) {
            bankInDurableResult.setErrorCode(e.getCode());
            bankInDurableResult.setErrorMessage(e.getMessage());
            return strDurableBankInReqResult;
        }

        // Auto Available(Bank In), by ho
        sxAutoAvailableForBankIn(objCommon,durableID,claimMemo);

        //-----------------------------------------------------------
        // Create Durable Bank Move Event
        //-----------------------------------------------------------
        Infos.DurablebankmoveeventMakeIn strDurableBankMoveEvent_Make_in = new Infos.DurablebankmoveeventMakeIn();
        strDurableBankMoveEvent_Make_in.setClaimMemo(claimMemo);
        strDurableBankMoveEvent_Make_in.setTransactionID(TransactionIDEnum.DURABLE_BANK_IN_REQ.getValue());
        strDurableBankMoveEvent_Make_in.setDurableCategory(durableCategory);
        strDurableBankMoveEvent_Make_in.setDurableID(durableID);
        try {
            eventMethod.durableBankMoveEventMake(objCommon, strDurableBankMoveEvent_Make_in);
        } catch (ServiceException e) {
            bankInDurableResult.setErrorCode(e.getCode());
            bankInDurableResult.setErrorMessage(e.getMessage());
            return strDurableBankInReqResult;
        }
        bankInDurableResult.setErrorCode(retCodeConfig.getSucc().getCode());
        bankInDurableResult.setErrorMessage("Sucess");
        return strDurableBankInReqResult;
    }

    @Override
    public void sxDurableJobStatusChangeReq(Infos.ObjCommon objCommon, Params.DurableJobStatusChangeReqParams params) {
        Validations.check(null == objCommon || null == params, retCodeConfig.getInvalidInputParam());
        String durableCategory = params.getDurableCategory();
        ObjectIdentifier durableID = params.getDurableID();
        ObjectIdentifier durableStatus = params.getDurableStatus();

        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        log.info("Check Durable Category");
        if (!BizConstant.SP_DURABLECAT_CASSETTE.equals(durableCategory)
                && !BizConstant.SP_DURABLECAT_RETICLE.equals(durableCategory)
                && !BizConstant.SP_DURABLECAT_RETICLEPOD.equals(durableCategory)) {
            Validations.check(new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }

        //---------------------------------------
        // Check Durable Status
        //---------------------------------------
        if (!CimStringUtils.equals(durableStatus, BizConstant.CIMFW_DURABLE_NOTAVAILABLE)) {
            Validations.check(retCodeConfig.getInvalidDurableProcStat(), ObjectIdentifier.fetchValue(durableID), durableStatus);
        }

        //---------------------------------------
        // Object Lock
        //---------------------------------------
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
                break;
            case SP_DURABLECAT_RETICLE:
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
                break;
            case SP_DURABLECAT_RETICLEPOD:
                objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
                break;
        }

        //---------------------------------------
        // Check Durable Job Status
        //---------------------------------------
        durableMethod.durableJobStateCheck(objCommon, durableCategory, durableID, params.getJobStatus());

        //---------------------------------------
        // Change durable Job Status
        //---------------------------------------
        durableMethod.durableJobStateChange(objCommon, durableCategory, durableID, params.getJobStatus());


        //---------------------------------------
        // Create Durable Job Status Change Event
        //---------------------------------------
        //---------------------------------------
        // Create Durable Job Status Change Event
        //---------------------------------------
        Infos.DurableJobStatusChangeEvent event = new Infos.DurableJobStatusChangeEvent();
        event.setUser(params.getUser());
        event.setDurableCategory(params.getDurableCategory());
        event.setDurableID(params.getDurableID());
        event.setJobStatus(params.getJobStatus());
        event.setClaimMemo(params.getClaimMemo());

        eventMethod.durableJobStatusChangeEventMake(objCommon, event, BizConstant.SP_DURABLEEVENT_ACTION_JOBSTATECHANGE_OMS);
    }

    @Override
    public void sxDurableJobStatusChangeRpt(Infos.ObjCommon objCommon, Params.DurableJobStatusChangeRptParams params) {
        Validations.check(null == objCommon || null == params, retCodeConfig.getInvalidInputParam());
        String durableCategory = params.getDurableCategory();
        ObjectIdentifier durableID = params.getDurableID();
        String equipmentID = params.getEquipmentID();
        String chamberID = params.getChamberID();

        Validations.check(CimStringUtils.isEmpty(equipmentID) || CimStringUtils.isEmpty(chamberID), retCodeConfig.getInvalidInputParam());

        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        log.info("Check Durable Category");
        if (!BizConstant.SP_DURABLECAT_CASSETTE.equals(durableCategory)
                && !BizConstant.SP_DURABLECAT_RETICLE.equals(durableCategory)
                && !BizConstant.SP_DURABLECAT_RETICLEPOD.equals(durableCategory)) {
            Validations.check(new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }

        //---------------------------------------
        // Check Machine and Chamber
        //---------------------------------------
        durableMethod.durableCheckConditionForMachineAndChamber(objCommon, equipmentID, chamberID);

        //---------------------------------------
        // Object Lock
        //---------------------------------------
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                objectLockMethod.objectLock(objCommon, CimCassette.class, durableID);
                break;
            case SP_DURABLECAT_RETICLE:
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, durableID);
                break;
            case SP_DURABLECAT_RETICLEPOD:
                objectLockMethod.objectLock(objCommon, CimReticlePod.class, durableID);
                break;
        }

        //---------------------------------------
        // Check Durable Job Status
        //---------------------------------------
        durableMethod.durableJobStateCheck(objCommon, durableCategory, durableID, params.getJobStatus());

        //---------------------------------------
        // Change durable Job Status
        //---------------------------------------
        durableMethod.durableJobStateChange(objCommon, durableCategory, durableID, params.getJobStatus());

        //---------------------------------------
        // Create Durable Job Status Change Event
        //---------------------------------------
        Infos.DurableJobStatusChangeEvent event = new Infos.DurableJobStatusChangeEvent();
        event.setUser(params.getUser());
        event.setDurableCategory(params.getDurableCategory());
        event.setDurableID(params.getDurableID());
        event.setJobStatus(params.getJobStatus());
        event.setEquipmentID(equipmentID);
        event.setChamberID(chamberID);
        event.setClaimMemo(params.getClaimMemo());

        eventMethod.durableJobStatusChangeEventMake(objCommon, event, BizConstant.SP_DURABLEEVENT_ACTION_JOBSTATECHANGE_EAP);
    }

    public void sxReworkDurableWithHoldReleaseReq (
            Infos.ObjCommon                            strObjCommonIn,
            Params.ReworkDurableWithHoldReleaseReqInParam strReworkDurableWithHoldReleaseReqInParam) {
        log.info("PPTManager_i:: txReworkDurableWithHoldReleaseReq ");
        Params.ReworkDurableWithHoldReleaseReqInParam strInParm = strReworkDurableWithHoldReleaseReqInParam;

        //------------------------------------------
        //  Call txHoldDurableReleaseReq
        //------------------------------------------
        Infos.HoldDurableReleaseReqInParam strHoldDurableReleaseReqInParam = new Infos.HoldDurableReleaseReqInParam();
        strHoldDurableReleaseReqInParam.setDurableCategory     ( strInParm.getStrReworkDurableReqInParam().getDurableCategory());
        strHoldDurableReleaseReqInParam.setDurableID           ( strInParm.getStrReworkDurableReqInParam().getDurableID());
        strHoldDurableReleaseReqInParam.setReleaseReasonCodeID ( strInParm.getReleaseReasonCodeID());
        strHoldDurableReleaseReqInParam.setStrDurableHoldList  ( strInParm.getStrDurableHoldList());
        String claimMemo = strInParm.getClaimMemo();
        sxHoldDrbReleaseReq(strObjCommonIn, strHoldDurableReleaseReqInParam, claimMemo);

        //---------------------------------------
        //  Check Durable Category
        //---------------------------------------
        sxDrbReworkReq(
                strObjCommonIn,
                strInParm.getStrReworkDurableReqInParam(),
                claimMemo );

        //---------------------------------------
        // Return
        //---------------------------------------
        log.info("PPTManager_i:: txReworkDurableWithHoldReleaseReq ");
    }

    public void sxAutoClean(Infos.ObjCommon objCommon,ObjectIdentifier cassetteID,String claimMemo) {
        Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams=new Params.CarrierDetailInfoInqParams();
        carrierDetailInfoInqParams.setCassetteID(cassetteID);
        carrierDetailInfoInqParams.setDurableOperationInfoFlag(false);
        carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        carrierDetailInfoInqParams.setUser(objCommon.getUser());
        Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = bankInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);
        Infos.CassettePmInfo cassettePMInfo = carrierDetailInfoInqResult.getCassettePMInfo();
        long operationStartCount = CimNumberUtils.longValue(cassettePMInfo.getOperationStartCount());
        long maximumOperationStartCount = CimNumberUtils.longValue(cassettePMInfo.getMaximumOperationStartCount());
        if (maximumOperationStartCount==0){
            return;
        }
        if (operationStartCount<=maximumOperationStartCount){
            return;
        }

        Params.MultiDurableStatusChangeReqParams params=new Params.MultiDurableStatusChangeReqParams();
        Infos.MultiDurableStatusChangeReqInParm multiDurableStatusChangeReqInParm=new Infos.MultiDurableStatusChangeReqInParm();
        Infos.CassetteStatusInfo cassetteStatusInfo = carrierDetailInfoInqResult.getCassetteStatusInfo();
        multiDurableStatusChangeReqInParm.setDurableStatus(CIMFW_DURABLE_NOTAVAILABLE);
        multiDurableStatusChangeReqInParm.setDurableSubStatus(ObjectIdentifier.buildWithValue(DURABLE_SUBSTATUS_DIRTY));
        multiDurableStatusChangeReqInParm.setDurableCategory(SP_DURABLECAT_CASSETTE);
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos=new ArrayList<>();
        Infos.StatusChangeDurableInfo statusChangeDurableInfo=new Infos.StatusChangeDurableInfo();
        statusChangeDurableInfo.setDurableID(cassetteID);
        statusChangeDurableInfo.setDurableStatus(cassetteStatusInfo.getCassetteStatus());
        statusChangeDurableInfo.setDurableSubStatus(cassetteStatusInfo.getDurableSubStatus());
        statusChangeDurableInfos.add(statusChangeDurableInfo);
        multiDurableStatusChangeReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
        params.setParm(multiDurableStatusChangeReqInParm);
        params.setUser(objCommon.getUser());
        params.setClaimMemo(claimMemo);
        sxMultiDurableStatusChangeReq(objCommon,params);
    }

    public void sxAutoAvailableForBankIn(Infos.ObjCommon objCommon,ObjectIdentifier cassetteID,String claimMemo){
        Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams=new Params.CarrierDetailInfoInqParams();
        carrierDetailInfoInqParams.setCassetteID(cassetteID);
        carrierDetailInfoInqParams.setDurableOperationInfoFlag(false);
        carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        carrierDetailInfoInqParams.setUser(objCommon.getUser());
        Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = bankInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);

        Infos.CassetteStatusInfo cassetteStatusInfo = carrierDetailInfoInqResult.getCassetteStatusInfo();
        List<Infos.HashedInfo> strDurableStatusList = cassetteStatusInfo.getStrDurableStatusList();
        if (CimArrayUtils.isEmpty(strDurableStatusList)){
            return;
        }
        boolean match = strDurableStatusList.stream().anyMatch(hashedInfo ->
                CimStringUtils.equals(hashedInfo.getHashKey(), "Durable Finished State") && CimStringUtils.equals(hashedInfo.getHashData(), SP_DURABLE_FINISHEDSTATE_COMPLETED));
        if (!match){
            return;
        }

        Params.MultiDurableStatusChangeReqParams params=new Params.MultiDurableStatusChangeReqParams();
        Infos.MultiDurableStatusChangeReqInParm multiDurableStatusChangeReqInParm=new Infos.MultiDurableStatusChangeReqInParm();
        multiDurableStatusChangeReqInParm.setDurableStatus(CIMFW_DURABLE_NOTAVAILABLE);
        multiDurableStatusChangeReqInParm.setDurableSubStatus(ObjectIdentifier.buildWithValue(DURABLE_SUBSTATUS_CLEAN));
        multiDurableStatusChangeReqInParm.setDurableCategory(SP_DURABLECAT_CASSETTE);
        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos=new ArrayList<>();
        Infos.StatusChangeDurableInfo statusChangeDurableInfo=new Infos.StatusChangeDurableInfo();
        statusChangeDurableInfo.setDurableID(cassetteID);
        statusChangeDurableInfo.setDurableStatus(cassetteStatusInfo.getCassetteStatus());
        statusChangeDurableInfo.setDurableSubStatus(cassetteStatusInfo.getDurableSubStatus());
        statusChangeDurableInfos.add(statusChangeDurableInfo);
        multiDurableStatusChangeReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
        params.setParm(multiDurableStatusChangeReqInParm);
        params.setUser(objCommon.getUser());
        params.setClaimMemo(claimMemo);
        sxMultiDurableStatusChangeReq(objCommon,params);
    }

    @Override
    public void sxEqpRSPPortAccessModeChangeReq(Infos.ObjCommon objCommon, Params.EqpRSPPortAccessModeChangeReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        //=========================================================================
        // Object Lock
        //=========================================================================
        lockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(),
                params.getReticlePodPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);

        //=========================================================================
        // Get boolean flag for equipment is stocker or machine
        //=========================================================================
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, params.getEquipmentID());
        log.debug("bStorageMachineFlag : {}", machineTypeGetOut.isBStorageMachineFlag());

        //=========================================================================
        // Get current port information
        //=========================================================================
        List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfo;
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("bStorageMachineFlag == FALSE");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut strEquipmentreticlePodPortInfoGetDRout = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, params.getEquipmentID());
            tmpReticlePodPortInfo = strEquipmentreticlePodPortInfoGetDRout.getReticlePodPortInfoList();
        } else {
            log.debug("bStorageMachineFlag == TRUE");
            tmpReticlePodPortInfo = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, params.getEquipmentID());
        }

        //=========================================================================
        // Check Access Mode validity
        //=========================================================================
        if (!CimStringUtils.equals(params.getNewAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_AUTO)
                && !CimStringUtils.equals(params.getNewAccessMode(), BizConstant.SP_RETICLEPODPORT_ACCESSMODE_MANUAL)) {
            Validations.check(retCodeConfigEx.getInvalidPortAccessMode(), ObjectIdentifier.fetchValue(params.getReticlePodPortID()), params.getNewAccessMode());
        }

        //=========================================================================
        // Compare current access mode and requested access mode.
        //=========================================================================
        Integer nLen = CimArrayUtils.getSize(tmpReticlePodPortInfo);
        Integer i = 0;
        if (CimArrayUtils.isNotEmpty(tmpReticlePodPortInfo)){
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : tmpReticlePodPortInfo) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), params.getReticlePodPortID())) {
                    log.debug("Found the Reticle Pod Port : {}", reticlePodPortInfo.getReticlePodPortID());
                    if (CimStringUtils.equals(reticlePodPortInfo.getAccessMode(), params.getNewAccessMode())) {
                        log.error("rspportAccessmodeSame ");
                        Validations.check(retCodeConfigEx.getRspportAccessmodeSame(),
                                ObjectIdentifier.fetchValue(params.getEquipmentID()),
                                ObjectIdentifier.fetchValue(params.getReticlePodPortID()),
                                params.getNewAccessMode());
                    }
                    break;
                }
            }
        }

        //=========================================================================
        // Update reticle pod port information
        //=========================================================================
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("bStorageMachineFlag == FALSE");
            equipmentMethod.equipmentReticlePodPortAccessModeChange(objCommon, params.getEquipmentID(),
                    params.getReticlePodPortID(), params.getNewAccessMode());
        } else {
            log.debug("bStorageMachineFlag == TRUE");
            stockerMethod.stockerReticlePodPortAccessModeChange(objCommon, params.getEquipmentID(),
                    params.getReticlePodPortID(), params.getNewAccessMode());

        }

        //=========================================================================
        // Notify port access mode change information to EAP
        //=========================================================================
        if (CimBooleanUtils.isTrue(params.getNotifyToEAPFlag())) {
            log.debug("EAP notify ");
            String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
            String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
            long sleepTimeValue = CimStringUtils.isEmpty(tmpSleepTimeValue) ? BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : Long.parseLong(tmpSleepTimeValue);
            long retryCountValue = CimStringUtils.isEmpty(tmpRetryCountValue) ? BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : Long.parseLong(tmpRetryCountValue);

            log.debug("env value of OM_EAP_CONNECT_SLEEP_TIME  = {}", sleepTimeValue);
            log.debug("env value of OM_EAP_CONNECT_RETRY_COUNT = {}", retryCountValue);

            /*
            objTCSMgr_SendEqpRSPPortAccessModeChangeReq_out strTCSMgr_SendEqpRSPPortAccessModeChangeReq_out;
            //'retryCountValue + 1' means first try plus retry count
            for(i = 0 ; i < (retryCountValue + 1) ; i++){
                *//*--------------------------*//*
             *//*    Send Request to TCS   *//*
             *//*--------------------------*//*
                rc = TCSMgr_SendEqpRSPPortAccessModeChangeReq( strTCSMgr_SendEqpRSPPortAccessModeChangeReq_out,
                        objCommon,
                        objCommon.strUser,
                        equipmentID,
                        reticlePodPortID, newAccessMode );

                log.info("rc = ",rc);

                if(rc == RC_OK)
                {
                    log.info("Now TCS subSystem is alive!! Go ahead");
                    break;
                }
                else if ( rc == RC_EXT_SERVER_BIND_FAIL ||
                        rc == RC_EXT_SERVER_NIL_OBJ   ||
                        rc == RC_TCS_NO_RESPONSE )
                {
                    log.info("TCS subsystem has return NO_RESPONSE!! just retry now!!  now count...",i);
                    log.info("now sleeping... ",sleepTimeValue);
                    sleep(sleepTimeValue);
                    continue;
                } else {
                    log.info( "TCSMgr_SendEqpRSPPortAccessModeChangeReq() != RC_OK", rc);
                    strEqpRSPPortAccessModeChangeReqResult.strResult = strTCSMgr_SendEqpRSPPortAccessModeChangeReq_out.strResult;
                    return( rc );
                }
            }
            */
        }
    }

    @Override
    public void sxReticleOfflineRetrieveReq(Infos.ObjCommon objCommon, Params.ReticleOfflineRetrieveReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());
        log.debug("machineID   : {} ", params.getMachineID());
        log.debug("RSPPortID   : {} ", params.getPortID());
        log.debug("reticlePodID : {}", params.getReticlePodID());
        /************************************/
        /*  Check input parameter validity  */
        /************************************/
        ObjectIdentifier equipmentID = null;
        ObjectIdentifier stockerID = null;
        Boolean isEquipment = false;

        if (ObjectIdentifier.isEmpty(params.getMachineID())) {
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        /**************************/
        /*   Check machine type   */
        /**************************/
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, params.getMachineID());

        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("This machine is Equipment : {}", ObjectIdentifier.fetchValue(params.getMachineID()));
            isEquipment = true;
            equipmentID = params.getMachineID();
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("This machine is BR Stocker : {}", ObjectIdentifier.fetchValue(params.getMachineID()));
            isEquipment = false;
            stockerID = params.getMachineID();
        } else {
            log.error("This machine is invalid for request.: {}, : {}", ObjectIdentifier.fetchValue(params.getMachineID()), machineTypeGetOut.getStockerType());
            Validations.check(retCodeConfigEx.getStkTypeDifferent(), machineTypeGetOut.getStockerType());
        }

        /******************************************/
        /*  Check online mode (must be offline)   */
        /******************************************/
        if (isEquipment) {
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, params.getMachineID());

            log.debug("Equipment's online mode : {}", onlineMode);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getEqpOnlineMode(), ObjectIdentifier.fetchValue(params.getMachineID()), onlineMode);

        } else {
            String onlineMode = stockerMethod.stockerOnlineModeGet(objCommon, params.getMachineID());
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getEqpOnlineMode(), ObjectIdentifier.fetchValue(params.getMachineID()), onlineMode);
        }

        /*********************************/
        /*  Choose successeded reticles  */
        /*********************************/
        List<Infos.MoveReticles> allMoveReticlesList = new ArrayList<>();
        List<Infos.MoveReticles> successMoveReticlesList = new ArrayList<>();

        int resultCount = CimArrayUtils.getSize(params.getReticleRetrieveResult());
        log.debug("Retrieve result count: {}", resultCount);

        int i = 0;
        for (i = 0; i < resultCount; i++) {
            log.debug("reticleID : {}", ObjectIdentifier.fetchValue(params.getReticleRetrieveResult().get(i).getReticleID()));
            log.debug("   slotNo : {}", params.getReticleRetrieveResult().get(i).getSlotNo());
            log.debug("retrieved : {}", (params.getReticleRetrieveResult().get(i).getReticleRetrievedFlag() ? "TRUE" : "false"));

            Infos.MoveReticles moveReticles = new Infos.MoveReticles();
            moveReticles.setReticleID(params.getReticleRetrieveResult().get(i).getReticleID());
            moveReticles.setSlotNumber(params.getReticleRetrieveResult().get(i).getSlotNo());
            allMoveReticlesList.add(moveReticles);
            if (CimBooleanUtils.isTrue(params.getReticleRetrieveResult().get(i).getReticleRetrievedFlag())) {
                Infos.MoveReticles successMoveReticles = new Infos.MoveReticles();
                successMoveReticles.setReticleID(params.getReticleRetrieveResult().get(i).getReticleID());
                successMoveReticles.setSlotNumber(params.getReticleRetrieveResult().get(i).getSlotNo());
                successMoveReticlesList.add(successMoveReticles);
            }
        }

        /********************************************************************/
        /*  Check equipment/reticlePod/reticle combination.                 */
        /*  (for all reticles...though it has not been successed to move)   */
        /********************************************************************/
        if (isEquipment) {
            equipmentMethod.equipmentConditionCheckForReticleRetrieve(objCommon, equipmentID, params.getPortID(), params.getReticlePodID(), allMoveReticlesList);
        } else {
            stockerMethod.stockerStatusCheckForReticleRetrieve(objCommon, stockerID, params.getPortID(), params.getReticlePodID(), allMoveReticlesList);
        }

        /*****************************************************************************/
        /*  Check and get reticle's following status. (move success reticles only)   */
        /*   1) if reticle is reserved by user, then return error.                   */
        /*   2) if reticle is reserved by control job, then return error.            */
        /*****************************************************************************/
        for (i = 0; i < CimArrayUtils.getSize(successMoveReticlesList); i++) {
            reticleMethod.reticleReservationCheck(objCommon, successMoveReticlesList.get(i).getReticleID());
        }

        /**************************************************/
        /*  Check empty slots of destination reticlePod.  */
        /**************************************************/
        reticleMethod.reticlePodVacantSlotPositionCheck(objCommon, params.getReticlePodID(), successMoveReticlesList);


        /**************************************/
        /*  Check reticlePod has RDJ or not.  */
        /**************************************/
        List<Infos.ReticleDispatchJob> strReticleDispatchJobCheckExistenceDROut = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, null, params.getReticlePodID(), null);


        /***********************************/
        /*  Check reticle has RDJ or not.  */
        /***********************************/
        for (i = 0; i < CimArrayUtils.getSize(successMoveReticlesList); i++) {
            strReticleDispatchJobCheckExistenceDROut = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, null, successMoveReticlesList.get(i).getReticleID(), null);

            reticleMethod.reticleComponentJobCheckExistenceDR(objCommon,
                    successMoveReticlesList.get(i).getReticleID(),  //reticleID
                    params.getReticlePodID(),                          //reticlePodID
                    params.getMachineID(),                             //fromMachineID
                    params.getPortID(),                                //fromPortID
                    null,                               //toMachineID
                    null);                             //toPortID
        }

        /************************************/
        /*                                  */
        /*          Main Routine            */
        /*                                  */
        /************************************/

        /*********************/
        /*  Do Retrieve job  */
        /*********************/
        if (CimArrayUtils.getSize(successMoveReticlesList) > 0) {
            Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
            reticleAllInOutRptParams.setReticlePodID(params.getReticlePodID());
            reticleAllInOutRptParams.setMoveDirection(BizConstant.SP_MOVEDIRECTION_JUSTIN);
            reticleAllInOutRptParams.setMoveReticles(successMoveReticlesList);
            durableService.sxReticleAllInOutRpt(objCommon, reticleAllInOutRptParams);
        }

        /******************************************/
        /*  Cancel reticle transport reservation  */
        /*  If destination machine is null        */
        /******************************************/
        for (i = 0; i < CimArrayUtils.getSize(successMoveReticlesList); i++) {
            Boolean bCheckToMachineFlag = true;
            reticleMethod.reticleReticlePodReserveCancel(objCommon, successMoveReticlesList.get(i).getReticleID(), bCheckToMachineFlag);
        }

        if (StandardProperties.OM_RTMS_CHECK_ACTIVE.isTrue() && params.isReportFlag()) {
            //call rtms do reticle just in
            for (Infos.MoveReticles moveReticles : successMoveReticlesList) {
                ReticleRTMSJustInOutReqParams rtmsJustInOutReqParams = new ReticleRTMSJustInOutReqParams();
                rtmsJustInOutReqParams.setMoveDirection(SP_MOVEDIRECTION_JUSTIN);
                rtmsJustInOutReqParams.setReticleName(moveReticles.getReticleID().getValue());
                rtmsJustInOutReqParams.setSlotNumber(moveReticles.getSlotNumber());
                rtmsJustInOutReqParams.setReticlePodName(params.getReticlePodID().getValue());
                rtmsRemote.requestRTMSJustInOut(rtmsJustInOutReqParams);
            }
        }
    }


    @Override
    public void sxEqpRSPPortStatusChangeRpt(Infos.ObjCommon objCommon, Params.EqpRSPPortStatusChangeRpt params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        ObjectIdentifier equipmentID = params.getEquipmentID();
        List<Infos.EqpRSPPortEventOnEAP> strEqpRSPPortEventOnEAPList = params.getStrEqpRSPPortEventOnEAP();

        if (log.isDebugEnabled())
            log.debug("in para equipmentID: {}", ObjectIdentifier.fetchValue(equipmentID));
        Validations.check(CimArrayUtils.isEmpty(strEqpRSPPortEventOnEAPList), retCodeConfig.getInvalidInputParam());

        //=========================================================================
        // Object Lock
        //=========================================================================
        log.debug("step1 - objectLockMethod.objectLockForEquipmentResource");
        for (Infos.EqpRSPPortEventOnEAP eqpRSPPortEventOnEAP : strEqpRSPPortEventOnEAPList) {
            objectLockMethod.objectLockForEquipmentResource(objCommon,
                    equipmentID,
                    eqpRSPPortEventOnEAP.getPortID(),
                    BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

        }
        //=========================================================================
        // Check input equipment is equipment or stocker
        //=========================================================================
        log.debug("step2 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, equipmentID);
        if (log.isDebugEnabled())
            log.debug("bStorageMachineFlag: {}", machineTypeGetOut.isBStorageMachineFlag());

        //=========================================================================
        // Check equipment is online or not
        //=========================================================================
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("bStorageMachineFlag = false");
            log.debug("step3 - equipmentMethod.equipmentOnlineModeGet");
            String onlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            if (log.isDebugEnabled())
                log.debug("onlineMode: {}", onlineModeGet);

            Validations.check(CimStringUtils.equals(onlineModeGet, BizConstant.SP_EQP_ONLINEMODE_OFFLINE),
                    retCodeConfig.getEqpOnlineMode(),
                    ObjectIdentifier.fetchValue(equipmentID),
                    onlineModeGet);
        } else {
            log.debug("bStorageMachineFlag = true");
            Validations.check(!CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE),
                    retCodeConfigEx.getStkTypeDifferent(), machineTypeGetOut.getStockerType());

            log.debug("step4 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon,
                    equipmentID);

            if (log.isDebugEnabled())
                log.debug("onlineMode: {}", stockerOnlineModeGet);
            Validations.check(CimStringUtils.equals(stockerOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_OFFLINE),
                    retCodeConfig.getEqpOnlineMode(),
                    ObjectIdentifier.fetchValue(equipmentID),
                    stockerOnlineModeGet);

        }

        //=========================================================================
        // Update equipment reticle pod port status
        //=========================================================================
        for (Infos.EqpRSPPortEventOnEAP eqpRSPPortEventOnEAP : strEqpRSPPortEventOnEAPList) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(eqpRSPPortEventOnEAP.getPortID()),
                    retCodeConfig.getRspportNotFound(),
                    ObjectIdentifier.fetchValue(equipmentID),
                    ObjectIdentifier.fetchValue(eqpRSPPortEventOnEAP.getPortID()));

            if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                log.debug("bStorageMachineFlag = FALSE");
                log.debug("step5 - equipmentMethod.equipmentReticlePodStateChange");
                equipmentMethod.equipmentReticlePodStateChange(objCommon,
                        equipmentID,
                        eqpRSPPortEventOnEAP.getPortID(),
                        eqpRSPPortEventOnEAP.getPortStatus());
            } else {
                log.debug("bStorageMachineFlag = TRUE");
                log.debug("step6 - stockerMethod.stockerReticlePodPortStateChange");
                stockerMethod.stockerReticlePodPortStateChange(objCommon, equipmentID, eqpRSPPortEventOnEAP.getPortID(), eqpRSPPortEventOnEAP.getPortStatus());
            }

            Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
            ObjectIdentifier dummy = ObjectIdentifier.buildWithValue("");
            reticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            reticleEventRecord.setReticleDispatchJobID("");
            reticleEventRecord.setReticleComponentJobID("");
            reticleEventRecord.setReticlePodStockerID(dummy);
            reticleEventRecord.setBareReticleStockerID(dummy);
            reticleEventRecord.setResourceID(dummy);
            reticleEventRecord.setEquipmentID(dummy);
            reticleEventRecord.setRSPPortID(dummy);
            reticleEventRecord.setReticlePodID(eqpRSPPortEventOnEAP.getReticlePodID());
            reticleEventRecord.setRSPPortEvent(eqpRSPPortEventOnEAP.getPortStatus());
            reticleEventRecord.setReticleID(dummy);
            reticleEventRecord.setReticleJobEvent("");

            if (CimBooleanUtils.isTrue(machineTypeGetOut.isBStorageMachineFlag())) {
                log.debug("isBStorageMachineFlag = TRUE");
                reticleEventRecord.setBareReticleStockerID(equipmentID);
                reticleEventRecord.setResourceID(eqpRSPPortEventOnEAP.getPortID());
            } else {
                log.debug("isBStorageMachineFlag = FALSE");
                reticleEventRecord.setEquipmentID(equipmentID);
                reticleEventRecord.setRSPPortID(eqpRSPPortEventOnEAP.getPortID());
            }

            log.debug("step7 - reticleMethod.reticleEventQueuePutDR");
            reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);

            //----------------------------------------------------------
            //  Create event for equipment port status change
            //----------------------------------------------------------
            Inputs.EquipmentPortStatusChangeEventMakeParams equipmentPortStatusChangeEventMakeParams = new Inputs.EquipmentPortStatusChangeEventMakeParams();
            equipmentPortStatusChangeEventMakeParams.setEquipmentID(equipmentID);
            equipmentPortStatusChangeEventMakeParams.setPortType(BizConstant.SP_DURABLECAT_RETICLEPOD);
            equipmentPortStatusChangeEventMakeParams.setPortID(eqpRSPPortEventOnEAP.getPortID());
            equipmentPortStatusChangeEventMakeParams.setPortUsage("");
            equipmentPortStatusChangeEventMakeParams.setPortStatus(eqpRSPPortEventOnEAP.getPortStatus());
            equipmentPortStatusChangeEventMakeParams.setDispatchDurableID(ObjectIdentifier.fetchValue(eqpRSPPortEventOnEAP.getReticlePodID()));
            equipmentPortStatusChangeEventMakeParams.setClaimMemo(params.getOpeMemo());

            if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                log.debug("step8 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, equipmentID);
                if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                        if (ObjectIdentifier.equalsWithValue(eqpRSPPortEventOnEAP.getPortID(), reticlePodPortInfo.getReticlePodPortID())) {
                            equipmentPortStatusChangeEventMakeParams.setAccessMode(reticlePodPortInfo.getAccessMode());
                            equipmentPortStatusChangeEventMakeParams.setDispatchTime(reticlePodPortInfo.getDispatchTimestamp());
                            equipmentPortStatusChangeEventMakeParams.setDispatchState(reticlePodPortInfo.getDispatchStatus());
                            break;
                        }
                    }
                }
            } else {
                log.debug("step9 - stockerMethod.stockerReticlePodPortInfoGetDR");
                // TODO: 2020/11/6 confrim  stockerReticlePodPortInfoGetDR replace stockerReticlePodPortInfoGet need test it
                List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, equipmentID);
                if (CimArrayUtils.isNotEmpty(reticlePodPortInfos)) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                        if (ObjectIdentifier.equalsWithValue(eqpRSPPortEventOnEAP.getPortID(), reticlePodPortInfo.getReticlePodPortID())) {
                            log.debug("portID = {}", ObjectIdentifier.fetchValue(eqpRSPPortEventOnEAP.getPortID()));
                            equipmentPortStatusChangeEventMakeParams.setAccessMode(reticlePodPortInfo.getAccessMode());
                            equipmentPortStatusChangeEventMakeParams.setDispatchTime(reticlePodPortInfo.getDispatchTimestamp());
                            equipmentPortStatusChangeEventMakeParams.setDispatchState(reticlePodPortInfo.getDispatchStatus());
                            break;
                        }
                    }
                }
            }
            log.debug("step10 - eventMethod.equipmentPortStatusChangeEventMake");
            eventMethod.equipmentPortStatusChangeEventMake(objCommon, equipmentPortStatusChangeEventMakeParams);
        }
    }


    @Override
    public void sxReticleRetrieveReq(Infos.ObjCommon objCommon, Params.ReticleRetrieveReqParams params) {
        String tmpARMS = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARMS switch on / off  tmpARMS = {}", tmpARMS);
        }
        Validations.check(!CimStringUtils.equals(tmpARMS, BizConstant.SP_ARMS_SWITCH_ON), retCodeConfig.getFunctionNotAvailable());

        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier resourceID = params.getResourceID();
        String reticleComponentJobID = params.getReticleComponentJobID();
        String reticleDispatchJobID = params.getReticleDispatchJobID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier reticlePodPortID = params.getReticlePodPortID();
        ObjectIdentifier stockerID = params.getStockerID();
        List<Infos.MoveReticles> strMoveReticles = params.getMoveReticlesList();
        /*--------------------------------------*/
        /*   Check equipment is online or not   */
        /*--------------------------------------*/
        ObjectIdentifier operateMachineID = null;
        ObjectIdentifier operatePortID = null;
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            if (log.isDebugEnabled()){
                log.debug("equipmentID is not blank.");
            }
            operateMachineID = equipmentID;
            operatePortID = reticlePodPortID;

            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode(), equipmentID, onlineMode);
        } else if (!ObjectIdentifier.isEmpty(stockerID)) {
            Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, stockerID);

            Validations.check(!CimStringUtils.equals(objStockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE), retCodeConfig.getInvalidParameter());

            if (log.isDebugEnabled()){
                log.debug("stockerID is not blank.");
            }
            operateMachineID = stockerID;
            operatePortID = resourceID;

            String onlineMode = stockerMethod.stockerOnlineModeGet(objCommon, stockerID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode(), stockerID, onlineMode);
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        //----------------------------------
        // Check reticlePod Status
        //----------------------------------
        if (log.isDebugEnabled()){
            log.debug("Check reticlePod Status");
        }
        reticleMethod.reticlePodAvailabilityCheck(objCommon, reticlePodID);

        /*-----------------------------------------------*/
        /*   Check equipment condition for reticle job   */
        /*-----------------------------------------------*/
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            equipmentMethod.equipmentConditionCheckForReticleRetrieve(objCommon, equipmentID, reticlePodPortID, reticlePodID, strMoveReticles);
        } else if (!ObjectIdentifier.isEmpty(stockerID)) {
            stockerMethod.stockerStatusCheckForReticleRetrieve(objCommon, stockerID, resourceID, reticlePodID, strMoveReticles);
        }

        //========================================================
        // If input reticle is reserved for control job,
        // that reticle has to be in the same equipment of input equipment
        //========================================================
        for (Infos.MoveReticles strMoveReticle : strMoveReticles) {
            reticleMethod.reticleReservationCheck(objCommon, strMoveReticle.getReticleID());
        }

        /*------------------------------------------------*/
        /*   Check reticle pod slot is available or not   */
        /*------------------------------------------------*/
        reticleMethod.reticlePodVacantSlotPositionCheck(objCommon, reticlePodID, strMoveReticles);

        List<Infos.ReticleDispatchJob> reticleDispatchJobs = reticleMethod.reticleDispatchJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.getSize(reticleDispatchJobs) == 0) {
            log.error("RDJ not found. id {}", reticleDispatchJobID);
            Validations.check(retCodeConfigEx.getRdjNotFound());
        } else if (CimArrayUtils.getSize(reticleDispatchJobs) > 1) {
            log.error("Duplicate RDJ detected. id {}", reticleDispatchJobID);
            Validations.check(retCodeConfigEx.getRdjDuplicate());
        }

        Infos.ReticleDispatchJob strTargetReticleDispatchJob = reticleDispatchJobs.get(0);
        List<Infos.ReticleComponentJob> reticleComponentJobs = reticleMethod.reticleComponentJobListGetDR(objCommon, reticleDispatchJobID);

        boolean targetRCJFound = false;

        Infos.ReticleComponentJob strTargetReticleComponentJob = null;
        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobs) {
            if (CimStringUtils.equals(reticleComponentJob.getReticleComponentJobID(), reticleComponentJobID)) {
                log.info("Found ReticleComponentJob in the list.");
                if (targetRCJFound) {
                    log.error("Detect duplicated RCJ. {}", reticleComponentJobID);
                    Validations.check(retCodeConfigEx.getRdjDuplicate());
                } else {
                    if (log.isDebugEnabled()){
                        log.debug("Found target RCJ. {}", reticleComponentJobID);
                    }
                    targetRCJFound = true;
                    strTargetReticleComponentJob = reticleComponentJob;
                }
            }
        }

        Validations.check(!targetRCJFound, retCodeConfigEx.getRcjNotFound());
        Validations.check(!CimStringUtils.equals(strTargetReticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE), retCodeConfigEx.getRdjStatusError(), strTargetReticleDispatchJob.getJobStatus());
        Validations.check(strTargetReticleComponentJob != null && !CimStringUtils.equals(strTargetReticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE), retCodeConfigEx.getMismatchRdjrcjStatus());
        Validations.check(!ObjectIdentifier.equalsWithValue(strTargetReticleDispatchJob.getReticlePodID(), reticlePodID), retCodeConfigEx.getRdjNotMatchRequest());


        Validations.check(strTargetReticleComponentJob != null && (!ObjectIdentifier.equalsWithValue(strTargetReticleComponentJob.getReticlePodID(), reticlePodID) ||
                !ObjectIdentifier.equalsWithValue(strTargetReticleComponentJob.getToEquipmentID(), operateMachineID) ||
                !ObjectIdentifier.equalsWithValue(strTargetReticleComponentJob.getToReticlePodPortID(), operatePortID) ||
                !CimStringUtils.equals(strTargetReticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_RETRIEVE)), retCodeConfigEx.getRcjNotMatchRequest());

        // mrLen must be 1 -- because input RDJ/RCJ is only one, so reticleID is unique.
        for (Infos.MoveReticles strMoveReticle : strMoveReticles) {
            Validations.check(!ObjectIdentifier.equalsWithValue(strMoveReticle.getReticleID(), strTargetReticleDispatchJob.getReticleID()), retCodeConfigEx.getRdjNotMatchRequest());

            Validations.check(strTargetReticleComponentJob != null && !ObjectIdentifier.equalsWithValue(strMoveReticle.getReticleID(), strTargetReticleComponentJob.getReticleID()), retCodeConfigEx.getRcjNotMatchRequest());
        }

        Inputs.SendReticleRetrieveReqIn in = new Inputs.SendReticleRetrieveReqIn();
        in.setObjCommonIn(objCommon);
        in.setRequestUserID(objCommon.getUser());
        in.setEquipmentID(equipmentID);
        in.setReticlePodPortID(reticlePodPortID);
        in.setStockerID(stockerID);
        in.setResourceID(resourceID);
        in.setReticlePodID(reticlePodID);
        in.setStrMoveReticles(strMoveReticles);
//        tcsMethod.sendTCSReq(TCSReqEnum.sendReticleRetrieveReq, in);

        reticleMethod.reticleJobStatusUpdateByRequestDR(objCommon, reticleDispatchJobID, reticleComponentJobID, true);
    }


    @Override
    public void sxReticleOfflineStoreReq(Infos.ObjCommon objCommon, Params.ReticleOfflineStoreReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        if (1 != CimArrayUtils.getSize(params.getReticleStoreResultList())) {
            Validations.check(retCodeConfig.getInvalidParameter());
        }
        Boolean bFoundFlag = false;

        /*---------------------*/
        /*   Check condition   */
        /*---------------------*/
        Outputs.ObjMachineTypeGetOut strMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, params.getMachineID());

        if (CimBooleanUtils.isFalse(strMachineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("bStorageMachineFlag = FALSE");
            //check onlineMode
            String strEquipmentOnlineModeGetOut = equipmentMethod.equipmentOnlineModeGet(objCommon, params.getMachineID());

            if (!CimStringUtils.equals(strEquipmentOnlineModeGetOut, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                log.debug("This equipment is not Offline Mode : {}", strEquipmentOnlineModeGetOut);
                Validations.check(retCodeConfig.getEqpOnlineMode(), params.getMachineID().getValue(), strEquipmentOnlineModeGetOut);
            }

            //check reticlePod/Port condition
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut strEquipmentReticlePodPortInfoGetDrOut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, params.getMachineID());


            int portLen = CimArrayUtils.getSize(strEquipmentReticlePodPortInfoGetDrOut.getReticlePodPortInfoList());
            log.debug("portLen : {}", portLen);
            if (CimArrayUtils.isNotEmpty(strEquipmentReticlePodPortInfoGetDrOut.getReticlePodPortInfoList())) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : strEquipmentReticlePodPortInfoGetDrOut.getReticlePodPortInfoList()) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), params.getPortID())) {
                        log.debug("Found the Reticle Pod Port");
                        if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), params.getReticlePodID())) {
                            log.debug("reticlePod/Port on equipment is correct condition.");
                            bFoundFlag = true;
                            break;
                        } else {
                            log.debug("reticlePod/Port on stocker is incorrect condition.");
                            Validations.check(retCodeConfigEx.getDiffRtclpodLoaded(), ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()),
                                    ObjectIdentifier.fetchValue(params.getMachineID()), ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));
                        }
                    }
                }
            }

            if (!bFoundFlag) {
                log.debug("Not found toReticlePodPort");
                Validations.check(retCodeConfig.getRspportNotFound(), ObjectIdentifier.fetchValue(params.getMachineID()), ObjectIdentifier.fetchValue(params.getPortID()));
            }
        } else if (CimStringUtils.equals(strMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("stockerType = BareReticle");

            //check onlineMode
            String strStockerOnlineModeGetOut = stockerMethod.stockerOnlineModeGet(objCommon, params.getMachineID());

            if (!CimStringUtils.equals(strStockerOnlineModeGetOut, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                log.debug("This equipment is not Offline Mode : {}", strStockerOnlineModeGetOut);
                Validations.check(retCodeConfig.getEqpOnlineMode(), params.getMachineID().getValue(), strStockerOnlineModeGetOut);
            }

            List<Infos.ReticlePodPortInfo> strStockerReticlePodPortInfoGetDrOut = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, params.getMachineID());


            int portLen = CimArrayUtils.getSize(strStockerReticlePodPortInfoGetDrOut);
            log.debug("portLen : {}", portLen);
            if (CimArrayUtils.isNotEmpty(strStockerReticlePodPortInfoGetDrOut)) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : strStockerReticlePodPortInfoGetDrOut) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), params.getPortID())) {
                        log.debug("Found the Reticle Pod Port");
                        if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), params.getReticlePodID())) {
                            log.debug("reticlePod/Port on stocker is correct condition.");
                            bFoundFlag = true;
                        } else {
                            log.debug("reticlePod/Port on stocker is incorrect condition.");
                            Validations.check(retCodeConfigEx.getDiffRtclpodLoaded(), ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()),
                                    ObjectIdentifier.fetchValue(params.getMachineID()), ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));
                        }
                    }
                }
            }

            if (CimBooleanUtils.isFalse(bFoundFlag)) {
                log.debug("Not found toReticlePodPort");
                Validations.check(retCodeConfig.getRspportNotFound(), ObjectIdentifier.fetchValue(params.getMachineID()), ObjectIdentifier.fetchValue(params.getPortID()));
            }
        } else {
            log.debug("Not EQP/NOT BRS");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        /*-------------------------------------------------------------------*/
        /* If input reticle is reserved for control job,                     */
        /* that reticle has to be in the same equipment of input equipment   */
        /*-------------------------------------------------------------------*/

        Outputs.ReticleReservationDetailInfoGetOut strReticleReservationDetailInfoGetOut = reticleMethod.reticleReservationDetailInfoGet(objCommon, params.getReticleStoreResultList().get(0).getReticleID());

        if (CimBooleanUtils.isTrue(strReticleReservationDetailInfoGetOut.isReservedFlag())) {
            log.debug("TRUE == reticleReserved");
            Validations.check(retCodeConfigEx.getAlreadyReservedReticle());
        }


        /*-------------------------------------------------------------------*/
        /* Set current timestamp as store time to reticleID                  */
        /*-------------------------------------------------------------------*/

        reticleMethod.reticleStoreTimeSet(objCommon, params.getReticleStoreResultList().get(0).getReticleID(), objCommon.getTimeStamp().getReportTimeStamp().toString());

        /*-----------------------*/
        /*   Check RDJ/RCJ job   */
        /*-----------------------*/
        reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon,
                params.getReticleStoreResultList().get(0).getReticleID(),
                params.getReticlePodID(),
                params.getMachineID());


        reticleMethod.reticleComponentJobCheckExistenceDR(objCommon,
                params.getReticleStoreResultList().get(0).getReticleID(),
                params.getReticlePodID(),
                params.getMachineID(),
                params.getPortID(),
                params.getMachineID(),
                params.getPortID());


        /*------------------------------*/
        /*   Check Pod/Slot condition   */
        /*------------------------------*/

        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(params.getReticlePodID());
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        Outputs.ObjReticlePodFillInTxPDQ013DROut strReticlePodFillInTxPdq013DrOut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams); //DSN000101569


        bFoundFlag = false;
        int reticleLen = CimArrayUtils.getSize(strReticlePodFillInTxPdq013DrOut.getReticlePodStatusInfo().getStrContainedReticleInfo());
        log.debug("reticleLen : {}", reticleLen);

        if (CimArrayUtils.isNotEmpty(strReticlePodFillInTxPdq013DrOut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
            for (Infos.ContainedReticleInfo containedReticleInfo : strReticlePodFillInTxPdq013DrOut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                if (ObjectIdentifier.equalsWithValue(containedReticleInfo.getReticleID(), params.getReticleStoreResultList().get(0).getReticleID()) &&
                        containedReticleInfo.getSlotNo() == params.getReticleStoreResultList().get(0).getSlotNo()) {
                    bFoundFlag = true;
                    break;
                }
            }
        }

        Validations.check(!bFoundFlag, retCodeConfigEx.getRtclNotInReclPod(), ObjectIdentifier.fetchValue(params.getReticleStoreResultList().get(0).getReticleID()),
                ObjectIdentifier.fetchValue(params.getReticlePodID()),
                params.getReticleStoreResultList().get(0).getSlotNo());

        /*--------------------------*/
        /*   Check store capacity   */
        /*--------------------------*/
        equipmentMethod.machineCapacityCheckForReticleStore(objCommon, params.getMachineID());


        List<Infos.MoveReticles> strMoveReticlesSeq = new ArrayList<>();
        Infos.MoveReticles moveReticles = new Infos.MoveReticles();
        moveReticles.setReticleID(params.getReticleStoreResultList().get(0).getReticleID());
        moveReticles.setSlotNumber(params.getReticleStoreResultList().get(0).getSlotNo());
        strMoveReticlesSeq.add(moveReticles);
        /*--------------------------------------------------------------------------------------------------*/
        /*   Reticle - Reticle Pod relation remove.                                                         */
        /*   Use input parameter as following                                                               */
        /*     - moveDirection = SP_MoveDirection_JustOut                                                   */
        /*     - reticlePodID                                                                               */
        /*     - strMoveReticles : if input parameter strReticleStoreResultSeq[].reticleStoredFlag == false,   */
        /*                         omit that value                                                          */
        /*--------------------------------------------------------------------------------------------------*/
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
        reticleAllInOutRptParams.setMoveDirection(BizConstant.SP_MOVEDIRECTION_JUSTOUT);
        reticleAllInOutRptParams.setReticlePodID(params.getReticlePodID());
        reticleAllInOutRptParams.setMoveReticles(strMoveReticlesSeq);
        durableService.sxReticleAllInOutRpt(objCommon, reticleAllInOutRptParams);

        /*-----------------------------*/
        /*   Release reticle reserve   */
        /*-----------------------------*/
        reticleMethod.reticleReticlePodReserveCancel(objCommon, params.getReticleStoreResultList().get(0).getReticleID(), false);

        if (StandardProperties.OM_RTMS_CHECK_ACTIVE.isTrue() && params.isReportFlag()){
            //call rtms do reticle just out
            for (Infos.MoveReticles moveReticle : strMoveReticlesSeq) {
                ReticleRTMSJustInOutReqParams rtmsJustInOutReqParams = new ReticleRTMSJustInOutReqParams();
                rtmsJustInOutReqParams.setMoveDirection(SP_MOVEDIRECTION_JUSTOUT);
                rtmsJustInOutReqParams.setReticleName(moveReticle.getReticleID().getValue());
                rtmsJustInOutReqParams.setSlotNumber(moveReticle.getSlotNumber());
                rtmsJustInOutReqParams.setReticlePodName(params.getReticlePodID().getValue());
                rtmsRemote.requestRTMSJustInOut(rtmsJustInOutReqParams);
            }
        }
    }

    @Override
    public void sxReticleRetrieveRpt(Infos.ObjCommon objCommon, Params.ReticleRetrieveRptParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier resourceID = params.getResourceID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier reticlePodPortID = params.getReticlePodPortID();
        ObjectIdentifier stockerID = params.getStockerID();
        List<Infos.ReticleRetrieveResult> strReticleRetrieveResult = params.getReticleRetrieveResultList();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /*--------------------------------------*/
        /*   Check equipment is online or not   */
        /*--------------------------------------*/
        ObjectIdentifier machineID = null;
        ObjectIdentifier portID = null;

        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            log.debug("equipmentID is not blank.");
            machineID = equipmentID;
            portID = reticlePodPortID;
        } else if (!ObjectIdentifier.isEmpty(stockerID)) {
            log.debug("stockerID is not blank.");
            machineID = stockerID;
            portID = resourceID;
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        List<Infos.MoveReticles> allMoveReticlesList = new ArrayList<>();
        List<Infos.MoveReticles> successMoveReticlesList = new ArrayList<>();
        List<Infos.MoveReticles> failMoveReticlesList = new ArrayList<>();

        int resultCount = CimArrayUtils.getSize(strReticleRetrieveResult);
        if (log.isDebugEnabled())
            log.debug("Retrieve result count {}", resultCount);

        int successCount = 0;
        int failCount = 0;
        int i = 0;
        for (i = 0; i < resultCount; i++) {
            Infos.ReticleRetrieveResult reticleRetrieveResult = strReticleRetrieveResult.get(i);
            if (log.isDebugEnabled()){
                log.debug("reticleID {}", ObjectIdentifier.fetchValue(reticleRetrieveResult.getReticleID()));
                log.debug("   slotNo {}", reticleRetrieveResult.getSlotNo());
                log.debug("retrieved {}", reticleRetrieveResult.getReticleRetrievedFlag());
            }
            allMoveReticlesList.add(new Infos.MoveReticles(reticleRetrieveResult.getReticleID(), reticleRetrieveResult.getSlotNo()));

            if (CimBooleanUtils.isTrue(reticleRetrieveResult.getReticleRetrievedFlag())) {
                log.debug("reticleRetrievedFlag == TRUE");
                successMoveReticlesList.add(new Infos.MoveReticles(reticleRetrieveResult.getReticleID(), reticleRetrieveResult.getSlotNo()));
                successCount++;
            } else {
                log.debug("reticleRetrievedFlag != TRUE");
                failMoveReticlesList.add(new Infos.MoveReticles(reticleRetrieveResult.getReticleID(), reticleRetrieveResult.getSlotNo()));
                failCount++;
            }
        }

        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            log.debug("equipmentID is not blank.");
            equipmentMethod.equipmentConditionCheckForReticleRetrieve(objCommon, equipmentID, reticlePodPortID, reticlePodID, allMoveReticlesList);
        } else if (!ObjectIdentifier.isEmpty(stockerID)) {
            log.debug("stockerID is not blank.");
            stockerMethod.stockerStatusCheckForReticleRetrieve(objCommon, stockerID, resourceID, reticlePodID, allMoveReticlesList);
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        //=============================================================================================
        // Check reticle's following status. (move success reticles only)
        // 1) if reticle is reserved by user, then return error.
        // 2) if reticle is reserved by control job, then return error.
        //=============================================================================================
        for (i = 0; i < successCount; i++) {
            reticleMethod.reticleReservationCheck(objCommon, successMoveReticlesList.get(i).getReticleID());

        }

        // Check reticle pod slot is available or not
        reticleMethod.reticlePodVacantSlotPositionCheck(objCommon, reticlePodID, successMoveReticlesList);

        // find RCJ for success job.
        List<String> compRCJIDList = new ArrayList<>();
        //int compRCJCount = 0;
        for (i = 0; i < successCount; i++) {
            Infos.MoveReticles successMoveReticle = successMoveReticlesList.get(i);
            ObjectIdentifier reticleID = successMoveReticle.getReticleID();
            Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDROut = reticleMethod.reticleComponentJobGetByJobNameDR(objCommon, BizConstant.SP_RCJ_JOBNAME_RETRIEVE, reticleID, reticlePodID);
            Infos.ReticleComponentJob strRCJ = reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob();
            String completedRCJID = strRCJ.getReticleComponentJobID();
            if (!CimObjectUtils.isEmpty(strRCJ) && !CimObjectUtils.isEmpty(completedRCJID)) {
                log.debug("reticleComponentJobID is not blank.");
                Validations.check(!ObjectIdentifier.equalsWithValue(reticleID, strRCJ.getReticleID()) ||
                        successMoveReticle.getSlotNumber() != strRCJ.getSlotNo().intValue() ||
                        !ObjectIdentifier.equalsWithValue(reticlePodID, strRCJ.getReticlePodID()) ||
                        !ObjectIdentifier.equalsWithValue(portID, strRCJ.getToReticlePodPortID()) ||
                        !ObjectIdentifier.equalsWithValue(machineID, strRCJ.getToEquipmentID()) ||
                        !CimStringUtils.equals(BizConstant.SP_RCJ_JOBNAME_RETRIEVE, strRCJ.getJobName()), retCodeConfigEx.getRcjNotMatchRequest());
                compRCJIDList.add(completedRCJID);
            } else {
                log.debug("RCJ not found for successed retrieve job.");
            }
        }

        if (successCount > 0) {
            log.debug("successCount > 0");

            // do update.
            Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
            reticleAllInOutRptParams.setUser(objCommon.getUser());
            reticleAllInOutRptParams.setMoveDirection(BizConstant.SP_MOVEDIRECTION_JUSTIN);
            reticleAllInOutRptParams.setReticlePodID(reticlePodID);
            reticleAllInOutRptParams.setMoveReticles(successMoveReticlesList);
            durableService.sxReticleAllInOutRpt(objCommon, reticleAllInOutRptParams);

            // cancel reticle transport reservation if destination machine is null.
            for (i = 0; i < successCount; i++) {
                reticleMethod.reticleReticlePodReserveCancel(objCommon, successMoveReticlesList.get(i).getReticleID(), true);
            }

            // update completed RDJ/RCJ
            for (String comp : compRCJIDList) {
                reticleMethod.reticleJobStatusUpdateByReportDR(objCommon, comp, true);
            }
        }
        if (StandardProperties.OM_RTMS_CHECK_ACTIVE.isTrue()){
            //call rtms do reticle just in
            for (Infos.MoveReticles moveReticles : successMoveReticlesList) {
                ReticleRTMSJustInOutReqParams rtmsJustInOutReqParams = new ReticleRTMSJustInOutReqParams();
                rtmsJustInOutReqParams.setMoveDirection(SP_MOVEDIRECTION_JUSTIN);
                rtmsJustInOutReqParams.setReticleName(moveReticles.getReticleID().getValue());
                rtmsJustInOutReqParams.setSlotNumber(moveReticles.getSlotNumber());
                rtmsJustInOutReqParams.setReticlePodName(reticlePodID.getValue());
                rtmsRemote.requestRTMSJustInOut(rtmsJustInOutReqParams);
            }
        }



        // update failed RDJ/RCJ and send mail.
        for (i = 0; i < failCount; i++) {
            Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDROut = reticleMethod.reticleComponentJobGetByJobNameDR(objCommon, BizConstant.SP_RCJ_JOBNAME_RETRIEVE, failMoveReticlesList.get(i).getReticleID(), reticlePodID);

            Infos.ReticleComponentJob strReticleComponentJob = reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob();
            String failRCJ = strReticleComponentJob.getReticleComponentJobID();
            if (!CimObjectUtils.isEmpty(strReticleComponentJob) && !CimObjectUtils.isEmpty(failRCJ)) {
                if (log.isDebugEnabled())
                    log.debug("RCJ found for failed retrieve job {}", failRCJ);
                reticleMethod.reticleJobStatusUpdateByReportDR(objCommon, failRCJ, false);

                StringBuffer msgText = new StringBuffer();
                msgText.append("\n");
                msgText.append("Error Type          : RetrieveRpt" + "\n");
                msgText.append("\n");
                msgText.append("From Machine ID     : ");
                msgText.append(ObjectIdentifier.fetchValue(machineID)).append("\n");
                msgText.append("From Machine Port   : ");
                msgText.append(ObjectIdentifier.fetchValue(portID)).append("\n");
                msgText.append("To Machine ID       : ");
                msgText.append(ObjectIdentifier.fetchValue(machineID)).append("\n");
                msgText.append("To Machine Port     : ");
                msgText.append(ObjectIdentifier.fetchValue(portID)).append("\n");
                msgText.append("Reticle Pod ID      : ");
                msgText.append(ObjectIdentifier.fetchValue(reticlePodID)).append("\n");
                msgText.append("Reticle ID          : ");
                msgText.append(ObjectIdentifier.fetchValue(failMoveReticlesList.get(i).getReticleID())).append("\n");
                msgText.append("------------------------------------------------------------");
                msgText.append("Transaction ID      : ");
                msgText.append(objCommon.getTransactionID()).append("\n");
                OmCode armsTcsReportedError = retCodeConfigEx.getArmsTcsReportedError();
                msgText.append("Return Code         : ");
                msgText.append(armsTcsReportedError.getCode()).append("\n");
                msgText.append("Message ID          : ");
                msgText.append(armsTcsReportedError.getMessage()).append("\n");
                msgText.append("Message Text        : ");
                msgText.append(armsTcsReportedError.getMessage()).append("\n");
                msgText.append("Reason Text         : Request System Engineer to do problem determination and analysis of TCS trace log." + "\n");

                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_ARMSERROR);
                alertMessageRptParams.setSystemMessageText(msgText.toString());
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            } else {
                log.debug("RCJ not found for failed retrieve job");
            }
        }

        // put reticle event
        for (i = 0; i < successCount; i++) {
            Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
            reticleEventRecord.setEventTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
            reticleEventRecord.setBareReticleStockerID(stockerID);
            reticleEventRecord.setResourceID(resourceID);
            reticleEventRecord.setEquipmentID(equipmentID);
            reticleEventRecord.setRSPPortID(reticlePodPortID);
            reticleEventRecord.setReticlePodID(reticlePodID);
            reticleEventRecord.setReticleID(successMoveReticlesList.get(i).getReticleID());
            reticleEventRecord.setReticleJobEvent(BizConstant.SP_RETICLEJOB_RETRIEVE);
            reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);
        }
    }

    @Override
    public void sxReticleStoreReq(Infos.ObjCommon objCommon, Params.ReticleStoreReqParams params) {

        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier resourceID = params.getResourceID();
        String reticleComponentJobID = params.getReticleComponentJobID();
        String reticleDispatchJobID = params.getReticleDispatchJobID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier reticlePodPortID = params.getReticlePodPortID();
        ObjectIdentifier stockerID = params.getStockerID();
        List<Infos.MoveReticles> strMoveReticlesSeq = params.getMoveReticleList();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        Validations.check(1 != CimArrayUtils.getSize(strMoveReticlesSeq), retCodeConfig.getInvalidParameter());

        /*--------------------------------------*/
        /*   Check equipment is online or not   */
        /*--------------------------------------*/
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            if (log.isDebugEnabled()){
                log.debug("equipmentID {}", ObjectIdentifier.fetchValue(equipmentID));
            }
            Outputs.ObjMachineTypeGetOut objMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, equipmentID);
            Validations.check(objMachineTypeGetOut.isBStorageMachineFlag(), retCodeConfig.getInvalidParameter());

            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode(), equipmentID, onlineMode);

            Outputs.ObjEquipmentReticlePodPortInfoGetDROut objEquipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, equipmentID);

            //========================================================================
            // if this function (reticle store req) is not called
            // from "reticle pod loading report" function, call TCS.
            // (the purpose of calling this function from "reticle pod loading report" is only to update RDJ/RCJ information.)
            // if it is called by component job skip (TXPDC019), the pod is not loaded on RSP in db yet, so skip check logic
            //========================================================================
            if (!CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.RETICLE_POD_LOADING_RPT.getValue()) &&
                    !CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.RETICLE_COMPONENT_JOB_SKIP_REQ.getValue())) {
                boolean findFlag = false;
                List<Infos.ReticlePodPortInfo> reticlePodPortInfos = objEquipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
                long portLen = CimArrayUtils.getSize(reticlePodPortInfos);
                for (int i = 0; i < portLen; i++) {
                    Infos.ReticlePodPortInfo reticlePodPortInfo = reticlePodPortInfos.get(i);
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), reticlePodPortID) &&
                            ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        if (log.isDebugEnabled()){
                            log.debug("Found reticlePodPort and loadedReticlePod. {} {}",
                                    reticlePodPortInfo.getReticlePodPortID(),
                                    reticlePodPortInfo.getLoadedReticlePodID());
                        }
                        findFlag = true;
                        break;
                    }
                }
                Validations.check(!findFlag, retCodeConfigEx.getRtclpodNotLoaded(), reticlePodID, equipmentID, reticlePodPortID);
            }
        } else if (!ObjectIdentifier.isEmpty(stockerID)) {
            Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, stockerID);

            Validations.check(!CimStringUtils.equals(objStockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE), retCodeConfig.getInvalidParameter());

            String onlineMode = stockerMethod.stockerOnlineModeGet(objCommon, stockerID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode());

            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, stockerID);

            boolean findFlag = false;
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), resourceID) &&
                        ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                    if (log.isDebugEnabled()){
                        log.debug("Found reticlePodPort and loadedReticlePod. {} {}", reticlePodPortInfo.getReticlePodPortID(), reticlePodPortInfo.getLoadedReticlePodID());
                    }
                    findFlag = true;
                    break;
                }
            }
            Validations.check(!findFlag, retCodeConfigEx.getRtclpodNotLoaded(), reticlePodID, stockerID, resourceID);
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        /*-------------------------------------------------------------------*/
        /* If input reticle is reserved for control job,                     */
        /* that reticle has to be in the same equipment of input equipment   */
        /*-------------------------------------------------------------------*/
        Outputs.ReticleReservationDetailInfoGetOut reticleReservationDetailInfoGetOut = reticleMethod.reticleReservationDetailInfoGet(objCommon, strMoveReticlesSeq.get(0).getReticleID());
        Validations.check(reticleReservationDetailInfoGetOut.isReservedFlag(), retCodeConfigEx.getAlreadyReservedReticle());

        if (StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getIntValue() == 0) {
            if (log.isDebugEnabled()){
                log.debug("OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS == 0");
            }
            //===========================================================
            // Get current assigned control job and control job equipment
            //===========================================================
            if (CimArrayUtils.getSize(reticleReservationDetailInfoGetOut.getStrControlJobAttributeInfoSeq()) > 0) {
                if (log.isDebugEnabled()){
                    log.debug("reticle control job found!");
                }
                Validations.check(!ObjectIdentifier.equalsWithValue(reticleReservationDetailInfoGetOut.getStrControlJobAttributeInfoSeq().get(0).getMachineID(), equipmentID), retCodeConfig.getReticleRsvedForDiffEqp());
            }
        }

        /*--------------------------------------------------*/
        /*   Check reticle/slot combination in reticlePod   */
        /*--------------------------------------------------*/
        Params.ReticlePodDetailInfoInqParams infoInqParams = new Params.ReticlePodDetailInfoInqParams();
        infoInqParams.setReticlePodID(reticlePodID);
        infoInqParams.setDurableWipOperationInfoFlag(false);
        infoInqParams.setDurableOperationInfoFlag(false);
        Outputs.ObjReticlePodFillInTxPDQ013DROut objReticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, infoInqParams);

        boolean bFoundFlag = false;
        List<Infos.ContainedReticleInfo> strContainedReticleInfo = objReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo();
        for (Infos.ContainedReticleInfo containedReticleInfo : strContainedReticleInfo) {
            if (ObjectIdentifier.equalsWithValue(containedReticleInfo.getReticleID(), strMoveReticlesSeq.get(0).getReticleID()) &&
                    containedReticleInfo.getSlotNo() == strMoveReticlesSeq.get(0).getSlotNumber()) {
                if (log.isDebugEnabled()){
                    log.debug("Stored reticle/slot is {} {}", containedReticleInfo.getReticleID(), containedReticleInfo.getSlotNo());
                }
                bFoundFlag = true;
                break;
            }
        }
        Validations.check(!bFoundFlag, retCodeConfigEx.getRtclNotInReclPod(), strMoveReticlesSeq.get(0).getReticleID(), reticlePodID, strMoveReticlesSeq.get(0).getSlotNumber());

        /*-----------------------------*/
        /*   Compare RDJ information   */
        /*-----------------------------*/
        List<Infos.ReticleDispatchJob> reticleDispatchJobs = reticleMethod.reticleDispatchJobListGetDR(objCommon, reticleDispatchJobID);
        int rdjLen = CimArrayUtils.getSize(reticleDispatchJobs);
        if (rdjLen == 0) {
            log.error("reticleDispatchJobList is not found");
            Validations.check(retCodeConfigEx.getRdjNotFound());
        } else if (rdjLen > 1) {
            log.error("reticleDispatchJobList duplicated");
            Validations.check(retCodeConfigEx.getRdjDuplicate());
        }

        Infos.ReticleDispatchJob reticleDispatchJob = reticleDispatchJobs.get(0);
        Validations.check(!ObjectIdentifier.equalsWithValue(reticleDispatchJob.getReticleID(), strMoveReticlesSeq.get(0).getReticleID()) ||
                !ObjectIdentifier.equalsWithValue(reticleDispatchJob.getReticlePodID(), reticlePodID), retCodeConfigEx.getRdjIncomplete());

        /*-----------------------------*/
        /*   Compare RCJ information   */
        /*-----------------------------*/
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, reticleDispatchJobID);
        Validations.check(CimArrayUtils.isEmpty(reticleComponentJobList), retCodeConfigEx.getRcjNotFound());
        bFoundFlag = false;
        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
            if (CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_STORE) &&
                    CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE) &&
                    ObjectIdentifier.equalsWithValue(reticleComponentJob.getReticlePodID(), reticlePodID) &&
                    ObjectIdentifier.equalsWithValue(reticleComponentJob.getReticleID(), strMoveReticlesSeq.get(0).getReticleID())) {
                if (log.isDebugEnabled()){
                    log.debug("Requested information match RCJ List.");
                }
                bFoundFlag = true;
                break;
            }
        }

        Validations.check(!bFoundFlag, retCodeConfigEx.getRcjNotFound());

        //==================================================================
        // if this function (reticle store req) is not called
        // from "reticle pod loading report" function, call EAP.
        // (the purpose of calling this function from "reticle pod loading report" is only to update RDJ/RCJ information.)
        //==================================================================
        if (log.isDebugEnabled()){
            log.info("EAP notify ");
        }
        /*--------------------------*/
        /*    Send Request to EAP   */
        /*--------------------------*/
        Inputs.SendReticleStoreReqIn in = new Inputs.SendReticleStoreReqIn();
        in.setObjCommonIn(objCommon);
        in.setEquipmentID(equipmentID);
        in.setReticlePodPortID(reticlePodPortID);
        in.setStockerID(stockerID);
        in.setResourceID(resourceID);
        in.setReticlePodID(reticlePodID);
        in.setStrMoveReticles(strMoveReticlesSeq);
        // TODO: 2021/6/26 todo add EAP
//        tcsMethod.sendTCSReq(TCSReqEnum.sendReticleStoreReq, in);

        reticleMethod.reticleJobStatusUpdateByRequestDR(objCommon, reticleDispatchJobID, reticleComponentJobID, true);
    }

    @Override
    public void sxReticleStoreRpt(Infos.ObjCommon objCommon, Params.ReticleStoreRptParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier resourceID = params.getResourceID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier reticlePodPortID = params.getReticlePodPortID();
        ObjectIdentifier stockerID = params.getStockerID();
        List<Infos.ReticleStoreResult> strReticleStoreResultSeq = params.getReticleStoreResultList();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        int rsLen = CimArrayUtils.getSize(strReticleStoreResultSeq);
        Validations.check(1 != rsLen, retCodeConfig.getInvalidParameter());

        boolean bFoundFlag = false;
        ObjectIdentifier toMachineID = null, toPortID = null;
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            if (log.isDebugEnabled())
                log.debug("equipmentID {}", equipmentID);
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            Validations.check(CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getEqpOnlineMode(), ObjectIdentifier.fetchValue(equipmentID), onlineMode);

            Outputs.ObjEquipmentReticlePodPortInfoGetDROut objEquipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, equipmentID);
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = objEquipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            int portLen = CimArrayUtils.getSize(reticlePodPortInfoList);
            if (log.isDebugEnabled())
                log.debug("portLen {}", portLen);
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), reticlePodPortID)) {
                    log.debug("Reticle Pod Port found!");
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        log.debug("reticlePod/Port on equipment is correct condition.");
                        bFoundFlag = true;
                    } else {
                        log.error("reticlePod/Port on equipment is incorrect condition.");
                        Validations.check(retCodeConfigEx.getDiffRtclpodLoaded(), reticlePodPortInfo.getLoadedReticlePodID(), equipmentID, reticlePodPortInfo.getReticlePodPortID());
                    }
                }
            }
            Validations.check(!bFoundFlag, retCodeConfig.getRspportNotFound(), equipmentID, reticlePodPortID);

            toMachineID = equipmentID;
            toPortID = reticlePodPortID;
        } else if (!ObjectIdentifier.isEmpty(stockerID)) {
            if (log.isDebugEnabled())
                log.debug("stockerID {}", stockerID);

            //check onlineMode
            String onlineMode = stockerMethod.stockerOnlineModeGet(objCommon, stockerID);
            Validations.check(CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getEqpOnlineMode(), stockerID, onlineMode);

            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, stockerID);
            int portLen = CimArrayUtils.getSize(reticlePodPortInfos);
            if (log.isDebugEnabled())
                log.debug("portLen {}", portLen);

            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), resourceID)) {
                    log.debug("Reticle Pod Port (Resource) Found!");
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        log.debug("reticlePod/Port on stocker is correct condition.");
                        bFoundFlag = true;
                    } else {
                        log.error("reticlePod/Port on stocker is incorrect condition.");
                        Validations.check(retCodeConfigEx.getDiffRtclpodLoaded(), reticlePodPortInfo.getLoadedReticlePodID(), stockerID, reticlePodPortInfo.getReticlePodPortID());
                    }
                }
            }
            Validations.check(!bFoundFlag, retCodeConfig.getRspportNotFound(), stockerID, resourceID);
            toMachineID = stockerID;
            toPortID = resourceID;
        } else {
            log.error("equipmentID , stockerID not found");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        /*------------------------------*/
        /*   Check Pod/Slot condition   */
        /*------------------------------*/
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        Outputs.ObjReticlePodFillInTxPDQ013DROut strReticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        bFoundFlag = false;
        int reticleLen = CimArrayUtils.getSize(strReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo());
        if (log.isDebugEnabled())
            log.debug("reticleLen : {}", reticleLen);

        for (int i = 0; i < reticleLen; i++) {
            if (log.isDebugEnabled())
                log.debug("Round[i]-reticleLen : {}", i);
            if (ObjectIdentifier.equalsWithValue(strReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo().get(i).getReticleID(), strReticleStoreResultSeq.get(0).getReticleID()) &&
                    strReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo().get(i).getSlotNo() == strReticleStoreResultSeq.get(0).getSlotNo()) {
                bFoundFlag = true;
                break;
            }
        }
        Validations.check(!bFoundFlag, retCodeConfigEx.getRtclNotInReclPod(), strReticleStoreResultSeq.get(0).getReticleID(), strReticleStoreResultSeq.get(0).getSlotNo());

        //========================================================
        // If input reticle is reserved for control job,
        // that reticle has to be in the same equipment of input equipment
        //========================================================
        Outputs.ReticleReservationDetailInfoGetOut reticleReservationDetailInfoGetOut = reticleMethod.reticleReservationDetailInfoGet(objCommon, strReticleStoreResultSeq.get(0).getReticleID());
        Validations.check(reticleReservationDetailInfoGetOut.isReservedFlag(), retCodeConfigEx.getAlreadyReservedReticle());

        /*-------------------------------------------------------------------*/
        /* Set current timestamp as store time to reticleID                  */
        /*-------------------------------------------------------------------*/
        reticleMethod.reticleStoreTimeSet(objCommon, strReticleStoreResultSeq.get(0).getReticleID(), objCommon.getTimeStamp().getReportTimeStamp().toString());

        /*-------------------------------*/
        /*   Check RDJ/RCJ information   */
        /*-------------------------------*/
        List<Infos.ReticleDispatchJob> reticleDispatchJobs = reticleMethod.reticleDispatchJobListGetDR(objCommon, "");

        int rdjLen = CimArrayUtils.getSize(reticleDispatchJobs);
        if (log.isDebugEnabled())
            log.debug("rdjLen {}", rdjLen);

        bFoundFlag = false;
        for (Infos.ReticleDispatchJob reticleDispatchJob : reticleDispatchJobs) {
            if (ObjectIdentifier.equalsWithValue(reticleDispatchJob.getReticleID(), strReticleStoreResultSeq.get(0).getReticleID())) {
                log.debug("Found the reticle");
                bFoundFlag = true;
                break;
            }
        }

        boolean bRCJFoundFlag = false;
        if (bFoundFlag) {
            log.debug("bFoundFlag = TRUE");
            Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDROut = reticleMethod.reticleComponentJobGetByJobNameDR(objCommon, BizConstant.SP_RCJ_JOBNAME_STORE, strReticleStoreResultSeq.get(0).getReticleID(), reticlePodID);
            Infos.ReticleComponentJob strReticleComponentJob = reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob();
            if (strReticleComponentJob == null || CimObjectUtils.isEmpty(strReticleComponentJob.getReticleComponentJobID())) {
                log.debug("Reported data exist in RDJ list. but not found RCJ list.");
                Validations.check(retCodeConfigEx.getRcjIncomplete());
            } else {
                log.debug("RCJ is found");

                if (strReticleComponentJob.getSlotNo().intValue() != strReticleStoreResultSeq.get(0).getSlotNo() ||
                        !ObjectIdentifier.equalsWithValue(strReticleComponentJob.getToEquipmentID(), toMachineID) ||
                        !ObjectIdentifier.equalsWithValue(strReticleComponentJob.getToReticlePodPortID(), toPortID)) {
                    log.error("Does not match equipment, port or slot.");
                    Validations.check(retCodeConfigEx.getRcjNotMatchRequest());
                }

                bRCJFoundFlag = true;
                /*-------------------------*/
                /*   RDJ/RCJ data update   */
                /*-------------------------*/
                reticleMethod.reticleJobStatusUpdateByReportDR(objCommon, strReticleComponentJob.getReticleComponentJobID(), strReticleStoreResultSeq.get(0).getReticleStoredFlag());
            }
        }

        List<Infos.MoveReticles> strMoveReticlesSeq = new ArrayList<>();
        strMoveReticlesSeq.add(new Infos.MoveReticles(strReticleStoreResultSeq.get(0).getReticleID(), strReticleStoreResultSeq.get(0).getSlotNo()));
        if (CimBooleanUtils.isTrue(strReticleStoreResultSeq.get(0).getReticleStoredFlag())) {
            log.debug("strReticleStoreResultSeq[0].reticleStoredFlag == 1");
            /*--------------------------------------------------------------------------------------------------*/
            /*   Reticle - Reticle Pod relation remove.                                                         */
            /*   Use input parameter as following                                                               */
            /*     - moveDirection = SP_MoveDirection_JustOut                                                   */
            /*     - reticlePodID                                                                               */
            /*     - strMoveReticles : if input parameter strReticleStoreResultSeq[].reticleStoredFlag == false,*/
            /*                         omit that value                                                          */
            /*--------------------------------------------------------------------------------------------------*/
            Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
            reticleAllInOutRptParams.setMoveDirection(BizConstant.SP_MOVEDIRECTION_JUSTOUT);
            reticleAllInOutRptParams.setReticlePodID(reticlePodID);
            reticleAllInOutRptParams.setMoveReticles(strMoveReticlesSeq);
            durableService.sxReticleAllInOutRpt(objCommon, reticleAllInOutRptParams);

            reticleMethod.reticleReticlePodReserveCancel(objCommon, strReticleStoreResultSeq.get(0).getReticleID(), false);
        }

        if (StandardProperties.OM_RTMS_CHECK_ACTIVE.isTrue()){
            //call rtms do reticle just out
            for (Infos.MoveReticles moveReticles : strMoveReticlesSeq) {
                ReticleRTMSJustInOutReqParams rtmsJustInOutReqParams = new ReticleRTMSJustInOutReqParams();
                rtmsJustInOutReqParams.setMoveDirection(SP_MOVEDIRECTION_JUSTOUT);
                rtmsJustInOutReqParams.setReticleName(moveReticles.getReticleID().getValue());
                rtmsJustInOutReqParams.setSlotNumber(moveReticles.getSlotNumber());
                rtmsJustInOutReqParams.setReticlePodName(reticlePodID.getValue());
                rtmsRemote.requestRTMSJustInOut(rtmsJustInOutReqParams);
            }
        }

        if (bRCJFoundFlag && !strReticleStoreResultSeq.get(0).getReticleStoredFlag()) {
            log.debug("Reported data exist in RDJ/RCJ. but it was error report.");
            StringBuffer msgText = new StringBuffer();
            msgText.append("\n");
            msgText.append("Error Type          : StoreRpt" + "\n");
            msgText.append("\n");
            msgText.append("From Machine ID     : ");
            msgText.append(ObjectIdentifier.fetchValue(toMachineID)).append("\n");
            msgText.append("From Machine Port   : ");
            msgText.append(ObjectIdentifier.fetchValue(toPortID)).append("\n");
            msgText.append("To Machine ID       : ");
            msgText.append(ObjectIdentifier.fetchValue(toMachineID)).append("\n");
            msgText.append("To Machine Port     : ");
            msgText.append(ObjectIdentifier.fetchValue(toPortID)).append("\n");
            msgText.append("Reticle Pod ID      : ");
            msgText.append(ObjectIdentifier.fetchValue(reticlePodID)).append("\n");
            msgText.append("Reticle ID          : ");
            msgText.append(ObjectIdentifier.fetchValue(strReticleStoreResultSeq.get(0).getReticleID())).append("\n");
            msgText.append("------------------------------------------------------------");
            msgText.append("Transaction ID      : ");
            msgText.append(objCommon.getTransactionID()).append("\n");
            OmCode armsTcsReportedError = retCodeConfigEx.getArmsTcsReportedError();
            msgText.append("Return Code         : ");
            msgText.append(armsTcsReportedError.getCode()).append("\n");
            msgText.append("Message ID          : ");
            msgText.append(armsTcsReportedError.getMessage()).append("\n");
            msgText.append("Message Text        : ");
            msgText.append(armsTcsReportedError.getMessage()).append("\n");
            msgText.append("Reason Text         : Request System Engineer to do problem determination and analysis of TCS trace log." + "\n");

            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_ARMSERROR);
            alertMessageRptParams.setSystemMessageText(msgText.toString());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setSystemMessageTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }
        /*---------------------------------------------------------------------------------------*/
        /*   Create reticle event record                                                         */
        /*   Use same input parameter of reticleJob_DeleteDR - strReservedReticleJob   */
        /*---------------------------------------------------------------------------------------*/
        Infos.ReticleEventRecord strReticleEventRecord = new Infos.ReticleEventRecord();
        strReticleEventRecord.setEventTime(objCommon.getTimeStamp().getReportTimeStamp().toString());

        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, toMachineID);
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {

            log.debug("bStorageMachineFlag = FALSE");
            strReticleEventRecord.setEquipmentID(equipmentID);
            strReticleEventRecord.setRSPPortID(reticlePodPortID);
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("stockerType = SP_Stocker_Type_BareReticle");
            strReticleEventRecord.setBareReticleStockerID(stockerID);
            strReticleEventRecord.setResourceID(resourceID);
        }
        strReticleEventRecord.setReticlePodID(reticlePodID);
        strReticleEventRecord.setReticleID(strReticleStoreResultSeq.get(0).getReticleID());
        strReticleEventRecord.setReticleJobEvent(BizConstant.SP_RCJ_JOBNAME_STORE);
        reticleMethod.reticleEventQueuePutDR(objCommon, strReticleEventRecord);
    }

    @Override
    public void sxReticlePodUnloadingRpt(Infos.ObjCommon objCommon, Params.ReticlePodUnloadingRptParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier reticlePodPortID = params.getReticlePodPortID();
        ObjectIdentifier bareReticleStockerID = params.getBareReticleStockerID();
        ObjectIdentifier resourceID = params.getResourceID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();

        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=//
        //                                                                       //
        //   Check RequestUser is EAP && ReticlePod is onPort.                   //
        //                                                                       //
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=//
        if (ObjectIdentifier.equalsWithValue(objCommon.getUser().getUserID(), BizConstant.SP_TCS_PERSON)) {
            log.debug("step1 - reticleMethod.reticlePodCurrentMachineGet");
            Infos.ReticlePodCurrentMachineGetOut reticlePodCurrentMachineGetOut = reticleMethod.reticlePodCurrentMachineGet(objCommon, reticlePodID);
            if (null != reticlePodCurrentMachineGetOut) {
                if (ObjectIdentifier.isNotEmptyWithValue(reticlePodCurrentMachineGetOut.getCurrentReticlePodPortID())) {
                    //=========================================================================
                    // Get reticlePod's Current machine type
                    //=========================================================================
                    log.debug("step2 - equipmentMethod.machineTypeGet");
                    Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, reticlePodCurrentMachineGetOut.getCurrentMachineID());
                    if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                        log.debug("Current Machine is Equipment");
                    } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                        log.debug("Current Machine is BareReticleStocker.");
                    } else {
                        log.debug("ignore Unload for not on Port Equipment,BareReticleStocker");
                        return;
                    }
                } else {
                    log.debug("ignore Unload not on Port");
                    return;
                }
            }
        }
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        Boolean reqToEqp = false;
        //=========================================================================
        // Object Lock
        //=========================================================================
        ObjectIdentifier machineID = null;
        ObjectIdentifier rspPortID = null;
        if (ObjectIdentifier.isNotEmptyWithValue(equipmentID)) {
            log.debug("equipmentID != null");
            reqToEqp = true;
            machineID = equipmentID;
            rspPortID = reticlePodPortID;
        } else {
            log.debug("equipment == null");
            machineID = bareReticleStockerID;
            rspPortID = resourceID;
        }

        //for reticle pod port
        log.debug("step3 - objectLockMethod.objectLockForEquipmentResource");
        objectLockMethod.objectLockForEquipmentResource(objCommon, machineID, rspPortID, BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

        log.debug("step4 - durableMethod.durableDurableControlJobIDGet");
        ObjectIdentifier durableControlJobIDGet = durableMethod.durableDurableControlJobIDGet(objCommon, reticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD);
        if (ObjectIdentifier.isNotEmptyWithValue(durableControlJobIDGet)) {
            if (log.isDebugEnabled())
                log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableControlJobIDGet));
            objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobIDGet);
            log.debug("step5 - durableMethod.durableControlJobStatusGet");
            Infos.DurableControlJobStatusGet durableControlJobStatusGet = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobIDGet);
            if (!CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED)
                    && !CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                Validations.check(true, retCodeConfig.getInvalidDcjstatus(), durableControlJobStatusGet.getDurableControlJobStatus());
            }
        }
        // for reticle pod
        ObjectIdentifier tmpObjectID = reticlePodID;
        log.debug("step6 - bjectLockMethod.objectLock");
        objectLockMethod.objectLock(objCommon, CimReticlePod.class, tmpObjectID);

        //=========================================================================
        // Get reticle pod port information
        //=========================================================================
        List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfoList = new ArrayList<>();
        if (ObjectIdentifier.isNotEmptyWithValue(equipmentID) && ObjectIdentifier.isNotEmptyWithValue(reticlePodPortID)) {
            log.debug("equipmentID and reticlePodPortID ");
            log.debug("step7 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, equipmentID);
            if (null != equipmentReticlePodPortInfoGetDROut) {
                tmpReticlePodPortInfoList = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            }
        } else if (ObjectIdentifier.isNotEmptyWithValue(bareReticleStockerID) && ObjectIdentifier.isNotEmptyWithValue(resourceID)) {
            log.debug("bareReticleStockerID and resourceID ");
            log.debug("step8 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, bareReticleStockerID);
            tmpReticlePodPortInfoList = reticlePodPortInfos;
        } else {
            log.error("Other");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }
        //=========================================================================
        // Check port is vacant or not
        //=========================================================================
        Boolean bReticlePodPortFound = false;
        if (CimArrayUtils.isNotEmpty(tmpReticlePodPortInfoList)) {
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : tmpReticlePodPortInfoList) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortID, reticlePodPortInfo.getReticlePodPortID())
                        || ObjectIdentifier.equalsWithValue(resourceID, reticlePodPortInfo.getReticlePodPortID())) {
                    bReticlePodPortFound = true;
                    if (log.isDebugEnabled())
                        log.debug("loaded ReticlePodID {}", ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()));
                    if (ObjectIdentifier.isEmptyWithValue(reticlePodPortInfo.getLoadedReticlePodID())) {
                        log.debug("reticlePod not loaded.");
                        if (log.isDebugEnabled())
                            log.debug("reqToEqp {}", reqToEqp);
                        if (CimBooleanUtils.isTrue(reqToEqp)) {
                            Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                                    ObjectIdentifier.fetchValue(reticlePodID),
                                    ObjectIdentifier.fetchValue(equipmentID),
                                    ObjectIdentifier.fetchValue(reticlePodPortID));
                        } else {
                            Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                                    ObjectIdentifier.fetchValue(reticlePodID),
                                    ObjectIdentifier.fetchValue(bareReticleStockerID),
                                    ObjectIdentifier.fetchValue(resourceID));
                        }
                    }
                    if (!ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        if (log.isDebugEnabled()){
                            log.debug("unload request reticlePodID {}", ObjectIdentifier.fetchValue(reticlePodID));
                            log.debug("reqToEqp {}", reqToEqp);
                        }
                        if (CimBooleanUtils.isTrue(reqToEqp)) {
                            Validations.check(true, retCodeConfigEx.getDiffRtclpodLoaded(),
                                    ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()),
                                    ObjectIdentifier.fetchValue(equipmentID),
                                    ObjectIdentifier.fetchValue(reticlePodPortID));
                        } else {
                            Validations.check(true, retCodeConfigEx.getDiffRtclpodLoaded(),
                                    ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()),
                                    ObjectIdentifier.fetchValue(bareReticleStockerID),
                                    ObjectIdentifier.fetchValue(resourceID));
                        }
                    }
                }
            }
        }
        if (CimBooleanUtils.isFalse(bReticlePodPortFound)) {
            if (log.isDebugEnabled())
                log.debug("reqToEqp {}", reqToEqp);
            if (CimBooleanUtils.isTrue(reqToEqp)) {
                Validations.check(true, retCodeConfig.getRspportNotFound(),
                        ObjectIdentifier.fetchValue(equipmentID),
                        ObjectIdentifier.fetchValue(reticlePodPortID));
            } else {
                Validations.check(true, retCodeConfig.getRspportNotFound(),
                        ObjectIdentifier.fetchValue(bareReticleStockerID),
                        ObjectIdentifier.fetchValue(resourceID));
            }
        }
        //=========================================================================
        // Get reticle pod transfer status
        //=========================================================================
        log.debug("step9 - reticleMethod.reticlePodTransferStateGetDR");
        String transferStatus = reticleMethod.reticlePodTransferStateGetDR(objCommon, reticlePodID);
        if (log.isDebugEnabled())
            log.debug("transferStatus {}", transferStatus);

        //=========================================================================
        // Check reticle pod transfer status
        //=========================================================================
        if (!CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                && !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)) {
            Validations.check(true, retCodeConfig.getInvalidReticlepodXferStat(),
                    ObjectIdentifier.fetchValue(reticlePodID),
                    transferStatus);
        }

        //=========================================================================
        // UnLoad reticle pod onto equipment reticle pod port
        //=========================================================================
        log.debug("step10 - reticleMethod.reticlePodTransferStateGetDR");
        if (ObjectIdentifier.isNotEmptyWithValue(equipmentID)) {
            equipmentMethod.equipmentReticlePodUnload(objCommon,
                    equipmentID,
                    reticlePodPortID,
                    reticlePodID,
                    params.getOpeMemo());
        } else {
            log.debug("step11 - stockerMethod.stockerReticlePodUnload");
            stockerMethod.stockerReticlePodUnload(objCommon,
                    bareReticleStockerID,
                    resourceID,
                    reticlePodID,
                    params.getOpeMemo(), null);
        }
        //=========================================================================
        // Event Make
        //=========================================================================
        Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
        reticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        if (ObjectIdentifier.isNotEmptyWithValue(equipmentID)) {
            log.debug("equipmentID is not blank");
            reticleEventRecord.setEquipmentID(equipmentID);
            reticleEventRecord.setRSPPortID(reticlePodPortID);
        } else {
            log.debug("equipmentID is blank");
            reticleEventRecord.setBareReticleStockerID(bareReticleStockerID);
            reticleEventRecord.setResourceID(resourceID);
        }

        log.debug("step12 - reticleMethod.reticleEventQueuePutDR");
        reticleEventRecord.setReticlePodID(reticlePodID);
        reticleEventRecord.setRSPPortEvent(SP_RSP_EVENT_UNLOAD);
        reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);
    }


    @Override
    public void sxReticlePodLoadingRpt(Infos.ObjCommon objCommon, Params.ReticlePodLoadingRptParams params) {
        ObjectIdentifier bareReticleStockerID = params.getBareReticleStockerID();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier resourceID = params.getResourceID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier reticlePodPortID = params.getReticlePodPortID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        boolean reqToEqp = false;

        //=========================================================================
        // Object Lock
        //=========================================================================
        ObjectIdentifier machineID;
        ObjectIdentifier RSPportID;
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            if (log.isDebugEnabled())
                log.debug("equipmentID specified. eqpID {}", ObjectIdentifier.fetchValue(equipmentID));
            reqToEqp = true;
            machineID = equipmentID;
            RSPportID = reticlePodPortID;
        } else {
            log.debug("equipmentID not specified.");
            machineID = bareReticleStockerID;
            RSPportID = resourceID;
        }
        objectLockMethod.objectLockForEquipmentResource(objCommon, machineID, RSPportID, BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

        ObjectIdentifier tmpobjectID = reticlePodID;

        lockMethod.objectLock(objCommon, CimReticlePod.class, tmpobjectID);
        //=========================================================================
        // Get reticle pod port information
        //=========================================================================
        List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfoSeq = new ArrayList<>();

        if (!ObjectIdentifier.isEmpty(equipmentID) && !ObjectIdentifier.isEmpty(reticlePodPortID)) {
            log.debug("EquipmentID and reticlePodPortID specified.");
            if (log.isDebugEnabled()){
                log.debug("EquipmentID      {}", ObjectIdentifier.fetchValue(equipmentID));
                log.debug("reticlePodPortID {}", ObjectIdentifier.fetchValue(reticlePodPortID));
            }

            Outputs.ObjEquipmentReticlePodPortInfoGetDROut objEquipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, equipmentID);

            tmpReticlePodPortInfoSeq = objEquipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();

        } else if (!ObjectIdentifier.isEmpty(bareReticleStockerID) && !ObjectIdentifier.isEmpty(resourceID)) {
            log.debug("bareReticleStockerID and resourceID specified.");
            if (log.isDebugEnabled()){
                log.debug("bareReticleStockerID {}", ObjectIdentifier.fetchValue(bareReticleStockerID));
                log.debug("resourceID           {}", ObjectIdentifier.fetchValue(resourceID));
            }
            tmpReticlePodPortInfoSeq = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, bareReticleStockerID);
        } else {
            log.error("eqpID & portID  or  brstkID & resourceID not specified. parameter is invalid.");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        //=========================================================================
        // Check port is vacant or not
        //=========================================================================
        boolean bReticlePodPortFound = false;
        int ReticlePodPortInfoSeqLen = CimArrayUtils.getSize(tmpReticlePodPortInfoSeq);

        if (log.isDebugEnabled())
            log.debug("ReticlePodPortInfoSeqLen: {}", ReticlePodPortInfoSeqLen);

        for (int i = 0; i < ReticlePodPortInfoSeqLen; i++) {
            Infos.ReticlePodPortInfo reticlePodPortInfo = tmpReticlePodPortInfoSeq.get(i);
            if (log.isDebugEnabled()){
                log.info("Round[i]-ReticlePodPortInfoSeqLen {}", i);

                // Search reticlePodPort
                // from specified equipment's or bare reticle stocker's reticle pod port information

                log.info("Check for reticlePodPort {}", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));
            }
            if (ObjectIdentifier.equalsWithValue(reticlePodPortID, reticlePodPortInfo.getReticlePodPortID()) ||
                    ObjectIdentifier.equalsWithValue(resourceID, reticlePodPortInfo.getReticlePodPortID())) {
                if (log.isDebugEnabled())
                    log.debug("Reticle Pod Port Found {}", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));
                if (reqToEqp) {
                    if (log.isDebugEnabled())
                        log.debug("reticlePodPort {} found on equipment {}.", ObjectIdentifier.fetchValue(equipmentID), ObjectIdentifier.fetchValue(reticlePodPortID));
                } else {
                    if (log.isDebugEnabled())
                        log.debug("reticlePodPort {} found on brstocker {}.", ObjectIdentifier.fetchValue(bareReticleStockerID), ObjectIdentifier.fetchValue(resourceID));
                }
                bReticlePodPortFound = true;

                if (log.isDebugEnabled())
                    log.debug("loaded ReticlePod: {}", reticlePodPortInfo.getLoadedReticlePodID());

                if (!ObjectIdentifier.isEmpty(reticlePodPortInfo.getLoadedReticlePodID())) {
                    if (log.isDebugEnabled())
                        log.debug("loadedReticlePodID exists {}", ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()));
                    if (reqToEqp) {
                        log.error("reqToEqp = TRUE");
                        Validations.check(retCodeConfigEx.getDiffRtclpodLoaded(), reticlePodPortInfo.getLoadedReticlePodID(), equipmentID, reticlePodPortID);
                    } else {
                        log.error("reqToEqp = FALSE");
                        Validations.check(retCodeConfigEx.getDiffRtclpodLoaded(), reticlePodPortInfo.getLoadedReticlePodID(), bareReticleStockerID, resourceID);
                    }
                }

                //=========================================================================
                // Check port has reticle pod reserve information or not
                // If reserved, compare reticle pod / reserved reticle pod
                //=========================================================================
                if (log.isDebugEnabled())
                    log.debug("reserved ReticlePod: {}", reticlePodPortInfo.getReservedReticlePodID());

                if (!ObjectIdentifier.isEmpty(reticlePodPortInfo.getReservedReticlePodID()) &&
                        !ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReservedReticlePodID(), reticlePodID)) {
                    if (log.isDebugEnabled())
                        log.debug("different reserved reticle pod exists {}", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReservedReticlePodID()));
                    if (reqToEqp) {
                        log.error("reqToEqp = TRUE");
                        Validations.check(retCodeConfigEx.getDiffRtclpodReserved(), equipmentID, reticlePodPortID, reticlePodPortInfo.getReservedReticlePodID());
                    } else {
                        log.error("reqToEqp = FALSE");
                        Validations.check(retCodeConfigEx.getDiffRtclpodReserved(), bareReticleStockerID, resourceID, reticlePodPortInfo.getReservedReticlePodID());
                    }
                }

            } else {
                if (log.isDebugEnabled())
                    log.debug("Not specified Reticle Pod Port {}", reticlePodPortInfo.getReticlePodPortID());
                if (!ObjectIdentifier.isEmpty(reticlePodPortInfo.getReservedReticlePodID()) &&
                        ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReservedReticlePodID(), reticlePodID)) {
                    if (log.isDebugEnabled())
                        log.debug("reqToEqp {}", reqToEqp);
                    if (reqToEqp) {
                        log.error("reqToEqp = TRUE ");
                        Validations.check(retCodeConfigEx.getDiffRtclpodPortReserved(), reticlePodPortInfo.getReservedReticlePodID(), reticlePodPortID);
                    } else {
                        log.error("reqToEqp = FALSE ");
                        Validations.check(retCodeConfigEx.getDiffRtclpodPortReserved(), reticlePodPortInfo.getReservedReticlePodID(), resourceID);
                    }
                }
            }
        }
        if (!bReticlePodPortFound) {
            log.debug("bReticlePodPortFound == FALSE");
            if (log.isDebugEnabled()){
                log.info("equipmentID    {}", equipmentID);
                log.info("reticlePodPort {}", reticlePodPortID);
                log.info("reqToEqp {}", reqToEqp);
            }
            if (reqToEqp) {
                log.error("reqToEqp = TRUE ");
                Validations.check(retCodeConfig.getRspportNotFound(), equipmentID, reticlePodPortID);
            } else {
                log.error("reqToEqp = FALSE ");
                Validations.check(retCodeConfig.getRspportNotFound(), bareReticleStockerID, resourceID);
            }
        }

        //=========================================================================
        // Get reticle pod transfer status
        //=========================================================================
        String transferStatus = reticleMethod.reticlePodTransferStateGet(objCommon, reticlePodID);
        if (log.isDebugEnabled())
            log.debug("ReticlePod transferStatus {}", transferStatus);

        //=========================================================================
        // Check reticle pod transfer status
        //=========================================================================
        Validations.check(CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONIN) ||
                CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_BAYIN) ||
                CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_MANUALIN) ||
                CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ||
                CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_SHELFIN) ||
                CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEIN) ||
                CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALIN), retCodeConfig.getInvalidReticlepodXferStat(), reticlePodID, transferStatus);

        //=========================================================================
        // Load reticle pod onto equipment reticle pod port
        //=========================================================================
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            equipmentMethod.equipmentReticlePodLoad(objCommon, equipmentID, reticlePodPortID, reticlePodID, params.getOpeMemo());
        } else {
            stockerMethod.stockerReticlePodLoad(objCommon, bareReticleStockerID, resourceID, reticlePodID, params.getOpeMemo());
        }

        //=========================================================================
        // Event Make
        //=========================================================================
        Infos.ReticleEventRecord strReticleEventRecord = new Infos.ReticleEventRecord();

        strReticleEventRecord.setEventTime(objCommon.getTimeStamp().getReportTimeStamp().toString());

        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            if (log.isDebugEnabled())
                log.debug("equipmentID {}", ObjectIdentifier.fetchValue(equipmentID));
            strReticleEventRecord.setEquipmentID(equipmentID);
            strReticleEventRecord.setRSPPortID(reticlePodPortID);
        } else {
            if (log.isDebugEnabled())
                log.debug("equipmentID is blank");
            strReticleEventRecord.setBareReticleStockerID(bareReticleStockerID);
            strReticleEventRecord.setResourceID(resourceID);
        }
        strReticleEventRecord.setReticlePodID(reticlePodID);
        strReticleEventRecord.setRSPPortEvent(BizConstant.SP_RSP_EVENT_LOAD);
        reticleMethod.reticleEventQueuePutDR(objCommon, strReticleEventRecord);
    }

    @Override
    public void sxBareReticleStockerOnlineModeChangeReq(Infos.ObjCommon objCommon, Params.BareReticleStockerOnlineModeChangeReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        ObjectIdentifier bareReticleStockerID = params.getBareReticleStockerID();
        Boolean notifyToEAPFlag = params.getNotifyToEAPFlag();
        String newOnlineMode = params.getNewOnlineMode();


        log.debug("in-para bareReticleStockerID   :{}", ObjectIdentifier.fetchValue(bareReticleStockerID));
        log.debug("in-para newOnlineMode          :{}", newOnlineMode);

        // --------------------------------------------------------------------------------
        // 1. Input stocker exist check, and BareReticleStocker type check.
        // --------------------------------------------------------------------------------
        log.debug("step1 - stockerMethod.stockerTypeGet");
        Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = stockerMethod.stockerTypeGet(objCommon, bareReticleStockerID);
        log.debug("stockerType: {}", stockerTypeGetDROut.getStockerType());
        Validations.check(!CimStringUtils.equals(stockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE), retCodeConfigEx.getStkTypeDifferent(), stockerTypeGetDROut.getStockerType());

        // --------------------------------------------------------------------------------
        // 2. Input newOnlineMode check, "Off-Line" or "On-Line Local" or "On-Line Remote".
        // --------------------------------------------------------------------------------
        Validations.check(!CimStringUtils.equals(newOnlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                && !CimStringUtils.equals(newOnlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINELOCAL)
                && !CimStringUtils.equals(newOnlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfigEx.getInvalidOnLineMode(), newOnlineMode);

        // --------------------------------------------------------------------------------
        // 3. Comparison with OnlineMode of Now Stocker
        // --------------------------------------------------------------------------------
        log.debug("step2 - stockerMethod.stockerOnlineModeGet");
        String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, bareReticleStockerID);
        log.debug("Now Stocker OnlineMode  {}", stockerOnlineModeGet);
        Validations.check(CimStringUtils.equals(stockerOnlineModeGet, newOnlineMode), retCodeConfigEx.getOnLineModeSame(),
                stockerOnlineModeGet,
                ObjectIdentifier.fetchValue(bareReticleStockerID));

        // --------------------------------------------------------------------------------
        //   Check RSPport status validity.
        // --------------------------------------------------------------------------------
        String checkFlag = StandardProperties.OM_RTCLPOD_ON_PORT_CHECK.getValue();
        if (CimStringUtils.equals(checkFlag, BizConstant.VALUE_ONE)) {
            log.debug("OnlineMode check!!");
            /***********************************/
            /*  Check current BRS online mode  */
            /***********************************/
            if (CimStringUtils.equals(stockerOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                log.debug("BRS current online mode is offline. check port status.");
                /**********************************************************/
                /*  Check following items.                                */
                /*   1. Port dispatch state (should not be 'Dispatched')  */
                /*   2. Any RSP shuold not be on RSP port                 */
                /**********************************************************/
                log.debug("step3 - stockerMethod.stockerReticlePodPortInfoGetDR");
                List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, bareReticleStockerID);
                if (CimArrayUtils.isNotEmpty(reticlePodPortInfoList)) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                        Validations.check(ObjectIdentifier.isNotEmptyWithValue(reticlePodPortInfo.getLoadedReticlePodID())
                                        || ObjectIdentifier.isNotEmptyWithValue(reticlePodPortInfo.getReservedReticlePodID())
                                        || CimStringUtils.equals(reticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED), retCodeConfigEx.getReticlePodNotOnRspPort(),
                                ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));
                    }
                }
            }
        }

        // --------------------------------------------------------------------------------
        // 4. Update "FRSTK.STOCKER_ONLINE_MODE"
        // --------------------------------------------------------------------------------
        log.debug("Update to newOnlineMode {}", newOnlineMode);
        log.debug("step4 - stockerMethod.stockerOnlineModeChange");
        stockerMethod.stockerOnlineModeChange(objCommon, bareReticleStockerID, newOnlineMode);

        // --------------------------------------------------------------------------------
        // 5. Call EAP Function
        // --------------------------------------------------------------------------------

        if (CimBooleanUtils.isTrue(notifyToEAPFlag)){
            log.debug("notifyToEAPFlag is {}",notifyToEAPFlag);
            Inputs.SendBareReticleStockerOnlineModeChangeReqIn in = new Inputs.SendBareReticleStockerOnlineModeChangeReqIn();
            in.setObjCommonIn(objCommon);
            in.setRequestUserID(objCommon.getUser());
            in.setBareReticleStockerID(bareReticleStockerID);
            in.setNewOnlineMode(newOnlineMode);
            log.debug("step5 - tcsMethod.sendTCSReq");
//        tcsMethod.sendTCSReq(TCSReqEnum.sendBareReticleStockerOnlineModeChangeReq, in);
        }
    }


    @Override
    public Results.ReticlePodInventoryReqResult sxReticlePodInventoryReq(Infos.ObjCommon objCommon, Params.ReticlePodInventoryReqParams params) {
        Results.ReticlePodInventoryReqResult result = new Results.ReticlePodInventoryReqResult();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier stockerID = params.getStockerID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        OmCode checkResult = new OmCode();
        Boolean notFoundRpodFlg = false;
        //-----------------------------------------------
        //   Stocker case
        //-----------------------------------------------
        if (ObjectIdentifier.isNotEmpty(stockerID)) {
            log.debug("stockerID != null");
            //------------------------
            //   Check Stocker Type
            //------------------------
            log.debug("step1 - stockerMethod.stockerTypeGet");
            Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = stockerMethod.stockerTypeGet(objCommon, stockerID);
            if (log.isDebugEnabled())
                log.debug("stockerType {}", stockerTypeGetDROut.getStockerType());
            Validations.check(!CimStringUtils.equals(stockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD),
                    retCodeConfigEx.getNotReticleStocker(), stockerTypeGetDROut.getStockerType());

            //----------------------------
            //   Check Inventory Status
            //----------------------------
            log.debug("step2 - stockerMethod.stockerInventoryStateGet");
            Boolean inventoryRequestedFlag = stockerMethod.stockerInventoryStateGet(objCommon, stockerID);
            Validations.check(CimBooleanUtils.isTrue(inventoryRequestedFlag), retCodeConfig.getStockerInventoryInProcess(),
                    ObjectIdentifier.fetchValue(stockerID));


            /*-----------------------------*/
            /*   Change Inventory Status   */
            /*-----------------------------*/
            log.debug("step3 - stockerMethod.stockerInventoryStateChange");
            stockerMethod.stockerInventoryStateChange(objCommon, stockerID, true);

            //---------------------------------------
            //   Request to upload inventory info
            //---------------------------------------
            // TODO: 2020/11/20 not rtmUnloadInventory is same as unloadInventory because of MCS simulator same
            Inputs.SendUploadInventoryReqIn sendUploadInventoryReqIn = new Inputs.SendUploadInventoryReqIn();
            sendUploadInventoryReqIn.setObjCommon(objCommon);
            sendUploadInventoryReqIn.setUser(objCommon.getUser());
            Infos.UploadInventoryReq uploadInventoryReq = new Infos.UploadInventoryReq();
            uploadInventoryReq.setMachineID(stockerID);
            uploadInventoryReq.setUploadLevel("");
            sendUploadInventoryReqIn.setUploadInventoryReq(uploadInventoryReq);
            log.debug("step4 - tmsService.uploadInventoryReq");
            Results.AmhsUploadInventoryReqResult uploadInventoryReqResult = tmsService.uploadInventoryReq(sendUploadInventoryReqIn);

            List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo = new ArrayList<>();
            if (null != uploadInventoryReq && CimArrayUtils.isNotEmpty(uploadInventoryReqResult.getUploadInventoryReqResults())) {
                for (Results.UploadInventoryReqResult inventoryReqResult : uploadInventoryReqResult.getUploadInventoryReqResults()) {
                    /*-----------------------*/
                    /*   Check Port Status   */
                    /*-----------------------*/
                    // Return to TMS reticlePodID. Check portStatus Loaded case
                    Infos.ReticlePodCurrentMachineGetOut reticlePodCurrentMachineGetOut = null;
                    log.debug("step5 - reticleMethod.reticlePodCurrentMachineGet");
                    try {
                        reticlePodCurrentMachineGetOut = reticleMethod.reticlePodCurrentMachineGet(objCommon, inventoryReqResult.getCarrierID());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getInvalidStockerType(), e.getCode())) {
                            log.debug("rc = INVALID_STOCKER_TYPE");
                            Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
                            reticlePodDetailInfoInqParams.setReticlePodID(inventoryReqResult.getCarrierID());
                            reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
                            reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);

                            log.debug("step6 - reticleMethod.reticlePodFillInTxPDQ013DR");
                            Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);
                            if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo()) {
                                Infos.ReticlePodStatusInfo reticlePodStatusInfo = reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo();
                                if (ObjectIdentifier.isNotEmptyWithValue(reticlePodStatusInfo.getStockerID())) {
                                    log.info("step7 - stockerMethod.stockerTypeGetDR");
                                    Outputs.ObjStockerTypeGetDROut stockerTypeGetDR = stockerMethod.stockerTypeGetDR(objCommon, reticlePodStatusInfo.getStockerID());
                                    if (CimStringUtils.equals(stockerTypeGetDR.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLESHELF)) {
                                        log.debug("stockerType is ReticleShelf");
                                    } else {
                                        log.error("stockerType is not ReticleShelf");
                                        Validations.check(true, retCodeConfig.getInvalidStockerType());
                                    }
                                } else {
                                    log.error("stocker is null");
                                    Validations.check(true, retCodeConfig.getInvalidStockerType());
                                }
                            } else if (Validations.isEquals(retCodeConfig.getNotFoundReticlePod(), e.getCode())) {
                                if (log.isDebugEnabled())
                                    log.debug("this reticlePod {} is not registered in OMS", ObjectIdentifier.fetchValue(inventoryReqResult.getCarrierID()));
                                notFoundRpodFlg = true;
                                continue;
                            } else {
                                throw e;
                            }
                        }
                    }
                    Long machineType = 0L;
                    if (reticlePodCurrentMachineGetOut != null && ObjectIdentifier.isNotEmptyWithValue(reticlePodCurrentMachineGetOut.getCurrentMachineID())) {
                        log.debug("step8 - equipmentMethod.machineTypeGet");
                        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, reticlePodCurrentMachineGetOut.getCurrentMachineID());
                        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                            machineType = 1L;
                        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                            machineType = 2L;
                        }
                        if (log.isDebugEnabled())
                            log.debug("machineType: {}", machineType);
                    }
                    Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
                    reticlePodDetailInfoInqParams.setReticlePodID(inventoryReqResult.getCarrierID());
                    reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
                    reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
                    log.debug("step9 - durableInqService.sxReticlePodDetailInfoInq");
                    Results.ReticlePodDetailInfoInqResult reticlePodDetailInfoInqResult = durableInqService.sxReticlePodDetailInfoInq(objCommon, reticlePodDetailInfoInqParams);

                    if (machineType != 1L && machineType != 2L) {
                        log.debug("Return to TMS reticlePodID is in reticlePodStocker");
                        Infos.InventoryReticlePodInfo inventoryReticlePodInfo = new Infos.InventoryReticlePodInfo();
                        inventoryReticlePodInfo.setReticlePodID(inventoryReqResult.getCarrierID());
                        strInventoryReticlePodInfo.add(inventoryReticlePodInfo);
                    } else if (CimStringUtils.equals(reticlePodDetailInfoInqResult.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                        log.debug("transferStatus = 'EI'");
                        log.debug("step10 - equipmentMethod.equipmentReticlePodUnload");
                        equipmentMethod.equipmentReticlePodUnload(objCommon,
                                reticlePodCurrentMachineGetOut.getCurrentMachineID(),
                                reticlePodCurrentMachineGetOut.getCurrentReticlePodPortID(),
                                inventoryReqResult.getCarrierID(),
                                params.getOpeMemo());
                        Infos.InventoryReticlePodInfo inventoryReticlePodInfo = new Infos.InventoryReticlePodInfo();
                        inventoryReticlePodInfo.setReticlePodID(inventoryReqResult.getCarrierID());
                        strInventoryReticlePodInfo.add(inventoryReticlePodInfo);

                    } else if (CimStringUtils.equals(reticlePodDetailInfoInqResult.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_STATIONIN) && machineType == 2L) {
                        log.debug("transferStatus = 'SI'");
                        log.debug("step11 - stockerMethod.stockerReticlePodUnload");
                        stockerMethod.stockerReticlePodUnload(objCommon, reticlePodCurrentMachineGetOut.getCurrentMachineID(),
                                reticlePodCurrentMachineGetOut.getCurrentReticlePodPortID(),
                                inventoryReqResult.getCarrierID(),
                                params.getOpeMemo(), null);
                        Infos.InventoryReticlePodInfo inventoryReticlePodInfo = new Infos.InventoryReticlePodInfo();
                        inventoryReticlePodInfo.setReticlePodID(inventoryReqResult.getCarrierID());
                        strInventoryReticlePodInfo.add(inventoryReticlePodInfo);
                    } else {
                        log.debug("transferStatus != 'EI','SI'");
                        Infos.InventoryReticlePodInfo inventoryReticlePodInfo = new Infos.InventoryReticlePodInfo();
                        inventoryReticlePodInfo.setReticlePodID(inventoryReqResult.getCarrierID());
                        strInventoryReticlePodInfo.add(inventoryReticlePodInfo);
                    }
                }

                if (CimArrayUtils.isNotEmpty(strInventoryReticlePodInfo)) {
                    for (Infos.InventoryReticlePodInfo inventoryReticlePodInfo : strInventoryReticlePodInfo) {
                        if (log.isDebugEnabled())
                            log.debug("inventory carrierID {}", ObjectIdentifier.fetchValue(inventoryReticlePodInfo.getReticlePodID()));
                    }
                }

                List<Results.InventoriedReticlePodInfo> inventoriedReticlePodInfos = null;
                log.debug("step12 - reticleMethod.reticlePodPositionUpdateByStockerInventoryDR");
                try {
                    inventoriedReticlePodInfos = reticleMethod.reticlePodPositionUpdateByStockerInventoryDR(objCommon, stockerID, strInventoryReticlePodInfo, null);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getSomertclinvDataError(), e.getCode())) {
                        checkResult.setCode(e.getCode());
                        checkResult.setMessage(e.getMessage());
                    } else {
                        throw e;
                    }
                }

                /*-----------------------------*/
                /*   Change Inventory Status   */
                /*-----------------------------*/
                log.debug("step13 - stockerMethod.stockerInventoryStateChange");
                stockerMethod.stockerInventoryStateChange(objCommon, stockerID, false);
            }
        }
        /*---------------------------*/
        /*   Input Parameter Error   */
        /*---------------------------*/
        else {
            log.error("stockerID == null");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        result.setStockerID(stockerID);
        if (!Validations.isSuccess(checkResult)) {
            //Controller will catch the exception return ok
            Validations.check(true, checkResult);
        }

        if (CimBooleanUtils.isTrue(notFoundRpodFlg)) {
            log.error("When Inventry Upload was executed, ReticlePod not exist in OMS is reported.");
            //Controller will catch the exception return ok
            Validations.check(true, retCodeConfigEx.getReportedNotMmdurable());
        }
        return result;
    }


    @Override
    public void sxReticlePodOfflineLoadingReq(Infos.ObjCommon objCommon, Params.ReticlePodOfflineLoadingReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        ObjectIdentifier machineID = params.getMachineID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        Boolean bForceLoadFlag = params.getBForceLoadFlag();

        log.debug("in-para machineID {}", ObjectIdentifier.fetchValue(machineID));
        log.debug("in-para portID {}", ObjectIdentifier.fetchValue(portID));
        log.debug("in-para reticlePodID {}", ObjectIdentifier.fetchValue(reticlePodID));
        log.debug("in-para bForceLoadFlag {}", bForceLoadFlag);


        //get and check machine type
        log.debug("step1 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);

        String machineType = null;
        Boolean isEquipment = false;
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("bStorageMachineFlag == FALSE");
            isEquipment = true;
            machineType = BizConstant.SP_MACHINE_TYPE_EQP;
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("stockerType = BareReticle");
            isEquipment = false;
            machineType = machineTypeGetOut.getStockerType();
        } else {
            log.debug("This machine {} {} is invalid for request.", ObjectIdentifier.fetchValue(machineID),
                    machineTypeGetOut.getStockerType());
            Validations.check(true, retCodeConfigEx.getStkTypeDifferent(), machineTypeGetOut.getStockerType());
        }

        //object lock for RSP port.
        log.debug("isEquipment == TRUE");
        log.debug("step2 - objectLockMethod.objectLockForEquipmentResource");
        objectLockMethod.objectLockForEquipmentResource(objCommon, machineID, portID, BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

        //object lock for RSP
        log.debug("step3 - objectLockMethod.objectLock");
        objectLockMethod.objectLock(objCommon, CimReticlePod.class, reticlePodID);

        //if bForceLoadFlag is not set, when check equipment status, reticle pod reservation and RDJ/RCJ
        if (CimBooleanUtils.isFalse(bForceLoadFlag)) {
            log.debug("bForceLoadFlag == FALSE");
            //check equipment status is Offline or not
            String machineOnlineMode = null;
            if (CimBooleanUtils.isTrue(isEquipment)) {
                log.debug("isEquipment == TRUE");
                log.debug("step4 - equipmentMethod.equipmentOnlineModeGet");
                String equipmentOnlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, machineID);
                log.debug("Equipment's online mode {}", equipmentOnlineModeGet);
                machineOnlineMode = equipmentOnlineModeGet;
            } else {
                log.debug("isEquipment == FALSE");
                log.debug("step5 - stockerMethod.stockerOnlineModeGet");
                String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, machineID);
                log.debug("Now Stocker OnlineMode {}", stockerOnlineModeGet);
                machineOnlineMode = stockerOnlineModeGet;
            }

            if (!CimStringUtils.equals(machineOnlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                log.debug("OnlineMode is not offline.");
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        machineOnlineMode);
            }

            //check reticlePod does not have transfer reservation.
            log.debug("step6 - reticleMethod.reticlePodTransferReservationCheck");
            reticleMethod.reticlePodTransferReservationCheck(objCommon, reticlePodID);

            //check reticlePod is not reserved by RDJ
            log.debug("step7 - reticleMethod.reticleDispatchJobCheckExistenceDR");
            List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, null, reticlePodID, null);

            List<Infos.ReticleComponentJob> reticleComponentJobList = null;
            log.debug("step8 - reticleMethod.reticleComponentJobCheckExistenceDR");
            try {
                reticleComponentJobList = reticleMethod.reticleComponentJobCheckExistenceDR(objCommon, null, reticlePodID, null, null, machineID, portID);
            } catch (ServiceException e) {
                reticleComponentJobList = e.getData(List.class);
                if (Validations.isEquals(retCodeConfigEx.getFoundInRcj(), e.getCode())) {
                    if (CimArrayUtils.isNotEmpty(reticleComponentJobList)) {
                        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
                            if (!CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)
                                    || CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE)
                                    || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), portID)) {
                                throw e;
                            }
                        }
                    }
                } else {
                    throw e;
                }
            }
        } //bForceLoadFlag == FALSE

        //get RSP port infomation
        List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfoList = new ArrayList<>();
        if (CimBooleanUtils.isTrue(isEquipment)) {
            log.debug("isEquipment == TRUE");
            log.debug("step9 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineID);
            if (null != equipmentReticlePodPortInfoGetDROut) {
                tmpReticlePodPortInfoList = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            }
        } else {
            log.debug("isEquipment == FALSE");
            log.debug("step10 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineID);
            tmpReticlePodPortInfoList = reticlePodPortInfoList;
        }

        //get target RSPPort's information
        Boolean foundRSPPort = false;
        if (CimArrayUtils.isNotEmpty(tmpReticlePodPortInfoList)) {
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : tmpReticlePodPortInfoList) {
                if (ObjectIdentifier.equalsWithValue(portID, reticlePodPortInfo.getReticlePodPortID())) {
                    log.debug("Target RSPPort found on machine {} {}.", ObjectIdentifier.fetchValue(machineID), ObjectIdentifier.fetchValue(portID));
                    foundRSPPort = true;

                    //check target RSPPort is vacant or not.
                    if (ObjectIdentifier.isNotEmptyWithValue(reticlePodPortInfo.getLoadedReticlePodID())) {
                        log.debug("RSP {} is already loaded on target port {}.",
                                ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()),
                                ObjectIdentifier.fetchValue(machineID));
                        Validations.check(true, retCodeConfigEx.getDiffRtclpodLoaded(),
                                ObjectIdentifier.fetchValue(reticlePodPortInfo.getLoadedReticlePodID()),
                                ObjectIdentifier.fetchValue(machineID),
                                ObjectIdentifier.fetchValue(portID));
                    }

                    //check target RSPPort is reserved or not.
                    if (ObjectIdentifier.isNotEmptyWithValue(reticlePodPortInfo.getReservedReticlePodID()) &&
                            !ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReservedReticlePodID(), reticlePodID)) {
                        log.debug("Other RSP is reserved for this port.{} {} {} {}",
                                ObjectIdentifier.fetchValue(machineID),
                                ObjectIdentifier.fetchValue(portID),
                                ObjectIdentifier.fetchValue(reticlePodPortInfo.getReservedReticlePodID()),
                                ObjectIdentifier.fetchValue(reticlePodID));
                        Validations.check(true, retCodeConfigEx.getDiffRtclpodReserved(),
                                ObjectIdentifier.fetchValue(machineID),
                                ObjectIdentifier.fetchValue(portID),
                                ObjectIdentifier.fetchValue(reticlePodPortInfo.getReservedReticlePodID()));
                    }
                    break;
                }
            }
        }
        if (CimBooleanUtils.isFalse(foundRSPPort)) {
            log.debug("Target RSPPort is not found on machine {} {}", ObjectIdentifier.fetchValue(machineID), ObjectIdentifier.fetchValue(portID));
            Validations.check(true, retCodeConfig.getRspportNotFound(), ObjectIdentifier.fetchValue(machineID), ObjectIdentifier.fetchValue(portID));
        }

        //check RSP transfer status
        log.debug("step11 - reticleMethod.reticlePodTransferStateGetDR");
        String transferStatus = reticleMethod.reticlePodTransferStateGetDR(objCommon, reticlePodID);

        log.debug("RSP xfer status {}", transferStatus);
        if (!CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT) &&
                !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_BAYOUT) &&
                !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT) &&
                !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT) &&
                !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT) &&
                !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT) &&
                !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT) &&
                !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_UNKNOWN)) {
            log.debug("RSP xfer status {} is invalid for load. not *O", transferStatus);
            Validations.check(true, retCodeConfig.getInvalidReticlepodXferStat(),
                    ObjectIdentifier.fetchValue(reticlePodID),
                    transferStatus);
        }

        //do Load job
        if (CimBooleanUtils.isTrue(isEquipment)) {
            log.debug("isEquipment == TRUE");
            log.debug("step12 - equipmentMethod.equipmentReticlePodLoad");
            equipmentMethod.equipmentReticlePodLoad(objCommon,
                    machineID,
                    portID,
                    reticlePodID,
                    params.getOpeMemo());
        } else {
            log.debug("isEquipment == FALSE");
            log.debug("step13 - stockerMethod.stockerReticlePodLoad");
            stockerMethod.stockerReticlePodLoad(objCommon,
                    machineID,
                    portID,
                    reticlePodID,
                    params.getOpeMemo());
        }

        //make event
        Infos.ReticleEventRecord strReticleEventRecord = new Infos.ReticleEventRecord();
        strReticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        strReticleEventRecord.setReticlePodID(reticlePodID);
        strReticleEventRecord.setRSPPortEvent(BizConstant.SP_RSP_EVENT_LOAD);

        ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");
        if (CimBooleanUtils.isTrue(isEquipment)) {
            log.debug("isEquipment == TRUE");
            strReticleEventRecord.setBareReticleStockerID(dummyID);
            strReticleEventRecord.setResourceID(dummyID);
            strReticleEventRecord.setEquipmentID(machineID);
            strReticleEventRecord.setRSPPortID(portID);
        } else {
            log.debug("isEquipment == FALSE");
            strReticleEventRecord.setBareReticleStockerID(machineID);
            strReticleEventRecord.setResourceID(portID);
            strReticleEventRecord.setEquipmentID(dummyID);
            strReticleEventRecord.setRSPPortID(dummyID);
        }

        log.debug("step14 - reticleMethod.reticleEventQueuePutDR");
        reticleMethod.reticleEventQueuePutDR(objCommon, strReticleEventRecord);
    }


    @Override
    public void sxReticlePodOfflineUnloadingReq(Infos.ObjCommon objCommon, Params.ReticlePodOfflineUnloadingReqParams params) {
        ObjectIdentifier machineID = params.getMachineID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        Boolean bForceUnloadFlag = params.getBForceUnloadFlag();
        log.debug("input machineID    {}", ObjectIdentifier.fetchValue(machineID));
        log.debug("input portID       {}", ObjectIdentifier.fetchValue(portID));
        log.debug("input reticlePodID {}", ObjectIdentifier.fetchValue(reticlePodID));
        log.debug("input ForceFlag    {}", (CimBooleanUtils.isTrue(bForceUnloadFlag) ? "TRUE" : "FALSE"));

        /*****************************/
        /* Check ARHS (ARHS) switch  */
        /*****************************/
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /**********************************************************************/
        /*  object lock process. (ReticlePodPortResource/ReticlePod)          */
        /**********************************************************************/
        log.debug("step1 - objectLockMethod.objectLockForEquipmentResource");
        objectLockMethod.objectLockForEquipmentResource(objCommon,
                machineID,
                portID,
                BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE);

        log.debug("step2 - objectLockMethod.objectLock");
        objectLockMethod.objectLock(objCommon, CimReticlePod.class, reticlePodID);

        /**************************/
        /*   Check machine type   */
        /**************************/
        log.debug("step3 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);

        String machineType = null;
        Boolean isEquipment = false;
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("This machine {} is Equipment.", ObjectIdentifier.fetchValue(machineID));
            isEquipment = true;
            machineType = BizConstant.SP_MACHINE_TYPE_EQP;
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("This machine {} is Stocker.", ObjectIdentifier.fetchValue(machineID));
            isEquipment = false;
            machineType = machineTypeGetOut.getStockerType();
        } else {
            log.debug("This machine {} {} is invalid for request.", ObjectIdentifier.fetchValue(machineID), machineTypeGetOut.getStockerType());
            Validations.check(true, retCodeConfigEx.getStkTypeDifferent(), machineTypeGetOut.getStockerType());
        }

        /************************************************************/
        /* If bForceUnloadFlag is not set, check following items.   */
        /*   1. equipment online mode validity (must be offline)    */
        /*   2. Reticle pod reservation                             */
        /*   3. RDJ/RCJ.                                            */
        /************************************************************/
        if (CimBooleanUtils.isFalse(bForceUnloadFlag)) {
            /******************************************/
            /*  Check online mode (must be offline)   */
            /******************************************/
            String machineOnlineMode = null;
            if (CimBooleanUtils.isTrue(isEquipment)) {
                log.debug("step4 - equipmentMethod.equipmentOnlineModeGet");
                String equipmentOnlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, machineID);
                log.debug("Equipment's online mode {}", equipmentOnlineModeGet);
                machineOnlineMode = equipmentOnlineModeGet;
            } else {
                log.debug("step5 - stockerMethod.stockerOnlineModeGet");
                String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, machineID);
                machineOnlineMode = stockerOnlineModeGet;
            }

            if (!CimStringUtils.equals(machineOnlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                log.debug("OnlineMode is not offline.");
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        machineOnlineMode);
            }

            /********************************************/
            /*  Check reticlePod transfer reservation   */
            /********************************************/
            log.debug("step6 - reticleMethod.reticlePodTransferReservationCheck");
            reticleMethod.reticlePodTransferReservationCheck(objCommon, reticlePodID);

            /************************************************/
            /*  Check whether reticlePod includes reticles  */
            /*****************************|*******************/
            Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
            reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
            reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
            reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
            log.debug("step7 - reticleMethod.reticlePodFillInTxPDQ013DR");
            Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

            Boolean bReticleExistFlag = false;
            if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo() && CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
                log.debug("This reticlePod have reticle(s).");
                bReticleExistFlag = true;
            }

            if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
                /************************************************/
                /*  Check the included reticle has control job  */
                /************************************************/
                for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                    log.debug("step8 - reticleMethod.reticlecontrolJobInfoGet");
                    Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = reticleMethod.reticlecontrolJobInfoGet(objCommon, containedReticleInfo.getReticleID());

                    if (null != reticleControlJobInfoGetOut && CimArrayUtils.isNotEmpty(reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq())) {
                        //--------------------------------------------------
                        //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                        //--------------------------------------------------
                        Long retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getLongValue();
                        log.debug("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS ", retrieveReticleDuringLotProcFlag);
                        if (1L == retrieveReticleDuringLotProcFlag) {
                            log.debug("Check if reticle has reserve control job");
                            for (Outputs.ControlJobAttributeInfo controlJobAttributeInfo : reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq()) {
                                if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobAttributeInfo.getControlJobStatus())) {
                                    log.debug("controlJob status is Created");
                                    Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(), ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                                }
                            }
                        } else {
                            log.debug("This reticle have controlJob {} .", ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                            Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(), ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                        }
                    }
                }
            }

            /**************************************/
            /*  Check reticlePod has RDJ or not.  */
            /**************************************/
            List<Infos.ReticleDispatchJob> reticleDispatchJobList = null;
            log.debug("step9 - reticleMethod.reticleDispatchJobCheckExistenceDR");
            try {
                reticleDispatchJobList = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, null, reticlePodID, null);
            } catch (ServiceException e) {
                Boolean allowUnloadFlag = true;
                reticleDispatchJobList = e.getData(List.class);
                if (Validations.isEquals(retCodeConfig.getFoundInRdj(), e.getCode()) &&
                        CimArrayUtils.getSize(reticleDispatchJobList) == 1) {
                    log.debug("reticleDispatchJobCheckExistenceDR() == FOUND_IN_RDJ.");
                    Infos.ReticleDispatchJob strReticleDispatchJob = reticleDispatchJobList.get(0);
                    if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
                        log.debug("bReticleExistFlag == TRUE");
                        if (ObjectIdentifier.equalsWithValue(strReticleDispatchJob.getToEquipmentID(), machineID)) {
                            log.debug("toEquipmentID == machineID. {} Need to Store", ObjectIdentifier.fetchValue(machineID));
                            allowUnloadFlag = false;
                        }
                    } else {
                        log.debug("bReticleExistFlag == FALSE");
                        if (ObjectIdentifier.isNotEmptyWithValue(strReticleDispatchJob.getReticleID()) &&
                                ObjectIdentifier.equalsWithValue(strReticleDispatchJob.getFromEquipmentID(), machineID)) {
                            log.debug("fromEquipmentID == machineID {}. Need to Retrieve", ObjectIdentifier.fetchValue(machineID));
                            allowUnloadFlag = false;
                        }
                    }
                } else {
                    log.debug("reticleDispatchJobCheckExistenceDR() != FOUND_IN_RDJ.");
                    allowUnloadFlag = false;
                }
                log.debug("allowUnloadFlag = {}", allowUnloadFlag);
                if (CimBooleanUtils.isFalse(allowUnloadFlag)) {
                    log.debug("allowUnloadFlag == FALSE");
                    throw e;
                }
            }
        }

        /******************************/
        /*  Get RSP port information  */
        /******************************/
        List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfoList = new ArrayList<>();
        if (CimBooleanUtils.isTrue(isEquipment)) {
            log.debug("step10 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineID);
            tmpReticlePodPortInfoList = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
        } else {
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineID);
            tmpReticlePodPortInfoList = reticlePodPortInfoList;
        }

        /****************************************/
        /*  Check target RSPPort's information  */
        /****************************************/
        Boolean foundRSPPort = false;
        int rspPortCount = CimArrayUtils.getSize(tmpReticlePodPortInfoList);
        log.debug("Machine's RSPPort count {}", rspPortCount);
        Infos.ReticlePodPortInfo strTargetRSPPortInfo = new Infos.ReticlePodPortInfo();
        if (CimArrayUtils.isNotEmpty(tmpReticlePodPortInfoList)) {
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : tmpReticlePodPortInfoList) {
                if (ObjectIdentifier.equalsWithValue(portID, reticlePodPortInfo.getReticlePodPortID())) {
                    log.debug("Target RSPPort found on machine {} {}.", ObjectIdentifier.fetchValue(machineID), ObjectIdentifier.fetchValue(portID));
                    foundRSPPort = true;
                    strTargetRSPPortInfo = reticlePodPortInfo;
                    break;
                }
            }
        }

        if (CimBooleanUtils.isFalse(foundRSPPort)) {
            log.debug("Target RSPPort is not found on machine {} {}", ObjectIdentifier.fetchValue(machineID), ObjectIdentifier.fetchValue(portID));
            Validations.check(true, retCodeConfig.getRspportNotFound(),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(portID));
        }

        /******************************************************************/
        /*  Check whether target RSP is loaded on target RSPPort or not   */
        /******************************************************************/
        if (ObjectIdentifier.isEmptyWithValue(strTargetRSPPortInfo.getLoadedReticlePodID()) ||
                !ObjectIdentifier.equalsWithValue(strTargetRSPPortInfo.getLoadedReticlePodID(), reticlePodID)) {
            log.debug("Unload target RSP {} is not found on target port {} {}.",
                    ObjectIdentifier.fetchValue(strTargetRSPPortInfo.getLoadedReticlePodID()),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(portID));
            Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                    ObjectIdentifier.fetchValue(reticlePodID),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(portID));
        }

        /********************************/
        /*  check RSP transfer status   */
        /********************************/
        log.debug("step11 - reticleMethod.reticlePodTransferStateGetDR");
        String transferStatus = reticleMethod.reticlePodTransferStateGetDR(objCommon, reticlePodID);

        log.debug("RSP xfer status {}", transferStatus);
        if (!CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)
                && !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
            log.debug("RSP xfer status {} is invalid for unload.", transferStatus);
            Validations.check(true, retCodeConfig.getInvalidReticlepodXferStat(),
                    ObjectIdentifier.fetchValue(reticlePodID),
                    transferStatus);
        }

        /************************************/
        /*                                  */
        /*          Main Routine            */
        /*                                  */
        /************************************/

        /********************/
        /*  Do unload job   */
        /********************/
        if (CimBooleanUtils.isTrue(isEquipment)) {
            log.debug("step12 - equipmentMethod.equipmentReticlePodUnload");
            equipmentMethod.equipmentReticlePodUnload(objCommon,
                    machineID,
                    portID,
                    reticlePodID,
                    params.getOpeMemo());
        } else {
            log.debug("step13 - stockerMethod.stockerReticlePodUnload");
            stockerMethod.stockerReticlePodUnload(objCommon,
                    machineID,
                    portID,
                    reticlePodID,
                    params.getOpeMemo(),
                    null);
        }

        /************************/
        /*  Make Reticle Event  */
        /************************/
        Infos.ReticleEventRecord strReticleEventRecord = new Infos.ReticleEventRecord();
        strReticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        strReticleEventRecord.setReticlePodID(reticlePodID);
        strReticleEventRecord.setRSPPortEvent(BizConstant.SP_RSP_EVENT_UNLOAD);

        if (CimBooleanUtils.isTrue(isEquipment)) {
            strReticleEventRecord.setEquipmentID(machineID);
            strReticleEventRecord.setRSPPortID(portID);
        } else {
            strReticleEventRecord.setBareReticleStockerID(machineID);
            strReticleEventRecord.setResourceID(portID);
        }

        log.debug("step14 - reticleMethod.reticleEventQueuePutDR");
        reticleMethod.reticleEventQueuePutDR(objCommon, strReticleEventRecord);
    }

    @Override
    public Results.ReticleInventoryReqResult sxReticleInventoryReq(Infos.ObjCommon objCommon, Params.ReticleInventoryReqParams params) {
        Results.ReticleInventoryReqResult result = new Results.ReticleInventoryReqResult();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier stockerID = params.getStockerID();
        log.info("equipmentID : {}", ObjectIdentifier.fetchValue(equipmentID));
        log.info("stockerID : {}", ObjectIdentifier.fetchValue(stockerID));

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.info("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable(), objCommon.getTransactionID());
        // Check InParameter
        Validations.check(!ObjectIdentifier.isEmpty(stockerID) && !ObjectIdentifier.isEmpty(equipmentID), retCodeConfig.getInvalidInputParam());

        /*---------------------------------*/
        /*   Request for Reticle Stocker   */
        /*---------------------------------*/
        if (!ObjectIdentifier.isEmpty(stockerID)) {
            /*------------------------*/
            /*   Check Stocker Type   */
            /*------------------------*/
            Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = stockerMethod.stockerTypeGet(objCommon, stockerID);
            Validations.check(!CimStringUtils.equals(objStockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE), retCodeConfigEx.getNotReticleStocker(), stockerID);

            String onlineMode = stockerMethod.stockerOnlineModeGet(objCommon, stockerID);
            log.info("BRStk's online mode {}", onlineMode);
            Validations.check(CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getEqpOnlineMode(), ObjectIdentifier.fetchValue(stockerID), onlineMode);

            log.info("EAP notify ");
            Inputs.SendReticleInventoryReqIn sendReticleInventoryReqIn = new Inputs.SendReticleInventoryReqIn();
            sendReticleInventoryReqIn.setRequestUserID(objCommon.getUser());
            sendReticleInventoryReqIn.setEquipmentID(equipmentID);
            sendReticleInventoryReqIn.setStockerID(stockerID);
            sendReticleInventoryReqIn.setClaimMemo(params.getOpeMemo());
//            tcsMethod.sendTCSReq(TCSReqEnum.sendReticleInventoryReq, sendReticleInventoryReqIn);

            result.setStockerID(stockerID);
        }

        /*---------------------------*/
        /*   Request for Equipment   */
        /*---------------------------*/
        else if (!ObjectIdentifier.isEmpty(equipmentID)) {
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            Validations.check(CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getEqpOnlineMode(), equipmentID, onlineMode);

            /*-----------------------------------------------*/
            /*   Check Equipment Type (reticleRequiredFlag)  */
            /*-----------------------------------------------*/
            boolean reticleRequiredFlag = equipmentMethod.equipmentReticleRequiredFlagGet(objCommon, equipmentID);
            Validations.check(!reticleRequiredFlag, retCodeConfig.getEqpNotRequiredReticle(), equipmentID);

            /*-----------------------------------*/
            /*   Send Inventory Request to EAP   */
            /*-----------------------------------*/
            Inputs.SendReticleInventoryReqIn sendReticleInventoryReqIn = new Inputs.SendReticleInventoryReqIn();
            sendReticleInventoryReqIn.setRequestUserID(objCommon.getUser());
            sendReticleInventoryReqIn.setEquipmentID(equipmentID);
            sendReticleInventoryReqIn.setStockerID(stockerID);
            sendReticleInventoryReqIn.setClaimMemo(params.getOpeMemo());
//            tcsMethod.sendTCSReq(TCSReqEnum.sendReticleInventoryReq, sendReticleInventoryReqIn);

            result.setEquipmentID(equipmentID);
        }

        /*---------------------------*/
        /*   Input Parameter Error   */
        /*---------------------------*/
        else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        return result;
    }

    @Override
    public void sxLotCassettePostProcessForceDeleteReq(Infos.ObjCommon objCommon, Params.StrLotCassettePostProcessForceDeleteReqInParams strLotCassettePostProcessForceDeleteReqInParams) {
        log.info("PPTManager_i::txLotCassettePostProcessForceDeleteReq");
        log.info("in-para strLotCassettePostProcessForceDeleteReqInParm");
        log.info("cassetteID : {}", ObjectIdentifier.fetchValue(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID()));
        log.info("lotID : {}", ObjectIdentifier.fetchValue(strLotCassettePostProcessForceDeleteReqInParams.getLotID()));
        log.info("in-para claimMemo : {}", strLotCassettePostProcessForceDeleteReqInParams.getClaimMemo());

        //----------------------------------------
        //
        //  Delete Post Process Queue by force
        //
        //----------------------------------------
        durableMethod.postProcessQueueForceDeleteDR(objCommon, strLotCassettePostProcessForceDeleteReqInParams);


        // Get LotID Sequence
        List<ObjectIdentifier> lotIDs = Lists.newArrayList();
        long nLotLen = 0;
        String durableCategory = ""; //DSN000096126
        if (!ObjectIdentifier.isEmpty(strLotCassettePostProcessForceDeleteReqInParams.getLotID())) {
            log.info("Lot is specified.");
            lotIDs.add(strLotCassettePostProcessForceDeleteReqInParams.getLotID());
        } else if (!ObjectIdentifier.isEmpty(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID())) {
            log.info("Cassette is specified.");
            //DSN000096126 Add Start
            durableCategory = durableMethod.durableDurableCategoryGet(objCommon, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());
            if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSCASSETTE, durableCategory)) {
                log.info("durableCategory == Cassette");
                //DSN000096126 Add End
                //-----------------------------
                //  Get LotList in Cassette
                //-----------------------------
                Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteGetLotList(objCommon, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());
                nLotLen = lotListInCassetteInfo.getLotIDList().size();
                lotIDs = lotListInCassetteInfo.getLotIDList();
            }
        }

        //--------------------------------------
        //
        //  Release the Lot's Hold("LOCK").
        //
        //--------------------------------------
        ObjectIdentifier spReasonLotlockrelease = ObjectIdentifier.build(SP_REASON_LOTLOCKRELEASE, null);
        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
        holdLotReleaseReqParams.setReleaseReasonCodeID(spReasonLotlockrelease);
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldType(SP_HOLDTYPE_LOTHOLD);
        lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.build(SP_REASON_LOTLOCK, null));
        lotHoldReq.setHoldUserID(ObjectIdentifier.build(SP_POSTPROC_PERSON, null));
        lotHoldReq.setResponsibleOperationMark(SP_RESPONSIBLEOPERATION_CURRENT);
        lotHoldReq.setRelatedLotID(ObjectIdentifier.emptyIdentifier());
        lotHoldReq.setClaimMemo("");

        List<Infos.LotHoldReq> lotHoldReqList = Lists.newArrayList();
        lotHoldReqList.add(lotHoldReq);
        for (ObjectIdentifier lotID : lotIDs) {
            //---------------------------
            // Get the lot's hold list.
            //---------------------------
            log.info("Get the lot's hold list. {}", lotID);
            List<Infos.LotHoldListAttributes> listRetCode = lotMethod.lotFillInTxTRQ005DR(objCommon, lotID);

            log.info("strLot_FillInTxTRQ005DR_out.strLotHoldListAttributes.length() {}", listRetCode.size());
            boolean bFindLockHoldFlag = listRetCode.parallelStream()
                    .anyMatch(lotHoldListAttributes -> CimStringUtils.equals(ObjectIdentifier.fetchValue(lotHoldListAttributes.getReasonCodeID()), SP_REASON_LOTLOCK));

            if (TRUE == bFindLockHoldFlag) {
                log.info("# Release Lot's Hold(LOCK)");
                //------------------------
                //  Release LOCK Hold.
                //------------------------
                holdLotReleaseReqParams.setLotID(lotID);
                holdLotReleaseReqParams.setHoldReqList(lotHoldReqList);
                lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
            }
        }// for( CORBA::Long i = 0; i < nLotLen; i++ )

        //DSN000096126 Add Start
        if (CimStringUtils.isNotEmpty(durableCategory)) {
            log.info("durableCategory is not blank");
            //---------------------------------------
            //  Check Durable OnRoute
            //---------------------------------------
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                    log.info("durable_OnRoute_Check() == RC_DURABLE_ONROUTE {}", e.getCode());
                    //---------------------------
                    // Get the durable hold list.
                    //---------------------------
                    Params.StrDurableFillInTxPDQ025InParams strDurableFillInTxPDQ025InParams = new Params.StrDurableFillInTxPDQ025InParams();
                    Results.DurableHoldListAttributesResult result  = durableMethod.durableFillInTxPDQ025(objCommon, strDurableFillInTxPDQ025InParams);
                    List<Results.DurableHoldAttributeResult> durableHoldListAttributesResults = Optional.ofNullable(result.getDurableHoldListAttributesResults()).orElse(Lists.newArrayList());
                    if (CollectionUtils.isEmpty(durableHoldListAttributesResults)) {
                        log.info("durable_FillInTxPDQ025() != RC_OK");
                    }
                    //---------------------------
                    // Check LOCK Hold.
                    //---------------------------
                    boolean bFindLockHoldFlag = durableHoldListAttributesResults.parallelStream()
                            .anyMatch(durableHoldAttributeResult -> CimStringUtils.equals(SP_REASON_DURABLELOCK, ObjectIdentifier.fetchValue(durableHoldAttributeResult.getReasonCodeID())));

                    //------------------------
                    //  Release LOCK Hold.
                    //------------------------
                    if (bFindLockHoldFlag) {
                        List<Infos.DurableHoldList> strDurableHoldList = new ArrayList<>();
                        strDurableHoldList.add(new Infos.DurableHoldList());
                        strDurableHoldList.get(0).setHoldType(BizConstant.SP_HOLDTYPE_DURABLEHOLD);
                        strDurableHoldList.get(0).setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLELOCK));
                        strDurableHoldList.get(0).setHoldUserID(ObjectIdentifier.buildWithValue(BizConstant.SP_POSTPROC_PERSON));
                        strDurableHoldList.get(0).setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                        strDurableHoldList.get(0).setRouteID(ObjectIdentifier.emptyIdentifier());
                        strDurableHoldList.get(0).setOperationNumber("");
                        strDurableHoldList.get(0).setRelatedDurableID(ObjectIdentifier.emptyIdentifier());
                        strDurableHoldList.get(0).setRelatedDurableCategory("");
                        strDurableHoldList.get(0).setClaimMemo("");

                        Infos.HoldDurableReleaseReqInParam strHoldDurableReleaseReqInParam = new Infos.HoldDurableReleaseReqInParam();
                        strHoldDurableReleaseReqInParam.setDurableCategory(durableCategory);
                        strHoldDurableReleaseReqInParam.setDurableID(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());
                        strHoldDurableReleaseReqInParam.setReleaseReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLELOCKRELEASE));
                        strHoldDurableReleaseReqInParam.setStrDurableHoldList(strDurableHoldList);

                        //-----------------------------------------------------------
                        //   Call txHoldDurableReleaseReq()
                        //-----------------------------------------------------------
                        log.info("{}", "Call txHoldDurableReleaseReq()...");
                        sxHoldDrbReleaseReq(
                                objCommon,
                                strHoldDurableReleaseReqInParam,
                                strLotCassettePostProcessForceDeleteReqInParams.getClaimMemo());
                    }
                }
            }
        }
    }

}
