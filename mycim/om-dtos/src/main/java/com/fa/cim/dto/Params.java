package com.fa.cim.dto;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.pp.PostProcessSource;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/27        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/27 13:20
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Params {
    @Data
    public static class CarrierUnloadingForSORRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;
        private String opeMemo;
    }

    @Data
    public static class CarrierLoadingForSORRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;
    }


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
        private List<Infos.CarrierJobResult> strCarrierJobResult;
    }

    @Data
    public static class BankListInqParams {
        private User user;
        private SearchCondition searchCondition;
        private String inqBank;   // - the inquire bank type. it must be one of "N", "B", "P", otherwise the inqBank is invalid.
        //     - B: All Banks,
        //     - N: Non-Production Banks
        //     - P: Production Banks
        private ObjectIdentifier bankID;
    }

    @Data
    public static class SortJobHistoryParams {
        private User user;
        private String srcCastID;
        private String destCastID;
        private String sortJobCategory;
    }

    @Data
    public static class CimObjectListInqInParms {
        private User user;
        private Infos.CimObjectListInqInParm cimObjectListInqInParm;
    }

    @Data
    public static class EDCSpecCheckActionResultInqInParms {
        private User user;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
        private String siInfo;
    }

    @Data
    public static class SpecCheckResultInqInParms {
        private User user;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
        private String siInfo;
    }

    @Data
    public static class DurableStatusSelectionInqInParms {
        private User user;
        private ObjectIdentifier durableID;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/15 13:15:43
     */
    @Data
    public static class CarrierBasicInfoInqParms {
        private User user;
        private List<ObjectIdentifier> cassetteIDSeq;
        private String siInfo;
    }

    @Data
    public static class productInqParams {
        private User user;
        private String all;
        private String raw;
    }

    @Data
    public static class SplitLotWithHoldReleaseReqParams {
        private User user;
        private ObjectIdentifier parentLotID;
        private List<ObjectIdentifier> childWaferIDs;
        private Boolean futureMergeFlag;
        private ObjectIdentifier mergedRouteID;
        private String mergedOperationNumber;
        private Boolean branchingRouteSpecifyFlag;
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;
        private String claimMemo;
        private ObjectIdentifier releaseReasonCodeID;
        private List<Infos.LotHoldReq> strLotHoldReleaseReqList;
    }

    @Data
    public static class SplitLotWithoutHoldReleaseReqParams {
        private User user;
        private ObjectIdentifier parentLotID;
        private List<ObjectIdentifier> childWaferIDs;
        private Boolean futureMergeFlag;
        private ObjectIdentifier mergedRouteID;
        private String mergedOperationNumber;
        private Boolean branchingRouteSpecifyFlag;
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;
        private String claimMemo;
        private ObjectIdentifier releaseReasonCodeID;
        private List<Infos.LotHoldReq> strLotHoldReleaseReqList;
    }

    @Data
    public static class BranchCancelReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;             //<i>R/lot ID
        private ObjectIdentifier currentRouteID;    //<i>R/Current Route ID
        private String currentOperationNumber;       //<i>R/Current Operation Number
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class AutoCreateMonitorForInProcessLotReqParams {
        private User user;
        private ObjectIdentifier processEquipmentID;     //<i>R/Process eqp ID
        private String stbLotSubLotType;       //<i>R/STB lot Sub lot Type
        private Infos.NewLotAttributes strNewLotAttributes;    //<i>R/New lot Attributes
        private List<ObjectIdentifier> productLotIDs;          //<i>R/Sequence of Source Product IDs
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
     * @date 2018/10/15 16:34:30
     */
    @Data
    public static class MultipleCarrierStatusChangeRptParms implements PostProcessSource {
        private User user;
        private String cassetteStatus;
        private List<ObjectIdentifier> cassetteID;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> durableIDs() {
            return this.cassetteID;
        }

    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/16 10:22:51
     */
    @Data
    public static class CarrierUsageCountResetReqParms {
        private User user;
        private ObjectIdentifier cassetteID;
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
     * @date 2018/10/16 13:38:21
     */
    @Data
    public static class EDCPlanInfoInqParms {
        private User user;
        private ObjectIdentifier dcDefID;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/16 15:51:53
     */
    @Data
    public static class EDCSpecInfoInqParms {
        private User user;
        private ObjectIdentifier dcSpecID;
        private String siInfo;
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
        private SearchCondition searchCondition;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/22 11:05:49
     */
    @Data
    public static class ReticleStatusChangeRptParams {
        private User user;
        private ObjectIdentifier reticleID;
        private String reticleStatus;
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
     * @date 2018/10/26 14:09:50
     */
    @Data
    public static class ReticleDetailInfoInqParams {
        private User user;
        private ObjectIdentifier reticleID;
        private boolean durableOperationInfoFlag;
        private boolean durableWipOperationInfoFlag;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/26 15:24:53
     */
    @Data
    public static class MultipleReticleStatusChangeRptParams {
        private User user;
        private String reticleStatus;
        private List<ObjectIdentifier> reticleID;
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
     * @date 2018/10/26 15:51:49
     */
    @Data
    public static class ReticleUsageCountResetReqParams {
        private User user;
        private ObjectIdentifier reticleID;
        private String claimMemo;
    }

    @Data
    public static class ReticleInspectionReuqstReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private String inspectionType;
        private String claimMemo;
    }

    @Data
    public static class ReticleInspectionInReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private ObjectIdentifier eqpID;
        private String claimMemo;
    }

    @Data
    public static class ReticleConfirmMaskQualityParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private String claimMemo;
    }

    @Data
    public static class ReticleInspectionOutReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private boolean operationFlag;
        private String claimMemo;
    }

    @Data
    public static class ReticleRequestRepairReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private String claimMemo;
    }

    @Data
    public static class ReticleRepairInReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private String claimMemo;
    }

    @Data
    public static class ReticleRepairOutReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private String claimMemo;
    }

    @Data
    public static class ReticleHoldReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private List<Infos.ReticleHoldReq> holdReqList;
    }

    @Data
    public static class ReticleHoldReleaseReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private ObjectIdentifier releaseReasonCodeID;
        private List<Infos.ReticleHoldReq> holdReqList;
        private String claimMemo;
    }

    @Data
    public static class ReticleHoldListInqParams {
        private User user;
        private ObjectIdentifier reticleID;
        private SearchCondition searchCondition;
    }

    @Data
    public static class reticleUpdateParamsInqParams {
        private User user;
    }

    @Data
    public static class ReticleTerminateReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private List<Infos.ReticleReasonReq> terminateReqList;
    }

    @Data
    public static class ReticleScrapReqParams {
        private User user;
        private List<ObjectIdentifier> reticleIDList;
        private List<Infos.ReticleReasonReq> reqList;
    }

    @Data
    public static class ReticleScanRequestReqParams {
        private User user;
        private ObjectIdentifier reticleID;
        private String claimMemo;
    }

    @Data
    public static class ReticleScanCompleteReqParams {
        private User user;
        private ObjectIdentifier reticleID;
        private boolean resultFlag;
        private String claimMemo;
        private ObjectIdentifier releaseReasonCodeID;
        private Infos.ReticleReasonReq reticleHoldReason;
    }

    @Data
    public static class ReticleInspectionTypeChangeReqParams {
        private User user;
        private ObjectIdentifier reticleID;
        private String inspectionType;
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
     * @date 2018/10/29 10:49:37
     */
    @Data
    public static class ReticleTransferStatusChangeRptParams {
        private User user;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.XferReticle> strXferReticle;
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
     * @date 2018/10/30 13:17:23
     */
    @Data
    public static class MoveInForIBReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
        private boolean processJobPauseFlag;
        private String opeMemo;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/4 14:01:16
     */
    @Data
    public static class LotFuturePctrlListInqParams {
        private User user;
        private Infos.LotFuturePctrlListInqInParm strLotFuturePctrlListInqInParm;
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
        private String opeMemo;
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
        private String opeMemo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/24 16:19:31
     */
    @Data
    public static class ProcessOperationListForDurableDRParams {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Boolean searchDirection;
        private Boolean posSearchFlag;
        private Boolean currentFlag;
        private Integer searchCount;
        private ObjectIdentifier searchRouteID;
        private String searchOperationNumber;
        private String siInfo;
    }

    @Data
    public static class CarrierStatusChangeRptInParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier cassetteID;
        private String cassetteStatus;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> durableIDs() {
            return Collections.singletonList(this.cassetteID);
        }
    }

    @Data
    public static class ChamberWithProcessWaferRptInParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.ChamberProcessLotInfo> chamberProcessLotInfos;
        private String opeMemo;
    }

    @Data
    public static class StockerInfoInqInParams {
        private User user;
        private ObjectIdentifier machineID;
        private boolean detailFlag;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/9 10:05:21
     */
    @Data
    public static class StockerListInqInParams {
        private User user;
        private String stockerType;
        private boolean availFlag;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/17 13:25
     */
    @Data
    public static class WhereNextOHBCarrierInqInParm {
        private User user;
        private ObjectIdentifier cassetteID;
        private Boolean whatNextInqFlag;
        private Object siInfo;
    }


    @Data
    public static class EqpEAPInfoInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class BankMoveReqParams {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private ObjectIdentifier bankID;
        private String claimMemo;
    }

    @Data
    public static class AllAvailableEqpParams {
        private User user;
    }

    @Data
    public static class MMQTimeParams {
        private User user;
        private String LotID;
    }

    @Data
    public static class StockerForAutoTransferInqParams {
        private User user;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/11 14:08:46
     */
    @Data
    public static class EqpForAutoTransferInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier durableID;
    }

    @Data
    public static class BayListInqParams {
        private User user;
    }

    @Data
    public static class CarrierListInqParams {
        private User user;
        private String cassetteCategory; //Carrier Category
        private String carrierType; //Carrier Type
        private boolean emptyFlag;
        private ObjectIdentifier stockerID;        //stocker ID
        private ObjectIdentifier cassetteID;       //carrier id(wild-card can be used)
        private String cassetteStatus;  //Carrier Status
        //<c>CIMFW_Durable_InUse           "INUSE"
        //<c>CIMFW_Durable_Scrapped        "SCRAPPED"
        //<c>CIMFW_Durable_Available       "AVAILABLE"
        //<c>CIMFW_Durable_NotAvailable    "NOTAVAILABLE"
        private ObjectIdentifier durablesSubStatus; // durable Sub Status
        private String flowStatus;  // Flow Status
        private long maxRetrieveCount;  //Max retrieve count
        private boolean sorterJobCreationCheckFlag;     //Sorter job creation check flag
        private String interFabTransferState;   //interFab Transfer State
        private ObjectIdentifier bankID;  // BankID
        private String usageType;
        private SearchCondition searchCondition;    // add by myCIM
        private Boolean needTransferState; // need EI transfer stat    default need EI

    }

    @Data
    public static class CarrierDetailInfoInqParams {
        private User user;
        private ObjectIdentifier cassetteID;
        private Boolean durableOperationInfoFlag;
        private Boolean durableWipOperationInfoFlag;

    }

    @Data
    public static class ChamberStatusChangeReqPrams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.EqpChamberStatus> eqpChamberStatuses;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/27 10:41
     */
    @Data
    public static class SeasonPlanPrams {
        private User user;
        private Infos.Season season;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/27 10:41
     */
    @Data
    public static class SeasonPlansPrams {
        private User user;
        private List<Infos.Season> seasons;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/27 10:41
     */
    @Data
    public static class RecipeGroupPrams {
        private User user;
        private RecipeGroup.Info  recipeGroup;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/27 10:41
     */
    @Data
    public static class SeasonJobPrams {
        private User user;
        private Infos.SeasonJob seasonJob;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/27 10:41
     */
    @Data
    public static class SeasonPriorityParams {
        private User user;
        private List<Infos.Season> seasonList;
        private String claimMemo;
    }


    @Data
    public static class ChamberStatusChangeRptPrams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.EqpChamberStatus> eqpChamberStatuses;
    }

    @Data
    public static class CJStatusChangeReqParams {
        private User user;
        private ObjectIdentifier controlJobID;
        private String claimMemo;
        private String controlJobAction;     //action type of controlJob
        //<c>SP_ControlJobAction_Type_create
        //<c>SP_ControlJobAction_Type_queue
        //<c>SP_ControlJobAction_Type_execute
        //<c>SP_ControlJobAction_Type_complete
        //<c>SP_ControlJobAction_Type_delete
        //<c>SP_ControlJobAction_Type_delete_From_EQP
        //<c>SP_ControlJobAction_Type_delete_From_LotAndCassette
        //<c>SP_ControlJobAction_Type_priority
        //<c>SP_ControlJobAction_Type_stop
        //<c>SP_ControlJobAction_Type_abort
        private Infos.ControlJobCreateRequest controlJobCreateRequest;  // The information for controlJob creation.
    }

    @Data
    public static class SpecCheckReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
    }

    @Data
    public static class ExternalPostTaskExecuteReqInParams {
        private Long seqNo;
        private ObjectIdentifier lotID;
        private String key;
        private String status;
        private String extEventID;
    }

    @Data
    public static class OMSEnvInfoInqParams {
        private User user;
    }

    @Data
    public static class EqpInfoInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private boolean requestFlagForBasicInfo;
        private boolean requestFlagForBRInfo;
        private boolean requestFlagForStatusInfo;
        private boolean requestFlagForPMInfo;
        private boolean requestFlagForPortInfo;
        private boolean requestFlagForChamberInfo;
        private boolean requestFlagForStockerInfo;
        private boolean requestFlagForInprocessingLotInfo;
        private boolean requestFlagForReservedControlJobInfo;
        private boolean requestFlagForRSPPortInfo;
        private boolean requestFlagForEqpContainerInfo;
    }

    @Data
    public static class EqpInfoForIBInqParams {
        private User user;
        private SearchCondition searchCondition;
        private ObjectIdentifier equipmentID;
        private Boolean requestFlagForBasicInfo;
        private Boolean requestFlagForStatusInfo;
        private Boolean requestFlagForPMInfo;
        private Boolean requestFlagForPortInfo;
        private Boolean requestFlagForChamberInfo;
        private Boolean requestFlagForInternalBufferInfo;
        private Boolean requestFlagForStockerInfo;
        private Boolean requestFlagForInprocessingLotInfo;
        private Boolean requestFlagForReservedControlJobInfo;
        private Boolean requestFlagForRSPPortInfo;
    }

    @Data
    public static class EqpListByBayInqInParm {
        private User user;
        private ObjectIdentifier workArea;
        private String equipmentCategory;
        private ObjectIdentifier equipmentID;
        private List<String> fpcCategories;
        private String whiteDefSearchCriteria;
        private String specialControl;
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
     * @date 2019/2/13 10:17:03
     */
    @Data
    public static class EqpListByStepInqParm {
        private User user;
        private ObjectIdentifier productID;
        private ObjectIdentifier operationID;
        private List<String> fpcCategories;
        private String whiteDefSearchCriteria;
        private Object siInfo;
    }

    @Data
    public static class EqpUsageCountResetReqParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private String claimMemo;
    }

    @Data
    public static class DOCLotActionReqInParams {
        private ObjectIdentifier lotID;
    }

    @Data
    public static class HoldLotInBankReqParams {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }

    @Data
    public static class HoldLotReleaseReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier releaseReasonCodeID;
        private List<Infos.LotHoldReq> holdReqList;
        private String claimMemo;
		private String department;
		private String section;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class FutureReworkCancelReqParams {
        private User user;
        ObjectIdentifier lotID;
        ObjectIdentifier routeID;
        String operationNumber;
        List<Infos.FutureReworkDetailInfo> strFutureReworkDetailInfoSeq;
        String claimMemo;
    }

    @Data
    public static class HoldLotReleaseInBankReqParams {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }

    @Data
    public static class InterFabXferReserveReqInParams {
        private String category;
        private ObjectIdentifier objectID;
        private Infos.PostProcessActionInfo postProcessActionInfo;
    }

    @Data
    public static class LotBankHoldParams {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }

    @Data
    public static class LotInfoInqParams {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private Infos.LotInfoInqFlag lotInfoInqFlag;   //【bear】put most of flag attribute into object. in order to use them convenient.
    }

    @Data
    public static class AutoDispatchConfigInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/23 10:08:02
     */
    @Data
    public static class ProdOrderChangeReqParams {
        private User user;
        private List<Infos.ChangedLotAttributes> strChangedLotAttributes;
        private String claimMemo;
    }

    @Data
    public static class WhatNextLotListParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier selectCriteria;
        private SearchCondition searchCondition;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/11 10:46
     */
    @Data
    public static class SeasonWhatNextLotListParams {
        private User user;
        private Infos.Season season;
        private WhatNextLotListParams whatNextLotListParams;
    }

    @Data
    public static class LotListByCarrierInqParams {
        private ObjectIdentifier cassetteID;
        private User user;
    }

    @Data
    public static class LotListInqParams {
        private User user;
        private ObjectIdentifier bankID;
//        private String lotStatus;
        private List<String> lotStatusList;
        private String lotType;
        private ObjectIdentifier productID;
        private String orderNumber;
        private String customerCode;
        private ObjectIdentifier manufacturingLayer;                  //A: the siview source CimCode name manufacturingLayerID, but db name 'MFG_LAYER'
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Boolean bankInRequiredFlag;                 //A: BankIn Required Flag
        @Valid
        private ObjectIdentifier lotID;
        private String requiredCassetteCategory;
        private Boolean backupProcessingFlag;
        private String subLotType;
        private Boolean deleteLotFlag;
        private ObjectIdentifier holdReasonCodeID;
        private Boolean sorterJobCreationCheckFlag;         //Q: can't find this yield in db,it' just used in source CimCode?
        private String interFabXferState;
        private Boolean autoDispatchControlLotFlag;         //Q: Auto dispatch Control lot Flag
        private ObjectIdentifier operationID;

        private SearchCondition searchCondition;
        //add by Sun;
        private Boolean virtualOperationFlag;
        private Boolean poObjFilterFlag;                    //A: for filter STB lot;
        private Boolean inBankLotFilterFlag;                //A: for filter in bank lot for move bank screen;
        private String requiredCassetteType;
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
    public static class MaterialPrepareCancelInfoInqParams {
        private User user;
        private ObjectIdentifier preparationCancelledLotID;    // preparation cancelled lot id
    }

    @Data
    public static class MaterialPrepareCancelReqParams {
        private User user;
        private ObjectIdentifier preparationCancelledLotID;    // preparation cancelled lot id
        private List<Infos.NewVendorLotInfo> newVendorLotInfoList;
        private String claimMemo;
    }

    @Data
    public static class LotCurrentQueueReactivateReqParams implements PostProcessSource {
        private User user;
        private List<Infos.LotReQueueAttributes> lotReQueueAttributesList;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return this.lotReQueueAttributesList
                    .stream()
                    .map(Infos.LotReQueueAttributes::getLotID)
                    .collect(Collectors.toList());
        }
    }

    /**
     * description:
     * <p>this class will be used in new MaterialPrepareCancelReq interface</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Bear
     * @date 2018/7/10 11:25
     */
    @Data
    public static class LotPreparationCancelExReqParams {
        private User user;
        private ObjectIdentifier preparationCancelledLotID;    // preparation cancelled lot id
    }

    @Data
    public static class LotScheduleChangeReqParams {
        private User user;
        private List<Infos.ReScheduledLotAttributes> reScheduledLotAttributesList;
        private String claimMemo;
    }


    @Data
    public static class CarrierLoadingVerifyReqParams {
        private User user;
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier portId;
        private ObjectIdentifier cassetteId;
        private String loadPurposeType;      //SP_LoadPurposeType_EmptyCassette     "Empty cassette"
        //SP_LoadPurposeType_FillerDummy       "Filler Dummy lot"
        //SP_LoadPurposeType_ProcessLot        "Process lot"
        //SP_LoadPurposeType_ProcessMonitorLot "Process Monitor lot"
        //SP_LoadPurposeType_SideDummyLot      "Side Dummy lot"
        //SP_LoadPurposeType_WaitingMonitorLot "Waiting Monitor lot"
        //SP_LoadPurposeType_Any               "Any Purpose"
        //SP_LoadPurposeType_Other             "OTHER"
        //SP_LoadPurposeType_InternalBuffer    "Internal Buffer Eqp"
    }

    @Data
    public static class NewProdOrderCreateReqParams {
        private List<Infos.ReleaseLotAttributes> releaseLotAttributesList;
        private User user;
    }

    @Data
    public static class NewProdOrderModifyReqParams {
        private User user;
        private List<Infos.UpdateLotAttributes> updateLotAttributesList;
    }

    @Data
    public static class PortInfoInqParams {
        private User user;
        private SearchCondition searchCondition;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class PostTaskRegisterReqParams {
        private User user;
        private String transactionID;
        private String patternID;       // post process pattern ID
        private String key;              // key
        private Integer sequenceNumber; // sequenceNumber;
        private Infos.PostProcessRegistrationParam postProcessRegistrationParm;  // post process registration parameter
        private String claimMemo;

        public PostTaskRegisterReqParams() {

        }

        public PostTaskRegisterReqParams(String transactionID, String patternID, String key, Integer sequenceNumber, Infos.PostProcessRegistrationParam postProcessRegistrationParam, String claimMemo) {
            this.transactionID = transactionID;
            this.patternID = patternID;
            this.key = key;
            this.sequenceNumber = sequenceNumber;
            this.postProcessRegistrationParm = postProcessRegistrationParam;
            this.claimMemo = claimMemo;
        }
    }

    @Data
    public static class PostActionModifyReqParams {
        private User user;
        private String actionCode;
        private List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq;
        private List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq;
        private String claimMemo;
    }

    @Data
    public static class PostTaskExecuteReqParams {
        private User user;
        private String key;
        private Integer syncFlag;
        private Integer previousSequenceNumber;    // previous sequence number
        private String keyTimeStamp;
        private String claimMemo;

        public PostTaskExecuteReqParams() {

        }

        public PostTaskExecuteReqParams(User user, String key, Integer syncFlag, Integer previousSequenceNumber, String keyTimeStamp, String claimMemo) {
            this.user = user;
            this.key = key;
            this.syncFlag = syncFlag;
            this.previousSequenceNumber = previousSequenceNumber;
            this.keyTimeStamp = keyTimeStamp;
            this.claimMemo = claimMemo;
        }
    }

    @Data
    public static class ProductOrderInqParams {
        private User user;
        private ObjectIdentifier productRequestId;
    }

    @Data
    public static class AccessControlCheckInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private List<ObjectIdentifier> productIDList;
        private List<ObjectIdentifier> routeIDList;
        private List<ObjectIdentifier> lotIDLists;
        private List<ObjectIdentifier> machineRecipeIDList;
        private List<DepartmentCheckReasonCodeParams> departmentSectionCheckCodes;

        public AccessControlCheckInqParams() {
        }

        public AccessControlCheckInqParams(boolean isInitial) {
            if (isInitial) {
                init();
            }
        }

        private void init() {
            this.equipmentID = new ObjectIdentifier();
            this.stockerID = new ObjectIdentifier();
            this.productIDList = new ArrayList<>();
            this.routeIDList = new ArrayList<>();
            this.lotIDLists = new ArrayList<>();
            this.machineRecipeIDList = new ArrayList<>();
            this.departmentSectionCheckCodes = new ArrayList<>();
        }

        public void conversionDepartmentCheckCode(List<Infos.LotHoldReq> holdReqList) {
            if (CollectionUtils.isEmpty(holdReqList)) {
                return;
            }
            departmentSectionCheckCodes = Optional.ofNullable(departmentSectionCheckCodes).orElse(new ArrayList<>());
            List<DepartmentCheckReasonCodeParams> conversionList = holdReqList.parallelStream().map(lotHoldReq -> {
                DepartmentCheckReasonCodeParams checkCode = new DepartmentCheckReasonCodeParams();
                checkCode.setHoldReasonCodeId(lotHoldReq.getHoldReasonCodeID());
                checkCode.setSection(lotHoldReq.getSection());
                checkCode.setDepartment(lotHoldReq.getDepartment());
                return checkCode;
            }).collect(Collectors.toList());
            departmentSectionCheckCodes.addAll(conversionList);
        }

        public void conversionDepartmentEditCheckCode(List<Infos.LotHoldReq> holdReqList) {
            if (CollectionUtils.isEmpty(holdReqList)) {
                return;
            }
            departmentSectionCheckCodes = Optional.ofNullable(departmentSectionCheckCodes).orElse(new ArrayList<>());
            List<DepartmentCheckReasonCodeParams> conversionList = holdReqList.parallelStream().map(lotHoldReq -> {
                DepartmentCheckReasonCodeParams checkCode = new DepartmentCheckReasonCodeParams();
                checkCode.setHoldReasonCodeId(lotHoldReq.getOldHoldReasonCodeID());
                checkCode.setSection(lotHoldReq.getOldSection());
                checkCode.setDepartment(lotHoldReq.getOldDepartment());
                return checkCode;
            }).collect(Collectors.toList());
            departmentSectionCheckCodes.addAll(conversionList);
        }

        public void conversionDepartmentCheckCode(String department, String section, ObjectIdentifier reasonCodeId) {
            DepartmentCheckReasonCodeParams checkCode = new DepartmentCheckReasonCodeParams();
            checkCode.setHoldReasonCodeId(reasonCodeId);
            checkCode.setSection(section);
            checkCode.setDepartment(department);
            departmentSectionCheckCodes = Optional.ofNullable(departmentSectionCheckCodes).orElse(new ArrayList<>());
            departmentSectionCheckCodes.add(checkCode);
        }
    }

    @Data
    public static class ReasonCodeListByCategoryInqParams {
        private User user;
        private String codeCategory;
    }

    @Data
    public static class LagTimeActionReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;
        private String action;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class DurableLagTimeActionReqParams {
        private User user;
        private ObjectIdentifier castID;
        private String action;
        private String claimMemo;
    }

    @Data
    public static class ProductOrderReleasedListInqParams {
        private User user;
        private String productRequstID;   // original value: lotID, but the fact value is product Request ID
        private String lotType;
        private String subLotType;
        private String productID;
        private String sourceProductID;
        private String routeID;     // route ID
        private String manufacturingLayerID;        // MGF Layer ID
        private String startBankID;
        private String priorityClass;
        private String orderNumber;
        private String customerCode;
        private String planReleaseDate;     // Plan Release Date
        private Boolean sourceLotFlag;      // source lot flag
        private SearchCondition searchCondition;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/27 14:22
     */
    @Data
    public static class QtimeManageActionByPostTaskReqInParm {
        private User user;
        private ObjectIdentifier lotID;
        private Object siInfo;
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
     * @exception
     * @date 2019/8/27 14:22
     */
    @Data
    public static class ProductRelatedSourceProductInqParams {
        private User user;
        private ObjectIdentifier productID;
        private Object siInfo;
        private String claimMemo;
    }

    @Data
    public static class MoveInReserveReqParams implements PostProcessSource{
        private User user; //Request User ID
        private ObjectIdentifier equipmentID; //eqp ID
        private String portGroupID; //port Group ID
        private ObjectIdentifier controlJobID; //Control Job ID(Not Used)
        private List<Infos.StartCassette> startCassetteList; //Sequence of Start Carrier Information
        private String apcIFControlStatus;
        private Boolean cassetteChangeFlag; //QianDao MES-EAP Integration add
        private Boolean autoMoveInReserveFlag;//Qiandao AutoMoveInReserve add
        private boolean waferIDReadFlag; //Sorter need add

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public List<ObjectIdentifier> lotIDs() {
            if (CimArrayUtils.isEmpty(startCassetteList)) return Collections.emptyList();
            List<ObjectIdentifier> tempLotIDList = new ArrayList<>();
            for (Infos.StartCassette startCassette : startCassetteList) {
                if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                    continue;
                }
                List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        continue;
                    }
                    tempLotIDList.add(lotInCassette.getLotID());
                }
            }
            return tempLotIDList;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/15 14:58
     */
    @Data
    public static class MoveInReserveReqForSeasonParams {
        private User user; //Request User ID
        private Infos.Season season;
        private MoveInReserveReqParams seasonMoveinReserveReqParams;
        private MoveInReserveReqParams productMoveinReserveReqParams;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/10 13:23
     */
    @Data
    public static class SeasonLotMoveInReserveReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private SeasonMoveInReserveParams seasonLotParams;
        private SeasonMoveInReserveParams productLotParams;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/11 9:48
     */
    @Data
    public static class SeasonMoveInReserveReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private Infos.Season season;
        private SeasonMoveInReserveParams seasonLotParams;
        private SeasonMoveInReserveParams productLotParams;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/10 13:49
     */
    @Data
    public static class SeasonMoveInReserveParams {
        private User user;
        private String portGroupID;
        private ObjectIdentifier cassetteID;
        private Long loadSequenceNumber;
        private String loadPurposeType;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
    }

    @Data
    public static class MoveInReserveCancelReqParams implements PostProcessSource{
        private User user; //Request User ID
        private ObjectIdentifier equipmentID; //eqp ID
        private ObjectIdentifier controlJobID; //Control Job ID(Not Used)
        private String opeMemo;
        private String APCIFControlStatus;
        private boolean alreadySendMsgFlag;  //是否已经发送消息 供Sorter使用

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }
    }

    @Data
    public static class SorterJobListParams {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier createUser;
        private ObjectIdentifier sorterJobID;
        private Object siInfo;

        public SorterJobListParams(ObjectIdentifier lotID) {
            this.lotID = lotID;
        }
    }

    @Data
    public static class SourceLotListInqParams {
        private User user;
        private ObjectIdentifier productID;
        private ObjectIdentifier productRequestID;
        private boolean currentPOFlag;
        private boolean forLotGenFlag;
        private SearchCondition searchCondition;
    }

    @Data
    public static class SPCCheckReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
    }

    @Data
    public static class SPCDoActionReqParams {
        private User user;
        private List<Infos.BankMove> bankMoveList;
        private List<Infos.MailSend> mailSendList;
        private List<Infos.ReworkBranch> reworkBranchList;
        private String claimMemo;
    }

    @Data
    public static class WaferLotStartCancelInfoInqParams {
        private User user;
        private ObjectIdentifier stbCancelledLotID;  //<i>STB cancelled lot ID
    }

    @Data
    public static class WaferLotStartCancelReqParams {
        private User user;
        private ObjectIdentifier stbCancelledLotID;    // STB cancelled lot id
        private List<Infos.NewPreparedLotInfo> newPreparedLotInfoList;        // New prepared lot information
        private Infos.NewLotAttributes newLotAttributes;      // New lot attribute
        private String claimMemo;
    }

    @Data
    public static class STBCancelExReqParams {
        private User user;
        private ObjectIdentifier stbCancelledLotID;    // STB cancelled lot id
        private String siInfo;             //<i>Reserved for SI customization
    }

    @Data
    public static class WaferLotStartReqParams {
        private User user;
        private ObjectIdentifier productRequestID;            // product request ID
        private Infos.NewLotAttributes newLotAttributes;  // new lot attributes
        private String claimMemo;
    }

    @Data
    public static class SubLotTypeListInqParams {
        private User user;
        private String lotType;
    }

    @Data
    public static class SubLotTypeInqParams {
        private User user;
        private String lotType;
    }

    @Data
    public static class VendorLotEventMakeParams {
        private String transactionID;
        private ObjectIdentifier lotID;
        private String vendorLotID;
        private Long claimQuantity;
        private String claimMemo;
    }

    @Data
    public static class MaterialPrepareReqParams {
        private User user;
        private ObjectIdentifier bankID;              // bank ID
        private String lotType;             // lot Type
        private String subLotType;
        private Infos.NewLotAttributes newLotAttributes;
        private String claimMemo;
    }

    @Data
    public static class VendorLotReceiveParams {
        private User user;
        private ObjectIdentifier bankID;
        private ObjectIdentifier productID;
        private String lotID; // creating lot id
        private String subLotType;
        private Integer productWaferCount;     //before name productCount
        private String vendorLotID;
        private String vendorID;             //Different states for the variable "theLotType" in COMPONENT PPrMg
        private String claimMemo;
    }

    @Data
    public static class VendorLotReturnParams {
        private User user;
        private Integer productWaferCount;
        private ObjectIdentifier lotID;
        private String claimMemo;
    }

    @Data
    public static class EqpStatusChangeReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentStatusCode;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }

    @Data
    public static class EqpReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    /**
     * description:
     * lot cassette Xfer status change Params
     *
     * @author ho
     * @date 2018/7/19
     */
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
        private boolean manualInFlag;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private String zoneID;
        private String shelfType;
        private Timestamp transferStatusChangeTimeStamp;
        private Infos.ShelfPosition shelfPosition; //for e-rack, add by nyx
        private String claimMemo;
    }

    @Data
    public static class HoldLotReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;
        private List<Infos.LotHoldReq> holdReqList;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class HoldLotListInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private SearchCondition searchCondition;
    }

    /**
     * description:
     * loading or unloading lot report params
     *
     * @author PlayBoy
     * @date 2018/7/11
     */
    @Data
    public static class loadOrUnloadLotRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;
        private String loadPurposeType;
    }

    @Data
    public static class CarrierMoveToIBRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier loadedPortID;
        private ObjectIdentifier carrierID;
        private String opeMemo;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @author Jerry
     * @date 13:29
     * @return
     */
    @Data
    public static class SkipReqParams implements PostProcessSource{
        private User user;
        private Boolean locateDirection;
        private ObjectIdentifier lotID;
        private ObjectIdentifier currentRouteID;
        private String currentOperationNumber;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private Infos.ProcessRef processRef;
        private Integer sequenceNumber;
        private String claimMemo;
        private long seqno;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class OpeComWithDataReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private Boolean spcResultRequiredFlag;
        private String opeMemo;
        private List<ObjectIdentifier> holdReleasedLotIDs;
        private List<Infos.ApcBaseCassette> apcBaseCassetteListForOpeComp;
        private String apcifControlStatus;
        private String dcsifControlStatus;

    }

    /**
     * description:
     * MoveInCancelReqParams
     *
     * @author Fly
     * @date 2018/7/19
     */
    @Data
    public static class MoveInCancelReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String opeMemo;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }
    }

    /**
     * description:
     * MoveInReqParams
     *
     * @author Fly
     * @date 2018/7/19
     */
    @Data
    public static class MoveInReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
        private boolean processJobPauseFlag;
        private String opeMemo;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }
    }

    @Data
    public static class FutureHoldReqParams {
        private User user;
        private String holdType;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier reasonCodeID;
        private ObjectIdentifier relatedLotID;
        private boolean postFlag;
        private boolean singleTriggerFlag;
        private String claimMemo;
		private String department;
		private String section;
		// edit
		private ObjectIdentifier oldReasonCodeID;
		private ObjectIdentifier oldHoldUserID;
		private ObjectIdentifier oldRelatedLotID;
		private String oldDepartment;
		private String oldSection;
    }

    @Data
    public static class FutureHoldCancelReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier releaseReasonCodeID;
        private String entryType;
        private List<Infos.LotHoldReq> lotHoldList;
		private String department;
		private String section;
    }

    @Data
    public static class LotsMoveInInfoInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<ObjectIdentifier> cassetteIDs;

    }

    @Data
    public static class LotsMoveInReserveInfoInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> startCassettes;

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/7/28 14:57
     */
    @Data
    public static class SeasonLotsMoveInReserveInfoInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> seasonStartCassettes;
        private List<Infos.StartCassette> productStartCassettes;
    }

    @Data
    public static class LotOperationSelectionInqParams {
        private User user;
        private boolean searchDirection;     //<i>R/Forward or Backward Flag
        private boolean posSearchFlag;       //<i>R/Get backward process from FRPO or FRPOS
        private Integer searchCount;            //<i>R/Max Search Count
        private boolean currentFlag;         //<i>R/Acquire the current operation information whether flag.
        private ObjectIdentifier lotID;
        private SearchCondition searchCondition;

        public LotOperationSelectionInqParams() {
        }

        public LotOperationSelectionInqParams(boolean searchDirection, boolean posSearchFlag, Integer searchCount, boolean currentFlag, ObjectIdentifier lotID) {
            this.searchDirection = searchDirection;
            this.posSearchFlag = posSearchFlag;
            this.searchCount = searchCount;
            this.currentFlag = currentFlag;
            this.lotID = lotID;
        }
    }

    @Data
    public static class ScrapWaferReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;                    //<i>R/lot ID
        private ObjectIdentifier cassetteID;               //<i>R/Carrier ID
        private ObjectIdentifier reasonRouteID;            //<i>R/Reason Route ID
        private ObjectIdentifier reasonOperationID;        //<i>R/Reason Operation ID
        private String reasonOperationNumber;    //<i>R/Reason Operation Number
        private String reasonOperationPass;      //<i>R/Reason Operation Pass
        private String objrefPO;                 //<i>R/Object Reference of Process Operation
        private List<Infos.ScrapWafers> scrapWafers;           //<i>R/Scrap wafer Information
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }

        @Override
        public List<ObjectIdentifier> durableIDs() {
            return Collections.singletonList(this.cassetteID);
        }
    }

    /**
     * description:
     *
     * @author PlayBoy
     * @date 2018/9/18 14:24:11
     */
    @Data
    public static class CandidateE10SubStatusParams {
        private User user;
        private SearchCondition searchCondition;
    }

    /**
     * description:
     *
     * @author PlayBoy
     * @date 2018/9/18 15:42:55
     */
    @Data
    public static class EqpStatusSelectionInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private Boolean allInquiryFlag;
    }

    @Data
    public static class SplitLotReqParams {
        private User user;               //<i>R/Request User ID
        private ObjectIdentifier parentLotID;                 //<i>R/Parent lot ID
        private List<ObjectIdentifier> childWaferIDs;                //<i>R/Sequence of Child wafer ID
        private Boolean futureMergeFlag;             //<i>R/Future Merge Flag
        // support for auto combine function
        private Boolean combineHold;             //<i>R/combine
        private ObjectIdentifier mergedRouteID;               //<i>R/Merged Route ID
        private String mergedOperationNumber;       //<i>R/Merged Operation Number
        private Boolean branchingRouteSpecifyFlag;   //<i>R/Branching Route Specify Flag
        private ObjectIdentifier subRouteID;                  //<i>R/SubRoute ID
        private String returnOperationNumber;       //<i>R/Return Operation Number
        private String claimMemo;        //<i>O/Claim Memo       //D6000025

    }

    /**
     * description: use for TxChamberStatusSelectionInq
     *
     * @author PlayBoy
     * @date 2018/9/21 14:54:19
     */
    @Data
    public static class ChamberStatusSelectionInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/27 13:33
     */
    @Data
    public static class IdentifierParams {
        private User user;
        private ObjectIdentifier identifier;
    }

    @Data
    public static class UnscrapWaferReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private List<Infos.ScrapCancelWafers> scrapCancelWafers;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(lotID);
        }

        @Override
        public List<ObjectIdentifier> durableIDs() {
            return Collections.singletonList(cassetteID);
        }
    }

    @Data
    public static class MfgRestrictListInqParams {
        private User user;
        private SearchCondition searchCondition;
        private Boolean entityInhibitReasonDetailInfoFlag;
        private String functionRule;
        private String constraintID;
        private Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes;
    }

    @Data
    public static class MfgRestrictListByEqpInqParams {
        private User user;
        private String eqpID;
        private ObjectIdentifier bayID;
    }

    @Data
    public static class ScrapWaferNotOnPfReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private int waferCount;
        private ObjectIdentifier reasonCode;
        private String reasonDesc;
        private String claimMemo;
    }

    /**
     * description:
     * <p>use for CarrierExchangeReq</p>
     *
     * @author PlayBoy
     * @date 2018/10/8 11:09:07
     */
    @Data
    public static class CarrierExchangeReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.WaferTransfer> waferXferList;
        private String claimMemo;
    }

    @Data
    public static class MultiPathListInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private String routeType;
    }

    @Data
    public static class DynamicPathListInqParams {
        private User user;
        private String routeIDKey;              // the route id search key
        private String processDefinitionType;   // process defintion type, BRANCH or REWORK
        private Boolean activeVersionFlag;      // active version falg, if true, return only actived route.
        private SearchCondition searchCondition; // the parameters of Paging query
    }

    @Data
    public static class WaferScrappedHistoryInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
    }

    @Data
    public static class BasicUserInfoInqParams {
        private User user;
        private ObjectIdentifier userID;
    }

    @Data
    public static class OmsMsgInfoInqParams {
        private User user;
        private ObjectIdentifier userID;
    }

    @Data
    public static class AllUserInfoInqParams {
        private User user;
    }

    @Data
    public static class LotFamilyInqParams {
        private User user;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class BankInReqParams implements PostProcessSource {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return lotIDs;
        }
    }

    @Data
    public static class MfgRestrictReqParams {
        private User user;
        private Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes;
        private String claimMemo;
    }

    @Data
    public static class MfgRestrictReq_110Params {
        private User user;
        private List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList;
        private String claimMemo;
    }

    @Data
    public static class BankInCancelReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class AutoDispatchConfigModifyReqParams {
        private User user;
        private List<Infos.LotAutoDispatchControlUpdateInfo> lotAutoDispatchControlUpdateInfoList;
        private String claimMemo;
    }

    @Data
    public static class EDCDataItemWithTransitDataInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
    }

    /**
     * description: use for TxAllEqpListByBayInq
     *
     * @author Wind
     * @date 2018/10/16 16:22
     */
    @Data
    public static class EqpIDListInqParams {
        private User user;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @author Decade
     * @return
     * @date 2019/11/25/025 14:01
     */
    @Data
    public static class EqpFullAutoConfigListInqInParm {
        private User user;
        private List<ObjectIdentifier> equipmentIDs;
        private Object siInfo;
    }

    @Data
    public static class SplitLotNotOnPfReqParams {
        private User user;
        private ObjectIdentifier parentLotID;                 //<i>R/Parent lot ID
        private Integer parentLotWaferCount;                       //<i>R/Parent lot wafer Count
        private Integer childLotWaferCount;                        //<i>R/ChildLot wafer Count
        private List<ObjectIdentifier> childWaferIDs;                         //<i>R/Sequence of Child wafer ID
        private String claimMemo;                                   //<i>O/Claim Memo      //D6000025

    }


    @Data
    public static class MfgRestrictCancelReqParams {
        private User user;                            //<i>R/Requested User ID
        private List<Infos.EntityInhibitDetailInfo> entityInhibitions;    //<i>R/Entity Inhibitions that you want to cancel
        private ObjectIdentifier reasonCode;                       //<i>R/Reason CimCode (any code in the MfgRestrictCancel category) //D2300028
        private String claimMemo;
    }

    @Data
    public static class LotInfoByWaferInqParams {
        private User user;
        private ObjectIdentifier waferID;
    }

    @Data
    public static class AllLotTypeListInqParams {
        private User user;
    }

    @Data
    public static class ProcessFlowOperationListInqParams {
        private User user;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String pdType;
        private Integer searchCount;
    }

    @Data
    public static class ReworkReqParams implements PostProcessSource{
        private User user;
        private Infos.ReworkReq reworkReq;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            if (null == reworkReq) return Collections.emptyList();
            return Collections.singletonList(reworkReq.getLotID());
        }
    }

    @Data
    public static class ReworkCancelReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier currentRouteID;   // current route id
        private String currentOperationNumber;             // current operation number
        private ObjectIdentifier reasonCodeID;     // reason code id
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(lotID);
        }
    }


    @Data
    public static class PortStatusChangeRptParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.EqpPortEventOnTCS> eqpPortEventOnEAPesList;
        private String opeMemo;
    }

    @Data
    public static class NonProdBankStoreReqParams implements  PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier bankID;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class StockerInventoryUploadReqParam {
        private User user;
        private ObjectIdentifier machineID;
        private Infos.ShelfPosition shelfPosition; //for e-rack, add by Nyx
        private String claimMemo;
    }

    @Data
    public static class MergeLotReqParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier parentLotID;
        private ObjectIdentifier childLotID;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(parentLotID);
        }
    }

    @Data
    public static class MergeLotNotOnPfReqParams {
        private User user;
        private ObjectIdentifier parentLotID;
        private ObjectIdentifier childLotID;
        private String claimMemo;
    }


    @Data
    public static class StockerInventoryRptParam {
        private User user;
        private ObjectIdentifier stockerID;
        private Infos.ShelfPosition shelfPosition; //for e-rack, add byNyx
        private List<Infos.InventoryLotInfo> inventoryLotInfos;
        private String claimMemo;
    }

    @Data
    public static class ReticleStocInfoInqParams {
        private User user;
        private ObjectIdentifier stockerID;
    }

    @Data
    public static class NewProdOrderCancelReqParams {
        private User user;
        private List<ObjectIdentifier> lotIDs;
        private String claimMemo;
    }

    @Data
    public static class ProductIdListInqInParams {
        private User user;
        private String productCategory;       //<i>Product Category
        private String productGroupID;        //<i>Product Group ID
        private String productID;             //<i>Product ID
        private String routeID;               //<i>Route ID
        private String userID;                //<i>User ID
    }

    @Data
    public static class MaterialReceiveAndPrepareReqParams {
        private User user;                //<i>R/Request User ID
        private ObjectIdentifier equipmentID;         //<i>R/eqp ID
        private String portGroup;           //<i>R/port Group
        private String actionCode;          // sorter action code
        private ObjectIdentifier cassetteID;          //<i>R/cassette ID
        private String lotType;             //<i>R/lot Type
        private String subLotType;          //<i>R/Sub lot Type
        private ObjectIdentifier creatingLotID;       //<i>R/New lot ID
        private ObjectIdentifier vendorLotID;         //<i>R/Vendor lot ID
        private ObjectIdentifier vendorID;            //<i>R/Vendor ID
        private ObjectIdentifier productID;           //<i>R/Product ID
        private ObjectIdentifier bankID;              //<i>R/bank ID
        private String claimMemo;           //<i>O/Claim Memo
    }

    /**
     * description:
     * <p> use for TxEqpMemoInfoInq </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19                           Wind
     *
     * @author Wind
     * @date 2018/10/19 15:43
     */
    @Data
    public static class EqpMemoInfoInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    /**
     * description:
     * <p>use for TxEqpMemoAddReq__160</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23                              Wind
     *
     * @author Wind
     * @date 2018/10/23 10:30
     */
    @Data
    public static class EqpMemoAddReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String noteTitle;
        private String equipmentNote;
        private String claimMemo;
    }

    @Data
    public static class LotListByCJInqParams {
        private User user;
        private List<ObjectIdentifier> controlJobIDs;
    }

    @Data
    public static class TransportJobCreateReqParams {
        private String jobID;
        private boolean rerouteFlag;
        private String transportType;
        private List<Infos.JobCreateArray> jobCreateData;
        private Object siInfo;
    }

    @Data
    public static class LotMemoInfoInqParams {
        private User user;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class LotOpeMemoInfoInqParams {
        private User user;                          //<i>R/Request User ID
        private ObjectIdentifier lotID;       //<i>R/lot ID
        private ObjectIdentifier routeID;     //<i>R/Route ID
        private ObjectIdentifier operationID; //<i>R/Operation ID
        private String operationNumber;             //<i>R/Operation Number //D6000025
    }

    @Data
    public static class LotOpeMemoListInqParams {
        private User user;                          //<i>R/Request User ID
        private ObjectIdentifier lotID;       //<i>R/lot ID
    }

    @Data
    public static class LotMemoAddReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private String lotNoteTitle;
        private String lotNoteDescription;
        private String claimMemo;
    }

    @Data
    public static class LotOperationSelectionForMultipleLotsInqParams {
        private User user;
        private boolean searchDirection;
        private boolean posSearchFlag;
        private Integer searchCount;
        private boolean currentFlag;
        private List<ObjectIdentifier> lotIDs;
    }

    @Data
    public static class LotOperationNoteInfoRegisterReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String lotOperationNoteTitle;
        private String lotOperationNoteDescription;
        private String claimMemo;
    }

    @Data
    public static class RouteListInqParams {
        private User user;
        private String pdID;
        private String pdType;
        private String pdLevel;
        private String version;
    }

    @Data
    public static class MainProcessFlowListInqParams {
        private User user;
        private String routeType;
        private ObjectIdentifier lotID;
        private String procDefType;
        private Boolean activeShowFlag;
        private String bankID;
    }

    @Data
    public static class StageListInqParams {
        private User user;
        private ObjectIdentifier stageID;
    }

    @Data
    public static class FutureHoldListInqParams {
        private User user;
        private Infos.FutureHoldSearchKey futureHoldSearchKey;
        private SearchCondition searchCondition;
        private Integer count;
    }

    @Data
    public static class LotExtPriorityModifyReqParams {
        private User user;
        private List<Infos.LotExternalPriority> lotExternalPriorityList;
        private Boolean commitByLotFlag;
    }

    @Data
    public static class NonProdBankReleaseReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier lotID;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class WaferAliasSetReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private List<Infos.AliasWaferName> aliasWaferNames;
        private String claimMemo;
    }

    @Data
    public static class WaferAliasInfoInqParams {
        private User user;
        private List<ObjectIdentifier> waferIDSeq;
    }

    @Data
    public static class LotAnnotationInqParams {
        private User user;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ProcessFlowOperationListForLotInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private SearchCondition searchCondition;
    }

    @Data
    public static class CommonEqpInfoParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private Boolean requestFlagForBRInfo;
        private Boolean requestFlagForStatusInfo;
        private Boolean requestFlagForPMInfo;
        private Boolean requestFlagForPortInfo;
        private Boolean requestFlagForChamberInfo;
        private Boolean requestFlagForInternalBufferInfo;
        private Boolean requestFlagForStockerInfo;
        private Boolean requestFlagForInprocessingLotInfo;
        private Boolean requestFlagForReservedControlJobInfo;
        private Boolean requestFlagForRSPPortInfo;
        private Boolean requestFlagForEqpContainerInfo;
    }

    @Data
    public static class StockerStatusChangeRptParams {
        private User user;
        private ObjectIdentifier stockerID;          //<i>R/stocker ID
        private ObjectIdentifier stockerStatusCode;  //<i>R/stocker Status CimCode
        private String claimMemo;
    }

    /**
     * description:
     * <p> WhatNextNPWStandbyLotInqParams </p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 10/29/2018 10:23 AM
     */
    @Data
    public static class WhatNextNPWStandbyLotInqParams {
        private User user;
        private ObjectIdentifier bankID;
        private SearchCondition searchCondition;
    }

    @Data
    public static class ReticlePodMaintInfoUpdateReqParam {
        private User user;
        private ObjectIdentifier reticlePodID;
    }

    @Data
    public static class MultipleReticlePodStatusChangeRptParam {
        private User user;
        private String reticlePodStatus;
        private List<ObjectIdentifier> reticlePodIDList;
    }

    @Data
    public static class EqpStatusResetReqParams {
        private User user;
        private ObjectIdentifier equipmentID; //<i>R/eqp ID
        private String changeType;             //<i>R/Change Type
        private String claimMemo;              //<i>O/Claim Memo
    }

    @Data
    public static class ReticleAllInOutRptParams {
        private User user;
        private String moveDirection;
        private ObjectIdentifier reticlePodID;
        private List<Infos.MoveReticles> moveReticles;
        private String claimMemo;
    }

    @Data
    public static class EDCTransitDataRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
    }

    @Data
    public static class PartialReworkWithoutHoldReleaseReqParams {
        private User user;
        private Infos.PartialReworkReq partialReworkReq;
        private List<Infos.LotHoldReq> holdReqList;
        private String claimMemo;
    }

    @Data
    public static class PartialReworkWithHoldReleaseReqParams {
        private User user;
        private Infos.PartialReworkReq partialReworkReq;
        private ObjectIdentifier releaseReasonCodeID;
        private List<Infos.LotHoldReq> holdReqList;
        private String claimMemo;
		private String department;
		private String section;

    }

    @Data
    public static class ReworkWithHoldReleaseReqParams implements PostProcessSource {
        private User user;
        private Infos.ReworkReq strReworkReq;
        private ObjectIdentifier releaseReasonCodeID;
        private List<Infos.LotHoldReq> holdReqList;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.strReworkReq.getLotID());
        }
    }

    @Data
    public static class MonitorBatchCreateReqParams {
        private User user;
        private ObjectIdentifier monitorLotID;
        private List<Infos.MonRelatedProdLots> strMonRelatedProdLots;
    }

    @Data
    public static class MonitorBatchRelationInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
    }


    @Data
    public static class MonitorBatchDeleteReqParams {
        private User user;
        private ObjectIdentifier monitorLotID;
    }


    @Data
    public static class PartialReworkReqParams {
        private User user;
        private Infos.PartialReworkReq partialReworkReqInformation;
        private String claimMemo;
    }

    @Data
    public static class NPWLotStartReqParams{
        private User user;
        private ObjectIdentifier productID;
        private Integer waferCount;
        private String lotType;
        private String subLotType;
        private Infos.NewLotAttributes newLotAttributes;
        private String claimMemo;
    }

    @Data
    public static class SingleCarrierTransferReqParam {
        private User user;
        private Boolean rerouteFlag;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier lotID;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private List<Infos.ToMachine> toMachine;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
    }

    @Data
    public static class SingleDurableTransferReqParam {
        private User user;
        private Boolean rerouteFlag;
        private ObjectIdentifier durableID;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private List<Infos.ToMachine> toMachine;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
    }

    @Data
    public static class MultipleCarrierTransferReqParam {
        private User user;
        private boolean rerouteFlag;
        private String transportType;
        private List<Infos.CarrierXferReq> strCarrierXferReq;
    }

    @Data
    public static class CarrierReserveReqParam {
        private User user;
        private List<Infos.RsvLotCarrier> strRsvLotCarrier;
        private String claimMemo;
    }

    @Data
    public static class ReticlePodListWithBasicInfoInqParams {
        private User user;
        private List<ObjectIdentifier> reticlePodIDList;
    }

    @Data
    public static class ReticlePodTransferStatusChangeRptParams {
        private User user;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.XferReticlePod> xferReticlePodList;
        private String claimMemo;
        private Infos.ShelfPosition shelfPosition; //for e-rack, add by nyx
    }

    @Data
    public static class EDCWithSpecCheckActionReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
        private String claimMemo;
    }

    /**
     * description:
     * <p>This function returns candidate Q-Time definitions information to create Q-Time timer </p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/29                           Wind
     *
     * @author Wind
     * @date 2018/10/29 18:19
     */
    @Data
    public static class QtimeDefinitionSelectionInqParam {
        private User user;
        private Inputs.QtimeDefinitionSelectionInqIn qtimeDefinitionSelectionInqIn;
    }

    @Data
    public static class QtimeActionReqParam implements PostProcessSource {
        private User user;
        private ObjectIdentifier lotID;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/22 18:47
     */
    @Data
    public static class QtimeExpiredLotListWithActionPendingInqInParm {
        private User user;
        private Long maxRetrieveCount;
        private Object siInfo;
    }

    @Data
    public static class QrestTimeActionParam {
        private ObjectIdentifier qrestrictionTriggerRouteID;              //<i>Q Restriction Trigger Route ID
        private String qrestrictionTriggerOperationNumber;                     //<i>Q Restriction Trigger Operation Number
        private String qrestrictionTriggerBranchInfo;                           //<i>Q Restriction Trigger Branch Information
        private String qrestrictionTriggerReturnInfo;                           //<i>Q Restriction Trigger Return Information
        private ObjectIdentifier qrestrictionTargetRouteID;               //<i>Q Restriction Target Route ID
        private String qrestrictionTargetOperationNumber;                      //<i>Q Restriction Target Operation Number
        private String qrestrictionTargetBranchInfo;                            //<i>Q Restriction Target Branch Information
        private String qrestrictionTargetReturnInfo;                            //<i>Q Restriction Target Return Information
        private String qrestrictionTargetTimeStamp;                             //<i>Q Restriction Target Time Stamp
        private String originalQTime;                                             //<i>Original qtime
        private Integer expiredTimeDuration;                                     //<i>Expired Time Duration    //DSN000105390
        private String qrestrictionAction;                                       //<i>Q Restriction Action
        private ObjectIdentifier reasonCodeID;                              //<i>Reason CimCode ID
        private ObjectIdentifier actionRouteID;                             //<i>Action Route ID
        private String actionOperationNumber;                                    //<i>Action Operation Number
        private String futureHoldTiming;                                          //<i>Future Hold Timing (Pre/Post)
        private ObjectIdentifier reworkRouteID;                             //<i>Rework Route ID
        private ObjectIdentifier messageID;                                  //<i>Message ID
        private String customField;                                                //<i>Custom Field
        private Boolean actionDoneOnlyFlag;                                       //<i>ActionDoneOnlyFlag
        private String siInfo;
    }

    @Data
    public static class PartialReworkCancelReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier parentLotID;
        private ObjectIdentifier childLotID;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.parentLotID);
        }
    }

    @Data
    public static class BranchReqParams implements PostProcessSource{
        private User user;
        private Infos.BranchReq branchReq;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.branchReq.getLotID());
        }
    }

    @Data
    public static class EquipmentModeSelectionInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String modeChangeType;
        private List<ObjectIdentifier> portID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6                            Wind
     *
     * @author Wind
     * @date 2018/11/6 10:54
     */
    @Data
    public static class ReticlePodListInqParams {
        private User user;
        private ObjectIdentifier reticlePodID;
        private String reticlePodCategory;
        private String reticlePodStatus;
        private ObjectIdentifier durableSubStatus;
        private String flowStatus;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private boolean emptyFlag;
        private ObjectIdentifier reticleID;
        private ObjectIdentifier reticleGroupID;
        private String reticlePartNumber;
        private long maxRetrieveCount;
        private ObjectIdentifier bankID;
        private SearchCondition searchCondition;
    }

    @Data
    public static class BranchWithHoldReleaseReqParams implements PostProcessSource {
        private User user;
        private Infos.BranchReq branchReq;
        private ObjectIdentifier releaseReasonCodeID;
        private List<Infos.LotHoldReq> strLotHoldReleaseReqList;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.branchReq.getLotID());
        }
    }

    @Data
    public static class NPWUsageStateModifyReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private String controlUseState;
        private int usageCount;
        private String claimMemo;
    }

    @Data
    public static class ReticlePodDetailInfoInqParams {
        private User user;
        private ObjectIdentifier reticlePodID;
        private boolean durableOperationInfoFlag;
        private boolean durableWipOperationInfoFlag;
    }

    @Data
    public static class LoadPurposeTypeParam {
        private User user;
        private String portPurposeType;
    }

    @Data
    public static class MoveInCancelForIBReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier equipmentID;   //<i>R/eqp ID
        private ObjectIdentifier controlJobID;  //<i>R/Control Job ID
        private String opeMemo;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }
    }

    @Data
    public static class EqpBufferInfoInqInParm {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class EqpBufferTypeModifyReqInParm {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.BufferResourceUpdateInfo> strBufferResourceUpdateInfoSeq;
        private String claimMemo;
    }

    @Data
    public static class QtimeListInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier waferID;
        private String qTimeType;
        private Boolean activeQTime;
        private SearchCondition searchCondition;
        private String type;    // QTime类型: max/min(默认max)
    }

    @Data
    public static class LotCassetteReserveCancelParams {
        private User user;
        private List<Infos.ReserveCancelLotCarrier> reserveCancelLotCarriers;
        private String claimMemo;
    }

    @Data
    public static class QtimerReqParams {
        private User user;
        private String actionType;         //<i>Action Type;
        private ObjectIdentifier lotID;              //<i>lot ID;
        private List<Infos.QrestTimeInfo> qtimeInfoList;      //<i>List of Q Time Information;
        private String claimMemo;
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
     * @date 2018/11/14 13:53
     */
    @Data
    public static class CarrierMoveFromIBRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier destinationPortID;
        private ObjectIdentifier carrierID;
        private String opeMemo;
    }

    @Data
    public static class InProcessingLotsInq {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class EquipmentIDListParams {
        private User user;
    }

    @Data
    public static class EqpStatusChangeRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentStatusCode;
        private String opeMemo;
    }

    @Data
    public static class EDCDataItemListByKeyInqParams {
        private User user;
        private String searchKeyPattern;
        private List<Infos.HashedInfo> searchKeys;
    }

    @Data
    public static class LotsMoveInInfoForIBInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<ObjectIdentifier> cassetteIDs;
    }

    @Data
    public static class CDAInfoInqParams {
        private User user;
        private String classID;
    }

    @Data
    public static class DurableSetReqParams {
        private User user;
        private Infos.DurableRegistInfo durableRegistInfo;
        private String claimMemo;
    }

    @Data
    public static class CodeSelectionInqParams {
        private User user;
        private ObjectIdentifier category;
    }

    @Data
    public static class RecipeParamAdjustReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private Boolean allProcessStartFlag;
        private String claimMemo;
    }

    @Data
    public static class WaferListInLotFamilyInqParams {
        private User user;
        private ObjectIdentifier lotFamilyID;
    }

    @Data
    public static class MfgRestrictExclusionLotListInqParams {
        private User user;
        private ObjectIdentifier entityInhibitID;    //<i>Entity Inhibition ID
        private ObjectIdentifier lotID;              //<i>Entity Inhibit Exception lot
        private List<Infos.EntityIdentifier> entities;           //<i>Sequence of Entities
        private ObjectIdentifier ownerID;            //<i>Owner ID of who registed the Inhibition.
    }

    @Data
    public static class DurableSubStatusSelectionInqParams {
        private User user;
        private String durableCategory;
        private ObjectIdentifier durableID;
        private boolean allInquiryFlag;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/27/2018 5:53 PM
     */
    @Data
    public static class MfgRestrictExclusionLotReqParams {
        private User user;
        private List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLots;
        @Size(max = 256,message = "claimMemo Field length out of limit, maximum length 256")
        private String claimMemo;
    }

    @Data
    public static class EqpRecipeParameterListInq {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier machineRecipeID;
        private ObjectIdentifier pdID;
        private String fPCCategory;
        private String rParmSearchCriteria;
    }

    @Data
    public static class EqpRecipeSelectionInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class ProcessStatusRptParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<ObjectIdentifier> lotIDList;
        private String actionCode;
    }

    @Data
    public static class AllProductGroupListInq {
        private User user;
    }

    @Data
    public static class OMSEnvModifyReqParams {
        private User user;
        private List<Infos.EnvVariableList> strSvcEnvVariable;
    }

    @Data
    public static class EDCWithSpecCheckActionByPJReqParams {
        private User user;
        private ObjectIdentifier equipmentID;                //<i>eqp ID
        private ObjectIdentifier controlJobID;               //<i>Control Job ID;
        private ObjectIdentifier lotID;                      //<i>lot ID
        private String claimMemo;
    }

    @Data
    public static class PassThruReqParams {
        private User user;
        private List<Infos.GatePassLotInfo> gatePassLotInfos;
        private String claimMemo;
    }

    @Data
    public static class ReFlowBatchByManualActionReqParam implements PostProcessSource {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.ReFlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette;
        private String claimMemo;
    }

    @Data
    public static class FlowBatchByManualActionReqParam implements PostProcessSource {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            List<ObjectIdentifier> lotIDList = new ArrayList<>();
            for (Infos.FlowBatchByManualActionReqCassette cassette : this.strFlowBatchByManualActionReqCassette) {
                lotIDList.addAll(cassette.getLotID());
            }
            return lotIDList;
        }
    }

    @Data
    public static class VirtualOperationWipListInqParams {
        private User user;
        private ObjectIdentifier routeID;       // Route ID
        private String operationNumber;          // Operation Number
        private ObjectIdentifier operationID;   // Operation ID
        private String selectCriteria;            // Select Criteria
        private String claimMemo;                // Reserved For SI Customizated
    }

    @Data
    public static class FlowBatchLotRemoveReq {
        private User user;
        private ObjectIdentifier flowBatchID;
        private List<Infos.RemoveCassette> strRemoveCassette;
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
     * @date 2019/1/4 14:00:09
     */
    @Data
    public static class FlowBatchLotSelectionInqParam {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class EqpReserveForFlowBatchReqParam implements PostProcessSource {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier flowBatchID;
        private String claimMemo;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/10/2018 4:05 PM
     */
    @Data
    public static class ReworkWholeLotReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier currentRouteID;
        private String currentOperationNumber;
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;
        private ObjectIdentifier reasonCodeID;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/10/2018 4:08 PM
     */
    @Data
    public static class EDCDataItemListByCJInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String processJobID;
        private Boolean expandFlag;
    }

    @Data
    public static class FutureActionDetailInfoInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private Infos.OperationFutureActionAttributes operationFutureActionAttributes;
    }

    @Data
    public static class UploadedRecipeIdListByEqpInqParams {
        /**
         * R/Request User ID
         */
        private User user;
        /**
         * R/eqp ID
         */
        private ObjectIdentifier equipmentID;

        /**
         * Search Condition（分页暂时不加）
         */
        // private SearchCondition searchCondition;
    }

    @Data
    public static class FlowBatchStrayLotsListInqParams {
        private User user;
    }

    /**
     * @author ZQI
     * @date: 2018/12/11
     */
    @Data
    public static class EDCConfigListInqParams {
        /**
         * R/Request User ID
         */
        private User user;
        /**
         * R/In-parameter
         */
        private Infos.EDCConfigListInqInParm strEDCConfigListInqInParm;
    }


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/11/2018 5:16 PM
     */
    @Data
    public static class LotOperationSelectionFromHistoryInqParams {
        private User user;
        private Long searchCount;
        private ObjectIdentifier lotID;
        //add fromTimeStamp and toTimeStamp for runcard history
        private String fromTimeStamp;
        private String toTimeStamp;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @return
     * @exception
     * @date 2019/4/25 13:38
     */
    @Data
    public static class LotOperationHistoryInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationPass;
        private String operationCategory;
        private Boolean pinPointFlag;
        //add fromTimeStamp and toTimeStamp for runcard history
        private String fromTimeStamp;
        private String toTimeStamp;
        private SearchCondition searchCondition;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 14:01
     */
    @Data
    public static class HistoryInformationInqParams {
        private User user;
        private String fromTimeStamp;
        private String toTimeStamp;
        private String historyCategory;
        private List<Infos.TargetTableInfo> strTargetTableInfoSeq;
        private SearchCondition searchCondition;
        private Long maxRecordCount;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Scott
     * @date 2018/12/13 11:09:13
     */
    @Data
    public static class FlowBatchInfoInqParams {
        private User user;
        private ObjectIdentifier flowBatchID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class ProcessFlowOpeListWithNestInqParam {
        private User user;
        private ObjectIdentifier routeID;
        private String fromOperationNumber;
        private Long nestLevel;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/14/2018 3:00 PM
     */
    @Data
    public static class MoveOutForIBReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private Boolean spcResultRequiredFlag;
        private String opeMemo;

    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/14/2018 3:01 PM
     */
    @Data
    public static class MoveOutForIBReqExtraParams {
        private List<ObjectIdentifier> holdReleasedLotIDs;
        private List<Infos.ApcBaseCassette> apcBaseCassetteList;
        private String apcifControlStatus;
        private String dcsifControlStatus;

    }

    @Data
    public static class ProcessHoldReq {
        private User user;
        private String holdType;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier productID;
        private ObjectIdentifier reasonCodeID;
        private Boolean withExecHoldFlag;
        private String claimMemo;
		private String department;
		private String section;
		// edit
		private ObjectIdentifier oldReasonCodeID;
		private String oldSection;
		private String oldDepartment;
    }

    @Data
    public static class ProcessHoldCancelReq {
        private User user;
        private String holdType;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier productID;
        private ObjectIdentifier holdReasonCodeID;
        private ObjectIdentifier releaseReasonCodeID;
        private Boolean withExecHoldReleaseFlag;
        private String claimMemo;
		private String department;
		private String section;
    }

    @Data
    public static class LotsMoveInReserveInfoForIBInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> startCassettes;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/7/28 15:25
     */
    @Data
    public static class SeasonLotsMoveInReserveInfoForIBInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> seasonStartCassettes;
        private List<Infos.StartCassette> productStartCassettes;
    }

    /**
     * @author ZQI
     * @date 2018/12/18
     */
    @Data
    public static class EqpMaxFlowbCountModifyReqParams {
        /**
         * R/Request User ID
         */
        private User user;
        /**
         * R/eqp ID
         */
        private ObjectIdentifier equipmentID;
        /**
         * R/Flow Batch Max Count
         */
        private Integer flowBatchMaxCount;
        /**
         * O/Claim Memo
         */
        private String claimMemo;
    }

    @Data
    public static class FloatingBatchListInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class WaferPositionWithProcessResourceRptParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private Infos.ProcessResourcePositionInfo processResourcePositionInfo;
        private String opeMemo;
    }

    @Data
    public static class CarrierOutFromIBReqParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private String claimMemo;
    }

    @Data
    public static class CarrierLoadingVerifyForIBReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier portID;
        private ObjectIdentifier cassetteID;
        private String loadPurposeType;
    }

    /**
     * @author ZQI
     * @date 2018/12/19
     */
    @Data
    public static class EqpReserveCancelForflowBatchReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier flowBatchID;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/24/2018 10:38 AM
     */
    @Data
    public static class EDCHistoryInqParams {
        private User user;
        private ObjectIdentifier lotID;       //lot ID;
        private ObjectIdentifier routeID;     //Route ID;
        private String operationNumber;       //Operation Number;
        private String operationPass;        //Operation Pass;
        private Boolean getSpecFlag;          //Get Spec Flag;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @author Scott
     * @return
     * @date 2019/1/2 10:12:13
     */
    @Data
    public static class MoveInReserveCancelForIBReqParams implements PostProcessSource {
        private User user; //Request User ID
        private ObjectIdentifier equipmentID; //eqp ID
        private ObjectIdentifier controlJobID; //Control Job ID(Not Used)
        private String opeMemo;
        private String aPCIFControlStatus;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }
    }

    @Data
    public static class MoveInReserveForIBReqParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
        private String apcifControlStatus;
        private String opeMemo;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Optional.ofNullable(startCassetteList)
                    .map(startCassettes -> startCassettes
                            .stream()
                            // ignore the lots that is in a carrier with EMPTY as load purpose type
                            .filter(startCassette ->
                                    !CimStringUtils.equals(startCassette.getLoadPurposeType(),
                                            BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)
                            )
                            .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                            //-----------------------
                            // Omit not MoveIn Lot
                            //-----------------------
                            .filter(Infos.LotInCassette::getMoveInFlag)
                            .map(Infos.LotInCassette::getLotID)
                            .collect(Collectors.toList())
                    )
                    .orElseGet(Collections::emptyList);
        }

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/18 15:23
     */
    @Data
    public static class MoveInReserveForIBReqForSeasonParams {
        private User user;
        private Infos.Season season;
        private MoveInReserveForIBReqParams seasonMoveInReserveForIBReqParams;
        private MoveInReserveForIBReqParams productMoveInReserveForIBReqParams;
        private String claimMemo;
    }

    @Data
    public static class ProcessHoldListInqParams {
        private User user;
        private Infos.ProcessHoldSearchKey searchKey;
        private Long count;
    }

    @Data
    public static class FlowBatchCheckForLotSkipReqParams {
        private User user;
        private Boolean locateDirection;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private Infos.ProcessRef processRef;
        private Long seqno;
        private String claimMemo;
    }

    @Data
    public static class FlowBatchByAutoActionReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param * @param null -
     * @author Lin
     * @date 2018/12/25 11:06
     * @return
     */
    @Data
    public static class EqpModeChangeReqPrams {
        private User user;
        private ObjectIdentifier equipmentID;       //Equipment ID;
        private List<Infos.PortOperationMode> portOperationModeList;
        private Boolean notifyToEqpFlag;
        private Boolean notifyToEAPFlag;
        private String opeMemo;
    }

    @Data
    public static class EqpEAPStatusSyncReqPrams {
        private User user;
        private ObjectIdentifier equipmentID;       //Equipment ID;
        private String claimMemo;
    }

    @Data
    public static class CDAValueInqParams {
        private User user;
        private Infos.CDAValueInqInParm strCDAValueInqInParm;
        private String claimMemo;
    }

    @Data
    public static class CDAValueUpdateReqParams {
        private User user;
        private Infos.CDAValueUpdateReqInParm strCDAValueUpdateReqInParm;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Scott
     * @date 2019/1/8 10:45:36
     */
    @Data
    public static class WhatNextAMLotInqInParm {
        private User user;
        private ObjectIdentifier equipmentID;    //<i>Equipment ID
        private ObjectIdentifier eqpMonitorID;   //<i>Auto Monitor ID
        private String selectCriteria; //<i>Select Criteria
    }

    @Data
    public static class WhereNextStockerInqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
    }

    @Data
    public static class DurableWhereNextStockerInqParams {
        private User user;
        private ObjectIdentifier durableID;
    }

    @Data
    public static class PCSParameterValueSetReqParams {
        private User user;
        private String parameterClass;
        private String identifier;
        private List<Infos.UserParameterValue> parameters;
    }

    @Data
    public static class PCSParameterValueInqParams {
        private User user;
        private String parameterClass;
        private String identifier;
    }

    @Data
    public static class UserParams {
        private User user;
    }

    @Data
    public static class MultiDurableStatusChangeReqParams {
        private User user;
        private Infos.MultiDurableStatusChangeReqInParm parm;
        private String claimMemo;
    }

    @Data
    public static class CarrierTransferJobEndRptParams {
        private User user;
        private List<Infos.XferJobComp> strXferJob;
        private String claimMemo;
    }

    /**
     * description: for entity inhibit registration ui
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 3/14/2019 4:13 PM
     */
    @Data
    public static class SubLotTypeIDListExInqParams {
        private User user;
    }

    @Data
    public static class AMListInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier eqpMonitorID;
    }

    @Data
    public static class AMJobListInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier eqpMonitorJobID;
    }

    @Data
    public static class WaferSlotmapChangeReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.WaferTransfer> strWaferXferSeq;
        private Boolean bNotifyToTCS;
        private String claimMemo;
    }

    @Data
    public static class WaferSlotmapChangeRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.WaferTransfer> strWaferXferSeq;
        private String claimMemo;
    }


    @Data
    public static class AMStatusChangeRptInParm {
        private User user;
        private ObjectIdentifier equipmentID;                    //<i>Eqquipment ID
        private ObjectIdentifier eqpMonitorID;                   //<i>Auto Monitor ID
        private String monitorStatus;                  //<i>Status of Auto Monitor
        private Boolean forceRunFlag;                   //<i>Force Run is performed if this Flag is TRUE
        private Boolean warningActionFlag;              //<i>If True, Status Change to "Warning" isn't mandatory
        private String claimMemo;


    }

    @Data
    public static class EqpMonitorScheduleUpdateInParm {
        private User user;
        private ObjectIdentifier equipmentID;      //<i>Equipment ID
        private ObjectIdentifier eqpMonitorID;     //<i>Auto Monitor ID
        private String actionType;                 //<i>Action Type
        private Long postponeTime;            //<c>SP_EqpMonitor_OpeCategory_Skip
        private String claimMemo;
    }

    @Data
    public static class AMJobStatusChangeRptInParm {
        private User user;
        private ObjectIdentifier equipmentID;                    //<i>Equipment ID
        private ObjectIdentifier eqpMonitorID;                   //<i>Auto Monitor ID
        private ObjectIdentifier eqpMonitorJobID;                //<i>Auto Monitor Job ID
        private String monitorJobStatus;               //<i>Status of Auto Monitor Job
        private String claimMemo;
    }

    @Data
    public static class AMSetReqInParm {
        private User user;
        private String actionType;                       //<i>Action Type
        //<c>SP_EqpMonitor_OpeCategory_Create
        //<c>SP_EqpMonitor_OpeCategory_Update
        //<c>SP_EqpMonitor_OpeCategory_Delete
        private ObjectIdentifier eqpMonitorID;                  //<i>Auto Monitor ID
        private ObjectIdentifier equipmentID;                   //<i>Equipment ID
        private ObjectIdentifier chamberID;                     //<i>Chamber ID
        private String description;                   //<i>Description
        private String monitorType;                   //<i>Monitor Type (Routine or Manual)
        private String scheduleType;                  //<i>Schedule Type (In R14.0, "Time")
        private List<Infos.EqpMonitorProductInfo> strEqpMonitorProductInfoSeq;   //<i>Product List of Equipmenmt Monitor
        private String startTimeStamp;                //<i>First Execution Time of Auto Monitor
        private Integer executionInterval;             //<i>Interval of Auto Monitor Execution
        private Integer warningInterval;               //<i>Time for Expiration Notification
        private Integer expirationInterval;            //<i>Time for Expiration
        private Boolean standAloneFlag;                //<i>For Future Enhancement
        private Boolean kitFlag;                       //<i>Kit is required or not
        private Integer maxRetryCount;                 //<i>Max Retry Count before Fail
        private Infos.EqpStatus eqpStateAtStart;               //<i>Equipment State changed at Auto Monitor start
        private Infos.EqpStatus eqpStateAtPassed;              //<i>Equipment State changed at Auto Monitor passed
        private Infos.EqpStatus eqpStateAtFailed;              //<i>Equipment State changed at Auto Monitor failed
        private List<Infos.EqpMonitorActionInfo> strEqpMonitorActionInfoSeq;    //<i>Action List of Auto Monitor
        private String claimMemo;
    }

    @Data
    public static class DOCStepListInProcessFlowInqParams {
        private User user;
        private ObjectIdentifier lotID;
    }

    /**
     * description:PSMLotDefinitionListInqParams
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/19 11:30
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class PSMLotDefinitionListInqParams {
        private User user;
        private ObjectIdentifier lotFamilyID;
        private String claimMemo;
        private Boolean detailRequireFlag;
    }

    @Data
    public static class RecipeIdListForDOCInqParams {
        private User user;
        private Infos.RecipeIdListForDOCInqInParm strRecipeIdListForDOCInqInParm;
    }

    @Data
    public static class DOCLotInfoInqParams {
        private User user;
        private List<String> FPCIDs;                 //<i>Sequence of DOC ID
        private ObjectIdentifier lotID;                   //<i>Lot ID
        private ObjectIdentifier mainPDID;                //<i>Main PD ID       (defined DOC)
        private String mainOperNo;              //<i>Operation Number (defined DOC)
        private ObjectIdentifier orgMainPDID;             //<i>Original Main PD ID
        private String orgOperNo;               //<i>Operation Number (Connected Sub Route Point)
        private ObjectIdentifier subMainPDID;             //<i>Sub Main PD ID
        private String subOperNo;               //<i>Operation Number (Connected Sub2 Route Point)
        private boolean waferIDInfoGetFlag;      //<i>Wafer ID Information Get Flag
        private boolean recipeParmInfoGetFlag;   //<i>Recipe Parameter Information Get Flag
        private boolean reticleInfoGetFlag;      //<i>Reticle Information Get Flag
        private boolean dcSpecItemInfoGetFlag;   //<i>DC Spec Item Information Get Flag
    }

    @Data
    public static class DOCLotActionReqParams {
        private User user;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class CJPJOnlineInfoInqInParams {
        private User user;
        private ObjectIdentifier equipmentID;                    //<i>Equipment ID
        private ObjectIdentifier controlJobID;                   //<i>Control Job ID
        private String processJobID;                   //<i>Process Job ID
        private Boolean requestFlagForRecipeParameter;  //<i>Request recipe parameter information
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/20 下午 3:50
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class PSMLotInfoInqParams {
        private User user;                              //<i>R/Request User ID
        private ObjectIdentifier lotFamilyID;           //<i>R/Lot Family ID
        private ObjectIdentifier splitRouteID;          //<i>R/Split Route ID
        private String splitOperationNumber;           //<i>R/Split Operation Number //D6000025
        private ObjectIdentifier originalRouteID;       //<i>R/Original Route ID
        private String originalOperationNumber;         //<i>R/Original Operation Number
    }

    @Data
    public static class CJPJProgressInfoInqParams {
        private User user;
        private ObjectIdentifier equipmentID;                    //<i>Equipment ID
        private ObjectIdentifier controlJobID;                   //<i>Control Job ID
    }

    @Data
    public static class PSMLotRemoveReqParams {
        private User user;                        //<i>R/Request User ID
        private ObjectIdentifier lotFamilyID;                //<i>R/Lot Family ID                                                        //D7000015
        private ObjectIdentifier splitRouteID;               //<i>R/Split Route ID                                                       //D700001;
        private String splitOperationNumber;      //<i>R/Split Operation Number
        private ObjectIdentifier originalRouteID;            //<i>R/Original Route ID                                                    //D7000015
        private String originalOperationNumber;   //<i>R/Original Operation Number                                            //D7000015
        private String claimMemo;                   //<i>O/Claim Memo      //D6000025
        //add run card originalPsmKey
        private String originalPsmKey;  //use doc select psm_key to update and  psm delete the run card data
        //add runCardID for history
        private String runCardID;
    }

    @Data
    public static class DurablesInfoForOpeStartInqParams {
        private User user;
        private ObjectIdentifier equipmentID;                            //<i>Equipment ID
        private List<ObjectIdentifier> durableIDs;                             //<i>Sequence of Durable ID
        private String durableCategory;                        //<i>Carrier/ReticlePod/Reticle
        private ObjectIdentifier durableControlJobID;                    //<i>Durable ControlJob ID
    }

    @Data
    public static class DurableOperationStartReqParams {
        private User user;
        private ObjectIdentifier equipmentID;                           //<i>Equipment ID
        private ObjectIdentifier durableControlJobID;                   //<i>Durable Control Job ID
        private String durableCategory;                       //<i>Durable Category
        private List<Infos.StartDurable> strStartDurables;                      //<i>Sequence of Start Durable
        private Infos.DurableStartRecipe strDurableStartRecipe;                 //<i>Durable Start Recipe
    }

    @Data
    public static class DurableControlJobListInqParams {
        private User user;
        private ObjectIdentifier equipmentID;                            //<i>Equipment ID
        private ObjectIdentifier durableID;                              //<i>Durable ID
        private String durableCategory;                        //<i>Carrier/ReticlePod/Reticle
        private ObjectIdentifier createUserID;                           //<i>Create User ID
        private ObjectIdentifier durableJobID;                           //<i>Durable Job ID
        private Boolean durableInfoFlag;                        //<i>Durable Info Flag
    }

    @Data
    public static class DurableOperationStartCancelReqParams {
        private User user;
        private ObjectIdentifier equipmentID;                           //<i>Equipment ID
        private ObjectIdentifier durableControlJobID;                   //<i>Durable Control Job ID
    }

    @Data
    public static class DurableOpeCompReqParams {
        private User user;
        private ObjectIdentifier equipmentID;                           //<i>Equipment ID
        private ObjectIdentifier durableControlJobID;                   //<i>Durable Control Job ID
    }

    @Data
    public static class DOCLotInfoSetReqParams {
        private User user;
        private List<Infos.FPCInfoAction> strFPCInfoActionList;
        private String claimMemo;
        //add runCardID for history
        private String runCardID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/25 13:57
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class PSMLotInfoSetReqParams {
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
        private List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq;    //<i>R/strExperimentalLotDetailInfoSeq
        private String claimMemo;                          //<i>O/Claim Memo     //D6000025
        //runCard add originalPsmKeys
        private List<String> originalPsmKeys; //modify or delete use psm key to delete old RUNCARD_PSM/RUNCARD_PSM_DOC data and update new data
        //runCard add runCardID for history
        private String runCardID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 13:35
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class DOCLotRemoveReqParams {
        private User user;
        private List<String> fpcIDs;           //<i>Sequence of DOC ID
        private ObjectIdentifier lotFamilyID;       //<i>Lot Family ID
        private String claimMemo;
        //add runCardID for history
        private String runCardID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/10                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/10 15:19
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class RunningHoldReqParams {
        private User user;                    //<i>R/Request User ID
        private ObjectIdentifier equipmentID;             //<i>R/Equipment ID
        private ObjectIdentifier controlJobID;            //<i>R/Control Job ID
        private ObjectIdentifier holdReasonCodeID;        //<i>R/Reason Code
        private String opeMemo;                //<i>O/Claim Memo
    }

    @Data
    public static class EDCByPJRptInParms {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.CollectedDataItemStruct> collectedDataItemList;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/12 15:53
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ForceMoveOutReqParams {
        private User user;
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier controlJobId;
        private Boolean spcResultRequiredFlag;
        private String claimMemo;
    }

    /**
     * description:TxSubRouteBranchReq Input Params
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 4/16/2019 5:30 PM
     */
    @Data
    public static class SubRouteBranchReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier currentRouteID;
        private String currentOperationNumber;
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 11:23
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class PSMLotActionReqParams {
        private User user;
        private ObjectIdentifier lotId;
        private String claimMemo;
    }

    @Data
    public static class CastDeliveryReqParam {
        private User user;
        private ObjectIdentifier equipmentID;

        public CastDeliveryReqParam(User user, String equipmentID) {
            this.user = user;
            this.equipmentID = ObjectIdentifier.buildWithValue(equipmentID);
        }

        public CastDeliveryReqParam() {
        }
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 14:48
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierTransferJobInfoInqParam {
        private User user;
        private Boolean detailFlag;
        private String inquiryType;
        private ObjectIdentifier inquiryKey;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/18                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/18 11:20
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierTransferJobDetailInfoInqParam {
        private User user;
        private Boolean detailFlag;
        private String inquiryType;
        private ObjectIdentifier inquiryKey;
        private String functionID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 10:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierTransferJobDeleteReqParam {
        private User user;
        private String jobId;
        private List<Infos.DelCarrierJob> strDelCarrierJob;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 14:57
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class EAPRecoveryReqParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private String actionCode;
        private List<Infos.RecoverWafer> recoverWaferList;
        private String opeMemo;
    }

    @Data
    public static class ReticleSortRptParam {
        private User user;
        private List<Infos.ReticleSortInfo> strReticleSortInfo;
        private String claimMemo;
    }

    @Data
    public static class OpeGuideInq {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class EboardInfoSetReqParams {
        private User user;
        private String noticeTitle;
        private String noticeDescription;
    }

    @Data
    public static class EboardInfoInqParams {
        private User user;
    }

    @Data
    public static class RecipeDownloadReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private boolean formatFlag;
        private String claimMemo;
    }

    @Data
    public static class RecipeDeleteInFileReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private String claimMemo;
    }

    @Data
    public static class RecipeCompareReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private boolean formatFlag;
        private String claimMemo;
    }

    @Data
    public static class AMJobLotReserveReqInParams implements PostProcessSource {
        private User user;
        private ObjectIdentifier equipmentID;                 //<i>Equipment ID
        private ObjectIdentifier eqpMonitorID;                //<i>Auto Monitor ID
        private ObjectIdentifier eqpMonitorJobID;             //<i>Auto Monitor Job ID
        private List<Infos.EqpMonitorProductLotMap> strProductLotMap;         //<i>The Mapping between Auto Monitor Product and Target Lots
        private Boolean forceRunFlag;       //<i>Force run is performed if This Flag is TRUE.
        private String claimMemo;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }
    }

    @Data
    public static class NPWCarrierReserveReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
        private String opeMemo;
    }

    @Data
    public static class NPWCarrierReserveForIBReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
        private String opeMemo;
    }

    @Data
    public static class NPWCarrierReserveCancelForIBReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private Boolean notifyToTCSFlag;
        private List<Infos.NPWXferCassette> npwTransferCassetteList;
        private String opeMemo;
    }

    @Data
    public static class TCSParams {

    }

    @Data
    public static class PJInfoRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.ProcessJob> processJobList;
        private String opeMemo;
    }

    @Data
    public static class PJStatusChangeReqParams {
        private User user;
        private Infos.PJStatusChangeReqInParm strPJStatusChangeReqInParm;
        private String actionRequestTimeStamp;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/4 16:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class LotCarrierTOTIReqParam {
        private Boolean EqpToEqpFlag;               //<i>equipment to equipment flag
        private ObjectIdentifier equipmentID;                //<i>equipmentID
        private ObjectIdentifier controlJobID;               //<i>controlJobIDForEqpToEqp (optional)
        private Infos.EqpTargetPortInfo strEqpTargetPortInfo;       //<i>equipment target port information for take out in
        private List<Infos.CarrierXferReq> strCarrierXferReqSeq;       //<i>FOUP transfer information
        private Object siInfo;                       //<i>Reserved for SI customization
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/17 17:47
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class MoveInReserveForTOTIReqInParam {
        private ObjectIdentifier equipmentID;                //<i>Equipment ID
        private String portGroupID;                //<i>Port Group ID
        private ObjectIdentifier controlJobID;               //<i>Control Job ID
        private List<Infos.StartCassette> strStartCassetteSequence;   //<i>Start Cassette information
        private Object siInfo;
    }

    @Data
    public static class RecipeParamAdjustOnActivePJReqParam {
        private User user;
        private ObjectIdentifier equipmentID;                //<i>Equipment ID
        private ObjectIdentifier controlJobID;               //<i>Control Job ID
        private List<Infos.ProcessRecipeParameter> strProcessRecipeParameterSeq;   //<i>Wafer information with recipe parameters
        private String claimMemo;
    }

    @Data
    public static class PJStatusChangeRptInParm {
        private User user;
        private ObjectIdentifier equipmentID;                    //<i>Equipment ID
        private ObjectIdentifier controlJobID;                   //<i>controlJobID
        private List<ObjectIdentifier> waferList;                        //<i>Sequence of Wafer ID. Optional if equipment does not have SLM capability
        private String processJobID;                   //<i>Process job ID
        private String currentState;                   //<i>Current status of process job
        private String actionCode;                     //<i>Action Code

    }

    @Data
    public static class ProcessJobMapInfoRptParam {
        private User user;
        private Infos.ProcessJobMapInfoRptInParm processJobMapInfoRptInParm;
    }

    @Data
    public static class RecipeDirectoryInqParam {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class RecipeUploadReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private boolean formatFlag;
        private String claimMemo;
    }

    @Data
    public static class RecipeDeleteReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String physicalRecipeID;
        private boolean recipeFileDeleteFlag;
        private ObjectIdentifier machineRecipeID;
        private String fileLocation;
        private String fileName;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/24 15:37
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class RecipeParamAdjustRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.LotWafer> strLotWaferSeq;
        private Object siInfo;
    }

    @Data
    public static class PartialMoveOutReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.PartialOpeCompAction> partialOpeCompActionList;
        private boolean spcResultRequiredFlag;
        private Object siInfo;
        private String claimMemo;

    }

    @Data
    public static class LotPlanChangeReserveListInqParams {
        private User user;
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
        private long lotInfoChangeFlag;
    }

    @Data
    public static class OnlineSorterActionExecuteReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroup;
        private List<Infos.WaferSorterSlotMap> waferSorterSlotMapList;
        private String actionCode;
        private String physicalRecipeID;
        private ObjectIdentifier productRequestID;
        private MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams;
        private String claimMemo;
    }

    @Data
    public static class SJStatusChgRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier jobID;
        private String portGroup;
        private String jobType;
        private String jobStatus;
        private String sorterJobCategory;
        private String opeMemo;

    }

    @Data
    public static class ReqCategoryGetByLotParams {
        private User user;
        private ObjectIdentifier lotID;

    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 10:43
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeReqParams {
        private User user;
        private Infos.OwnerChangeReqInParm strOwnerChangeReqInParm;
        private String claimMemo;
    }

    @Data
    public static class OnlineSorterRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String actionCode;
        private Integer rcEAP;
        private List<Infos.WaferSorterSlotMap> waferSorterSlotMapList;
        private Integer eapErrorCode;
        private ObjectIdentifier productRequestID;
        private MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams;
        private String claimMemo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/31                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/31 18:06
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OnlineSorterSlotmapCompareReqParams {
        private User user;
        private String portGroup;
        private String actionCode;      // sorter action code
        private ObjectIdentifier equipmentID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 14:33
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OnlineSorterScrapWaferInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroup;
        private List<ObjectIdentifier> cassetteIDs;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 15:03
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OnlineSorterActionSelectionInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 16:43
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class WaferSorterActionRegisterReqParams {
        private User user;
        private List<Infos.WaferSorterActionList> strWaferSorterActionListSequence;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class SJCancelReqParm {
        private User user;
        private ObjectIdentifier sorterJobID;
        private Boolean notifyToTCSFlag;
        private String claimMemo;
        private List<ObjectIdentifier> sorterComponentJobIDseq;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/2 15:57
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OnlineSorterSlotmapAdjustReqParam {
        private User user;              //<i>R/Request User ID
        private String actionCode;      //<i>R/Action Code
        private ObjectIdentifier equipmentID;   //<i>R/Equipment ID
        private List<Infos.WaferSorterSlotMap> strWaferSorterSlotMapSequence;   //<i>R/Sequence of WaferSorter SlotMap
        private String portGroup;    //<i>R/Port Group
        private String physicalRecipeID;     //<i>R/Physical Recipe ID
        private List<Infos.WaferTransfer> strWaferXferSeq;  //<i>R/Sequence of Wafer Transfer
        private String adjustDirection;    //<i>R/Adjust Direction
        private String claimMemo;
    }

    @Data
    public static class OnlineSorterActionStatusInqParm {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroup;
        private String requiredData;
        private String sortPattern;
    }

    @Data
    public static class OnlineSorterActionCancelReqParm {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroup;
        private String requestTimeStamp;
        private String claimMemo;
    }

    @Data
    public static class NPWCarrierReserveCancelReqParm {
        private User user;
        private ObjectIdentifier equipmentID;
        private String portGroup;
        private List<Infos.NPWXferCassette> npwTransferCassetteList;
        private Boolean notifyToEAPFlag;
        private String opeMemo;
    }

    @Data
    public static class AlertMessageRptParams {
        private User user;
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
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/6 16:48
     */
    @Data
    public static class DChubDataSendCompleteRptInParam {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private Object siInfo;
        private User user;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/20 13:14
     */
    @Data
    public static class EDCDataUpdateForLotReqInParm {
        private ObjectIdentifier lotID;
        private ObjectIdentifier controlJobID;
        private List<Infos.DCDef> strDCDef;
        private Object siInfo;
        private User user;
        private String claimMemo;
    }

    @Data
    public static class EDCDataShowForUpdateInqParams {
        private User user;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class AMVerifyReqInParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
        private String claimMemo;
    }

    @Data
    public static class AMJObLotDeleteReqInParams {
        private User user;
        private ObjectIdentifier equipmentID;                    //<i>Equipment ID
        private ObjectIdentifier controlJobID;                   //<i>Control Job ID
        private ObjectIdentifier lotID;                          //<i>Lot ID
        private String claimMemo;
    }

    @Data
    public static class LotPlanChangeReserveCreateReqParams {
        private User user;
        private Infos.SchdlChangeReservation schdlChangeReservation;
        private String claimMemo;
    }

    @Data
    public static class LotPlanChangeReserveModifyReqParams {
        private User user;
        private Infos.SchdlChangeReservation strCurrentSchdlChangeReservation;
        private Infos.SchdlChangeReservation strNewSchdlChangeReservation;
        private String claimMemo;
    }

    @Data
    public static class LotPlanChangeReserveCancelReqParams {
        private User user;
        private List<Infos.SchdlChangeReservation> strSchdlChangeReservations;
        private String claimMemo;
    }

    @Data
    public static class ProcessControlScriptRunReqParams {
        private User user;
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier lotId;
        private String phase;
    }

    @Data
    public static class EqpFullAutoConfigChgReqInParm {
        private User user;
        private List<Infos.EqpAuto3SettingInfo> eqpAuto3SettingInfo;
        private String updateMode;
        private Object siInfo;
    }

    @Data
    public static class ReserveUnloadingLotsForIBRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier unloadReservePortID;
        private String opeMemo;
    }

    @Data
    public static class ReserveCancelUnloadingLotsForIBReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier carrierID;
        private String opeMemo;
    }

    @Data
    public static class SJCreateReqParams implements PostProcessSource {
        private User user;
        private List<Infos.SorterComponentJobListAttributes> strSorterComponentJobListAttributesSequence;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private Boolean waferIDReadFlag;
        private String sorterJobCategory;
        private String claimMemo;

        @Override
        public List<ObjectIdentifier> durableIDs() {
            List<ObjectIdentifier> retVal = new ArrayList<>();
            if (CimArrayUtils.isNotEmpty(strSorterComponentJobListAttributesSequence)) {
                strSorterComponentJobListAttributesSequence.forEach(data -> {
                    retVal.add(data.getOriginalCarrierID());
                    retVal.add(data.getDestinationCarrierID());
                });
            }
            return retVal;
        }
    }

    @Data
    public static class StepContentResetByLotReqParams {
        private User user;
        private Infos.StepContentResetByLotReqInParm stepContentResetByLotReqInParm;
        private String claimMemo;
    }


    @Data
    public static class SJListInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier createUser;
        private ObjectIdentifier sorterJobID;
    }

    @Data
    public static class SJStatusInqParams {
        private User user;
        private ObjectIdentifier sorterJobID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/19 10:55
     */
    @Data
    public static class ReticlePodInvUpdateRptParam {
        private User user;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo;
        private String claimMemo;
        private Infos.ShelfPosition shelfPosition; //for e-rack, add by nyx
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @author Decade
     * @return DurableBankInReqInParam
     * @date 2020/6/15/015 12:47
     */
    @Data
    public static class DurableBankInReqInParam {
        private User user;
        private List<ObjectIdentifier>              durableIDs;
        private ObjectIdentifier                    bankID;
        private String                              durableCategory;
        private Object                              siInfo;
        private String                              claimMemo;
    }

    @Data
    public static class DurableBankInCancelReqInParam {
        private User user;
        private ObjectIdentifier durableID;
        private String durableCategory;
        private Object siInfo;
        private String claimMemo;
    }

    @Data
    public static class DurableBankMoveReqParam {
        private User user;
        private List<ObjectIdentifier> durableIDs;
        private ObjectIdentifier toBankID;
        private String durableCategory;
        private Object siInfo;
        private String claimMemo;
    }

    @Data
    public static class DurableDeleteParam {
        private User user;
        private List<ObjectIdentifier> durableIDList;
        private String className;
        private Object siInfo;
        private String claimMemo;
    }

    @Data
    public static class CarrierDispatchAttrChgReqParm {
        private User user;
        private ObjectIdentifier cassetteID;       //<i>R/Carrier ID
        private Boolean setFlag;          //<i>R/SetFlag
        private String actionCode;       //<i>R/ActionCode
        //<c>SP_Sorter_Read             "WaferIDRead"
        //<c>SP_Sorter_MiniRead         "WaferIDMiniRead"
        //<c>SP_Sorter_PositionChange   "PositionChange"
        //<c>SP_Sorter_JustIn           "JustIn"
        //<c>SP_Sorter_JustOut          "JustOut"
        //<c>SP_Sorter_Scrap            "ScrapWafer"
        //<c>SP_Sorter_VendLot_R_And_P  "SP_Sorter_VendLot_R_And_P"
    }

    @Data
    public static class SJStartReqParams {
        private User user;
        private Infos.SJStartReqInParm sjStartReqInParm;
        private String claimMemo;
    }

    @Data
    public static class PostActionListInqParams {
        private User user;
        private String key;
        private Long seqNo;
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
        private boolean additionalInfoFlag;
        private SearchCondition searchCondition;
    }

    @Data
    public static class PostFilterCreateForExtReqParams {
        private User user;
        private Infos.PostFilterCreateForExtReqInParm postFilterCreateForExtReqInParm;
        private String claimMemo;
    }

    @Data
    public static class PostFilterRemoveForExtReqParams {
        private User user;
        private List<Infos.ExternalPostProcessFilterInfo> externalPostProcessFilterInfos;
        private String claimMemo;
    }

    @Data
    public static class PostFilterListForExtInqParams {
        private User user;
        private Infos.PostFilterListForExtInqInParm postFilterListForExtInqInParm;
    }

    @Data
    public static class ForceMoveOutForIBReqParams {
        private User user;
        private ObjectIdentifier equipmentID;     //<i>R/Equipment ID
        private ObjectIdentifier controlJobID;    //<i>R/Control Job ID
        private Boolean spcResultRequiredFlag;    //<i>R/SPC Result Required Flag
        private String claimMemo;
    }

    @Data
    public static class EqpAlarmHistInqParams {
        private User user;
        private ObjectIdentifier objectID;
        private String inquireType;
        private String fromTimeStamp;
        private String toTimeStamp;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/13 10:30
     */
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
    public static class DurableDeleteReqParams {
        private User user;
        private Infos.DurableDeleteInfo durableDeleteInfo;
    }

    @Data
    public static class FutureReworkReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Infos.FutureReworkDetailInfo futureReworkDetailInfo;
        private String claimMemo;
    }

    @Data
    public static class FutureReworkListInqParams {
        private User user;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class FutureReworkActionDoReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private String claimMemo;
    }

    @Data
    public static class AMRecoverReqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier eqpMonitorID;
        private String claimMemo;
    }

    @Data
    public static class SpcCheckInfoInqParams {
        private User user;
        private String flag;
    }

    @Data
    public static class BondingGroupUpdateReqInParams {
        private User user;
        private List<Infos.BondingGroupInfo> strBondingGroupInfoSeq;
        private String updateMode;
        private String claimMemo;
    }

    @Data
    public static class BondingMapResultRptInParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.BondingMapInfo> bondingMapInfoList;
        private String opeMemo;
    }

    @Data
    public static class BondingGroupListInqInParams {
        private User user;
        private String bondingGroupID;             //<i>Bonding Group ID
        private String bondingGroupState;          //<i>Bonding Group State
        private ObjectIdentifier targetEquipmentID;          //<i>Target Equipment ID
        private ObjectIdentifier controlJobID;               //<i>Control Job ID
        private ObjectIdentifier updateUserID;               //<i>Update User ID
        private ObjectIdentifier lotID;                      //<i>Lot ID
        private ObjectIdentifier baseProductID;              //<i>Base Product ID
        private ObjectIdentifier topProductID;               //<i>Top Product ID
        private Boolean bondingMapInfoFlag;         //<i>Bonding Map Information Inquiry Flag
    }

    @Data
    public static class StackedWaferListInqInParams {
        private User user;
        private ObjectIdentifier lotID;                      //<i>Lot ID
        private ObjectIdentifier cassetteID;                 //<i>Cassette ID
        private Boolean baseLotSearchFlag;          //<i>Base Lot Search Flag
    }

    @Data
    public static class LotListInBondingFlowInqInParams {
        private User user;
        private List<Infos.HashedInfo> strSearchConditionSeq;
        private boolean bSiviewFlag;
    }

    @Data
    public static class BondingFLowListInqInParams {
        private User user;
    }

    @Data
    public static class BondingLotListInqInParams {
        private User user;
        private ObjectIdentifier productID;                  //<i>Base Product ID
        private ObjectIdentifier targetRouteID;              //<i>Target Route ID
        private String targetOperationNumber;      //<i>Target Operation Number
        private ObjectIdentifier targetEquipmentID;          //<i>Target Equipment ID
        private String selectCriteria;             //<i>Select Criteria
    }

    @Data
    public static class BOMPartsDefinitionInqInParams {
        private User user;
        private ObjectIdentifier lotID;                  //<i>Lot ID
        private ObjectIdentifier productID;              //<i>Product ID
    }

    @Data
    public static class BOMPartsLotListForProcessInqInParams {
        private User user;
        private ObjectIdentifier lotID;                      //<i>Lot ID
        private ObjectIdentifier productID;                  //<i>Product ID
        private ObjectIdentifier routeID;                    //<i>Route D
        private String operationNumber;            //<i>Operation Number
        private ObjectIdentifier operationID;                //<i>Operation ID
    }

    @Data
    public static class WaferStackingReqInParams {
        private User user;
        private String bondingGroupID;             //<i>Bonding Group ID
        private String claimMemo;
    }

    @Data
    public static class WaferStackingCancelReqInParams {
        private User user;
        private List<ObjectIdentifier> topLotIDSeq;
        private String claimMemo;
    }

    @Data
    public static class SLMStartLotsReservationReqInParams implements PostProcessSource{
        private User user;
        private ObjectIdentifier equipmentID;                   //<i>Equipment ID
        private String portGroupID;                   //<i>Port Group ID
        private ObjectIdentifier controlJobID;                  //<i>Control Job ID
        private List<Infos.StartCassette> startCassetteList;              //<i>Sequence of Start Carrier
        private List<Infos.MtrlOutSpec> mtrlOutSpecList;             //<i>Sequence of Material Out Spec
        private String opeMemo;

        @Override
        public ObjectIdentifier equipmentID() {
            return this.equipmentID;
        }

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return this.startCassetteList.stream()
                    .flatMap(startCassette -> {
                        if (CimStringUtils.equals(startCassette.getLoadPurposeType(),
                                BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                            return null;
                        }
                        return startCassette.getLotInCassetteList().stream();
                    })
                    .filter(Objects::nonNull)
                    .filter(lotInCassette -> CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()))
                    .map(Infos.LotInCassette::getLotID)
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class SLMWaferRetrieveCassetteReserveReqInParams {
        private User user;
        private ObjectIdentifier equipmentID;           //<i>Equipment ID
        private ObjectIdentifier lotID;                 //<i>Lot ID
        private ObjectIdentifier controlJobID;          //<i>Control Job ID
        private ObjectIdentifier cassetteID;            //<i>Cassette ID
        private ObjectIdentifier destPortID;            //<i>Destination Port ID
        private List<Infos.MtrlOutSpec> mtrlOutSpecList;     //<i>Sequence of Material Out Spec
        private String actionCode;            //<i>Action Code
        private String claimMemo;
    }

    @Data
    public static class SLMProcessJobStatusRptInParams {
        private User user;
        private ObjectIdentifier equipmentID;                //<i>Equipment ID
        private ObjectIdentifier controlJobID;               //<i>Control Job ID
        private String processJobID;               //<i>Process Job ID
        private List<ObjectIdentifier> waferSeq;                   //<i>Sequence of Wafer ID
        private String actionCode;                 //<i>Action Code
    }

    @Data
    public static class SLMWaferStoreRptInParams {
        private User user;
        private ObjectIdentifier equipmentID;            //<i>Equipment ID
        private ObjectIdentifier controlJobID;           //<i>Control Job ID
        private String processJobID;           //<i>Process Job ID
        private List<Infos.SlmSlotMap> slmSrcSlotMapList;    //<i>Sequence of SLM Source Slot Map
    }

    @Data
    public static class SLMWaferRetrieveRptInParams {
        private User user;
        private ObjectIdentifier equipmentID;             //<i>Equipment ID
        private ObjectIdentifier controlJobID;            //<i>Control Job ID
        private String processJobID;            //<i>Process Job ID
        private List<Infos.SlmSlotMap> slmDestSlotMapList;    //<i>Sequence of SLM Destination Slot Map
    }

    @Data
    public static class SLMCassetteDetachFromCJReqInParams {
        private User user;
        private ObjectIdentifier equipmentID;     //<i>Equipment ID
        private ObjectIdentifier controlJobID;    //<i>Control Job ID
        private ObjectIdentifier cassetteID;      //<i>Cassette ID
        private String opeMemo;
    }

    @Data
    public static class BondingGroupPartialReleaseReqInParam {
        private User user;
        private List<Infos.BondingGroupReleaseLotWafer> strBondingGroupReleaseLotWaferSeq;
        private String claimMemo;
    }

    @Data
    public static class APCRecipeParameterAdjustInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> strStartCassette;
        private List<Infos.APCRunTimeCapabilityResponse> strAPCRunTimeCapabilityResponse;
        private boolean finalBoolean;
    }

    @Data
    public static class APCRunTimeCapabilityInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> strStartCassette;
        private boolean sendTxFlag;
    }

    @Data
    public static class APCIFListInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class EqpCandidateForBondingInqInParams {
        private User user;
        private List<String> bondingGroupIDSeq;
    }

    @Data
    public static class SLMCassetteUnclampReqInParams {
        private User user;
        private ObjectIdentifier equipmentID;    //<i>Equipment ID
        private ObjectIdentifier cassetteID;     //<i>Cassette ID
        private ObjectIdentifier portID;         //<i>Port ID
        private String claimMemo;
    }

    @Data
    public static class SLMSwitchUpdateReqInParams {
        private User user;
        private ObjectIdentifier equipmentID;     //<i>Equipment ID
        private String FMCMode;                 //<i>SLM Switch
        private String claimMemo;
    }

    @Data
    public static class FmcRsvMaxCountUpdateReqInParams {
        private User user;
        private ObjectIdentifier equipmentID;     //<i>Equipment ID
        private ObjectIdentifier equipmentContainerID;     //<i>equipmentContainerID
        private Integer maxRsvCount;
        private String claimMemo;
    }

    @Data
    public static class EntityListInqParams {
        private User user;
        private String entityClass;
        private String searchKeyName;
        private String searchKeyValue;
        private String option;
    }

    @Data
    public static class APCIFPointReqParams {
        private User user;
        private String operation;
        private Infos.APCIf apcIf;
    }

    @Data
    public static class SLMCandidateCassetteForRetrievingInqInParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier portID;
    }

    @Data
    public static class EqpMonitorUsedCountUpdateReqInParam {
        private User user;
        private List<Infos.LotEqpMonitorWaferUsedCount> strEqpLotMonitorWaferUsedCountSeq;
        private String claimMemo;
    }

    @Data
    public static class SortJobCheckConditionReqInParam {
        private User user;
        private List<Infos.SorterComponentJobListAttributes> strSorterComponentJobListAttributesSequence;
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private String claimMemo;
    }

    @Data
    public static class DurableBankInParam {
        private User user;
        private Boolean                             onRouteFlag;
        private String                              durableCategory;
        private ObjectIdentifier                    durableID;
        private ObjectIdentifier                    bankID;
        private Object                              siInfo;
    }

    @Data
    public static class DurablesInfoForStartReservationInqInParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private String durableCategory;
        private List<ObjectIdentifier> durableIDs;
    }

    @Data
    public static class DurablesInfoForOpeStartInqInParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private List<ObjectIdentifier> durableIDs;
        private String durableCategory;
        private ObjectIdentifier durableControlJobID;
    }

    @Data
    public static class DurableOperationListInqInParam {
        private User user;
        private boolean searchDirection;            //<i>Search Direction
        private boolean posSearchFlag;              //<i>Pos Search Flag
        private long searchCount;                //<i>Search Count
        private boolean currentFlag;                //<i>Current Flag
        private String durableCategory;            //<i>Durable Category
        private ObjectIdentifier durableID;                  //<i>Durable ID
    }

    @Data
    public static class ProcessOperationListForDurableDRInParam {
        private User user;
        private boolean searchDirection;            //<i>Search Direction
        private boolean posSearchFlag;              //<i>Pos Search Flag
        private long searchCount;                //<i>Search Count
        private boolean currentFlag;                //<i>Current Flag
        private String durableCategory;            //<i>Durable Category
        private ObjectIdentifier durableID;                  //<i>Durable ID
        private ObjectIdentifier searchRouteID;                  //<i>searchRoute ID
        private String searchOperationNumber;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/22 10:09
     */
    @Data
    public static class DurableOperationListFromHistoryInqInParam {
        private User user;
        private Long                            searchCount;
        private String                          durableCategory;
        private ObjectIdentifier                durableID;
        private Object                             siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/22 10:09
     */
    @Data
    public static class DurableOperationHistoryInqInParam {
        private User user;
        private String                          durableCategory;
        private ObjectIdentifier                durableID;
        private ObjectIdentifier                routeID;
        private ObjectIdentifier                operationID;
        private String                          operationNumber;
        private String                          operationPass;
        private String                          operationCategory;
        private Boolean                         pinPointFlag;
        private Object                             siInfo;
    }

    @Data
    public static class DurableHoldListInqInParam {
        private User user;
        private String                          durableCategory;
        private ObjectIdentifier                durableID;
        private String                          claimMemo;
        private Object                             siInfo;
    }

    @Data
    public static class DurableOperationStartReqInParam {
        private User user;
        private ObjectIdentifier equipmentID;                           //<i>Equipment ID
        private ObjectIdentifier durableControlJobID;                   //<i>Durable Control Job ID
        private String durableCategory;                       //<i>Durable Category
        private List<Infos.StartDurable> strStartDurables;                      //<i>Sequence of Start Durable
        private Infos.DurableStartRecipe strDurableStartRecipe;                 //<i>Durable Start Recipe
        private String claimMemo;
    }

    @Data
    public static class DurableProcessLagTimeUpdateReqInParm {
        private User user;
        private String durableCategory;                        //<i>Durable Category
        private ObjectIdentifier durableID;                              //<i>Durable ID
        private String action;                                 //<i>Action
        private String claimMemo;
    }

    @Data
    public static class DurablePostProcessActionRegistReqInParam {
        private User user;
        private String txID;                       //<i>Transaction ID
        private String patternID;                  //<i>Pattern ID
        private String key;                        //<i>Key
        private long seqNo;                      //<i>Sequence Number
        private Infos.DurablePostProcessRegistrationParm strDurablePostProcessRegistrationParm; //<i>Sequence of Durable Post Process Registration
        private String claimMemo;
    }

    @Data
    public static class DurablePFXDeleteReqInParam {
        private User user;
        private String durableCategory;                       //<i>Durable Category
        private List<ObjectIdentifier> durableIDs;                            //<i>Sequence of Durable ID
        private String claimMemo;
    }

    @Data
    public static class DurablePFXCreateReqInParam {
        private User user;
        private String durableCategory;                       //<i>Durable Category
        private List<ObjectIdentifier> durableIDs;                            //<i>Sequence of Durable ID
        private ObjectIdentifier routeID;                               //<i>Route ID
        private String claimMemo;
    }

    @Data
    public static class DurableOperationStartCancelReqInParam {
        private User user;
        private ObjectIdentifier equipmentID;                           //<i>Equipment ID
        private ObjectIdentifier durableControlJobID;                   //<i>Durable Control Job ID
        private String claimMemo;
    }

    @Data
    public static class SortJobPriorityChangeReqParam {
        private User user;
        private List<ObjectIdentifier> jobIDs;
        private String jobType;
        private String claimMemo;
    }

    @Data
    public static class DurableBankInByPostProcReqInParam {
        private User user;
        private String durableCategory;                           //<i>Equipment ID
        private ObjectIdentifier durableID;                   //<i>Durable Control Job ID
        private String claimMemo;
    }

    @Data
    public static class DurableControlJobManageReqInParam {
        private User user;
        private String controlJobAction;
        private ObjectIdentifier durableControlJobID;                   //<i>Durable Control Job ID
        private Infos.DurableControlJobCreateRequest strDurableControlJobCreateRequest;
        private String claimMemo;
    }


    @Data
    public static class DurableGatePassReqInParam {
        private User user;
        private List<Infos.GatePassDurableInfo> strGatePassDurableInfo;
        private String claimMemo;
    }

    @Data
    public static class DurableOpeCompReqInParam {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier durableControlJobID;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/28 14:08
     */
    @Data
    public static class DurableOpeLocateReqInParam {
        private User                            user;
        private Boolean                         locateDirection;
        private String                          durableCategory;
        private ObjectIdentifier                durableID;
        private ObjectIdentifier                currentRouteID;
        private String                          currentOperationNumber;
        private ObjectIdentifier                routeID;
        private ObjectIdentifier                operationID;
        private String                          operationNumber;
        private Infos.ProcessRef                processRef;
        private Long                            seqno;
        private Object                          siInfo;
        private String                          claimMemo;
    }


    @Data
    public static class HoldDurableReqInParam {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private List<Infos.DurableHoldList> durableHoldLists;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/28 14:08
     */
    @Data
    public static class WhatNextDurableListInqInParam {
        private User                            user;
        private ObjectIdentifier                equipmentID;                //<i>Equipment ID
        private String                          durableCategory;            //<i>Durable Category
        private String                          selectCriteria;             //<i>Select Criteria
        private Object                          siInfo;                     //<i>Reserved for SI customization
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/28 14:08
     */
    @Data
    public static class StartDurablesReservationReqInParam {
        private User                                 user;
        private ObjectIdentifier                     equipmentID;                           //<i>Equipment ID
        private String                               durableCategory;                       //<i>Durable Category
        private List<Infos.StartDurable>             strStartDurables;                      //<i>Sequence of Start Durable
        private Infos.DurableStartRecipe             strDurableStartRecipe;                 //<i>Durable Start Recipe
        private Object                               siInfo;                                //<i>Reserved for SI customization
        private String                               claimMemo;
    }

    @Data
    public static class RunCardListInqParams {
        private User user;
        private String runCardID;
        private String runCardState;
        private SearchCondition searchCondition;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class RunCardUpdateReqParams {
        private User user;
        private String runCardID;
        private String runCardState;
        private ObjectIdentifier lotID;
        private ObjectIdentifier owner;
        private String createTime;
        private String updateTime;
        private List<Infos.RunCardPsmInfo> runCardPsmDocInfos;
        private String claimMemo;
    }

    @Data
    public static class RunCardDeleteReqParams {
        private User user;
        private List<String> runCardIDs;
    }


    @Data
    public static class RunCardStateApprovalReqParams {
        private User user;
        private String approvalInstruction;
        private List<String> runCardIDs;
        private String claimMemo;
    }

    @Data
    public static class RunCardStateChangeReqParams {
        private User user;
        private String runCardChangeState;
        private List<String> runCardIDs;
        private String claimMemo;
        private Boolean autoCompleteFlag;
    }

    @Data
    public static class CopyFromInqParams {
        private User user;
        private Infos.FPCInfo fpcInfo;
        private ObjectIdentifier productID;
        private ObjectIdentifier lotID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/28 14:08
     */
    @Data
    public static class StartDurablesReservationCancelReqInParam {
        private User                                 user;
        private ObjectIdentifier                     equipmentID;                           //<i>Equipment ID
        private ObjectIdentifier                     durableControlJobID;                   //<i>Durable Control Job ID
        private Object                               siInfo;                                //<i>Reserved for SI customization
        private String                               claimMemo;
    }

    @Data
    public static class HoldDurableReleaseReqParams {
        private User user;
        private Infos.HoldDurableReleaseReqInParam holdDurableReleaseReqInParam;
        private String claimMemo;
    }

    @Data
    public static class ReworkDurableCancelReqParams {
        private User user;
        private Infos.ReworkDurableCancelReqInParam reworkDurableCancelReqInParam;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/7/10 10:27
     */
    @Data
    public static class ReworkDurableReqInParam {
        private User                            user;
        private String                          durableCategory;            //<i>Durable Category
        private ObjectIdentifier                durableID;                  //<i>Durable ID
        private ObjectIdentifier                currentRouteID;             //<i>Current Route ID
        private String                          currentOperationNumber;     //<i>Current Operation Number
        private ObjectIdentifier                subRouteID;                 //<i>Sub Route ID
        private String                          returnOperationNumber;      //<i>Return Operation Number
        private ObjectIdentifier                reasonCodeID;               //<i>Reason Code ID
        private Boolean                         bForceRework;               //<i>ForceRework Flag
        private Boolean                         bDynamicRoute;              //<i>DynamicRoute Flag
        private Object                          siInfo;                     //<i>Reserved for SI customization
        private String                          claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/7/10 10:27
     */
    @Data
    public static class RouteOperationListForDurableInqInParam {
        private User                            user;
        private String                          durableCategory;            //<i>Durable Category
        private ObjectIdentifier                durableID;                  //<i>Durable ID
        private Object                          siInfo;                     //<i>Reserved for SI customization
        private String                          claimMemo;
    }

    @Data
    public static class HoldDurableReqParams {
        private User user;
        private HoldDurableReqInParam holdDurableReqInParam;
        private String claimMemo;
    }

    @Data
    public static class ConnectedDurableRouteListInqParams {
        private User user;
        private Infos.ConnectedDurableRouteListInqInParam connectedDurableRouteListInqInParam;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/7/10 10:27
     */
    @Data
    public static class DurableForceOpeLocateReqInParam {
        private Boolean                         locateDirection;            //<i>Location Direction
        private String                          durableCategory;            //<i>Durable Category
        private ObjectIdentifier                durableID;                  //<i>Durable ID
        private ObjectIdentifier                currentRouteID;             //<i>Current Route ID
        private String                          currentOperationNumber;     //<i>Current Operation Number
        private ObjectIdentifier                routeID;                    //<i>Route ID
        private ObjectIdentifier                operationID;                //<i>Operation ID
        private String                          operationNumber;            //<i>Operation Number
        private Infos.ProcessRef                processRef;                 //<i>Process Reference
        private Long                            seqno;                      //<i>Sequence number
        private Object                          siInfo;                     //<i>Reserved for SI customization
        private User user;
        private String claimMemo;
    }

    @Data
    public static class DurableJobStatusSelectionInqParams {
        private User user;
        private String durableCategory;
        private ObjectIdentifier durableID;
    }

    @Data
    public static class DurableJobStatusChangeReqParams {
        private User user;
        private String durableCategory;
        private ObjectIdentifier durableID;
        private ObjectIdentifier durableStatus;
        private String jobStatus;
        private String claimMemo;
    }

    @Data
    public static class DurableJobStatusChangeRptParams {
        private User user;
        private String durableCategory;
        private ObjectIdentifier durableID;
        private String jobStatus;
        private String equipmentID;
        private String chamberID;
        private String claimMemo;
    }

    @Data
    public static class ReworkDurableWithHoldReleaseReqInParam {
        private User user;
        private ReworkDurableReqInParam      strReworkDurableReqInParam; //<i>Rework Durable Request
        private ObjectIdentifier                releaseReasonCodeID;        //<i>Release Reason Code ID
        private List<Infos.DurableHoldList>      strDurableHoldList;         //<i>Sequence of Durable Hold
        private Object                             siInfo;                     //<i>Reserved for SI customization
        private String claimMemo;
    }

    @Data
    public static class ErackPodInfoInqParams {
        private User user;
        private ObjectIdentifier stockerID;
    }

    @Data
    public static class WhatReticleActionListInqParams {
        private User user;
        private String dispatchStationID;
    }

    @Data
    public static class WhatReticleRetrieveInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class ReticlePodStockerInfoInqParams {
        private User user;
        private ObjectIdentifier stockerID;
    }

    @Data
    public static class WhereNextForReticlePodInqParams {
        private User user;
        private ObjectIdentifier reticlePodID;
    }

    @Data
    public static class AsyncReticleXferJobCreateReqParams {
        private User user;
        private ObjectIdentifier                             reticleID;
        private ObjectIdentifier                             reticlePodID;
        private ObjectIdentifier                             toMachineID;
        private String                                       opeMemo;
    }

    @Data
    public static class ReticleOfflineRetrieveReqParams {
        private User user;
        private ObjectIdentifier                        machineID;                       //<i>R/EquipmentID or StockerID //PSIV00002617
        private ObjectIdentifier                        portID;                         //<i>R/Port ID                  //PSIV00002617
        private ObjectIdentifier                        reticlePodID;                    //<i>R/Reticle Pod ID           //PSIV00002617
        private List<Infos.ReticleRetrieveResult>       reticleRetrieveResult;        //<i>R/Reticle Retrieve Result  //PSIV00002617
        private boolean                           reportFlag; // if need to call rtms to update the reticle info
        private String             opeMemo;
    }

    @Data
    public static class EqpRSPPortAccessModeChangeReqParams {
        private User user;
        private ObjectIdentifier    equipmentID;       //<i>R/Equipment ID          //PSIV00002617
        private ObjectIdentifier    reticlePodPortID;  //<i>O/Reticle Pod Port ID   //PSIV00002617
        private String              newAccessMode;     //<i>R/New Access Mode       //PSIV00002617
        private Boolean             notifyToEAPFlag;   //<i>R/Notify to EAP Flag    //PSIV00002617
        private String              opeMemo;
    }

    @Data
    public static class EqpRSPPortStatusChangeRpt {
        private User user;
        private ObjectIdentifier                  equipmentID;
        private List<Infos.EqpRSPPortEventOnEAP>  strEqpRSPPortEventOnEAP;
        private String                            opeMemo;
    }

    @Data
    public static class RSPXferStatusChangeRptParams {
        private User user;
        private ObjectIdentifier reticlePodID;
        private String xferStatus;
        private Boolean ManualInFlag;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private String claimMemo;
    }

    @Data
    public static class ReticleRetrieveReqParams {
        private User user;
        private String reticleDispatchJobID;
        private String reticleComponentJobID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier resourceID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.MoveReticles> moveReticlesList;
        private String opeMemo;
    }

    @Data
    public static class ReticleActionReleaseErrorReqParams {
        private User user;
        private String reticleDispatchJobID;
        private ObjectIdentifier machineID;
        private String claimMemo;
    }

    @Data
    public static class ReticleActionReleaseReqParams {
        private User user;
        private String reticleDispatchJobID;
        private String opeMemo;
    }

    @Data
    public static class ReticleDispatchJobCancelReqParams {
        private User user;
        private String dispatchJobID;
        private String opeMemo;
    }

    @Data
    public static class ReticleComponentJobRetryReqParams {
        private User user;
        private String reticleDispatchJobID;
        private String reticleComponentJobID;
        private String opeMemo;
    }

    @Data
    public static class ReticleOfflineStoreReqParams {
        private User user;
        private ObjectIdentifier                  machineID;                //<i>R/EquipmentID or StockerID          //PSIV00002617
        private ObjectIdentifier                  portID;                   //<i>R/Port ID                           //PSIV00002617
        private ObjectIdentifier                  reticlePodID;             //<i>R/Reticle Pod ID                    //PSIV00002617
        private List<Infos.ReticleStoreResult>    reticleStoreResultList; //<i>R/Sequence of Reticle Store Result  //PSIV00002617
        private boolean                           reportFlag; // if need to call rtms to update the reticle info
        private String opeMemo;
    }

    @Data
    public static class ReticleComponentJobSkipReqParams {
        private User user;
        private String reticleDispatchJobID;
        private String reticleComponentJobID;
        private String opeMemo;
    }

    @Data
    public static class ReticleDispatchJobDeleteReqParams {
        private User user;
        private String reticleDispatchJobID;
        private String claimMemo;
    }

    @Data
    public static class ReticleDispatchJobInsertReqParams {
        private User user;
        private Infos.ReticleDispatchJob strReticleDispatchJob;
        private String claimMemo;
    }

    @Data
    public static class ReticleRetrieveJobCreateReqParams {
        private User user;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.MoveReticles> moveReticlesList;
        private String  opeMemo;
    }

    @Data
    public static class ReticleRetrieveRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier resourceID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.ReticleRetrieveResult> reticleRetrieveResultList;
        private String opeMemo;
    }

    @Data
    public static class ReticleStoreJobCreateReqParams {
        private User user;
        private ObjectIdentifier machineID;
        private ObjectIdentifier portID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.MoveReticles> moveReticlesList;
        private String opeMemo;
    }

    @Data
    public static class ReticleStoreReqParams {
        private User user;
        private String reticleDispatchJobID;
        private String reticleComponentJobID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier resourceID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.MoveReticles> moveReticleList;
        private String opeMemo;
    }

    @Data
    public static class RecipeTimeSetParams {
        private User user;
        private String recipeID;
        private Integer time;
    }

    @Data
    public static class RecipeUseSetParams {
        private User user;
        private List<Infos.TimeRecipeUse> timeRecipeUseList;
    }

    @Data
    public static class RecipeTimeCancelParams {
        private User user;
        private String recipeID;
    }

    @Data
    public static class RecipeTimeInqParams {
        private User user;
        private String recipeID;
    }

    @Data
    public static class MfgModifyMMReqParams {
        private User user;
        private ObjectIdentifier reasonCode;
        private Infos.EntityInhibitDetailInfo modifyEntityInhibitDetailInfo;
        private String claimMemo;
    }

    @Data
    public static class ReticlePodXferReqParams {
        private User user;
        private String                       reticleDispatchJobID;
        private String                       reticleComponentJobID;
        private ObjectIdentifier             reticlePodID;
        private ObjectIdentifier             fromMachineID;
        private ObjectIdentifier             fromPortID;
        private ObjectIdentifier             toMachineID;
        private ObjectIdentifier             toPortID;
        private String                       claimMemo;
    }

    @Data
    public static class ReticleDispatchAndComponentJobStatusChangeReqParams {
        private User user;
        private RetCode                     stxResult;
        private Boolean                     isRequest;      //<i>R/Request Flag      //PSIV00002617
        private String                      rdjID;        //<i>R/RDJ ID            //PSIV00002617
        private String                      rcjID;         //<i>R/RCJ ID            //PSIV00002617
        private String                      jobName;        //<i>R/Jo bName          //PSIV00002617
        private ObjectIdentifier            reticleID;     //<i>R/Reticle ID        //PSIV00002617
        private ObjectIdentifier            reticlePodID;   //<i>R/Reticle Pod ID    //PSIV00002617
        private ObjectIdentifier            fromMachineID;  //<i>R/From Machine ID   //PSIV00002617
        private ObjectIdentifier            fromPortID;     //<i>O/From Port ID      //PSIV00002617
        private ObjectIdentifier            toMachineID;    //<i>R/To Machine ID     //PSIV00002617
        private ObjectIdentifier            toPortID;       //<i>O/To Port ID        //PSIV00002617
        private String                      claimMemo;      //<i>O/Claim Memo        //PSIV00002617
    }

    @Data
    public static class ReticleStoreRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier resourceID;
        private ObjectIdentifier reticlePodID;
        private List<Infos.ReticleStoreResult> reticleStoreResultList;
        private String opeMemo;
    }

    @Data
    public static class ReticleXferJobCreateReqParams {
        private User user;
        private ObjectIdentifier reticleID;
        private ObjectIdentifier reticlePodID;
        private ObjectIdentifier toMachineID;
        private String claimMemo;
    }

    @Data
    public static class WhatReticleActionListReqParams {
        private User user;
        private String dispatchStationID;
        private String claimMemo;
    }

    @Data
    public static class ReticlePodXferJobCreateReqParams {
        private User user;
        private ObjectIdentifier            reticlePodID;     //<i>R/Reticle Pod ID   //PSIV00002617
        private ObjectIdentifier            fromMachineID;    //<i>R/From Machine ID  //PSIV00002617
        private ObjectIdentifier            fromPortID;       //<i>O/From Port ID     //PSIV00002617
        private ObjectIdentifier            toMachineID;     //<i>R/To Machine ID    //PSIV00002617
        private ObjectIdentifier            toPortID;         //<i>O/To Port ID       //PSIV00002617
        private String                      opeMemo;
    }

    @Data
    public static class ReticlePodXferJobCompRptParams {
        private User user;
        private List<Infos.ReticlePodXferJobCompInfo> strReticlePodXferJobCompInfo;
        private String claimMemo;
    }

    @Data
    public static class ReticlePodUnloadingRptParams {
        private User user;
        private ObjectIdentifier    equipmentID;             //<i>O/Equipment ID             //PSIV00002617
        private ObjectIdentifier    reticlePodPortID;        //<i>O/Reticle Pod Port ID      //PSIV00002617
        private ObjectIdentifier    bareReticleStockerID;    //<i>O/Bare Reticle StockerID   //PSIV00002617
        private ObjectIdentifier    resourceID;              //<i>O/Resource ID              //PSIV00002617
        private ObjectIdentifier    reticlePodID;            //<i>R/Reticle Pod ID           //PSIV00002617
        private String              opeMemo;                //<i>O/Claim Memo
    }

    @Data
    public static class ReticlePodUnclampRptParams {
        private User user;
        private ObjectIdentifier            machineID;      //<i>R/EquipmentID or StockerID   //PSIV00002617
        private ObjectIdentifier            portID;         //<i>R/Port ID                    //PSIV00002617
        private ObjectIdentifier            reticlePodID;   //<i>R/Reticle Pod ID             //PSIV00002617
        private Boolean                     bSuccessFlag;   //<i>R/Success Flag               //PSIV00002617
        private String                      opeMemo;      //<i>O/Claim Memo                 //PSIV00002617
    }

    @Data
    public static class ReticlePodLoadingRptParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private ObjectIdentifier bareReticleStockerID;
        private ObjectIdentifier resourceID;
        private ObjectIdentifier reticlePodID;
        private String opeMemo;
    }

    @Data
    public static class ReticlePodUnclampReqParams {
        private User user;
        private String                        reticleDispatchJobID;    //<i>R/Reticle Dispatch Job ID    //PSIV00002617
        private String                        reticleComponentJobID;   //<i>R/Reticle Component Job ID   //PSIV00002617
        private ObjectIdentifier              machineID;               //<i>R/EquipmentID or StockerID   //PSIV00002617
        private ObjectIdentifier              portID;                  //<i>R/Port ID                    //PSIV00002617
        private ObjectIdentifier              reticlePodID;            //<i>R/Reticle Pod ID             //PSIV00002617
        private String                        opeMemo;               //<i>O/Claim Memo
    }

    @Data
    public static class ReticlePodUnclampAndXferJobCreateReqParams {
        private User user;
        private ObjectIdentifier reticlePodID;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String opeMemo;
    }

    @Data
    public static class LotContaminationParams{
        private User user;
        private ObjectIdentifier lotId;
        private String contaminationLevel;
        private Integer prFlag;
        private String claimMemo;
    }


    @Data
    public static class BareReticleStockerInfoInqParams {
        private User user;
        private ObjectIdentifier stockerID;
    }

    @Data
    public static class BareReticleStockerOnlineModeChangeReqParams {
        private User user;
        private ObjectIdentifier bareReticleStockerID;
        private String newOnlineMode;
        private Boolean notifyToEAPFlag;
        private String opeMemo;
    }

    @Data
    public static class ReticleComponentJobListInqParams {
        private User user;
        private String reticleDispatchJobID;
    }

    @Data
    public static class ReticleDispatchJobListInqParams {
        private User user;
    }

    @Data
    public static class ReticlePodInventoryReqParams {
        private User user;
        private ObjectIdentifier equipmentID;      //<i>O/Equipment ID (Not Used) //PSIV00002617
        private ObjectIdentifier stockerID;         //<i>R/Stocker ID              //PSIV00002617
        private String           opeMemo;
    }

    @Data
    public static class ReticlePodOfflineLoadingReqParams {
        private User user;
        private ObjectIdentifier machineID;       //<i>R/EquipmentID or StockerID //PSIV00002617
        private ObjectIdentifier portID;          //<i>R/Port ID                  //PSIV00002617
        private ObjectIdentifier reticlePodID;    //<i>R/Reticle Pod ID           //PSIV00002617 //PSN000105591
        private Boolean          bForceLoadFlag;  //<i>R/Force Load Flag          //PSIV00002617
        private boolean                           reportFlag; // if need to call rtms to update the reticle info
        private String           opeMemo;        //<i>O/Claim Memo                //PSIV00002617
    }

    @Data
    public static class ReticlePodOfflineUnloadingReqParams {
        private User user;
        private ObjectIdentifier machineID;            //<i>R/EquipmentID or StockerID //PSIV00002617
        private ObjectIdentifier portID;               //<i>R/Port ID                  //PSIV00002617
        private ObjectIdentifier reticlePodID;         //<i>R/Reticle Pod ID           //PSIV00002617
        private Boolean          bForceUnloadFlag;     //<i>R/Force Unload Flag        //PSIV00002617
        private boolean                           reportFlag; // if need to call rtms to update the reticle info
        private String           opeMemo;            //<i>O/Claim Memo
    }

    @Data
    public static class ReticlePodUnclampJobCreateReqParams {
        private User user;
        private ObjectIdentifier            machineID;         //<i>R/EquipmentID or StockerID    //PSIV00002617
        private ObjectIdentifier            portID;            //<i>R/Port ID                     //PSIV00002617
        private ObjectIdentifier            reticlePodID;      //<i>R/Reticle Pod ID              //PSIV00002617
        private String                      opeMemo;           //<i>O/Claim Memo
    }

    @Data
    public static class ReticleInventoryReqParams {
        private User user;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private String opeMemo;
    }

    @Data
    public static class ReticlePodXferJobDeleteReqParams {
        private User user;
        private String                        jobID;
        private List<Infos.ReticlePodJob>     reticlePodJobList;
        private String                        opeMemo;
    }

    @Data
    public static class ReticlePodXferJobListInqParams {
        private User user;
        private ObjectIdentifier    reticlePodID;   //<i>O/Reticle Pod ID    //PSIV00002617
        private ObjectIdentifier    toMachineID;    //<i>R/To Machine ID     //PSIV00002617
        private ObjectIdentifier    toStockerID;    //<i>R/To Stocker ID     //PSIV00002617
        private ObjectIdentifier    fromMachineID;  //<i>R/From Machine ID   //PSIV00002617
        private ObjectIdentifier    fromStockerID;  //<i>R/From Stocker ID   //PSIV00002617
        private Boolean             detailFlag;     //<i>R/Detail Flag
    }

    @Data
    public static class WhatReticlePodForReticleXferInqParams {
        private User user;
        private ObjectIdentifier reticleID;
    }

    @Data
    public static class ConstraintListByEqpInqParams {
        private User user;
        private ObjectIdentifier equipmentID;
        private String functionRule;
        private SearchCondition searchCondition;
    }

    @Data
    public static class ConstraintHistoryListInqParams {
        private User user;
        private String functionRule;
        private Boolean specificTool;
        private SearchCondition searchCondition;
    }

    @Data
    public static class ConstraintEqpAddReqParams {
        private User user;
        private Infos.ConstraintDetailAttributes entityInhibitDetailAttributes;
        private String claimMemo;
    }

    @Data
    public static class ConstraintEqpModifyReqParams {
        private User user;                            //<i>R/Requested User ID
        private Infos.ConstraintEqpDetailInfo entityInhibitions;    //<i>R/Entity Inhibitions that you want to cancel
        private String claimMemo;
    }

    @Data
    public static class ConstraintEqpCancelReqParams {
        private User user;                            //<i>R/Requested User ID
        private List<Infos.ConstraintEqpDetailInfo> entityInhibitions;    //<i>R/Entity Inhibitions that you want to cancel
        private String claimMemo;
    }

    @Data
    public static class CreatePilotRunJobParams {
        private User user;
        private ObjectIdentifier piLotrunPlanID;                 //<i>R/piLotrun PlanID
        private ObjectIdentifier parentLotID;                 //<i>R/Parent lot ID
        private List<ObjectIdentifier> childWaferIDs;                //<i>R/Sequence of Child wafer ID
        private ObjectIdentifier mergedRouteID;               //<i>R/Merged Route ID
        private String mergedOperationNumber;       //<i>R/Merged Operation Number
        private List<Infos.ForceMeasurementInfo> ForceMeasurementInfos;
        private String claimMemo;
    }

    @Data
    public static class CreatePilotRunParams {
        private User user;
        private ObjectIdentifier lotID;                 //<i>R/lot ID
        private ObjectIdentifier mergedRouteID;               //<i>R/Merged Route ID
        private ObjectIdentifier tagEquipment;
        private ObjectIdentifier carrierID;
        private Integer pilotCount;
        private String category;
        private String action;
        private ObjectIdentifier entity;             //ricipe
        private String claimMemo;
    }

    @Data
    public static class PilotRunCancelParams {
        private User user;
        private ObjectIdentifier pilotRunID;
        private String claimMemo;

    }

    @Data
    public static class PilotRunInqParams {
        private User user;
        private ObjectIdentifier pilotRunPlanID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier carrierID;
        private String pilotRunStatus;
        private String claimMemo;
    }

    @Data
    public static class PilotRunStatusChangeParams {
        private User user;
        private ObjectIdentifier entity;
        private String entityType;
        private String Status;
        private String claimMemo;
    }

    @Data
    public static class PilotRunJobInfoParams {
        private User user;
        private ObjectIdentifier piLotRunPlanID;
    }

    @Data
    public static class CreateRecipeInfo {
        private User user;
        private ObjectIdentifier recipeID;
    }

    @Data
    public static class OcapReqParams {
        private User user;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private String ocapNo;
        private Boolean addMeasureFlag;
        private Boolean reMeasureFlag;
        private Boolean removeFlag;
        private String waferList;
        /**
         * Qiandao Task-331 add RecipeID
         */
        private ObjectIdentifier recipeID;
    }

    @Data
    public static class EDCWithSpecCheckActionByPostTaskReqParams {
        private User user;
        private ObjectIdentifier                equipmentID;                    //<i>Equipment ID
        private ObjectIdentifier                controlJobID;                   //<i>Control Job ID
        private ObjectIdentifier                lotID;                          //<i>Lot ID
        private String opeMemo;
    }

    @Data
    public static class DepartmentSectionParams{
        private User user;
        private String purpose;
    }

    @Data
    public static class ReasonCodeQueryParams{
		private User user;
		private String department;
		private String section;
		private String codeCategory;
		private List<String> groupIds;
    }

	@Data
	public static class DepartmentCheckReasonCodeParams{
		private String department;
		private String section;
		private ObjectIdentifier holdReasonCodeId;
	}

    @Data
    public static class EqpSearchForSettingEqpBoardParams {
        private String eqpId;
        private String bayId;
        private String eqpStatus;
        private String subStatus;
        private String bayGroup;
        private User user;
    }

    @Data
    public static class EqpAreaBoardListParams {
        private String workZone;
        private User user;
    }

    @Data
    public static class EqpWorkZoneListParams {
        private User user;
    }

    @Data
    public static class EqpBoardWorkZoneBindingParams {
        private User user;
        private List<ObjectIdentifier> eqpIds;
        private String category;
        private String zone;
    }

    @Data
    public static class EqpAreaCancelParams {
        private User user;
        private String category;
        private String zone;
        private ObjectIdentifier eqpId;
        private Boolean clearAll;
    }

    @Data
    public static class EqpAreaMoveParams {
		private User user;
		private List<ObjectIdentifier> eqpIds;
		private String category;
	    private String zone;
    }

    @Data
    public static class EqpAutoMoveInReserveReqParams{
        private User user;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier portID;
    }

	@Data
	public static class StrLotCassettePostProcessForceDeleteReqInParams{
		private User user;
		private ObjectIdentifier cassetteID;
		private ObjectIdentifier lotID;
		private String claimMemo;
	}

	@Data
	public static class StrDurableFillInTxPDQ025InParams {
		private ObjectIdentifier durableID;
		private String durableCategory;
	}



    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Hunag
     * @date 2020/11/11
     */
    @Data
    public static class ProbeStatusChangeParams{
        private User user;
        private ObjectIdentifier fixtureID;
        private String fixtureStatus;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/11/11 17:42
     */
    @Data
    public static class ProbeUsageCountResetReqParams{
        private User user;
        private ObjectIdentifier fixtureID;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/12 17:42
     */
    @Data
    public static class ProbeStatusMultiChangeRptParams{
        private User user;
        private List<ObjectIdentifier> fixtureID;
        private String fixtureStatus;
        private String claimMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/15 12:42
     */
    @Data
    public static class probeXferStatusChangeRptParams{
        private User user;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private String claimMemo;
        private List<Infos.XferFixture> strXferFixture;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Hunag
     * @date 2020/11/5
     */
    @Data
    public static class ProbeListInqParams{
        private User user;
        private ObjectIdentifier fixtureCategoryID;
        private ObjectIdentifier fixtureGroupID;
        private String fixturePartNumber;
        private ObjectIdentifier fixtureID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private String fixtureStatus;
        private Long maxRetrieveCount;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Hunag
     * @date 2020/11/5
     */
    @Data
    public static class ProbeIDListInqParams{
        private User user;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Hunag
     * @date 2020/11/10
     */
    @Data
    public static class ProbeStatusParams{
        private User user;
        private ObjectIdentifier fixtureID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Hunag
     * @date 2020/11/10
     */
    @Data
    public static class ProbeStockerInfoInq{
        private User user;
        private ObjectIdentifier stockerID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author Jerry_Hunag
     * @date 2020/11/10
     */
    @Data
    public static class probeGroupIdListParams{
        private User user;
    }

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/10/13 15:36                      Decade                Create
    *
    * @author Decade
    * @date 2021/10/13 15:36
    * @param null -
    * @return
    */
    @Data
    public static class ContaminationAllLotCheckParams{
        private ObjectIdentifier lotID;
        private boolean moveInFlag;
        private String contaminationIn;
        private String contaminationOut;
    }

}

