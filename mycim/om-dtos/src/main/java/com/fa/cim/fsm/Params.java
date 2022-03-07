package com.fa.cim.fsm;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class Params {
    @Data
    public static class FSMLotInfoSetReqParams {
        private User user;                                //<i>R/Request User ID
        private ObjectIdentifier lotFamilyID;                        //<i>R/Lot Family ID                                 //D7000015
        private ObjectIdentifier splitRouteID;                       //<i>R/Split Route ID                                //D7000015
        private String splitOperationNumber;              //<i>R/Split Operation Number
        private ObjectIdentifier originalRouteID;                   //<i>R/Original Route ID                             //D7000015
        private String originalOperationNumber;          //<i>R/Original Operation Number                     //D7000015
        private Boolean actionEMail;                        //<i>R/ActionEMail
        private Boolean actionHold;                         //<i>R/ActionHold
        private Boolean actionSeparateHold;                         //<i>R/ActionSeparateHold
        private Boolean actionCombineHold;                         //<i>R/ActionCombineHold
        private String testMemo;                           //<i>R/Test Memo
        private Boolean execFlag;                           //<i>R/Eexec Flag
        private String actionTimeStamp;                   //<i>R/Action Time Stamp
        private ObjectIdentifier modifyUserID;                      //<i>R/Modify User ID
        private List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq;    //<i>R/strExperimentalFutureLotDetailInfoSeq
        private String claimMemo;                          //<i>O/Claim Memo     //D6000025
    }

    @Data
    public static class FSMLotActionReqParams {
        private User user;
        private ObjectIdentifier lotId;
        private String claimMemo;
    }

    @Data
    public static class FSMLotRemoveReqParams {
        private User user;                        //<i>R/Request User ID
        private ObjectIdentifier lotFamilyID;                //<i>R/Lot Family ID                                                        //D7000015
        private ObjectIdentifier splitRouteID;               //<i>R/Split Route ID                                                       //D700001;
        private String splitOperationNumber;      //<i>R/Split Operation Number
        private ObjectIdentifier originalRouteID;            //<i>R/Original Route ID                                                    //D7000015
        private String originalOperationNumber;   //<i>R/Original Operation Number                                            //D7000015
        private String claimMemo;                   //<i>O/Claim Memo      //D6000025
    }

    @Data
    public static class FSMLotInfoInqParams {
        private User user;                              //<i>R/Request User ID
        private ObjectIdentifier lotFamilyID;           //<i>R/Lot Family ID
        private ObjectIdentifier splitRouteID;          //<i>R/Split Route ID
        private String splitOperationNumber;           //<i>R/Split Operation Number //D6000025
        private ObjectIdentifier originalRouteID;       //<i>R/Original Route ID
        private String originalOperationNumber;         //<i>R/Original Operation Number
    }

    @Data
    public static class FSMLotDefinitionListInqParams {
        private User user;
        private ObjectIdentifier lotFamilyID;
        private String claimMemo;
        private Boolean detailRequireFlag;
    }

//    @Data
//    public static class FutureSplitLotReqParams {
//        private User user;               //<i>R/Request User ID
//        private ObjectIdentifier parentLotID;                 //<i>R/Parent lot ID
//        private List<Infos.Wafer> childWaferIDs;                //<i>R/Sequence of Child wafer ID
//        private Boolean futureMergeFlag;             //<i>R/Future Merge Flag
//        // support for auto combine function
//        private Boolean combineHold;             //<i>R/combine
//        private ObjectIdentifier mergedRouteID;               //<i>R/Merged Route ID
//        private String mergedOperationNumber;       //<i>R/Merged Operation Number
//        private Boolean branchingRouteSpecifyFlag;   //<i>R/Branching Route Specify Flag
//        private ObjectIdentifier RouteID;                  //<i>R/Route ID
//        private String returnOperationNumber;       //<i>R/Return Operation Number
//        private String claimMemo;        //<i>O/Claim Memo       //D6000025
//    }

//    @Data
//    public static class FutureSplitLotWithHoldReleaseReqParams {
//        private User user;
//        private ObjectIdentifier parentLotID;
//        private List<Infos.Wafer> childWaferIDs;
//        private Boolean futureMergeFlag;
//        private ObjectIdentifier mergedRouteID;
//        private String mergedOperationNumber;
//        private Boolean branchingRouteSpecifyFlag;
//        private ObjectIdentifier RouteID;
//        private String returnOperationNumber;
//        private String claimMemo;
//        private ObjectIdentifier releaseReasonCodeID;
//        private List<com.fa.cim.dto.Infos.LotHoldReq> strLotHoldReleaseReqList;
//    }

//    @Data
//    public static class FutureSplitLotWithoutHoldReleaseReqParams {
//        private User user;
//        private ObjectIdentifier parentLotID;
//        private List<Infos.Wafer> childWaferIDs;
//        private Boolean futureMergeFlag;
//        private ObjectIdentifier mergedRouteID;
//        private String mergedOperationNumber;
//        private Boolean branchingRouteSpecifyFlag;
//        private ObjectIdentifier RouteID;
//        private String returnOperationNumber;
//        private String claimMemo;
//        private ObjectIdentifier releaseReasonCodeID;
//        private List<com.fa.cim.dto.Infos.LotHoldReq> strLotHoldReleaseReqList;
//    }

//    @Data
//    public static class UnFutureScrapWaferReqParams {
//        private User user;
//        private ObjectIdentifier lotID;
//        private ObjectIdentifier cassetteID;
//        private List<com.fa.cim.fsm.Infos.FutureScrapCancelWafers> scrapCancelWafers;
//        private String claimMemo;
//    }

//    @Data
//    public static class FuturePartialReworkReqParams {
//        private User user;
//        private com.fa.cim.fsm.Infos.FuturePartialReworkReq partialReworkReqInformation;
//        private String claimMemo;
//    }

//    @Data
//    public static class FuturePartialReworkWithHoldReleaseReqParams {
//        private User user;
//        private com.fa.cim.fsm.Infos.FuturePartialReworkReq partialReworkReq;
//        private ObjectIdentifier releaseReasonCodeID;
//        private List<com.fa.cim.dto.Infos.LotHoldReq> holdReqList;
//        private String claimMemo;
//    }

}
