package com.fa.cim.fsm;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.List;
@Slf4j
public class Infos {
    @Data
    public static class ExperimentalFutureLotInfo {
        private ObjectIdentifier lotFamilyID;
        private ObjectIdentifier splitRouteID;
        private ObjectIdentifier originalRouteID;
        private ObjectIdentifier modifyUserID;
        private String splitOperationNumber;
        private String originalOperationNumber;
        private String testMemo;
        private String actionTimeStamp;
        private String modifyTimeStamp;
        private Boolean actionEMail;
        private Boolean actionHold;
        //add auto separate and combine  - jerry
        private Boolean actionSeparateHold;
        private Boolean actionCombineHold;
        private Boolean execFlag;
        private List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq;
        private Object siInfo;
        //add planSplitJobID for history
        private String fsmJobID;
    }

    @Data
    public static class ExperimentalFutureLotDetailInfo {
        private ObjectIdentifier routeID;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private String memo;
        private String actionTimeStamp;
        private List<com.fa.cim.fsm.Infos.Wafer> waferIDs;
        private Boolean dynamicFlag;
        private Boolean execFlag;
        private Object siInfo;
        private Boolean modifyFlag;
    }

    @Data
    public static class ExperimentalFutureLotRegistInfo {
        private String userID;                         //<i>User ID
        private String action;                         //<i>Action
        private String claimedTimeStamp;               //<i>Claimed Time Stamp
        private ObjectIdentifier lotFamilyID;                    //<i>Lot Family ID                               //D7000015
        private ObjectIdentifier splitRouteID;                   //<i>Split Route ID                              //D7000015
        private String splitOperationNumber;           //<i>Split Operation Number
        private ObjectIdentifier originalRouteID;                //<i>Original Route ID                           //D7000015
        private String originalOperationNumber;        //<i>Original Operation Number                   //D7000015
        private boolean actionEMail;                    //<i>Action EMail Flag
        private boolean actionHold;                     //<i>Action Hold Flag
        private String testMemo;                       //<i>Test Memo
        private List<com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist> strExperimentalFutureLotRegistSeq;    //<i>Sequence of Experimental Lot Regist
        private String siInfo;                                                           //<i>Reserved for SI customization
        //add psmJobID for history
        private String fsmJobID;
    }

    @Data
    public static class PilotEventMakeInfo {
//        private String userId;
        private String action;
        private ObjectIdentifier recipeGroupID;
        private ObjectIdentifier lotID;
        private String status;
        private ObjectIdentifier eqpID;
        private String prType;
        private Integer piLotWaferCount;
        private Integer coverLevel;
        private Integer coverRecipe;
        private String fromEqpState;
        private String toEqpState;
        private List<PilotEventMakeRecipeInfo> piLotRecipeIDs;
    }

    @Data
    public static class PilotEventMakeRecipeInfo {
        private ObjectIdentifier recipeIDs;
    }

    @Data
    public static class ExperimentalFutureLotDetailResultInfo {
        private ObjectIdentifier userID;                         //<i>User ID
        private String action;                         //<i>Action          //2001-12-04
        private ObjectIdentifier lotFamilyID;                    //<i>Lot Family ID                               //D7000015
        private ObjectIdentifier splitRouteID;                   //<i>Split Route ID                              //D7000015
        private String splitOperationNumber;           //<i>Split Operation Number
        private ObjectIdentifier originalRouteID;                //<i>Original Route ID                           //D7000015
        private String originalOperationNumber;        //<i>Original Operation Number                   //D7000015
        private boolean actionEMail;                    //<i>Action EMail Flag    //2001-12-04
        private boolean actionHold;                     //<i>ActionHold Flag      //2001-12-04
        private List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetail> strExperimentalFutureLotDetailSeq;    //<i>Sequence of Experimental Lot Detail
        //add psmJobID for history
        private String fsmJobID;
    }

    @Data
    public static class ExperimentalFutureLotRegist {
        private ObjectIdentifier routeID;
        private String returnOperationNumber;                                                           //D7000015
        private String mergeOperationNumber;                                                            //D7000015
        private String memo;
        private List<Wafer> waferIDs;
        private String siInfo;
    }

    @Data
    public static class FutureSplitJobInfoDetail {
        private ObjectIdentifier routeID;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private String memo;
        private boolean dynamicFlag;
        private boolean executedFlag;
        private Timestamp executedTimeStamp;
        private List<Wafer> wafers;
    }

    @Data
    public static class FutureSplitJobInfo {
        private ObjectIdentifier lotFamilyID;
        private ObjectIdentifier splitRouteID;
        private String splitOperationNumber;
        private ObjectIdentifier originalRouteID;
        private String originalOperationNumber;
        private boolean actionEMail;
        private boolean actionHold;
        private Boolean actionSeparateHold;
        private Boolean actionCombineHold;
        private String testMemo;
        private boolean executedFlag;
        private Timestamp executedTimeStamp;
        private Timestamp lastClaimedTimeStamp;
        private ObjectIdentifier modifier;
        private List<FutureSplitJobInfoDetail> futureSplitJobInfoDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wafer {
        private ObjectIdentifier waferID;
        private long slotNumber;
        private String groupNo;
    }

    @Data
    public static class ExperimentalFutureLotDetail {
        private ObjectIdentifier routeID;
        private String returnOperationNumber;                                                           //D7000015
        private String mergeOperationNumber;                                                            //D7000015
        private String actionTimeStamp;
        private String memo;
        private ObjectIdentifier parentLotID;                                                                     //D7000015
        private ObjectIdentifier childLotID;
        private List<com.fa.cim.fsm.Infos.ExperimentalFutureLotWafer> strExperimentalLotWaferSeq;
        private Object siInfo;
    }

    @Data
    public static class ExperimentalFutureLotWafer {
        private Wafer waferId;
        private String status;
        private Object siInfo;
        private String  groupNo;
    }

    @Data
    public static class FutureScrapCancelWafers {
        private ObjectIdentifier waferID;          //<i>wafer ID
        private Integer slotNumber;                  //<i>wafer's slot number
        private ObjectIdentifier reasonCodeID;     //<i>Reason CimCode ID
        private ObjectIdentifier reasonedLotID;    //<i>Reasoned lot ID
        private String scrappedTimeStamp;         //<i>Time stamp of scrapped

        public FutureScrapCancelWafers() {
        }

        public FutureScrapCancelWafers(ObjectIdentifier waferID, Integer slotNumber, ObjectIdentifier reasonCodeID, ObjectIdentifier reasonedLotID, String scrappedTimeStamp) {
            this.waferID = waferID;
            this.slotNumber = slotNumber;
            this.reasonCodeID = reasonCodeID;
            this.reasonedLotID = reasonedLotID;
            this.scrappedTimeStamp = scrappedTimeStamp;
        }
    }

    @Data
    public static class FuturePartialReworkReq {
        private ObjectIdentifier parentLotID;              //<i>Parent lot ID
        private List<Wafer> childWaferID;             //<i>Sequence of Child wafer ID
        private ObjectIdentifier RouteID;               //<i>Route ID
        private String returnOperationNumber;    //<i>Return Operation Number
        private ObjectIdentifier reasonCodeID;             //<i>Reason CimCode ID
        private String eventTxId;                //<i>Event Tx ID
        private Boolean bForceRework;             //<i>Force Rework Flag
        private Boolean bDynamicRoute;            //<i>Dynamic Route Flag
    }
}
