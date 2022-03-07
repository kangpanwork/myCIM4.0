package com.fa.cim.tms.event.recovery.constant;

import org.springframework.lang.NonNull;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 13:01
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public final class Constant {

    public static final String TM_TRANSSTATE_STATIONIN = "SI";
    public static final String TM_TRANSSTATE_STATIONOUT = "SO";
    public static final String TM_TRANSSTATE_BAYIN = "BI";
    public static final String TM_TRANSSTATE_BAYOUT = "BO";
    public static final String TM_TRANSSTATE_MANUALIN = "MI";
    public static final String TM_TRANSSTATE_MANUALOUT = "MO";
    public static final String TM_TRANSSTATE_EQUIPMENTIN = "EI";
    public static final String TM_TRANSSTATE_EQUIPMENTOUT = "EO";
    public static final String TM_TRANSSTATE_SHELFIN = "HI";
    public static final String TM_TRANSSTATE_SHELFOUT = "HO";
    public static final String TM_TRANSSTATE_INTERMEDIATEIN = "II";
    public static final String TM_TRANSSTATE_INTERMEDIATEOUT = "IO";
    public static final String TM_TRANSSTATE_ABNORMALIN = "AI";
    public static final String TM_TRANSSTATE_ABNORMALOUT = "AO";


    //CIMFW_Lot_State_Finished

    public static final String TM_LOT_STATE_FINISHED = "FINISHED";
    public static final String TM_LOT_STATE_SHIPPED = "SHIPPED";
    public static final String TM_LOT_HOLDSTATE_OINHOLD = "ONHOLD";
    public static final String TM_LOT_FINISHEDSTATE_COMPLETED = "COMPLETED";
    public static final String TM_LOT_FINISHEDSTATE_SCRAPPED = "SCRAPPED";

    //DurableXferJobStatus
    public static final String TM_DURABLE_XFER_JOB_STATUS_COMPLETED = "Completed";
    public static final String TM_DURABLE_XFER_JOB_STATUS_UPDATED = "Updated";
    public static final String TM_DURABLE_XFER_JOB_STATUS_CANCELLED = "Cancelled";
    public static final String TM_DURABLE_XFER_JOB_STATUS_CREATED = "Created";

    //TransferJobStatus
    public static final String TM_TRANSFER_JOB_STATUS_XCMP = "XCMP";//SP_TRANSFERJOBSTATUS_COMP
    public static final String TM_TRANSFER_JOB_STATUS_XERR = "XERR";//SP_TRANSFERJOBSTATUS_ERROR

    //DurableType
    public static final String TM_DURABLE_TYPE_CARRIER = "Carrier";

    //TransferJobInqFunctionID
    public static final String TM_TRANSFER_JOB_INQ_FUNCTION_IDINQUIRY = "INQUIRY";
    public static final String TM_TRANSFER_JOB_INQ_FUNCTION_UPDATE = "UPDATE";

    //InquiryType
    public static final String TM_INQUIRY_TYPE_BY_CARRIER = "C";
    public static final String TM_INQUIRY_TYPE_BY_TO_MACHINE = "T";
    public static final String TM_INQUIRY_TYPE_BY_FROM_MACHINE = "F";
    public static final String TM_INQUIRY_TYPE_BY_JOB = "J";


    //Boolean
    public static final String TM_YES = "YES";
    public static final String TM_NO = "NO";
    public static final String TM_ON = "ON";
    public static final String TM_OFF = "OFF";

    //transferJobdeleteType
    public static final String TM_TRANSFER_JOB_DELETE_TYPE_JOBID = "JB";
    public static final String TM_TRANSFER_JOB_DELETE_TYPE_CARRIERID = "CA";
    public static final String TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID = "CJ";


    //eventType
    public static final String TM_EVENTTYPE_M0 = "M0";
    public static final String TM_EVENTTYPE_M1 = "M1";
    public static final String TM_EVENTTYPE_M3 = "M3";
    public static final String TM_EVENTTYPE_M6 = "M6";

    //other
    public static final String TM_STRING_R = "R";
    public static final String TM_STRING_DEFAULT = "Default";
    public static final String TM_TRANSFER_JOB_EXPECTED_END_TIME = "21001231101010";
    public static final String TM_STRING_O = "O";
    public static final String TM_STRING_W = "W";
    public static final String TM_VALUE_ONE = "1";
    public static final String TM_VALUE_ZERO = "0";


    //transferJobStatus
    public static final String TM_TRANSFER_JOB_STATUS_TWO = "2";
    public static final String TM_TRANSFER_JOB_STATUS_THREE = "3";

    //carrierJobStatus
    public static final String TM_CARRIER_JOB_STATUS = "13";

    //transferportType'
    public static final String TM_TRANSPORT_TYPE_S = "S";

    //transferJobPriority
    public static final String TM_TRANSFER_JOB_PRIORITY_FOUR = "4";
    public static final String TM_TRANSFER_JOB_PRIORITY_NINETY_NINE = "99";

    //resourceType
    public static final String TM_RESOURCE_TYPE_MANUAL = "MANUAL";
    public static final String TM_RESOURCE_TYPE_MANUAL_OUT = "Manual Out";

    //System
    public static final String TM_ALARM_CODE_XM01 = "0001";
    public static final String TM_ALARM_CODE_RXM01 = "0001";
    public static final String TM_DISPATCH_ERROR = "4049";
    public static final String TM_ALARM_TEXT_XM01 = "EndTimeViolation";
    public static final String TM_ALARM_TEXT_RXM01 = "EndTimeViolation";
    public static final String TM_SYSTEM_ERROR = "2";

    //stockerType
    public static final String TM_STOCKER_TYPE_AUTO = "Auto";
    public static final String TM_STOCKER_TYPE_RETICLEPOD = "ReticlePod";
    public static final String TM_STOCKER_TYPE_BARERETICLE = "BareReticle";
    public static final String TM_STOCKER_TYPE_INTERBAY = "Inter Bay";
    public static final String TM_STOCKER_TYPE_INTRABAY = "Intra Bay";
    public static final String TM_STOCKER_TYPE_RETICLE = "Reticle";
    public static final String TM_STOCKER_TYPE_RETICLESHELF = "ReticleShelf";
    public static final String TM_STOCKER_TYPE_SHELF = "Shelf";
    public static final String TM_STOCKER_TYPE_INTERM = "Interm";

    //zoneType
    public static final String TM_ZONE_TYPE_UNKNOWN = "UNKNOWN";
    public static final String TM_ZONE_TYPE_EMP = "EMP";
    public static final String TM_ZONE_TYPE_RSP = "RSP";


    public static boolean equalsIgnoreCase(@NonNull String bizConstant, String value) {
        return bizConstant.equalsIgnoreCase(value);
    }
}


