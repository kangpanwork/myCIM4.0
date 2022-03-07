package com.fa.cim.sorter;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.pp.PostProcessSource;
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
 * @date: 2021/3/1 14:44
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Info {

    @Data
    public static class SorterComponentJobListAttributes {
        private ObjectIdentifier sorterComponentJobID;                // Sorter Component Job ID
        private String requestTimeStamp;                    // <String Type> Request Time Stamp
        private ObjectIdentifier originalCarrierID;                  // original Carrier ID
        private ObjectIdentifier originalPortID;                      // original port ID
        private String originalCarrierTransferState;      // original Carrier Xfer Status
        private ObjectIdentifier originalCarrierEquipmentID;        // original Carrier eqp ID
        private ObjectIdentifier originalCarrierStockerID;          // original Carrier Stoker ID
        private ObjectIdentifier destinationCarrierID;               // destination Carrier ID
        private ObjectIdentifier destinationPortID;                  // destination port ID
        private String destinationCarrierTranferStatus;   // destination Carrier Xfer Status
        private ObjectIdentifier destinationCarrierEquipmentID;   // destination Carrier eqp ID
        private ObjectIdentifier destinationCarrierStockerID;      // destination Carrier stocker ID
        private String componentSorterJobStatus;          // component sorter job status
        private ObjectIdentifier preSorterComponentJobID;          // Pre Sorter Component Job ID
        private List<Info.WaferSorterSlotMap> waferSorterSlotMapList;
        private String actionCode;
        private String requestUserID;
        private String opeMemo;
        private String replyTimeStamp;
    }

    @Data
    public static class WaferSorterSlotMap {
        private ObjectIdentifier lotID;                                //<i>lot ID
        private ObjectIdentifier waferID;                              //<i>wafer ID
        private Integer destinationSlotNumber;                //<i>Destination Slot Number
        private Integer originalSlotNumber;                   //<i>Original Slot Number
        private String aliasName;                            // T7_Code
        private Boolean status = true ;                       //<i>Sorted Status
        private String direction;
    }

    @Data
    public static class SortJobPostAct {
        private String sorterJobID;
        private String actionCode;
        private String productOrderId;
        private String vendorId;
        private int waferCount;
        private String sourceProductId;
        private ObjectIdentifier childLotId;
        private ObjectIdentifier parentLotId;
    }

    @Data
    public static class ComponentJob {
        private String componentJobID;                //<i>Component Job ID
        private String componentJobStatus;      //<i>Sorter Component Job Status
        private ObjectIdentifier originalCassetteID;            //<i>Original Cassette ID
        private ObjectIdentifier destinationCassetteID;         //<i>Destination Cassette ID
        private ObjectIdentifier destinationPortID;                    //<i>Destination port ID
        private ObjectIdentifier originalPortID;                       //<i>Original port ID
        private String replyTimeStamp;
        private String requestTimeStamp;              //<i>Request Time Stamp
        private String actionCode;

        /**
         * EAP return execute result
         * 0/不报：默认成功
         * 1：lot start transfer成功但有错误
         * 2：lot start transfer失败，EAP报告error message
         * 3：lot start transfer部分成功部分失败，EAP报告error message
         */
        private int resultCode;
        private String opeMemo;
        private List<Info.WaferSorterSlotMap> waferList; //<i> Slot Map Information
        private ObjectIdentifier preSorterComponentJobID;
        private String operation;     //标识事件的操作类型
    }



    @Data
    public static class SlotmapHisRecord {
        private String action;
        private String waferId;
        private String destPosition;
    }

    @Data
    public static class SortActionAttributes {
        private ObjectIdentifier equipmentID;         //<i>Equipment ID
        private String actionCode;          //<i>Action Code
        private String physicalRecipeID;    //<i>Physical Recipe ID
        private ObjectIdentifier userID;              //<i>User ID
        private String storeTime;           //<i>Store Time
    }

    @Data
    public static class WaferSorterCompareSlotMap {
        private int mmDestinationPosition;       //<i>OMS Destination Position
        private ObjectIdentifier mmDestinationLotID;          //<i>OMS Destination Lot ID
        private ObjectIdentifier mmDestinationWaferID;        //<i>OMS Destination Wafer ID
        private String mmDestinationAliasName;        //<i>OMS Destination Wafer ID  由于eap不报wafer,故以T7_code代替
        private int tcsDestinationPosition;      //<i>TCS Destination Position
        private ObjectIdentifier tcsDestinationLotID;         //<i>TCS Destination Lot ID
        private ObjectIdentifier tcsDestinationWaferID;       //<i>TCS Destination Wafer I
        private String tcsDestinationAliasName;       //<i>TCS Destination Wafer I 由于eap不报wafer,故以T7_code代替
        private String compareStatus;               //<i>Compare Status
        private Object siInfo;
    }

    @Data
    public static class WaferSorterCompareCassette {
        private ObjectIdentifier cassetteID;                              //<i>Carrier ID
        private ObjectIdentifier portID;                                  //<i>Port ID                          //D4100098
        private List<Info.WaferSorterCompareSlotMap> strWaferSorterCompareSlotMapSequence;    //<i>Sequence of Wafer Sorter Compare SlotMap
        private Object siInfo;
    }

    @Data
    public static class SorterComponentJobType {
        private ObjectIdentifier sortJobID;
        private ObjectIdentifier componentJobID;
        private String jobType;
        private String jobStatus;
    }

    @Data
    public static class SortJobListAttributes {
        private ObjectIdentifier sorterJobID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier portGroupID;
        private String sorterJobStatus;
        private ObjectIdentifier requestUserID;
        private String requestTimeStamp;
        private int componentCount;
        private ObjectIdentifier preSorterJobID;
        private boolean waferIDReadFlag;
        private List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList;
        private String ctrljobId;
        private Info.SortJobPostAct postAct; //千岛二期增加
        private String operationMode;
    }

    @Data
    public static class SorterSorterJobLockDRIn {
        private ObjectIdentifier      sorterJobID;
        private ObjectIdentifier      sorterComponentJobID;
        private ObjectIdentifier      cassetteID;
        private Integer   lockType;
    }

    @Data
    public static class ObjSorterComponentJobInfoGetByComponentJobIDDROut {
        private ComponentJob   componentJob;
        private ObjectIdentifier                         sorterJobID;
        private Object                                   siInfo;
    }

    @Data
    public static class SorterLinkedJobUpdateDRIn {
        private String                          jobType;
        private List<ObjectIdentifier>          jobIDs;
        private Object                          siInfo;
    }

    @Data
    public static class SorterComponentJobDeleteDRIn {
        private ObjectIdentifier            sorterJobID;
        private List<ObjectIdentifier>      componentJobIDseq;
        private Object                      siInfo;
    }

    @Data
    public static class WaferTransfer {
        private ObjectIdentifier waferID;
        private ObjectIdentifier destinationCassetteID;
        private Boolean bDestinationCassetteManagedByOM;
        private Boolean bOriginalCassetteManagedByOM;
        private Integer destinationSlotNumber;
        private ObjectIdentifier originalCassetteID;
        private Integer originalSlotNumber;
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
        private String sorterJobStatus;
        private Boolean fosbActionStartMethodFlag = true;  //针对于FOSB,当actionCode=waferStart 默认为true需要等待 此时就为false需要等待流程执行
        private Info.ComponentJob componentJob;    //<i> Sorter Component Job Information

    }

    @Data
    public static class LotAliasName {
        private ObjectIdentifier reservaLotID ;//预约的lotID;
        private ObjectIdentifier relationLotID  ;//T7Code与lotID绑定了关系;
        private Boolean compareResults;
    }

    @Data
    public static class ObjObjectValidSorterJobGetOut {
        private List<SortJobListAttributes> strValidSorterJob;
        private List<SortJobListAttributes> strOtherSorterJob;
    }


    @Data
    public static class ObjectValidSorterJobGetIn {
        private String classification;
        private ObjectIdentifier objectID;
    }

    @Data
    public static class SlotMapHistory {
        private String sortJobHisID;
        private String componentJobHisID;
        private String lotID;
        private String waferID;
        private Integer destPosition;
        private Integer srcPosition;
        private String aliasName;
        private Timestamp eventCreateTime;
    }

    @Data
    public static class CarrierAndLotHistory {
        private List<ObjectIdentifier> carrierIDs;
        private List<ObjectIdentifier> lotIDs;
    }

    @Data
    public static class PostActDRIn {
        private ObjectIdentifier sortJobID;
        private String opeMemo;
        private String actionCode;
    }

}
