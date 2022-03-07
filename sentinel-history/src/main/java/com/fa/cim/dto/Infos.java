package com.fa.cim.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;


/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/25 11:16:27
 */
public class Infos {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 11:17:49
     */
    @Data
    public static class LotOperationStartEventRecord {
        private LotEventData lotData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<String> reticleIDs;
        private List<String> fixtureIDs;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String batchID;
        private String controlJobID;
        private Boolean locateBackFlag;
        private Boolean testCriteriaResult;
        private EventData eventCommon;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private List<WaferLevelRecipeEventData> waferLevelRecipe;
        private List<String> samplingWafers;
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
     * @date 2019/7/1 15:37
     */
    @Data
    public static class EqpMonitorCountEventRecord {
        private EventData eventCommon;
        private LotEventData lotData;
        private String equipmentID;
        private String controlJobID;
        private List<EqpMonitorWaferCountEventData> wafers;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class NoteChangeEventRecord {
        private String objectID;
        private String noteType;
        private String action;
        private String routeID;
        private String operationID;
        private String operationNumber;
        private String noteTitle;
        private String noteContents;
        private String ownerID;
        private EventData eventCommon;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class LotReticleSetChangeEventRecord {
        private LotEventData lotData;
        private String reticleSetID;
        private EventData eventCommon;
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
     * @date 2019/7/12 10:30
     */
    @Data
    public static class DurableOperationCompleteEventRecord {
        private DurableEventData durableData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private String durableControlJobID;
        private Boolean locateBackFlag;
        private EventData eventCommon;
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
     * @date 2019/7/18 18:08
     */
    @Data
    public static class DurableReworkEventRecord {
        private DurableEventData durableData;
        private ObjectIdentifier reasonCodeID;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private Long reworkCount;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private DurableProcessOperationEventData oldCurrentDurablePOData;
        private EventData eventCommon;
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
     * @date 2019/7/18 18:08
     */
    @Data
    public static class DurableProcessOperationEventData {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Long operationPassCount;
        private String objrefPOS;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
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
     * @date 2019/7/23 10:34
     */
    @Data
    public static class DurableOperationMoveEventRecord {
        private DurableEventData durableData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private String durableControlJobID;
        private Boolean locateBackFlag;
        private DurableProcessOperationEventData oldCurrentDurablePOData;
        private EventData eventCommon;
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
     * @date 2019/7/23 10:34
     */
    @Data
    public static class LotWaferScrapEventRecord {

        private LotEventData lotData;
        private List<WaferEventData> currentWafers;
        private List<WaferScrapEventData> scrapWafers;

        private String reasonRouteID;

        private String reasonOperationNumber;

        private String reasonOperationID;

        private Long reasonOperationPassCount;
        private EventData eventCommon;

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
     * @date 2019/7/23 10:34
     */
    @Data
    public static class WaferScrapEventData {

        private String waferID;
        private ObjectIdentifier reasonCodeID;
        private Boolean controlWaferFlag;

    }

    @Data
    public static class LotTerminateEventRecord {

        private LotEventData lotData;
        private EventData eventCommon;
        private ObjectIdentifier reasonCode;

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
     * @date 2019/7/23 23:23
     */
    @Data
    public static class Ohscrhs {
        private String    lot_id;
        private String    wafer_id;
        private String    reason_code;
        private String    reason_description;

        private Integer     scrap_unit_count;
        private String    prodspec_id;
        private String    lot_owner_id;
        private String    prodgrp_id;
        private String    tech_id;
        private String    custprod_id;
        private String    order_no;
        private String    customer_id;

        private Integer     control_wafer;
        private Integer     good_unit_wafer;
        private Integer     repair_unit_wafer;
        private Integer     fail_unit_wafer;
        private String    lot_type;
        private String    cast_id;
        private String    cast_category;
        private String    prod_type;
        private String    claim_mainpd_id;
        private String    claim_ope_no;
        private String    claim_pd_id;

        private Integer     claim_pass_count;
        private String    claim_ope_name;
        private String    claim_test_type;
        private String    claim_time;
        private Double  claim_shop_date;
        private String    claim_user_id;
        private String    claim_stage_id;
        private String    claim_stagegrp_id;
        private String    claim_photo_layer;
        private String    claim_department;
        private String    claim_bank_id;
        private String    reason_lot_id;
        private String    reason_mainpd_id;
        private String    reason_ope_no;
        private String    reason_pd_id;

        private Integer     reason_pass_count;
        private String    reason_ope_name;
        private String    reason_test_type;
        private String    reason_stage_id;
        private String    reason_stagegrp_id;
        private String    reason_photo_layer;
        private String    reason_department;
        private String    reason_location_id;
        private String    reason_area_id;
        private String    reason_eqp_id;
        private String    reason_eqp_name;
        private String    scrap_type;
        private String    claim_memo;
        private String    event_create_time;
    }

    @Data
    public static class Ohtrmhs {

        private String lotId;
        private String waferId;
        private String reasonCode;
        private String reasonDescription;

        private String prodspecId;
        private String lotOwnerId;
        private String prodgrpId;
        private String techId;
        private String custprodId;
        private String orderNo;
        private String customerId;

        private Integer controlWafer;
        private Integer goodUnitWafer;
        private Integer repairUnitWafer;
        private Integer failUnitWafer;
        private String lotType;
        private String castId;
        private String castCategory;
        private String prodType;
        private String claimMainpdId;
        private String claimOpeNo;
        private String claimPdId;

        private Integer claimPassCount;
        private String claimOpeName;
        private String claimTestType;
        private String claimTime;
        private Double claimShopDate;
        private String claimUserId;
        private String claimStageId;
        private String claimStagegrpId;
        private String claimPhotoLayer;
        private String claimDepartment;
        private String claimBankId;
        private String reasonLotId;
        private String reasonMainpdId;
        private String reasonOpeNo;
        private String reasonPdId;

        private Integer reasonPassCount;
        private String reasonOpeName;
        private String reasonTestType;
        private String reasonStageId;
        private String reasonStagegrpId;
        private String reasonPhotoLayer;
        private String reasonDepartment;
        private String reasonLocationId;
        private String reasonAreaId;
        private String reasonEqpId;
        private String reasonEqpName;
        private String terminateType;
        private String claimMemo;
        private String eventCreateTime;
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
     * @date 2019/7/12 10:30
     */
    @Data
    public static class DurablePFXEventRecord {
        private DurableEventData durableData;
        private EventData eventCommon;
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
     * @date 2019/7/12 10:30
     */
    @Data
    public static class DurableControlJobStatusChangeEventRecord {
        private String durableCtrlJobID;
        private String durableCtrlJobState;
        private String eqpID;
        private String eqpDescription;
        private List<DurableControlJobStatusChangeDurableEventData> durables;
        private EventData eventCommon;
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
     * @date 2019/7/12 10:30
     */
    @Data
    public static class DurableAttributeEventData {
       protected String durableID;
       protected String durableCategory;
       protected String prodSpecID;
       protected String mainPDID;
       protected String opeNo;
       protected String pdID;
       protected Long opePassCount;
       protected String pdName;
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
     * @date 2019/7/12 10:30
     */
    @Data
    public static class DurableControlJobStatusChangeDurableEventData extends DurableAttributeEventData {}

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/12 10:30
     */
    @Data
    public static class DurableBankMoveEventRecord {
        private DurableEventData durableData;
        private String previousBankID;
        private EventData eventCommon;
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
     * @date 2019/7/12 10:30
     */
    @Data
    public static class Ohdcjschs {
        private String dctrljob_id            ;
        private String dctrljob_state         ;
        private String drbl_category          ;
        private String eqp_id                 ;
        private String eqp_description        ;
        private String claim_time             ;
        private Double  claim_shop_date;
        private String claim_user_id          ;
        private String claim_memo             ;
        private String store_time             ;
        private String event_create_time      ;
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
     * @date 2019/7/12 10:30
     */
    @Data
    public static class Ohdcjschs_drbl {
        private String dctrljob_id            ;
        private String durable_id             ;
        private String drbl_category          ;
        private String prodspec_id            ;
        private String mainpd_id              ;
        private String ope_no                 ;
        private String pd_id                  ;
        private Long    ope_pass_count;
        private String pd_name                ;
        private String claim_time             ;
        private String store_time             ;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class DurableEventRecord {
        private String action;
        private String durableType;
        private String durableID;
        private String description;
        private String categoryID;
        private String instanceName;
        private Boolean usageCheckRequiredFlag;
        private Double durationLimit;
        private Long timeUsedLimit;
        private Long intervalBetweenPM;
        private String contents;
        private Long contentsSize;
        private Long capacity;
        private EventData eventCommon;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class DurableOperationStartEventRecord {
        private DurableEventData durableData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private String durableControlJobID;
        private Boolean locateBackFlag;
        private EventData eventCommon;
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
     * @date 2019/7/30 9:42
     */
    @Data
    public static class SLMSwitchEventRecord {

        private String machineID;
        private String SLMSwitch;
        private EventData eventCommon;

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
     * @date 2019/7/30 9:50
     */
    @Data
    public static class SLMMaxReserveCountEventRecord {

        private String machineID;
        private String machineContainerID;
        private Long maxReserveCount;
        private EventData eventCommon;

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @exception
     * @author Ho
     * @date 2019/7/30 9:59
     */
    @Data
    public static class UserDataChangeEventRecord {

        private String className;
        private String hashedInfo;
        private List<UserDataChangeActionEventData> actions;
        private EventData eventCommon;

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
     * @date 2019/7/30 9:59
     */
    @Data
    public static class UserDataChangeActionEventData {

        private String name;
        private String orig;
        private String actionCode;
        private String fromType;
        private String fromValue;
        private String toType;
        private String toValue;

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
     * @date 2019/7/30 10:19
     */
    @Data
    public static class FPCEventRecord {

        private String action;
        private String FPCID;
        private String lotFamilyID;
        private String mainPDID;
        private String operationNumber;
        private String originalMainPDID;
        private String originalOperationNumber;
        private String subMainPDID;
        private String subOperationNumber;
        private String FPCGroupNo;
        private String FPCType;
        private String mergeMainPDID;
        private String mergeOperationNumber;
        private String FPCCategory;
        private String pdID;
        private String pdType;
        private String correspondingOperNo;
        private Boolean skipFlag;
        private Boolean restrictEqpFlag;
        private String equipmentID;
        private String machineRecipeID;
        private String dcDefID;
        private String dcSpecID;
        private String recipeParameterChangeType;
        private Boolean sendEmailFlag;
        private Boolean holdLotFlag;
        private String description;
        private String createTime;
        private String updateTime;
        private List<WaferRecipeParameterEventData> wafers;
        private List<ReticleEventData> reticles;
        private List<DCSpecItemEventData> dcSpecItems;
        private List<CorrespondingOperationEventData> correspondingOperations;
        private EventData eventCommon;

        //add runCardID for history
        private String runCardID;

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
     * @date 2019/7/30 10:20
     */
    @Data
    public static class WaferRecipeParameterEventData {

        private String waferID;
        private List<RecipeParameterEventData> recipeParameters;

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
     * @date 2019/7/30 10:20
     */
    @Data
    public static class RecipeParameterEventData {

        private Long seq_No;
        private String parameterName;
        private String parameterValue;
        private Boolean useCurrentSettingValueFlag;
        private String parameterTag;
        private String parameterUnit;
        private String parameterDataType;
        private String parameterLowerLimit;
        private String parameterUpperLimit;
        private String parameterTargetValue;

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
     * @date 2019/7/30 10:21
     */
    @Data
    public static class ReticleEventData {

        private Long seq_No;
        private String reticleID;
        private String reticleGroupID;

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
     * @date 2019/7/30 10:22
     */
    @Data
    public static class DCSpecItemEventData {

        private String dcItemName;
        private Boolean screenUpperRequired;
        private Double screenUpperLimit;
        private String screenUpperActions;
        private Boolean screenLowerRequired;
        private Double screenLowerLimit;
        private String screenLowerActions;
        private Boolean specUpperRequired;
        private Double specUpperLimit;
        private String specUpperActions;
        private Boolean specLowerRequired;
        private Double specLowerLimit;
        private String specLowerActions;
        private Boolean controlUpperRequired;
        private Double controlUpeerLimit;
        private String controlUpperActions;
        private Boolean controlLowerRequired;
        private Double controlLowerLimit;
        private String controlLowerActions;
        private Double dcItemTargetValue;
        private String dcItemTag;
        private String dcSpecGroup;

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
     * @date 2019/7/30 10:22
     */
    @Data
    public static class CorrespondingOperationEventData {

        private String correspondingOperationNumber;
        private String dcSpecGroup;

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
     * @date 2019/7/30 13:27
     */
    @Data
    public static class APCInterfaceEventRecord {

        private String equipmentID;
        private String APC_systemName;
        private String storeTime;
        private String operationCategory;
        private String equipmentDescription;
        private Boolean ignoreAbleFlag;
        private String APC_responsibleUserID;
        private String APC_subResponsibleUserID;
        private String APC_configState;
        private String APC_registeredUserID;
        private String registeredTime;
        private String registeredMemo;
        private String approvedUserID;
        private String approvedTime;
        private String approvedMemo;
        private EventData eventCommon;

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
     * @date 2019/7/30 13:42
     */
    @Data
    public static class ScriptParameterChangeEventRecord {

        private String parameterClass;
        private String identifier;
        private List<UserParameterValue> userParameterValues;
        private EventData eventCommon;

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
     * @date 2019/7/30 13:43
     */
    @Data
    public static class UserParameterValue {

        private Long changeType;
        private String parameterName;
        private String dataType;
        private String keyValue;
        private String value;
        private Boolean valueFlag;
        private String description;

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
     * @date 2019/7/30 13:58
     */
    @Data
    public static class SystemMessageEventRecord {

        private String subSystemID;
        private String systemMessageCode;
        private String systemMessageText;
        private Boolean notifyFlag;
        private String equipmentID;

        private String equipmentState;
        private String stockerID;

        private String stockerState;
        private String AGVID;

        private String AGVState;
        private String lotID;
        private String lotState;

        private String routeID;

        private String operationNumber;

        private String operationID;
        private EventData eventCommon;

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
     * @date 2019/7/30 13:58
     */
    @Data
    public static class Ohsysmhs {
        private String    sub_system_id;
        private String    sys_msg_code;
        private String    sys_msg_text;

        private Integer     notify_flag;
        private String    eqp_id;
        private String    eqp_state;
        private String    stk_id;
        private String    stk_state;
        private String    agv_id;
        private String    agv_state;
        private String    lot_id;
        private String    lot_state;
        private String    mainpd_id;
        private String    ope_no;
        private String    pd_id;
        private String    pd_name;
        private String    clam_time;
        private Double  clam_shop_date;
        private String    clam_user_id;
        private String    claim_memo;
        private String    event_create_time;
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
     * @date 2019/7/30 13:44
     */
    @Data
    public static class Ohbrpchs {
        private String   parameter_class;
        private String   identifier;
        private String   change_type;

        private String   parameter_name;
        private String   data_type;
        private String   key_value;
        private String   value;

        private Integer    value_flag;
        private String   description;
        private String   event_time;
        private Double event_shop;
        private String   claim_user_id;
        private String   event_memo;
        private String   event_create_time;
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
     * @date 2019/7/30 13:28
     */
    @Data
    public static class  Ohapcifhs {
        private String   equipmentID;
        private String   APC_systemName;
        private String   storeTime;
        private String   operationCategory;
        private String   equipmentDescription;

        private Integer    ignoreAbleFlag ;
        private String   APC_responsibleUserID;
        private String   APC_subResponsibleUserID;
        private String   APC_configState;
        private String   APC_registeredUserID;
        private String   registeredTime;
        private String   registeredMemo;
        private String   approvedUserID;
        private String   approvedTime;
        private String   approvedMemo;
        private String   eventCreationTimeStamp;
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
     * @date 2019/7/30 10:27
     */
    @Data
    public static class OhfpchsCorope{
        private String    FPCID;
        private Integer     seq_no                        ;
        private String    correspondingOpeNo;
        private String    dcSpecGroup;
        private String    claim_time;

        //add runCardID for history
        private String runCardID;
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
     * @date 2019/7/30 10:26
     */
    @Data
    public static class OhfpchsRtcl{
        private String    FPCID;

        private Integer     seq_no                        ;
        private String    reticleID;
        private String    reticleGroupID;
        private String    claim_time;

        //add runCardID for history
        private String runCardID;
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
     * @date 2019/7/30 10:25
     */
    @Data
    public static class OhfpchsDcspecs{
        private String         FPCID;
        private String         dcItemName;

        private Integer          screenUpperRequired   ;
        private Double       screenUpperLimit      ;

        private String         screenUpperActions;

        private Integer          screenLowerRequired   ;
        private Double       screenLowerLimit      ;

        private String         screenLowerActions;

        private Integer          specUpperRequired     ;
        private Double       specUpperLimit        ;

        private String         specUpperActions;

        private Integer          specLowerRequired     ;
        private Double       specLowerLimit        ;

        private String         specLowerActions;

        private Integer          controlUpperRequired  ;
        private Double       controlUpeerLimit     ;

        private String         controlUpperActions;

        private Integer          controlLowerRequired  ;
        private Double       controlLowerLimit     ;

        private String         controlLowerActions;
        private Double       dcItemTargetValue     ;

        private String         dcItemTag;
        private String         dcSpecGroup;

        private String         claim_time;

        //add runCardID for history
        private String runCardID;
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
     * @date 2019/7/30 10:25
     */
    @Data
    public static class OhfpchsWaferRparm{
        private String    FPCID;
        private String    wafer_id;

        private Integer     seq_no                     ;
        private String    parameterName;
        private String    parameterUnit;
        private String    parameterDataType;
        private String    parameterLowerLimit;
        private String    parameterUpperLimit;
        private String    parameterValue;

        private Integer     useCurrentSettingValueFlag ;
        private String    parameterTargetValue;
        private String    parameterTag;
        private String    claim_time;

        //add runCardID for history
        private String runCardID;
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
     * @date 2019/7/30 10:24
     */
    @Data
    public static class OhfpchsWafer{
        private String    FPCID;
        private String    wafer_id;
        private String    claim_time;

        //add runCardID for history
        private String runCardID;
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
     * @date 2019/7/30 10:23
     */
    @Data
    public static class Ohfpchs{
        private String    action;
        private String    FPCID;
        private String    lotFamilyID;
        private String    mainPDID;
        private String    operationNumber;
        private String    originalMainPDID;
        private String    originalOperationNumber;
        private String    subMainPDID;
        private String    subOperationNumber;
        private String    FPCGroupNo;
        private String    FPCType;
        private String    mergeMainPDID;
        private String    mergeOperationNumber;
        private String    FPCCategory;
        private String    pdID;
        private String    pdType;
        private String    correspondingOperNo;

        private Integer     skipFlag                  ;
        private Integer     restrictEqpFlag           ;
        private String    equipmentID;
        private String    machineRecipeID;
        private String    dcDefID;
        private String    dcSpecID;
        private String    recipeParameterChangeType;
        private String    description;

        private Integer     sendEmailFlag             ;
        private Integer     holdLotFlag               ;
        private String    createTime;
        private String    updateTime;
        private String    claim_time;
        private String    claim_user_id;
        private String    claim_memo;
        private String    event_create_time;

        //add runCardID for history
        private String runCardID;
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
     * @date 2019/7/30 10:01
     */
    @Data
    public static class OhudathsAction {
        private String  className;
        private String  hashedInfo;
        private String  name;
        private String  orig;
        private String  actionCode;
        private String  fromType;
        private String  fromValue;
        private String  toType;
        private String  toValue;
        private String  claimTime;
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
     * @date 2019/7/30 10:00
     */
    @Data
    public static class Ohudaths {
        private String  className;
        private String  hashedInfo;
        private String  claimTime;
        private String  claimUser;
        private String  claimMemo;
        private String  storeTime;
        private String  eventCreateTime;
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
     * @date 2019/7/30 9:51
     */
    @Data
    public static class Ohslmmaxrsvcnths {
        private String  equipmentID;
        private String  machineContainerID;
        private Integer   maxReserveCount;
        private String  claimTime;
        private String  claimUser;
        private String  claimMemo;
        private String  storeTime;
        private String  eventCreateTime;
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
     * @date 2019/7/30 9:43
     */
    @Data
    public static class Ohslmswitchhs {
        private String  equipmentID;
        private String  SLMSwitch;
        private String  claimTime;
        private String  claimUser;
        private String  claimMemo;
        private String  storeTime;
        private String  eventCreateTime;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class DurableEventData {
        private String durableID;
        private String durableCategory;
        private String durableStatus;
        private String holdState;
        private String bankID;
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Long operationPassCount;
        private String objrefPOS;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class Ohdrblopehs_rparm {
        private String durable_id             ;
        private String drbl_category          ;
        private String mainpd_id              ;
        private String ope_no                 ;
        private Long    ope_pass_count;
        private String claim_time             ;
        private String ope_category           ;
        private String rparm_name             ;
        private String rparm_value            ;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class Ohdrblopehs {
        private String durable_id            ;
        private String drbl_category         ;
        private String mainpd_id             ;
        private String ope_no                ;
        private String pd_id                 ;
        private Long     ope_pass_count;
        private String pd_name               ;
        private String claim_time            ;
        private Double   claim_shop_date;
        private String claim_user_id         ;
        private String move_type             ;
        private String ope_category          ;
        private String stage_id              ;
        private String stagegrp_id           ;
        private String photo_layer           ;
        private String location_id           ;
        private String area_id               ;
        private String eqp_id                ;
        private String eqp_name              ;
        private String ope_mode              ;
        private String lc_recipe_id          ;
        private String recipe_id             ;
        private String ph_recipe_id          ;
        private Long     rparm_count;
        private String bank_id               ;
        private String prev_bank_id          ;
        private String prev_mainpd_id        ;
        private String prev_ope_no           ;
        private String prev_pd_id            ;
        private String prev_pd_name          ;
        private Long     prev_pass_count;
        private String prev_stage_id         ;
        private String prev_stagegrp_id      ;
        private String prev_photo_layer      ;
        private String dctrl_job             ;
        private String drbl_owner_id         ;
        private String plan_end_time         ;
        private Boolean criteria_flag;
        private String claim_memo            ;
        private String store_time            ;
        private String rparm_change_type     ;
        private String event_create_time     ;
        private String original_fab_id       ;
        private String destination_fab_id    ;
        private String hold_state            ;
        private Integer init_hold_flag;
        private String hold_time             ;
        private Double   hold_shop_date;
        private String hold_user_id          ;
        private String hold_type             ;
        private String hold_reason_code      ;
        private String hold_reason_desc      ;
        private String reason_code           ;
        private String reason_description    ;
        private Integer rework_count;
        private String hold_ope_no           ;
        private String hold_reason_ope_no    ;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class Ohdrblhs {
        private String action              ;
        private String durable_type        ;
        private String durable_id          ;
        private String description         ;
        private String category_id         ;
        private String instance_name       ;
        private Boolean usage_check_required;
        private Double  duration_limit;
        private Integer times_used_limit;
        private Integer interval_between_pm;
        private String contents            ;
        private Integer contents_size;
        private Integer capacity;
        private String claim_time          ;
        private Double  claim_shop_date;
        private String claim_user_id       ;
        private String claim_memo          ;
        private String store_time          ;
        private String event_create_time   ;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class Ohrsths {
        private String lot_id          ;
        private String rtclset_id      ;
        private String prodspec_id     ;
        private String lot_type        ;
        private String lot_status      ;
        private String cast_id         ;
        private String customer_id     ;
        private Integer     priority_class;
        private String claim_mainpd_id ;
        private String claim_ope_no    ;
        private String claim_pd_id     ;
        private Integer     claim_pass_count;
        private String claim_time      ;
        private Double  claim_shop_date;
        private String claim_user_id   ;
        private String event_create_time ;
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
     * @date 2019/7/11 10:40
     */
    @Data
    public static class Ohnotehs {
        private String object_id            ;
        private String note_type            ;
        private String action               ;
        private String mainpd_id            ;
        private String pd_id                ;
        private String ope_no               ;
        private String note_title           ;
        private String note_contents        ;
        private String owner_id             ;
        private String claim_user_id        ;
        private String claim_memo           ;
        private String claim_time           ;
        private Double  claim_shop_date;
        private String event_create_time    ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class LotFutureHoldEventRecord {
        private String lotID;
        private String entryType;
        private String holdType;
        private String registerReasonCode;
        private String registerPerson;
        private String routeID;
        private String opeNo;
        private Boolean singleTriggerFlag;
        private Boolean postFlag;
        private String relatedLotID;
        private String releaseReasonCode;
        private EventData eventCommon;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class DynamicBufferResourceChangeEventRecord {
        private String equipmentID;
        private String equipmentState;
        private String E10State;
        private String bufferCategory;
        private Long smCapacity;
        private Long dynamicCapacity;
        private EventData eventCommon;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Ohbuffrscchs {
        private String eqp_id                 ;
        private String eqp_state              ;
        private String e10_state              ;
        private String buffer_category        ;
        private Long    sm_capacity;
        private Long    dynamic_capacity;
        private String claim_user_id          ;
        private String claim_memo             ;
        private String claim_time             ;
        private Double  claim_shop_date;
        private String event_create_time      ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class FutureReworkEventRecord {
        private String action;
        private String lotID;
        private String routeID;
        private String operationNumber;
        private List<FutureReworkRouteEventData> reworkRoutes;
        private EventData eventCommon;
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
     * @date 2019/7/9 15:58
     */
    @Data
    public static class DurableChangeEventRecord {
        private String durableID;
        private String durableType;
        private String action;
        private String durableStatus;
        private String durableSubStatus;
        private String xferStatus;
        private String xferStatChgTimeStamp;
        private String location;
        private EventData eventCommon;
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
     * @date 2019/7/9 15:58
     */
    @Data
    public static class Ohdrchs {
        private String durable_id      ;
        private String durable_type    ;
        private String action_code     ;
        private String durable_status  ;
        private String xfer_status     ;
        private String xfer_stat_chg_time ;
        private String location        ;
        private String claim_time      ;
        private Double claim_shop_date ;
        private String claim_user_id   ;
        private String claim_memo      ;
        private String event_create_time ;
        private String durable_sub_status ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class FutureReworkRouteEventData {
        private String trigger;
        private String reworkRouteID;
        private String returnOperationNumber;
        private ObjectIdentifier reasonCodeID;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class EntityInhibitEventRecord {
        private String historyID;
        private String inhibitID;
        private String description;
        private String startTimeStamp;
        private String endTimeStamp;
        private List<EntityIdentifier> entities;
        private List<EntityIdentifier> expEntities;
        private List<String> sublottypes;
        private String reasonCode;
        private String reasonDesc;
        private String ownerID;
        private List<EntityInhibitReasonDetailInfo> reasonDetailInfos;
        private EventData eventCommon;
        private List<ExceptionLotRecord> exceptionLots;
        private String functionRule;
        private Boolean specificTool;
        private String appliedContext;
        private String Memo;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class ExceptionLotRecord {
        private ObjectIdentifier lotID;
        private Boolean singleTriggerFlag;
        private Boolean usedFlag;
        private ObjectIdentifier claimUserID;
        private Timestamp claimTimeStamp;
        private String claimMemo;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class EntityInhibitReasonDetailInfo {
        private String relatedLotID;
        private String relatedControlJobID;
        private String relatedFabID;
        private String relatedRouteID;
        private String relatedProcessDefinitionID;
        private String relatedOperationNumber;
        private String relatedOperationPassCount;
        private List<EntityInhibitSpcChartInfo> relatedSpcChartInfos;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class EntityInhibitSpcChartInfo {
        private String relatedSpcDcType;
        private String relatedSpcChartGroupID;
        private String relatedSpcChartID;
        private String relatedSpcChartType;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class EntityIdentifier {
        private String className;
        private ObjectIdentifier objectId;
        private String attrib;
        private String refrenceKey;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Oheninhs_entity {
        private String refrenceKey;
        private String inhibit_id          ;
        private String claim_time          ;
        private String class_name          ;
        private String entity_id           ;
        private String attrib              ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Oheninhs_sublot {
        private String inhibit_id          ;
        private String claim_time          ;
        private String sub_lot_type        ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Oheninhs_rsninfo_spc {
        private String inhibit_id          ;
        private String dc_type             ;
        private String chart_grp_id        ;
        private String chart_id            ;
        private String chart_type          ;
        private String claim_time          ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Oheninhs_explot {
        private String inhibit_id          ;
        private String lot_id              ;
        private Boolean single_trig_flag;
        private String claim_time          ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Oheninhs_rsninfo {
        private String inhibit_id          ;
        private String lot_id              ;
        private String ctrljob_id          ;
        private String fab_id              ;
        private String mainpd_id           ;
        private String pd_id               ;
        private String ope_no              ;
        private String pass_count          ;
        private String claim_time          ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Oheninhs {
        private String historyID;
        private String inhibit_id          ;
        private String inhibit_type        ;
        private String start_time          ;
        private String end_time            ;
        private String reason_code         ;
        private String reason_desc         ;
        private String description         ;
        private String claim_time          ;
        private Double  claim_shop_date;
        private String claim_user_id       ;
        private String claim_memo          ;
        private String store_time          ;
        private String event_create_time   ;
        private String applied_context     ;
        private String function_rule       ;
        private Boolean specific_tool      ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Ohfrwkhs {
        private String action_code         ;
        private String lot_id              ;
        private String mainpd_id           ;
        private String ope_no              ;
        private String trigger             ;
        private String rwk_route_id        ;
        private String return_ope_no       ;
        private String reason_code         ;
        private String reason_description  ;
        private String claim_time          ;
        private Double  claim_shop_date;
        private String claim_user_id       ;
        private String claim_memo          ;
        private String store_time          ;
        private String event_create_time   ;
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
     * @date 2019/7/4 13:29
     */
    @Data
    public static class Ohfhldhs {
        private String lot_id           ;
        private String entry_type       ;
        private String hold_Type        ;
        private String reg_reason_code  ;
        private String reg_person_id    ;
        private String mainpd_id        ;
        private String ope_no           ;
        private Boolean single_trig_flag ;
        private Boolean post_flag;
        private String related_lot_id   ;
        private String rel_reason_code  ;
        private String claim_time       ;
        private Double  claim_shop_date  ;
        private String claim_user_id    ;
        private String claim_memo       ;
        private String event_create_time ;
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
     * @date 2019/7/15 10:36
     */
    @Data
    public static class DurableHoldEventRecord {
        private DurableEventData durableData;
        private List<DurableHoldEventData> holdRecords;
        private ObjectIdentifier releaseReasonCodeID;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private EventData eventCommon;
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
     * @date 2019/7/15 10:36
     */
    @Data
    public static class DurableHoldEventData {
        private Boolean movementFlag;
        private Boolean changeStateFlag;
        private String holdType;
        private ObjectIdentifier holdReasonCodeID;
        private String holdUserID;
        private String holdTimeStamp;
        private Boolean responsibleOperationFlag;
        private String holdClaimMemo;
        private Boolean responsibleOperationExistFlag;
        private ObjectIdentifier responsibleRouteID;
        private ObjectIdentifier responsibleOperationID;
        private String responsibleOperationNumber;
        private String responsibleOperationName;
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
     * @date 2019/7/3 13:32
     */
    @Data
    public static class DurableXferJobStatusChangeEventRecord {
        private String durableType;
        private String operationCategory;
        private String carrierID;
        private String jobID;
        private String carrierJobID;
        private String transportType;
        private String zoneType;
        private Long n2purgeFlag;
        private String fromMachineID;
        private String fromPortID;
        private String toStockerGroup;
        private String toMachineID;
        private String toPortID;
        private String expectedStrtTime;
        private String expectedEndTime;
        private String estimateStrtTime;
        private String estimateEndTime;
        private Long mandatoryFlag;
        private String priority;
        private String jobStatus;
        private String carrierJobStatus;
        private EventData eventCommon;
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
     * @date 2019/7/3 10:58
     */
    @Data
    public static class APCProcessDispositionEventRecord {
        private String ctrlJob;
        private String eqpID;
        private String eqpDescription;
        private String APC_systemName;
        private String requestCategory;
        private String storeTime;
        private List<APCProcessDispositionLotEventData> lots;
        private EventData eventCommon;
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
     * @date 2019/7/3 11:00
     */
    @Data
    public static class InterFabXferEventRecord {
        private String opeCategory;
        private String lotID;
        private String originalFabID;
        private String originalRouteID;
        private String originalOpeNumber;
        private String destinationFabID;
        private String destinationRouteID;
        private String destinationOpeNumber;
        private EventData eventCommon;
        private LotEventData lotData;
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
     * @date 2019/7/3 11:00
     */
    @Data
    public static class LotAttributeEventData {
       protected String lotID;
       protected String castID;
       protected String lotType;
       protected String subLotType;
       protected String prodSpecID;
       protected String mainPDID;
       protected String opeNo;
       protected String pdID;
       protected Long opePassCount;
       protected String pdName;
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
     * @date 2019/7/3 11:02
     */
    @Data
    public static class APCProcessDispositionLotEventData extends LotAttributeEventData {}

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/3 11:02
     */
    @Data
    public static class Ohxferjobhs {
        private String durable_type           ;
        private String ope_category           ;
        private String carrier_id             ;
        private String job_id                 ;
        private String carrier_job_id         ;
        private String transport_type         ;
        private String zone_type              ;
        private Integer n2purge_flag;
        private String from_machine_id        ;
        private String from_port_id           ;
        private String to_stocker_group       ;
        private String to_machine_id          ;
        private String to_port_id             ;
        private String expected_start_time    ;
        private String expected_end_time      ;
        private String estimate_start_time    ;
        private String estimate_end_time      ;
        private Integer mandatory_flag;
        private String priority               ;
        private String job_status             ;
        private String carrier_job_status     ;
        private String claim_time             ;
        private String claim_user_id          ;
        private String claim_memo             ;
        private String store_time             ;
        private String event_create_time      ;
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
     * @date 2019/7/3 11:02
     */
    @Data
    public static class Ohprcdphs {
        private String ctrl_job          ;
        private String claim_time        ;
        private String eqp_id            ;
        private String eqp_descripstion  ;
        private String claim_user_id     ;
        private String apc_system_name   ;
        private String req_category      ;
        private String claim_memo        ;
        private String store_time        ;
        private String event_create_time ;
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
     * @date 2019/7/3 11:02
     */
    @Data
    public static class Ohprcdphs_lots {
        private String ctrl_job          ;
        private String claim_time        ;
        private String lot_id            ;
        private String cast_id           ;
        private String lot_type          ;
        private String sub_lot_type      ;
        private String prodspec_id       ;
        private String mainpd_id         ;
        private String ope_no            ;
        private String pd_id             ;
        private Integer ope_pass_count;
        private String pd_name           ;
        private String store_time        ;
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
     * @date 2019/7/2 14:46
     */
    @Data
    public static class ProcessStatusEventRecord {
        private LotEventData lotData;
        private String actionCode;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private String batchID;
        private String controlJobID;
        private EventData eventCommon;
    }

    @Data
    public static class ProcessJobChangeEventRecord {
        private String ctrlJob;
        private String prcsJob;
        private String opeCategory;
        private String processStart;
        private String currentState;
        private List<ProcessJobChangeWaferEventData> wafers;
        private List<ProcessJobChangeRecipeParameterEventData> recipeParameters;
        private EventData eventCommon;
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
     * @date 2019/7/2 16:59
     */
    @Data
    public static class ProcessJobChangeRecipeParameterEventData {
        private String parameterName;
        private String previousParameterValue;
        private String parameterValue;
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
     * @date 2019/7/2 16:58
     */
    @Data
    public static class ProcessJobChangeWaferEventData {
        private String waferID;
        private String lotID;
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
     * @date 2019/7/2 15:29
     */
    @Data
    public static class ProcessHoldEventRecord {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private String holdType;
        private ObjectIdentifier reasonCodeID;
        private String objrefPOS;
        private EventData eventCommon;
        private String productID;
        private Boolean withExecHoldFlag;
        private String entryType;
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
     * @date 2019/7/2 17:01
     */
    @Data
    public static class Ohpjchs {
        private String ctrlJob               ;
        private String prcsJob               ;
        private String opeCategory           ;
        private String processStart          ;
        private String currentState          ;
        private String claimTime             ;
        private Double  claim_shop_date;
        private String claimUser             ;
        private String claimMemo             ;
        private String storeTime             ;
        private String eventCreateTime       ;
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
     * @date 2019/7/2 17:02
     */
    @Data
    public static class Ohpjchs_wafer {
        private String prcsJob                 ;
        private String waferID                 ;
        private String lotID                   ;
        private String claimTime               ;
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
     * @date 2019/7/2 17:03
     */
    @Data
    public static class Ohpjchs_rparm {
        private String prcsJob                 ;
        private String parameterName           ;
        private String previousParameterValue  ;
        private String parameterValue          ;
        private String claimTime               ;
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
     * @date 2019/7/2 15:32
     */
    @Data
    public static class Ohphlhs {
        private String mainpd_id          ;
        private String ope_no             ;
        private String pd_id              ;
        private String ope_name           ;
        private String prod_id            ;
        private String claim_time         ;
        private Double claim_shop_date;
        private String claim_user_id      ;
        private String entry_type         ;
        private String hold_type          ;
        private String reason_code        ;
        private String reason_description ;
        private Integer hold_flag;
        private String stage_id           ;
        private String stagegrp_id        ;
        private String photo_layer        ;
        private String claim_memo         ;
        private String store_time         ;
        private String event_create_time  ;
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
     * @date 2019/7/1 16:38
     */
    @Data
    public static class EquipmentFlowBatchMaxCountChangeEventRecord {
        private String equipmentID;
        private Long newFlowBatchMaxCount;
        private EventData eventCommon;
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
     * @date 2019/7/1 15:38
     */
    @Data
    public static class EqpMonitorWaferCountEventData {
        private String waferID;
        private Long eqpMonitorUsedCount;
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
     * @date 2019/7/1 15:00
     */
    @Data
    public static class EqpMonitorJobEventRecord {
        private String opeCategory;
        private String equipmentID;
        private String chamberID;
        private String eqpMonitorID;
        private String eqpMonitorJobID;
        private String monitorJobStatus;
        private String prevMonitorJobStatus;
        private Long retryCount;
        private EventData eventCommon;
        private List<EqpMonitorLotEventData> monitorLots;
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
     * @date 2019/7/25 11:31
     */
    @Data
    public static class LotWaferSortEventRecord {

        private String lotID;
        private List<WaferEventData> currentWafers;
        private EventData eventCommon;
        private List<WaferEventData> sourceWafers;
        private String equipmentID;
        private String sorterJobID;
        private String componentJobID;

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
     * @date 2019/7/25 13:29
     */
    @Data
    public static class WaferSortJobEventRecord {
        private EventData eventCommon;
        private String operation;
        private String equipmentID;
        private String portGroupID;
        private String sorterJobID;
        private String sorterJobStatus;
        private boolean waferIDReadFlag;
        private int componentJobCount;
        private String ctrlJobID;
        private List<SortJobComponentEventData> componentJobs;
        private WaferSortJobPostActRecord postActRecord;
        private List<SortJobSlotMapEventData> slotMaps;
    }

    /**
    * description:
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/1 8:25 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/1 8:25 下午
    * @param  ‐
    * @return
    */
    @Data
    public static class WaferSortJobPostActRecord {
        private String sorterJobID;
        private String actionCode;
        private String productOrderID;
        private String vendorID;
        private Integer waferCount;
        private String sourceProductID;
        private String childLotId;
        private String parentLotId;

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
     * @date 2019/7/25 13:30
     */
    @Data
    public static class SortJobSlotMapEventData {
        private String componentJobID;
        private String lotID;
        private String waferID;
        private Integer destinationPosition;
        private Integer sourcePosition;
        private String aliasName;
        private String direction;
        private Integer sortStatus;
        private Timestamp replyTimestamp;
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
     * @date 2019/7/25 15:01
     */
    @Data
    public static class LotWaferStackEventRecord {

        private EventData eventCommon;
        private LotEventData lotData;
        private String bondingGroupID;
        private String bondingCategory;
        private String relatedLotID;
        private String equipmentID;
        private String controlJobID;
        private List<StackWaferEventData> wafers;

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
     * @date 2019/7/25 15:01
     */
    @Data
    public static class StackWaferEventData {

        private String waferID;
        private String originalWaferID;
        private String aliasWaferName;
        private String originalAliasWaferName;
        private Boolean controlWaferFlag;
        private String relatedWaferID;
        private String originalCassetteID;
        private Long originalSlotNumber;
        private String destinationCassetteID;
        private Long destinationSlotNumber;

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @exception
     * @author Ho
     * @date 2019/7/25 16:22
     */
    @Data
    public static class RecipeBodyManageEventRecord {

        private String equipmentID;
        private String actionCode;
        private String machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private Boolean formatFlag;
        private EventData eventCommon;

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
     * @date 2019/7/25 18:00
     */
    @Data
    public static class CollectedDataChangeEventRecord {

        private String lotID;
        private String controlJobID;
        private String dataCollectionDefinitionID;
        private List<ChangedDCData> changedDCDataSeq;
        private EventData eventCommon;

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
     * @date 2019/7/25 18:00
     */
    @Data
    public static class ChangedDCData {

        private String dataCollectionItemName;
        private String previousDataValue;
        private String currentDataValue;

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
     * @date 2019/7/26 10:11
     */
    @Data
    public static class LotFlowBatchEventRecord {

        private LotEventData lotData;
        private String flowBatchID;
        private String eventType;
        private String targetOperationNumber;
        private String targetEquipmentID;
        private String fromFlowBatchID;
        private EventData eventCommon;

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
     * @date 2019/7/26 10:26
     */
    @Data
    public static class AutoDispatchControlEventRecord {

        private String lotID;
        private String action;
        private String routeID;
        private String operationNumber;
        private Boolean singleTriggerFlag;
        private String description;
        private EventData eventCommon;

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
     * @date 2019/7/26 10:52
     */
    @Data
    public static class ControlJobStatusChangeEventRecord {

        private String ctrlJob;
        private String ctrlJobState;
        private String eqpID;
        private String eqpDescription;
        private List<ControlJobStatusChangeLotEventData> lots;
        private EventData eventCommon;

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
     * @date 2019/7/26 10:57
     */
    @Data
    public static class ControlJobStatusChangeLotEventData extends LotAttributeEventData {}

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/26 13:13
     */
    @Data
    public static class PlannedSplitEventRecord {

        private String action;
        private String lotFamilyID;
        private String splitRouteID;
        private String splitOperationNumber;
        private String originalRouteID;
        private String originalOperationNumber;
        private Boolean actionEMail;
        private Boolean actionHold;
        private List<SplitSubRouteEventData> subRoutes;
        private EventData eventCommon;

        //add psmJobID for history
        private String psmJobID;
        //add runCardID for history
        private String runCardID;
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
     * @date 2019/7/26 13:13
     */
    @Data
    public static class SplitSubRouteEventData {

        private String subRouteID;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private String parentLotID;
        private String childLotID;
        private String memo;
        private List<SplitedWaferEventData> wafers;

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
     * @date 2019/7/26 13:14
     */
    @Data
    public static class SplitedWaferEventData {

        private String waferID;
        private String successFlag;

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
     * @date 2019/7/26 13:15
     */
    @Data
    public static class  OhplsphsWafer {
        private String    action_code;
        private String    lotfamily_id;
        private String    split_route_id;
        private String    split_ope_no;
        private String    original_route_id;
        private String    original_ope_no;

        private Integer     seq_no;
        private String    sub_route_id;
        private String    parent_lot_id;
        private String    child_lot_id;
        private String    wafer_id;
        private String    success_flag;
        private String    claim_time;
        //add psmJobID for history
        private String plsplitjob_id;
        //add runCardID for history
        private String runcard_id;
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
     * @date 2019/7/26 13:15
     */
    @Data
    public static class  Ohplsphs {
        private String    action_code;
        private String    lotfamily_id;
        private String    split_route_id;
        private String    split_ope_no;
        private String    original_route_id;
        private String    original_ope_no;
        private String    return_ope_no;
        private String    merge_ope_no;

        private Integer     actionemail;
        private Integer     actionhold;
        private Integer     seq_no;
        private String    sub_route_id;
        private String    parent_lot_id;
        private String    child_lot_id;
        private String    memo;
        private String    claim_time;
        private Double  claim_shop_date;
        private String    claim_user_id;
        private String    claim_memo;
        private String    event_create_time;

        //add psmJobID for history
        private String plsplitjob_id;

        //add runCardID for history
        private String runcard_id;
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
     * @date 2019/7/26 11:03
     */
    @Data
    public static class  OhcjschsLots {
        private String    ctrljob_id;
        private String    lot_id;
        private String    cast_id;
        private String    lot_type;
        private String    sub_lot_type;
        private String    prodspec_id;
        private String    mainpd_id;
        private String    ope_no;
        private String    pd_id;

        private Integer     ope_pass_count;
        private String    pd_name;
        private String    claim_time;
        private String    store_time;
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
     * @date 2019/7/26 10:59
     */
    @Data
    public static class  Ohcjschs {
        private String    ctrljob_id;
        private String    ctrljob_state;
        private String    eqp_id;
        private String    eqp_descripstion;
        private String    claim_time;
        private Double  claim_shop_date;
        private String    claim_user_id;
        private String    claim_memo;
        private String    store_time;
        private String    event_create_time;
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
     * @date 2019/7/26 10:12
     */
    @Data
    public static class Ohflbchhs {
        private String   lot_id;
        private String   lot_type;
        private String   cast_id;
        private String   lot_status;
        private String   customer_id;

        private Integer    priority_class;
        private String   prodspec_id;

        private Integer    original_qty;
        private Integer    cur_qty;
        private Integer    prod_qty;
        private Integer    cntl_qty;
        private String   lot_hold_state;
        private String   bank_id;
        private String   mainpd_id;
        private String   ope_no;
        private String   pd_id;

        private Integer    pass_count;
        private String   wafer_his_time;
        private String   event_type;
        private String   target_ope_no;
        private String   target_eqp_id;
        private String   flowbatch_id;
        private String   fr_flowbatch_id;
        private String   claim_time;
        private Double claim_shop_date;
        private String   claim_user_id;
        private String   claim_memo;
        private String   event_create_time;
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
     * @date 2019/7/25 18:01
     */
    @Data
    public static class Ohcdchghs {
        private String  lotID;
        private String  ctrljob_id;
        private String  dcdef_id;
        private String  dcitem_name;
        private String  pre_dcitem_value;
        private String  cur_dcitem_value;
        private String  claimTime;
        private String  claimUser;
        private String  claimMemo;
        private String  storeTime;
        private String  eventCreateTime;
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
     * @date 2019/7/25 16:23
     */
    @Data
    public static class Ohrcpmhs {
        private String    eqp_id;
        private String    eqp_name;
        private String    action_code;
        private String    recipe_id;
        private String    ph_recipe_id;
        private String    file_location;
        private String    file_name;

        private Integer     format_flag;
        private String    claim_memo;
        private String    store_time;
        private String    event_create_time;
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
     * @date 2019/7/25 13:31
     */
    @Data
    public static class Ohsortjobhs {
        private String  equipmentID;
        private String  portGroupID;
        private String  sorterJobID;
        private String  operation;
        private String  sorterJobStatus;
        private Integer   waferIDReadFlag;
        private String  claimTime;
        private String  claimUser;
        private String  claimMemo;
        private String  storeTime;
        private String  eventCreateTime;
        private String ctrlJobID;
        private Integer componentJobCount;
    }

    @Data
    public static class OhPostAct {
        private String sorterJobID;
        private String actionCode;
        private String productOrderID;
        private String vendorID;
        private Integer waferCount;
        private String sourceProductID;
        private String  claimTime;
        private String  claimUser;
        private String refkey;
        private String childLotId;
        private String parentLotId;
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
     * @date 2019/7/25 13:34
     */
    @Data
    public static class OhsortjobhsSlotmap {
        private String componentJobID;
        private String lotID;
        private String waferID;
        private Integer destinationSlotPosition;
        private Integer sourceSlotPosition;
        private String aliasName;
        private String direction;
        private Integer sorterStatus;
        private String claimTime;
        private String claimUser;
        private String refkey;
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
     * @date 2019/7/25 13:33
     */
    @Data
    public static class OhsortjobhsComponent {
        private String  componentJobID;
        private String  sourceCassetteID;
        private String  destinationCassetteID;
        private String  sourcePortID;
        private String  destinationPortID;
        private String componentJobStatus;
        private String actionCode;
        private String operation;
        private String  claimTime;
        private String  claimUser;
        private String refkey;
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
     * @date 2019/7/25 13:30
     */
    @Data
    public static class SortJobComponentEventData {
        private String componentJobID;
        private String destinationCarrierID;
        private String destinationPortID;
        private String sourceCarrierID;
        private String sourcePortID;
        private String componentJobStatus;
        private String actionCode;
        private String operation;
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
     * @date 2019/7/1 15:02
     */
    @Data
    public static class EqpMonitorLotEventData {
        private String lotID;
        private String productSpecificationID;
        private Long startSeqNo;
        private String mainPDID;
        private String opeNo;
        private String pdID;
        private Long opePassCount;
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
     * @date 2019/6/30 15:20
     */
    @Data
    public static class Oheqpmonhs {
        private String opeCategory           ;
        private String eqpID                 ;
        private String chamberID             ;
        private String eqpMonID              ;
        private String monitorType           ;
        private String monitorStatus         ;
        private String prevMonitorStatus     ;
        private String claimTime             ;
        private String claimUser             ;
        private String claimMemo             ;
        private String storeTime             ;
        private String eventCreateTime       ;
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
     * @date 2019/6/30 15:22
     */
    @Data
    public static class Oheqpmonhs_defaction {
        private String eqpMonID              ;
        private String eventType             ;
        private String action                ;
        private String reasonCode            ;
        private String sysMessageID          ;
        private String claimTime             ;
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
     * @date 2019/6/30 15:26
     */
    @Data
    public static class Oheqpmonhs_defprodspec{
        private String eqpMonID              ;
        private String prodSpecID            ;
        private Long    waferCount;
        private Long    startSeqNo;
        private String claimTime             ;
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
     * @date 2019/6/30 15:28
     */
    @Data
    public static class Oheqpmonhs_schchg {
        private String  eqpMonID              ;
        private String  prevNextExecutionTime ;
        private String  nextExecutionTime     ;
        private String  claimTime             ;
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
     * @date 2019/6/30 15:24
     */
    @Data
    public static class Oheqpmonhs_def {
        private String eqpMonID              ;
        private String description           ;
        private String scheduleType          ;
        private String startTimeStamp        ;
        private Long    executionInterval;
        private Long    warningInterval;
        private Long    expirationInterval;
        private Boolean standAloneFlag;
        private Boolean kitFlag;
        private Long    maxRetryCount;
        private String machineStateAtStart   ;
        private String machineStateAtPassed  ;
        private String machineStateAtFailed  ;
        private String claimTime             ;
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
     * @date 2019/6/30 15:09
     */
    @Data
    public static class EqpMonitorEventRecord {
       private String opeCategory;
       private String equipmentID;
       private String chamberID;
       private String eqpMonitorID;
       private String monitorType;
       private String monitorStatus;
       private String prevMonitorStatus;
       private EventData eventCommon;
       private List<EqpMonitorDefEventData> monitorDefs;
       private List<EqpMonitorDefprodEventData> monitorDefprods;
       private List<EqpMonitorDefactionEventData> monitorDefactions;
       private List<EqpMonitorSchchgEventData> monitorSchchgs;
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
     * @date 2019/7/1 14:08
     */
    @Data
    public static class Oheqpmonjobhs {
        private String opeCategory           ;
        private String eqpID                 ;
        private String chamberID             ;
        private String eqpMonID              ;
        private String eqpMonJobID           ;
        private String monJobStatus          ;
        private String prevMonJobStatus      ;
        private Long    retryCount;
        private String claimTime             ;
        private String claimUser             ;
        private String claimMemo             ;
        private String storeTime             ;
        private String eventCreateTime       ;
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
     * @date 2019/7/1 16:40
     */
    @Data
    public static class Ohfbmchs {
        private String eqp_id             ;
        private Integer maxCount;
        private String claimTime          ;
        private String claimUser          ;
        private String claimMemo          ;
        private String storeTime          ;
        private String eventCreateTime    ;
    }

    /**
     * description:
     * <p>OHAMONJOB_LOT</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/1 14:11
     */
    @Data
    public static class Oheqpmonjobhs_lot {
        private String eqpMonJobID         ;
        private String eventType           ;
        private String lotID               ;
        private String prodSpecID          ;
        private Long    startSeqNo;
        private String claimTime           ;
    }

    /**
     * description:
     * <p>OHADCLOT</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/7/1 14:15
     */
    @Data
    public static class Ohdispctrlhs extends Oheqpmonjobhs {
        private String lotID                 ;
        private String action                ;
        private String routeID               ;
        private String operationNumber       ;
        private Boolean singleTriggerFlag;
        private String description           ;
        private String claimTime             ;
        private String claimUser             ;
        private String claimMemo             ;
        private String storeTime             ;
        private String eventCreateTime       ;
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
     * @date 2019/6/30 15:17
     */
    @Data
    public static class EqpMonitorDefactionEventData {
        private String eventType;
        private String action;
        private String reasonCode;
        private String sysMessageID;
        private String customField;
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
     * @date 2019/6/30 15:19
     */
    @Data
    public static class EqpMonitorSchchgEventData {
        private String prevNextExecutionTime;
        private String nextExecutionTime;
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
     * @date 2019/6/30 15:13
     */
    @Data
    public static class EqpMonitorDefprodEventData {
       private String productSpecificationID;
       private Long waferCount;
       private Long startSeqNo;
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
     * @date 2019/6/30 15:11
     */
    @Data
    public static class EqpMonitorDefEventData {
       private String description;
       private String scheduleType;
       private String startTimeStamp;
       private Long executionInterval;
       private Long warningInterval;
       private Long expirationInterval;
       private Boolean standAloneFlag;
       private Boolean kitFlag;
       private Long maxRetryCount;
       private String machineStateAtStart;
       private String machineStateAtPassed;
       private String machineStateAtFailed;
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
     * @date 2019/6/28 17:20
     */
    @Data
    public static class ChamberStatusChangeEventRecord {
       private String equipmentID;
       private String processResourceID;
       private String processResourceState;
       private String startTimeStamp;
       private String newProcessResourceState;
       private EventData eventCommon;
       private String actualProcessResourceE10State;
       private String actualProcessResourceState;
       private String newActualProcessResourceE10State;
       private String newProcessResourceE10State;
       private String processResourceE10State;
       private String newActualProcessResourceState;
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
     * @date 2019/7/1 15:39
     */
    @Data
    public static class Ohopehs_emucnt {
        private String lot_id          ;
        private String mainpd_id       ;
        private String ope_no          ;
        private Integer ope_pass_count;
        private String ope_category    ;
        private String claim_time      ;
        private String wafer_id        ;
        private Integer eqpmonused_count;
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
     * @date 2019/6/28 17:22
     */
    @Data
    public static class Ohcschs {
        private String eqp_id          ;
        private String eqp_name        ;
        private String procrsc_id      ;
        private String area_id         ;
        private String pr_state        ;
        private String claim_user_id   ;
        private String start_time      ;
        private Double  start_shop_date;
        private String end_time        ;
        private Double  end_shop_date;
        private String new_pr_state    ;
        private String claim_memo      ;
        private String e10_state       ;
        private String act_e10_state   ;
        private String act_chamber_state ;
        private String new_e10_state   ;
        private String new_chamber_state ;
        private String new_act_e10_state   ;
        private String new_act_chamber_state ;
        private String event_create_time   ;
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
     * @date 2019/6/28 16:50
     */
    @Data
    public static class EquipmentAlarmEventRecord {
       private String equipmentID;
       private String stockerID;
       private String AGVID;
       private String alarmCategory;
       private String alarmCode;
       private String alarmID;
       private String alarmText;
       private EventData eventCommon;
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
     * @date 2019/6/28 16:51
     */
    @Data
    public static class Ohalmhs{
        private String eqp_id          ;
        private String eqp_name        ;
        private String stk_id          ;
        private String stk_name        ;
        private String agv_id          ;
        private String alarm_category  ;
        private String alarm_code      ;
        private String alarm_id        ;
        private String alarm_text      ;
        private String claim_time      ;
        private Double  claim_shop_date;
        private String claim_user_id   ;
        private String claim_memo      ;
        private String event_create_time ;
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
     * @date 2019/6/28 15:00
     */
    @Data
    public static class EquipmentModeChangeEventRecord {
       private String equipmentID;
       private String portID;
       private String operationMode;
       private String onlineMode;
       private String dispatchMode;
       private String accessMode;
       private String operationStartMode;
       private String operationCompMode;
       private String description;
       private EventData eventCommon;
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
     * @date 2019/6/28 15:01
     */
    @Data
    public static class Ohemchs {
        private String eqp_id          ;
        private String eqp_name        ;
        private String port_id         ;
        private String area_id         ;
        private String ope_mode        ;
        private String online_mode     ;
        private String disp_mode       ;
        private String access_mode     ;
        private String ope_start_mode  ;
        private String ope_comp_mode   ;
        private String description     ;
        private String claim_time      ;
        private Double  claim_shop_date;
        private String claim_user_id   ;
        private String claim_memo      ;
        private String event_create_time ;
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
     * @date 2019/6/28 14:08
     */
    @Data
    public static class EqpPortStatusChangeEventRecord {
        private String portType;
        private String portID;
        private String equipmentID;
        private String portUsage;
        private String portStatus;
        private String accessMode;
        private String dispatchState;
        private String dispatchTime;
        private String dispatchDurableID;
        private EventData eventCommon;
        private ObjectIdentifier reasonCode;
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
     * @date 2019/6/28 14:11
     */
    @Data
    public static class Oheqpportschs {
        private String port_type;
        private String port_id;
        private String eqp_id;
        private String port_usage;
        private String port_state;
        private String access_mode;
        private String diap_state;
        private String diap_time;
        private String diap_debl_id;
        private String claim_time;
        private String claim_user_id;
        private String claim_memo;
        private String store_time;
        private String event_create_time;
        private String reasonCode;
        private String reasonDescription;
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
     * @date 2019/6/28 13:10
     */
    @Data
    public static class VendorLotEventRecord {
       private LotEventData lotData;
       private String vendorLotID;
       private Long claimQuantity;
       private EventData eventCommon;
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
     * @date 2019/6/28 13:12
     */
    @Data
    public static class Ohvltrhs {
        private String lot_id              ;
        private String lot_type            ;
        private String vendor_lot_id       ;
        private String vendor_name         ;
        private String prod_type           ;
        private String prodspec_id         ;
        private String prodgrp_id          ;
        private String tech_id             ;
        private Integer     wafer_qty;
        private String claim_time          ;
        private Double  claim_shop_date;
        private String claim_user_id       ;
        private String claim_memo          ;
        private String store_time          ;
        private String event_create_time   ;
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
     * @date 2019/6/25 13:49
     */
    @Data
    public static class LotBankMoveEventRecord {
       private LotEventData lotData;
       private String previousBankID;
       private EventData eventCommon;
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
     * @date 2019/6/27 11:18
     */
    @Data
    public static class EquipmentStatusChangeEventRecord {
       private String equipmentID;
       private String stockerID;
       private String equipmentState;
       private String operationMode;
       private String startTimeStamp;
       private String newEquipmentState;
       private String newOperationMode;
       private EventData eventCommon;
       private String E10State;
       private String actualE10State;
       private String actualEquipmentState;
       private String newActualE10State;
       private String newActualEquipmentState;
       private String newE10State;
        private ObjectIdentifier reasonCode;
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
     * @date 2019/6/27 11:19
     */
    @Data
    public static class Oheschs {
        private String eqp_id          ;
        private String eqp_name        ;
        private String stk_id          ;
        private String stk_name        ;
        private String area_id         ;
        private String eqp_state       ;
        private String ope_mode        ;
        private String claim_user_id   ;
        private String start_time      ;
        private Double  start_shop_date;
        private String end_time        ;
        private Double  end_shop_date;
        private String new_eqp_state   ;
        private String new_ope_mode    ;
        private String claim_memo      ;
        private String e10_state       ;
        private String act_e10_state   ;
        private String act_equipment_state ;
        private String new_e10_state   ;
        private String new_equipment_state ;
        private String new_act_e10_state   ;
        private String new_act_equipment_state ;
        private String event_create_time   ;
        private String reasonCode;
        private String reasonDescription;
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
     * @date 2019/6/25 16:33
     */
    @Data
    public static class LotChangeEventRecord {
       private String lotID;
       private String lotStatus;
       private Long externalPriority;
       private String lotOwnerID;
       private String lotComment;
       private String orderNumber;
       private String customerID;
       private Long priorityClass;
       private String productID;
       private String previousProductID;
       private String stageID;
       private String planStartTime;
       private String planCompTime;
       private EventData eventCommon;
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
     * @date 2019/6/24 15:15
     */
    @Data
    public static class ProductRequestEventRecord {
        private String lotID;
        private String lotType;
        private String subLotType;
        private String routeID;
        private Long productQuantity;
        private String planStartTime;
        private String planCompTime;
        private String lotGenerationType;
        private String lotScheduleMode;
        private String lotIDGenerationMode;
        private String productDefinitionMode;
        private Long externalPriority;
        private Long priorityClass;
        private String productID;
        private String lotOwnerID;
        private String orderNumber;
        private String customerID;
        private String lotComment;
        private EventData eventCommon;
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
     * @date 2019/6/24 15:19
     */
    @Data
    public static class Ohprqhs {
        private String lot_id          ;
        private String lot_type        ;
        private String sub_lot_type    ;
        private String mainpd_id       ;
        private String claim_time      ;
        private Double  claim_shop_date;
        private String claim_user_id   ;
        private String ope_category    ;
        private String prod_type       ;
        private Integer prod_qty;
        private String mfg_layer       ;
        private String plan_start_time ;
        private String plan_end_time   ;
        private String lot_gen_type    ;
        private String lot_sch_mode    ;
        private String lot_gen_mode    ;
        private String prod_def_mode   ;
        private Integer ext_priority;
        private Integer priority_class;
        private String prodspec_id     ;
        private String lot_owner_id    ;
        private String prodgrp_id      ;
        private String tech_id         ;
        private String custprod_id     ;
        private String order_no        ;
        private String customer_id     ;
        private String start_bank_id   ;
        private String lot_comment     ;
        private String claim_memo      ;
        private String event_create_time ;
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
     * @date 2019/6/24 15:22
     */
    @Data
    public static class Frprodreq {
        private String subLotType;
        private String customerID;
        private String deliveryTime;
        private String lotComment;
        private String lotGenMode;
        private String lotGenType;
        private String lotOwner;
        private String mfgLayer;
        private String orderNO;
        private String planReleaseTime;
        private Integer priorityClass;
        private String productMode;
        private String scheduleMode;
        private Integer schedulePriority;
        private String startBankID;
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
     * @date 2019/5/31 16:18
     */
    @Data
    public static class LotHoldEventRecord {
        private LotEventData lotData;
        private List<LotHoldEventData> holdRecords;
        private ObjectIdentifier releaseReasonCodeID;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private EventData eventCommon;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
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
     * @date 2019/5/31 16:21
     */
    @Data
    public static class LotHoldEventData {
        private Boolean movementFlag;
        private Boolean changeStateFlag;
        private String holdType;
        private ObjectIdentifier holdReasonCodeID;
        private String holdUserID;
        private String holdTimeStamp;
        private Boolean responsibleOperationFlag;
        private String holdClaimMemo;
        private Boolean responsibleOperationExistFlag;
        private String departmentNamePlate;
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
     * @date 2019/5/31 16:24
     */
    @Data
    public static class ObjectIdentifier {
        private String identifier;
        private String stringifiedObjectReference;
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
     * @date 2019/5/11 15:18
     */
    @Data
    public static class ProcessedLotEventData {
        private String processLotID;
        private String processRouteID;
        private String processOperationNumber;
        private Long processOperationPassCount;
        private String processObjrefPO;
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
     * @date 2019/5/11 15:18
     */
    @Data
    public static class CollectedDataEventRecord {
        private LotEventData measuredLotData;
        private String monitorGroupID;
        private List<ProcessedLotEventData> processedLots;
        private String equipmentID;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private EventData eventCommon;
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
     * @date 2019/3/29 10:22
     */
    @Data
    public static class QTimeEventRecord {
        private String lotID;
        private String waferID;
        private String originalQTime;
        private String qTimeType;
        private String processDefinitionLevel;
        private String opeCategory;
        private String triggerMainProcessDefinitionID;
        private String triggerOperationNumber;
        private String triggerBranchInfo;
        private String triggerReturnInfo;
        private String triggerTimeStamp;
        private String targetMainProcessDefinitionID;
        private String targetOperationNumber;
        private String targetBranchInfo;
        private String targetReturnInfo;
        private String targetTimeStamp;
        private String previousTargetInfo;
        private String control;
        private Boolean watchdogRequired;
        private Boolean actionDone;
        private Boolean manualCreated;
        private Boolean preTrigger;
        private List<QTimeActionEventData> actions;
        private EventData eventCommon;
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
     * @date 2019/3/29 10:34
     */
    @Data
    public static class QTimeActionEventData {
        private String targetTimeStamp;
        private String action;
        private String reasonCode;
        private String actionRouteID;
        private String operationNumber;
        private String timing;
        private String mainProcessDefinitionID;
        private String messageDefinitionID;
        private String customField;
        private Boolean watchdogRequired;
        private Boolean actionDone;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/27 16:54:10
     */
    @Data
    public static class LotOperationCompleteEventRecord {
        private LotEventData lotData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<String> reticleIDs;
        private List<String> fixtureIDs;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Integer previousOperationPassCount;
        private String previousObjrefPOS;
        private String batchID;
        private String controlJobID;
        private Boolean locateBackFlag;
        private Boolean testCriteriaResult;
        private EventData eventCommon;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private List<WaferLevelRecipeEventData> waferLevelRecipe;
        private List<WaferPassCountNoPreviousEventData> processWafers;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/27 17:02:16
     */
    @Data
    public static class WaferPassCountNoPreviousEventData {
        private String waferID;
        private Boolean currentOperationFlag;
        private Integer passCount;
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
     * @date 2019/6/14 10:33
     */
    @Data
    public static class ProcessResourceWaferPositionEventRecord {
        private String lotID;
        private String waferID;
        private String controlJobID;
        private String mainPDID;
        private String opeNo;
        private Long opePassCount;
        private String equipmentID;
        private String processResourceID;
        private String waferPosition;
        private String processTime;
        private EventData eventCommon;
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
     * @date 2019/6/14 11:33
     */
    @Data
    public static class WaferChamberProcessEventRecord {
        private String waferID;
        private String lotID;
        private String routeID;
        private String opeNo;
        private Long passCount;
        private String equipmentID;
        private String processResourceID;
        private String processTime;
        private EventData eventCommon;
        private String actionCode;
        private String controlJobID;
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
     * @date 2019/6/14 11:36
     */
    @Data
    public static class Ohwcphs {
        private String wafer_id         ;
        private String lot_id           ;
        private String mainpd_id        ;
        private String ope_no           ;
        private Integer ope_pass_count;
        private String eqp_id           ;
        private String eqp_name         ;
        private String procrsc_id       ;
        private String proc_time        ;
        private Double  proc_shop_date;
        private String claim_time       ;
        private Double  claim_shop_date;
        private String claim_user_id    ;
        private String claim_memo       ;
        private String store_time       ;
        private String action_code      ;
        private String event_create_time ;
        private String ctrl_job         ;
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
     * @date 2019/6/14 10:37
     */
    @Data
    public static class Ohprwphs {
        private String wafer_id         ;
        private String lot_id           ;
        private String ctrljob_id       ;
        private String mainpd_id        ;
        private String ope_no           ;
        private Integer ope_pass_count;
        private String eqp_id           ;
        private String eqp_name         ;
        private String procrsc_id       ;
        private String wafer_position   ;
        private String proc_time        ;
        private Double  proc_shop_date;
        private String claim_time       ;
        private Double  claim_shop_date;
        private String claim_user_id    ;
        private String claim_memo       ;
        private String store_time       ;
        private String event_create_time ;
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
     * @date 2019/5/13 13:42
     */
    @Data
    public static class FrpoSmpl {
        private String wafer_id        ;
        private Boolean       smpl_flg;
        private String prcs_job        ;
        private String prcs_position   ;
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
     * @date 2019/5/13 13:18
     */
    @Data
    public static class OhcdatahsData {
        private String meas_lot_id          ;
        private String meas_mainpd_id       ;
        private String meas_ope_no          ;
        private Integer meas_pass_count      ;
        private String claim_time           ;
        private String meas_dcdef_id        ;
        private String dcitem_name          ;
        private String dcmode               ;
        private String dcunit               ;
        private String data_type            ;
        private String item_type            ;
        private String meas_type            ;
        private String wafer_id             ;
        private String wafer_position       ;
        private String site_position        ;
        private String dcitem_value         ;
        private String specchk_result       ;
        private String act_code             ;
        private Double scrn_upper_limit     ;
        private Double spec_upper_limit     ;
        private Double ctrl_upper_limit     ;
        private Double dcitem_target        ;
        private Double ctrl_lower_limit     ;
        private Double spec_lower_limit     ;
        private Double scrn_lower_limit     ;
        private String prcs_job             ;
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
     * @date 2019/5/13 10:49
     */
    @Data
    public static class Ohcdatahs {
        private String meas_lot_id          ;
        private String meas_lot_type        ;
        private String meas_prodspec_id     ;
        private String meas_prodgrp_id      ;
        private String meas_tech_id         ;
        private String monitor_grp_id       ;
        private String meas_mainpd_id       ;
        private String meas_ope_no          ;
        private String meas_pd_id           ;
        private String meas_pd_type         ;
        private Integer meas_pass_count      ;
        private String meas_pd_name         ;
        private String meas_area_id         ;
        private String meas_eqp_id          ;
        private String meas_eqp_name        ;
        private String meas_lc_recipe_id    ;
        private String meas_recipe_id       ;
        private String meas_ph_recipe_id    ;
        private String claim_time           ;
        private Double claim_shop_date      ;
        private String claim_user_id        ;
        private String meas_dcdef_id        ;
        private String meas_dc_type         ;
        private String meas_dcspec_id       ;
        private String meas_wfrhs_time      ;
        private String event_create_time    ;
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
     * @date 2019/5/11 16:13
     */
    @Data
    public static class OhcdatahsAction {
        private String meas_lot_id         ;
        private String meas_mainpd_id      ;
        private String meas_ope_no         ;
        private Integer meas_pass_count;
        private String claim_time          ;
        private String meas_dcdef_id       ;
        private String meas_dcspec_id      ;
        private String check_type          ;
        private String reason_code         ;
        private String act_code            ;
        private String proc_lot_id         ;
        private Boolean mlot_flg;
        private String  proc_mainpd_id      ;
        private String  proc_ope_no         ;
        private String  proc_pd_id          ;
        private Integer proc_pass_count;
        private String proc_eqp_id         ;
        private String proc_recipe_id      ;
        private String proc_fab_id         ;
        private String bank_id             ;
        private String rework_route_id     ;
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
     * @date 2019/5/11 16:16
     */
    @Data
    public static class OhcdatahsActionEntity {
        private String meas_lot_id         ;
        private String meas_mainpd_id      ;
        private String meas_ope_no         ;
        private Integer meas_pass_count;
        private String claim_time          ;
        private String meas_dcdef_id       ;
        private String check_type          ;
        private String reason_code         ;
        private String act_code            ;
        private String proc_lot_id         ;
        private String proc_mainpd_id      ;
        private String proc_ope_no         ;
        private Integer proc_pass_count;
        private String class_name          ;
        private String entity_id           ;
        private String entity_attrib       ;
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
     * @date 2019/5/13 10:07
     */
    @Data
    public static class OhcdatahsLot {
        private String proc_lot_id         ;
        private String proc_lot_type       ;
        private String monitor_grp_id      ;
        private String proc_prodspec_id    ;
        private String proc_prodgrp_id     ;
        private String proc_tech_id        ;
        private String customer_id         ;
        private String proc_custprod_id    ;
        private String meas_lot_id         ;
        private String meas_mainpd_id      ;
        private String meas_ope_no         ;
        private Integer meas_pass_count     ;
        private String claim_time          ;
        private Double claim_shop_date     ;
        private String claim_user_id       ;
        private String proc_mainpd_id      ;
        private String proc_ope_no         ;
        private String proc_pd_id          ;
        private Long proc_pass_count     ;
        private String proc_pd_name        ;
        private String proc_area_id        ;
        private String proc_eqp_id         ;
        private String proc_eqp_name       ;
        private String proc_lc_recipe_id   ;
        private String proc_recipe_id      ;
        private String proc_ph_recipe_id   ;
        private String ctrl_job            ;
        private String proc_time           ;
        private Timestamp proc_shop_date      ;
        private String proc_wfrhs_time     ;
        private String store_time          ;
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
     * @date 2019/5/13 10:13
     */
    @Data
    public static class Frpo {
        private String  pd_id               ;
        private String  asgn_eqp_id         ;
        private String  asgn_lcrecipe_id    ;
        private String  asgn_recipe_id      ;
        private String  asgn_phrecipe_id    ;
        private String  ctrljob_id          ;
        private String  actual_end_time     ;
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
     * @date 2019/5/11 17:27
     */
    @Data
    public static class OhcdatahsSpec {
        private String meas_lot_id          ;
        private String meas_mainpd_id       ;
        private String meas_ope_no          ;
        private Integer meas_pass_count      ;
        private String claim_time           ;
        private String meas_dcdef_id        ;
        private String meas_dcdef_desc      ;
        private String meas_dc_type         ;
        private String meas_dcspec_id       ;
        private String meas_dcspec_desc     ;
        private String dcitem_name          ;
        private Integer scrn_upper_req       ;
        private Double scrn_upper_limit     ;
        private String scrn_upper_actions   ;
        private Integer spec_upper_req       ;
        private Double spec_upper_limit     ;
        private String spec_upper_actions   ;
        private Integer cntl_upper_req       ;
        private Double cntl_upper_limit     ;
        private String cntl_upper_actions   ;
        private Integer cntl_lower_req       ;
        private Double cntl_lower_limit     ;
        private String cntl_lower_actions   ;
        private Integer spec_lower_req       ;
        private Double spec_lower_limit     ;
        private String spec_lower_actions   ;
        private Integer scrn_lower_req       ;
        private Double scrn_lower_limit     ;
        private String scrn_lower_actions   ;
        private Double dcitem_target        ;
        private String dcitem_tag           ;
        private String dcspec_group         ;
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
     * @date 2019/5/11 16:20
     */
    @Data
    public static class FrpoActions {
        private String lot_id          ;
        private Boolean mlot_flg;
        private String dcdef_id        ;
        private String dcspec_id       ;
        private String check_type      ;
        private String reason_code     ;
        private String act_code        ;
        private String proc_mainpd_id  ;
        private String proc_ope_no     ;
        private String proc_pd_id      ;
        private Integer proc_pass_count;
        private String proc_eqp_id     ;
        private String proc_recipe_id  ;
        private String proc_fab_id     ;
        private String bank_id         ;
        private String rework_route_id ;
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
     * @date 2019/5/11 16:22
     */
    @Data
    public static class FrpoActionsEntity {
        private String class_name      ;
        private String entity_id       ;
        private String entity_attrib   ;
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
     * @date 2019/6/11 14:49
     */
    @Data
    public static class Frwafer {
        private String productID;
        private Integer badDiceQty;
        private Integer goodDiceQty;
        private Integer repairedDiceQty;
        private String alias_wafer_name    ;
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
     * @date 2019/6/11 14:45
     */
    @Data
    public static class Ohwlths {
        private String wafer_id            ;
        private String cur_lot_id          ;
        private String cur_cast_id         ;
        private String cur_cast_category   ;
        private Integer cur_cast_slot_no;
        private String claim_user_id       ;
        private String claim_time          ;
        private Double  claim_shop_date;
        private String ope_category        ;
        private String apply_wafer_flag    ;
        private String prodspec_id         ;
        private Integer good_unit_count;
        private Integer repair_unit_count;
        private Integer fail_unit_count;
        private String exist_flag          ;
        private Boolean control_wafer;
        private String prev_lot_id         ;
        private String prev_cast_id        ;
        private String prev_cast_category  ;
        private Integer prev_cast_slot_no;
        private String reason_code         ;
        private String reason_description  ;
        private String org_wafer_id        ;
        private String org_prodspec_id     ;
        private String alias_wafer_name    ;
        private String event_create_time   ;
        private String equipmentID         ;
        private String sorterJobID         ;
        private String componentJobID      ;
        private String org_alias_wafer_name;
        private String related_wafer_id    ;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 16:06:46
     */
    @Data
    public static class Ohopehs {
        private String lot_id       ;
        private String lot_type     ;
        private String sub_lot_type ;
        private String cast_id      ;
        private String cast_category;
        private String mainpd_id    ;
        private String ope_no       ;
        private String pd_id        ;

        private Integer ope_pass_count;
        private String pd_name   ;
        private String hold_state;
        private String claim_time;
        Double  claim_shop_date;
        private String claim_user_id;
        private String move_type    ;
        private String ope_category ;
        private String prod_type    ;
        private String test_type    ;
        private String mfg_layer    ;

        private Integer ext_priority;
        private Integer priority_class;
        private String prev_prodspec_id;
        private String prodspec_id     ;
        private String prodgrp_id      ;
        private String tech_id         ;
        private String customer_id     ;
        private String custprod_id     ;
        private String order_no        ;
        private String stage_id        ;
        private String stagegrp_id     ;
        private String photo_layer     ;
        private String location_id     ;
        private String area_id         ;
        private String eqp_id          ;
        private String eqp_name        ;
        private String ope_mode        ;
        private String lc_recipe_id    ;
        private String recipe_id       ;
        private String ph_recipe_id    ;

        private Integer reticle_count;
        private Integer fixture_count;
        private Integer rparm_count;
        private Integer init_hold_flag;
        private Integer last_hldrel_flag;
        private String  hold_time;
        private Double hold_shop_date;
        private String hold_user_id      ;
        private String hold_type         ;
        private String hold_reason_code  ;
        private String hold_reason_desc  ;
        private String reason_code       ;
        private String reason_description;
        private String bank_id           ;
        private String prev_bank_id      ;
        private String prev_mainpd_id    ;
        private String prev_ope_no       ;
        private String prev_pd_id        ;

        private Integer prev_pass_count;
        private String prev_pd_name    ;
        private String prev_photo_layer;
        private String prev_stage_id   ;
        private String prev_stagegrp_id;
        private String flowbatch_id    ;
        private String prcss_grp       ;
        private String ctrl_job        ;

        private Integer rework_count;
        private Integer org_wafer_qty;
        private Integer cur_wafer_qty;
        private Integer prod_wafer_qty;
        private Integer cntl_wafer_qty;
        private Integer claim_prod_qty;
        private Integer claim_cntl_qty;
        private Integer total_good_unit;
        private Integer total_fail_unit;
        private String lot_owner_id ;
        private String plan_end_time;
        private String wfrhs_time   ;

        private Integer criteria_flag  ;

        private String claim_memo        ;
        private String rparm_change_type ;
        private String event_create_time ;
        private String hold_ope_no       ;
        private String hold_reason_ope_no;
        private String originalFabID     ;
        private String destinationFabID  ;
        private String relatedLotID      ;
        private String bondingGroupID    ;
        private String eqpMonJobID       ;
        private String pd_type           ;
        private String prev_pd_type      ;

        private String dpt_name_plate    ;
        private String mon_grp_id        ;

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
     * @date 2019/3/29 14:54
     */
    @Data
    public static class Ohqtimehs {
        private String qtime_type         ;
        private String lot_id             ;
        private String wafer_id           ;
        private String org_qtime          ;
        private String pd_level           ;
        private String ope_category       ;
        private String trigger_mainpd_id  ;
        private String trigger_ope_no     ;
        private String trigger_branch_info;
        private String trigger_return_info;
        private String trigger_time       ;
        private String target_mainpd_id   ;
        private String target_ope_no      ;
        private String target_branch_info ;
        private String target_return_info ;
        private String target_time        ;
        private String prev_target_info   ;
        private String control            ;
        private Boolean watchdog_req;
        private Boolean action_done_flag;
        private Boolean manual_created_flag;
        private Boolean pre_trigger_flag;
        private String claim_time       ;
        private String claim_user_id    ;
        private String claim_memo       ;
        private String store_time       ;
        private String event_create_time;
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
     * @date 2019/3/29 14:59
     */
    @Data
    public static class OhqtimehsAction {
        private String lot_id      ;
        private String wafer_id    ;
        private String org_qtime   ;
        private String target_time ;
        private String action      ;
        private String reason_code ;
        private String ope_no      ;
        private String timing      ;
        private String mainpd_id   ;
        private String msgdef_id   ;
        private String custom_field;
        private Boolean watchdog_req;
        private Boolean action_done_flag;
        private String claim_time     ;
        private String action_route_id;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:01:41
     */
    @Data
    public static class Frpd {
        private String department;
        private String operationName;
        private String startBankID;
        private String testType;
        private String pd_type;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:03:10
     */
    @Data
    public static class Frlot {
        private String customerID;
        private String prodspec_id;
        private String lotOwner;
        private String mfgLayer;
        private String orderNO;
        private String planEndTime;
        private String prodReqObj;
        private Integer priority;
        private Integer priorityClass;
        private String subLotType;
        private String vendor_name;
        private String lotType;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:06:25
     */
    @Data
    public static class Frpos {
        private String operationNO;
        private String pdID;
        private String photoLayer;
        private String stageID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:09:13
     */
    @Data
    public static class OhopehsReticle {
        private String lot_id   ;
        private String mainpd_id;
        private String ope_no   ;
        private Integer ope_pass_count;
        private String ope_category;
        private String claim_time  ;
        private String reticle_id  ;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:10:29
     */
    @Data
    public static class OhopehsFixture {
        private String lot_id   ;
        private String mainpd_id;
        private String ope_no   ;
        private Integer ope_pass_count;
        private String ope_category;
        private String claim_time  ;
        private String fixture_id  ;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:14:39
     */
    @Data
    public static class OhopehsRparm {
        private String lot_id   ;
        private String mainpd_id;
        private String ope_no   ;
        private Integer ope_pass_count;
        private String ope_category;
        private String claim_time  ;
        private String rparm_name  ;
        private String rparm_value ;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:17:17
     */
    @Data
    public static class OhopehsRparmWafer {
        private String lot_id;
        private String wafer_id;
        private String mainpd_id;
        private String ope_no;
        private Integer ope_pass_count;
        private String claim_time;
        private String ope_category;
        private String machine_recipe_id;
        private String rparm_name;
        private String rparm_value;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 17:19:49
     */
    @Data
    public static class OhopehsWafersampling {
        private String lot_id;
        private String mainpd_id;
        private String ope_no;
        private Integer ope_pass_count;
        private String ope_category;
        private String claim_time;
        private String smpl_wafer_id;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 11:30:27
     */
    @Data
    public static class WaferLevelRecipeEventData {
        private String waferID;
        private String machineRecipeID;
        private List<WaferRecipeParmEventData> waferRecipeParameters;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 11:32:24
     */
    @Data
    public static class WaferRecipeParmEventData {
        private String parameterName;
        private String parameterValue;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 11:28:28
     */
    @Data
    public static class EventData {
        private String transactionID;
        private String eventTimeStamp;
        private Double eventShopDate;
        private String userID;
        private String eventMemo;
        private String eventCreationTimeStamp;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 11:25:32
     */
    @Data
    public static class RecipeParmEventData {
        private String parameterName;
        private String parameterValue;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 11:25:37
     */
    @Data
    public static class LotEventData {
        private String lotID;
        private String lotType;
        private String cassetteID;
        private String lotStatus;
        private String customerID;
        private Long priorityClass;
        private String productID;
        private Integer originalWaferQuantity;
        private Integer currentWaferQuantity;
        private Integer productWaferQuantity;
        private Integer controlWaferQuantity;
        private String holdState;
        private String bankID;
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Integer operationPassCount;
        private String objrefPOS;
        private String waferHistoryTimeStamp;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
        private List<String> samplingWafers;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/25 14:30:34
     */
    @Data
    public static class UserDataSet {
        private String name;
        private String type;
        private String value;
        private String originator;
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
     * @date 2019/6/5 14:07
     */
    @Data
    public static class LotOperationMoveEventRecord {
        private LotEventData lotData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<String> reticleIDs;
        private List<String> fixtureIDs;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String batchID;
        private String controlJobID;
        private Boolean locateBackFlag;
        private Boolean testCriteriaResult;
        private EventData eventCommon;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private ProcessOperationEventData oldCurrentPOData;
        private List<WaferPassCountEventData> processWafers;
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
     * @date 2019/7/29 13:55
     */
    @Data
    public static class BackupOperationEventRecord {

        private LotEventData lotData;
        private String request;
        private String hostName;
        private String serverName;
        private String itDaemonPort;
        private String entryRouteID;
        private String entryOperationNumber;
        private String exitRouteID;
        private String exitOperationNumber;
        private EventData eventCommon;

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
     * @date 2019/7/29 14:16
     */
    @Data
    public static class BackupChannelEventRecord {

        private String request;
        private String categoryLevel;
        private String categoryID;
        private String routeID;
        private String operationNumber;
        private String hostName;
        private String serverName;
        private String itDaemonPort;
        private String entryRouteID;
        private String entryOperationNumber;
        private String exitRouteID;
        private String exitOperationNumber;
        private String state;
        private String startTime;
        private String endTime;
        private EventData eventCommon;

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
     * @date 2019/7/29 14:29
     */
    @Data
    public static class BondingGroupEventRecord {

        private EventData eventCommon;
        private String bondingGroupID;
        private String action;
        private String bondingGroupStatus;
        private String equipmentID;
        private String controlJobID;
        private List<BondingMapEventData> bondingMapInfos;

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
     * @date 2019/7/29 14:29
     */
    @Data
    public static class BondingMapEventData {

        private String bondingGroupID;
        private String action;
        private Long bondingSeqNo;
        private String baseLotID;
        private String baseProductID;
        private String baseWaferID;
        private String baseBondingSide;
        private String topLotID;
        private String topProductID;
        private String topWaferID;
        private String topBondingSide;

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
     * @date 2019/7/29 14:49
     */
    @Data
    public static class OwnerChangeEventRecord {

        private String fromOwnerID;
        private String toOwnerID;
        private List<OwnerChangeObjectEventData> changeObjects;
        private EventData eventCommon;

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
     * @date 2019/7/29 14:50
     */
    @Data
    public static class OhowchhsChgobj {
        private String  fromOwnerID;
        private String  toOwnerID;
        private String  objectName;
        private String  hashedInfo;
        private String  claimTime;
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
     * @date 2019/7/29 14:50
     */
    @Data
    public static class Ohowchhs {
        private String  fromOwnerID;
        private String  toOwnerID;
        private String  claimTime;
        private String  claimUser;
        private String  claimMemo;
        private String  storeTime;
        private String  eventCreateTime;
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
     * @date 2019/7/29 14:49
     */
    @Data
    public static class OwnerChangeObjectEventData {

        private String objectName;
        private String hashedInfo;

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
     * @date 2019/7/29 14:33
     */
    @Data
    public static class OhbgrphsMap {
        private String    bondingGroupID;
        private String    action;
        private Integer     bondingSeqNo;
        private String    baseLotID;
        private String    baseProductID;
        private String    baseWaferID;
        private String    baseBondingSide;
        private String    topLotID;
        private String    topProductID;
        private String    topWaferID;
        private String    topBondingSide;
        private String    claim_time;
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
     * @date 2019/7/29 14:31
     */
    @Data
    public static class Ohbgrphs {
        private String    bondingGroupID;
        private String    action;
        private String    bondingGroupStatus;
        private String    equipmentID;
        private String    controlJobID;
        private String    tx_id;
        private String    claim_time;
        private String    claim_user_id;
        private String    claim_memo;
        private String    storeTime;
        private String    event_create_time;
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
     * @date 2019/7/29 14:17
     */
    @Data
    public static class Ohbucnhs {
        private String   category_level;
        private String   category_id;
        private String   route_id;
        private String   ope_no;
        private String   host_name;
        private String   server_name;
        private String   it_daemon_port;
        private String   entry_route_id;
        private String   entry_ope_no;
        private String   exit_route_id;
        private String   exit_ope_no;
        private String   state;
        private String   start_time;
        private String   end_time;
        private String   ope_category;
        private String   claim_time;
        private Double claim_shop_date;
        private String   claim_user_id;
        private String   event_memo;
        private String   event_create_time;
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
     * @date 2019/7/29 13:55
     */
    @Data
    public static class Ohbuophs {
        private String   lot_id;
        private String   lot_type;
        private String   cast_id;
        private String   cast_category;
        private String   mainpd_id;
        private String   ope_no;
        private String   pd_id;

        private Integer    ope_pass_count;
        private String   pd_name;
        private String   claim_time;
        private Double claim_shop_date;
        private String   claim_user_id;
        private String   ope_category;
        private String   host_name;
        private String   server_name;
        private String   it_daemon_port;
        private String   entry_route_id;
        private String   entry_ope_no;
        private String   exit_route_id;
        private String   exit_ope_no;
        private String   prodspec_id;
        private String   customer_id;
        private String   stage_id;
        private String   stagegrp_id;
        private String   hold_state;
        private String   bank_id;

        private Integer    org_wafer_qty;
        private Integer    cur_wafer_qty;
        private Integer    prod_wafer_qty;
        private Integer    cntl_wafer_qty;
        private String   lot_owner_id;
        private String   plan_end_time;
        private String   wfrhs_time;
        private String   event_memo;
        private String   event_create_time;
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
     * @date 2019/6/3 17:51
     */
    @Data
    public static class OhopehsRwkcnt {
        private String lot_id          ;
        private String mainpd_id       ;
        private String ope_no          ;
        private Integer ope_pass_count;
        private String ope_category    ;
        private String claim_time      ;
        private String wafer_id        ;
        private Integer rework_count;
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
     * @date 2019/6/3 13:39
     */
    @Data
    public static class LotReworkEventRecord {
        private LotEventData lotData;
        private ObjectIdentifier reasonCodeID;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private Long reworkCount;
        private EventData eventCommon;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private ProcessOperationEventData oldCurrentPOData;
        private List<WaferReworkCountEventData> reworkWafers;
        private List<WaferPassCountEventData> processWafers;
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
     * @date 2019/6/6 16:52
     */
    @Data
    public static class LotWaferMoveEventRecord {
        private LotEventData destinationLotData;
        private List<NewWaferEventData> currentWafers;
        private List<SourceLotEventData> sourceLots;
        private EventData eventCommon;
        private List<NewWaferEventData> sourceWafers;
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
     * @date 2019/6/6 16:55
     */
    @Data
    public static class SourceLotEventData {
        private LotEventData sourceLotData;
        private List<WaferEventData> sourceWafers;
        private List<WaferEventData> currentWafers;
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
     * @date 2019/6/6 16:57
     */
    @Data
    public static class WaferEventData {
        private String waferID;
        private String originalWaferID;
        private Boolean controlWaferFlag;
        private String originalCassetteID;
        private Long originalSlotNumber;
        private String destinationCassetteID;
        private Long destinationSlotNumber;
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
     * @date 2019/6/6 16:53
     */
    @Data
    public static class NewWaferEventData {
        private String waferID;
        private Boolean controlWaferFlag;
        private Long slotNumber;
        private String originalWaferID;
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
     * @date 2019/6/3 16:53
     */
    @Data
    public static class ProcessOperationEventData {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private String objrefPOS;
        private Long operationPassCount;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
        private List<String> samplingWafers;
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
     * @date 2019/6/3 16:58
     */
    @Data
    public static class WaferPassCountEventData {
        private String waferID;
        private Long previousPassCount;
        private Long passCount;
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
     * @date 2019/6/3 16:55
     */
    @Data
    public static class WaferReworkCountEventData {
        private String waferID;
        private Long reworkCount;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/27 17:24:48
     */
    @Data
    public static class OhopehsPasscnt {
        private String lot_id;
        private String mainpd_id;
        private String ope_no;
        private String claim_time;
        private Integer ope_pass_count;
        private String move_type;
        private String ope_category;
        private String wafer_id;
        private Integer pass_count;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/27 17:30:03
     */
    @Data
    public static class Frlrcp {
        private String test_type;
    }

    @Data
    public static class DurableCleanJobStatusChangeEventRecord {
        private String durableID;
        private String durableType;
        private String action;
        private String jobStatus;
        private String statusChangeTime;
        private String process;
        private String route;
        private String step;
        private String operationNo;
        private String eqpID;
        private String chamberID;
        private EventData eventCommon;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author salt
     * @date 2020/1/9 17:30:03
     */
    @Data
    public static class ReticleEventOperationalData {
        private String id;
        private String reticleId;
        private String reticleObj;
        private String reticleType;
        private String reticleStatus;
        private String reticleGrade;
        private String reticleLocation;
        private String opeCategory;
        private String reticlePodId;
        private String inspectionType;
        private String eqpId;
        private String stocerId;
        private String reasonCode;
        private String txId;
        private String xferStatus;
        private String eventTime;
        private Double eventShop;
        private String claimUserId;
        private String claimMemo;
        private Timestamp eventCreateTime;
        private String dObjmanager;
        private Boolean historyApplied;
        private Boolean cfmApplied;
        private String reticleSubStatus;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author salt
     * @date 2020/1/9 17:30:03
     */
    @Data
    public static class ReticleHistory {
        private String reticleId;
        private String reticleObj;
        private String reticleType;
        private String reticleStatus;
        private String reticleGrade;
        private String reticleLocation;
        private String opeCategory;
        private String claimUser;
        private String reticlePodId;
        private Timestamp claimTime;
        private String inspectionType;
        private String claimMemo;
        private String eqpId;
        private String stockerId;
        private String reasonCode;
        private String transactionId;
        private String xferStatus;
        private String reticleSubStatus;
    }

    @Data
    public static class Output {
        private Response response;
        private String refkey;
    }

    @Data
    public static class LayoutRecipeEventRecord{
		private String eqpId;
		private String eqpObj;
		private String machineRecipeID;
		private String machineRecipeObj;
		private String replaceRecipeId;
		private String replaceRecipeObj;
		private Integer lowerLimit;
		private Integer upperLimit;
		private String memo;
		private String operationMemo;
		private Timestamp createdTime;
		private String createdUser;
		private String category;
		private Integer lastLowerLimit;
		private Integer lastUpperLimit;
		private String lastReplaceRecipeId;
		private String lastReplaceRecipeObj;
		private String lastMemo;
		private String lastOperationMemo;
		private String transactionID;
    }

    @Data
    public static class LotMonitorGroupEventRecord {
        private static final long serialVersionUID = 7061446986760668983L;
        private EventData eventData;
        private String lotId; // lot id
        private String lotObj; // lot id
        private String operationId; // 工步id
        private String operationObj; // 工步id
        private String operationNumber; // 工步编号
        private String monitorGroupId; // monitor group id
        private String processFlowId; // flow id
        private Integer operationPassCount; // pass count
        private String operationType;  // 工步类型
        private String lotType; // lot 类型
        private String subLotType; // lot 子类型
        private String carrierId; // carrier Id
        private String carrierCategory; // carrier 类型
        private String operationName; // 工步名称
        private String holdState; // 是否hold
        private String productType; // 产品类型
        private String productId; // 产品 id
        private String technologyId; // technology id
        private Integer lotPriority; // lot 优先级
        private String mfgLayer; // mfg
        private String productFmlyId; // group id
        private String stageId; // stage id
        private String stageGroupId; // stage group
        private String lotOwnerId; // user
    }

}
