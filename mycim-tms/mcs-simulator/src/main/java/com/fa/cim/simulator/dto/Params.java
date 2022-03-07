package com.fa.cim.simulator.dto;

import com.fa.cim.simulator.pojo.Infos;
import com.fa.cim.simulator.pojo.ObjectIdentifier;
import lombok.Data;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/27        ********             Miner               create file
 *
 * @author: miner
 * @date: 2018/11/27 13:20
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Params {
    @Data
    public static class TransportJobResumeReqParams {
        private User requestUserID;
        private String jobID;
        private List<Infos.CarrierJob> carrierJobData;
    }

    @Data
    public static class N2PurgeReqParams {
        private User requestUserID;
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class AccessControlCheckInqParam {
        private User requestUserID;
        private String subSystemID;
        private String category;
    }

    @Data
    public static class SubComponentStatusReportParam {
        public List<Infos.SubComponent> subComponentData;
        private User requestUserID;
        private ObjectIdentifier machineID;
    }

    @Data
    public static class OnlineHostInqParam {
        private User requestUserID;
    }

    @Data
    public static class PriorityChangeReqParam {
        private User requestUserID;
        private String jobID;
        private List<Infos.PriorityInfo> priorityInfoData;
    }

    @Data
    public static class AlarmReportParam {
        private User requestUserID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier vehicleID;
        private String alarmCode;
        private String alarmID;
        private String alarmCategory;
        private String alarmText;
        private String alarmTimestamp;
    }

    @Data
    public static class EquipmentAlarmParam {
        private String alarmCode;
        private String alarmID;
        private String alarmCategory;
        private String alarmText;
        private String alarmTimestamp;
    }

    @Data
    public static class EndTimeViolationReportParam {
        private User requestUserID;
        private String jobID;
        private List<EndTimeViolation> endTimeViolationReport;
    }

    @Data
    public static class EndTimeViolation {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier toMachineID;
        private String expectedStartTime;
        private String expectedEndTime;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private Boolean mandatoryFlag;
        private ObjectIdentifier changeReason;
        private String changeReasonDesc;
    }

    @Data
    public static class N2PurgeReportParams {
        private User requestUserID;
        private String jobID;
        private ObjectIdentifier machineID;
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class CarrierStatusReportParam {
        private User requestUserID;
        private String jobID;
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierStatus;
    }

    @Data
    public static class CarrierInfoInqParam {
        private User requestUserID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier machineID;
    }

    @Data
    public static class EstimatedTransportTimeInqParams {
        private User requestUserID;
        private Infos.EstimatedTarnsportTimeInq estimatedTarnsportTime;
    }

    @Data
    public static class TransportRouteCheckReqParams {
        private User requestUserID;
        private String transportType;
        private List<RouteCheck> routeCheckData;
    }

    @Data
    public static class RouteCheck {
        public ObjectIdentifier carrierID;
        public String zoneType;
        public ObjectIdentifier fromMachineID;
        public ObjectIdentifier fromPortID;
        public String toStockerGroup;
        public List<Infos.ToDestinationStr> toMachine;
        public String priority;
    }

    @Data
    public static class TransportJobStopReqParams {
        private User requestUserID;
        private String jobID;
        private List<Infos.CarrierJob> carrierJobData;
    }

    @Data
    public static class TransportJobRemoveReqParams {
        private User requestUserID;
        private String jobID;
        private List<Infos.CarrierJob> carrierJobData;
    }

    @Data
    public static class TransportJobPauseReqParams {
        private User requestUserID;
        private String jobID;
        private List<Infos.CarrierJob> carrierJobData;
    }

    @Data
    public static class TransportJobAbortReqParams {
        private User requestUserID;
        private String jobID;
        private List<Infos.CarrierJob> carrierJobData;
    }

    @Data
    public static class OnlineAmhsInqParams {
        private User requestUserID;
    }

    @Data
    public static class LocalTransportJobReqParams {
        private User requestUserID;
        private String jobID;
        private List<Infos.LocalTransportJob> localTransportJobData;
    }

    @Data
    public static class TransportJobInqParams {
        private User requestUserID;
        private String inquiryType;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier fromMachineID;
        private String functionID;
    }

    @Data
    public static class TransportJobCreateReqParams {
        private User requestUserID;
        private String jobID;
        private Boolean rerouteFlag;
        private String transportType;//S=Single, B=Batch, L=Load/Unload
        private List<Infos.AmhsJobCreateArray> jobCreateData;

    }

    @Data
    public static class TransportJobStatusReportParams {
        private User requestUserID;
        private String jobID;
        private String jobStatus;
        private String originator;
        private Boolean jobRemoveFlag;
        private List<Infos.JobStatusReportArray> jobStatusReportData;

    }


    @Data
    public static class CarrierLocationReportParmas {
        private User requestUserID;
        private String jobID;
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String status;
        private ObjectIdentifier currMachineID;
        private ObjectIdentifier currPortID;
        private String currZoneID;
        private String currShelfType;
    }

    @Data
    public static class CarrierIDReadReportParmas {
        private User requestUserID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier machineID;//将要去的设备id
        private ObjectIdentifier portID;
        private ObjectIdentifier portType;
        private String inOutType;
        private Boolean jobRequestFlag;
    }

    @Data
    public static class StockerDetailInfoInqParmas {
        private User requestUserID;
        private ObjectIdentifier machineID;
    }

    @Data
    public static class TransportJobCancelReqParams {
        private User requestUserID;
        private String jobID;
        private List<Infos.AmhsCarrierJob> carrierJobData;
    }

    @Data
    public static class DateAndTimeReqRestParmas {
        private User requestUserID;
    }

    @Data
    public static class E10StatusReportParmas {
        private User requestUserID;
        private ObjectIdentifier machineID;
        private ObjectIdentifier e10Status;
        private String claimMemo;
    }


    @Data
    public static class AmhsUploadInventoryReqParmas {
        private User requestUserID;
        private ObjectIdentifier machineID;
        private String uploadLevel;
    }

    @Data
    public static class CarrierInfoChangeReqParam {
        private User requestUserID;
        private ObjectIdentifier carrierID;
        private String zoneType;
    }

    @Data
    public static class AllCarrierIDInquiryParam {
        private User requestUserID;
    }
}
