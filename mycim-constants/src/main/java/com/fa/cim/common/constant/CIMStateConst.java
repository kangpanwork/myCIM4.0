package com.fa.cim.common.constant;

import com.fa.cim.common.utils.CimStringUtils;

public class CIMStateConst {
    public static final String SP_UNDEFINED_STATE = "-";
    public static final String SP_USERDATA_ORIG_SM = "MDS";
    public static final String SP_Charater_Pound = "#";


    //CLASS: DocumentRevision
    public static final String CIM_DOCUMENT_REVISION_INWORK                        = "INWORK";
    public static final String CIM_DOCUMENT_REVISION_ACTIVE                        = "ACTIVE";
    public static final String CIM_DOCUMENT_REVISION_INACTIVE                      = "INACTIVE";

    //CLASS: Durable
    public static final String CIM_DURABLE_INUSE                                   = "INUSE";
    public static final String CIM_DURABLE_SCRAPPED                                = "SCRAPPED";
    public static final String CIM_DURABLE_AVAILABLE                               = "AVAILABLE";
    public static final String CIM_DURABLE_NOTAVAILABLE                            = "NOTAVAILABLE";
    public static final String CIM_DURABLE_UNDEFINED                               = "UNDEFINED";
    public static final String SP_DURABLE_CAT_RETICLE                              = "Reticle";
    public static final String SP_DURABLE_CAT_CASSETTE                              = "Cassette";
    public static final String SP_DURABLE_CAT_RETICLE_POD                           ="ReticlePod";
    public static final String SP_DURABLE_HOLD_STATE_ON_HOLD                       ="ONHOLD";
    public static final String SP_DURABLE_FINISHED_STATE_COMPLETED                   ="COMPLETED";
    public static final String SP_DURABLE_PROCESS_STATE_WAITING                    ="WAITING";
    public static final String SP_DURABLE_PROCESS_STATE_PROCESSING                   ="PROCESSING";
    public static final String SP_DURABLE_ON_ROUTE_STATE_ACTIVE                     ="ACTIVE";
    public static final String SP_DURABLE_ON_ROUTE_STATE_FINISHED                   ="FINISHED";


    //CLASS: MESFactory
    public static final String CIM_FACTORY_STARTINGUP                              = "STARTINGUP";
    public static final String CIM_FACTORY_OPERATING                               = "OPERATING";
    public static final String CIM_FACTORY_GOING_TO_STANDBY                        = "GOINGTOSTANDBY";
    public static final String CIM_FACTORY_STANDBY                                 = "STANDBY";
    public static final String CIM_FACTORY_SHUTTING_DOWN_IMMEDIATELY               = "SHUTTINGDOWNIMMEDIATELY";
    public static final String CIM_FACTORY_SHUTTING_DOWN_NORMALLY                  = "SHUTTINGDOWNNORMALLY";
    public static final String CIM_FACTORY_OFF                                     = "OFF";

    //CLASS: ManagedJob
    public static final String CIM_MANAGED_JOB_CREATED                             = "CREATED";
    public static final String CIM_MANAGED_JOB_CANCELLED                           = "CANCELLED";
    public static final String CIM_MANAGED_JOB_QUEUED                              = "QUEUED";
    public static final String CIM_MANAGED_JOB_ACTIVE                              = "ACTIVE";
    public static final String CIM_MANAGED_JOB_FINISHED                            = "FINISHED";
    public static final String CIM_MANAGED_JOB_EXECUTE_STATE_COMPLETED             = "COMPLETED";
    public static final String CIM_MANAGED_JOB_PAUSE_STATE_NOTPAUSED               = "NOTPAUSED";
    public static final String CIM_MANAGED_JOB_PAUSE_STATE_PAUSING                 = "PAUSING";
    public static final String CIM_MANAGED_JOB_PAUSE_STATE_PAUSED                  = "PAUSED";
    public static final String CIM_MANAGED_JOB_STOP_STATE_NOTSTOPPING              = "NOTSTOPPING";
    public static final String CIM_MANAGED_JOB_STOP_STATE_STOPPING                 = "STOPPING";
    public static final String CIM_MANAGED_JOB_STOP_STATE_STOPPED                  = "STOPPED";
    public static final String CIM_MANAGED_JOB_ABORT_STATE_NOTABORTING             = "NOTABORTING";
    public static final String CIM_MANAGED_JOB_ABORT_STATE_ABORTING                = "ABORTING";
    public static final String CIM_MANAGED_JOB_ABORT_STATE_ABORTED                 = "ABORTED";

    //CLASS: Machine
    public static final String CIM_E10_NON_SCHEDULED                               = "NST";
    public static final String CIM_E10_UN_SCHEDULED_DOWN_TIME                      = "UDT";
    public static final String CIM_E10_SCHEDULED_DOWN_TIME                         = "SDT";
    public static final String CIM_E10_ENGINEERING                                 = "ENG";
    public static final String CIM_E10_STANDBY                                     = "SBY";
    public static final String CIM_E10_PRODUCTIVE                                  = "PRD";

    //CLASS: MachineResource
    public static final String CIM_MCRSC_INITIALIZING                              = "INITIALIZING";
    public static final String CIM_MCRSC_WAITING                                   = "WAITING";
    public static final String CIM_MCRSC_ACTIVE                                    = "ACTIVE";
    public static final String CIM_MCRSC_EXECUTING                                 = "EXECUTING";
    public static final String CIM_MCRSC_NOTPAUSED                                 = "NOTPAUSED";
    public static final String CIM_MCRSC_PAUSING                                   = "PAUSING";
    public static final String CIM_MCRSC_PAUSED                                    = "PAUSED";
    public static final String CIM_MCRSC_NOTSTOPPING                               = "NOTSTOPPING";
    public static final String CIM_MCRSC_STOPPING                                  = "STOPPING";
    public static final String CIM_MCRSC_NOTABORTING                               = "NOTABORTING";
    public static final String CIM_MCRSC_ABORTING                                  = "ABORTING";

    //CLASS: PortResource
    public static final String CIM_PORT_RESOURCE_INPUT                             = "INPUT";
    public static final String CIM_PORT_RESOURCE_OUTPUT                            = "OUTPUT";
    public static final String CIM_PORT_RESOURCE_INPUTOUTPUT                       = "INPUT_OUTPUT";

    //CLASS: ProductRequest
    public static final String CIM_PRRQ_PLAN_STATE_NOTPLANNED                      = "NOTPLANNED";
    public static final String CIM_PRRQ_PLAN_STATE_PLANNED                         = "PLANNED";
    public static final String CIM_PRRQ_PROD_STATE_NOTINRELEASE                    = "NOTINRELEASE";
    public static final String CIM_PRRQ_PROD_STATE_INRELEASE                       = "INRELEASE";
    public static final String CIM_PRRQ_PROD_STATE_INWORK                          = "INWORK";
    public static final String CIM_PRRQ_PROD_STATE_ONHOLD                          = "ONHOLD";
    public static final String CIM_PRRQ_PROD_STATE_NOTONHOLD                       = "NOTONHOLD";
    public static final String CIM_PRRQ_PROD_STATE_COMPLETED                       = "COMPLETED";

    //CLASS: Lot
    public static final String CIM_LOT_STATE_LOTCREATED                            = "LOTCREATED";
    public static final String CIM_LOT_STATE_RELEASED                              = "RELEASED";
    public static final String CIM_LOT_STATE_ACTIVE                                = "ACTIVE";
    public static final String CIM_LOT_STATE_FINISHED                              = "FINISHED";
    public static final String CIM_LOT_STATE_SHIPPED                               = "SHIPPED";
    public static final String CIM_LOT_PRODUCTION_STATE_INPRODUCTION               = "INPRODUCTION";
    public static final String CIM_LOT_PRODUCTION_STATE_INREWORK                   = "INREWORK";
    public static final String CIM_LOT_HOLD_STATE_ONHOLD                           = "ONHOLD";
    public static final String CIM_LOT_HOLD_STATE_NOTONHOLD                        = "NOTONHOLD";
    public static final String CIM_LOT_INVENTORY_STATE_INBANK                      = "InBank";
    public static final String CIM_LOT_INVENTORY_STATE_ONFLOOR                      = "OnFloor";
    public static final String CIM_LOT_FINISHED_STATE_COMPLETED                    = "COMPLETED";
    public static final String CIM_LOT_FINISHED_STATE_SCRAPPED                     = "SCRAPPED";
    public static final String CIM_LOT_FINISHED_STATE_EMPTIED                      = "EMPTIED";
    public static final String CIM_LOT_FINISHED_STATE_WAITING                      = "WAITING";
    public static final String CIM_LOT_FINISHED_STATE_ONFLOOR                      = "ONFLOOR";
    public static final String CIM_LOT_HOLD_TYPE_LOTHOLD                           = "HoldLot";
    public static final String CIM_LOT_HOLD_TYPE_FUTUREHOLD                        = "FutureHold";
    public static final String CIM_LOT_HOLD_TYPE_FUTUREHOLDCANCEL                  = "FutureHoldCancel";
    public static final String CIM_LOT_PROCESS_STATE_WAITING                       = "Waiting";
    public static final String CIM_LOT_PROCESS_STATE_PROCESSED                     = "Processed";
    public static final String SP_LOT_PROCESS_STATE_PREPARING                           = "Preparing";
    public static final String SP_LOT_PROCESS_STATE_PROCESSING                     = "Processing";


    //CLASS: Product
    public static final String CIM_PRODUCT_PRODUCT_STATE_PRODUCTCREATED            = "PRODUCT_CREATED";
    public static final String CIM_PRODUCT_PRODUCT_STATE_ININVENTORY               = "IN_INVENTORY";
    public static final String CIM_PRODUCT_PRODUCT_STATE_ALLOCATED                 = "ALLOCATED";
    public static final String CIM_PRODUCT_PRODUCT_STATE_ACTIVE                    = "ACTIVE";
    public static final String CIM_PRODUCT_PRODUCT_STATE_FINISHED                  = "FINISHED";
    public static final String CIM_PRODUCT_PRODUCT_STATE_SHIPPED                   = "SHIPPED";
    public static final String CIM_PRODUCT_FINISHED_STATE_PROCESSING_COMPLETED     = "PROCESSING_COMPLETED";
    public static final String CIM_PRODUCT_FINISHED_STATE_SCRAPPED                 = "SCRAPPED";
    public static final String CIM_PRODUCT_PRODUCTION_STATE_INPRODUCTION           = "IN_PRODUCTION";
    public static final String CIM_PRODUCT_PRODUCTION_STATE_INREWORK               = "IN_REWORK";
    public static final String CIM_PRODUCT_ACTIVITY_STATE_TRAVELING                = "TRAVELING";
    public static final String CIM_PRODUCT_ACTIVITY_STATE_IDLE                     = "IDLE";
    public static final String CIM_PRODUCT_ACTIVITY_STATE_PROCESSING               = "PROCESSING";
    public static final String CIM_PRODUCT_HOLD_STATE_ONHOLD                       = "ON_HOLD";
    public static final String CIM_PRODUCT_HOLD_STATE_NOTONHOLD                    = "NOT_ON_HOLD";

    //CLASS: ComponentManager
    public static final String CIM_COMPONENT_MANAGER_STOPPED                       = "STOPPED";
    public static final String CIM_COMPONENT_MANAGER_STARTING_UP                   = "STARTINGUP";
    public static final String CIM_COMPONENT_MANAGER_OPERATING                     = "OPERATING";
    public static final String CIM_COMPONENT_MANAGER_SHUTTING_DOWN                 = "SHUTTINGDOWN";
    public static final String CIM_COMPONENT_MANAGER_REGISTRATION_REGISTERED       = "REGISTERED";
    public static final String CIM_COMPONENT_MANAGER_REGISTRATION_NOT_REGISTERED   = "NOTREGISTERED";

    //CLASS: ChangeNotice
    public static final String CIM_CHANGE_NOTICE_STATE_INWORK                      = "INWORK";
    public static final String CIM_CHANGE_NOTICE_STATE_PREPARING_TO_ACTIVATE       = "PREPARING_TO_ACTIVATE";
    public static final String CIM_CHANGE_NOTICE_STATE_ACTIVATED                   = "ACTIVATED";

    //CLASS:EntryType
    public static final String CIM_ENTRY_TYPE_CANCEL                                   = "CANCEL";
    public static final String CIM_ENTRY_TYPE_ENTRY                                      = "ENTRY";
    public static final String CIM_ENTRY_TYPE_REMOVE                                    = "REMOVE";

    //CLASS: Wafer
    public static final String CIM_MC_CATEGORY_WAFER_BONDING                             = "Wafer Bonding";

    //Different states of the variable "theProcessDefinitionLevel" in CLASS PosProcessDefinition  COMPONENT PPcDf
    public static final String SP_PD_FLOWLEVEL_MAIN                                        ="Main";
    public static final String SP_PD_FLOWLEVEL_MAIN_FOR_MODULE                      = "Main_Mod";
    public static final String SP_PD_FLOWLEVEL_MODULE                                = "Module";
    public static final String SP_FLOWTYPE_SUB                                        = "Sub";
    public static final String SP_CATEGORY_DURABLESTATE                             = "Durable State";

    // Main PD Type
    public static final String CIM_MAINPDTYPE_REWORK                               ="Rework";

    // Miscellaneous
    public static final String SEPARATOR_CHAR                                      =".";
    public static final String ACTIVE_VERSION                                      ="##";
    public static final String SP_FUNCTION_AVAILABLE_TRUE                         ="1";

    //TransportState
    public static final String SP_TRANS_STATE_STATION_IN                                       ="SI";
    public static final String SP_TRANS_STATE_STATION_OUT                                      ="SO";
    public static final String SP_TRANS_STATE_BAY_IN                                           ="BI";
    public static final String SP_TRANS_STATE_BAY_OUT                                          ="BO";
    public static final String SP_TRANS_STATE_MANUAL_IN                                        ="MI";
    public static final String SP_TRANS_STATE_MANUAL_OUT                                       ="MO";
    public static final String SP_TRANS_STATE_EQUIPMENT_IN                                     ="EI";
    public static final String SP_TRANS_STATE_EQUIPMENT_OUT                                    ="EO";
    public static final String SP_TRANS_STATE_SHELF_IN                                          ="HI";
    public static final String SP_TRANS_STATE_SHELF_OUT                                         ="HO";
    public static final String SP_TRANS_STATE_INTERMEDIATE_IN                                  ="II";
    public static final String SP_TRANS_STATE_INTERMEDIATE_OUT                                 ="IO";
    public static final String SP_TRANS_STATE_ABNORMAL_IN                                       ="AI";
    public static final String SP_TRANS_STATE_ABNORMAL_OUT                                      ="AO";
    public static final String SP_TRANS_STATE_UNKNOWN                                           ="-";

    //Scraped State
    public static final String SP_SCRAP_STATE_ACTIVE                                         ="Active";
    public static final String SP_SCRAP_STATE_SCRAP                                           ="Scrap";
    public static final String SP_SCRAP_STATE_GARBAGE                                     ="Garbage";
    public static final String SP_INTERFAB_XFER_STATE_REQUIRED                             ="Required";
    public static final String SP_INTERFAB_XFER_STATE_NONE                                 ="-";
    public static final String SP_INTERFAB_XFER_STATE_TRANSFERRING                        ="Transferring";
    public static final String SP_INTERFAB_XFER_STATE_ORIGIN_DELETING                     ="OriginDeleting";
    public static final String SP_INTERFAB_XFER_STATE_EMPTY                                  ="";

    //Control Job States
    public static final String SP_CONTROL_JOB_STATUS_QUEUED                                      ="Queued";
    public static final String SP_CONTROL_JOB_STATUS_CREATED                                      ="Created";
    public static final String SP_CONTROL_JOB_STATUS_EXECUTING                                      ="Executing";

    //CASSETTE
    public static final String SP_CASSETTE_MINIMUM_RUNTIME                                            ="0";
    public static final String SP_CASSETTE_MAXIMUM_RUNTIME                                            ="999999";
    public static final String SP_CASSETTE_DEFAULT_CAPACITY                                           ="25";
    public static final String SP_CASSETTE_DEFAULT_NOMINALSIZE                                        ="12";
    public static final String SP_CASSETTE_MINIMUM_OPERATIONSTARTCOUNT                               ="0";
    public static final String SP_CASSETTE_MINIMUM_INTERVALBETWEENPM                                 ="0";
    public static final String SP_RETICLEPOD_MINIMUM_INTERVALBETWEENPM                               ="0";
    public static final String SP_CASSETTE_MAXIMUM_OPERATIONSTARTCOUNT                               ="999999";
    public static final String SP_CASSETTE_MAXIMUM_INTERVALBETWEENPM                                  ="999999";
    public static final String SP_RETICLEPOD_MAXIMUM_INTERVALBETWEENPM                                ="999999";

    // Season
    public static final String CIM_CLASS_NAME_SEASON = "Season";
    public static final String CIM_CLASS_NAME_SEASON_JOB = "SeasonJob";


    public static boolean equals(String src, String dest) {
        return CimStringUtils.equals(src, dest);
    }
}
