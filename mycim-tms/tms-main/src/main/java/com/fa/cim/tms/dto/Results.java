package com.fa.cim.tms.dto;

import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             miner               create file
 *
 * @author: miner
 * @date: 2018/6/28 15:12
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class Results {
    @Data
    public static class TransportJobResumeReqResult {

        private String jobID;
        private List<CarrierJobRc> carrierJobRcData;
    }

    @Data
    public static class N2PurgeReqResult {
        private String jobID;
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class EqpAlarmRptResult {

    }

    @Data
    public static class LoginCheckInqResult {
        private List<FuncList> subSystemFuncLists;
        private List<ObjectIdentifier> productIDs;
        private List<ObjectIdentifier> machineRecipeIDs;
    }

    @Data
    public static class FuncList {
        private String subSystemID;
        private List<FuncID> strFuncIDs;
    }

    @Data
    public static class FuncID {
        private String categoryID;
        private String functionID;
        private String permission;
    }

    @Data
    public static class AccessControlCheckInqResult {
        private List<PrivilegeCheck> privilegeCheckData;
    }

    @Data
    public static class PrivilegeCheck {
        private String subSystemID;
        private List<PrivilegeCategory> privilegeCategoryData;
    }

    @Data
    public static class PrivilegeCategory {
        private String categoryID;
        private String functionID;
        private String permission;
    }

    @Data
    public static class SubComponentStatusReportResult {

    }


    @Data
    public static class PriorityChangeReqResult {
        private String jobID;
        private List<PiorityInfoResult> priorityInfoResultData;
    }

    @Data
    public static class PiorityInfoResult {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierReturnCode;
        private ObjectIdentifier toMachineID;
        private String estimatedStartTime;
        private String estimatedEndTime;
    }

    @Data
    public static class CarrierStatusReportResult {
        public String jobID;
        public String carrierJobID;
        public ObjectIdentifier carrierID;

    }

    @Data
    public static class AlarmReportResult {

    }

    @Data
    public static class EndTimeViolationReportResult {
        public List<EndTimeViolationResult> endTimeViolationResultData;
        private String jobID;
    }

    @Data
    public static class EndTimeViolationResult {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class N2PurgeReportResult {
        private String jobID;
    }

    @Data
    public static class EstimatedTransportTimeInqResult {
        private String estimatedTransportTime;
    }

    @Data
    public static class TransportRouteCheckReqResult {
        private List<RouteCheckResult> routeCheckResultData;
    }

    @Data
    public static class RouteCheckResult {
        private ObjectIdentifier carrierID;
        private String carrierReturnCode;
        private ObjectIdentifier toMachineID;
        private String estimatedStartTime;
        private String estimatedEndTime;

    }

    @Data
    public static class TransportJobStopReqResult {
        private String jobID;
        private List<CarrierJobRc> carrierJobRcData;
    }

    @Data
    public static class TransportJobRemoveReqResult {
        private String jobID;
        private List<Infos.CarrierJob> carrierJobData;
    }

    @Data
    public static class TransportJobPauseReqResult {
        private String jobID;
        private List<CarrierJobRc> carrierJobRcData;
    }

    @Data
    public static class TransportJobAbortReqResult {
        private String jobID;
        private List<CarrierJobRc> carrierJobRcData;
    }

    @Data
    public static class CarrierJobRc {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierReturnCode;
    }

    @Data
    public static class StockerInventoryRptResult {
        private ObjectIdentifier stockerID;
        private List<CarrierInfo> carrierInfos;
    }

    @Data
    public static class CarrierInfo {
        private ObjectIdentifier carrierID;
        private List<ObjectIdentifier> lotID;
        private String zoneID;
        private String shelfType;
        private String stockInTime;
        private Boolean alternateStockerFlag;
        private ObjectIdentifier cassetteID;
    }

    @Data
    public static class StockerForAutoTransferInqResult {
        private List<AvailableStocker> strAvailableStocker;
    }

    @Data
    public static class AvailableStocker {
        public ObjectIdentifier stockerID;
        public String stockerName;
    }

    @Data
    public static class StockerListInqResult {
        private String stockerType;
        private List<StockerInfo> strStockerInfo;
    }

    @Data
    public static class StockerInfo {
        private ObjectIdentifier stockerID;
        private String description;
        private Boolean UTSFlag;
    }

    @Data
    public static class AlertMessageRptResult {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier subSystemID;
        private String systemMessageCode;

    }

    @Data
    public static class CarrierDetailInfoInqResult {
        private ObjectIdentifier cassetteID;            //<i>Carrier ID
        private Infos.CassetteBrInfo cassetteBRInfo;        //<i>Carrier Basic Record Information
        private Infos.CassetteStatusInfo cassetteStatusInfo;    //<i>Carrier Status Information
        private CassettePmInfo cassettePMInfo;        //<i>Carrier Preventive-Maintenance Information
        private DurableLocationInfo cassetteLocationInfo;  //<i>Carrier Location Information
        private DurableOperationInfo strDurableOperationInfo;                //<i>durable Operation Info
        private DurableWipOperationInfo strDurableWipOperationInfo;             //<i>durable WIP Operation Info
    }


    @Data
    public static class DurableWipOperationInfo {
        private ObjectIdentifier responsibleRouteID;         //<i>Responsible Route ID
        private ObjectIdentifier responsibleOperationID;     //<i>Responsible Operation ID
        private String responsibleOperationNumber; //<i>Responsible Operation Number
        private String responsibleOperationName;   //<i>Responsible Operation Name
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class DurableOperationInfo {
        private ObjectIdentifier startBankID;                //<i>Start bank ID
        private ObjectIdentifier routeID;                    //<i>Route ID
        private ObjectIdentifier operationID;                //<i>Operation ID
        private String operationNumber;            //<i>Operation Number
        private String operationName;              //<i>Operation Name
        private ObjectIdentifier stageID;                    //<i>Stage ID
        private String department;                 //<i>Department
        private Boolean mandatoryOperationFlag;     //<i>Mandatory Operation Flag
        private List<LotEquipmentList> strEquipmentList;           //<i>Sequence of eqp List
        private Timestamp queuedTimeStamp;            //<i>Queued TimeStamp
        private Long reworkCount;                    //<i>Rework Count
        private ObjectIdentifier logicalRecipeID;            //<i>Logical Recipe ID
        private Double standardProcessTime;        //<i>Standard Process Time
        private String dueTimeStamp;               //<i>Due TimeStamp
        private ObjectIdentifier bankID;                     //<i>bank ID
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class LotEquipmentList {
        private ObjectIdentifier equipmentID;
        private String equipmentName;
    }

    @Data
    public static class CassetteBrInfo {
        private String description;
        private String cassetteCategory;
        private Boolean usageCheckFlag;
        private int capacity;
        private int nominalSize;
        private String contents;
    }

    @Data
    public static class CassettePmInfo {
        private String runTime;
        private String maximumRunTime;
        private int operationStartCount;
        private int maximumOperationStartCount;
        private int passageTimeFromLastPM;
        private int intervalBetweenPM;
        private String lastMaintenanceTimeStamp;
        private ObjectIdentifier lastMaintenancePerson;
    }

    @Data
    public static class DurableLocationInfo {
        private String instanceName;
        private Boolean currentLocationFlag;
        private String backupState;
    }

    @Data
    public static class StockerInfoInqResult {
        private ObjectIdentifier stockerID;
        private String description;
        private String stockerType;
        private ObjectIdentifier e10Status;
        private ObjectIdentifier stockerStatusCode;
        private String statusName;
        private String statusDescription;
        private String statusChangeTimeStamp;
        private ObjectIdentifier actualE10Status;
        private ObjectIdentifier actualStatusCode;
        private String actualStatusName;
        private String actualStatusDescription;
        private String actualStatusChangeTimeStamp;
        private List<ResourceInfo> resourceInfoData;
        private List<ZoneInfo> zoneInfoData;
        private List<CarrierInStocker> strCarrierInStocker;
        private Boolean UTSFlag;
        private int maxUTSCapacity;
    }

    @Data
    public static class ResourceInfo {
        private String resourceID;
        private String resourceType;
        private Boolean resourceAvailable;
    }

    @Data
    public static class ZoneInfo {
        private String zoneID;
        private String zoneDescription;
        private int standerdCapacityOfZone;
        private int emergencyCapacityOfZone;
    }

    @Data
    public static class CarrierInStocker {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier lotID;
        private Boolean emptyFlag;
        private String carrierCategory;
        private String transferJobStatus;
        private String multiLotType;
        private String resrvUserId;
        private String dispatchReserved;
    }

    @Data
    public static class TransportJobInqResult {
        private String inquiryType;
        private List<Infos.AmhsTransportJobInqData> jobInqData;
    }

    @Data
    public static class TransportJobCreateReqResult {
        private String jobID;
        private List<Infos.AmhsJobCreateResult> jobCreateResultSequenceData;
    }

    @Data
    public static class TransportJobStatusReportResult {
        private String jobID;
    }

    @Data
    public static class CarrierLocationReportResult {
        private String jobID;
        private String carrierJobID;
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class CarrierIDReadReportResult {
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class StockerDetailInfoInqResult {
        private ObjectIdentifier machineID;
        private ObjectIdentifier e10Status;
        private List<Infos.AmhsResourceInfo> resourceInfoData;
        private List<Infos.AmhsZoneInfo> zoneInfoData;
    }

    @Data
    public static class TransportJobCancelReqResult {
        private String jobID;
        private List<Infos.AmhsCarrierJobRc> carrierJobRcData;
    }

    @Data
    public static class DateAndTimeReqResult {
        private String systemTime;
    }


    @Data
    public static class E10StatusReportResult {
        private ObjectIdentifier machineID;
    }

    @Data
    public static class UploadInventoryReq {
        private String shelfType;
        private String zoneID;
        private ObjectIdentifier carrierID;
        private String stockInTime;
        private Boolean alternateStockerFlag;
    }

    @Data
    public static class UploadInventoryReqResult {
        private List<UploadInventoryReq> uploadInventoryReqResults;
    }

    @Data
    public static class CarrierTransferStatusChangeRptResult {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<XferLot> strXferLot;
    }

    @Data
    public static class XferLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private String xPosition;
        private String yPosition;
        private String zPosition;
        private ObjectIdentifier portID;
        private String transferStatus;
    }


    @Data
    public static class ReserveCancelReqResult {
        private List<ReserveCancelLot> strReserveCancelLot;
        private String claimMeme;
    }

    @Data
    public static class ReserveCancelLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier forEquipmentID;
    }

    @Data
    public static class WhereNextInterBayResult {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private String lotState;
        private String productionState;
        private String holdState;
        private String finishedState;
        private String processState;
        private String inventoryState;
        private String transferStatus;
        private ObjectIdentifier current_stockerID;
        private ObjectIdentifier current_equipmentID;
        private ObjectIdentifier transferReserveUserID;
        private List<Infos.WhereNextEqpStatus> whereNextEqpStatus;
    }

    @Data
    public static class WhereNextStockerInqResult {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private String lotState;
        private String productionState;
        private String holdState;
        private String finishedState;
        private String processState;
        private String inventoryState;
        private String transferStatus;
        private ObjectIdentifier current_stockerID;
        private ObjectIdentifier current_equipmentID;
        private ObjectIdentifier transferReserveUserID;
        private List<Infos.WhereNextEqpStatus> whereNextEqpStatus;
        private Object siInfo;
    }

    @Data
    public static class ReticlePodTransferStatusChangeRptResult {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<XferReticlePod> strXferReticlePod;
    }

    @Data
    public static class XferReticlePod {
        private ObjectIdentifier reticlePodID;
        private String transferStatus;
        private String transferStatusChangeTimeStamp;
    }

    @Data
    public static class CarrierInfoChangeReqResult {
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class AllCarrierIDInquiryResult {
        private List<ObjectIdentifier> carrierSequenceData;
    }

    @Data
    public static class CarrierInfoInqResult {
        private ObjectIdentifier carrierID;
        private String zoneType;
    }

    @Data
    public static class DRcassetteXfstatResult {
        String transferStatus;
        String stkID;
        String eqpID;
    }

    @Data
    public static class ReticlePodXferCompRptResult {
        List<Infos.ReticlePodXferJobCompInfo> strReticlePodXferJobCompInfo;
    }

    @Data
    public static class RSPXferStatusChangeRptResult {
        private ObjectIdentifier            stockerID;
        private ObjectIdentifier            equipmentID;
        private List<XferReticlePod>        strXferReticlePod;
        private Object                      siInfo;
    }

    @Data
    public static class ReticlePodStatusInqResult {
        private ObjectIdentifier                 reticlePodID;                       //<i>Reticle Pod ID
        private Infos.ReticlePodBrInfo                 reticlePodBRInfo;                   //<i>Reticle Pod Basic Record Information
        private Infos.ReticlePodStatusInfo             reticlePodStatusInfo;               //<i>Reticle Pod Status Information
        private Infos.ReticlePodPmInfo                 reticlePodPMInfo;                   //<i>Reticle Pod Preventive-Maintenance Information
        private Infos.ReticlePodAdditionalAttribute    strReticlePodAdditionalAttribute;   //<i>Reticle Xfer information
        private DurableLocationInfo              reticlePodLocationInfo;             //<i>Reticle Pod Location Information
        private Object                           siInfo;
    }

    @Data
    public static class ReticlePodStockerInfoInqResult {
        private ObjectIdentifier                        stockerID;                          //<i>Stocker ID
        private String                                  description;                        //<i>Description
        private String                                  stockerType;                        //<i>Stocker Type
        private ObjectIdentifier                        e10Status;                          //<i>E10 Status
        private ObjectIdentifier                        stockerStatusCode;                  //<i>Stocker Status Code
        private String                                  statusName;                         //<i>Status Name
        private String                                  statusDescription;                  //<i>Status Description
        private String                                  statusChangeTimeStamp;              //<i>Status Change Time Stamp
        private ObjectIdentifier                        actualE10Status;                    //<i>Actual E10 Status
        private ObjectIdentifier                        actualStatusCode;                   //<i>Actual Status Code
        private String                                  actualStatusName;                   //<i>Actual Status Name
        private String                                  actualStatusDescription;            //<i>Actual Status Description
        private String                                  actualStatusChangeTimeStamp;        //<i>Actual Status Change Time Stamp
        private List<Infos.AmhsResourceInfo>            resourceInfoData;                   //<i>Sequence of resource information
        private List<Infos.ReticlePodInfoInStocker>     strReticlePodInfoInStocker;         //<i>Sequence of Reticle Pod Infomation in Stocker
        private Object                                  siInfo;                             //<i>Reserved for SI customization
    }

    @Data
    public static class ReticlePodInventoryRptResult {
        private ObjectIdentifier stockerID;                                            //<i>Stocker ID
        private ObjectIdentifier equipmentID;                                          //<i>Equipment ID
        List<Infos.InventoriedReticlePodInfo>  strInventoriedReticlePodInfo;    //<i>Sequence of Inventoried Reticle Pod Information
        private Object siInfo;                                                            //<i>Reserved for SI customization
    }
}
