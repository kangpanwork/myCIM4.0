package com.fa.cim.tms.manager.impl;

import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.*;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.manager.ITmsControllerListenerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * oms call tms ->mq listener
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/3        ********             Miner               create file
 *
 * @author: Miner
 * @date: 2020/2/3 19:41
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
@Listenable
public class TmsControllerListenerManagerImpl implements ITmsControllerListenerManager {
    @Autowired
    private ITransportJobCreateReqController transportJobCreateReqController;
    @Autowired
    private ITransportJobStatusRptController transportJobStatusRptController;
    @Autowired
    private ICarrierLocationRptController carrierLocationRptController;
    @Autowired
    private ICarrierIDReadRptController carrierIDReadRptController;
    @Autowired
    private ILocalTransportJobRptController localTransportJobRptController;
    @Autowired
    private IOnlineMcsInqController onlineMcsInqController;
    @Autowired
    private IDateAndTimeReqController dateAndTimeReqController;
    @Autowired
    private ITransportJobInqController transportJobInqController;
    @Autowired
    private ITransportJobCancelReqController transportJobCancelReqController;
    @Autowired
    private IStockerDetailInfoInqController stockerDetailInfoInqController;
    @Autowired
    private IUploadInventoryReqController uploadInventoryReqController;
    @Autowired
    private IPriorityChangeReqController priorityChangeReqController;
    @Autowired
    private IAlarmReportController alarmReportController;
    @Autowired
    private ICarrierIDReadRptRetryController carrierIDReadRptRetryController;
    @Autowired
    private IE10StatusRptController e10StatusRptController;
    @Autowired
    private IAccessControlCheckInqController accessControlCheckInqController;
    @Autowired
    private IAllCarrierIDInqController carrierIDInqController;
    @Autowired
    private ICarrierStatusRptController carrierStatusRptController;
    @Autowired
    private IEndTimeViolationRptController endTimeViolationRptController;
    @Autowired
    private ISubComponentStatusRptController subComponentStatusRptController;
    @Autowired
    private ICarrierInfoInqController carrierInfoInqController;
    @Autowired
    private IN2PurgeRptController n2PurgeRptController;

    @CimMapping(names = "OM01")
    public Response tmsTransportJobCreateReq(@RequestBody Params.TransportJobCreateReqParams timRequest) {
        log.info("OMS->TMS tmsTransportJobCreateReq Request Json From MQ >>>:" + timRequest, toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM01.getValue(), "OMS messageBody is null");
        }
//        Params.TransportJobCreateReqParams transportJobCreateReqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobCreateReqParams.class);
        Response response = transportJobCreateReqController.tmsTransportJobCreateReq(timRequest);
        log.info("OMS->TMS tmsTransportJobCreateReq Response Json To MQ >>>:" + response.toString());

        return response;
    }

    @CimMapping(names = "OM04")
    public Response tmsTransportJobCancelReq(@RequestBody Params.TransportJobCancelReqParams timRequest) {
        log.info("OMS->TMS tmsTransportJobCancelReq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM04.getValue(), "OMS messageBody is null");
        }
//        Params.TransportJobCancelReqParams transportJobCancelReqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobCancelReqParams.class);
        Response response = transportJobCancelReqController.tmsTransportJobCancelReq(timRequest);
        log.info("OMS->TMS tmsTransportJobCancelReq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM09")
    public Response tmsUploadInventoryReq(@RequestBody Params.UploadInventoryReqParmas timRequest) {
        log.info("OMS->TMS tmsUploadInventoryReq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM09.getValue(), "OMS messageBody is null");
        }
//        Params.AmhsUploadInventoryReqParmas amhsUploadInventoryReqParmas = JSONObject.parseObject(messageBody.toString(), Params.AmhsUploadInventoryReqParmas.class);
        Response response = uploadInventoryReqController.tmsUploadInventoryReq(timRequest);
        log.info("OMS->TMS tmsUploadInventoryReq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM10")
    public Response tmsStockerDetailInfoInq(@RequestBody Params.StockerDetailInfoInqParmas timRequest) {
        log.info("OMS->TMS tmsStockerDetailInfoInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM10.getValue(), "OMS messageBody is null");
        }
//        Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas = JSONObject.parseObject(messageBody.toString(), Params.StockerDetailInfoInqParmas.class);
        Response response = stockerDetailInfoInqController.tmsStockerDetailInfoInq(timRequest);
        log.info("OMS->TMS tmsStockerDetailInfoInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM12")
    public Response tmsPriorityChangeReq(@RequestBody Params.PriorityChangeReqParam timRequest) {
        log.info("OMS->TMS tmsPriorityChangeReq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM12.getValue(), "oms messageBody is null");
        }
//        Params.PriorityChangeReqParam priorityChangeReqParam = JSONObject.parseObject(messageBody.toString(), Params.PriorityChangeReqParam.class);
        Response response = priorityChangeReqController.tmsPriorityChangeReq(timRequest);
        log.info("OMS->TMS tmsPriorityChangeReq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM14")
    public Response tmsTransportJobInq(@RequestBody Params.TransportJobInqParams timRequest) {
        log.info("OMS->TMS tmsTransportJobInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM14.getValue(), "oms messageBody is null");
        }
//        Params.TransportJobInqParams transportJobInqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobInqParams.class);
        Response response = transportJobInqController.tmsTransportJobInq(timRequest);
        log.info("OMS->TMS tmsTransportJobInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "ROM01")
    public Response rmsTransportJobCreateReq(@RequestBody Params.TransportJobCreateReqParams timRequest) {
        log.info("OMS->RTMS rmsTransportJobCreateReq Request Json From MQ >>>:" + timRequest, toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM01.getValue(), "OMS messageBody is null");
        }
//        Params.TransportJobCreateReqParams transportJobCreateReqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobCreateReqParams.class);
        Response response = transportJobCreateReqController.rtmsTransportJobCreateReq(timRequest);
        log.info("OMS->RTMS rmsTransportJobCreateReq Response Json To MQ >>>:" + response.toString());

        return response;
    }

    @CimMapping(names = "ROM04")
    public Response rtmsTransportJobCancelReq(@RequestBody Params.TransportJobCancelReqParams timRequest) {
        log.info("OMS->RTMS rtmsTransportJobCancelReq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM04.getValue(), "OMS messageBody is null");
        }
//        Params.TransportJobCancelReqParams transportJobCancelReqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobCancelReqParams.class);
        Response response = transportJobCancelReqController.rtmsTransportJobCancelReq(timRequest);
        log.info("OMS->RTMS rtmsTransportJobCancelReq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "ROM14")
    public Response rtmsTransportJobInq(@RequestBody Params.TransportJobInqParams timRequest) {
        log.info("OMS->RTMS rtmsTransportJobInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.OM14.getValue(), "oms messageBody is null");
        }
//        Params.TransportJobInqParams transportJobInqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobInqParams.class);
        Response response = transportJobInqController.rtmsTransportJobInq(timRequest);
        log.info("OMS->RTMS rtmsTransportJobInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM01")
    public Response tmsCarrierLocationRpt(@RequestBody Params.CarrierLocationReportParmas timRequest) {
        log.info("MCS->TMS tmsCarrierLocationRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM01.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierLocationReportParmas carrierLocationReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierLocationReportParmas.class);
        Response response = carrierLocationRptController.tmsCarrierLocationRpt(timRequest);
        log.info("MCS->TMS tmsCarrierLocationRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM01")
    public Response rtmsCarrierLocationRpt(@RequestBody Params.CarrierLocationReportParmas timRequest) {
        log.info("MCS->RTMS rtmsCarrierLocationRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM01.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierLocationReportParmas carrierLocationReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierLocationReportParmas.class);
        Response response = carrierLocationRptController.rtmsCarrierLocationRpt(timRequest);
        log.info("MCS->RTMS rtmsCarrierLocationRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM02")
    public Response tmsCarrierStatusRpt(@RequestBody Params.CarrierStatusReportParam timRequest) {
        log.info("MCS->TMS tmsCarrierStatusRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM02.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierStatusReportParam carrierLocationReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierStatusReportParam.class);
        Response response = carrierStatusRptController.tmsCarrierStatusRpt(timRequest);
        log.info("MCS->TMS tmsCarrierStatusRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM02")
    public Response rtmsCarrierStatusRpt(@RequestBody Params.CarrierStatusReportParam timRequest) {
        log.info("MCS->RTMS rtmsCarrierStatusRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM02.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierStatusReportParam carrierLocationReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierStatusReportParam.class);
        Response response = carrierStatusRptController.rtmsCarrierStatusRpt(timRequest);
        log.info("MCS->RTMS rtmsCarrierStatusRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM03")
    public Response tmsTransportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams timRequest) {
        log.info("MCS->TMS tmsTransportJobStatusRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM03.getValue(), "MCS messageBody is null");
        }
//        Params.TransportJobStatusReportParams transportJobStatusReportParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobStatusReportParams.class);
        Response response = transportJobStatusRptController.tmsTransportJobStatusRpt(timRequest);
        log.info("MCS->TMS tmsTransportJobStatusRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM03")
    public Response rtmsTransportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams timRequest) {
        log.info("MCS->RTMS rtmsTransportJobStatusRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM03.getValue(), "MCS messageBody is null");
        }
//        Params.TransportJobStatusReportParams transportJobStatusReportParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobStatusReportParams.class);
        Response response = transportJobStatusRptController.rtmsTransportJobStatusRpt(timRequest);
        log.info("MCS->RTMS rtmsTransportJobStatusRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM04")
    public Response tmsEndTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam timRequest) {
        log.info("MCS->TMS tmsEndTimeViolationRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM04.getValue(), "MCS messageBody is null");
        }
//        Params.EndTimeViolationReportParam endTimeViolationReportParam = JSONObject.parseObject(messageBody.toString(), Params.EndTimeViolationReportParam.class);
        Response response = endTimeViolationRptController.tmsEndTimeViolationRpt(timRequest);
        log.info("MCS->TMS tmsEndTimeViolationRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM04")
    public Response rtmsEndTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam timRequest) {
        log.info("MCS->RTMS rtmsEndTimeViolationRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM04.getValue(), "MCS messageBody is null");
        }
//        Params.EndTimeViolationReportParam endTimeViolationReportParam = JSONObject.parseObject(messageBody.toString(), Params.EndTimeViolationReportParam.class);
        Response response = endTimeViolationRptController.rtmsEndTimeViolationRpt(timRequest);
        log.info("MCS->RTMS rtmsEndTimeViolationRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM05")
    public Response tmsE10StatusRpt(@RequestBody Params.E10StatusReportParmas timRequest) {
        log.info("MCS->TMS tmsE10StatusRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM05.getValue(), "MCS messageBody is null");
        }
//        Params.E10StatusReportParmas e10StatusReportParmas = JSONObject.parseObject(messageBody.toString(), Params.E10StatusReportParmas.class);
        Response response = e10StatusRptController.tmsE10StatusReport(timRequest);
        log.info("MCS->TMS tmsE10StatusRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM06")
    public Response tmsCarrierIDReadRpt(@RequestBody Params.CarrierIDReadReportParmas timRequest) {
        log.info("MCS->TMS tmsCarrierIDReadRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM06.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierIDReadReportParmas carrierIDReadReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierIDReadReportParmas.class);
        Response response = carrierIDReadRptController.tmsCarrierIDReadRpt(timRequest);
        log.info("MCS->TMS tmsCarrierIDReadRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM06")
    public Response rtmsCarrierIDReadRpt(@RequestBody Params.CarrierIDReadReportParmas timRequest) {
        log.info("MCS->RTMS rtmsCarrierIDReadRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM06.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierIDReadReportParmas carrierIDReadReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierIDReadReportParmas.class);
        Response response = carrierIDReadRptController.rtmsCarrierIDReadRpt(timRequest);
        log.info("MCS->RTMS rtmsCarrierIDReadRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM07")
    public Response tmsCarrierInfoInq(@RequestBody Params.CarrierInfoInqParam timRequest) {
        log.info("MCS->TMS tmsCarrierInfoInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM07.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierInfoInqParam carrierIDReadReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierInfoInqParam.class);
        Response response = carrierInfoInqController.tmsCarrierInfoInq(timRequest);
        log.info("MCS->TMS tmsCarrierInfoInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM07")
    public Response rtmsCarrierInfoInq(@RequestBody Params.CarrierInfoInqParam timRequest) {
        log.info("MCS->RTMS rtmsCarrierInfoInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM07.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierInfoInqParam carrierIDReadReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierInfoInqParam.class);
        Response response = carrierInfoInqController.rtmsCarrierInfoInq(timRequest);
        log.info("MCS->RTMS rtmsCarrierInfoInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM08")
    public Response tmsAccessControlCheckInq(@RequestBody Params.AccessControlCheckInqParam timRequest) {
        log.info("MCS->TMS tmsAccessControlCheckInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM08.getValue(), "MCS messageBody is null");
        }
//        Params.AccessControlCheckInqParam accessControlCheckInqParam = JSONObject.parseObject(messageBody.toString(), Params.AccessControlCheckInqParam.class);
        Response response = accessControlCheckInqController.tmsAccessControlCheckInq(timRequest);
        log.info("MCS->TMS tmsAccessControlCheckInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM09")
    public Response tmsOnlineMcsInq(@RequestBody Params.OnlineAmhsInqParams timRequest) {
        log.info("OMS->TMS tmsOnlineMcsInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM09.getValue(), "MCS messageBody is null");
        }
//        Params.OnlineAmhsInqParams onlineAmhsInqParams = JSONObject.parseObject(messageBody.toString(), Params.OnlineAmhsInqParams.class);
        Response response = onlineMcsInqController.tmsOnlineMcsInq(timRequest);
        log.info("OMS->TMS tmsOnlineMcsInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM09")
    public Response rtmsOnlineMcsInq(@RequestBody Params.OnlineAmhsInqParams timRequest) {
        log.info("MCS->RTMS rtmsOnlineMcsInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM09.getValue(), "MCS messageBody is null");
        }
//        Params.OnlineAmhsInqParams onlineAmhsInqParams = JSONObject.parseObject(messageBody.toString(), Params.OnlineAmhsInqParams.class);
        Response response = onlineMcsInqController.rtmsOnlineMcsInq(timRequest);
        log.info("MCS->RTMS rtmsOnlineMcsInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM10")
    public Response tmsAlarmRpt(@RequestBody Params.AlarmReportParam timRequest) {
        log.info("MCS->TMS tmsAlarmRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM10.getValue(), "MCS messageBody is null");
        }
//        Params.AlarmReportParam alarmReportParam = JSONObject.parseObject(messageBody.toString(), Params.AlarmReportParam.class);
        Response response = alarmReportController.tmsAlarmRpt(timRequest);
        log.info("MCS->TMS tmsAlarmRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM11")
    public Response tmsDateAndTimeReq(@RequestBody Params.DateAndTimeReqRestParmas timRequest) {
        log.info("MCS->TMS tmsDateAndTimeReq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM11.getValue(), "MCS messageBody is null");
        }
//        Params.DateAndTimeReqRestParmas dateAndTimeReqRestParmas = JSONObject.parseObject(messageBody.toString(), Params.DateAndTimeReqRestParmas.class);
        Response response = dateAndTimeReqController.tmsDateAndTimeReq(timRequest);
        log.info("MCS->TMS tmsDateAndTimeReq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM12")
    public Response tmsSubComponentStatusRpt(@RequestBody Params.SubComponentStatusReportParam timRequest) {
        log.info("MCS->TMS tmsSubComponentStatusRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM12.getValue(), "MCS messageBody is null");
        }
//        Params.SubComponentStatusReportParam subComponentStatusReportParam = JSONObject.parseObject(messageBody.toString(), Params.SubComponentStatusReportParam.class);
        Response response = subComponentStatusRptController.tmsSubComponentStatusRpt(timRequest);
        log.info("MCS->TMS tmsSubComponentStatusRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM13")
    public Response tmsN2PurgeRpt(@RequestBody Params.N2PurgeReportParams timRequest) {
        log.info("MCS->TMS tmsN2PurgeRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM13.getValue(), "MCS messageBody is null");
        }
//        Params.N2PurgeReportParams n2PurgeReportParams = JSONObject.parseObject(messageBody.toString(), Params.N2PurgeReportParams.class);
        Response response = n2PurgeRptController.tmsN2PurgeRpt(timRequest);
        log.info("MCS->TMS tmsN2PurgeRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM14")
    public Response tmsAllCarrierIDInq(@RequestBody Params.AllCarrierIDInquiryParam timRequest) {
        log.info("MCS->TMS tmsAllCarrierIDInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM14.getValue(), "MCS messageBody is null");
        }
//        Params.AllCarrierIDInquiryParam dateAndTimeReqRestParmas = JSONObject.parseObject(messageBody.toString(), Params.AllCarrierIDInquiryParam.class);
        Response response = carrierIDInqController.tmsAllCarrierIDInq(timRequest);
        log.info("MCS->TMS tmsAllCarrierIDInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "RTM14")
    public Response rtmsAllCarrierIDInq(@RequestBody Params.AllCarrierIDInquiryParam timRequest) {
        log.info("MCS->RTMS rtmsAllCarrierIDInq Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM14.getValue(), "MCS messageBody is null");
        }
//        Params.AllCarrierIDInquiryParam dateAndTimeReqRestParmas = JSONObject.parseObject(messageBody.toString(), Params.AllCarrierIDInquiryParam.class);
        Response response = carrierIDInqController.rtmsAllCarrierIDInq(timRequest);
        log.info("MCS->RTMS rtmsAllCarrierIDInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "TM15")
    public Response tmsLocalTransportJobRpt(@RequestBody Params.LocalTransportJobReqParams timRequest) {
        log.info("MCS->TMS tmsLocalTransportJobRpt Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM15.getValue(), "MCS messageBody is null");
        }
//        Params.LocalTransportJobReqParams localTransportJobReqParams = JSONObject.parseObject(messageBody.toString(), Params.LocalTransportJobReqParams.class);
        Response response = localTransportJobRptController.tmsLocalTransportJobRpt(timRequest);
        log.info("MCS->TMS tmsLocalTransportJobRpt Response Json To MQ >>>:" + response.toString());
        return response;
    }


    @CimMapping(names = "TM16")
    public Response tmsCarrierIDReadReportRetry(@RequestBody Params.CarrierIDReadReportParmas timRequest) {
        log.info("MCS->TMS tmsCarrierIDReadReportRetry Request Json From MQ >>>:" + timRequest.toString());
//        Object messageBody = timRequest.getMessageBody();
        if (timRequest == null) {
            return Response.createError(TransactionIDEnum.TM16.getValue(), "MCS messageBody is null");
        }
//        Params.CarrierIDReadReportParmas carrierIDReadReportParmas = JSONObject.parseObject(messageBody.toString(), Params.CarrierIDReadReportParmas.class);
        Response response = carrierIDReadRptRetryController.tmsCarrierIDReadRptRetry(timRequest);
        log.info("MCS->TMS tmsCarrierIDReadReportRetry Response Json To MQ >>>:" + response.toString());
        return response;
    }
}
