package com.fa.cim.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.sorter.Info;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
 * 2018/6/28        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/28 9:50
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Inputs {

    @Data
    public static class SorterSorterJobCreateIn {
        private List<Infos.SorterComponentJobListAttributes> strSorterComponentJobListAttributesSeq;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private boolean waferIDReadFlag;
        private String sorterJobCategory;
        private String operationMode;
        private String ctrljobId;
    }

    @Data
    public static class SortJobPostActInsertDRIn {
        private Info.SortJobPostAct postAct;
    }

    @Data
    public static class DurableXferJobStatusChangeEventMakeIn{
        private String durableType;
        private String operationCategory;
        private String jobID;
        private String jobStatus;
        private String transportType;
        private String eventTime;
        private List<Infos.CarrierJobResult> strCarrierJobResult;
        private String claimUserID;
        private String claimMemo;
    }

    @Data
    public static class ObjAdvancedLockForEquipmentResourceIn {
        private ObjectIdentifier equipmentID;
        private String className;
        private ObjectIdentifier objectID;
        private Long objectLockType;
        private String bufferResourceName;
        private Long bufferResourceLockType;
    }

    @Data
    public static class ObjAdvanceLockIn {
        private ObjectIdentifier objectID;
        private String className;
        private String objectType;
        private Long lockType;
        private List<String> keyList;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization

        public ObjAdvanceLockIn(){}

        public ObjAdvanceLockIn(ObjectIdentifier objectID, Long lockType, List<String> keyList){
            this.objectID = objectID;
            this.lockType = lockType;
            this.keyList = keyList;
        }

        public ObjAdvanceLockIn(ObjectIdentifier objectID, String className,
                                String objectType, Long lockType, List<String> keyList) {
            this.objectID = objectID;
            this.className = className;
            this.lockType = lockType;
            this.objectType = objectType;
            this.keyList = keyList;
        }

    }

    @Data
    public static class ObjAutoDispatchControlInfoGetDRIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Object reserve;
    }

    @Data
    public static class ObjEqpMonitorListGetDRIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier chamberID;
        private ObjectIdentifier eqpMonitorID;
    }

    @Data
    public static class ObjBondingGroupInfoByEqpGetDRIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private Boolean bondingMapInfoFlag;
    }

    @Data
    public static class ObjCassetteInPostProcessFlagGetIn {
        private ObjectIdentifier cassetteID;
        private Object reserve;
    }

    @Data
    public static class ObjCassetteListGetDRIn {
        private String cassetteCategory;
        private boolean emptyFlag;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier cassetteID;
        private String cassetteStatus;
        private ObjectIdentifier durablesSubStatus;
        private String flowStatus;
        private long maxRetrieveCount;
        private boolean sorterJobCreationCheckFlag;
        private String interFabTransferState;
        private ObjectIdentifier bankID;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization
        private String cassetteTye; //Carrier Type
        private String usageType;

        private SearchCondition searchCondition;   // add by bear
        private int page;
        private int size;
        private Boolean needTransferState; // need EI transfer stat    default need EI


        public ObjCassetteListGetDRIn(Params.CarrierListInqParams carrierListInqParams) {
            if (null != carrierListInqParams) {
                this.cassetteTye = carrierListInqParams.getCarrierType();
                this.cassetteCategory = carrierListInqParams.getCassetteCategory();
                this.emptyFlag = carrierListInqParams.isEmptyFlag();
                this.stockerID = carrierListInqParams.getStockerID();
                this.cassetteID = carrierListInqParams.getCassetteID();
                this.cassetteStatus = carrierListInqParams.getCassetteStatus();
                this.durablesSubStatus = carrierListInqParams.getDurablesSubStatus();
                this.flowStatus = carrierListInqParams.getFlowStatus();
                this.usageType = carrierListInqParams.getUsageType();
                if (0 != carrierListInqParams.getMaxRetrieveCount()) {
                    this.maxRetrieveCount = carrierListInqParams.getMaxRetrieveCount();
                }

                if (null != carrierListInqParams.getSearchCondition()) {
                    SearchCondition searchCondition = carrierListInqParams.getSearchCondition();
                    if (null != searchCondition.getSize()) {
                        this.size = carrierListInqParams.getSearchCondition().getSize();
                    }
                    if (null != searchCondition.getPage()) {
                        this.page = searchCondition.getPage();
                    }
                } else {
                    this.page = 0;
                }

                this.sorterJobCreationCheckFlag = carrierListInqParams.isSorterJobCreationCheckFlag();
                this.interFabTransferState = carrierListInqParams.getInterFabTransferState();
                this.bankID = carrierListInqParams.getBankID();
                this.searchCondition = carrierListInqParams.getSearchCondition();
                this.needTransferState = carrierListInqParams.getNeedTransferState();
            }
        }
    }

    @Data
    public static class ObjControlJobProcessOperationListGetDRIn {
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ObjDurableCassetteCategoryCheckForContaminationControlIn {
        private ObjectIdentifier cassetteId;
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier portId;
    }

    @Data
    public static class ObjDurableCheckConditionForOperationIn {
        private String operation;
        private ObjectIdentifier equipmentId;
        private String portGroupId;
        private String durableCategory;
        private List<Infos.StartDurable> startDurables;
        private Infos.DurableStartRecipe durableStartRecipe;
    }

    @Data
    public static class ObjDurableStatusCheckForOperationIn {
        private String operation;
        private ObjectIdentifier durableId;
        private String durableCategory;
    }

    @Data
    public static class ObjEquipmentAndPortStateCheckForDurableOperationIn {
        private String operation;
        private ObjectIdentifier equipmentId;
        private String portGroupId;
        private String durableCategory;
        private List<Infos.StartDurable> startDurables;
    }

    @Data
    public static class ObjEquipmentContainerInfoGetIn {
        private ObjectIdentifier equipmentID;
        private Object reserve;
    }

    @Data
    public static class ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn {
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        List<Infos.StartCassette> startCassetteList;
    }

    @Data
    public static class ObjEquipmentRecipeGetListForRecipeBodyManagementIn {
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> startCassetteList;
    }

    @Data
    public static class ObjEquipmentPortGroupIDGetIn {
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier portId;
    }

    @Data
    public static class ObjLockModeIn {
        private ObjectIdentifier objectID;
        private String className;
        private String functionCategory;
        private Boolean userDataUpdateFlag;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization

        public ObjLockModeIn(){}

        public ObjLockModeIn(ObjectIdentifier objectID, String className, String functionCategory, Boolean userDataUpdateFlag) {
            this.objectID = objectID;
            this.className = className;
            this.functionCategory = functionCategory;
            this.userDataUpdateFlag = userDataUpdateFlag;
        }
    }

    @Data
    public static class ObjLotBondingGroupIDGetDRIn {
        private ObjectIdentifier lotID;
        private Object reserve;              //<i>Reserved for SI customization

        public ObjLotBondingGroupIDGetDRIn(ObjectIdentifier lotID) {
            this.lotID = lotID;
        }
    }

    @Data
    public static class ObjLotInPostProcessFlagGetIn {
        private ObjectIdentifier lotID;
        private Object reserve;             // Reserved for myCIM4.0 customization

        public ObjLotInPostProcessFlagGetIn(ObjectIdentifier lotID) {
            this.lotID = lotID;
        }
    }

    @Data
    public static class ObjLotCheckFlowBatchConditionForReworkIn {
        private Infos.ReworkReq reworkReq;
        private Object reserve;
    }

    @Data
    public static class ObjLotInPostProcessFlagSetIn {
        private ObjectIdentifier lotID;
        private Boolean inPostProcessFlag;
    }

    @Data
    public static class ObjLotPreparationCancelCheckIn extends ObjLotPreparationCancelIn {
        private List<Infos.PreparationCancelledWaferInfo> preparationCancelledWaferInfoList;
    }

    @Data
    public static class ObjLotPreparationCancelIn {
        private ObjectIdentifier preparationCancelledLoID;
        private ObjectIdentifier bankID;
        private List<Infos.NewVendorLotInfo> newVendorLotInfoList;
    }

    @Data
    public static class ObjLotSTBCancelCheckIn {
        private ObjectIdentifier stbCancelledLotID;
        private ObjectIdentifier startBankID;
        private List<Infos.STBCancelInfo> stbCancelInfoList;
        private Object reserve;             // Reserved for myCIM4.0 customization

        public ObjLotSTBCancelCheckIn(ObjectIdentifier stbCancelledLotID, ObjectIdentifier startBankID, List<Infos.STBCancelInfo> stbCancelInfoList) {
            this.stbCancelledLotID = stbCancelledLotID;
            this.startBankID = startBankID;
            this.stbCancelInfoList = stbCancelInfoList;
        }
    }

    @Data
    public static class ObjLotSTBCancelIn {
        private ObjectIdentifier stbCancelledLotID;
        private ObjectIdentifier startBankID;
        private List<Infos.STBCancelInfo> stbCancelInfoList;
        private Object reserve;             // Reserved for myCIM4.0 customization

        public ObjLotSTBCancelIn(ObjectIdentifier stbCancelledLotID, ObjectIdentifier startBankID, List<Infos.STBCancelInfo> stbCancelInfoList) {
            this.stbCancelledLotID = stbCancelledLotID;
            this.startBankID = startBankID;
            this.stbCancelInfoList = stbCancelInfoList;
        }
    }

    @Data
    public static class ObjLotSTBCancelInfoIn {
        private ObjectIdentifier stbCancelledLotID;
        private Object reserve;             // Reserved for myCIM4.0 customization

        public ObjLotSTBCancelInfoIn(ObjectIdentifier stbCancelledLotID) {
            this.stbCancelledLotID = stbCancelledLotID;
        }
    }

    //todo:PANDA可删除，待定
    @Data
    public static class ObjLotWafersGetIn {
        private ObjectIdentifier lotID;
        private Boolean scrapCheckFlag;
        private Object reserve;

        public ObjLotWafersGetIn(){}

        public ObjLotWafersGetIn(ObjectIdentifier lotID, Boolean scrapCheckFlag) {
            this.lotID = lotID;
            this.scrapCheckFlag = scrapCheckFlag;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ObjLotWaferIDListGetDRIn {
        private ObjectIdentifier lotID;
        private Boolean scrapCheckFlag;
    }

    @Data
    public static class ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier logicalRecipeID;
        private ObjectIdentifier machineRecipeID;
        private boolean inhibitCheckFlag;
    }

    @Data
    public static class ObjPostProcessQueueIn {
        private String key;
        private Long prevSequenceNumber;
        private List<Long> sequenceNumberList;
        private String status;
        private Long syncFlag;
        private String targetType;
        private String objectID;
    }

    @Data
    public static class ObjPostProcessQueueListIn {
        private String key;
        private Long sequenceNumber;
        private String watchdogName;
        private String postProcID;
        private Long syncFlag;
        private String txID;
        private String targetType;
        private Infos.PostProcessTargetObject postProcessTargetObject;
        private String status;
        private Long passedTime;
        private String claimUserID;
        private String startCreateTimeStamp;
        private String endCreateTimeStamp;
        private String startUpdateTimeStamp;
        private String endUpdateTimeStamp;
        private Long maxCount;
        private Boolean committedReadFlag;
    }

    @Data
    public static class ObjProductRequestReleaseBySTBCancelIn {
        private ObjectIdentifier productID;
        private ObjectIdentifier bankID;
        private String lotType;
        private String subLotType;
        private int productCount;
        private Object reserve;              //<i>Reserved for SI customization

        public ObjProductRequestReleaseBySTBCancelIn(ObjectIdentifier productID, ObjectIdentifier bankID, String lotType, String subLotType, int productCount) {
            this.productID = productID;
            this.bankID = bankID;
            this.lotType = lotType;
            this.subLotType = subLotType;
            this.productCount = productCount;
        }
    }

    @Data
    public static class ObjSlmCheckConditionForOperationIn {
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
        private List<Infos.MaterialOutSpec> materialOutSpecList;
        private String operation;
    }

    @Data
    public static class ObjReticleLastUsedTimeSetIn {
        private ObjectIdentifier reticleId;
        private String lastUsedTimeStamp;
    }

    @Data
    public static class ObjSorterJobListGetDRIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier createUser;
        private ObjectIdentifier sorterJob;
    }

    @Data
    public static class ObjValidSorterJobIn {
        private String classification;
        private ObjectIdentifier objectID;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization
    }

    @Data
    public static class ObjWaferSorterJobCheckForOperation {
        private Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute;
        private List<ObjectIdentifier> cassetteIDList;
        private List<ObjectIdentifier> lotIDList;
        private String operation;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization
    }

    @Data
    public static class PostProcessAdditionalInfoDeleteDRIn {
        private String dKey;
        private Object reserve;
    }

    @Data
    public static class PostProcessAdditionalInfoGetDRIn {
        private String dKey;
        private Long sequenceNumber;
    }

    @Data
    public static class ObjMessageQueuePutIn {
        private String equipmentID;
        private List<Infos.PortOperationMode> portOperationModeList;
        private String equipmentStatusCode;
        private String lotID;
        private String lotProcessState;
        private String lotHoldState;
        private String cassetteID;
        private String cassetteTransferState;
        private Boolean cassetteTransferReserveFlag;
        private Boolean cassetteDispatchReserveFlag;
        private String durableID;
        private String durableTransferState;

    }

    @Data
    public static class ObjEquipmentLotsWhatNextDRIn {
        private ObjectIdentifier equipmentID;
        private String selectCriteria;
        private ObjectIdentifier eqpMonitorID;
    }

    @Data
    public static class ObjProcessGetTargetOperationIn {
        private Boolean locateDirection;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
    }

    @Data
    public static class ObjAdvancedObjectLockIn {
        private ObjectIdentifier objectID;
        private String className;
        private String objectType;
        private Long lockType;
        private List<String> keySeq;
    }

    @Data
    public static class RemoveCassette {
        private ObjectIdentifier cassetteID;    //<i>Carrier ID
        private List<ObjectIdentifier> lotID;         //<i>Sequence of lot ID
    }

    @Data
    public static class ObjProcessCheckInterFabXferPlanSkipIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier currentRouteID;
        private String currentOpeNo;
        private ObjectIdentifier jumpingRouteID;
        private String jumpingOpeNo;
    }

    @Data
    public static class OldCurrentPOData {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Integer operationPassCount;
        private String objrefPOS;
        private String objrefMainPF;
        private String objrefModulePOS;
        private String reworkOutOperation;    //D7000012
    }

    @Data
    public static class ObjProcessOperationProcessRefListForLotIn {
        private boolean searchDirection;
        private boolean posSearchFlag;
        private int searchCount;
        private ObjectIdentifier searchRouteID;
        private String searchOperationNumber;
        private boolean currentFlag;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ObjDurableSubStateDBInfoGetDRIn {
        private String durableSubStatus;
        private Boolean availableSubLotTypeInfoFlag;
        private Boolean nextTransitionDurableSubStatusInfoFlag;
        private String siInfo;
    }

    /**
     * description:
     * objSchdlChangeReservation_GetListDR_in__110_struct
     *
     * @author PlayBoy
     * @date 2018/8/10
     */
    @Data
    public static class ObjScheduleChangeReservationGetListIn {
        private String eventID;
        private String objectID;
        private String objectType;
        private String targetRouteID;
        private String targetOperationNumber;
        private String productID;
        private String routeID;
        private String operationNumber;
        private String subLotType;
        private String startDate;
        private String endDate;
        private String status;
        private Long lotInfoChangeFlag;
    }

    @Data
    public static class ObjProcessOperationListForLotIn {
        private Boolean searchDirectionFlag;
        private Boolean posSearchFlag;
        private Integer searchCount;
        private ObjectIdentifier searchRouteID;
        private String searchOperationNumber;
        private Boolean currentFlag;
        private ObjectIdentifier lotID;
        private Object reserve;
    }

    @Data
    public static class ObjProcessOperationRefListForLotIn {
        private Boolean searchDirectionFlag;
        private Boolean posSearchFlag;
        private long searchCount;
        private ObjectIdentifier searchRouteID;
        private String searchOperationNumber;
        private Boolean currentFlag;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ObjMachineRecipeGetListForRecipeBodyManagementIn {
        private ObjectIdentifier equipmentId;
        private List<Infos.StartCassette> startCassettes;
    }

    @Data
    public static class EquipmentRelatedInfoUpdateForLotSplitOnEqpIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier parentLotID;
        private ObjectIdentifier childLotID;
    }

    @Data
    public static class ProcessOperationListForRoute {
        ObjectIdentifier routeID;
        ObjectIdentifier operationID;
        String operationNumber;
        String pdType;
        long searchCount;
    }

    @Data
    public static class EntityInhibitEventMake {
        String transactionID;
        Infos.EntityInhibitDetailInfo strEntityInhibitDetailInfo;
        ObjectIdentifier reasonCode;
        ObjectIdentifier controlJobID;
        String claimMemo;
    }

    @Data
    public static class VendorLotEventMakeParams {
        private String transactionID;
        private ObjectIdentifier lotID;
        private String vendorLotID;
        private Integer claimQuantity;
        private String claimMemo;
    }

    @Data
    public static class EquipmentBufferResourceTypeChangeEventMakeParams {
        private ObjectIdentifier equipmentID;
        private List<Infos.BufferResourceUpdateInfo> bufferResourceUpdateInfoList;
        private String claimMemo;
    }

    @Data
    public static class ObjectNoteEventMakeParams {
        private ObjectIdentifier objectID;
        private String noteType;
        private String action;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String noteTitle;
        private String noteContents;
        private ObjectIdentifier ownerID;
        private String transactionID;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class EquipmentPortStatusChangeEventMakeParams {
        private ObjectIdentifier equipmentID;
        private String portType;
        private ObjectIdentifier portID;
        private String portUsage;
        private String portStatus;
        private String accessMode;
        private String dispatchState;
        private String dispatchTime;
        private String dispatchDurableID;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class EquipmentStatusChangeEventMakeParams {
        private String transactionID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier newEquipmentStatus;
        private ObjectIdentifier newE10Status;
        private ObjectIdentifier newActualStatus;
        private ObjectIdentifier newActualE10Status;
        private String newOperationMode;
        private ObjectIdentifier previousStatus;
        private ObjectIdentifier previousE10Status;
        private ObjectIdentifier previousActualStatus;
        private ObjectIdentifier previousActualE10Status;
        private String previousOpeMode;
        private String prevStateStartTime;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }

    @Data
    public static class EqpMonitorJobChangeEventMakeParams {
        private String transactionID;
        private String opeCategory;
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier eqpMonitorJobID;
        private String monitorJobStatus;
        private String previousMonitorJobStatus;
        private List<Infos.EqpMonitorLotInfo> eqpMonitorLotInfoList;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class EqpMonitorChangeEventMakeParams {
        private String transactionID;
        private String opeCategory;
        private ObjectIdentifier eqpMonitorID;
        private String previousMonitorStatus;
        private String previousNextExecutionTime;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class QTimeTargetOperationReplace {
        private ObjectIdentifier lotID;
        private Boolean specificControlFlag;
    }

    @Data
    public static class LotReworkEventMakeParams{
        private ObjectIdentifier lotID;
        private ObjectIdentifier reasonCodeID;
        private OldCurrentPOData oldCurrentPOData;
        private String claimMemo;
        private String transactionID;
    }

    @Data
    public static class LotReticleSetChangeEventMakeParams{
        private ObjectIdentifier lotID;
        private String claimMemo;
        private String transactionID;
    }

    @Data
    public static class ObjEqpMonitorWaferUsedCountUpdateEventMakeParams{
        private String transactionID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.EqpMonitorWaferUsedCount> strEqpMonitorWaferUsedCountList;
        private String claimMemo;
    }

    @Data
    public static class LotHoldEventMakeParams {
        private String transactionID;
        private ObjectIdentifier lotID;
        private List<Infos.HoldHistory> holdHistoryList;
    }

    @Data
    public static class LotPartialReworkCancelEventMakeParams {
        private String transactionID;
        private ObjectIdentifier childLotID;
        private ObjectIdentifier parentLotID;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }
    @Data
    public static class LotOperationMoveEventMakeOtherParams{
        private ObjectIdentifier createdID;
        private String claimMemo;
        private String transactionID;
    }

    @Data
    public static class LotOperationMoveEventMakeOpeComp{
        private String transactionID;
        private ObjectIdentifier equipmentID;
        private String operationMode;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier cassetteID;
        private Infos.LotInCassette lotInCassette;
        private String claimMemo;
    }

    @Data
    public static class LotFutureHoldEventMakeParams{
        private String transactionID;
        private ObjectIdentifier lotID;
        private String entryType;
        private Infos.FutureHoldHistory futureHoldHistory;
        private ObjectIdentifier releaseReasonCode;
        private String claimMemo;
    }

    @Data
    public static class LotBankMoveEventMakeParams{
        private String transactionID;
        private ObjectIdentifier lotID;
        private String claimMemo;
    }

    @Data
    public static class LotOperationMoveEventMakeBranchParams{
        private String transactionID;
        private ObjectIdentifier lotID;
        private OldCurrentPOData oldCurrentPOData;
        private String claimMemo;
    }

    @Data
    public static class ProcessResourceWaferPositionEventMakeParams{
        private String transactionID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private Infos.ProcessResourcePositionInfo processResourcePositionInfo;
        private String claimMemo;
    }

    @Data
    public static class LotOperationMoveEventMakeLocateParams{
        private String transactionID;
        private ObjectIdentifier aLotID;
        private Boolean lotcateDirection;
        private OldCurrentPOData oldCurrentPOData;
        private String claimMemo;
    }
    @Data
    public static class LotWaferMoveEventMakeMergeParams{
        private String transactionID;
        private ObjectIdentifier sourceLotID;
        private ObjectIdentifier destinationLotID;
    }

    @Data
    public static class LotWaferScrapEventMakeParams{
        private String transactionID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier reasonRouteID;
        private ObjectIdentifier reasonOperationID;
        private String reasonOperationNumber;
        private String reasonOperationPass;
        private String claimMemo;
        private List<Infos.ScrapWafers> scrapWafers;
    }

    @Data
    public static class WaferChamberProcessEventMakeParams{
        private String transactionID;
        private ObjectIdentifier equipmentID;
        private String claimMemo;
        private List<Infos.ChamberProcessLotInfo> chamberProcessLotInfos;
    }

    @Data
    public static class ProductRequestEventMakeReleaseParams{
        private String transactionID;
        private Infos.ReleaseLotAttributes releaseLotAttributes;
        private String claimMemo;
    }

    @Data
    public static class ProductRequestEventMakeUpdateParams{
        private String transactionID;
        private Infos.UpdateLotAttributes updateLotAttributes;
        private String claimMemo;
    }

    @Data
    public static class DurableXferStatusChangeEventMakeParams {
        private String transactionID;
        private ObjectIdentifier durableID;
        private String durableType;
        private String actionCode;
        private String durableStatus;
        private String xferStatus;
        private String transferStatusChangeTimeStamp;
        private String location;
        private String claimMemo;
    }

    @Data
    public static class ProcessJobChangeEventMakeParams {
        private ObjectIdentifier controlJobID;
        private String processJobID;
        private String opeCategory;
        private String processStart;
        private String currentState;
        private List<Infos.ProcessWafer> processWaferList;
        private List<Infos.ProcessJobChangeRecipeParameter> processJobChangeRecipeParameterList;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class EntityInhibitEventMakeParams {
        private String transactionID;
        private Infos.EntityInhibitDetailInfo entityInhibitDetailInfo;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier controlJobID;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class LotChangeEventMakeParams {
        private String transactionID;
        private String lotID;
        private String externalPriority;
        private String lotOwnerID;
        private String orderNumber;
        private String customerCodeID;
        private String lotComment;
        private String priorityClass;
        private String productID;
        private String previousProductID;
        private String planStartTime;
        private String planCompTime;
        private String claimMemo;
    }

    @Data
    public static class QTimeChangeEventMakeParams {
        private String updateMode;
        private ObjectIdentifier lotID;
        private Infos.QtimeInfo qtimeInfo;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class ReticleOperationEventMakeParams {
        private String reticleID;
        private String reticleObj;
        private String reticleType;
        private String reticleStatus;
        private String reticleGrade;
        private String reticleLocation;
        private String opeCategory;
        private String reticlePodID;
        private String inspectionType;
        private String eqpID;
        private String stockerID;
        private String reasonCode;
        private String transferStatus;
        private String reticleSubStatus;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class AutoDispatchControlEventMakeIn {
        private ObjectIdentifier lotID;
        private Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo;
        private String claimMemo;
    }
    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/29                              Wind
     *
     * @author Wind
     * @date 2018/10/29 18:27
     */
    @Data
    public static class QtimeDefinitionSelectionInqIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private String branchInfo;
    }

    @Data
    public static class ObjQtimeCandidateListInRouteGetDRIn {
        private ObjectIdentifier lotID;
        private String productID;
        private String productGroupID;
        private String technologyID;
        private String processFlow;
        private String mainProcessFlow;
        private String moduleProcessFlow;
        private String operationNumber;
    }

    @Data
    public static class QtimeActionReqIn {
        private ObjectIdentifier lotID;
        private String lotStatus;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier equipmentID;
        private Outputs.QrestTimeAction strQrestTimeAction;
        private String claimMemo;

    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/7/2018 4:05 PM
     */
    @Data
    public static class QTimeChangeEventMake {
        private String updateMode;
        private ObjectIdentifier lotID;
        private Infos.QtimeInfo strQtimeInfo;
        private String claimMemo;
    }

    @Data
    public static class ObjDurableHoldRecordGetDRIn {
        private String durableCategory;
        private ObjectIdentifier durableID;

    }

    @Data
    public static class ObjReticleDetailInfoGetDRIn {
        private ObjectIdentifier reticleID;
        private boolean durableOperationInfoFlag;
        private boolean durableWipOperationInfoFlag;
    }

    @Data
    public static class ObjDurableInPostProcessFlagGetIn {
        private String durableCategory;
        private ObjectIdentifier durableID;

    }

    @Data
    public static class ObjProcessOperationListForDurableDRIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private boolean searchDirection;
        private boolean posSearchFlag;
        private boolean currentFlag;
        private long searchCount;
        private ObjectIdentifier searchRouteID;
        private String searchOperationNumber;
    }

    @Data
    public static class ObjProcessOperationInfoGetDrIn {
        private String poObj;
        private boolean asgnProcessResourceInfoFlag;
        private boolean processResourcePositionInfoFlag;
        private boolean dcDefFlag;
        private boolean assignedRecipeParameterFlag;
        private boolean assignedReticleFlag;
        private boolean assignedFixtureFlag;
        private boolean samplingWaferFlag;
    }

    @Data
    public static class ObjectLockModeGetIn {
        private ObjectIdentifier objectID;
        private String className;
        private String functionCategory;
        private boolean userDataUpdateFlag;
    }

    @Data
    public static class ObjDurableControlJobIDGet {
        private ObjectIdentifier durableID;
        private String durableCategory;
    }

    @Data
    public static class ObjEquipmentBufferResourceTypeChangeIn {
        private ObjectIdentifier equipmentID;
        private List<Infos.BufferResourceUpdateInfo> strBufferResourceUpdateInfoSeq;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/16/2018 9:50 AM
     */
    @Data
    public static class ObjQTimeActionConsistencyCheck {
        private ObjectIdentifier lotID;
        private Infos.QTimeActionInfo strQtimeActionInfo;
        private ObjectIdentifier triggerRouteID;
        private String triggerOperationNumber;
        private String triggerBranchInfo;
        private String triggerReturnInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/14                          Wind
     *
     * @author Wind
     * @date 2018/11/14 15:15
     */
    @Data
    public static class ObjAdvancedObjectLockForEquipmentResourceIn {
        private ObjectIdentifier equipmentID;
        private String className;
        private ObjectIdentifier objectID;
        private long objectLockType;
        private String bufferResourceName;
        private long bufferResourceLockType;
    }

    @Data
    public static class ObjObjectLockForEquipmentContainerPositionIn {
        private ObjectIdentifier          equipmentID;
        private List<Infos.StartCassette>  strStartCassette;
        private ObjectIdentifier          controlJobID;
        private ObjectIdentifier          destCassetteID;
        private ObjectIdentifier          waferID;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/27/2018 10:16 AM
     */
    @Data
    public static class ObjEntityInhibitExceptionLotAttributesIn {
        private List<Infos.EntityIdentifier> entityIdentifierList;
        private Infos.EntityInhibitExceptionLotInfo exceptionLotInfo;
    }

    @Data
    public static class ObjEntityInhibiteffectiveForLotGetDRIn {
        private List<Infos.EntityInhibitInfo> strEntityInhibitInfos;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ObjProcessOperationRawDCItemsGetDR {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String processJobID;
        private Boolean expandFlag;
    }

    @Data
    public static class ObjProcessOperationNestListGetDRIn {
        private ObjectIdentifier routeID;
        private String fromOperationNumber;
        private Long nestLevel;
        private String routeRequirePattern;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 12/19/2018 2:24 PM
     */
    @Data
    public static class ObjEntityInhibitExceptionLotChangeForOpeCompIn{
        private List<ObjectIdentifier> lotIDs;
        private ObjectIdentifier controlJobID;
    }


    /**
     * @author ZQI
     * @date 2018/12/18
     */
    @Data
    public static class ObjEquipmentFlowBatchMaxCountChangeIn {
        private ObjectIdentifier equipmentID;
        private Integer flowBatchMaxCount;
    }

    /**
     * @author ZQI
     * @date 2018/12/18
     */
    @Data
    public static class ObjEquipmentFlowBatchMaxCountChangeEventMakeIn {
        private String transactionID;
        private ObjectIdentifier equipmentID;
        private Integer flowBatchMaxCount;
        private String claimMemo;
    }

    /**
     * description:objQTime_triggerOpe_Replace_in_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 12/21/2018 2:57 PM
     */
    @Data
    public static class ObjQTimeTriggerOpeReplaceIn{
        private ObjectIdentifier lotID;
        private Boolean retriggerFlag;
    }

    @Data
    public static class ObjEqpMonitorWaferUsedCountUpdateIn {
        private String action;
        private ObjectIdentifier lotID;
        private String transactionID;
        private List<Infos.EqpMonitorWaferUsedCount> strEqpMonitorWaferUsedCountSeq;
        private String claimMemo;
    }

    @Data
    public static class ObjSchdlChangeReservationCheckForActionDRIn {
        private ObjectIdentifier lotID;
        private String routeID;
        private String operationNumber;
    }

    @Data
    public static class ObjTempFlowBatchSelectForEquipmentDRIn {
        private ObjectIdentifier equipmentID;
        private Results.FlowBatchLotSelectionInqResult strFlowBatchLotSelectionInqResult;
    }

    @Data
    public static class ObjObjectGetIn {
        private String stringifiedObjectReference;
        private String className;
        private List<Infos.HashedInfo> strHashedInfoSeq;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Scott
     * @date 2019/1/8 11:26:28
     */
    @Data
    public static class ObjLotCheckConditionForWhatNextEqpMonitorLotIn {
        private ObjectIdentifier                    eqpMonitorID;
        private String                              checkLevel;
        private List<Infos.WhatNextAttributes>      strWhatNextAttributes;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Scott
     * @date 2019/1/8 13:29:17
     */
    @Data
    public static class strLotCheckConditionForEqpMonitorIn {
        private ObjectIdentifier eqpMonitorID;
        private String           operation;
        private String           checkLevel;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ObjLotListForDeletionGetDRIn {
        private ObjectIdentifier lotID;
        private List<String> lotStatusList;
//        private String lotStatus;
        private String lotType;
        private String subLotType;
        private ObjectIdentifier bankID;
        private ObjectIdentifier productID;
        private String orderNumber;
        private String customerCode;
        private ObjectIdentifier manufacturingLayerID;
        private ObjectIdentifier routeID;

        public ObjLotListForDeletionGetDRIn(){}

        public ObjLotListForDeletionGetDRIn(Params.LotListInqParams lotListInqParams) {
            this.lotID = lotListInqParams.getLotID();
//            this.lotStatus = lotListInqParams.getLotStatus();
            this.lotStatusList = lotListInqParams.getLotStatusList();
            this.lotType = lotListInqParams.getLotType();
            this.subLotType = lotListInqParams.getSubLotType();
            this.bankID = lotListInqParams.getBankID();
            this.productID = lotListInqParams.getProductID();
            this.orderNumber = lotListInqParams.getOrderNumber();
            this.customerCode = lotListInqParams.getCustomerCode();
            this.manufacturingLayerID = lotListInqParams.getManufacturingLayer();
            this.routeID = lotListInqParams.getRouteID();

        }
    }

    @Data
    public static class ObjDurableCurrentStateCheckTransitionIn {
        private String durableCategory;                        //<i>Durable Category
        private ObjectIdentifier durableID;                              //<i>Durable ID
        private String durableStatus;                          //<i>Durable Status
        private ObjectIdentifier durableSubStatus;                       //<i>Durable Sub Status
        private String currentDurableStatus;                   //<i>Current Durable Status
        private ObjectIdentifier currentDurableSubStatus;                //<i>Current Durable Sub Status
    }

    @Data
    public static class ObjDurableCurrentStateChangeIn {
        private String durableCategory;                        //<i>Durable Category
        private ObjectIdentifier durableID;                              //<i>Durable ID
        private String durableStatus;                          //<i>Durable Status
        private String reticleLocation;
        private ObjectIdentifier durableSubStatus;                       //<i>Durable Sub Status
    }

    @Data
    public static class ObjVirtualOperationLotsGetDRIn {
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier operationID;
        private String selectCriteria;
    }

    @Data
    public static class ObjVirtualOperationInprocessingLotsGetDRIn {
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier operationID;
    }

    @Data
    public static class ObjProcessOperationListInRouteForFpcGetDRIn {
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier subRouteID;
        private String subOperationNumber;
        private ObjectIdentifier orgRouteID;
        private String orgOperationNumber;
        private String routeRequirePattern;
        private ObjectIdentifier lotFamilyID;
        private Boolean FPCCountGetFlag;
    }

    @Data
    public static class ObjFPCInfoGetDRIn {
        private List<String> FPCIDs;                 //<i>Sequence of DOC ID
        private ObjectIdentifier lotID;                   //<i>Lot ID
        private ObjectIdentifier lotFamilyID;                   //<i>Lot ID
        private ObjectIdentifier mainPDID;                //<i>Main PD ID       (defined DOC)
        private String mainOperNo;              //<i>Operation Number (defined DOC)
        private ObjectIdentifier orgMainPDID;             //<i>Original Main PD ID
        private String orgOperNo;               //<i>Operation Number (Connected Sub Route Point)
        private ObjectIdentifier subMainPDID;             //<i>Sub Main PD ID
        private String subOperNo;               //<i>Operation Number (Connected Sub2 Route Point)
        private ObjectIdentifier equipmentID;             //<i>equipment ID
        private boolean waferIDInfoGetFlag;      //<i>Wafer ID Information Get Flag
        private boolean recipeParmInfoGetFlag;   //<i>Recipe Parameter Information Get Flag
        private boolean reticleInfoGetFlag;      //<i>Reticle Information Get Flag
        private boolean dcSpecItemInfoGetFlag;   //<i>DC Spec Item Information Get Flag
    }

    @Data
    public static class FPCCheckConditionForUpdateIn {
        private ObjectIdentifier lotFamilyID;
        private String actionType;
        private ObjectIdentifier mainPDID;
        private String mainOpeNo;
        private ObjectIdentifier orgMainPDID;
        private String orgOpeNo;
        private ObjectIdentifier subMainPDID;
        private String subOpeNo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/22 上午 10:31
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjLotInterFabTransferStateGetIn {
        private ObjectIdentifier lotID;
        private String           siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/27                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/27 10:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjInterFabXferPlanListGetDRIn {
        private Infos.InterFabLotXferPlanInfo  strInterFabLotXferPlanInfo;
        private String   siInfo;
    }

    @Data
    public static class CollectedDataCheckConditionForDataStoreIn{
        private ObjectIdentifier controlJobID;
        private List<Infos.CollectedDataItemStruct> collectedDataItemList;
    }

    @Data
    public static class SendMoveInReserveReqIn extends TCSIn{
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;

    }

    @Data
    public static class SendMoveInReqIn extends TCSIn{
        private User requestUserID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private Boolean processJobPauseFlag;
        private String claimMemo;
    }

    @Data
    public static class SendRecipeParamAdjustReqIn extends TCSIn{
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private Boolean allProcessStartFlag;
        private String claimMemo;

    }

    @Data
    public static class SendCJPJProgressIn extends TCSIn{
        private ObjectIdentifier controlJobID;
    }

    @Data
    public static class SendCJPJInfoIn extends TCSIn{
        private Params.CJPJOnlineInfoInqInParams cjpjOnlineInfoInqInParams;
    }

    @Data
    public static class SendEqpModeChangeReqIn extends TCSIn{
        private List<Infos.PortOperationMode> portOperationModes;
        private Boolean notifyToEqpFlag;
        private Boolean notifyToTCSFlag;

    }

    @Data
    public static class SendMoveOutReqIn extends TCSIn{
        private User requestUserID;
        private ObjectIdentifier controlJobID;
        private Boolean spcResultRequiredFlag;
        private String claimMemo;
    }

    @Data
    public static class SendEqpEAPInfoInqIn extends TCSIn{
    }

    @Data
    public static class SendEqpEAPInfoInqInput {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class SendControlJobActionReqIn extends TCSIn{
        private ObjectIdentifier controlJobID;
        private String actionCode;
        private String claimMemo;
    }

    @Data
    public static class SendControlJobActionReqInput{
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier equipmentID;
        private String actionCode;
        private String claimMemo;
    }

    @Data
    public static class SendMoveInCancelReqIn extends TCSIn{
        private ObjectIdentifier controlJobID;
        private User requestUserID;
        private String claimMemo;
    }

    @Data
    public static class SendMoveInReserveCancelReqIn extends TCSIn{
        private ObjectIdentifier controlJobID;
    }

    @Data
    public static class StockerDetailInfoInq {
        private ObjectIdentifier                        machineID;
        private Object                                     siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/9 19:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class SendUploadInventoryReqIn {
        private Infos.ObjCommon objCommon;
        private User user;
        private Infos.UploadInventoryReq uploadInventoryReq;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 10:51
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ProcessOperationSequenceGetDRIn {
        private ObjectIdentifier routeId;
        private String OperationNumber;
        private String siInfo;
    }

    @Data
    public static class ObjObjectIDListGetDRIn {
        private String className;
        private ObjectIdentifier objectID;
        private List<Infos.AdditionalSearchCondition> strAdditionalSearchConditionSeq;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/6/18 15:54
     */
    @Data
    public static class QTimeTriggerOpeReplaceIn {
        private ObjectIdentifier lotID;
        private Boolean retriggerFlag;
        private Object siInfo;
    }


    @Data
    public static class ObjFPCListGetDRIn {
        private List<String> fpcIDs;                 //<i>Sequence of DOC ID
        private ObjectIdentifier lotID;
        private ObjectIdentifier lotFamilyID;
        private ObjectIdentifier mainPDID;
        private String mainOperNo;
        private ObjectIdentifier orgMainPDID;
        private String orgOperNo;
        private ObjectIdentifier subMainPDID;
        private String subOperNo;
        private ObjectIdentifier equipmentID;
        private Boolean waferIDInfoGetFlag;
        private Boolean recipeParmInfoGetFlag;
        private Boolean reticleInfoGetFlag;
        private Boolean dcSpecItemInfoGetFlag;
    }

    @Data
    public static class EntityInhibitCheckForReticleInhibition{
        private List<Infos.EntityInhibitInfo> entityInhibitInfoSeq;
        private List<Infos.EntityIdentifier> entityIDSeq;
        private List<Infos.FoundReticle> reticleSeq;
        private List<String> sublottypes;
        private Boolean useFPCInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/13                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/13 17:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public  static class PostProcessQueueListDRIn {
        private String key;
        private Long seqNo;
        private String watchDogName;
        private String postProcId;
        private Long syncFlag;
        private String txId;
        private String targetType;
        private Infos.PostProcessTargetObject strPostProcessTargetObject;
        private String status;
        private Long passedTime;
        private String claimUserId;
        private String startCreateTimeStamp;
        private String endCreateTieStamp;
        private String startUpdateTimeStamp;
        private String endUpdateTimeStamp;
        private Long maxCount;
        private Boolean committedReadFlag;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/13                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/13 17:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjCassetteInPostProcessFlagSetIn {
        private ObjectIdentifier    cassetteID;
        private boolean            inPostProcessFlag;
        private ObjectIdentifier    lotID;
        private Object              siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 15:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class SendTransportJobInqIn {
        private Infos.ObjCommon strObjCommonIn;
        private User user;
        private Infos.TransportJobInq transportJobInq;
        private String functionID;
    }

    @Data
    public static class SendRTMSTransportJobInqIn {
        private Infos.ObjCommon              strObjCommonIn;
        private User                         requestUserID;
        private ObjectIdentifier             reticlePodID;
        private ObjectIdentifier             toMachineID;
        private ObjectIdentifier             toStockerID;
        private ObjectIdentifier             fromMachineID;
        private ObjectIdentifier             fromStockerID;
        private Boolean                      detailFlag;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 11:23
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class SendTransportJobCancelReqIn {
        private Infos.ObjCommon strObjCommonIn;
        private User user;
        private Infos.TranJobCancelReq tranJobCancelReq;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 17:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class SendEAPRecoveryReqIn extends TCSIn {
        private User                            user;
        private String                          actionCode;
        private List<Infos.RecoverWafer>        strRecoveryWafer;
        private String                          claimMemo;
    }

    @Data
    public static class SendEAPRecoveryReqInput{
        private User                            user;
        private String                          actionCode;
        private ObjectIdentifier                equipmentID;
        private String                          opeMemo;
    }

    @Data
    public static class ObjEquipmentCheckInhibitForDurableWithMachineRecipeIn{
        private ObjectIdentifier equipmentID;
        private List<ObjectIdentifier> durableIDs;
        private String durableCategory;
        private Infos.DurableStartRecipe durableStartRecipe;
        private Object siInfo;
    }


    @Data
    public static class LotCheckConditionForEqpMonitorLotReserveIn{
        private ObjectIdentifier                     eqpMonitorID;
        private ObjectIdentifier                     equipmentID;
        private String                               checkLevel;
        private List<Infos.EqpMonitorProductLotMap>  strProductLotMap;
    }

    @Data
    public static class SendPJStatusChangeReqIn extends TCSIn {
        private User user;
        private Infos.PJStatusChangeReqInParm strPJStatusChangeReqInParm;
        private String actionRequestTimeStamp;
        private String claimMemo;
    }

    @Data
    public static class SendNPWCarrierReserveForIBReqIn extends TCSIn {
        private User user;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private String claimMemo;
    }
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/5 11:12
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjEquipmentPortStateCheckForCassetteDeliveryIn {
        private ObjectIdentifier           equipmentID;
        private ObjectIdentifier           portID;
        private Object                     siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/17 16:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjWhatNextLotListToStartCassetteForTakeOutInDeliveryReqIn {
        private ObjectIdentifier            equipmentID;
        private List<Infos.PortGroup>       strPortGroup;
        private Results.WhatNextLotListResult strWhatNextInqResult;
        private Object                       siInfo;
    }

    @Data
    public static class TCSIn {
        private Infos.ObjCommon objCommonIn;
        private ObjectIdentifier equipmentID;
        private String sendName;
        private String transactionRoute;
        private String realTransaction;
    }

    @Data
    public static class SendRecipeDownloadReqIn extends TCSIn {
        private User requestUserID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private boolean formatFlag;
        private String claimMemo;
    }

    @Data
    public static class SendRecipeCompareReqIn extends TCSIn {
        private User requestUserID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private boolean formatFlag;
        private String claimMemo;
    }

    @Data
    public static class SendRecipeDeleteInFileReqIn extends TCSIn {
        private User requestUserID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private String claimMemo;
    }

    @Data
    public static class SendNPWCarrierReserveReqIn extends TCSIn {
        private User requestUserID;
        private String portGropuID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private String claimMemo;
    }

    @Data
    public static class SendRecipeDirectoryInqIn extends TCSIn {
        private User requestUserID;
    }

    @Data
    public static class AutoDispatchControlInfoUpdateIn {
        private ObjectIdentifier lotID;
        private Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo;
    }

    @Data
    public static class SendRecipeUploadReqIn extends TCSIn {
        private User requestUserID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private String claimMemo;
        private boolean formatFlag;
    }

    @Data
    public static class SendRecipeDeleteReqIn extends TCSIn {
        private User requestUserID;
        private String physicalRecipeID;
        private boolean recipeFileDeleteFlag;
        private ObjectIdentifier machineRecipeID;
        private String fileLocation;
        private String fileName;
        private String claimMemo;
    }

    @Data
    public static class EquipmentContainerPositionProcessJobStatusSetIn {
        private ObjectIdentifier            equipmentID;
        private List<ObjectIdentifier>       waferSeq;
        private String actionCode;
        private Object                       siInfo;
    }

    @Data
    public static class ControlJobStartLotWaferInfoGetIn {
        private ObjectIdentifier controlJobID;
        private boolean startLotOnlyFlag;
        private Object siInfo;
    }

    @Data
    public static class SorterSorterJobStatusGetDRIn {
        private ObjectIdentifier            equipmentID;
        private ObjectIdentifier            originalCassetteID;
        private ObjectIdentifier            destinationCassetteID;
        private String                      portGroupID;
        private Object                      siInfo;
    }

    @Data
    public static class QtimeSetByPJCompIn {
        private ObjectIdentifier            lotID;
        private List<ObjectIdentifier>      waferIDSeq;
        private Object                      siInfo;
    }

    @Data
    public static class QTimeTriggerOpeReplaceByPJCompIn {
        private ObjectIdentifier            lotID;
        private List<ObjectIdentifier>      waferIDSeq;
        private Object                      siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 13:35
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjOwnerChangeObjectListGetIn {
        private ObjectIdentifier                        ownerID;                          //<i>Owner ID
        private String                                  targetClassName;                  //<i>Target Class Name
        private String                                  targetTableName;                  //<i>Target Table Name
        private String                                  targetColumnName;                 //<i>Target Column Name
        private List<Infos.HashedInfo>                  strHashedInfoSeq;                 //<i>Sequence of Hashed Information
        private Infos.OwnerChangeDefinition             strOwnerChangeDefinition;         //<i>Owner Change Definition
        private Object                                  siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 14:30
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjOwnerChangeEventMakeIn {
        private ObjectIdentifier                fromOwnerID;                         //<i>From Owner ID
        private ObjectIdentifier                toOwnerID;                           //<i>To Owner ID
        private List<Infos.OwnerChangeObject>   strOwnerChangeObjectSeq;             //<i>Sequence of Owner Change Object Information
        private String                          claimMemo;                           //<i>Claim Memo
        private Object                          siInfo;                              //<i>Reserved for SI customization
    }

    @Data
    public static class SystemMessageEventMakeIn {
        private String transactionID;
        private String subSystemID;
        private String systemMessageCode;
        private String systemMessageText;
        private boolean notifyFlag;
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

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 17:23
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjOwnerChangeObjectListGetDRIn {
        private ObjectIdentifier                         ownerID;
        private String                                   tableName;
        private String                                   objRefColumn;
        private String                                   parentTable;
        private List<Infos.OwnerChangeColumnDefinition>  strOwnerChangeColumnDefinitionSeq;
        private Object                                   siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/30 14:24
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjOwnerChangeUpdateIn {
        private String                          stringifiedObjectReference;
        private Infos.OwnerChangeReqInParm     strOwnerChangeReqInParm;
        private Infos.OwnerChangeDefinition     strOwnerChangeDefinition;
        private Object                          siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/30 14:42
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjOwnerChangeDefObjUpdateDRIn {
        private ObjectIdentifier                    fromOwnerID;
        private ObjectIdentifier                    toOwnerID;
        private Infos.OwnerChangeDefObjDefinition   strOwnerChangeDefObjDefinition;
        private Object                              siInfo;
    }

    @Data
    public static class PostProcessQueueParallelExecCheckIn {
        private String dKey;
        private List<Infos.PostProcessActionInfo> postProcessActionInfoList;
        private boolean lotCountCheckFlag;
    }


    @Data
    public static class SorterSorterJobLockDRIn {
        private ObjectIdentifier      sorterJobID;
        private ObjectIdentifier      sorterComponentJobID;
        private ObjectIdentifier      cassetteID;
        private Integer   lockType;
    }
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/6 13:19
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjSorterSorterJobEventMakeIn {
        private String                          action;
        private String                          transactionID;
        private Infos.SortJobListAttributes     strSortJobListAttributes;
        private String                          sorterJobCategory;
        private String                          claimMemo;
        private Object                          siInfo;
    }

    @Data
    public static class SorterComponentJobDeleteDRIn {
        private ObjectIdentifier                          sorterJobID;
        private List<ObjectIdentifier>                    componentJobIDseq;
        private Object                          siInfo;
    }

    @Data
    public static class SorterLinkedJobUpdateDRIn {
        private String                          jobType;
        private List<ObjectIdentifier>                    jobIDs;
        private Object                          siInfo;
    }

    @Data
    public static class ObjectValidSorterJobGetIn {
        private String classification;
        private ObjectIdentifier objectID;
    }

    @Data
    public static class ObjLotFPCInfoForOperationGetIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Boolean waferIDInfoGetFlag;
        private Boolean recipeParmInfoGetFlag;
        private Boolean reticleInfoGetFlag;
        private Boolean dcSpecItemInfoGetFlag;
        private Boolean corresOpeInfoGetFlag;
    }

    @Data
    public static class MessageQueuePutIn {
        private ObjectIdentifier equipmentID;
        private List<Infos.PortOperationMode> strPortOperationMode;
        private ObjectIdentifier equipmentStatusCode;
        private ObjectIdentifier lotID;
        private String lotProcessState;
        private String lotHoldState;
        private ObjectIdentifier cassetteID;
        private String cassetteTransferState;
        private boolean cassetteTransferReserveFlag;
        private boolean cassetteDispatchReserveFlag;
        private ObjectIdentifier  durableID;
        private String durableTransferState;
    }

    @Data
    public static class ObjProcessOperationActionResultSetIn {
        private List<Infos.StartCassette> strStartCassette;
        private List<Results.DCActionLotResult> strDCActionLotResult;
        private boolean overwriteFlag;
    }

    @Data
    public static class ObjEquipmentUsageCountUpdateForPostProcIn {
        private ObjectIdentifier equipmentID;
        private long waferCnt;
        private long opeStartCnt;
        private String action;
    }

    @Data
    public static class SendMoveInReserveForIBReqIn extends TCSIn{
        private User requestUserID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private String claimMemo;
    }

    @Data
    public static class SendDurableControlJobActionReqIn extends TCSIn{
        private User requestUserID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier durableControlJobID;
        private String actionCode;
        private String claimMemo;
    }

    @Data
    public static class SendCarrierTransferJobEndRptIn extends TCSIn{
        private User requestUserID;
        private List<Infos.XferJobComp> strXferJob;
        private String claimMemo;
    }

    @Data
    public static class SendEDCDataItemWithTransitDataInqIn extends TCSIn{
        private Infos.SendEDCDataItemWithTransitDataInqInParm strSendEDCDataItemWithTransitDataInqInParm;
    }

    @Data
    public static class SendMoveInReserveCancelForIBReqIn extends TCSIn{
        private ObjectIdentifier controlJobID;
    }

    @Data
    public static class SendArrivalCarrierNotificationCancelForInternalBufferReqIn extends TCSIn{
        private User requestUserID;
        private String portGroupID;
        private List<Infos.NPWXferCassette> strNPWXferCassette;
        private String claimMemo;
    }

    @Data
    public static class SendArrivalCarrierNotificationCancelReqIn extends TCSIn{
        private User user;
        private String portGroupID;
        private List<Infos.NPWXferCassette> strNPWXferCassette;
        private String claimMemo;
    }

    @Data
    public static class SendRecipeParamAdjustOnActivePJReqIn extends TCSIn{
        private Params.RecipeParamAdjustOnActivePJReqParam strRecipeParamAdjustOnActivePJReqInParm;
        private String                                           actionRequestTimeStamp;
        private String                                           claimMemo;
    }

    @Data
    public static class SendMoveOutForIBReqIn extends TCSIn{
        private User requestUserID;
        private ObjectIdentifier controlJobID;
        private Boolean spcResultRequiredFlag;
        private String claimMemo;
    }

    @Data
    public static class SendMoveInCancelForIBReqIn extends TCSIn{
        private User requestUserID;
        private ObjectIdentifier controlJobID;
        private String claimMem;
    }

    @Data
    public static class SendMoveInForIBReqIn extends TCSIn{
        private User requestUserID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private Boolean processJobPauseFlag;
        private String claimMemo;
    }

    @Data
    public static class SendWaferSortOnEqpReqIn extends TCSIn{
        private User requestUserID;
        private String  actionCode;
        private List<Infos.WaferSorterSlotMap>  strWaferSorterActionListSequence;
        private String  portGroup;
        private String physicalRecipeID;
    }

    @Data
    public static class SendWaferSortOnEqpCancelReqIn extends TCSIn{
        private User requestUserID;
        private String  portGroup;
        private String  requestTimeStamp;
    }

    @Data
    public static class SendPartialMoveOutReqIn extends TCSIn{
        private ObjectIdentifier                 equipmentID;
        private ObjectIdentifier                 controlJobID;
        private List<Infos.PartialOpeCompLot>    strPartialOpeCompLotSeq;
        private String                           claimMemo;
        private Object                           siInfo;
    }

    @Data
    public static class SendReserveCancelUnloadingLotsForIBReqIn extends TCSIn{
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
    }


    @Data
    public static class SorterWaferSlotmapInsertDRIn {
        private ObjectIdentifier sorterJobID;
        private ObjectIdentifier sorterComponentJobID;
        private List<Infos.WaferSorterSlotMap>    strWaferSorterSlotMapSequence;
    }


    @Data
    public static class ObjLotWIPInfoResetIn {
        private long updateLevel;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
    }

    @Data
    public static class SpcInput{
//        private ObjectIdentifier requestUserID;
//        private ObjectIdentifier lotID;
//        private ObjectIdentifier processEquipmentID;
//        private ObjectIdentifier processRecipeID;
//        private ObjectIdentifier processMainProcessDefinitionID;
//        private ObjectIdentifier processProcessDefinitionID;
//        private String processOperationNumber;
//        private ObjectIdentifier measurementEquipmentID;
//        private ObjectIdentifier technologyID;
//        private ObjectIdentifier productGroupID;
//        private ObjectIdentifier productID;
//        private ObjectIdentifier reticleID;
//        private List<ObjectIdentifier> reticleIDs;
//        private ObjectIdentifier fixtureID;
//        private ObjectIdentifier mainProcessDefinitionID;
//        private String operationNumber;
//        private String operationName;
//        private ObjectIdentifier ownerUserID;
//        private String collectionType;
//        private ObjectIdentifier dcDefID;
//        private List<Infos.SpcDcItem> dcItems;
//        private List<Infos.WaferIDByChamber> waferIDByChambers;
//        private String lotComment;
//        private String processTimestamp;
//        private String measurementTimestamp;
//        private String processFabID;
        /**
         * description:
         * change history: qiandao project mes-spc integration update
         * date             defect#             person             comments
         * ---------------------------------------------------------------------------------------------------------------------
         * 2021/1/5                               Neyo                create file
         *
         * @author: Neyo
         * @date: 2021/1/5 18:56
         * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
         */
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier requestUserID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier processEquipmentID;
        private ObjectIdentifier processRecipeID;
        //change processMainProcessDefinitionID to processProcessFlowID
        private ObjectIdentifier processProcessFlowID;
        //change processProcessDefinitionID to processStepID
        private ObjectIdentifier processStepID;
        private String processOperationNumber;
        private ObjectIdentifier measurementEquipmentID;
        private ObjectIdentifier technologyID;
        private ObjectIdentifier productGroupID;
        private ObjectIdentifier productID;
        private ObjectIdentifier reticleID;
        private List<ObjectIdentifier> reticleIDs;
        private ObjectIdentifier fixtureID;
        //change mainProcessDefinitionID to processFlowID
        private ObjectIdentifier processFlowID; //measurement current flow id
        private String operationNumber;
        private String operationName;
        private ObjectIdentifier ownerUserID;
        //change collectionType to edcType
        private String edcType;
        //change dcDefID to edcID
        private ObjectIdentifier edcID;
        private List<Infos.SpcDcItem> dcItems;
        private List<Infos.WaferIDByChamber> waferIDByChambers;
        private String lotComment;
        private String processTimestamp;
        private String measurementTimestamp;
        private ObjectIdentifier processFabID;

        //add some new param
        //-------flow-------
        private String subLotType;

        @JSONField(name = "LotType")
        private String LotType;
        //----measurement-----
        private ObjectIdentifier route;                 //measurement current route id
        private ObjectIdentifier measurementRecipeID;   //measurement current recipe id
        private String measurementEquipmentGroup;       //measurement eqp group(mes now null eqpGroup now is eqpType)
        private ObjectIdentifier stepID;                //measurement current step id
        private ObjectIdentifier logicalRecipeID;       //measurement current eqp logical recipe id
        private ObjectIdentifier department;            //measurement department
        private ObjectIdentifier stage;                 //measurement stage

        //-----process----
        private ObjectIdentifier processRoute;          //process station route id
        private ObjectIdentifier processLogicalRecipeID;//process station logical recipe id
        private String processEquipmentGroup;           //process eqp group(mes now null eqpGroup now is eqpType)
        private ObjectIdentifier processDepartment;     //process department
        private ObjectIdentifier processStage;          //process stage


        // TODO: 2021/1/5 confirm
        private ObjectIdentifier batchID;                      //monitor group id
        private List<Inputs.ProductLotInfo>  productionLotList;//monitor lot->product lot list
        private String measureType;                            //normal:empty; remeasure(ocapNo:RE_MEASURE);add(ocapNo:ADD_MEASURE)

    }

    @Data
    public static class ProductLotInfo {
        private ObjectIdentifier productID;                 //flow product id
        private ObjectIdentifier productGroupID;            //flow product group id
        private ObjectIdentifier technologyID;              //logical grouping for product groups.
        private ObjectIdentifier lotID;                     //current operation lot id
        private String subLotType;                          //sub lot type information
        private String lotType;                             //lot type infomation
        //product lot  measurement information
        private String operationName;                       //measurement current opeName
        private String operationNumber;                     //measurement current opeNum
        private ObjectIdentifier measurementEquipmentID;    //measurement current equipment id
        private ObjectIdentifier processFlowID;             //measurement current flow id
        private ObjectIdentifier route;                     //measurement current route id
        private String measurementTimestamp;                //measurement time
        private ObjectIdentifier measurementRecipeID;       //measurement current machine reicpe id
        private String measurementEquipmentGroup;           //measurement eqp group(mes now null eqpGroup now is eqpType)
        private ObjectIdentifier stepID;                    //measurement current step id
        private ObjectIdentifier logicalRecipeID;           //measurement current eqp logical recipe id
        private ObjectIdentifier department;                //measurement department
        private ObjectIdentifier stage;                     //measurement stage

        //product lot process information
        private ObjectIdentifier processStage;              //process stage
        private String processOperationNumber;              //process station opeNum
        private ObjectIdentifier processRecipeID;           //process station machine recipe id
        private ObjectIdentifier processEquipmentID;        //process station equipment id
        private ObjectIdentifier processStepID;             //process station step id
        private ObjectIdentifier processProcessFlowID;      //process station flow id
        private ObjectIdentifier processRoute;              //process station route id;
        private String processTimestamp;                    //process station time
        private ObjectIdentifier processLogicalRecipeID;    //process station equipmnet logical recipe id
        private String processEquipmentGroup;               //process eqp group(mes now null eqpGroup now is eqpType)
        private ObjectIdentifier processDepartment;         //process station department
    }

    @Data
    public static class ObjLotStackedWaferInfoGetDRIn {
        private List<ObjectIdentifier> baseLotIDSeq;                           //<i>Sequence of Lot ID
        private ObjectIdentifier baseCarrierID;                          //<i>Cassette ID
        private List<ObjectIdentifier> topLotIDSeq;                            //<i>Sequence of Lot ID
    }

    @Data
    public static class ObjEquipmentContainerPositionReservationCreateIn {
        private List<ObjectIdentifier> containerPositionIDs;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private List<Infos.MtrlOutSpec> strMtrlOutSpecSeq;
    }
    @Data
    public static class ObjLogicalRecipeMachineRecipeForSubLotTypeGetDRIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier logicalRecipeID;
        private ObjectIdentifier productID;
        private String subLotType;
        private ObjectIdentifier equipmentID;
        private List<Infos.EqpChamberAvailableInfo> strEqpChamberAvailableInfoSeq;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjEquipmentContainerPositionInfoGetIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier key;
        private String keyCategory;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjEquipmentContainerPositionInfoClearIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier key;
        private String keyCategory;
    }

    @Data
    public static class ObjEquipmentContainerWaferStoreIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String processJobID;
        private List<Infos.SlmSlotMap> strSLMSlotMapSeq;
    }

    @Data
    public static class ObjEquipmentContainerWaferRetrieveIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String processJobID;
        private List<Infos.SlmSlotMap> strSLMSlotMapSeq;
    }

    @Data
    public static class ObjEquipmentContainerReservationUpdateIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier desPortID;
        private String actionCode;
        private List<Infos.EqpContainerPosition> strEqpContainerPositionSeq;
        private List<ObjectIdentifier> preCassetteIDs;
    }

    @Data
    public static class ApcIn{
        private String sendName;
    }

    @Data
    public static class ApcRecipeParamInq extends ApcIn{
        private User requestUser;
        private String equipmentID;
        private String portGroupID;
        private String controlJobID;
        private List<Infos.StartCassette> strStartCassette;
    }

    @Data
    public static class TCSMgrSendSLMWaferRetrieveCassetteReserveReqIn{
        private Params.SLMWaferRetrieveCassetteReserveReqInParams strSLMWaferRetrieveCassetteReserveReqInParm;
        private Infos.ObjCommon objCommonIn;
    }

    @Data
    public static class RecipeParameterRequestIn extends ApcIn {
        private User requestUser;
        private String equipmentID;
        private List<String> systemNameList;
        private List<Infos.ApcBaseCassette> strAPCBaseCassetteList;
        private String finalBoolean;
    }

    @Data
    public static class APCRunTimeCapabilityRequestIn extends ApcIn {
        private User requestUser;
        private String equipmentID;
        private Infos.APCBaseIdentification strAPCBaseIdentification;
        private List<String> systemNameList;
        private List<Infos.ApcBaseCassette> strAPCBaseCassetteList;
    }

    @Data
    public static class ControlJobInformationIn extends ApcIn {
        private User requestUser;
        private String controlJobID;
        private String equipmentID;
        private String controlJobStatus;
        private List<Infos.ApcBaseCassette> strAPCBaseCassetteList;
        private Infos.APCBaseReturnCode apcBaseReturnCode;
        private List<String> systemNameList;
    }

    @Data
    public static class SendSLMCassetteUnclampReqIn extends TCSIn {
        Params.SLMCassetteUnclampReqInParams slmCassetteUnclampReqInParams;
    }

    @Data
    public static class SLMWaferRetrieveCassetteReserveReqIn extends TCSIn {
        private User requestUserID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier equipmentID;           //<i>Equipment ID
        private ObjectIdentifier lotID;                 //<i>Lot ID
        private ObjectIdentifier cassetteID;            //<i>Cassette ID
        private ObjectIdentifier destPortID;            //<i>Destination Port ID
        private List<Infos.MtrlOutSpec> strMtrlOutSpecSeq;     //<i>Sequence of Material Out Spec
        private String actionCode;            //<i>Action Code
        private String claimMemo;
    }

    @Data
    public static class SendSortJobNotificationReqIn extends TCSIn{
        private User user;
        private ObjectIdentifier sorterJobID;
        private String portGroupID;
        private String opeMemo;
    }

    @Data
    public static class SendSortJobCancelNotificationReqIn extends TCSIn{
        private User user;
        private ObjectIdentifier sorterJobID;
        private ObjectIdentifier equipmentID;
        private String opeMemo;
    }

    @Data
    public static class ProcessStartDurablesReserveInformationSetIn {
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier durableControlJobID;
        private String durableCategory;
        private List<Infos.StartDurable> strStartDurables;
        private Infos.DurableStartRecipe strDurableStartRecipe;
    }

    @Data
    public static class DurableControlJobListGetDRIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier durableID;
        private String durableCategory;
        private ObjectIdentifier createUserID;
        private ObjectIdentifier durableJobID;
        private Boolean durableInfoFlag;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SendDurableOpeStartReqIn extends TCSIn{
        private Params.DurableOperationStartReqInParam strDurableOperationStartReqInParam;
        private String claimMemo;
    }

    @Data
    public static class DurablePostProcessQueueMakeIn {
        private String txID;
        private String patternID;
        private String key;
        private long seqNo;
        private Infos.DurablePostProcessRegistrationParm strDurablePostProcessRegistrationParm;
        private String claimMemo;
    }

    @Data
    public static class SendReticleRetrieveReqIn extends TCSIn {
        private Infos.ObjCommon strObjCommonIn;
        private User requestUserID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier resourceID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.MoveReticles> strMoveReticles;
    }

    @Data
    public static class SendReticleStoreCancelReqIn extends TCSIn{
        private Infos.ObjCommon             strObjCommonIn;
        private ObjectIdentifier            machineID;
        private ObjectIdentifier            portID;
        private ObjectIdentifier            reticlePodID;
        private List<Infos.MoveReticles>    strMoveReticles;

    }

    @Data
    public static class SendReticleRetrieveCancelReqIn extends TCSIn{
        private Infos.ObjCommon             strObjCommonIn;
        private ObjectIdentifier            machineID;
        private ObjectIdentifier            portID;
        private ObjectIdentifier            reticlePodID;
        private List<Infos.MoveReticles>    strMoveReticles;
    }

    @Data
    public static class SendReticlePodUnclampCancelReqIn extends TCSIn{
        private Infos.ObjCommon             strObjCommonIn;
        private ObjectIdentifier            machineID;
        private ObjectIdentifier            portID;
        private ObjectIdentifier            reticlePodID;
    }

    @Data
    public static class StockerReticlePodPortDispatchStateChangeIn {
        private ObjectIdentifier         stockerID;
        private Infos.ReticlePodPortInfo strReticlePodPortInfo;
        private Object                   siInfo;
    }

    @Data
    public static class SendReticleStoreReqIn extends TCSIn {
        private Infos.ObjCommon strObjCommonIn;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier resourceID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.MoveReticles> strMoveReticles;
    }

    @Data
    public static class SendReticlePodUnclampReqIn extends TCSIn {
        private Infos.ObjCommon strObjCommonIn;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private ObjectIdentifier reticlePodID;
    }

    @Data
    public static class SendBareReticleStockerOnlineModeChangeReqIn extends TCSIn{
        private User requestUserID;
        private Infos.ObjCommon strObjCommonIn;
        private ObjectIdentifier bareReticleStockerID;
        private String newOnlineMode;
    }

    @Data
    public static class SendReticleInventoryReqIn extends TCSIn {
        private User requestUserID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private String claimMemo;
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
            this.srvName = "MES";
            this.rqstAddr = rqstAddr;
            this.service = service;
            this.msgType = "R";
            this.locale = "en";
            this.txId = counter.get() >= 9999 ? counter.getAndSet(1) : counter.getAndIncrement();
            this.rqstTime = CimDateUtils.convert("yyyyMMddHHmmssSSS", CimDateUtils.getCurrentTimeStamp());
            this.retCode = retCode;
            this.retMsg = retMsg;
            this.traceId = this.getSrvName() +"@"+this.getRqstTime();
        }
    }

    @Data
    public static class OcapInput {
        private String caseNo;
        private String reqUser;
        private String reqSystem;
        private String remakr;
        private Map<String,String> params;
    }
}
