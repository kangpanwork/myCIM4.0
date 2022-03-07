package com.fa.cim.service.sort.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
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
import com.fa.cim.method.impl.CassetteMethod;
import com.fa.cim.method.impl.EventMakeMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.bank.IBankInqService;
import com.fa.cim.service.bank.IBankService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.equipment.IEquipmentInqService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.lotstart.ILotStartService;
import com.fa.cim.service.sort.ISortInqService;
import com.fa.cim.service.sort.ISortService;
import com.fa.cim.service.system.ISystemInqService;
import com.fa.cim.service.system.ISystemService;
import com.fa.cim.service.tms.ITransferManagementSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static com.fa.cim.common.constant.BizConstant.CIMFW_DURABLE_SUB_STATE_DEFAULT;
import static com.fa.cim.common.constant.BizConstant.SP_DURABLECAT_CASSETTE;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:58
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class SortServiceImpl implements ISortService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IBankInqService bankInqService;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ISorterMethod sorterMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private IEAPMethod eapMethod;

    @Autowired
    private ILotStartService lotStartService;

    @Autowired
    private IBankService bankService;

    @Autowired
    private ILotService lotService;

    @Autowired
    private ISystemInqService systemInqService;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;


    @Autowired
    private IDurableService durableService;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private ITransferManagementSystemService transferManagementSystemService;

    @Autowired
    private EventMakeMethod eventMakeMethod;

    @Autowired
    private IBankInqService iBankInqService;


    @Autowired
    private IEquipmentInqService equipmentInqService;

    @Autowired
    private ISortInqService sortInqService;


    @Autowired
    private ISystemService systemService;

    @Autowired
    private IContaminationMethod contaminationMethod;


    public void sxOnlineSorterActionCancelReq(Infos.ObjCommon objCommon, Params.OnlineSorterActionCancelReqParm params){

        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());

        String sorterJobLockFlagStr = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getValue();
        int sorterJobLockFlag = null == sorterJobLockFlagStr ? 0 : Integer.valueOf(sorterJobLockFlagStr);
        log.info("sorterJobLockFlag : {}", sorterJobLockFlag);
        Long lockMode = Long.valueOf("0");

        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();
        if ( 1 == sorterJobLockFlag ) {
            log.info("sorterJobLockFlag = 1");
            // Get required equipment lock mode
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

            lockMode = objLockModeOut.getLockMode();
        }
        log.info("lockMode : {}", lockMode );


        // ================================================================
        // Checking Previous Job is running or not
        // ================================================================
        Infos.WaferSorterSlotMap strWaferSorterSlotMap = new Infos.WaferSorterSlotMap();

        //------------------------------------------------------
        //   Setting Search Condition Value
        //   Condition
        //   sorterStatus = "Resuested"
        //   direction    = "OMS"
        //   This condition means Sort request was submitted,
        //   but TCS Responce is not received
        //------------------------------------------------------
        strWaferSorterSlotMap.setEquipmentID(params.getEquipmentID());
        strWaferSorterSlotMap.setPortGroup(params.getPortGroup());
        strWaferSorterSlotMap.setRequestTime(params.getRequestTimeStamp());
        strWaferSorterSlotMap.setSorterStatus(BizConstant.SP_SORTER_REQUESTED);
        strWaferSorterSlotMap.setDirection(BizConstant.SP_SORTER_DIRECTION_MM);

        //------------------------------------------------------
        //   Setting Not Search Condition Value
        //------------------------------------------------------
        strWaferSorterSlotMap.setDestinationSlotNumber(0L);
        strWaferSorterSlotMap.setOriginalSlotNumber(0L);
        strWaferSorterSlotMap.setDestinationCassetteManagedByOM(false);
        strWaferSorterSlotMap.setOriginalCassetteManagedByOM(false);

        //------------------------------------------------------
        //   Search From Flotmap DB
        //------------------------------------------------------
        List<Infos.WaferSorterSlotMap> strWaferSorter_slotMap_SelectDR_out = waferMethod.waferSorterSlotMapSelectDR(objCommon,
                BizConstant.SP_SORTER_SLOTMAP_ALLDATA,
                "",
                BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                strWaferSorterSlotMap);

        //------------------------------------------------------------
        // Check output parameter of sorter_waferTransferInfo_Restructure()
        //------------------------------------------------------------
        log.info("txOnlineSorterActionCancelReq , Check output parameter of waferSorter_GetSlotMapDR()");

        int nOutputLotLen = CimArrayUtils.getSize(strWaferSorter_slotMap_SelectDR_out);
        //------------------------------------------------------------
        // If Current running Job is not existed
        // then return
        //------------------------------------------------------------
        if ( nOutputLotLen == 0 ) {
            throw new ServiceException(retCodeConfigEx.getNotFoundScrap());
        }

        List<ObjectIdentifier> tmpCassetteIDs = new ArrayList<>();
        List<Boolean> tmpCassetteManagedBySiViewSeq = new ArrayList<>();
        int nCastLen = 0;
        int nSlotMapLen = CimArrayUtils.getSize(strWaferSorter_slotMap_SelectDR_out);
        Boolean autoSorterJobFlag = false;

        for( int i = 0; i < nSlotMapLen; i++ ) {
            Boolean bExist = false;

            //-----------------------------------
            //  Get all destinationCassetteID
            //-----------------------------------
            if ( 0 == i &&  (CimStringUtils.equals( strWaferSorter_slotMap_SelectDR_out.get(i).getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)||
                    CimStringUtils.equals( strWaferSorter_slotMap_SelectDR_out.get(i).getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)||
                    CimStringUtils.equals( strWaferSorter_slotMap_SelectDR_out.get(i).getActionCode(), BizConstant.SP_SORTER_WAFEREND)) ) {
                log.info("i = 0 and actionCode = SP_Sorter_AutoSorting");
                autoSorterJobFlag = true;
            }
            if(!ObjectIdentifier.isEmpty( strWaferSorter_slotMap_SelectDR_out.get(i).getDestinationCassetteID() ) ) {
                bExist = false;
                nCastLen = CimArrayUtils.getSize(tmpCassetteIDs);

                for( int j = 0; j < nCastLen; j++ ) {
                    if(ObjectIdentifier.equalsWithValue( tmpCassetteIDs.get(j), strWaferSorter_slotMap_SelectDR_out.get(i).getDestinationCassetteID() ) ) {
                        log.info( "destinationCassetteID bExist == true");
                        bExist = true;
                        break;
                    }
                }

                if(CimBooleanUtils.isFalse(bExist) ) {
                    log.info( "destinationCassetteID bExist == false");
                    tmpCassetteIDs.add(strWaferSorter_slotMap_SelectDR_out.get(i).getDestinationCassetteID());
                    tmpCassetteManagedBySiViewSeq.add(strWaferSorter_slotMap_SelectDR_out.get(i).getDestinationCassetteManagedByOM());
                }
            }

            //-----------------------------------
            //  Get all originalCassetteID
            //-----------------------------------
            if(!ObjectIdentifier.isEmpty( strWaferSorter_slotMap_SelectDR_out.get(i).getOriginalCassetteID()) ) {
                bExist = false;
                nCastLen = CimArrayUtils.getSize(tmpCassetteIDs);

                for( int j = 0; j < nCastLen; j++ ) {
                    if( ObjectIdentifier.equalsWithValue(tmpCassetteIDs.get(j), strWaferSorter_slotMap_SelectDR_out.get(i).getOriginalCassetteID()) ) {
                        log.info( "originalCassetteID bExist == true");
                        bExist = true;
                        break;
                    }
                }

                if( CimBooleanUtils.isFalse(bExist) ) {
                    tmpCassetteIDs.add(strWaferSorter_slotMap_SelectDR_out.get(i).getOriginalCassetteID());
                    tmpCassetteManagedBySiViewSeq.add(strWaferSorter_slotMap_SelectDR_out.get(i).getOriginalCassetteManagedByOM());
                }
            }
        }

        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        log.info("Check InPostProcessFlag.");
        nCastLen = CimArrayUtils.getSize(tmpCassetteIDs);

        List<ObjectIdentifier> loadCastSeq = new ArrayList<>();
        for(int i = 0; i < nCastLen; i++ ) {
            if(CimBooleanUtils.isTrue(tmpCassetteManagedBySiViewSeq.get(i)) ) {
                loadCastSeq.add(tmpCassetteIDs.get(i)); //DSN000049350

                //---------------------------------------
                //  Get InPostProcessFlag of Cassette
                //---------------------------------------
                Boolean strCassette_inPostProcessFlag_Get_out = cassetteMethod.cassetteInPostProcessFlagGet(objCommon, tmpCassetteIDs.get(i));


                //---------------------------------------------------
                //  If Cassette is in post process, returns error
                //---------------------------------------------------
                if(CimBooleanUtils.isTrue(strCassette_inPostProcessFlag_Get_out)) {
                    log.info("Cassette is in post process.");
                    throw new ServiceException(retCodeConfig.getCassetteInPostProcess());
                }


                //-------------------------------------------------------
                // Check cassette InterFabXfer State
                //-------------------------------------------------------
                String strCassette_interFabXferState_Get_out = cassetteMethod.cassetteInterFabXferStateGet(objCommon, tmpCassetteIDs.get(i));
                if(CimStringUtils.equals( strCassette_interFabXferState_Get_out, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING) ) {
                    log.info( "return RC_INTERFAB_INVALID_CAST_XFERSTATE_FOR_REQ");
                    throw new ServiceException(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());
                }
            }
        }


        if ( 1 == sorterJobLockFlag && 0 < CimArrayUtils.getSize(loadCastSeq) && autoSorterJobFlag ) {
            log.info("sorterJobLockFlag = 1");

            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------
            ObjectIdentifier loadCassetteID = loadCastSeq.get(0);
            Inputs.SorterSorterJobLockDRIn sorterSorterJobLockDRIn = new Inputs.SorterSorterJobLockDRIn();
            sorterSorterJobLockDRIn.setCassetteID(loadCassetteID);
            sorterSorterJobLockDRIn.setLockType(Integer.valueOf("0"));
            log.info("calling sorter_sorterJob_LockDR()");
            sorterMethod.sorterSorterJobLockDR(objCommon, sorterSorterJobLockDRIn);
        }

        if (lockMode.intValue() != 0) {
            log.info( "lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object

            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            if (CimArrayUtils.getSize(loadCastSeq) > 0) {
                log.info( "loadCastSeq.lengthloadCastSeq.length : {} > 0", CimArrayUtils.getSize(loadCastSeq));
                // Lock Equipment LoadCassette Element (Read)

                Inputs.ObjAdvanceLockIn objAdvancedObjectLock = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLock.setObjectID(params.getEquipmentID());
                objAdvancedObjectLock.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLock.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvancedObjectLock.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ));
                objAdvancedObjectLock.setKeyList(CimArrayUtils.objConvertStringList(loadCastSeq));
                objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLock);

            }
        }

        //-------------------------------------------------------
        // Check InterFabXfer State of Lot
        //-------------------------------------------------------

        // Make LotID unique sequence
        log.info( "Make LotID unique sequence");
        List<ObjectIdentifier> tmpLotIDs = new ArrayList<>();
        int nLotCnt = 0;

        for (int i=0; i < nSlotMapLen; i++ ) {
            Infos.WaferSorterSlotMap slotMap = strWaferSorter_slotMap_SelectDR_out.get(i);

            if (ObjectIdentifier.isEmpty(slotMap.getLotID())) {
                continue;
            }

            Boolean bAlreadySet = false;
            for ( int nLot=0; nLot < nLotCnt; nLot++ ) {
                if ( ObjectIdentifier.equalsWithValue(slotMap.getLotID(), tmpLotIDs.get(nLot)) ) {
                    bAlreadySet = true;
                    break; //[nLot]
                }
            }
            if ( CimBooleanUtils.isTrue(bAlreadySet) ) {
                continue;
            }

            log.info( "found LotID : {}", slotMap.getLotID());
            tmpLotIDs.add(slotMap.getLotID());
            nLotCnt++;
        } //[i]

        log.info( "Check Lot's InterFabXferState");
        for ( int nLot=0; nLot < nLotCnt; nLot++ ) {
            log.info("call lot_interFabXferState_Get() : {}", tmpLotIDs.get(nLot));

            String strLot_interFabXferState_Get_out = lotMethod.lotInterFabXferStateGet(objCommon, tmpLotIDs.get(nLot));

            if ( CimStringUtils.equals(strLot_interFabXferState_Get_out, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)
                    ||  CimStringUtils.equals(strLot_interFabXferState_Get_out, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING) ) {
                log.info("##### return RC_INTERFAB_INVALID_LOT_XFERSTATE_FOR_REQ");
                throw new ServiceException(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());
            }
        }

        // ================================================================
        // Sumbit request to TCS
        // ================================================================
        Inputs.SendWaferSortOnEqpCancelReqIn sendWaferSortOnEqpCancelReqIn = new Inputs.SendWaferSortOnEqpCancelReqIn();
        sendWaferSortOnEqpCancelReqIn.setObjCommonIn(objCommon);
        sendWaferSortOnEqpCancelReqIn.setRequestUserID(objCommon.getUser());
        sendWaferSortOnEqpCancelReqIn.setEquipmentID(params.getEquipmentID());
        sendWaferSortOnEqpCancelReqIn.setPortGroup(params.getPortGroup());
        sendWaferSortOnEqpCancelReqIn.setRequestTimeStamp(params.getRequestTimeStamp());
//        tcsMethod.sendTCSReq(TCSReqEnum.sendWaferSortOnEqpCancelReq,sendWaferSortOnEqpCancelReqIn);
        String tmpSleepTimeValue  = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue;
        long retryCountValue;

        if (CimStringUtils.length(tmpSleepTimeValue) == 0) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
        }

        if (CimStringUtils.length(tmpRetryCountValue) == 0) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
        }

        log.debug("{}{}","env value of OM_EAP_CONNECT_SLEEP_TIME  = ",sleepTimeValue);
        log.debug("{}{}","env value of OM_EAP_CONNECT_RETRY_COUNT = ",retryCountValue);
        IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), params.getEquipmentID(), null, true);
        if (eapRemoteManager!=null){
            for(int i = 0 ; i < (retryCountValue + 1) ; i++) {
                try {
                    // 暂时不需要
//                    eapRemoteManager.sendWaferSortOnEqpCancelReq(sendWaferSortOnEqpCancelReqIn);
                } catch (ServiceException ex){
                    if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                        if (i<retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                    }
                    throw ex;
                } catch (Exception ex){
                    if (i<retryCountValue){
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            ex.addSuppressed(e);
                            throw ex;
                        }
                    }
                    throw ex;
                }
                break;
            }
        }
        //========================================================
        // Delete Slotmap information of requested data
        //========================================================
        // same Eqp/PortGroup/requestTimeStamp/
        //      SP_Sorter_Requested/SP_Sorter_Direction_MM
        //--------------------------------------------------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();

        //--------------------------------------------------------------------------
        // Does not need cassetteIDs.
        // Because record in DB will be deleted by EQP, Port Group and Request Time.
        //--------------------------------------------------------------------------

        log.info("Try to waferSorter_slotMap_DeleteDR ");
        waferMethod.waferSorterSlotMapDeleteDR(objCommon, params.getPortGroup(), params.getEquipmentID(),
                cassetteIDs, params.getRequestTimeStamp(), BizConstant.SP_SORTER_REQUESTED, BizConstant.SP_SORTER_DIRECTION_MM);
    }

    public void sxOnlineSorterActionExecuteReq(Infos.ObjCommon objCommon, Params.OnlineSorterActionExecuteReqParams params) {
        // Wafer start先保存UI参数,如果失败,就不执行后续代码
        if (CimStringUtils.equals(BizConstant.SP_SORTER_START,params.getActionCode())){
            waferMethod.waferSorterSlotMapSTInfoInsertDR(objCommon,params);
        }
        // Initialize
        //--------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        Infos.WaferSorterSlotMap strWaferSorterSlotMap = new Infos.WaferSorterSlotMap();


        //-----------------------------------------
        // collect SiView managed casettes only.
        //-----------------------------------------
        int  waferMapLen = CimArrayUtils.getSize(params.getWaferSorterSlotMapList());
        int  casLen = CimArrayUtils.getSize(cassetteIDs);

        for(int i=0; i<waferMapLen; i++ ) {
            // collect SiView managed casettes only.
            if(CimBooleanUtils.isTrue(params.getWaferSorterSlotMapList().get(i).getOriginalCassetteManagedByOM())) {
                Boolean dupCastFlag = false ;
                for(int j= 0 ; j < casLen ; j++ ) {
                    if(ObjectIdentifier.equalsWithValue(cassetteIDs.get(j), params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID())) {
                        dupCastFlag = true;
                        break;
                    }
                }
                if(CimBooleanUtils.isTrue(dupCastFlag)) {
                    cassetteIDs.add(params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID());
                    log.info("SiView managed original cassette found. : {}", params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID());
                    casLen++;
                }
            }
            if( CimBooleanUtils.isTrue(params.getWaferSorterSlotMapList().get(i).getDestinationCassetteManagedByOM())) {
                Boolean dupCastFlag = false ;
                for(int j= 0 ; j < casLen ; j++ ) {
                    if(ObjectIdentifier.equalsWithValue(cassetteIDs.get(j), params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID())) {
                        dupCastFlag = true;
                        break;
                    }
                }
                if( CimBooleanUtils.isFalse(dupCastFlag)) {
                    cassetteIDs.add(params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                    log.info("SiView managed destination cassette found.: {}", params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                    casLen++;
                }
            }
        }

        String sorterJobLockFlagStr = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getValue();
        int sorterJobLockFlag = null == sorterJobLockFlagStr ? 0 : Integer.valueOf(sorterJobLockFlagStr);
        log.info("sorterJobLockFlag : {}", sorterJobLockFlag);
        int lockMode = 0;

        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();

        if ( 1 == sorterJobLockFlag ) {
            log.info("sorterJobLockFlag = 1");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);

            log.info("calling object_lockMode_Get() : {}", params.getEquipmentID());
            objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode().intValue();
            // Get required equipment lock mode

        }
        log.info("lockMode : {}", lockMode );
        int len = CimArrayUtils.getSize(params.getWaferSorterSlotMapList());
        if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFEREND)) {
            //--------------------------------------------------------------
            //  Check inpurt parameters length
            //--------------------------------------------------------------
            Validations.check(len == 0, retCodeConfig.getInvalidParameter());

            if ( 1 == sorterJobLockFlag ) {
                log.info("sorterJobLockFlag = 1");

                //---------------------------------
                // Lock Sort Jobs
                //---------------------------------
                ObjectIdentifier loadCassetteID = cassetteIDs.get(0);
                Inputs.SorterSorterJobLockDRIn sorterSorterJobLockDRIn = new Inputs.SorterSorterJobLockDRIn();
                sorterSorterJobLockDRIn.setCassetteID(loadCassetteID);
                sorterSorterJobLockDRIn.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                log.info("calling sorter_sorterJob_LockDR()");
                sorterMethod.sorterSorterJobLockDR(objCommon, sorterSorterJobLockDRIn);

            }
            if ( lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE ) {
                log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

                // Lock Equipment Main Object
                Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
                objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
                objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);


                int cassetteIDsLen = CimArrayUtils.getSize(cassetteIDs);
                if (0 < cassetteIDsLen) {
                    // Lock Equipment LoadCassette Element (Read)
                    List<String> loadCastSeq = new ArrayList<>();
                    for ( int loadCastNo = 0; loadCastNo < cassetteIDsLen; loadCastNo++ ) {
                        loadCastSeq.add(ObjectIdentifier.fetchValue(cassetteIDs.get(loadCastNo)));
                    }

                    Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                    objAdvanceLockIn.setObjectID(params.getEquipmentID());
                    objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                    objAdvanceLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ));
                    objAdvanceLockIn.setKeyList(loadCastSeq);
                    objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
                }
            } else {
                log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
                /*---------------------------------------------------*/
                /*   Lock Macihne object for check condition         */
                /*---------------------------------------------------*/
                objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
            }


            //----------------------------------------------
            // Lock cassette objects for check condition.
            //----------------------------------------------
            objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        }
        else if ( lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE ) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Lock Equipment Main Object

            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(params.getEquipmentID());
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

            // Lock Equipment LoadCassette Element (Read)
            List<String> loadCastSeq = new ArrayList<>();
            for (int loadCastNo = 0; loadCastNo < casLen; loadCastNo++ ){
                loadCastSeq.add(ObjectIdentifier.fetchValue(cassetteIDs.get(loadCastNo)));
            }

            Inputs.ObjAdvanceLockIn advanceLockIn = new Inputs.ObjAdvanceLockIn();
            advanceLockIn.setObjectID(params.getEquipmentID());
            advanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            advanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
            advanceLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ));
            advanceLockIn.setKeyList(loadCastSeq);
            objectLockMethod.advancedObjectLock(objCommon, advanceLockIn);


        }


        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        log.info("Check Transaction ID and equipment Category combination.");

        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());

        for( int casCnt = 0 ; casCnt < casLen ; casCnt++ ) {
            //---------------------------------------
            // Check lot InterFabXfer State
            //---------------------------------------

            /*-------------------------------*/
            /*  Get Lot list in carrier      */
            /*-------------------------------*/

            Infos.LotListInCassetteInfo cassetteLotListOut = cassetteMethod.cassetteLotIDListGetDR(objCommon, cassetteIDs.get(casCnt));

            int lotLen = CimArrayUtils.getSize(cassetteLotListOut.getLotIDList());
            for( int lotCnt = 0 ; lotCnt < lotLen ; lotCnt++ ) {

                String strLot_interFabXferState_Get_out = lotMethod.lotInterFabXferStateGet(objCommon, cassetteLotListOut.getLotIDList().get(lotCnt));

                if(CimStringUtils.equals(strLot_interFabXferState_Get_out, BizConstant.SP_INTERFAB_XFERSTATE_ORIGINDELETING) ) {
                    throw new ServiceException(retCodeConfigEx.getInterfabInvalidXferstateDeleting());
                }
            }
        }

        //================================================================
        // Checking Previous Job is running or not
        //================================================================
        try {
            waferMethod.waferSorterCheckRunningJobs(objCommon, params.getEquipmentID(), params.getPortGroup());
        } catch (ServiceException e) {
            // If there are running Jobs, then return
            if (Validations.isEquals(retCodeConfigEx.getWaferSortAlreadyRunning(), e.getCode())) {
                throw e;
            }else if (!Validations.isEquals(retCodeConfigEx.getWaferSortPreviousJobNotFound(), e.getCode())){
                throw e;
            }
        }


        //================================================================
        // Checking Cassette Status
        //================================================================
        if(CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_POSITIONCHANGE)||
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_LOTTRANSFEROFF) ||
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_SEPARATEOFF) ||
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_COMBINEOFF) ||
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_JUSTIN) ||
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_SCRAP) ||
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_ADJUSTTOMM) ||   //P4000346
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_AUTOSORTING) ||   //D9000005
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_LOTTRANSFER) ||   //D9000005
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_WAFEREND) ||   //D9000005
                CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_ADJUSTTOSORTER)) {
            int slotMapLen = CimArrayUtils.getSize(params.getWaferSorterSlotMapList());

            for(int i = 0;i < slotMapLen;i++) {
                Outputs.ObjCassetteStatusOut cassetteStatusDR = null;
                try{
                    cassetteStatusDR = cassetteMethod.cassetteGetStatusDR(objCommon, params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                }catch (ServiceException e) {
                    if(Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())){
                        log.info("cassette_getStatusDR() == RC_NOT_FOUND_CASSETTE");
                        continue;
                    }
                    throw e;
                }

                log.info("strCassette_getStatusDR_out.drbl_state---> : {}", cassetteStatusDR.getDurableState());

                if( CimStringUtils.equals(cassetteStatusDR.getDurableState(), CIMStateConst.CIM_DURABLE_NOTAVAILABLE)) {
                    log.error("Cassette Status == Not Available");
                    throw new ServiceException(retCodeConfigEx.getCassetteNotAvailable());
                }
            }
        }


        //--------------------------------------------------------------------------
        // If ActionCode is SP_Sorter_End , check any data is existed on SLOTMAP DB
        //--------------------------------------------------------------------------
        if ( CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_END)){
            strWaferSorterSlotMap.setPortGroup(params.getPortGroup());
            strWaferSorterSlotMap.setEquipmentID(params.getEquipmentID());
            strWaferSorterSlotMap.setDestinationCassetteManagedByOM(false);
            strWaferSorterSlotMap.setDestinationSlotNumber(0L);
            strWaferSorterSlotMap.setOriginalCassetteManagedByOM(false);
            strWaferSorterSlotMap.setOriginalSlotNumber(0L);

            //------------------------------------------------------
            //   Search From Flotmap DB
            //------------------------------------------------------
            List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapSelectDROut = null;
            try {
                strWaferSorterSlotMapSelectDROut = waferMethod.waferSorterSlotMapSelectDR(objCommon, BizConstant.SP_SORTER_SLOTMAP_ALLDATA,
                        "", BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG, BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG, strWaferSorterSlotMap);
            } catch (ServiceException e) {
                //------------------------------------------------------
                // When there's no data
                //------------------------------------------------------
                if (Validations.isEquals(e.getCode(), retCodeConfigEx.getNotFoundSlotMapRecord())) {
                    log.error("There are not Runing job records ");
                    throw new ServiceException(new OmCode(retCodeConfigEx.getWaferSortPreviousJobNotFound(),params.getEquipmentID().getValue(),params.getPortGroup()));
                }
                //------------------------------------------------------
                // When Error Occures.
                //------------------------------------------------------
                else  {
                    throw e;
                }
            }
            log.info("Found previously running jobs");
        }

        //======================================================
        //   Check WaferSorter Operation Data Readiness
        //======================================================
        // P4000099 change name objWaferSorter_CheckConditionForAction_out to objWaferSorter_CheckConditionForAction_out
        log.info("Try to waferSorter_CheckConditionForAction");
        List<Infos.WaferSorterSlotMap> strModWaferSorterSlotMapSequence = waferMethod.waferSorterCheckConditionForAction(objCommon, params.getEquipmentID(), params.getActionCode(),
                params.getWaferSorterSlotMapList(), params.getPortGroup(), params.getPhysicalRecipeID());

        //check contamination
//        if ((StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn())
//        && (StandardProperties.OM_SORTER_CARRIER_CATEGORY_CHK_MODE.isOn())){
//            //qiandao mode
//            //check exchange first
//            List<Infos.WaferSorterSlotMap> waferSorterSlotMapList = params.getWaferSorterSlotMapList();
//            for (Infos.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMapList) {
//                contaminationMethod.carrierExchangeCheckQiandaoMode(objCommon,
//                        waferSorterSlotMap.getDestinationCassetteID(), waferSorterSlotMap.getWaferID());
//            }
//            //check sorter
//            if (StandardProperties.OM_SORTER_CARRIER_CATEGORY_CHK_MODE.isOn()) {
//                contaminationMethod.contaminationSorterCheckForQiandao(params);
//            }
//        } else if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOff()
//                && StandardProperties.OM_SORTER_CARRIER_CATEGORY_CHK_MODE.isOff()) {
//            //OMS mode
//            contaminationMethod.contaminationSorterCheck(params);
//        } else if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOff()
//                && StandardProperties.OM_SORTER_CARRIER_CATEGORY_CHK_MODE.isOn()) {
//            contaminationMethod.contaminationSorterCheckForQiandao(params);
//        }else if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn()
//                && StandardProperties.OM_SORTER_CARRIER_CATEGORY_CHK_MODE.isOff()){
//            List<Infos.WaferSorterSlotMap> waferSorterSlotMapList = params.getWaferSorterSlotMapList();
//            for (Infos.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMapList) {
//                contaminationMethod.carrierExchangeCheckQiandaoMode(objCommon,
//                        waferSorterSlotMap.getDestinationCassetteID(), waferSorterSlotMap.getWaferID());
//            }
//        }
        //------------------------------------------------------
        // Verified Condition Structure Condition Structure
        //------------------------------------------------------

        //======================================================
        //   Send Request to TCS
        //======================================================
        if ( CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFEREND)) {
            log.info("actionCode == SP_Sorter_AutoSorting");
            log.info("No need to call TCSMgr_SendWaferSortOnEqpReq.");
        } else {
            String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
            String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
            long sleepTimeValue;
            long retryCountValue;

            if (null == tmpSleepTimeValue) {
                sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
            } else {
                sleepTimeValue = Long.valueOf(tmpSleepTimeValue) ;    //D9000001
            }

            if (null == tmpRetryCountValue) {
                retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
            } else {
                retryCountValue = Integer.valueOf(tmpRetryCountValue);    //D9000001
            }

            log.info("env value of OM_EAP_CONNECT_SLEEP_TIME  = : {}",sleepTimeValue);
            log.info("env value of OM_EAP_CONNECT_RETRY_COUNT = : {}",retryCountValue);

            //objTCSMgr_SendWaferSortOnEqpReq_out strTCSMgr_SendWaferSortOnEqpReq_out;

            //'retryCountValue + 1' means first try plus retry count
            IEAPRemoteManager remoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), params.getEquipmentID(), null, true);
            for(int i = 0 ; i < (retryCountValue + 1) ; i++) {
                if( CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_END)) {
                    log.info("actionCode == SP_Sorter_End, Send PortInfo for TCS");

                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, params.getEquipmentID());
                    int portLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                    log.info("portLen = :{}",portLen);

                    for ( int j = 0; j < portLen; j++ ) {
                        log.info("strEquipment_portInfo_GetDR_out.strEqpPortInfo.strEqpPortStatus(j).portGroup = {}",eqpPortInfo.getEqpPortStatuses().get(j).getPortGroup());
                        log.info("strEquipment_portInfo_GetDR_out.strEqpPortInfo.strEqpPortStatus(j).portID    = {}",eqpPortInfo.getEqpPortStatuses().get(j).getPortID());
                        if( CimStringUtils.equals(params.getPortGroup(), eqpPortInfo.getEqpPortStatuses().get(j).getPortGroup())) {
                            Infos.WaferSorterSlotMap waferSorterSlotMap = new Infos.WaferSorterSlotMap();
                            waferSorterSlotMap.setPortGroup(params.getPortGroup());
                            waferSorterSlotMap.setEquipmentID(params.getEquipmentID());
                            waferSorterSlotMap.setActionCode(BizConstant.SP_SORTER_END);
                            waferSorterSlotMap.setDirection(BizConstant.SP_SORTER_DIRECTION_MM);
                            waferSorterSlotMap.setDestinationCassetteID(eqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID());
                            waferSorterSlotMap.setDestinationPortID(eqpPortInfo.getEqpPortStatuses().get(j).getPortID());

                            if(null == eqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID()) {
                                waferSorterSlotMap.setDestinationCassetteManagedByOM(false);
                            } else {
                                waferSorterSlotMap.setDestinationCassetteManagedByOM(true);
                            }

                            waferSorterSlotMap.setDestinationSlotNumber(0L);
                            waferSorterSlotMap.setOriginalCassetteID(eqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID());
                            waferSorterSlotMap.setOriginalPortID(eqpPortInfo.getEqpPortStatuses().get(j).getPortID());
                            waferSorterSlotMap.setOriginalCassetteManagedByOM(waferSorterSlotMap.getDestinationCassetteManagedByOM());
                            waferSorterSlotMap.setOriginalSlotNumber(0L);
                            waferSorterSlotMap.setRequestUserID(params.getUser().getUserID());
                            waferSorterSlotMap.setSorterStatus(BizConstant.SP_SORTER_REQUESTED);
                            strModWaferSorterSlotMapSequence.add(waferSorterSlotMap);
                        }
                    }
                }

                if ( CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_ADJUSTTOSORTER)
                        || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_END)) {
                    log.info("actionCode == AdjustToSorter, No Need to send Request to TCS.");
                    break;
                } else {
                    log.info("actionCode != AdjustToSorter, Need to send Request to TCS.");

                    //send to TCSMgr_SendWaferSortOnEqpReq
                    /*--------------------------*/
                    /*    Send Request to TCS   */
                    /*--------------------------*/
                    //TCSMgr_SendWaferSortOnEqpReq
                    /*Inputs.SendWaferSortOnEqpReqIn sendWaferSortOnEqpReqIn = new Inputs.SendWaferSortOnEqpReqIn();
                    sendWaferSortOnEqpReqIn.setObjCommonIn(objCommon);
                    sendWaferSortOnEqpReqIn.setRequestUserID(objCommon.getUser());
                    sendWaferSortOnEqpReqIn.setActionCode(params.getActionCode());
                    sendWaferSortOnEqpReqIn.setEquipmentID(params.getEquipmentID());
                    sendWaferSortOnEqpReqIn.setStrWaferSorterActionListSequence(strModWaferSorterSlotMapSequence);
                    sendWaferSortOnEqpReqIn.setPortGroup(params.getPortGroup());
                    sendWaferSortOnEqpReqIn.setPhysicalRecipeID(params.getPhysicalRecipeID());
                    tcsMethod.sendTCSReq(TCSReqEnum.sendWaferSortOnEqpReq,sendWaferSortOnEqpReqIn);*/
                    if (remoteManager==null){
                        break;
                    }
                    try {
                        // Jerry TODO: 2021/6/23 - 见new Sorter相关功能
                        //remoteManager.sendOnlineSorterActionExecuteReq(params);
                    } catch (ServiceException ex){
                        if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                                Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                                Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                            if (i<retryCountValue){
                                try {
                                    Thread.sleep(sleepTimeValue);
                                    continue;
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    ex.addSuppressed(e);
                                    throw ex;
                                }
                            }
                        }
                        throw ex;
                    } catch (Exception ex){
                        if (i<retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                        throw ex;
                    }
                    break;
                }
            }

            /*
            if ( rc != RC_OK ) {
                log.info("","TCSMgr_SendWaferSortOnEqpReq() returned error RC = ",rc);
                if(rc == RC_NOT_FOUND_TCS ) {
                    SET_MSG_RC( strOnlineSorterActionExecuteReqResult, MSG_NOT_FOUND_TCS, RC_NOT_FOUND_TCS );        //P5000145
                }
                // Temporary Logic 2001/08/27
                else {
                    strOnlineSorterActionExecuteReqResult.strResult = strTCSMgr_SendWaferSortOnEqpReq_out.strResult ;
                    log.info("PPTManager_i::txOnlineSorterActionExecuteReq result  msgID/msgText",
                            strOnlineSorterActionExecuteReqResult.strResult.messageID,
                            strOnlineSorterActionExecuteReqResult.strResult.messageText);
                    return(rc);
                }
            }
            */
        }
        //====================================================================
        // Delete FOSB's Slotmap information when Seqp_mode_change/reqP_Sorter_End
        //====================================================================
        if ( CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_END)) {
            //--------------------------------------------------------------------
            // Delete All SlotMap information
            //--------------------------------------------------------------------
            log.info("Try to waferSorter_slotMap_DeleteDR for FOSB");
            waferMethod.waferSorterSlotMapDeleteDR(objCommon, params.getPortGroup(), params.getEquipmentID(),
                    cassetteIDs, "", BizConstant.SP_SORTER_ALLDELETE, "");
            return;
        }
        //--------------------------------------------------------------------
        // Other Case(Except End Operation), Insert SlotMap Information with
        // status SP_Sorter_Requested.
        //--------------------------------------------------------------------
        else {
            log.info("Try to waferSorter_slotMap_InsertDR ");

            //-----------------------------------------------------------------------------------------------------------------
            // (sorterStatus)
            // When Request is done in TCS at the time of AdjustToSorter, sorterStatus is written in the record as Succeeded.
            // TCS is because Report can't be returned.
            //
            // (Direction)
            // direction is changed to "TCS".
            // It is because AdjustToSorter Operation can't be indicated on the Action Result List screen.
            //-----------------------------------------------------------------------------------------------------------------
            if(CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_ADJUSTTOSORTER)) {
                len = CimArrayUtils.getSize(strModWaferSorterSlotMapSequence);
                for(int i = 0; i < len ; i++) {
                    Infos.WaferSorterSlotMap waferSorterSlotMap = strModWaferSorterSlotMapSequence.get(i);
                    waferSorterSlotMap.setSorterStatus(BizConstant.SP_SORTER_SUCCEEDED);
                    waferSorterSlotMap.setDirection(BizConstant.SP_SORTER_DIRECTION_TCS);
                }
            }
            waferMethod.waferSorterSlotMapInsertDR(objCommon, strModWaferSorterSlotMapSequence);
        }

        //----------------------------------------------------------------------
        //  Sorter component Job Status Update (Xfer -> Executing)
        //----------------------------------------------------------------------
        if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFEREND)) {
            log.info("actionCode == SP_Sorter_AutoSorting ");
            Inputs.SorterSorterJobStatusGetDRIn strSorterSorterJobStatusGetDRIn = new Inputs.SorterSorterJobStatusGetDRIn();
            strSorterSorterJobStatusGetDRIn.setEquipmentID(params.getEquipmentID());
            strSorterSorterJobStatusGetDRIn.setOriginalCassetteID(params.getWaferSorterSlotMapList().get(0).getOriginalCassetteID());
            strSorterSorterJobStatusGetDRIn.setDestinationCassetteID(params.getWaferSorterSlotMapList().get(0).getDestinationCassetteID());
            strSorterSorterJobStatusGetDRIn.setPortGroupID(params.getPortGroup());

            Outputs.SorterSorterJobStatusGetDROut strSorterSorterJobStatusGetDROut = sorterMethod.sorterSorterJobStatusGetDR(objCommon, strSorterSorterJobStatusGetDRIn );

            Params.SJStatusChgRptParams sjStatusChgRptParams = new Params.SJStatusChgRptParams();
            sjStatusChgRptParams.setEquipmentID(params.getEquipmentID());
            sjStatusChgRptParams.setPortGroup(params.getPortGroup());
            sjStatusChgRptParams.setJobID(strSorterSorterJobStatusGetDROut.getSorterComponentJobID());
            sjStatusChgRptParams.setJobType(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB);
            sjStatusChgRptParams.setJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_EXECUTING);
            this.sxSJStatusChgRpt(objCommon, sjStatusChgRptParams);
        }

        // 检查环境变量 OM_REGISTER_FOSB是否需要注册fosb
        if (StandardProperties.OM_REGISTER_FOSB.isTrue()){
            Validations.check(waferMapLen==0,retCodeConfig.getInvalidParameter());
            ObjectIdentifier cassetteID = params.getWaferSorterSlotMapList().get(0).getDestinationCassetteID();
            try {
                cassetteMethod.cassetteCheckEmpty(cassetteID);
            } catch (ServiceException ex){
                if (Validations.isEquals(ex.getCode(),retCodeConfig.getNotFoundCassette())){
                    Infos.DurableAttribute strDurableAttribute=new Infos.DurableAttribute();
                    strDurableAttribute.setDurableID(cassetteID);
                    strDurableAttribute.setCategory(BizConstant.SP_INTERFAB_XFERCATEGORY_FOSB);
                    strDurableAttribute.setUsageCheckFlag(true);
                    strDurableAttribute.setCapacity(25);
                    strDurableAttribute.setNominalSize(12);
                    strDurableAttribute.setMaximumOperationStartCount(0D);
                    strDurableAttribute.setIntervalBetweenPM(0);
                    durableService.sxDurableSetReq(objCommon,false,BizConstant.SP_DURABLECAT_CASSETTE,strDurableAttribute,params.getClaimMemo());
                }
            }
        }

        /*Params.OnlineSorterRptParams onlineSorterRptParams=new Params.OnlineSorterRptParams();
        BeanUtils.copyProperties(params,onlineSorterRptParams);
        onlineSorterRptParams.setRcEAP(0);
        Optional.ofNullable(onlineSorterRptParams.getWaferSorterSlotMapList()).ifPresent(waferSorterSlotMaps
                -> waferSorterSlotMaps.forEach(waferSorterSlotMap -> {
                    waferSorterSlotMap.setDirection(BizConstant.SP_SORTER_DIRECTION_TCS);
                    waferSorterSlotMap.setSorterStatus(BizConstant.SP_SORTER_SUCCEEDED);
                }
        ));
        List<Infos.WaferSorterSlotMap> waferSorterSlotMapList = onlineSorterRptParams.getWaferSorterSlotMapList();
        if (StringUtils.equals(BizConstant.SP_SORTER_READ,onlineSorterRptParams.getActionCode())&&ArrayUtils.isNotEmpty(waferSorterSlotMapList)){
            Infos.WaferSorterSlotMap waferSorterSlotMap = waferSorterSlotMapList.get(0);
            waferSorterSlotMap.setAliasName("Alias-01");
            waferSorterSlotMap.setDestinationSlotNumber(1L);
            waferSorterSlotMap.setOriginalSlotNumber(1L);
            for (int j = 0; j < 24; j++) {
                Infos.WaferSorterSlotMap newWaferSorterSlotMap=new Infos.WaferSorterSlotMap();
                waferSorterSlotMapList.add(newWaferSorterSlotMap);
                BeanUtils.copyProperties(waferSorterSlotMap,newWaferSorterSlotMap);
                newWaferSorterSlotMap.setDestinationSlotNumber(j+2L);
                newWaferSorterSlotMap.setOriginalSlotNumber(j+2L);
                newWaferSorterSlotMap.setAliasName("Alias-"+1+j);
            }
        }
        if (StringUtils.equals(BizConstant.SP_SORTER_START,onlineSorterRptParams.getActionCode())&&ArrayUtils.isNotEmpty(waferSorterSlotMapList)){
            for (int j = 0; j < ArrayUtils.getSize(waferSorterSlotMapList); j++) {
                Infos.WaferSorterSlotMap waferSorterSlotMap = waferSorterSlotMapList.get(j);
                waferSorterSlotMap.setDestinationSlotNumber(j+1L);
                waferSorterSlotMap.setOriginalSlotNumber(j+1L);
            }
        }
        objCommon.setTransactionID(TransactionIDEnum.WAFER_SORTER_ON_EQP_RPT.getValue());

        try {
            sxOnlineSorterRpt(objCommon,onlineSorterRptParams);
        } catch (ServiceException ex){
            if (Validations.isEquals(ex.getCode(),retCodeConfig.getInvalidActionCode())||
                    Validations.isEquals(ex.getCode(),retCodeConfig.getInvalidParameter())||
                    Validations.isEquals(ex.getCode(),retCodeConfigEx.getInvalidSorterstatus())||
                    Validations.isEquals(ex.getCode(),retCodeConfigEx.getWafersorterSlotmapCompareError())){
                log.debug("{} {} {}","PPTServiceManager_i:: txWaferSorterOnEqpRpt", "rc =",ex.getMessage());
                return;
            }
            log.debug("{} {}","PPTServiceManager_i:: txWaferSorterOnEqpRpt", "rc != RC_OK");
            throw ex;
        }*/
    }

    public void sxOnlineSorterRpt(Infos.ObjCommon objCommon, Params.OnlineSorterRptParams params) {

        //---------------------------------
        // INITIALIZATION
        //---------------------------------
        OmCode rcParamChk = new OmCode();
        rcParamChk.setCode(0);
        Integer rcTCS = params.getRcEAP();

        int i = 0;
        int j = 0;


        List<ObjectIdentifier> tmpCassetteIDs = new ArrayList<>();
        int castLen = 0;
        Boolean bExist = false;

        // check alias name是否一致,注掉自定义检查,flow source
        /*try {
            waferMethod.waferSorterCheckAliasName(objCommon,params.getWaferSorterSlotMapList());
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(),retCodeConfigEx.getWafersorterSlotmapCompareError())){
                rcParamChk=retCodeConfigEx.getWafersorterSlotmapCompareError();
            } else {
                throw ex;
            }
        }*/

        //----------------------------------------
        //  Check Equipment ID is SORTER or
        //----------------------------------------
        log.info("Check Transaction ID and equipment Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());

        //-------------------------------------------------------------------------
        //    Check INPUT PARAMETER: TCS Reply Sequence Length Check
        //-------------------------------------------------------------------------
        int nListLen = CimArrayUtils.getSize(params.getWaferSorterSlotMapList());
        if (nListLen == 0) {
            log.info(" In Para params.getStrWaferSorterSlotMapSequence().length == 0");
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        //-------------------------------------------------------------------------
        //    Check INPUT PARAMETER: Action Code exist And Same Data And Sorter Status
        //-------------------------------------------------------------------------
        String tempRequestTime = params.getWaferSorterSlotMapList().get(0).getRequestTime();
        String tempDirection = params.getWaferSorterSlotMapList().get(0).getDirection();
        String tempPortGroup = params.getWaferSorterSlotMapList().get(0).getPortGroup();

        for (i = 0; i < nListLen; i++) {
            //-----------------------------
            //    Check Action Code exist
            //-----------------------------
            if (!CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_READ)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_START)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_MINIREAD)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_POSITIONCHANGE)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_LOTTRANSFEROFF)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_SEPARATEOFF)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_COMBINEOFF)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_JUSTIN)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_JUSTOUT)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_WAFERENDOFF)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_SCRAP)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_ADJUSTTOMM)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_ADJUSTTOSORTER)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), BizConstant.SP_SORTER_WAFEREND)
                    && !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getActionCode(), params.getActionCode())) {
                log.info("actionCode Check Error = {}", params.getWaferSorterSlotMapList().get(i).getActionCode());
                rcParamChk = retCodeConfig.getInvalidActionCode();
                break;
            }

            //-----------------------------
            //    Check In Parameter
            //-----------------------------
            if (!CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getRequestTime(), tempRequestTime)
                    || !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getDirection(), tempDirection)
                    || !CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getPortGroup(), tempPortGroup)
                    || !ObjectIdentifier.equalsWithValue(params.getWaferSorterSlotMapList().get(i).getEquipmentID(), params.getEquipmentID())) {
                log.info(" Others Code Check Error ");
                rcParamChk = retCodeConfig.getInvalidInputParam();
                break;
            }

            //-------------------------------
            //  Check In sorterStatus exist
            //-------------------------------
            if (!CimStringUtils.equals(params.getWaferSorterSlotMapList().get(i).getSorterStatus(), BizConstant.SP_SORTER_SUCCEEDED)) {
                log.info(" Sorter Statu Error ");
                rcParamChk = retCodeConfigEx.getInvalidSorterstatus();
                break;
            }
        }

        //---------------------------------------------------------------------------------
        // Collect participant cassettes (original/destination) from WaferSorterSlotMapSeq.
        //---------------------------------------------------------------------------------
        int waferMapLen = CimArrayUtils.getSize(params.getWaferSorterSlotMapList());
        int casLen = 0;
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();

        for (i = 0; i < waferMapLen; i++) {
            // interested in SiView managed casettes only.
            if (CimBooleanUtils.isTrue(params.getWaferSorterSlotMapList().get(i).getOriginalCassetteManagedByOM())) {
                Boolean dupCastFlag = false;
                for (j = 0; j < casLen; j++) {
                    if (ObjectIdentifier.equalsWithValue(cassetteIDs.get(j), params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID())) {
                        dupCastFlag = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(dupCastFlag)) {
                    cassetteIDs.add(params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID());
                    log.info("SiView managed original cassette found. : {}", params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID());
                    casLen++;
                }
            }
            if (CimBooleanUtils.isTrue(params.getWaferSorterSlotMapList().get(i).getDestinationCassetteManagedByOM())) {
                Boolean dupCastFlag = false;
                for (j = 0; j < casLen; j++) {
                    if (ObjectIdentifier.equalsWithValue(cassetteIDs.get(j), params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID())) {
                        dupCastFlag = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(dupCastFlag)) {
                    cassetteIDs.add(params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                    log.info("SiView managed destination cassette found. ： {}", params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                    casLen++;
                }
            }
        }


        String sorterJobLockFlagStr = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getValue();
        int sorterJobLockFlag = null == sorterJobLockFlagStr ? 0 : 1;
        log.info("sorterJobLockFlag", sorterJobLockFlag);

        if (1 == sorterJobLockFlag && (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)||
                CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFEREND))) {
            log.info("sorterJobLockFlag = 1 and actionCode = SP_Sorter_AutoSorting");

            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------
            ObjectIdentifier loadCassetteID = cassetteIDs.get(0);
            Inputs.SorterSorterJobLockDRIn sorterSorterJobLockDRIn = new Inputs.SorterSorterJobLockDRIn();
            sorterSorterJobLockDRIn.setCassetteID(loadCassetteID);
            sorterSorterJobLockDRIn.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
            log.info("calling sorter_sorterJob_LockDR()");
            sorterMethod.sorterSorterJobLockDR(objCommon, sorterSorterJobLockDRIn);
        }

        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();
        int lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue();
        if ( 1 == sorterJobLockFlag ) {
            log.info("sorterJobLockFlag = 1");

            // Get required equipment lock mode
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

            lockMode = objLockModeOut.getLockMode().intValue();
        }
        log.info(  "lockMode : {}", lockMode );

        if ( lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE ) {
            log.info( "lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            // Lock Equipment LoadCassette Element (Write)
            List<String> loadCastSeq = new ArrayList<>();
            for ( int loadCastNo = 0; loadCastNo < casLen; loadCastNo++ ) {
                loadCastSeq.add(ObjectIdentifier.fetchValue(cassetteIDs.get(loadCastNo)));
            }

            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(params.getEquipmentID());
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
            objAdvanceLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
            objAdvanceLockIn.setKeyList(loadCastSeq);
            objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info( "lockMode = SP_EQP_LOCK_MODE_WRITE");
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        //---------------------------------------
        // Lock cassette objects for update.
        //---------------------------------------
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        //----------------------------------------
        // Set Sorter Status Value for UPDATE
        //----------------------------------------
        String updateStatus = BizConstant.SP_SORTER_ERRORED;

        if (( rcTCS == 0 ) && (Validations.isEquals(rcParamChk, retCodeConfig.getSucc()))) {
            log.info("rcTCS & rcParamChk == RC_OK : SET SP_Sorter_Succeeded  ");
            updateStatus = BizConstant.SP_SORTER_SUCCEEDED;
        }


        //----------------------------------------
        //  Update Sorter Status On SLOTMAP
        //----------------------------------------

        Infos.WaferSorterSlotMap strWaferSorterSlotMapUpdateReferenceCondition = new Infos.WaferSorterSlotMap();

        strWaferSorterSlotMapUpdateReferenceCondition.setPortGroup(params.getWaferSorterSlotMapList().get(0).getPortGroup());
        strWaferSorterSlotMapUpdateReferenceCondition.setEquipmentID(params.getEquipmentID());
        strWaferSorterSlotMapUpdateReferenceCondition.setActionCode(params.getActionCode());
        strWaferSorterSlotMapUpdateReferenceCondition.setRequestTime(params.getWaferSorterSlotMapList().get(0).getRequestTime());
        strWaferSorterSlotMapUpdateReferenceCondition.setDirection(BizConstant.SP_SORTER_DIRECTION_MM);
        strWaferSorterSlotMapUpdateReferenceCondition.setSorterStatus(BizConstant.SP_SORTER_REQUESTED);

        //----------------------------------------
        // Slot Map UPDATE to Requested Record
        //----------------------------------------
        waferMethod.waferSorterSlotMapStatusUpdateDR(objCommon, strWaferSorterSlotMapUpdateReferenceCondition, updateStatus);

        //------------------------------------
        //  Reserve Writing Buffer All Record
        //------------------------------------
        List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapWrite = params.getWaferSorterSlotMapList();

        //----------------------------------
        // GET Requesting Slot Map
        //----------------------------------
        Infos.WaferSorterSlotMap strWaferSorterGetSlotMapReferenceCondition = new Infos.WaferSorterSlotMap();
        String requiredData = BizConstant.SP_SORTER_SLOTMAP_LATESTDATA;

        strWaferSorterGetSlotMapReferenceCondition.setPortGroup(params.getWaferSorterSlotMapList().get(0).getPortGroup());
        strWaferSorterGetSlotMapReferenceCondition.setEquipmentID(params.getEquipmentID());
        strWaferSorterGetSlotMapReferenceCondition.setActionCode(params.getActionCode());
        strWaferSorterGetSlotMapReferenceCondition.setRequestTime(params.getWaferSorterSlotMapList().get(0).getRequestTime());
        strWaferSorterGetSlotMapReferenceCondition.setDirection(BizConstant.SP_SORTER_DIRECTION_MM);
        strWaferSorterGetSlotMapReferenceCondition.setDestinationCassetteManagedByOM(false);
        strWaferSorterGetSlotMapReferenceCondition.setDestinationSlotNumber(0L);
        strWaferSorterGetSlotMapReferenceCondition.setOriginalCassetteManagedByOM(false);
        strWaferSorterGetSlotMapReferenceCondition.setOriginalSlotNumber(0L);


        List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapSelectDROut = waferMethod.waferSorterSlotMapSelectDR(objCommon, requiredData, "", BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG, strWaferSorterGetSlotMapReferenceCondition);

        //-------------------------------------------------------------------------
        //  SlotMap Request Sequence Reserve Writing Buffer All Record
        //-------------------------------------------------------------------------
        int SlotMapRequestLen = CimArrayUtils.getSize(strWaferSorterSlotMapSelectDROut);

        //-------------------------------------------------------------------------
        //  CassetteIDs of the object is acquired for NOT AVAILABLE.
        //-------------------------------------------------------------------------
        for (i = 0; i < SlotMapRequestLen; i++) {
            //--------------------------------
            //  Get All destinationCassetteID
            //--------------------------------
            bExist = false;
            castLen = CimArrayUtils.getSize(tmpCassetteIDs);
            for (j = 0; j < castLen; j++) {
                if (ObjectIdentifier.equalsWithValue(tmpCassetteIDs.get(j), strWaferSorterSlotMapSelectDROut.get(i).getDestinationCassetteID())) {
                    log.info("destinationCassetteID ：  bExist == TRUE ");
                    bExist = true;
                }
            }

            if (CimBooleanUtils.isFalse(bExist)) {
                if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMapSelectDROut.get(i).getDestinationCassetteID())) {
                    tmpCassetteIDs.add(strWaferSorterSlotMapSelectDROut.get(i).getDestinationCassetteID());
                }
            }

            //--------------------------------
            //  Get All originalCassetteID
            //--------------------------------
            bExist = false;
            castLen = CimArrayUtils.getSize(tmpCassetteIDs);
            for (j = 0; j < castLen; j++) {
                if (ObjectIdentifier.equalsWithValue(tmpCassetteIDs.get(j), strWaferSorterSlotMapSelectDROut.get(i).getOriginalCassetteID())) {
                    log.info("originalCassetteID bExist == TRUE ");
                    bExist = true;
                }
            }

            if (CimBooleanUtils.isFalse(bExist)) {
                if (!ObjectIdentifier.isEmpty(strWaferSorterSlotMapSelectDROut.get(i).getOriginalCassetteID())) {
                    tmpCassetteIDs.add(strWaferSorterSlotMapSelectDROut.get(i).getOriginalCassetteID());
                }
            }
        }

        //--------------------------------
        //  All CassetteIDs TRACE
        //--------------------------------
        castLen = CimArrayUtils.getSize(tmpCassetteIDs);
        log.info("tmpCassetteIDs.length() = {}", castLen);

        log.info("//---------------------------//");
        log.info("// All CassetteIDs Start     //");
        log.info("//---------------------------//");

        for (j = 0; j < castLen; j++) {
            log.info("tmpCassetteIDs = {}", tmpCassetteIDs.get(j));
        }

        log.info("//---------------------------//");
        log.info("// All CassetteIDs E n d     //");
        log.info("//---------------------------//");

        //-------------------------
        //  When TCS RC is ERROR
        //-------------------------
        if (( rcTCS != 0 ) || (!Validations.isEquals(rcParamChk, retCodeConfig.getSucc()))) {

            //----------------------------------------
            //  MKAE A CASSETTE NOT AVAILABLE
            //----------------------------------------
            castLen = CimArrayUtils.getSize(tmpCassetteIDs);
            for (i = 0; i < castLen; i++) {
                Outputs.ObjCassetteStatusOut cassetteStatusDR = null;
                try{
                    cassetteStatusDR = cassetteMethod.cassetteGetStatusDR(objCommon, tmpCassetteIDs.get(i));
                }catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                        log.info("cassette_getStatusDR() == RC_NOT_FOUND_CASSETTE");
                        continue;
                    }else {
                        throw e;
                    }

                }
                log.info("strCassette_getStatusDR_out.drbl_state---> : {}", cassetteStatusDR.getDurableState());

                if (CimStringUtils.equals(cassetteStatusDR.getDurableState(), CIMStateConst.CIM_DURABLE_NOTAVAILABLE)) {
                    log.info("Cassette Status == Not Available");
                    continue;
                }

                try {
                    //todo:because of changing the db so we can not sxCarrierStatusChangeRpt
                    Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams=new Params.CarrierDetailInfoInqParams();
                    carrierDetailInfoInqParams.setCassetteID(tmpCassetteIDs.get(i));
                    carrierDetailInfoInqParams.setDurableOperationInfoFlag(false);
                    carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
                    carrierDetailInfoInqParams.setUser(objCommon.getUser());
                    Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = bankInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);
                    Infos.CassetteStatusInfo cassetteStatusInfo = carrierDetailInfoInqResult.getCassetteStatusInfo();

                    Params.MultiDurableStatusChangeReqParams multiDurableStatusChangeReqParams=new Params.MultiDurableStatusChangeReqParams();
                    Infos.MultiDurableStatusChangeReqInParm multiDurableStatusChangeReqInParm=new Infos.MultiDurableStatusChangeReqInParm();
                    multiDurableStatusChangeReqInParm.setDurableStatus(CIMStateConst.CIM_DURABLE_NOTAVAILABLE);
                    multiDurableStatusChangeReqInParm.setDurableSubStatus(ObjectIdentifier.buildWithValue(CIMFW_DURABLE_SUB_STATE_DEFAULT));
                    multiDurableStatusChangeReqInParm.setDurableCategory(SP_DURABLECAT_CASSETTE);
                    List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos=new ArrayList<>();
                    Infos.StatusChangeDurableInfo statusChangeDurableInfo=new Infos.StatusChangeDurableInfo();
                    statusChangeDurableInfo.setDurableID(tmpCassetteIDs.get(i));
                    statusChangeDurableInfo.setDurableStatus(cassetteStatusInfo.getCassetteStatus());
                    statusChangeDurableInfo.setDurableSubStatus(cassetteStatusInfo.getDurableSubStatus());
                    statusChangeDurableInfos.add(statusChangeDurableInfo);
                    multiDurableStatusChangeReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
                    multiDurableStatusChangeReqParams.setParm(multiDurableStatusChangeReqInParm);
                    params.setUser(objCommon.getUser());
                    params.setClaimMemo(params.getClaimMemo());
                    durableService.sxMultiDurableStatusChangeReq(objCommon,multiDurableStatusChangeReqParams);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())){
                        throw e;
                    }
                }
            }

            if (!Validations.isEquals(rcParamChk, retCodeConfig.getInvalidParameter())) {
                //-------------------------------
                //  Insert TCS Data to SLOTMAP
                //-------------------------------
                waferMethod.waferSorterSlotMapInsertDR(objCommon, strWaferSorterSlotMapWrite);
            }

            //--------------------------------------------------------------------
            //  Sorter Job Status Update (Executing -> Errored)
            //--------------------------------------------------------------------
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)||
                    CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)||
                    CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFEREND)) {
                log.info("actionCode == SP_Sorter_AutoSorting");

                Inputs.SorterSorterJobStatusGetDRIn strSorterSorterJobStatusGetDRIn = new Inputs.SorterSorterJobStatusGetDRIn();
                strSorterSorterJobStatusGetDRIn.setEquipmentID(params.getEquipmentID());
                strSorterSorterJobStatusGetDRIn.setOriginalCassetteID(params.getWaferSorterSlotMapList().get(0).getOriginalCassetteID());
                strSorterSorterJobStatusGetDRIn.setDestinationCassetteID(params.getWaferSorterSlotMapList().get(0).getDestinationCassetteID());
                strSorterSorterJobStatusGetDRIn.setPortGroupID(params.getWaferSorterSlotMapList().get(0).getPortGroup());

                Outputs.SorterSorterJobStatusGetDROut strSorterSorterJobStatusGetDROut = sorterMethod.sorterSorterJobStatusGetDR(objCommon, strSorterSorterJobStatusGetDRIn);


                Params.SJStatusChgRptParams sjStatusChgRptParams = new Params.SJStatusChgRptParams();
                sjStatusChgRptParams.setEquipmentID(params.getEquipmentID());
                sjStatusChgRptParams.setPortGroup(params.getWaferSorterSlotMapList().get(0).getPortGroup());
                sjStatusChgRptParams.setOpeMemo(null);
                sjStatusChgRptParams.setJobID(strSorterSorterJobStatusGetDROut.getSorterComponentJobID());
                sjStatusChgRptParams.setJobType(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB);
                sjStatusChgRptParams.setJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR);

                this.sxSJStatusChgRpt(objCommon, sjStatusChgRptParams);
            }
        } else {
            //----------------------------------------
            //   CASE Action Code is Read OR MINI Read
            //----------------------------------------
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_READ)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_START)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_MINIREAD)) {
                log.info("params.getActionCode()() == SP_Sorter_Read or SP_Sorter_MiniRead");

                //0.0.3 Add Start
                Boolean ckeckSTATUS = true;
                Boolean bRecCompStat = false;
                Boolean bFosb = false;

                //---------------------------------
                // Check MM and TCS Srlot NO For MINI READ
                //---------------------------------
                if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_MINIREAD)) {
                    //----------------------------------------
                    //  TCS Reply Data -> Request Data Compare
                    //----------------------------------------
                    int nGetSlotMapDRLen = CimArrayUtils.getSize(strWaferSorterSlotMapSelectDROut);
                    Boolean bSlotNmCompStat = false;

                    if (nListLen == nGetSlotMapDRLen) {
                        //----------------------------------------
                        //  Data Compare SLOT NUMBER
                        //----------------------------------------
                        for (i = 0; i < nListLen; i++) {
                            bSlotNmCompStat = false;
                            log.info("//--------------------------------//");
                            log.info("//       DATA COMPARE START       //");
                            log.info("//--------------------------------//");
                            for (j = 0; j < nGetSlotMapDRLen; j++) {
                                //------------------------------------------
                                // Compare CassetteID,PortID,SlotNumber
                                // MM Request and TCS Reply
                                //------------------------------------------
                                log.info("nWaferMap Count = ", j);
                                log.info("(TCS) destinationCassetteID = {}", strWaferSorterSlotMapWrite.get(i).getDestinationCassetteID());
                                log.info("(SLOTMAP ) destinationCassetteID = {}", strWaferSorterSlotMapSelectDROut.get(j).getDestinationCassetteID());
                                log.info("(TCS) destinationPortID = {}", strWaferSorterSlotMapWrite.get(i).getDestinationPortID());
                                log.info("(SLOTMAP ) destinationPortID = {}", strWaferSorterSlotMapSelectDROut.get(j).getDestinationPortID());
                                log.info("(TCS) destinationSlotNumber = {}", strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber());
                                log.info("(SLOTMAP ) destinationSlotNumber = {}", strWaferSorterSlotMapSelectDROut.get(j).getDestinationSlotNumber());

                                if ((ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapWrite.get(i).getDestinationCassetteID(), strWaferSorterSlotMapSelectDROut.get(j).getDestinationCassetteID()))
                                        && (ObjectIdentifier.equals(strWaferSorterSlotMapWrite.get(i).getDestinationPortID(), strWaferSorterSlotMapSelectDROut.get(j).getDestinationPortID()))
                                        && (strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber().intValue() == strWaferSorterSlotMapSelectDROut.get(j).getDestinationSlotNumber().intValue())) {
                                    log.info("slotMapCompareStatus = OK(brank) ");
                                    strWaferSorterSlotMapWrite.get(i).setSlotMapCompareStatus("");

                                    log.info("bSlotNmCompStat = TRUE ");
                                    bSlotNmCompStat = true;
                                    break;
                                } else {
                                    log.info("slotMapCompareStatus = SP_Sorter_ERROR ");
                                    strWaferSorterSlotMapWrite.get(i).setSlotMapCompareStatus(BizConstant.SP_SORTER_ERROR);
                                }
                            }

                            log.info("//--------------------------------//");
                            log.info("//       DATA COMPARE END         //");
                            log.info("//--------------------------------//");
                            log.info("bSlotNmCompStat Check !!!");

                            //---------------------------------------
                            // Check Compare Status
                            //---------------------------------------
                            if (CimBooleanUtils.isFalse(bSlotNmCompStat)) {
                                log.info("Invalid Reply Data !!!");
                                log.info("bSlotNmCompStat ckeckSTATUS = false !!!");
                                ckeckSTATUS = false;    //0.0.6
                            }
                        }
                    } else {
                        ckeckSTATUS = false;    //0.0.
                    }
                }

                //---------------------------------
                //   Get a object length
                //---------------------------------
                List<Infos.WaferMapInCassetteInfo>  strCassette_GetWaferMapDR_out = new ArrayList<>();
                int nWaferMap = 0;    //0.0.1
                int nSlotMap = CimArrayUtils.getSize(strWaferSorterSlotMapWrite);

                log.info("WaferMap Count= {}", nWaferMap);
                log.info("SlotMap Count = {}", nSlotMap);

                ObjectIdentifier strTempCassetteID = null;    //0.0.1

                List<ObjectIdentifier> cassetteIDsForNotAvailable = new ArrayList<>();
                int notAvairableCassetteCnt = 0;

                for (i = 0; i < nSlotMap; i++) {
                    bRecCompStat = false;

                    strWaferSorterSlotMapWrite.get(i).setOmsCompareStatus(BizConstant.SP_SORTER_ERROR);

                    //---------------------------------------------------
                    // Check Cassette Change By Record
                    //---------------------------------------------------
                    log.info("Compare Cassette ID : strTempCassetteID =  {}", strTempCassetteID);
                    log.info("Compare Cassette ID : TCS Slotmap SEQ CassetteID =  {}", params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());

                    if (!ObjectIdentifier.equalsWithValue(strTempCassetteID, params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID())) {
                        //---------------------------------------------------
                        // Get Slot Map On MM Server (TCS Key)
                        //---------------------------------------------------
                        bFosb = false;           //P4000373
                        try{
                            strCassette_GetWaferMapDR_out = cassetteMethod.cassetteGetWaferMapDR(objCommon, params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                        }catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())){
                                log.info("cassette_GetWaferMapDR() == RC_NOT_FOUND_CASSETTE is FOSB");
                                bFosb = true;
                            } else {
                                throw e;
                            }
                        }

                        //---------------------------------------------------
                        // Store Changed Cassette ID and Sequence Length
                        //---------------------------------------------------
                        strTempCassetteID = params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID();
                        nWaferMap = CimArrayUtils.getSize(strCassette_GetWaferMapDR_out);

                        log.info("NEW strTempCassetteID = {}", strTempCassetteID);
                        log.info("NEW nWaferMap         = {}", nWaferMap);
                    }
                    //0.0.1 Add End

                    //--------------------------------
                    //   DATA COMPARE
                    //--------------------------------
                    Boolean bMMCastEmpty = true;    //0.0.3

                    log.info("//--------------------------------//");
                    log.info("//       DATA COMPARE START       //");
                    log.info("//--------------------------------//");

                    if (CimBooleanUtils.isFalse(bFosb)) {
                        for (j = 0; j < nWaferMap; j++) {
                            log.info("nWaferMap Count = {}", j);
                            log.info("(TCS) slotNumber : {}", strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber());
                            log.info("(MM ) slotNumber : {}", strCassette_GetWaferMapDR_out.get(j).getSlotNumber());
                            log.info("(TCS) waferID : {}", strWaferSorterSlotMapWrite.get(i).getWaferID());
                            log.info("(MM ) waferID : {}", strCassette_GetWaferMapDR_out.get(j).getWaferID());
                            log.info("//--------------------------------//");

                            //0.0.3 Add Start
                            //--------------------------------
                            // Case of Read Empty Carrier
                            //--------------------------------
                            if (0 == strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber()) {
                                if (!ObjectIdentifier.isEmpty(strCassette_GetWaferMapDR_out.get(j).getWaferID())) {
                                    bMMCastEmpty = false;
                                    break;
                                }
                            } else {
                                if ((strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber().intValue() == strCassette_GetWaferMapDR_out.get(j).getSlotNumber())) {
                                    if (ObjectIdentifier.isEmptyWithValue(strWaferSorterSlotMapWrite.get(i).getWaferID())||
                                            ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapWrite.get(i).getWaferID(), strCassette_GetWaferMapDR_out.get(j).getWaferID())){
                                        strWaferSorterSlotMapWrite.get(i).setWaferID(strCassette_GetWaferMapDR_out.get(j).getWaferID());
                                        if (CimStringUtils.equals(strWaferSorterSlotMapWrite.get(i).getAliasName(), strCassette_GetWaferMapDR_out.get(j).getAliasWaferName())){
                                            log.info("bRecCompStat = TRUE ");
                                            strWaferSorterSlotMapWrite.get(i).setOmsCompareStatus(BizConstant.SP_SORTER_OK);
                                            bRecCompStat = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        log.info("Cassette Is Fosb");
                        strWaferSorterSlotMapWrite.get(i).setOmsCompareStatus(BizConstant.SP_SORTER_OK);
                        bRecCompStat = true;
                    }
                    log.info("//--------------------------------//");
                    log.info("//       DATA COMPARE END         //");
                    log.info("//--------------------------------//");

                    //0.0.3 Add Start
                    //---------------------------------
                    // Enpty Casstte Compare
                    //---------------------------------
                    if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_READ)) {
                        if ((CimBooleanUtils.isTrue(bMMCastEmpty)) && (strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber() == 0)) {
                            strWaferSorterSlotMapWrite.get(i).setOmsCompareStatus(BizConstant.SP_SORTER_OK);
                            bRecCompStat = true;
                        }
                    } else if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_MINIREAD)) {
                        if ((CimBooleanUtils.isTrue(bMMCastEmpty)) && (nWaferMap == 0)) {
                            strWaferSorterSlotMapWrite.get(i).setOmsCompareStatus(BizConstant.SP_SORTER_OK);
                            bRecCompStat = true;
                        }
                    }else if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_START)){
                        if ((CimBooleanUtils.isTrue(bMMCastEmpty)) && (strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber() != 0)) {
                            strWaferSorterSlotMapWrite.get(i).setOmsCompareStatus(BizConstant.SP_SORTER_OK);
                            bRecCompStat = true;
                        }
                    }
                    //0.0.3 Add End

                    log.info("bRecCompStat Check !!!");
                    if (CimBooleanUtils.isFalse(bRecCompStat)) {
                        log.info("ckeckSTATUS = false !!!");
                        ckeckSTATUS = false;
                        notAvairableCassetteCnt++;
                        cassetteIDsForNotAvailable.add(params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                    }
                }

                //0.0.5 Add Start
                if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_READ)||
                        CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_START)) {
                    castLen = CimArrayUtils.getSize(tmpCassetteIDs);
                    int getWaferMapLen = 0;
                    ObjectIdentifier tmpWaferID;
                    String tmpAliasWaferName;
                    int k = 0;
                    for (i = 0; i < castLen; i++) {
                        log.info("Cassette Loop = {}", i);        //P6000242
                        //---------------------------------------------------
                        // Get Slot Map On MM Server (SlotMap Key)
                        //---------------------------------------------------
                        try{
                            strCassette_GetWaferMapDR_out = cassetteMethod.cassetteGetWaferMapDR(objCommon, tmpCassetteIDs.get(i));
                        }catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())) {
                                log.info("cassette_GetWaferMapDR() == RC_NOT_FOUND_CASSETTE is FOSB");
                                bFosb = true;
                            } else {
                                throw e;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFosb)) {
                            getWaferMapLen = CimArrayUtils.getSize(strCassette_GetWaferMapDR_out);
                            for (j = 0; j < getWaferMapLen; j++) {
                                bRecCompStat = false;
                                tmpWaferID = strCassette_GetWaferMapDR_out.get(j).getWaferID();
                                tmpAliasWaferName = strCassette_GetWaferMapDR_out.get(j).getAliasWaferName();
                                if (ObjectIdentifier.isEmpty(tmpWaferID)) {
                                    log.info("tmpWaferID is Blunk slotNumber = {}", j + 1);
                                    continue;
                                }

                                //---------------------------------------------------
                                // MM Exist Sequence VS TCS Repry Sequence
                                //---------------------------------------------------
                                for (k = 0; k < nSlotMap; k++) {
                                    if ((CimObjectUtils.equalsWithValue(tmpCassetteIDs.get(i), strWaferSorterSlotMapWrite.get(k).getDestinationCassetteID())
                                            && (strCassette_GetWaferMapDR_out.get(j).getSlotNumber().intValue() == strWaferSorterSlotMapWrite.get(k).getDestinationSlotNumber().intValue()))) {
                                        ObjectIdentifier waferID = strWaferSorterSlotMapWrite.get(k).getWaferID();
                                        if (ObjectIdentifier.isEmptyWithValue(waferID)|| ObjectIdentifier.equalsWithValue(tmpWaferID, waferID)){
                                            strWaferSorterSlotMapWrite.get(k).setWaferID(tmpWaferID);
                                            if (CimStringUtils.equals(strWaferSorterSlotMapWrite.get(k).getAliasName(),tmpAliasWaferName)){
                                                log.info("bRecCompStat = TRUE ");
                                                bRecCompStat = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (CimBooleanUtils.isFalse(bRecCompStat)) {
                                    log.info("ckeckSTATUS = false !!!");
                                    ckeckSTATUS = false;
                                    notAvairableCassetteCnt++;
                                    cassetteIDsForNotAvailable.add(tmpCassetteIDs.get(i));
                                }
                            }
                        } else {
                            log.info("Cassette Is Fosb");

                        }
                    }
                }

                //-------------------------------
                //  Insert TCS Data to SLOTMAP
                //-------------------------------
                waferMethod.waferSorterSlotMapInsertDR(objCommon, strWaferSorterSlotMapWrite);

                //----------------------------------------
                //  MAKE A CASSETTE IS NOT AVAILABLE
                //----------------------------------------
                if (CimBooleanUtils.isFalse(ckeckSTATUS)) {
                    //P4200131 Add Start
                    List<ObjectIdentifier> tmpNotAvailableCasts = new ArrayList<>();
                    int tmpNotAvailableCastsLen = 0;
                    //-------------------------------
                    // Checking Duplicate Cassette ID
                    //-------------------------------
                    log.info("Cassette Duplicate Check notAvairableCassetteCnt is : {}", notAvairableCassetteCnt);

                    for (i = 0; i < notAvairableCassetteCnt; i++) {
                        if (i == 0) {
                            tmpNotAvailableCasts.add(cassetteIDsForNotAvailable.get(i));
                            continue;
                        }
                        Boolean notAvailableCastsFind = false;
                        tmpNotAvailableCastsLen = CimArrayUtils.getSize(tmpNotAvailableCasts);
                        for (j = 0; j < tmpNotAvailableCastsLen; j++) {
                            if (ObjectIdentifier.equalsWithValue(tmpNotAvailableCasts.get(j), cassetteIDsForNotAvailable.get(i))) {
                                log.info("Find Duplicate Cassette.");
                                notAvailableCastsFind = true;
                                break;
                            }
                        }
                        if (!notAvailableCastsFind) {
                            tmpNotAvailableCasts.add(cassetteIDsForNotAvailable.get(i));
                        }
                    }

                    log.info("Final ! tmpNotAvailableCastsLen is : {}", tmpNotAvailableCastsLen);
                    //P4200131 Add End

                    for (i = 0; i < tmpNotAvailableCastsLen; i++) {
                        log.info("Makes Cassette NotAvailable :{}", tmpNotAvailableCasts.get(i));
                        Outputs.ObjCassetteStatusOut strCassette_getStatusDR_out = null;
                        try{
                            strCassette_getStatusDR_out = cassetteMethod.cassetteGetStatusDR(objCommon, tmpNotAvailableCasts.get(i));
                        }catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())) {
                                log.info("cassette_getStatusDR() == RC_NOT_FOUND_CASSETTE");
                                continue;
                            }
                            throw e;
                        }
                        log.info("strCassette_getStatusDR_out.drbl_state--->; {}", strCassette_getStatusDR_out.getDurableState());

                        if (CimStringUtils.equals(strCassette_getStatusDR_out.getDurableState(), CIMStateConst.CIM_DURABLE_NOTAVAILABLE)) {
                            log.info("Cassette Status == Not Available");
                            continue;
                        }

                        Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams=new Params.CarrierDetailInfoInqParams();
                        carrierDetailInfoInqParams.setCassetteID(tmpCassetteIDs.get(i));
                        carrierDetailInfoInqParams.setDurableOperationInfoFlag(false);
                        carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
                        carrierDetailInfoInqParams.setUser(objCommon.getUser());
                        Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = bankInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);
                        Infos.CassetteStatusInfo cassetteStatusInfo = carrierDetailInfoInqResult.getCassetteStatusInfo();

                        Params.MultiDurableStatusChangeReqParams multiDurableStatusChangeReqParams=new Params.MultiDurableStatusChangeReqParams();
                        Infos.MultiDurableStatusChangeReqInParm multiDurableStatusChangeReqInParm=new Infos.MultiDurableStatusChangeReqInParm();
                        multiDurableStatusChangeReqInParm.setDurableStatus(CIMStateConst.CIM_DURABLE_NOTAVAILABLE);
                        multiDurableStatusChangeReqInParm.setDurableSubStatus(ObjectIdentifier.buildWithValue(CIMFW_DURABLE_SUB_STATE_DEFAULT));
                        multiDurableStatusChangeReqInParm.setDurableCategory(SP_DURABLECAT_CASSETTE);
                        List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos=new ArrayList<>();
                        Infos.StatusChangeDurableInfo statusChangeDurableInfo=new Infos.StatusChangeDurableInfo();
                        statusChangeDurableInfo.setDurableID(tmpCassetteIDs.get(i));
                        statusChangeDurableInfo.setDurableStatus(cassetteStatusInfo.getCassetteStatus());
                        statusChangeDurableInfo.setDurableSubStatus(cassetteStatusInfo.getDurableSubStatus());
                        statusChangeDurableInfos.add(statusChangeDurableInfo);
                        multiDurableStatusChangeReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
                        multiDurableStatusChangeReqParams.setParm(multiDurableStatusChangeReqInParm);
                        params.setUser(objCommon.getUser());
                        params.setClaimMemo(params.getClaimMemo());
                        durableService.sxMultiDurableStatusChangeReq(objCommon,multiDurableStatusChangeReqParams);
                    }
                    if (!CimStringUtils.equals(BizConstant.SP_SORTER_START,params.getActionCode())){
                        rcParamChk = retCodeConfigEx.getWafersorterSlotmapCompareError();
                    }
                }
            } else {
                //----------------------------------------------------------------------
                //   CASE Action Code is PosChange, JustIn, JustOut, Scrap ,AdjustToMM
                //----------------------------------------------------------------------
                log.info("params.getActionCode()() != SP_Sorter_Read or SP_Sorter_MiniRead");

                //----------------------------------------
                //  TCS Reply Data -> Request Data Compare
                //----------------------------------------
                int nSlotMapWriteLen = CimArrayUtils.getSize(strWaferSorterSlotMapWrite);
                int nGetSlotMapDRLen = CimArrayUtils.getSize(strWaferSorterSlotMapSelectDROut);

                List<Infos.WaferSorterSlotMap> tmpSlotSeq = strWaferSorterSlotMapSelectDROut;

                Boolean ckeckSTATUS = true;
                Boolean bRecCompStat = false;

                if (nSlotMapWriteLen != nGetSlotMapDRLen) {
                    ckeckSTATUS = false;
                } else {
                    //----------------------------------------
                    //  Data Compare
                    //----------------------------------------
                    for (i = 0; i < nSlotMapWriteLen; i++) {
                        bRecCompStat = false;

                        strWaferSorterSlotMapWrite.get(i).setOmsCompareStatus("");
                        strWaferSorterSlotMapWrite.get(i).setSlotMapCompareStatus(BizConstant.SP_SORTER_ERROR);

                        for (j = 0; j < nGetSlotMapDRLen; j++) {
                            if ((ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapWrite.get(i).getWaferID(), tmpSlotSeq.get(j).getWaferID()))
                                    && (ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapWrite.get(i).getDestinationCassetteID(), tmpSlotSeq.get(j).getDestinationCassetteID()))
                                    && (ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapWrite.get(i).getDestinationPortID(), tmpSlotSeq.get(j).getDestinationPortID()))
                                    && (strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber().intValue() == tmpSlotSeq.get(j).getDestinationSlotNumber().intValue())
                                    && (ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapWrite.get(i).getOriginalCassetteID(), tmpSlotSeq.get(j).getOriginalCassetteID()))
                                    && (ObjectIdentifier.equalsWithValue(strWaferSorterSlotMapWrite.get(i).getOriginalPortID(), tmpSlotSeq.get(j).getOriginalPortID()))
                                    && (strWaferSorterSlotMapWrite.get(i).getOriginalSlotNumber().intValue() == tmpSlotSeq.get(j).getOriginalSlotNumber().intValue())) {
                                log.info("bRecCompStat = TRUE ");
                                strWaferSorterSlotMapWrite.get(i).setSlotMapCompareStatus(BizConstant.SP_SORTER_OK);
                                bRecCompStat = true;
                                break;
                            }
                        }

                        log.info("bRecCompStat Check !!!");

                        if (CimBooleanUtils.isFalse(bRecCompStat)) {
                            log.info("ckeckSTATUS = false !!!");
                            ckeckSTATUS = false;
                        }
                    }
                }

                //----------------------------------------
                //  MKAE A CASSETTE NOT AVAILABLE
                //----------------------------------------
                if (CimBooleanUtils.isFalse(ckeckSTATUS)) {
                    castLen = CimArrayUtils.getSize(tmpCassetteIDs);
                    for (i = 0; i < castLen; i++) {
                        Outputs.ObjCassetteStatusOut strCassette_getStatusDR_out = null;
                        try{
                            strCassette_getStatusDR_out = cassetteMethod.cassetteGetStatusDR(objCommon, tmpCassetteIDs.get(i));
                        }catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                                log.info("cassette_getStatusDR() == RC_NOT_FOUND_CASSETTE");
                                continue;
                            }
                            throw e;
                        }
                        log.info("strCassette_getStatusDR_out.drbl_state---> : {}", strCassette_getStatusDR_out.getDurableState());

                        if (CimStringUtils.equals(strCassette_getStatusDR_out.getDurableState(), CIMStateConst.CIM_DURABLE_NOTAVAILABLE)) {
                            log.info("Cassette Status == Not Available");
                            continue;
                        }
                        try {
                            Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams=new Params.CarrierDetailInfoInqParams();
                            carrierDetailInfoInqParams.setCassetteID(tmpCassetteIDs.get(i));
                            carrierDetailInfoInqParams.setDurableOperationInfoFlag(false);
                            carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
                            carrierDetailInfoInqParams.setUser(objCommon.getUser());
                            Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = bankInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);
                            Infos.CassetteStatusInfo cassetteStatusInfo = carrierDetailInfoInqResult.getCassetteStatusInfo();

                            Params.MultiDurableStatusChangeReqParams multiDurableStatusChangeReqParams=new Params.MultiDurableStatusChangeReqParams();
                            Infos.MultiDurableStatusChangeReqInParm multiDurableStatusChangeReqInParm=new Infos.MultiDurableStatusChangeReqInParm();
                            multiDurableStatusChangeReqInParm.setDurableStatus(CIMStateConst.CIM_DURABLE_NOTAVAILABLE);
                            multiDurableStatusChangeReqInParm.setDurableSubStatus(ObjectIdentifier.buildWithValue(CIMFW_DURABLE_SUB_STATE_DEFAULT));
                            multiDurableStatusChangeReqInParm.setDurableCategory(SP_DURABLECAT_CASSETTE);
                            List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos=new ArrayList<>();
                            Infos.StatusChangeDurableInfo statusChangeDurableInfo=new Infos.StatusChangeDurableInfo();
                            statusChangeDurableInfo.setDurableID(tmpCassetteIDs.get(i));
                            statusChangeDurableInfo.setDurableStatus(cassetteStatusInfo.getCassetteStatus());
                            statusChangeDurableInfo.setDurableSubStatus(cassetteStatusInfo.getDurableSubStatus());
                            statusChangeDurableInfos.add(statusChangeDurableInfo);
                            multiDurableStatusChangeReqInParm.setStatusChangeDurableInfos(statusChangeDurableInfos);
                            multiDurableStatusChangeReqParams.setParm(multiDurableStatusChangeReqInParm);
                            params.setUser(objCommon.getUser());
                            params.setClaimMemo(params.getClaimMemo());
                            durableService.sxMultiDurableStatusChangeReq(objCommon,multiDurableStatusChangeReqParams);
                        } catch (ServiceException e) {
                            if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())){
                                throw e;
                            }
                        }
                    }

                    rcParamChk = retCodeConfigEx.getWafersorterSlotmapCompareError();
                }

                //---------------------------------------------------------
                //  The one with the relation of Wafer and Carrier is cut.
                //---------------------------------------------------------
                if (CimBooleanUtils.isTrue(ckeckSTATUS)) {
                    Boolean bMMKnownCastFlg = false;    //D4000220
                    if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_SCRAP)) {
                        //------------------------------------------------------------------------------------------------------
                        //  MM doesn't go with Scrap through destinationCassetteID to cut the one with the relation if it knows.
                        //------------------------------------------------------------------------------------------------------
                        int len = CimArrayUtils.getSize(params.getWaferSorterSlotMapList());
                        int seqlen = 0;

                        List<Infos.WaferSorterSlotMap> strMMKnownSeq = new ArrayList<>();
                        Infos.WaferSorterSlotMap waferSorterSlotMap = null;


                        List<ObjectIdentifier> tmpOriginalCasstteIDs = new ArrayList<>();
                        int tmpOriginalCasstteIDLen = 0;

                        for (i = 0; i < len; i++) {
                            Outputs.ObjCassetteStatusOut cassetteStatusDR = null;
                            try{
                                cassetteStatusDR = cassetteMethod.cassetteGetStatusDR(objCommon, params.getWaferSorterSlotMapList().get(i).getDestinationCassetteID());
                                strMMKnownSeq.add(params.getWaferSorterSlotMapList().get(i));
                                bMMKnownCastFlg = true;
                            }catch (ServiceException e) {
                                if (Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())) {
                                    waferSorterSlotMap = params.getWaferSorterSlotMapList().get(i);
                                    bMMKnownCastFlg = false;
                                } else {
                                    throw e;
                                }
                            }

                            if (CimBooleanUtils.isFalse(bMMKnownCastFlg)) {
                                log.info("params.getActionCode() is Scrap");
                                List<Infos.WaferSorterSlotMap> strMMUnKnownSeq = new ArrayList<>();
                                strMMUnKnownSeq.add(waferSorterSlotMap);
                                waferMethod.waferSorterLotMaterialsScrap(objCommon, strMMUnKnownSeq);

                                //----------------------------------------------------
                                // Collect Original Cassette ID For MultiLotTypeUpdate
                                //----------------------------------------------------
                                log.info("MultiLotTypeUpdate Caaseete is : {}", params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID());

                                if (tmpOriginalCasstteIDLen == 0) {
                                    tmpOriginalCasstteIDs.add(params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID());
                                } else {
                                    int i_cas = 0;
                                    Boolean bSameCas = false;

                                    for (i_cas = 0; i_cas < tmpOriginalCasstteIDLen; i_cas++) {
                                        if (ObjectIdentifier.equalsWithValue(tmpOriginalCasstteIDs.get(i_cas), params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID())) {
                                            log.info("Find same cassette. BREAK!");
                                            bSameCas = true;
                                            break;
                                        }
                                    }
                                    if (CimBooleanUtils.isFalse(bSameCas)) {
                                        tmpOriginalCasstteIDs.add(params.getWaferSorterSlotMapList().get(i).getOriginalCassetteID());
                                    }
                                }
                                log.info("Collected Cassette Cout is : {}", tmpOriginalCasstteIDLen);
                            }
                        }
                        //--------------------------------------------
                        // Multi Lot Type Update For Collected Casstte
                        //--------------------------------------------
                        int i_cas = 0;

                        for (i_cas = 0; i_cas < tmpOriginalCasstteIDLen; i_cas++) {
                            try {
                                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, tmpOriginalCasstteIDs.get(i_cas));
                            } catch (ServiceException e){
                                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())){
                                    throw e;
                                }
                            }
                        }

                        len = CimArrayUtils.getSize(strMMKnownSeq);
                        if (len > 0) {
                            bMMKnownCastFlg = true;
                            strWaferSorterSlotMapWrite = strMMKnownSeq;
                        }
                    }

                    //-------------------------------------------------------------------------
                    //  It doesn't need to update data on MM in the case of AdjustToMM.
                    //-------------------------------------------------------------------------
                    if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_ADJUSTTOMM)) {
                        log.info("params.getActionCode() is AdjustToMM");
                        //---------------------------------------
                        // update the data base of OSSLOTMAP
                        //---------------------------------------
                        log.info("Calling Method:waferSorter_slotMap_WaferIdRead_UpdateDR");
                        waferMethod.waferSorterSlotMapWaferIdReadUpdateDR(objCommon, params.getWaferSorterSlotMapList());
                    }

                    //-------------------------------------------------------------------------
                    //  It doesn't need to update data on MM in the case of AdjustToSorter.
                    //-------------------------------------------------------------------------
                    if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_ADJUSTTOSORTER)) {
                        log.info("params.getActionCode() is AdjustToSorter");
                    }

                    //-------------------------------------------------------------------------
                    //  Other Action Code (JustIn JustOut PositionChange
                    //-------------------------------------------------------------------------
                    if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_JUSTIN)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_JUSTOUT)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFERENDOFF)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_POSITIONCHANGE)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFEROFF)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_SEPARATEOFF)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_COMBINEOFF)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)
                            || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFEREND)
                            || CimBooleanUtils.isTrue(bMMKnownCastFlg)) {
                        log.info("params.getActionCode() is JustIn,JustOut,PosChange");
                        //---------------------------
                        //  Set Slot Map Data For Update
                        //---------------------------
                        List<Infos.WaferTransfer> strWaferXferSeq = new ArrayList<>();
                        int nLen = CimArrayUtils.getSize(strWaferSorterSlotMapWrite);
                        for (i = 0; i < nLen; i++) {
                            Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
                            waferTransfer.setWaferID(strWaferSorterSlotMapWrite.get(i).getWaferID());
                            waferTransfer.setDestinationCassetteID(strWaferSorterSlotMapWrite.get(i).getDestinationCassetteID());
                            waferTransfer.setBDestinationCassetteManagedByOM(strWaferSorterSlotMapWrite.get(i).getDestinationCassetteManagedByOM());
                            waferTransfer.setDestinationSlotNumber(strWaferSorterSlotMapWrite.get(i).getDestinationSlotNumber().intValue());
                            waferTransfer.setOriginalCassetteID(strWaferSorterSlotMapWrite.get(i).getOriginalCassetteID());
                            waferTransfer.setBOriginalCassetteManagedByOM(strWaferSorterSlotMapWrite.get(i).getOriginalCassetteManagedByOM());
                            waferTransfer.setOriginalSlotNumber(strWaferSorterSlotMapWrite.get(i).getOriginalSlotNumber().intValue());
                            strWaferXferSeq.add(waferTransfer);
                        }

                        //---------------------------
                        //  Slot Map Update
                        //---------------------------
                        Params.WaferSlotmapChangeRptParams waferSlotmapChangeRptParams = new Params.WaferSlotmapChangeRptParams();
                        waferSlotmapChangeRptParams.setEquipmentID(params.getEquipmentID());
                        waferSlotmapChangeRptParams.setStrWaferXferSeq(strWaferXferSeq);
                        sxWaferSlotmapChangeRpt(objCommon, waferSlotmapChangeRptParams);

                        /*******************************************************/
                        /* Adjust equipment lot info by actual cassette info.  */
                        /*******************************************************/
                        for (int castCnt = 0; castCnt < CimArrayUtils.getSize(cassetteIDs); castCnt++) {
                            equipmentMethod.equipmentLotInCassetteAdjust(objCommon, params.getEquipmentID(), cassetteIDs.get(castCnt));
                        }
                    }
                    waferMethod.waferSorterSlotMapInsertDR(objCommon, strWaferSorterSlotMapWrite);
                }

                //--------------------------------------------------------------
                //  Auto Sorting
                //--------------------------------------------------------------
                if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_AUTOSORTING)||
                        CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_LOTTRANSFER)||
                        CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SORTER_WAFEREND)) {
                    log.info("params.getActionCode() == SP_Sorter_AutoSorting");

                    if (Validations.isEquals(rcParamChk, retCodeConfigEx.getWafersorterSlotmapCompareError())) {
                        //--------------------------------------------------------------------
                        //  Sorter Job Status Update (Executing -> Errored)
                        //--------------------------------------------------------------------
                        log.info("params.getActionCode() == SP_Sorter_AutoSorting");


                        Inputs.SorterSorterJobStatusGetDRIn strSorterSorterJobStatusGetDRIn = new Inputs.SorterSorterJobStatusGetDRIn();
                        strSorterSorterJobStatusGetDRIn.setEquipmentID(params.getEquipmentID());
                        strSorterSorterJobStatusGetDRIn.setOriginalCassetteID(params.getWaferSorterSlotMapList().get(0).getOriginalCassetteID());
                        strSorterSorterJobStatusGetDRIn.setDestinationCassetteID(params.getWaferSorterSlotMapList().get(0).getDestinationCassetteID());
                        strSorterSorterJobStatusGetDRIn.setPortGroupID(params.getWaferSorterSlotMapList().get(0).getPortGroup());

                        Outputs.SorterSorterJobStatusGetDROut strSorterSorterJobStatusGetDROut = sorterMethod.sorterSorterJobStatusGetDR(objCommon, strSorterSorterJobStatusGetDRIn);

                        Params.SJStatusChgRptParams sjStatusChgRptParams = new Params.SJStatusChgRptParams();
                        sjStatusChgRptParams.setEquipmentID(params.getEquipmentID());
                        sjStatusChgRptParams.setPortGroup(params.getWaferSorterSlotMapList().get(0).getPortGroup());
                        sjStatusChgRptParams.setJobID(strSorterSorterJobStatusGetDROut.getSorterComponentJobID());
                        sjStatusChgRptParams.setJobType(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB);
                        sjStatusChgRptParams.setJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR);
                        this.sxSJStatusChgRpt(objCommon, sjStatusChgRptParams);
                    } else {
                        //----------------------------------------------------------
                        //  Sorter Job Status Update (Update -> Completed)
                        //----------------------------------------------------------

                        Inputs.SorterSorterJobStatusGetDRIn strSorterSorterJobStatusGetDRIn = new Inputs.SorterSorterJobStatusGetDRIn();
                        strSorterSorterJobStatusGetDRIn.setEquipmentID(params.getEquipmentID());
                        strSorterSorterJobStatusGetDRIn.setOriginalCassetteID(params.getWaferSorterSlotMapList().get(0).getOriginalCassetteID());
                        strSorterSorterJobStatusGetDRIn.setDestinationCassetteID(params.getWaferSorterSlotMapList().get(0).getDestinationCassetteID());
                        strSorterSorterJobStatusGetDRIn.setPortGroupID(params.getWaferSorterSlotMapList().get(0).getPortGroup());

                        Outputs.SorterSorterJobStatusGetDROut strSorterSorterJobStatusGetDROut = sorterMethod.sorterSorterJobStatusGetDR(objCommon, strSorterSorterJobStatusGetDRIn);

                        if (ObjectIdentifier.isEmpty(strSorterSorterJobStatusGetDROut.getSorterComponentJobID())) {
                            throw new ServiceException(retCodeConfigEx.getNotFoundSorterjob());
                        }

                        Params.SJStatusChgRptParams sjStatusChgRptParams = new Params.SJStatusChgRptParams();
                        sjStatusChgRptParams.setEquipmentID(params.getEquipmentID());
                        sjStatusChgRptParams.setPortGroup(strSorterSorterJobStatusGetDRIn.getPortGroupID());
                        sjStatusChgRptParams.setJobID(strSorterSorterJobStatusGetDROut.getSorterComponentJobID());
                        sjStatusChgRptParams.setJobType(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB);
                        sjStatusChgRptParams.setJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED);
                        this.sxSJStatusChgRpt(objCommon, sjStatusChgRptParams);

                        //----------------------------------------------------------
                        //  Delete soter job, if Sorter Job Status is all compeleted
                        //----------------------------------------------------------
                        Inputs.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Inputs.ObjSorterJobListGetDRIn();
                        objSorterJobListGetDRIn.setEquipmentID(params.getEquipmentID());
                        List<Infos.SortJobListAttributes>  strSorter_jobList_GetDR_out = sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);

                        int srtJobLen = CimArrayUtils.getSize(strSorter_jobList_GetDR_out);

                        if (srtJobLen > 0) {
                            log.info("sorter job status check.");

                            int compStatusLen = 0;
                            int compoJobLen = CimArrayUtils.getSize(strSorter_jobList_GetDR_out.get(0).getSorterComponentJobListAttributesList());
                            for (int iCJLen = 0; iCJLen < compoJobLen; iCJLen++) {
                                if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED, strSorter_jobList_GetDR_out.get(0).getSorterComponentJobListAttributesList().get(iCJLen).getComponentSorterJobStatus())) {
                                    log.info("sorter job status is 'Completed'.");
                                    compStatusLen++;
                                } else {
                                    log.info("sorter job status is not 'Completed'.");
                                    break;
                                }
                            }

                            //----------------------------------------------------------
                            //  Delete sorter job
                            //----------------------------------------------------------
                            if (compStatusLen == compoJobLen) {
                                log.info("compStatusLen == compoJobLen");
                                Params.SJStatusChgRptParams sjStatusChgRptParamsTmp = new Params.SJStatusChgRptParams();
                                sjStatusChgRptParamsTmp.setEquipmentID(params.getEquipmentID());
                                sjStatusChgRptParamsTmp.setPortGroup(strSorterSorterJobStatusGetDRIn.getPortGroupID());
                                sjStatusChgRptParamsTmp.setJobID(strSorterSorterJobStatusGetDROut.getSorterJobID());
                                sjStatusChgRptParamsTmp.setJobType(BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB);
                                sjStatusChgRptParamsTmp.setJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED);
                                sjStatusChgRptParamsTmp.setSorterJobCategory(strSorter_jobList_GetDR_out.get(0).getSorterJobCategory());
                                this.sxSJStatusChgRpt(objCommon, sjStatusChgRptParamsTmp);

                                Params.SJCancelReqParm sjCancelReqParm = new Params.SJCancelReqParm();
                                sjCancelReqParm.setSorterJobID(strSorterSorterJobStatusGetDROut.getSorterJobID());
                                sjCancelReqParm.setNotifyToTCSFlag(true);
                                this.sxSJCancelReq(objCommon, sjCancelReqParm);

                                // online sorter 增加passThu
                                if (CimStringUtils.equals(params.getActionCode(),BizConstant.SP_SORTER_LOTTRANSFER)) {
                                    // 检验参数
                                    List<Infos.WaferSorterSlotMap> waferSorterSlotMapList = params.getWaferSorterSlotMapList();
                                    Validations.check(CimArrayUtils.isEmpty(waferSorterSlotMapList),retCodeConfig.getInvalidParameter());

                                    List<Infos.GatePassLotInfo> strGatePassLotInfo=new ArrayList<>();
                                    Set<String> lotList=new HashSet<>();
                                    waferSorterSlotMapList.forEach(waferSorterSlotMap -> {
                                        ObjectIdentifier lotID = waferSorterSlotMap.getLotID();
                                        if (ObjectIdentifier.isEmptyWithValue(lotID)||lotList.contains(ObjectIdentifier.fetchValue(lotID))){
                                            return;
                                        }
                                        lotList.add(ObjectIdentifier.fetchValue(lotID));
                                        Infos.GatePassLotInfo gatePassLotInfo=new Infos.GatePassLotInfo();
                                        strGatePassLotInfo.add(gatePassLotInfo);
                                        gatePassLotInfo.setLotID(lotID);

                                        Params.LotInfoInqParams lotInfoInqParams=new Params.LotInfoInqParams();
                                        lotInfoInqParams.setLotIDs(CimArrayUtils.generateList(lotID));

                                        Infos.LotInfoInqFlag lotInfoInqFlag=new Infos.LotInfoInqFlag();
                                        lotInfoInqFlag.setLotBasicInfoFlag(false);
                                        lotInfoInqFlag.setLotControlUseInfoFlag(false);
                                        lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
                                        lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
                                        lotInfoInqFlag.setLotOperationInfoFlag(true);
                                        lotInfoInqFlag.setLotOrderInfoFlag(false);
                                        lotInfoInqFlag.setLotControlJobInfoFlag(false);
                                        lotInfoInqFlag.setLotProductInfoFlag(false);
                                        lotInfoInqFlag.setLotRecipeInfoFlag(false);
                                        lotInfoInqFlag.setLotLocationInfoFlag(false);
                                        lotInfoInqFlag.setLotWipOperationInfoFlag(false);
                                        lotInfoInqFlag.setLotWaferAttributesFlag(false);
                                        lotInfoInqFlag.setLotBackupInfoFlag(false);
                                        lotInfoInqFlag.setLotListInCassetteInfoFlag(false);
                                        lotInfoInqFlag.setLotWaferMapInCassetteInfoFlag(false);
                                        lotInfoInqParams.setLotInfoInqFlag(lotInfoInqFlag);

                                        Results.LotInfoInqResult lotInfoInqResult = lotInqService.sxLotInfoInq(objCommon, lotInfoInqParams);

                                        Validations.check(lotInfoInqResult==null,retCodeConfig.getInvalidParameter());
                                        List<Infos.LotInfo> lotInfoList = lotInfoInqResult.getLotInfoList();
                                        Validations.check(CimArrayUtils.isEmpty(lotInfoList),retCodeConfig.getInvalidParameter());
                                        Infos.LotOperationInfo lotOperationInfo = lotInfoList.get(0).getLotOperationInfo();

                                        gatePassLotInfo.setCurrentOperationNumber(lotOperationInfo.getOperationNumber());
                                        gatePassLotInfo.setCurrentRouteID(lotOperationInfo.getRouteID());
                                    });

                                    String claimMemo = params.getClaimMemo();
                                    for (Infos.GatePassLotInfo gatePassLotInfo : strGatePassLotInfo) {
                                        lotService.sxPassThruReq(objCommon, gatePassLotInfo, claimMemo);
                                    }
                                }
                            }
                        } else {
                            log.info("sorter job information is invalid.");
                            Validations.check(retCodeConfigEx.getInvalidSorterJobid(), strSorterSorterJobStatusGetDROut.getSorterJobID());
                        }
                    }
                }
            }
        }

        ObjectIdentifier lotID = null;
        if (CimStringUtils.equals(BizConstant.SP_SORTER_START,params.getActionCode())){

            waferMethod.waferSorterSlotMapSTInfoDeleteDR(objCommon,params);

            Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams=params.getMaterialReceiveAndPrepareReqParams();

            sxOnlineSorterSlotmapCompareReq(objCommon,materialReceiveAndPrepareReqParams.getPortGroup(),params.getEquipmentID(),params.getActionCode());

            bankService.sxMaterialReceiveAndPrepareReq(materialReceiveAndPrepareReqParams,objCommon);

            ObjectIdentifier cassetteID=materialReceiveAndPrepareReqParams.getCassetteID();
            Results.LotListByCarrierInqResult lotListByCarrierInqResult = lotInqService.sxLotListByCarrierInq(objCommon, cassetteID);

            ObjectIdentifier productRequestID = params.getProductRequestID();
            Infos.NewLotAttributes newLotAttributes=new Infos.NewLotAttributes();
            newLotAttributes.setCassetteID(cassetteID);
            List<Infos.NewWaferAttributes> newWaferAttributesList=new ArrayList<>();
            newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);

            Params.OnlineSorterActionStatusInqParm onlineSorterActionStatusInqParm=new Params.OnlineSorterActionStatusInqParm();
            onlineSorterActionStatusInqParm.setEquipmentID(params.getEquipmentID());
            onlineSorterActionStatusInqParm.setPortGroup(materialReceiveAndPrepareReqParams.getPortGroup());
            onlineSorterActionStatusInqParm.setRequiredData(BizConstant.SP_SORTER_SLOTMAP_ALLDATA);
            List<Infos.WaferSorterSlotMap> waferSorterSlotMaps = sortInqService.sxOnlineSorterActionStatusInq(objCommon, onlineSorterActionStatusInqParm);
            Validations.check(CimArrayUtils.isEmpty(waferSorterSlotMaps),retCodeConfig.getInvalidParameter());

            Optional.ofNullable(lotListByCarrierInqResult.getWaferMapInCassetteInfoList()).ifPresent(waferMapInCassetteInfos -> waferMapInCassetteInfos.forEach(waferMapInCassetteInfo -> {
                if (ObjectIdentifier.isEmptyWithValue(waferMapInCassetteInfo.getLotID())){
                    return;
                }
                Infos.NewWaferAttributes newWaferAttributes=new Infos.NewWaferAttributes();
                newWaferAttributesList.add(newWaferAttributes);
                newWaferAttributes.setNewLotID(productRequestID);
                Integer slotNumber = waferMapInCassetteInfo.getSlotNumber();
                newWaferAttributes.setNewSlotNumber(slotNumber);
                newWaferAttributes.setSourceLotID(waferMapInCassetteInfo.getLotID());
                newWaferAttributes.setSourceWaferID(waferMapInCassetteInfo.getWaferID());
            }));

            Results.WaferLotStartReqResult waferLotStartReqResult = lotStartService.sxWaferLotStartReq(objCommon, productRequestID, newLotAttributes, params.getClaimMemo());

            lotID = waferLotStartReqResult.getLotID();

            Params.WaferAliasSetReqParams waferAliasSetReqParams = new Params.WaferAliasSetReqParams();
            waferAliasSetReqParams.setLotID(lotID);
            waferAliasSetReqParams.setClaimMemo(params.getClaimMemo());
            List<Infos.AliasWaferName> aliasWaferNames=new ArrayList<>();
            waferAliasSetReqParams.setAliasWaferNames(aliasWaferNames);

            lotListByCarrierInqResult = lotInqService.sxLotListByCarrierInq(objCommon, cassetteID);

            Optional.ofNullable(lotListByCarrierInqResult.getWaferMapInCassetteInfoList()).ifPresent(waferMapInCassetteInfos -> waferMapInCassetteInfos.forEach(waferMapInCassetteInfo -> {
                if (ObjectIdentifier.isEmptyWithValue(waferMapInCassetteInfo.getLotID())){
                    return;
                }
                Integer slotNumber = waferMapInCassetteInfo.getSlotNumber();
                Infos.AliasWaferName aliasWaferName=new Infos.AliasWaferName();
                aliasWaferNames.add(aliasWaferName);
                aliasWaferName.setWaferID(waferMapInCassetteInfo.getWaferID());

                String aliasName=null;
                for (Infos.WaferSorterSlotMap waferSorterSlotMap : waferSorterSlotMaps) {
                    if (CimNumberUtils.intValue(slotNumber)== CimNumberUtils.intValue(waferSorterSlotMap.getDestinationSlotNumber())&&
                            CimStringUtils.equals(waferSorterSlotMap.getActionCode(),BizConstant.SP_SORTER_START)&&
                            CimStringUtils.equals(waferSorterSlotMap.getDirection(),BizConstant.SP_TCS_PERSON)){
                        aliasName=waferSorterSlotMap.getAliasName();
                        break;
                    }
                }
                aliasWaferName.setAliasWaferName(aliasName);
            }));

            lotStartService.sxWaferAliasSetReq(objCommon, waferAliasSetReqParams);
        }

        // eap有错误码,执行对应hold
        Integer eapErrorCode = params.getEapErrorCode();
        if (eapErrorCode==null||eapErrorCode==0){
            /*----------------------------*/
            /*       Return to Caller     */
            /*----------------------------*/
            if (!Validations.isEquals(rcParamChk, retCodeConfig.getSucc())) {
                throw new ServiceException(rcParamChk);
            }
            return;
        }

        Set<String> lotList=new HashSet<>();

        // 必须存在lot才能hold.
        if (ObjectIdentifier.isEmptyWithValue(lotID)){
            Optional.ofNullable(params.getWaferSorterSlotMapList()).ifPresent(waferSorterSlotMaps -> {
                waferSorterSlotMaps.forEach(waferSorterSlotMap -> {
                    String value = ObjectIdentifier.fetchValue(waferSorterSlotMap.getLotID());
                    if (CimStringUtils.isNotEmpty(value))
                    lotList.add(ObjectIdentifier.fetchValue(waferSorterSlotMap.getLotID()));
                });
            });
            Validations.check(lotList.isEmpty(),retCodeConfig.getInvalidParameter());
        }else {
            lotList.add(ObjectIdentifier.fetchValue(lotID));
        }

        lotList.forEach(lot->{
            ObjectIdentifier holdLotID=ObjectIdentifier.buildWithValue(lot);
            Params.LotInfoInqParams lotInfoInqParams=new Params.LotInfoInqParams();
            lotInfoInqParams.setLotIDs(CimArrayUtils.generateList(holdLotID));
            Infos.LotInfoInqFlag lotInfoInqFlag=new Infos.LotInfoInqFlag();
            lotInfoInqFlag.setLotOperationInfoFlag(true);
            lotInfoInqParams.setLotInfoInqFlag(lotInfoInqFlag);
            Results.LotInfoInqResult lotInfoInqResult = lotInqService.sxLotInfoInq(objCommon, lotInfoInqParams);
            Validations.check(lotInfoInqResult==null|| CimArrayUtils.getSize(lotInfoInqResult.getLotInfoList())!=1,retCodeConfig.getInvalidParameter());

            Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();

            Infos.LotHoldReq lotHoldReq=new Infos.LotHoldReq();
            lotHoldReq.setClaimMemo(params.getClaimMemo());
            lotHoldReq.setHoldType(BizConstant.SP_REASONCAT_LOTHOLD);

            List<Infos.ReasonCodeAttributes> reasonCodeAttributes = systemInqService.sxReasonCodeListByCategoryInq(objCommon, BizConstant.CATEGORY_HOLD_LOT);
            Validations.check(CimArrayUtils.isEmpty(reasonCodeAttributes),retCodeConfig.getNotFoundSystemObj());

            String reasonCode=null;
            switch (eapErrorCode){
                case 1:
                    reasonCode=BizConstant.SP_REASON_WAITINGTRANSFERERRORONHOLD;
                    break;
                case 2:
                    reasonCode=BizConstant.SP_REASON_WAITINGTRANSFERFAILONHOLD;
                    lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_WAITINGTRANSFERFAILONHOLD));
                    break;
                case 3:
                    reasonCode=BizConstant.SP_REASON_WAITINGTRANSFERPARTIALFAILONHOLD;
                    break;
            }
            String finalReasonCode = reasonCode;
            List<ObjectIdentifier> reasonCodes = reasonCodeAttributes.stream().filter(
                    reasonCodeAttribute -> CimStringUtils.equals(reasonCodeAttribute.getReasonCodeID(), finalReasonCode)).map(
                    reasonCodeAttribute -> reasonCodeAttribute.getReasonCodeID()).collect(Collectors.toList());
            Validations.check(CimArrayUtils.isEmpty(reasonCodes),retCodeConfig.getNotFoundSystemObj());

            lotHoldReq.setHoldReasonCodeID(reasonCodes.get(0));

            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setOperationNumber(lotOperationInfo.getOperationNumber());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(lotOperationInfo.getRouteID());
            List<Infos.LotHoldReq> holdReqList=new ArrayList<>();
            holdReqList.add(lotHoldReq);
            lotService.sxHoldLotReq(objCommon,holdLotID,holdReqList);
        });

        /*----------------------------*/
        /*       Return to Caller     */
        /*----------------------------*/
        if (!Validations.isEquals(rcParamChk, retCodeConfig.getSucc())) {
            throw new ServiceException(rcParamChk);
        }
    }

    public void sxOnlineSorterSlotmapAdjustReq(Infos.ObjCommon objCommon, Params.OnlineSorterSlotmapAdjustReqParam params){

        //init
        String actionCode = params.getActionCode();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapSequence = params.getStrWaferSorterSlotMapSequence();
        String portGroup = params.getPortGroup();
        String physicalRecipeID = params.getPhysicalRecipeID();
        List<Infos.WaferTransfer> strWaferXferSeq = params.getStrWaferXferSeq();
        String adjustDirection = params.getAdjustDirection();
        String claimMemo = params.getClaimMemo();
        User user = params.getUser();

        // Get required equipment lock mode
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
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

        }

        //================================
        // Check equipment availability
        //================================
        //【step3】 - equipment_CheckAvail
        equipmentMethod.equipmentCheckAvail(objCommon, equipmentID);

        //================================================================
        // Checking Action one's personal history (Look for WaferIDRead)
        //================================================================
        //【step4】 - waferSorter_CheckCondition_AfterWaferIdReadDR
        waferMethod.waferSorterCheckConditionAfterWaferIdReadDR(objCommon,portGroup,equipmentID);

        //--------------------------------------------------------
        // Case adjustDirection is SP_Sorter_Adjust_To_MM
        //--------------------------------------------------------
        if (CimStringUtils.equals(BizConstant.SP_SORTER_ADJUST_TO_MM,adjustDirection)){
            //---------------------------------------------------------------------------------
            // Collect participant cassettes (original/destination) from WaferSorterSlotMapSeq.
            //---------------------------------------------------------------------------------
            int slotMapLen = CimArrayUtils.getSize(strWaferSorterSlotMapSequence);
            int cstLen = 0;
            List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
            for (int i = 0; i < slotMapLen; i++) {
                // interested in SiView managed casettes only.
                if (CimBooleanUtils.isTrue(strWaferSorterSlotMapSequence.get(i).getOriginalCassetteManagedByOM())){
                    cassetteIDs.add(cstLen,strWaferSorterSlotMapSequence.get(i).getOriginalCassetteID());
                    log.info("SiView managed original cassette found.{}",cassetteIDs.get(cstLen).getValue());
                    cstLen++;
                }
                if (CimBooleanUtils.isTrue(strWaferSorterSlotMapSequence.get(i).getDestinationCassetteManagedByOM())){
                    cassetteIDs.add(cstLen,strWaferSorterSlotMapSequence.get(i).getDestinationCassetteID());
                    log.info("SiView managed destination cassette found.{}",cassetteIDs.get(cstLen).getValue());
                    cstLen++;
                }
            }
            if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
                log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

                // Advanced Mode
                // Lock Equipment Main Object
                Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
                objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvancedObjectLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
                objAdvancedObjectLockIn.setKeyList(new ArrayList<>());
                objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            }
            objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
            log.info("adjust to MM");
            //【step7】 - txOnlineSorterActionExecuteReq
            Params.OnlineSorterActionExecuteReqParams param = new Params.OnlineSorterActionExecuteReqParams();
            param.setActionCode(actionCode);
            param.setEquipmentID(equipmentID);
            param.setPhysicalRecipeID(physicalRecipeID);
            param.setPortGroup(portGroup);
            param.setWaferSorterSlotMapList(strWaferSorterSlotMapSequence);
            param.setUser(user);
            this.sxOnlineSorterActionExecuteReq(objCommon, param);
            //ok return
        }
        //--------------------------------------------------------
        // Case adjustDirection is SP_Sorter_Adjust_To_WaferSorter
        //--------------------------------------------------------
        else {
            //================================================================
            // Checking Previous Job is running or not
            //================================================================
            //【step8】 - waferSorter_CheckRunningJobs
            try {
                waferMethod.waferSorterCheckRunningJobs(objCommon, equipmentID, portGroup);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfigEx.getWaferSortAlreadyRunning(),e.getCode())){
                    log.info("SorterJob is already running");
                    throw e;
                }else if (!Validations.isEquals(retCodeConfigEx.getWaferSortPreviousJobNotFound(),e.getCode())){
                    log.error("Error Occured RC is {}",e.getCode());
                    throw e;
                }
            }
            //---------------------------------------------------------------------------------
            // Collect participant cassettes (original/destination) from WaferTransferSequence.
            //---------------------------------------------------------------------------------
            List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
            int casLne = 0;
            for (int i = 0; i < CimArrayUtils.getSize(strWaferXferSeq); i++) {
                // interested in SiView managed casettes only.
                if (CimBooleanUtils.isTrue(strWaferXferSeq.get(i).getBOriginalCassetteManagedByOM())){
                    cassetteIDs.add(casLne,strWaferXferSeq.get(i).getOriginalCassetteID());
                    log.info("SiView managed original cassette found.{}",cassetteIDs.get(casLne).getValue());
                    casLne++;
                }
                if (CimBooleanUtils.isTrue(strWaferXferSeq.get(i).getBDestinationCassetteManagedByOM())){
                    cassetteIDs.add(casLne,strWaferXferSeq.get(i).getDestinationCassetteID());
                    log.info("SiView managed destination cassette found.{}",cassetteIDs.get(casLne).getValue());
                    casLne++;
                }
            }
            if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
                log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

                // Advanced Mode
                // Lock Equipment Main Object
                Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
                objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvancedObjectLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
                objAdvancedObjectLockIn.setKeyList(new ArrayList<>());
                objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            }
            objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
            //---------------------------------------
            // Check input parameter for
            // wafer transfer (Sorter operation)
            //---------------------------------------
            //【step11】- sorter_waferTransferInfo_Verify
            sorterMethod.sorterWaferTransferInfoVerify(objCommon, strWaferXferSeq, BizConstant.SP_SORTER_LOCATION_CHECKBY_MM);

            //---------------------------------------
            // Check input parameter and
            // Server data condition
            //---------------------------------------
            //【step12】 - cassette_CheckConditionForWaferSort
            cassetteMethod.cassetteCheckConditionForWaferSort(objCommon, strWaferXferSeq, equipmentID);
            //------------------------------------------------------------
            // Retrieve Equipment's onlineMode
            //------------------------------------------------------------
            //【step13】 - equipment_onlineMode_Get

            String equipmentOnlineModeResult = BizConstant.SP_EQP_ONLINEMODE_OFFLINE;
            if (CimStringUtils.isNotEmpty(equipmentID.getValue())){
                equipmentOnlineModeResult  = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
            }
            log.info("bNotifyToTCS != TRUE  && onlineMode == SP_Eqp_OnlineMode_OnlineRemote");

            //【step14】 - sorter_waferTransferInfo_Restructure
            Outputs.ObjSorterWaferTransferInfoRestructureOut restructureSorterWaferTransferInfoOut = sorterMethod.sorterWaferTransferInfoRestructure(objCommon, strWaferXferSeq);
            //------------------------------------------------------------
            // Check output parameter of sorter_waferTransferInfo_Restructure()
            //------------------------------------------------------------
            log.info("Check output parameter of sorter_waferTransferInfo_Restructure()");
            for (int i = 0; i < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList()); i++) {
                log.info("Lot ID : {}",restructureSorterWaferTransferInfoOut.getLotInventoryStateList().get(i));
                int nOutputWaferLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
                for (int j = 0; j < nOutputWaferLen; j++) {
                    log.info("Wafer ID : {}",restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getWaferID().getValue());
                    log.info("Slot Number : {}",restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getSlotNumber());
                }
            }
            //---------------------------------------
            // At first, all relation between
            // carrier-wafer should be canceled
            // If bNotifyToTCS is true, Only Check Logic
            // works inside of wafer_materialContainer_Change()
            // or lot_materialContainer_Change()
            //---------------------------------------
            ObjectIdentifier newCassetteID = new ObjectIdentifier();
            int nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
            for (int i = 0; i < nILen; i++) {
                log.info("strLotInventoryStateSeq == SP_Lot_InventoryState_InBank,{}", i);
                int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
                for (int j = 0; j < nJLen; j++) {
                    //【step15】 - wafer_materialContainer_Change
                    Infos.Wafer strWafer = new Infos.Wafer();
                    strWafer.setWaferID(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getWaferID());
                    strWafer.setSlotNumber(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getSlotNumber());
                    waferMethod.waferMaterialContainerChange(objCommon, newCassetteID, strWafer);
                    // Input parameter
                    //  - equipmentID = input equipmentID
                    //  - newCassetteID = null
                    //  - strWafer = strSorter_waferTransferInfo_Restructure_out.strLotSeq[i].strWafer[j]
                    //  - bNotifyToTCS = input bNotifyToTCS
                }
            }
            //---------------------------------------
            // Next, new relation between
            // carrier-wafer should be created
            //---------------------------------------
            nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
            for (int i = 0; i < nILen; i++) {
                log.info("strLotInventoryStateSeq == SP_Lot_InventoryState_InBank = {}",i);
                int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
                for (int j = 0; j < nJLen; j++) {
                    //【step16】 - wafer_materialContainer_Change
                    Infos.Wafer strWafer = new Infos.Wafer();
                    strWafer.setWaferID(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getWaferID());
                    strWafer.setSlotNumber(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j).getSlotNumber());
                    waferMethod.waferMaterialContainerChange(objCommon,
                            restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), strWafer);
                    // Input parameter
                    //  - equipmentID = input equipmentID
                    //  - newCassetteID = strSorter_waferTransferInfo_Restructure_out.cassetteIDSeq[i]
                    //  - strWafer = strSorter_waferTransferInfo_Restructure_out.strLotSeq[i].strWafer[j]
                }
            }
            //---------------------------------------
            // Collect Cassette IDs of input parameter
            //---------------------------------------
            log.info("Collect Cassette IDs of input parameter");
            List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
            for (int i = 0; i < CimArrayUtils.getSize(strWaferXferSeq); i++) {
                Boolean bCassetteAdded = false;
                for (int j = 0; j < CimArrayUtils.getSize(cassetteIDSeq); j++) {
                    if (CimStringUtils.equals(strWaferXferSeq.get(i).getDestinationCassetteID().getValue(),cassetteIDSeq.get(j).getValue())){
                        log.info("break!!");
                        bCassetteAdded = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bCassetteAdded)){
                    cassetteIDSeq.add(strWaferXferSeq.get(i).getDestinationCassetteID());
                }
                bCassetteAdded = false;
                for (int j = 0; j < CimArrayUtils.getSize(cassetteIDSeq); j++) {
                    if (CimStringUtils.equals(strWaferXferSeq.get(i).getOriginalCassetteID().getValue(),cassetteIDSeq.get(j).getValue())){
                        log.info("break !!");
                        bCassetteAdded = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bCassetteAdded)){
                    cassetteIDSeq.add(strWaferXferSeq.get(i).getOriginalCassetteID());
                }
            }
            //---------------------------------------
            // Update Machine/ControlJob related information
            //---------------------------------------
            log.info("Call controlJob_relatedInfo_Update");
            //【step17】 - controlJob_relatedInfo_Update
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDSeq);
            //---------------------------------------
            // Update Carrier Multi Lot Type
            //---------------------------------------
            log.info("Update Carrier Multi Lot Type");
            for (int i = 0; i < CimArrayUtils.getSize(cassetteIDSeq); i++) {
                //【step18】 - cassette_multiLotType_Update
                try {
                    cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteIDSeq.get(i));
                } catch (ServiceException e){
                    boolean checkFlag = e.getCode() != retCodeConfig.getSucc().getCode() && e.getCode() != retCodeConfig.getNotFoundCassette().getCode();
                    Validations.check(checkFlag, new OmCode(e.getCode(), e.getMessage()));
                }
            }
            //---------------------------------------
            // Update WaferLotHistoryPointer of Lot
            //---------------------------------------
            log.info("Update WaferLotHistoryPointer of Lot");
            for (int i = 0; i < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList()); i++) {
                log.info(" ##### Update lotID = {}",restructureSorterWaferTransferInfoOut.getLotList().get(i).getLotID().getValue());
                //【step19】 - lot_waferLotHistoryPointer_Update
                lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, restructureSorterWaferTransferInfoOut.getLotList().get(i).getLotID());
            }
            //---------------------------------------
            // Prepare input parameter of lotWaferSortEvent_Make()
            //---------------------------------------
            log.info("Prepare input parameter of lotWaferSortEvent_Make()");
            List<Infos.WaferTransfer> tmpWaferXferSeq = new ArrayList<>();
            tmpWaferXferSeq = strWaferXferSeq;
            for (int i = 0; i < CimArrayUtils.getSize(tmpWaferXferSeq); i++) {
                Boolean bStringifiedObjRefFilled = false;
                for (int j = 0; j < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList()); j++) {
                    for (int k = 0; k < CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(j).getWaferList()); k++) {
                        if (CimStringUtils.equals(tmpWaferXferSeq.get(i).getWaferID().getReferenceKey(),restructureSorterWaferTransferInfoOut.getLotList().get(j).getWaferList().get(k).getWaferID().getReferenceKey())){
                            bStringifiedObjRefFilled = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bStringifiedObjRefFilled)){
                        break;
                    }
                }
            }
            //---------------------------------------
            // Create Wafer Sort Event
            //---------------------------------------
            log.info("Create Wafer Sort Event");
            //【step20】 - lotWaferSortEvent_Make
            eventMethod.lotWaferSortEventMake(objCommon, TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ.getValue(), tmpWaferXferSeq, claimMemo);
            //---------------------------------------
            // Creation Data
            //---------------------------------------
            int lenXferSql = CimArrayUtils.getSize(strWaferXferSeq);
            List<Infos.WaferSorterSlotMap> tmpWaferSorterSlotMapSequence = new ArrayList<>();
            actionCode = BizConstant.SP_SORTER_ADJUSTTOSORTER;
            ObjectIdentifier portID = new ObjectIdentifier();
            //---------------------------------------
            // Get Port Info
            //---------------------------------------
            log.info("Calling Method:equipment_portInfo_Get");
            //【step21】 - equipment_portInfo_Get
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
            int lenPortInfo = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
            //---------------------------------------
            // Set Data
            //---------------------------------------
            for (int i = 0; i < lenXferSql; i++) {
                //---------------------------------------
                // Find Target PortID
                //---------------------------------------
                for (int j = 0; j < lenPortInfo; j++) {
                    if (ObjectIdentifier.equalsWithValue(eqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID(),
                            strWaferXferSeq.get(i).getDestinationCassetteID())){
                        log.info("Find Target PortID");
                        portID = eqpPortInfo.getEqpPortStatuses().get(j).getPortID();
                        log.info("Target PortID = {}",portID.getValue());
                    }
                }
                //---------------------------------------
                // Get Target lotID
                //---------------------------------------
                ObjectIdentifier lotID = waferMethod.waferLotGet(objCommon, strWaferXferSeq.get(i).getWaferID());
                //---------------------------------------
                // Set Sequence Data of slotmap
                //---------------------------------------
                Infos.WaferSorterSlotMap waferSorterSlotMap = new Infos.WaferSorterSlotMap();
                tmpWaferSorterSlotMapSequence.add(i,waferSorterSlotMap);
                waferSorterSlotMap.setPortGroup(portGroup);
                waferSorterSlotMap.setEquipmentID(equipmentID);
                waferSorterSlotMap.setActionCode(BizConstant.SP_SORTER_ADJUSTTOSORTER);
                waferSorterSlotMap.setRequestTime("");
                waferSorterSlotMap.setDirection(BizConstant.SP_SORTER_DIRECTION_MM);
                waferSorterSlotMap.setWaferID(strWaferXferSeq.get(i).getWaferID());
                waferSorterSlotMap.setLotID(lotID);
                waferSorterSlotMap.setDestinationCassetteID(strWaferXferSeq.get(i).getDestinationCassetteID());
                waferSorterSlotMap.setDestinationPortID(portID);
                waferSorterSlotMap.setDestinationCassetteManagedByOM(strWaferXferSeq.get(i).getBDestinationCassetteManagedByOM());
                waferSorterSlotMap.setDestinationSlotNumber(CimNumberUtils.longValue(strWaferXferSeq.get(i).getDestinationSlotNumber()));
                waferSorterSlotMap.setOriginalCassetteID(strWaferXferSeq.get(i).getOriginalCassetteID());
                waferSorterSlotMap.setOriginalPortID(portID);
                waferSorterSlotMap.setOriginalCassetteManagedByOM(strWaferXferSeq.get(i).getBOriginalCassetteManagedByOM());
                waferSorterSlotMap.setOriginalSlotNumber(CimNumberUtils.longValue(strWaferXferSeq.get(i).getOriginalSlotNumber()));
                waferSorterSlotMap.setRequestUserID(user.getUserID());
                waferSorterSlotMap.setReplyTime("");
                waferSorterSlotMap.setSorterStatus(BizConstant.SP_SORTER_REQUESTED);
                waferSorterSlotMap.setSlotMapCompareStatus("");
                waferSorterSlotMap.setOmsCompareStatus("");
            }
            //---------------------------------------
            // update the data base of TCS
            //---------------------------------------
            log.info("adjust to Sorter");
            //【step23】 - txOnlineSorterActionExecuteReq
            Params.OnlineSorterActionExecuteReqParams param = new Params.OnlineSorterActionExecuteReqParams();
            param.setActionCode(actionCode);
            param.setEquipmentID(equipmentID);
            param.setPhysicalRecipeID(physicalRecipeID);
            param.setPortGroup(portGroup);
            param.setWaferSorterSlotMapList(tmpWaferSorterSlotMapSequence);
            param.setUser(user);
            this.sxOnlineSorterActionExecuteReq(objCommon, param);
            //ok return
        }
    }

    public List<Infos.WaferSorterCompareCassette> sxOnlineSorterSlotmapCompareReq(Infos.ObjCommon objCommon, String portGroup, ObjectIdentifier equipmentID,String actionCode) {
        log.info("START sxOnlineSorterSlotmapCompareReq");
        //init
        //----------------------------------------
        //  Check Equipm    ent ID is SORTER or
        //----------------------------------------
        log.info("Check Transaction ID and equipment Category combination.");
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);
        User dummyUser = new User();
        //================================================================
        // Checking Previous Job is running or not
        //================================================================
        //【step2】 - waferSorter_CheckRunningJobs  dummyUser is not used in waferSorter_CheckRunningJobs
        try {
            waferMethod.waferSorterCheckRunningJobs(objCommon, equipmentID, portGroup);
        } catch (ServiceException e) {
            //------------------------------------------------------
            // If there are running Jobs, then return
            //------------------------------------------------------
            if (Validations.isEquals(retCodeConfigEx.getWaferSortAlreadyRunning(),e.getCode())){
                throw e;
            }else if (!Validations.isEquals(retCodeConfigEx.getWaferSortPreviousJobNotFound(),e.getCode())){
                throw e;
            }
        }
        //================================================================
        // Checking Action one's personal history (Look for WaferIDRead)
        //================================================================
        //【step3】 - waferSorter_CheckCondition_AfterWaferIdReadDR
        waferMethod.waferSorterCheckConditionAfterWaferIdReadDR(objCommon,portGroup,equipmentID);

        //----------------------------------------
        //  Get SLOTMAP DATA When Requested
        //----------------------------------------
        Infos.WaferSorterSlotMap strWaferSorterGetSlotMapReferenceCondition = new Infos.WaferSorterSlotMap();
        String requiredData = BizConstant.SP_SORTER_SLOTMAP_LATESTDATA;
        strWaferSorterGetSlotMapReferenceCondition.setPortGroup(portGroup);
        strWaferSorterGetSlotMapReferenceCondition.setEquipmentID(equipmentID);
        strWaferSorterGetSlotMapReferenceCondition.setActionCode(CimStringUtils.isEmpty(actionCode)?BizConstant.SP_SORTER_READ:actionCode);
        strWaferSorterGetSlotMapReferenceCondition.setRequestTime("");
        strWaferSorterGetSlotMapReferenceCondition.setDirection(BizConstant.SP_SORTER_DIRECTION_TCS);
        strWaferSorterGetSlotMapReferenceCondition.setWaferID(new ObjectIdentifier(""));
        strWaferSorterGetSlotMapReferenceCondition.setLotID(new ObjectIdentifier(""));
        strWaferSorterGetSlotMapReferenceCondition.setDestinationCassetteID(new ObjectIdentifier(""));
        strWaferSorterGetSlotMapReferenceCondition.setDestinationPortID(new ObjectIdentifier(""));
        strWaferSorterGetSlotMapReferenceCondition.setDestinationCassetteManagedByOM(false);
        strWaferSorterGetSlotMapReferenceCondition.setDestinationSlotNumber(0L);
        strWaferSorterGetSlotMapReferenceCondition.setOriginalCassetteID(new ObjectIdentifier(""));
        strWaferSorterGetSlotMapReferenceCondition.setOriginalPortID(new ObjectIdentifier(""));
        strWaferSorterGetSlotMapReferenceCondition.setOriginalCassetteManagedByOM(false);
        strWaferSorterGetSlotMapReferenceCondition.setOriginalSlotNumber(0L);
        strWaferSorterGetSlotMapReferenceCondition.setRequestUserID(new ObjectIdentifier(""));
        strWaferSorterGetSlotMapReferenceCondition.setReplyTime("");
        strWaferSorterGetSlotMapReferenceCondition.setSorterStatus(BizConstant.SP_SORTER_SUCCEEDED);
        strWaferSorterGetSlotMapReferenceCondition.setSlotMapCompareStatus("");
        strWaferSorterGetSlotMapReferenceCondition.setOmsCompareStatus("");

        //【step4】 - waferSorter_slotMap_SelectDR
        List<Infos.WaferSorterSlotMap> waferSorterSlotMapSelectDROut = waferMethod.waferSorterSlotMapSelectDR(objCommon,
                requiredData,
                "",
                BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                BizConstant.SP_SORTER_IGNORE_SIVIEWFLAG,
                strWaferSorterGetSlotMapReferenceCondition);
        //---------------------------------------------------
        //  SET TCS'S SLOTMAP INFORMATION & GET destCastID
        //---------------------------------------------------
        List<Infos.WaferSorterSlotMap> strSlotMapSeq = waferSorterSlotMapSelectDROut;
        List<Infos.WaferSorterCompareCassette> strCastSeq = new ArrayList<>();
        ObjectIdentifier cassetteIdBase = new ObjectIdentifier();
        int tcsLen = CimArrayUtils.getSize(strSlotMapSeq);
        int castCnt = 0;
        Long slotPosition = 0L;
        Long maxWaferLen = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getLongValue();
        log.info("TCS's SlotMapCount = {}",tcsLen);
        log.info("maxWaferLen = {}",maxWaferLen);
        Infos.WaferSorterCompareCassette waferSorterCompareCassette = new Infos.WaferSorterCompareCassette();
        List<Infos.WaferSorterCompareSlotMap> strWaferSorterCompareSlotMapSequence = new ArrayList<>();
        waferSorterCompareCassette.setStrWaferSorterCompareSlotMapSequence(strWaferSorterCompareSlotMapSequence);
        strCastSeq.add(waferSorterCompareCassette);
        for (int i = 0; i < tcsLen; i++) {
            log.info("TCS's Loop Count = {}",i);
            if (!CimStringUtils.equals(cassetteIdBase.getValue(),strSlotMapSeq.get(i).getDestinationCassetteID().getValue())){
                castCnt ++;
                log.info("Old CassetteId != New CassetteId");
                log.info("Old CassetteId = {}",cassetteIdBase.getValue());
                log.info("New CassetteId = {}",strSlotMapSeq.get(i).getDestinationCassetteID().getValue());
                log.info("TCS's Cassette Count = {}",castCnt);
                cassetteIdBase = strSlotMapSeq.get(i).getDestinationCassetteID();
                waferSorterCompareCassette.setCassetteID(ObjectIdentifier.buildWithValue(cassetteIdBase.getValue()));
                waferSorterCompareCassette.setPortID(ObjectIdentifier.buildWithValue(strSlotMapSeq.get(i).getDestinationPortID().getValue()));
            }
            slotPosition = CimNumberUtils.longValue(strSlotMapSeq.get(i).getDestinationSlotNumber());
            if (slotPosition > 0){
                log.info("slotPosition > 0,{}",slotPosition);
                Infos.WaferSorterCompareSlotMap waferSorterCompareSlotMap = new Infos.WaferSorterCompareSlotMap();
                strWaferSorterCompareSlotMapSequence.add(waferSorterCompareSlotMap);
                waferSorterCompareSlotMap.setTcsDestinationLotID(strSlotMapSeq.get(i).getLotID());
                waferSorterCompareSlotMap.setTcsDestinationPosition(strSlotMapSeq.get(i).getDestinationSlotNumber());
                waferSorterCompareSlotMap.setTcsDestinationWaferID(strSlotMapSeq.get(i).getWaferID());
                waferSorterCompareSlotMap.setTcsDestinationAliasName(strSlotMapSeq.get(i).getAliasName());
            }else {
                log.error("slotPosition < 0 {}",slotPosition);
                throw new ServiceException(retCodeConfigEx.getInvalidTcsResult());
            }
        }
        //-----------------------------
        //  Read a Slot Map (MM)
        //-----------------------------
        castCnt = CimArrayUtils.getSize(strCastSeq);
        int mmLen = 0;
        log.info("Cassette Count= {}",castCnt);
        for (int i = 0; i < castCnt; i++) {
            log.info("TCS's CasstteID Loop Count ={}",i);
            //【step5】 - cassette_GetWaferMapDR
            List<Infos.WaferMapInCassetteInfo>  cassetteGetWaferMapOutRetCode = null;
            try{
                cassetteGetWaferMapOutRetCode = cassetteMethod.cassetteGetWaferMapDR(objCommon, strCastSeq.get(i).getCassetteID());
            }catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                    throw e;
                }
                log.info("CASSETTE NOT FOUND : CassetteID = {}",strCastSeq.get(i).getCassetteID().getValue());
            }
            //--------------------------------------------------------------------------------
            //  SET MM'S SLOTMAP INFORMATION
            //--------------------------------------------------------------------------------
            mmLen = CimArrayUtils.getSize(cassetteGetWaferMapOutRetCode);
            log.info("MM's SlotMapCount = {}",mmLen);
            for (int j = 0; j < mmLen; j++) {
                slotPosition = CimNumberUtils.longValue(cassetteGetWaferMapOutRetCode.get(j).getSlotNumber());
                log.info("MM's SlotMap Loop Count = {}",j);
                log.info("slotPosition = {}",slotPosition);
                if (slotPosition > 0 ){
                    Long finalSlotPosition = slotPosition;
                    List<Infos.WaferMapInCassetteInfo> finalCassetteGetWaferMapOutRetCode = cassetteGetWaferMapOutRetCode;
                    int finalJ = j;
                    strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().stream().filter(waferSorterCompareSlotMap ->
                            finalSlotPosition == waferSorterCompareSlotMap.getTcsDestinationPosition()
                    ).forEach(waferSorterCompareSlotMap -> {
                        waferSorterCompareSlotMap.setMmDestinationPosition(CimNumberUtils.longValue(finalCassetteGetWaferMapOutRetCode.get(finalJ).getSlotNumber()));
                        waferSorterCompareSlotMap.setMmDestinationLotID(finalCassetteGetWaferMapOutRetCode.get(finalJ).getLotID());
                        waferSorterCompareSlotMap.setMmDestinationWaferID(finalCassetteGetWaferMapOutRetCode.get(finalJ).getWaferID());
                        waferSorterCompareSlotMap.setMmDestinationAliasName(finalCassetteGetWaferMapOutRetCode.get(finalJ).getAliasWaferName());
                    });
                }else {
                    log.error("slotPosition < 0 ,{}",slotPosition);
                    throw new ServiceException(retCodeConfigEx.getInvalidMMResult());
                }
            }
        }
        //---------------------------------------------------
        //  COMPARE MM INFORMATION WITH SLOTMAP INFORMATION
        //---------------------------------------------------
        castCnt = CimArrayUtils.getSize(strCastSeq);
        log.info("Casstte = {}",castCnt);
        for (int i = 0; i < castCnt; i++) {
            log.info("Casstte ID Compare Loop Count = {}",i);
            for (int j = 0; j < CimArrayUtils.getSize(strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence()); j++) {
                log.info("Wafer Compare Loop Count = {}",j);
                //MM VS TCS DATA
                log.info("MM  WaferID Positon ==>>>{}",strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getMmDestinationPosition());
                log.info("TCS WaferID Positon ==>>>{}",strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getTcsDestinationPosition());
                log.info("MM  WaferID         ==>>>{}", ObjectIdentifier.fetchValue(strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getMmDestinationWaferID()));
                log.info("TCS WaferID         ==>>>{}", ObjectIdentifier.fetchValue(strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getTcsDestinationWaferID()));
                String mmDestWaferID = strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getMmDestinationAliasName();
                String tcsDestWaferID = strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getTcsDestinationAliasName();
                Long mmDestPos = CimNumberUtils.longValue(strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getMmDestinationPosition());
                Long tcsDestPos = CimNumberUtils.longValue(strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).getTcsDestinationPosition());
                if (CimObjectUtils.isEmpty(mmDestWaferID) && CimObjectUtils.isEmpty(tcsDestWaferID)){
                    log.info("matchUnmatch = MATCHING");
                    strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).setCompareStatus(BizConstant.SP_SORTER_SLOTMAP_MATCH);
                }else if (CimStringUtils.equals(mmDestWaferID,tcsDestWaferID)){
                    if (mmDestPos.equals(tcsDestPos)){
                        log.info("matchUnmatch = MATCHING");
                        strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).setCompareStatus(BizConstant.SP_SORTER_SLOTMAP_MATCH);
                    }else {
                        log.info("matchUnmatch = UNMATCHING");
                        strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).setCompareStatus(BizConstant.SP_SORTER_SLOTMAP_UNMATCH);
                    }
                }else {
                    strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).setCompareStatus(BizConstant.SP_SORTER_SLOTMAP_UNMATCH);
                }
                if (CimObjectUtils.isEmpty(tcsDestWaferID)){
                    continue;
                }
                //【step6】 - wafer_lot_Get
                /* EAP不报Wafer,故无此段逻辑
                try {
                    ObjectIdentifier waferLotOut = waferMethod.waferLotGet(objCommon, tcsDestWaferID);
                } catch (ServiceException e){
                    log.info("matchUnmatch = UNKNOWN");
                    strCastSeq.get(i).getStrWaferSorterCompareSlotMapSequence().get(j).setCompareStatus(BizConstant.SP_SORTER_SLOTMAP_UNKNOWN);
                }*/
            }
        }
        return strCastSeq;
    }

    public void sxSJCancelReq(Infos.ObjCommon objCommon, Params.SJCancelReqParm params){
        if(ObjectIdentifier.isEmpty(params.getSorterJobID())) {
            throw new ServiceException(retCodeConfigEx.getNotFoundSorterjob());
        }
        String sorterJobLockFlagStr = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getValue();
        int sorterJobLockFlag = CimNumberUtils.intValue(sorterJobLockFlagStr);
        log.info("sorterJobLockFlag : {}", sorterJobLockFlag);

        if ( 1 == sorterJobLockFlag ) {
            log.info("sorterJobLockFlag = 1");

            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------

            log.info(  "calling sorter_sorterJob_LockDR()" );
            Inputs.SorterSorterJobLockDRIn sorterSorterJobLockDRIn = new Inputs.SorterSorterJobLockDRIn();
            sorterSorterJobLockDRIn.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
            sorterSorterJobLockDRIn.setSorterJobID(params.getSorterJobID());
            log.info("calling sorter_sorterJob_LockDR()");
            sorterMethod.sorterSorterJobLockDR(objCommon, sorterSorterJobLockDRIn);
        }

        //-----------------------------------------
        // Get Sorter Job Information
        //-----------------------------------------
        log.info( "Get Sorter Job Information. : {}", params.getSorterJobID());

        Inputs.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Inputs.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setSorterJob(params.getSorterJobID());
        List<Infos.SortJobListAttributes>  strSorter_jobList_GetDR_out = sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        if( 0 >= CimArrayUtils.getSize(strSorter_jobList_GetDR_out)) {
            log.info( "strSorter_jobList_GetDR_out_EQP.strSortJobListAttributesSeq.length() <= 0");
            throw new ServiceException(retCodeConfigEx.getNotFoundSorterjob());
        }

        //Copy
        Infos.SortJobListAttributes strSortJobListAttributes = strSorter_jobList_GetDR_out.get(0);

        //------------------------------------------------------------------
        //  Object Lock Process
        //------------------------------------------------------------------

        //---------------------
        // Equipment
        //---------------------

        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();
        int lockMode = 0;
        if ( 1 == sorterJobLockFlag ) {
            log.info("sorterJobLockFlag = 1");
            // Get required equipment lock mode

            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(strSorter_jobList_GetDR_out.get(0).getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

            lockMode = objLockModeOut.getLockMode().intValue();
        }
        log.info(  "lockMode : {}", lockMode );

        if ( lockMode != 0) {
            log.info( "lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(strSortJobListAttributes.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

            if (!CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING, strSortJobListAttributes.getSorterJobStatus()) ) {
                log.info( "strSortJobListAttributes.sorterJobStatus != SP_SorterJobStatus_Wait_To_Executing");

                log.info(  "calling equipment_portGroupInfo_Get()" );
                Outputs.ObjEquipmentPortGroupInfoGetOut strEquipment_portGroupInfo_Get_out = portMethod.equipmentPortGroupInfoGet(objCommon, strSortJobListAttributes.getEquipmentID(), ObjectIdentifier.fetchValue(strSortJobListAttributes.getPortGroupID()), null);

                int lenEqpPort = CimArrayUtils.getSize(strEquipment_portGroupInfo_Get_out.getEqpPortAttributes());
                log.info( "lenEqpPort : {}", lenEqpPort);
                for (int ii = 0; ii < lenEqpPort; ii++ ) {
                    objectLockMethod.objectLockForEquipmentResource(objCommon, strSortJobListAttributes.getEquipmentID(),
                            strEquipment_portGroupInfo_Get_out.getEqpPortAttributes().get(ii).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);

                    log.info( "Locked port object  : {}", strEquipment_portGroupInfo_Get_out.getEqpPortAttributes().get(ii).getPortID()) ;
                }
            }
        } else {
            log.info( "lockMode = SP_EQP_LOCK_MODE_WRITE");
            log.info( "Object Lock .. Equipment. : {}", strSortJobListAttributes.getEquipmentID());
            objectLockMethod.objectLock(objCommon, CimMachine.class, strSortJobListAttributes.getEquipmentID());
        }

        //---------------------
        // Carrier
        //---------------------
        log.info( "Object Lock .. Carrier." );
        //Gathering carrier no-duplicated.
        int srtCmpLen  = CimArrayUtils.getSize(strSortJobListAttributes.getSorterComponentJobListAttributesList());
        int srtCmpCnt  = 0;
        int slotMapLen = 0;
        int slotMapCnt = 0;
        int lotIDLen   = 0;
        List<ObjectIdentifier> orgCarrierIDs = new ArrayList<>();
        List<ObjectIdentifier> dstCarrierIDs = new ArrayList<>();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();

        for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
            Boolean orgFoundFlag = false;
            for(int i = 0; i < CimArrayUtils.getSize(orgCarrierIDs); i++) {
                if(ObjectIdentifier.equalsWithValue(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalCarrierID(), orgCarrierIDs.get(i))) {
                    orgFoundFlag = true;
                    break;
                }
            }
            if(CimBooleanUtils.isFalse(orgFoundFlag )) {
                orgCarrierIDs.add(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalCarrierID());
            }

            //destinationCarrier
            Boolean dstFoundFlag = false;
            for(int i = 0; i< CimArrayUtils.getSize(dstCarrierIDs); i++) {
                if( ObjectIdentifier.equalsWithValue(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationCarrierID(), dstCarrierIDs.get(i))) {
                    dstFoundFlag = true;
                    break;
                }
            }
            if( CimBooleanUtils.isFalse(dstFoundFlag )) {
                dstCarrierIDs.add(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationCarrierID());
            }

            slotMapLen = CimArrayUtils.getSize(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getWaferSorterSlotMapList());
            for(slotMapCnt = 0; slotMapCnt<slotMapLen; slotMapCnt++) {
                Boolean lotFoundFlag = false;
                lotIDLen = CimArrayUtils.getSize(lotIDs);
                for(int j=0; j<lotIDLen; j++) {
                    if(ObjectIdentifier.equalsWithValue(lotIDs.get(j), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID()) ) {
                        lotFoundFlag = true;
                        break;
                    }
                }
                if( CimBooleanUtils.isFalse(lotFoundFlag )) {
                    lotIDs.add(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID());
                }
            }
        }

        int orgCarrierLen = CimArrayUtils.getSize(orgCarrierIDs);
        int dstCarrierLen = CimArrayUtils.getSize(dstCarrierIDs);
        int allCarrierLen = orgCarrierLen + dstCarrierLen;
        int allCarrierCnt = 0 ;
        List<ObjectIdentifier> allCarrierIDs = new ArrayList<>();

        for(int i=0; i<orgCarrierLen; i++)    allCarrierIDs.add(orgCarrierIDs.get(i));
        for(int i=0; i<dstCarrierLen; i++)    allCarrierIDs.add(dstCarrierIDs.get(i));

        for(int i=0; i<orgCarrierLen; i++) {
            log.info( "Object Lock .. Original Carrier. : {}", orgCarrierIDs.get(i));
            objectLockMethod.objectLock( objCommon, CimCassette.class, orgCarrierIDs.get(i));
        }

        for(int i=0; i<dstCarrierLen; i++) {
            log.info( "Object Lock .. Destination Carrier: {}", dstCarrierIDs.get(i));
            objectLockMethod.objectLock( objCommon, CimCassette.class, dstCarrierIDs.get(i));
        }

        //---------------------
        // Lot
        //---------------------
        log.info( "Object Lock .. Lot " );
        lotIDLen = CimArrayUtils.getSize(lotIDs);
        for(int lotIDCnt = 0; lotIDCnt<lotIDLen; lotIDCnt++) {
            log.info( "Object Lock .. Lot. : {}", lotIDs.get(lotIDCnt));
            objectLockMethod.objectLock( objCommon, CimLot.class, lotIDs.get(lotIDCnt));
        }
        //--------------------------------------------
        // Check Inpara.componentJobID existence
        //--------------------------------------------
        log.info( "Check Inpara.sorterComponentJobID existence.");
        int compIDLen = CimArrayUtils.getSize(params.getSorterComponentJobIDseq());
        for(int i=0; i<compIDLen; i++) {
            Boolean compIDFoundFlag = false;
            for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                if(ObjectIdentifier.equalsWithValue(params.getSorterComponentJobIDseq().get(i), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getSorterComponentJobID())) {
                    compIDFoundFlag = true;
                    break;
                }
            }
            if( CimBooleanUtils.isFalse(compIDFoundFlag )) {
                log.info( "input sorterComponentJobID does NOT exist.");
                throw new ServiceException(retCodeConfigEx.getNotFoundSorterjobComponent());
            }
        }

        //--------------------------------------------------------------------------------------------
        // Judgement for deletion.
        //
        //  SorterJobStatus  ComponentJobStatus     Can delete SortJob?    Can delete CompJob?
        //  ------------------------------------------------------------------------------------
        //  WaitToExecuting  WaitToExecuting        OK                     OK
        //  Executing        Xfer                   NG                     NG
        //  Executing        Executing              NG                     NG
        //  Executing        Wait To Executing      NG                     OK
        //  Executing        Completed              NG                     NG
        //  Error            Error                  OK                     OK
        //  Completed        Completed              OK                     OK
        //--------------------------------------------------------------------------------------------
        Boolean srtDELFlag = false;
        Boolean cmpDELFlag = false;

        //Delete SortJob ? or Delete ComponentJob ?
        if( srtCmpLen == compIDLen ) {
            //----------------------------------------
            // Delete Sort Job Information ?
            //----------------------------------------
            log.info( "Delete Sort Job Information? ( srtCmpLen == compIDLen ) ");
            srtDELFlag = true;

            // If Component Job Status is Xfer or Executing. Sort Job can not be deleted.
            for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                if(CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER, strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus())
                        || CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_EXECUTING, strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus())){
                    Validations.check(retCodeConfigEx.getInvalidComponentjobStatus(), ObjectIdentifier.fetchValue(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getSorterComponentJobID()),
                            strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus(),
                            ObjectIdentifier.fetchValue(strSortJobListAttributes.getSorterJobID()),strSortJobListAttributes.getSorterJobStatus());
                }
            }//Loop of Component Job
        } else if( 0 == compIDLen ) {

            //----------------------------------------
            // Delete Sort Job Information ?
            //----------------------------------------
            log.info( "Delete Sort Job Information?  ( 0 == compIDLen ) ");
            srtDELFlag = true;
            //SortJobStatus is not Executing? WaitToExecuting , Error, Completed can be deleted.
            if(CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_EXECUTING, strSortJobListAttributes.getSorterJobStatus())) {
                Validations.check(retCodeConfigEx.getInvalidSorterJobStatus(), ObjectIdentifier.fetchValue(strSortJobListAttributes.getSorterJobID()),strSortJobListAttributes.getSorterJobStatus());
            }
        } else {
            //----------------------------------------
            // Delete Component Job Information ?
            //----------------------------------------
            log.info( "Delete Component Job Information? other ");
            cmpDELFlag = true;

            for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {

                if(CimStringUtils.equals( BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER, strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus() )
                        || CimStringUtils.equals( BizConstant.SP_SORTERCOMPONENTJOBSTATUS_EXECUTING, strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus() )
                        || CimStringUtils.equals( BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED, strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus() )) {
                    if(CimStringUtils.equals( BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED, strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus() )
                            && CimStringUtils.equals( BizConstant.SP_SORTERJOBSTATUS_COMPLETED, strSortJobListAttributes.getSorterJobStatus() )) {
                        //OK
                        log.info( "Sort Job Status and Component Job Staus are Completed. OK.");
                    } else {
                        Validations.check(retCodeConfigEx.getInvalidComponentjobStatus(), ObjectIdentifier.fetchValue(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getSorterComponentJobID()),
                                strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getComponentSorterJobStatus(),
                                ObjectIdentifier.fetchValue(strSortJobListAttributes.getSorterJobID()),strSortJobListAttributes.getSorterJobStatus());
                    }
                }
            }//Loop of Component
        }

        //---------------------------------------------
        // Get Sort Job by the Equipment.
        //---------------------------------------------
        log.info( "Get Sorter Job Information. : {}", params.getSorterJobID());

        Inputs.ObjSorterJobListGetDRIn objSorterJobListGetDRInByTheEquipment = new Inputs.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRInByTheEquipment.setEquipmentID(strSortJobListAttributes.getEquipmentID());
        List<Infos.SortJobListAttributes> strSortJobListAttributesSeq_EQP = sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRInByTheEquipment);

        //------------------------------------------
        // Delete Sort Job Information
        //------------------------------------------
        if( CimBooleanUtils.isTrue(srtDELFlag )) {
            //Delete Slot Map , Component Job Information.

            Inputs.SorterComponentJobDeleteDRIn strSorter_componentJob_DeleteDR_in = new Inputs.SorterComponentJobDeleteDRIn();
            strSorter_componentJob_DeleteDR_in.setSorterJobID(params.getSorterJobID());
            sorterMethod.sorterComponentJobDeleteDR(objCommon, strSorter_componentJob_DeleteDR_in);

            //Delete Sorter Job Information.
            sorterMethod.sorterSorterJobDeleteDR( objCommon, params.getSorterJobID() );

            //Adjust order sequence of remained Sort Job .
            int remainSortJobLen = CimArrayUtils.getSize(strSortJobListAttributesSeq_EQP);
            List<ObjectIdentifier> remainSortJobIDs = new ArrayList<>();

            for(int i=0; i<remainSortJobLen; i++) {
                if(!ObjectIdentifier.equalsWithValue( params.getSorterJobID(), strSortJobListAttributesSeq_EQP.get(i).getSorterJobID() )) {
                    remainSortJobIDs.add(strSortJobListAttributesSeq_EQP.get(i).getSorterJobID());
                }
            }

            //--------------------------------------------
            // Change priority of remained Sort Job.
            //--------------------------------------------
            if (CimArrayUtils.getSize(remainSortJobIDs) > 0) {
                log.info( "### Chage priority for remain sorterJobID.");
                Inputs.SorterLinkedJobUpdateDRIn strSorter_linkedJobUpdateDR_in = new Inputs.SorterLinkedJobUpdateDRIn();
                strSorter_linkedJobUpdateDR_in.setJobIDs(remainSortJobIDs);
                strSorter_linkedJobUpdateDR_in.setJobType(BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB);
                sorterMethod.sorterLinkedJobUpdateDR( objCommon, strSorter_linkedJobUpdateDR_in);
            }
        }
        //------------------------------------------
        // Delete Component Job Information
        //------------------------------------------
        else if(CimBooleanUtils.isTrue(cmpDELFlag )) {
            //Delete Slot Map, Component Job Information.
            Inputs.SorterComponentJobDeleteDRIn strSorter_componentJob_DeleteDR_in = new Inputs.SorterComponentJobDeleteDRIn();
            strSorter_componentJob_DeleteDR_in.setSorterJobID(params.getSorterJobID());
            strSorter_componentJob_DeleteDR_in.setComponentJobIDseq(params.getSorterComponentJobIDseq());
            sorterMethod.sorterComponentJobDeleteDR( objCommon, strSorter_componentJob_DeleteDR_in );

            //Adjust order sequence of remained Component Job .
            int remainComponentJobLen = CimArrayUtils.getSize(strSortJobListAttributes.getSorterComponentJobListAttributesList());
            List<ObjectIdentifier> remainComponentJobIDs = new ArrayList<>();
            for(int i=0; i<remainComponentJobLen; i++) {
                compIDLen = CimArrayUtils.getSize(params.getSorterComponentJobIDseq());
                for(int j=0; j<compIDLen; j++) {
                    if( !ObjectIdentifier.equalsWithValue( params.getSorterComponentJobIDseq().get(j), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID())) {
                        remainComponentJobIDs.add(strSortJobListAttributes.getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID());
                    }
                }
            }

            //--------------------------------------------
            // Change priority of remained Component Job.
            //--------------------------------------------
            if (CimArrayUtils.getSize(remainComponentJobIDs) > 0) {
                log.info( "### Chage priority for remain sorter Component Job.");
                Inputs.SorterLinkedJobUpdateDRIn strSorter_linkedJobUpdateDR_in = new Inputs.SorterLinkedJobUpdateDRIn();
                strSorter_linkedJobUpdateDR_in.setJobIDs(remainComponentJobIDs);
                strSorter_linkedJobUpdateDR_in.setJobType(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB);
                sorterMethod.sorterLinkedJobUpdateDR( objCommon, strSorter_linkedJobUpdateDR_in);
            }
        }
        //--------------------------------------------------------------
        //  Call TxSendSortJobCancelNotificationReq
        //--------------------------------------------------------------
        log.info( "Notify Sort Job Cancel to TCS : {}", params.getNotifyToTCSFlag());
        if( CimBooleanUtils.isTrue(params.getNotifyToTCSFlag())) {
            log.info( "### Sorter Job Cancel Notice to TCS. notifyToTCSFlag == true");

            String tmpSleepTimeValue  = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
            String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
            int sleepTimeValue = 0;
            int retryCountValue = 0;

            if (CimStringUtils.isEmpty(tmpSleepTimeValue)) {
                sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS.intValue();
            } else {
                sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue) ;
            }

            if (CimStringUtils.isEmpty(tmpRetryCountValue)) {
                retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS.intValue();
            } else {
                retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
            }

            log.info("env value of OM_EAP_CONNECT_SLEEP_TIME  = {}",sleepTimeValue);
            log.info("env value of OM_EAP_CONNECT_RETRY_COUNT = {}",retryCountValue);

            /*--------------------------*/
            /*    Send Request to TCS   */
            /*--------------------------*/
            //TCSMgr_SendSortJobCancelNotificationReq

            Inputs.SendSortJobCancelNotificationReqIn sendSortJobCancelNotificationReqIn = new Inputs.SendSortJobCancelNotificationReqIn();
//          sendSortJobCancelNotificationReqIn.setObjCommonIn(objCommon);
            sendSortJobCancelNotificationReqIn.setUser(objCommon.getUser());
            sendSortJobCancelNotificationReqIn.setEquipmentID(strSortJobListAttributes.getEquipmentID());
            sendSortJobCancelNotificationReqIn.setSorterJobID(strSortJobListAttributes.getSorterJobID());
            sendSortJobCancelNotificationReqIn.setOpeMemo(params.getClaimMemo());

//            tcsMethod.sendTCSReq(TCSReqEnum.SORT_JOB_CANCEL_REQ, sendSortJobCancelNotificationReqIn);
            // 更改为EAPMethood
            IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), strSortJobListAttributes.getEquipmentID(), null, true);
//            IEAPRemoteManager eapRemoteManager = null;
            if (eapRemoteManager!=null){
                for(int i = 0 ; i < (retryCountValue + 1) ; i++) {
                    try {
                        eapRemoteManager.sendSortJobCancelNotificationReq(sendSortJobCancelNotificationReqIn);
                    } catch (ServiceException ex){
                        if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                                Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                                Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                            if (i<retryCountValue){
                                try {
                                    Thread.sleep(sleepTimeValue);
                                    continue;
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    ex.addSuppressed(e);
                                    throw ex;
                                }
                            }
                        }
                        throw ex;
                    } catch (Exception ex){
                        if (i<retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                        throw ex;
                    }
                    break;
                }
            }

        }

        //------------------------
        // NPWReserve Cancel
        //------------------------
        allCarrierLen = CimArrayUtils.getSize(allCarrierIDs);
        List<ObjectIdentifier> NPWRsvCanCarrierIDs = new ArrayList<>();
        List<ObjectIdentifier> NPWRsvLoadPortIDs = new ArrayList<>();
        int NPWRsvCnt = 0;
        for(allCarrierCnt =0; allCarrierCnt<allCarrierLen; allCarrierCnt++) {
            log.info( "Carrier has 'NPW Reserve  : {}", allCarrierIDs.get(allCarrierCnt));
            ObjectIdentifier loadPortID = null;
            if( CimBooleanUtils.isTrue(srtDELFlag) ) {
                for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                    if(ObjectIdentifier.equalsWithValue( allCarrierIDs.get(allCarrierCnt), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalCarrierID() )) {
                        loadPortID = strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalPortID();
                        break;
                    }
                    else if(ObjectIdentifier.equalsWithValue( allCarrierIDs.get(allCarrierCnt), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationCarrierID() ))
                    {
                        loadPortID = strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationPortID();
                        break;
                    }
                }
            } else if(CimBooleanUtils.isTrue(cmpDELFlag) ) {
                boolean carrierFoundFlag = false;
                compIDLen = CimArrayUtils.getSize(params.getSorterComponentJobIDseq());
                for(int i=0; i<compIDLen; i++) {
                    for(srtCmpCnt = 0; srtCmpCnt<srtCmpLen; srtCmpCnt++) {
                        if(ObjectIdentifier.equalsWithValue( params.getSorterComponentJobIDseq().get(i), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getSorterComponentJobID() )) {
                            if(ObjectIdentifier.equals( allCarrierIDs.get(allCarrierCnt), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalCarrierID() )) {
                                loadPortID = strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getOriginalPortID();
                                carrierFoundFlag = true;
                                break;
                            } else if(ObjectIdentifier.equals( allCarrierIDs.get(allCarrierCnt), strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationCarrierID() )) {
                                loadPortID = strSortJobListAttributes.getSorterComponentJobListAttributesList().get(srtCmpCnt).getDestinationPortID();
                                carrierFoundFlag = true;
                                break;
                            }
                        }
                    }
                    if(CimBooleanUtils.isTrue(carrierFoundFlag) ) break;
                }
                if(CimBooleanUtils.isFalse(carrierFoundFlag) ) {
                    //not found.
                    log.info(" carrierFoundFlag == false .continue. : {}",allCarrierIDs.get(allCarrierCnt) );
                    continue;
                }
            }

            //---------------------------------------
            // Carrier is reserved for dispatching?
            //---------------------------------------
            Boolean strCassette_dispatchState_Get_out = cassetteMethod.cassetteDispatchStateGet(objCommon, allCarrierIDs.get(allCarrierCnt));
            if(CimBooleanUtils.isTrue(strCassette_dispatchState_Get_out)) {
                log.info( "cassette_dispatchState_Get cassette is reserved");
                NPWRsvCanCarrierIDs.add(allCarrierIDs.get(allCarrierCnt));
                NPWRsvLoadPortIDs.add(loadPortID);
            }
        }

        //----------------------------------------
        // Call txNPWCarrierReserveCancelReq__090
        //----------------------------------------
        int NPWRsvLen = CimArrayUtils.getSize(NPWRsvCanCarrierIDs);
        log.info( "NPW Reserve Cancel .: {}" , NPWRsvLen );
        if( 0 < NPWRsvLen ) {
            List<Infos.NPWXferCassette>            strNPWXferCassette = new ArrayList<>();
            for(NPWRsvCnt=0; NPWRsvCnt<NPWRsvLen; NPWRsvCnt++) {
                log.info( "NPW Reserve Cancel Cassette : {}" , NPWRsvCanCarrierIDs.get(NPWRsvCnt));
                log.info( "NPW Reserve Cancel LoatPort : {}" , NPWRsvLoadPortIDs.get(NPWRsvCnt) );
                Infos.NPWXferCassette npwXferCassette =new Infos.NPWXferCassette();
                npwXferCassette.setCassetteID(NPWRsvCanCarrierIDs.get(NPWRsvCnt));
                npwXferCassette.setLoadPortID(NPWRsvLoadPortIDs.get(NPWRsvCnt));
                strNPWXferCassette.add(npwXferCassette);
            }

            transferManagementSystemService.sxNPWCarrierReserveCancelReq(objCommon, strSortJobListAttributes.getEquipmentID(),
                    ObjectIdentifier.fetchValue(strSortJobListAttributes.getPortGroupID()), strNPWXferCassette, params.getNotifyToTCSFlag(), params.getClaimMemo() );

        }

        // Caller is TxOnlineSorterRpt
        if ( !CimStringUtils.equals(objCommon.getTransactionID(), "OSRTR001" )) {
            Inputs.ObjSorterSorterJobEventMakeIn objSorterSorterJobEventMakeIn = new Inputs.ObjSorterSorterJobEventMakeIn();
            objSorterSorterJobEventMakeIn.setTransactionID(objCommon.getTransactionID());
            objSorterSorterJobEventMakeIn.setStrSortJobListAttributes(strSortJobListAttributes);
            objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBDELETED );
            log.info("#### sorter_sorterJobEvent_Make Action :{}",objSorterSorterJobEventMakeIn.getAction());
            eventMakeMethod.sorterSorterJobEventMake(objCommon,objSorterSorterJobEventMakeIn);
        }
    }

    public void sxSJConditionCheckReq(Infos.ObjCommon objCommon, Params.SortJobCheckConditionReqInParam params){
        sorterMethod.sorterCheckConditionForJobCreate( objCommon, params.getStrSorterComponentJobListAttributesSequence(), params.getEquipmentID(), params.getPortGroupID());
    }

    public ObjectIdentifier sxSJCreateReq(Infos.ObjCommon objCommon, Params.SJCreateReqParams params) {

        //------------------------------------------------------------------
        //
        //  Object Lock
        //
        //------------------------------------------------------------------
        Outputs.ObjLockModeOut strObject_lockMode_Get_out = null;

        int sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getIntValue();
        log.info("sorterJobLockFlag : {}", sorterJobLockFlag);
        int lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue();

        if ( 1 == sorterJobLockFlag ) {
            log.info("sorterJobLockFlag = 1");
            // Get required equipment lock mode

            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(params.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            strObject_lockMode_Get_out = objectMethod.objectLockModeGet(objCommon, objLockModeIn);


            log.info("calling object_lockMode_Get() : {}", params.getEquipmentID() );

            lockMode = strObject_lockMode_Get_out.getLockMode().intValue();
        }
        log.info( "lockMode : {}", lockMode );

        if ( lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE ) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn strAdvanced_object_Lock_in = new Inputs.ObjAdvanceLockIn();
            strAdvanced_object_Lock_in.setObjectID(params.getEquipmentID());
            strAdvanced_object_Lock_in.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvanced_object_Lock_in.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvanced_object_Lock_in.setLockType(strObject_lockMode_Get_out.getRequiredLockForMainObject());
            log.info("calling advanced_object_Lock() : {} ", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT );
            objectLockMethod.advancedObjectLock(objCommon, strAdvanced_object_Lock_in);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------------------------------
            //  Object Lock for Equipment
            //--------------------------------------------------------
            log.info("object_Lock EqpID : {}", params.getEquipmentID());
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        //-------------------------------------------------------------------
        // Call equipment_portInfo_Get
        //-------------------------------------------------------------------
        Infos.EqpPortInfo strEquipment_portInfo_Get_out = equipmentMethod.equipmentPortInfoGet(objCommon, params.getEquipmentID());

        int portLen = CimArrayUtils.getSize(strEquipment_portInfo_Get_out.getEqpPortStatuses());
        int portCnt = 0;
        for (portCnt = 0; portCnt < portLen; portCnt++) {
            log.info( "portGroup : {}  ", strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup());
            if (CimStringUtils.equals(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup(), params.getPortGroupID())) {
                objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(),
                        strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortID(),
                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }

        //--------------------------------------------------------
        //  Object Lock for Cassette
        //--------------------------------------------------------
        List<ObjectIdentifier> sortCassetteIDs = new ArrayList<>();
        int castCntAll             = 0;
        int sortCompoJobLen        = CimArrayUtils.getSize(params.getStrSorterComponentJobListAttributesSequence());
        Boolean origCastCheckedFlag = false;
        Boolean destCastCheckedFlag = false;
        int srtCompCnt    = 0;


        log.info("SorterComponentJob length =  {}", sortCompoJobLen);

        for( srtCompCnt = 0; srtCompCnt < sortCompoJobLen; srtCompCnt++ ){
            origCastCheckedFlag = false;
            destCastCheckedFlag = false;

            for(int castCnt = 0; castCnt < castCntAll; castCnt++ ) {
                if( ObjectIdentifier.equalsWithValue(params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getOriginalCarrierID(), sortCassetteIDs.get(castCnt)) ) {
                    log.info( "original cassette is already checked. cassetteID = {}", params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getOriginalCarrierID());
                    origCastCheckedFlag = true;
                }

                if( ObjectIdentifier.equalsWithValue(params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getDestinationCarrierID(), sortCassetteIDs.get(castCnt))) {
                    log.info( "destination cassette is already checked. cassetteID = {}", params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getDestinationCarrierID());
                    destCastCheckedFlag = true;
                }
            }

            if(CimBooleanUtils.isFalse(origCastCheckedFlag)) {
                log.info( "check originalCassetteID for object_lock target, cassetteID = {}", params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getOriginalCarrierID());

                sortCassetteIDs.add(params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getOriginalCarrierID());
                castCntAll++;
            }

            if( CimBooleanUtils.isFalse(destCastCheckedFlag)) {
                log.info( "check destinationCassetteID for object_lock target, cassetteID = {}", params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getDestinationCarrierID());

                sortCassetteIDs.add(params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getDestinationCarrierID());
                castCntAll++;
            }
        }

        log.info( "object_lock target cassetteID length = {}", castCntAll);

        for(int castCnt = 0; castCnt < castCntAll; castCnt++ ) {
            log.info( "object_lock for cassette. : {}", sortCassetteIDs.get(castCnt));
            objectLockMethod.objectLock(objCommon, CimCassette.class, sortCassetteIDs.get(castCnt));
        }

        //--------------------------------------------------------
        //  Object Lock for Lot
        //--------------------------------------------------------
        List<ObjectIdentifier> sortLotIDs = new ArrayList<>();
        int lotCnt                = 0;
        int lotCntAll             = 0;
        Boolean lotCheckedFlag = false;

        log.info( "SorterComponentJob length = {}", sortCompoJobLen);

        for( srtCompCnt = 0; srtCompCnt < sortCompoJobLen; srtCompCnt++ ) {
            int slotMapLen  = CimArrayUtils.getSize(params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getWaferSorterSlotMapList());
            int slotMapCnt  = 0;
            log.info("Slot Map  length = {}", slotMapLen );

            for( slotMapCnt=0; slotMapCnt<slotMapLen; slotMapCnt++) {
                lotCheckedFlag = false;
                for( lotCnt = 0; lotCnt < lotCntAll; lotCnt++ ) {
                    if( ObjectIdentifier.equalsWithValue(params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID(), sortLotIDs.get(lotCnt))) {
                        log.info( "lotID is already checked. lotID = : {}", params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID());
                        lotCheckedFlag = true;
                        break;
                    }
                }

                if( CimBooleanUtils.isFalse(lotCheckedFlag)) {
                    log.info( "check lotID for object_lock target, lotID = {}", params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID());

                    sortLotIDs.add(params.getStrSorterComponentJobListAttributesSequence().get(srtCompCnt).getWaferSorterSlotMapList().get(slotMapCnt).getLotID());
                    lotCntAll++;
                }
            }//loop of Slot Map
        }//loop of Component Job Attributes

        log.info("object_lock target lotID length = {}", lotCntAll);

        for( lotCnt = 0; lotCnt < lotCntAll; lotCnt++ ) {
            log.info( "object_lock for Lot. : {}", sortLotIDs.get(lotCnt));
            objectLockMethod.objectLock( objCommon, CimLot.class ,sortLotIDs.get(lotCnt));
        }

        //------------------------------------------------------------------
        //
        //  Check the validity of Sorter Job Information.
        //
        //------------------------------------------------------------------

        if (CimArrayUtils.getSize(strEquipment_portInfo_Get_out.getEqpPortStatuses()) == 0) {
            log.info( "eqpPortStatus.length() == 0.");
            Validations.check(retCodeConfig.getNotFoundPort());
        }

        boolean bArrivalCallFlag = false;
        String operationMode = null;
        portLen = CimArrayUtils.getSize(strEquipment_portInfo_Get_out.getEqpPortStatuses());
        for (portCnt = 0; portCnt < portLen; portCnt++) {
            log.info( "portGroup : {}", strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup());

            if (CimStringUtils.equals(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getPortGroup(), params.getPortGroupID())) {
                //------------------------------------------------------------------
                // Check Port Operation Mode
                //------------------------------------------------------------------
                log.info( "operationModeID : {}", strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getOperationModeID() );
                if ( ObjectIdentifier.equalsWithValue(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getOperationModeID(), BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1 )) {
                    log.info( "operationModeID is Auto-1...Call txNPWCarrierReserveReq");
                    bArrivalCallFlag = true;
                    operationMode = BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1;
                    break;
                }
                else if ( ObjectIdentifier.equalsWithValue(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(portCnt).getOperationModeID(), BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_2 )) {
                    operationMode = BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_2;
                }
            }
        }

        //------------------------------------------------------------------
        // Call sorter_CheckConditionForJobCreate
        //------------------------------------------------------------------
        log.info( "### Check Process for Sorter Job Create.");
        sorterMethod.sorterCheckConditionForJobCreate( objCommon, params.getStrSorterComponentJobListAttributesSequence(), params.getEquipmentID(), params.getPortGroupID());


        //------------------------------------------------------------------
        //  Create Sorter Job Information
        //------------------------------------------------------------------
        log.info("### Create Soter Job Information.");

        Inputs.SorterSorterJobCreateIn strSorter_sorterJobCreate_in = new Inputs.SorterSorterJobCreateIn();
        strSorter_sorterJobCreate_in.setStrSorterComponentJobListAttributesSeq(params.getStrSorterComponentJobListAttributesSequence());
        strSorter_sorterJobCreate_in.setEquipmentID(params.getEquipmentID());
        strSorter_sorterJobCreate_in.setPortGroupID(params.getPortGroupID());
        strSorter_sorterJobCreate_in.setWaferIDReadFlag(params.getWaferIDReadFlag());
        strSorter_sorterJobCreate_in.setSorterJobCategory(params.getSorterJobCategory());
        strSorter_sorterJobCreate_in.setOperationMode(operationMode);
        Infos.SortJobListAttributes strSorter_sorterJobCreate_out = sorterMethod.sorterSorterJobCreate(objCommon, strSorter_sorterJobCreate_in);

        //------------------------------------------------------------------
        //
        //  Notify Sorter Job ID to TCS.
        //    if operation Mode is Auto-1, call TCSMgr_SendSortJobNotificationReq
        //
        //------------------------------------------------------------------

        String tmpSleepTimeValue  = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        int sleepTimeValue = 0;
        int retryCountValue = 0;

        if (null == tmpSleepTimeValue || 0 == Integer.valueOf(tmpSleepTimeValue)) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS.intValue();
        } else {
            sleepTimeValue = Integer.valueOf(tmpSleepTimeValue);
        }

        if (null == tmpRetryCountValue || 0 == Integer.valueOf(tmpRetryCountValue))
        {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS.intValue();
        } else {
            retryCountValue = Integer.valueOf(tmpRetryCountValue);
        }

        log.info("env value of OM_EAP_CONNECT_SLEEP_TIME  = {}",sleepTimeValue);
        log.info("env value of OM_EAP_CONNECT_RETRY_COUNT = {}",retryCountValue);

        log.info( "### Notify Sort Job to TCS.");
        Inputs.SendSortJobNotificationReqIn sendSortJobNotificationReqIn = new Inputs.SendSortJobNotificationReqIn();
//        sendSortJobNotificationReqIn.setObjCommonIn(objCommon);
        sendSortJobNotificationReqIn.setUser(objCommon.getUser());
        sendSortJobNotificationReqIn.setEquipmentID(params.getEquipmentID());
        sendSortJobNotificationReqIn.setSorterJobID(strSorter_sorterJobCreate_out.getSorterJobID());
        sendSortJobNotificationReqIn.setPortGroupID(params.getPortGroupID());
        sendSortJobNotificationReqIn.setOpeMemo(params.getClaimMemo());

        //'retryCountValue + 1' means first try plus retry count
//        tcsMethod.sendTCSReq(TCSReqEnum.SORT_JOB_CREATE_REQ, sendSortJobNotificationReqIn );
        // 更改为EAPMethod方式
        IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), params.getEquipmentID(), null, true);
        if (eapRemoteManager!=null){
            for(int i = 0 ; i < (retryCountValue + 1) ; i++) {
                try {
                    eapRemoteManager.sendSortJobNotificationReq(sendSortJobNotificationReqIn);
                } catch (ServiceException ex){
                    if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                        if (i<retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                    }
                    throw ex;
                } catch (Exception ex){
                    if (i<retryCountValue){
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            ex.addSuppressed(e);
                            throw ex;
                        }
                    }
                    throw ex;
                }
                break;
            }
        }

        if(CimBooleanUtils.isTrue(bArrivalCallFlag )) {
            //----------------------------------------------------------
            //  Case operation mode 'Auto-1'
            //----------------------------------------------------------
            Infos.StartCassette strStartCassette_Src = new Infos.StartCassette();
            Infos.StartCassette strStartCassette_Dst = new Infos.StartCassette();

            strStartCassette_Src.setCassetteID(params.getStrSorterComponentJobListAttributesSequence().get(0).getOriginalCarrierID());
            strStartCassette_Src.setLoadPortID(params.getStrSorterComponentJobListAttributesSequence().get(0).getOriginalPortID());
            strStartCassette_Src.setUnloadPortID(params.getStrSorterComponentJobListAttributesSequence().get(0).getOriginalPortID());
            log.info( "  src cassetteID   ： {}", strStartCassette_Src.getCassetteID());
            log.info( "  src loadPortID   ： {}", strStartCassette_Src.getLoadPortID());
            log.info( "  src unloadPortID ： {}", strStartCassette_Src.getUnloadPortID());

            strStartCassette_Dst.setCassetteID(params.getStrSorterComponentJobListAttributesSequence().get(0).getDestinationCarrierID());
            strStartCassette_Dst.setLoadPortID(params.getStrSorterComponentJobListAttributesSequence().get(0).getDestinationPortID());
            strStartCassette_Dst.setUnloadPortID(params.getStrSorterComponentJobListAttributesSequence().get(0).getDestinationPortID());
            log.info( "  dest cassetteID : {}", strStartCassette_Dst.getCassetteID());
            log.info( "  dest loadPortID : {} ", strStartCassette_Dst.getLoadPortID());
            log.info( "  dest unloadPortID : {}", strStartCassette_Dst.getUnloadPortID());

            //Set Load Port Sequence Number.
            for(int nPortIndex = 0; nPortIndex < portLen; nPortIndex++) {
                log.info("Loop : {}", nPortIndex);
                if( CimStringUtils.equals(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(nPortIndex).getPortGroup(), params.getPortGroupID())
                        && ObjectIdentifier.equalsWithValue(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(nPortIndex).getPortID(), strStartCassette_Src.getLoadPortID())) {
                    strStartCassette_Src.setLoadSequenceNumber(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(nPortIndex).getLoadSequenceNumber());
                    log.info( "  src loadSequenceNumber  : {}", strStartCassette_Src.getLoadSequenceNumber());
                }

                if( CimStringUtils.equals(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(nPortIndex).getPortGroup(), params.getPortGroupID())
                        && ObjectIdentifier.equalsWithValue(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(nPortIndex).getPortID(), strStartCassette_Dst.getLoadPortID())) {
                    strStartCassette_Dst.setLoadSequenceNumber(strEquipment_portInfo_Get_out.getEqpPortStatuses().get(nPortIndex).getLoadSequenceNumber());
                    log.info( "  dest loadSequenceNumber : {}", strStartCassette_Dst.getLoadSequenceNumber());
                }
            }

            // Set Cassette to strStartCassette Sequence.
            List<Infos.StartCassette> strStartCassetteSequence = new ArrayList<>();

            if(ObjectIdentifier.equalsWithValue(strStartCassette_Src.getCassetteID(), strStartCassette_Dst.getCassetteID()) ) {
                log.info( "sourceCassettte == destinationCassette");
                strStartCassetteSequence.add(strStartCassette_Src);
            } else {
                log.info( "sourceCassettte != destinationCassette");
                strStartCassetteSequence.add(strStartCassette_Src);
                strStartCassetteSequence.add(strStartCassette_Dst);
            }

            int nCassetteLen = CimArrayUtils.getSize(strStartCassetteSequence);
            for(int nCassetteCnt = 0; nCassetteCnt < nCassetteLen; nCassetteCnt++) {
                //----------------------------------
                // Call txCarrierDetailInfoInq__170
                //----------------------------------
                log.info( "Call txCarrierDetailInfoInq__170");
                Results.CarrierDetailInfoInqResult strCarrierDetailInfoInqResult;
                Boolean durableOperationInfoFlag    = false;
                Boolean durableWipOperationInfoFlag = false;
                Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams = new Params.CarrierDetailInfoInqParams();
                carrierDetailInfoInqParams.setUser(objCommon.getUser());
                carrierDetailInfoInqParams.setCassetteID(strStartCassetteSequence.get(nCassetteCnt).getCassetteID());
                carrierDetailInfoInqParams.setDurableOperationInfoFlag(durableOperationInfoFlag);
                carrierDetailInfoInqParams.setDurableWipOperationInfoFlag(durableWipOperationInfoFlag);
                strCarrierDetailInfoInqResult = iBankInqService.sxCarrierDetailInfoInq(objCommon, carrierDetailInfoInqParams);

                if(CimBooleanUtils.isTrue(strCarrierDetailInfoInqResult.getCassetteStatusInfo().isEmptyFlag()) ){
                    log.info( "emptyFlag == true");
                    strStartCassetteSequence.get(nCassetteCnt).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                } else {
                    log.info( "emptyFlag == false");
                    strStartCassetteSequence.get(nCassetteCnt).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_OTHER);
                }
            }

            //--------------------------------------
            // Call txNPWCarrierReserveReq
            //--------------------------------------
            log.info( "Call txNPWCarrierReserveReq");
            Params.NPWCarrierReserveReqParams npwCarrierReserveReqParams = new Params.NPWCarrierReserveReqParams();
            npwCarrierReserveReqParams.setUser(objCommon.getUser());
            npwCarrierReserveReqParams.setEquipmentID(params.getEquipmentID());
            npwCarrierReserveReqParams.setPortGroupID(params.getPortGroupID());
            npwCarrierReserveReqParams.setStartCassetteList(strStartCassetteSequence);
            transferManagementSystemService.sxNPWCarrierReserveReq(objCommon, npwCarrierReserveReqParams);
        }

        //------------------------------------------------------------------
        // Create Event
        //------------------------------------------------------------------
        //Sort Job Create

        log.info( "Call sorter_sorterJobEvent_Make : {}", BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCREATE);
        Inputs.ObjSorterSorterJobEventMakeIn objSorterSorterJobEventMakeIn = new Inputs.ObjSorterSorterJobEventMakeIn();
        objSorterSorterJobEventMakeIn.setTransactionID(objCommon.getTransactionID());
        objSorterSorterJobEventMakeIn.setStrSortJobListAttributes(strSorter_sorterJobCreate_out);
        objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCREATE );
        objSorterSorterJobEventMakeIn.setSorterJobCategory(params.getSorterJobCategory());
        log.info("#### sorter_sorterJobEvent_Make Action :{}",objSorterSorterJobEventMakeIn.getAction());

        eventMethod.sorterSorterJobEventMake(objCommon, objSorterSorterJobEventMakeIn);


        if(CimStringUtils.equals( operationMode, BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_1)) {
            //Sort Job Start
            log.info( "Call sorter_sorterJobEvent_Make : {}", BizConstant.SP_SORTER_JOB_ACTION_SORTJOBSTART);
            objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBSTART );
            eventMethod.sorterSorterJobEventMake(objCommon, objSorterSorterJobEventMakeIn );

            //Component Job Xfer
            log.info( "Call sorter_sorterJobEvent_Make : {}", BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBSTART);
            objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBSTART );
            eventMethod.sorterSorterJobEventMake(objCommon, objSorterSorterJobEventMakeIn );
        }
        // Set out parameter
        log.info( "Created sorter job ID is : {}", strSorter_sorterJobCreate_out.getSorterJobID());
        return strSorter_sorterJobCreate_out.getSorterJobID();
    }

    public void sxSJStartReq(Infos.ObjCommon objCommon, Infos.SJStartReqInParm params, String claimMemo) {

        Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
        eqpInfoInqParams.setEquipmentID(params.getEquipmentID());
        eqpInfoInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoInqParams.setRequestFlagForStatusInfo(false);
        eqpInfoInqParams.setRequestFlagForPMInfo(false);
        eqpInfoInqParams.setRequestFlagForPortInfo(true);
        eqpInfoInqParams.setRequestFlagForChamberInfo(false);
        eqpInfoInqParams.setRequestFlagForStockerInfo(false);
        eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(false);
        eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(false);
        eqpInfoInqParams.setRequestFlagForRSPPortInfo(false);
        eqpInfoInqParams.setRequestFlagForEqpContainerInfo(false);
        Results.EqpInfoInqResult strEqpInfoInqResult = equipmentInqService.sxEqpInfoInq(objCommon, eqpInfoInqParams);


        //--------------------------------------------
        // Check machine type
        //--------------------------------------------
        if (!CimStringUtils.equals(strEqpInfoInqResult.getEquipmentBasicInfo().getEquipmentCategory(), BizConstant.SP_MC_CATEGORY_WAFERSORTER) ) {
            log.info("Machine Category is not WaferSorter : {}", strEqpInfoInqResult.getEquipmentBasicInfo().getEquipmentCategory());
            Validations.check(retCodeConfig.getMachineTypeNotSorter());
        }

        Outputs.ObjLockModeOut strObjectLockModeGetOut = null;
        Inputs.ObjLockModeIn strObjectLockModeGetIn = new Inputs.ObjLockModeIn();

        String sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getValue();
        log.info("sorterJobLockFlag : {}", sorterJobLockFlag);
        Long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;

        if (CimStringUtils.equals(sorterJobLockFlag,"1")) {
            log.info("sorterJobLockFlag = 1");

            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------
            Inputs.SorterSorterJobLockDRIn  strSorter_sorterJob_LockDR_in = new Inputs.SorterSorterJobLockDRIn();
            strSorter_sorterJob_LockDR_in.setSorterJobID(params.getSorterJobID());
            strSorter_sorterJob_LockDR_in.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);

            log.info("calling sorter_sorterJob_LockDR()");
            String strSorter_sorterJob_LockDR_out = sorterMethod.sorterSorterJobLockDR(objCommon, strSorter_sorterJob_LockDR_in);

            // Get required equipment lock mode
            strObjectLockModeGetIn.setObjectID(params.getEquipmentID());
            strObjectLockModeGetIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strObjectLockModeGetIn.setFunctionCategory(BizConstant.SP_FUNCTIONCATEGORY_SORTERTXID);
            strObjectLockModeGetIn.setUserDataUpdateFlag(false);

            log.info("calling object_lockMode_Get() : {}", params.getEquipmentID() );
            strObjectLockModeGetOut = objectMethod.objectLockModeGet(objCommon, strObjectLockModeGetIn);

            lockMode = strObjectLockModeGetOut.getLockMode();
        }
        log.info("lockMode : {}", lockMode );

        Inputs.ObjAdvanceLockIn strAdvanced_object_Lock_in = new Inputs.ObjAdvanceLockIn();

        if ( lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue() ) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Lock Equipment Main Object
            log.info("calling advanced_object_Lock() : {}", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT );

            strAdvanced_object_Lock_in.setObjectID(params.getEquipmentID());
            strAdvanced_object_Lock_in.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvanced_object_Lock_in.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvanced_object_Lock_in.setLockType(strObjectLockModeGetOut.getRequiredLockForMainObject());
            log.info("calling advanced_object_Lock() : {} ", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT );
            objectLockMethod.advancedObjectLock(objCommon, strAdvanced_object_Lock_in);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------------------
            // Machine Object Lock
            //--------------------------------------------
            log.info( "Machine Object Lock ");
            objectLockMethod.objectLock( objCommon, CimMachine.class ,params.getEquipmentID());
        }

        //--------------------------------------------
        // Port Object Lock
        //--------------------------------------------
        log.info( "Port Object Lock");
        int lenPort = CimArrayUtils.getSize(strEqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses());
        log.info( "lenPort : {}", lenPort);
        for ( int i=0 ; i < lenPort; i++ ){
            log.info( "PortID : {}", strEqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(i).getPortID());
            if (!ObjectIdentifier.equalsWithValue(params.getPortGroupID(), strEqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(i).getPortGroup())) {
                // Different PortGroup ignores.
                continue; //(i)
            }

            log.info( "object_LockForEquipmentResource()");
            objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(), strEqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(i).getPortID(),
                    BizConstant.SP_CLASSNAME_POSPORTRESOURCE);

        } //(i)

        //--------------------------------------------
        // Cassette Object Lock
        //--------------------------------------------
        log.info( "Cassette Object Lock");
        int lenCast = CimArrayUtils.getSize(params.getStrStartCassette());
        log.info("lenCast ： {}", lenCast);
        for (int i=0 ; i < lenCast; i++ ) {
            objectLockMethod.objectLock(objCommon, CimCassette.class, params.getStrStartCassette().get(i).getCassetteID());
        } //(i)

        //--------------------------------------------
        // Get status of parent Job info
        //--------------------------------------------
        log.info( "txSJListInq()");
        Params.SJListInqParams sjListInqParams = new Params.SJListInqParams();
        sjListInqParams.setSorterJobID(params.getSorterJobID());
        List<Infos.SortJobListAttributes> strSJListInqResult = sortInqService.sxSJListInq(objCommon, sjListInqParams);

        if ( 0 == CimArrayUtils.getSize(strSJListInqResult)){
            log.info( "Sorter Job length was 0");
            Validations.check(retCodeConfigEx.getNotFoundFssortjob());
        }
        String parentSorterJobStatus = strSJListInqResult.get(0).getSorterJobStatus();
        log.info("parentSorterJobStatus : {}", parentSorterJobStatus);

        // process check flags
        Boolean bAllProcessOK = true;
        Boolean bNPWReserveResult = true;
        Boolean bJobStatusChangeResult = true;
        Boolean bMultiCarrierXfer = true;

        // for system message
        String  systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_SORTERR;


        //--------------------------------------------
        // txNPWCarrierReserveReq
        //--------------------------------------------
        log.info( "txNPWCarrierReserveReq()");
        StringBuffer resultReasonTextStr =  new StringBuffer();
        Params.NPWCarrierReserveReqParams npwCarrierReserveReqParams = new Params.NPWCarrierReserveReqParams();
        npwCarrierReserveReqParams.setStartCassetteList(params.getStrStartCassette());
        npwCarrierReserveReqParams.setEquipmentID(params.getEquipmentID());
        npwCarrierReserveReqParams.setPortGroupID(ObjectIdentifier.fetchValue(params.getPortGroupID()));
        try {
            transferManagementSystemService.sxNPWCarrierReserveReq(objCommon, npwCarrierReserveReqParams);
        } catch (ServiceException e) {
            bAllProcessOK = false;
            bNPWReserveResult = false;
            resultReasonTextStr.append("sxNPWCarrierReserveReq failed. \\n")
                    .append("### messageText = ").append(e.getCode()).append("\\n")
                    .append("### reasonText = ").append(e.getReasonText()).append("\\n")
                    .append("### transactionID = ").append(e.getTransactionID()).append("\\n");
        }
        // if SorterJobStatus of parent is already 'Executing', Don't change it twice.
        if (CimStringUtils.equals(parentSorterJobStatus, BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING) && bAllProcessOK ) {
            //--------------------------------------------
            // txSJStatusChgRpt (Parent) --> 'Executing'
            //--------------------------------------------
            log.info( "txSJStatusChgRpt (Parent -> 'Executing')");
            Params.SJStatusChgRptParams sjStatusChgRptParams = new Params.SJStatusChgRptParams();
            sjStatusChgRptParams.setEquipmentID(params.getEquipmentID());
            sjStatusChgRptParams.setPortGroup(ObjectIdentifier.fetchValue(params.getPortGroupID()));
            sjStatusChgRptParams.setJobID(params.getSorterJobID());
            sjStatusChgRptParams.setJobType(BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB);
            sjStatusChgRptParams.setJobStatus(BizConstant.SP_SORTERJOBSTATUS_EXECUTING);
            try {
                this.sxSJStatusChgRpt(objCommon,sjStatusChgRptParams);
            } catch (ServiceException e) {
                bAllProcessOK = false;
                bJobStatusChangeResult = false;
                resultReasonTextStr.append("sxSJStatusChgRpt (Parent ->'Executing') failed.\\n")
                        .append("### messageText = ").append(e.getCode()).append("\\n")
                        .append("### reasonText = ").append(e.getReasonText()).append("\\n")
                        .append("### transactionID = ").append(e.getTransactionID()).append("\\n");
            }
        }

        ObjectIdentifier componentJobID = params.getComponentJobID();

        if ( bAllProcessOK ) {
            //--------------------------------------------
            // txSJStatusChgRpt (Child) --> 'Executing'
            //--------------------------------------------
            log.info( "txSJStatusChgRpt (Child -> 'Executing')");
            Params.SJStatusChgRptParams sjStatusChgRptParams = new Params.SJStatusChgRptParams();
            sjStatusChgRptParams.setEquipmentID(params.getEquipmentID());
            sjStatusChgRptParams.setPortGroup(ObjectIdentifier.fetchValue(params.getPortGroupID()));
            sjStatusChgRptParams.setJobID(componentJobID);
            sjStatusChgRptParams.setJobType(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB);
            sjStatusChgRptParams.setJobStatus(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER);
            try {
                this.sxSJStatusChgRpt(objCommon,sjStatusChgRptParams);
            } catch (ServiceException e) {
                bAllProcessOK = false;
                bJobStatusChangeResult = false;
                resultReasonTextStr.append("sxSJStatusChgRpt (Parent ->'Executing') failed.\\n")
                        .append("### messageText = ").append(e.getCode()).append("\\n")
                        .append("### reasonText = ").append(e.getReasonText()).append("\\n")
                        .append("### transactionID = ").append(e.getTransactionID()).append("\\n");
            }
        }

        if ( bAllProcessOK ) {
            //--------------------------------------------
            // txMultipleCarrierTransferReq
            //--------------------------------------------
            log.info( "SxMultipleCarrierTransferReq()");
            try {
                transferManagementSystemService.sxMultipleCarrierTransferReq(objCommon, false, params.getTransportType(), params.getStrCarrierXferReq());
            } catch (ServiceException e) {
                bAllProcessOK = false;
                bMultiCarrierXfer = false;
                resultReasonTextStr.append("sxMultipleCarrierTransferReq (Parent ->'Executing') failed.\\n")
                        .append("### messageText = ").append(e.getCode()).append("\\n")
                        .append("### reasonText = ").append(e.getReasonText()).append("\\n")
                        .append("### transactionID = ").append(e.getTransactionID()).append("\\n");
            }
        }

        // Post-processing
        if ( !bAllProcessOK ) {
            log.info( "bAllProcessOK is false ---> Post-processing");

            if ( bNPWReserveResult ) {
                //--------------------------------------------
                // Arrival Carrier Cancel
                //--------------------------------------------
                log.info( "txNPWCarrierReserveCancelReq()");
                List<Infos.NPWXferCassette> strNPWXferCassette = new ArrayList<>();
                lenCast = CimArrayUtils.getSize(params.getStrStartCassette());

                for (int i=0 ; i < lenCast; i++ ) {
                    Infos.NPWXferCassette npwXferCassette = new Infos.NPWXferCassette();
                    npwXferCassette.setLoadSequenceNumber(params.getStrStartCassette().get(i).getLoadSequenceNumber());
                    npwXferCassette.setCassetteID(params.getStrStartCassette().get(i).getCassetteID());
                    npwXferCassette.setLoadPurposeType(params.getStrStartCassette().get(i).getLoadPurposeType());
                    npwXferCassette.setLoadPortID(params.getStrStartCassette().get(i).getLoadPortID());
                    npwXferCassette.setUnloadPortID(params.getStrStartCassette().get(i).getUnloadPortID());
                    strNPWXferCassette.add(npwXferCassette);
                } //(i)

                Boolean notifyToTCSFlag = true;
                try {
                    transferManagementSystemService.sxNPWCarrierReserveCancelReq(objCommon,params.getEquipmentID(), ObjectIdentifier.fetchValue(params.getPortGroupID()),strNPWXferCassette, notifyToTCSFlag, "" ); //claimMemo
                } catch (ServiceException e) {
                    resultReasonTextStr.append("sxNPWCarrierReserveCancelReq failed.\\n")
                            .append("### messageText = ").append(e.getCode()).append("\\n")
                            .append("### reasonText = ").append(e.getReasonText()).append("\\n")
                            .append("### transactionID = ").append(e.getTransactionID()).append("\\n");
                }
            }

            //--------------------------------------------
            // txSJStatusChgRpt (Child) --> 'Error'
            //--------------------------------------------
            log.info( "txSJStatusChgRpt (Child) --> 'Error'");
            Params.SJStatusChgRptParams sjStatusChgRptParams = new Params.SJStatusChgRptParams();
            sjStatusChgRptParams.setEquipmentID(params.getEquipmentID());
            sjStatusChgRptParams.setPortGroup(ObjectIdentifier.fetchValue(params.getPortGroupID()));
            sjStatusChgRptParams.setJobID(componentJobID);
            sjStatusChgRptParams.setJobType(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB);
            sjStatusChgRptParams.setJobStatus(BizConstant.SP_SORTERJOBSTATUS_ERROR);
            try {
                this.sxSJStatusChgRpt(objCommon, sjStatusChgRptParams);
            } catch (ServiceException e) {
                resultReasonTextStr.append("sxNPWCarrierReserveCancelReq failed.\\n")
                        .append("### messageText = ").append(e.getCode()).append("\\n")
                        .append("### reasonText = ").append(e.getReasonText()).append("\\n")
                        .append("### transactionID = ").append(e.getTransactionID()).append("\\n");
            }

            //--------------------------------------------
            // txAlertMessageRpt
            //--------------------------------------------
            Results.AlertMessageRptResult strAlertMessageRptResult;
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(systemMsgCode);
            alertMessageRptParams.setSystemMessageText(resultReasonTextStr.toString());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(params.getEquipmentID());
            alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            strAlertMessageRptResult = systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            log.info("rc = RC_SORTJOB_START_FAIL");
        } else {
            log.info("bAllProcessOK is true");
        }
    }

    public void sxSJStatusChgRpt(Infos.ObjCommon objCommon, Params.SJStatusChgRptParams params){

        //init
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier jobID = params.getJobID();
        String jobStatus = params.getJobStatus();
        String jobType = params.getJobType();
        String portGroup = params.getPortGroup();
        User user = params.getUser();
        String claimMemo = params.getOpeMemo();

        //---------------------------------
        // Check Input parameter
        //---------------------------------
        log.info("Check Input parameter");
        //Check Length
        if (CimStringUtils.isEmpty(jobID.getValue()) || CimStringUtils.isEmpty(jobType) || CimStringUtils.isEmpty(jobStatus)){
            log.error("(0 == CIMFWStrLen(jobID) ||  0 == CIMFWStrLen(jobType) || 0 == CIMFWStrLen(jobStatus) ) ");
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        log.info("Check Length(JobID, JobType, JobStatus).  OK");
        //Check JogType contents
        if (!CimStringUtils.equals(BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB,jobType)
                && !CimStringUtils.equals(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB,jobType)){
            log.info("Job Type is invalid data.{}",jobType);
            throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidSorterJobType(),jobType));
        }
        log.info("Check JobType contents.   OK");
        Long sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getLongValue();
        if (1 == sorterJobLockFlag){
            log.info("sorterJobLockFlag = 1");
            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------
            ObjectIdentifier sorterJobID = new ObjectIdentifier();
            ObjectIdentifier sorterComponentJobID = new ObjectIdentifier();
            ObjectIdentifier dummyID = new ObjectIdentifier();
            if (CimStringUtils.equals(BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB,jobType)){
                log.info("jobType = SP_Sorter_Job_Type_SorterJob");
                sorterJobID = jobID;
            }else {
                log.info("jobType != SP_Sorter_Job_Type_SorterJob,{}",jobType);
                sorterComponentJobID = jobID;
            }
            log.info("calling sorter_sorterJob_LockDR()");
            Inputs.SorterSorterJobLockDRIn sorterSorterJobLockDRIn = new Inputs.SorterSorterJobLockDRIn();
            sorterSorterJobLockDRIn.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
            sorterSorterJobLockDRIn.setSorterJobID(sorterJobID);
            sorterSorterJobLockDRIn.setSorterComponentJobID(sorterComponentJobID);
            log.info("calling sorter_sorterJob_LockDR()");
            sorterMethod.sorterSorterJobLockDR(objCommon, sorterSorterJobLockDRIn);
        }
        //---------------------------------------------------
        //
        // Sort Job Status
        //
        //---------------------------------------------------
        if (CimStringUtils.equals(BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB,jobType)){
            log.info("Change Sort Job Status:{},{}",jobType,jobStatus);
            //--------------------------------------------------
            // Check Inpara
            //
            //       SP_SorterJobStatus_Wait_To_Executing
            //       SP_SorterJobStatus_Executing
            //       SP_SorterJobStatus_Completed
            //       SP_SorterJobStatus_Error
            //--------------------------------------------------
            if (!CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING,jobStatus)
                    && !CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_EXECUTING,jobStatus)
                    && !CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_COMPLETED,jobStatus)
                    && !CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_ERROR,jobStatus)){
                log.info("Sort Job Status is invalid.{}",jobStatus);
                throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidSorterJobStatus(),"",jobStatus));
            }
            //---------------------------------
            // Get Sort job information
            //---------------------------------
            log.info("Call sorter_jobList_GetDR ");
            //【step2】 - sorter_jobList_GetDR
            Inputs.ObjSorterJobListGetDRIn sorterJobListGetDRIn = new Inputs.ObjSorterJobListGetDRIn();
            sorterJobListGetDRIn.setSorterJob(jobID);
            List<Infos.SortJobListAttributes> sorterJobListGetDROut = sorterMethod.sorterJobListGetDR(objCommon, sorterJobListGetDRIn);
            int srtLotLen = CimArrayUtils.getSize(sorterJobListGetDROut);
            if (0 == srtLotLen){
                log.error("strSorter_jobList_GetDR_out.strSortJobListAttributesSeq.length() <= 0");
                throw new ServiceException(new OmCode(retCodeConfigEx.getNotFoundSorterjob(),sorterJobListGetDRIn.getSorterJob().getValue()));
            }
            //-------------------------------------------------
            // Current Sort Job Status is Wait To Executing
            //     -> Executing   OK
            //     -> Error       OK
            // The OTHER status changes are NG.
            //-------------------------------------------------
            log.info("Current Sort job status..{}",sorterJobListGetDROut.get(0).getSorterJobStatus());
            if (CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING,
                    sorterJobListGetDROut.get(0).getSorterJobStatus())){
                if (!CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_EXECUTING,jobStatus)
                        && !CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_ERROR,jobStatus)){
                    log.info("Sort Job Status can NOT change to {}",jobStatus);
                    throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),sorterJobListGetDROut.get(0).getSorterJobStatus(),jobStatus));
                }
            }
            //-----------------------------------------
            // Current Sort Job Status is Executing
            // Executing
            //     -> Completed   OK
            //     -> Error       OK
            // The OTHER status changes are NG.
            //-----------------------------------------
            else if (CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_EXECUTING,sorterJobListGetDROut.get(0).getSorterJobStatus())){
                if (!CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_COMPLETED,jobStatus)
                        && !CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_ERROR,jobStatus)){
                    log.error("Sort Job Status can NOT change to {}",jobStatus);
                    throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),sorterJobListGetDROut.get(0).getSorterJobStatus(),jobStatus));
                }
            }else {
                log.info("Sort Job Status can NOT change to {}",jobStatus);
                throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),sorterJobListGetDROut.get(0).getSorterJobStatus(),jobStatus));
            }
            //--------------------------------------------------------------------
            // Change Sort Job Status
            //--------------------------------------------------------------------
            log.info("Call sorter_sorterJob_status_UpdateDR");
            //【step3】 - sorter_sorterJob_status_UpdateDR
            sorterMethod.sorterSorterJobStatusUpdateDR(objCommon,jobID,jobStatus);
            //------------------------------------------------------------------
            // Create Event
            //------------------------------------------------------------------
            //【step4】 - sorter_sorterJobEvent_Make
            log.info("#### Call sorter_sorterJobEvent_Make");
            Infos.SortJobListAttributes strSortJobListAttributesEvent = sorterJobListGetDROut.get(0);
            strSortJobListAttributesEvent.setSorterJobStatus(jobStatus);
            Inputs.ObjSorterSorterJobEventMakeIn objSorterSorterJobEventMakeIn = new Inputs.ObjSorterSorterJobEventMakeIn();
            objSorterSorterJobEventMakeIn.setTransactionID(objCommon.getTransactionID());
            objSorterSorterJobEventMakeIn.setStrSortJobListAttributes(strSortJobListAttributesEvent);
            objSorterSorterJobEventMakeIn.setSorterJobCategory(params.getSorterJobCategory());
            objSorterSorterJobEventMakeIn.setClaimMemo(claimMemo);
            if (CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_EXECUTING,jobStatus)){
                objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBSTART);
            }else if (CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_COMPLETED,jobStatus)){
                objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBCOMP);
            }else if (CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_ERROR,jobStatus)){
                objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBERROR);
                if (CimArrayUtils.getSize(objSorterSorterJobEventMakeIn.getStrSortJobListAttributes().getSorterComponentJobListAttributesList()) == 0){
                    List<Infos.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = new ArrayList<>();
                    objSorterSorterJobEventMakeIn.getStrSortJobListAttributes().setSorterComponentJobListAttributesList(sorterComponentJobListAttributesList);
                }
                objSorterSorterJobEventMakeIn.getStrSortJobListAttributes().getSorterComponentJobListAttributesList().get(0).setComponentSorterJobStatus("");
            }
            log.info("#### sorter_sorterJobEvent_Make Action :{}",objSorterSorterJobEventMakeIn.getAction());
            eventMakeMethod.sorterSorterJobEventMake(objCommon,objSorterSorterJobEventMakeIn);

        }
        //---------------------------------------------------
        //
        // Component Job Status
        //
        //---------------------------------------------------
        else if (CimStringUtils.equals(BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB,jobType)){
            log.info("### Change Component Job Status:{},{}",jobType,jobStatus);
            //--------------------------------------------------------
            // Check Inpara
            //
            //       SP_SorterComponentJobStatus_Wait_To_Executing
            //       SP_SorterComponentJobStatus_Xfer
            //       SP_SorterComponentJobStatus_Executing
            //       SP_SorterComponentJobStatus_Completed
            //       SP_SorterComponentJobStatus_Error
            //--------------------------------------------------------
            if (CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_WAIT_TO_EXECUTING,jobStatus)
                    && CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER,jobStatus)
                    && CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_EXECUTING,jobStatus)
                    && CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED,jobStatus)
                    && CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR,jobStatus)){
                log.info("Component Job Status is invalid.{}",jobStatus);
                throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),"",jobStatus));
            }
            //------------------------
            // Get Sort Job ID
            //------------------------
            log.info("Get Sort Job ID with Component Job ID:{}",jobID.getValue());
            //【step5】 - sorter_componentJob_Info_Get_ByComponentJobIDDR
            Outputs.ObjSorterComponentJobInfoGetByComponentJobIDDROut sorterComponentJobInfoGetByComponentJobIDDROutRetCode = sorterMethod.sorterComponentJobInfoGetByComponentJobIDDR(objCommon,jobID);
            log.info("Got sorterJobID.. {}",sorterComponentJobInfoGetByComponentJobIDDROutRetCode.getSorterJobID().getValue());
            //---------------------------------
            // Get Sort job information
            //---------------------------------
            log.info("#### Call sorter_jobList_GetDR ");
            //step6 - sorter_jobList_GetDR
            Inputs.ObjSorterJobListGetDRIn sorterJobListGetDRIn = new Inputs.ObjSorterJobListGetDRIn();
            sorterJobListGetDRIn.setSorterJob(sorterComponentJobInfoGetByComponentJobIDDROutRetCode.getSorterJobID());
            List<Infos.SortJobListAttributes> sorterJobListGetDROut = sorterMethod.sorterJobListGetDR(objCommon, sorterJobListGetDRIn);
            int srtJobLen = CimArrayUtils.getSize(sorterJobListGetDROut);
            if (0 == srtJobLen){
                log.error("strSorter_jobList_GetDR_out.strSortJobListAttributesSeq.length() <= 0");
                throw new ServiceException(new OmCode(retCodeConfigEx.getNotFoundSorterjob(),sorterJobListGetDRIn.getSorterJob().getValue()));
            }
            Infos.SortJobListAttributes strSortJobListAttributesEvent = sorterJobListGetDROut.get(0);
            List<Infos.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = new ArrayList<>();
            strSortJobListAttributesEvent.setSorterComponentJobListAttributesList(sorterComponentJobListAttributesList);
            int compLen = CimArrayUtils.getSize(sorterJobListGetDROut.get(0).getSorterComponentJobListAttributesList());
            int completedCnt = 0;
            for (int i = 0; i < compLen; i++) {
                String tmpCompStatus = sorterJobListGetDROut.get(0).getSorterComponentJobListAttributesList().get(i).getComponentSorterJobStatus();
                log.info("Component Job ID and Current Component job status:{},{}",sorterJobListGetDROut.get(0).getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID().getValue(),tmpCompStatus);
                if (!CimStringUtils.equals(sorterJobListGetDROut.get(0).getSorterComponentJobListAttributesList().get(i).getSorterComponentJobID().getValue(),jobID.getValue())){
                    if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED,jobStatus)){
                        completedCnt ++;
                    }
                    continue;
                }else {
                    log.info("Create Event Record . jobID ,jobStatus:{},{}",jobID.getValue(),jobStatus);
                    strSortJobListAttributesEvent.getSorterComponentJobListAttributesList().add(sorterJobListGetDROut.get(0).getSorterComponentJobListAttributesList().get(i));
                    strSortJobListAttributesEvent.getSorterComponentJobListAttributesList().get(0).setSorterComponentJobID(jobID);
                    strSortJobListAttributesEvent.getSorterComponentJobListAttributesList().get(0).setComponentSorterJobStatus(jobStatus);
                }
                //-----------------------------------------------------
                // Current Component Job Status is Wait To Executing
                //     -> Xfer        OK
                //     -> Error       OK
                // The OTHER status changes are NG.
                //-----------------------------------------------------
                log.info("Target Component JobID 's Current Component job status..{}",tmpCompStatus);
                if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_WAIT_TO_EXECUTING,tmpCompStatus)){
                    if (CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER,jobStatus)
                            && CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR,jobStatus)){
                        log.error("Component Job Status can NOT change to {}",jobStatus);
                        throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),tmpCompStatus,jobStatus));
                    }
                }
                //-----------------------------------------------------
                // Current Component Job Status is Xfer
                //     -> Executing   OK
                //     -> Error       OK
                // The OTHER status changes are NG.
                //-----------------------------------------------------
                else if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER,tmpCompStatus)){
                    if (CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_EXECUTING,jobStatus)
                            && CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR,jobStatus)){
                        log.error("Component Job Status can NOT change to {}",jobStatus);
                        throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),tmpCompStatus,jobStatus));
                    }
                }
                //-----------------------------------------------------
                // Current Component Job Status is Executing
                //     -> Completed   OK
                //     -> Error       OK
                // The OTHER status changes are NG.
                //-----------------------------------------------------
                else if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_EXECUTING,tmpCompStatus)){
                    if (CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED,jobStatus)
                            && CimStringUtils.unEqual(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR,jobStatus)){
                        log.error("Component Job Status can NOT change to {}",jobStatus);
                        throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),tmpCompStatus,jobStatus));
                    }
                }else {
                    log.error("Sort Job Status can NOT change to {}",jobStatus);
                    throw new ServiceException(new OmCode(retCodeConfigEx.getCannotSortJobStatusChange(),sorterJobListGetDROut.get(0).getSorterJobStatus(),jobStatus));
                }
            }
            //----------------------------------------------
            // Need to change Sort Job Status ?
            //----------------------------------------------
            String chgSortJobStatus = null;
            if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR,jobStatus)){
                log.info("Sort Job Status is also changed to Error.");
                chgSortJobStatus = BizConstant.SP_SORTERJOBSTATUS_ERROR;
            }
            //--------------------------------------------------------------------
            // Change Component Job Status
            //--------------------------------------------------------------------
            log.info("#### Call sorter_componentJob_status_UpdateDR ");
            sorterMethod.sorterComponentJobStatusUpdateDR(objCommon,jobID,jobStatus);

            log.info(" Need to change Sort Job Status? {}",chgSortJobStatus);
            if (CimStringUtils.isNotEmpty(chgSortJobStatus)){
                //--------------------------------------------------------------------
                // Change Sort Job Status
                //--------------------------------------------------------------------
                log.info("#### Call sorter_sorterJob_status_UpdateDR ");
                //【step8】 - sorter_sorterJob_status_UpdateDR
                sorterMethod.sorterSorterJobStatusUpdateDR(objCommon,sorterComponentJobInfoGetByComponentJobIDDROutRetCode.getSorterJobID(),chgSortJobStatus);
            }
            //------------------------------------------------------------------
            // Create Event
            //------------------------------------------------------------------
            Inputs.ObjSorterSorterJobEventMakeIn objSorterSorterJobEventMakeIn = new Inputs.ObjSorterSorterJobEventMakeIn();
            objSorterSorterJobEventMakeIn.setTransactionID(objCommon.getTransactionID());
            objSorterSorterJobEventMakeIn.setStrSortJobListAttributes(strSortJobListAttributesEvent);
            objSorterSorterJobEventMakeIn.setSorterJobCategory(params.getSorterJobCategory());
            objSorterSorterJobEventMakeIn.setClaimMemo(claimMemo);
            if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_XFER,jobStatus)){
                objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBXFER);
            }else if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_EXECUTING,jobStatus)){
                objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBSTART);
            }else if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_COMPLETED,jobStatus)){
                objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_COMPONENTJOBCOMP);
            }else if (CimStringUtils.equals(BizConstant.SP_SORTERCOMPONENTJOBSTATUS_ERROR,jobStatus)) {
                objSorterSorterJobEventMakeIn.setAction(BizConstant.SP_SORTER_JOB_ACTION_SORTJOBERROR);
                objSorterSorterJobEventMakeIn.getStrSortJobListAttributes().setSorterJobStatus(BizConstant.SP_SORTERJOBSTATUS_ERROR);
            }
            log.info("#### sorter_sorterJobEvent_Make Action :{}",objSorterSorterJobEventMakeIn.getAction());
            eventMakeMethod.sorterSorterJobEventMake(objCommon,objSorterSorterJobEventMakeIn);
        }
    }

    public void sxSortJobPriorityChangeReq(Infos.ObjCommon objCommon, Params.SortJobPriorityChangeReqParam param){
        //------------------------------------------------------------------
        //  Check Process
        //------------------------------------------------------------------
        int i =0;
        int j =0;
        log.info("Check Input parameter");
        // Check length
        if(0 == CimArrayUtils.getSize(param.getJobIDs()) ||  CimStringUtils.isEmpty(param.getJobType()) ) {
            log.info("(0 == jobIDs.length() ||  0 == CIMFWStrLen(jobType)) ");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //Check jobID duplication
        for(i = 0; i < CimArrayUtils.getSize(param.getJobIDs()); i++) {
            for(j=i+1 ; j< CimArrayUtils.getSize(param.getJobIDs()); j++) {
                if(ObjectIdentifier.equalsWithValue(param.getJobIDs().get(i), param.getJobIDs().get(j))) {
                    Validations.check(retCodeConfig.getInvalidParameter());
                }
            }
        }

        if(CimStringUtils.unEqual(param.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB)
                && CimStringUtils.unEqual(param.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB)) {
            Validations.check(new OmCode(retCodeConfigEx.getInvalidSorterJobType(), param.getJobType()));
        }


        //------------------------------------------------------------------
        //  Sort Job
        //------------------------------------------------------------------
        int jobLen = CimArrayUtils.getSize(param.getJobIDs());
        String sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getValue();
        if (CimStringUtils.equals("1",sorterJobLockFlag )) {
            //---------------------------------
            // Lock Sort Jobs
            //---------------------------------
            for ( i = 0; i < jobLen; i++ ) {
                ObjectIdentifier sorterJobID = null;
                ObjectIdentifier sorterComponentJobID = null;

                if (CimStringUtils.equals(param.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB) ) {
                    sorterJobID = param.getJobIDs().get(i);
                } else {
                    sorterComponentJobID = param.getJobIDs().get(i);
                }

                Inputs.SorterSorterJobLockDRIn strSorter_sorterJob_LockDR_in = new Inputs.SorterSorterJobLockDRIn();
                strSorter_sorterJob_LockDR_in.setSorterJobID(sorterJobID);
                strSorter_sorterJob_LockDR_in.setSorterComponentJobID(sorterComponentJobID);
                strSorter_sorterJob_LockDR_in.setLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);

                sorterMethod.sorterSorterJobLockDR ( objCommon, strSorter_sorterJob_LockDR_in );
            }
        }


        Outputs.ObjLockModeOut strObject_lockMode_Get_out = null;

        ObjectIdentifier tmpEqpID = null;
        ObjectIdentifier tmpEqpID2 = null;
        if(CimStringUtils.equals(param.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB) ) {
            for(i=0; i<jobLen; i++) {
                //---------------------------------
                // Get Sort job information
                //---------------------------------
                Inputs.ObjSorterJobListGetDRIn strSorter_jobList_GetDR_in = new Inputs.ObjSorterJobListGetDRIn();
                strSorter_jobList_GetDR_in.setSorterJob(param.getJobIDs().get(i));
                List<Infos.SortJobListAttributes> strSorter_jobList_GetDR_out = sorterMethod.sorterJobListGetDR(objCommon, strSorter_jobList_GetDR_in);

                if(CimArrayUtils.isEmpty(strSorter_jobList_GetDR_out)) {
                    Validations.check(new OmCode(retCodeConfigEx.getNotFoundSorterjob(), ObjectIdentifier.fetchValue(param.getJobIDs().get(i))));
                }

                if( 0 == i ) {

                    tmpEqpID  = strSorter_jobList_GetDR_out.get(0).getEquipmentID();
                    tmpEqpID2 = tmpEqpID;
                    Long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;

                    if (CimStringUtils.equals("1", sorterJobLockFlag )) {

                        // Get required equipment lock mode
                        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                        objLockModeIn.setObjectID(strSorter_jobList_GetDR_out.get(0).getEquipmentID());
                        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
                        objLockModeIn.setUserDataUpdateFlag(false);
                        strObject_lockMode_Get_out = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

                        lockMode = strObject_lockMode_Get_out.getLockMode();
                    }

                    if ( lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue() ) {

                        // Lock Equipment Main Object
                        Inputs.ObjAdvanceLockIn strAdvanced_object_Lock_in = new Inputs.ObjAdvanceLockIn();
                        strAdvanced_object_Lock_in.setObjectID(strSorter_jobList_GetDR_out.get(0).getEquipmentID());
                        strAdvanced_object_Lock_in.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                        strAdvanced_object_Lock_in.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                        strAdvanced_object_Lock_in.setLockType(strObject_lockMode_Get_out.getRequiredLockForMainObject());
                        log.info("calling advanced_object_Lock() : {} ", BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT );
                        objectLockMethod.advancedObjectLock(objCommon, strAdvanced_object_Lock_in );

                    } else {
                        //-----------------------------
                        // Lock Equipment Object
                        //-----------------------------
                        objectLockMethod.objectLock( objCommon, CimMachine.class,strSorter_jobList_GetDR_out.get(0).getEquipmentID() );

                    }
                    continue;
                }

                tmpEqpID2 = strSorter_jobList_GetDR_out.get(0).getEquipmentID();

                //----------------------
                // the same Equipment ?
                //----------------------
                if(!ObjectIdentifier.equalsWithValue(tmpEqpID, tmpEqpID2 )) {
                    Validations.check(new OmCode(retCodeConfigEx.getCannotPriorityChange(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB));
                }

                //-------------------------------------------------------
                // Sort Job Status is WaitToExecuting after the second ?
                //-------------------------------------------------------
                if(!CimStringUtils.equals(BizConstant.SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING, strSorter_jobList_GetDR_out.get(0).getSorterJobStatus() )) {
                    Validations.check(new OmCode(retCodeConfigEx.getCannotPriorityChange(), BizConstant.SP_SORTER_JOB_TYPE_SORTERJOB));
                }
            }
        }
        //------------------------------------------------------------------
        //  Component Job
        //------------------------------------------------------------------
        else if(CimStringUtils.equals(param.getJobType(), BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB)) {
            Boolean notWaitFoundFlag = false;
            ObjectIdentifier tmpSortJobID = null;
            ObjectIdentifier tmpSortJobID2 = null;
            for(i=0; i<jobLen; i++) {
                //------------------------
                // Get Sort Job ID
                //------------------------
                // typedef struct objSorter_componentJob_Info_Get_ByComponentJobIDDR_in_struct{
                //      ObjectIdentifier   sorterComponentJobID;
                //      any                siInfo;
                // }objSorter_componentJob_Info_Get_ByComponentJobIDDR_in;

                Outputs.ObjSorterComponentJobInfoGetByComponentJobIDDROut strSorter_componentJob_Info_Get_ByComponentJobIDDR_out = sorterMethod.sorterComponentJobInfoGetByComponentJobIDDR(objCommon, param.getJobIDs().get(i));
                if( 0 == i ) {
                    tmpSortJobID   = strSorter_componentJob_Info_Get_ByComponentJobIDDR_out.getSorterJobID();
                    tmpSortJobID2  = tmpSortJobID;
                }

                tmpSortJobID2 = strSorter_componentJob_Info_Get_ByComponentJobIDDR_out.getSorterJobID();

                if(!CimObjectUtils.equalsWithValue( tmpSortJobID, tmpSortJobID2 )) {
                    Validations.check(new OmCode(retCodeConfigEx.getCannotPriorityChange(), BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB));
                }
            }


            //---------------------------------
            // Get Sort job information
            //---------------------------------
            Inputs.ObjSorterJobListGetDRIn strSorter_jobList_GetDR_in = new Inputs.ObjSorterJobListGetDRIn();
            strSorter_jobList_GetDR_in.setSorterJob(tmpSortJobID);
            List<Infos.SortJobListAttributes> strSorter_jobList_GetDR_out = sorterMethod.sorterJobListGetDR(objCommon, strSorter_jobList_GetDR_in);


            if(CimArrayUtils.isEmpty(strSorter_jobList_GetDR_out)) {
                Validations.check(new OmCode(retCodeConfigEx.getNotFoundSorterjob(), ObjectIdentifier.fetchValue(tmpSortJobID)));
            }

            long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
            if (CimStringUtils.equals("1", sorterJobLockFlag)) {

                // Get required equipment lock mode

                Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                objLockModeIn.setObjectID(strSorter_jobList_GetDR_out.get(0).getEquipmentID());
                objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objLockModeIn.setFunctionCategory(ThreadContextHolder.getTransactionId());
                objLockModeIn.setUserDataUpdateFlag(false);
                strObject_lockMode_Get_out = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

                lockMode = strObject_lockMode_Get_out.getLockMode();
            }

            if ( lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE ) {

                // Lock Equipment Main Object
                Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvancedObjectLockIn.setObjectID(strSorter_jobList_GetDR_out.get(0).getEquipmentID());
                objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                objAdvancedObjectLockIn.setLockType(strObject_lockMode_Get_out.getRequiredLockForMainObject());
                objectLockMethod.advancedObjectLock( objCommon ,objAdvancedObjectLockIn );

            } else {
                //-----------------------------
                // Lock Equipment Object
                //-----------------------------
                objectLockMethod.objectLock( objCommon, CimMachine.class, strSorter_jobList_GetDR_out.get(0).getEquipmentID());
            }

            //-------------------------------------------------
            // Sort Job Status is WaitToExecuting after two ?
            //-------------------------------------------------
            int cmpLen = CimArrayUtils.getSize(strSorter_jobList_GetDR_out.get(0).getSorterComponentJobListAttributesList());
            for(i=0; i<jobLen; i++) {
                if( 0 == i ) {
                    continue;
                }
                notWaitFoundFlag = false;
                for(j=0; j<cmpLen; j++) {

                    if(ObjectIdentifier.equalsWithValue(param.getJobIDs().get(i), strSorter_jobList_GetDR_out.get(0).getSorterComponentJobListAttributesList().get(j).getSorterComponentJobID())
                            && !CimStringUtils.equals( BizConstant.SP_SORTERCOMPONENTJOBSTATUS_WAIT_TO_EXECUTING, strSorter_jobList_GetDR_out.get(0).getSorterComponentJobListAttributesList().get(j).getComponentSorterJobStatus())) {
                        notWaitFoundFlag = true;
                        break;
                    }
                }//CompoList
                if(notWaitFoundFlag) {
                    Validations.check(new OmCode(retCodeConfigEx.getCannotPriorityChange(), BizConstant.SP_SORTER_JOB_TYPE_COMPONENTJOB));
                }
            }
        }

        //------------------------------------------------------------------
        //  Priority Change
        //------------------------------------------------------------------
        Inputs.SorterLinkedJobUpdateDRIn strSorter_linkedJobUpdateDR_in = new Inputs.SorterLinkedJobUpdateDRIn();
        strSorter_linkedJobUpdateDR_in.setJobIDs(param.getJobIDs());
        strSorter_linkedJobUpdateDR_in.setJobType(param.getJobType());
        sorterMethod.sorterLinkedJobUpdateDR(objCommon, strSorter_linkedJobUpdateDR_in );
    }

    public void sxWaferSorterActionRegisterReq(Infos.ObjCommon objCommon, Params.WaferSorterActionRegisterReqParams params) {

        //init
        ObjectIdentifier equipmentID = params.getEquipmentID();
        List<Infos.WaferSorterActionList> strWaferSorterActionListSequence = params.getStrWaferSorterActionListSequence();

        //----------------------------------------
        //  Check Equipment ID is SORTER or
        //----------------------------------------
        log.info("Check Transaction ID and equipment Category combination.");
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        //---------------------------------
        //   Check In Parameter ALL Record
        //---------------------------------
        int nListLen = CimArrayUtils.getSize(strWaferSorterActionListSequence);
        for (int i = 0; i < nListLen; i++) {
            //-----------------------------
            //    Check Action Code exist
            //-----------------------------
            if (!CimStringUtils.equals(BizConstant.SP_SORTER_READ,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_START,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_MINIREAD,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_POSITIONCHANGE,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_LOTTRANSFEROFF,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_SEPARATEOFF,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_COMBINEOFF,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_JUSTIN,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_JUSTOUT,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_WAFERENDOFF,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_ADJUSTTOMM,strWaferSorterActionListSequence.get(i).getActionCode())
                    &&!CimStringUtils.equals(BizConstant.SP_SORTER_SCRAP,strWaferSorterActionListSequence.get(i).getActionCode())){
                log.error("actionCode Check Error = {}",strWaferSorterActionListSequence.get(i).getActionCode());
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidActionCode(),strWaferSorterActionListSequence.get(i).getActionCode()));
            }
            //------------------------------------------------------------
            //   Check Equipment ID ( Comp First Record and other record )
            //------------------------------------------------------------
            if (!ObjectIdentifier.equalsWithValue(strWaferSorterActionListSequence.get(i).getEquipmentID(),equipmentID)){
                log.error("equipmentID Check Error = {}", ObjectIdentifier.fetchValue(strWaferSorterActionListSequence.get(i).getEquipmentID()));
                throw new ServiceException(new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(strWaferSorterActionListSequence.get(i).getEquipmentID())));
            }
        }
        //-----------------------------
        //   Insert to DB
        //-----------------------------
        //【step2】 - waferSorter_actionList_InsertDR
        waferMethod.waferSorterActionListInsertDR(objCommon,strWaferSorterActionListSequence,equipmentID);
    }

    @Override
    public void sxWaferSlotmapChangeReq(Infos.ObjCommon objCommon, Params.WaferSlotmapChangeReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        List<Infos.WaferTransfer> strWaferXferSeq = params.getStrWaferXferSeq();
        //------------------------------------------------------------
        // Retrieve Equipment's onlineMode
        //------------------------------------------------------------
        Long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
        String onlineMode = BizConstant.SP_EQP_ONLINEMODE_OFFLINE;
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)){
            // object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.WAFER_SORT_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                // advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
            }
            // equipment_onlineMode_Get
            onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);
        }

        //-----------------------
        // Cassette ID Collection
        //-----------------------
        Boolean bCassetteFind = false;
        List<ObjectIdentifier> casts = new ArrayList<>();
        int lenCasIDSeq = 0;
        List<String> castIDSeq = new ArrayList<>();
        int lenWaferXferSeq = CimArrayUtils.getSize(strWaferXferSeq);
        for (int i = 0; i < lenWaferXferSeq; i++) {
            //------------------------------------------
            // Collect CassetteID of DestinationCassette
            //------------------------------------------
            bCassetteFind = false;
            lenCasIDSeq = CimArrayUtils.getSize(casts);
            for (int j = 0; j < lenCasIDSeq; j++) {
                if (ObjectIdentifier.equalsWithValue(strWaferXferSeq.get(i).getDestinationCassetteID(), casts.get(j))) {
                    bCassetteFind = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bCassetteFind)) {
                casts.add(strWaferXferSeq.get(i).getDestinationCassetteID());
                if (CimBooleanUtils.isTrue(strWaferXferSeq.get(i).getBDestinationCassetteManagedByOM())) {
                    castIDSeq.add(strWaferXferSeq.get(i).getDestinationCassetteID().getValue());
                }
            }
            //---------------------------------------
            // Collect CassetteID of OriginalCassette
            //---------------------------------------
            bCassetteFind = false;
            lenCasIDSeq = CimArrayUtils.getSize(casts);
            for (int j = 0; j < lenCasIDSeq; j++) {
                if (ObjectIdentifier.equalsWithValue(strWaferXferSeq.get(i).getOriginalCassetteID(), casts.get(j))) {
                    bCassetteFind = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bCassetteFind)) {
                casts.add(strWaferXferSeq.get(i).getOriginalCassetteID());
                if (CimBooleanUtils.isTrue(strWaferXferSeq.get(i).getBOriginalCassetteManagedByOM())) {
                    castIDSeq.add(strWaferXferSeq.get(i).getOriginalCassetteID().getValue());
                }
            }
        }
        // advanced_object_Lock
        if(!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                    (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, castIDSeq));
        }
        //--------------------------------------------------
        // Collect cassette's equipment ID / Cassette ObjRef
        //--------------------------------------------------
        lenCasIDSeq = CimArrayUtils.getSize(casts);
        log.info("lenCasIDSeq :{} and objectIdentifierSequence :{}", lenCasIDSeq, casts);
        for (int i = 0; i < lenCasIDSeq; i++) {
            ObjectIdentifier controlJobID = null;
            //---------------------------------------------------
            // Error Judgement
            // If rc was RC_NOT_FOUND_CASSETTE, It is OK for FOSB
            //---------------------------------------------------
            int retCode = 0;
            try {
                controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, casts.get(i));
            } catch (ServiceException e){
                retCode = e.getCode();
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())){
                    throw e;
                }
            }
            if (!ObjectIdentifier.isEmptyWithValue(controlJobID)){
                throw new ServiceException(retCodeConfig.getNotClearedControlJob());
            }
            if (retCode == 0){
                /*-------------------------------*/
                /*   Check SorterJob existence   */
                /*-------------------------------*/
                List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                cassetteIDs.add(casts.get(i));
                Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
                objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDs);
                objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
                waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
            }
        }
        //contamination check
        for (Infos.WaferTransfer waferTransfer : strWaferXferSeq){
            contaminationMethod.lotWaferSlotMapChangeCheck(objCommon,waferTransfer);
        }

        //------------------------------------------------------------
        // If bNotifyToTCSFlag is true, Verify request inside of MM
        // and notify to EAP
        //------------------------------------------------------------
        if (CimBooleanUtils.isTrue(params.getBNotifyToTCS())) {
            //sorter_waferTransferInfo_Verify
            sorterMethod.sorterWaferTransferInfoVerify(objCommon, strWaferXferSeq, BizConstant.SP_SORTER_LOCATION_CHECKBY_SLOTMAP);

            //---------------------------------------
            // Check input parameter and
            // Server data condition
            //---------------------------------------
            cassetteMethod.cassetteCheckConditionForWaferSort(objCommon, strWaferXferSeq, params.getEquipmentID());
            //【TODO】【TODO - NOTIMPL】- TCSMgr_SendWaferSlotmapChangeReq
        }
        //------------------------------------------------------------
        // If bNotifyToTCSFlag is false, Update MM Data and
        // does not notify to EAP
        //------------------------------------------------------------
        else if (CimBooleanUtils.isFalse(params.getBNotifyToTCS())) {
            Params.WaferSlotmapChangeRptParams waferSlotmapChangeRptParams = new Params.WaferSlotmapChangeRptParams();
            waferSlotmapChangeRptParams.setEquipmentID(params.getEquipmentID());
            waferSlotmapChangeRptParams.setStrWaferXferSeq(strWaferXferSeq);
            waferSlotmapChangeRptParams.setUser(params.getUser());
            waferSlotmapChangeRptParams.setClaimMemo(params.getClaimMemo());
            sxWaferSlotmapChangeRpt(objCommon, waferSlotmapChangeRptParams);
        } else {
            throw new ServiceException(retCodeConfig.getInvalidSorterOperation());
        }
    }

    @Override
    public void sxCarrierExchangeReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.WaferTransfer> waferXferList, String claimMemo) {
        Validations.check(equipmentID == null, "equipmentID can not be null");

        Long lockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
        if (!CimStringUtils.isEmpty(equipmentID.getValue())) {
            // Step1 - object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.CASSETTE_EXCHANGE_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                // Lock Equipment Main Object
                // Step2 - advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
            }
        }
        //	Step3 - equipment_CheckConditionForEmptyCassetteEarlyOut, Check eqp allow early carrier out or not
        boolean earlyOutFlag = true;
        try {
            equipmentMethod.equipmentCheckConditionForEmptyCassetteEarlyOut(objCommon, equipmentID);
        } catch (ServiceException e) {
            earlyOutFlag = false;
        }
        log.debug("sxCarrierExchangeReq(): Check whether eqp is Internal Buffer or not.");
        if (earlyOutFlag){
            String equipmentCategory = null;
            if (lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_ELEMENT)) {
                //	Step4 - equipment_brInfo_GetDR__120
                Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
                equipmentCategory = eqpBrInfo.getEquipmentCategory();
                if (CimStringUtils.equals(equipmentCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)) {
                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
                    List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
                    int lenPortInfo = CimArrayUtils.getSize(eqpPortStatuses);
                    // Step6 - object_LockForEquipmentResource
                    for (int i = 0; i < lenPortInfo; i++){
                        objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, eqpPortStatuses.get(i).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                    }
                }
            }
        }
        //Collect participant cassettes (original/destination) from WaferTransferSequence.
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(waferXferList)) {
            waferXferList.forEach(waferTransfer -> {
                if (Boolean.TRUE.equals(waferTransfer.getBOriginalCassetteManagedByOM())) {
                    cassetteIDs.add(waferTransfer.getOriginalCassetteID());
                    log.debug("sxCarrierExchangeReq(): OM managed original cassette found.");
                }
                if (Boolean.TRUE.equals(waferTransfer.getBDestinationCassetteManagedByOM())) {
                    cassetteIDs.add(waferTransfer.getDestinationCassetteID());
                    log.debug("sxCarrierExchangeReq(): OM managed destination cassette found.");
                }
            });
        }

        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
            //	Step7 - cassette_controlJobID_Get,Get cassette's ControlJobID
            ObjectIdentifier cassetteControlJobIDOut = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteIDs.get(0));
            if (!ObjectIdentifier.isEmpty(cassetteControlJobIDOut)) {
                log.debug("sxCarrierExchangeReq(): Get controljob Info");
                //	Step8 - controlJob_containedLot_Get
                List<String> processLotList = new ArrayList<>();
                List<Infos.ControlJobCassette> controlJobCassettes = controlJobMethod.controlJobContainedLotGet(objCommon, cassetteControlJobIDOut);
                if (!CimArrayUtils.isEmpty(controlJobCassettes)) {
                    for (Infos.ControlJobCassette controlJobCassette : controlJobCassettes) {
                        if (CimArrayUtils.isNotEmpty(controlJobCassette.getControlJobLotList())) {
                            controlJobCassette.getControlJobLotList().forEach(controlJobLot -> {
                                if (controlJobLot.getOperationStartFlag()) {
                                    processLotList.add(controlJobLot.getLotID().getValue());
                                }
                            });
                        }
                    }
                }
                if (!CimArrayUtils.isEmpty(processLotList)) {
                    // Step9 - advanced_object_Lock
                    // Lock Equipment ProcLot Element (Write)
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT,
                            (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, processLotList));
                }
                // Lock eqp LoadCassette Element (Write)
                // Step10 - advanced_object_Lock
                List<String> loadCastSeq = cassetteIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                        (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, loadCastSeq));
                //Lock controljob Object
                // Step11 - object_Lock
                objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobIDOut);
            }
        }
        // Step12 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        Inputs.ObjWaferSorterJobCheckForOperation checkForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        checkForOperation.setCassetteIDList(cassetteIDs);
        checkForOperation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, checkForOperation);

        //---------------------------------------
        // Check input parameter for
        // wafer transfer (Sorter operation)
        //---------------------------------------
        sorterMethod.sorterWaferTransferInfoVerify(objCommon, waferXferList, BizConstant.SP_SORTER_LOCATION_CHECKBY_SLOTMAP);

        //contamination check
        for (Infos.WaferTransfer waferTransfer: waferXferList){
            if (StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn()){
                //qiandao mode
                contaminationMethod.carrierExchangeCheckQiandaoMode(objCommon, waferTransfer.getDestinationCassetteID(), waferTransfer.getWaferID(),"");
            }else {
                //OMS mode
                contaminationMethod.lotWaferCarrierExchangeChangeCheck(objCommon, waferTransfer,"");
            }
        }

        //	Step15 - cassette_CheckConditionForExchange
        cassetteMethod.cassetteCheckConditionForExchange(objCommon, equipmentID, waferXferList);
        if (!CimArrayUtils.isEmpty(cassetteIDs)) {
            for (ObjectIdentifier cassetteID : cassetteIDs) {
                String interFabXferState = cassetteMethod.cassetteInterFabXferStateGet(objCommon, cassetteID);
                Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                        retCodeConfig.getInterfabInvalidLotXferstateForReq(), cassetteID, interFabXferState);
                /*-------------------------------*/
                /*  Get lot list in carrier      */
                /*-------------------------------*/
                //	Step17 - cassette_lotList_GetDR
                Infos.LotListInCassetteInfo cassetteLotListOut = cassetteMethod.cassetteLotListGetDR(objCommon, cassetteID);
                List<ObjectIdentifier> lotIDList = cassetteLotListOut.getLotIDList();
                if (!CimObjectUtils.isEmpty(lotIDList)) {
                    for (ObjectIdentifier lotID : lotIDList) {
                        String lotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
                        Validations.check(CimStringUtils.equals(lotInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_ORIGINDELETING),
                                retCodeConfigEx.getInterfabInvalidXferstateDeleting(), lotID);
                    }
                }
            }
        }
        //	Step19 - sorter_waferTransferInfo_Restructure
        Outputs.ObjSorterWaferTransferInfoRestructureOut restructureSorterWaferTransferInfoOut = sorterMethod.sorterWaferTransferInfoRestructure(objCommon, waferXferList);
        //Reject if request lot is backup processing.
        List<Infos.PLot> lotList = restructureSorterWaferTransferInfoOut.getLotList();
        if (!CimArrayUtils.isEmpty(lotList)) {
            for (Infos.PLot pLot : lotList) {
                //	Step20 - lot_backupInfo_Get, Check Backup Info
                Infos.LotBackupInfo lotBackupInfoOut = lotMethod.lotBackupInfoGet(objCommon, pLot.getLotID());

                Validations.check(Boolean.FALSE.equals(lotBackupInfoOut.getCurrentLocationFlag())
                        || Boolean.TRUE.equals(lotBackupInfoOut.getTransferFlag()), retCodeConfig.getLotInOthersite());
            }
        }
        List<String> lotInventoryStateList = restructureSorterWaferTransferInfoOut.getLotInventoryStateList();
        if (!CimArrayUtils.isEmpty(lotInventoryStateList)) {
            ObjectIdentifier newCassetteID = new ObjectIdentifier();
            for (int i = 0; i < lotInventoryStateList.size(); i++) {
                if (CimStringUtils.equals(lotInventoryStateList.get(i), CIMStateConst.CIM_LOT_INVENTORY_STATE_INBANK)) {
                    List<Infos.PWafer> waferList = lotList.get(i).getWaferList();
                    for (Infos.PWafer pWafer : waferList) {
                        //	Step21 - wafer_materialContainer_Change
                        Infos.Wafer strWafer = new Infos.Wafer();
                        strWafer.setWaferID(pWafer.getWaferID());
                        strWafer.setSlotNumber(pWafer.getSlotNumber());
                        waferMethod.waferMaterialContainerChange(objCommon, newCassetteID, strWafer);
                        //Next, new relation between carrier-wafer should be created
                        //	Step23 - wafer_materialContainer_Change
                        Infos.Wafer waferInfo = new Infos.Wafer();
                        waferInfo.setWaferID(pWafer.getWaferID());
                        waferInfo.setSlotNumber(pWafer.getSlotNumber());
                        waferMethod.waferMaterialContainerChange(objCommon, restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), waferInfo);
                    }
                } else {
                    //	Step22 - lot_materialContainer_Change
                    lotMethod.lotMaterialContainerChange(newCassetteID, lotList.get(i));

                    //Next, new relation between carrier-wafer should be created
                    //	Step24 - lot_materialContainer_Change
                    lotMethod.lotMaterialContainerChange(restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), lotList.get(i));
                }
            }
        }
        //Collect cassette IDs of input parameter
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        CassetteMethod.collectCassetteIDsOfParams(waferXferList, cassetteIDList);
        //	Step25 - controlJob_relatedInfo_Update, Update Machine/controljob information
        List<ObjectIdentifier> cassetteIDStrList = new ArrayList<>(cassetteIDList);
        controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDStrList);
        if (earlyOutFlag){
            controlJobMethod.controlJobEmptyCassetteInfoDelete(objCommon, cassetteIDList);
        }
        //	Step26 - controlJob_emptyCassetteInfo_Delete, Remove empty cassette from Control Job if eqp configuration allow

        //	Step27 - cassette_multiLotType_Update, Update MultiLotType of Carrier
        for (ObjectIdentifier cassetteID : cassetteIDList) {
            try {
                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
            } catch (ServiceException e) {
                boolean checkFlag = e.getCode() != retCodeConfig.getSucc().getCode() && e.getCode() != retCodeConfig.getNotFoundCassette().getCode();
                Validations.check(checkFlag, new OmCode(e.getCode(), e.getMessage()));
            }
        }
        //	Step28 - lot_waferLotHistoryPointer_Update, Update WaferLotHistoryPointer of lot
        for (Infos.PLot pLot : lotList) {
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, pLot.getLotID());
        }
        // Step29 - lotWaferSortEvent_Make
        //---------------------------------------
        // Prepare input parameter of lotWaferSortEvent_Make()
        //---------------------------------------
        List<Infos.WaferTransfer> tmpWaferXferList = waferXferList;
        for (Infos.WaferTransfer waferTransfer : tmpWaferXferList) {
            boolean bStringifiedObjRefFilled = false;
            List<Infos.PLot> pLotList = restructureSorterWaferTransferInfoOut.getLotList();
            for (Infos.PLot pLot : pLotList) {
                List<Infos.PWafer> pWaferList = pLot.getWaferList();
                for (Infos.PWafer pWafer : pWaferList) {
                    if (ObjectIdentifier.equals(waferTransfer.getWaferID(), pWafer.getWaferID())) {
                        ObjectIdentifier waferID = waferTransfer.getWaferID();
                        waferID.setReferenceKey(pWafer.getWaferID().getReferenceKey());
                        bStringifiedObjRefFilled = true;
                        break;
                    }
                }
                if (bStringifiedObjRefFilled) {
                    break;
                }
            }
        }
        //---------------------------------------
        // Create Wafer Sort Event
        //---------------------------------------
        eventMethod.lotWaferSortEventMake(objCommon, TransactionIDEnum.CASSETTE_EXCHANGE_REQ.getValue(), tmpWaferXferList, claimMemo);
    }

    @Override
    public void sxWaferSlotmapChangeRpt(Infos.ObjCommon objCommon, Params.WaferSlotmapChangeRptParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        //---------------------------------------------------------------------------------
        // Collect participant cassettes (original/destination) from WaferTransferSequence.
        //---------------------------------------------------------------------------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        int waferMapLen = CimArrayUtils.getSize(params.getStrWaferXferSeq());
        for (int i = 0; i < waferMapLen; i++) {
            if (CimBooleanUtils.isTrue(params.getStrWaferXferSeq().get(i).getBDestinationCassetteManagedByOM())) {
                cassetteIDs.add(params.getStrWaferXferSeq().get(i).getDestinationCassetteID());
            }
            if (CimBooleanUtils.isTrue(params.getStrWaferXferSeq().get(i).getBOriginalCassetteManagedByOM())) {
                cassetteIDs.add(params.getStrWaferXferSeq().get(i).getOriginalCassetteID());
            }
        }
        log.info("cassetteIDs :{}", cassetteIDs);
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            // object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.WAFER_SORTER_RPT.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            Long lockMode = objLockModeOut.getLockMode();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                // advanced_object_Lock
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                        objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                // advanced_object_Lock
                List<String> loadCastSeq = cassetteIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
                objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                        BizConstant.SP_CLASSNAME_POSMACHINE,
                        BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                        (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, loadCastSeq));
            } else {
                objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
            }
        }
        //-----------------------------------
        // Lock cassette objects for update.
        //-----------------------------------
        // objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        //---------------------------------------
        // Check input parameter for
        // wafer transfer (Sorter operation)
        //---------------------------------------
        sorterMethod.sorterWaferTransferInfoVerify(objCommon, params.getStrWaferXferSeq(), BizConstant.SP_SORTER_LOCATION_CHECKBY_SLOTMAP);
        //---------------------------------------
        // Check input parameter and
        // Server data condition
        //---------------------------------------
        cassetteMethod.cassetteCheckConditionForWaferSort(objCommon, params.getStrWaferXferSeq(), params.getEquipmentID());

        //------------------------------------------------------------
        // Retrieve Equipment's onlineMode
        //------------------------------------------------------------
        String strEquipment_onlineMode_Get_out = BizConstant.SP_EQP_ONLINEMODE_OFFLINE;
        if (!ObjectIdentifier.isEmpty(params.getEquipmentID())) {
            strEquipment_onlineMode_Get_out = equipmentMethod.equipmentOnlineModeGet(objCommon, params.getEquipmentID());
        }

        Outputs.ObjSorterWaferTransferInfoRestructureOut restructureSorterWaferTransferInfoOut = sorterMethod.sorterWaferTransferInfoRestructure(objCommon, params.getStrWaferXferSeq());
        //------------------------------------------------------------
        //   Reject if request lot is backup processing.
        //------------------------------------------------------------
        int lotLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList());
        /*-----------------------------------*/
        /*   Check Backup Info               */
        /*-----------------------------------*/
        List<Infos.PLot> pLots = restructureSorterWaferTransferInfoOut.getLotList();
        for (int i = 0; i < lotLen; i++) {
            Infos.LotBackupInfo lotBackupInfoOut = lotMethod.lotBackupInfoGet(objCommon, pLots.get(i).getLotID());
            if (CimBooleanUtils.isFalse(lotBackupInfoOut.getCurrentLocationFlag())
                    || CimBooleanUtils.isTrue(lotBackupInfoOut.getTransferFlag())) {
                throw new ServiceException(retCodeConfig.getLotInOthersite());
            }
        }
        //---------------------------------------
        // At first, all relation between
        // carrier-wafer should be canceled
        // If bNotifyToTCS is true, Only Check Logic
        // works inside of wafer_materialContainer_Change()
        // or lot_materialContainer_Change()
        //---------------------------------------
        int nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
        log.info("nILen : {}", nILen);
        for (int i = 0; i < nILen; i++) {
            int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
            for (int j = 0; j < nJLen; j++) {
                Infos.PWafer pWafer = restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j);
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setWaferID(pWafer.getWaferID());
                strWafer.setSlotNumber(pWafer.getSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, null, strWafer);
            }
        }
        //---------------------------------------
        // Next, new relation between
        // carrier-wafer should be created
        //---------------------------------------
        nILen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotInventoryStateList());
        for (int i = 0; i < nILen; i++) {
            int nJLen = CimArrayUtils.getSize(restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList());
            for (int j = 0; j < nJLen; j++) {
                Infos.PWafer pWafer = restructureSorterWaferTransferInfoOut.getLotList().get(i).getWaferList().get(j);
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setWaferID(pWafer.getWaferID());
                strWafer.setSlotNumber(pWafer.getSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, restructureSorterWaferTransferInfoOut.getCassetteIDList().get(i), strWafer);
            }
        }
        //---------------------------------------
        // Collect Cassette IDs of input parameter
        //---------------------------------------
        nILen = CimArrayUtils.getSize(params.getStrWaferXferSeq());
        List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
        for (int i = 0; i < nILen; i++) {
            Boolean bCassetteAdded = false;
            int nJLen = CimArrayUtils.getSize(cassetteIDSeq);
            for (int j = 0; j < nJLen; j++) {
                if (ObjectIdentifier.equalsWithValue(cassetteIDSeq.get(j), params.getStrWaferXferSeq().get(i).getDestinationCassetteID())) {
                    bCassetteAdded = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bCassetteAdded)) {
                cassetteIDSeq.add(params.getStrWaferXferSeq().get(i).getDestinationCassetteID());
            }
            bCassetteAdded = false;
            nJLen = CimArrayUtils.getSize(cassetteIDSeq);
            for (int j = 0; j < nJLen; j++) {
                if (ObjectIdentifier.equalsWithValue(cassetteIDSeq.get(j),params.getStrWaferXferSeq().get(i).getOriginalCassetteID())) {
                    bCassetteAdded = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bCassetteAdded)) {
                cassetteIDSeq.add(params.getStrWaferXferSeq().get(i).getOriginalCassetteID());
            }
        }
        log.info("cassetteIDSeq : {}", cassetteIDSeq);
        //---------------------------------------
        // Update Machine/ControlJob related information
        //---------------------------------------
        controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDSeq);
        //---------------------------------------
        // Update Carrier Multi Lot Type
        //---------------------------------------
        nILen = CimArrayUtils.getSize(cassetteIDSeq);
        for (int i = 0; i < nILen; i++) {
            try {
                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteIDSeq.get(i));
            } catch (ServiceException e){
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(),e.getCode())){
                    throw e;
                }
            }
        }
        //---------------------------------------
        // Update WaferLotHistoryPointer of Lot
        //---------------------------------------
        // lot_waferLotHistoryPointer_Update
        for (int i = 0; i < lotLen; i++){
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, pLots.get(i).getLotID());
        }
        //---------------------------------------
        // Prepare input parameter of lotWaferSortEvent_Make()
        //---------------------------------------
        List<Infos.WaferTransfer> tmpWaferXferList = params.getStrWaferXferSeq();
        for (Infos.WaferTransfer waferTransfer : tmpWaferXferList){
            boolean bStringifiedObjRefFilled = false;
            for (Infos.PLot pLot : pLots){
                List<Infos.PWafer> waferList = pLot.getWaferList();
                for (Infos.PWafer pWafer : waferList){
                    if (ObjectIdentifier.equalsWithValue(waferTransfer.getWaferID(), pWafer.getWaferID())){
                        waferTransfer.getWaferID().setReferenceKey(pWafer.getWaferID().getReferenceKey());
                        bStringifiedObjRefFilled = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isTrue(bStringifiedObjRefFilled)){
                    break;
                }
            }
        }
        //---------------------------------------
        // Create Wafer Sort Event
        //---------------------------------------
        // lotWaferSortEvent_Make
        eventMethod.lotWaferSortEventMake(objCommon, TransactionIDEnum.WAFER_SORTER_RPT.getValue(), tmpWaferXferList, params.getClaimMemo());

    }


}
