package com.fa.cim.tms.manager.impl;

import com.fa.tms.corba.core.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/17        ********             miner               create file
 *
 * @author: Miner
 * @date: 2020/2/17 15:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class XMServiceManagerTestImpl implements XMServiceManagerOperations {
    @Override
    public void XMServiceManager_init() {

    }

    @Override
    public void XMServiceManager_uninit() {

    }

    @Override
    public pptCarrierLocationReportResult_struct TxCarrierLocationReport(pptUser_struct pptUser_struct, pptCarrierLocationReport_struct pptCarrierLocationReport_struct) {
        return null;
    }

    @Override
    public pptCarrierStatusReportResult_struct TxCarrierStatusReport(pptUser_struct pptUser_struct, pptCarrierStatusReport_struct pptCarrierStatusReport_struct) {
        return null;
    }

    @Override
    public pptTransportJobStatusReportResult_struct TxTransportJobStatusReport(pptUser_struct pptUser_struct, pptTransportJobStatusReport_struct pptTransportJobStatusReport_struct) {
        return null;
    }

    @Override
    public pptEndTimeViolationReportResult_struct TxEndTimeViolationReport(pptUser_struct pptUser_struct, pptEndTimeViolationReport_struct pptEndTimeViolationReport_struct) {
        return null;
    }

    @Override
    public pptE10StatusReportResult_struct TxE10StatusReport(pptUser_struct pptUser_struct, pptE10StatusReport_struct pptE10StatusReport_struct) {
        return null;
    }

    @Override
    public pptCarrierIDReadReportResult_struct TxCarrierIDReadReport(pptUser_struct pptUser_struct, pptCarrierIDReadReport_struct pptCarrierIDReadReport_struct) {
        return null;
    }

    @Override
    public pptCarrierInfoInqResult_struct TxCarrierInfoInq(pptUser_struct pptUser_struct, pptCarrierInfoInq_struct pptCarrierInfoInq_struct) {
        return null;
    }

    @Override
    public pptPrivilegeCheckRequestResult_struct TxPrivilegeCheckReq(pptUser_struct pptUser_struct, pptPrivilegeCheckReq_struct pptPrivilegeCheckReq_struct) {
        return null;
    }

    @Override
    public pptBaseResult_struct TxOnlineAmhsInq(pptUser_struct pptUser_struct) {
        return null;
    }

    @Override
    public pptBaseResult_struct TxAlarmReport(pptUser_struct pptUser_struct, pptAlarmReport_struct pptAlarmReport_struct) {
        return null;
    }

    @Override
    public pptDateAndTimeReqResult_struct TxDateAndTimeReq(pptUser_struct pptUser_struct) {
        return null;
    }

    @Override
    public pptBaseResult_struct TxSubComponentStatusReport(pptUser_struct pptUser_struct, pptSubComponentStatusReport_struct pptSubComponentStatusReport_struct) {
        return null;
    }

    @Override
    public pptN2PurgeReportResult_struct TxN2PurgeReport(pptUser_struct pptUser_struct, pptN2PurgeReport_struct pptN2PurgeReport_struct) {
        return null;
    }

    @Override
    public pptAllCarrierIDInquiryResult_struct TxAllCarrierIDInquiry(pptUser_struct pptUser_struct) {
        return null;
    }

    @Override
    public pptBaseResult_struct TxLocalTransportJobReport(pptUser_struct pptUser_struct, pptLocalTransportJobReq_struct pptLocalTransportJobReq_struct) {
        return null;
    }

    @Override
    public amhsTransportJobCreateReqResult_struct TxXmTransportJobCreateReq(pptUser_struct pptUser_struct, amhsTranJobCreateReq_struct amhsTranJobCreateReq_struct) {
        amhsTransportJobCreateReqResult_struct amhsTransportJobCreateReqResult_struct = new amhsTransportJobCreateReqResult_struct();
        amhsTransportJobCreateReqResult_struct.jobID = "111";
        amhsJobCreateResult_struct amhsJobCreateResult_struct = new amhsJobCreateResult_struct();
        objectIdentifier_struct carrierID = new objectIdentifier_struct();
        carrierID.identifier = "aa";
        carrierID.stringifiedObjectReference = "bb";
        amhsJobCreateResult_struct.carrierID = carrierID;
        amhsJobCreateResult_struct[] amhsJobCreateResultStructs = new amhsJobCreateResult_struct[2];
        amhsJobCreateResultStructs[0] = amhsJobCreateResult_struct;
        amhsTransportJobCreateReqResult_struct.jobCreateResultSequenceData = amhsJobCreateResultStructs;
        return amhsTransportJobCreateReqResult_struct;
    }

    @Override
    public amhsTransportJobStopReqResult_struct TxXmTransportJobStopReq(pptUser_struct pptUser_struct, amhsTranJobStopReq_struct amhsTranJobStopReq_struct) {
        return null;
    }

    @Override
    public amhsTransportJobPauseReqResult_struct TxXmTransportJobPauseReq(pptUser_struct pptUser_struct, amhsTranJobPauseReq_struct amhsTranJobPauseReq_struct) {
        return null;
    }

    @Override
    public amhsTransportJobCancelReqResult_struct TxXmTransportJobCancelReq(pptUser_struct pptUser_struct, amhsTranJobCancelReq_struct amhsTranJobCancelReq_struct) {
        return null;
    }

    @Override
    public amhsTransportJobResumeReqResult_struct TxXmTransportJobResumeReq(pptUser_struct pptUser_struct, amhsTranJobResumeReq_struct amhsTranJobResumeReq_struct) {
        return null;
    }

    @Override
    public amhsTransportJobAbortReqResult_struct TxXmTransportJobAbortReq(pptUser_struct pptUser_struct, amhsTranJobAbortReq_struct amhsTranJobAbortReq_struct) {
        return null;
    }

    @Override
    public amhsTransportJobRemoveReqResult_struct TxXmTransportJobRemoveReq(pptUser_struct pptUser_struct, amhsTranJobRemoveReq_struct amhsTranJobRemoveReq_struct) {
        return null;
    }

    @Override
    public amhsTransportRouteCheckReqResult_struct TxXmTransportRouteCheckReq(pptUser_struct pptUser_struct, amhsTransportRouteCheckReq_struct amhsTransportRouteCheckReq_struct) {
        return null;
    }

    @Override
    public amhsUploadInventoryReqResult_struct TxXmUploadInventoryReq(pptUser_struct pptUser_struct, amhsUploadInventoryReq_struct amhsUploadInventoryReq_struct) {
        return null;
    }

    @Override
    public amhsStockerDetailInfoInqResult_struct TxXmStockerDetailInfoInq(pptUser_struct pptUser_struct, amhsStockerDetailInfoInq_struct amhsStockerDetailInfoInq_struct) {
        return null;
    }

    @Override
    public amhsOnlineHostInqResult_struct TxXmOnlineHostInq(pptUser_struct pptUser_struct) {
        return null;
    }

    @Override
    public amhsPriorityChangeReqResult_struct TxXmPriorityChangeReq(pptUser_struct pptUser_struct, amhsPriorityChangeReq_struct amhsPriorityChangeReq_struct) {
        return null;
    }

    @Override
    public amhsN2PurgeReqResult_struct TxXmN2PurgeReq(pptUser_struct pptUser_struct, amhsN2PurgeReq_struct amhsN2PurgeReq_struct) {
        return null;
    }

    @Override
    public amhsTransportJobInqResult_struct TxXmTransportJobInq(pptUser_struct pptUser_struct, amhsTransportJobInq_struct amhsTransportJobInq_struct, String s) {
        return null;
    }

    @Override
    public amhsCarrierInfoChangeReqResult_struct TxXmCarrierInfoChangeReq(pptUser_struct pptUser_struct, amhsCarrierInfoChangeReq_struct amhsCarrierInfoChangeReq_struct) {
        return null;
    }

    @Override
    public amhsEstimatedTarnsportTimeInqResult_struct TxXmEstimatedTarnsportTimeInq(pptUser_struct pptUser_struct, amhsEstimatedTarnsportTimeInq_struct amhsEstimatedTarnsportTimeInq_struct) {
        return null;
    }

    @Override
    public pptCarrierIDReadReportResult_struct TxCarrierIDReadReportRetry(pptUser_struct pptUser_struct, pptCarrierIDReadReport_struct pptCarrierIDReadReport_struct) {
        return null;
    }
}
