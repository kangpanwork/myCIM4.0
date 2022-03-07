package com.fa.cim.common.constant;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/24                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/12/24 9:56
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public enum ServerNameDispatchEnum {
    NULL(null),
    EqpInfoForIBInq("OEQPQ002"),
    MoveInReserveReq("ODISW001"),
    MoveInReserveForIBReq("ODISW003"),
    PortStatusChangeRpt("OEQPR004"),
    CarrierLoadingRpt("OEQPR001"),
    CarrierLoadingForIBRpt("OEQPR002"),
    LotListByCarrierInq("OLOTQ003"),
    LotsMoveInInfoInq("OEQPQ006"),
    LotsMoveInInfoForIBInq("OEQPQ007"),
    MoveInReq("OEQPW005"),
    MoveInForIBReq("OEQPW004"),
    PJInfoRpt("OCJPR002"),
    ProcessStatusRpt("OEQPR012"),
    PJStatusChangeRpt("OCJPR001"),
    ChamberWithProcessWaferRpt("OEQPR010"),
    WaferPositionWithProcessResourceRpt("OEQPR011"),
    EDCDataItemWithTransitDataInq("OEDCQ001"),
    EDCTransitDataRpt("OEDCR001"),
    MoveOutReq("OEQPW006"),
    MoveOutForIBReq("OEQPW008"),
    CarrierUnloadingRpt("OEQPR005"),
    CarrierUnloadingForIBRpt("OEQPR006"),
    MoveInReserveCancelReq("ODISW002"),
    MoveInReserveCancelForIBReq("ODISW004"),
    MoveInCancelReq("OEQPW009"),
    MoveInCancelForIBReq("OEQPW010"),
    RunningHoldReq("OEQPW020"),
    CarrierMoveFromIBRpt("OEQPR009"),
    CarrierMoveToIBRpt("OEQPR008"),
    ReserveUnloadingLotsForIBRpt("OEQPR013"),
    CarrierOutFromIBReq("OEQPW022"),
    NPWCarrierReserveReq("OTMSW008"),
    NPWCarrierReserveForIBReq("OTMSW009"),
    NPWCarrierReserveCancelReq("OTMSW010"),
    NPWCarrierReserveCancelForIBReq("OTMSW012"),
    SortJobNotificationReq("OSRTW009"),
    SortJobCancelNotificationReq("OSRTW008"),
    SJListInq("OSRTQ005"),
    SJStatusInq("OSRTQ006"),
    OnlineSorterActionExecuteReq("OSRTW004"),
    OnlineSorterRpt("OSRTR001"),
    SJStatusChgRpt("OSRTR002"),
    CarrierExchangeReq("OSRTW002"),
    ChamberStatusChangeRpt("OEQPR007"),
    EqpModeChangeReq("OEQPW002"),
    EqpStatusChangeRpt("OEQPR003"),
    CJPJOnlineInfoInq("OCJPQ001"),
    EAPRecoveryReq("OEQPW013"),
    EqpEAPInfoInq("OEQPQ011"),
    BondingGroupListInq("OBNDQ002"),
    BondingMapRpt("OBNDR001"),
    FMCMoveInReserveReq("OFMCW003"),
    ProcessJobMapInfoRpt("TXEQR013"),
    FMCProcessJobStatusRpt("OFMCR001"),
    FMCWaferStoreRpt("OFMCR003"),
    FMCCarrierRemoveFromCJReq("OFMCW001"),
    FMCWaferRetrieveRpt("OFMCR002"),
    EqpUsageCountResetReq("OEQPW016"),
    MfgRestrictReq("OCONW001_EX"),
    LotOperationSelectionInq("OLOTQ010"),
    SkipReq("OLOTW003"),
    HoldLotReleaseReq("OLOTW002"),
    FutureHoldReq("OPRCW001"),
    FutureHoldCancelReq("OPRCW002"),
    LotInfoInq("OLOTQ001"),
    OcapUpdateRpt("OOCPR001"),
    EqpAutoMoveInReserveReq("ODISW0010"),
    HoldLotReq("OLOTW001"),
    EntityInhibitListInq("OCONQ001_EX"),
    EntityInhibitCancelReq("OCONW002_EX"),
    HoldLotListInq("OLOTQ006"),
    AllAvailableEqpInq("OTMSQ008"),
    AvailableEqpInq("OTMSQ007"),
    CarrierTransferForIBReq("OTMSW002"),
    CarrierTransferReq("OTMSW001"),
    FmcCarrierTransferReq("TXDSC017"),
    DmsTransferReq("OTMSW015"),
    DmsTransferForIBReq("OTMSW016"),
    EqpRecipeSelectionInq("OEQPQ014"),
    ReticleStoreReq("ODRBW016"),
    ReticleRetrieveReq("ODRBW015"),
    ReticlePodUnclampReq("OARHW014"),
    ReticlePodXferReq("OARHW016"),
    WhereNextForReticlePodInq("OARHQ001"),
    ReticlePodUnclampAndXferJobCreateReq("OARHW013"),
    ReticlePodXferJobCreateReq("OARHW015"),
    WhatReticleRetrieveInq("OARHQ005"),
    ReticleXferJobCreateReq("OARHW004"),
    ReticleActionReleaseReq("OARHW003"),
    ForceSkipReq("OLOTW004"),
    ReasonCodeListByCategoryInq	("OSYSQ003"),
    MultiPathListInq("OLOTQ013"),
    ReworkReq("OLOTW010"),
    ReworkWithHoldReleaseReq("OLOTW015"),
    CarrierOutReq("OEQPW031"),
    CarrierOutPortReq("OEQPW030"),
    CancelCarrierOutPortReq("OEQPW021"),

    //Tms
    TransportJobCreateReq("OM01"),
    TransportJobCancelReq("OM04"),
    UploadInventoryReq("OM09"),
    StockerDetailInfoInq("OM10"),
    OnlineHostInq("OM11"),
    PriorityChangeReq("OM12"),
    TransportJobInq("OM14"),
    RtransportJobCreateReq("ROM01"),
    RtransportJobCancelReq("ROM04"),
    RtransportJobInq("ROM14"),

    CarrierTransferStatusChangeRpt("OTMSR001"),
    DurableXferStatusChangeRpt("OTMSR003"),
    StockerStatusChangeRpt("OTMSR005"),
    WhereNextInterBay("OTMSQ005"),
    CassetteStatusInq("ODRBQ001"),
    StockerInfoInq("OTMSQ001"),
    SystemMsgRpt("OSYSR001"),
    StockerForAutoTransferInq("OTMSQ006"),
    StockerInventoryRpt("OTMSR004"),
    LotCassetteXferJobCompRpt("OTMSR002"),
    LoginCheckInq("OACCQ001"),
    EqpAlarmRpt("OEQPR014"),
    ReserveCancelReq("OTMSW004"),
    ReticlePodXferJobCompRpt("OARHR002"),
    RSPXferStatusChangeRpt("OARHR003"),
    ReticlePodStockerInfoInq("ODRBQ016"),
    ReticlePodStatusInq("ODRBQ008"),
    ReticlePodInventoryRpt("ODRBR016"),

    //RTMS API
    ReticleTransferStatusChangeRpt("ODRBR005"),
    ReticleStatusChangeReq("ODRBW004"),
    ReticlePodStatusChangeReq("ODRBW005"),
    ReticleHoldReq("ORTLW005"),
    ReticleHoldReleaseReq("ORTLW006"),
    ReticleJustInOutRpt("ODRBR003"),
    EqpAllInq("OEQPQ016"),
    StockerListInq("OTMSQ002"),
    EqpInfoInq("OEQPQ001"),
    ReticleInspectionRequestReq("ORTLW002"),
    ReticleinspectioninReq("ORTLW003"),
    ReticleinspectionoutReq("ORTLW004"),
    ReticleSortRpt("ODRBR009"),
    ReticleTerminateReq("ORTLW007"),
    ReticleTerminateCancelReq("ORTLW008"),
    ReticleScrapReq("ORTLW012"),
    ReticleScrapCancelReq("ORTLW013"),
    ReticlePodTransferStatusChangeRpt("ODRBR008"),
    DurableSubStatusInq("ODRBQ005"),
    ReticleUpdateParamsInq("ORTLQ102"),
    ReticleScanRequestReq("ORTLW014"),
    ReticleScanCompleteReq("ORTLW015"),
    ReticleInspectionTypeChangeReq("ORTLW016"),
    ReticleConfirmMaskQualityReq("ORTLW001"),
    ReticleRepairRequestReq("ORTLW009"),
    ReticleRepairInReq("ORTLW010"),
    ReticleRepairOutReq("ORTLW011"),
    ReticlePodOfflineLoadingReq("ODRBW013"),// old:TXPDC022  new: ODRBW013
    ReticlePodOfflineUnloadingReq("ODRBW014"),
    ReticleOfflineStoreReq("ODRBW011"),
    ReticleOfflineRetrieveReq("ODRBW010"),

    //Mcs
    CarrierLocationRpt("TM01"),
    CarrierStatusRpt("TM02"),
    TransportJobStatusRpt("TM03"),
    EndTimeViolationRpt("TM04"),
    E10StatusRpt("TM05"),
    CarrierIDReadRpt("TM06"),
    CarrierInfoInq("TM07"),
    AccessControlCheckInq("TM08"),
    OnlineMcsInq("TM09"),
    AlarmRpt("TM10"),
    DateAndTimeReq("TM11"),
    SubComponentStatusRpt("TM12"),
    N2PurgeRpt("TM13"),
    AllCarrierIDInq("TM14"),
    LocalTransportJobRpt("TM15"),
    CarrierIDReadReportRetry("TM16"),
    RCarrierLocationRpt("RTM01"),
    RCarrierStatusRpt("RTM02"),
    RTransportJobStatusRpt("RTM03"),
    REndTimeViolationRpt("RTM04"),
    RCarrierIDReadRpt("RTM06"),
    RCarrierInfoInq("RTM07"),
    ROnlineMcsInq("RTM09"),
    RAllCarrierIDInq("RTM14"),

    //SortTer
    SorterActionReq("OSRTW015"),
    SorterActionRpt("OSRTR005"),
    CarrierLoadingForSRTRpt("OSRTR006"),
    CarrierUnloadingForSRTRpt("OSRTR007"),
    SorterActionInq("OSRTQ012"),
    SortActionCancelReq("OSRTW013");


    private String value;

    public String getValue() {
        return value;
    }

    ServerNameDispatchEnum(String value) {
        this.value = value;
    }

    public static String getServerName(String value) {
        if (value instanceof String) {
            for (ServerNameDispatchEnum serverNameDispatchEnum : ServerNameDispatchEnum.values()) {
                if (value.equals(serverNameDispatchEnum.getValue())) {
                    return serverNameDispatchEnum.name();
                }
            }
        }
        return null;
    }
}
