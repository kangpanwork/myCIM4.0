package com.fa.cim.Constant;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 13:48:11
 */
public class SPConstant {

    public static final String SP_MOVEMENTTYPE_NONMOVE                     ="NonMove" ;
    public static final String SP_MOVEMENTTYPE_STB                         ="LotStart";
    public static final String SP_MOVEMENTTYPE_STBCANCEL                   ="LotStartCancel";
    public static final String SP_MOVEMENTTYPE_START                       ="Start"  ;

    public static final String SP_OPERATIONCATEGORY_ADJUSTCHIP             ="AdjustChip";
    public static final String SP_OPERATIONCATEGORY_BANKHOLD               ="HoldLotInBank";
    public static final String SP_OPERATIONCATEGORY_BANKHOLDRELEASE        ="ReleaseHoldLotInBank";
    public static final String SP_OPERATIONCATEGORY_BANKIN                 ="MoveToBank";
    public static final String SP_OPERATIONCATEGORY_BANKINCANCEL           ="CancelMoveToBank" ;
    public static final String SP_OPERATIONCATEGORY_CASSETTECHANGE         ="CarrierChange";
    public static final String SP_OPERATIONCATEGORY_CHIPSCRAP              ="ChipScrap";
    public static final String SP_OPERATIONCATEGORY_CHIPSCRAPCANCEL        ="ChipScrapCancel";
    public static final String SP_OPERATIONCATEGORY_CORRELATIONUSE         ="CorrelationUse";
    public static final String SP_OPERATIONCATEGORY_CORRELATIONUSECANCEL   ="CorrelationUseCancel";
    public static final String SP_OPERATIONCATEGORY_DEFECTDIE              ="DefectDie";
    public static final String SP_OPERATIONCATEGORY_DIEADJUST              ="ReviseDieCount";
    public static final String SP_OPERATIONCATEGORY_GATEPASS               ="PassThru";
    public static final String SP_OPERATIONCATEGORY_LOCATEBACKWARD         ="SkipBack";
    public static final String SP_OPERATIONCATEGORY_LOCATEFORWARD          ="SkipFront";
    public static final String SP_OPERATIONCATEGORY_LOTHOLD                ="HoldLot";
    public static final String SP_OPERATIONCATEGORY_LOTHOLDRELEASE         ="ReleaseHoldLot";
    public static final String SP_OPERATIONCATEGORY_MERGE                  ="MergeLot";
    public static final String SP_OPERATIONCATEGORY_MONITORIN              ="MonitorIn";
    public static final String SP_OPERATIONCATEGORY_MONITOROUT             ="MonitorOut";
    public static final String SP_OPERATIONCATEGORY_MOVEBANK               ="ChangeBank";
    public static final String SP_OPERATIONCATEGORY_NONPRODBANKIN          ="MoveToNonProdBank";
    public static final String SP_OPERATIONCATEGORY_NONPRODBANKOUT         ="MoveOutNonProdBank";
    public static final String SP_OPERATIONCATEGORY_MOVETOSPLIT            ="MoveSplitLotToBranch";
    public static final String SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL      ="CancelMoveSplitLotToBranch";
    public static final String SP_OPERATIONCATEGORY_OPERATIONCOMPLETE      ="MoveOut";
    public static final String SP_OPERATIONCATEGORY_OPERATIONSTART         ="MoveIn";
    public static final String SP_OPERATIONCATEGORY_OPERATIONSTARTCANCEL   ="MoveInCancel";
    public static final String SP_OPERATIONCATEGORY_ORDERCHANGE            ="ModifyPO";
    public static final String SP_OPERATIONCATEGORY_PILOTSPLIT             ="PilotSplit"       ;
    public static final String SP_OPERATIONCATEGORY_PILOTMERGE             ="PilotMerge"       ;
    public static final String SP_OPERATIONCATEGORY_PROCESSSTART           ="ProcessStart"     ;
    public static final String SP_OPERATIONCATEGORY_PROCESSEND             ="ProcessEnd"       ;
    public static final String SP_OPERATIONCATEGORY_RELEASE                ="Release";
    public static final String SP_OPERATIONCATEGORY_RELEASECANCEL          ="ReleaseCancel";
    public static final String SP_OPERATIONCATEGORY_UPDATE                 ="Update"      ;
    public static final String SP_OPERATIONCATEGORY_REQUEUE                ="RefreshLotQ"     ;
    public static final String SP_OPERATIONCATEGORY_REWORK                 ="Rework";
    public static final String SP_OPERATIONCATEGORY_REWORKCANCEL           ="CancelRework";
    public static final String SP_OPERATIONCATEGORY_SCHEDULECHANGE         ="ModifyLotPlan";
    public static final String SP_OPERATIONCATEGORY_SHIP                   ="Ship";
    public static final String SP_OPERATIONCATEGORY_SHIPCANCEL             ="CancelShip";
    public static final String SP_OPERATIONCATEGORY_SLMCASSETTECHANGE      ="SLMCassetteChange";
    public static final String SP_OPERATIONCATEGORY_SPLIT                  ="Split";
    public static final String SP_OPERATIONCATEGORY_STB                    ="LotStart";
    public static final String SP_OPERATIONCATEGORY_DURABLESTART           ="DurableStart";
    public static final String SP_OPERATIONCATEGORY_STBUSED                ="UsedForLotStart";
    public static final String SP_OPERATIONCATEGORY_VENDORLOTRECEIVE       ="MaterialReceive";
    public static final String SP_OPERATIONCATEGORY_VENDORLOTRETURN        ="MaterialReturn";
    public static final String SP_OPERATIONCATEGORY_WAFERSCRAP             ="ScrapWafer";
    public static final String SP_OPERATIONCATEGORY_WAFERSCRAPCANCEL       ="CancelScrapWafer";
    public static final String SP_OPERATIONCATEGORY_WAFERSORT              ="WaferSort";
    public static final String SP_OPERATIONCATEGORY_BRANCH                 ="Branch"      ;
    public static final String SP_OPERATIONCATEGORY_BRANCHCANCEL           ="CancelBranch";
    public static final String SP_OPERATIONCATEGORY_RUNNINGHOLD            ="RunningHold" ;
    public static final String SP_OPERATIONCATEGORY_FORCECOMP              ="ForceMoveOut"   ;
    public static final String SP_OPERATIONCATEGORY_STBCANCEL              ="LotStartCancel"   ;
    public static final String SP_OPERATIONCATEGORY_DURABLESTARTCANCEL     ="DurableStartCancel"   ;
    public static final String SP_OPERATIONCATEGORY_STBCANCELLED           ="LotStartCancelled";
    public static final String SP_OPERATIONCATEGORY_PREPARECANCEL          ="PrepareCancel"      ;
    public static final String SP_OPERATIONCATEGORY_PREPARECANCELLED       ="PrepareCancelled"   ;
    public static final String SP_OPERATIONCATEGORY_OPECOMPPARTIAL         ="RunningSplit-MoveOut"     ;
    public static final String SP_OPERATIONCATEGORY_OPESTARTCANCELPARTIAL  ="RunningSplit-MoveInCancel";
    public static final String SP_OPERATIONCATEGORY_DURABLEHOLD            ="DurableHold";
    public static final String SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE     ="DurableHoldRelease";
    public static final String SP_OPERATIONCATEGORY_LOTTERMINATE = "LotTerminate";
    public static final String SP_OPERATIONCATEGORY_LOTTERMINATECANCEL = "LotTerminateCancel";

    public static final String SP_OPERATIONCATEGORY_VENDORLOTPREPARATION    = "MaterialPrepare";
    public static final String SP_OPERATIONCATEGORY_VENDORWAFEROUT          = "MaterialOut";

    public static final String SP_OPERATIONCATEGORY_WIPLOTRESET                 = "WIPLotReset";

    public static final String SP_RPARM_CHANGETYPE_BYLOT                      = "RecipeParmChangeByLot";
    public static final String SP_RPARM_CHANGETYPE_BYWAFER                    = "RecipeParmChangeByWafer";

    public static final String SP_HOLDLIST_HOLDMARK                        = "Hold";
    public static final String SP_HOLDLIST_RELEASEMARK                     = "Release";
    public static final String SP_MODETRANSITION_ALLOWABLE                 = "ModeChange OK";
    public static final String SP_MODETRANSITION_ALLOWABLEWITHCONDITION    = "ModeChange OK WithCondition";
    public static final String SP_MODETRANSITION_NOTALLOWED                = "ModeChange NG";
    public static final String SP_MOVEMENTTYPE_END                         = "End";
    public static final String SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION       = "BackwardOperation";
    public static final String SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE           = "BackwardStage";
    public static final String SP_MOVEMENTTYPE_MOVEFORWARDOPERATION        = "ForwardOperation";
    public static final String SP_MOVEMENTTYPE_MOVEFORWARDSTAGE            = "ForwardStage";
    public static final String SP_MOVEMENTTYPE_MOVENONPRODBANK             = "MoveNonProBank";
    public static final String SP_MOVEMENTTYPE_MOVEWIPAREA                 = "MoveWIPArea";

    public static final String SP_DCDEF_MEAS_PROCESSGROUP          = "ProcessGroup";
    public static final String SP_DCDEF_MEAS_CONTROLJOB            = "Control Job";
    public static final String SP_DCDEF_MEAS_LOT                   = "Lot";
    public static final String SP_DCDEF_MEAS_WAFER                 = "Wafer";
    public static final String SP_DCDEF_MEAS_SITE                  = "Site";
    public static final String SP_DCDEF_MEAS_PJ                    = "Process Job";
    public static final String SP_DCDEF_MEAS_PJWAFER               = "Proc Wafer" ;
    public static final String SP_DCDEF_MEAS_PJWAFERSITE           = "Proc Site"  ;

    public static final String DCDATA_D_THETABLEMARKER  = "PosProcessOperation_CollectedData_collectedData%d" ;
    public static final String DC_D_THETABLEMARKER      = "PosProcessOperation_AssignedDataCollections_dcItems%d";


    public static final String SP_EQPMONITOR_ACTION_MAIL                         = "Mail";
    public static final String SP_EQPMONITOR_ACTION_INHIBIT                      = "Inhibit";
    public static final String SP_EQPMONITOR_STATUS_WAITING                      = "Waiting";
    public static final String SP_EQPMONITOR_STATUS_RUNNING                      = "Running";
    public static final String SP_EQPMONITOR_STATUS_DELETED                      = "Deleted";
    public static final String SP_EQPMONITOR_STATUS_WARNING                      = "Warning";
    public static final String SP_EQPMONITOR_STATUS_MONITOROVER                  = "MonitorOver";
    public static final String SP_EQPMONITORJOB_STATUS_CREATED                   = "Created";
    public static final String SP_EQPMONITORJOB_STATUS_REQUESTED                 = "Requested";
    public static final String SP_EQPMONITORJOB_STATUS_RESERVED                  = "Reserved";
    public static final String SP_EQPMONITORJOB_STATUS_READY                     = "Ready";
    public static final String SP_EQPMONITORJOB_STATUS_EXECUTING                 = "Executing";
    public static final String SP_EQPMONITORJOB_STATUS_PASSED                    = "Passed";
    public static final String SP_EQPMONITORJOB_STATUS_FAILED                    = "Failed";
    public static final String SP_EQPMONITORJOB_STATUS_ABORTED                   = "Aborted";
    public static final String SP_EQPMONITOR_LOTSTATUS_EXECUTING                 = "Executing";
    public static final String SP_EQPMONITOR_LOTSTATUS_RESERVED                  = "Reserved";
    public static final String SP_EQPMONITOR_RESULT_PASSED                       = "Passed";
    public static final String SP_EQPMONITOR_RESULT_FAILED                       = "Failed";
    public static final String SP_EQPMONITOR_RESULT_ABORTED                      = "Aborted";
    public static final String SP_EQPMONITOR_EVENT_FAILED                        = "Failed";
    public static final String SP_EQPMONITOR_EVENT_WARNING                       = "Warning";
    public static final String SP_EQPMONITOR_EVENT_MONITOROVER                   = "MonitorOver";
    public static final String SP_EQPMONITOR_SCHEDULE_NEXT                       = "Next";
    public static final String SP_EQPMONITOR_SCHEDULE_SKIP                       = "Skip";
    public static final String SP_EQPMONITOR_SCHEDULE_POSTPONE                   = "Postpone";
    public static final String SP_EQPMONITOR_SCHEDULE_FORCERUN                   = "ForceRun";
    public static final String SP_EQPMONITOR_SCHEDULE_TYPE_TIME                  = "Time";
    public static final String SP_EQPMONITOR_OPECATEGORY_CREATE                  = "Create";
    public static final String SP_EQPMONITOR_OPECATEGORY_UPDATE                  = "Update";
    public static final String SP_EQPMONITOR_OPECATEGORY_DELETE                  = "Delete";
    public static final String SP_EQPMONITOR_OPECATEGORY_STATUSCHANGE            = "StatusChange";
    public static final String SP_EQPMONITOR_OPECATEGORY_RESET                   = "Reset";
    public static final String SP_EQPMONITOR_OPECATEGORY_SKIP                    = "Skip";
    public static final String SP_EQPMONITOR_OPECATEGORY_POSTPONE                = "Postpone";
    public static final String SP_EQPMONITOR_OPECATEGORY_FORCERUN                = "ForceRun";
    public static final String SP_EQPMONITOR_OPECATEGORY_WHATSNEXT               = "WhatsNext";
    public static final String SP_EQPMONITOR_OPECATEGORY_EQPMONRESERVE           = "EqpMonReserve";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_OPESTART             = "OpeStart";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_OPESTARTCANCEL       = "OpeStartCancel";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_GATEPASS             = "PassThru";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_SPECCHECK            = "SpecCheck";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_STATUSCHANGE         = "StatusChange";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_EQPMONSTART          = "AMJob-LotReserve";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_EQPMONCOMP           = "AMJob-LotRemove";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_LOTREMOVE            = "LotRemove";
    public static final String SP_EQPMONITORJOB_OPECATEGORY_EQPMONITORLOTFAILED  = "EqpMonitorLotFailed";
    public static final String SP_EQPMONITOR_LEVEL_EQPMONKIT                     = "EqpMonKit";
    public static final String SP_EQPMONITOR_LEVEL_EQPMONNOKIT                   = "EqpMonNoKit";
    public static final String SP_EQPMONITOR_TYPE_MANUAL                         = "Manual";
    public static final String SP_EQPMONITOR_TYPE_ROUTINE                        = "Routine";
    public static final String SP_EQPMONITOR_OPELABEL_MONITOR                    = "Monitor";
    public static final String SP_EQPMONITOR_OPELABEL_POSTMEASUREMENT            = "Post Measurement";
    public static final String SP_POSTPROCESS_ACTIONID_EQPMONITOREVAL            = "AutoMonitorEval";
    public static final String SP_POSTPROCESS_ACTIONID_EQPMONITORJOBLOTREMOVE    = "AutoMonitorJobLotRemove";
    public static final String SP_EQPMONITOROPERATIONKEY_SEPARATOR_CHAR          = ".";
    public static final String SP_SYSTEMMSGCODE_EMONSERR                         = "AMONSERR";

    public static final String SP_POSTPROCESS_ACTIONID_EQPMONITORUSEDCOUNTUP   = "AutoMonitorUsedCountUp";
    public static final String SP_EQPMONUSEDCNT_ACTION_UPDATE                  = "Update";
    public static final String SP_EQPMONUSEDCNT_ACTION_INCREMENT               = "Increment";
    public static final String SP_EQPMONUSEDCNT_ACTION_RESET                   = "Reset";
    public static final String SP_OPERATIONCATEGORY_EQPMONUSEDCNTUPDATE        = "EqpMonUsedCntUpdate";

    public static final String SP_PCSTACTIONCODE_PROCESSSTART       = "ProcessStart";
    public static final String SP_PCSTACTIONCODE_PROCESSEND         = "ProcessEnd";

    public static final String SP_TIMESTAMP_NIL_OBJECT_STRING                 = "1901-01-01-00.00.00.000000";


    public static final String EVTYPE_FLOWBATCHING       = "FlowBatching";
    public static final String EVTYPE_EQP_RESERVE        = "EqpReserve";
    public static final String EVTYPE_EQP_RESERVE_CANCEL = "EqpReserveCancel";
    public static final String EVTYPE_LOTREMOVE          = "LotRemove";
    public static final String EVTYPE_REFLOWBATCHING     = "ReFlowBatching";

    public static final String SP_BONDINGGROUPACTION_CREATE                    = "Create";
    public static final String SP_BONDINGGROUPACTION_DELETE                    = "Delete";
    public static final String SP_BONDINGGROUPACTION_PARTIALRELEASE            = "PartialRelease";
    public static final String SP_BONDINGGROUPACTION_PARTIALRELEASEDESTINATION = "Destination";
    public static final String SP_BONDINGGROUPACTION_PARTIALRELEASESOURCE      = "Source";
    public static final String SP_BONDINGGROUPACTION_UPDATE                    = "Update";
    public static final String SP_BONDINGGROUPSTATE_CREATED                    = "Created";
    public static final String SP_BONDINGGROUPSTATE_ERROR                      = "Error";
    public static final String SP_BONDINGGROUPSTATE_PROCESSED                  = "Processed";
    public static final String SP_BONDINGGROUPSTATE_RESERVED                   = "Reserved";
    public static final String SP_BONDINGGROUP_UPDATEMODE_CREATE               = "Create";
    public static final String SP_BONDINGGROUP_UPDATEMODE_DELETE               = "Delete";
    public static final String SP_BONDINGGROUP_UPDATEMODE_UPDATE               = "Update";
    public static final String SP_BONDINGPROCESSSTATE_COMPLETED                = "Completed";
    public static final String SP_BONDINGPROCESSSTATE_ERROR                    = "Error";
    public static final String SP_BONDINGPROCESSSTATE_UNKNOWN                  = "-";
    public static final String SP_BONDINGSIDE_BOTTOM                           = "Bottom";
    public static final String SP_BONDINGSIDE_TOP                              = "Top";
    public static final String SP_FLOWSECTIONCONTROLCATEGORY_BONDINGFLOW       = "Bonding Flow";
    public static final String SP_LOT_FINISHED_STATE_STACKED                   = "STACKED";
    public static final String SP_LOT_BONDINGCATEGORY_BASE                     = "Base";
    public static final String SP_LOT_BONDINGCATEGORY_BASECANCEL               = "BaseCan";
    public static final String SP_LOT_BONDINGCATEGORY_TOP                      = "Top";
    public static final String SP_LOT_BONDINGCATEGORY_TOPCANCEL                = "TopCan";
    public static final String SP_MC_CATEGORY_WAFERBONDING                     = "Wafer Bonding";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKING              = "WaferStacking";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGBASE          = "WaferStackingBase";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGBASECANCEL    = "WaferBondingCancel";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGTOP           = "WaferStackingTop";
    public static final String SP_OPERATIONCATEGORY_WAFERSTACKINGTOPCANCEL     = "WaferBondingCancel";
    public static final String SP_POSTPROCESS_ACTIONID_WAFERSTACKING           = "WaferStacking";
    public static final String SP_SCRAPSTATE_STACKED                           = "Stacked";


    public static final String DURABLE_STATUS_CHANGE = "STATCHG";
    public static final String PREVENTIVE_MAINTENANCE_RESET = "PMRESET";


    public static final int DEFAULTRETRYCOUNT               =  5;
    public static final int DEFAULTRETRYINTERVALTIME        = 60;
    public static final int CRITERIA_NA                     =  1;

    public static final int SP_PARVAL_NOCHANGE                  = 0;
    public static final int SP_PARVAL_ADD                       = 1;
    public static final int SP_PARVAL_UPDATE                    = 2;
    public static final int SP_PARVAL_DELETE                    = 3;

    public static final String SP_PARVAL_CHANGETYPE_NOCHANGE       = "NoChange";
    public static final String SP_PARVAL_CHANGETYPE_ADD            = "Add";
    public static final String SP_PARVAL_CHANGETYPE_UPDATE         = "Update";
    public static final String SP_PARVAL_CHANGETYPE_DELETE         = "Delete";

    public static final String HOLD_REASON_CODE_RUNNING_HOLD_RELEASE    = "RNHR";

    public static final String SP_REASON_SPCOUTOFRANGEINHIBIT              = "SOOR"  ;
    public static final String SP_REASON_SPECOVERINHIBIT                   = "SPEC"  ;
    public static final String SP_REASON_GATEPASS                          = "GTPS"  ;

    public static final String SP_REASON_NONPROBANKHOLD                    = "NPBH"  ;
    public static final String SP_REASON_NONPROBANKHOLDRELEASE             = "NPBR"  ;

    public static final String SP_REASON_PROCESSLAGTIMEHOLD                = "PLTH"  ;
    public static final String SP_REASON_PROCESSLAGTIMEHOLDRELEASE         = "PLTR"  ;

    public static final String SP_REASON_RSRV                              = "RSRV"  ;

    public static final String SP_REASON_PROCESSHOLDCANCEL                 = "PHLC"  ;

    public static final String SP_REASON_QTIMEOVER                         = "QTOV"  ;
    public static final String SP_REASON_QTIMECLEAR                        = "QTCL"   ;

    public static final String SP_REASON_FPCHOLD                           = "FPCH"  ;

    public static final String SP_REASON_WAFERSAMPLINGHOLD                 = "SMPL"  ;

    public static final String SP_REASON_STBCANCELHOLD                     = "STBC"  ;

}
