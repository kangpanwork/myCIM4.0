package com.fa.cim.tms.status.recovery.pojo;

import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.tms.status.recovery.dto.User;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/27        ********             miner               create file
 *
 * @author: miner
 * @date: 2018/6/27 15:02
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Infos {
    @Data
    public static class SubComponent {
        private String resourceID;
        private String resourceType;
        private String resourceStatus;
        private Boolean resourceAvailable;
    }

    @Data
    public static class EquipmentAlarm {
        private String alarmCode;
        private String alarmID;
        private String alarmCategory;
        private String alarmText;
        private String alarmTimeStamp;
        private Object siInfo;
    }

    @Data
    public static class PriorityInfo {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
    }

    @Data
    public static class CarrierJob {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class EstimatedTarnsportTimeInq {
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier toMachineID;
    }

    @Data
    public static class InventoryLotInfo {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier lotID;
        private String carrierStatus;
        private String transferJobStatus;
    }

    @Data
    public static class LocalTransportJob {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String expectedEndTime;
        private String priority;

    }

    @Data
    public static class AmhsTransportJobInqData {
        private String jobID;
        private String transportType;
        private String jobStatus;
        private List<TransferJobInfo> carrierJobInqInfo;
    }

    @Data
    public static class TimeStamp {
        private Timestamp reportTimeStamp;
        private Double reportShopDate;
        private Object reserve;
    }

    @Data
    public static class ObjCommon {
        private String transactionID;
        private User user;
        private TimeStamp timeStamp;
        private Object reserve;
    }

    @Data
    public static class TransportJobCreateReqResult {
        private String lotId;
        private String returnCode;

    }


    @Data
    public static class AmhsJobCreateArray {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private List<AmhsToDestination> toMachine;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
    }


    @Data
    public static class AmhsJobCreateResult {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierReturnCode;
        private ObjectIdentifier toMachineID;
        private String estimatedStartTime;
        private String estimatedEndTime;
    }

    @Data
    public static class AmhsTranJobCreateReq {
        private String jobID;
        private Boolean rerouteFlag;
        private String transportType;
        private List<AmhsJobCreateArray> jobCreateData;
    }


    @Data
    public static class AmhsToDestination {
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
    }

    @Data
    public static class RsvCanLotCarrier {
        private ObjectIdentifier lotID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier forEquipmentID;
    }

    @Data
    public static class JobStatusReportArray {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierjobStatus;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String originator;
        private Boolean carrierJobRemoveFlag;
    }

    @Data
    public static class xmTranJobDeleteReq {
        private String jobID;
        private carrierJobData[] carrierJobData;
    }

    @Data
    public static class carrierJobData {
        private String carrierjobID;
        private String carrierID;
    }

    @Data
    public static class pptCarrierIDReadReport {
        private ObjectIdentifier carrierID;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private ObjectIdentifier portType;
        private String inOutType;
        private Boolean jobRequestFlag;
    }

    @Data
    public static class XferReticlePod {
        private ObjectIdentifier reticlePodID;
        private String transferStatus;
        private String transferStatusChangeTimeStamp;
    }

    @Data
    public static class WhereNextEqpStatus {
        private ObjectIdentifier equipmentID;
        private String equipmentMode;
        private ObjectIdentifier equipmentStatusCode;
        private ObjectIdentifier E10Status;
        private String equipmentStatusDescription;
        private Boolean equipmentAvailableFlag;
        private List<EqpStockerStatus> eqpStockerStatus;
        private List<EntityInhibitAttributes> entityInhibitions;
        private Object siInfo;
    }

    @Data
    public static class EqpStockerStatus {
        private ObjectIdentifier stockerID;        //<i>stocker ID
        private String stockerType;      //<i>stocker Type
        //<c>SP_Stocker_Type_Auto           "Auto"
        //<c>SP_Stocker_Type_Interm         "Interm"
        //<c>SP_Stocker_Type_Shelf          "Shelf"
        //<c>SP_Stocker_Type_Reticle        "Reticle"
        //<c>SP_Stocker_Type_Fixture        "Fixture"
        //<c>SP_Stocker_Type_InterBay       "Inter Bay"
        //<c>SP_Stocker_Type_IntraBay       "Intra Bay"
        //<c>SP_Stocker_Type_ReticleShelf   "ReticleShelf"
        //<c>SP_Stocker_Type_ReticlePod     "reticlepod"
        //<c>SP_Stocker_Type_BareReticle    "BareReticle"
        private ObjectIdentifier stockerStatus;       //<i>stocker State
        private String stockerPriority;    //<i>stocker Priority
        private String e10Status;          //<i>stocker E10 status         //D8000028
        private Boolean utsFlag;            //<i>UTS Flag                   //D8000028
        private long maxUTSCapacity;     //<i>Max UTS Capacity           //D8000028
        private String siInfo;             //<i>Reserved for SI customization
    }

    @Data
    public static class EntityInhibitAttributes {
        private EntityIdentifier[] entities;
        private String[] subLotTypes;
        private String startTimeStamp;
        private String endTimeStamp;
        private String reasonCode;
        private String reasonDesc;
        private String memo;
        private ObjectIdentifier ownerID;
        private String claimedTimeStamp;
    }

    @Data
    public static class EntityIdentifier {
        private String className;
        private ObjectIdentifier objectID;
        private String attrib;
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
    public static class CassetteStatusInfo {
        private String cassetteStatus;                     //<i>Carrier Status
        private String transferStatus;                     //<i>Transfer Status
        private ObjectIdentifier transferReserveUserID;              //<i>Transfer Reserve User ID
        private String transferReserveUserReference;
        private ObjectIdentifier equipmentID;                        //<i>eqp ID
        private String equipmentReference;
        private ObjectIdentifier stockerID;                          //<i>stocker ID
        private String stockerReference;
        private Boolean emptyFlag;                          //<i>Empty Flag
        private String multiLotType;                       //<i>Multi lot Type
        private String zoneType;                           //<i>Zone Type
        private String priority;                           //<i>Priority
        private List<ContainedLotInfo> strContainedLotInfo;                //<i>Sequence of Contained lot Information
        private String lastClaimedTimeStamp;               //<i>Last Claimed Time Stamp
        private ObjectIdentifier lastClaimedPerson;                  //<i>Last Claimed person
        private String lastClaimedPersonReference;
        private Boolean sorterJobExistFlag;                 //<i>Sorter Job Existence Flag
        private Boolean inPostProcessFlagOfCassettea;        //<i>InPostProcessFlag of cassette
        private ObjectIdentifier slmRsvEquipmentID;                  //<i>Reserved eqp for SLM
        private String slmRsvEquipmentReference;
        private String interFabXferState;                  //<i>interFab Xfer State

        private ObjectIdentifier controlJobID;
        private ObjectIdentifier durableControlJobID;
        private ObjectIdentifier durableSubStatus;
        private String durableSubStatusName;
        private String durableSubStatusDescription;
        private Boolean availableForDurableFlag;
        private Boolean durableSTBFlag;
        private Map<String, String> durableStatusList;
        private String processLagTime;

        private Object siInfo;                             //<i>Reserved for SI customization
    }


    @Data
    public static class ContainedLotInfo {
        private ObjectIdentifier lotID;    //<i>lot ID
        private Boolean autoDispatchDisableFlag;       //<i>Auto dispatch Disable Flag
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class DurableLocationInfo {
        private String instanceName;
        private Boolean currentLocationFlag;
    }

    @Data
    public static class AmhsResourceInfo {
        private String resourceID;
        private String resourceType;
        private Boolean resourceAvailable;
    }

    @Data
    public static class AmhsZoneInfo {
        private String zoneID;
        private String zoneDescription;
        private int standerdCapacityOfZone;
        private int emergencyCapacityOfZone;
    }

    @Data
    public static class AmhsStockerDetailInfoInq {
        private ObjectIdentifier machineID;
    }

    @Data
    public static class AmhsCarrierJobRc {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierReturnCode;
    }

    @Data
    public static class AmhsCarrierJob {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
    }

    @Data
    public static class AmhsTranJobCancelReq {
        private String jobID;
        private List<AmhsCarrierJob> carrierJobData;
    }

    @Data
    public static class XferJob {
        private String jobID;
        private String carrierJobID;
        private String transportType;
        private ObjectIdentifier carrierID;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private List<ToDestinationStr> toMachine;
        private String expectedStartTime;
        private String expectedEndTime;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
    }

    @Data
    public static class ToDestinationStr {
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
    }


    @Data
    public static class XferJobComp {
        private ObjectIdentifier carrierID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String transferJobStatus;
    }

    @Data
    public static class AmhsTransportJobInq {
        private String inquiryType;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier fromMachineID;
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
    public static class TransferJobInfo {
        private ObjectIdentifier carrierID;
        private String jobID;
        private String carrierJobID;
        private String transportType;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String expectedStartTime;
        private String expectedEndTime;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
        private String jobStatus;
        private String carrierJobStatus;
        private String timeStamp;
    }

    @Data
    public static class TransferJobDeleteInfo {
        private String jobID;
        private List<CarrierJobInfo> carrierJobData;
        private Object siInfo;
    }

    @Data
    public static class CarrierJobInfo {
        private String carrierJobID;
        private String carrierJobStatus;
        private ObjectIdentifier carrierID;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private Object siInfo;
    }

    @Data
    public static class ReticlePodXferJobCompInfo {
        private ObjectIdentifier reticlePodID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String transferJobStatus;
        private Object siInfo;

    }

    @Data
    public static class ReticlePodBrInfo {
        private String description;           //<i>Description
        private String reticlePodCategory;    //<i>Reticle Pod Category
        private Long capacity;              //<i>Capacity                                                 //D8000084
        private Object siInfo;                //<i>Reserved for SI customization
    }

    @Data
    public static class ReticlePodStatusInfo {
        private String reticlePodStatus;                       //<i>Reticle Pod Status
        private ObjectIdentifier durableSubStatus;                       //<i>Durable Sub Status                 //DSN000101569
        private String durableSubStatusName;                   //<i>Durable Sub Status Name            //DSN000101569
        private String durableSubStatusDescription;            //<i>Durable Sub Status Description     //DSN000101569
        private Boolean availableForDurableFlag;                //<i>Process Available for Durable Flag //DSN000101569
        private String processLagTime;                         //<i>Proccess Lag Time                  //DSN000101569
        private String transferStatus;                         //<i>Transfer Status
        private ObjectIdentifier equipmentID;                            //<i>Equipment ID
        private ObjectIdentifier stockerID;                              //<i>Stocker ID
        private Boolean emptyFlag;                              //<i>Empty Flag
        private List<ContainedReticleInfo> strContainedReticleInfo;                //<i>Sequence of Contained Reticle Information
        private ObjectIdentifier durableControlJobID;                    //<i>Durable Control Job ID
        private Boolean durableSTBFlag;                         //<i>Durable STB Flag//DSN000096126
        private String lastClaimedTimeStamp;                   //<i>Last Claimed Time Stamp
        private ObjectIdentifier lastClaimedPerson;                      //<i>Last Claimed Person
        private ObjectIdentifier transferReserveUserID;                  //<i>Transfer Reserve UserID
        private Boolean inPostProcessFlagOfReticlePod;          //<i>In PostProcess Flag Of ReticlePod
        private List<HashedInfo> strDurableStatusList;                   //<i>The following status are set.
        private Object siInfo;                                 //<i>Reserved for SI customization
    }

    @Data
    public static class ReticlePodPmInfo {
        private Long passageTimeFromLastPM;       //<i>Passage Time From Last PM      //minutes
        private Long intervalBetweenPM;           //<i>Interval Between PM            //minutes
        private String lastMaintenanceTimeStamp;    //<i>Last Maintenance Time Stamp    //TimeStamp
        private ObjectIdentifier lastMaintenancePerson;       //<i>Last Maintenance Person
        private Object siInfo;                      //<i>Reserved for SI customization

    }

    @Data
    public static class ContainedReticleInfo {
        private Long slotNo;              //<i>Slot No
        private ObjectIdentifier reticleID;           //<i>Reticle ID
        private ReticleBrInfo strReticleBrInfo;    //<i>Reticle Basic Record Information
        private Object siInfo;              //<i>Reserved for SI customization
    }

    @Data
    public static class HashedInfo {
        private String hashKey;                     //<i>Hash Key
        private String hashData;                    //<i>Hash Data
        private Object siInfo;                      //<i>Reserved for SI customization
    }

    @Data
    public static class ReticleBrInfo {
        private String description;                            //<i>Description
        private String reticlePartNumber;                      //<i>Reticle Part Number
        private String reticleSerialNumber;                    //<i>Reticle Serial Number
        private String supplierName;                           //<i>Supplier Name
        private Boolean usageCheckFlag;                         //<i>Usage Check Flag
        private ObjectIdentifier reticleGroupID;                      //<i>Reticle Group ID
        private String reticleGroupDescription;    //<i>Reticle Group Description
        private Object siInfo;                                              //<i>Reserved for SI customization
    }

    @Data
    public static class ReticlePodAdditionalAttribute {
        private Boolean transferReservedFlag;      //<i>Transfer Reserved Flag
        private ObjectIdentifier transferDestEquipmentID;   //<i>Transfer Destination Equipment ID
        private List<ContainedReticleInfo> strReservedReticleInfo;    //<i>Sequence of Contained Reticle Information
        private ObjectIdentifier transferReservedUserID;    //<i>Transfer Reserved User ID
        private Object siInfo;                    //<i>Reserved for SI customization
    }

    @Data
    public static class ReticlePodInfoInStocker {
        private ObjectIdentifier reticlePodID;               //<i>Reticle Pod ID
        private ObjectIdentifier reticlePodCategoryID;       //<i>Reticle Pod Category ID
        private String reticlePodStatus;           //<i>Reticle Pod Status
        private Boolean emptyFlag;                  //<i>Empty Flag
        private ObjectIdentifier reticleID;                  //<i>Reticle ID
        private String reticleSubStatus;           //<i>Reticle Sub Status
        private Boolean reticleInspectionNGFlg;     //<i>Reticle Inspection NG Flag
        private String transferStatus;             //<i>Transfer Status
        private Boolean transferReserveFlag;        //<i>Transfer Reserve Flag
        private ObjectIdentifier transferReserveUserID;      //<i>Transfer Reserve User ID
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class MachineTypeGetDR {
        private Boolean bStorageMachineFlag;
        private ObjectIdentifier equipmentID;
        private String equipmentType;
        private ObjectIdentifier stockerID;
        private String stockerType;
        private ObjectIdentifier areaID;
        private Object siInfo;
    }

    @Data
    public static class CarrierCurrentLocationGetDR {
        private ObjectIdentifier carrierID;
        private ObjectIdentifier machineID;
        private String transferStatus;
        private Object siInfo;
    }

    @Data
    public static class ReticlePodXferStat {
        private String transferStatus;
        private String eqpID;
        private String stkID;
        private Object siInfo;
    }

    @Data
    public static class InventoryReticlePodInfo {
        private ObjectIdentifier reticlePodID;         //<i>Reticle Pod ID
        private String transferJobStatus;    //<i>Transfer Job Status
        private Object siInfo;               //<i>Reserved for SI customization
    }

    @Data
    public static class InventoriedReticlePodInfo {
        private ObjectIdentifier reticlePodID;    //<i>Reticle Pod ID
        private List<ObjectIdentifier> reticleID;       //<i>Sequence of Reticle ID
        private String returnCode;      //<i>Return Code
        private Object siInfo;          //<i>Reserved for SI customization
    }

    @Data
    public static class CarrierQueGetData {
        private String timeStamp;
        private ObjectIdentifier carrierID;
        private String jobID;
        private String carrierJobID;
        private String eventType;
        private String eventStatus;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private String transferStatus;
        private Object siInfo;
    }

    @Data
    public static class XferJobEventQueData {
        private String operationCategory;
        private ObjectIdentifier carrierID;
        private String jobID;
        private String carrierJobID;
        private String transportType;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String expectedStartTime;
        private String expectedEndTime;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
        private String jobStatus;
        private String carrierJobStatus;
        private String timestamp;
        private String claimUserID;
        private Object siInfo;
    }

    @Data
    public static class StatQueGetData {
        private String timeStamp;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier stockerStatus;
        private Object siInfo;
    }

    @Data
    public static class IntegrationHeader{
        private IntegrationMsgHead msgHead;
        private Object msgBody;
    }

    public static final AtomicInteger counter = new AtomicInteger(1);

    @Data
    public static class IntegrationMsgHead {
        private String traceId; //Original datetime:20191218141236333 "EAP.APHO01@hostname.pid#datetime.txId"
        private String rqstId;//app ID of requester "EAP@hostname.pid"
        private String srvName;//app name of service_provider  "RMS"
        private String rqstAddr;//requester's address  "EAP.ADOX01"
        private String service;//service name "CheckRecipeBody"
        private String msgType;//R,r,P. R->Request,r->reply,P->Publish "r"
        private String locale;// Client locale. en,cn
        private Integer txId;// 1,2,3,4,5…9999, loop in the range [1-9999]
        private String rqstTime;//Timestamp  "20201109101049000" yyyyMMddHHmmssSSS
        private Integer retCode;//Return code. Value is "0" when primary message
        private String retMsg;//Return message. Value is empty when primary message. "RMS-0001: recipe {PPID} on {TOOL_ID} parameter {param} value out of spec"

        public IntegrationMsgHead() {
        }

        public IntegrationMsgHead(boolean isInitial) {
            if (isInitial) {
                init();
            }
        }

        private void init(){
            this.rqstId = rqstId;
            this.srvName = "TMS_STATUS_RECOVERY";
            this.rqstAddr = rqstAddr;
            this.service = service;
            this.msgType = "R";
            this.locale = "en";
            this.txId = counter.get() >= 9999 ? counter.getAndSet(1) : counter.getAndIncrement();
            this.rqstTime = CimDateUtils.convert("yyyyMMddHHmmssSSS", new Date());
            this.retCode = retCode;
            this.retMsg = retMsg;
            this.traceId = this.getSrvName() +"@"+this.getRqstTime();
        }
    }
}
