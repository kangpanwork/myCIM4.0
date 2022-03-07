package com.fa.cim.common.constant;

import org.springframework.lang.NonNull;

/**
 * description:
 * <p>BizConstant : constant for biz which exists Type of String , Long , Boolean , Entity etc..<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/8        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/10/8 12:39
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public final class BizConstant {
    private BizConstant() {
    }

    public static final String EMPTY = "";
    public static final String BLANK = " ";
    public static final String COLON = ":";
    public static final String HYPHEN = "-";
    public static final String DOT = ".";
    public static final String PERCENT = "%";
    public static final String SEPARATOR_COMMA = ",";
    public static final String SEARCH_COUNT = "2";
    public static final String SEARCH_COUNT_MAX = "9999";
    public static final String CONST_FIVE_HUNDRED = "500";
    public static final String CONST_TWO_THOUSAND = "2000";

    public static final Long SP_DEFAULT_SLEEP_TIME_TCS = 1L;
    public static final Long SP_DEFAULT_RETRY_COUNT_TCS = 5L;
    public static final int SP_CAPACITY_INCREMENT_10 = 10;
    public static final int SP_CAPACITY_INCREMENT_100 = 100;
    public static final Integer SP_FLOWBATCH_CLEAR = 1;
    public static final Integer SP_FLOWBATCH_RECOVER = 0;
    public static final Long SP_EQP_LOCK_MODE_MIGRATION = -1L;
    public static final Long SP_EQP_LOCK_MODE_WRITE = 0L;
    public static final Long SP_OBJECT_LOCK_TYPE_WRITE = 0L;
    public static final Long SP_OBJECT_LOCK_TYPE_READ = 1L;
    public static final Long SP_EQP_LOCK_MODE_READ_WRITE = 1L;
    public static final Long SP_EQP_LOCK_MODE_ELEMENT = 2L;
    public static final String SP_ConfigurationCategory_EquipmentLockMode = "EquipmentLockMode";

    public static final long SP_RETICLE_DEFAULTGROUPSEQUENCENUMBER = 0;

    /**
     * PERSON Type
     */
    public static final String SP_PPT_SVC_MGR_PERSON = "OMS";
    public static final String SP_QTIME_WATCH_DOG_PERSON = "QTimeSentinel";
    public static final String SP_TIME_CONSTRAINT_WATCH_DOG_PERSON = "TimeConstraintSentinel";
    public static final String SP_LTIME_WATCH_DOG_PERSON = "LagTimeSentinel";
    public static final String SP_PPWATCH_DOG_PERSON = "PPWatcher";
    public static final String SP_POST_PROC_PERSON = "PostProc";
    public static final String SP_TCS_PERSON = "EAP";
    public static final String SP_XMS_PERSON = "TMS";
    public static final String SP_RXM_PERSON = "RXMS";
    public static final String SP_SAR_PERSON = "FAM";
    public static final String SP_RTD_PERSON = "RTD";
    public static final String SP_SPC_PERSON = "SPC";
    public static final String SP_DCSC_PERSON = "DCSC";
    public static final String SP_AMS_PERSON = "AMS";
    public static final String SP_ADM_PERSON = "ADM";
    public static final String SP_SENTINEL = "SENTINEL";
    public static final String SP_CARRIER_OUT_PERSON ="CarrierOutSentinel";
    public static final String SP_AM_SENTINEL_PERSON = "AMSentinel";
    public static final String SP_ARHS_PERSON = "ARHS";

    public static final String SIVIEW_SECURE_RELEASE_VERSION = "8.00";

    public static final String CONSTANT_QUANTITY_ZERO = "0";
    public static final String CONSTANT_QUANTITY_ONE = "1";
    public static final String PHASE_ZERO = "0";
    public static final String PHASE_ONE = "1";

    public static final String LEVEL_ZERO = "0";
    public static final String LEVEL_ONE = "1";
    public static final String LEVEL_TWO = "2";

    public static final String VALUE_ZERO = "0";
    public static final String VALUE_ONE = "1";
    public static final String VALUE_TWO = "2";

    //--------------------------------------------------------------------
    //   Future Actions
    //--------------------------------------------------------------------
    public static final String SP_HASHKEY_QTIMEFLAG = "qTimeFlag";
    public static final String SP_HASHKEY_FUTUREQTIMEFLAG = "futureQTimeFlag";
    public static final String SP_HASHKEY_FUTUREMINQTIMEFLAG = "futureMinQTimeFlag";
    public static final String SP_HASHKEY_FUTUREHOLDFLAG = "futureHoldFlag";
    public static final String SP_HASHKEY_FUTUREREWORKFLAG = "futureReworkFlag";
    public static final String SP_HASHKEY_ADCFLAG = "ADCFlag";
    public static final String SP_HASHKEY_PSMFLAG = "PSMFlag";
    public static final String SP_HASHKEY_FPCFLAG = "FPCFlag";
    public static final String SP_HASHKEY_SCRIPTFLAG = "ScriptFlag";
    public static final String SP_HASHKEY_PROCESSHOLDFLAG = "ProcessHoldFlag";

    public static final String SP_HASHDATA_FLAG_0 = "0";
    public static final String SP_HASHDATA_FLAG_1 = "1";
    public static final String SP_ADCSETTING_ASTERISK = "*";
    public static final String SP_KEY_SEPARATOR_DOT = ".";
    public static final String SP_VERSION_SEPARATOR = ".";

    public static final Integer SP_FLOWBATCH_LOWERLIMIT = 0;
    public static final Integer SP_FLOWBATCH_UPPERLIMIT = 1000;

    public static final Integer SP_MAXLIMITCOUNT_FOR_LISTINQ = 9999;

    public static final Long QTIME_TX_RETRIEVE_MAX_CNT = -1L;

    public static final Long SP_PAR_VAL_NO_CHANGE = 0L;
    public static final Long SP_PAR_VAL_ADD = 1L;
    public static final Long SP_PAR_VAL_UPDATE = 2L;
    public static final Long SP_PAR_VAL_DELETE = 3L;
    public static final Long SP_CHECK_MSG_DEFAULT_MAX_COUNT = 10L;

    public static final Integer SP_DISPATCH_PRECEDE_NOT_FOUND = -1;


    public static final String SP_HOLDLIST_HOLDMARK = "Hold";
    public static final String SP_HOLDLIST_RELEASEMARK = "Release";
    public static final String SP_MODETRANSITION_ALLOWABLE = "ModeChange OK";
    public static final String SP_MODETRANSITION_ALLOWABLEWITHCONDITION = "ModeChange OK WithCondition";
    public static final String SP_MODETRANSITION_NOTALLOWED = "ModeChange NG";
    public static final String SP_MOVEMENTTYPE_END = "End";
    public static final String SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION = "BackwardOperation";
    public static final String SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE = "BackwardStage";
    public static final String SP_MOVEMENTTYPE_MOVEFORWARDOPERATION = "ForwardOperation";
    public static final String SP_MOVEMENTTYPE_MOVEFORWARDSTAGE = "ForwardStage";
    public static final String SP_MOVEMENTTYPE_MOVENONPRODBANK = "MoveNonProBank";
    public static final String SP_MOVEMENTTYPE_MOVEWIPAREA = "MoveWIPArea";

    public static final String SP_OPERATIONCATEGORY_ADJUSTCHIP = "AdjustChip";
    public static final String SP_OPERATIONCATEGORY_BANKHOLD = "HoldLotInBank";
    public static final String SP_OPERATIONCATEGORY_BANKHOLDRELEASE = "ReleaseHoldLotInBank";
    public static final String SP_OPERATIONCATEGORY_BANKIN = "MoveToBank";
    public static final String SP_OPERATIONCATEGORY_BANKINCANCEL = "CancelMoveToBank";
    public static final String SP_OPERATIONCATEGORY_CASSETTECHANGE = "CarrierChange";
    public static final String SP_OPERATIONCATEGORY_CHIPSCRAP = "ChipScrap";
    public static final String SP_OPERATIONCATEGORY_CHIPSCRAPCANCEL = "ChipScrapCancel";
    public static final String SP_OPERATIONCATEGORY_CORRELATIONUSE = "CorrelationUse";
    public static final String SP_OPERATIONCATEGORY_CORRELATIONUSECANCEL = "CorrelationUseCancel";
    public static final String SP_OPERATIONCATEGORY_DEFECTDIE = "DefectDie";
    public static final String SP_OPERATIONCATEGORY_DIEADJUST = "ReviseDieCount";
    public static final String SP_OPERATIONCATEGORY_GATEPASS = "PassThru";
    public static final String SP_OPERATIONCATEGORY_LOCATEBACKWARD = "SkipBack";
    public static final String SP_OPERATIONCATEGORY_LOCATEFORWARD = "SkipFront";
    public static final String SP_OPERATIONCATEGORY_LOTHOLD = "HoldLot";
    public static final String SP_OPERATIONCATEGORY_LOTHOLDRELEASE = "ReleaseHoldLot";
    public static final String SP_OPERATIONCATEGORY_MERGE = "MergeLot";
    public static final String SP_OPERATIONCATEGORY_MONITORIN = "MonitorIn";
    public static final String SP_OPERATIONCATEGORY_MONITOROUT = "MonitorOut";
    public static final String SP_OPERATIONCATEGORY_MOVEBANK = "ChangeBank";
    public static final String SP_OPERATIONCATEGORY_NONPRODBANKIN = "MoveToNonProdBank";
    public static final String SP_OPERATIONCATEGORY_NONPRODBANKOUT = "MoveOutNonProdBank";
    public static final String SP_OPERATIONCATEGORY_MOVETOSPLIT = "MoveSplitLotToBranch";
    public static final String SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL = "CancelMoveSplitLotToBranch";
    public static final String SP_OPERATIONCATEGORY_OPERATIONCOMPLETE = "MoveOut";
    public static final String SP_OPERATIONCATEGORY_OPERATIONSTART = "MoveIn";
    public static final String SP_OPERATIONCATEGORY_OPERATIONSTARTCANCEL = "MoveInCancel";
    public static final String SP_OPERATIONCATEGORY_ORDERCHANGE = "ModifyPO";
    public static final String SP_OPERATIONCATEGORY_PILOTSPLIT = "PilotSplit";
    public static final String SP_OPERATIONCATEGORY_PILOTMERGE = "PilotMerge";
    public static final String SP_OPERATIONCATEGORY_PROCESSSTART = "ProcessStart";
    public static final String SP_OPERATIONCATEGORY_PROCESSEND = "ProcessEnd";
    public static final String SP_OPERATIONCATEGORY_RELEASE = "Release";
    public static final String SP_OPERATIONCATEGORY_RELEASECANCEL = "ReleaseCancel";
    public static final String SP_OPERATIONCATEGORY_UPDATE = "Update";
    public static final String SP_OPERATIONCATEGORY_REQUEUE = "RefreshLotQ";
    public static final String SP_OPERATIONCATEGORY_REWORK = "Rework";
    public static final String SP_OPERATIONCATEGORY_REWORKCANCEL = "CancelRework";
    public static final String SP_OPERATIONCATEGORY_SCHEDULECHANGE = "ModifyLotPlan";
    public static final String SP_OPERATIONCATEGORY_SHIP = "Ship";
    public static final String SP_OPERATIONCATEGORY_SHIPCANCEL = "CancelShip";
    public static final String SP_OPERATIONCATEGORY_SLMCASSETTECHANGE = "SLMCassetteChange";
    public static final String SP_OPERATIONCATEGORY_SPLIT = "Split";
    public static final String SP_OPERATIONCATEGORY_STB = "LotStart";
    public static final String SP_OPERATIONCATEGORY_STBUSED = "UsedForLotStart";
    public static final String SP_OPERATIONCATEGORY_VENDORLOTRECEIVE = "MaterialReceive";
    public static final String SP_OPERATIONCATEGORY_VENDORLOTRETURN = "MaterialReturn";
    public static final String SP_OPERATIONCATEGORY_WAFERSCRAP = "ScrapWafer";
    public static final String SP_OPERATIONCATEGORY_WAFERSCRAPCANCEL = "CancelScrapWafer";
    public static final String SP_OPERATIONCATEGORY_WAFERSORT = "WaferSort";
    public static final String SP_OPERATIONCATEGORY_BRANCH = "Branch";
    public static final String SP_OPERATIONCATEGORY_BRANCHCANCEL = "CancelBranch";
    public static final String SP_OPERATIONCATEGORY_RUNNINGHOLD = "RunningHold";
    public static final String SP_OPERATIONCATEGORY_FORCECOMP = "ForceMoveOut";
    public static final String SP_OPERATIONCATEGORY_STBCANCEL = "LotStartCancel";
    public static final String SP_OPERATIONCATEGORY_STBCANCELLED = "LotStartCancelled";
    public static final String SP_OPERATIONCATEGORY_PREPARECANCEL = "PrepareCancel";
    public static final String SP_OPERATIONCATEGORY_PREPARECANCELLED = "PrepareCancelled";
    public static final String SP_OPERATIONCATEGORY_OPECOMPPARTIAL = "RunningSplit-MoveOut";
    public static final String SP_OPERATIONCATEGORY_OPESTARTCANCELPARTIAL = "RunningSplit-MoveInCancel";
    public static final String SP_OPERATIONCATEGORY_DURABLEHOLD = "DurableHold";
    public static final String SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE = "DurableHoldRelease";
    public static final String SP_OPERATIONCATEGORY_LOTTERMINATE = "LotTerminate";
    public static final String SP_OPERATIONCATEGORY_LOTTERMINATECANCEL = "LotTerminateCancel";

    public static final String SP_HISTORYCATEGORY_EQUIPMENT = "Equipment";
    public static final String SP_HISTORYCATEGORY_RETICLE = "Reticle";
    public static final String SP_HISTORYCATEGORY_DURABLE = "Durable";
    public static final String SP_HISTORYCATEGORY_PROCESSWAFERINCJ = "ProcessWaferInCJ";
    public static final String SP_HISTORYCATEGORY_PROCESS = "Process";
    public static final String SP_HISTORYCATEGORY_RECIPEPARAMETERADJUST = "RecipeParameterAdjust";
    public static final String SP_HISTORYCATEGORY_PROCESSJOBINCJ = "ProcessJobInCJ";
    public static final String SP_HISTORYCATEGORY_EQPMONITORJOB = "EqpMonitorJob";
    public static final String SP_HISTORYCATEGORY_BUFFRSCTYPECHANGE = "BuffRscTypeChange";
    public static final String SP_HISTORYCATEGORY_RUNCARD = "RunCard";

    public static final String SP_HISTORYTABLENAME_FHCSCHS = "OHCMBSC";
    public static final String SP_HISTORYTABLENAME_FHESCHS = "OHEQSC";
    public static final String SP_HISTORYTABLENAME_FHEMCHS = "OHEQPMODECHG";
    public static final String SP_HISTORYTABLENAME_FHOPEHS = "OHLOTOPE";
    public static final String SP_HISTORYTABLENAME_FHDRCHS = "OHDURSC";
    public static final String SP_HISTORYTABLENAME_OHRTLHS = "OHRTLHS";
    public static final String SP_HISTORYTABLENAME_OHRTLHS_ISSUE = "OHRTLHS_ISSUE";
    public static final String SP_HISTORYTABLENAME_FHPJCHS_RPARM = "OHPJCHG_RPARAM";
    public static final String SP_HISTORYTABLENAME_FRPO_SMPL = "OMPROPE_WAFERJOB";
    public static final String SP_HISTORYTABLENAME_FHWCPHS = "OHWFCMB";
    public static final String SP_HISTORYTABLENAME_FHPROCRSCWPOS = "OHPRCRESWP";
    public static final String SP_HISTORYTABLENAME_FHPJCHS_PJ = "OHPJCHG";
    public static final String SP_HISTORYTABLENAME_FHPJCHS = "OHPJCHG";
    public static final String SP_HISTORYTABLENAME_FHEQPMONHS = "OHAMON";
    public static final String SP_HISTORYTABLENAME_OHSEASON = "OHSEASON";
    public static final String SP_HISTORYTABLENAME_FHEQPMONJOBHS = "OHAMONJOB";
    public static final String SP_HISTORYTABLENAME_OHSEASONJOB = "OHSEASONJOB";
    public static final String SP_HISTORYTABLENAME_FHBUFFRSCCHS = "OHEQBUFCHG";
    public static final String SP_HISTORYTABLENAME_OHRUNCARD = "OHRUNCARD";
    public static final String SP_HISTORYTABLENAME_FHPLSPHS = "OHPSM";
    public static final String SP_HISTORYTABLENAME_FHPLSPHS_WAFER = "OHPSM_WAFER";
    public static final String SP_HISTORYTABLENAME_FHFPCHS = "OHDOC";
    public static final String SP_HISTORYTABLENAME_FHFPCHS_WAFER = "OHDOC_WAFER";
    public static final String SP_HISTORYTABLENAME_FHFPCHS_WAFER_RPARM = "OHDOC_WAFER_RPARAM";
    public static final String SP_HISTORYTABLENAME_FHFPCHS_DCSPECS = "OHDOC_EDCSPEC";
    public static final String SP_HISTORYTABLENAME_FHFPCHS_RTCL = "OHDOC_RTCL";
    public static final String SP_HISTORYTABLENAME_FHFPCHS_COROPE = "OHDOC_MEASRELATED";

    //--------------------------------------------------------------------------------------
    //   Add for Durable Management
    //--------------------------------------------------------------------------------------
    public static final String SP_DURABLECONTROLJOBOBJECTFACTORY_MARKER = "";
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_DURABLE = "Durable";
    public static final String SP_OPERATION_OPERATIONCOMP = "OperationComp";
    public static final String SP_OPERATION_LOADING = "Loading";
    public static final String SP_OPERATION_PFXCREATE = "PFXCreate";
    public static final String SP_OPERATION_PFXDELETE = "PFXDelete";
    public static final String SP_OPERATION_DURABLECONTROLJOBMANAGE = "DurableControlJobManage";
    public static final String SP_DURABLECONTROLJOBSTATUS_CREATED = "Created";
    public static final String SP_DURABLECONTROLJOBACTION_TYPE_CREATE = "create";
    public static final String SP_DURABLECONTROLJOBACTION_TYPE_DELETE = "delete";
    public static final String SP_DURABLECONTROLJOBACTION_TYPE_QUEUE = "queue";
    public static final String SP_CLASSNAME_POSDURABLECONTROLJOB = "PosDurableControlJob";
    public static final String SP_CLASSNAME_POSPROCESSDURABLE = "PosProcessDurable";
    public static final String SP_DURABLECONTROLJOBSTATUS_DELETE = "Delete";
    public static final String SP_HASHDATA_DCTRL_JOB = "DCJ_ID";
    public static final String SP_HISTORYTABLENAME_FHDRBLOPEHS = "OHDUROPE";
    public static final String SP_HISTORYTABLENAME_FHDRJOBSTCHS = "OHDRJOBSTCHS";
    public static final String SP_HISTORYCOLUMNNAME_DURABLECONTROLJOBID = "Durable ControlJob ID";

    public static final String SP_HISTORYCOLUMNNAME_EQUIPMENTID = "Equipment ID";
    public static final String SP_HISTORYCOLUMNNAME_DURABLEID = "Durable ID";
    public static final String SP_HISTORYCOLUMNNAME_RETICLEID = "Reticle ID";
    public static final String SP_HISTORYCOLUMNNAME_CHAMBERID = "Chamber ID";
    public static final String SP_HISTORYCOLUMNNAME_STOKERID = "Stoker ID";
    public static final String SP_HISTORYCOLUMNNAME_RECIPEID = "Recipe ID";
    public static final String SP_HISTORYCOLUMNNAME_PRODUCTID = "Product ID";
    public static final String SP_HISTORYCOLUMNNAME_HISTORYNAME = "History Name";
    public static final String SP_HISTORYCOLUMNNAME_REPORTTIMESTAMP = "Report Time Stamp";
    public static final String SP_HISTORYCOLUMNNAME_USERID = "User ID";
    public static final String SP_HISTORYCOLUMNNAME_STATUSMODESTARTEDTIME = "Status/Mode Started Time";
    public static final String SP_HISTORYCOLUMNNAME_STATUSMODESTARTEDSHOPDATE = "Status/Mode Started Shop Date";
    public static final String SP_HISTORYCOLUMNNAME_STATUSMODEENDEDTIME = "Status/Mode Ended Time";
    public static final String SP_HISTORYCOLUMNNAME_STATUSMODEENDEDSHOPDATE = "Status/Mode Ended Shop Date";
    public static final String SP_HISTORYCOLUMNNAME_CLAIMMEMO = "Claim Memo";
    public static final String SP_HISTORYCOLUMNNAME_STOREDTIMESTAMP = "Stored Time Stamp";
    public static final String SP_HISTORYCOLUMNNAME_PORTID = "Port ID";
    public static final String SP_HISTORYCOLUMNNAME_OPERATIONCATEGORY = "Operation Category";
    public static final String SP_HISTORYCOLUMNNAME_OPERATIONMODE = "Operation Mode";
    public static final String SP_HISTORYCOLUMNNAME_LOGICALRECIPEID = "Logical Recipe ID";
    public static final String SP_HISTORYCOLUMNNAME_MACHINERECIPEID = "Machine Recipe ID";
    public static final String SP_HISTORYCOLUMNNAME_PHYSICALRECIPEID = "Physical Recipe ID";
    public static final String SP_HISTORYCOLUMNNAME_CONTROLJOBID = "Control Job ID";
    public static final String SP_HISTORYCOLUMNNAME_E10STATUS = "E10 Status";
    public static final String SP_HISTORYCOLUMNNAME_CHAMBERSTATUS = "Chamber Status";
    public static final String SP_HISTORYCOLUMNNAME_EQUIPMENTSTATUS = "Equipment Status";
    public static final String SP_HISTORYCOLUMNNAME_NEWE10STATUS = "New-E10 Status";
    public static final String SP_HISTORYCOLUMNNAME_NEWCHAMBERSTATUS = "New-Chamber Status";
    public static final String SP_HISTORYCOLUMNNAME_NEWEQUIPMENTSTATUS = "New-Equipment Status";
    public static final String SP_HISTORYCOLUMNNAME_TCSREPORTE10STATUS = "EAP Report E10 Status";
    public static final String SP_HISTORYCOLUMNNAME_TCSREPORTCHAMBERSTATUS = "EAP Report Chamber Status";
    public static final String SP_HISTORYCOLUMNNAME_TCSREPORTEQUIPMENTSTATUS = "EAP Report Equipment Status";
    public static final String SP_HISTORYCOLUMNNAME_TCSREPORTNEWE10STATUS = "EAP Report New-E10 Status";
    public static final String SP_HISTORYCOLUMNNAME_TCSREPORTNEWCHAMBERSTATUS = "EAP Report New-Chamber Status";
    public static final String SP_HISTORYCOLUMNNAME_TCSREPORTNEWEQUIPMENTSTATUS = "EAP Report New-Equipment Status";
    public static final String SP_HISTORYCOLUMNNAME_PORTOPERATIONMODE = "Port Operation Mode";
    public static final String SP_HISTORYCOLUMNNAME_PORTONLINEMODE = "Port Online Mode";
    public static final String SP_HISTORYCOLUMNNAME_PORTDISPATCHMODE = "Port Dispatch Mode";
    public static final String SP_HISTORYCOLUMNNAME_PORTACCESSMODE = "Port Access Mode";
    public static final String SP_HISTORYCOLUMNNAME_EQUIPMENTOPERATIONMODE = "Equipment Operation Mode";
    public static final String SP_HISTORYCOLUMNNAME_NEWEQUIPMENTOPERATIONMODE = "New Equipment Operation Mode";
    public static final String SP_HISTORYCOLUMNNAME_PORTOPERATIONSTARTMODE = "Port Operation Start Mode";
    public static final String SP_HISTORYCOLUMNNAME_PORTOPERATIONCOMPLETIONMODE = "Port Operation Completion Mode";
    public static final String SP_HISTORYCOLUMNNAME_DESCRIPTIONOFOPERATIONMODE = "Description of Operation Mode";
    public static final String SP_HISTORYCOLUMNNAME_CLAIMEDTIME = "Claimed Time";
    public static final String SP_HISTORYCOLUMNNAME_CLAIMEDSHOPDATE = "Claimed Shop Date";
    public static final String SP_HISTORYCOLUMNNAME_DURABLETYPE = "Durable Type";
    public static final String SP_HISTORYCOLUMNNAME_RETICLETYPE = "Reticle Type";
    public static final String SP_HISTORYCOLUMNNAME_ACTIONCODE = "Action Code";
    public static final String SP_HISTORYCOLUMNNAME_DURABLESTATUS = "Durable Status";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS = "Job Status";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS_CHANGE_TIME = "Job Status Change Time";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS_PROCESS = "Process";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS_ROUTE = "Route";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS_STEP = "Step";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS_OPE_NO = "Operation Number";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS_EQP = "Equipment";
    public static final String SP_HISTORYCOLUMNNAME_DURABLE_JOB_STATUS_CHAMBER = "Chamber";
    public static final String SP_HISTORYCOLUMNNAME_DURABLESUBSTATUS = "Durable Sub Status";
    public static final String SP_HISTORYCOLUMNNAME_XFERSTATUS = "Xfer Status";
    public static final String SP_HISTORYCOLUMNNAME_XFERSTATUSCHANGETIME = "Xfer Status Change Time";
    public static final String SP_HISTORYCOLUMNNAME_LOCATION = "Location";
    public static final String SP_HISTORYCOLUMNNAME_RECIPEPARAMETERNAME = "RecipeParameterName";
    public static final String SP_HISTORYCOLUMNNAME_PREVIOUSVALUE = "Previous Value";
    public static final String SP_HISTORYCOLUMNNAME_CHANGEDVALUE = "Changed Value";
    public static final String SP_HISTORYCOLUMNNAME_PROCESSJOBID = "Process Job ID";
    public static final String SP_HISTORYCOLUMNNAME_PROCESSJOBPOS = "Process Job Position";
    public static final String SP_HISTORYCOLUMNNAME_WAFERID = "Wafer ID";
    public static final String SP_HISTORYCOLUMNNAME_WAFERALIASNAME = "Wafer Alias Name";
    public static final String SP_HISTORYCOLUMNNAME_LOTID = "Lot ID";
    public static final String SP_HISTORYCOLUMNNAME_PROCESSTIME = "Process Time";
    public static final String SP_HISTORYCOLUMNNAME_PROCESSJOBSTART = "Process Job Start Flag";
    public static final String SP_HISTORYCOLUMNNAME_PROCESSJOBSTATUS = "Process Job Status";
    public static final String SP_HISTORYCOLUMNNAME_WAFERPOS = "Wafer Position";
    public static final String SP_HISTORYCOLUMNNAME_EQPMONID = "Eqp Monitor ID";
    public static final String SP_HISTORYCOLUMNNAME_SEASONID = "Eqp Season ID";
    public static final String SP_HISTORYCOLUMNNAME_MONITORTYPE = "Monitor Type";
    public static final String SP_HISTORYCOLUMNNAME_SEASONTYPE = "Season Type";
    public static final String SP_HISTORYCOLUMNNAME_MONSTATUS = "Mon Status";
    public static final String SP_HISTORYCOLUMNNAME_SEASONSTATUS = "Season Status";
    public static final String SP_HISTORYCOLUMNNAME_PREVMONSTATUS = "Prev. Monitor Status";
    public static final String SP_HISTORYCOLUMNNAME_EQPMONJOBID = "Eqp Monitor Job ID";
    public static final String SP_HISTORYCOLUMNNAME_SEASONJOBID = "Eqp Season Job ID";
    public static final String SP_HISTORYCOLUMNNAME_MONJOBSTATUS = "Mon Job Status";
    public static final String SP_HISTORYCOLUMNNAME_SEASONJOBSTATUS = "Season Job Status";
    public static final String SP_HISTORYCOLUMNNAME_PREMONJOBSTATUS = "Pre. Mon Job Status";
    public static final String SP_HISTORYCOLUMNNAME_EQPMONITORJOBLOTID = "Lot ID";
    public static final String SP_HISTORYCOLUMNNAME_RETRYCOUNT = "Retry Count";
    public static final String SP_HISTORYCOLUMNNAME_PREVMONJOBSTATUS = "Prev. Monitor Job Status";
    public static final String SP_HISTORYCOLUMNNAME_BUFFRSCCATEGORY = "Buffer Category";
    public static final String SP_HISTORYCOLUMNNAME_SMCAPACITY = "MDS Capacity";
    public static final String SP_HISTORYCOLUMNNAME_DYNAMICCAPACITY = "Dynamic Capacity";
    public static final String SP_HISTORYCOLUMNNAME_RUNCARDID = "RunCard ID";
    public static final String SP_HISTORYCOLUMNNAME_CLAIMUSERID = "Cliam User ID";
    public static final String SP_HISTORYCOLUMNNAME_ACTION = "Action";
    public static final String SP_HISTORYCOLUMNNAME_RUNCARDSTATE = "RunCard State";
    public static final String SP_HISTORYCOLUMNNAME_OWNREID = "Owner ID";
    public static final String SP_HISTORYCOLUMNNAME_EXTAPROVALFLAG = "Ext Aproval Flag";
    public static final String SP_HISTORYCOLUMNNAME_CREATETIME = "Create Time";
    public static final String SP_HISTORYCOLUMNNAME_UPDATETIME = "Update Time";
    public static final String SP_HISTORYCOLUMNNAME_APPROVERS = "Approvers";
    public static final String SP_HISTORYCOLUMNNAME_RUNCARDTYPE = "RunCard Type";
    public static final String SP_HISTORYCOLUMNNAME_AUTOCOMPLETEFLAG = "Auto Complete Flag";
    public static final String SP_HISTORYCOLUMNNAME_LOTFAMILYID = "LotFamily ID";
    public static final String SP_HISTORYCOLUMNNAME_SPLITMAINPDID = "Split MainPD ID";
    public static final String SP_HISTORYCOLUMNNAME_SPLITOPENO = "Split Ope No";
    public static final String SP_HISTORYCOLUMNNAME_ORIGMAINPDID = "Orig MainPD ID";
    public static final String SP_HISTORYCOLUMNNAME_ORIGOPENO = "Orig Ope No";
    public static final String SP_HISTORYCOLUMNNAME_MAILACTION = "Mail Action";
    public static final String SP_HISTORYCOLUMNNAME_HOLDACTION = "Hold Action";
    public static final String SP_HISTORYCOLUMNNAME_SEQNO = "Seq No";
    public static final String SP_HISTORYCOLUMNNAME_SUBROUTEID = "Sub Route ID";
    public static final String SP_HISTORYCOLUMNNAME_RETURNOPENO = "Return Ope No";
    public static final String SP_HISTORYCOLUMNNAME_MERGEOPENO = "Merge Ope No";
    public static final String SP_HISTORYCOLUMNNAME_PARENTLOTID = "Parent Lot ID";
    public static final String SP_HISTORYCOLUMNNAME_CHILDLOTID = "Child Lot ID";
    public static final String SP_HISTORYCOLUMNNAME_MEMO = "Memo";
    public static final String SP_HISTORYCOLUMNNAME_PLANSPLITJOBID = "PsmJob ID";
    public static final String SP_HISTORYCOLUMNNAME_SUCESSFLAG = "Success Flag";
    public static final String SP_HISTORYCOLUMNNAME_DOCID = "DocJob ID";
    public static final String SP_HISTORYCOLUMNNAME_MAINPDID = "MainPD ID";
    public static final String SP_HISTORYCOLUMNNAME_OPENO = "Ope No";
    public static final String SP_HISTORYCOLUMNNAME_SUBOPENO = "Sub Ope No";
    public static final String SP_HISTORYCOLUMNNAME_DOCGROUPNO = "Doc Group No";
    public static final String SP_HISTORYCOLUMNNAME_DOCTYPE = "Doc Type";
    public static final String SP_HISTORYCOLUMNNAME_MERGEMAINPDID = "Merge MainPD ID";
    public static final String SP_HISTORYCOLUMNNAME_DOCCATEGORYID = "Doc Category ID";
    public static final String SP_HISTORYCOLUMNNAME_PDID = "PD ID";
    public static final String SP_HISTORYCOLUMNNAME_PDTYPE = "PD Type";
    public static final String SP_HISTORYCOLUMNNAME_CORRESPONDOPENO = "Correspond Ope No";
    public static final String SP_HISTORYCOLUMNNAME_SKIPFLAG = "Skip Flag";
    public static final String SP_HISTORYCOLUMNNAME_RESTRICTEQPFLAG = "Restrict Eqp Flag";
    public static final String SP_HISTORYCOLUMNNAME_RPRMCHANGETYPE = "Rprm Change Type";
    public static final String SP_HISTORYCOLUMNNAME_DCDEFID = "Dcdef ID";
    public static final String SP_HISTORYCOLUMNNAME_DCSPECID = "Dcspec ID";
    public static final String SP_HISTORYCOLUMNNAME_SENDEMAILFLAG = "Send Email Flag";
    public static final String SP_HISTORYCOLUMNNAME_HOLDLOTFLAG = "Hold Lot Flag";
    public static final String SP_HISTORYCOLUMNNAME_DESCRIPTION = "Description";
    public static final String SP_HISTORYCOLUMNNAME_PARAMNAME = "Param Name";
    public static final String SP_HISTORYCOLUMNNAME_PARAMUNIT = "Param Unit";
    public static final String SP_HISTORYCOLUMNNAME_PARAMDATATYPE = "Param Data Type";
    public static final String SP_HISTORYCOLUMNNAME_PARAMLOWNERLIMIT = "Param Lower Limit";
    public static final String SP_HISTORYCOLUMNNAME_PARAMUPPERLIMIT = "Param Upper Limit";
    public static final String SP_HISTORYCOLUMNNAME_PARAMUSECURFLAG = "Param Use Cur Flag";
    public static final String SP_HISTORYCOLUMNNAME_PARAMTARGETVALUE = "Param Target Value";
    public static final String SP_HISTORYCOLUMNNAME_PARAMVALUE = "Param Value";
    public static final String SP_HISTORYCOLUMNNAME_PARAMTAG = "Param Tag";
    public static final String SP_HISTORYCOLUMNNAME_DCITEMNAME = "DC Item Name";
    public static final String SP_HISTORYCOLUMNNAME_SCRNUPPERREQ = "Scrn Upper Req";
    public static final String SP_HISTORYCOLUMNNAME_SCRNUPPERLIMIT = "Scrn Upper Limit";
    public static final String SP_HISTORYCOLUMNNAME_SCRNUPPERACTIONS = "Scrn Upper Cctions";
    public static final String SP_HISTORYCOLUMNNAME_SCRNLOWERREQ = "Scrn Lower Req";
    public static final String SP_HISTORYCOLUMNNAME_SCRNLOWERLIMIT = "Scrn Lower Limit";
    public static final String SP_HISTORYCOLUMNNAME_SCRNLOWERACTIONS = "Scrn Lower Actions";
    public static final String SP_HISTORYCOLUMNNAME_SPECUPPERREQ = "Spec Uppe Req";
    public static final String SP_HISTORYCOLUMNNAME_SPECUPPERLIMIT = "Spec Uppe Limit";
    public static final String SP_HISTORYCOLUMNNAME_SPECUPPERACTIONS = "Spec Uppe Actions";
    public static final String SP_HISTORYCOLUMNNAME_SPECLOWERREQ = "Spec Lower Req";
    public static final String SP_HISTORYCOLUMNNAME_SPECLOWERLIMIT = "Spec Lower Limit";
    public static final String SP_HISTORYCOLUMNNAME_SPECLOWERACTIONS = "Spec Lower Actions";
    public static final String SP_HISTORYCOLUMNNAME_CNTLUPPERREQ = "Cntl Upper Req";
    public static final String SP_HISTORYCOLUMNNAME_CNTLUPPERLIMIT = "Cntl Upper Limit";
    public static final String SP_HISTORYCOLUMNNAME_CNTLUPPERACTIONS = "Cntl Upper Actions";
    public static final String SP_HISTORYCOLUMNNAME_CNTLLOWERREQ = "Cntl Lower Req";
    public static final String SP_HISTORYCOLUMNNAME_CNTLLOWERLIMIT = "Cntl Lower Limit";
    public static final String SP_HISTORYCOLUMNNAME_CNTLLOWERACTIONS = "Cntl Lower Actions";
    public static final String SP_HISTORYCOLUMNNAME_DCITEMTARGETVALUE = "Dcitem Target Value";
    public static final String SP_HISTORYCOLUMNNAME_DCITEMTAG = "Dcitem Tag";
    public static final String SP_HISTORYCOLUMNNAME_DCSPECGROUP = "Dc Spec Group";
    public static final String SP_HISTORYCOLUMNNAME_RTCLID = "Rtcl ID";
    public static final String SP_HISTORYCOLUMNNAME_RTCLFROUPID = "Rtcl Group ID";

    //----------------------------------------------------------------------
    //   Equipment and Foup History Tracking Support
    //----------------------------------------------------------------------
    public static final String SP_HASHDATA_EVENT_CREATE_TIME = "EVENT_CREATE_TIME";
    public static final String SP_HASHDATA_CLAIM_USER_ID = "TRX_USER_ID";
    public static final String SP_HASHDATA_START_TIME = "START_TIME";
    public static final String SP_HASHDATA_START_SHOP_DATE = "START_WORK_DATE";
    public static final String SP_HASHDATA_END_TIME = "END_TIME";
    public static final String SP_HASHDATA_CLAIM_MEMO = "TRX_MEMO";
    public static final String SP_HASHDATA_STORE_TIME = "STORE_TIME";
    public static final String SP_HASHDATA_OPE_CATEGORY = "OPE_CATEGORY";
    public static final String SP_HASHDATA_ACTION = "TASK_TYPE";
    public static final String SP_HASHDATA_OPE_MODE = "OPE_MODE";
    public static final String SP_HASHDATA_LC_RECIPE_ID = "LRCP_ID";
    public static final String SP_HASHDATA_PH_RECIPE_ID = "PRCP_ID";
    public static final String SP_HASHDATA_CTRL_JOB = "CJ_ID";
    public static final String SP_HASHDATA_E10_STATE = "E10_STATE_ID";
    public static final String SP_HASHDATA_PR_STATE = "PROCRES_STATE_ID";
    public static final String SP_HASHDATA_EQP_STATE = "EQP_STATE_ID";
    public static final String SP_HASHDATA_NEW_E10_STATE = "NEW_E10_STATE_ID";
    public static final String SP_HASHDATA_NEW_CHAMBER_STATE = "NEW_CMB_STATE_ID";
    public static final String SP_HASHDATA_NEW_EQPMENT_STATE = "NEW_EQP_STATE_ID";
    public static final String SP_HASHDATA_ACT_E10_STATE = "ACTUAL_E10_STATE_ID";
    public static final String SP_HASHDATA_ACT_CHAMBER_STATE = "ACTUAL_CMB_STATE_ID";
    public static final String SP_HASHDATA_ACT_EUIPMENT_STATE = "ACTUAL_EQP_STATE_ID";
    public static final String SP_HASHDATA_NEW_ACT_E10_STATE = "NEW_ACTUAL_E10_STATE_ID";
    public static final String SP_HASHDATA_NEW_ACT_CHMBR_ST = "NEW_ACTUAL_CMB_STATE_ID";
    public static final String SP_HASHDATA_NEW_ACT_EQPMENT_ST = "NEW_ACTUAL_EQP_STATE_ID";
    public static final String SP_HASHDATA_EQP_OPE_MODE = "OPE_MODE";
    public static final String SP_HASHDATA_NEW_EQP_OPE_MODE = "NEW_OPE_MODE";
    public static final String SP_HASHDATA_END_SHOP_DATE = "END_WORK_DATE";
    public static final String SP_HASHDATA_PORT_OPE_MODE = "OPE_MODE";
    public static final String SP_HASHDATA_ONLINE_MODE = "ONLINE_MODE";
    public static final String SP_HASHDATA_DISP_MODE = "DISPATCH_MODE";
    public static final String SP_HASHDATA_ACCESS_MODE = "ACCESS_MODE";
    public static final String SP_HASHDATA_OPE_START_MODE = "MOVE_IN_MODE";
    public static final String SP_HASHDATA_OPE_COMP_MODE = "MOVE_OUT_MODE";
    public static final String SP_HASHDATA_DESCRIPTION = "DESCRIPTION";
    public static final String SP_HASHDATA_CLAIM_TIME = "TRX_TIME";
    public static final String SP_HASHDATA_CLAIM_SHOP_DATE = "TRX_WORK_DATE";
    public static final String SP_HASHDATA_DURABLE_ID = "DRBL_ID";
    public static final String SP_HASHDATA_FULL_DURABLE_ID = "DURABLE_ID";
    public static final String SP_HASHDATA_RETICLE_ID = "RETICLE_ID";
    public static final String SP_HASHDATA_DURABLE_TYPE = "DRBL_TYPE";
    public static final String SP_HASHDATA_ACTION_CODE = "ACTION_CODE";
    public static final String SP_HASHDATA_DURABLE_STATUS = "DRBL_STATUS";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS = "JOB_STATUS";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS_CHANGE_TIME = "STAT_CHG_TIME";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS_PROCESS = "PROCESS";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS_ROUTE = "ROUTE";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS_STEP = "STEP";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS_OPE_NO = "OPE_NO";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS_EQP_ID = "EQP_ID";
    public static final String SP_HASHDATA_DURABLE_JOB_STATUS_CHAMBER_ID = "CHAMBER_ID";
    public static final String SP_HASHDATA_DRBLSUBSTATE_ID = "DRBL_SUB_STATE_ID";
    public static final String SP_HASHDATA_DCTRLJOB_ID = "DCJ_ID";
    public static final String SP_HASHDATA_XFER_STATUS = "XFER_STATUS";
    public static final String SP_HASHDATA_XFER_STAT_CHG_TIME = "XFER_STATE_CHG_TIME";
    public static final String SP_HASHDATA_LOCATION = "LOCATION";
    public static final String SP_HASHDATA_PROCESS_JOB = "PROCESS_JOB";
    public static final String SP_HASHDATA_LOT_ID = "LOT_ID";
    public static final String SP_HASHDATA_EQP_MONITOR_ID = "EQP_MONITOR_ID";
    public static final String SP_HASHDATA_EQP_MONITOR_JOB_ID = "EQP_MONITOR_JOB_ID";
    public static final String SP_HASHDATA_CHAMBER_ID = "CHAMBER_ID";
    public static final String SP_HASHDATA_CHAMBER = "CHAMBER";
    public static final String SP_HASHDATA_EQPMON_ID = "AM_PLAN_ID";
    public static final String SP_HASHDATA_SEASON_ID = "SEASON_ID";
    public static final String SP_HASHDATA_MONITOR_TYPE = "AM_TYPE";
    public static final String SP_HASHDATA_SEASON_TYPE = "SEASON_TYPE";
    public static final String SP_HASHDATA_EQPMONJOB_ID = "AM_JOB_ID";
    public static final String SP_HASHDATA_SEASON_JOB_ID = "SEASON_JOB_ID";
    public static final String SP_HASHDATA_MON_STATUS = "AM_STATUS";
    public static final String SP_HASHDATA_SEASON_STATUS = "STATUS";
    public static final String SP_HASHDATA_PREV_MON_STATUS = "PREV_AM_STATUS";
    public static final String SP_HASHDATA_MONJOB_STATUS = "AM_JOB_STATUS";
    public static final String SP_HASHDATA_SEASON_JOB_STATUS = "SEASON_JOB_STATUS";
    public static final String SP_HASHDATA_PREV_MONJOB_STATUS = "PREV_AM_JOB_STATUS";
    public static final String SP_HASHDATA_RETRY_COUNT = "RETRY_COUNT";
    public static final String SP_HASHDATA_DRBL_CATEGORY = "DRBL_CATEGORY";
    public static final String SP_HASHDATA_BUFFRSC_CATEGORY = "BUFFRES_TYPE";
    public static final String SP_HASHDATA_SM_CAPACITY = "DEFINED_CAPACITY";
    public static final String SP_HASHDATA_DYNAMIC_CAPACITY = "MODIFIED_CAPACITY";

    //reticle
    public static final String SP_HASHDATA_RETICLE_OBJ = "RETICLE_RKEY";
    public static final String SP_HASHDATA_RETICLE_TYPE = "RETICLE_TYPE";
    public static final String SP_HASHDATA_RETICLE_STATUS = "RETICLE_STATUS";
    public static final String SP_HASHDATA_RETICLE_SUB_STATUS = "RETICLE_SUB_STATUS";
    public static final String SP_HASHDATA_RETICLE_GRADE = "RETICLE_GRADE";
    public static final String SP_HASHDATA_RETICLE_LOCATION = "RETICLE_LOCATION";
    public static final String SP_HASHDATA_CLAIM_USER = "TRX_USER_ID";
    public static final String SP_HASHDATA_RETICLE_POD_ID = "RETICLE_POD_ID";
    public static final String SP_HASHDATA_INSPECTION_TYPE = "INSPECTION_TYPE";
    public static final String SP_HASHDATA_EQP_ID = "EQP_ID";
    public static final String SP_HASHDATA_STOCKER_ID = "STOCKER_ID";
    public static final String SP_HASHDATA_REASON_CODE = "REASON_CODE";
    public static final String SP_HASHDATA_TRANSACTION_ID = "TRANSACTION_ID";


    public static final Integer SP_SIVIEW_RELEASE_NUMBER__150 = 15000;
    public static final String SP_QRESTTIME_OPECATEGORY_CREATE = "Create";
    public static final String SP_QRESTTIME_OPECATEGORY_UPDATE = "Update";
    public static final String SP_QRESTTIME_OPECATEGORY_DELETE = "Delete";
    public static final String SP_QRESTTIME_OPECATEGORY_RESET = "Reset";
    public static final String SP_QRESTTIME_SPECIFICCONTROL_RETRIGGER = "Retrigger";
    public static final String SP_QRESTTIME_SPECIFICCONTROL_DELETE = "Delete";
    public static final String SP_QRESTTIME_SPECIFICCONTROL_REPLACETARGET = "ReplaceTarget";
    public static final String SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO = "PreviousBranchInfo";
    public static final String SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO = "PreviousReturnInfo";
    public static final String SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY = "PreviousReworkOutKey";


    public static final Integer SP_SIVIEW_RELEASE_NUMBER__180 = 18000;
    public static final String SP_QTIMETYPE_BYLOT = "By Lot";
    public static final String SP_QTIMETYPE_BYWAFER = "By Wafer";


    public static final String SP_USERDATAUPDATEREQ_ACTIONCODE_REGIST = "Regist";
    public static final String SP_USERDATAUPDATEREQ_ACTIONCODE_DELETE = "Delete";
    public static final String SP_USERDATAUPDATERESULT_ACTIONCODE_UPDATE = "Update";
    public static final String SP_USERDATAUPDATERESULT_ACTIONCODE_INSERT = "Insert";
    public static final String SP_USERDATAUPDATERESULT_ACTIONCODE_DELETE = "Delete";
    public static final String SP_USERDATAUPDATERESULT_ACTIONCODE_SKIP = "Skip";
    public static final String SP_HASHDATA_AREA_ID = "BAY_ID";
    public static final String SP_HASHDATA_AREAGRP_ID = "AREAGRP_ID";
    public static final String SP_HASHDATA_BANK_ID = "BANK_ID";
    public static final String SP_HASHDATA_BINDEF_ID = "BIN_SETUP_ID";
    public static final String SP_HASHDATA_BINSPEC_ID = "TEST_BINSPEC_ID";
    public static final String SP_HASHDATA_BOM_ID = "BOM_ID";
    public static final String SP_HASHDATA_BUFFRSC_ID = "BUFFRES_ID";
    public static final String SP_HASHDATA_CALENDAR_DATE = "CALENDAR_DATE";
    public static final String SP_HASHDATA_CAST_ID = "CARRIER_ID";
    public static final String SP_HASHDATA_CODE_ID = "CODE_ID";
    public static final String SP_HASHDATA_CATEGORY_ID = "CATEGORY_ID";
    public static final String SP_HASHDATA_CTRLJOB_ID = "CJ_ID";
    public static final String SP_HASHDATA_CUSTOMER_ID = "CUSTOMER_ID";
    public static final String SP_HASHDATA_CUSTPROD_ID = "CUST_PROD_ID";
    public static final String SP_HASHDATA_DCDEF_ID = "EDC_PLAN_ID";
    public static final String SP_HASHDATA_DCSPEC_ID = "EDC_SPEC_ID";
    public static final String SP_HASHDATA_DRBL_ID = "DRBL_ID";
    public static final String SP_HASHDATA_DRBLGRP_ID = "DRBLGRP_ID";
    public static final String SP_HASHDATA_E10STATE_ID = "E10_STATE_ID";
    public static final String SP_HASHDATA_INHIBIT_ID = "RESTRICT_ID";
    public static final String SP_HASHDATA_EQPCTN_ID = "EQPCTNR_ID";
    public static final String SP_HASHDATA_EQPCTNPST_ID = "EQPCTNRPOS_ID";
    public static final String SP_HASHDATA_NOTE_TITLE = "MEMO_HEADER";
    public static final String SP_HASHDATA_CREATED_TIME = "CREATED_TIME";
    public static final String SP_HASHDATA_OWNER_ID = "OWNER_ID";
    public static final String SP_HASHDATA_EQPSTATE_ID = "EQP_STATE_ID";
    public static final String SP_HASHDATA_FLOWBATCH_ID = "FLOWB_ID";
    public static final String SP_HASHDATA_FUTUREREWORK_ID = "FUTURE_REWORK_ID";
    public static final String SP_HASHDATA_LOTFAMILY_ID = "LOTFAMILY_ID";
    public static final String SP_HASHDATA_OPE_NO = "OPE_NO";
    public static final String SP_HASHDATA_PD_ID = "STEP_ID";
    public static final String SP_HASHDATA_LOTSCHE_ID = "LOTPLAN_ID";
    public static final String SP_HASHDATA_LOTTYPE_ID = "LOTTYPE_ID";
    public static final String SP_HASHDATA_LCRECIPE_ID = "LRCP_ID";
    public static final String SP_HASHDATA_MONITOR_GRP_ID = "MON_GRP_ID";
    public static final String SP_HASHDATA_RECIPE_ID = "MRCP_ID";
    public static final String SP_HASHDATA_MSGDEF_ID = "NOTIFY_ID";
    public static final String SP_HASHDATA_MTRLLOC_ID = "MTRL_LOC_ID";
    public static final String SP_HASHDATA_RESOURCE_TYPE = "RESOURCE_TYPE";
    public static final String SP_HASHDATA_PORT_ID = "PORT_ID";
    public static final String SP_HASHDATA_OPEMODE_ID = "OPE_MODE_ID";
    public static final String SP_HASHDATA_PD_LEVEL = "PRP_LEVEL";
    public static final String SP_HASHDATA_PLSPLITJOB_ID = "PSM_JOB_ID";
    public static final String SP_HASHDATA_ROUTE_ID = "PROCESS_ID";
    public static final String SP_HASHDATA_PROCRSC_ID = "PROCRES_ID";
    public static final String SP_HASHDATA_PRIVGRP_KEY = "ACCESS_GRP_KEY";
    public static final String SP_HASHDATA_PROD_CATEGORY_ID = "PROD_CAT_ID";
    public static final String SP_HASHDATA_PRODGRP_ID = "PRODFMLY_ID";
    public static final String SP_HASHDATA_PRODREQ_ID = "PROD_ORDER_ID";
    public static final String SP_HASHDATA_PRODSPEC_ID = "PROD_ID";
    public static final String SP_HASHDATA_TARGET_OPE_NO = "TARGET_OPE_NO";
    public static final String SP_HASHDATA_TRIGGER_MAINPD_ID = "TRIGGER_PROCESS_ID";
    public static final String SP_HASHDATA_TRIGGER_OPE_NO = "TRIGGER_OPE_NO";
    public static final String SP_HASHDATA_TARGET_MAINPD_ID = "TARGET_PROCESS_ID";
    public static final String SP_HASHDATA_RAWEQPSTATE_ID = "PHY_EQP_STATE_ID";
    public static final String SP_HASHDATA_RTCLPOD_ID = "RTCLPOD_ID";
    public static final String SP_HASHDATA_RTCLSET_ID = "RTCLSET_ID";
    public static final String SP_HASHDATA_SCRIPT_ID = "PCS_ID";
    public static final String SP_HASHDATA_SMPLSPEC_ID = "QSAMPLE_SPEC_ID";
    public static final String SP_HASHDATA_STAGE_ID = "STAGE_ID";
    public static final String SP_HASHDATA_STAGEGRP_ID = "STAGE_GRP_ID";
    public static final String SP_HASHDATA_STK_ID = "STK_ID";
    public static final String SP_HASHDATA_SYSMESSAGE_ID = "NOTIFY_MSG_ID";
    public static final String SP_HASHDATA_TECH_ID = "TECH_ID";
    public static final String SP_HASHDATA_TESTSPEC_ID = "TEST_SPEC_ID";
    public static final String SP_HASHDATA_TESTTYPE_ID = "TEST_TYPE_ID";
    public static final String SP_HASHDATA_USER_ID = "USER_ID";
    public static final String SP_HASHDATA_USERGRP_ID = "USERGRP_ID";
    public static final String SP_HASHDATA_WAFER_ID = "WAFER_ID";
    public static final String SP_HASHDATA_MAINPD_ID = "MAIN_PROCESS_ID";


    public static final String SP_HISTORYCATEGORY_EQUIPMENTPORTSTATUSCHANGE = "EquipmentPortStatusChange";
    public static final String SP_HISTORYTABLENAME_FHEQPPORTSCHS = "OHEQPRTSC";
    public static final String SP_HISTORYCOLUMNNAME_PORTTYPE = "Port Type";
    public static final String SP_HISTORYCOLUMNNAME_PORTUSAGE = "Port Usage";
    public static final String SP_HISTORYCOLUMNNAME_PORTSTATE = "Port State";
    public static final String SP_HISTORYCOLUMNNAME_DISPATCHSTATE = "Dispatch State";
    public static final String SP_HISTORYCOLUMNNAME_DISPATCHTIME = "Dispatch Time";
    public static final String SP_HISTORYCOLUMNNAME_DISPATCHDURABLEID = "Dispatch Dulable ID";
    public static final String SP_HASHDATA_PORT_TYPE = "PORT_TYPE";
    public static final String SP_HASHDATA_PORT_USAGE = "PORT_USAGE";
    public static final String SP_HASHDATA_PORT_STATE = "PORT_STATE";
    public static final String SP_HASHDATA_DISP_STATE = "DISP_STATE";
    public static final String SP_HASHDATA_DISP_TIME = "DISP_TIME";
    public static final String SP_HASHDATA_DISP_DRBL_ID = "DISP_CARRIER_ID";

    public static final String SP_TIMESTAMP_NIL_OBJECT_STRING = "1901-01-01 00:00:00.0";

    public static final String SP_PROCESSJOBOPECATEGORY_ABORTED = "PJAborted";
    public static final String SP_PROCESSJOBOPECATEGORY_PAUSED = "PJPaused";
    public static final String SP_PROCESSJOBOPECATEGORY_RESUMED = "PJResumed";
    public static final String SP_PROCESSJOBOPECATEGORY_STARTED = "PJStarted";
    public static final String SP_PROCESSJOBOPECATEGORY_STOPPED = "PJStopped";
    public static final String SP_PROCESSJOBOPECATEGORY_COMPLETED = "PJCompleted";
    public static final String SP_PROCESSJOBOPECATEGORY_RECIPEPARAMETERADJUST = "PJRParamAdjust";

    public static final String SP_PROCESSJOBOPECATEGORY_ABORTREQ = "PJAbortRequest";
    public static final String SP_PROCESSJOBOPECATEGORY_PAUSEREQ = "PJPauseRequest";
    public static final String SP_PROCESSJOBOPECATEGORY_RESUMEREQ = "PJResumeRequest";
    public static final String SP_PROCESSJOBOPECATEGORY_STARTREQ = "PJStartRequest";
    public static final String SP_PROCESSJOBOPECATEGORY_STOPREQ = "PJStopRequest";
    public static final String SP_PROCESSJOBOPECATEGORY_CREATED = "PJCreated";
    public static final String SP_PROCESSJOBSTART_YES = "Y";
    public static final String SP_PROCESSJOBSTART_NO = "N";
    public static final String SP_PROCESSJOBSTART_DEFAULT = "-";


    public static final String SP_PROCESSLAGTIME_ACTION_SET = "SP_PROCESSLAGTIME_ACTION_SET";

    public static final String SP_INHIBITCLASSID_PRODUCT = "Product Specification";
    public static final String SP_INHIBITCLASSID_ROUTE = "Route";
    public static final String SP_INHIBITCLASSID_OPERATION = "Operation";
    public static final String SP_INHIBITCLASSID_PROCESS = "Process Definition";
    public static final String SP_INHIBITCLASSID_MACHINERECIPE = "Machine Recipe";
    public static final String SP_INHIBITCLASSID_EQUIPMENT = "Equipment";
    public static final String SP_INHIBITCLASSID_BAY = "Bay";
    public static final String SP_INHIBITCLASSID_RETICLE = "Reticle";
    public static final String SP_INHIBITCLASSID_RETICLEGROUP = "Reticle Group";
    public static final String SP_INHIBITCLASSID_FIXTURE = "Fixture";
    public static final String SP_INHIBITCLASSID_FIXTUREGROUP = "Fixture Group";
    public static final String SP_INHIBITCLASSID_STAGE = "Stage";
    public static final String SP_INHIBITCLASSID_MODULEPD = "Module Process Definition";
    public static final String SP_INHIBITCLASSID_CHAMBER = "Chamber";
    public static final String SP_INHIBITCLASSID_LOT = "Lot";


    public static final String SP_AREACATEGORY_LOCATION = "Location";
    public static final String SP_AREACATEGORY_WORKAREA = "WorkArea";
    public static final String SP_CATEGORY_ACTIONCODE = "Action Code";
    public static final String SP_CATEGORY_BRMMENU = "BRM Menu";
    public static final String SP_CATEGORY_BRMOWNERGROUP = "BRM Owner Group";
    public static final String SP_CATEGORY_CALCULATIONTYPE = "Calculation Type";
    public static final String SP_CATEGORY_CASSETTECATEGORY = "Cassette Category";
    public static final String SP_CATEGORY_CARRIERCATEGORY = "Carrier Type";
    public static final String SP_CATEGORY_CSTEXCHANGE_JUSTIN = "In-comming Case";
    public static final String SP_CATEGORY_CSTEXCHANGE_JUSTOUT = "Out-going Case";
    public static final String SP_CATEGORY_CSTEXCHANGE_NORMAL = "Normal Case";
    public static final String SP_CATEGORY_DEPARTMENT = "Department";
    public static final String SP_CATEGORY_DURABLESTATE = "Durable State";
    public static final String SP_CATEGORY_EMPTYCASSETTELOADINGMODE = "Empty Cassette Load Mode";
    public static final String SP_CATEGORY_EQUIPMENTCATEGORY = "Equipment Category";
    public static final String SP_CATEGORY_EQUIPMENTSTATUS = "Equipment Status";
    public static final String SP_CATEGORY_EQUIPMENTTYPE = "Equipment Type";
    public static final String SP_CATEGORY_FIXTURECATEGORY = "Fixture Category";
    public static final String SP_CATEGORY_FUTUREHOLDTYPE = "Future Hold Type";
    public static final String SP_CATEGORY_IFTHENACTION = "If Then Action";
    public static final String SP_CATEGORY_INSPECTIONTYPE = "InspectionType";
    public static final String SP_CATEGORY_LOADPURPOSETYPE = "Load Purpose Type";
    public static final String SP_CATEGORY_LOTCONTROLUSESTATE = "Lot Control Use State";
    public static final String SP_CATEGORY_LOTTYPE = "Lot Type";
    public static final String SP_CATEGORY_MAINPROCESSDEFINITIONTYPE = "Process Type";
    public static final String SP_CATEGORY_MESSAGEDISTRIBUTIONTYPE = "Notification Type";
    public static final String SP_CATEGORY_MESSAGEMEDIA = "Message Media";
    public static final String SP_CATEGORY_MFGLAYER = "Mfg Layer";
    public static final String SP_CATEGORY_OPERATIONMODE = "Operation Mode";
    public static final String SP_CATEGORY_OPIMENU = "OPI Menu";
    public static final String SP_CATEGORY_PHOTOLAYER = "Photo Layer";
    public static final String SP_CATEGORY_PRIORITYCLASSTYPE = "Priority Class Type";
    public static final String SP_CATEGORY_PROCESSHOLDTYPE = "Process Hold Type";
    public static final String SP_CATEGORY_PRODUCTTYPE = "Product Type";
    public static final String SP_CATEGORY_SCHEDULERMENU = "Scheduler Menu";
    public static final String SP_CATEGORY_STOCKERCATEGORY = "Stocker Category";
    public static final String SP_CATEGORY_TESTTYPE = "Test Type";
    public static final String SP_CATEGORY_WHATSNEXT = "Whats Next";
    public static final String SP_CATEGORY_WHERENEXT = "Where Next";
    public static final String SP_CATEGORY_WORKGROUP = "Work Group";
    public static final String SP_CATEGORY_RETICLEPODCATEGORY = "ReticlePod Category";
    public static final String SP_REASONCAT_ADJUSTCHIP = "AdjustChip";
    public static final String SP_REASONCAT_ADJUSTDIE = "AdjustDie";
    public static final String SP_REASONCAT_BANKHOLD = "HoldLotInBank";
    public static final String SP_REASONCAT_BANKHOLDRELEASE = "ReleaseHoldLotInBank";
    public static final String SP_REASONCAT_CHIPSCRAP = "ChipScrap";
    public static final String SP_REASONCAT_CHIPSCRAPCANCEL = "ChipScrapCancel";
    public static final String SP_REASONCAT_FUTUREHOLD = "FutureHold";
    public static final String SP_REASONCAT_FUTUREHOLDCANCEL = "FutureHoldCancel";
    public static final String SP_REASONCAT_LOTHOLD = "HoldLot";
    public static final String SP_REASONCAT_LOTHOLDRELEASE = "ReleaseHoldLot";
    public static final String SP_REASONCAT_PROCESSHOLD = "ProcessHold";
    public static final String SP_REASONCAT_PROCESSHOLDCANCEL = "ProcessHoldCancel";
    public static final String SP_REASONCAT_PROCESSPENDING = "ProcessPending";
    public static final String SP_REASONCAT_PROCESSRESUME = "ProcessResume";
    public static final String SP_REASONCAT_REWORK = "Rework";
    public static final String SP_REASONCAT_REWORKCANCEL = "CancelRework";
    public static final String SP_REASONCAT_RUNNINGHOLD = "RunningHold";
    public static final String SP_REASONCAT_WAFERSCRAP = "ScrapWafer";
    public static final String SP_REASONCAT_WAFERSCRAPCANCEL = "CancelScrapWafer";
    public static final String SP_REASONCAT_ENTITYINHIBIT = "MfgRestrict";
    public static final String SP_REASONCAT_ENTITYINHIBITCANCEL = "MfgRestrictCancel";
    public static final String SP_REASONCAT_MAIL = "Mail";
    public static final String SP_REASONCAT_DURABLEHOLD = "DurableHold";
    public static final String SP_REASONCAT_DURABLEHOLDRELEASE = "DurableHoldRelease";
    public static final String SP_REASONCAT_EQUIPMENTSTATE = "EquipmentState";
    public static final String CIMFW_COMPONENTMANAGER_UNDEFINED = "UNDEFINED";
    public static final String CIMFW_COMPONENTMANAGER_REGISTRATIONUNDEFINED = "UNDEFINED";

    //-------------------------------------------------------
    // D7000095  For Smart QTimeRestriction
    //-------------------------------------------------------
    public static final String SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD = "ImmediateHold";
    public static final String SP_QTIMERESTRICTION_ACTION_FUTUREHOLD = "FutureHold";
    public static final String SP_QTIMERESTRICTION_ACTION_FUTUREREWORK = "FutureRework";
    public static final String SP_QTIMERESTRICTION_ACTION_MAIL = "Mail";
    public static final String SP_QTIMERESTRICTION_ACTION_DISPATCHPRECEDE = "DispatchPrecede";
    public static final String SP_QTIMERESTRICTION_ACTION_ACTUALTARGETTIME = "ActualTargetTime";


    public static final int SP_PostProcess_SyncFlag_Async_Sequential = 0;
    public static final int SP_PostProcess_SyncFlag_Async_Parallel = 2;
    public static final int SP_PostProcess_SyncFlag_Sync_Sequential = 1;
    public static final int SP_PostProcess_SyncFlag_Sync_Parallel = 3;

    //---------------------------------------------------------
    // For Durable Change Event   (D4100081)
    //---------------------------------------------------------
    public static final String SP_DURABLECAT_RETICLEPOD = "ReticlePod";
    public static final String SP_DURABLEEVENT_ACTION_XFERSTATECHANGE = "XFERSTATCHG";
    public static final String SP_DURABLEEVENT_ACTION_STATECHANGE = "STATCHG";
    public static final String SP_DURABLEEVENT_ACTION_PMRESET = "PMRESET";

    public static final String SP_DURABLECAT_CASSETTE = "Cassette";
    public static final String SP_DURABLECAT_RETICLE = "Reticle";
    public static final String SP_DURABLECAT_FIXTURE = "Fixture";

    public static final String SP_LOT_TYPE_PRODUCTIONLOT = "Production";
    public static final String SP_LOT_TYPE_ENGINEERINGLOT = "Engineering";
    public static final String SP_LOT_TYPE_PRODUCTIONMONITORLOT = "Process Monitor";
    public static final String SP_LOT_TYPE_EQUIPMENTMONITORLOT = "Auto Monitor";
    public static final String SP_LOT_TYPE_DUMMYLOT = "Dummy";
    public static final String SP_LOT_TYPE_VENDORLOT = "Vendor";
    public static final String SP_LOT_TYPE_RECYCLELOT = "Recycle";
    public static final String SP_LOT_TYPE_CORRELATIONLOT = "Correlation";

    public static final String SP_NOTETYPE_LOTNOTE = "LotNote";
    public static final String SP_NOTEACTION_CREATE = "Create";

    //----------------------------------------------------------
    // Recipe Body Management         (R22)
    //----------------------------------------------------------
    public static final String SP_RCPMANAGEACTION_UPLOAD = "Upload";
    public static final String SP_RCPMANAGEACTION_DOWNLOAD = "Download";
    public static final String SP_RCPMANAGEACTION_RECIPEDELETE = "Recipe Delete";
    public static final String SP_RCPMANAGEACTION_FILEDELETE = "File Delete";

    public static final String SP_SUBSYSTEMID_MM = "OMS";
    public static final String SP_SUBSYSTEMID_SM = "MDS";
    public static final String SP_SUBSYSTEMID_AMS = "AMS";
    public static final String SP_SUBSYSTEMID_PM = "PM";
    public static final String SP_SUBSYSTEMID_SPC = "SPC";
    public static final String SP_SUBSYSTEMID_XMS = "TMS";
    public static final String SP_SUBSYSTEMID_TCS = "EAP";
    public static final String SP_SUBSYSTEMID_CFM = "CFM";
    public static final String SP_SUBSYSTEMID_SCH = "SPS";
    public static final String SP_SUBSYSTEMID_RPT = "RPT";
    public static final String SP_SUBSYSTEMID_OCAP = "OCAP";
    public static final String SP_SUBSYSTEMID_APC = "APC";
    public static final String SP_SUBSYSTEMID_ADM = "ADM";
    public static final String SP_SUBSYSTEMID_MCS = "MCS";
    public static final String SP_SUBSYSTEMID_MES = "MES";

    //-----------------------------------------------------------------------
    //   Add for Other System Message                            (D9900098) (R20)
    //-----------------------------------------------------------------------
    public static final String SP_SYSTEMMSGCODE_WHATNEXTERROR = "WTNXTERR";
    public static final String SP_SYSTEMMSGCODE_EQPUSAGELIMITOVER = "EQPOVER";
    public static final String SP_SYSTEMMSGCODE_CASTUSAGELIMITOVER = "CASTOVER";
    public static final String SP_SYSTEMMSGCODE_RTCLUSAGELIMITOVER = "RTCLOVER";
    public static final String SP_SYSTEMMSGCODE_FIXTUSAGELIMITOVER = "FIXTOVER";
    public static final String SP_SYSTEMMSGCODE_DELIVERYERROR = "DLVRYERR";
    public static final String SP_SYSTEMMSGCODE_RTDERROR = "RTDIFERR";
    public static final String SP_SYSTEMMSGCODE_WHERENEXTERROR = "WENXTERR";
    public static final String SP_SYSTEMMSGCODE_FBNOTICE = "FBNOTICE";
    public static final String SP_SYSTEMMSGCODE_FBREMIND = "FBREMIND";
    public static final String SP_SYSTEMMSGCODE_PSMEXEC = "PSMEXEC";
    public static final String SP_SYSTEMMSGCODE_QRESTRICTIONSYSMSG = "QT-OVER";
    public static final String SP_SYSTEMMSGCODE_FUTUREREWORKDUP = "FRWKDUP";
    public static final String SP_SYSTEMMSGCODE_APCERROR = "APCIFERR";
    public static final String SP_SYSTEMMSGCODE_FPCHOLD = "DOCHOLD";
    public static final String SP_SYSTEMMSGCODE_FPCEXEC = "DOCEXEC";

    public static final int SP_SAMPLING_IGNORED_MAIL = 0;
    public static final int SP_SAMPLING_ERROR_MAIL = 1;
    public static final int SP_SAMPLING_WARN_MAIL = 2;

    public static final String SP_SYSTEMMSGCODE_SMPLERR = "SMPLERR";

    public static final String SP_SYSTEMMSGCODE_DCSIFERR = "DCSIFERR";

    public static final String SP_TRANSSTATE_STATIONIN = "SI";
    public static final String SP_TRANSSTATE_STATIONOUT = "SO";
    public static final String SP_TRANSSTATE_BAYIN = "BI";
    public static final String SP_TRANSSTATE_BAYOUT = "BO";
    public static final String SP_TRANSSTATE_MANUALIN = "MI";
    public static final String SP_TRANSSTATE_MANUALOUT = "MO";
    public static final String SP_TRANSSTATE_EQUIPMENTIN = "EI";
    public static final String SP_TRANSSTATE_EQUIPMENTOUT = "EO";
    public static final String SP_TRANSSTATE_SHELFIN = "HI";
    public static final String SP_TRANSSTATE_SHELFOUT = "HO";
    public static final String SP_TRANSSTATE_INTERMEDIATEIN = "II";
    public static final String SP_TRANSSTATE_INTERMEDIATEOUT = "IO";
    public static final String SP_TRANSSTATE_ABNORMALIN = "AI";
    public static final String SP_TRANSSTATE_ABNORMALOUT = "AO";
    public static final String SP_TRANSSTATE_UNKNOWN = "-";
    public static final String SP_UNDEFINED_STATE = "-";

    // Different states of the variable "theLotProcessState" in CLASS PosLot COMPONENT PPrMg
    public static final String SP_LOT_PROCSTATE_WAITING = "Waiting";
    public static final String SP_LOT_PROCSTATE_PROCESSING = "Processing";
    public static final String SP_LOT_PROCSTATE_PROCESSED = "Processed";
    public static final String SP_LOT_PROCSTATE_PREPARING = "Preparing";

    // Different states of the variable "theLotInventoryState" in CLASS PosLot COMPONENT PPrMg
    public static final String SP_LOT_INVENTORYSTATE_ONFLOOR = "OnFloor";
    public static final String SP_LOT_INVENTORYSTATE_INBANK = "InBank";
    public static final String SP_LOT_INVENTORYSTATE_NONPROBANK = "NonProBank";

    // check lot action
    public static final String CHECK_LOT_ACTION_MERGE = "Merge";
    public static final String CHECK_LOT_ACTION_SPLIT = "Split";

    // states of data member "lotType"
    public static final String SP_LOT_TYPE_PRODUCTION = "Production";

    //Different states for the data members of the STRUCTURE posHoldStructure COMPONENT PPrMg
    // states of data member "holdType"
    public static final String SP_HOLDTYPE_LOTHOLD = "HoldLot";
    public static final String SP_HOLDTYPE_BANKHOLD = "HoldLotInBank";
    public static final String SP_HOLDTYPE_FUTUREHOLD = "FutureHold";
    public static final String SP_HOLDTYPE_MERGEHOLD = "MergeHold";
    public static final String SP_HOLDTYPE_MONITORSPCHOLD = "MonitorSPCHold";
    public static final String SP_HOLDTYPE_MONITORSPECHOLD = "MonitorSpecHold";
    public static final String SP_HOLDTYPE_REWORKHOLD = "ReworkHold";
    public static final String SP_HOLDTYPE_SPCOUTOFRANGEHOLD = "SPCOutOfRangeHold";
    public static final String SP_HOLDTYPE_SPECOVERHOLD = "SpecOverHold";
    public static final String SP_HOLDTYPE_WAITINGMONITORRESULTHOLD = "WaitingMonitorHold";
    public static final String SP_HOLDTYPE_PROCESSHOLD = "ProcessHold";
    public static final String SP_HOLDTYPE_RECIPEHOLD = "RecipeHold";
    public static final String SP_HOLDTYPE_RUNNINGHOLD = "RunningHold";
    public static final String SP_HOLDTYPE_FORCECOMPHOLD = "ForceCompHold";
    public static final String SP_HOLDTYPE_DURABLEHOLD = "DurableHold";
    /**
     * Task-331 add ocap hold type
     */
    public static final String HOLDTYPE_OCAPHOLD = "OcapHold";
    public static final String SP_POSLOT_SEPARATOR_CHAR = ".";


    //Different states of the variable "theProductType" in CLASS PosProductSpecification COMPONENT PPrSp
//also defined for variable "theProductType" in CLASS PosProcessDefinition COMPONENT  PPcDf
//also defined for variable "theProductType" in CLASS PosBank COMPONENT PFa
    public static final String SP_PRODTYPE_WAFER = "Wafer";
    public static final String SP_PRODTYPE_DIE = "Die";
    public static final String SP_PRODTYPE_CHIP = "Chip";
    public static final String SP_PRODTYPE_PACKAGE = "Package";

    //Different states of the variable "theLotGenerationType" in CLASS PosProductSpecification COMPONENT PPrSp
    public static final String SP_PPRSP_VOLUME = "By Volume";
    public static final String SP_PPRSP_SOURCELOT = "By Source Lot";
    public static final String SP_PPRSP_SAFETYSTOCKLEVEL = "By SSL";

    //Different states of the variable "theProcessDefinitionLevel" in CLASS PosProcessDefinition  COMPONENT PPcDf
    public static final String SP_PD_FLOWLEVEL_MAIN = "Main";
    public static final String SP_PD_FLOWLEVEL_MAIN_FOR_OPERATION = "Main_Ope";
    public static final String SP_PD_FLOWLEVEL_MAIN_FOR_MODULE = "Main_Mod";
    public static final String SP_PD_FLOWLEVEL_MODULE = "Module";
    public static final String SP_PD_FLOWLEVEL_OPERATION = "Operation";

    //Different states of the variable "theState" in CLASS PosProcessDefinition  COMPONENT PPcDf
    public static final String SP_PD_ACTIVE = "Active";
    public static final String SP_PD_INACTIVE = "Inactive";

    //Different states of the variable "theMandatoryOperation", "theSamplingTest" in CLASS PosProcessOperationSpecification  COMPONENT PPcDf
    public static final String SP_PD_ON = "Y";
    public static final String SP_PD_OFF = "N";
    public static final String SP_PD_BLANK = " ";

    //Different states of the variable "theCollectionType" in CLASS PosDataCollectionDefinition  COMPONENT PDcMg
    public static final String SP_DCDEF_COLLECTION_PROCESS = "Process";
    public static final String SP_DCDEF_COLLECTION_MEASUREMENT = "Measurement";
    public static final String SP_DCDEF_COLLECTION_TRACKING = "Tracking";
    public static final String SP_DCDEF_COLLECTION_PM = "PM";

    //Different states for the data members of the STRUCTURE posDCItemDefinition COMPONENT  PDcMg
// states of data member "valType"
    public static final String SP_DCDEF_VAL_INTEGER = "Integer";
    public static final String SP_DCDEF_VAL_FLOAT = "Float";
    public static final String SP_DCDEF_VAL_STRING = "String";
    // states of data member "itemType"
    public static final String SP_DCDEF_ITEM_RAW = "Raw";
    public static final String SP_DCDEF_ITEM_DERIVED = "Derived";
    public static final String SP_DCDEF_ITEM_USERFUNCTION = "User Func";
    // states of data member "dataCollectionMode"
    public static final String SP_DCDEF_MODE_MANUAL = "Manual";
    public static final String SP_DCDEF_MODE_AUTO = "Auto";
    public static final String SP_DCDEF_MODE_DERIVED = "Derived";
    // states of data member "measType"
    public static final String SP_DCDEF_MEAS_PROCESSGROUP = "ProcessGroup";
    public static final String SP_DCDEF_MEAS_CONTROLJOB = "Control Job";
    public static final String SP_DCDEF_MEAS_LOT = "Lot";
    public static final String SP_DCDEF_MEAS_WAFER = "Wafer";
    public static final String SP_DCDEF_MEAS_SITE = "Site";
    public static final String SP_DCDEF_MEAS_PJ = "Process Job";
    public static final String SP_DCDEF_MEAS_PJWAFER = "Proc Wafer";
    public static final String SP_DCDEF_MEAS_PJWAFERSITE = "Proc Site";
    // states of data member "calculationType"
    public static final String SP_DCDEF_CALC_RAW = "Raw";
    public static final String SP_DCDEF_CALC_DELTA = "Delta";
    public static final String SP_DCDEF_CALC_MEAN = "Mean";
    public static final String SP_DCDEF_CALC_STDDEV = "Standard Deviation";
    public static final String SP_DCDEF_CALC_RANGE = "Range";
    public static final String SP_DCDEF_CALC_MIN = "Min";
    public static final String SP_DCDEF_CALC_MAX = "Max";
    public static final String SP_DCDEF_CALC_UNIFORMITY = "Uniformity";

    public static final String SP_POSPROCESSDEFINITION_SEPARATOR_CHAR = ".";
    public static final String SP_POSPROCESSOPERATIONSPECIFICATION_SEPARATOR_CHAR = ".";
    public static final String SP_POSPROCESSFLOWCONTEXT_SEPARATOR_CHAR = ".";
    public static final String SP_POSPROCESSOPERATION_SEPARATOR_CHAR = ".";

    //Different states for the data members of the STRUCTURE posBRMMenu COMPONENT  PPsMg
    public static final String SP_BRM_R = "R";
    public static final String SP_BRM_W = "W";
    public static final String SP_BRM_X = "X";

    //Different states for the variable "theOperationMode" and "theCapableOperationMode"
//  in CLASS PosMachine COMPONENT PMcMg
    public static final String SP_MC_MANUSMIFOFF = "ManuSmifOff";
    public static final String SP_MC_MANUSMIFON = "ManuSmifOn";
    public static final String SP_MC_SEMISMIFON = "SemiSmifOn";
    public static final String SP_MC_SEMISMIFTR = "SemiSmifTr";
    public static final String SP_MC_FUL2SMIF = "Ful2Smif";
    public static final String SP_MC_FUL1SMIF = "Ful1Smif";
    public static final String SP_MC_MANUCAST = "ManuCast";
    public static final String SP_MC_SEMICAST = "SemiCast";
    public static final String SP_MC_FUL2CAST = "Ful2Cast";
    public static final String SP_MC_FUL1CAST = "Ful1Cast";

    // Different states for the variable "theStockerType" in CLASS PosStorageMachine COMPONENT PMcMg
    public static final String SP_STOCKER_TYPE_AUTO = "Auto";
    public static final String SP_STOCKER_TYPE_INTERM = "Interm";
    public static final String SP_STOCKER_TYPE_SHELF = "Shelf";
    public static final String SP_STOCKER_TYPE_RETICLE = "Reticle";
    public static final String SP_STOCKER_TYPE_FIXTURE = "Fixture";
    public static final String SP_STOCKER_TYPE_INTERBAY = "Inter Bay";
    public static final String SP_STOCKER_TYPE_INTRABAY = "Intra Bay";
    public static final String SP_STOCKER_TYPE_RETICLESHELF = "ReticleShelf";
    public static final String SP_STOCKER_TYPE_RETICLEPOD = "ReticlePod";
    public static final String SP_STOCKER_TYPE_BARERETICLE = "BareReticle";
    public static final String SP_STOCKER_TYPE_ERACK = "eRACK";

    // Different states for the variable "thePortState" in CLASS PosPortResource COMPONENT PMcMg
    public static final String SP_PORTRSC_PORTSTATE_LOADAVAIL = "LoadAvail";
    public static final String SP_PORTRSC_PORTSTATE_LOADREQ = "LoadReq";
    public static final String SP_PORTRSC_PORTSTATE_LOADCOMP = "LoadComp";
    public static final String SP_PORTRSC_PORTSTATE_UNLOADREQ = "UnloadReq";
    public static final String SP_PORTRSC_PORTSTATE_UNLOADCOMP = "UnloadComp";
    public static final String SP_PORTRSC_PORTSTATE_UNLOADAVAIL = "UnloadAvail";
    public static final String SP_PORTRSC_PORTSTATE_UNKNOWN = "-";
    public static final String SP_PORTRSC_PORTSTATE_UNKNOWNFORTCS = "Unknown";
    public static final String SP_PORTRSC_PORTSTATE_DOWN = "Down";

    // Different states for the variable "theUsageType" in CLASS PosPortResource COMPONENT PMcMg
    public static final String SP_PORTRSC_USAGETYPE_MANUAL = "Manual";
    public static final String SP_PORTRSC_USAGETYPE_AGV = "AGV";
    public static final String SP_PORTRSC_USAGETYPE_COMMON = "Common";

    // Different states for the variable "theUnloadRule" in CLASS PosPortResource COMPONENT PMcMg
    public static final String SP_PORTRSC_UNLOADRULE_FIFO = "FIFO";
    public static final String SP_PORTRSC_UNLOADRULE_LIFO = "LIFO";
    public static final String SP_PORTRSC_UNLOADRULE_RANDOM = "Random";

    // Different states for the variable "thePodOnState" in CLASS PosTransportResource COMPONENT PMcMg
    public static final String SP_TRNSRSC_PODON = "Pod On";
    public static final String SP_TRNSRSC_PODOFF = "Pod Off";

    // Different states of the variable "theRecipeType" in CLASS PosMachineRecipe COMPONENT PRcMg
    public static final String SP_RECIPE_PROCESS = "Process";
    public static final String SP_RECIPE_MEASUREMENT = "Measurement";
    public static final String SP_RECIPE_TEST = "Test";

    public static final String SP_POSMACHINE_SEPARATOR_CHAR = ".";
    public static final String SP_POSMACHINERECIPE_SEPARATOR_CHAR = ".";

    // Different states of the variable "theType" in CLASS PosNote COMPONENT PAbCl
    public static final String SP_POSNOTE_TYPESTRING = "STRING";
    public static final String SP_POSNOTE_TYPEFILE = "FILE";

    // Different states of the variable "theMessageMediaId" in CLASS PosMessageDefinition COMPONENT PMsDs
    public static final String SP_MSGDF_MSGDISTYP_EMAIL = "E-Mail";
    public static final String SP_MSGDF_MSGDISTYP_PAGER = "Pager";

    // Different states of the variable "theMessageType" in CLASS PosMessageDefinition COMPONENT PMsDs
    public static final String SP_MSGDF_MSGTYPE_FILETRANSFER = "File Transfer";
    public static final String SP_MSGDF_MSGTYPE_TEMPLATE = "Template";

    // Different states of the variable "status" in struct PosMachineCassette COMPONENT PMcMg
    public static final String SP_MC_LOT_PROCESSING = "Processing";
    public static final String SP_MC_LOT_LOADED = "Loaded";
    public static final String SP_MC_LOT_UNLOADABLE = "Unloadable";

    // Different states of the variable "theReticleAvailabilityStatus" in struct brEquipmentinfo COMPONENT PMcMg
    public static final String SP_RETICLE_AVAIL_IN = "AVAIL_IN";
    public static final String SP_RETICLE_AVAIL_OUT = "AVAIL_OUT";
    public static final String SP_RETICLE_NOT_AVAIL = "NOT_AVAIL";
//    public static final int SP_RETICLE_DEFAULTGROUPSEQUENCENUMBER = 0;                           // R30 Newly Added;

    // Different states of the variable in class PosDataCollection
    public static final String SP_POSDATACOLLECTION_STRING_SEPERATOR_CHAR = ",";

    // Different states of the variable in class PosProcessOperation
    public static final String SP_PROPERTYID_MACHINERECIPE = "$MachineRecipe";
    public static final String SP_PROPERTYID_PROCESSCAPABILITY = "$ProcessCapability";
    public static final String SP_PROPERTYID_RECIPEPARAMETER = "$RecipeParameter";

    // Different states for class PosSpcManager
    public static final int SQL_RC_OK = 0;
    //public static final  INT SPC_GM_NOT_FOUND                    "1509"    // 99/01/22;
//public static final  INT SPC_CM_NOT_FOUND                    "1512";
//public static final  INT SPC_CD_NOT_FOUND                    "1515";
    public static final String SP_SPCMG_STRING_SEPARATOR_CHAR = ".";
    public static final String SP_ACTION_DONE = "1";
    public static final String SP_ACTIONCODE_LOTHOLD = "HoldLot";
    public static final String SP_ACTIONCODE_PROCESSHOLD = "Constraint by Process Flow Operation";
    public static final String SP_ACTIONCODE_MAIL = "Mail";
    public static final String SP_ACTIONCODE_EQUIPMENTHOLD = "Constraint by Equipment";
    public static final String SP_ACTIONCODE_RECIPEHOLD = "Constraint by Recipe";
    public static final String SP_ACTIONCODE_ROUTEHOLD = "Constraint by Process Flow";
    public static final String SP_ACTIONCODE_REWORKBRANCH = "ReworkBranch";
    public static final String SP_ACTIONCODE_MAILSEND = "MailSend";
    public static final String SP_ACTIONCODE_BANKMOVE = "BankMove";
    public static final String SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD = "Inhibit-Equipment and Recipe";
    public static final String SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD = "Inhibit-Process and Equipment";
    public static final String SP_ACTIONCODE_CHAMBERHOLD = "Inhibit-Chamber";
    public static final String SP_ACTIONCODE_CHAMBERANDRECIPEHOLD = "Inhibit-Chamber and Recipe";


    public static final String SP_ENTRYTYPE_CANCEL = "Cancel";
    public static final String SP_ENTRYTYPE_ENTRY = "Entry";
    public static final String SP_ENTRYTYPE_REMOVE = "Remove";

    public static final String SP_MOVEMENTTYPE_NONMOVE = "NonMove";
    public static final String SP_MOVEMENTTYPE_STB = "LotStart";
    public static final String SP_MOVEMENTTYPE_STBCANCEL = "LotStartCancel";
    public static final String SP_MOVEMENTTYPE_START = "Start";

    public static final String SP_REASON_MERGE = "MERG";
    public static final String SP_REASON_MERGEHOLD = "MGHL";
    //add SP_REASON_COMBINEHOLD support for auto combine
    public static final String SP_REASON_COMBINEHOLD = "CBHL";
    public static final String SP_REASON_MERGEHOLDRELEASE = "MGHR";
    public static final String SP_REASON_REWORKCANCEL = "RWKC";
    public static final String SP_REASON_REWORKHOLD = "REHL";
    public static final String SP_REASON_SPCOUTOFRANGEHOLD = "SOOR";
    public static final String SP_REASON_SPECOVERHOLD = "SOHL";
    public static final String SP_REASON_WAITINGMONITORRESULTHOLD = "WMRH";
    public static final String SP_REASON_QTIMEOVERHOLD = "QTHL";
    public static final String SP_REASON_PILOTMERGEHOLD = "PLMG";
    public static final String SP_REASON_WAFERCOUNTUNMATCH = "WFUM";
    public static final String SP_REASON_PRE1SCRIPTFAILHOLD = "PR1E";
    public static final String SP_REASON_FORCECOMPHOLD = "FCHL";
    public static final String SP_REASON_RUNNINGHOLDRELEASE = "RNHR";
    public static final String SP_REASON_RUNNINGHOLD = "RNHL";
    public static final String SP_REASON_WAFERSTACKINGHOLDRELEASE = "WSHR";
    public static final String SP_REASON_CONTAMINATION_HOLD = "CCMH";
    public static final String SP_REASON_PR_HOLD = "PCMH";
    public static final String SP_REASON_CARRIER_CATEGORY_HOLD = "OCMH";
    public static final String SP_REASON_LOTHOLD_RELEASE = "LOHR";

    public static final String SP_REASON_SPCOUTOFRANGEINHIBIT = "SOOR";
    public static final String SP_REASON_SPECOVERINHIBIT = "SPEC";
    public static final String SP_REASON_GATEPASS = "GTPS";

    public static final String SP_REASON_NONPROBANKHOLD = "NPBH";
    public static final String SP_REASON_NONPROBANKHOLDRELEASE = "NPBR";

    public static final String SP_REASON_PROCESSLAGTIMEHOLD = "PLTH";
    public static final String SP_REASON_PROCESSLAGTIMEHOLDRELEASE = "PLTR";

    public static final String SP_REASON_RSRV = "RSRV";

    public static final String SP_REASON_PROCESSHOLDCANCEL = "PHLC";

    public static final String SP_REASON_QTIMEOVER = "QTOV";
    public static final String SP_REASON_QTIMECLEAR = "QTCL";

    public static final String SP_REASON_FPCHOLD = "FPCH";

    public static final String SP_REASON_WAFERSAMPLINGHOLD = "SMPL";

    public static final String SP_REASON_STBCANCELHOLD = "STBC";

    //------------------------------------------------------
//  Detail reason code of spec check result
//------------------------------------------------------
    public static final String SP_REASON_SPECOVERHOLD_UPPERCONTROL = "SHUC";
    public static final String SP_REASON_SPECOVERHOLD_LOWERCONTROL = "SHLC";
    public static final String SP_REASON_SPECOVERHOLD_UPPERSPEC = "SHUS";
    public static final String SP_REASON_SPECOVERHOLD_LOWERSPEC = "SHLS";
    public static final String SP_REASON_SPECOVERHOLD_UPPERSCREEN = "SHUR";
    public static final String SP_REASON_SPECOVERHOLD_LOWERSCREEN = "SHLR";

    public static final String SP_REASON_SPECOVERINHIBIT_UPPERCONTROL = "SIUC";
    public static final String SP_REASON_SPECOVERINHIBIT_LOWERCONTROL = "SILC";
    public static final String SP_REASON_SPECOVERINHIBIT_UPPERSPEC = "SIUS";
    public static final String SP_REASON_SPECOVERINHIBIT_LOWERSPEC = "SILS";
    public static final String SP_REASON_SPECOVERINHIBIT_UPPERSCREEN = "SIUR";
    public static final String SP_REASON_SPECOVERINHIBIT_LOWERSCREEN = "SILR";

    public static final String SP_DP_CONTROLLOTCAT_ALL = "All";
    public static final String SP_DP_CONTROLLOTCAT_DUMMY = "Dummy";
    public static final String SP_DP_CONTROLLOTCAT_EQUIPMENTMONITOR = "Auto Monitor";
    public static final String SP_DP_CONTROLLOTCAT_PROCESSMONITOR = "Process Monitor";
    public static final String SP_DP_CONTROLLOTCAT_PRODUCTION = "Production";
    public static final String SP_DP_CONTROLLOTCAT_RECYCLE = "Recycle";

    public static final String SP_DP_SELECTCRITERIA_ALL = "SALL";
    public static final String SP_DP_SELECTCRITERIA_CANBEPROCESSED = "SAVL";
    //D5100000 public static final  String SP_DP_SelectCriteria_Full2                  ="SFUL";
    public static final String SP_DP_SELECTCRITERIA_AUTO3 = "Auto3";
    public static final String SP_DP_SELECTCRITERIA_HOLD = "SHLD";
    public static final String SP_DP_SELECTCRITERIA_SORTER = "Sorter";
    public static final String SP_DP_SELECTCRITERIA_EQPMONKIT = "EqpMonKit";
    public static final String SP_DP_SELECTCRITERIA_EQPMONNOKIT = "EqpMonNoKit";

    public static final String CIMFW_DOCUMENTREVISION_UNDEFINED = "UNDEFINED";
    public static final String CIMFW_DURABLE_UNDEFINED = "UNDEFINED";
    public static final String CIMFW_DURABLE_AVAILABLE = "AVAILABLE";
    public static final String CIMFW_DURABLE_INUSE = "INUSE";
    public static final String CIMFW_DURABLE_NOTAVAILABLE = "NOTAVAILABLE";
    public static final String CIMFW_DURABLE_SCRAPPED = "SCRAPPED";
    public static final String CIMFW_DURABLE_SCRAP = "SCRAP";


    public static final String CIMFW_DURABLE_SUB_STATE_DEFAULT = "Default";
    public static final String CIMFW_DURABLE_SUB_STATE_SORTER = "Sorter";

    public static final String SP_XFERPRTY_HOT = "Hot";
    public static final String SP_XFERPRTY_NORMAL = "Normal";
    public static final String SP_XFERPRTY_RUSH = "Rush";

    public static final int SP_LOT_MAXIMUMWAFERSINALOT = 25;
    public static final int SP_DURABLE_MAXCOUNTINGROUP = 100;

    public static final String CIMFW_MACHINE_BATCHSIZEUNIT_LOT = "LOT";
    public static final String CIMFW_MACHINE_BATCHSIZEUNIT_WAFER = "WFR";

    public static final String CIMFW_LOT_HOLDSTATE_NOTONHOLD = "NOTONHOLD";
    public static final String CIMFW_LOT_HOLDSTATE_ONHOLD = "ONHOLD";

    public static final String CIMFW_LOT_STATE_FINISHED = "FINISHED";
    public static final String CIMFW_LOT_STATE_RELEASED = "RELEASED";

    public static final String CIMFW_LOT_FINISHEDSTATE_EMPTIED = "EMPTIED";
    public static final String CIMFW_LOT_STATE_SHIPPED = "SHIPPED";

    public static final String CIMFW_LOT_PRODUCTIONSTATE_INPRODUCTION = "INPRODUCTION";

    public static final String SP_LOADPURPOSETYPE_EMPTYCASSETTE = "Empty Cassette";
    public static final String SP_LOADPURPOSETYPE_FILLERDUMMY = "Filler Dummy Lot";
    public static final String SP_LOADPURPOSETYPE_PROCESSLOT = "Process Lot";
    public static final String SP_LOADPURPOSETYPE_PROCESSMONITORLOT = "Process Monitor Lot";
    public static final String SP_LOADPURPOSETYPE_SIDEDUMMYLOT = "Side Dummy Lot";
    public static final String SP_LOADPURPOSETYPE_WAITINGMONITORLOT = "Waiting Monitor Lot";
    public static final String SP_LOADPURPOSETYPE_ANY = "Any Purpose";
    public static final String SP_LOADPURPOSETYPE_OTHER = "Other";
    public static final String SP_LOADPURPOSETYPE_INTERNALBUFFER = "Internal Buffer Eqp";

    public static final String CIMFW_MCRSC_STOPPED = "STOPPED";
    public static final String CIMFW_MALOC_INSERVICE = "IN_SERVICE";
    public static final String CIMFW_MALOC_OUTOFSERVICE = "OUT_OF_SERVICE";
    public static final String CIMFW_FACTORY_UNDEFINED = "UNDEFINED";

    public static final String SP_MSGDF_MSGDISTYPE_LOTOWNER = "Lot Owner";
    //  public static FINAL  int SP_MsgDf_MsgDisType_EqpOwner        "Eqp Owner";
    public static final String SP_MSGDF_MSGDISTYPE_EQPOWNER = "Equipment Owner";
    public static final String SP_MSGDF_MSGDISTYPE_ROUTEOWNER = "Route Owner";
    public static final String SP_MSGDF_MSGDISTYPE_OPERATOR = "Operator";
    public static final String SP_MSGDF_MSGDISTYPE_USER = "User";
    public static final String SP_MSGDF_MSGDISTYPE_NOBODY = "Nobody";
    public static final String SP_MSGDF_MSGDISTYPE_LOTOWNERUSER = "Lot Owner User";
    //  public static FINAL  int SP_MsgDf_MsgDisType_EqpOwnerUser    "Eqp Owner User";
    public static final String SP_MSGDF_MSGDISTYPE_EQPOWNERUSER = "Equipment Owner User";
    public static final String SP_MSGDF_MSGDISTYPE_ROUTEOWNERUSER = "Route Owner User";
    public static final String SP_MSGDF_MSGDISTYPE_OPERATORUSER = "Operator User";

    public static final String CIMFW_PERSON_JOBASSIGNCAPACITYSTATE_AVAILABLEFORMOREJOBASSIGNMENTS = "AVAILABLE_FOR_MORE_JOB_ASSIGNMENTS";
    public static final String CIMFW_PERSON_JOBASSIGNCAPACITYSTATE_JOBASSIGNMENTATCAPACITYEXCEEEDED = "JOB_ASSIGNMENT_AT_CAPACITY_EXCEEDED";
    public static final String CIMFW_PERSON_JOBASSIGNOCCUPIEDSTATE_BUSYWITHJOB = "BUSY_WITH_JOB";
    public static final String CIMFW_PERSON_JOBASSIGNOCCUPIEDSTATE_IDLEWITHJOB = "IDLE_WITH_JOB";
    public static final String CIMFW_PERSON_JOBASSIGNSTATE_ASSIGNEDTOJOBS = "ASSIGNED_TO_JOBS";
    public static final String CIMFW_PERSON_JOBASSIGNSTATE_UNASSIGNEDTOJOBS = "UNASSIGNED_TO_JOBS";
    public static final String CIMFW_PERSON_ASSIGNAVAILSTATE_AVAILABLEFORMOREMACHINEASSIGNMENTS = "AVAILABLE_FOR_MORE_MACHINE_ASSIGNMENTS";
    public static final String CIMFW_PERSON_ASSIGNAVAILSTATE_MACHINEASSIGNMENTATCAPACITYEXCEEDED = "MACHINE_ASSIGNMENT_CAPABILITY_EXCEEDED";
    public static final String CIMFW_PERSON_ASSIGNSTATE_ASSIGNEDTOMACHINES = "ASSIGNED_TO_MACHINE";
    public static final String CIMFW_PERSON_ASSIGNSTATE_UNASSIGNEDTOMACHINES = "UNASSIGNED_TO_MACHINE";
    public static final String CIMFW_PERSON_AVAILABLEFORWORK = "AVAILABLE_FOR_WORK";
    public static final String CIMFW_PERSON_NOTAVAILABLEFORWORK = "NOT_AVAILABLE_FOR_WORK";

    public static final String CIMFW_PERSON_OFFSHIFT = "OFFSHIFT";
    public static final String CIMFW_PERSON_ONSHIFT = "ONSHIFT";
    public static final String CIMFW_PERSON_UNDEFINED = "Undefined";

    public static final String SP_PPTSVCMGR_PERSON = "OMS";
    public static final String SP_QTIMEWATCHDOG_PERSON = "QTimeSentinel";
    public static final String SP_LTIMEWATCHDOG_PERSON = "LagTimeSentinel";
    public static final String SP_PPWATCHDOG_PERSON = "PPWatcher";
    public static final String SP_POSTPROC_PERSON = "PostProc";

    public static final String SP_FLOWTYPE_MAIN = "Main";
    public static final String SP_FLOWTYPE_SUB = "Sub";

    public static final String SP_MAINPDTYPE_BRANCH = "Branch";
    public static final String SP_MAINPDTYPE_DUMMY = "Dummy";
    public static final String SP_MAINPDTYPE_ENGINEERING = "Engineering";
    public static final String SP_MAINPDTYPE_EQUIPMENTMONITOR = "Auto Monitor";
    public static final String SP_MAINPDTYPE_PROCESSMONITOR = "Process Monitor";
    public static final String SP_MAINPDTYPE_PRODUCTION = "Production";
    public static final String SP_MAINPDTYPE_REWORK = "Rework";
    public static final String SP_MAINPDTYPE_RECYCLE = "Recycle";
    public static final String SP_MAINPDTYPE_BACKUP = "Backup";
    public static final String SP_MAINPDTYPE_DURABLE = "Durable";
    public static final String SP_MAINPDTYPE_DURABLEREWORK = "Durable Rework";

    public static final String SP_OPEPDTYPE_MEASUREMENT = "Measurement";
    public static final String SP_OPEPDTYPE_PROCESS = "Process";
    public static final String SP_OPEPDTYPE_VIRTUAL = "Virtual";

    public static final String SP_PRODUCTCATEGORY_DUMMY = "Dummy";
    public static final String SP_PRODUCTCATEGORY_EQUIPMENTMONITOR = "Auto Monitor";
    public static final String SP_PRODUCTCATEGORY_PROCESSMONITOR = "Process Monitor";
    public static final String SP_PRODUCTCATEGORY_PRODUCTION = "Production";
    public static final String SP_PRODUCTCATEGORY_RAW = "Raw";
    public static final String SP_PRODUCTCATEGORY_RECYCLE = "Recycle";

    public static final String SP_PRODUCTCATEGORY_ALL = "ALL";

    public static final String SP_WAFER_TYPE_PRODUCTIONMONITOR = "Process Monitor";
    public static final String SP_WAFER_TYPE_CORRELATION = "Correlation";
    public static final String SP_WAFER_TYPE_DUMMY = "Dummy";
    public static final String SP_WAFER_TYPE_ENGINEERING = "Engineering";
    public static final String SP_WAFER_TYPE_EQUIPMENTMONITOR = "Auto Monitor";
    public static final String SP_WAFER_TYPE_PRODUCTION = "Production";
    public static final String SP_WAFER_TYPE_RECYCLE = "Recycle";
    public static final String SP_WAFER_TYPE_VENDOR = "Vendor";
    public static final int ON = 1;
    public static final int OFF = 0;

    public static final int SP_PRIORITYCLASS_SUPERHOT = 1;
    public static final int SP_PRIORITYCLASS_HOT = 2;
    public static final int SP_PRIORITYCLASS_RUSH = 3;
    public static final int SP_PRIORITYCLASS_NORMAL = 4;
    public static final int SP_PRIORITYCLASS_SLOWING = 5;

    public static final String SP_SPCCHECK_OK = "O";
    public static final String SP_SPCCHECK_WARNINGLIMITOFF = "W";
    public static final String SP_POSMACHINERECIPE_DEFAULT_CHAR = "*";
    public static final String SP_DEFAULT_CHAR = "*";

    public static final String SP_SPCCHECK_HOLDLIMITOFF = "H";
    public static final String SP_SPCCHECK_NOTDEFINED = "N";
    public static final String SP_SPCCHECK_IF_EXCEPTION = "E";

    public static final String SP_SCRAPSTATE_ACTIVE = "Active";
    public static final String SP_SCRAPSTATE_SCRAP = "Scrap";
    public static final String SP_SCRAPSTATE_GARBAGE = "Garbage";

    public static final String SP_RESPONSIBLEOPERATION_CURRENT = "C";
    public static final String SP_RESPONSIBLEOPERATION_PREVIOUS = "P";
    public static final String SP_HOLDTYPE_QTIMEOVERHOLD = "QTimeOverHold";

    public static final int SP_PARVAL_NOCHANGE = 0;
    public static final int SP_PARVAL_ADD = 1;
    public static final int SP_PARVAL_UPDATE = 2;
    public static final int SP_PARVAL_DELETE = 3;

    public static final String SP_PARVAL_CHANGETYPE_NOCHANGE = "NoChange";
    public static final String SP_PARVAL_CHANGETYPE_ADD = "Add";
    public static final String SP_PARVAL_CHANGETYPE_UPDATE = "Update";
    public static final String SP_PARVAL_CHANGETYPE_DELETE = "Delete";

    public static final String SP_EQPALMINQTYPE_EQP = "EqpAlarm";
    public static final String SP_EQPALMINQTYPE_STK = "StkAlarm";
    public static final String SP_EQPALMINQTYPE_AGV = "AGVAlarm";
    public static final String SP_EQPALARMCAT_OPEN = "Open";
    public static final String SP_EQPALARMCAT_CLOSE = "Close";
    public static final String SP_TCSRECOVERYACTION_ALLCLEAR = "A";
    public static final String SP_TCSRECOVERYACTION_LOTDELETE = "L";
    public static final String SP_TCSRECOVERYACTION_RETRY = "R";
    public static final String SP_TCSRECOVERYACTION_FORCECOMP = "C";

    public static final String SP_LOT_CONTROLUSESTATE_WAITUSE = "WaitUse";
    public static final String SP_LOT_CONTROLUSESTATE_INUSE = "InUse";
    public static final String SP_LOT_CONTROLUSESTATE_WAITRECYCLE = "WaitRecycle";
    public static final String SP_LOT_CONTROLUSESTATE_INRECYCLE = "InRecycle";

    public static final String SP_OPERATIONCATEGORY_VENDORLOTPREPARATION = "MaterialPrepare";
    public static final String SP_OPERATIONCATEGORY_VENDORWAFEROUT = "MaterialOut";

    public static final String SP_REASON_WAITINGMONITORHOLDRELEASE = "WMRL";
    public static final String SP_TRANSJOBSTATE_COMPLETION = "CP";
    public static final String SP_TRANSJOBSTATE_TRANSFERRING = "XF";
    public static final String SP_TRANSJOBSTATE_INVENTORIED = "IN";
    public static final String SP_TRANSJOBSTATE_ERRORREPORTED = "ER";

    public static final String SP_REASON_OPERATIONCOMPLETE = "LHRC";
    public static final String SP_MESSAGEID_SPCCHECKWARNING = "SPCWARN";
    public static final String SP_MESSAGEID_SPCCHECKOVER = "SPCOVER";
    public static final String SP_MESSAGEID_SPCCHARTOVER = "SPCCHART";
    public static final String SP_MESSAGEID_SPECCHECKOVER = "SPECOVER";
    public static final String SP_MESSAGEID_MONITORWARNING = "AMONWARN";
    public static final String SP_MESSAGEID_MONITOROVER = "AMONOVER";
    public static final String SP_MESSAGEID_MONITORFAIL = "AMONFAIL";
    public static final String SP_REASON_MONITOROVER = "EMMO";
    public static final String SP_REASON_MONITORFAIL = "EMFL";

    //---------------------------------------------
//   Equipment Online Mode  (theOnlineMode)
//---------------------------------------------
    public static final String SP_MC_ONLINEMODE_OFFLINE = "Offline";
    public static final String SP_MC_ONLINEMODE_ONLINE = "Online";

    //--------------------------------------
//   Port Claim Mode  (theClaimMode)
//--------------------------------------
    public static final String SP_PORTRSC_CLAIMMODE_MANUAL = "Manual";
    public static final String SP_PORTRSC_CLAIMMODE_TCS = "EAP";

    //--------------------------------------------
//   Port Transfer Mode  (theTransferMode)
//--------------------------------------------
    public static final String SP_PORTRSC_TRANSMODE_MANUAL = "Manual";
    public static final String SP_PORTRSC_TRANSMODE_AGV = "AGV";

    //-----------------------------------------------------
//   Port Lot Selection Mode  (theLotSelectionMode)
//-----------------------------------------------------
    public static final String SP_PORTRSC_LOTSELECTIONMODE_MANUAL = "Manual";
    public static final String SP_PORTRSC_LOTSELECTIONMODE_AUTO = "Auto";

    //------------------------------------------
//   Port Queuing Mode  (theQueuingMode)
//------------------------------------------
    public static final String SP_PORTRSC_QUEUINGMODE_DISABLE = "Disable";
    public static final String SP_PORTRSC_QUEUINGMODE_ENABLE = "Enable";

    //--------------------------------------
//   Port Dispatch Status
//--------------------------------------
    public static final String SP_PORTRSC_DISPATCHSTATE_REQUIRED = "Required";
    public static final String SP_PORTRSC_DISPATCHSTATE_DISPATCHED = "Dispatched";
    public static final String SP_PORTRSC_DISPATCHSTATE_NOTDISPATCHED = "NotDispatched";
    public static final String SP_PORTRSC_DISPATCHSTATE_ERROR = "Error";

    //--------------------------------------
//   Port Default Machine Operation Mode
//--------------------------------------
    public static final String SP_PORTRSC_DEFAULTMACHINEOPERATIONMODE = "Default";

    //--------------------------------------
//   Item Flag for auto interface queue
//--------------------------------------
    public static final int SP_WD_EQUIPMENTMODECHANGED = 1;
    public static final int SP_WD_EQUIPMENTSTATECHANGED = 2;
    public static final int SP_WD_LOTPROCESSSTATECHANGED = 3;
    public static final int SP_WD_LOTHOLDSTATECHANGED = 4;
    public static final int SP_WD_CASSETTETRANSFERSTATECHANGED = 5;
    public static final int SP_WD_CASSETTETRANSFERRESERVECHANGED = 6;
    public static final int SP_WD_CASSETTEDISPATCHSTATECHANGED = 7;
    public static final int SP_WD_DURABLETRANSPORTSTATECHANGED = 8;
    public static final int SP_WD_LOTIDONLY = 9;
    public static final int SP_WD_EQUIPMENTIDONLY = 10;
    public static final int SP_WD_CASSETTEIDONLY = 11;
    public static final int SP_WD_DURABLEIDONLY = 12;


    //-----------------------------------------------------------------------
    //   Access Level for Unified Security Control D9900183 (R20B)
    //-----------------------------------------------------------------------
    public static final String SP_MM_PERMISSION_ACCESS = "W";
    public static final String SP_MM_PERMISSION_NOACCESS = "X";


    //--------------------------------------
    //   Special Equipment Control
    //--------------------------------------
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_FURNACE = "Furnace";
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_FURNACE_AB_BATCH = "Furnace AB Batch";
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_INLINEPILOT = "Inline Pilot";
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_AUTOFLOWBATCHING = "Auto Flow Batching";

    //--------------------------------------
    //   Object Lock Parameter  (D9800405)
    //--------------------------------------
    public static final String SP_CLASSNAME_POSLOT = "PosLot";
    public static final String SP_CLASSNAME_POSCASSETTE = "PosCassette";
    public static final String SP_CLASSNAME_POSMACHINE = "PosMachine";
    public static final String SP_CLASSNAME_POSSTORAGEMACHINE = "PosStorageMachine";
    public static final String SP_CLASSNAME_POSPROCESSRESOURCE = "PosProcessResource";
    public static final String SP_CLASSNAME_POSMACHINECONTAINER = "PosMachineContainer";
    public static final String SP_CLASSNAME_POSMACHINECONTAINERPOSITION = "PosMachineContainerPosition";
    public static final String SP_CLASSNAME_POSPORTRESOURCE = "PosPortResource";
    public static final String SP_CLASSNAME_POSPRODUCTREQUEST = "PosProductRequest";
    public static final String SP_CLASSNAME_POSMATERIALLOCATION = "PosMaterialLocation";
    public static final String SP_CLASSNAME_POSRETICLEPOD = "PosReticlePod";
    public static final String SP_CLASSNAME_POSRETICLE = "PosReticle";
    public static final String SP_CLASSNAME_POSRETICLESET = "PosReticleSet";
    public static final String SP_CLASSNAME_POSPROCESSDEFINITION = "PosProcessDefinition";
    public static final String SP_CLASSNAME_POSCODE = "PosCode";
    public static final String SP_CLASSNAME_POSUSER = "PorUser";
    public static final String SP_CLASSNAME_SORTERJOB = "SorterJob";
    public static final String SP_CLASSNAME_POSAREA = "PosBay";
    public static final String SP_CLASSNAME_POSAREAGROUP = "PosBayGroup";
    public static final String SP_CLASSNAME_POSBANK = "PosBank";
    public static final String SP_CLASSNAME_POSBINDEFINITION = "PosBinDefinition";
    public static final String SP_CLASSNAME_POSBINSPECIFICATION = "PosBinSpecification";
    public static final String SP_CLASSNAME_POSBOM = "PosBOM";
    public static final String SP_CLASSNAME_POSBUFFERRESOURCE = "PosBufferResource";
    public static final String SP_CLASSNAME_POSCALENDARDATE = "PosCalendarDate";
    public static final String SP_CLASSNAME_POSCONTROLJOB = "PosControlJob";
    public static final String SP_CLASSNAME_POSCUSTOMER = "PosCustomer";
    public static final String SP_CLASSNAME_POSCUSTOMERPRODUCT = "PosCustomerProduct";
    public static final String SP_CLASSNAME_POSDATACOLLECTIONDEFINITION = "PosDataCollectionDefinition";
    public static final String SP_CLASSNAME_POSDATACOLLECTIONSPECIFICATION = "PosDataCollectionSpecification";
    public static final String SP_CLASSNAME_POSDISPATCHER = "PosDispatcher";
    public static final String SP_CLASSNAME_POSPROCESSDURABLECAPABILITY = "PosProcessDurableCapability";
    public static final String SP_CLASSNAME_POSE10STATE = "PosE10State";
    public static final String SP_CLASSNAME_POSENTITYINHIBIT = "PosEntityInhibit";
    public static final String SP_CLASSNAME_POSMACHINENOTE = "PosMachineNote";
    public static final String SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE = "PosMachineOperationProcedure";
    public static final String SP_CLASSNAME_POSMACHINESTATE = "PosMachineState";
    public static final String SP_CLASSNAME_POSFACTORYNOTE = "PosFactoryNote";
    public static final String SP_CLASSNAME_POSFLOWBATCH = "PosFlowBatch";
    public static final String SP_CLASSNAME_POSFLOWBATCHDISPATCHER = "PosFlowBatchDispatcher";
    public static final String SP_CLASSNAME_POSFUTUREREWORKREQUEST = "PosFutureReworkRequest";
    public static final String SP_CLASSNAME_POSLOTCOMMENT = "PosLotComment";
    public static final String SP_CLASSNAME_POSLOTFAMILY = "PosLotFamily";
    public static final String SP_CLASSNAME_POSLOTNOTE = "PosLotNote";
    public static final String SP_CLASSNAME_POSLOTOPERATIONNOTE = "PosLotOperationNote";
    public static final String SP_CLASSNAME_POSLOTOPERATIONSCHEDULE = "PosLotOperationSchedule";
    public static final String SP_CLASSNAME_POSLOTSCHEDULE = "PosLotSchedule";
    public static final String SP_CLASSNAME_POSLOTTYPE = "PosLotType";
    public static final String SP_CLASSNAME_POSLOGICALRECIPE = "PosLogicalRecipe";
    public static final String SP_CLASSNAME_POSMONITORGROUP = "PosMonitorGroup";
    public static final String SP_CLASSNAME_POSMACHINERECIPE = "PosMachineRecipe";
    public static final String SP_CLASSNAME_POSMESSAGEDEFINITION = "PosMessageDefinition";
    public static final String SP_CLASSNAME_POSMACHINEOPERATIONMODE = "PosMachineOperationMode";
    public static final String SP_CLASSNAME_POSPROCESSFLOW = "PosProcessFlow";
    public static final String SP_CLASSNAME_POSPROCESSFLOWCONTEXT = "PosProcessFlowContext";
    public static final String SP_CLASSNAME_POSPLANNEDSPLITJOB = "PosPlannedSplitJob";
    public static final String SP_CLASSNAME_POSPROCESSOPERATION = "PosProcessOperation";
    public static final String SP_CLASSNAME_POSPROCESSOPERATIONSPECIFICATION = "PosProcessOperationSpecification";
    public static final String SP_CLASSNAME_POSPRIVILEGEGROUP = "PosPrivilegeGroup";
    public static final String SP_CLASSNAME_POSPRODUCTCATEGORY = "PosProductCategory";
    public static final String SP_CLASSNAME_POSQTIMERESTRICTION = "PosQTimeRestriction";
    public static final String SP_CLASSNAME_POSRAWMACHINESTATESET = "PosRawMachineStateSet";
    public static final String SP_CLASSNAME_POSSCRIPT = "PosScript";
    public static final String SP_CLASSNAME_POSSAMPLESPECIFICATION = "PosSampleSpecification";
    public static final String SP_CLASSNAME_POSSTAGE = "PosStage";
    public static final String SP_CLASSNAME_POSSTAGEGROUP = "PosStageGroup";
    public static final String SP_CLASSNAME_POSSYSTEMMESSAGECODE = "PosSystemMessageCode";
    public static final String SP_CLASSNAME_POSTESTSPECIFICATION = "PosTestSpecification";
    public static final String SP_CLASSNAME_POSTESTTYPE = "PosTestType";
    public static final String SP_CLASSNAME_POSPERSON = "PosPerson";
    public static final String SP_CLASSNAME_POSUSERGROUP = "PosUserGroup";
    public static final String SP_CLASSNAME_POSWAFER = "PosWafer";
    public static final String SP_CLASSNAME_POSEQPMONITOR = "PosEqpMonitor";
    public static final String SP_CLASSNAME_POSEQPMONITORJOB = "PosEqpMonitorJob";
    public static final String SP_CLASSNAME_POSMAINPROCESSDEFINITION = "PosMainProcessDefinition";
    public static final String SP_CLASSNAME_POSMODULEPROCESSDEFINITION = "PosModuleProcessDefinition";
    public static final String SP_CLASSNAME_POSRETICLEGROUP = "PosReticleGroup";
    public static final String SP_CLASSNAME_POSDURABLEPROCESSFLOWCONTEXT = "PosDurableProcessFlowContext";
    public static final String SP_CLASSNAME_POSDURABLEPROCESSOPERATION = "PosDurableProcessOperation";
    public static final String SP_CLASSNAME_POSDURABLESUBSTATE = "PosDurableSubState";
    public static final String SP_CLASSNAME_POSQTIMERESTRICTIONBYWAFER = "PosQTimeRestrictionByWafer";

    //---------------------------------------------------------
//   TMS / RXM  / SPC Server & Host Name for getenv()
//---------------------------------------------------------

    public static final String SP_XMS_SERVER_NAME_XMJOBDEL = "XMS_SERVER_NAME_XMJOBDEL";
    public static final String SP_XMS_SERVER_NAME_XMJOBINQ = "XMS_SERVER_NAME_XMJOBINQ";
    public static final String SP_XMS_SERVER_NAME_XMXFERRQ = "XMS_SERVER_NAME_XMXFERRQ";
    public static final String SP_XMS_SERVER_NAME_XMSTINFO = "XMS_SERVER_NAME_XMSTINFO";
    public static final String SP_XMS_SERVER_NAME_XMINVENT = "XMS_SERVER_NAME_XMINVENT";
    public static final String SP_XMS_SERVER_NAME_XMMODCHG = "XMS_SERVER_NAME_XMMODCHG";

    public static final String SP_RXM_SERVER_NAME_RXMINVENT = "RXM_SERVER_NAME_RXMINVENT";
    public static final String SP_RXM_SERVER_NAME_RXMSTINFO = "RXM_SERVER_NAME_RXMSTINFO";
    public static final String SP_RXM_SERVER_NAME_RXMMODCHG = "RXM_SERVER_NAME_RXMMODCHG";
    public static final String SP_RXM_SERVER_NAME_RXMSTOUT = "RXM_SERVER_NAME_RXMSTOUT";


    //---------------------------------------------------------
    // For genIOR
    //---------------------------------------------------------
    public static final String SP_CHECKEXISTENCEFLAG = "SP_CHECKEXISTENCEFLAG";

    //---------------------------------------------------------
    //   For External SubSystem Interface Performance-Tuning
    //---------------------------------------------------------
    public static final String SP_BINDEVERYTIME_TCS = "SP_BINDEVERYTIME_TCS";
    public static final String SP_BINDEVERYTIME_SPC = "SP_BINDEVERYTIME_SPC";
    public static final String SP_BINDEVERYTIME_XMS = "SP_BINDEVERYTIME_XMS";
    public static final String SP_BINDEVERYTIME_CSS = "SP_BINDEVERYTIME_CSS";
    public static final String SP_BINDEVERYTIME_RTD = "SP_BINDEVERYTIME_RTD";
    public static final String SP_BINDEVERYTIME_DCS = "SP_BINDEVERYTIME_DCS";

    //----------------------------------------------------------
    // Equipment state model
    //----------------------------------------------------------
    public static final String SP_EQP_MODEL_PROCESS = "Process";
    public static final String SP_EQP_MODEL_MEASUREMENT = "Measurement";
    public static final String SP_EQP_MODEL_ASSIST_PROCESS = "Assist Process";
    public static final String SP_EQP_MODEL_ASSIST_NON_PROCESS = "Assist Non-Process";

    //---------------------------------------------
    //   Equipment Category (R20CP)
    //---------------------------------------------
    public static final String SP_MC_CATEGORY_DUMMY = "Dummy";
    public static final String SP_MC_CATEGORY_WAFERSORTER = "Wafer Sorter";
    public static final String SP_MC_CATEGORY_ASSEMBLYVENDOR = "Assembly Vendor";
    public static final String SP_MC_CATEGORY_INSPECTION = "Inspection";
    public static final String SP_MC_CATEGORY_TEST = "Test";
    public static final String SP_MC_CATEGORY_MEASUREMENT = "Measurement";
    public static final String SP_MC_CATEGORY_PROCESS = "Process";
    public static final String SP_MC_CATEGORY_CIRCUITPROBE = "Circuit Probe";
    public static final String SP_MC_CATEGORY_INTERNALBUFFER = "Internal Buffer";

    //----------------------------------------------------------
    //   Tester IF Environment Setting (R20CP)
    //----------------------------------------------------------
    public static final String SP_TESTERIF_INTERVALTIME = "SP_TESTERIF_INTERVALTIME";
    public static final String SP_TESTERIF_RETRYCOUNT = "SP_TESTERIF_RETRYCOUNT";
    public static final String SP_TESTERIF_TARGETDIRECTORY = "SP_TESTERIF_TARGETDIRECTORY";
    public static final String SP_TESTERIF_ERRORDIRECTORY = "SP_TESTERIF_ERRORDIRECTORY";

    //----------------------------------------------------------
    //   Bin Pass Criteria Code        (R20CP)
    //----------------------------------------------------------
    public static final String SP_PASSCRITERIA_GOOD = "Good";
    public static final String SP_PASSCRITERIA_NOGOOD = "No Good";
    public static final String SP_PASSCRITERIA_REPAIR = "Repairable";


    //----------------------------------------------------------
    // Class: E10State                 (R098)
    //----------------------------------------------------------
    public static final String SP_E10STATE_DOWNTIME = "DWT";
    public static final String SP_E10STATE_NONSCHEDULEDTIME = "NST";
    public static final String SP_E10STATE_UNSCHEDULEDDOWNTIME = "UDT";
    public static final String SP_E10STATE_SCHEDULEDDOWNTIME = "SDT";
    public static final String SP_E10STATE_ENGINEERING = "ENG";
    public static final String SP_E10STATE_STANDBY = "SBY";
    public static final String SP_E10STATE_PRODUCTIVE = "PRD";

    //----------------------------------------------------------
    // User Data Set D9900159
    //----------------------------------------------------------
    public static final String SP_USERDATA_ORIG_SM = "MDS";
    public static final String SP_USERDATA_ORIG_MM = "OMS";


    //----------------------------------------------------------
    // Pilot Process Action Code
    //----------------------------------------------------------
    public static final String SP_PILOT_PROCESS_FIRSTPILOT = "FirstPilot";
    public static final String SP_PILOT_PROCESS_REPILOT = "RePilot";
    public static final String SP_PILOT_PROCESS_GO = "Go";

    //----------------------------------------------------------
// Speck Check Result Code
//----------------------------------------------------------
    public static final String SP_SPECCHECKRESULT_OK = "0";
    public static final String SP_SPECCHECKRESULT_UPPERCONTROLLIMIT = "1";
    public static final String SP_SPECCHECKRESULT_LOWERCONTROLLIMIT = "2";
    public static final String SP_SPECCHECKRESULT_UPPERSPECLIMIT = "3";
    public static final String SP_SPECCHECKRESULT_LOWERSPECLIMIT = "4";
    public static final String SP_SPECCHECKRESULT_UPPERSCREENLIMIT = "5";
    public static final String SP_SPECCHECKRESULT_LOWERSCREENLIMIT = "6";
    public static final String SP_SPECCHECKRESULT_ERROR = "E";
    public static final String SP_SPECCHECKRESULT_1X_OK = "10";
    public static final String SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT = "11";
    public static final String SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT = "12";
    public static final String SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT = "13";
    public static final String SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT = "14";
    public static final String SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT = "15";
    public static final String SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT = "16";
    public static final String SP_SPECCHECKRESULT_1X_ASTERISK = "1*";
    public static final String SP_SPECCHECKRESULT_1X_POUND = "1#";

    public static final String SP_SPECCHECKRESULT_0 = "OK";
    public static final String SP_SPECCHECKRESULT_1 = "UpperControlLimit";
    public static final String SP_SPECCHECKRESULT_2 = "LowerControlLimit";
    public static final String SP_SPECCHECKRESULT_3 = "UpperSpecLimit";
    public static final String SP_SPECCHECKRESULT_4 = "LowerSpecLimit";
    public static final String SP_SPECCHECKRESULT_5 = "UpperScreenLimit";
    public static final String SP_SPECCHECKRESULT_6 = "LowerScreenLimit";
    public static final String SP_SPECCHECKRESULT_10 = "OK";
    public static final String SP_SPECCHECKRESULT_11 = "UpperControlLimit";
    public static final String SP_SPECCHECKRESULT_12 = "LowerControlLimit";
    public static final String SP_SPECCHECKRESULT_13 = "UpperSpecLimit";
    public static final String SP_SPECCHECKRESULT_14 = "LowerSpecLimit";
    public static final String SP_SPECCHECKRESULT_15 = "UpperScreenLimit";
    public static final String SP_SPECCHECKRESULT_16 = "LowerScreenLimit";
    public static final String SP_SPECCHECKRESULT_1ASTERISK = "*";
    public static final String SP_SPECCHECKRESULT_1POUND = "#";

    //----------------------------------------------------------
    // Process Status Event Action    (R22)
    //----------------------------------------------------------
    public static final String SP_PCSTACTIONCODE_PROCESSSTART = "ProcessStart";
    public static final String SP_PCSTACTIONCODE_PROCESSEND = "ProcessEnd";

    //----------------------------------------------------------
    // Entity Inhibition Class ID     (R22)
    //----------------------------------------------------------
    public static final String SP_INHIBITALLOPERATIONS = "*";
    public static final String SP_INHIBITALLSUBLOTTYPES = "*";


    //----------------------------------------------------------
    // BIB Management                 (R40a)
    //----------------------------------------------------------
    public static final String SP_BIBSTATUS_LOADED = "Loaded";
    public static final String SP_BIBSTATUS_UNLOADED = "Unloaded";
    public static final String SP_BIBTOUCHCOUNTSTATE_AVAILABLE = "Available";
    public static final String SP_BIBTOUCHCOUNTSTATE_NOTAVAILABLE = "Not Available";
    public static final String SP_BIBTOUCHCOUNTSTATE_MAINTENANCEWARNING = "MaintenanceWarning";

    //----------------------------------------------------------
    // ALF for Component to for Class
    //    define MarkerName
    //----------------------------------------------------------
    //FactoryManagement
    public static final String SP_FACTORYOBJECTFACTORY_MARKER = "";
    public static final String SP_AREAOBJECTFACTORY_MARKER = "";
    public static final String SP_CALENDARDATEOBJECTFACTORY_MARKER = "";
    public static final String SP_BANKOBJECTFACTORY_MARKER = "";
    public static final String SP_STAGEOBJECTFACTORY_MARKER = "";
    public static final String SP_STAGEGROUPOBJECTFACTORY_MARKER = "";
    public static final String SP_FACTORYNOTEOBJECTFACTORY_MARKER = "";
    //CodeManagement
    public static final String SP_CODEMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_MACHINESTATEOBJECTFACTORY_MARKER = "";
    public static final String SP_CODEOBJECTFACTORY_MARKER = "";
    public static final String SP_CATEGORYOBJECTFACTORY_MARKER = "";
    public static final String SP_SCRIPTOBJECTFACTORY_MARKER = "";
    public static final String SP_SYSTEMMESSAGECODEOBJECTFACTORY_MARKER = "";
    public static final String SP_RAWMACHINESTATESETOBJECTFACTORY_MARKER = "";
    public static final String SP_E10STATEOBJECTFACTORY_MARKER = "";
    public static final String SP_MACHINEOPERATIONMODEOBJECTFACTORY_MARKER = "";
    //DataCollectionManagement
    public static final String SP_DATACOLLECTIONMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_DATACOLLECTIONDEFINITIONOBJECTFACTORY_MARKER = "";
    public static final String SP_DATACOLLECTIONSPECIFICATIONOBJECTFACTORY_MARKER = "";
    //DispatchingManagement
    public static final String SP_DISPATCHINGMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_FLOWBATCHOBJECTFACTORY_MARKER = "";
    public static final String SP_FLOWBATCHDISPATCHEROBJECTFACTORY_MARKER = "";
    //DurableManagement
    public static final String SP_DURABLEMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_CASSETTEOBJECTFACTORY_MARKER = "";
    public static final String SP_PROCESSDURABLECAPABILITYOBJECTFACTORY_MARKER = "";
    public static final String SP_PROCESSDURABLEOBJECTFACTORY_MARKER = "";
    // public static final  String SP_TransportGroupObjectFactory_Marker           ="";
    public static final String SP_RETICLEPODOBJECTFACTORY_MARKER = "";
    public static final String SP_RETICLESETOBJECTFACTORY_MARKER = "";
    //MachineManagement
    public static final String SP_MACHINEMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_DISPATCHEROBJECTFACTORY_MARKER = "";
    // public static final  String SP_ProcessJobManagerObjectFactory_Marker         ="";
    public static final String SP_MACHINENOTEOBJECTFACTORY_MARKER = "";
    public static final String SP_MACHINEOPERATIONPROCEDUREOBJECTFACTORY_MARKER = "";
    public static final String SP_MACHINEOBJECTFACTORY_MARKER = "";
    public static final String SP_STORAGEMACHINEOBJECTFACTORY_MARKER = "";
    public static final String SP_EQPMONITOROBJECTFACTORY_MARKER = "";
    //MessageDistributionManagement
    public static final String SP_MESSAGEDISTRIBUTIONMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_MESSAGEDEFINITIONOBJECTFACTORY_MARKER = "";
    //ProcessDefinition
    public static final String SP_PROCESSDEFINITIONMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_PROCESSOPERATIONOBJECTFACTORY_MARKER = "";
    public static final String SP_PROCESSDEFINITIONOBJECTFACTORY_MARKER = "";
    public static final String SP_PROCESSOPERATIONSPECIFICATIONOBJECTFACTORY_MARKER = "";
    public static final String SP_QTIMERESTRICTIONMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_QTIMERESTRICTIONOBJECTFACTORY_MARKER = "";
    //Planning
    public static final String SP_PLANMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_LOTOPERATIONSCHEDULEOBJECTFACTORY_MARKER = "";
    public static final String SP_LOTSCHEDULEOBJECTFACTORY_MARKER = "";
    public static final String SP_PRODUCTREQUESTOBJECTFACTORY_MARKER = "";
    //ProductManagement
    public static final String SP_PRODUCTMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_LOTCOMMENTOBJECTFACTORY_MARKER = "";
    public static final String SP_LOTNOTEOBJECTFACTORY_MARKER = "";
    public static final String SP_LOTOPERATIONOBJECTFACTORY_MARKER = "";
    public static final String SP_LOTTYPEOBJECTFACTORY_MARKER = "";
    public static final String SP_MONITORGROUPOBJECTFACTORY_MARKER = "";
    public static final String SP_WAFEROBJECTFACTORY_MARKER = "";
    public static final String SP_CONTROLJOBOBJECTFACTORY_MARKER = "";
    public static final String SP_PLANNEDSPLITJOBOBJECTFACTORY_MARKER = "";
    public static final String SP_FUTUREREWORKREQUESTOBJECTFACTORY_MARKER = "";
    //PersonManager
    public static final String SP_PERSONMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_AREAGROUPOBJECTFACTORY_MARKER = "";
    public static final String SP_PERSONOBJECTFACTORY_MARKER = "";
    public static final String SP_PRIVILEGEGROUPOBJECTFACTORY_MARKER = "";
    //ProductSpecification
    public static final String SP_PRODUCTSPECIFICATIONMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_CUSTOMERPRODUCTOBJECTFACTORY_MARKER = "";
    public static final String SP_SAMPLINGCRITERIAOBJECTFACTORY_MARKER = "";
    public static final String SP_BINDEFINITIONOBJECTFACTORY_MARKER = "";
    public static final String SP_CUSTOMEROBJECTFACTORY_MARKER = "";
    public static final String SP_PRODUCTCATEGORYOBJECTFACTORY_MARKER = "";
    public static final String SP_PRODUCTGROUPOBJECTFACTORY_MARKER = "";
    public static final String SP_PRODUCTSPECIFICATIONOBJECTFACTORY_MARKER = "";
    public static final String SP_TECHNOLOGYOBJECTFACTORY_MARKER = "";
    public static final String SP_TESTSPECIFICATIONOBJECTFACTORY_MARKER = "";
    public static final String SP_BINSPECIFICATIONOBJECTFACTORY_MARKER = "";
    public static final String SP_TESTTYPEOBJECTFACTORY_MARKER = "";
    public static final String SP_SAMPLESPECIFICATIONOBJECTFACTORY_MARKER = "";
    //RecipeManagement
    public static final String SP_RECIPEMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_LOGICALRECIPEOBJECTFACTORY_MARKER = "";
    public static final String SP_MACHINERECIPEOBJECTFACTORY_MARKER = "";
    public static final String SP_SETUPOBJECTFACTORY_MARKER = "";
    //EntityInhibitManagement
    public static final String SP_ENTITYINHIBITMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_ENTITYINHIBITOBJECTFACTORY_MARKER = "";
    //PartsManagement
    public static final String SP_PARTSMANAGEROBJECTFACTORY_MARKER = "";
    public static final String SP_PARTSOBJECTFACTORY_MARKER = "";
    public static final String SP_BOMFACTORY_MARKER = "";
    //AbstractClassManagement
    public static final String SP_PROPERTYSETOBJECTFACTORY_MARKER = "";
    //EventManagement
    public static final String SP_EVENTMANAGEROBJECTFACTORY_MARKER = "";

    public static final String SP_USERGROUPOBJECTFACTORY_MARKER = "";

    //---------------------------------
    // Operation Start (New define for R30)
    //---------------------------------
    public static final String SP_OPERATION_STARTRESERVATION = "StartReservation";
    public static final String SP_OPERATION_FLOWBATCHING = "FlowBatching";
    public static final String SP_OPERATION_FLOWBATCHINGAUTO = "FlowBatchingAuto";
    public static final String SP_OPERATION_FLOWBATCH_REBATCH = "ReFlowBatching";
    public static final String SP_OPERATION_FLOWBATCH_EQPRESERVE = "FlowBatchEqpReserve";
    public static final String SP_OPERATION_FLOWBATCH_EQPRESERVECANCEL = "FlowBatchEqpReserveCancel";
    public static final String SP_OPERATION_FLOWBATCH_LOTREMOVE = "FlowBatchLotRemove";
    public static final String SP_OPERATION_OPESTART = "OpeStart";
    public static final String SP_OPERATION_NPWCARRIERXFER = "NPWCarrierXfer";
    public static final String SP_OPERATION_NORMAL = "Normal";
    public static final String SP_OPERATION_INTERNALBUFFER = "InternalBuffer";
    public static final String SP_OPERATION_CASSETTEDELIVERY = "CassetteDelivery";
    public static final String SP_OPERATION_LOADINGLOT = "LoadingLot";
    public static final String SP_OPERATION_FOR_CAST = "Cast";
    public static final String SP_OPERATION_FOR_DESTCAST = "DestCast";
    public static final String SP_OPERATION_FOR_LOT = "Lot";
    public static final String SP_OPERATION_STARTRESERVATIONCANCEL = "StartReservationCancel";
    public static final String SP_OPERATION_OPESTARTCANCEL = "OpeStartCancel";
    public static final String SP_OPERATION_OPESTARTBYWAFER = "SP_OPERATION_OPESTARTBYWAFER";


    //---------------------------------
    // Equipment Multi Recipe Capability (New define for R30)
    //---------------------------------
    public static final String SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE = "Multiple Recipe";
    public static final String SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE = "Single Recipe";
    public static final String SP_EQP_MULTIRECIPECAPABILITY_BATCH = "Batch";

    //---------------------------------
    // Carrier Multi Lot Type (New define for R30)
    //---------------------------------
    public static final String SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE = "SL-SR";
    public static final String SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE = "ML-SR";
    public static final String SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE = "ML-MR";

    //---------------------------------
    // Equipment Mode (New define for R30)
    //---------------------------------
    public static final String SP_EQP_ONLINEMODE_OFFLINE = "Off-Line";
    public static final String SP_EQP_ONLINEMODE_ONLINELOCAL = "On-Line Local";
    public static final String SP_EQP_ONLINEMODE_ONLINEREMOTE = "On-Line Remote";
    public static final String SP_EQP_ACCESSMODE_MANUAL = "Manual";
    public static final String SP_EQP_ACCESSMODE_AUTO = "Auto";
    public static final String SP_EQP_DISPATCHMODE_MANUAL = "Manual";
    public static final String SP_EQP_DISPATCHMODE_AUTO = "Auto";
    public static final String SP_EQP_STARTMODE_MANUAL = "Manual";
    public static final String SP_EQP_STARTMODE_AUTO = "Auto";
    public static final String SP_EQP_COMPMODE_MANUAL = "Manual";
    public static final String SP_EQP_COMPMODE_AUTO = "Auto";
    public static final String SP_EQP_OPERATION_ONLINEMODE_CHANGE = "OnlineModeChange";
    public static final String SP_EQP_OPERATION_ACCESSMODE_CHANGE = "AccessModeChange";
    public static final String SP_EQP_OPERATION_OTHERMODE_CHANGE = "OtherModeChange";

    //---------------------------------
    // Durable Category (New define for R30)
    //---------------------------------
    public static final String SP_DURABLE_CATEGORY_CASSETTE = "Cassette";
    public static final String SP_DURABLE_CATEGORY_FIXTURE = "Fixture";
    public static final String SP_DURABLE_CATEGORY_RETICLE = "Reticle";

    //---------------------------------
    // Equipment Recipe Parameter Specify Type
    // (New define for R30)
    //---------------------------------
    public static final String SP_RPARM_CHANGETYPE_BYLOT = "RecipeParmChangeByLot";
    public static final String SP_RPARM_CHANGETYPE_BYWAFER = "RecipeParmChangeByWafer";

    //---------------------------------
    // Lot Status Category
    //---------------------------------

    public static final String SP_LOTSTATECAT_STATE = "Lot State";
    public static final String SP_LOTSTATECAT_PRODUCTIONSTATE = "Lot Production State";
    public static final String SP_LOTSTATECAT_HOLDSTATE = "Lot Hold State";
    public static final String SP_LOTSTATECAT_FINISHEDSTATE = "Lot Finished State";
    public static final String SP_LOTSTATECAT_PROCSTATE = "Lot Process State";
    public static final String SP_LOTSTATECAT_TRANSSTATE = "Lot Transfer State";
    public static final String SP_LOTSTATECAT_INVENTORYSTATE = "Lot Inventory State";
    public static final String SP_IOMODE_APPROVAL = "Approval";
    public static final String SP_IOMODE_INHIBIT = "Inhibit";
    public static final String SP_BRSCRIPT_PRE1 = "Pre1";
    public static final String SP_BRSCRIPT_PRE2 = "Pre2";
    public static final String SP_BRSCRIPT_POST = "Post";

    public static final String SP_EQPSTATUSRECOVERTYPE_MANUFACTURING = "MANU";
    public static final String SP_EQPSTATUSRECOVERTYPE_PREVIOUSE10 = "PE10";
    public static final String SP_STOCKERMODE_INTERINON = "InterInOn";
    public static final String SP_STOCKERMODE_INTEROUTON = "InterOutOn";
    public static final String SP_STOCKERMODE_INTRAINON = "IntraInOn";
    public static final String SP_STOCKERMODE_INTRAOUTON = "IntraOutOn";

    //---------------------------------
    // RTD Intaface Info
    //---------------------------------
    public static final String SP_RTD_KEYNAME = "RTD";
    public static final String SP_RTD_COLUMNHEADER_EQPID = "Equipment ID";
    public static final String SP_RTD_COLUMNHEADER_CASTID = "Carrier ID";
    public static final String SP_RTD_COLUMNHEADER_LOTID = "Lot ID";
    public static final String SP_RTD_COLUMNHEADER_ROUTEID = "Route ID";
    public static final String SP_RTD_COLUMNHEADER_OPERNO = "Oper No";
    public static final String SP_RTD_COLUMNHEADER_STATIONID = "Station ID";
    public static final String SP_RTD_COLUMNHEADER_CASTXFERSTAT = "Carrier Xfer Status";
    public static final String SP_RTD_COLUMNHEADER_CASTEQPID = "Carrier Eqp ID";
    public static final String SP_RTD_COLUMNHEADER_CASTSTKID = "Carrier Station ID";
    public static final String SP_RTD_COLUMNHEADER_LRECIPEID = "Logical Recipe ID";
    public static final String SP_RTD_COLUMNHEADER_MRECIPEID = "Machine Recipe ID";
    public static final String SP_RTD_COLUMNHEADER_PRECIPEID = "Physical Recipe ID";
    public static final String SP_RTD_COLUMNHEADER_RETICLEGROUPID = "Reticle Group ID";
    public static final String SP_RTD_COLUMNHEADER_BATCHNO = "Batch No";
    public static final String SP_RTD_COLUMNHEADER_PROCESSRUNSIZEMAXIMUM = "Process RunSize Maximum";
    public static final String SP_RTD_FUNCTION_CODE_WHATNEXT = "WhatNextLotList";
    public static final String SP_RTD_FUNCTION_CODE_WHERENEXT = "WhereNextInterBay";
    public static final String SP_RTD_DISPATCHLOGIC_SIVIEW = "SiView";
    public static final String SP_RTD_CONFIGINFO_NEW = "New";
    public static final String SP_RTD_CONFIGINFO_UPDATE = "Update";
    public static final String SP_RTD_CONFIGINFO_DELETE = "Delete";

    //----------------------------------------------------------
    // Lot Customization & Flexible Rework (D3000118,D3000119)
    //----------------------------------------------------------

    public static final String SP_LOTCUSTOMIZE_DUMMY_OPERATIONNUMBER = "0.0";


    //----------------------------------------------------------
    // Reticle Pod Support (D3100008)
    //----------------------------------------------------------
    public static final String SP_MOVEDIRECTION_JUSTIN = "Just-In";
    public static final String SP_MOVEDIRECTION_JUSTOUT = "Just-Out";


    //----------------------------------------------------------
    // Active Version (D4000023)
    //----------------------------------------------------------
    public static final String SP_ACTIVE_VERSION = "##";

    //----------------------------------------------------------
    // Environment Value Control   (D4000012)
    //----------------------------------------------------------
    public static final String PPT_TRACE_INCOMING = "PPT_TRACE_INCOMING";


    //----------------------------------------------------------
    // Add Several Variables for WaferSorter (D4000056)
    //----------------------------------------------------------
    public static final String SP_SORTER_READ = "WaferIDRead";
    public static final String SP_SORTER_START = "WaferStart";
    public static final String SP_SORTER_MINIREAD = "WaferIDMiniRead";
    public static final String SP_SORTER_POSITIONCHANGE = "PositionChange";
    public static final String SP_SORTER_LOTTRANSFEROFF = "LotTransferOff";
    public static final String SP_SORTER_SEPARATEOFF = "SeparateOff";
    public static final String SP_SORTER_COMBINEOFF = "CombineOff";
    public static final String SP_SORTER_LOTTRANSFER = "LotTransfer";
    public static final String SP_SORTER_JUSTIN = "JustIn";
    public static final String SP_SORTER_JUSTOUT = "JustOut";
    public static final String SP_SORTER_WAFERENDOFF = "WaferEndOff";
    public static final String SP_SORTER_WAFEREND = "WaferEnd";
    public static final String SP_SORTER_SCRAP = "ScrapWafer";
    public static final String SP_SORTER_VENDLOT_R_AND_P = "SP_Sorter_VendLot_R_And_P";
    public static final String SP_SORTER_REQUESTED = "SP_Sorter_Requested";
    public static final String SP_SORTER_END = "SP_Sorter_End";
    public static final String SP_SORTER_SUCCEEDED = "SP_Sorter_Succeeded";
    public static final String SP_SORTER_ERRORED = "SP_Sorter_Errored";
    public static final String SP_SORTER_ALLDELETE = "SP_Sorter_AllDelete";
    public static final String SP_SORTER_DIRECTION_MM = "OMS";
    public static final String SP_SORTER_DIRECTION_TCS = "EAP";
    public static final String SP_SORTER_OK = "OK";
    public static final String SP_SORTER_ERROR = "ERROR";
    public static final String SP_SORTER_SLOTMAP_ALLDATA = "SP_Sorter_SlotMap_AllData";
    public static final String SP_SORTER_SLOTMAP_LATESTDATA = "SP_Sorter_SlotMap_LatestData";
    public static final String SP_SORTER_SLOTMAP_MATCH = "Match";
    public static final String SP_SORTER_SLOTMAP_UNMATCH = "UnMatch";
    public static final String SP_SORTER_SLOTMAP_UNKNOWN = "Unknown";
    public static final int SP_SORTER_IGNORE = 9999;
    public static final String SP_SORTER_SEARCH_SIVIEWFLAG = "Search";
    public static final String SP_SORTER_IGNORE_SIVIEWFLAG = "Ignore";
    public static final String SP_SORTER_LOCATION_CHECKBY_MM = "SP_Sorter_Location_CheckBy_MM";
    public static final String SP_SORTER_LOCATION_CHECKBY_SLOTMAP = "SP_Sorter_Location_CheckBy_SlotMap";
    public static final String SP_SORTER_ADJUST_TO_WAFERSORTER = "SP_Sorter_Adjust_To_WaferSorter";
    public static final String SP_SORTER_ADJUST_TO_MM = "SP_Sorter_Adjust_To_MM";
    public static final String SP_SORTER_ADJUSTTOMM = "AdjustToMM";
    public static final String SP_SORTER_ADJUSTTOSORTER = "AdjustToSorter";
    public static final String SP_SORTER_ROUTE_MODEL_ON_ROUTE = "OnRoute";
    public static final String SP_SORTER_ROUTE_MODEL_OFF_ROUTE = "OffRoute";
    public static final String SP_SORTERJOBSTATUS_CREATE = "Create";

    //----------------------------------------------------------
    // Add Several Variables for Internal Buffer (D4000015)
    //----------------------------------------------------------
    public static final String SP_CONTROLLOTTYPE_FILLERDUMMY = "Filler Dummy Lot";
    public static final String SP_CONTROLLOTTYPE_SIDEDUMMYLOT = "Side Dummy Lot";
    public static final String SP_CONTROLLOTTYPE_WAITINGMONITORLOT = "Waiting Monitor Lot";

    //------------------------------------------------------------------------
    // Add Environment Variable for APC InterFace. (D4000014)
    //------------------------------------------------------------------------
    public static final String SP_APC_NOTAVAILABLE = "0";


    //----------------------------------------------------------
    // State of product specification  (P4100107)
    //----------------------------------------------------------
    public static final String SP_PRODUCTSPECIFICATION_STATE_OBSOLETE = "Obsolete";
    public static final String SP_PRODUCTSPECIFICATION_STATE_DRAFT = "Draft";
    public static final String SP_PRODUCTSPECIFICATION_STATE_COMPLETE = "Complete";


    //---------------------------------------------------------
    // For Process Lag Time Function    (D40A0027)
    //---------------------------------------------------------
    public static final String SP_PROCESSLAGTIME_ACTION_CLEAR = "SP_PROCESSLAGTIME_ACTION_CLEAR";

    //---------------------------------------------------------
    // For MM <--> TMS *Stocker related definitions (D4100072)
    //---------------------------------------------------------
    public static final String SP_STOCKER_PORTTYPE_INPUT = "Input";
    public static final String SP_STOCKER_PORTTYPE_OUTPUT = "Output";
    public static final String SP_STOCKER_PORTTYPE_COMMON = "Common";

    public static final String SP_STOCKER_PORTSTATE_AVAILABLE = "Avail";
    public static final String SP_STOCKER_PORTSTATE_DOWN = "Down";

    public static final String SP_XMS_CARRIERSTATE_EMPTYCAST = "Empty Cassette";
    public static final String SP_XMS_CARRIERSTATE_PRODLOT = "Production Lot";

    public static final String SP_XMS_TRANSJOBSTATE_COMPLETION = "CP";
    public static final String SP_XMS_TRANSJOBSTATE_TRANSFERRING = "XF";
    public static final String SP_XMS_TRANSJOBSTATE_INVENTORY = "IN";

    public static final String SP_ZONE_ATTR_EMPTY = "Empty";
    public static final String SP_ZONE_ATTR_CATEGORY = "SP_ZONE_ATTR_CATEGORY";
    public static final String SP_ZONE_ATTR_CARRIER_STATE = "CarrierStatus";
    public static final String SP_ZONE_ATTR_PRIORITY_CLASS = "LotPriorityClass";
    public static final String SP_ZONE_ATTR_NEXT_EQUIPMENT = "NextEquipment";
    public static final String SP_ZONE_ATTR_NEXT_EQUIPMENT_TYPE = "NextEquipmentType";
    public static final String SP_ZONE_ATTR_LOT_STATUS_ALL = "LotStatusAll";
    public static final String SP_ZONE_ATTR_LOT_TYPE_ALL = "LotTypeAll";
    public static final String SP_ZONE_ATTR_LOT_STATUS_ONE = "LotStatusOne";
    public static final String SP_ZONE_ATTR_LOT_TYPE_ONE = "LotTypeOne";
    public static final String SP_ZONE_DEFAULT = "Default";
    public static final String SP_ZONE_EMPTY = "Empty";
    public static final String SP_ZONE_OCUPIED = "Ocupied";
    public static final int SP_SQL_NOT_FOUND = 100;
    public static final int SP_SQL_BUSY = -913;


    //---------------------------------------------------------
    // For EWR-Experimental Lot Control (D4100091)
    //---------------------------------------------------------
    public static final String SP_EWR_ACTION_DELETE = "SP_EWR_ACTION_DELETE";
    public static final String SP_EWR_ACTION_EMAIL = "SP_EWR_ACTION_EMAIL";
    public static final String SP_EWR_ACTION_UPDATE = "SP_EWR_ACTION_UPDATE";
    public static final String SP_EWR_ACTION_SPLIT = "SP_EWR_ACTION_SPLIT";
    public static final String SP_EWR_ACTION_BRANCH = "SP_EWR_ACTION_BRANCH";
    public static final String SP_HOLDTYPE_PSM_HOLD = "PlannedSplitAndMergeHold";
    public static final String SP_REASON_PSM_HOLD = "PSMH";

    //------------------------PSM------------------------------
    public static final String SP_PSM_ACTION_DELETE = "Delete";
    public static final String SP_PSM_ACTION_CREATE = "Create";
    public static final String SP_PSM_ACTION_UPDATE = "Update";
    public static final String SP_PSM_ACTION_SPLIT = "Split";
    public static final String SP_PSM_ACTION_BRANCH = "Branch";
    //------------------------PSM------------------------------

    //add SP_REASON_SPR_HOLD support separate
    public static final String SP_REASON_SPR_HOLD = "SPRH";
    public static final String SP_REASON_PSM_HOLDRELEASE = "PSMR";

    public static final String SP_PILOT_RECIPE_JOB_CREATE = "SP_PILOT_RECIPE_JOB_CREATE";
    public static final String SP_PILOT_RECIPE_JOB_DELETE = "SP_PILOT_RECIPE_JOB_DELETE";
    public static final String SP_PILOT_RECIPE_JOB_BIND_LOT = "SP_PILOT_RECIPE_JOB_BIND_LOT";

    //---------------------------------------------------------
    // For Enhancement Future Hold (D4100083)
    //---------------------------------------------------------
    public static final String SP_FUTUREHOLD_PRE = "PRE";
    public static final String SP_FUTUREHOLD_POST = "POST";
    public static final String SP_FUTUREHOLD_ALL = "ALL";
    public static final String SP_FUTUREHOLD_SINGLE = "SINGLE";
    public static final String SP_FUTUREHOLD_MULTIPLE = "MULTIPLE";

    //---------------------------------------------------------
    // For Future Action (D4100120)
    //---------------------------------------------------------
    public static final String SP_SCHDL_CHG_STATUS_WAITING = "Waiting";
    public static final String SP_SCHDL_CHG_STATUS_ONGOING = "OnGoing";
    public static final String SP_SCHDL_CHG_STATUS_COMP = "Comp";
    public static final String SP_ACTION_CODE_SCHEDULE_CHANGE = "ModifyLotPlan";
    public static final String SP_SCHDL_CHG_OBJTYPE_LOT = "Lot";
    public static final String SP_SCHDL_CHG_OBJTYPE_PRD = "Product";
    public static final String SP_SCHDL_CHG_OBJTYPE_MPD = "MainPD";
    public static final String SP_SCHDL_CHG_OBJTYPE_MDP = "ModulePD";

    //----------------------------------------------------------
    // EAP Bind Time Out Default (D4100133)
    //----------------------------------------------------------
    public static final int SP_DEFAULT_BIND_TIMEOUT_TCS = 120;

    //----------------------------------------------------------
    // Max length of environment variable (D4100134)
    // EnvName + EnvValue < SP_ENV_MAX_LEN - 1
    //----------------------------------------------------------
    public static final String ENV_ENABLE = "1";
    public static final String ENV_DISABLE = "0";

    //---------------------------------------------------------------------------------
    // Max length of fetch count for process_operationListInRoute_GetDR.dr(D4100020)
    //---------------------------------------------------------------------------------
    public static final int SP_FETCH_COUNT_FORPOLISTINROUTE = 10;

    //----------------------------------------------------------
    // Script Execute System User ID (D4200040)
    //----------------------------------------------------------
    public static final String SP_BRSCRIPT_USER_PRE1 = "Pre1";
    public static final String SP_BRSCRIPT_USER_PRE2 = "Pre2";
    public static final String SP_BRSCRIPT_USER_POST = "Post";

    //----------------------------------------------------------
    // BackupOperation (D4200062)
    //----------------------------------------------------------
    public static final String SP_BACKUPREQUEST_SEND = "Send";
    public static final String SP_BACKUPREQUEST_SENDCANCEL = "SendCancel";
    public static final String SP_BACKUPREQUEST_RETURN = "Return";
    public static final String SP_BACKUPREQUEST_RETURNCANCEL = "ReturnCancel";
    public static final String SP_BACKUPREQUEST_RECEIVEFORSEND = "ReceiveForSend";
    public static final String SP_BACKUPREQUEST_RECEIVEFORRETURN = "ReceiveForReturn";
    public static final String SP_BACKUPREQUEST_RECEIVECANCELFORSEND = "ReceiveCancelForSend";
    public static final String SP_BACKUPREQUEST_RECEIVECANCELFORRETURN = "ReceiveCancelForReturn";

    public static final String SP_BACKUPSTATE_UNDEFINED = "-";
    public static final String SP_BACKUPSTATE_SEND = "Send";
    public static final String SP_BACKUPSTATE_RECEIVE = "Receive";
    public static final String SP_BACKUPSTATE_RETURN = "Return";

    public static final String SP_BACKUPDEFINITIONSTATE_ACTIVE = "Active";
    public static final String SP_BACKUPDEFINITIONSTATE_INACTIVE = "Inactive";

    public static final String SP_BACKUPDEFINITIONCATEGORY_PRODUCT = "Product";
    public static final String SP_BACKUPDEFINITIONCATEGORY_PRODUCTGROUP = "ProductGroup";
    public static final String SP_BACKUPDEFINITIONCATEGORY_TECHNOLOGY = "Technology";

    public static final String SP_BACKUPCHANNEL_REGISTER = "Register";
    public static final String SP_BACKUPCHANNEL_DELETE = "Delete";
    public static final String SP_BACKUPCHANNEL_ADD = "Add";
    public static final String SP_BACKUPCHANNEL_UPDATE = "Update";

    public static final String SP_REASON_BACKUPOPERATION_HOLD = "BOHL";
    public static final String SP_REASON_BACKUPOPERATION_HOLDRELEASE = "BOHR";

    public static final String SP_PREDEFINITIONID_FOR_BKUP_OWNER = "BKUP-OWNER";
    public static final String SP_PREDEFINITIONID_FOR_BKUP_CUSTOMER = "BO-Customer";


    //----------------------------------------------------------
    // Wafer Level Control (D5000001)
    //----------------------------------------------------------
    public static final String SP_PROCESSJOBSTATUS_COMP = "Comp";
    public static final String SP_PROCESSJOBSTATUS_INPROCESS = "Inpr";
    public static final String SP_PROCESSJOBSTATUS_WAIT = "Wait";
    public static final String SP_PROCESSJOBSTATUS_FAIL = "Fail";

    //--------------------------------------------------------------------
    // Add some Table Marker(D5000080)
    //--------------------------------------------------------------------
    public static final String SP_PD_SPECEQP_EQP_TABLEMARKER = "PosProcessDefinition_SpecificMachines_machines";
    public static final String SP_PD_SPECEQPPGRP_EQP_TABLEMARKER = "PosProcessDefinition_SpecificMachinesByProductGroup_machines";
    public static final String SP_PD_SPECEQPTECH_EQP_TABLEMARKER = "PosProcessDefinition_SpecificMachinesByTechnology_machines";

    //P5000070 add
    public static final String SP_FLWDP_LOT_TABLEMARKER = "PosFlowBatchDispatcher_Lots";

    //P5000087 add
    public static final String SP_LOT_PRODCTN_STATE_INREWORK = "INREWORK";

    //--------------------------------------------------------------------
    // LotInfoInq by DR FLAG(D5000154)
    //--------------------------------------------------------------------
    public static final String SP_LRCP_DSET_PRST_TABLEMARKER = "PosLogicalRecipe_DefaultRecipeSettings_processResourceStates";
    public static final String SP_RTCLSET_DF_RGRP_TABLEMARKER = "PosReticleSet_DefaultReticleGroupData_reticleGroups";
    public static final String SP_LRCP_DSET_RPARM_TABLEMARKER = "PosLogicalRecipe_DefaultRecipeSettings_recipeParameters";
    public static final String SP_LRCP_DSET_FXTR_TABLEMARKER = "PosLogicalRecipe_DefaultRecipeSettings_fixtureGroups";


    //--------------------------------------------------------------------
    // Wafer Chamber Process Event Action (D5000192)
    //--------------------------------------------------------------------
    public static final String SP_CHAMBERPROCACTIONCODE_PROCESSSTART = "ProcessStart";
    public static final String SP_CHAMBERPROCACTIONCODE_PROCESSEND = "ProcessEnd";


    //----------------------------------------------------------
    // Hold Lot for AddToQueue Err (D5000225)
    //----------------------------------------------------------
    public static final String SP_HOLDTYPE_ADDTOQUEUEERRHOLD = "AddToQueueErrHold";
    public static final String SP_REASON_ADDTOQUEUEERRHOLD = "AEHL";

    //----------------------------------------------------------
    // LotComment_Contents null string for schduler (P5000290)
    //----------------------------------------------------------
    public static final String SP_LOTCOMMENT_CONTENTS_NULLSTRING = " ";

    //----------------------------------------------------------
    // ScriptParameter deletion (D5100006)
    //----------------------------------------------------------
    public static final String SP_SCRIPTPARM_DATATYPE_STRING = "STRING";
    public static final String SP_SCRIPTPARM_DATATYPE_INT = "INTEGER";
    public static final String SP_SCRIPTPARM_DATATYPE_REAL = "REAL";
    public static final String SP_SCRIPTPARM_DATATYPE_SI = "TABLESI";
    public static final String SP_SCRIPTPARM_DATATYPE_SS = "TABLESS";
    public static final String SP_SCRIPTPARM_DATATYPE_SR = "TABLESR";

    public static final String SP_SCRIPTPARM_CLASS_EQP = "Eqp";
    public static final String SP_SCRIPTPARM_CLASS_LOT = "Lot";
    public static final String SP_PARVALCLASS_CASSETTE = "Carrier";
    public static final String SP_PARVALCLASS_RETICLEPOD = "ReticlePod";
    public static final String SP_PARVALCLASS_RETICLE = "Reticle";
    public static final String SP_SCRIPTPARM_CLASS_LOGICALRECIPE = "LogicalRecipe";
    public static final String SP_SCRIPTPARM_CLASS_RECIPE = "Recipe";
    public static final String SP_SCRIPTPARM_CLASS_RETICLE = "Reticle";
    public static final String SP_SCRIPTPARM_CLASS_FIXTURE = "Fixture";
    public static final String SP_SCRIPTPARM_CLASS_ROUTE = "Route";
    public static final String SP_SCRIPTPARM_CLASS_PROCEESS = "Process";
    public static final String SP_SCRIPTPARM_CLASS_PRODUCT = "Product";
    public static final String SP_SCRIPTPARM_CLASS_TECH = "Tech";
    public static final String SP_SCRIPTPARM_CLASS_USER = "User";
    public static final String SP_SCRIPTPARM_CLASS_FACTORY = "Factory";
    public static final String SP_SCRIPTPARM_CLASS_WAFER = "Wafer";

    //----------------------------------------------------------
    // PSM Execution Fail Hold (D5100068)
    //----------------------------------------------------------
    public static final String SP_REASON_PSMEXECUTIONFAILHOLD = "PSME";

    public static final String SP_REASON_FSMEXECUTIONFAILHOLD = "FSME";

    //----------------------------------------------------------
    // suffix of ServerManager's object reference file (D5100157)
    //----------------------------------------------------------
    public static final String SP_MMOBJREF_SUFFIX_FOR_STCS = "MMObjRef";

    //--------------------------------------------------------------------
    // Max Chamber length (P5100310)
    //--------------------------------------------------------------------
    public static final int SP_MAX_CHAMBER_LEN = 20;


    //--------------------------------------------------------------------
    // APC I/F Enhance for Interface Spec-B(D5100354)
    //--------------------------------------------------------------------
    public static final String SP_USE_APC_PARM_AT_OPESTART = "USE_APC_PARM_AT_OPESTART";

    public static final String SP_APC_CONTROLJOBSTATUS_CREATED = "Created";
    public static final String SP_APC_CONTROLJOBSTATUS_EXECUTING = "Executing";
    public static final String SP_APC_CONTROLJOBSTATUS_PAUSED = "Paused";
    public static final String SP_APC_CONTROLJOBSTATUS_COMPLETED = "Completed";
    public static final String SP_APC_CONTROLJOBSTATUS_STOPPED = "Stopped";
    public static final String SP_APC_CONTROLJOBSTATUS_ABORTED = "Aborted";
    public static final String SP_APC_CONTROLJOBSTATUS_CANCELED = "Canceled";
    public static final String SP_APC_CONTROLJOBSTATUS_PARTIALCOMPLETED = "PartialCompleted";

    public static final String SP_APC_CONFIG_STATE_REQUESTED = "NotApproved";
    public static final String SP_APC_CONFIG_STATE_APPROVED = "Approved";
    public static final String SP_APC_CONFIG_STATE_REJECTED = "NotApproved";

    public static final String SP_APCBASERESPONSE_REQUIRED_TRUE = "true";
    public static final String SP_APCBASERESPONSE_REQUIRED_FALSE = "false";

    public static final String SP_APCRETURNCODE_OK = "OK";
    public static final String SP_APCRETURNCODE_ERROR = "ERROR";

    public static final String SP_APCFUNCTIONTYPE_RECIPEPARAMETERADJUST = "recipeParameterAdjust";
    public static final String SP_APCFUNCTIONTYPE_DERIVEDDATA = "derivedData";
    public static final String SP_APCFUNCTIONTYPE_FAULTDETECTION = "faultDetection";
    public static final String SP_APCFUNCTIONTYPE_DATACOLLECTION = "dataCollection";
    public static final String SP_APCFUNCTIONTYPE_PROCESSDISPOSITION = "processDisposition";
    public static final String SP_APCFUNCTIONTYPE_PRODUCTDISPOSITION = "productDisposition";
    public static final String SP_APCFUNCTIONTYPE_SAHDACTIVATE = "SAHDActivate";
    public static final String SP_APCFUNCTIONTYPE_SAHDRELEASECHECK = "SAHDReleaseCheck";
    public static final String SP_APCFUNCTIONTYPE_LOTSAMPLING = "lotSampling";
    public static final String SP_APCFUNCTIONTYPE_WAFERSAMPLING = "waferSampling";

    public static final String SP_APCIF_POINT_NEW = "new";
    public static final String SP_APCIF_POINT_UPDATE = "update";

    public static final String SP_APCSPECIALINSTRUCTION_ROUTE = "route";
    public static final String SP_APCSPECIALINSTRUCTION_PROCESSDEFINITION = "processDefinition";
    public static final String SP_APCSPECIALINSTRUCTION_EQUIPMENT = "equipment";
    public static final String SP_APCSPECIALINSTRUCTION_PRODUCT = "product";
    public static final String SP_APCSPECIALINSTRUCTION_RETICLE = "reticle";
    public static final String SP_APCSPECIALINSTRUCTION_FIXTURE = "fixture";
    public static final String SP_APCSPECIALINSTRUCTION_RETICLEGROUP = "reticleGroup";
    public static final String SP_APCSPECIALINSTRUCTION_FIXTUREGROUP = "fixtureGroup";
    public static final String SP_APCSPECIALINSTRUCTION_MACHINERECIPE = "machineRecipe";
    public static final String SP_APCSPECIALINSTRUCTION_OPERATION = "operation";
    public static final String SP_APCSPECIALINSTRUCTION_STAGE = "stage";
    public static final String SP_APCSPECIALINSTRUCTION_CHAMBER = "chamber";
    public static final String SP_APCSPECIALINSTRUCTION_MODULEPROCESSDEFINITION = "moduleProcessDefinition";

    public static final String SP_REASON_ENTITYINHIBIT_FOR_APCBUILDTIME = "APC1";
    //D5100372 public static final  String SP_Reason_EntityInhibit_For_APCError       ="APCE";
    public static final String SP_REASON_ENTITYINHIBIT_FOR_APCERROR = "APCE";
    public static final String SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE = "APNR";
    public static final String SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG = "APNG";
    public static final String SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR = "APVE";
    public static final String SP_REASON_ENTITYINHIBIT_FOR_APCBUILDTIME_END = "APBE";

    public static final String SP_APCIFOPECATEGORY_NEW = "new";
    public static final String SP_APCIFOPECATEGORY_UPDATE = "update";
    public static final String SP_APCIFOPECATEGORY_DELETE = "delete";

    public static final String SP_DCDEF_CALC_APC = "APC";

    public static final String SP_LOT_SENDAHEADLOT = "sendAheadLot";

    public static final String SP_APCPROCESSDISPOSITION_ACTIONTYPE_INHIBIT = "Inhibit Action";
    public static final String SP_APCPROCESSDISPOSITION_ACTIONTYPE_CONTROLJOB = "ControlJob Action";
    public static final String SP_APCPROCESSDISPOSITION_ACTIONTYPE_EQUIPMENTSTATUS = "Equipment Status Action";

    public static final String SP_APCPROCESSDISPOSITION_CONTROLJOBACTION_TYPE_STOP = "stop";
    public static final String SP_APCPROCESSDISPOSITION_CONTROLJOBACTION_TYPE_ABORT = "abort";
    public static final String SP_APCPROCESSDISPOSITION_CONTROLJOBACTION_TYPE_PAUSE = "pause";
    public static final String SP_APCPROCESSDISPOSITION_CONTROLJOBACTION_TYPE_RESTART = "restart";
    public static final String SP_APCPROCESSDISPOSITION_CONTROLJOBACTION_TYPE_COMPLETE = "complete";

    public static final String SP_CONTROLJOBACTION_TYPE_STOP = "stop";
    public static final String SP_CONTROLJOBACTION_TYPE_ABORT = "abort";
    public static final String SP_CONTROLJOBACTION_TYPE_PAUSE = "pause";
    public static final String SP_CONTROLJOBACTION_TYPE_RESTART = "restart";
    public static final String SP_CONTROLJOBACTION_TYPE_COMPLETE = "complete";

    public static final String SP_CONTROLJOBACTION_TYPE_PRIORITY = "priority";
    public static final String SP_CONTROLJOBACTION_TYPE_CREATE = "create";
    public static final String SP_CONTROLJOBACTION_TYPE_QUEUE = "queue";
    public static final String SP_CONTROLJOBACTION_TYPE_EXECUTE = "execute";
    public static final String SP_CONTROLJOBACTION_TYPE_DELETE = "delete";
    public static final String SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP = "delete_FromEqp";
    public static final String SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE = "delete_FromLotAndCassette";

    public static final String SP_CONTROLJOBSTATUS_CREATED = "Created";
    public static final String SP_CONTROLJOBSTATUS_QUEUED = "Queued";
    public static final String SP_CONTROLJOBSTATUS_EXECUTING = "Executing";
    public static final String SP_CONTROLJOBSTATUS_COMPLETED = "Completed";
    public static final String SP_CONTROLJOBSTATUS_PRIORITY = "Priority";
    public static final String SP_CONTROLJOBSTATUS_STOPPED = "Stopped";
    public static final String SP_CONTROLJOBSTATUS_ABORTED = "Aborted";
    public static final String SP_CONTROLJOBSTATUS_DELETED = "Deleted";
    public static final String SP_TCSCONTROLJOBSTATUS_RESERVED = "Reserved";
    public static final String SP_TCSCONTROLJOBSTATUS_OPESTARTED = "OpeStarted";
    public static final String SP_TCSCONTROLJOBSTATUS_CREATED = "Created";
    public static final String SP_TCSCONTROLJOBSTATUS_SENT = "Sent";
    public static final String SP_TCSCONTROLJOBSTATUS_QUEUED = "QUEUED";
    public static final String SP_TCSCONTROLJOBSTATUS_SELECTED = "SELECTED";
    public static final String SP_TCSCONTROLJOBSTATUS_WAITINGFORSTART = "WAITINGFORSTART";
    public static final String SP_TCSCONTROLJOBSTATUS_EXECUTING = "EXECUTING";
    public static final String SP_TCSCONTROLJOBSTATUS_PAUSED = "PAUSED";
    public static final String SP_TCSCONTROLJOBSTATUS_COMPLETED = "COMPLETED";
    public static final String SP_TCSCONTROLJOBSTATUS_OPECOMPLETED = "OpeCompleted";
    public static final String SP_TCSCONTROLJOBSTATUS_CJCANCELED = "CJCanceled";

    public static final String SP_CONTROLJOB_STATUS = "CONTROLJOB_STATUS";
    public static final String SP_CONTROLJOB_CLAIM_USER_ID = "CONTROLJOB_CLAIM_USER_ID";
    public static final String SP_CONTROLJOB_CLAIM_TIME = "CONTROLJOB_CLAIM_TIME";

    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_RELEASESAHD = "releaseSAHD";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_ANOTHERSAHD = "anotherSAHD";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_PASS = "pass";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_REDO = "redo";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_TESTMORE = "testMore";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_HOLD = "hold";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_FUTUREHOLD = "futureHold";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_REWORK = "rework";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SCRAP = "scrap";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT = "split";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_BANK = "bank";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_LOCATE = "locate";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_BRANCH = "branch";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT_HOLD = "split_hold";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT_FUTUREHOLD = "split_futureHold";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT_REWORK = "split_rework";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT_SCRAP = "split_scrap";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT_BRANCH = "split_branch";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT_BANK = "split_bank";
    public static final String SP_APCPRODUCTDISPOSITION_FUNCTIONTYPE_SPLIT_LOCATE = "split_locate";

    public static final String SP_APC_FHSAHDHS_OPECATEGORY_ACTIVATED = "activated";
    public static final String SP_APC_FHSAHDHS_OPECATEGORY_CANCELED = "canceled";
    public static final String SP_APC_FHSAHDHS_OPECATEGORY_RELEASED = "released";

    public static final String SP_LOT_ORIGINALPRIORITYCLASS = "originalPriorityClass";

    public static final String SP_APCBASELOT_SENDAHEADTYPE_NONE = "none";
    public static final String SP_APCBASELOT_SENDAHEADTYPE_SINGLEWAFER = "singleWafer";
    public static final String SP_APCBASELOT_SENDAHEADTYPE_MULTIWAFER = "multiWafer";
    public static final String SP_APCBASELOT_SENDAHEADTYPE_ALLWAFERS = "allWafers";

    public static final String SP_APCBASEWAFER_SENDAHEADWAFERFLAG_TRUE = "true";
    public static final String SP_APCBASEWAFER_SENDAHEADWAFERFLAG_FALSE = "false";

    public static final String SP_APCBASEWAFER_PROCESSFLAG_TRUE = "true";
    public static final String SP_APCBASEWAFER_PROCESSFLAG_FALSE = "false";

    public static final String SP_APCENTITYVALUEFILTER_USERIDFILTER = "UserIDFilter";
    public static final String SP_APCENTITYVALUEFILTER_EQUIPMENTFILTER = "EquipmentFilter";
    public static final String SP_APCENTITYVALUEFILTER_RESOURCESTATUSFILTER = "ResourceStatusFilter";
    public static final String SP_APCENTITYVALUEFILTER_LOGICALRECIPEFILTER = "LogicalRecipeFilter";
    public static final String SP_APCENTITYVALUEFILTER_MACHINERECIPEFILTER = "MachineRecipeFilter";
    public static final String SP_APCENTITYVALUEFILTER_ROUTEFILTER = "RouteFilter";
    public static final String SP_APCENTITYVALUEFILTER_OPERATIONFILTER = "OperationFilter";
    public static final String SP_APCENTITYVALUEFILTER_TECHNOLOGYFILTER = "TechnologyFilter";
    public static final String SP_APCENTITYVALUEFILTER_PRODUCTGROUPFILTER = "ProductGroupFilter";
    public static final String SP_APCENTITYVALUEFILTER_PRODUCTFILTER = "ProductFilter";
    public static final String SP_APCENTITYVALUEFILTER_PROCESSDEFINITIONFILTER = "ProcessDefinitionFilter";
    public static final String SP_APCENTITYVALUEFILTER_RETICLEFILTER = "ReticleFilter";
    public static final String SP_APCENTITYVALUEFILTER_RETICLEGROUPFILTER = "ReticleGroupFilter";
    public static final String SP_APCENTITYVALUEFILTER_FIXTUREFILTER = "FixtureFilter";
    public static final String SP_APCENTITYVALUEFILTER_FIXTUREGROUPFILTER = "FixtureGroupFilter";
    public static final String SP_APCENTITYVALUEFILTER_LOTFILTER = "LotFilter";
    public static final String SP_APCENTITYVALUEFILTER_REASONCATEGORYFILTER = "ReasonCategoryFilter";
    public static final String SP_APCENTITYVALUEFILTER_REASONFILTER = "ReasonFilter";
    public static final String SP_APCENTITYVALUEFILTER_BANKFILTER = "BankFilter";
    public static final String SP_APCENTITYVALUEFILTER_PHOTOLAYERFILTER = "PhotoLayerFilter";

    public static final String SP_APCENTITYVALUEWHERE_BANKTYPE = "bankType";
    public static final String SP_APCENTITYVALUEWHERE_DEPARTMENTID = "departmentID";
    public static final String SP_APCENTITYVALUEWHERE_EQUIPMENTID = "equipmentID";
    public static final String SP_APCENTITYVALUEWHERE_FAMILYCODE = "familyCode";
    public static final String SP_APCENTITYVALUEWHERE_FIXTURECATEGORY = "fixtureCategory";
    public static final String SP_APCENTITYVALUEWHERE_FIXTUREGROUPID = "fixtureGroupID";
    public static final String SP_APCENTITYVALUEWHERE_FIXTUREID = "fixtureID";
    public static final String SP_APCENTITYVALUEWHERE_LOCATION = "location";
    public static final String SP_APCENTITYVALUEWHERE_LOGICALRECIPEID = "logicalRecipeID";
    public static final String SP_APCENTITYVALUEWHERE_LOTID = "lotID";
    public static final String SP_APCENTITYVALUEWHERE_LOTSTATUS = "lotStatus";
    public static final String SP_APCENTITYVALUEWHERE_LOTTYPE = "lotType";
    public static final String SP_APCENTITYVALUEWHERE_MACHINERECIPEID = "machineRecipeID";
    public static final String SP_APCENTITYVALUEWHERE_MODELNUMBER = "modelNumber";
    public static final String SP_APCENTITYVALUEWHERE_PHOTOLAYERID = "photoLayerID";
    public static final String SP_APCENTITYVALUEWHERE_PROCESSDEFINITIONID = "processDefinitionID";
    public static final String SP_APCENTITYVALUEWHERE_PRODUCTGROUPID = "productGroupID";
    public static final String SP_APCENTITYVALUEWHERE_PRODUCTID = "productID";
    public static final String SP_APCENTITYVALUEWHERE_REASONCATEGORYID = "reasonCategoryID";
    public static final String SP_APCENTITYVALUEWHERE_RECIPENAMESPACE = "recipeNameSpace";
    public static final String SP_APCENTITYVALUEWHERE_RETICLEGROUPID = "reticleGroupID";
    public static final String SP_APCENTITYVALUEWHERE_RETICLEID = "reticleID";
    public static final String SP_APCENTITYVALUEWHERE_ROUTEID = "routeID";
    public static final String SP_APCENTITYVALUEWHERE_ROUTETYPE = "routeType";
    public static final String SP_APCENTITYVALUEWHERE_TECHNOLOGYID = "technologyID";
    public static final String SP_APCENTITYVALUEWHERE_WORKAREA = "workArea";
    public static final String SP_APCENTITYVALUEWHERE_OPERATION_EQUALS = "equals";
    public static final String SP_APCENTITYVALUEWHERE_OPERATION_LIKE = "like";

    public static final String SP_APCENTITYVALUE_COLUMNNAME_USERID = "UserID";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_EQUIPMENT = "Equipment";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_RESOURCE = "Resource";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_LOGICALRECIPE = "LogicalRecipe";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_MACHINERECIPE = "MachineRecipe";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_ROUTE = "Route";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_OPERATION = "Operation";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_TECHNOLOGY = "Technology";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_PRODUCTGROUP = "ProductGroup";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_PRODUCT = "Product";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_PROCESSDEFINITION = "ProcessDefinition";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_RETICLE = "Reticle";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_RETICLEGROUP = "ReticleGroup";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_FIXTURE = "Fixture";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_FIXTUREGROUP = "FixtureGroup";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_LOT = "Lot";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_REASONCATEGORY = "ReasonCategory";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_REASON = "Reason";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_BANK = "Bank";
    public static final String SP_APCENTITYVALUE_COLUMNNAME_PHOTOLAYER = "PhotoLayer";
    public static final String SP_APCENTITYVALUE_COLUMNTYPE_STRING = "string";

    public static final String SP_REASON_APCERRORHOLD = "APCE";
    public static final String SP_SPECCHECKRESULT_APCERROR = "7";
    public static final String SP_SPECCHECKRESULT_7 = "APCCalc-Error";
    public static final String SP_SPECCHECKRESULT_1X_APCERROR = "17";
    public static final String SP_SPECCHECKRESULT_17 = "APCCalc-Error";


    public static final String SP_APCSPCVIOLATION_ACTION_WARNING = "warning";
    public static final String SP_APCSPCVIOLATION_ACTION_LOTHOLD = "lotHold";
    public static final String SP_APCSPCVIOLATION_ACTION_INHIBITEQUIPMENTID = "inhibitEquipmentID";
    public static final String SP_APCSPCVIOLATION_ACTION_INHIBITOPERATIONNUMBER = "inhibitOperationNumber";
    public static final String SP_APCSPCVIOLATION_ACTION_INHIBITROUTEID = "inhibitRouteID";
    public static final String SP_APCSPCVIOLATION_ACTION_INHIBITRECIPEID = "inhibitRecipeID";

    public static final String SP_APCSPCRESULT_VIOLATION_TRUE = "true";
    public static final String SP_APCSPCRESULT_VIOLATION_FALSE = "false";

    public static final String SP_RECIPEPARAMETERREQUEST_FINAL_TRUE = "true";
    public static final String SP_RECIPEPARAMETERREQUEST_FINAL_FALSE = "false";

    public static final String SP_APCIF_OPERATION_NEW = "new";
    public static final String SP_APCIF_OPERATION_UPDATE = "update";
    public static final String SP_APCIF_OPERATION_DELETE = "delete";
    public static final String SP_APCIF_OPERATION_APPROVE = "approve";
    public static final String SP_APCIF_OPERATION_APPROVEX = "approvex";
    public static final String SP_APCIF_OPERATION_REJECT = "reject";

    public static final String SP_APC_ENTITYCLASS_EQUIPMENT = "Equipment";
    public static final String SP_APC_ENTITYCLASS_EQUIPMENTTYPE = "EquipmentType";

    //--------------------------------------------------------------------
    // Internal Lot Hold for OpeComp (D6000051)
    //--------------------------------------------------------------------
    public static final String SP_REASON_LOTLOCK = "LOCK";
    public static final String SP_REASON_LOTLOCKRELEASE = "LOCR";
    public static final String SP_REASON_PARTIALOPECOMPHOLD = "HPOC";


    //--------------------------------------------------------------------
    // Inhibit inquiry condition (D6000217)
    //--------------------------------------------------------------------
    public static final String SP_ALL_WILD_CARD = "***";
    public static final String SP_ENDTIME_TODAY = "0";
    public static final String SP_ENDTIME_PAST = "-1";
    public static final String SP_TIMESTAMP_SEPARATOR_HYPHEN = "-";
    public static final String SP_TIMESTAMP_SEPARATOR_DOT = ".";
    public static final int SP_TIMESTAMP_MAX_HOUR = 23;
    public static final int SP_TIMESTAMP_MAX_MINUTE = 59;
    public static final int SP_TIMESTAMP_MAX_SEC = 59;
    public static final int SP_TIMESTAMP_MAX_MILLISEC = 999999;

    //--------------------------------------------------------------------
    // Check for float's range.  (D6000255)
    //--------------------------------------------------------------------
    public static final int SP_FLOAT_RANGE_DIGITS = 12;
    public static final double SP_FLOAT_RANGE_MAX = 1000000000000.0;
    public static final double SP_FLOAT_RANGE_MIN = -100000000000.0;

    //--------------------------------------------------------------------
    // Internal Hold Improvement: Internal Hold User.  (D6000285)
    //--------------------------------------------------------------------
    public static final String SP_INTERNALHOLDUSER_AFTEROPECOMP = "AfterOpeComp";
    public static final String SP_INTERNALHOLDUSER_AFTERSCRIPT = "AfterScript";


    //--------------------------------------------------------------------
    // Add for split number enhancement.  (D6000389)
    //--------------------------------------------------------------------
    public static final String SP_DEFAULT_LOT_FAMILY_SUFFIX = "00";


    //-------------------------------------------------------
    // D7000021  For Post Processing Execution
    //-------------------------------------------------------
    public static final String SP_POSTPROCESSACTIONINFO_ADD = "Add";
    public static final String SP_POSTPROCESSACTIONINFO_UPDATE = "Update";
    public static final String SP_POSTPROCESSACTIONINFO_DELETE = "Delete";
    public static final String SP_POSTPROCESSACTIONINFO_DELETEWITHLOT = "DeleteWithLot";
    public static final String SP_POSTPROCESSACTIONINFO_DELETEADDITIONALINFO = "DeleteAdditionalInfo";
    public static final String SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO = "AddAdditionalInfo";
    public static final String SP_POSTPROCESSACTIONINFO_DELETEWITHCAST = "DeleteWithCast";

    public static final String SP_POSTPROCESS_STATE_WAITING = "Waiting";
    public static final String SP_POSTPROCESS_STATE_RESERVED = "Reserved";
    public static final String SP_POSTPROCESS_STATE_BUSY = "Busy";
    public static final String SP_POSTPROCESS_STATE_COMPLETED = "Completed";
    public static final String SP_POSTPROCESS_STATE_ERROR = "Error";

    public static final int SP_POSTPROCESS_ERRACTION_NONE = 0;
    public static final int SP_POSTPROCESS_ERRACTION_REMOVEQUEUE = 1;

    public static final String SP_POSTPROCESS_ACTIONID_SCRIPT = "Script";
    public static final String SP_POSTPROCESS_ACTIONID_PSM = "PlannedSplit";
    public static final String SP_POSTPROCESS_ACTIONID_APCDISPOSITION = "APCDisposition";
    public static final String SP_POSTPROCESS_ACTIONID_MESSAGEQUEUEPUT = "MessageQueuePut";
    public static final String SP_POSTPROCESS_ACTIONID_MESSAGEQUEUEPUTFORLOTRECOVERY = "MessageQueuePutForLotRecovery";
    public static final String SP_POSTPROCESS_ACTIONID_FUTUREREWORK = "FutureRework";
    public static final String SP_POSTPROCESS_ACTIONID_UTSQUEUEPUT = "UTSQueuePut";
    public static final String SP_POSTPROCESS_ACTIONID_FPC = "DOCExec";
    public static final String SP_POSTPROCESS_ACTIONID_PARTIALCOMPLOTHOLD = "PartialCompLotHold";
    public static final String SP_POSTPROCESS_ACTIONID_COLLECTEDDATAACTIONBYPJ = "CollectedDataActionByPJ";

    public static final String SP_POSTPROCESS_TARGETTYPE_LOT = "LOT";
    public static final String SP_POSTPROCESS_TARGETTYPE_EQP = "EQP";
    public static final String SP_POSTPROCESS_TARGETTYPE_CJ = "CJ";
    public static final String SP_POSTPROCESS_TARGETTYPE_EQPANDCJ = "EQPandCJ";
    public static final String SP_POSTPROCESS_TARGETTYPE_LOT_ABSOLUTE = "LOT-ABSOLUTE";
    public static final String SP_POSTPROCESS_TARGETTYPE_CAST = "CAST";
    public static final String SP_POSTPROCESS_TARGETTYPE_CAST_ABSOLUTE = "CAST-ABSOLUTE";

    public static final String SP_POSTPROCESS_PATTERN_OPECOMP_SYNC = "OPECOMP-SYNC";
    public static final String SP_POSTPROCESS_PATTERN_LOT_SYNC = "LOT-SYNC";
    public static final String SP_POSTPROCESS_PATTERN_APC_SYNC = "APC-SYNC";
    public static final String SP_POSTPROCESS_PATTERN_LOT_BRSCRIPT_SYNC = "LOT-BRS-SYNC";
    public static final String SP_POSTPROCESS_PATTERN_LOT_BRSCRIPT_PSM_SYNC = "LOT-BRS-PSM-SYNC";
    public static final String SP_POSTPROCESS_PATTERN_LOT_BRSCRIPT_MSGQUEUE_SYNC = "LOT-BRS-MSG-SYNC";

    public static final String SP_POSTPROCESS_WATCHDOGNAME_DEFAULT = "PPSentinel";

    //-------------------------------------------------------
    // For Future Rework Request
    //-------------------------------------------------------
    public static final String SP_FUTUREREWORK_ACTION_ENTRY = "Entry";
    public static final String SP_FUTUREREWORK_ACTION_UPDATE = "Update";
    public static final String SP_FUTUREREWORK_ACTION_CANCEL = "Cancel";
    public static final String SP_FUTUREREWORK_ACTION_EXECUTE = "Execute";
    public static final String SP_FUTUREREWORK_ITEM_LOTID = "Lot ID";
    public static final String SP_FUTUREREWORK_ITEM_ROUTEID = "Route ID";
    public static final String SP_FUTUREREWORK_ITEM_OPENO = "Operation Number";
    public static final String SP_FUTUREREWORK_ITEM_TRIGGER = "Trigger";
    public static final String SP_FUTUREREWORK_ITEM_REWORKROUTEID = "Rework Route ID";
    public static final String SP_FUTUREREWORK_ITEM_RETURNOPENO = "Return Operation Number";
    public static final String SP_FUTUREREWORK_ITEM_REASONCODEID = "Reason Code ID";

    //---------------------------------------------------
    // D7000178 Waiting For Data Collection
    //---------------------------------------------------
    public static final String SP_REASON_WAITINGFORDATACOLLECTIONHOLD = "WDCH";
    public static final String SP_REASON_WAITINGDATACOLLECTIONHOLDRELEASE = "WDCR";

    /*
    1) 如果lot start transfer成功但有错误，EAP报告reason code（WFTE: Transfer error）
    2) 如果lot start transfer失败，EAP报告error message（WFTF: Transfer fail）
    3) 如果lot start transfer部分成功部分失败，EAP报告error message（WFTP: Transfer partial fail）
     */
    public static final String SP_REASON_WAITINGTRANSFERERRORONHOLD = "WFTE";
    public static final String SP_REASON_WAITINGTRANSFERFAILONHOLD = "WFTF";
    public static final String SP_REASON_WAITINGTRANSFERPARTIALFAILONHOLD = "WFTP";

    //--------------------------------------------------------------------
    // D7000248 Limit count for Entity Inhibition.
    //--------------------------------------------------------------------
    public static final int SP_ENTITYINHIBIT_LIMIT_COUNT = 3;

    //--------------------------------------------------------------------
    // D7000288 column length of D_THETABLEMARKER.
    //--------------------------------------------------------------------
    public static final int SP_TABLEMARKER_LENGTH = 129;

    //--------------------------------------------------------------------
    // P7000369 D_THETABLEMARKER for FRPO_DC_ITEMS.
    //--------------------------------------------------------------------
    public static final String SP_PO_DC_ITEMS_TABLEMARKER = "PosProcessOperation_AssignedDataCollections_dcItems";

    public static final String SP_FPC_CONTINUOUSSKIPLIMIT = "SP_FPC_CONTINUOUS_SKIPnc_LIMIT";

    public static final String SP_FPC_CATEGORY = "FPC";
    public static final String SP_FPCTYPE_BYLOT = "ByLot";
    public static final String SP_FPCTYPE_BYWAFER = "ByWafer";

    public static final String SP_FPCINFO_CREATE = "Create";
    public static final String SP_FPCINFO_UPDATE = "Update";
    public static final String SP_FPCINFO_NOCHANGE = "NoChange";
    public static final String SP_FPCINFO_DELETE = "Delete";

    public static final String SP_DCDEFINITION = "DCDefinition";
    public static final String SP_DCSPECIFICATION = "DCSpecification";

    public static final String SP_WHITEDEF_SEARCHCRITERIA_WHITE = "White";
    public static final String SP_WHITEDEF_SEARCHCRITERIA_NONWHITE = "NonWhite";
    public static final String SP_WHITEDEF_SEARCHCRITERIA_ALL = "All";

    public static final String SP_DC_SEARCHCRITERIA_PD = "PD";
    public static final String SP_DC_SEARCHCRITERIA_ALL = "All";

    public static final String SP_MCRECIPE_SEARCHCRITERIA_PD = "PD";
    public static final String SP_MCRECIPE_SEARCHCRITERIA_ALL = "All";

    public static final String SP_RPARM_SEARCHCRITERIA_LOGICALRECIPE = "LogicalRecipe";
    public static final String SP_RPARM_SEARCHCRITERIA_EQUIPMENT = "Equipment";

    public static final String SP_FPC_DEFINITION_ROUTE_MAIN = "Main";
    public static final String SP_FPC_DEFINITION_ROUTE_SUB = "Sub";
    public static final String SP_FPC_DEFINITION_ROUTE_SUB2 = "Sub2";

    public static final String SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT = "Equipment";
    public static final String SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE = "MachineRecipe";
    public static final String SP_FPC_PROCESSCONDITION_TYPE_DCDEF = "DCDef";
    public static final String SP_FPC_PROCESSCONDITION_TYPE_DCSPEC = "DCSpec";
    public static final String SP_FPC_PROCESSCONDITION_TYPE_PD = "PD";
    public static final String SP_FPC_PROCESSCONDITION_TYPE_LOGICALRECIPE = "LogicalRecipe";
    public static final String SP_FPC_PROCESSCONDITION_TYPE_RETICLE = "Reticle";
    public static final String SP_FPC_PROCESSCONDITION_TYPE_RETICLEGROUP = "ReticleGroup";

    public static final String SP_DCTYPE_PROCESS = "Process";
    public static final String SP_DCTYPE_MEASUREMENT = "Measurement";

    public static final String SP_FPC_EXCHANGETYPE_STARTRESERVEINFO = "StartReserveInfo";
    public static final String SP_FPC_EXCHANGETYPE_STARTRESERVEREQ = "StartReserveReq";
    public static final String SP_FPC_EXCHANGETYPE_OPECOMPREQ = "OpeCompReq";
    public static final String SP_FPC_EXCHANGETYPE_ALL = "ALL";
    public static final String SP_FPC_EXCHANGETYPE_BONDINGGROUP = "BondingGroup";


    //--------------------------------------------------------------------
    // D8000028 Port dispatch mode (OMEQPOPEMODE.OPE_MODE_ID)
    //--------------------------------------------------------------------
    public static final String SP_EQUIPMENT_DISPATCH_MODE_SEMI3 = "Semi-Start-3";
    public static final String SP_EQUIPMENT_DISPATCH_MODE_AUTO3 = "Auto-3";

    //--------------------------------------------------------------------
    // D8000028 durable available state (FRCAST.DRBL_STATE)
    //--------------------------------------------------------------------
    public static final String SP_DRBL_STATE_AVAILABLE = "AVAILABLE";
    public static final String SP_DRBL_SUBSTATE_IDLE = "IDLE";
    public static final String SP_DRBL_SUBSTATE_WIATINSP = "WaitInsp";
    public static final String SP_DRBL_SUBSTATE_INSPECTION = "Inspection";
    public static final String SP_DRBL_SUBSTATE_EQP = "EQP";
    public static final String SP_DRBL_SUBSTATE_WAIT_RELEASE = "WaitQC";
    public static final String SP_DRBL_SUBSTATE_HOLD = "Hold";
    public static final String SP_DRBL_SUBSTATE_WAIT_REPAIR = "WaitRepair";
    public static final String SP_DRBL_SUBSTATE_REPAIR = "Repair";
    public static final String SP_DRBL_SUBSTATE_TERMINATE = "Terminate";
    public static final String SP_DRBL_SUBSTATE_SCRAP = "Scrap";
    public static final String SP_DRBL_SUBSTATE_WAITSCAN = "WaitScan";
    //reticle action
    public static final String SP_RETICLE_INSPECTION_REQUEST = "Inspection Request";
    public static final String SP_RETICLE_INSPECTION_REQUEST_CANCEL = "Inspection Request Cancel";
    public static final String SP_RETICLE_INSPECTION_IN = "Inspection In";
    public static final String SP_RETICLE_INSPECTION_OUT = "Inspection Out";
    public static final String SP_RETICLE_INSPECTION_OUT_SUCESS = "Inspection Out Sucess";
    public static final String SP_RETICLE_INSPECTION_OUT_FAIL = "Inspection Out Fail";
    public static final String SP_RETICLE_CONFIRM_MASK_QUALITY = "Confirm Mask Quality";
    public static final String SP_RETICLE_HOLD = "Hold";
    public static final String SP_RETICLE_HOLD_RELEASE = "Hold Release";
    public static final String SP_RETICLE_TERMINATE = "Terminate";
    public static final String SP_RETICLE_XFERCHG = "Xfer Status Change";
    public static final String SP_RETICLE_TERMINATE_CANCEL = "Terminate Cancel";
    public static final String SP_RETICLE_SCRAP = "Scrap";
    public static final String SP_RETICLE_SCRAP_CANCEL = "Scrap Cancel";
    public static final String SP_RETICLE_REQUEST_REPAIR = "Request Repair";
    public static final String SP_RETICLE_REPAIR_IN = "Repair In";
    public static final String SP_RETICLE_REPAIR_OUT = "Repair Out";
    public static final String SP_RETICLE_JUST_IN = "Just In";
    public static final String SP_RETICLE_JUST_OUT = "Just Out";
    public static final String SP_RETICLE_SCAN_REQUEST = "Scan Request";
    public static final String SP_RETICLE_SCAN_COMPLETE = "Scan Complete";
    public static final String SP_RETICLE_SCAN_COMPLETE_SUCCESS = "Scan Complete Success";
    public static final String SP_RETICLE_SCAN_COMPLETE_FAIL = "Scan Complete Fail";
    public static final String SP_RETICLE_INSPECTION_TYPE_CHANGE = "Inspection Type Change";
    //reticle location
    public static final String SP_RETICLE_MASK_ROOM = "Mask Room";
    public static final String SP_RETICLE_STOCKER_ROOM = "Stocker Room";
    public static final String SP_RETICLE_QUALITY_CHECK = "IQC";
    public static final String SP_RETICLE_WAREHOUSE = "Warehouse";

    public static final String SP_RETICLE_EQUIPMENT = "Equipment";

    public static final String SP_HOLDTYPE_RETICLEHOLD = "ReticleHold";
    public static final String SP_HOLDTYPE_RELEASE_RETICLE_HOLD = "ReleaseReticleHold";
    public static final String SP_HOLDTYPE_RETICLE_SCRAP = "ReticleScrap";
    public static final String SP_HOLDTYPE_RELEASE_RETICLE_SCRAP = "ReleaseReticleScrap";
    public static final String SP_HOLDTYPE_RETICLE_TERMINATE = "ReticleTerminate";
    public static final String SP_HOLDTYPE_RELEASE_RETICLE_TERMINATE = "ReleaseReticleTerminate";

    //hold reason
    public static final String SP_RETICLE_REASON_INOF = "INOF";
    public static final String SP_RETICLE_REASON_REPO = "REPO";

    //--------------------------------------------------------------------
    // D8000028 WatchDog person (FRUSER.USER_ID)
    //--------------------------------------------------------------------
    public static final String SP_UTSWATCHDOG_PERSON = "UTWatcher";
    public static final String SP_UTSEVENTWATCHDOG_PERSON = "UEWatcher";

    //--------------------------------------------------------------------
    // For Durable Administration
    //--------------------------------------------------------------------
    //===== The actions for durables' administration event =======//
    public static final String SP_DURABLE_ACTION_CREATE = "Create";
    public static final String SP_DURABLE_ACTION_UPDATE = "Update";
    public static final String SP_DURABLE_ACTION_DELETE = "Delete";

    //===== The items of durables =======//
    public static final String SP_DURABLE_ITEM_MAXRUNTIME = "Max Run Time";
    public static final String SP_DURABLE_ITEM_MAXOPESTARTCOUNT = "Max Ope Start Count";
    public static final String SP_DURABLE_ITEM_CAPACITY = "Capacity";
    public static final String SP_DURABLE_ITEM_NOMINALSIZE = "Nominal Size";
    public static final String SP_DURABLE_ITEM_CONTENTS = "Contents";
    public static final String SP_DURABLE_ITEM_INSTANCENAME = "Instance Name";
    public static final String SP_DURABLE_ITEM_INTERVALBETWEENPM = "Interval Between PM";
    public static final String SP_DURABLE_ITEM_USAGECHECKFLAG = "Usage Check Required";

    public static final String SP_CHECKFLAG_ON = "1";
    public static final String SP_CHECKFLAG_OFF = "0";

    //===== The reference values for cassettes' settings =======//
    public static final int SP_CASSETTE_DEFAULT_CAPACITY = 25;
    public static final int SP_CASSETTE_DEFAULT_NOMINALSIZE = 12;
    public static final int SP_CASSETTE_MINIMUM_RUNTIME = 0;
    public static final int SP_CASSETTE_MINIMUM_OPERATIONSTARTCOUNT = 0;
    public static final int SP_CASSETTE_MINIMUM_INTERVALBETWEENPM = 0;
    public static final int SP_RETICLEPOD_MINIMUM_INTERVALBETWEENPM = 0;
    public static final int SP_CASSETTE_MAXIMUM_RUNTIME = 999999;
    public static final int SP_CASSETTE_MAXIMUM_OPERATIONSTARTCOUNT = 999999;
    public static final int SP_CASSETTE_MAXIMUM_INTERVALBETWEENPM = 999999;
    public static final int SP_RETICLEPOD_MAXIMUM_INTERVALBETWEENPM = 999999;

    public static final String SP_UNDEFINED_LIMIT = "-";

    //===== The reference values for reticle pods' settings =======//
    public static final int SP_RETICLEPOD_MINIMUM_CAPACITY = 1;
    public static final int SP_RETICLEPOD_MAXIMUM_CAPACITY = 6;

    //===== Data Type =====//
    public static final String SP_UDATA_TYPE_INTEGER = "Integer";
    public static final String SP_UDATA_TYPE_FLOAT = "Float";
    public static final String SP_UDATA_TYPE_BOOLEAN = "Boolean";

    public static final int SP_UDATA_MINIMUMVALUE_INTEGER = -9999999;
    public static final int SP_UDATA_MAXIMUMVALUE_INTEGER = 9999999;
    public static final double SP_UDATA_MINIMUMVALUE_FLOAT = -9999999.9999999;
    public static final double SP_UDATA_MAXIMUMVALUE_FLOAT = 9999999.9999999;
    public static final String SP_UDATA_TRUE = "1";
    public static final String SP_UDATA_FALSE = "0";

    //===== Class vs Udata's classID =====//
    public static final String SP_UDATA_POSCASSETTE = "DR06";
    public static final String SP_UDATA_POSRETICLEPOD = "DR08";

    public static final String SP_MATERIAL_CONTENTS_WAFER = "Wafer";

    //--------------------------------------------------------------------
    // D8000142 The Lot finished status
    //--------------------------------------------------------------------
    public static final String SP_LOT_FINISHED_STATE_COMPLETED = "COMPLETED";
    public static final String SP_LOT_FINISHED_STATE_EMPTIED = "EMPTIED";
    public static final String SP_LOT_FINISHED_STATE_SCRAPPED = "SCRAPPED";
    public static final String SP_LOT_FINISHED_STATE_TERMINATED = "TERMINATED";

    //--------------------------------------------------------------------
    // P8000123 Max length of HoldType
    //--------------------------------------------------------------------
    public static final int SP_HOLDTYPE_MAX_LEN = 20;

    //----------------------------------------------------------------------
    // D9000003 Prepare siview release number
    //----------------------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__080 = 8000;
    public static final int SP_SIVIEW_RELEASE_NUMBER__090 = 9000;

    //----------------------------------------------------------------------
    // D9000003 Wafer Sampling Policy Name, e-Mail type, System Message Code and HoldType.
    //----------------------------------------------------------------------
    public static final String SP_SAMPLING_POLICY_TOP = "Top";
    public static final String SP_SAMPLING_POLICY_BOTTOM = "Bottom";
    public static final String SP_SAMPLING_POLICY_RANDOM = "Random";
    public static final String SP_SAMPLING_POLICY_TOPANDBOTTOM = "Top and Bottom";
    public static final String SP_SAMPLING_POLICY_SLOT = "Slot";
    public static final String SP_SAMPLING_POLICY_EVEN = "Even";
    public static final String SP_SAMPLING_POLICY_ODD = "Odd";

    public static final String SP_SORTER_JOB_TYPE_SORTERJOB = "SorterJob";
    public static final String SP_SORTER_JOB_TYPE_COMPONENTJOB = "ComponentJob";

    public static final String SP_SORTERJOBSTATUS_WAIT_TO_EXECUTING = "Wait To Executing";
    public static final String SP_SORTERJOBSTATUS_EXECUTING = "Executing";
    public static final String SP_SORTERJOBSTATUS_COMPLETED = "Completed";
    public static final String SP_SORTERJOBSTATUS_ERROR = "Error";

    public static final String SP_SORTERCOMPONENTJOBSTATUS_WAIT_TO_EXECUTING = "Wait To Executing";
    public static final String SP_SORTERCOMPONENTJOBSTATUS_XFER = "Xfer";
    public static final String SP_SORTERCOMPONENTJOBSTATUS_EXECUTING = "Executing";
    public static final String SP_SORTERCOMPONENTJOBSTATUS_COMPLETED = "Completed";
    public static final String SP_SORTERCOMPONENTJOBSTATUS_ERROR = "Error";
    public static final String SP_SORTERCOMPONENTJOBSTATUS_CREATE = "Create";

    public static final String SP_SORTER_JOB = "SORT";
    public static final String SP_COMPONENT_JOB = "COMPONENT";


    public static final String SP_EQP_PORT_OPERATIONMODE_AUTO_1 = "Auto-1";
    public static final String SP_EQP_PORT_OPERATIONMODE_AUTO_2 = "Auto-2";
    public static final String SP_EQP_PORT_OPERATIONMODE_AUTO_3 = "Auto-3";

    public static final String SP_SORTER_JOB_ACTION_SORTJOBCREATE = "Sort Job Create";
    public static final String SP_SORTER_JOB_ACTION_SORTJOBSTART = "Sort Job Start";
    public static final String SP_SORTER_JOB_ACTION_COMPONENTJOBXFER = "Component Job Xfer";
    public static final String SP_SORTER_JOB_ACTION_COMPONENTJOBSTART = "Component Job Start";
    public static final String SP_SORTER_JOB_ACTION_COMPONENTJOBCOMP = "Component Job Comp";
    public static final String SP_SORTER_JOB_ACTION_SORTJOBCOMP = "Sort Job Comp";
    public static final String SP_SORTER_JOB_ACTION_SORTJOBERROR = "Sort Job Error";
    public static final String SP_SORTER_JOB_ACTION_SORTJOBDELETED = "Sort Job Deleted";

    public static final String SP_SORTER_AUTOSORTING = "AutoSorting";
    public static final String SP_SORTER_POSITIONCHANGEREAD = "PositionChangeRead";

    public static final int SP_SORTER_PORTCOUNTINPORTGROUP = 2;

    public static final String SP_SYSTEMMSGCODE_SORTERR = "SORTERR";
    public static final String SP_SORTERWATCHDOG_PERSON = "SRTWatcher";

    //----------------------------------------------------------------------
    // D9000068 For Systemkey Generation Logic Enhancement
    //----------------------------------------------------------------------
    //===== This is the environment variable at the time of creating Connection ID =======//
    //===== This is the constant value at the time of creating Connection ID =======//
    public static final String SP_SYSTEMKEY_MAINPD_KEYSTR_DEFAULT = "Main";
    public static final String SP_SYSTEMKEY_MODULEPD_KEYSTR_DEFAULT = "Module";
    public static final String SP_SYSTEMKEY_PD_KEYSTR_DEFAULT = "Operation";
    public static final String SP_SYSTEMKEY_PRTRSC_KEYSTR_DEFAULT = "PRT";
    public static final String SP_SYSTEMKEY_BUFRSC_KEYSTR_DEFAULT = "BUF";
    public static final String SP_SYSTEMKEY_EQP_KEYSTR_DEFAULT = "EQP";
    public static final String SP_SYSTEMKEY_STK_KEYSTR_DEFAULT = "STK";
    //===== This is the constant value for using it for the judgment of resourceLevel =======//
    public static final String SP_RESOURCELEVEL_BUFFERRESOURCE = "BufferResource";
    public static final String SP_RESOURCELEVEL_PORTRESOURCE = "PortResource";

    public static final String SP_CUSTOM_CARRIER_VALUE_FOSB = "FOSB";

    //-------------------------------------------------
    // D9000084 for ARMS
    //-------------------------------------------------
    public static final String SP_ARMS_SWITCH_ON = "1";
    //    public static final int SP_CAPACITY_INCREMENT_10 = 10;
    public static final int SP_CAPACITY_INCREMENT_50 = 50;
    //    public static final int SP_CAPACITY_INCREMENT_100 = 100;
    public static final int SP_MAX_RCJLEN_FROM_ONE_RDJ = 6;
    public static final String SP_RAL_SET_CODE_INSERT = "Insert";
    public static final String SP_RAL_SET_CODE_DELETE = "Delete";
    public static final String SP_RAL_SET_CODE_REJECTED = "Rejected";
    public static final String SP_RCJ_JOBNAME_STORE = "Store";
    public static final String SP_RCJ_JOBNAME_RETRIEVE = "Retrieve";
    public static final String SP_RCJ_JOBNAME_UNCLAMP = "Unclamp";
    public static final String SP_RCJ_JOBNAME_XFER = "Xfer";
    public static final String SP_RDJ_STATUS_CREATED = "Created";
    public static final String SP_RDJ_STATUS_WAITTORELEASE = "WaitToRelease";
    public static final String SP_RDJ_STATUS_WAITTOEXECUTE = "WaitToExecute";
    public static final String SP_RDJ_STATUS_EXECUTING = "Executing";
    public static final String SP_RDJ_STATUS_COMPLETED = "Completed";
    public static final String SP_RDJ_STATUS_ERROR = "Error";
    public static final String SP_RCJ_STATUS_WAITTOEXECUTE = "WaitToExecute";
    public static final String SP_RCJ_STATUS_EXECUTING = "Executing";
    public static final String SP_RCJ_STATUS_COMPLETED = "Completed";
    public static final String SP_RCJ_STATUS_ERROR = "Error";
    //-----------------------------------------------------------
    // Define Macro additional Reticle Attributes
    //-----------------------------------------------------------
    public static final String SP_RETICLE_CHIPSIZE_X = "CHIPSIZE_X";
    public static final String SP_RETICLE_CHIPSIZE_Y = "CHIPSIZE_Y";
    public static final String SP_RETICLE_CHIPS_FLD = "CHIPS_FLD";
    public static final String SP_RETICLE_COMMENTS = "COMMENTS";
    public static final String SP_RETICLE_GRP_PRIORITY = "GRP_PRIORITY";
    public static final String SP_RETICLE_KERF_PN = "KERF_PN";
    public static final String SP_RETICLE_LAST_USED_BY = "LAST_USED_BY";
    public static final String SP_RETICLE_MAGNIFICATION = "MAGNIFICATION";
    public static final String SP_RETICLE_MASK_PN = "MASK_PN";
    public static final String SP_RETICLE_MASK_EC = "MASK_EC";
    public static final String SP_RETICLE_MASK_TYPE = "MASK_TYPE";
    public static final String SP_RETICLE_MOPS_ORDNO = "MOPS_ORD#";
    public static final String SP_RETICLE_OWNER = "OWNER";
    public static final String SP_RETICLE_PEL_BACK = "PEL_BACK";
    public static final String SP_RETICLE_PEL_FRONT = "PEL_FRONT";
    public static final String SP_RETICLE_PRIORITY_HIST = "PRIORITY_HIST";
    public static final String SP_RETICLE_RESERVE_TOOL = "RESERVE_TOOL";
    public static final String SP_RETICLE_RETICLE_TYPE = "RETICLE_TYPE";
    public static final String SP_RETICLE_STEP_ARRAY_PN = "STEP_ARRAY_PN";
    public static final String SP_RETICLE_SUPPLIER = "SUPPLIER";
    public static final String SP_RETICLE_TECHNOLOGY = "TECHNOLOGY";
    public static final String SP_RETICLE_TIME_LAST_USED = "TIME_LAST_USED";
    public static final String SP_RETICLE_TOOL_LAST_USED = "TOOL_LAST_USED";
    public static final String SP_RETICLE_TOOL_TYPE = "TOOL_TYPE";
    public static final String SP_RETICLE_EXPOSURE_LIMIT = "EXPOSURE_LIMIT";
    public static final String SP_RETICLE_EXPOSURE_WARN_LIMIT = "EXPOSURE_WARN_LIMIT";
    public static final String SP_RETICLE_CURR_EXPOSURE_COUNT = "CURR_EXPOSURE_COUNT";
    public static final String SP_RETICLE_CURR_EXP_CNT_REQUAL_ID = "CURR_EXP_CNT_REQUAL_ID";
    public static final String SP_RETICLE_WAFER_LIMIT = "WAFER_LIMIT";
    public static final String SP_RETICLE_WAFER_WARN_LIMIT = "WAFER_WARN_LIMIT";
    public static final String SP_RETICLE_CURR_WAFER_COUNT = "CURR_WAFER_COUNT";
    public static final String SP_RETICLE_CURR_WAR_CNT_REQUAL_ID = "CURR_WAFER_CNT_REQUAL_ID";
    public static final String SP_RETICLE_JOB_LIMIT = "JOB_LIMIT";
    public static final String SP_RETICLE_JOB_WARN_LIMIT = "JOB_WARN_LIMIT";
    public static final String SP_RETICLE_CURR_JOB_COUNT = "CURR_JOB_COUNT";
    public static final String SP_RETICLE_CURR_JOB_CNT_REQUAL_ID = "CURR_JOB_CNT_REQUAL_ID";
    public static final String SP_RETICLE_TIME_LIMIT = "TIME_LIMIT";
    public static final String SP_RETICLE_TIME_WARN_LIMIT = "TIME_WARN_LIMIT";
    public static final String SP_RETICLE_CURR_TIME_COUNT = "CURR_TIME_COUNT";
    public static final String SP_RETICLE_CURR_TIME_CNT_REQUAL_ID = "CURR_TIME_CNT_REQUAL_ID";
    public static final String SP_RETICLE_CURR_INSP = "CURR_INSP";
    public static final String SP_RETICLE_LAST_MAINT_DATE = "LAST_MAINT_DATE";
    public static final String SP_RETICLE_LAST_MAINT_ID = "LAST_MAINT_ID";
    public static final String SP_RETICLE_PELLICLE_REPLACE_DATE = "PELLICLE_REPLACE_DATE";
    public static final String SP_RETICLE_SUCCESSFUL_REQUAL_ACTION = "SUCCESSFUL_REQUAL_ACTION";
    public static final String SP_RETICLE_REQUAL_ID = "REQUAL_ID";
    public static final String SP_RETICLE_COUNTER_LIMIT_OVER_FLAG = "COUNTER_LIMIT_OVER_FLAG";

    // for RMS
    public static final String SP_ENABLE_RMS_FLAG = "RMS";

    // for RXM
    public static final String SP_ARMS_DELIVERY_FUNC_ENABLED = "SP_DELIVERY_FUNC_ENABLED";
    public static final String SP_BINDEVERYTIME_RXM = "SP_BINDEVERYTIME_RXM";
    public static final String SP_ARMS_POD_RETURN_TO_RPS = "SP_ARMS_POD_RETURN_TO_RPS";

    //--------------------------------------
    //   Object Lock Parameter
    //--------------------------------------
    public static final String SP_CLASSNAME_POSRETICLEPODPORTRESOURCE = "PosReticlePodPortResource";
    public static final String SP_MACHINE_TYPE_EQP = "Equipment";
    public static final String SP_RETICLEJOB_STORE = "Store";
    public static final String SP_RETICLEJOB_RETRIEVE = "Retrieve";
    public static final String SP_RETICLEPODPORT_ACCESSMODE_AUTO = "Auto";
    public static final String SP_RETICLEPODPORT_ACCESSMODE_MANUAL = "Manual";
    public static final String SP_RETICLEPODPORT_NOTRESERVED = "NotReserved";
    public static final String SP_RETICLEPODPORT_RESERVED = "Reserved";
    public static final String SP_RSP_EVENT_LOAD = "Load";
    public static final String SP_RSP_EVENT_UNLOAD = "Unload";
    public static final String SP_SYSTEMMSGCODE_ARMSERROR = "ARMSERR";
    public static final String SP_SYSTEMMSGCODE_WAFERSAMPLINGERROR = "WFSMPERR";
    public static final String SP_STOCKER_RETICLE_CAPACITY = "RETICLE_CAPACITY";
    public static final String SP_STOCKER_ONLINE_MODE = "ONLINE_MODE";
    public static final String SP_STOCKER_MAX_RETICLE_CAPACITY = "MAX_RETICLE_CAPACITY";
    public static final String SP_TRANSFERJOBSTATUS_COMP = "XCMP";
    public static final String SP_TRANSFERJOBSTATUS_ERROR = "XERR";
    public static final String SP_RTD_FUNCTION_CODE_RETICLEACTIONLISTINQ = "ReticleActionListInq";
    public static final String SP_RTD_COLUMNHEADER_PRIORITY = "Priority";
    public static final String SP_RTD_COLUMNHEADER_RETICLEID = "Reticle ID";
    public static final String SP_RTD_COLUMNHEADER_RETICLEPODID = "Reticle Pod ID";
    public static final String SP_RTD_COLUMNHEADER_TOEQUIPMENTID = "To EquipmentID";
    public static final String SP_EVENT_IPCKEY_EXSYS = "EVENT_IPCKEY_EXSYS";
    public static final String SP_RXMS_PERSON = "RXMS";
    public static final String SP_MACHINE_CATEGORY_EQUIPMENT = "Equipment";
    public static final String SP_MACHINE_CATEGORY_BARERETICLESTOCKER = "Bare Reticle Stocker";

    //-------------------------------------------------
    // FlowBatch Enhancement
    //-------------------------------------------------
    public static final int SP_FLOWBATCH_NOT_CLEAR = 0;

    //--------------------------------------------------------------------
    //   TakeOutIn transfer function support or not
    //--------------------------------------------------------------------
    public static final String SP_TRANSPORT_TYPE_TAKE_OUT_IN = "L";

    //--------------------------------------------------------------------
    //   Performance improvement for checkForDeletion.
    //--------------------------------------------------------------------
    public static final String SP_FRLOTPO_ACCESS_ACTUALSTART = "ActualStart";
    public static final String SP_FRLOTPO_ACCESS_DATACOLLECTION = "DataCollection";
    public static final String SP_FRLOTPO_ACCESS_RETICLE = "Reticle";
    public static final String SP_FRLOTPO_ACCESS_FIXTURE = "Fixture";
    public static final String SP_FRLOTPO_ACCESS_MACHINE = "Machine";
    public static final String SP_FRLOTPO_ACCESS_MACHINERECIPE = "MachineRecipe";
    public static final String SP_FRLOTPO_ACCESS_LOGICALRECIPE = "LogicalRecipe";
    public static final String SP_CHECK_FLAG_OFF = "0";

    //----------------------------------------------------------------------
    //   Prepare siview release number
    //----------------------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__100 = 10000;

    //----------------------------------------------------------------------
    //   SLM Dev
    //----------------------------------------------------------------------
    public static final String SP_RTD_FUNCTION_CODE_SLM = "SLMRetrievingCassette";
    public static final String SP_RTD_COLUMNHEADER_SLMCASSETTEID = "Carrier ID";
    public static final String SP_LOADPURPOSETYPE_SLMRETRIEVING = "SLM Retrieving";

    public static final String SP_OPERATION_STARTRESERVATIONFORSLM = "StartReservationForSLM";
    public static final String SP_OPERATION_SLMWAFERRETRIEVE = "SLMWaferRetrieve";
    public static final String SP_OPERATION_SLMWAFERRETRIEVECASSETTERESERVE = "SLMWaferRetrieveCassetteReserve";

    public static final String SP_SLMSTATE_RESERVED = "Reserved";
    public static final String SP_SLMSTATE_PROCESSSTART = "ProcessStart";
    public static final String SP_SLMSTATE_STORED = "Stored";
    public static final String SP_SLMSTATE_RETRIEVED = "Retrieved";
    public static final String SP_SLMSTATE_NONSLMOPE = "NonSLMOperation";

    public static final String SP_SLM_SWITCH_ON = "ON";
    public static final String SP_SLM_SWITCH_OFF = "OFF";

    public static final String SP_SYSTEMMSGCODE_SLMERROR = "SLMERR";

    public static final String SP_SLM_KEYCATEGORY_EQUIPMENT = "Equipment";
    public static final String SP_SLM_KEYCATEGORY_CONTROLJOB = "ControlJob";
    public static final String SP_SLM_KEYCATEGORY_PROCESSJOB = "ProcessJob";
    public static final String SP_SLM_KEYCATEGORY_LOT = "Lot";
    public static final String SP_SLM_KEYCATEGORY_WAFER = "Wafer";

    public static final String SP_SLM_ACTIONCODE_CASSETTERESERVE = "CassetteReserve";
    public static final String SP_SLM_ACTIONCODE_PORTRESERVE = "PortReserve";
    public static final String SP_SLM_ACTIONCODE_PORTRESERVECANCEL = "PortReserveCancel";
    public static final String SP_SLM_ACTIONCODE_PROCESSSTART = "ProcessStart";
    public static final String SP_SLM_ACTIONCODE_PROCESSINGCOMP = "ProcessingComp";

    public static final String SP_SLM_EVENTNAME_LOADREQ = "LoadReq";
    public static final String SP_SLM_EVENTNAME_UNLOADREQ = "UnloadReq";
    public static final String SP_SLM_EVENTNAME_WAFERSTORE = "WaferStore";
    public static final String SP_SLM_EVENTNAME_PROCESSINGCOMP = "ProcessingComp";
    public static final String SP_SLM_EVENTNAME_OPESTARTCANCEL = "OpeStartCancel";

    public static final String SP_SLM_MSG_DESTNOTACCESSIBLE = "SubstrateDestinationNotAccessible";
    public static final String SP_SLM_MSG_DESTUNKNOWN = "SubstrateDestinationUnknown";

    public static final String SP_POSTPROCESS_OBJECTTYPE_PRODUCTSPEC = "ProductSpec";
    public static final String SP_POSTPROCESS_OBJECTTYPE_PRODUCTGROUP = "ProductGroup";
    public static final String SP_POSTPROCESS_OBJECTTYPE_TECHNOLOGY = "Technology";
    public static final String SP_POSTPROCESS_OBJECTTYPE_LOT = "Lot";
    public static final String SP_CLASSNAME_POSPRODUCTSPECIFICATION = "PosProductSpecification";
    public static final String SP_CLASSNAME_POSPRODUCTGROUP = "PosProductGroup";
    public static final String SP_CLASSNAME_POSTECHNOLOGY = "PosTechnology";

    public static final String SP_POSTPROCESS_ACTIONID_COLLECTEDDATAACTION = "CollectedDataAction";
    public static final String SP_POSTPROCESS_ACTIONID_QTIME = "QTime";
    public static final String SP_POSTPROCESS_ACTIONID_PROCESSLAGTIME = "ProcessLagTime";
    public static final String SP_POSTPROCESS_ACTIONID_FUTUREHOLDPOST = "FutureHoldPost";
    public static final String SP_POSTPROCESS_ACTIONID_SCHEXEC = "SCHExec";
    public static final String SP_POSTPROCESS_ACTIONID_MONITOREDLOTHOLD = "MonitoredLotHold";
    public static final String SP_POSTPROCESS_ACTIONID_FUTUREHOLDPRE = "FutureHoldPre";
    public static final String SP_POSTPROCESS_ACTIONID_PROCESSHOLD = "ProcessHold";
    public static final String SP_POSTPROCESS_ACTIONID_AUTOBANKIN = "AutoBankIn";
    public static final String SP_POSTPROCESS_ACTIONID_EXTERNALPOSTPROCESSEXECREQ = "ExternalPostTaskExecuteReq";
    public static final String SP_POSTPROCESS_STATE_EXECUTING = "Executing";
    public static final String SP_POSTPROCESSACTIONINFO_UPDATEFORSTATUS = "UpdateForStatus";
    public static final String SP_POSTPROCESS_CONDITION_PRODUCTCATEGORY = "ProductCategory";
    public static final String SP_DEFAULTEXTPOSTPROCUSERGROUP = "ExtPostProc";

    public static final String CIMFW_LOT_STATE_LOTCREATED = "LOTCREATED";
    public static final String CIMFW_LOT_STATE_ACTIVE = "ACTIVE";
    public static final String CIMFW_LOT_PRODUCTIONSTATE_INREWORK = "INREWORK";
    public static final String CIMFW_LOT_FINISHEDSTATE_COMPLETED = "COMPLETED";
    public static final String CIMFW_LOT_FINISHEDSTATE_SCRAPPED = "SCRAPPED";

    //----------------------------------------------------------------------
//   Multi Fab Transfer Support
//----------------------------------------------------------------------
    public static final String SP_INTERFAB_UPDATEMODE_UPDATE = "Update";
    public static final String SP_INTERFAB_UPDATEMODE_DELETE = "Delete";

    public static final String SP_INTERFAB_XFERCATEGORY_FOUP = "FOUP";
    public static final String SP_INTERFAB_XFERCATEGORY_FOSB = "FOSB";

    public static final String SP_INTERFAB_OBJECTCATEGORY_LOT = "LOT";

    public static final String SP_INTERFAB_XFERSTATE_NONE = "-";
    public static final String SP_INTERFAB_XFERSTATE_REQUIRED = "Required";
    public static final String SP_INTERFAB_XFERSTATE_TRANSFERRING = "Transferring";
    public static final String SP_INTERFAB_XFERSTATE_ORIGINDELETING = "OriginDeleting";
    public static final String SP_INTERFAB_XFERSTATE_EMPTY = "";

    public static final String SP_INTERFAB_XFERPLANSTATE_CREATED = "Created";
    public static final String SP_INTERFAB_XFERPLANSTATE_EXECUTING = "Executing";
    public static final String SP_INTERFAB_XFERPLANSTATE_COMPLETED = "Completed";
    public static final String SP_INTERFAB_XFERPLANSTATE_CANCELED = "Canceled";

    public static final String SP_INTERFAB_XFEREVENTCATEGORY_XFERSTART = "InterFabXferStart";
    public static final String SP_INTERFAB_XFEREVENTCATEGORY_XFEREND = "InterFabXferEnd";
    public static final String SP_INTERFAB_XFEREVENTCATEGORY_XFERCANCEL = "InterFabXferCancel";

    public static final String SP_INTERFAB_CHECKLEVEL_REQUIRED = "Required";
    public static final String SP_INTERFAB_CHECKLEVEL_TRANSFERRING = "Transferring";
    public static final String SP_INTERFAB_CHECKLEVEL_REQUIREDANDXFERRING = "Required and Transferring";
    public static final String SP_INTERFAB_CHECKLEVEL_ORIGINDELETING = "OriginDeleting";

    public static final String SP_POSTPROCESS_ACTIONID_INTERFABXFER = "InterFabXfer";


    public static final String SP_INTERFAB_SAR_EVENT_COMPONENTNAME = "MMS";
    public static final String SP_INTERFAB_SAR_EVENT_LOTTRANSFER_START = "MMS_LotTransferStart";
    public static final String SP_INTERFAB_SAR_EVENT_LOTTRANSFER_COMP = "MMS_LotTransferXferComp";
    public static final String SP_INTERFAB_SAR_EVENT_ACTION_START = "MMS_ActionStart";

    public static final String SP_SYSTEMMSGCODE_FUTUREREWORKREGISTERROR = "FTRWREGERR";

    public static final String SP_INTERFAB_MONITORGROUPRELEASETYPE_MANUAL = "ManualRelease";

    public static final String SP_INTERFAB_ACTIONTX_TXENTITYINHIBITREQ = "TxMfgRestrictReq";
    public static final String SP_INTERFAB_ACTIONTX_TXSYSTEMMSGRPT = "TxAlertMessageRpt";
    public static final String SP_INTERFAB_ACTIONTX_TXINTERFABACTIONMONITORGROUPRELEASEREQ = "TxInterFabActionMonitorGroupReleaseReq";

    public static final char SP_INTERFAB_SPLIT_FABID_SUFFIX = '_';

    //----------------------------------------------------------------------
    //   STB Cancel support
    // ----------------------------------------------------------------------


    public static final String SP_LOT_STBCANCEL_ON = "1";
    //public static final String SP_LOT_PREPARECANCEL_ON = "1";

    //----------------------------------------------------------------------
    //   improvement of Auto3Dispatch
    //----------------------------------------------------------------------
    public static final String SP_EQPAUTO3SETTING_UPDATEMODE_INSERT = "Insert";
    public static final String SP_EQPAUTO3SETTING_UPDATEMODE_UPDATE = "Update";
    public static final String SP_EQPAUTO3SETTING_UPDATEMODE_DELETE = "Delete";
    public static final String SP_EQPAUTO3SETTING_EVENTTYPE_LOADREQ = "LoadReq";
    public static final String SP_EQPAUTO3SETTING_EVENTTYPE_UNLOADREQ = "UnloadReq";
    public static final String SP_EQPAUTO3SETTING_EVENTTYPE_SLMRETRIEVE = "SLMRetrieve";

    //----------------------------------------------------------------------
    //   Security Control Enhancement
    //----------------------------------------------------------------------
    public static final String SP_AUTH_AUTHSERVER_AVAILABLE = "SP_AUTH_AUTHSERVER_AVAILABLE";
    public static final String SP_AUTH_AUTHSERVER_AVAILABLE_TRUE = "1";
    public static final int SP_AUTH_AUTHENTICATE_RETRY_DEFAULT = 0;
    public static final int SP_AUTH_AUTHENTICATE_INTERVAL_DEFAULT = 1;
    public static final String SP_AUTH_CLIENTNAME_MMS = "MMS";

    //----------------------------------------------------------------------
    //   Multiple Corresponding Operations Support
    //----------------------------------------------------------------------
    public static final String SP_ENCRYPT_PASSWORD_ENCRYPT = "1";

    //----------------------------------------------------------------------
    //   Multiple Corresponding Operations Support
    //----------------------------------------------------------------------
    public static final String SP_ACTIONRESULT_CHECKTYPE_SPEC = "SPEC";
    public static final String SP_ACTIONRESULT_CHECKTYPE_SPC = "SPC";
    //----------------------------------------------------------------------
    //   Prepare siview release number
    //----------------------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__101 = 10100;

    public static final String SP_SPC_CHART_TYPE_MEAN = "Mean";
    public static final String SP_SPC_CHART_TYPE_RANGE = "Range";
    public static final String SP_SPC_CHART_TYPE_SIGMA = "Sigma";
    public static final String SP_SPC_CHART_TYPE_UNIFORMITY = "Uniformity";
    public static final String SP_SPC_CHART_TYPE_SIGMA_WITHIN_WAFER = "SigmaWithinWafer";
    public static final String SP_SPC_CHART_TYPE_SIGMA_BETWEEN_WAFER = "SigmaBetweenWafer";
    public static final String SP_SPC_CHART_TYPE_SINGLE = "Single";
    public static final String SP_SPC_CHART_TYPE_MOVING_RANGE = "MovingRange";
    public static final String SP_SPC_CHART_TYPE_COUNT = "Count";

    public static final String SP_SPC_CHART_TYPE_MEAN_INITIAL = "M";
    public static final String SP_SPC_CHART_TYPE_RANGE_INITIAL = "R";
    public static final String SP_SPC_CHART_TYPE_SIGMA_INITIAL = "S";
    public static final String SP_SPC_CHART_TYPE_UNIFORMITY_INITIAL = "UN";
    public static final String SP_SPC_CHART_TYPE_SIGMA_WITHIN_WAFER_INITIAL = "SWW";
    public static final String SP_SPC_CHART_TYPE_SIGMA_BETWEEN_WAFER_INITIAL = "SBW";
    public static final String SP_SPC_CHART_TYPE_SINGLE_INITIAL = "";
    public static final String SP_SPC_CHART_TYPE_MOVING_RANGE_INITIAL = "MR";
    public static final String SP_SPC_CHART_TYPE_COUNT_INITIAL = "C";


    //----------------------------------------------------------------------
    //   Wafer Stacking Operation Support
    //----------------------------------------------------------------------
    public static final String SP_BONDINGGROUPACTION_CREATE = "Create";
    public static final String SP_BONDINGGROUPACTION_DELETE = "Delete";
    public static final String SP_BONDINGGROUPACTION_PARTIALRELEASE = "PartialRelease";
    public static final String SP_BONDINGGROUPACTION_PARTIALRELEASEDESTINATION = "Destination";
    public static final String SP_BONDINGGROUPACTION_PARTIALRELEASESOURCE = "Source";
    public static final String SP_BONDINGGROUPACTION_UPDATE = "Update";
    public static final String SP_BONDINGGROUPSTATE_CREATED = "Created";
    public static final String SP_BONDINGGROUPSTATE_ERROR = "Error";
    public static final String SP_BONDINGGROUPSTATE_PROCESSED = "Processed";
    public static final String SP_BONDINGGROUPSTATE_RESERVED = "Reserved";
    public static final String SP_BONDINGGROUP_UPDATEMODE_CREATE = "Create";
    public static final String SP_BONDINGGROUP_UPDATEMODE_DELETE = "Delete";
    public static final String SP_BONDINGGROUP_UPDATEMODE_UPDATE = "Update";
    public static final String SP_BONDINGPROCESSSTATE_COMPLETED = "Completed";
    public static final String SP_BONDINGPROCESSSTATE_ERROR = "Error";
    public static final String SP_BONDINGPROCESSSTATE_UNKNOWN = "-";
    public static final String SP_BONDINGSIDE_BOTTOM = "Bottom";
    public static final String SP_BONDINGSIDE_TOP = "Top";
    public static final String SP_FLOWSECTIONCONTROLCATEGORY_BONDINGFLOW = "Bonding Flow";
    public static final String SP_FLOWSECTIONCONTROLCATEGORY_FLOWBATCH = "Flow Batch";
    public static final String SP_LOT_FINISHED_STATE_STACKED = "STACKED";
    public static final String SP_LOT_BONDINGCATEGORY_BASE = "Base";
    public static final String SP_LOT_BONDINGCATEGORY_BASECANCEL = "BaseCan";
    public static final String SP_LOT_BONDINGCATEGORY_TOP = "Top";
    public static final String SP_LOT_BONDINGCATEGORY_TOPCANCEL = "TopCan";
    public static final String SP_MC_CATEGORY_WAFERBONDING = "Wafer Bonding";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKING = "WaferStacking";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGBASE = "WaferStackingBase";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGBASECANCEL = "WaferBondingCancel";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGTOP = "WaferStackingTop";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGTOPCANCEL = "WaferBondingCancel";
    public static final String SP_POSTPROCESS_ACTIONID_WAFERSTACKING = "WaferStacking";
    public static final String SP_SCRAPSTATE_STACKED = "Stacked";

    //----------------------------------------------------------------------
    //   Owner Change Support
    //----------------------------------------------------------------------
    public static final String SP_OWNERCHANGEDEFINITIONTABLETYPE_FW = "FW";
    public static final String SP_OWNERCHANGEDEFINITIONTABLETYPE_UDATA = "UDATA";
    public static final String SP_OWNERCHANGEDEFINITIONTABLETYPE_OTHER = "OTHER";

    //--------------------------------------------------------------------
    //   Wafer Stacking Follow-up
    //--------------------------------------------------------------------
    public static final String SP_RTD_FUNCTION_CODE_LOTSINBONDINGFLOW = "LotsInBondingFlow";
    public static final String SP_HASHKEY_SECTIONNAME = "SectionName";
    public static final String SP_HASHKEY_PRODUCTID = "ProductID";
    public static final String SP_HASHKEY_LOTTYPE = "LotType";
    public static final String SP_HASHKEY_SUBLOTTYPE = "SubLotType";
    public static final String SP_HASHKEY_TARGETEQUIPMENTID = "TargetEquipmentID";
    public static final String SP_HASHKEY_LOTID = "LotID";

    public static final int DEFAULTRETRYINTERVALTIME = 60;

    //----------------------------------------------------------------------
    //   Prepare siview release number
    //----------------------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__110 = 11000;

    //----------------------------------------------------------------------
    //   Support SubLotType change by Schedule Change Reservation
    //----------------------------------------------------------------------
    public static final String SP_SYSTEMMSGCODE_SCRNOTICE = "SCRNOTIC";

    //----------------------------------------------------------------------
    //   PO Maintenance Improvement
    //----------------------------------------------------------------------
    public static final String SP_POSTPROCESS_ACTIONID_PODELQUEUEPUT = "PODelQueuePut";


    public static final int SP_POMAINTEVENTCREATETYPE_DISABLED = 0;
    public static final int SP_POMAINTEVENTCREATETYPE_ACTIVELOTENABLED = 1;
    public static final int SP_POMAINTEVENTCREATETYPE_INACTIVELOTENABLED = 2;
    public static final int SP_POMAINTEVENTCREATETYPE_ENABLED = 3;


    //--------------------------------------------------------------------
    //   Advanced Wafer Level Control
    //--------------------------------------------------------------------

    public static final String SP_FUNCTION_AVAILABLE_TRUE = "1";

    public static final String SP_PROCESSJOBSTATUS_CREATED = "Created";
    public static final String SP_PROCESSJOBSTATUS_QUEUED = "Queued";
    public static final String SP_PROCESSJOBSTATUS_SETTINGUP = "SettingUp";
    public static final String SP_PROCESSJOBSTATUS_WAITFORSTART = "WaitForStart";
    public static final String SP_PROCESSJOBSTATUS_EXECUTING = "Processing";
    public static final String SP_PROCESSJOBSTATUS_COMPLETE = "Completed";
    public static final String SP_PROCESSJOBSTATUS_ABORTED = "Aborted";
    public static final String SP_PROCESSJOBSTATUS_ABORTING = "Aborting";
    public static final String SP_PROCESSJOBSTATUS_PAUSED = "Paused";
    public static final String SP_PROCESSJOBSTATUS_PAUSING = "Pausing";
    public static final String SP_PROCESSJOBSTATUS_STOPPED = "Stopped";
    public static final String SP_PROCESSJOBSTATUS_STOPPING = "Stopping";
    public static final String SP_PROCESSJOBSTATUS_UNKNOWN = "-";
    public static final String SP_PROCESSJOBSTATUS_NONPROCESS = "NonProcess";

    public static final String SP_PROCESSJOBACTION_ABORT = "ProcessJobAbort";
    public static final String SP_PROCESSJOBACTION_PAUSE = "ProcessJobPause";
    public static final String SP_PROCESSJOBACTION_RESUME = "ProcessJobResume";
    public static final String SP_PROCESSJOBACTION_START = "ProcessJobStart";
    public static final String SP_PROCESSJOBACTION_STOP = "ProcessJobStop";
    public static final String SP_PROCESSJOBACTION_RECIPEPARAMETERADJUST = "ProcessJobRecipeParameterAdjust";

    public static final String SP_PROCESSJOBACTIONABORTED = "ProcessJobAborted";
    public static final String SP_PROCESSJOBACTIONPAUSED = "ProcessJobPaused";
    public static final String SP_PROCESSJOBACTIONRESUMED = "ProcessJobResumed";
    public static final String SP_PROCESSJOBACTIONSTARTED = "ProcessJobStarted";
    public static final String SP_PROCESSJOBACTIONSTOPPED = "ProcessJobStopped";
    public static final String SP_PROCESSJOBACTIONCOMPLETED = "ProcessJobCompleted";

    public static final String SP_PARTIALOPECOMP_ACTION_OPECOMP = "MoveOut";
    public static final String SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD = "MoveOutWithHold";
    public static final String SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL = "MoveInCancel";
    public static final String SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD = "MoveInCancelWithHold";

    public static final String SP_ACTIONRESULT_OK = "0";
    public static final String SP_APCDERIVEDDATAREQ_BYCONTROLJOB = "ByControlJob";
    public static final String SP_APCDERIVEDDATAREQ_BYPROCESSJOB = "ByProcessJob";

    public static final int SP_SIVIEW_RELEASE_NUMBER__120 = 12000;

    public static final String SP_DATACONDITIONCATEGORY_DATAVALUE = "DataValue";
    public static final String SP_DATACONDITIONCATEGORY_SPECCHECKRESULT = "SpecCheckResult";
    public static final String SP_DATACONDITIONCATEGORY_BYPJDATAITEM = "ByPJDataItem";
    public static final String SP_DATACONDITIONCATEGORY_EXPANDDERIVEDDATA = "ExpandDerivedData";

    //--------------------------------------------------------------------
    //   Auto Dispatch Control
    //--------------------------------------------------------------------
    public static final String SP_AUTODISPATCHCONTROL_CREATE = "Create";
    public static final String SP_AUTODISPATCHCONTROL_UPDATE = "Update";
    public static final String SP_AUTODISPATCHCONTROL_DELETE = "Delete";
    public static final String SP_AUTODISPATCHCONTROL_AUTODELETE = "AutoDelete";

    public static final String SP_POSTPROCESS_ACTIONID_AUTODISPATCHCONTROL = "AutoDispatchControl";


    //--------------------------------------------------------------------
    //   MainPOS Deletion Logic Improvement
    //--------------------------------------------------------------------
    public static final String SP_POSPROCESSOPERATIONSPECIFICATIONSTATE_UNINITIALIZED = "";
    public static final String SP_POSPROCESSOPERATIONSPECIFICATIONSTATE_INIT = "Init";
    public static final String SP_POSPROCESSOPERATIONSPECIFICATIONSTATE_OBSOLETE = "Obsolete";
    public static final String SP_POSPROCESSOPERATIONSPECIFICATIONSTATE_CLEAR = "Clear";


    //--------------------------------------------------------------------------------------
    //   Support equipment user defined data with dynamic key table
    //--------------------------------------------------------------------------------------
    public static final String SP_CONFIGURATIONCATEGORY_EQUIPMENTUSERDEFINEDDATAMODE = "EquipmentUserDefinedDataMode";

    public static final String SP_CONFIGURATIONCATEGORY_EQUIPMENTLOCKMODE = "EquipmentLockMode";


    //--------------------------------------------------------------------------------------
    //   Support Equipment PM related attributes update in post process
    //--------------------------------------------------------------------------------------
    public static final String SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE = "RunWaferInfoUpdate";

    public static final String SP_LAST_USED_RECIPE_UPDATE_FLAG_OFF = "0";
    public static final String SP_EQPATTR_UPDATE_BY_POSTPROC_FLAG_ON = "1";

    public static final String SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT = "RunWaferCnt";
    public static final String SP_THREADSPECIFICDATA_KEY_OPESTARTCNT = "OpeStartCnt";


    public static final String SP_EQPATTR_UPDATE_ACTION_INCREASE = "Increase";
    public static final String SP_EQPATTR_UPDATE_ACTION_DECREASE = "Decrease";

    //--------------------------------------------------------------------------------------
    //   PostProcessPattern selection with eqp
    //--------------------------------------------------------------------------------------
    public static final String SP_POSTPROCESS_SEARCH_KEY_EQUIPMENTID = "EquipmentID";
    public static final String SP_POSTPROCESS_SEARCH_KEY_EQUIPMENTTYPE = "EquipmentType";

    public static final String SP_THREADSPECIFICDATA_KEY_POSTPROCFORLOTFLAG = "PostProcForLotFlag";
    public static final String SP_CONFIGURATIONCATEGORY_EQUIPMENTPOSTPROCDECOUPLEMODE = "EquipmentPostProcDecoupleMode";

    //--------------------------------------------------------------------------------------
    //   Equipment parallel process optimization (P2)
    //--------------------------------------------------------------------------------------
    public static final String SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT = "MainObject";
    public static final String SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE = "LoadCassette";
    public static final String SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT = "InProcessingLot";
    public static final String SP_OBJECTLOCK_OBJECTTYPE_USERDEFINEDDATA = "UserDefinedData";

    public static final int SP_OBJECTLOCK_LOCKTYPE_WRITE = 0;

    public static final int SP_OBJECTLOCK_LOCKTYPE_READ = 1;

    public static final int SP_OBJECTLOCK_LOCKTYPE_COUNT = 2;
    public static final String SP_FUNCTIONCATEGORY_SORTERTXID = "SorterTxID";
    public static final String SP_FUNCTIONCATEGORY_OPESTARTWITHCJIDGENTXID = "OpeStartWithCJIDGenTxID";
    public static final String SP_FUNCTIONCATEGORY_OPESTARTFORIBWITHCJIDGENTXID = "OpeStartForIBWithCJIDGenTxID";

    public static final String SP_CLASSNAME_POSMATERIALLOCATION_BYCJ = "PosMaterialLocation_ByCJ";
    public static final String SP_CLASSNAME_POSMATERIALLOCATION_BYCASTID = "PosMaterialLocation_ByCastID";
    //--------------------------------------------------------------------------------------
    //   Add SP_ClassName_PosMaterialLocation_EmptyML
    //--------------------------------------------------------------------------------------
    public static final String SP_CLASSNAME_POSMATERIALLOCATION_EMPTYML = "PosMaterialLocation_EmptyML";

    //--------------------------------------------------------------------------------------
    //   [MainDI] Support MainPD Dependency Injection function
    //--------------------------------------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__130 = 13000;
    public static final String SP_MAINDI_REWORKROUTE = "SP_MAINDI_REWORKROUTE";
    public static final String SP_MAINDI_DEFAULTTIMERESTRICTION = "SP_MAINDI_DEFAULTTIMERESTRICTION";
    public static final String SP_MAINDI_SUBROUTE = "SP_MAINDI_SUBROUTE";
    public static final String SP_MAINDI_DELTADCDEFINITION = "SP_MAINDI_DELTADCDEFINITION";
    public static final String SP_MAINDI_DEFAULTDELTADCSPECIFICATION = "SP_MAINDI_DEFAULTDELTADCSPECIFICATION";
    public static final String SP_MAINDI_CORRESPONDINGPROCESSOPERATIONNUMBER = "SP_MAINDI_CORRESPONDINGPROCESSOPERATIONNUMBER";
    public static final String SP_MAINDI_CORRESPONDINGOPERATIONS = "SP_MAINDI_CORRESPONDINGOPERATIONS";
    public static final String SP_MAINDI_RESPONSIBLEPROCESSOPERATIONNUMBERS = "SP_MAINDI_RESPONSIBLEPROCESSOPERATIONNUMBERS";
    public static final String SP_MAINDI_DEFAULTMAXREWORKCOUNT = "SP_MAINDI_DEFAULTMAXREWORKCOUNT";
    public static final String SP_MAINDI_DEFAULTMAXPROCESSCOUNT = "SP_MAINDI_DEFAULTMAXPROCESSCOUNT";
    public static final String SP_MAINDI_PROCESSLAGTIME = "SP_MAINDI_PROCESSLAGTIME";

    //--------------------------------------------------------------------------------------
    //   [MainDI] Add MainDI for Script
    //--------------------------------------------------------------------------------------
    public static final String SP_MAINDI_PRE1SCRIPT = "SP_MAINDI_PRE1SCRIPT";
    public static final String SP_MAINDI_PRE2SCRIPT = "SP_MAINDI_PRE2SCRIPT";
    public static final String SP_MAINDI_POSTSCRIPT = "SP_MAINDI_POSTSCRIPT";

    //--------------------------------------------------------------------------------------
    //   Post Process parallel execution
    //--------------------------------------------------------------------------------------
    public static final String SP_THREAD_TYPE_POSTPROCESS = "postproc";
    public static final String SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG = "PostProcParallelFlag";
    public static final String SP_OBJECTLOCK_OBJECTTYPE_OBJECT = "Object";
    public static final String SP_POSTPROCESS_ACTIONID_PARALLELEXECFINALIZE = "ParallelExecFinalize";
    public static final String SP_POSTPROCESS_PARALLELEXECUTION_ON = "1";
    public static final String SP_POSTPROCESS_PARALLELEXECUTION_OFF = "0";
    public static final String SP_POSTPROCESS_EXECRESULT_COMPLETED = "Completed";
    public static final String SP_POSTPROCESS_EXECRESULT_EXECUTING = "Executing";
    public static final String SP_POSTPROCESS_EXECRESULT_SKIPPED = "Skipped";
    public static final String SP_POSTPROCESS_EXECRESULT_ERROR = "Error";

    public static final int SP_POSTPROCESS_SYNCFLAG_ASYNC_SEQUENTIAL = 0;

    public static final int SP_POSTPROCESS_SYNCFLAG_ASYNC_PARALLEL = 2;

    public static final int SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL = 1;

    public static final int SP_POSTPROCESS_SYNCFLAG_SYNC_PARALLEL = 3;


    //--------------------------------------------------------------------------------------
    //   Two parameter sets are created for one lot object
    //--------------------------------------------------------------------------------------
    public static final String SP_CLASSNAME_POSPROPERTYSET = "PosPropertySet";

    //----------------------------------------------------------
    //   Remove part of the EI state limitation
    //----------------------------------------------------------

    public static final int SP_PRIVILEGECHECK_FOR_CJ_ON = 1;
    public static final int SP_DEFAULT_PRIVILEGECHECK_FOR_CJ = SP_PRIVILEGECHECK_FOR_CJ_ON;

    public static final int SP_PRIVILEGECHECK_FOR_CAST_ON = 1;
    public static final int SP_DEFAULT_PRIVILEGECHECK_FOR_CAST = SP_PRIVILEGECHECK_FOR_CAST_ON;


    //----------------------------------------------------------
    //   Auto Monitor enhancement
    //----------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__140 = 14000;
    public static final String SP_EQPMONITOR_SECTIONLABEL_MONITORBUILD = "Monitor Build";
    public static final String SP_EQPMONITOR_SECTIONLABEL_PREMEASUREMENT = "Pre Measurement";
    public static final String SP_EQPMONITOR_SECTIONLABEL_MONITOR = "Monitor";
    public static final String SP_EQPMONITOR_SECTIONLABEL_POSTMEASUREMENT = "Post Measurement";
    public static final String SP_EQPMONITOR_SECTIONLABEL_DEKIT = "De-kit";
    public static final String SP_POSEQPMONITOR_SEPARATOR_CHAR = ".";

    //----------------------------------------------------------
    //   Auto Monitor enhancement
    //----------------------------------------------------------
    public static final String SP_EQPMONITOR_ACTION_MAIL = "Mail";
    public static final String SP_EQPMONITOR_ACTION_INHIBIT = "Inhibit";
    public static final String SP_EQPMONITOR_STATUS_WAITING = "Waiting";
    public static final String SP_EQPMONITOR_STATUS_RUNNING = "Running";
    public static final String SP_EQPMONITOR_STATUS_DELETED = "Deleted";
    public static final String SP_EQPMONITOR_STATUS_WARNING = "Warning";
    public static final String SP_EQPMONITOR_STATUS_MONITOROVER = "MonitorOver";
    public static final String SP_EQPMONITORJOB_STATUS_CREATED = "Created";
    public static final String SP_EQPMONITORJOB_STATUS_REQUESTED = "Requested";
    public static final String SP_EQPMONITORJOB_STATUS_RESERVED = "Reserved";
    public static final String SP_EQPMONITORJOB_STATUS_READY = "Ready";
    public static final String SP_EQPMONITORJOB_STATUS_EXECUTING = "Executing";
    public static final String SP_EQPMONITORJOB_STATUS_PASSED = "Passed";
    public static final String SP_EQPMONITORJOB_STATUS_FAILED = "Failed";
    public static final String SP_EQPMONITORJOB_STATUS_ABORTED = "Aborted";
    public static final String SP_EQPMONITOR_LOTSTATUS_EXECUTING = "Executing";
    public static final String SP_EQPMONITOR_LOTSTATUS_RESERVED = "Reserved";
    public static final String SP_EQPMONITOR_RESULT_PASSED = "Passed";
    public static final String SP_EQPMONITOR_RESULT_FAILED = "Failed";
    public static final String SP_EQPMONITOR_RESULT_ABORTED = "Aborted";
    public static final String SP_EQPMONITOR_EVENT_FAILED = "Failed";
    public static final String SP_EQPMONITOR_EVENT_WARNING = "Warning";
    public static final String SP_EQPMONITOR_EVENT_MONITOROVER = "MonitorOver";
    public static final String SP_EQPMONITOR_SCHEDULE_NEXT = "Next";
    public static final String SP_EQPMONITOR_SCHEDULE_SKIP = "Skip";
    public static final String SP_EQPMONITOR_SCHEDULE_POSTPONE = "Postpone";
    public static final String SP_EQPMONITOR_SCHEDULE_FORCERUN = "ForceRun";
    public static final String SP_EQPMONITOR_SCHEDULE_TYPE_TIME = "Time";
    public static final String SP_EQPMONITOR_OPECATEGORY_CREATE = "Create";
    public static final String SP_EQPMONITOR_OPECATEGORY_UPDATE = "Update";
    public static final String SP_EQPMONITOR_OPECATEGORY_DELETE = "Delete";
    public static final String SP_EQPMONITOR_OPECATEGORY_STATUSCHANGE = "StatusChange";
    public static final String SP_EQPMONITOR_OPECATEGORY_RESET = "Reset";
    public static final String SP_EQPMONITOR_OPECATEGORY_SKIP = "Skip";
    public static final String SP_EQPMONITOR_OPECATEGORY_POSTPONE = "Postpone";
    public static final String SP_EQPMONITOR_OPECATEGORY_FORCERUN = "ForceRun";
    public static final String SP_EQPMONITOR_OPECATEGORY_WHATSNEXT = "WhatsNext";
    public static final String SP_EQPMONITOR_OPECATEGORY_EQPMONRESERVE = "EqpMonReserve";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_OPESTART = "OpeStart";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_OPESTARTCANCEL = "OpeStartCancel";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_GATEPASS = "PassThru";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_SPECCHECK = "SpecCheck";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_STATUSCHANGE = "StatusChange";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_EQPMONSTART = "AMJob-LotReserve";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP = "AMJob-LotRemove";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_LOTREMOVE = "LotRemove";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_EQPMONITORLOTFAILED = "EqpMonitorLotFailed";
    public static final String SP_EQPMONITOR_LEVEL_EQPMONKIT = "EqpMonKit";
    public static final String SP_EQPMONITOR_LEVEL_EQPMONNOKIT = "EqpMonNoKit";
    public static final String SP_EQPMONITOR_TYPE_MANUAL = "Manual";
    public static final String SP_EQPMONITOR_TYPE_ROUTINE = "Routine";
    public static final String SP_EQPMONITOR_OPELABEL_MONITOR = "Monitor";
    public static final String SP_EQPMONITOR_OPELABEL_POSTMEASUREMENT = "Post Measurement";
    public static final String SP_POSTPROCESS_ACTIONID_EQPMONITOREVAL = "AutoMonitorEval";
    public static final String SP_POSTPROCESS_ACTIONID_EQPMONITORJOBLOTREMOVE = "AutoMonitorJobLotRemove";
    public static final String SP_EQPMONITOROPERATIONKEY_SEPARATOR_CHAR = ".";
    public static final String SP_SYSTEMMSGCODE_EMONSERR = "AMONSERR";

    //----------------------------------------------------------
    //   Add for post process on chained mode
    //----------------------------------------------------------
    public static final String SP_POSTPROC_DKEY_CHAINED = "PP_DKEY_CHAINED";
    public static final String TX_ID_P_BRANCH = "P_BRANCH";
    public static final String TX_ID_P_LOCATE = "P_LOCATE";
    public static final String SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY = "TriggerDKey";
    public static final String SP_THREADSPECIFICDATA_KEY_CHAINEXECCNT = "ChainExecCnt";
    public static final String SP_POSTPROCESS_SEARCH_KEY_TRIGGERTXID = "TriggerTXID";
    public static final String SP_THREADSPECIFICDATA_POSTPROC_CHAINEDFLAG = "ChainedFlag";



    //--------------------------------------------------------------------------------------
    //   Add for Auto Monitor Wafer Used Count Update
    //--------------------------------------------------------------------------------------
    public static final String SP_POSTPROCESS_ACTIONID_EQPMONITORUSEDCOUNTUP = "AutoMonitorUsedCountUp";
    public static final String SP_EQPMONUSEDCNT_ACTION_UPDATE = "Update";
    public static final String SP_EQPMONUSEDCNT_ACTION_INCREMENT = "Increment";
    public static final String SP_EQPMONUSEDCNT_ACTION_RESET = "Reset";
    public static final String SP_OPERATIONCATEGORY_EQPMONUSEDCNTUPDATE = "EqpMonUsedCntUpdate";

    //--------------------------------------------------------------------------------------
    //   Add for WIP Lot Reset Function
    //--------------------------------------------------------------------------------------
    public static final String SP_OPERATIONCATEGORY_WIPLOTRESET = "WIPLotReset";

    //--------------------------------------------------------------------------------------
    //   Add for Dynamic Buffer Resource Type Change
    //--------------------------------------------------------------------------------------
    public static final String SP_BUFFERCATEGORY_EMPTYCASSETTE = "Empty Cassette";
    public static final String SP_BUFFERCATEGORY_FILLERDUMMY = "Filler Dummy Lot";
    public static final String SP_BUFFERCATEGORY_PROCESSLOT = "Process Lot";
    public static final String SP_BUFFERCATEGORY_PROCESSMONITORLOT = "Process Monitor Lot";
    public static final String SP_BUFFERCATEGORY_SIDEDUMMYLOT = "Side Dummy Lot";
    public static final String SP_BUFFERCATEGORY_WAITINGMONITORLOT = "Waiting Monitor Lot";
    public static final String SP_BUFFERCATEGORY_ANYPROCESSLOT = "Any Process Lot";

    //--------------------------------------------------------------------------------------
    //   Add for Durable Process Flow Control
    //--------------------------------------------------------------------------------------
    public static final String SP_DURABLE_PRODUCTIONSTATE_INPRODUCTION = "INPRODUCTION";
    public static final String SP_DURABLE_PRODUCTIONSTATE_INREWORK = "INREWORK";
    public static final String SP_DURABLE_HOLDSTATE_ONHOLD = "ONHOLD";
    public static final String SP_DURABLE_HOLDSTATE_NOTONHOLD = "NOTONHOLD";
    public static final String SP_DURABLE_FINISHEDSTATE_COMPLETED = "COMPLETED";
    public static final String SP_DURABLE_PROCSTATE_WAITING = "Waiting";
    public static final String SP_DURABLE_PROCSTATE_PROCESSING = "Processing";
    public static final String SP_DURABLE_PROCSTATE_PROCESSED = "Processed";
    public static final String SP_DURABLE_INVENTORYSTATE_ONFLOOR = "OnFloor";
    public static final String SP_DURABLE_INVENTORYSTATE_INBANK = "InBank";
    public static final String SP_WHATSNEXTRULE_PREFIX_FOR_DURABLE = "DURABLE-";
    public static final String SP_WHATSNEXTRULE_DEFAULT_DURABLE = "DURABLE-FIFO";
    public static final String SP_DURABLE_SEPARATOR_CHAR = ".";
    public static final String SP_REASON_DURABLELOCK = "LOCK";
    public static final String SP_REASON_DURABLELOCKRELEASE = "LOCR";
    public static final String SP_OPERATION_LOCATE = "OpeLocate";
    public static final String SP_OPERATION_BRSCRIPT = "BRScript";
    public static final String SP_POSTPROCESS_ACTIONID_DSCRIPT = "DScript";
    public static final String SP_POSTPROCESS_ACTIONID_DAUTOBANKIN = "DAutoBankIn";
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_DURABLECARRIER = "Durable Carrier";
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_DURABLERETICLE = "Durable Reticle";
    public static final String SP_MC_SPECIALEQUIPMENTCONTROL_DURABLERETICLEPOD = "Durable Reticle Pod";
    public static final String SP_DEFAULTDURABLEDISPATCHRULE = "DURABLE-FIFO";
    public static final String SP_SCRIPTPARM_CLASS_CARRIER = "Carrier";
    public static final String SP_SCRIPTPARM_CLASS_RETICLEPOD = "ReticlePod";
    //--------------------------------------------------------------------------------------
    //  add start
    //--------------------------------------------------------------------------------------
    public static final String SP_MAINPROCESSDEFINITION_CONDITION_ROUTEID = "routeID";
    public static final String SP_MAINPROCESSDEFINITION_CONDITION_ROUTETYPE = "routeType";
    public static final String SP_MAINPROCESSDEFINITION_CONDITION_PROCDEFTYPE = "procDefType";
    public static final String SP_MAINPROCESSDEFINITION_CONDITION_ACTIVESHOWFLAG = "activeShowFlag";
    public static final String SP_PROCESSDEFINITION_CONDITION_OPERATIONID = "operationID";
    public static final String SP_PROCESSDEFINITION_CONDITION_PDTYPE = "pdType";
    //  add end


    //--------------------------------------------------------------------------------------
    //   Add for ObjectID List Inquiry supports some additional classes
    //--------------------------------------------------------------------------------------
    public static final String SP_STORAGEMACHINE_CONDITION_STOCKERTYPE = "StockerType";
    public static final String SP_STORAGEMACHINE_CONDITIONVALUE_OTHER = "Other";

    //--------------------------------------------------------------------------------------
    //   Add for Claim Memo Improvement
    //--------------------------------------------------------------------------------------
    public static final String SP_NOTETYPE_EQPNOTE = "EqpNote";
    public static final String SP_NOTETYPE_LOTOPENOTE = "LotOpeNote";

    //--------------------------------------------------------------------------------------
    //   Add for Future Actions enhancement
    //--------------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------------
    //   Add for (MainPD,ModulePD)[SetInNotebook] Performance Improvement by MM-FW
    //--------------------------------------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__160 = 16000;


    //--------------------------------------------------------------------------------------
    //   Search ModulePD/Recipe by condition
    //--------------------------------------------------------------------------------------
    public static final String SP_MODULEPROCESSDEFINITION_CONDITION_ACTIVESHOWFLAG = "activeShowFlag";
    public static final String SP_MACHINERECIPE_CONDITION_EQUIPMENTID = "EquipmentID";

    //--------------------------------------------------------------------------------------
    // Add for Durable Sub Status Control
    //--------------------------------------------------------------------------------------
    public static final int SP_SIVIEW_RELEASE_NUMBER__170 = 17000;
    public static final String SP_DURABLESUBSTATEOBJECTFACTORY_MARKER = "";
    public static final String SP_POSDURABLESUBSTATE_SEPARATOR_CHAR = ".";
    public static final String SP_DURABLE_ONROUTESTATE_ACTIVE = "ACTIVE";
    public static final String SP_DURABLE_ONROUTESTATE_FINISHED = "FINISHED";
    public static final String SP_DURABLE_ONROUTESTATE_INACTIVE = "INACTIVE";
    public static final String SP_REASON_DURABLEPROCESSLAGTIMEHOLD = "DLTH";
    public static final String SP_REASON_DURABLEPROCESSLAGTIMEHOLDRELEASE = "DLTR";
    public static final String SP_POSTPROCESS_ACTIONID_DPROCESSLAGTIME = "DProcessLagTime";
    public static final String SP_LOT_SAMPLING_ACTION = "LotSampling";
    public static final String SP_NPW_LOT_AUTO_SKIP = "NPWLotSkip";

    //--------------------------------------------------------------------------------------
    // Add for Enhance DCItem object model for better flexibility
    //--------------------------------------------------------------------------------------
    public static final String SP_LRCP_SSET_PRST_TABLEMARKER = "PosLogicalRecipe_MachineSpecificRecipeSettings_processResourceStates";
    public static final String SP_DCITEM_SEARCHKEYPATTERN_DCDEF = "DCDefID";
    public static final String SP_DCITEM_SEARCHKEYPATTERN_DCSPEC = "DCSpecID";
    public static final String SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO = "LotID+RouteID+OpeNo";
    public static final String SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO = "ProductID+RouteID+OpeNo";
    public static final String SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO = "ProductGroupID+RouteID+OpeNo";
    public static final String SP_DCITEM_SEARCHKEYPATTERN_TECH_ROUTE_OPENO = "TechnologyID+RouteID+OpeNo";
    public static final String SP_DCITEM_SEARCHKEYPATTERN_ROUTE_OPENO = "RouteID+OpeNo";
    public static final String SP_DCITEM_SEARCHKEY_DATAITEMNAME = "DataItemName";
    public static final String SP_DCITEM_SEARCHKEY_DCSPECGROUPID = "DCSpecGroupID";
    public static final String SP_DCITEM_SEARCHKEY_DCDEFID = "DCDefID";
    public static final String SP_DCITEM_SEARCHKEY_DCSPECID = "DCSpecID";
    public static final String SP_DCITEM_SEARCHKEY_LOTID = "LotID";
    public static final String SP_DCITEM_SEARCHKEY_ROUTEID = "RouteID";
    public static final String SP_DCITEM_SEARCHKEY_OPENO = "OpeNo";
    public static final String SP_DCITEM_SEARCHKEY_PRODUCTID = "ProductID";
    public static final String SP_DCITEM_SEARCHKEY_PRODUCTGROUPID = "ProductGroupID";
    public static final String SP_DCITEM_SEARCHKEY_TECHNOLOGYID = "TechnologyID";


    //--------------------------------------------------------------------------------------
    // Post process error information save
    //--------------------------------------------------------------------------------------
    public static final String SP_POSTPROCESSADDITIONALINFO_ERRORCODE = "ErrorCode";
    public static final String SP_POSTPROCESSADDITIONALINFO_ERRORMSG = "ErrorMsg";
    public static final String SP_POSTPROCESSACTIONINFO_ADDUPDATEADDITIONALINFO = "AddUpdateAdditionalInfo";

    //--------------------------------------------------------------------------------------
    // Clear dispatch carrier for equipment when it was changed mode
    //--------------------------------------------------------------------------------------
    public static final String SP_DISPATCH_CAST_CLEARED_BY_CHANGE_TO_ONLINE = "SP_DISPATCH_CAST_CLEARED_BY_CHANGE_TO_ONLINE";

    //--------------------------------------------------------------------------------------
    // Add for Transfer Job Status History
    //--------------------------------------------------------------------------------------
    public static final String SP_DURABLEXFERJOBSTATUS_CREATED = "Created";
    public static final String SP_DURABLEXFERJOBSTATUS_COMPLETED = "Completed";
    public static final String SP_DURABLEXFERJOBSTATUS_UPDATED = "Updated";
    public static final String SP_DURABLEXFERJOBSTATUS_CANCELLED = "Cancelled";


    //--------------------------------------------------------------------------------------
    // DSN000104683 Add for Scheduler function
    //--------------------------------------------------------------------------------------
    public static final String SP_HASHKEY_SUBLOTIDHEADER = "SubLotIDHeader";
    public static final String SP_HASHKEY_STARTNO = "StartNo";
    public static final String SP_PROPERTYSET_INTEGER = "PosPropertyInteger";
    public static final String SP_PROPERTYSET_REAL = "PosPropertyReal";
    public static final String SP_PROPERTYSET_STRING = "PosPropertyString";
    public static final String SP_PROPERTYSET_TABLESI = "PosPropertyTableSI";
    public static final String SP_PROPERTYSET_TABLESR = "PosPropertyTableSR";
    public static final String SP_PROPERTYSET_TABLESS = "PosPropertyTableSS";
    public static final String SAMPLING_SEPARATOR = ",";

    public static final String SP_SEARCH_COUNT_MAX = "9999";


    public static final String SP_EQPSTATECONVERTTYPE_BYSTARTLOT = "STLT";
    public static final String SP_EQPSTATECONVERTTYPE_BYSTARTSUBLOT = "STSL";
    public static final String SP_EQPSTATECONVERTTYPE_BYSTARTROUTE = "STRT";
    public static final String SP_EQPSTATECONVERTTYPE_BYINPROCESSLOT = "IPLT";
    public static final String SP_EQPSTATECONVERTTYPE_BYINPROCESSSUBLOT = "IPSL";
    public static final String SP_EQPSTATECONVERTTYPE_BYINPROCESSROUTE = "IPRT";

    public static final int DEFAULTSENTMESSAGECHECKTIME = 0;
    public static final int DEFAULTMAXSENTCOUNT = 1;

    public static final String SEASON_TYPE_IDLE = "IDLE";

    public static final String SEASON_TYPE_RECIPEIDLE = "RECIPE IDLE";

    public static final String SEASON_TYPE_INTERVAL = "INTERVAL";

    public static final String SEASON_TYPE_PM = "PM";

    public static final String SEASON_TYPE_RECIPEGROUP = "RECIPE GROUP";

    public static final String SEASON_STATUS_ACTIVE = "ACTIVE";

    public static final String SEASON_STATUS_INACTIVE = "INACTIVE";

    public static final String SEASONJOB_STATUS_REQUESTED = "REQUESTED";

    public static final String SEASONJOB_STATUS_RESERVED = "RESERVED";

    public static final String SEASONJOB_STATUS_EXECUTING = "EXECUTING";

    public static final String SEASONJOB_STATUS_COMPLETED = "COMPLETED";

    public static final String SEASONJOB_STATUS_ABORTED = "ABORTED";

    public static final String SEASON_LOT_TYPE = "Season";

    public static final String SEASON_EQP_STAT = "SESN";  // SBY.SESN

    public static final String SEASONPLAN_ACTION_CREATE = "create";

    public static final String SEASONPLAN_ACTION_MODIFY = "modify";

    public static final String SEASONPLAN_ACTION_DELETE = "delete";

    public static final String SEASONJOB_ACTION_CREATE = "create";

    public static final String SEASONJOB_ACTION_MODIFY = "modify";

    public static final String SEASONJOB_ACTION_DELETE = "delete";

    public static final String SEASONJOB_ACTION_ABORTED = "aborted";

    public static final String SEASON_SEPARATOR = ",";

    public static final String DURABLE_SUBSTATUS_DIRTY = "Dirty";

    public static final String DURABLE_SUBSTATUS_NORMALUSE = "NORMAL_USE";

    public static final String DURABLE_SUBSTATUS_CLEAN = "Clean";


    //runCard state
    public static final String RUNCARD_DRAFT = "Draft";
    public static final String RUNCARD_ONGOING = "OnGoing";
    public static final String RUNCARD_ACTIVE = "Active";
    public static final String RUNCARD_CANCEL = "Cancel";
    public static final String RUNCARD_MODIFY = "Modify";
    public static final String RUNCARD_RUNNING = "Running";
    public static final String RUNCARD_DONE = "Done";
    //runCard approval instruction
    public static final String RUNCARD_INSTRUCTTION_SUBMIT = "Submit";
    public static final String RUNCARD_INSTRUCTTION_APPROVE = "Approve";
    public static final String RUNCARD_INSTRUCTTION_REJECT = "Reject";
    public static final String RUNCARD_INSTRUCTTION_ABORT = "Abort";
    public static final String RUNCARD_INSTRUCTTION_MODIFY = "Modify";
    public static final String RUNCARD_INSTRUCTTION_EXECUTE = "Execute";
    public static final String RUNCARD_INSTRUCTTION_COMPLETE = "Complete";
    //runCard approval group
    public static final String RUNCARD_APPROVAL_USER_GROUP = "RCAppror";

    //runCard event action
    public static final String RUNCARD_ACTION_CREATE = "CREATE";
    public static final String RUNCARD_ACTION_SUBMIT = "SUBMIT";
    public static final String RUNCARD_ACTION_APPROVE = "APPROVE";
    public static final String RUNCARD_ACTION_REJECT = "REJECT";
    public static final String RUNCARD_ACTION_ABORT = "ABORT";
    public static final String RUNCARD_ACTION_MODIFY = "MODIFY";
    public static final String RUNCARD_ACTION_EXECUTE = "EXECUTE";
    public static final String RUNCARD_ACTION_COMPLETE = "COMPLETE";
    public static final String RUNCARD_ACTION_DELETE = "DELETE";

    // AMS System category
    public static final String CATEGORY_HOLD_LOT = "HoldLot";
    public static final String CATEGORY_CONSTRAINT = "Constraint";
    public static final String CATEGORY_EQP_ALARM = "EQPAlarm";
    public static final String CATEGORY_SYSTEM_MESSAGE = "SystemMessage";

    //new constraint
    public static final String RECIPE_TIME_LOCK_REASON = "RTIO";
    public static final String MFG_REASON = "MfgRestrict";
    public static final String RECIPE_USE_MOVE_IN = "MoveIn";
    public static final String RECIPE_USE_MOVE_OUT = "MoveOut";
    public static final String SOITEC_MFG_ENDTIME = "2099-12-31 00:00:00";
    public static final String SP_MFGSTATE_ACTIVE = "Active";
    public static final String SP_MFGSTATE_INACTIVE = "Inactive";

    //RTMS constatnts
    public static final String RTMS_RETICLE_CHECK_ACTION_MOVE_IN = "Move-In";
    public static final String RTMS_RETICLE_CHECK_ACTION_MOVE_OUT = "Move-Out";
    public static final String RTMS_RETICLE_CHECK_ACTION_MOVE_IN_RESERVE = "Move-In-Reserve";
    public static final String RTMS_RETICLE_CHECK_ACTION_MOVE_IN_CANCEL = "Move-In-Cancel";
    public static final String RTMS_RETICLE_CHECK_ACTION_MOVE_IN_RESERVE_CANCEL = "Move-In-Reserve-Cancel";
    // CIM4.0 Factory
    public static final String CIM_FACTORY = "CIMFactory";


    //---------------------------------------------------------
    // For Durable Job Status Change Event
    //---------------------------------------------------------
    public static final String SP_DURABLEEVENT_ACTION_JOBSTATECHANGE_OMS = "JOBSTATCHG";
    public static final String SP_DURABLEEVENT_ACTION_JOBSTATECHANGE_EAP = "EAPREPORT";

    //---------------------------------------------------------
    // ARHS
    //---------------------------------------------------------
    public static final String SP_DISPATCH_STATIONID_ALL = "ALL";
    public static final String SP_RCJ_ERROR_TYPE_STOREREQ = "StoreReq";
    public static final String SP_RCJ_ERROR_TYPE_RETRIEVEREQ = "RetrieveReq";
    public static final String SP_RCJ_ERROR_TYPE_UNCLAMPREQ = "UnclampReq";
    public static final String SP_RCJ_ERROR_TYPE_XFERREQ = "XferReq";
    public static final String SP_RCJ_ERROR_TYPE_STORERPT = "StoreRpt";
    public static final String SP_RCJ_ERROR_TYPE_RETRIEVERPT = "RetrieveRpt";
    public static final String SP_RCJ_ERROR_TYPE_UNCLAMPRPT = "UnclampRpt";
    public static final String SP_RCJ_ERROR_TYPE_XFERRPT = "XferRpt";
    public static final String SP_TRANSFERJOB_INQ_TYPE_C = "C";
    public static final String SP_TRANSFERJOB_INQ_TYPE_T = "T";
    public static final String SP_TRANSFERJOB_INQ_TYPE_F = "F";

    //---------------------------------------------------------
    // CONTAMINATION
    //---------------------------------------------------------
    public static final String EQP_PR_CONTROL_AVOID = "AvoidPR";
    public static final String EQP_PR_CONTROL_IGNORE = "IgnorePR";
    public static final String EQP_PR_ACTION_SET = "SetPR";
    public static final String EQP_PR_ACTION_REMOVE = "RemovePR";
    public static final String LOT_CONTAMINATION_NO_CHECK = "NoCheck";

    //---------------------------------------------------------
    // Constraint
    //---------------------------------------------------------
    public static final String FUNCTION_RULE_BLIST = "BLIST";
    public static final String FUNCTION_RULE_WLIST = "WLIST";

    //---------------------------------------------------------
    // Sampling check type
    //---------------------------------------------------------
    public static final String LS_CHILD_LOT_EXECUTE = "CHILD_LOT_EXECUTE";
    public static final String LS_BASIC_LOT_EXECUTE = "BASIC_LOT_EXECUTE";

    //---------------------------------------------------------
    // eqp port base or top
    //---------------------------------------------------------
    public static final String WAFER_BONDING_BASE_LOAD_PORT = "Bottom";
    public static final String WAFER_BONDING_TOP_LOAD_PORT = "Top";

    // lot qtime warn
    public static final String LOT_QTIME_WARN_NORMAL = "Normal";
    public static final String LOT_QTIME_WARN_HURRY = "Hurry";
    public static final String LOT_QTIME_WARN_OVERTIME = "OverTime";

    // eqp area
    public static final String EQP_WORK_ZONE_CATEGORY = "WorkZone";
    public static final String EQP_WORK_USER_CATEGORY = "User";

    // ocap relation
    //---------------------------------------------------------
    public static final String OCAP_ADD_MEASURE = "ADD_MEASURE";
    public static final String OCAP_RE_MEASURE = "RE_MEASURE";
    /**
     * OHLC: spc first trigger ocap defaul hold
     * ORLR: ocap Lot Re-Measurement Need Hold Lot
     * OAMH: ocap lot Add-Measurement Need Hold Lot
     * ORLC: ocap release above ocap relation onHold
     */
    public static final String OCAP_HOLD_LOT = "OHLC";
    public static final String OCAP_RE_MEASURE_HOLD_LOT = "ORMH";
    public static final String OCAP_ADD_MEASURE_HOLD_LOT = "OAMH";
    public static final String OCAP_HOLD_LOT_RELEASE = "ORLC";
    public static final String OCAP_TYPE_KEY = "status";
    public static final String OCAP_COMPLETE = "COMPLETED";

    //constraint type
    public static final String CONSTRAINT_TYPE_EQP = "Equipment Related";
    public static final String CONSTRAINT_TYPE_PRODUCT = "Product";
    public static final String CONSTRAINT_TYPE_PF = "Main PF";
    public static final String CONSTRAINT_TYPE_ROUTE = "Route";
    public static final String CONSTRAINT_TYPE_STEP = "Step";
    public static final String CONSTRAINT_TYPE_PFOPE = "Main PF Operation";
    public static final String CONSTRAINT_TYPE_PFOPE_PRODUCT = "Main PF Operation and Product";
    public static final String CONSTRAINT_TYPE_RECIPE = "Recipe";
    public static final String CONSTRAINT_TYPE_RETICLE = "Reticle";
    public static final String CONSTRAINT_TYPE_RETICLEGRP = "Reticle Group";
    // tool constraint switch,default on
    public static final String CONSTRAINT_ONETIME_EXCEPTION_SWITCH_ON = "1";
    public static final String TOOL_CONSTRAINT_REQ = "R";
    public static final String TOOL_CONSTRAINT_DELETE = "D";
    public static final String TOOL_CONSTRAINT_CANCEL = "C";
    public static final String TOOL_CONSTRAINT_MODIFY = "M";
    public static final String TOOL_CONSTRAINT_EDCFail = "U";

    //contamination mode
    public static final String CONTAMINATION_MODE_QIANDAO = "QIANDAO";

    // EDC Plan setting
    public static final String EDC_PLAN_SETTING_TYPE_SPECIFIC = "Specific";
    public static final String EDC_PLAN_SETTING_TYPE_GENERAL = "General";

    public static final String OM_QTIME_TYPE_MAX = "Max QTime";
    public static final String OM_QTIME_TYPE_MIN = "Min QTime";
    public static final String DURABLE_ON_ROUTE = "BankID can not be specified on BankIn operation, " +
            "When durable is on route.";

    //---------------------------//
    //      sampling service     //
    //---------------------------//
    public static final String SAMPLING_WAFER_LOGIC_ERROR_CODE_PREFIX = "2";

    //Integration Error Code
    public static final String SPC_SUCCESS_CODE = "0000";
    public static final String SPC_NO_JOB_DEFINITION_CODE = "11401";
    public static final String SPC_JOB_IN_OCAP_FLOW_CODE = "2000";
    public static final String SPC_INVALID_INPUT_PARAM_ERROR_CODE = "1000";

    //------------------------------//
    // layout recipe control        //
    //------------------------------//
    public static final String LAYOUT_RECIPE_SPECIFIC_CONTROL_SEQUENCE = "Designated Lot Processing Sequence";
    public static final String LAYOUT_RECIPE_SPECIFIC_CONTROL_POSITION = "Designated Lot Processing Position";

    public static boolean equalsIgnoreCase(@NonNull String bizConstant, String value) {
        return bizConstant.equalsIgnoreCase(value);
    }
}
