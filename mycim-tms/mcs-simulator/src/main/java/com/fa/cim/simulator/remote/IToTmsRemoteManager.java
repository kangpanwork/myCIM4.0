package com.fa.cim.simulator.remote;

import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.simulator.dto.CimRequest;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import com.fa.cim.simulator.remote.remoteBack.CallTmsDefaultCheckBack;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/5                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/5 14:53
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager("mcs-tms-service")
public interface IToTmsRemoteManager extends RemoteManager {

    //MCS - TMS interface 
//    @Dispatchable("TM01")
    @Dispatchable("CarrierLocationRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsCarrierLocationRpt(@RequestBody Params.CarrierLocationReportParmas param);

//    @Dispatchable("TM02")
    @Dispatchable("CarrierStatusRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsCarrierStatusRpt(@RequestBody Params.CarrierStatusReportParam cimRequest);

//    @Dispatchable("TM03")
    @Dispatchable("TransportJobStatusRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsTransportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams param);

//    @Dispatchable("TM04")
    @Dispatchable("EndTimeViolationRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsEndTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam cimRequest);

//    @Dispatchable("TM05")
    @Dispatchable("E10StatusRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsE10StatusRpt(@RequestBody Params.E10StatusReportParmas param);

//    @Dispatchable("TM06")
    @Dispatchable("CarrierIDReadRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsCarrierIDReadRpt(@RequestBody Params.CarrierIDReadReportParmas param);

//    @Dispatchable("TM07")
    @Dispatchable("CarrierInfoInq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsCarrierInfoInq(@RequestBody Params.CarrierInfoInqParam cimRequest);

//    @Dispatchable("TM08")
    @Dispatchable("AccessControlCheckInq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsAccessControlCheckInq(@RequestBody Params.AccessControlCheckInqParam param);

//    @Dispatchable("TM09")
    @Dispatchable("OnlineMcsInq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsOnlineMcsInq(@RequestBody Params.OnlineAmhsInqParams param);

//    @Dispatchable("TM10")
    @Dispatchable("AlarmRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsAlarmRpt(@RequestBody Params.AlarmReportParam param);

//    @Dispatchable("TM11")
    @Dispatchable("DateAndTimeReq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsDateAndTimeReq(@RequestBody Params.DateAndTimeReqRestParmas param);

//    @Dispatchable("TM12")
    @Dispatchable("SubComponentStatusRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsSubComponentStatusRpt(@RequestBody Params.SubComponentStatusReportParam cimRequest);

//    @Dispatchable("TM13")
    @Dispatchable("N2PurgeRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsN2PurgeRpt(@RequestBody Params.N2PurgeReportParams cimRequest);

//    @Dispatchable("TM14")
    @Dispatchable("AllCarrierIDInq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsAllCarrierIDInq(@RequestBody Params.AllCarrierIDInquiryParam cimRequest);

//    @Dispatchable("TM15")
    @Dispatchable("LocalTransportJobRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsLocalTransportJobRpt(@RequestBody Params.LocalTransportJobReqParams param);

//    @Dispatchable("TM16")
    @Dispatchable("CarrierIDReadReportRetry")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsCarrierIDReadReportRetry(@RequestBody Params.CarrierIDReadReportParmas cimRequest);
    
    //MCS - RTMS interface
    //RTM01
//    @Dispatchable("RTM01")
    @Dispatchable("RCarrierLocationRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsCarrierLocationRpt(@RequestBody Params.CarrierLocationReportParmas cimRequest);

    //RTM02
//    @Dispatchable("RTM02")
    @Dispatchable("RCarrierStatusRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsCarrierStatusRpt(@RequestBody Params.CarrierStatusReportParam timRequest);

    //RTM03
//    @Dispatchable("RTM03")
    @Dispatchable("RTransportJobStatusRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsTransportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams cimRequest);

    //RTM04
//    @Dispatchable("RTM04")
    @Dispatchable("REndTimeViolationRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsEndTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam timRequest);

    //RTM06
//    @Dispatchable("RTM06")
    @Dispatchable("RCarrierIDReadRpt")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsCarrierIDReadRpt(@RequestBody Params.CarrierIDReadReportParmas cimRequest);

    //RTM07
//    @Dispatchable("RTM07")
    @Dispatchable("RCarrierInfoInq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsCarrierInfoInq(@RequestBody Params.CarrierInfoInqParam cimRequest);

    //RTM09
//    @Dispatchable("RTM09")
    @Dispatchable("ROnlineMcsInq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsOnlineMcsInq(@RequestBody Params.OnlineAmhsInqParams cimRequest);

    //RTM14
//    @Dispatchable("RTM14")
    @Dispatchable("RAllCarrierIDInq")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callRTmsAllCarrierIDInq(@RequestBody CimRequest timRequest);

}

