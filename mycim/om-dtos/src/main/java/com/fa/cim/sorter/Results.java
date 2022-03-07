package com.fa.cim.sorter;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
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
 * 2021/3/1        ********            Aoki                create file
 *
 * @author: Aoki
 * @date: 2021/3/1 14:45
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Results {

    @Data
    public static class OnlineSorterActionSelectionInqResult {
        private ObjectIdentifier equipmentID;                         //<i>Equipment ID
        private List<Info.SortActionAttributes> strWaferSorterActionListSequence;    //<i>Sequence of Wafer Sorter Action List
    }


    @Data
    public static class ObjSorterWaferTransferInfoRestructureOut {
        private List<String> lotInventoryStateList;
        private List<ObjectIdentifier> cassetteIDList;
        private List<Infos.PLot> lotList;
        private Object siInfo;
    }

    @Data
    public static class SortJobHistoryResult {
        private ObjectIdentifier sorterJobID;
        private ObjectIdentifier requestUserID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier equipmentID;                          //<i>Equipment ID
        private String portGroup;                          //<i>Port Group ID
        private Integer componentJobCount;
        private Timestamp reqTime;
        private String sorterJobStatus;
        private SortJobPostAct postAct;
        private List<Info.SorterComponentJobListAttributes> componentJobs;    //<i> Sorter Component Job Information
    }

    @Data
    public static class SortJobPostAct {
        private String sorterJobID;
        private String actionCode;
        private String productOrderId;
        private String vendorId;
        private int waferCount;
        private String sourceProductId;
    }

    @Data
    public static class SortJobInfo {
        private User user;
        private ObjectIdentifier sorterJobID;
        private ObjectIdentifier requestUserID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier equipmentID;                          //<i>Equipment ID
        private String portGroup;                          //<i>Port Group ID
        private Boolean waferIDReadFlag;                      //<i>Wafer ID Read Flag
        private String opeMemo;
        private Info.ComponentJob componentJob;    //<i> Sorter Component Job Information
    }
}
