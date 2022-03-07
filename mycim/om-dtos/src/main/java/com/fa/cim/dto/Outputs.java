package com.fa.cim.dto;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimObjectUtils;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/28 9:51
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Outputs {

    @Data
    public static class ObjAdvanceLockOut {
        private List<String> notFoundKeyList;
    }

    @Data
    public static class ObjBankLotPreparationOut {
        private ObjectIdentifier createdLotID;
    }

    @Data
    public static class ObjBondingGroupInfoByEqpGetDROut {
        private List<Infos.BondingGroupInfo> bondingGroupInfoList;
        private List<ObjectIdentifier> topLotIDSeq;
    }

    @Data
    public static class ObjBondingGroupInfoGetDROut {
        private Infos.BondingGroupInfo bondingGroupInfo;
        private List<ObjectIdentifier> bondingLotIDList;
    }

    @Data
    public static class ObjCassetteEquipmentIDGetOut {
        private ObjectIdentifier equipmentID;
    }


    @Data
    public static class ObjCassetteScrapWaferSelectOut {
        private List<Infos.LotWaferMap> lotWaferMapList;
    }

    @Data
    public static class ObjEquipmentBankIDGetOut {
        private ObjectIdentifier fillerDummyBankID;
        private ObjectIdentifier sideDummyBankID;
        private ObjectIdentifier monitorBankID;
    }

    @Data
    public static  class ObjCassetteReservedStateGetOut {
        private boolean transferReserved;
        private String reservedPerson;
    }

    @Data
    public static class ObjEquipmentStateConvertOut {
        private ObjectIdentifier convertedStatusCode;
        private Boolean stateConverted;
    }

    @Data
    public static class ObjEquipmentContainerInfoGetOut {
        private Infos.EqpContainerInfo strEqpContainerInfo;
        private Object reserve;
    }

    @Data
    public static class objCarrierEquipmentInfoGetOut
    {
        private ObjectIdentifier carrierID;
        private ObjectIdentifier equipmentID;
    }


    @Data
    public static class ObjEquipmentLotCheckFlowBatchConditionForOperationStartOut {
        private ObjectIdentifier flowBatchID;
    }

    @Data
    public static class ObjEquipmentMonitorSectionInfoGetForJobOut {
        private String operationLabel;
        private boolean exitFlag;
        private String monitorOperationPosObjRef;
        private ObjectIdentifier equipmentMonitorID;
        private ObjectIdentifier equipmentMonitorJobID;
    }


    @Data
    public static class ObjEquipmentPortGroupIDGetOut {
        private String portGroupId;
    }

    @Data
    public static class ObjPortResourceCurrentOperationModeGetOut {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier portID;
        private Infos.OperationMode operationMode;

        public void init() {
            this.equipmentID = new ObjectIdentifier();
            this.portID = new ObjectIdentifier();
            this.operationMode = new Infos.OperationMode();
        }
    }


    @Data
    public static class ObjEquipmentProcessBatchConditionGetOut {
         private long maxBatchSize;
         private long minBatchSize;
         private long minWaferSize;
         private boolean cassetteExchangeFlag;
         private boolean monitorCreationFlag;
    }


    @Data
    public static class ObjEquipmentRecipeGetListForRecipeBodyManagementOut {
        private List<Infos.RecipeBodyManagement> recipeBodyManagementList;
    }
    @Data
    public static class ObjEquipmentReticlePodPortInfoGetDROut {
        private List<Infos.ReticlePodPortInfo> reticlePodPortInfoList;
        private long reticleStoreMaxCount;
        private long reticleStoreLimitCount;
    }

    @Data
    public static class ObjPersonAllowProductListGetOut {
        private List<ObjectIdentifier> productIDList;
    }

    @Data
    public static class ObjPersonProductGroupListGetOut {
        private List<ObjectIdentifier> productGroupIDList;
    }

    @Data
    public static class ObjPersonProductListGetOut {
        private List<ObjectIdentifier> productIDList;
    }

    @Data
    public static class ObjPersonAllowMachineRecipeListGetOut {
        private List<ObjectIdentifier> machineRecipeIDList;
    }


    @Data
    public static class ObjMonitorGroupDeleteCompOut {
        private List<Infos.MonitoredCompLots> monitoredCompLotsList;
        private List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList;
    }

    @Data
    public static class ObjLotInPostProcessFlagOut {
        private Boolean inPostProcessFlagOfCassette;
        private Boolean inPostProcessFlagOfLot;
    }

    @Data
    public static class ObjLockModeOut{
        private ObjectIdentifier objectID;
        private String className;
        private Long lockMode;
        private Long requiredLockForMainObject;
    }


    @Data
    public static class ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut {
        private boolean multiChamberFlag;
        private boolean recipeDefinedFlag;
        private List<Infos.CandidateChamber> candidateChamberList;
    }

    @Data
    public static class ObjLotControlJobIDGetOut {
        private ObjectIdentifier controlJobID;
    }

    @Data
    public static class ObjLotCurrentRouteIDGetOut{
        private ObjectIdentifier currentRouteID;
    }

    @Data
    public static class ObjLotCurrentOperationInfoGetOut {
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;

        private ObjectIdentifier controlJobID;
        private String operationPass;
    }

    @Data
    public static class ObjUserDefinedAttributeInfoGetDROut {
        private List<Infos.UserDefinedData> strUserDefinedDataSeq;
    }

    @Data
    public static class ObjLotCurrentOperationInfoGetDROut {
        private ObjectIdentifier mainPDID;
        private ObjectIdentifier modulePDID;
        private ObjectIdentifier operationID;
        private Integer passCount;
        private String mainPFforMainPOS;
        private String opeNo;
        private String mainPOS;
        private String mainPFforModulePD;
        private String moduleNo;
        private String modulePF;
        private String moduleOpeNo;
        private String modulePOS;
        private ObjectIdentifier stageID;
    }
    @Data
    public static class ObjLotEffectiveFPCInfoGetOut {
        private Infos.FPCInfo fpcInfo;
        private boolean equipmentActionRequiredFlag;
        private boolean machineRecipeActionRequiredFlag;
        private boolean recipeParameterActionRequiredFlag;
        private boolean dcDefActionRequiredFlag;
        private boolean dcSpecActionRequiredFlag;
        private boolean reticleActionRequiredFlag;

        public ObjLotEffectiveFPCInfoGetOut init() {
            equipmentActionRequiredFlag = false;
            machineRecipeActionRequiredFlag = false;
            recipeParameterActionRequiredFlag = false;
            dcDefActionRequiredFlag = false;
            dcSpecActionRequiredFlag = false;
            reticleActionRequiredFlag = false;
            fpcInfo = new Infos.FPCInfo();
            return this;
        }
    }


    @Data
    public static class ObjLotFamilySplitNoAdjustOut {
        private long splitNumber;
        private Object reserve;
    }
    @Data
    public static class ObjLotGetSourceLotsOut{
        private ObjectIdentifier startBankID;
        private Page<Infos.SourceLot> sourceLotList;  //See OLSTQ005,0.04
        private Object reserve;
    }

    @Data
    public static class ObjLotInterFabTransferStateGetOut {
        private String interFabTransferState;
    }

    @Data
    public static class ObjLotMonitorRouteFlagGetOut{
        private boolean monitorRouteFlag;
    }

    @Data
    public static class ObjLotParameterForLotGenerationCheckOut {
        private boolean waferIDAssignRequiredFlag;
        private Object reserve;              //<i>Reserved for SI customization
    }

    @Data
    public static class ObjLotPreparationCancelInfoGetDROut {
        private Infos.PreparationCancelledLotInfo preparationCancelledLotInfo;    // lot preparation cacelled lot's info
        private List<Infos.NewVendorLotInfo> newVendorLotInfoList;                // new vendor lot information
        private List<Infos.PreparationCancelledWaferInfo> preparationCancelledWaferInfoList;   // preparation cancelled wafer information
    }

    @Data
    public static class ObjLotProcessJobExecFlagValidCheckOut {
        private List<Infos.StartCassette> startCassetteList;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ObjLotRecipeGetOut {
        private ObjectIdentifier logicalRecipeId;
        private ObjectIdentifier machineRecipeId;
    }

    @Data
    public static class ObjLotSTBCancelInfoOut {
        private Infos.STBCancelledLotInfo stbCancelledLotInfo;
        private List<Infos.NewPreparedLotInfo> newPreparedLotInfoList;
        private List<Infos.STBCancelWaferInfo> stbCancelWaferInfoList;
        private Object reserve;
    }

    @Data
    public static class ObjLotSTBCancelOut {
        private List<Infos.PreparedLotInfo> preparedLotInfoList;
        private Object reserve;
    }

    @Data
    public static class ObjLotSTBOut {
        private ObjectIdentifier createdLotID;
        private String productType;
        private String lotType;
        private String subLotType;
    }


    @Data
    public static class ObjLotWaferCreateOut {
        private ObjectIdentifier lotID;
        private ObjectIdentifier newWaferID;
        private Object reserve;              //<i>Reserved for SI customization
    }

    @Data
    public static class ObjLotWaferIDGenerateOut {
        private Infos.NewLotAttributes newLotAttributes;
        private Object reserve;              //<i>Reserved for SI customization
    }

    @Data
    public static class ObjLotWaferIDListGetDROut {
        private List<ObjectIdentifier> waferIDs;
    }

    @Data
    public static class ObjProcessDataCollectionDefinitionGetOut {
        private boolean dataCollectionFlag;
        private List<Infos.DataCollectionInfo> dataCollectionDefList;
    }

    @Data
    public static class ObjProcessDurableCheckConditionForOperationStartOut {
        private List<Infos.StartReticleInfo> startReticleList;
        private List<Infos.StartFixtureInfo> startFixtureList;
    }

    @Data
    public static class ObjProcessFlowBatchDefinitionGetDROut {
        private Infos.FlowBatchControl flowBatchControl;
        private Infos.FlowBatchSection flowBatchSection;
    }


    @Data
    public static class ObjProcessNextOperationInModuleGetDROut {
        private String modulePF;
        private String moduleOpeNo;
        private String modulePOS;
        private ObjectIdentifier operationID;
    }

    @Data
    public static class ObjProcessCheckForDynamicRouteOut {
        private String processDefinitionType;
        private Boolean dynamicRouteFlag;
        private Boolean activeVersionFlag;
    }

    @Data
    public static class ObjProcessGetReturnOperationOut {
        private String operationNumber;
        private String processDefinitionType;
    }

    @Data
    public static class ObjProcessResourceCurrentStateChangeOut {
        private ObjectIdentifier equipmentID;
        private List<Infos.EqpChamberState> eqpChamberStates;
    }

    @Data
    public static class ObjProcessResourceCurrentStateChangeByAutoOut {
        private ObjectIdentifier equipmentID;
        private List<Infos.ChamberStateHisByAuto> chamberStateHisByAuto;
    }

    @Data
    public static class ObjProcessResourceStateConvertOut {
        private ObjectIdentifier equipmentID;
        private List<Infos.EqpChamberStatus> eqpChamberStatusCheckResults;
    }

    @Data
    public static class ObjProcessStartReserveInformationGetOut {
        private List<Infos.StartCassette> startCassetteList;
    }

    @Data
    public static class ObjProductBOMInfoGetOut {
        private ObjectIdentifier bomID;
        private String bomName;
        private String bomDescription;
    }

    @Data
    public static class ObjProductRequestForVendorLotReleaseOut {
        private ObjectIdentifier createProductRequestID;
        private Object reserve;              //<i>Reserved for SI customizatio n
    }

    @Data
    public static class ObjProductRequestReleaseBySTBCancelOut {
        private ObjectIdentifier createdProductRequestID; // created product request ID
    }

    @Data
    public static class ObjProductRequestReleaseOut {
        private ObjectIdentifier createProductRequest;
        private Object reserve;              //<i>Reserved for SI customization
    }

    @Data
    public static class ObjReticleUsageLimitationCheckOut {
        private ObjectIdentifier reticleId;
        private boolean usageLimitOverFlag;
        private boolean runTimeOverFlag;
        private boolean startCountOverFlag;
        private boolean pmTimeOverFlag;        //D3100032
        private long runTime;
        private long maxRunTime;
        private long startCount;
        private long maxStartCount;
        private long passageTimeFromLastPM;
        private long intervalBetweenPM;
        private String messageText;
    }


    @Data
    public static class ObjStockerTypeGetDROut {
        private String stockerType;
        private boolean utsFlag;
        private long maxUtsCapacity;

    }

    @Data
    public static class ObjLotHoldReleaseOut {
        private List<Infos.HoldHistory> holdHistoryList;
        private List<Infos.LotHoldReq> holdList;
    }


    @Data
    public static class ObjProcessGetTargetOperationOut {
        private Infos.ProcessRef processRef;
    }


    @Data
    public static class ObjProcessGetOperationByProcessRefOut{
       private ObjectIdentifier routeID;
       private String operationNumber;
    }

    @Data
    public static class ObjLotFutureHoldRequestsDeleteEffectedByConditionOut {
        private ObjectIdentifier lotID;
        private List<Infos.LotHoldReq> strFutureHoldReleaseReqList;
    }

    @Data
    public static class ObjLotFutureHoldRequestsEffectByConditionOut {
        private ObjectIdentifier lotID;
        private List<Infos.LotHoldReq> strLotHoldReqList;
    }

    @Data
    public static class FutureHoldRequestsForPreviousPO {
        private ObjectIdentifier lotID;
        private List<Infos.LotHoldReq> strLotHoldReqList;
    }

    @Data
    public static class FutureHoldRequestsDeleteForPreviousPO {
        private ObjectIdentifier lotID;
        private List<Infos.LotHoldReq> strFutureHoldReleaseReqList;
    }

    @Data
    public static class ObjProcessLocateOut {
        private Inputs.OldCurrentPOData oldCurrentPOData;   //D4100020
        private Boolean autoBankInFlag;
    }

    /**
     * description:
     * objOperationStartLot_lotCount_GetByLoadPurposeType_out_struct
     *
     * @author PlayBoy
     * @date 2018/7/30
     */
    @Data
    public static class ObjOperationStartLotCountByLoadPurposeTypeOut {
        private List<ObjectIdentifier> startCassetteIDs;
        private List<ObjectIdentifier> emptyCassetteIDs;
        private List<ObjectIdentifier> processLotIDs;
        private ObjectIdentifier processMonitorLotID;
    }

    /**
     * description:<br/>
     * objControlJob_startReserveInformation_Get_out_struct
     *
     * @author PlayBoy
     * @date 2018/8/8
     */
    @Data
    public static class ObjControlJobStartReserveInformationOut {
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private List<Infos.StartCassette> startCassetteList;
    }

    /**
     * description:<br/>
     * objLot_futureHoldRequests_EffectForOpeStartCancel_out_struct<br/>
     *
     * @author PlayBoy
     * @date 2018/8/10
     */
    @Data
    public static class ObjLotFutureHoldRequestsEffectForOpeStartCancelOut {
        private ObjectIdentifier lotID;
        private List<Infos.LotHoldReq> lotHoldReqList;
        private List<Infos.LotHoldReq> futureHoldReleaseReqList;
    }

    @Data
    public static class ObjProcessStartReserveInformationGetByCassetteOut {
        private List<Infos.StartCassette> startCassetteList;
    }

    @Data
    public static class ObjProcessStartReserveInformationGetBaseInfoForClientOut {
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> startCassetteList;
    }



    @Data
    public static class ObjLotCheckConditionForPOByControlJobOut{
        private Boolean currentPOFlag;
    }

    @Data
    public static class ObjDurableRecipeGetOut {
        private ObjectIdentifier logicalRecipeID;
        private ObjectIdentifier machineRecipeID;
    }

    @Data
    public static class ObjLotFutureHoldEffectedProcessConversionOut{
        private ObjectIdentifier lotId;
        private List<Infos.LotHoldReq> lotHoldReqList;
    }

    @Data
    public static class ObjEquipmentCurrentStateChangeOut {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentStatusCode;         // Changed new status (= input) if no-error
        private ObjectIdentifier E10Status;                   //E10 of changed new status if no-error
        private ObjectIdentifier actualStatus;                //Actual status at this time if no-error
        private ObjectIdentifier actualE10Status;             //Actual E10 status at this time if no-error
        private String operationMode;               //Operation Mode when this StatusClaim
        private ObjectIdentifier previousStatus;              //Status which was overwritten
        private ObjectIdentifier previousE10Status;           //E10 of previous status
        private ObjectIdentifier previousActualStatus;        //Actual status at this time if no-error
        private ObjectIdentifier previousActualE10Status;     //Actual E10 status at this time if no-error
        private String previousOpeMode;             // OperationMode when this StatusClaim
        private String prevStateStartTime;          // Overwritten status started time
    }

    @Data
    public static class ObjEquipmentCurrentStateChangeByAutoOut {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentStatusCode;         // Changed new status (= input) if no-error
        private ObjectIdentifier E10Status;                   //E10 of changed new status if no-error
        private ObjectIdentifier actualStatus;                //Actual status at this time if no-error
        private ObjectIdentifier actualE10Status;             //Actual E10 status at this time if no-error
        private String operationMode;               //Operation Mode when this StatusClaim
        private ObjectIdentifier previousStatus;              //Status which was overwritten
        private ObjectIdentifier previousE10Status;           //E10 of previous status
        private ObjectIdentifier previousActualStatus;        //Actual status at this time if no-error
        private ObjectIdentifier previousActualE10Status;     //Actual E10 status at this time if no-error
        private String previousOpeMode;             // OperationMode when this StatusClaim
        private String prevStateStartTime;          // Overwritten status started time
        private Boolean genHistory;
    }

    @Data
    public static class ObjEquipmentUsageLimitationCheckOut {
        private ObjectIdentifier equipmentId;
        private boolean usageLimitOverFlag;
        private boolean runWaferOverFlag;
        private boolean runTimeOverFlag;
        private boolean startCountOverFlag;
        private boolean intervalTimeOverFlag;
        private long runWafer;
        private long maxRunWafer;
        private long runTime;
        private long maxRunTime;
        private long startCount;
        private long maxStartCount;
        private long elapsedTimeFromPM;
        private long intervalTimeBetweenPM;
        private String messageText;
    }

    @Data
    public static class ObjCassetteUsageLimitationCheckOut{
        private ObjectIdentifier cassetteID;
        private boolean usageLimitOverFlag;
        private boolean runTimeOverFlag;
        private boolean startCountOverFlag;
        private boolean pmTimeOverFlag;
        private long runTime;
        private long maxRunTime;
        private long startCount;
        private long maxStartCount;
        private long passageTimeFromLastPM;
        private long intervalBetweenPM;
        private String  messageText;
    }

    @Data
    public static class ObjLotCheckConditionForAutoBankInOut{
        private boolean autoBankInFlag;
    }



    @Data
    public static class ObjProcessMoveOut {
        private Boolean autoBankInFlag;
    }

    @Data
    public static class ObjLotCurrentOperationNumberGetOut {
        private String currentOperationNumber;
    }

    @Data
    public static class ObjLotOriginalRouteListGetOut {
        private List<ObjectIdentifier> originalRouteID;
        private List<String> returnOperationNumber;
        private String siInfo;
    }


    @Data
    public static class ObjProcessCompareCurrentOut{
        private ObjectIdentifier currentRouteID;
        private String currentOperationNumber;
    }

    @Data
    public static class ControlJobAttributeInfo {
        private ObjectIdentifier machineID;                   //<i>Machine ID
        private String portGroup;                   //<i>port Group
        private List<Infos.ControlJobCassette> strControlJobCassetteSeq;    //<i>Sequence of Control Job cassette
        private ObjectIdentifier ownerID;                     //<i>Owner ID
        private String controlJobStatus;            //<i>Control Job Status
        private String lastClaimedUser;             //<i>Last Claimed User
        private Timestamp lastClaimedTimeStamp;        //<i>Last Claimed TimeStamp
    }


    @Data
    public static class ObjLotControlUseInfoChangeOut {
        private String controlUseState;
        private Integer usageCount;
        //private lot lot;
    }

    @Data
    public static class ObjTransAccessControlCheckOut {
        private Boolean productState;
        private Boolean machineRecipeState;
        private Boolean queryState;

    }

    @Data
    public static class ObjWaferLotCassetteGetOut {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
    }


    /**
     * description:
     * <p>objSorter_waferTransferInfo_Restructure_out_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/8 15:08:35
     */
    @Data
    public static class ObjSorterWaferTransferInfoRestructureOut {
        private List<String> lotInventoryStateList;
        private List<ObjectIdentifier> cassetteIDList;
        private List<Infos.PLot> lotList;
        private Object siInfo;
    }

    @Data
    public static class LotSubLotTypeGetDetailInfoDR {
        private String        lotType;
        private String        lotTypeDescription;
        private String        subLotType;
        private String        description;
        private String        leadingCharacter;
        private Integer          duration;
    }

    @Data
    public static class ObjLotAllStateGetOut {
        private String lotState;
        private String productionState;
        private String holdState;
        private String finishedState;
        private String processState;
        private String inventoryState;
    }

    /**
     * description:
     * <p>objProcessOperation_DataCondition_GetDR_out_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/16 10:31:14
     */
    @Data
    public static class ObjProcessOperationDataConditionGetDrOut {
        private Long count;
        private List<Infos.DataCollectionInfo> expandDerivedDCDefList;
    }

    @Data
    public static class ObjEquipmentPortGroupInfoGetOut {
        private String portGroupID;
        private List<Infos.EqpPortAttributes> eqpPortAttributes;
    }
    @Data
    public static class ObjLotAllStateCheckSame {
        private String lotState;
        private String lotProductionState;
        private String lotHoldState;
        private String lotFinishedState;
        private String lotProcessState;
        private String lotInventoryState;
    }
    /**
     * description:
     * <p>objDataValue_CheckValidityForSpecCheckDR_out_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/19 11:18:32
     */
    @Data
    public static class ObjDataValueCheckValidityForSpecCheckDrOut {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
    }



    @Data
    public static class ObjCassetteStatusOut {
        private Long castUsedCapacity;
        private String castCategory;
        private String durableState;
    }

    @Data
    public static class ObjRawEquipmentStateTranslateOut {
        private ObjectIdentifier      equipmentStatusCode;  // Translated status
        private String                equipmentStatusName;  // Translated status name  2.01
        private ObjectIdentifier      E10Status;            // E10 status of the translated status  2.01
        private boolean               stateTranslated;      // True; translated, Flase; not done
    }

    @Data
    public static class CassetteInformationOut
    {
      private String cassetteID;
      private String cassetteObj;
      private String transState;
      private String equipmentID;
      private String equipmentObj;
      private String cassetteCategory;
    }

    @Data
    public static class ObjMonitorLotSTBInfoGetOut {
        private ObjectIdentifier  monitorLotProductID;
        private String lotType;
        private String subLotType;
    }

    @Data
    public static class ObjProcessLagTimeGetOut {
        private Double expriedTimeDuration;
        private Timestamp processLagTimeStamp;
    }

    @Data
    public static class CassetteReservationInfoGetDROut {
        private ObjectIdentifier reserveUserID;
        private ObjectIdentifier controlJobID;
        private String NPWLoadPurposeType;
        private String reserve;
    }

    @Data
    public static class ObjDurableCurrentOperationInfoGetOut {
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private Object siInfo;
    }

    /**
     * description: objQtime_originalInformation_Get_out_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @author Sun
     * @date 11/1/2018 2:42 PM
     * @return
     */
    @Data
    public static class QtimeOriginalInformationOut {
        private ObjectIdentifier triggerRouteID;
        private String triggerOperationNumber;
        private ObjectIdentifier targetRouteID;
        private String targetOperationNumber;
    }

    /**
     * description:
     * <p>objStartLot_actionList_EffectSpecCheck_out__101_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/5 16:54:58
     */
    @Data
    public static class ObjStartLotActionListEffectSpecCheckOut {
        private List<Infos.MessageAttributes> messageList;
        private List<Infos.EntityInhibitDetailAttributes> entityInhibitions;
        private List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList;
        private List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitionsWithFabInfoList;
        private List<Results.DCActionLotResult> dcActionLotResultList;
    }

    /**
     * description:
     * <p>objLot_holdRecord_EffectSpecCheckResult_out__101_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/6 11:11:53
     */
    @Data
    public static class ObjLotHoldRecordEffectSpecCheckResultOut {
        private List<Infos.LotHoldEffectList> lotHoldEffectList;
        private List<Infos.LotHoldEffectList> futureHoldEffectList;
        private List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList;
        private List<Results.DCActionLotResult> dcActionLotResultList;
    }

    @Data
    public static class QrestLotInfo {
        private ObjectIdentifier lotID;                                 //<i>lot ID
        private ObjectIdentifier cassetteID;                            //<i>Carrier ID
        private String lotStatus;                             //<i>lot Status
        private ObjectIdentifier stockerID;                             //<i>stocker ID
        private String transferStatus;                        //<i>Transfer Status
        private ObjectIdentifier routeID;                               //<i>Route ID
        private ObjectIdentifier operationID;                           //<i>Operation ID
        private String operationNumber;                       //<i>Operation Number
        private ObjectIdentifier equipmentID;                           //<i>eqp ID
        private List<Infos.QtimeInfo> strQtimeInfo;                          //<i>Sequence of Q Time Information
        private String siInfo;                                //<i>Reserved for SI customization
    }


    @Data
    public static class QrestTimeAction {
        private  ObjectIdentifier                     qrestrictionTriggerRouteID;            //<i>Q Restriction Trigger Route ID
        private  String                               qrestrictionTriggerOperationNumber;    //<i>Q Restriction Trigger Operation Number
        private  String                               qrestrictionTriggerBranchInfo;         //<i>Q Restriction Trigger Branch Information
        private  String                               qrestrictionTriggerReturnInfo;         //<i>Q Restriction Trigger Return Information
        private  ObjectIdentifier                     qrestrictionTargetRouteID;             //<i>Q Restriction Target Route ID
        private  String                               qrestrictionTargetOperationNumber;     //<i>Q Restriction Target Operation Number
        private  String                               qrestrictionTargetBranchInfo;          //<i>Q Restriction Target Branch Information
        private  String                               qrestrictionTargetReturnInfo;          //<i>Q Restriction Target Return Information
        private  String                               qrestrictionTargetTimeStamp;           //<i>Q Restriction Target Time Stamp
        private  String                               originalQTime;                         //<i>Original qtime
        private  Integer                                 expiredTimeDuration;                   //<i>Expired Time Duration    //DSN000105390
        private  String                               qrestrictionAction;                    //<i>Q Restriction Action
        private  ObjectIdentifier                     reasonCodeID;                          //<i>Reason CimCode ID
        private  ObjectIdentifier                     actionRouteID;                         //<i>Action Route ID
        private  String                               actionOperationNumber;                 //<i>Action Operation Number
        private  String                               futureHoldTiming;                      //<i>Future Hold Timing (Pre/Post)
        private  ObjectIdentifier                     reworkRouteID;                         //<i>Rework Route ID
        private  ObjectIdentifier                     messageID;                             //<i>Message ID
        private  String                               customField;                           //<i>Custom Field
        private  Boolean                              actionDoneOnlyFlag;                    //<i>ActionDoneOnlyFlag
        private  String                                  siInfo;
    }

    @Data
    public static class ObjLotHoldRecordEffectSPCCheckResultOut {
        private List<Infos.LotHoldEffectList> lotHoldEffectList;
        private List<Infos.LotHoldEffectList> futureHoldEffectList;
        private List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList;
        private List<Results.DCActionLotResult> dcActionLotResultList;
    }

    @Data
    public static class ObjStartLotActionListEffectSPCCheckOut {
        private List<Infos.MessageAttributes> strMessageList;
        private List<Infos.EntityInhibitDetailAttributes> strEntityInhibitions;
        private List<Infos.BankMove> bankMoveList;
        private List<Infos.MailSend> mailSendList;
        private List<Infos.ReworkBranch> reworkBranchList;
        private List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoSequence;
        private List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitionsWithFabInfo;
        private List<Infos.MailSendWithFabInfo> mailSendListWithFabInfo;
        private List<Results.DCActionLotResult> dcActionLotResult;
    }

    @Data
    public static class ObjSPCMgrSendSPCCheckReqOut {
        private Results.SPCCheckReqResult spcCheckReqResult;
        private List<Infos.SpcIFParm> spcIFParmList;
    }

    @Data
    public static class ObjEquipmentPortCombinationCheck {
        private List<String> equipmentPortGroup;
        private List<String> inputPortGroup;
    }

    @Data
    public static class ObjOperationPdTypeGetOut {
        private String pdType;
        private String operationName;
    }

    @Data
    public static class ObjReticlePodReticleListGetDROut{
        private List<Long> slotPosition;
        private List<ObjectIdentifier> reticleID;
    }

    @Data
    public static class ObjReticlePodWildCardStringCheckOut{
        private boolean checkFlag;
    }

    @Data
    public static class ObjReticleDetailInfoGetDROut{
        Results.ReticleDetailInfoInqResult strReticleDetailInfoInqResult;
    }
	
	@Data
    public static class CassetteLotReserveOut {
        private List<Infos.ReserveLot> reserveLot;
        private String claimMemo;
        private String reserve;
    }

    @Data
    public static class EquipmentTargetPortPickupOut {
        private Infos.EqpTargetPortInfo eqpTargetPortInfo;
        private String targetPortType;
        private Boolean whatsNextRequireFlag;
        private Boolean emptyCassetteRequireFlag;
    }

    @Data
    public static class ObjReticlePodFillInTxPDQ013DROut{
        private ObjectIdentifier reticlePodID;
        private Infos.ReticlePodBrInfo reticlePodBRInfo;
        private Infos.ReticlePodStatusInfo reticlePodStatusInfo;
        private Infos.ReticlePodPmInfo reticlePodPMInfo;
        private Infos.DurableLocationInfo reticlePodLocationInfo;
        private Infos.DurableOperationInfo strDurableOperationInfo;
        private Infos.DurableWipOperationInfo strDurableWipOperationInfo;

    }

    @Data
    public static class ObjectLockModeGetOut {
        private ObjectIdentifier objectID;
        private String className;
        private long lockMode;
        private long requiredLockForMainObject;
    }

    @Data
    public static class ObjDurableControlJobStatusGetOut{
        private String durableControlJobStatus;
        private ObjectIdentifier lastClaimedUserID;
        private String lastClaimedTimeStamp;
    }

    @Data
    public static class ObjReticlePodPortResourceCurrentAccessModeGetOut{
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier reticlePodPortID;
        private String accessMode;
        private Object siInfo;
    }

	@Data
    public static class WhereNextTransferEqpOut {
        private ObjectIdentifier equipmentID;
        private String portGroup;
        private List<Infos.StartCassette> startCassetteList;
        private String reserve;
    }

    @Data
    public static class DurableWhereNextTransferEqpOut {
        private ObjectIdentifier equipmentID;
        private String portGroup;
        private String durableCategory;
        private List<Infos.StartDurable>   strStartDurables;                      //<i>Sequence of Start Durable
    }

    @Data
    public static class CassetteDBInfoGetDROut {
        private Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult;
        private String reserve;
    }

    @Data
    public static class CassetteZoneTypeGetOut {
        private String zoneType;
        private String priority;
        private String reserve;
    }

    @Data
    public static class ProcessFlowInnerJoinWithProcessFlowPosListOut {
        private String processFlowObj;
        private Integer processFlowPosListDSeqNo;
        private String processFlowPosListObj;
    }

    @Data
    public static class ProcessFlowInnterJoinWithProcessDefinitionOut {
        private String mainProcessDefinitionID;
        private Integer dSeqNo;
        private String StageID;
        private String StageObj;
    }

    @Data
    public static class objProcessPreviousProcessReferenceOut {
        private String previousOperationNumber;
        private Infos.ProcessRef previousProcessRef;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 11/16/2018 3:35 PM
     */
    @Data
    public static class ObjectIDList {
        private  List<Infos.ObjectIDInformation> objectIDInformationList;
    }

	@Data
    public static class LotQTimeGetDROut {
        private ObjectIdentifier lotID;
        private List<Infos.QTimeInformation> lotQTimeInfo;
        private String reserved;
    }

    @Data
    public static class ObjDataCollectionItemFillInEDCDataItemListByKeyInqOut {
        private List<Infos.DCDefDataItem> dcDefDataItems;
        private List<Infos.DCSpecDataItem> dcSpecDataItems;
        private Infos.OperationDataCollectionSetting operationDataCollectionSetting;
    }

    @Data
    public static class ObjLotChangeScheduleOut {
        private Boolean opehsAddFlag;
        private Boolean opehsMoveFlag;
        private String oldCurrentPOS;         //PosProcessOperationSpecification 0.04  //D4100020 Delete
        private Inputs.OldCurrentPOData oldCurrentPOData;                                           //D4100020 Add
        private ObjectIdentifier previousProductID;     // DCR 9800140 Previous productspec    1.01(His)
    }

    @Data
    public static class ObjCassetteLotListGetWithPriorityOrderOut {
        private Integer waitingLotCount;
        private Integer holdLotCount;
        private Integer bankInLotCount;
        private List<Infos.LotStatusInfo> LotStatusInfos;
    }

    @Data
    public static class ObjQtimeAllClearByRouteChangeOut {
        private List<Infos.LotHoldReq> strLotHoldReleaseList;
        private List<Infos.LotHoldReq> strFutureHoldCancelList;
        private List<Infos.FutureReworkInfo> strFutureReworkCancelList;
    }

    @Data
    public static class ObjProcessGetFlowBatchDefinitionOut {
        private String name;
        private long size;
        private long minimumSize;
        private long minWaferCount;
        private boolean targetOperation;
    }

    @Data
    public static class ObjEquipmentReserveFlowBatchIDGetOut {
        private List<ObjectIdentifier> flowBatchIDs;
        private long flowBatchMaxCount;
    }

    @Data
    public static class ObjFlowBatchMakeOut {
        private ObjectIdentifier batchID;
        private List<Infos.BatchedLot> strBatchedLot;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 12/10/2018 4:20 PM
     */
    @Data
    public static class  ObjControlJobStatusGetOut {
        private String controlJobStatus;
        private ObjectIdentifier lastClaimedUserID;
        private String lastClaimedTimeStamp;
    }

    @Data
    public static class ObjProcessFutureQrestTimeInfoGetDROut {
        private List<Infos.FutureQtimeInfo> futureQtimeInfoList;
    }

    @Data
    public static class lotFutureReworkListGetDROut {
        private List<Infos.FutureReworkInfo> futureReworkDetailInfoList;
    }

    @Data
    public static class ObjLotMainRouteIDGetOut {
        private ObjectIdentifier mainRouteID;
    }

    @Data
    public static class ObjFlowBatchLostLotsListGetDRout {
        private List<Infos.FlowBatchLostLotInfo> flowBatchedCassetteInfoList;
    }

    @Data
    public static class ObjLotQtimeInfoGetForClearOut {
        private List<String> qTimeClearList;
        private List<Infos.LotHoldReq> strLotHoldReleaseList;
        private List<Infos.LotHoldReq> strFutureHoldCancelList;
        private List<Infos.FutureReworkInfo> strFutureReworkCancelList;
    }

    @Data
    public static class ObjQtimeOriginalInformationGetOut {
        private ObjectIdentifier triggerRouteID;
        private String triggerOperationNumber;
        private ObjectIdentifier targetRouteID;
        private String targetOperationNumber;
    }

    @Data
    public static class ObjFlowBatchInformationGetOut {
        private ObjectIdentifier          reservedEquipmentID;
        private Long                      maxCountForFlowBatch;
        private List<Infos.FlowBatchInfo> flowBatchInfoList;
    }

    @Data
    public static class ObjFlowBatchInfoSortByCassetteOut {
        private List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassetteList;
    }

    @Data
    public static class ObjStartCassetteProcessJobExecFlagSetOut {
        private List<Infos.StartCassette> startCassettes;
        private List<Infos.ObjSamplingMessageAttribute> samplingMessage;
    }


    @Data
    public static class QtimeClearByOpeStartCancelOut {
        List<Infos.LotHoldReq> futureHoldCancelList;
        List<Infos.FutureReworkInfo> futureReworkCancelList;
    }

    @Data
    public static class ObjQtimeSetClearByOperationCompOut {
        private List<Infos.LotHoldReq> strLotHoldReleaseList;
        private List<Infos.LotHoldReq> strFutureHoldCancelList;
        private List<Infos.FutureReworkInfo> strFutureReworkCancelList;
    }

    @Data
    public static class ObjSchdlChangeReservationCheckForActionDROut {
        private boolean existFlag;
        private Infos.SchdlChangeReservation strSchdlChangeReservation;
    }
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Lin
     * @date 2019/1/3 16:45
     * @param  * @param null -
     * @return
     */
    @Data
    public static class ObjEquipmentOperationModeCombinationCheck {
        List<String> portGroup;
        List<String> modeChangeType;
    }

    @Data
    public static class ObjectClassIDInfoGetDROut {
        private String className;
        private List<Infos.HashedInfo> strHashedInfoSeq;
    }

    @Data
    public static class MachineRecipe {
        private ObjectIdentifier  machineRecipeID;   //<i>Machine Recipe ID
        private boolean           whiteDefFlag;      //<i>White Definition Flag
        private String            description;       //<i>Description
        private String            fpcCategory;       //<i>FPC Category
        private String               siInfo;            //<i>Reserved for SI customization
    }

    @Data
    public static class ObjFPCCheckConditionForUpdateOut {
        private Boolean holdFlag;
        private List<ObjectIdentifier> strLotFamilyCurrentStatusList;
        private List<ObjectIdentifier> heldLotIDs;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/26 11:09
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public  static class ObjProcessBondingFlowDefinitionGetDROut {
        private ObjectIdentifier            operationID;
        private String                      operationNumber;
        private Infos.FlowSectionControl    flowSectionControl;
        private String                      siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/27                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/27 10:07
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static  class ObjInterFabXferPlanListGetDROut {
        private String                              currentFabID;
        private List<Infos.InterFabLotXferPlanInfo>  strInterFabLotXferPlanInfoSeq;
        private String                               siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/27                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/27 14:36
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjLotWafersStatusGetDROut {
        private List<Infos.WaferListInLotFamilyInfo> strWaferListInLotFamilyInfoSeq;
        private String                               siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 15:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    /*@Data
    public static class ObjFpcCheckConditionForUpdateOut {
        private boolean                           holdFlag;
        private List<Infos.LotFamilyCurrentStatus> strLotFamilyCurrentStatusList;
        private List<ObjectIdentifier>             heldLotIDs;
        private String                             siInfo;
    }*/


    @Data
    public static class SendCJPJOnlineInfoInqOut extends TCSOut{
        private Results.CJPJOnlineInfoInqResult strCJPJOnlineInfoInqResult;
    }

    @Data
    public static class SendMoveInReserveReqOut extends TCSOut {
        private Results.MoveInReserveReqResult moveInReserveReqResult;
    }

    @Data
    public static class SendMoveInReqOut extends TCSOut{
        private Results.MoveInReqResult strMoveInReqResult;
    }

    @Data
    public static class SendCJPJProgressOut extends TCSOut{
        private Results.CJPJProgressInfoInqResult cjpjProgressInqResult;
    }

    @Data
    public static class SendMoveOutReqOut extends TCSOut{
        private Results.MoveOutReqResult moveOutReqResult;
    }

    @Data
    public static class SendMoveOutForIBReqOut extends TCSOut {
        private Results.MoveOutReqResult moveOutReqResult;
    }

    @Data
    public static class SendEqpEAPInfoInqOut extends TCSOut {
        private Results.EqpEAPInfoInqResult eqpEapInfoInqResult;
    }

    @Data
    public static class SendMoveInCancelReqOut extends TCSOut{
        private Results.MoveInCancelReqResult strMoveInCancelReqResult;
    }

    @Data
    public static class SendStockerDetailInfoInqOut{
        private Results.StockerDetailInfoInqResult strStockerDetailInfoInqResult;
        private Object siInfo;
    }

    @Data
    public static class SendMoveInReserveCancelReqOut extends TCSOut{
        private Results.MoveInReserveCancelReqResult strMoveInReserveCancelReqResult;
    }

    @Data
    public static class CassetteDestinationInfoGetOut {
    	private Results.WhereNextStockerInqResult destinationOrder;
    	private Object siInfo;
    }

    @Data
    public static class CassetteDestinationInfoGetForSLMOut {
        private Results.WhereNextStockerInqResult destinationOrder;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 14:01
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjExperimentalLotInfoGetOut {
        private Infos.ExperimentalLotInfo strExperimentalLotInfo;
        private String siInfo;
    }

    /**
     * description:
     * 2020/09/01  Add parameter actionSeparateHold and actionCombineHold support for Auto separate and combine
     *
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 15:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjExperimentalLotActualInfoCreateOut {
        private ObjectIdentifier lotFamilyId;
        private ObjectIdentifier splitRouteId;
        private String splitOperationNumber;
        private ObjectIdentifier originalRouteId;
        private String originalOperationNumber;
        private boolean actionEmail;
        private boolean actionHold;
        private Boolean actionSeparateHold;
        private Boolean actionCombineHold;
        private String testMemo;
        private List<Infos.ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq;
        private Infos.ExperimentalLotDetailResultInfo strExperimentalLotDetailResultInfo;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 10:47
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjProcessOperationSequenceGetDROut {
        private long OperationSequenceNumber;
        String siInfo;
    }

    @Data
    public static class PostProcessQueueMakeOut {
        private String dKey;
        private String keyTimeStamp;
        private List<Infos.PostProcessActionInfo> strActionInfoSeq;
    }

    @Data
    public static class ObjMachineTypeGetOut {
        private boolean bStorageMachineFlag;
        private ObjectIdentifier equipmentID;
        private String equipmentType;
        private ObjectIdentifier stockerID;
        private String stockerType;
        private ObjectIdentifier areaID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/13                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/13 17:11
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjPostProcessQueListDROut {
        private List<Infos.PostProcessActionInfo> strActionInfoSeq;
        private Object siInfo;
    }

    @Data
    public static class ObjPostProcessPageQueListDROut {
        private Page<Infos.PostProcessActionInfo> strActionInfoSeq;
        private Object siInfo;
    }

    @Data
    public static class ObjPostProcessQueueParallelExecCheckOut {
        private boolean parallelExecFlag;
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
     * @date: 2019/6/17 15:27
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class SendTransportJobInqOut {
        private Results.TransportJobInqResult strTransportJobInqResult;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 11:11
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class SendTransportJobCancelReqOut {
        private Results.TransportJobCancelReqResult strTransportJobCancelReqResult;
        private Object siInfo;
    }
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/21                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/21 9:53
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class SendEAPRecoveryReqOut extends TCSOut {
        private Results.EAPRecoveryReqResult strEAPRecoveryReqResult;
        private Object siInfo;
    }

    @Data
    public static class ObjLotEquipmentOrderGetByLotStatusOut {
        private List<Infos.WhereNextEqpStatus> whereNextEqpStatusList;
        private boolean availableEqpExistFlag;
        private Object siInfo;
    }

    @Data
    public static class ObjLotQueuedMachinesGetByOperationOrderOut {
        private List<ObjectIdentifier> queuedMachineList;
        private Object siInfo;
    }

    @Data
    public static class ObjLotQueuedMachinesGetDROut{
        private List<ObjectIdentifier> queuedMachineList;
        private Object siInfo;
    }

    @Data
    public static class ObjEquipmentPriorityOrderGetByLotAvailabilityOut {
        private List<Infos.WhereNextEqpStatus> whereNextEqpStatuseList;
        private Boolean availableEqpExistFlag;
        private Object siInfo;
    }


    @Data
    public static class ObjEquipmentAllStockerGetByUTSPriorityDROut{
        private List<Infos.EqpStockerStatus> stockers;
        private List<Infos.EqpStockerStatus> UTSstockers;
    }

    @Data
    public static class ObjStockerUTSVacantSpaceCheckDROut {
        private Long maxCapacity;
        private Long vacantSpace;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/15 22:25
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjLotPreviousOperationInfoGetOut {
        private ObjectIdentifier    routeID;
        private ObjectIdentifier    operationID;
        private String              operationNumber;
        private ObjectIdentifier    controlJobID;
        private String              operationPass;
        private Object              siInfo;
    }
/**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/3 15:47
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjMultiCarrierXferFillInOTMSW005InParmOut {
        private List<Infos.CarrierXferReq> strCarrierXferReq;
        private Object siInfo;
    }

    @Data
    public static class ObjMultiDurableXferFillInOTMSW005InParmOut {
        private List<Infos.DurableXferReq> strDurableXferReq;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/4 18:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjCassetteDeliveryRTDInterfaceReqOut {
        private Results.WhereNextStockerInqResult strWhereNextStockerInqResult;
        private Results.WhatNextLotListResult    strWhatNextInqResult;
        private Object siInfo;
    }

    @Data
    public static class ObjDurableDeliveryRTDInterfaceReqOut {
        private Results.DurableWhereNextStockerInqResult strWhereNextStockerInqResult;
        private Results.WhatNextDurableListInqResult    strWhatNextInqResult;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/5 10:29
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjSingleCarrierXferFillInOTMSW006InParmOut {
        private Boolean rerouteFlag;
        private Infos.CarrierXferReq strCarrierXferReq;
        private Object siInfo;
    }

    @Data
    public static class ObjSingleDurableXferFillInOTMSW006InParmOut {
        private Boolean rerouteFlag;
        private Infos.DurableXferReq strDurableXferReq;
        private Object siInfo;
    }



    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/9 14:08
     * @copyright: 2019 FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjCassetteListEmptyAvailablePickUpOut {
        private List<Infos.FoundCassette> strFoundCassette;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/9 14:30
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjFlowBatchCheckConditionForCassetteDeliveryOut {
        private ObjectIdentifier flowBatchID;
        private List<Infos.ContainedLotsInFlowBatch> strContainedLotsInFlowBatch;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/9 17:01
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjLotProductIDGetOut {
        private ObjectIdentifier productID;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/11 16:29
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjCassetteDeliverySearchEmptyCassetteAssignPortOut {
        private Infos.PortID     strFoundPort;
        private ObjectIdentifier foundEmptyCassetteID;
        private Object           siInfo;
    }

    @Data
    public static class RecipeParameterCheckConditionForAdjustOut {
        private ObjectIdentifier    equipmentID;
        private ObjectIdentifier    controlJobID;
        private List<Infos.ProcessRecipeParameter> strProcessRecipeParameterSeq;
        private List<Infos.ProcessRecipeParameter> preProcessRecipeParameterSeq;
    }

    @Data
    public static class SendRecipeDirectoryInqOut extends TCSOut {
        private Results.RecipeDirectoryInqResult recipeDirectoryInqResult;
    }

    @Data
    public static class TCSOut {}

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/24 16:45
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjRecipeParameterCheckConditionForStoreOut {
        private ObjectIdentifier                     controlJobID;
        private List<Infos.LotStartRecipeParameter>  strLotStartRecipeParameterSeq;
        private Object                               siInfo;
    }

    @Data
    public static class ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout {
        private ObjectIdentifier branchRouteID;
        private String branchOperationNumber;
        private ObjectIdentifier returnRouteID;
        private String returnOperationNumber;
        private String reworkOutKey;
    }

    @Data
    public static class SorterSorterJobStatusGetDROut {
        private ObjectIdentifier                    sorterJobID ;
        private ObjectIdentifier                    sorterComponentJobID;
        private Object                               siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:11
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjOwnerChangeDefinitionGetDROut {
        private List<Infos.OwnerChangeDefinition>           strOwnerChangeDefinitionSeq;           //<i>Sequence of Owner Change Definition
        private List<Infos.OwnerChangeDefObjDefinition>     strOwnerChangeDefObjDefinitionSeq;     //<i>Sequence of DefObj Definition
        private List<String>                                allClassNames;                         //<i>Class Name
        private List<String>                                allTableNames;                         //<i>Table Name
        private List<String>                                allTableAndColumNames;                 //<i>All Table Name and Column Names
        private List<String>                                allClassAndTableNames;                 //<i>All Class Name and Table Names
        private Object                                      siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/6                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/6 15:43
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ObjSorterComponentJobInfoGetByComponentJobIDDROut {
        private Infos.SorterComponentJobListAttributes   strSorterComponentJobListAttributes;
        private ObjectIdentifier                         sorterJobID;
        private Object                                   siInfo;
    }

    @Data
    public static class ObjLotPreviousOperationDataCollectionInformationGetOut {
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> strStartCassette;
    }

    @Data
    public static class ObjLotCurrentOperationDataCollectionInformationGetOut {
        private ObjectIdentifier equipmentID;
        private List<Infos.StartCassette> strStartCassette;
    }

    @Data
    public static class ObjObjectValidSorterJobGetOut {
        private List<Infos.SortJobListAttributes> strValidSorterJob;
        private List<Infos.SortJobListAttributes> strOtherSorterJob;
    }

    @Data
    public static class ObjCassetteTransferInfoGetDROut{
        private String transferStatus;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
    }

    @Data
    public static class ObjProductRequestGetDetailOut{
        private Infos.ProdReqInq prodReqInq;
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
     * @date 2019/4/29 16:46
     */
    @Data
    public static class RecipeParameterAdjustHistoryGetDROut {
        private List<Infos.TableRecordInfo> strTableRecordInfoSeq;
        private List<Infos.TableRecordValue> strTableRecordValueSeq;
        private Page<List<Infos.TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    @Data
    public static class ObjLotQTimeGetDROut {
        private ObjectIdentifier lotID;
        private List<Infos.LotQtimeInfo> strLotQtimeInfoList;
    }

    @Data
    public static class ObjLotBondingOperationInfoGetDROut {
        private String bondingFlowSectionName;
        private ObjectIdentifier productID;
        private ObjectIdentifier targetRouteID;
        private String targetOperationNumber;
        private ObjectIdentifier targetOperationID;
    }

    @Data
    public static class ObjLotEffectiveFPCInfoForOperationGetOut {
        private Infos.FPCInfo strFPCInfo;
        private boolean equipmentActionRequired;
        private boolean machineRecipeActionRequired;
        private boolean recipeParameterActionRequired;
        private boolean reticleActionRequired;
        private boolean dcDefActionRequired;
        private boolean dcSpecActionRequired;
        private boolean correspondingOperationActionRequired;
        private boolean multipleCorrespondingOperationActionRequired;
    }

    @Data
    public static class objFixtureUsageLimitationCheckOut {
        private CimObjectUtils fixtureID;
        private boolean usageLimitOverFlag;
        private boolean runTimeOverFlag;
        private boolean startCountOverFlag;
        private boolean pmTimeOverFlag;
        private int runTime;
        private int maxRunTime;
        private int startCount;
        private int maxStartCount;
        private int passageTimeFromLastPM;
        private int intervalBetweenPM;
        private String messageText;
    }

    @Data
    public static class ObjCassetteLotReserveCancelOut {
        private List<Infos.ReserveCancelLot> strReserveCancelLot;
        private String claimMemo;
    }

    @Data
    public static class RawProcessResourceStateTranslateOut {
        private ObjectIdentifier equipmentStatusCode;
        private Boolean stateTranslated;
        private List<Infos.TranslatedChamberState> strTranslatedChamberState;
    }

    @Data
    public static class NPWReserveInfo{
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier reservedLoadPortID;
        private String loadPurposeType;
    }

    @Data
    public static class SendMoveInReserveForIBReqOut extends TCSOut {
        private Results.StartLotsReservationFotInternalBufferReqResult moveInReserveReqResult;
    }

    @Data
    public static class SendMoveInReserveCancelForIBReqOut extends TCSOut{
        private Results.MoveInReserveCancelForIBReqResult strMoveInReserveCancelReqResult;
    }

    @Data
    public static class SendRecipeParamAdjustOnActivePJReqOut extends TCSOut{
        private Results.RecipeParamAdjustOnActivePJReqResult recipeParamAdjustOnActivePJReqResult;
    }

    @Data
    public static class SendEDCDataItemWithTransitDataInqOut extends TCSOut{
        private List<Infos.CollectedDataItem>    collectedDataItems;
    }

    @Data
    public static class SendMoveInCancelForIBReqOut extends TCSOut {
        private Results.MoveInCancelReqResult  strMoveInCancelReqResult;
    }

    @Data
    public static  class SendMoveInForIBReqOut extends TCSOut{
        private Results.MoveInReqResult strMoveInReqResult;
    }

    @Data
    public static class SendPartialMoveOutReqOut extends TCSOut{
        private Results.PartialMoveOutReqResult partialMoveOutReqResult;
    }

    @Data
    public static class ObjLotWIPInfoResetOut {
        private boolean opehsAddFlag;
        private Inputs.OldCurrentPOData oldCurrentPOData;
    }

    @Data
    public static class ObjLotFutureReworkInfoCheckOut {
        private boolean updateFlag;
        private List<Infos.FutureReworkDetailInfo> futureReworkDetailInfos;
    }

    @Data
    public static class SpcOutput{
        private String txRC;
        private ObjectIdentifier lotID;
        private String lotRC;
        private String lotHoldAction;
        private List<Infos.ChamberHoldAction> chamberHoldActions;
        private String equipmentHoldAction;
        private String processHoldAction;
        private String recipeHoldAction;
        private String reworkBranchAction;
        private String mailSendAction;
        private String bankMoveAction;
        private String bankID;
        private String reworkRouteID;
        private String otherAction;
        private List<Results.SpcItemResult> itemResults;
        /**
         * Task-311 add ocapNo for constraint memo
         */
        private String ocapNo;
    }


    @Data
    public static class ObjBondingMapFillInTxPCR003DROut {
        private List<Infos.BondingMapInfo> strBondingMapInfoSeq;
        private List<ObjectIdentifier> bondingLotIDSeq;
    }

    @Data
    public static class ObjLotStackedWaferInfoGetDROut {
        private List<Infos.StackedWaferInfo> strStackedWaferInfoSeq;
        private List<ObjectIdentifier> baseLotIDSeq;
    }

    @Data
    public static class ObjLotProductProcessOperationInfoGetOut {
        private Infos.LotProductInfo strLotProductInfo;
        private Infos.ProcessOperationInformation strProcessOperationInformation;
    }

    @Data
    public static class BondingMapInfoConsistencyCheckForPartialReleaseOut {
        private List<Infos.BondingMapInfo> strPartialReleaseDestinationMapSeq;     //<i>Bonding Map to be Deleted
        private List<Infos.BondingMapInfo> strPartialReleaseSourceMapSeq;          //<i>Bonding Map to be Remained
        private List<Infos.BondingGroupReleaseLotWafer> strBondingGroupReleaseLotWaferSeq;      //<i>Sequence of Bonding Group Release Lot Wafer
    }

    @Data
    public static class EquipmentAvailableInfoGetDROut {
        private boolean                             conditionalAvailableFlagForChamber;     //<i>Whether the Equipment has a Chamber in Conditional Available State
        private List<Infos.EqpChamberAvailableInfo>  strEqpChamberAvailableInfoSeq;          //<i>Sequence of Chamber Availability Info
        private boolean                             availableFlagForEqp;                    //<i>Whether the Equipment is Available State
        private boolean                             conditionalAvailableFlagForEqp;         //<i>Whether the Equipment is Conditional Available State
        private List<String>                      availableSubLotTypesForEqp;
    }

    @Data
    public static class ObjLogicalRecipeMachineRecipeForSubLotTypeGetDROut {
        private ObjectIdentifier machineRecipeID; //<i>Machine Recipe ID
        private List<Infos.Chamber> chamberSeq;
        private boolean availableFlagForEqp;                    //<i>Whether the Equipment is Available State
        private boolean conditionalAvailableFlagForEqp;         //<i>Whether the Equipment is Conditional Available State
        private List<String> availableSubLotTypesForEqp;             //<i>Available SubLotType if the Equipment is Conditional Availale
    }

    @Data
    public static class TCSMgrSendSLMWaferRetrieveCassetteReserveReqout {
        private Results.SLMWaferRetrieveCassetteReserveReqResult strSLMWaferRetrieveCassetteReserveReqResult;
    }

    @Data
    public static class ApcOut {
        private Integer code;
        private String message;
        private String resultBody;
    }

    @Data
    public static class ProcessStartDurablesReserveInformationGetBaseInfoForClientOut {
        private ObjectIdentifier equipmentID;
        private String durableCategory;
        private List<Infos.StartDurable> strStartDurables;
        private Infos.DurableStartRecipe strDurableStartRecipe;
    }

    @Data
    public static class DurableControlJobStartReserveInformationGetOut {
        private ObjectIdentifier equipmentID;
        private String durableCategory;
        private List<Infos.StartDurable> strStartDurables;
        private Infos.DurableStartRecipe strDurableStartRecipe;
    }


    @Data
    public static class DurableHoldOut {
        private List<Infos.DurableHoldHistory> durableHoldHistories;
    }



    @Data
    public static class DurablePostProcessQueueMakeOut {
        private String dKey;
        private String keyTimeStamp;
        List<Infos.PostProcessActionInfo> strActionInfoSeq;
    }

    @Data
    public static class ReticleDispatchJobCreateOut {
        private Infos.ReticleDispatchJob    strReticleDispatchJob;
        private Infos.ReticleEventRecord    strReticleEventRecord;
        private Object                      siInfo;
    }

    @Data
    public static class ReticleAvailabilityCheckForXferOut {
        private ObjectIdentifier  machineID;
        private String            machineType;
        private ObjectIdentifier  reticlePodID;
        private String            transferStatus;
        private String            durableState;
        private Object            siInfo;
    }

    @Data
    public static class ReticlePodAvailabilityCheckForXferOut {
        private ObjectIdentifier          machineID;
        private String                    machineType;
        private String                    transferStatus;
        private ObjectIdentifier          portID;
        private String                    portStatus;
        private String                    portAccessMode;
        private Boolean                   emptyFlag;
        private List<ObjectIdentifier>    reticleList;
        private List<Long>                emptySlotNoList;
        private Object                    siInfo;
    }

    @Data
    public static class ReticleReticlePodGetForXferOut {
        private ObjectIdentifier                 reticleID;
        private List<Infos.CandidateReticlePod>  strCandidateReticlePods;
        private Object                           siInfo;
    }

    @Data
    public static class ReticleComponentJobCreateOut {
        private Infos.ReticleDispatchJob                strReticleDispatchJob;
        private List<Infos.ReticleComponentJob>         strReticleComponentJobList;
        private Infos.ReticleXferJob                    strReticleXferJob;
        private Infos.ReticleEventRecord                strReticleEventRecord;
        private Object                                  siInfo;
    }

    @Data
    public static class ReticleReservationDetailInfoGetOut {
        private boolean             reservedFlag;
        private String              reservePerson;
        private ObjectIdentifier    currentAssignedMachineID;
        private ObjectIdentifier    transferDestinationEquipmentID;
        private ObjectIdentifier    transferDestinationStockerID;
        private ObjectIdentifier    transferReserveUserID;
        private ObjectIdentifier    transferReservedReticlePodID;
        private String              transferReservedTimeStamp;
        private long                transferReservedReticlePodSlotNumber;
        private List<ControlJobAttributeInfo> strControlJobAttributeInfoSeq;
    }


    @Data
    public static class ReticleControlJobInfoGetOut {
        private List<ControlJobAttributeInfo> strControlJobAttributeInfoSeq;
    }

    @Data
    public static class ReticleArhsJobCreateOut {
        private String          reticleDispatchJobID;
        private String          reticleComponentJobID;
        private Object          siInfo;
    }

    @Data
    public static class ReticleComponentJobGetByJobNameDROut {
        private Infos.ReticleComponentJob strReticleComponentJob;
        private long jobCountInRDJ;
    }

    @Data
    public static class AutoSplitOut {
        private String cassetteID;
        private String destinationCassetteID;
        private String sorterJobCategory;
        private String sorterLotID;
        private List<String> lotIDs;
        private String holdType;
        private String holdReasonCodeID;
        private String releaseReasonCodeID;
        private ObjectIdentifier splitID;
    }


    @Data
    public static class ObjFixtureChangeTransportStateOut{
        private ObjectIdentifier    stockerID;
        private ObjectIdentifier        equipmentID;
        private List<Infos.XferFixture> strXferFixture;
    }

}
