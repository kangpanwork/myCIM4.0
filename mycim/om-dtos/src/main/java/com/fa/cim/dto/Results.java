package com.fa.cim.dto;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.pp.Additional;
import com.fa.cim.dto.pp.AdditionalKeys;
import com.fa.cim.dto.pp.CheckEDCLotStatus;
import com.fa.cim.dto.pp.PostProcessSource;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/28 15:12
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Results {

    @Data
    public static class CimObjectListInqResult {
        private String className;
        private List<Infos.ObjectIDList> strObjectIDListSeq; //Sequence of Scrap wafer Information
    }


    @Data
    public static class DurableProcessLagTimeUpdateReqResult {
        private ObjectIdentifier durableID;
        private String durableCategory;
        private String  action;
    }


    @Data
    public static class EqpEAPInfoInqResult {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentStatusCode;
        private String eapEquipmentStatus;
        private List<Infos.EqpInBuffer> eqpInBufferList;
        private List<Infos.EqpProcessBuffer> eqpProcessBufferList;
        private List<Infos.EqpOutBuffer> eqpOutBufferList;
        private List<Infos.EqpControlJobInfo> eqpControlJobInfoList;
        private String siInfo;
    }

    @Data
    public static class EDCSpecCheckActionResultInqResult {
        private List<Infos.DCActionResult> strDCActionResult;
        private String siInfo;
    }

    @Data
    public static class SpecCheckResultInqResult {
        private List<Infos.DataSpecCheckResult> strDataSpecCheckResult;
        private String siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/3/19 11:11
     */
    @Data
    public static class ReticlePodInvUpdateRptResult {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<InventoriedReticlePodInfo>  strInventoriedReticlePodInfo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/6/15/015 12:49
     */
    @Data
    public static class DurableBankInReqResult {
        private RetCode  strResult;
        private List<BankInDurableResult>  strBankInDurableResults;
        private Object siInfo;
    }

    @Data
    public static class DurableBankMoveReqResult {
        private RetCode  strResult;
        private List<BankMoveDurableResult>  strBankMoveDurableResults;
        private Object siInfo;
    }

    @Data
    public static class DurableDeleteReqResult {
        private RetCode  strResult;
        private List<DurableDeleteResult>  strDurableDeleteResults;
        private Object siInfo;
    }

    @Data
    public static class DurableGatePassResult {
        private RetCode  strResult;
        private List<DurableGatePass>  strGatePassDurablesResults;
        private Object siInfo;
    }

    @Data
    public static class DurableGatePass {
        private ObjectIdentifier  durableID;
        private Integer errorCode;
        private String errorMessage;
        private Object siInfo;
    }

    @Data
    public static class OmsMsgInqResult {
        private List<Infos.OmsMsgInfo> omsMsgInqResult;
    }

    @Data
    public static class BankInDurableResult {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Integer errorCode;
        private String errorMessage;
        private Object siInfo;
    }

    @Data
    public static class BankMoveDurableResult {
        private ObjectIdentifier durableID;
        private Integer errorCode;
        private String errorMessage;
        private Object siInfo;
    }

    @Data
    public static class DurableDeleteResult {
        private ObjectIdentifier durableID;
        private Integer errorCode;
        private String errorMessage;
        private Object siInfo;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/3/19 11:11
     */
    @Data
    public static class InventoriedReticlePodInfo {
        private ObjectIdentifier            reticlePodID;
        private List<ObjectIdentifier>    reticleID;
        private String                      returnCode;
        private Object                         siInfo;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/3/17 15:15
     */
    @Data
    public static class WhatNextInqResult {
        private ObjectIdentifier                   equipmentID;
        private String                             equipmentCategory;
        private ObjectIdentifier                   lastRecipeID;
        private String                             dispatchRule;
        private Long                               processRunSizeMaximum;
        private Long                               processRunSizeMinimum;
        private List<Infos.WhatNextAttributes> strWhatNextAttributes;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @return
     * @date 2018/9/28 10:21:49
     */
    @Data
    public static class DurableStatusSelectionInqResult {
        private List<Infos.CandidateDurableStatus> strCandidateDurableStatus;
        private ObjectIdentifier durableID;
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
     * @date 2018/12/4 14:10:19
     */
    @Data
    public static class LotFuturePctrlListInqResult {
        private ObjectIdentifier lotID;
        private List<Infos.OperationFutureActionAttributes> strOperationFutureActionAttributes;
        private Object siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/15 13:24:59
     */
    @Data
    public static class CarrierBasicInfoInqResult {
        private List<Infos.DurableAttribute> strDurableAttributeSeq;
        private String siInfo;
    }

    @Data
    public static class StockerInfoInqResult {
        private ObjectIdentifier stockerID;
        private String description;
        private String stockerType;
        private ObjectIdentifier e10Status;
        private ObjectIdentifier stockerStatusCode;
        private String statusName;
        private String statusDescription;
        private Timestamp statusChangeTimeStamp;
        private ObjectIdentifier actualE10Status;
        private ObjectIdentifier actualStatusCode;
        private String actualStatusName;
        private String actualStatusDescription;
        private Timestamp actualStatusChangeTimeStamp;
        private List<Infos.ResourceInfo> resourceInfoData;
        private List<Infos.ZoneInfo> zoneInfoData;
        private List<Infos.CarrierInStocker> strCarrierInStocker;
        private Boolean utsFlag;
        private Boolean SLMUTSFlag;
        private Integer maxUTSCapacity;
        private Integer maxReticleCapacity;
        private String onlineMode;
        private Object siInfo;
    }


    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/9 10:10:41
     */
    @Data
    public static class StockerListInqResult {
        private String stockerType;
        private List<Infos.StockerInfo> strStockerInfo;
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
     * @date 2018/10/9 15:57:48
     */
    @Data
    public static class StockerForAutoTransferInqResult {
        private List<Infos.AvailableStocker> strAvailableStocker;
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
     * @date 2018/10/11 14:03:24
     */
    @Data
    public static class EqpForAutoTransferInqResult {
        private List<ObjectIdentifier> equipmentIDs;
        private String siInfo;
    }


    @Data
    public static class BankListInqResult {
        //private Page<CimBankDO> bankAttributes;  //Sequence of bank Attributes
        private Page<Infos.BankAttributes> bankAttributes;
    }

    @Data
    public static class BayListInqResult {
        private List<Infos.WorkArea> strWorkArea;
        private String siInfo;
    }

    @Data
    public static class CarrierListInqResult {
        private OmPage<Infos.FoundCassette> foundCassettePage;
    }


    @Data
    public static class CarrierListInq170Result {
        private Page<Infos.FoundCassette> foundCassette;
    }

    @Data
    public static class CarrierDetailInfoInqResult {
        private ObjectIdentifier cassetteID;            //<i>Carrier ID
        private Infos.CassetteBrInfo cassetteBRInfo;        //<i>Carrier Basic Record Information
        private Infos.CassetteStatusInfo cassetteStatusInfo;    //<i>Carrier Status Information
        private Infos.CassettePmInfo cassettePMInfo;        //<i>Carrier Preventive-Maintenance Information
        private Infos.DurableLocationInfo cassetteLocationInfo;  //<i>Carrier Location Information
        private Infos.DurableOperationInfo strDurableOperationInfo;                //<i>durable Operation Info
        private Infos.DurableWipOperationInfo strDurableWipOperationInfo;             //<i>durable WIP Operation Info
    }

    @Data
    public static class CJStatusChangeReqResult {
        private ObjectIdentifier controlJobID;
    }

    @Data
    public static class SpecCheckReqResult {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
    }

    @Data
    public static class OMSEnvInfoInqResult {
        private List<Infos.EnvVariableList> svcEnvVariableList; // Sequence of Environment Variable List
        private List<Infos.EnvVariableList> pptEnvVariableList; // Sequence of Environment Variable List
    }

    @Data
    public static class EqpInfoInqResult {
        private ObjectIdentifier equipmentID;                                //<i>eqp ID
        private Infos.EqpBrInfo equipmentBasicInfo;                          //<i>eqp Basic Record Information
        private Infos.EqpStatusInfo equipmentStatusInfo;                      //<i>eqp State Information
        private Infos.EqpPMInfo equipmentPMInfo;                         //<i>eqp Preventive-Maintenance Information
        private Infos.EqpChamberInfo equipmentChamberInfo;                     //<i>eqp Infos.Chamber Information
        private Infos.EqpPortInfo equipmentPortInfo;                        //<i>eqp port Information
        private List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList;          //<i>Sequence of eqp Inprocessing Control Job
        private Infos.EqpReservedControlJobInfo equipmentReservedControlJobInfo;          //<i>Reserved Control Job Information
        private List<ObjectIdentifier> equipmentInprocessingDurableControlJobs; //<i>Sequence of eqp InProcessing durable Control Job
        private List<ObjectIdentifier> equipmentReservedDurableControlJobs;     //<i>Reserved durable Control Job Information
        private Infos.EqpStockerInfo equipmentStockerInfo;                     //<i>eqp stocker Information
        private Infos.EqpStockerInfo equipmentOHBInfo;                         //<i>eqp stocker Information
        private Infos.EqpStockerInfo equipmentFMCOHBInfo;                      //<i>eqp stocker Information
        private List<Infos.EntityInhibitAttributes> constraintList;                        //<i>Sequence of Entity Inhibit Attribute
        private Infos.EquipmentAdditionalReticleAttribute equipmentAdditionalReticleAttribute;   //<i>eqp Additional Reticle Attribute
        private List<Infos.EqpContainer> eqpContainerList;                             //<i>Sequence of eqp Container
        private String reserve;                                   //<i>Reserved for SI customization
        private Infos.EqpBrInfo equipmentBRInfo;
    }

    @Data
    public static class EqpInfoForIBInqResult {
        private ObjectIdentifier equipmentID;                                //<i>eqp ID
        private Infos.EqpBrInfoForInternalBuffer eqpBasicInfoForInternalBuffer;    //<i>eqp Basic Record Information
        private Infos.EqpStatusInfo equipmentStatusInfo;                      //<i>eqp State Information
        private Infos.EqpPMInfo equipmentPMInfo;                         //<i>eqp Preventive-Maintenance Information
        private Infos.EqpPortInfo equipmentPortInfo;                        //<i>eqp port Information
        private Infos.EqpChamberInfo equipmentChamberInfo;                     //<i>eqp Infos.Chamber Information
        private List<Infos.EqpInternalBufferInfo> eqpInternalBufferInfos;       ////<i>Sequence of eqp Internal Buffer Information // New member structure for Internal Buffer eqp
        private List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList;          //<i>Sequence of eqp Inprocessing Control Job
        private Infos.EqpReservedControlJobInfo equipmentReservedControlJobInfo;          //<i>Reserved Control Job Information
        private List<ObjectIdentifier> equipmentInprocessingDurableControlJobs; //<i>Sequence of eqp InProcessing durable Control Job
        private List<ObjectIdentifier> equipmentReservedDurableControlJobs;     //<i>Reserved durable Control Job Information
        private Infos.EqpStockerInfo equipmentStockerInfo;                     //<i>eqp stocker Information
        private Infos.EqpStockerInfo equipmentOHBInfo;                         //<i>eqp stocker Information
        private List<Infos.EntityInhibitAttributes> constraintList;                        //<i>Sequence of Entity Inhibit Attribute
        private Infos.EquipmentAdditionalReticleAttribute equipmentAdditionalReticleAttribute;   //<i>eqp Additional Reticle Attribute
    }

    @Data
    public static class EqpListByBayInqResult {
        private ObjectIdentifier workArea;
        private List<Infos.AreaStocker> strAreaStocker;
        private List<Infos.AreaEqp> strAreaEqp;
        private Object siInfo;
    }

    @Data
    public static class HoldLotInBankReqResult {
        private List<ObjectIdentifier> lotList;
    }



    @Data
    public static class LoginCheckInqResult {
        private List<Infos.FuncList> subSystemFuncList;  // Sequence of Sub System Function Lists
        private List<ObjectIdentifier> productIDList;   // Sequence of Access Permitted Product IDs
        private List<ObjectIdentifier> machineRecipeIDList; // Sequence of Access Permitted Machine Recipe IDs
    }

    @Data
    public static class LotBankHoldResult {
        private List<Infos.HoldHistory> holdHistoryList = new ArrayList<>();
    }

    @Data
    public static class LotQuantitySubtractResult {
        private String vendorLotID;
        private String productID;
        private String productType;
        private int productCount;
        private String vendorID;
        private ObjectIdentifier bankID;
    }

    @Data
    public static class LotBankHoldReleaseResult {
        private Boolean movementFlag;
        private Boolean changeStateFlag;
        private String holdType;
        private ObjectIdentifier holdReasonCode;
        private ObjectIdentifier holdPerson;
        private Timestamp holdTime;
        private Boolean responsibleOperationFlag;
        private Boolean responsibleOperationExistFlag;
        private ObjectIdentifier releaseReasonCode;
        private Timestamp releaseTime;
        private ObjectIdentifier releasePerson;
        private String releaseClaimMemo;
    }

    @Data
    public static class LotCurrentOperationInfoResult {
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
    }


    @Data
    public static class LotFillInTxBKC007Result {
        private ObjectIdentifier lotID;
        private String vendorLotID;
        private ObjectIdentifier productID;
        private Integer productWaferCount;
        private String vendorID;
        private ObjectIdentifier bankID;
    }

    @Data
    public static class LotHoldStateGetResult {
        private String lotHoldState;
    }

    @Data
    public static class LotInfoInqResult {
        private List<Infos.LotInfo> lotInfoList;
        private Infos.LotListInCassetteInfo lotListInCassetteInfo;
        private List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList;
        private Object reserve;
    }

    @Data
    public static class AutoCreateMonitorForInProcessLotReqResult implements PostProcessSource {
        private ObjectIdentifier monitorLotID;       //<i>Monitor lot ID
        private ObjectIdentifier productID;          //<i>Product ID
        private Integer usedCount;          //<i>Used Count
        private ObjectIdentifier routeID;            //<i>Route ID
        private ObjectIdentifier operationID;        //<i>Operation ID
        private String operationNumber;    //<i>Operation Number
        private String operationName;      //<i>Operation Name

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.monitorLotID);
        }
    }

    @Data
    public static class CarrierLoadingVerifyReqResult {
        private List<Infos.LoadingVerifiedLot> loadingVerifiedLots;
    }

    @Data
    public static class WhatNextLotListResult {
        private ObjectIdentifier equipmentID;                 //<i>Eqipment ID
        private String equipmentCategory;           //<i>eqp Category
        private ObjectIdentifier lastRecipeID;                //<i>Last Recipe ID
        private String dispatchRule;                //<i>dispatch Rule
        private Integer processRunSizeMaximum;       //<i>Process Run Size Maximum
        private Integer processRunSizeMinimum;       //<i>Process Run Size Minimum
        private List<Infos.WhatNextAttributes> strWhatNextAttributes;       //<i>Sequence of Whats Next Attributes
        private Page<Infos.WhatNextAttributes> whatNextAttributesPage;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/22 10:17
     */
    @Data
    public static class DurableOperationListFromHistoryInqResult {
        private String                          durableCategory;
        private ObjectIdentifier                durableID;
        private List<Infos.OperationNameAttributesFromHistory> strOperationNameAttributes;
        private Object                             siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @return
     * @exception
     * @author ho
     * @date 2020/6/11 11:14
     */
    @Data
    public static class SeasonWhatNextLotListResult {
        private ObjectIdentifier equipmentID;
        private String equipmentCategory;
        private ObjectIdentifier lastRecipeID;
        private String dispatchRule;
        private Integer processRunSizeMaximum;
        private Integer processRunSizeMinimum;
        private List<Infos.WhatNextAttributes> seasonWhatNextAttributes;
        private List<Infos.WhatNextAttributes> productWhatNextAttributes;
    }

    @Data
    public static class LotInfoResult {
        private Infos.LotInfo lotInfo;
    }

    @Data
    public static class LotInventoryStateGetResult {
        private String lotInventoryState;
    }

    @Data
    public static class LotListByCarrierInqResult {
        private Infos.LotListInCassetteInfo lotListInCassetteInfo;        // structure of lot list in cassette information
        private List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList;  // sequence of wafer map in cassette information
    }

    @Data
    public static class MaterialPrepareCancelInfoInqResult {
        private Infos.PreparationCancelledLotInfo preparationCancelledLotInfo;    // lot preparation cacelled lot's info
        private List<Infos.NewVendorLotInfo> newVendorLotInfoList;                // new vendor lot information
        private List<Infos.PreparationCancelledWaferInfo> preparationCancelledWaferInfoList;   // preparation cancelled wafer information
    }

    @Data
    public static class MaterialPrepareCancelReqResult {
        private ObjectIdentifier bankID;              // bank where new lot was received
        private ObjectIdentifier productID;           // product specification id
        private List<Infos.ReceivedLotInfo> receivedLotInfoList;   // new vendor lot information
    }

    @Data
    public static class LotProductIDResult {
        private ObjectIdentifier productID;
    }

    @Data
    public static class LotRouteIDGetResult {
        private ObjectIdentifier routeID;
    }

    @Data
    public static class LotStateGetResult {
        private String lotState;
    }

    @Data
    public static class LotTypeLotIDAssignResult {
        private String assignedLotID;
    }

    @Data
    public static class NewProdOrderCreateReqResult {
        private List<Infos.ReleasedLotReturn> releasedLotReturnList;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/16 13:44:35
     */
    @Data
    public static class EDCPlanInfoInqResult {
        private ObjectIdentifier dcDefID;
        private String dcType;
        private String description;
        private Boolean whiteDefFlag;
        private String fpcCategory;
        private List<Infos.DCItem> strDCItemList;
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
     * @date 2018/10/16 15:56:22
     */
    @Data
    public static class EDCSpecInfoInqResult {
        private ObjectIdentifier dcSpecID;
        private String description;
        private Boolean whiteDefFlag;
        private String fpcCategory;
        private List<Infos.DCSpecDetailInfo> strDCSpecList;
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
     * @date 2018/10/17 10:25:20
     */
    @Data
    public static class ReticleListInqResult {
        private List<Infos.FoundReticle> strFoundReticle;
        private String siInfo;
    }

    @Data
    public static class PageReticleListInqResult {
        private Page<Infos.FoundReticle> strFoundReticlelList;
        private String siInfo;
    }

    @Data
    public static class PageReticlePodListInfo{
        private Page<Infos.ReticlePodListInfo> ReticlePodListInfoList;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/22 11:34:03
     */
    @Data
    public static class ReticleDetailInfoInqResult {
        private ObjectIdentifier reticleID;
        private Infos.ReticleBrInfo reticleBRInfo;
        private Infos.ReticleStatusInfo reticleStatusInfo;
        private Infos.ReticlePmInfo reticlePMInfo;
        private List<Infos.EntityInhibitAttributes> entityInhibitions;
        private Infos.ReticleAdditionalAttribute strReticleAdditionalAttribute;
        private Infos.DurableOperationInfo strDurableOperationInfo;
        private Infos.DurableWipOperationInfo strDurableWipOperationInfo;
        private String siInfo;
    }

    @Data
    public static class AutoDispatchConfigInqResult {
        private List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfoList;
        private Object siInfo;
    }

    @Data
    public static class AutoDispatchConfigModifyReqResult {
        private Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo;
    }

    @Data
    public static class EqpFullAutoConfigChgReqResult {
        private List<Results.EqpAuto3SettingUpdateResult> eqpAuto3SettingUpdateResults;
    }

    @Data
    public static class EqpAuto3SettingUpdateResult {
        private ObjectIdentifier eqpID;
        private RetCode strResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/3/13 10:29
     */
    @Data
    public static class EqpAlarmRptResult {
        private String        machineID;
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
     * @date 2018/10/29 11:10:08
     */
    @Data
    public static class ReticleTransferStatusChangeRptResult {
        private Object stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.XferReticle> strXferReticle;
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
     * @date 2018/10/22 11:13:56
     */
    @Data
    public static class ReticleStatusChangeRptResult {
        private ObjectIdentifier reticleID;
        private Infos.ReticleBrInfo reticleBRInfo;
        private Infos.ReticleStatusInfo reticleStatusInfo;
        private Infos.ReticlePmInfo reticlePMInfo;
        private String siInfo;
    }

    @Data
    public static class PostTaskRegisterReqResult {
        private String dKey;    // D_Key
        private String keyTimeStamp;    // key time stamp
        private List<Infos.PostProcessActionInfo> postProcessActionInfoList;  // sequence of post process action information
    }

    @Data
    public static class PostTaskExecuteReqResult {
        private OmCode omCode;
        private Integer lastSequenceNumber;
        private List<Infos.PostProcessActionInfo> postProcessActionInfoList; // Sequence of Post Process Action Information
        private String relatedQueueKey;     // Related Queue Key (for Hold Released lot)
        private List<Infos.OpeCompLot> strLotSpecSPCCheckResultSeq; // Sequence of OpeComp lot Information
    }

    @Data
    public static class ProductIDResult {
        private List<String> productSpecificationList;
    }

    @Data
    public static class ProductOrderInqResult {
        private Infos.ProdReqInq prodReqInq;
    }

    @Data
    public static class ReasonCodeResult {
        private String categoryID;
        private String description;
        private ObjectIdentifier codeID;
    }

    @Data
    public static class ProductOrderReleasedListInqResult {
        private OmPage<Infos.ProdReqListAttribute> productReqListAttributePage;
        private List<Infos.ProdReqListAttribute> prodReqListAttributeList;
    }

    @Data
    public static class MoveInReserveReqResult implements PostProcessSource{
        private ObjectIdentifier controlJobID; //Control Job ID

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
     * @date 2020/6/11 9:59
     */
    @Data
    public static class SeasonMoveInReserveReqResult {
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier recipeID;
        private ObjectIdentifier productID;
        private ObjectIdentifier cassetteID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/10 13:38
     */
    @Data
    public static class SeasonLotMoveInReserveReqResult {
        private ObjectIdentifier seasonLotControlJobID;
        private ObjectIdentifier productLotControlJobID;
    }

    @Data
    public static class MoveInReserveCancelReqResult implements PostProcessSource {
        private List<Infos.StartCassette> startCassetteList;

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
    }

    @Data
    public static class MoveInReserveCancelForIBReqResult implements PostProcessSource{
        private List<Infos.StartCassette> startCassetteList;

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
    }

    @Data
    public static class SorterJobListResult {
        private List<Infos.SortJobListAttributes> sortJobListAttributesList;
    }

    @Data
    public static class SourceLotListInqResult {
        private ObjectIdentifier startBankID;
        private Page<Infos.SourceLot> sourceLotList;
    }

    @Data
    public static class SPCCheckReqResult {
        private ObjectIdentifier equipmentID;       //<i>eqp ID
        private ObjectIdentifier controlJobID;      //<i>Control Job ID
        private List<Infos.SpcCheckLot> spcCheckLot;    //<i>Sequence of SPC Check lot
        private String siInfo;             //<i>Reserved for SI customization
    }

    @Data
    public static class WaferLotStartCancelInfoInqResult {
        private Infos.STBCancelledLotInfo stbCancelledLotInfo;     //<i>STB cancelled lot information
        private List<Infos.NewPreparedLotInfo> newPreparedLotInfoList;   //<i>New prepared lot information
        private List<Infos.STBCancelWaferInfo> stbCancelWaferInfoList;   //<i>STB cancelled wafer information
    }

    @Data
    public static class WaferLotStartCancelReqResult {
        private ObjectIdentifier bankID;     // bank where new lot was prepared
        private List<Infos.PreparedLotInfo> preparedLotInfoList;  // prepared lot information
    }

    @Data
    public static class WaferLotStartReqResult implements PostProcessSource{
        private ObjectIdentifier lotID;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.lotID);
        }
    }

    @Data
    public static class SubLotTypeListInqResult {
        private List<Infos.LotTypeInfo> strLotTypes;
    }

    @Data
    public static class SubLotTypeResult {
        private List<String> lotTypeSubLotTypeList;
    }

    @Data
    public static class MaterialPrepareReqResult {
        private ObjectIdentifier lotID;       // generated lot id
    }

    @Data
    public static class VendorLotReceiveReqResult {
        private String createdLotID;    //<i>Created lot ID
    }

    @Data
    public static class VendorLotReturnReqResult {
        private String vendorLotID;             //<i>Vendor lot ID
        private ObjectIdentifier productID;               //<i>Product ID
        private Integer productWaferCount;      //<i>Product Count
        private String vendorID;                //<i>Vendor ID
        private ObjectIdentifier bankID;
        private String lotState;
        private String lotInventoryState;
        private String lotFinishedState;
        private String lotHoldState;
        private String lotType;
        private ObjectIdentifier lotID;
        private String productType;
    }

    @Data
    public static class HoldLotListInqResult {
        private Page<Infos.LotHoldListAttributes> lotHoldListAttributes;
    }

    @Data
    public static class LotCurrentQueueReactivateReqResult {
        List<Infos.LotReQueueReturn> lotReQueueReturnList;
    }

    /**
     * description:
     * objEquipment_currentState_Change_out_struct
     *
     * @author PlayBoy
     * @date 2018/7/3
     */
    @Data
    public static class EqpCurrentStateChangeResult {
        private String equipmentId;
        private String equipmentStatusCode;       // Changed new status (= input) if no-error DCR 9900001
        private String E10Status;                 //E10 of changed new status if no-error DCR 9900001
        private String actualStatus;              //Actual status at this time if no-error DCR 9900001
        private String actualE10Status;           //Actual E10 status at this time if no-error DCR 9900001
        private String operationMode;             //Operation Mode when this StatusClaim DCR 9900001
        private String previousStatus;            //Status which was overwritten         0.01
        private String previousE10Status;         //E10 of previous status DCR 9900001
        private String previousActualStatus;      //Actual status at this time if no-error DCR 9900001
        private String previousActualE10Status;   //Actual E10 status at this time if no-error DCR 9900001
        private String previousOpeMode;   // OperationMode when this StatusClaim  0.01
        private String prevStateStartTime;// Overwritten status started time      0.01
    }

    @Data
    public static class FlowBatchLotRemoveReqResult {
        private ObjectIdentifier flowBatchID;                   //<i>Flow Batch ID
        private ObjectIdentifier reserveEquipmentID;            //<i>Reserve eqp ID
        List<Infos.FlowBatchedCassetteInfo> strFlowBatchedCassetteInfo;    //<i>Sequence of Flow Batched Carrier Info    //D4100036(3)
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/1/7 11:00:39
     */
    @Data
    public static class FlowBatchLotSelectionInqResult {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentStatusCode;
        private String equipmentStatusName;
        private String equipmentStatusDescription;
        private String E10Status;
        private Long maxCountForFlowBatch;
        private List<ObjectIdentifier> reserveFlowBatchIDs;
        private List<Infos.TempFlowBatch> strTempFlowBatch;
        private Object siInfo;
    }

    /**
     * description:
     * <p>
     * retCode                    strResult;           //Transaction Execution Result Information
     * OpeCompLotSequence         strOpeCompLot;       //Sequence of OpeComp lot Information
     * any siInfo;                                     //Reserved for SI customization
     *
     * @author Fly
     * @date 2018/7/19
     */
    @Data
    public static class MoveOutReqResult implements CheckEDCLotStatus {
        private List<Infos.OpeCompLot> moveOutLot;        //Sequence of OpeComp lot Information
        private List<ObjectIdentifier> holdReleasedLotIDs; //add by nyx, OpeComp needs to return holdReleasedLotIDs

        private List<Infos.ApcBaseCassette> apcBaseCassetteListForOpeComp;
        private String apcIfControlStatus;
        private String dcsIfControlStatus;

        @Override
        public void overrideLotStatus(ObjectIdentifier lotID, String lotStatus) {
            moveOutLot.stream()
                    .filter(out->CimStringUtils.equals(ObjectIdentifier.fetchValue(out.getLotID()),ObjectIdentifier.fetchValue(lotID)))
                    .findFirst().orElse(new Infos.OpeCompLot()).setLotStatus(lotStatus);

        }
    }

    @Data
    public static class PartialMoveOutReqResult implements  CheckEDCLotStatus{
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.PartialOpeCompLot> partialOpeCompLotList;
        private Object siInfo;
        private List<ObjectIdentifier> holdReleasedLotIDs;
        @Override
        public void overrideLotStatus(ObjectIdentifier lotID, String lotStatus) {
            partialOpeCompLotList.stream()
                    .filter(out->CimStringUtils.equals(ObjectIdentifier.fetchValue(out.getLotID()),ObjectIdentifier.fetchValue(lotID)))
                    .findFirst().orElse(new Infos.PartialOpeCompLot()).setLotStatus(lotStatus);

        }

    }

    /**
     * description:
     * pptMoveInReqResult_struct
     *
     * @author PlayBoy
     * @date 2018/7/24
     */
    @Data
    public static class MoveInReqResult implements PostProcessSource{
        private ObjectIdentifier controlJobID;
        /**
         * Sequence of Start Carrier;
         */
        private List<Infos.StartCassette> startCassetteList;

        @Override
        public ObjectIdentifier controlJobID() {
            return this.controlJobID;
        }

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
    }

    @Data
    public static class MoveInCancelReqResult implements PostProcessSource{
        /**
         * Sequence of Start Carrier;
         */
        private List<Infos.StartCassette> startCassetteList;

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
                            //Bug-2958
                            .filter(startCassette ->
                                    !CimObjectUtils.isEmpty(startCassette.getCassetteID())
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
    }

    /**
     * description:
     * retCode                       strResult;             //Return Codes
     * objectIdentifier              controlJobID;           //Control Job ID
     * StartCassetteSequence         strStartCassette;      //Sequence of Start Carrier
     * any siInfo;                                          //Reserved for SI customization
     *
     * @author Fly
     * @date 2018/7/19
     */
    @Data
    public static class OpeCancelReqResult {
        private RetCode strResult;
        private List<Infos.StartCassette> startCassetteList;         //<i>Sequence of Start Carrier
        private String controlJobID;
        private String siInfo;
    }

    /**
     * description:
     * lot cassette Xfer status change result
     *
     * @author ho
     * @date 2018/7/19
     */
    @Data
    public static class CarrierTransferStatusChangeRptResult {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<XferLot> strXferLot;
        private String siInfo;  //Reserved for SI Customization
    }

    /**
     * @author ho
     */
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

    /**
     * description:
     * pptChamberStatusChangeReqResult_struct
     *
     * @author PlayBoy
     * @date 2018/7/30
     */
    @Data
    public static class ChamberStatusChangeReqResult {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentStatusCode;
        private List<Infos.EqpChamberStatus> eqpChamberStatusList;
    }

    /**
     * description:
     * pptEqpStatusSelectionInqResult_struct
     *
     * @author PlayBoy
     * @date 2018/8/2
     */
    @Data
    public static class EqpStatusSelectionInqResult {
        /**
         * Current Status CimCode
         */
        ObjectIdentifier currentStatusCode;
        /**
         * Current Status Name
         */
        String currentStatusName;
        /**
         * Current Status Description
         */
        String currentStatusDescription;
        /**
         * pptCandidateE10Status_struct
         */
        Infos.CandidateE10Status candidateCurrentE10;
        /**
         * Sequence of pptCandidateE10Status struct
         */
        List<Infos.CandidateE10Status> candidateOtherE10List;

        public EqpStatusSelectionInqResult(){}

        public EqpStatusSelectionInqResult(boolean isInit) {
            if (isInit) {
                init();
            }
        }

        public void init() {
            this.currentStatusCode = new ObjectIdentifier();
            this.currentStatusName = BizConstant.EMPTY;
            this.currentStatusDescription = BizConstant.EMPTY;
            this.candidateCurrentE10 = new Infos.CandidateE10Status(true);
            this.candidateOtherE10List = new ArrayList<>();
        }
    }

    @Data
    public static class DynamicPathListInqResult {
        private List<Infos.DynamicRouteList> dynamicRouteLists;
    }

    @Data
    public static class LotsMoveInInfoInqResult {
        private ObjectIdentifier equipmentID;         //<i>eqp ID
        private String portGroupID;         //<i>port Group ID
        private ObjectIdentifier controlJobID;        //<i>Control Job ID
        private List<Infos.StartCassette> startCassetteList;    //<i>Sequence of Start Carrier
        private Boolean processJobLevelCtrl;//Qiandao MES-EAP integration add param
    }

    @Data
    public static class LotsMoveInReserveInfoInqResult {
        private ObjectIdentifier equipmentID;         //<i>eqp ID
        private List<Infos.StartCassette> strStartCassette;    //<i>Sequence of Start Carrier
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/7/28 15:05
     */
    @Data
    public static class SeasonLotsMoveInReserveInfoInqResult {
        private ObjectIdentifier equipmentID;         //<i>eqp ID
        private List<Infos.StartCassette> seasonStrStartCassette;    //<i>Sequence of Start Carrier
        private List<Infos.StartCassette> productStrStartCassette;    //<i>Sequence of Start Carrier
    }

    @Data
    public static class LotOperationSelectionInqResult {
        private ObjectIdentifier lotID;
        private Page<Infos.OperationNameAttributes> operationNameAttributesAttributes;
    }

    @Data
    public static class InProcessingLotsResult {
        private OmPage<Infos.InProcessingLot> data;
    }

    @Data
    public static class SplitLotReqResult {
        private ObjectIdentifier childLotID;
    }

    @Data
    public static class NewMfgRestrictListInqResult {
        private Page<Infos.EntityInhibitDetailInfo> strEntityInhibitions;  //<i>Sequence of Entity Inhibitions Detail Information that is registered in the database.
    }

    @Data
    public static class MfgRestrictListInq_110Result {
        private List<Infos.EntityInhibitDetailInfo> strEntityInhibitions;  //<i>Sequence of Entity Inhibitions Detail Information that is registered in the database.
    }

    @Data
    public static class MfgRestrictListByEqpInqResult {
        private List<Infos.ConstraintEqpDetailInfo> strEntityInhibitions;  //<i>Sequence of Entity Inhibitions Detail Information that is registered in the database.
    }

    @Data
    public static class MultiPathListInqResult {
        private List<Infos.ConnectedRouteList> strConnectedRouteList;    //<i>Sequence of Connected Route List
    }


    @Data
    public static class WaferScrappedHistoryInqResult {
        private List<Infos.ScrapHistories> scrapHistoriesList; //Sequence of Scrap wafer Information
    }

    @Data
    public static class BasicUserInfoInqResult {
        private String userID;
        private String userName;
        private String departmentNumber;  // department nubmer
        private String departmentName;  // department name
        private String telephoneNumber; // telephone number
        private String mailAddress;     // mail address
        private String password;
    }

    @Data
    public static class LotFamilyInqResult {
        private List<Infos.LotListAttributes> strLotListAttributes;    //<i>Sequence of lot List Attributes
    }

    /**
     * description:
     * <p>pptEDCDataItemWithTransitDataInqResult_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/12 10:26:14
     */
    @Data
    public static class EDCDataItemWithTransitDataInqResult {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/16                           Wind
     *
     * @author Wind
     * @date 2018/10/16 17:20
     */
    @Data
    public static class EqpIDListInqResult {
        private List<ObjectIdentifier> objectIdentifiers;
    }

    @Data
    public static class StockerInventoryRptResult {
        private ObjectIdentifier stockerID;
        private List<Infos.CarrierInfo> carrierInfos;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19                           Wind
     *
     * @author Wind
     * @date 2018/10/19 15:11
     */
    @Data
    public static class EqpMemoInfoInqResult {
        private ObjectIdentifier equipmentID;
        private List<Infos.EqpNote> strEqpNote;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23                           Wind
     *
     * @author Wind
     * @date 2018/10/23 10:37
     */
    @Data
    public static class EqpMemoAddReqResult {
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class LotAnnotationInqResult {
        private ObjectIdentifier lotID;                  //<i>lot ID that you want to get comments
        private List<Infos.LotCommentInfo> lotCommentInfos;    //<i>Sequence of lot Comment Information
    }

    @Data
    public static class EqpFullAutoConfigListInqResult{
        private List<Infos.EqpAuto3SettingInfo> strEqpAuto3SettingInfoSeq;
    }

    @Data
    public static class LotListByCJInqResult {
        private List<Infos.ControlJobInfo> controlJobInfoList;    //<i>Sequence of Control Job Information
    }

    @Data
    public static class LotMemoInfoInqResult {
        private ObjectIdentifier lotID;
        private List<Infos.LotNoteInfo> lotNoteInfos;
    }

    @Data
    public static class LotOpeMemoInfoInqResult {
        private ObjectIdentifier lotID;                                   //<i>lot ID
        private ObjectIdentifier routeID;                                 //<i>Route ID
        private ObjectIdentifier operationID;                             //<i>Operation ID
        private String operationNumber;                                         //<i>Operation Number
        private List<Infos.LotNoteInfo> LotOperationNoteInfoList;               //<i>Sequence of lot Operation Note Information
    }

    @Data
    public static class LotOpeMemoListInqResult {
        private ObjectIdentifier lotID;                                   //<i>lot ID
        private List<Infos.LotOperationNoteList> LotOperationNotesList;         //<i>Sequence of lot Operation Note List
    }

    @Data
    public static class LotOperationSelectionForMultipleLotsInqResult {
        private List<LotOperationSelectionInqResult> lotOperationSelectionInqResults;
    }

    @Data
    public static class ProcessDefinitionIndexListInqResult {
        private List<Infos.ProcessDefinitionIndexList> ProcessDefinitionIndexLists;
    }

    @Data
    public static class VirtualOperationWipListInqResult {
        private ObjectIdentifier operationID;   // Operation ID
        private String operationName;            // Operation Name
        private List<Infos.VirtualOperationLot> virtualOperationLotList;
        private List<Infos.VirtualOperationInprocessingLot> virtualOperationInprocessingLotList;
    }
    @Data
    public static class MainProcessFlowListInqResult {
        private List<Infos.RouteIndexInformation> routeIndexInformationList;
    }

    @Data
    public static class StageListInqResult {
        private List<Infos.StageInformation> stageInformationList;
    }
    /**
     * pptLotExtPrtyResult_struct
     */
    @Data
    public static class LotExternalPriorityResult {
        private OmCode resultCode;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class WaferAliasInfoInqResult {
        private List<Infos.AliasWaferName> aliasWaferNames;
    }

    @Data
    public static class ProcessFlowOperationListInqResult {
        private Page<Infos.OperationNameAttributes> operationNameAttributesList;

        public ProcessFlowOperationListInqResult(){}

        public ProcessFlowOperationListInqResult(Page<Infos.OperationNameAttributes> operationNameAttributesPage) {
            this.operationNameAttributesList = operationNameAttributesPage;
        }
    }

    @Data
    public static class GeneralEqpInfoInqResult {
        private ObjectIdentifier equipmentID;                                //<i>eqp ID
        private Infos.CommonEqpBrInfo equipmentBRInfo;                          //<i>eqp Basic Record Information
        private Infos.EqpStatusInfo equipmentStatusInfo;                      //<i>eqp State Information
        private Infos.EqpPMInfo equipmentPMInfo;                         //<i>eqp Preventive-Maintenance Information
        private Infos.EqpChamberInfo equipmentChamberInfo;                     //<i>eqp Infos.Chamber Information
        private Infos.EqpPortInfo equipmentPortInfo;                        //<i>eqp port Information
        private List<Infos.EqpInternalBufferInfo> equipmentInternalBufferInfo;      //<i>Sequence of eqp Internal Buffer Information
        private List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList;          //<i>Sequence of eqp Inprocessing Control Job
        private Infos.EqpReservedControlJobInfo equipmentReservedControlJobInfo;          //<i>Reserved Control Job Information
        private List<ObjectIdentifier> equipmentInprocessingDurableControlJobs;     //<i>Sequence of eqp InProcessing durable Control Job
        private List<ObjectIdentifier> equipmentReservedDurableControlJobs;           //<i>Reserved durable Control Job Information
        private Infos.EqpStockerInfo equipmentStockerInfo;                     //<i>eqp stocker Information
        private Infos.EqpStockerInfo equipmentUTSInfo;                         //<i>eqp stocker Information
        private Infos.EqpStockerInfo equipmentSLMUTSInfo;                      //<i>eqp stocker Information
        private List<Infos.EntityInhibitAttributes> entityInhibitionList;                        //<i>Sequence of Entity Inhibit Attribute
        private Infos.EquipmentAdditionalReticleAttribute equipmentAdditionalReticleAttribute;   //<i>eqp Additional Reticle Attribute
        private List<Infos.EqpContainer> eqpContainerList;                             //<i>Sequence of eqp Container
        private String reserve;                                   //<i>Reserved for SI customization
    }

    @Data
    public static class WhatNextNPWStandbyLotInqResult {
        List<Infos.WhatNextStandbyAttributes> whatNextStandbyAttributesList;
    }

    @Data
    public static class EqpStatusResetReqResult {
        private ObjectIdentifier equipmentID;                 //<i>eqp ID
        private ObjectIdentifier equipmentE10Status;          //<i>eqp E10 Status
        private ObjectIdentifier equipmentStatusCode;         //<i>eqp Status CimCode
        private String equipmentStatusName;         //<i>eqp Status Name
        private String equipmentStatusDescription;  //<i>eqp Status Description
    }

    @Data
    public static class PartialReworkWithoutHoldReleaseReqResult {
        private ObjectIdentifier childLotID;
    }

    @Data
    public static class MonitorBatchDeleteReqResult {
        private List<Infos.MonRelatedProdLots> strMonRelatedProdLots;
    }

    /**
     * objMessageQueue_Put_out_struct
     *
     * @author Yuri
     */
    @Data
    public static class MessageQueuePutResult {
        private String reserve;
    }

    @Data
    public static class SingleCarrierTransferReqResult {
        private String reserve;
    }

    @Data
    public static class CarrierReserveReqResult {
        private String claimMemo;
        private String reserve;
        private List<Infos.ReserveLot> strRsvLotCarrier;
    }

    @Data
    public static class TransportJobCreateReqResult {
        private String jobID;
        private List<JobCreateResult> jobCreateResultSequenceData;
        private String reserve;
    }

    @Data
    public static class JobCreateResult {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierReturnCode;
        private ObjectIdentifier toMachineID;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private String reserve;
    }

    @Data
    public static class LotInPostProcessFlagGetResult {
        private Boolean inPostProcessFlagOfCassette;
        private Boolean inPostProcessFlagOfLot;
    }

    /**
     * description:
     * <p>pptDCActionLotResult_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/5 17:18:35
     */
    @Data
    public static class DCActionLotResult {
        private ObjectIdentifier measurementLotID;
        private List<Infos.DCActionResultInfo> dcActionResultInfo;
    }

    /**
     * description:
     * <p>pptEDCWithSpecCheckActionReqResult_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/6 15:28:32
     */
    @Data
    public static class EDCWithSpecCheckActionReqResult {
        private MoveOutReqResult moveOutReqResult;
        private List<ObjectIdentifier> holdReleasedLotIDs;
    }

    @Data
    public static class SpcItemResult {
        private String dataItemName;
        private List<SpcChartResult> chartResults;
    }

    @Data
    public static class SpcChartResult {
        private String chartGroupID;
        private String chartID;
        private String chartType;
        private String chartRC;
        private List<SpcCheckResultByRule> returnCodes;
        private String chartOwnerMailAddress;
        private List<String> chartSubOwnerMailAddresses;
    }

    @Data
    public static class SpcCheckResultByRule {
        private String rule;
        private String description;
        private String returnCodeStatus;
    }


    @Data
    public static class EquipmentModeSelectionInqResult {
        private ObjectIdentifier equipmentID;            //<i>eqp ID
        private List<Infos.CandidatePortMode> candidatePortMode;   //<i>Sequence of pptCandidatePortMode struct
    }

    @Data
    public static class CarrierTransferReqResult {
        private List<Infos.SysMsgStockInfo> sysMstStockInfoList;
        private String reserve;
    }

    @Data
    public static class NPWUsageStateModifyReqResult {
        private String controlUseState;
        private Integer usageCount;
    }


    @Data
    public static class ReticlePodDetailInfoInqResult {
        private ObjectIdentifier reticlePodID;
        private Infos.ReticlePodBrInfo reticlePodBRInfo;
        private Infos.ReticlePodStatusInfo reticlePodStatusInfo;
        private Infos.ReticlePodPmInfo reticlePodPMInfo;
        private Infos.ReticlePodAdditionalAttribute strReticlePodAdditionalAttribute;
        private Infos.DurableLocationInfo reticlePodLocationInfo;
        private Infos.DurableOperationInfo strDurableOperationInfo;
        private Infos.DurableWipOperationInfo strDurableWipOperationInfo;
    }

    @Data
    public static class ReticleStocInfoInqResult {
        private ObjectIdentifier stockerID;
        private String description;
        private String stockerType;
        private ObjectIdentifier E10Status;
        private ObjectIdentifier stockerStatusCode;
        private String statusName;
        private String statusDescription;
        private String statusChangeTimeStamp;
        private ObjectIdentifier actualE10Status;
        private ObjectIdentifier actualStatusCode;
        private String actualStatusName;
        private String actualStatusDescription;
        private String actualStatusChangeTimeStamp;
        private List<ObjectIdentifier> reticleID;
        private List<Infos.ReticlePodInStocker> reticlePodInStocker;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/9                          Wind
     *
     * @author Wind
     * @date 2018/11/9 13:53
     */
    @Data
    public static class ReticlePodTransferStatusChangeRptResult {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.XferReticlePod> strXferReticlePod;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/13                          Wind
     *
     * @author Wind
     * @date 2018/11/13 10:19
     */
    @Data
    public static class EqpBufferInfoInqResult {
        List<Infos.BufferResourceInfo> strBufferResourceInfoSeq;
    }

    @Data
    public static class EqpStatusChangeRptResult {
        private ObjectIdentifier equipmentID;              //<i>eqp ID
        private ObjectIdentifier translatedStatusCode;    //<i>Translated Status CimCode
        private String translatedStatusName;                    //<i>Translated Status Name
        private ObjectIdentifier translatedE10Status;     //<i>Translated E10 Status
    }

    @Data
    public static class EqpStatusChangeReqResult {
        private ObjectIdentifier equipmentID;              //<i>eqp ID
        private ObjectIdentifier equipmentStatusCode;    //<i>Translated Status CimCode
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date defect person
     * comments
     * ------------------------------------------------------------
     * ---------------------------------------------------------
     *
     * @author Arnold
     * @date 2018/11/26 10:21:54
     */
    @Data
    public static class CDAInfoInqResult {
        private List<Infos.UserDefinedData> strUserDefinedDataSeq;
    }

    @Data
    public static class EDCDataItemListByKeyInqResult {
        private List<Infos.DCDefDataItem> dcDefDataItems;
        private List<Infos.DCSpecDataItem> dcSpecDataItems;
        private Infos.OperationDataCollectionSetting operationDataCollectionSetting;
    }

    @Data
    public static class EqpRecipeParameterListInqResult {
        private Boolean whiteDefFlag;
        private List<Infos.RecipeParameterInfo> strRecipeParameterInfoList;
    }

    @Data
    public static class EboardInfoInqResult{
        private ObjectIdentifier reportUserID;         //<i>Report User ID
        private String reportTimeStamp;      //<i>Report Time Stamp
        private String noticeTitle;          //<i>Notice Title
        private String noticeDescription;    //<i>Notice Description
    }

    @Data
    public static class EqpRecipeSelectionInqResult {
        private String multiRecipeCapability;    //<i>Multi Recipe Capability
        private List<Infos.MandPRecipeInfo> mandPRecipeInfo; //Sequence of Scrap wafer Information
    }

    @Data
    public static class EDCWithSpecCheckActionByPJReqResult {
        private ObjectIdentifier lotID;                      //<i>lot ID
        private String lotStatus;                  //<i>lot Status
        private String specCheckResult;            //<i>SPEC Check Result
    }

    /**
     * @author ZQI
     * @date: 2018/12/05
     */
    @Data
    public static class UploadedRecipeIdListByEqpInqResult {
        /**
         * eqp ID
         */
        private ObjectIdentifier equipmentID;
        /**
         * Sequence of Machine Recipe Information
         */
        private List<Infos.MachineRecipeInfo> strMachineRecipeInfo;
    }

    @Data
    public static class ReFlowBatchByManualActionReqResult implements PostProcessSource{
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier flowBatchID;
        private List<Infos.FlowBatchedLot> strFlowBatchedLot;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return  Optional.ofNullable(this.strFlowBatchedLot).orElseGet(Collections::emptyList).stream()
                    .map(Infos.FlowBatchedLot::getLotID)
                    .collect(Collectors.toList());

        }

        @Override
        public List<ObjectIdentifier> durableIDs() {
            return  Optional.ofNullable(this.strFlowBatchedLot).orElseGet(Collections::emptyList).stream()
                    .map(Infos.FlowBatchedLot::getCassetteID)
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class FlowBatchByManualActionReqResult implements PostProcessSource {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier flowBatchID;
        private List<Infos.FlowBatchedLot> strFlowBatchedLot;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return this.strFlowBatchedLot.stream()
                    .map(Infos.FlowBatchedLot::getLotID)
                    .collect(Collectors.toList());
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 12/10/2018 4:11 PM
     */
    @Data
    public static class EDCDataItemListByCJInqResult {
        private ObjectIdentifier equipmentID;                         //<i>eqp ID
        private ObjectIdentifier controlJobID;                        //<i>Data Collection Definition ID
        private List<Infos.CollectedDataItem> collectedDataItemList;  //<i>Sequence of Data Collection Items
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param   * @param null
     * @return
     * @author Scott
     * @date 2018/12/4 14:46:21
     */
    @Data
    public static class LotFuturePctrlDetailInfoInqResult {
        private ObjectIdentifier                       lotID;                         //<i>lot ID
        private List<Infos.FutureQtimeInfo>            futureQtimeInfoList;           //<i>Future Queue Time List
        private List<Infos.FutureHoldListAttributes>   futureHoldListAttributes;      //<i>Future Hold List
        private List<Infos.FutureReworkDetailInfo>     futureReworkDetailInfoListSeq; //<i>Future Rework List
        private List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfoSeq; //<i>lot ADC List
        private List<Infos.ExperimentalLotInfo>        experimentalLotInfo;           //<i>lot SPM List
        private List<Infos.FPCInfo>                    fPCInfoList;                   //<i>lot DOC List
        private List<Infos.ScriptInfo>                 scriptInfoList;                //<i>lot Script List
        private List<Infos.ProcHoldListAttributes>     procHoldListAttributes;        //<i>Process Hold List
    }

    @Data
    public static class FlowBatchStrayLotsListInqResult {
        private List<Infos.FlowBatchLostLotInfo> flowBatchLostLotsList;        //<i>Process Hold List
    }
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 12/11/2018 5:18 PM
     */
    @Data
    public static class LotOperationSelectionFromHistoryInqResult{
        private ObjectIdentifier lotID;
        private List<Infos.OperationNameAttributesFromHistory> operationNameAttributesList;
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
     * @date 2019/4/25 13:45
     */
    @Data
    public static class LotOperationHistoryInqResult{
        private List<Infos.OperationHisInfo> strOperationHisInfo;
        private Page<List<Infos.OperationHisInfo>> strOperationHisInfoPage;
        private Object siInfo;
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
     * @date 2019/4/29 14:12
     */
    @Data
    public static class HistoryInformationInqResult{
        private List<Infos.TableRecordInfo> strTableRecordInfoSeq;
        private List<Infos.TableRecordValue> strTableRecordValueSeq;
        private Page<List<Infos.TableRecordValue>> strTableRecordValuePage;
        private Object  siInfo;
    }

    @Data
    public static class MoveOutForIBReqResult{
        private List<Infos.OpeCompLot> opeCompLotList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Scott
     * @date 2018/12/13 11:28:04
     */
    @Data
    public static class FlowBatchInfoInqResult {
        private  ObjectIdentifier  reservedEquipmentID;  //<i>Reserved eqp ID
        private  Long              maxCountForFlowBatch; //<i>Max count for flowbatch
        private  List<Infos.FlowBatchInfo>  strFlowBatchInfoList; //<i>flowbatch Information with flowbatch ID and Flow Batched Cassettes
    }


    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/12/17                          Wind
      * @author Wind
      * @date 2018/12/17 16:24
      */
    @Data
    public static class ProcessHoldReqResult implements PostProcessSource {
        private List<Infos.ProcessHoldLot> heldLotIDs;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return heldLotIDs.stream()
                    .filter(processHoldLot -> processHoldLot.isExecPostProcessFlag())
                    .map(Infos.ProcessHoldLot::getLotID)
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class ProcessHoldCancelReqResult implements PostProcessSource{
        private List<Infos.ProcessHoldLot> releasedLotIDs;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return releasedLotIDs.stream()
                    .filter(processHoldLot -> processHoldLot.isExecPostProcessFlag())
                    .map(Infos.ProcessHoldLot::getLotID)
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class FloatingBatchListInqResult {
        private ObjectIdentifier equipmentID;           //<i>eqp ID
        private List<Infos.FloatBatch> floatBatches;    //<i>Sequence of FloatBatch
    }

    /**
     * description: pptEDCHistoryInqResult__120_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 12/24/2018 11:02 AM
     */
    @Data
    public static class EDCHistoryInqResult{
        private List<Infos.DCDefResult> strDCDefResult;
    }

    @Data
    public static class PassThruReqResult {
        private List<Results.GatePassLotsResult> strGatePassLotsResult;
        private List<ObjectIdentifier> holdReleasedLotIDs;
    }

    @Data
    public static class GatePassLotsResult {
        private ObjectIdentifier lotID;
        private String returnCode;
    }

    @Data
    public static class FlowBatchByAutoActionReqResult implements PostProcessSource{
        private Boolean retryFlag;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier flowBatchID;
        private List<Infos.FlowBatchedCassette> strBatchedCassette;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            if (CimArrayUtils.isEmpty(this.strBatchedCassette)) {
                return Collections.emptyList();
            }
            List<ObjectIdentifier> lotIDList = new ArrayList<>();
            for (Infos.FlowBatchedCassette flowBatchedCassette : this.strBatchedCassette) {
                lotIDList.addAll(flowBatchedCassette.getLotID());
            }
            return lotIDList;
        }

        @Override
        public List<ObjectIdentifier> durableIDs() {
            if (CimArrayUtils.isEmpty(this.strBatchedCassette)) {
                return Collections.emptyList();
            }
            List<ObjectIdentifier> retVal = new ArrayList<>();
            for (Infos.FlowBatchedCassette flowBatchedCassette : this.strBatchedCassette) {
                retVal.add(flowBatchedCassette.getCassetteID());
            }
            return retVal;
        }
    }

    @Data
    public static class UserDataUpdateResult {
        private String name;
        private String originator;
        private String actionCode;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Scott
     * @date 2019/1/8 10:54:48
     */
    @Data
    public static class WhatNextAMLotInqResult {
        private ObjectIdentifier equipmentID;                 //<i>Equipment ID
        private ObjectIdentifier eqpMonitorID;                //<i>Auto Monitor ID
        private String dispatchRule;                //<i>Dispatch Rule
        private List<Infos.WhatNextAttributes> strWhatNextAttributes;       //<i>Sequence of Whats Next Attributes
    }

    @Data
    public static class EqpMonitorNextExecutionTimeCalculateResult {
       private String nextExecutionTime;
       private String nextScheduleBaseTime;
       private String expirationTime;
    }

    @Data
    public static class AMJobListInqResult {
        private List<Infos.EqpMonitorJobDetailInfo> strEqpMonitorJobDetailInfoSeq;
    }

    @Data
    public static class AMStatusChangeRptResult {
        private String monitorStatus;                  //<i>Current Status of Auto Monitor
        private ObjectIdentifier eqpMonitorJobID;      // //<i>Auto Monitor job ID
        private String nextExecutionTime;              //<i>Next Execution Time
        private List<Infos.EqpMonitorActionInfo> strEqpMonitorActionInfoSeq;        //<i>Action info
    }

    @Data
    public static class DOCStepListInProcessFlowInqResult {
        private Boolean FPCAvailableFlag;         //<i>DOC Available Flag
        private Infos.MainRouteInfo mainRouteInfo;         //<i>Main Route Information
        private Infos.LotRouteInfoForFPC lotRouteInfoForFPC;    //<i>Lot's Route info
	}

	@Data
    public static class RecipeIdListForDOCInqResult {
        private List<Outputs.MachineRecipe> strMachineRecipeList;
    }

    @Data
    public static class DOCLotInfoInqResult {
        private List<Infos.FPCInfo> FPCInfoList;      //<i>Sequence of DOC Information
        private Boolean FPCAvailableFlag;    //<i>DOC Available Flag
    }

    @Data
    public static class CJPJOnlineInfoInqResult {
        private ObjectIdentifier equipmentID;                        //<i>Equipment ID
        private ObjectIdentifier controlJobID;                       //<i>Control Job ID
        private List<Infos.ProcessJobInfo>       processJobInfoList;               //<i>process job information sequence
    }


    @Data
    public static class CJPJProgressInfoInqResult {
        private List<Infos.StartCassette> strStartCassette;           //<i>Sequence of Start Carrier
    }


    @Data
    public static class DOCLotRemoveReqResult {
        private List<String>              fpcIDs;          //<i>Sequence of DOC ID
        private ObjectIdentifier          lotFamilyID;      //<i>Lot Family ID
        private List<ObjectIdentifier>    lotIDs;           //<i>Sequence of LotID
        private String                    siInfo;           //<i>Reserved for SI customization
    }

    @Data
    public static class DurablesInfoForOpeStartInqResult {
        private ObjectIdentifier equipmentID;                            //<i>Equipment ID
        private String portGroupID;                            //<i>Port Group ID
        private String durableCategory;                        //<i>Carrier/ReticlePod/Reticle
        private List<Infos.StartDurable> strStartDurables;                       //<i>Sequence of Start Durable
        private Infos.DurableStartRecipe strDurableStartRecipe;                  //<i>Durable Start Recipe
    }

    @Data
    public static class DurableOperationStartReqResult {
        private ObjectIdentifier durableControlJobID;                   //<i>Durable Control Job ID
        private String durableCategory;                       //<i>Durable Category
        private List<Infos.StartDurable> strStartDurables;                      //<i>Sequence of Start Durable
        private Infos.DurableStartRecipe strDurableStartRecipe;                 //<i>Durable Start Recipe
    }

    @Data
    public static class DurableOperationStartCancelReqResult {
        private String durableCategory;                       //<i>Durable Category
        private List<Infos.StartDurable> strStartDurables;                      //<i>Sequence of Start Durable
    }

    @Data
    public static class DurableOpeCompReqResult {
        private String durableCategory;                       //<i>Durable Category
        private List<Infos.StartDurable> strStartDurables;                      //<i>Sequence of Start Durable
    }

    @Data
    public static class DOCLotInfoSetReqResult {
        private List<String> FPC_IDs;          //<i>Sequence of DOC ID
        private ObjectIdentifier lotFamilyID;      //<i>Lot Family ID
        private List<ObjectIdentifier> lotIDs;           //<i>Sequence of LotID
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/20 ???? 4:23
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class PSMLotInfoInqResult {
        private Boolean editFlag;                                                 //<i>Edit Flag
        private Infos.ExperimentalLotInfo  strExperimentalLotInfo;             //<i>Experimental Lot Information             //D7000015
    }

    /**
     * description: EDCByPJRptResult
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 4/11/2019 2:03 PM
     */
    @Data
    public static class EDCByPJRptResult{
        private List<ObjectIdentifier> lotIDs;
    }
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/12                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/12 16:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ForceMoveOutReqResult implements CheckEDCLotStatus {
        private List<Infos.OpeCompLot>  strOpeCompLot;    //<i>Sequence of OpeComp Lot And Specification Check Result
        private List<ObjectIdentifier> holdReleasedLotIDs;
        private String apcIfControlStatus;

        @Override
        public void overrideLotStatus(ObjectIdentifier lotID, String lotStatus) {
            strOpeCompLot.stream()
                    .filter(out->CimStringUtils.equals(ObjectIdentifier.fetchValue(out.getLotID()),ObjectIdentifier.fetchValue(lotID)))
                    .findFirst().orElse(new Infos.OpeCompLot()).setLotStatus(lotStatus);

        }
    }

    @Data
    public static class StockerDetailInfoInqResult {
        private ObjectIdentifier                        machineID;
        private ObjectIdentifier                        e10Status;
        private List<Infos.ResourceInfo>                 resourceInfoData;
        private List<Infos.ZoneInfo>                     zoneInfoData;
        private Object                                     siInfo;
    }

    @Data
    public static class WhereNextInterByInqResult {
    	private ObjectIdentifier lotID;
    	private ObjectIdentifier cassetteID;
    	private String productionState;
    	private String holdState;
    	private String finishedState;
    	private String processState;
    	private String inventoryState;
    	private String transferStatus;
    	private ObjectIdentifier currentStockerID;
    	private ObjectIdentifier currentEquipmentID;
    	private ObjectIdentifier transferReserveUserID;
    	private List<WhereNextEqpStatus> whereNextEqpStatuses;

    }

    @Data
    public static class WhereNextEqpStatus {
    	private ObjectIdentifier equipmentID;
    	private String equipmentMode;
    	private ObjectIdentifier equipmentStatus;
    	private ObjectIdentifier e10Status;
    	private String equipmentStatusDescription;
    	private Boolean equipmentAvailableFlag;
    	private List<EqpStockerStatus> eqpStockerStatuses;
    	private List<EntityInhibitAttributes> entityInhibitions;
    }

    @Data
    public static class EqpStockerStatus {
		private ObjectIdentifier stockerID;
		private String stockerType;
		private ObjectIdentifier stockerStatus;
		private String stockerPriority;
		private String e10Status;
		private Boolean utsFlag;
		private Long maxUtsCapacity;
    }

    @Data
    public static class EntityInhibitAttributes {
		private List<EntityIdentifier> entities;
		private List<String> subLotTypes;
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

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 15:04
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierTransferJobInfoInqResult {
        private List<Results.JobResult>  strJobResult;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 15:07
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class JobResult {
        private String jobID;
        private String jobStatus;
        private String transportType;
        private List<Infos.CarrierJobResult> strCarrierJobResult;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 15:30
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class TransportJobInqResult {
        private String                                   inquiryType;
        private List<Infos.TransportJobInqData>          jobInqData;
        private Object                                   siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/18                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/18 11:27
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class LotCassetteXferJobDetailResult {
        private List<JobResult> strJobResult;
        private Object          siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 11:12
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class TransportJobCancelReqResult {
        private String                                  jobID;
        private List<Infos.CarrierJobRc>                carrierJobRcData;
        private Object                                  siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 16:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class EAPRecoveryReqResult {
        private ObjectIdentifier                    equipmentID;             //<i>Equipment ID
        private String                              eapEquipmentStatus;     //<i>EAP Equipment Status
        private List<Infos.EqpChamberStatus>        eqpChamberStatusList;    //<i>Sequence of Equipment Chamber Status
        private List<Infos.EqpInBuffer>             eqpInBufferList;          //<i>Sequence of Equipment In Buffer
        private List<Infos.EqpProcessBuffer>        eqpProcessBufferList;    //<i>Sequence of Equipment Process Buffer
        private List<Infos.EqpOutBuffer>            eqpOutBufferList;        //<i>Sequence of Equipment Out Buffer
        private Object                              siInfo;                  //<i>Reserved for SI customization
    }

    @Data
    public static class WhereNextStockerInqResult {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private String lotState;
        private String productionState;
        private String holdState;
        private String finishedState;
        private String processState;
        private String inventoryState;
        private String transferStatus;
        private ObjectIdentifier current_stockerID;
        private ObjectIdentifier current_equipmentID;
        private ObjectIdentifier transferReserveUserID;
        private List<Infos.WhereNextEqpStatus> whereNextEqpStatus;
        private Object siInfo;
    }

    @Data
    public static class DurableWhereNextStockerInqResult {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private String productionState;
        private String holdState;
        private String finishedState;
        private String processState;
        private String inventoryState;
        private String transferStatus;
        private ObjectIdentifier currentStockerID;
        private ObjectIdentifier currentEquipmentID;
        private ObjectIdentifier transferReserveUserID;
        private List<Infos.WhereNextEqpStatus> whereNextEqpStatus;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/20                              Zack                create file
     *
     * @author: Zack
     * @date: 2019/6/20 10:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OpeGuideInqResult {
        private ObjectIdentifier equipmentID;
        private List<Infos.OpeProcedureInfo> strOpeProcedureInfo;
        private Object siInfo;
    }

    @Data
    public static class AMJobLotReserveReqResult {
        private ObjectIdentifier eqpMonitorJobID;
        private String nextExecutionTime;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/4 16:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class LotCarrierTOTIReqResult {
        private List<Infos.SysMsgStockInfo> strSysMsgStockInfoSeq;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/17 17:49
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class MoveInReserveForTOTIReqResult {
        private ObjectIdentifier        controlJobID;        //<i>Control Job ID
        private String                  APCIFControlStatus;  //<i>APCIF control status
        private Object                  siInfo;                          //<i>Reserved for SI customization
    }

    @Data
    public static class RecipeDirectoryInqResult {
        private ObjectIdentifier equipmentID;
        private List<String> physicalRecipeIDs;
    }

    @Data
    public static class RecipeParamAdjustOnActivePJReqResult {
        private List<Infos.MultipleBaseResult>        strMultipleBaseResultSeq;        //<i>Control Job ID
    }

    @Data
    public static class LotPlanChangeReserveListInqResult {
        private List<Infos.SchdlChangeReservation> schdlChangeReservations;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 13:50
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeReqResult {
        private List<Infos.OwnerChangeErrorInfo> strOwnerChangeErrorInfoSeq; //<i>Error Information Sequence
        private Object                           siInfo;                     //<i>Reserved for SI customization
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 15:07
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OnlineSorterActionSelectionInqResult {
        private ObjectIdentifier                           equipmentID;                         //<i>Equipment ID
        private List<Infos.WaferSorterActionList>          strWaferSorterActionListSequence;    //<i>Sequence of Wafer Sorter Action List
        private Object                                     siInfo;
    }

    @Data
    public static class AlertMessageRptResult {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier subSystemID;
        private String systemMessageCode;
    }

    @Data
    public static class EDCDataShowForUpdateInqResult {
        private ObjectIdentifier lotID;                  //<i>LotID
        private ObjectIdentifier controlJobID;           //<i>Lot Previous controlJobID
        private ObjectIdentifier routeID;                //<i>Lot Previous Operation Route ID
        private String operationNumber;        //<i>Lot Previous Operation  Number
        private String operationPass;          //<i>Lot Previous Operation  Pass Count
        private List<Infos.DataCollectionInfo> strDCDef;               //recipeStart返参定义DCDef错定义为List<Infos.DataCollectionInfo>, 所以当前对象也只有定义DataCollectionInfo,不然赋值非常麻烦
    }

    @Data
    public static class FixtureListInqResult {
        List<Infos.FoundFixture> strFoundFixture;
    }

    @Data
    public static class CarrierReserveCancelReqResult {
        private List<Infos.ReserveCancelLot> strReserveCancelLot;
        private String claimMemo;
    }

    @Data
    public static class AMVerifyReqResult {
        private ObjectIdentifier    eqpMonitorJobID;                //<i>Auto Monitor Job ID
        private String              eqpMonitorJobState;             //<i>Status of Auto Monitor Job
        private Infos.EqpMonitorActionInfo  strPerformedEqpMonitorActions;  //<i>Performed Actions when Auto Monitor Fails
        private List<ObjectIdentifier>      removedEqpMonitorLots;          //<i>Lot IDs removed from Auto Monitor Job
    }

    @Data
    public static class AMJObLotDeleteReqResult {
        private ObjectIdentifier  eqpMonitorID;                   //<i>Auto Monitor ID
        private ObjectIdentifier  eqpMonitorJobID;                //<i>Auto Monitor Job ID
        private String   eqpMonitorJobState;             //<i>Current Status of Auto Monitor Job
        private boolean  removedFlag;                    //<i>True if the Lot is removed from Auto Monitor Job
    }

    @Data
    public static class StartLotsReservationFotInternalBufferReqResult {
        private ObjectIdentifier controlJobID;
    }

    @Data
    public static class SJStatusInqResult {
        private ObjectIdentifier controlJobID;
        private List<Infos.SorterComponentJobList>    sorterComponentJobList;    //<i>Sequence of Sorter Component Job Information
        private ObjectIdentifier                     equipmentID;                          //<i>Equipment ID
        private ObjectIdentifier                     portGroupID;                          //<i>Port Group ID
        private Boolean                              waferIDReadFlag;                      //<i>Wafer ID Read Flag
        private List<Infos.ObjectResult> objectResults;
    }

    @Data
    public static class SJInfoForAutoLotStartInqResult {
        private String equipmentID;
        private String portGroupID;
        private String inputPortID;
        private String outPutPortID;
    }

    @Data
    public static class reqCategoryGetByLotResult {
        private String carrierCategory;
    }

    @Data
    public static class PostActionListInqResult {
        private List<Infos.PostProcessActionInfo> postProcessActionInfos;       //<i>Sequence of Post Process Action Information
        private List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfos;   //<i>Sequence of Post Process Additional Information
    }

    @Data
    public static class PostActionPageListInqResult {
        private Page<Infos.PostProcessActionInfo> postProcessActionInfos;       //<i>Sequence of Post Process Action Information
        private Page<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfos;   //<i>Sequence of Post Process Additional Information
    }

    @Data
    public static class SpcCheckInfoInqResult {
        private List<String> department;
        private List<String> route;
        private List<String> process;
        private List<String> operation;
        private List<String> operationSeq;
        private List<String> lotType;
        private List<String> product;
        private List<String> customer;
        private List<String> equipment;
        private List<String> parameter;
    }

    @Data
    public static class PriorityChangeReqResult {
        private String                                  jobID;
        private List<Infos.PriorityInfoResult>          priorityInfoResultData;
        private Object                                  siInfo;
    }

    @Data
    public static class AmhsUploadInventoryReqResult {
        private List<UploadInventoryReqResult> uploadInventoryReqResults;
    }

    @Data
    public static class UploadInventoryReqResult {
        private RetCode strResult = new RetCode();
        private String shelfType;
        private String zoneID;
        private ObjectIdentifier carrierID = new ObjectIdentifier();
        private String stockInTime;
        private boolean alternateStockerFlag;
    }

    @Data
    public static class BondingGroupUpdateReqResult {
        private List<Infos.BondingGroupUpdateResult> strBondingGroupUpdateResultSeq;
    }

    @Data
    public static class BOMPartsDefinitionInqResult {
        private ObjectIdentifier productID;                  //<i>Product ID
        private ObjectIdentifier bomID;                      //<i>BOM ID
        private String bomDescription;             //<i>BOM Description
        private List<Infos.BOMPartsDefInProcess> strBOMPartsDefInProcessSeq; //<i>Sequence of BOM Parts Definition in Process
    }

    @Data
    public static class SLMWaferRetrieveCassetteReserveReqResult {
        private ObjectIdentifier lotID;
        private ObjectIdentifier controlJobID;
        private List<Infos.MtrlOutSpec> strMtrlOutSpecSeq;
        private String siInfo;
    }

    @Data
    public static class SLMCheckConditionForCassetteReserveResult {
        private Infos.EqpContainerPositionInfo strEqpContainerPositionInfo;
        private Infos.EqpContainerInfo strEqpContainerInfo;
        private String siInfo;
    }

    @Data
    public static class SLMCheckConditionForPortReserveResult {
        private Infos.EqpContainerPositionInfo strEqpContainerPositionInfo;
        private Infos.EqpContainerInfo strEqpContainerInfo;
        private String siInfo;
    }

    @Data
    public static class EntityListInqResult {
        private String entityClass;
        private List<Infos.EntityValue> entityValueList;
    }

    @Data
    public static class SLMCandidateCassetteForRetrievingInqResult {
        private List<ObjectIdentifier> cassetteIDs;
    }

    @Data
    public static class RTDDispatchDataToSLMInfoConvertoutResult {
        private List<ObjectIdentifier> cassetteIDs;
    }

    @Data
    public static class RTDDispatchListInqResult {
        private String equipmentCategory;
        List<Infos.DispatchResult> strDispatchResult;
    }

    @Data
    public static class AdjustedRecipeParamResult {
        private List<Infos.StartCassette> strStartCassette;
        private String systemErrorCode;
        private String systemErrorMessage;
        private List<Infos.AdjustedRecipeParamResultByLot> strLotResultSequence;
    }

    @Data
    public static class DurablesInfoForStartReservationInqResult {
        private ObjectIdentifier equipmentID;
        private String durableCategory;
        private List<Infos.StartDurable> strStartDurables;
        private Infos.DurableStartRecipe strDurableStartRecipe;
    }

    @Data
    public static class DurablePostProcessActionRegistReqResult {
        private String dKey;                       //<i>D_KEY
        private String keyTimeStamp;               //<i>Key TimeStamp
        private List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq; //<i>Sequence of Post Process Action Information
    }

    @Data
    public static class DurableOperationListInqResult {
        private String durableCategory;                       //<i>D_KEY
        private ObjectIdentifier durableID;               //<i>Key TimeStamp
        private List<Infos.DurableOperationNameAttributes> strDurableOperationNameAttributes; //<i>Sequence of Post Process Action Information
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/30 11:01
     */
    @Data
    public static class WhatNextDurableListInqResult {
        private ObjectIdentifier                equipmentID;                        //<i>Equipment ID
        private String                          equipmentCategory;                  //<i>Equipment  Category
        private ObjectIdentifier                lastRecipeID;                       //<i>Last Recipe ID
        private String                          dispatchRule;                       //<i>Dispatch    Rule
        private Long                            processRunSizeMaximum;              //<i>Process Run Size Maximum
        private Long                            processRunSizeMinimum;              //<i>Process Run Size Minimum
        private List<Infos.WhatNextDurableAttributes> strWhatNextDurableAttributes;  //<i>Sequence of What's Next Durable attribute
        private Object                          siInfo;                             //<i>Reserved for SI customization
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @exception
     * @author ho
     * @date 2020/6/30 11:01
     */
    @Data
    public static class StartDurablesReservationCancelReqResult {
        private String                               durableCategory;                       //<i>Durable Category
        private List<Infos.StartDurable>             strStartDurables;                      //<i>Sequence of Start Durable
        private Object                               siInfo;                                //<i>Reserved for SI customization
    }


    @Data
    public static class StockerInventoryUploadReqResult {
        private ObjectIdentifier StockerID;
        private String reasonText;
    }

    @Data
    public static class CandidateDurableJobStatusDetail {
        private String durableStatus;
        private List<String> jobStatus;
    }

    @Data
    public static class ErackPodInfoInqResult {
        private ObjectIdentifier stockerID;
        private String stockerType;
        private List<Infos.PodInErack> podInEracks;
    }

    @Data
    public static class WhatReticleActionListInqResult {
        private List<Infos.ReticleDispatchJob>           strReticleDispatchJobDeleteList;    //<i>Sequence of Reticle Dispatch Job (Delete)
        private List<Infos.ReticleDispatchJob>           strReticleDispatchJobInsertList;    //<i>Sequence of Reticle Dispatch Job (Insert)
        private List<Infos.RALSetResult>                 strRALSetResult;                    //<i>Reticle Action List set information
        private Infos.DispatchResultRet                  strDispatchResult;                  //<i>Dispatch result from RTD I/F
        private Object                                   siInfo;                             //<i>Reserved for SI customization       //<i>Dispatch result from RTD I/F
    }

    @Data
    public static class ReticlePodStockerInfoInqResult {
        private ObjectIdentifier                        stockerID;                          //<i>Stocker ID
        private String                                  description;                        //<i>Description
        private String                                  stockerType;                        //<i>Stocker Type
        private ObjectIdentifier                        e10Status;                          //<i>E10 Status
        private ObjectIdentifier                        stockerStatusCode;                  //<i>Stocker Status Code
        private String                                  statusName;                         //<i>Status Name
        private String                                  statusDescription;                  //<i>Status Description
        private String                                  statusChangeTimeStamp;              //<i>Status Change Time Stamp
        private ObjectIdentifier                        actualE10Status;                    //<i>Actual E10 Status
        private ObjectIdentifier                        actualStatusCode;                   //<i>Actual Status Code
        private String                                  actualStatusName;                   //<i>Actual Status Name
        private String                                  actualStatusDescription;            //<i>Actual Status Description
        private String                                  actualStatusChangeTimeStamp;        //<i>Actual Status Change Time Stamp
        private List<Infos.ResourceInfo>                 resourceInfoData;                   //<i>Sequence of resource information
        private List<Infos.ReticlePodInfoInStocker>      reticlePodInfoInStockerList;         //<i>Sequence of Reticle Pod Infomation in Stocker
    }

    @Data
    public static class WhereNextForReticlePodInqResult {
        private ObjectIdentifier                        reticlePodID;                     //<i>Reticle Pod ID
        private String                                  transferStatus;                   //<i>Transfer Status
        private ObjectIdentifier                        transferReserveUserID;            //<i>Transfer Reserve User ID
        private ObjectIdentifier                        currentReticlePodStockerID;       //<i>Current Reticle Pod Stocker ID
        private ObjectIdentifier                        currentBareReticleStockerID;      //<i>Current Bare Reticle Stocker ID
        private ObjectIdentifier                        currentEquipmentID;               //<i>Current Equipment ID
        List<Infos.NextMachineForReticlePod>            nextMachineForReticlePodList;      //<i>Sequence of next Machine for Reticle Pod
        private Object                                  siInfo;                           //<i>Reserved for SI customization
    }

    @Data
    public static class RSPXferStatusChangeRptResult {
        private ObjectIdentifier stockerID;                  //<i>Stocker ID
        private ObjectIdentifier equipmentID;                //<i>Equipment ID
        private List<Infos.XferReticlePod> strXferReticlePod;          //<i>Sequence Transfer Reticle Pod
    }

    @Data
    public static class RecipeTimeInqResult {
        private List<Infos.RecipeTime> recipeTimeList; //Sequence of Scrap wafer Information
    }

    @Data
    public static class ReticlePodXferReqResult {
        private String                                  transferJobNo;              //<i>Transfer job number
        private ObjectIdentifier                        reticlePodID;               //<i>Reticle Pod ID
        private String                                  returnCode;                 //<i>Return Code
        private Object                                  siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class BareReticleStockerInfoInqResult {
        private ObjectIdentifier                        stockerID;                      //<i>Stocker ID
        private String                                  description;                    //<i>Description
        private String                                  stockerType;                    //<i>Stocker Type
        private ObjectIdentifier                        e10Status;                      //<i>E10 Status
        private ObjectIdentifier                        stockerStatusCode;              //<i>Stocker Status Code
        private String                                  statusName;                     //<i>Status Name
        private String                                  statusDescription;              //<i>Status Description
        private String                                  statusChangeTimeStamp;          //<i>Status Change Time Stamp
        private ObjectIdentifier                        actualE10Status;                //<i>Actual E10 Status
        private ObjectIdentifier                        actualStatusCode;               //<i>Actual Status Code
        private String                                  actualStatusName;               //<i>Actual Status Name
        private String                                  actualStatusDescription;        //<i>Actual Status Description
        private String                                  actualStatusChangeTimeStamp;    //<i>Actual Status Change Time Stamp
        private Long                                    reticleStoreMaxCount;           //<i>Reticle Store Max Count
        private String                                  onlineMode;                     //<i>Online Mode
        private List<Infos.ReticlePodPortInfo>          strReticlePodPortIDs;           //<i>Sequence of Reticle Pod Port ID
        private List<Infos.StoredReticle>               strStoredReticles;              //<i>Sequence of Stored Reticle
        private Object                                  siInfo;
    }

    @Data
    public static class ReticlePodInventoryReqResult {
        private ObjectIdentifier                        stockerID;         //<i>Stocker ID
        private ObjectIdentifier                        equipmentID;       //<i>Equipment ID
        private Object                                  siInfo;            //<i>Reserved for SI customization
    }

    @Data
    public static class ReticleInventoryReqResult {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class ReticlePodXferJobListInqResult {
        private boolean                                     detailFlag;             //<i>Detail Flag
        private List<Infos.ReticlePodXferJob>               reticlePodXferJobList;  //<i>Sequence of Reticle Pod Transfer Job
        private Object                                      siInfo;
    }

    @Data
    public static class WhatReticlePodForReticleXferInqResult {
        private ObjectIdentifier reticleID;
        private List<Infos.CandidateReticlePod> strCandidateReticlePods;
    }

    @Data
    public static class ReticleOperationExceptionResult {
        private List<ReticleOperationException> reticleOperationExceptionList;
    }

    @Data
    public static class ReticleOperationException {
        private ObjectIdentifier reticleID;
        private String errorMessage;
        private Integer omCode;
    }

    @Data
    public static class ReticleHoldListInqResult {
        private Page<Infos.ReticleHoldListAttributes> reticleHoldListAttributes;
    }

    @Data
    public static class SpcCheckResult {
        private String  errorCode;
        private String  errorMessage;
        private Boolean lotHoldAction;
        private String  lotID;
        private String  reasonCode;
        private String  amsID;              //now don't care
        private String  ocapNo;             //ocap number key
        private String  ocapID;             //ocap name
        private String  jobID;              //spc jobID
        private String  jobName;            //spc jobName
        private List<Infos.HoldEquipmentModel> holdEquipmentModel;
        private List<Infos.InhibitRecipeResultModel> inhibitRecipeResultModel;
    }

    @Data
    public static class CollectedDataActionByPostProcReqResult {
        private MoveOutReqResult                moveOutReqResult;
        private String                          relatedQueuekey;
    }

	@Data
    public static class EqpSearchForSettingEqpBoardResult{
		private ObjectIdentifier eqpId;
		private String bayId;
		private String bayGroup;
		private String eqpStatus;
		private String subStatus;
	}

	@Data
    public static class EqpAreaBoardListResult{
		private ObjectIdentifier eqpId;
		private String eqpStatus;
		private String subStatus;
		private String bayId;
		private String eqpMode;
		private String eqpCategory;
		private String capableRcpMode;
		private String changeUserId;
		private Timestamp changeTime;
		private String lotId;
		private String carrierId;
	}

	@Data
    public static class EqpAutoMoveInReserveReqResult implements PostProcessSource{
        private User user; //Request User ID
        private ObjectIdentifier equipmentID; //eqp ID
        private String portGroupID; //port Group ID
        private ObjectIdentifier controlJobID; //Control Job ID(Not Used)
        private List<Infos.StartCassette> startCassetteList; //Sequence of Start Carrier Information
        private String apcIFControlStatus;
        private Boolean cassetteChangeFlag; //QianDao MES-EAP Integration add

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

	@Data
	public static class DurableHoldListAttributesResult {
		private List<DurableHoldAttributeResult> durableHoldListAttributesResults;
	}

	@Data
	public static class DurableHoldAttributeResult {
		private String                          holdType;                   //<i>Hold Type
		private ObjectIdentifier                reasonCodeID;               //<i>Reason Code ID
		private String                          codeDescription;            //<i>Code Description
		private ObjectIdentifier                userID;                     //<i>User ID
		private String                          userName;                   //<i>User Name
		private String                          holdTimeStamp;              //<i>Hold TimeStamp
		private String                          responsibleOperationMark;   //<i>Responsible Operation Mark
																   //<c>SP_ResponsibleOperation_Current        "C"
																   //<c>SP_ResponsibleOperation_Previous       "P"
		private ObjectIdentifier                responsibleRouteID;         //<i>Responsible Route ID
		private String                          responsibleOperationNumber; //<i>Responsible Operation Number
		private String                          responsibleOperationName;   //<i>Responsible Operation Name
		private ObjectIdentifier                relatedDurableID;           //<i>Related Durable ID
		private String                          relatedDurableCategory;     //<i>Related Durable Category
		private String                          claimMemo;                  //<i>Claim Memo
	}


    @Data
    public static class FixtureStatusChangeRptResult{
        private ObjectIdentifier fixtureID;
        private Infos.FixtureBr fixtureBRInfo;
        private Infos.FixtureStatusInfo fixtureStatusInfo;
        private Infos.FixturePm fixturePMInfo;
    }

    @Data
    public static class FixtureXferStatusChangeRptResult{
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<Infos.XferFixture> strXferFixture;
    }

    @Data
    public static class FixtureIDListInqResult {
        private  List<ObjectIdentifier> objectIdentifiers;
    }

    @Data
    public static class FixtureStatusInqResult {
        private ObjectIdentifier fixtureID;
        private Infos.FixtureBr fixtureBRInfo;
        private Infos.FixtureStatusInfo fixtureStatusInfo;
        private Infos.FixturePm fixturePMInfo;
        private List<Infos.EntityInhibitAttributes> entityInhibitions;
    }

    @Data
    public static class FixtureStockerInfoInqResult{
        private ObjectIdentifier stockerID;
        private String description;
        private ObjectIdentifier workarea;
        private String stockerType;
        private List<Infos.InventoryFixtureInfo> strInventoryFixtureInfo;
    }

    @Data
    public static class FixtureGroupIDListInqResult{
        private List<ObjectIdentifier> objectIdentifiers;
    }

    @Data
    public static class PartialReworkReqResult {
        private ObjectIdentifier createdLotID;

    }

    @Data
    public static class NPWLotStratReq implements PostProcessSource {
        private ObjectIdentifier controlLotID;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return Collections.singletonList(this.controlLotID);
        }

    }

    @Data
    public static class EqpReserveForFlowBatchReqResult  implements PostProcessSource {
        private List<Infos.FlowBatchedLot> flowBatchedLots;

        @Override
        public List<ObjectIdentifier> lotIDs() {
            return this.flowBatchedLots.stream()
                    .map(Infos.FlowBatchedLot::getLotID)
                    .collect(Collectors.toList());
        }
    }
}


