package com.fa.cim.tms.status.recovery.enums;

/**
 * description:
 * This file use to define transaction ID by Enum
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Miner         create file
 * M-TM S-OM
 *
 * @author: miner
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public enum TransactionIDEnum {
    NULL(null),
    //----------TMS  Tx Funciton ID------------------------
    OM01("OM01"),// The TransportJobCreateReqService ID
    OM02("OM02"), // The TransportJobStopReqController ID
    OM03("OM03"), // The TransportJobPauseReqService ID
    OM04("OM04"), // The TransportJobCancelReqController ID
    OM05("OM05"),// The TransportJobResumeReqController ID
    OM06("OM06"), // The TransportJobAbortReqService ID
    OM07("OM07"), //The TransportJobRemoveReqController ID
    OM08("OM08"),//TransportRouteCheckReqController
    OM09("OM09"), // The UploadInventoryReqController ID
    OM10("OM10"),//The StockerDetailInfoInqController ID
    OM11("OM11"),//The OnlineHostInqController ID
    OM12("OM12"),//PriorityChangeReqController
    OM13("OM13"), //The N2PurgeReqController ID
    OM14("OM14"),//The TransportJobInqController ID
    OM15("OM15"),// CarrierInfoChangeReqController
    OM16("OM16"),// EstimatedTransportTimeInqController
    TM01("TM01"), // The CarrierLocationReportController ID
    TM02("TM02"),//CarrierStatusReportController
    TM03("TM03"),// The TransportJobStatusReportController ID
    TM04("TM04"),//EndTimeViolationReportController
    TM05("TM05"),//E10StatusReportController
    TM06("TM06"), // The CarrierIDReadReportController ID
    TM07("TM07"),//CarrierInfoInqController
    TM08("TM08"),//The AccessControlCheckInqController ID
    TM09("TM09"),//The OnlineAmhsInqController ID
    TM10("TM10"),//AlarmReportController
    TM11("TM11"),//DateAndTimeReqController
    TM12("TM12"),//The SubComponentStatusReportService ID
    TM13("TM13"),//N2PurgeReportController
    TM14("TM14"),//The AllCarrierIDInquiryController ID
    TM15("TM15"),//The LocalTransportJobReportController ID
    TM16("TM16"),//The CarrierIDReadReportRetryController ID
    //----------RTMS  Tx Funciton ID------------------------
    RTM01("RTM01"), // The RtmsCarrierLocationReportController ID
    RTM02("RTM02"),//The RtmsCarrierStatusReportController
    RTM03("RTM03"),// The RtmsTransportJobStatusReportController ID
    RTM04("RTM04"),//The RtmsEndTimeViolationReportController
    RTM06("RTM06"), // The RtmsCarrierIDReadReportController ID
    RTM07("RTM07"),//The RtmsCarrierInfoInqController
    RTM09("RTM09"),//The RtmsOnlineAmhsInqController ID
    RTM14("RTM14"),//The RtmsAllCarrierIDInquiryController ID
    RTM17("RTM17"),//The RtmsCarrierIDReadReportRetryController ID
    ROM01("ROM01"),// The RtmsTransportJobCreateReqService ID
    ROM02("ROM02"), // The TransportJobStopReqController ID
    ROM04("ROM04"), // The RtmsTransportJobCancelReqController ID
    ROM06("ROM06"), // The RtmsTransportJobAbortReqService ID
    ROM07("ROM07"), //The RtmsTransportJobRemoveReqController ID
    ROM11("ROM11"),//The RtmsOnlineHostInqController ID
    ROM14("ROM14"),//The RtmsTransportJobInqController ID

    //----------OMS  Tx Funciton ID------------------------

    EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT("OTMSR001"),
    DUARBLE_XFER_JOB_STATUS_RPT("OTMSR003"),
    CASSETTE_STATUS_INQ("ODRBQ001"),
    OPERATION_START_CANCEL_REQ("OEQPW009"),
    LOT_CASSETTE_RESERVE_CANCEL_REQ("OTMSW004"),
    LOT_ASSETTE_XFER_JOB_COMP_RPT("OTMSR002"),
    EQP_ALARM_RPT("OEQPR014"),
    SYSTEM_MSG_RPT("OSYSR001"),
    STOCKER_INFO_INQ("OTMSQ001"),
    STOCKER_LIST_INQ("OTMSQ002"),
    STOCKER_STATUS_CHANGE_RPT("OTMSR005"),
    RETICLE_POD_XFER_STAUES_CHANGE_RPT("ODRBR008"),
    AVAILABLE_STOCKER_INQ("OTMSQ006"),
    STOCKER_INVENTORY_RPT("OTMSR004"),
    WHERE_NEXT_INTER_BAY_INQ("OTMSQ005"),
    LOG_ON_CHECK_REQ("OACCQ001"),
    RETICLE_POD_XFER_JOB_COMP_RPT("OARHR002"),//TXPDR022
    RSPXFER_STATUS_CHANGE_RPT("OARHR003"),//TXPDR023
    RETICLE_POD_STOCKER_INFO_INQ("ODRBQ016"),//TXPDQ018
    RETICLE_POD_STATUS_INQ("ODRBQ008"),//TXPDQ013
    RETICLE_POD_INVENTORY_RPT("ODRBR016");//TXPDR014


    private final String value;

    TransactionIDEnum(String value) {
        this.value = value;
    }

    /**
     * description:
     * This method use to get TransactionIDEnum by value, if not found, it will return null.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param value
     * @return com.fa.cim.enuma.TransactionIDEnum
     * @author miner
     * @date 2018/3/20
     */
    public static TransactionIDEnum get(String value) {
        if (value instanceof String) {
            for (TransactionIDEnum transactionIDEnum : TransactionIDEnum.values()) {
                if (value.equals(transactionIDEnum.getValue())) {
                    return transactionIDEnum;
                }
            }
        }
        return NULL;
    }

    /**
     * description:
     * This method use to determine whether the transactionIDEnum's value and value are equal.
     * If equal then return true,otherwise return false.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param transactionIDEnum - transaction ID
     * @param value             - value
     * @return boolean
     * @author miner
     * @date 2018/3/20
     */
    public static boolean equals(TransactionIDEnum transactionIDEnum, String value) {
        if (value instanceof String) {
            return value.equals(transactionIDEnum.getValue());
        }
        return (null == transactionIDEnum.getValue());
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
