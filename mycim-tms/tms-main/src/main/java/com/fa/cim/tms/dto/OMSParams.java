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
 * 2018/6/27        ********             miner               create file
 *
 * @author: miner
 * @date: 2018/6/27 13:20
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class OMSParams {
    @Data
    public static class DurableTransferJobStatusRptParams {
        private User user;
        private String claimMemo;
        private String durableType;
        private String operationCategory;
        private String jobID;
        private String jobStatus;
        private String transportType;
        private String eventTime;
        private String claimUserID;
        private List<CarrierJobResult> strCarrierJobResult;
    }

    @Data
    public static class StockerListInqInParams {
        private User user;
        private String stockerType;
        private Boolean availFlag;
    }

    @Data
    public static class StockerForAutoTransferInqParams {
        private User user;
    }

    @Data
    public static class StockerInventoryRptParam {
        private User user;
        private ObjectIdentifier stockerID;
        private List<Infos.InventoryLotInfo> inventoryLotInfos;
        private String claimMemo;
    }

    @Data
    public static class AlertMessageRptParams {
        private User user;
        private String subSystemID;
        private String systemMessageCode;
        private String systemMessageText;
        private Boolean notifyFlag;
        private ObjectIdentifier equipmentID;
        private String equipmentStatus;
        private ObjectIdentifier stockerID;
        private String stockerStatus;
        private ObjectIdentifier AGVID;
        private String AGVStatus;
        private ObjectIdentifier lotID;
        private String lotStatus;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String systemMessageTimeStamp;
        private String claimMemo;
    }

    @Data
    public static class CarrierTransferJobEndRptParams {
        private User user;
        private List<Infos.XferJobComp> strXferJob;
        private String claimMemo;
    }

    @Data
    public static class MoveInCancelReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String claimMemo;

    }

    @Data
    public static class LotCassetteReserveCancelParams {
        private User user;
        private List<Infos.RsvCanLotCarrier> reserveCancelLotCarriers;
        private String claimMemo;
    }


    @Data
    public static class EqpAlarmRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier AGVID;
        private Infos.EquipmentAlarm strEquipmentAlarm;
        private String claimMemo;
    }

    @Data
    public static class StockerInfoInqInParams {
        private User user;
        private ObjectIdentifier machineID;
        private Boolean detailFlag;
    }

    @Data
    public static class CarrierJobResult {
        private String carrierJobID;
        private String carrierJobStatus;
        private ObjectIdentifier carrierID;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private ObjectIdentifier toMachine;
        private ObjectIdentifier toPortID;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
        private String estimatedStartTime;
        private String estimatedEndTime;
    }

    @Data
    public static class CarrierTransferStatusChangeRptParams {
        private User user;
        private ObjectIdentifier carrierID;
        private String xferStatus;   //<i>R/Transfer Status
        //<c>SP_TransState_StationIn          "SI"
        //<c>SP_TransState_StationOut         "SO"
        //<c>SP_TransState_BayIn              "BI"
        //<c>SP_TransState_BayOut             "BO"
        //<c>SP_TransState_ManualIn           "MI"
        //<c>SP_TransState_ManualOut          "MO"
        //<c>SP_TransState_EquipmentIn        "EI"
        //<c>SP_TransState_EquipmentOut       "EO"
        //<c>SP_TransState_ShelfIn            "HI"
        //<c>SP_TransState_ShelfOut           "HO"
        //<c>SP_TransState_IntermediateIn     "II"
        //<c>SP_TransState_IntermediateOut    "IO"
        //<c>SP_TransState_AbnormalIn         "AI"
        //<c>SP_TransState_AbnormalOut        "AO"
        private Boolean manualInFlag;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private String zoneID;
        private String shelfType;
        private Timestamp transferStatusChangeTimeStamp;
        private String claimMemo;
    }

    @Data
    public static class WhereNextStockerInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
    }

    @Data
    public static class StockerStatusChangeRptParams {
        private User user;
        private ObjectIdentifier stockerID;          //<i>R/stocker ID
        private ObjectIdentifier stockerStatusCode;  //<i>R/stocker Status CimCode
        private String claimMemo;
    }

    @Data
    public static class ReticlePodTransferStatusChangeRptParams {
        private User user;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.XferReticlePod> xferReticlePodList;
        private String claimMemo;
    }

    @Data
    public static class CarrierDetailInfoInqParams {
        private User user;
        private ObjectIdentifier cassetteID;
        private Boolean durableOperationInfoFlag;
        private Boolean durableWipOperationInfoFlag;

    }

    @Data
    public static class BranchCancelReqParams {
        private User user;
        private ObjectIdentifier lotID;             //<i>R/lot ID
        private ObjectIdentifier currentRouteID;    //<i>R/Current Route ID
        private String currentOperationNumber;       //<i>R/Current Operation Number
        private String claimMemo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/17 10:18:59
     */
    @Data
    public static class ReticleListInqParams {
        private User user;
        private ObjectIdentifier reticleGroupID;
        private String reticlePartNumber;
        private ObjectIdentifier reticleID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private String reticleStatus;
        private ObjectIdentifier durableSubStatus;
        private String flowStatus;
        private long maxRetrieveCount;
        private String FPCCategory;
        private String whiteDefSearchCriteria;
        private ObjectIdentifier bankID;
        private String siInfo;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/12 15:55:39
     */
    @Data
    public static class CarrierLoadingForIBRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;
        private String loadPurposeType;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/14 13:29:50
     */
    @Data
    public static class CarrierUnloadingForIBRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;
        private String claimMemo;
    }


    @Data
    public static class BankMoveReqParams {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private ObjectIdentifier bankID;
        private String claimMemo;
    }

    @Data
    public static class LoginCheckInqParams {
        private User user;
        private String subSystemID;
        private String categoryID;
        private Boolean productRequestFlag;         // accessable product list request list
        private Boolean recipeRequestFlag;          // accessasble recipe list request flag
    }

    @Data
    public static class ReticlePodXferJobCompRptParams {
        private User user;
        private List<Infos.ReticlePodXferJobCompInfo> strReticlePodXferJobCompInfo;
        private String claimMemo;
    }

    @Data
    public static class RSPXferStatusChangeRptParams {
        private User user;
        private ObjectIdentifier reticlePodID;
        private String xferStatus;
        private Boolean manualInFlag;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private String claimMemo;
    }

    @Data
    public static class ReticlePodStockerInfoInqParams {
        private User user;
        private ObjectIdentifier stockerID;
    }

    @Data
    public static class ReticlePodStatusInqParams {
        private User user;
        private ObjectIdentifier        reticlePodID;          //<i>R/Reticle Pod ID  //D6000025
        private Boolean                 durableOperationInfoFlag;   //<i>R/DurableOperationInfoFlag    //DSN000096126
        private Boolean                 durableWipOperationInfoFlag; //<i>R/DurableWipOperationInfoFlag //DSN000096126
    }

    @Data
    public static class ReticlePodInventoryRptParams {
        private User user;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo;
        private String claimMemo;
    }
}