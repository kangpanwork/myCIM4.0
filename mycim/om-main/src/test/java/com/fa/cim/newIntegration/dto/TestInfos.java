package com.fa.cim.newIntegration.dto;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/3        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/3 13:34
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class TestInfos {
    @Data
    public static class VendorLotReceiveInfo {
        private String bankID;
        private String subLotType;
        private String lotID;
        private String productID;
        private int count;
        public VendorLotReceiveInfo() {}
        public VendorLotReceiveInfo(String bankID, String subLotType, String lotID, String productID, int count) {
            this.bankID = bankID;
            this.subLotType = subLotType;
            this.lotID = lotID;
            this.productID = productID;
            this.count = count;
        }
    }

    @Data
    public static class StbInfo {
        private String bankID;
        private String sourceProductID;
        private String productID;
        private Long productCount;
        private boolean isNeedPrepare;
        private boolean isNeedAssignWaferID;
        private ObjectIdentifier cassetteID;  // opitional, if it's not null, then use it to make stb

        public StbInfo(String bankID, String sourceProductID, String productID) {
            this.bankID = bankID;
            this.sourceProductID = sourceProductID;
            this.productID = productID;
            this.productCount = null;
            this.isNeedPrepare = false;
            this.isNeedAssignWaferID = false;
        }
        public StbInfo(String bankID, String sourceProductID, String productID, Long productCount, boolean isNeedPrepare) {
            this.bankID = bankID;
            this.sourceProductID = sourceProductID;
            this.productID = productID;
            this.productCount = productCount;
            this.isNeedPrepare = isNeedPrepare;
            this.isNeedAssignWaferID = false;
        }

        public StbInfo(String bankID, String sourceProductID, String productID, Long productCount, boolean isNeedPrepare, ObjectIdentifier cassetteID) {
            this.bankID = bankID;
            this.sourceProductID = sourceProductID;
            this.productID = productID;
            this.productCount = productCount;
            this.isNeedPrepare = isNeedPrepare;
            this.cassetteID = cassetteID;
        }

        public StbInfo(String bankID, String sourceProductID, String productID, Long productCount, boolean isNeedPrepare, boolean isNeedAssignWaferID) {
            this.bankID = bankID;
            this.sourceProductID = sourceProductID;
            this.productID = productID;
            this.productCount = productCount;
            this.isNeedPrepare = isNeedPrepare;
            this.isNeedAssignWaferID = isNeedAssignWaferID;
        }

        public StbInfo(String bankID, String sourceProductID, String productID, Long productCount, boolean isNeedPrepare, boolean isNeedAssignWaferID, ObjectIdentifier cassetteID) {
            this.bankID = bankID;
            this.sourceProductID = sourceProductID;
            this.productID = productID;
            this.productCount = productCount;
            this.isNeedPrepare = isNeedPrepare;
            this.isNeedAssignWaferID = isNeedAssignWaferID;
            this.cassetteID = cassetteID;
        }

        public StbInfo() {}
    }

    @Data
    public static class StbMonitorLotInfo{
        private String bankID;
        private String sourceProductID;
        private ObjectIdentifier productID;    // optional
        private int productCount;
    }

    @Data
    public static class StartReservationForInternalBufferInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier emptyCassette;  //emptyCassette and lotID can't be set simultaneously
        private String loadPurposeType;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unLoadPortID;
        public StartReservationForInternalBufferInfo() {}
        public StartReservationForInternalBufferInfo(ObjectIdentifier lotID, ObjectIdentifier emptyCassette, String loadPurposeType, ObjectIdentifier loadPortID, ObjectIdentifier unLoadPortID) {
            this.lotID = lotID;
            this.emptyCassette = emptyCassette;
            this.loadPurposeType = loadPurposeType;
            this.loadPortID = loadPortID;
            this.unLoadPortID = unLoadPortID;
        }
    }

    @Data
    public static class LoadCassetteInfo {
        private ObjectIdentifier cassetteID;
        private String loadPurposeType;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unLoadPortID;
        public LoadCassetteInfo() {}
        public LoadCassetteInfo(ObjectIdentifier cassetteID, String loadPurposeType, ObjectIdentifier loadPortID, ObjectIdentifier unLoadPortID) {
            this.cassetteID = cassetteID;
            this.loadPurposeType = loadPurposeType;
            this.loadPortID = loadPortID;
            this.unLoadPortID = unLoadPortID;
        }
    }

    @Data
    public static class LotHoldInfo {
        private ObjectIdentifier lotID;
        private String reasonCode;
        private String reasonableOperation;

        public LotHoldInfo(ObjectIdentifier lotID, String reasonCode, String reasonableOperation) {
            this.lotID = lotID;
            this.reasonCode = reasonCode;
            this.reasonableOperation = reasonableOperation;
        }
        public LotHoldInfo(){}
    }

    @Data
    public static class ScrapInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier reasonRouteID;
        private ObjectIdentifier reasonCodeID;
        private String reasonOperationNumber;
        private int scrapWaferCount;
        public ScrapInfo(){}

        public ScrapInfo(ObjectIdentifier lotID, ObjectIdentifier reasonRouteID, ObjectIdentifier reasonCodeID, String reasonOperationNumber, int scrapWaferCount) {
            this.lotID = lotID;
            this.reasonRouteID = reasonRouteID;
            this.reasonCodeID = reasonCodeID;
            this.reasonOperationNumber = reasonOperationNumber;
            this.scrapWaferCount = scrapWaferCount;
        }
    }
    @Data
    public static class FlowBatchResultInfo{
        private List<ObjectIdentifier> cassetteList;
        private List<ObjectIdentifier> lotList;
        private List<Infos.TempFlowBatch> tempFlowBatchList;
        private ObjectIdentifier flowBatchID;
    }

    @Data
    public static class PsmUpdateInfo {
        private ObjectIdentifier lotFamilyID;
        private ObjectIdentifier splitRouteID;
        private String splitOperationNumber;
        private ObjectIdentifier originalRouteID;
        private String originalOperationNumber;
        private Boolean actionEMail;
        private Boolean actionHold;
        private Boolean dynamicFlag;
        private List<ObjectIdentifier> waferList;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private ObjectIdentifier subRouteID;



        public PsmUpdateInfo() {
        }

        public PsmUpdateInfo(ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber, Boolean actionEMail, Boolean actionHold, Boolean dynamicFlag, List<ObjectIdentifier> waferList, String returnOperationNumber, String mergeOperationNumber,ObjectIdentifier subRouteID) {
            this.lotFamilyID = lotFamilyID;
            this.splitRouteID = splitRouteID;
            this.splitOperationNumber = splitOperationNumber;
            this.originalRouteID = originalRouteID;
            this.originalOperationNumber = originalOperationNumber;
            this.actionEMail = actionEMail;
            this.actionHold = actionHold;
            this.dynamicFlag = dynamicFlag;
            this.waferList = waferList;
            this.returnOperationNumber = returnOperationNumber;
            this.mergeOperationNumber = mergeOperationNumber;
            this.subRouteID = subRouteID;
        }
    }


    @Data
    public static class LoadForInternalBufferInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private String loadPurposeType;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier portID;
        private boolean isNeedStartReserved;
        public LoadForInternalBufferInfo(){}
        public LoadForInternalBufferInfo(ObjectIdentifier lotID, ObjectIdentifier cassetteID, String loadPurposeType, ObjectIdentifier equipmentID, ObjectIdentifier portID, boolean isNeedStartReserved) {
            this.lotID = lotID;
            this.cassetteID = cassetteID;
            this.loadPurposeType = loadPurposeType;
            this.equipmentID = equipmentID;
            this.portID = portID;
            this.isNeedStartReserved = isNeedStartReserved;
        }
    }

    @Data
    public static class UnloadForInternalBufferInfo {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;
        private ObjectIdentifier equipmentID;
        public UnloadForInternalBufferInfo() {}
        public UnloadForInternalBufferInfo(ObjectIdentifier cassetteID, ObjectIdentifier portID, ObjectIdentifier equipmentID) {
            this.cassetteID = cassetteID;
            this.portID = portID;
            this.equipmentID = equipmentID;
        }
    }


    @Data
    public static class MoveToSelfInfo {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier equipmentID;
        public MoveToSelfInfo() {}
        public MoveToSelfInfo(ObjectIdentifier cassetteID, ObjectIdentifier loadPortID, ObjectIdentifier equipmentID) {
            this.cassetteID = cassetteID;
            this.loadPortID = loadPortID;
            this.equipmentID = equipmentID;
        }
    }

    @Data
    public static class MoveInForInternalBufferInfo {
        private ObjectIdentifier equipmentID;
        private List<ObjectIdentifier> cassetteIDList;
        private boolean processJobPauseFlag = false;    // when not do start reserve, it's flag be default.
        public MoveInForInternalBufferInfo() {}

        public MoveInForInternalBufferInfo(ObjectIdentifier equipmentID, List<ObjectIdentifier> cassetteIDList) {
            this.equipmentID = equipmentID;
            this.cassetteIDList = cassetteIDList;
        }

        public MoveInForInternalBufferInfo(ObjectIdentifier equipmentID, List<ObjectIdentifier> cassetteIDList, boolean processJobPauseFlag) {
            this.equipmentID = equipmentID;
            this.cassetteIDList = cassetteIDList;
            this.processJobPauseFlag = processJobPauseFlag;
        }
    }

    @Data
    public static class SplitMergePointInfo {
        private String mergedOperationNumber;
        private ObjectIdentifier mergeRouteID;

        public SplitMergePointInfo(String mergedOperationNumber, ObjectIdentifier mergeRouteID) {
            this.mergedOperationNumber = mergedOperationNumber;
            this.mergeRouteID = mergeRouteID;
        }
        public SplitMergePointInfo() {}
    }
    @Data
    public static class SplitInfo {
        private StbInfo stbInfo;             // it will stb one product lot, when it's not null.
        private ObjectIdentifier lotID;      // the lot which has been stb, it can't be setting at the same time with stbInfo.
        private String skipOperationNumber;  // it will skip to [skipOperationNumber], if it's not null
        private SplitMergePointInfo splitMergePointInfo;  // it will set split merge point info, if it's not null
        private int childLotWaferSize;       // specified the child lot wafer size, if it's null, then make it randomly...
        private int startPosition = 0;           // start Position, from 0,1,2,3..., the default is 0
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;  // if selected the sub route, then must set it's returnOperationNumber

        //private boolean futureMergeFlag;
        //private boolean branchingRouteSpecifyFlag;

        public SplitInfo(ObjectIdentifier lotID, String skipOperationNumber, int childLotWaferSize) {
            this.lotID = lotID;
            this.skipOperationNumber = skipOperationNumber;
            this.childLotWaferSize = childLotWaferSize;
        }

        public SplitInfo(StbInfo stbInfo, String skipOperationNumber, int childLotWaferSize) {
            this.stbInfo = stbInfo;
            this.skipOperationNumber = skipOperationNumber;
            this.childLotWaferSize = childLotWaferSize;
        }

        public SplitInfo() {}
    }

    @Data
    public static class SplitWithHoldInfo {
        private ObjectIdentifier lotID;
        private List<Infos.LotHoldReq>  lotHoldReqList;
        private int childLotWaferSize;

        //------------------------------------------------------ optianl----------------------
        private SplitMergePointInfo splitMergePointInfo;  // it will set split merge point info, if it's not null
        private ObjectIdentifier subRouteID;
        private ObjectIdentifier releaseReasonCodeID;    // if it's be setting, means call split with hold release, otherwise call split without hold release

        public SplitWithHoldInfo(ObjectIdentifier lotID, int childLotWaferSize, ObjectIdentifier releaseReasonCodeID, List<Infos.LotHoldReq> lotHoldReqList) {
            this.lotID = lotID;
            this.childLotWaferSize = childLotWaferSize;
            this.releaseReasonCodeID = releaseReasonCodeID;
            this.lotHoldReqList = lotHoldReqList;
        }

        public SplitWithHoldInfo() {}
    }

    @Data
    public static class SplitWithNotOnRouteInfo {
        private ObjectIdentifier parentID;
        private int parentLotWaferCount;
        private int childLotWaferSize;
        private boolean isPrepared;

        public SplitWithNotOnRouteInfo(ObjectIdentifier parentID, int parentLotWaferCount, int childLotWaferSize, boolean isPrepared) {
            this.parentID = parentID;
            this.parentLotWaferCount = parentLotWaferCount;
            this.childLotWaferSize = childLotWaferSize;
            this.isPrepared = isPrepared;
        }

        public SplitWithNotOnRouteInfo(){}
    }

    @Data
    public static class SchedulingLotSelection {
        private String lotID;
        private String productID;
        private String mfgLayer;
        private String description;
        private String customerID;
        private String lotOwnerID;
        private String orderNumber;
        private String lotState;
        private String lotFinishState;
        private String mainPDID;
        private String opeNo;
        private String pdID;
        private Timestamp completionTime;
        private Timestamp planEndTime;
        private Timestamp remainCycleTime;
        private Integer qty;
        private Integer cntlQty;
    }

    @Data
    public static class WIPInfos {
        private String lotID;
        private String mainPDID;
        private String opeNo;
        private String scheduleMode;
        private Timestamp planStartTime;
        private Timestamp planEndTime;
        private Integer priorityClass;
        private String customerID;
        private String lotOwnerID;
        private String orderNo;
        private String lotType;
        private String lotProcessState;
        private String flowType;
        private String orderType;
        private String subLotType;
        private String controlJobID;
        private String lotInfoChangeFlag;
    }

    @Data
    public static class BankInInfos {
        private String lotID;
        private String mainPDID;
        private String opeNo;
        private String scheduleMode;
        private Timestamp releasedTime;
        private Timestamp completionTime;
        private Integer priorityClass;
        private String customerID;
        private String lotOwnerID;
        private String orderNo;
        private String orderType;
        private String lotType;
        private String subLotType;
    }

    @Data
    public static class PreLotStartInfos {
        private String prodReqID;
        private String mainPDID;
        private String scheduleMode;
        private Timestamp planReleaseTime;
        private Timestamp deliveryTime;
        private Integer priorityClass;
        private String customerID;
        private String lotOwnerID;
        private String orderNo;
        private String lotComment;
        private String lotType;
        private String orderType;
        private String subLotType;
    }
}