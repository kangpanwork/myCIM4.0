package com.fa.cim.common.constant;

public enum TCSReqEnum {
    sendRecipeDownloadReq("RECIPE_DOWNLOAD"),
    sendRecipeCompareReq("RECIPE_COMPARE"),
    sendRecipeDeleteInFileReq("RECIPE_DELETE_IN_FILE"),
    sendNPWCarrierReserveReq("NPW_CARRIER_RESERVE"),
    sendNPWCarrierReserveForIBReq("NPW_CARRIER_RESERVE_FOR_IB"),
    sendPJStatusChangeReq("PJ_STATUS_CHANGE"),
    sendRecipeDirectoryInq("RECIPE_DIRECTORY"),
    sendRecipeUploadReq("RECIPE_UPLOAD"),
    sendRecipeDeleteReq("RECIPE_DELETE"),
    sendEqpEAPInfoInq("EQP_EAP_INFO"),
    sendMoveInReserveReq("MOVE_IN_RESERVE"),
    sendDurableControlJobActionReq("DURABLE_CONTROL_JOB_ACTION"),
    sendMoveInReserveForIBReq("MOVE_IN_RESERVE_FOR_IB"),
    sendEqpModeChangeReq("EQP_MODE_CHANGE"),
    sendCarrierTransferJobEndRpt("CARRIER_TRANSFER_JOB_END"),
    sendTcsRecoveryReq("EAP_RECOVERY"),
    sendRecipeParamAdjustReq("RECIPE_PARAM_ADJUST"),
    sendControlJobActionReq("CJ_STATUS_CHANGE"),
    sendMoveInReserveCancelReq("MOVE_IN_RESERVE_CANCEL"),
    sendMoveInReserveCancelForIBReq("MOVE_IN_RESERVE_CANCEL_FOR_IB"),
    sendArrivalCarrierNotificationCancelForInternalBufferReq("ARRIVAL_CARRIER_CANCEL_FOR_INTERNAL_BUFFER"),
    sendArrivalCarrierNotificationCancel("NPW_CARRIER_RESERVE_CANCEL"),
    sendCJPJProgressInfoInq("CJPJ_PROGRESS_INFO"),
    sendCJPJOnlineInfoInq("CJPJ_ONLINE_INFO"),
    sendRecipeParamAdjustOnActivePJReq("RECIPE_PARAM_ADJUST_ON_ACTIVE_PJ"),
    sendEDCDataItemWithTransitDataInq("EDC_DATA_ITEM_WITH_TRANSIT_DATA"),
    sendMoveOutForIBReq("MOVE_OUT_FOR_IB"),
    sendMoveOutReq("MOVE_OUT"),
    sendMoveInCancelForIBReq("MOVE_IN_CANCEL_FOR_IB"),
    sendMoveInCancelReq("MOVE_IN_CANCEL"),
    sendMoveInForIBReq("MOVE_IN_FOR_IB"),
    sendMoveInReq("MOVE_IN"),
    sendWaferSortOnEqpReq("ONLINE_SORTER_ACTION_EXECUTEC"),
    sendWaferSortOnEqpCancelReq("ONLINE_SORTER_ACTION_CANCEL"),
    sendPartialMoveOutReq("MOVE_OUT_WITH_RUNNING_SPLIT"),
    sendPartialMoveOutForInternalBufferReq("MOVE_OUT_WITH_RUNNING_SPLIT_FORINTERNAL_BUFFERREQ"),
    sendReserveCancelUnloadingLotsForIBReq("RESERVE_CANCEL_UNLOADING_LOTS_FOR_IB"),
    sendDoSpcCheck("DO_SPC_CHECK"),
    sendSLMCassetteUnclampReq("SLM_CASSETTE_UNCLAMP"),
    sendSLMWaferRetrieveCassetteReserveReq("SLM_WAFER_RETRIEVE_CASSETTE_RESERVE"),
    SORT_JOB_CREATE_REQ("SORT_JOB_CREATE"),
    SORT_JOB_CANCEL_REQ("SORT_JOB_CANCEL_REQ"),
    sendDurableMoveInReq("DURABLE_MOVE_IN"),
    sendReticleRetrieveReq("RETICLE_RETRIEVE_REQ"),
    sendReticleStoreCancelReq("RETICLE_STORE_CANCEL_REQ"),
    sendReticleRetrieveCancelReq("RETICLE_RETRIEVE_CANCEL_REQ"),
    sendReticlePodUnclampCancelReq("RETICLE_POD_UNCLAMP_CANCEL_REQ"),
    sendReticleStoreReq("RETICLE_STORE_REQ"),
    sendReticlePodUnclampReq("RETICLE_POD_UNCLAMP_REQ"),
    sendBareReticleStockerOnlineModeChangeReq("BARE_RETICLE_STOCKER_ONLINE_MODE_CHANGE_REQ"),
    sendReticleInventoryReq("RETICLE_INVENTORY_REQ"),
    ;

    private String value;

    TCSReqEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}