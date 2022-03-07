package com.fa.cim.sorter;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.PostProcessSource;
import com.fa.cim.jpa.SearchCondition;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 *
 * <p>change history: date defect# person comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/6/22 ******** Aoki create file
 *
 * @author: Jerry
 * @date: 2021/6/22 14:44
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Params {

    @Data
    public static class ObjSorterJobListGetDRIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier createUser;
        private ObjectIdentifier sorterJob;
        private ObjectIdentifier controlJob;
        private String portGroup;
        private String actionCode;
    }

    @Data
    public static class OnlineSorterSlotmapCompareReqParams {
        private User user;
        private Info.SortJobInfo sortJobInfo;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class SJStartReqParams {
        private User user;
        private Infos.SJStartReqInParm sjStartReqInParm;
        private String claimMemo;
    }

    @Data
    public static class ReticleSortInfo {
        private ObjectIdentifier reticleID;                  //<i>Reticle ID
        private ObjectIdentifier destinationReticlePodID;    //<i>Destination Reticle Pod ID
        private int destinationSlotNumber;      //<i>Destination Slot Number
        private ObjectIdentifier originalReticlePodID;       //<i>Original Reticle Pod ID
        private int originalSlotNumber;         //<i>Original Slot Number
    }

    @Data
    public static class OnlineSorterActionSelectionInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class WaferSorterActionRegisterReqParams {
        private User user;
        private List<Info.SortActionAttributes> strWaferSorterActionListSequence;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class SJListInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier createUser;
        private ObjectIdentifier sorterJobID;
        private String actionCode;
    }

    @Data
    public static class SortJobPriorityChangeReqParam {
        private User user;
        private List<ObjectIdentifier> jobIDs;
        private String jobType;
        private String claimMemo;
    }

//    @Data
//    public static class SJCancelReqParm {
//        private User user;
//        private ObjectIdentifier sorterJobID;
//        private Boolean notifyToTCSFlag;
//        private String claimMemo;
//        private List<ObjectIdentifier> sorterComponentJobIDseq;
//        private boolean externalCallFlag;
//        private boolean resEAPFlag;
//        private boolean runtimeCancelFlag;
//    }

    @Data
    public static class SJCancelReqParm {
        private User user;
        private Boolean notifyEAPFlag;
        private ObjectIdentifier jobID;
        private String opeMemo;
        private String jobType;
    }

    @Data
    public static class SJCancelRptParam {
        private User user;
        private Boolean notifyEAPFlag;
        private ObjectIdentifier jobID;
        private String jobType;   //区分是SORT/COMPONENT
        private String opeMemo;
    }

    @Data
    public static class SorterActionInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;

    }

    @Data
    public static class ScanBarCodeInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier portID;
        private ObjectIdentifier vendorLotID;//条形码的lotId
    }


    @Data
    public static class OnlineSorterSlotmapAdjustReqParam {
        private User user;              //<i>R/Request User ID
        private ObjectIdentifier equipmentID;   //<i>R/Equipment ID
        private String portGroup;    //<i>R/Port Group
        private Info.ComponentJob strWaferXferSeq;  //<i>R/Sequence of Wafer Transfer
        private String adjustDirection;    //<i>R/Adjust Direction
        private String claimMemo;
    }

    @Data
    public static class SortJobCheckConditionReqInParam {
        private User user;
        private List<Info.ComponentJob> strSorterComponentJobListAttributesSequence;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private String claimMemo;
    }


    @Data
    public static class SJStatusChgRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier jobID;
        private String portGroup;
        @NotNull(message = "The jobType cannot be empty")
        private String jobType;
        @NotNull(message = "The jobStatus cannot be empty")
        private String jobStatus;
        private String opeMemo;
    }


    @Data
    public static class ObjSorterSorterJobEventMakeIn {
        private String                          transactionID;
        private Params.SJCreateReqParams      strSortJobListAttributes;
        private String                          actionCode;
        private String                          claimMemo;
        private Object                          siInfo;
    }


    @Data
    public static class SortJobHistoryParams {
        private User user;
        private ObjectIdentifier equipmentID;
        @NotNull(message = "startTimeStamp is null")
        private String startTimeStamp;
        @NotNull(message = "endTimeStamp is null")
        private String endTimeStamp;
        private SearchCondition searchCondition;
    }


    @Data
    public static class SJCreateReqParams implements PostProcessSource {
        private User user;
        private List<Info.ComponentJob> strSorterComponentJobListAttributesSequence;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String portGroupID;
        private Boolean waferIDReadFlag;
        private String claimMemo;
        private Info.SortJobPostAct postAct; //千岛二期增加
        private ObjectIdentifier sorterJobID;
        private String operationMode;
        private String sorterJobStatus;
        private Integer componentCount;
        private String operation;     //标识事件的操作类型

        @Override
        public List<ObjectIdentifier> durableIDs() {
            List<ObjectIdentifier> retVal = new ArrayList<>();
            if (CimArrayUtils.isNotEmpty(strSorterComponentJobListAttributesSequence)) {
                strSorterComponentJobListAttributesSequence.forEach(data -> {
                    retVal.add(data.getDestinationCassetteID());
                    retVal.add(data.getOriginalCassetteID());
                });
            }
            return retVal;
        }
    }

    @Data
    public static class WaferSlotMapChange {
        private User user;
        private Boolean bNotifyToTCS;
        private Info.SortJobInfo sortJobInfo;
    }

    @Data
    public static class CarrierExchangeParams {
        private User user;
        private Info.SortJobInfo sortJobInfo;
    }
}
