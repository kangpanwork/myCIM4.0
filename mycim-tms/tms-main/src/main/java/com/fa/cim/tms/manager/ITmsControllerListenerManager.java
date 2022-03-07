package com.fa.cim.tms.manager;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/12 13:21
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITmsControllerListenerManager {

    //OMS -> TMS interface
    //OM01
    Response tmsTransportJobCreateReq(Params.TransportJobCreateReqParams cimRequest);

    //OM04
    Response tmsTransportJobCancelReq(Params.TransportJobCancelReqParams cimRequest);

    //OM09
    Response tmsUploadInventoryReq(Params.UploadInventoryReqParmas cimRequest);

    //OM10
    Response tmsStockerDetailInfoInq(Params.StockerDetailInfoInqParmas cimRequest);

    //OM12
    Response tmsPriorityChangeReq(Params.PriorityChangeReqParam cimRequest);

    //OM14
    Response tmsTransportJobInq(Params.TransportJobInqParams cimRequest);

    //OMS -> RTMS interface
    //ROM01
    Response rmsTransportJobCreateReq(Params.TransportJobCreateReqParams timRequest);

    //ROM04
    Response rtmsTransportJobCancelReq(Params.TransportJobCancelReqParams cimRequest);

    //ROM14
    Response rtmsTransportJobInq(Params.TransportJobInqParams timRequest);


    //MCS -> TMS interface
    //TM01
    Response tmsCarrierLocationRpt(Params.CarrierLocationReportParmas cimRequest);

    //TM02
    Response tmsCarrierStatusRpt(Params.CarrierStatusReportParam timRequest);

    //TM03
    Response tmsTransportJobStatusRpt(Params.TransportJobStatusReportParams cimRequest);

    //TM04
    Response tmsEndTimeViolationRpt(Params.EndTimeViolationReportParam timRequest);

    //TM05
    Response tmsE10StatusRpt(Params.E10StatusReportParmas cimRequest);

    //TM06
    Response tmsCarrierIDReadRpt(Params.CarrierIDReadReportParmas cimRequest);

    //TM07
    Response tmsCarrierInfoInq(Params.CarrierInfoInqParam cimRequest);

    //TM08
    Response tmsAccessControlCheckInq(Params.AccessControlCheckInqParam timRequest);

    //TM09
    Response tmsOnlineMcsInq(Params.OnlineAmhsInqParams cimRequest);

    //TM10
    Response tmsAlarmRpt(Params.AlarmReportParam cimRequest);

    //TM11
    Response tmsDateAndTimeReq(Params.DateAndTimeReqRestParmas cimRequest);

    //TM12
    Response tmsSubComponentStatusRpt(Params.SubComponentStatusReportParam timRequest);

    //TM13
    Response tmsN2PurgeRpt(Params.N2PurgeReportParams timRequest);

    //TM14
    Response tmsAllCarrierIDInq(Params.AllCarrierIDInquiryParam timRequest);

    //TM15
    Response tmsLocalTransportJobRpt(Params.LocalTransportJobReqParams cimRequest);

    //TM16
    Response tmsCarrierIDReadReportRetry(Params.CarrierIDReadReportParmas cimRequest);

    //MCS -> RTMS interface
    //RTM01
    Response rtmsCarrierLocationRpt(Params.CarrierLocationReportParmas cimRequest);

    //RTM02
    Response rtmsCarrierStatusRpt(Params.CarrierStatusReportParam timRequest);

    //RTM03
    Response rtmsTransportJobStatusRpt(Params.TransportJobStatusReportParams cimRequest);

    //RTM04
    Response rtmsEndTimeViolationRpt(Params.EndTimeViolationReportParam timRequest);

    //RTM06
    Response rtmsCarrierIDReadRpt(Params.CarrierIDReadReportParmas cimRequest);

    //RTM07
    Response rtmsCarrierInfoInq(Params.CarrierInfoInqParam cimRequest);

    //RTM09
    Response rtmsOnlineMcsInq(Params.OnlineAmhsInqParams cimRequest);

    //RTM14
    Response rtmsAllCarrierIDInq(Params.AllCarrierIDInquiryParam timRequest);
}
