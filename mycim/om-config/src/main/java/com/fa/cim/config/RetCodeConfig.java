package com.fa.cim.config;

import com.fa.cim.common.support.OmCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * description:
 *      This Class is Reference to the cim-code.properties, when we add one data in cim-code.properties,
 *      We must add the define in the ReCodeConfig Class, too. their reference satisfy the following rules. for example.
 *      cim-code.properties                 RetCodeConfig
 *      rc.succ = 0                                     private CimCode succ            // when we start the service, the succ's value is 0.
 *      rc.not_found_bank = 1422                        private CimCode notFoundBank;   // when we start the service, the notFoundBank's value is 1422.
 *
 *      note: The statement of constants can't be modified, only the constants value could be modified.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Bear         create file
 *
 * @author: Bear
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@PropertySource("classpath:cim-code.properties")
@ConfigurationProperties(prefix="rc")
@Setter
@Getter
public class RetCodeConfig {
    public static final int SUCCESS_CODE = 0;
    public static final int WARNING_CODE = 1;
    public static final int ERROR_CODE = 2;
    public static final int SYSTEM_ERROR = 2037;

    private OmCode succ;                          //succ = (0, "succ")
    private OmCode warn;                          //warn = (1, "%s")
    private OmCode error;                         //error = (2, "%s")

    /************************************************ Fields (1 - 200) ************************************************/
    private OmCode sqlNotFound;                             // sql_not_found = (100, "sql not found")

    private OmCode allCassetteInventoryDataError;
    /**
     * all_cassette_failed = (103, "All carrier Failed")
     */
    private OmCode allCassetteFailed;

    /**
     * all_data_val_asterisk = (105, "All Data Val Asterisk")
     */
    private OmCode allDataValAsterisk;
    /**
     * all_data_value_blank = (106,"All Data Value Blank")
     */
    private OmCode allDataValueBlank;
    private OmCode allScraped;                            //all_scraped = (107, "all scraped")
    private OmCode allAlreadyProcessed;                  //all_already_processed = (112, "all already processed")
    private OmCode alreadyReservedCst;                    // 113
    private OmCode systemError;                             // system_error = (2037, "system error")
    private OmCode alreadyExistMonitorGroup;              //already_exist_monitor_group = (115， "already exist monitor group")
    private OmCode alreadyDispatchReservedCassette;      //already_dispatch_reserved_cassette = (116, "already dispatch reserved carrier")
    private OmCode alreadyReservedPortGroup;                 //already_reserved_port_group = (117, "already reserved port group")
    private OmCode alreadyReservedLoadPort;               //already_reserved_load_port = (120, "already reserved load port")
    /**
     * all_request_fail = (123,"All Request Fail")
     */
    private OmCode allRequestFail;
    /**
     * already_xfer_reserved_cassette = (124, "Already Xfer Reserved carrier")
     */
    private OmCode alreadyXferReservedCassette;
    private OmCode apcServerBindFail;                       //add_to_queue_fail = (127, "add to queue fail")
    private OmCode apcRecipeparameterreqError;                       //rc.apc_recipeparameterreq_error = (133, "apc recipeparameterreq error")
    private OmCode apcReturnDuplicateParametername;                       //rc.apc_return_duplicate_parametername = (134, "apc return duplicate parametername")
    private OmCode apcRuntimecapabilityError;                       //rc.apc_runtimecapability_error = (139, "apc runtimecapability error")
    private OmCode addToQueueFail;                       //rc.apc_server_bind_fail = (140, "apc server bind fail")
    private OmCode administrationNotAuthrize;            //rc.administration_not_authrize = (143, "administration not authrize")
    private OmCode actionCodeNotSame;            //rc.action_code_not_same = (145, "actioncode not same")
    private OmCode allWaferDestCastNotSame;               //rc.all_wafer_dest_cast_not_same = (150, "all wafer dest cast not same")
    private OmCode allWaferDestPortNotSame;               //rc.all_wafer_dest_port_not_same = (151, "all wafer dest port not same")
    private OmCode authServerBindFail;                    //rc.auth_server_bind_fail = (155, "auth server bind fail")
    private OmCode alreadyReservedCassetteSlm;            //already_reserved_cassette_slm = (152, "already reserved carrier FMC")
    private OmCode attributeDifferentForMerge;        // attribute_different_for_merge = (159, "attribute different for merge")
    private OmCode attributeReservedDctrljob;          // attribute_reserved_dctrljob = (161, "LoadPort[%s] already has reserved DurableControlJob.")
    private OmCode cstNotInLoader;                      //rc.cst_not_in_loader = (187, "cst not in loader")
    private OmCode bankinOperation;                      //bankin_operation = (201, "bank in operation")
    private OmCode bomNotDefined;                           //bom_not_defined = (208, "bom not defined")
    private OmCode bondGroupInvalidEqp;                   //bond_group_invalid_eqp = (219, "bond group invalid eqp")
    private OmCode invalidLotProcessState;                // invalid_lot_process_state = (269, "Lot %s processStatus %s is invalid for this request.")
    private OmCode connotMergeFuturehold;             //connot_merge_futurehold = (301, "A child lot has valid FutureHold and you cannot merge it.")
    private OmCode cannotPassOperation;                //rc.cannot_pass_operation = (302, "cannot pass operation")
    private OmCode connotSplitHeldlot;                  // connot_split_heldlot = (303, "connot split heldlot")
    private OmCode cassetteCategoryMismatch;            //cassette_category_mismatch = (304,"carrier category mismatch")
    private OmCode castHasAnyLots;            //rc.cast_has_any_lots = (308,"cast has any lots")
    private OmCode castIsEmpty;                 //rc.cast_is_empty=(309,"The specified carrier %s is empty.")
    private OmCode castNotEmpty;                         // rc.cast_not_empty = (310, "cast not empty")
    private OmCode countMismatch;            //count_mismatch = (314, "count mismatch")
    private OmCode countUnderZero;            //count_under_zero = (315, "count under zero")
    private OmCode castControlJobIdBlank;                 //rc.cast_control_job_id_blank = (328, "cast control job id blank")
    private OmCode cassetteControlJobFilled;             //cassette_control_job_filled = (329, "carrier control job filled")
    private OmCode cassetteControlJobMix;                 //cassette_control_job_mix = (330, "carrier control job mix")
    private OmCode cassetteEquipmentConditionError;     //cassette_equipment_condition_error = (331, "carrier condition error")
    private OmCode cassetteNotInLoader;                   //cassette_not_in_loader = (317, "carrier not in loader")
    private OmCode cassetteNotInProcessing;               //cassette_not_in_processing = (318, "carrier not in pprocessing")
    private OmCode cassetteNotInUnloader;              //cassette_not_in_unloader = (319, "carrier not in unloader")
    private OmCode currentToperationEarly;          // current_toperation_early = (320, "current toperation early")
    private OmCode currentToperationLate ;          // current_toperation_late = (321, "current toperation late")
    private OmCode currentToperationSame ;          // current_toperation_same = (322, "current toperation same")
    private OmCode currentStateSame;                     // curr_state_same = (323, "current state same")
    private OmCode cannotMergeHeldlotInbank;       //  cannot_merge_heldlot_inbank = (326, "cannot merge heldlot inbank")
    private OmCode cstAlreadyLoaded;                      // rc.cst_already_loaded = (316, "cst already loaded")
    private OmCode canNotReceiveVendorLotInBank;          //can_not_receive_vendor_lot_in_bank = (327, "can't receive supplier lot in bank")
    private OmCode castPortCtrljobCountUnmatch;          //rc.cast_port_ctrljob_count_unmatch = (332, "cast port ctrljob count unmatch")
    private OmCode cassettePortControlJobUnMatch;      //cassette_port_control_job_un_match = (333, "carrier port control job unmatch")
    private OmCode cassetteDifferentForMerge;      //cassette_different_for_merge = (334, "carrier different for merge")
    private OmCode cassetteNotSame;                     //cassette_not_same = (335, "carrier not same")
    private OmCode cassetteSlotNotBlank;              //cassette_slot_not_blank = (336, "carrier slot not blank")
    private OmCode cassetteLotRelationWrong;      //cassette_lot_relation_wrong = (337, "carrier lot relation wrong")
    private OmCode cassetteExist;                 //cassette_exist = (338, "carrier exist")
    private OmCode cassetteEquipmentDifferent;    //cassette_equipment_different = (339, "carrier eqp different")
    private OmCode cassetteNotOnEquipment;        //cassette_not_on_equipment = (340, "carrier not on eqp")
    private OmCode cannotGetStartinfo ;     // cannot_get_startinfo = (342, "cannot get startinfo")
    private OmCode changedToEiByOtherOperation;       //changed-to-ei-by-other-operation = (396, "changed to ei by other operation")
    private OmCode cannotVendorLotReturn;         //cannot_vendor_lot_return = (341, "can't material return")
    private OmCode castResvedForAnotherEqp; // cast_resved_for_another_eqp = (343, "cast resved for another eqp")
    private OmCode cannotHoldWithNpbh;            //cannot_hold_with_npbh = (344, "can't hold with npbh")
    private OmCode cannotHoldreleaseWithNpbh; //cannot_holdrelease_with_npbh = (345, "can't hold release withc npbh")
    private OmCode cannotHoldreleaseWithNpbr;     //cannot_holdrelease_with_npbr = (346, "can't hold release withc npbr")
    private OmCode cannotSetLcSkipToLastOpe; //cannot_set_lc_skip_to_last_ope = (347, "cannot set lc skip to last ope")
    /**
     * category_inconsistency = (349, "Category Inconsistency")
     */
    private OmCode categoryInconsistency;
    private OmCode cannotChangePassword;      //cannot_change_password = (351, "cannot change password")
    private OmCode cannotHoldreleaseWithBohl; //cannot_holdrelease_with_bohl = (354, "can't hold release withc bohl")
    private OmCode cannotHoldreleaseWithBohr; //cannot_holdrelease_with_bohr = (355, "can't hold release withc bohr")
    private OmCode cannotHoldWithBohl;            //cannot-hold-with-bohl= = (357, "can't hold with bohl")
    /**
     * control_job_lot_unmatch = (361, "control Job lot Unmatch")
     */
    private OmCode controlJobLotUnmatch;
    private OmCode ctrljobLotUnmatch;            //ctrljob-lot-unmatch= = (361, "ctrljob lot unmatch")
    private OmCode cannotMergeDifferentProduct;   // cannot_merge_different_product= (362, "cannot merge different product")
    private OmCode cannotHoldReleaseWithLocr;    //cannot-hold-release-with-locr = (364, "con,t hold release with locr")
    private OmCode cannotNonOroBankOutWithBohr;//cannot_non_pro_bank_out_with_bohr = (356, "can not pro bank out with bohr")
    private OmCode cannotHoldReleaseForLock; //cannot_hold_release_for_lock = (365, "con,t hold release for lock")
    private OmCode cannotBankShipped;              //cannot_bank_shipped = (366, "can't bank shipped")
    private OmCode castForceLoaded;             //cast_force_loaded = (367, "cast force loaded")
    private OmCode calledFromInvalidTransaction;  //called_from_invalid_transaction = (368, "called from invalid transaction")
    private OmCode cdataDeleted;  //rc.cdata_deleted = (369, "cdata deleted")
    private OmCode carrierNotTransfering;         //carrier_not_transfering = (371, "carrier not transfering")
    private OmCode msgNotFoundPd;                 //msg_not_found_pd = (373, "not found pd")
    /**
     * control_job_eqp_unmatch = (377, "Control Job Eqp Unmatch")
     */
    private OmCode controlJobEqpUnmatch;
    private OmCode cassetteInPostProcess;         //cassette_in_post_process = (378, "carrier in post process")
    private OmCode cassetteNotInPostProcess;      //cassette_not_in_post_process = (379, "carrier not in post process")
    /**
     * ctrljob_eqpctnpst_unmatch = (382, "The combination of control job [%s] / wafer [%s] in equipment container is not valid.")
     */
    private OmCode ctrljobEqpctnpstUnmatch;
    private OmCode castNotLoadedDestPort;       //rc.cast_not_loaded_dest_port =(385, "cast not loaded dest port")
    private OmCode childLotNotEmptied;           //  child_lot_not_emptied = (387, "child lot not emptied")
    /**
     * can_not_registered_sm_data = (388, Can Not Registered SM Data)
     */
    private OmCode canNotRegisteredMDSData;
    /**
     * can_not_delete_sm_data = (389, Can Not Delete SM Data)
     */
    private OmCode canNotDeleteMDSData;
    /**
     * can_not_mod_sm_data = (390, Can Not Mod SM Data)
     */
    private OmCode canNotModMDSData;
    private OmCode chamberNotAvailableForLot;    //chamber_not_available_for_lot = (394, "chamber not available for lot")
    private OmCode loadCastNotNpwReserved;        //load_cast_not_npw_reserved = (395, "load cast not npw reserved")
    private OmCode datavalCannotConvToInt;      //dataval_cannot_conv_to_int = (401, "Reported data is not of correct type.")
    private OmCode duplicateBankHold;             //duplicate_bank_hold = (404, "duplicate bank hold")
    private OmCode duplicateFtholdEntry;          //duplicate_fthold_entry = (405, "duplicate future hold entry")
    private OmCode duplicateLot;                      //duplicate_lot = (406, "dupplicate lot")
    /**
     * duplicate_process_hold_entry = (408, "Duplicate Process Hold Entry")
     */
    private OmCode duplicateProcessHoldEntry;
    private OmCode duplicateWafer;                  // duplicate_wafer = (410, "duplicate wafer")
    /**
     * duplicate_inhibit = (414,"Duplicate Inhibit")
     */
    private OmCode duplicateInhibit;
    private OmCode duplicateFamily;                // duplicate_family = (423, "duplicate family")
    private OmCode dcdefDcspecMismatch;                // rc.dcdef_dcspec_mismatch = (424, "dcdef dcspec mismatch")
    private OmCode duplicateValuesInInput;        // duplicate_values_in_input = (430, "duplicate values in input")
    private OmCode duplicateEqpAuto3Setting;        // duplicate_eqp_setting = (431, "duplicate eqp auto3setting")
    private OmCode diffEquipmentMonitorJobEquipmentID; //diff_equipment_monitor_job_equipment_id = (435, "diff with auto monitor job and eqp id")
    private OmCode durableControlJobFilled;   //(438,"DurableControlJobID is not blank.")
    private OmCode durableControlJobBlank;   //(439,"DurableControlJobID is blank.")
    private OmCode durableEQPDrbCtrljobUnmatch;   //(440,"[%s]'s durable control job and Equipment's durable control job is not same.")
    private OmCode durableOnroute; //rc.durable_onroute = (441, "durable on process")
    private OmCode durableNotOnroute; //rc.durable_not_onroute = (442, "durable_not_onroute")
    private OmCode durableInPostProcess;   // (443,"Durable [%s] is in post process.")
    private OmCode notFoundPfForDurable;
    private OmCode durableCannotStatechangeOnfloor; //rc.durable_cannot_statechange_onfloor = (451, "durable cannot statechange onfloor")
    private OmCode durableNotAvailableStateForLotProcess; //durable_not_available_state_for_lot_process = (452, "durable not available state for lot process")
    private OmCode durableSubstateNotBlank; //rc.durable_substate_not_blank = (454, "durable substate not blank")
    private OmCode durableNotAvailableStateForDrblProcess;       // durable_not_available_state_for_drbl_process = (453, "Specified durable %s cannot enable the durable process in current durable status/sub-status.")
    /**
     * durable_stat_changed_by_other_operation = (455, "durable stat changed by other operation")
     */
    private OmCode durableStatChangedByOtherOperation;
    /**
     * durable_new_substat_blank = (457, "durable new substat blank")
     */
    private OmCode durableNewSubstatBlank;
    /**
     * durable_stat_substat_unmatch = (458, "durable stat substat unmatch : %s %s")
     */
    private OmCode durableStatSubstatUnmatch;
    private OmCode notFoundLogicRecipe;               //not_found_logic_recipe = (495, "not found logic recipe")
    private OmCode eqpAlreadyReserved;               //rc.eqp_already_reserved = (501, "eqp already reserved")
    private OmCode equipmentCastUnmatch;            // equipment_cast_unmatch = (503, "equipment_cast_unmatch")
    private OmCode eqpFlowbatchNotReqd;            // rc.eqp_flowbatch_not_reqd = (504, "eqp flowbatch not reqd")
    private OmCode eqpNotRequiredReticle;            // rc.eqp_not_required_reticle = (510, "eqp not required reticle")
    private OmCode equipmentProcessDurableFixtRequired;  //equipment_process_durable_fixt_required = (516, "eqp process durable fixt required")
    private OmCode equipmentProcessDurableNotRequired;  // equipment_process_durable_not_required = (517,"eqp process durable not required")
    private OmCode equipmentProcessDurableReticleRequired;               //equipment_process_durable_reticle_required=(519, "eqp process durable reticle required")
    private OmCode existProductRequest;           //exist_product_request = (527, "exist product order")
    private OmCode equipmentNotRequiredFixture;           //rc.equipment_not_required_fixture = (529, "eqp not required tool")
    /**
     * eqp_rcpflag_off = (531, "Equipment does not have recipe body manage function. ")
     */
    private OmCode eqpRcpflagOff;
    private OmCode equipmentNotAvailableStatForLot;   //rc.equipment_not_available_stat_for_lot = (532, "eqp not available state for lot")
    private OmCode equipmentNotAvailableStat;    //equipment_not_available_stat = (534, "eqp not available state")
    private OmCode equipmentNotReservedForControlJob; //equipment_not_reserved_for_control_job = (537, "eqp not reserved for controlJob")
    /**
     * equipment_reserved_for_other_flow_batch = (538,"eqp Reserved For Other Flow Batch")
     */
    private OmCode equipmentReservedForOtherFlowBatch;
    /**
     * equipment_not_reserved_for_flow_batch = (539, "eqp Not Reserved For Flow Batch")
     */
    private OmCode equipmentNotReservedForFlowBatch;
    /**
     * equipment_reserved_flow_batch_id_not_same = (542, "eqp Reserved Flow Batch Id Not Same")
     */
    private OmCode equipmentReservedFlowBatchIdNotSame;
    /**
     * equipment_of_cassette_not_same = (543,"eqp Of carrier Not Same")
     */
    private OmCode equipmentOfCassetteNotSame;
    private OmCode extServiceBindFail;      //ext_service_bind_fail = (545, "Binding to external server (TMS, EAP, SPC, or BRS) is failed. Please wait for a while and try it again. If you face same error, please call system administrator. ")
    private OmCode extServiceNilObj;      //ext_service_nil_obj = (546, "Orbix returns nil object at binding to external server (TMS, EAP, SPC, or BRS). Please wait for a while and try it again. If you face same error, please call system administrator. ")
    private OmCode equipmentCategoryTransactionIdMismatch; //equipment_category_transaction_id_mismatch = (548, "eqp category tx id mismatch")
    /**
     * equipment_type_not_early_cast_out = (550,"eqp Type Not Early Cast Out")
     */
    private OmCode equipmentTypeNotEarlyCastOut;
    private OmCode equipmentPortReservedForAnotherCast;   // equipment_port_reserved_for_another_cast = (551, "equipment_port_reserved_for_another_cast")
    /**
     * extpostproc_executed = (565, "xxxxxxxxxxxxxxxxxxxxxx")
     */
    private OmCode extpostprocExecuted;
    private OmCode exceedExpirationTime;            //exceed_expiration_time = (568, "The exceed expiration time")
    private OmCode eqpmonitorWaferNotEnough;               // eqpmonitor_wafer_not_enough = (570, "The total wafer count of auto monitor lots is shorter than required count.")
    /**
     * eqp_online_mode = (556, "Eqp Online Mode")
     */
    private OmCode eqpOnlineMode;

    /**
     *  flow_batch_lots_missing = (605, "Some lots in the flowBatch are missing in the starting lots.")
     */
    private OmCode flowBatchLotsMissing;

    /**
     * flow_batch_not_reserved_for_eqp = (606, "Flow Batch Not Reserved For Eqp")
     */
    private OmCode flowBatchNotReservedForEqp;
    /**
     * flow_batch_removed = (608, "Flow Batch Removed")
     */
    private OmCode flowBatchRemoved;
    /**
     * flow_batch_reserved_for_other_eqp = (609, "Flow Batch Reserved For Other Eqp")
     */
    private OmCode flowBatchReservedForOtherEqp;
    /**
     * flow_batch_reserved_eqp_id_blank = (610, "Flow Batch Reserved Eqp Id Blank")
     */
    private OmCode flowBatchReservedEqpIdBlank;
    /**
     * flow_batch_reserved_eqp_id_filled = (611, "Flow Batch Reserved Eqp Id Filled")
     */
    private OmCode flowBatchReservedEqpIdFilled;
    /**
     * flow_batch_reserved_eqp_id_not_same = (612, "Flow Batch Reserved Eqp Id Not Same")
     */
    private OmCode flowBatchReservedEqpIdNotSame;
    private OmCode notFutureholdInLocate;      //not_futurehold_in_locate = (617 ,"not_futurehold_in_locate")
    private OmCode futureHoldInBranch;             //future_hold_in_branch = (616, "future hold in branch")
    private OmCode futureholdOnBranch;  // futurehold_on_branch = (618 ,"futurehold on branch")
    private OmCode notFutureholdInSplit;   // not_futurehold_in_split = (619 ,"not futurehold in split")
    private OmCode futureholdReservedUntilJoinpoint;  //futurehold_reserved_until_joinpoint = (620, "future holdreserved until joinpoint")
    private OmCode foundScrap;                        //found-scrap = (623, "found scrap")
    private OmCode failMakeHistory;     //624

    private OmCode flowBatchReservedEqpNotTargetEqp;// flow_batch_reserved_eqp_not_target_eqp = (626, "Reserved Equipment [%s] of FlowBatch [%s] is not Target Equipment.")
    private OmCode msgNotFoundPf;                     //msg_not_found_pf = (629, "msg not found pf")
    private OmCode ftholdNotFound;                //fthold_not_found = (636, "future hold not found")
    /**
     * fpc_delete_error = (637, "This DOC information [%s] can NOT be deleted from the registration.")
     */
    private OmCode fpcDeleteError;
    /**
     * fpc_notavailable_error = (638, "The action was rejected because DOC Available Flag is not available.")
     */
    private OmCode fpcNotavailableError;
    /**
     * fpc_category_mismatch = (639, "The DOC Category of each Process Condition are not consistent.Please reconfirm it and try again.")
     */
    private OmCode fpcCategoryMismatch;
    /**
     * fpc_whitedefinition_existence = (640, "Some dynamic mode exist at the specified process condition.Please check these process condition items and try again.%s")
     */
    private OmCode fpcWhitedefinitionExistence;
    /**
     * fpc_cannot_skip_operation = (641, "Cannot skip this operation because it is a mandatory operation.\nPlease check the Skip option of DOC definition and try again.")
     */
    private OmCode fpcCannotSkipOperation;
    /**
     * fpc_update_error = (642, "The specified DOC information can NOT be updated. %s")
     */
    private OmCode fpcUpdateError;
    /**
     * fpc_type_mismatch = (645, "The definition DOC information have different FPCType. Please check the DOC definition.")
     */
    private OmCode fpcTypeMismatch;
    /**
     * fpc_route_info_error = (646, "The specified process or process Operation information is invalid. Please check these items.")
     */
    private OmCode fpcRouteInfoError;
    /**
     * fpc_duplicate_wafer = (647, "The some duplicate wafers exist. Please check the each wafer ID.")
     */
    private OmCode fpcDuplicateWafer;
    /**
     * fpc_recipe_param_error = (648, "The some specified Recipe Parameter Names invalid. Please check the defined DOC information on process Operation.")
     */
    private OmCode fpcRecipeParamError;
    /**
     * fpc_dcspecitem_error = (649, "The some specified DC Spec Items are invalid. Please check the defined DOC information on process Operation.")
     */
    private OmCode fpcDcspecitemError;
    /**
     * fpc_wafer_mismatch_in_fpc_group = (650, "The each wafers in same DOC Group are mismatch, or same wafer is in different DOC Group.")
     */
    private OmCode fpcWaferMismatchInFpcGroup;
    /**
     * fpc_info_not_found=(651,"The specified DOC information is not found. [%s]")
     */
    private OmCode fpcInfoNotFound;
    /**
     * fpc_updated_by_another=(652,"The DOC information is updated by another. [%s]")
     */
    private OmCode fpcUpdatedByAnother;
    /**
     * fpc_multipul_recipe_error=(653,"The carrier [%s] is not transferring. ")
     */
    private OmCode fpcMultipulRecipeError;
    /**
     * fpc_invalid_group_number = (654, "The DOC GroupNo in the DOC information is out of range.")
     */
    private OmCode fpcInvalidGroupNumber;
    /**
     * fpc_groupno_and_type_mismatch = (655, "The FPCType and the DOC GroupNo in the DOC information are mismatch.")
     */
    private OmCode fpcGroupnoAndTypeMismatch;
    /**
     * fpc_dcdef_required_for_dcspec = (656, "The DCDefID required in the DOC information when the DCSpecID specified in it.")
     */
    private OmCode fpcDcdefRequiredForDcspec;
    /**
     * fpc_eqpid_required_for_rcp_param = (657, "The EquipmentID required in the DOC information when the recipeParameterChangeType specified in it.")
     */
    private OmCode fpcEqpidRequiredForRcpParam;
    /**
     * fpc_invalid_rcp_paramchange = (658, "The recipe parameter change setting has some inconsistency. Please check the DOC information.")
     */
    private OmCode fpcInvalidRcpParamchange;
    /**
     * fpc_invalid_dcdef_change = (659, "The DCSpec item change setting has some inconsistency. Please check the DOC information.")
     */
    private OmCode fpcInvalidDcdefChange;
    /**
     * fpc_not_uniqueset_in_operation = (660, "LotFamily, HoldLot Option and Send e-mail Option in the DOC information must have unique setting in a process-operation.")
     */
    private OmCode fpcNotUniquesetInOperation;
    /**
     * fpc_not_uniqueset_in_group = (661, "The Skip Option in the DOC information must have unique setting in a DOC Group.")
     */
    private OmCode fpcNotUniquesetInGroup;
    /**
     * fpc_invalid_restrict_setting = (662, "The Equipment restrict setting has some inconsistency. Please check the DOC information.")
     */
    private OmCode fpcInvalidRestrictSetting;
    /**
     * fpc_duplicate_eqp_in_group = (663, "The EquipmentID (or EquipmentID not defined record) cannot be duplicate in a DOC Group.")
     */
    private OmCode fpcDuplicateEqpInGroup;

    private OmCode fpcLotExistCurrentOpeWithOnhold;
    /**
     * fpc_lot_exist_current_ope_with_not_onhold = (666, "There are some target lots on a current operation.")
     */
    private OmCode fpcLotExistCurrentOpeWithNotOnhold;
    /**
     * fpc_dcdef_dcspec_mismatch = (667, "DC Definition [%s] and DC Specification [%s] are under inconsistency situation. Please confirm DOC information. %s ")
     */
    private OmCode fpcDcdefDcspecMismatch;
    /**
     * fpc_require_start_reserve = (669, "The Requested operation cannot be done without Start Reservation. Because the lot %s is DOC target lot.")
     */
    private OmCode fpcRequireStartReserve;
    /**
     * fpc_activeversion_error = (670, "Active version cannot be used for DOC information.")
     */
    private OmCode fpcActiveversionError;
    /**
     * fpc_invalid_dcinfo = (671, "Delta EDC Definition or Delta EDC Specification is specified. %s Please check the DOC information.")
     */
    private OmCode fpcInvalidDcinfo;
    /**
     * fpc_mrecipe_set_error = (673, "Machine recipe is NOT specified in spite of setting equipment.")
     */
    private OmCode fpcMrecipeSetError;
    private OmCode foundInRdj;             //rc.found_in_rdj = (675, "found in rdj")
    private OmCode functionDisable;                 // function_disable = (679, "function disable")
    private OmCode holdRecordOfChildOwn;          // hold_record_of_child_own = (802, "hold record of child own")
    private OmCode holdRecordCannotInherit;    // hold_record_cannot_inherit = (803, "hold record cannot inherit")
    private OmCode lotCarrierCategoryUnmatch;        //lot_carrier_category_unmatch = (878,"lot Carrier Type unmatch")
    private OmCode carrierPortCarrierCategoryUnmatch;   //carrier_port_carrier_category_unmatch = (879,"carrier port Carrier Type unmatch")
    private OmCode invalidBankData;                //invalid_bank_data = (901, "invalid bank data")
    private OmCode invalidBankType;             //invalid_bank_type = (902, "invalid bank type")
    private OmCode invalidCassettePosition;       //invalid_cassette_position = (903, "invalid carrier position")
    private OmCode invalidCassetteState;          //invalid_cassette_state = (904, "invalid carrier state")
    private OmCode invalidCassetteTransferState;  //invalid_cassette_transfer_state = (905, "The transfer status [%s] of carrier [%s] is invalid, Or carrier has another transporting reservation.")
    private OmCode invalidDataCombinAtion;        // rc.invalidDataCombinAtion = (907, "invalid Data Combin Ation")
    private OmCode invalidDataContents;         // invalid_data_contents = (908, "invalid Data Contents")
    private OmCode invalidEmptyCount;          //rc.invalid_empty_count = (910, "invalid empty count")
    private OmCode invalidEquipmentMode;          //rc.invalid_equipment_mode = (911, "invalid eqp mode")
    private OmCode invalidEquipmentStatus;        // rc.invalid_equipment_stat = (912, "invalid eqp status")
    private OmCode invalidEventRecordType;            //invalid_event_record_type = (913, "invalid event record type")
    private OmCode invalidFinishedStat;               //invalid_finished_stat = (914, "invalid finished stat")
    private OmCode invalidFixtureStat;               //invalid_fixture_stat = (915, "The tool %s status %s is invalid for the request")
    private OmCode invalidFixtureXferStat;           //invalid_fixture_xfer_stat = (916, "The tool %s transfer status %s is invalid for the request.")
    private OmCode invalidFromEqpMode;           //invalid_from_eqp_mode = (917,"invalid from eqp mode")
    private OmCode invalidFtholdType;              //invalid_fthold_type = (921, "invalid future hold type")
    private OmCode invalidInputLotCount;              //invalid_input_lot_count = (922, "invalid input lot count")
    private OmCode invalidInventoryState;             //invalid_inventory_stat = (924, "Inventory Status %s is invalid for the request.")
    private OmCode invalidLoadingPort;              // invalid_loading_port = (925, "invalid loading port")
    private OmCode invalidLoadingSequence;            //invalid_loading_sequence = (926, "invalid loading sequence")
    private OmCode invalidLotControlUseStat;          //invalid_lot_control_use_stat = (927, "invalid lot control use stat")
    private OmCode invalidLotContents;                //invalid_lot_contents = (928, "invalid lot contents")
    private OmCode invalidLotFinishStat;              //invalid_lot_finish_stat = (930, "invalid lot finish stat")
    private OmCode invalidLotHoldStat;                    //.invalid_lot_hold_stat = (931, "invalid lot hold stat")
    private OmCode invalidLotInventoryStat;               //invalid_lot_inventory_stat = (932, "invalid lot inventory stat")
    private OmCode invalidLotProcstat;                //invalid-lot-procstat = (933, "invalid lot procstat")
    private OmCode invalidLotProductionState;         //invalid_lot_production_state = (934, "invalid lot production state")
    private OmCode invalidLotStat;                    //invalid_lot_stat = (936, invalid lot stat)
    private OmCode invalidLotType;                    //invalid_lot_type = (937, "invalid lot type")
    private OmCode invalidLotXferstat;                //invalid_lot_xferstat = (938, "The current Lot %s transfer status %s is invalid for this request.")
    private OmCode invalidInputParam;                     //invalid_input_param = (923, "invalid input param")
    private OmCode invalidModeTransition;                       //invalid_mode_transition = (941, "invalid mode transition")
    private OmCode invalidPassword;                       //invalid_password = (945, "invalid password")
    private OmCode invalidPortState;                      //invalid_port_state = (946, "invalid port state")
    private OmCode invalidPortUsage;                // invalid_port_usage = (947, "invalid port usage")
    private OmCode invalidProdId;                            //invalid_prod_id = (950, "invalid prod id")
    private OmCode invalidPurposeType;                            //rc.invalid_purpose_type = (954, "invalid purpose type")
    private OmCode invalidReticleStat;                            //rc.invalid_reticle_stat = (957, "invalid reticle stat")
    /**
     * invalid_reticle_xfer_Stat = (958, "The reticle %s transfer status %s is invalid for the request. ")
     */
    private OmCode invalidReticleXferStat;
    private OmCode invalidStateTrans;             //invalid_state_trans = (961, "invalid state transfer")
    private OmCode invalidStateTransition;             //invalid_state_transition = (962, "invalid state transition")
    private OmCode invalidTransferState;          // (970,"")
    private OmCode invalidWaferState ;            // invalid_wafer_state = (973, "Status of wafer %s is invalid for the operation.")
    private OmCode invalidStockerType;             //rc.invalid_stocker_type = (964, "invalid stocker type")
    private OmCode invalidCastDispatchStat;   //invalid_cast_dispatch_stat = (975, invalid cast dispatch stat)
    private OmCode lotIsNotAtRoute;            //lot_is_not_at_route = (976,"Lot is not at Route can not do split.")
    /**
     * inhibit_lot = (979, "Inhibit lot")
     */
    private OmCode inhibitLot;
    private OmCode inhibitEntity;   //rc.inhibit_entity = (980, "inhibit entity")
    private OmCode inifileOpenErr;                    //rc.inifile_open_err = (981, "inifile open err")
    private OmCode invalidBankStb;                    //invalid_bank_stb = (982, "invalid bank stb")
    private OmCode invalidRecoverStateType;           //rc.invalid_recover_state_type = (985, "invalid recover state type")
    /**
     * invalid_data_value = (988,"Invalid Data Value")
     */
    private OmCode invalidDataValue;
    /**
     * invalid_request_for_stocker = (986,"Invalid Request For stocker")
     */
    private OmCode invalidRequestForStocker;
    private OmCode lotAlreadyHold;            //lot_already_hold = (1204, "lot already hold")
    private OmCode lotCastUnmatch;            //lot_cast_unmatch = (1211, "lot cast unmatch");
    /**
     *lot_flow_batch_mismatch = (1212, "lot Flow Batch Mismatch")
     */
    private OmCode lotFlowBatchMismatch;
    private OmCode lotNotHeld;                //lot_not_held = (1213, "lot not hold")
    private OmCode lotNotInBank;              //lot_not_in_bank = (1214, "lot not in bank")
    private OmCode lotRemoveFromBatch;   //lot_remove_from_batch = (1221 ,"lot remove from batch")
    private OmCode lotOnRoute;                //  lot_on_route = (1227, "lot on process")
    private OmCode lotControlJobIdBlank;    //  lot_control_job_id_blank = (1228, "lot controlJobID blank")
    private OmCode lotControlJobidFilled; // lot_control_jobid_filled = (1229, "The lot %s has control job ID %s.")
    private OmCode lotPortControlJobUnMatch;  //lot_port_control_job_un_match = (1231, ""lot port control job unmatch)
    private OmCode lotProductQuantityFull;        //lot_product_quantity_full = (1232, "lot product quantity full")
    private OmCode lotTypeNotSupported;           //lot_type_not_supported = (1233, "lot type not supported")
    private OmCode lotControlJobMix;              //lot_control_job_mix = (1234, "lot controlJob mix")
    private OmCode lotBankDifferent;              //lot_bank_different = (1237, "lot bank different")
    private OmCode lotMultipleCassette;           //lot_multiple_cassette = (1238, "lot lot-carrier multiple")
    private OmCode lotLotFamilyDataInvalid;       //lot_lot_family_data_invalid = (1239, "lot lot-family data invalid")
    private OmCode lotStatusProcessing;            //lot_status_processing = (1241, "lot_status_processing")
    private OmCode lotCassetteOnPort;            //lot_cassette_on_port = (1242, "lot_cassette_on_port")
    private OmCode lotStartCassetteUnMatch;      //rc.lot_start_cassette_unmatch = (1243, "lot start carrier unmatch")
    private OmCode lotInCassette;             // lot_in_cassette = (1244, "lot in carrier")
    private OmCode lotOnBackupOperation;     // lot_on_backupoperation = (1245, "The lot is in backup processing.")
    /**
     * lot_in_backupoperation = (1246, "lot in back up operation")
     */
    private OmCode lotInBackupoperation;
    private OmCode lotInOthersite;        //lot-in-othersite = (1247, "lot in othersite")
    /**
     * lot_wafer_unmatch = (1248, "lot wafer Unmatch")
     */
    private OmCode lotWaferUnmatch;
    private OmCode lotIDAssignFail;      // lotid_assign_fail = (1249, "lotid assign fail")
    private OmCode lotHasEvent;           // lot_has_event = (1251, "lot has event")
    private OmCode lackOfSmplWafer;   // lack_of_smpl_wafer = (1252, "lack of smpl wafer")
    private OmCode loadNotMatchNpwRsv;    //rc.load_not_match_npw_rsv = (1253, "load not match npw rsv")
    private OmCode lotInPostProcess;                // lot_in_post_process = (1254, "lot in post process")
    private OmCode lotHoldForHoldType;      // lot_hold_for_hold_type = (1257, "lot hold for hold type")
    private OmCode lotTypeNotSupportedForPrepare; //lot_type_not_supported_for_prepare = (1259, "lot type not supported for prepare")
    private OmCode lotRegisteredToFlowBatch;  //lot_registered_to_flow_batch = (1270, "lot registered to flow batch")
    private OmCode lotHasBondingGroup;        // lot_has_bonding_group = (1274, "lot has bonding group")
    /**
     * lot_res_eqpmonjob = (1277, "Target lot [%s] is reserved to auto monitor job.")
     */
    private OmCode lotResEqpmonjob;
    private OmCode materialLocationNotAvailable;  //material_location_not_available = (1301, "material site not available")
    private OmCode mergedBeforeMergepoint;      // merged_before_mergepoint = (1302, "merge before mergepoint")
    private OmCode machineTypeNotSorter;         // machine_type_not_sorter = (1309, "Equipment Type is not Wafer Sorter [%s].")
    /**
     * machine_type_not_exchange = (1310, "Machine Type Not Exchange")
     */
    private OmCode machineTypeNotExchange;
    private OmCode materialLocationNotFound;  //rc.material_location_not_found = (1311, "material site not found")
    private OmCode missingCassetteOfControljob; // missing_cassette_of_controljob = (1312, "missing carrier of controljob")
    private OmCode mutipleFlowBatchLotsFound;  // rc.mutiple_flow_batch_lots_found = (1317, "Multiple FlowBatched lots are found in the carrier [%s]")
    private OmCode mismatchEqpMonitorStartSeq;  //mismatch_eqp_monitor_start_seq = (1321, "mismatch auto monitor start sequence")
    private OmCode notAuthEqp;                  //not_auth_eqp = (1402, "not auth eqp")
    private OmCode notAuthFunc;                 //not_auth_func = (1403, "not auth function")
    private OmCode notAuthMenu;                 //not_auth_menu = (1404, "not auth menu")
    private OmCode notAuthStocker;              //not_auth_stocker = (1405, "not auth stocker")
    private OmCode notAutoStocker;              //not_auto_stocker = (1406, ""not auto stocker)

    private OmCode notBankInOperation;     //not_bank_in_operation = (1409, "not bank in operation")
    /**
     * not_batched_lot = (1410, "Not Batched lot")
     */
    private OmCode notBatchedLot;
    private OmCode notCorrectEqpForOperationStart; //not_correct_eqp_for_operation_start = (1413, "not correct eqp for move in")
    private OmCode notNotDirectparent;      //not_not_directparent = (1414, "not not directparent")
    /**
     * not_entry_point_of_batch = (1416, "Not Entry Point Of Batch")
     */
    private OmCode notEntryPointOfBatch;
    private OmCode notExistHold;               //not_exist_hold = (1417, "not exist hold")
    private OmCode notFoundAvailStk;               //rc.not_found_avail_stk = (1421, "not found avail stk")
    private OmCode notFoundBank;          //not_found_bank = (1422, "not found bank")
    private OmCode notFoundCassette;  //not_found_cassette = (1424, "not found carrier")
    private OmCode notFoundCategory;     //not_found_category = (1425, "not found category")
    /**
     * not_found_chamber = (1426, "not found chamber")
     */
    private OmCode notFoundChamber;
    private OmCode notFoundCode;                    //not_found_code = (1427, "not found code")
    private OmCode notFoundCorrpo;                    // not_found_corrpo = (1428, "not found corrpo")
    private OmCode notFoundCst;                     //not-found-cst = (1429, "not found cst")
    private OmCode notFoundDcdef;                     //rc.not_found_dcdef = (1432, "not found dcdef")
    private OmCode notFoundDcspec;                     //rc.not_found_dcspec = (1433, "not found dcspec")
    /**
     * not_found_delta_process_operation = (1434, "Not Found Delta Process Operation")
     */
    private OmCode notFoundDeltaProcessOperation;
    private OmCode notFoundDurable;                //rc.not_found_durable = (1436, "not found durable")
    private OmCode notFoundEntry;                //not_found_entry = (1437, "not found entry")
    private OmCode notFoundEntryW;                  //not_found_entry_w = (1438, "not found entry w")
    private OmCode notFoundEqp;                     //not_found_eqp = (1439, "not found eqp")
    private OmCode notFoundEqpFromCtrljob;         //not_found_eqp_from_ctrljob = (2818, "not found eqp from ctrljob")
    private OmCode notFoundEqpGroup;                    //not_found_eqp_group = (1440, "not found eqp group")
    private OmCode notFoundEqpAlarm;                    //not_found_eqp_alarm = (1441, "not found eqp alarm")
    private OmCode notFoundEquipment;                   //not_found_equipment = (1442, "not found eqp")
    private OmCode notFoundFlowBatch;                   //not_found_flow_batch = (1444, "not found flow batch")
    private OmCode notFoundFtholdEntW;// not_found_fthold_ent_w = (1446, "not found fthold ent w")
    private OmCode notFoundLogicalRecipe;              //not_found_logical_recipe = (1449, "not found logical recipe")
    private OmCode notFoundLot;                             //not_found_lot = (1450, "not found lot")
    private OmCode notFoundMainRoute;                       //not_found_mainroute = (1454, "not found process")
    /**
     * not_found_material = (1455, "Wafer %s has not been registered. Please call system engineer. ")
     */
    private OmCode notFoundMaterial;
    private OmCode notFoundMachineRecipe;                  //not_found_machine_recipe = (1457, "not found pmcmg recipe")
    /**
     * not_found_monitorgroup = (1458, "not found monitorgroup")
     */
    private OmCode notFoundMonitorgroup;
    private OmCode notFoundRecipeJob;                      //not_found_recipe_job = (1459, "Recipe Job %s information has not been found.")
    /**
     * not_found_object = (1462, "Not Found Object")
     */
    private OmCode notFoundObject;
    private OmCode notFoundOperation;             //not_found_operation = (1464,"not found operation")
    private OmCode notFoundProcessDefinition;             //rc.not_found_process_definition = (1465, "not found step")

    private OmCode notFoundPerson;                         //not_found_person = (1466, "not found person")
    private OmCode notFoundPfx;               //not_found_pfx = (1467, "not found pfx")
    /**
     * not_found_process_operation = (1468, "Not Found Process Operation")
     */
    private OmCode notFoundProcessOperation;   //not_found_operation = (1464, "Operation %s has not been found. Please call system engineer.")
    private OmCode notFoundPort;              //not_found_port = (1469, "not found port")
    private OmCode notFoundPos;               //not_found_pos = (1470, "not found position")
    private OmCode notFoundProductGroup;          //not_found_product_group = (1479, "not found product group")
    private OmCode notFoundProductRequest;        //rc.not_found_product_request = (1480, "not found product order")
    private OmCode notFoundProductSpec;  //not_found_product_spec = (1481, "not found product spec")
    private OmCode notFoundQtime;         //not_found_qtime = (1483, "not funf qtime")
    /**
     * not_found_request_delete_entry = (1485, "Not Found Request Delete Entry")
     */
    private OmCode notFoundRequestDeleteEntry;
    /**
     * rc.not_found_reqd_opehis = (1486, "not found reqd opehis")
     */
    private OmCode notFoundReqdOpehis;
    /**
     * not_found_req_chamber = (1487, "Not Found Req Chamber")
     */
    private OmCode notFoundReqChamber;
    private OmCode notFoundReticle;                   //rc.not_found_reticle = (1488, "not found reticle")
    private OmCode notFoundRoute;         //not_found_route = (1490, "not found process")
    private OmCode notFoundScript;         //rc.not_found_script = (1494, "not found script")
    private OmCode notFoundScrList;        //not_found_scr_list = (1495, "not found scr list")
    private OmCode notFoundStkType;         //rc.not_found_stk_type = (1498, "not found stk type")
    private OmCode notFoundSubRoute;        //not_found_sub_route = (1500, "not found sub process")
    private OmCode notFoundTechnology;        //not_found_technology = (1504, "not found technology")
    private OmCode notFoundWafer;             //not_found_wafer = (1508, "not found wafer")
    private OmCode notInAllSameState;      //not_in_all_same_state = (1510, "not in all same state")
    private OmCode notInSameBank;          //not_in_same_bank = (1511, "not in same bank")
    private OmCode notInSubroute;        //not_in_subroute = (1512, "not in subroute")
    private OmCode notMatchStartBank;         //not_match_start_bank = (1513, "not match start bank")
    private OmCode notNonProductBank;         //not_non_product_bank = (1514, "not non product bank")
    private OmCode notReservedCassette;       //rc.not_reserved_cassette = (1517, "not reserved carrier")
    private OmCode notSameLoadPurpose;        //not_same_load_purpose = (1523, "not same load purpose")
    private OmCode notSameProcstat;          //not_same_procstat = (1530, "not same procstat")
    private OmCode notSameRoute;               //not_same_route = (1534, "not_same_route")
    /**
     * no_eqp_note = (1538, "No Eqp Note")
     */
    private OmCode noEqpNote;
    private OmCode noMatchedLot;      // no_matched_lot = (1539, "There is no lot that matches the condition used by users to select lots.")
    /**
     * no_need_to_data_collect = (1543, "No Need To Data Collect")
     */
    private OmCode noNeedToDataCollect;
    private OmCode noProductRequest;          //no_product_request = (1545, "no product order")
    private OmCode noNeedReticle;             //rc.no_need_reticle = (1551, "no need reticle")
    private OmCode notFoundTargetPort;        //(1554, "Not found target port")
    private OmCode notDispatchReservedCassette; //rc.not_dispatch_reserved_cassette = (1562, "not dispatch reserved carrier")
    private OmCode notLocateToBatchOpe;         //not_locate_to_batch_ope = (1570, "not locate to batch ope")
    private OmCode notLocateToOverTarget;       //not_locate_to_over_target = (1571, "not locate to over target")
    private OmCode notFoundLotFamily;         //not_found_lot_family = (1572, "not found lot family")
    private OmCode notFoundPoForLot;      //rc.not_found_po_for_lot=(1575, "not found po for lot")
    private OmCode notFoundSourceLot;         //not_found_source_lot = (1579, "not found source lot")
    private OmCode notMatchSourceProduct;     //not_match_source_product = (1580, "not match source product")
    private OmCode notFoundSubLotType;            //not_found_sub_lot_type = (1582, "SubLotType %s is not found.")
    private OmCode notFoundEqpState;         //not_found_eqp_state = (1585, "not found eqp state")
    private OmCode notFoundProcessFlow;      //not_found_process_flow = (1586, "not found process flow")
    private OmCode notFoundSystemObj;         //not_found_system_obj = (1587, "not found system obj")
    /**
     * not_found_factorynote = (1588, "FactoryNote %s information has not been found. ")
     */
    private OmCode notFoundFactorynote;
    /**
     * not_found_machine_note = (1589, "Not Found Machine Note")
     */
    private OmCode notFoundMachineNote;
    private OmCode notFoundE10State;         //not_found_e10_state = (1590, "not found e10 state")
    private OmCode notFoundPortResource;      //not_found_port_resource = (1591,"not found port resource")
    private OmCode notFoundLotcomment;      //rc.not_found_lotcomment = (1592,"not found lotcomment")
    private OmCode notFoundLotOperationSchedule;  // rc.not_found_lot_operation_schedule = (1593, "not found lot operation schedule")
    private OmCode notFoundLotSchedule;       //not_found_lot_schedule = (1594, "not found lot schedule")
    private OmCode notFoundLotNote;           //not_found_lot_note = (1595, "not found lot note")
    private OmCode notFoundLotType;           //not_found_lot_type = (1596, "not found lot type")
    private OmCode origlotCannotBeChild;        //origlot_cannot_be_child = (1601, "original lot cannot be child lot.")
    private OmCode objectAlreadyExist;           //rc.object_already_exist = (1604, "The target object already exists.")
    private OmCode originalVendorLotInfoNotInput;       // original_vendor_lot_info_not_in_input = (1606, "original supplier lot info not in input")
    private OmCode originalVendorLotInfoNotExist;       // original_vendor_lot_info_not_exist = (1607, "original supplier lot info exist")
    private OmCode passwordExpired;                     // passsword_expired = (1701, "password expired")
    private OmCode portGroupMixed;                        //port_group_mixed = (1703, "port group mixed")
    private OmCode productSpecUnMatch;        //product_spec_un_match = (1709, "product spec un match")
    private OmCode productCountZero;                // product_count_zero = (1710, "peoduct count zero")
    private OmCode portHasLoadedCassette;                // port_has_loaded_cassette = (1721, "port has loaded carrier")
    private OmCode portGroupHasLoadedCast;           // port_group_has_loaded_cast = (1722, "pore group has loaded cast")
    private OmCode plotEmptySplit;                  // plot_empty_split = (1723, "plot empty split")
    /**
     * postproc_next_entry_with_commit = (1736, "Successfully end a execution of Post-Processing. Continue the next one with TX_COMMIT.")
     */
    private OmCode postprocNextEntryWithCommit;
    /**
     * postproc_next_entry_without_commit = (1737, "Successfully end a execution of Post-Processing. Continue the next one without TX_COMMIT.")
     */
    private OmCode postprocNextEntryWithoutCommit;
    /**
     * postproc_error = (1738, "The Post-Processing Execution was failed. The d_key of the specified queue is [%s] and the seq_no is [%s]. The request will be retried.\r\n%s")
     */
    private OmCode postprocError;
    /**
     * postproc_unknown_targettype = (1740, "The specified TARGET_TYPE [%s] is invalid.")
     */
    private OmCode postprocUnknownTargettype;
    /**
     * postproc_pattern_mismatch = (1743, "The combination of input-parameter [%s] and the Post-Processing pattern [%s] is invalid. ")
     */
    private OmCode postprocPatternMismatch;

    private OmCode prohibitiveCharacterUse;                  // rc.prohibitive_character_use = (1751, "Some invalid character is used as the target object's ID.")
    private OmCode preventedBySorterJob;              //rc.prevented_by_sorter_job = (1752, "prevented by sorter job")
    /**
     * process_job_ctrl_not_available = (1757, "Process Job Ctrl Not Available")
     */
    private OmCode processJobCtrlNotAvailable;
    /**
     * pjctrl_available = (1758, "Process Job Level Control Function is available for equipment [%s]. Please use Process Job Level Control function Instead.")
     */
    private OmCode pjctrlAvailable;
    /**
     * process_job_data_not_reported = (1758, "Process Job Data Reported")
     */
    private OmCode processJobDataNotReported;
    /**
     * postproc_queue_exist = (1762, "Post Process Queue exits: dKey[%s]")
     */
    private OmCode postprocQueueExist;
    /**
     * postrpoc_dkey_recreate = (1764, "The post process actions have been recreated for lot [%s].")
     */
    private OmCode postrpocDkeyRecreate;
    /**
     * rolled_back_by_other_lot_error = (1921,"Rolled Back By Other lot Error")
     */
    private OmCode rolledBackByOtherLotError;
    private OmCode reachMaxRework;                        //reach_max_rework = (1901, "reach max rework")
    private OmCode reticleIdPod;                        //rc.reticle_id_pod = (1915, "reticle id pod")
    private OmCode reticlepodSlotNotBlank;             //rc.reticlepod_slot_not_blank = (1916, "reticlepod slot not blank")
    private OmCode reservedByDeletionProgram;             //reserved_by_deletion_program = (1922, "reserved by deletion program")
    private OmCode reservedMaxSplitCount;          // reserved_max_split_count = (1924, "reserved max split count")
    private OmCode reticleIsInReticlepod;          // rc.reticle_is_in_reticlepod = (1925, "reticle is in reticlepod")
    private OmCode referenceValueDiffer;              //rc.reference_value_differ = (1926, "reference value differ")
    private OmCode rspportNotFound;               //rspport_not_found = (1942, "not found rspport")
    private OmCode reticleLoadedRtclpod;           // reticle_loaded_rtclpod = (1958, "Input reticle pod is [%s]. This is different from current loaded reticle pod [%s] on equipment [%s] / port [%s].")
    private OmCode reticleRsvedForDiffEqp;     //reticle_rsved_for_diff_eqp = (1949, "this reticle rsved for diff eqp")
    private OmCode reticlepodLoadedReticlepod;     //(1958, "Input reticle pod is [%s]. This is different from current loaded reticle pod [%s] on equipment [%s] / port [%s]. ")
    private OmCode receivedVendorLot;            // received_vendor_lot = (1964, "this lot is received supplier lot")
    private OmCode runningEqpMonJob;            // running_eqpmonjob = (1966, "running auto monitor job")
    private OmCode reticlepodNotEmpty;            // (1967, "The reticle pod %s is not empty.")
    /**
     * postproc_delete_by_other = (1767, "The post process action by d_key [%s] and seqno [%s] has been deleted by others.")
     */
    private OmCode postprocDeleteByOther;
    /**
     * reticle_not_in_the_eqp = (1906, "Required reticle %s is not in the equipment. Please check the transfer status. ")
     */
    private OmCode reticleNotInTheEqp;
    private OmCode sameCastStat;            // rc.same_cast_stat = (2001, "same cast stat")
    private OmCode sameReticleStat;            // rc.same_reticle_stat = (2004, "same reticle stat")
    private OmCode someBankDataError;             //rc.some_bank_data_error = (2009, "some bank data error")
    private OmCode someCassetteInventoryDataError;        //rc.some_cassette_inventory_data_error = (2010, "some carrier inventory data error")
    /**
     * some_eqp_note_data_error = (2014, "Some Eqp Note Data Error")
     */
    private OmCode someEqpNoteDataError;
    /**
     *  some_lot_comt_data_error = (2018, "some lot comment data error")
     */
    private OmCode someLotComtDataError;
    private OmCode someLotidDataError;     //some_lotid_data_error = (2019, "some lotid data error")
    private OmCode someLotNoteDataError;  //some_lot_note_data_error = (2020, "some lot note data error")
    private OmCode someopehisDataError;  //someopehis_data_error = (2021, "someopehis data error")
    private OmCode someopelistDataError;      //someopelist_data_error = (2022, "some operation list data error")
    private OmCode someOpGuideDataError;      //some_opguide_data_error = (2024, "some operation guide data error")
    private OmCode someProdspDataError;       //some_prodsp_data_error = (2026, "some prodsp data error")
    /**
     * some_data_value_blank = (2031,"Some Data Value Blank")
     */
    private OmCode someDataValueBlank;
    private OmCode sourceWaferObjrefBlank;             //rc.sourcewafer_objref_blank = (2043, "Stringified Object Reference of Source Wafer [%s] in Input Parameter is blank. This field must be filled.")
    private OmCode stbWaferCountNotEnough;            //stb_wafer_count_not_enough = (2044, "stb wafer count not enough")
    private OmCode stbCassetteHasControlJob;          //stb_cassette_has_control_job = (2045, "stb carrier has control job")
    private OmCode sameReticlepodStat;                //rc.same_reticlepod_stat = (2047, "same reticlepod stat")
    /**
     * some_lot_failed = (2049,"Some lot Failed")
     */
    private OmCode someLotFailed;
    /**
     * some_product_group_data_error = (2054,"Some Product Group Data Error")
     */
    private OmCode someProductGroupDataError;
    private OmCode someRequestsFailed;                //some_requests_failed = (2056, "some requests failed")
    private OmCode startReserveControlJobOperationStateControlJobUnMatch; //start_reserve_control_job_operation_state_control_job_un_match = (2057, "start reserve control job vs operation state control job unmatch")
    /**
     * smpl_slotmap_conflict_warn = (2058, "smpl slotmap conflict warn")
     */
    private OmCode smplSlotmapConflictWarn;
    /**
     * smpl_invalid_slot_select = (2059, "smpl invalid slot select")
     */
    private OmCode smplInvalidSlotSelect;
    private OmCode sorterValidityNotChecked;          //sorter_validity_not_checked = (2061, "sorted validity not checked")
    private OmCode slmWaferNotFoundInPosition;        //rc.slm_wafer_not_found_in_position = (2074, "FMC wafer not found in position")
    /**
     * slm_invalid_parameter_for_cj = (2076, "The specified lot [%s]'s controlJob [%s] and the specified controlJob [%s] are different.")
     */
    private OmCode slmInvalidParameterForCj;
    private OmCode slmInvalidKeyCategory;             //rc.slm_invalid_key_category = (2080, "FMC invalid key category")
    private OmCode slmDestinationCassetteUndefine; //slm_destination_cassette_undefine = (2086,"FMC destination carrier undefine")
    private OmCode lotStbCancelOff;               //lot_stb_cancel_off = (2089, "lot stb cancel off")
    private OmCode stbSourceLotInfoNotInInput; // stb_source_lot_info_not_in_input = (2091, "stb source lot info not in input")
    private OmCode stbSourceLotInfoNotExist;   // stb_source_lot_info_not_exist = (2092, "stb source lot info not exist")
    private OmCode functionNotAvailable;    // function_not_available = (2098, "function not available")
    private OmCode speccheckError;         //speccheck_error = (2099, "Some error occurred in Spec Check. Spec check for reported collected data is suspended.")
    private OmCode sameEqpMonState;     // same_eqpmon_state = (2100, "The equipment %s is already in the specified status %s.")
    private OmCode sameEqpmonJobStat;   // same_eqpmonJob_stat = (2101, "The auto monitor job %s is already in the specified status %s.")
    private OmCode tcsNoResponse;     //tcs_no_response = (2104, "There was no response from EAP.")
    private OmCode udataCheckError;           //rc.udata_check_error = (2212, "customer defined attributes check error")
    private OmCode undefinedPortState;        //rc.undefined_port_state = (2202, "Port %s's port state %s has not been defined.")
    private OmCode unloaderReservedByAnother;        //rc.unloader_reserved_by_another = (2208, "unloader reserved by another")
    private OmCode unknownOperation ;         // unknown_Operation = (2215, "Unknown Operation")
    /**
     * unknown_search_key_pattern = (2216, "Unknown Search Key Pattern")
     */
    private OmCode unknownSearchKeyPattern;
    private OmCode unmatchCassetteCombination;   // unmatch_cassette_combination = (2210, "unmatch carrier combination")
    private OmCode unmatchControljobEqpVsCast; // rc.unmatch_controljob_eqp_vs_cast = (2211, "unmatch_controljob_equipment_vs_cast")
    private OmCode stockerInventoryInProcess;      //rc.stocker_inventory_in_process = (2034, "stocker inventory in process")
    private OmCode valueRangeExceed;               //rc.value_range_exceed = (2301, "value range exceed")
    private OmCode waferAllocated;                //wafer_allocated = (2401, "vafer allocated")
    private OmCode waferLotConnectionError;       //wafer_lot_connection_error = (2402, "wafer-lot connection error")
    private OmCode waferIdAssignRequired;     //wafer_id_assign_required = (2403, "wafer id assign required")
    private OmCode waferNotPrepared;                  //wafer_not_prepared = (2404, "wafer not prepared")
    private OmCode waferInDifferentCassette;          //wafer_in_different_cassette = (2405, "wafer in different carrier")
    private OmCode waferNotInEqp;                    //wafer_not_in_eqp = (2406, "wafer not in eqp")
    private OmCode waferIDBlank;                  // wafer_id_blank = (2407, "Wafer ID is not input. Wafer ID must be input. ")
    private OmCode waferNoOriginalVendorLotInfo;    // wafer_no_original_vendor_lot_info = (2419, "wafer no original supplier lot info")
    private OmCode waferNoStbSourceLotInfo;         //wafer_no_stb_source_lot_info = (2420, "wafer no stb source lot info")
    private OmCode waferPsmReserved;                //wafer_psm_reserved = (2421, "Specified wafer[%s] is reserved for PSM.")
    private OmCode waferStbCountMoreThanOne;        // wafer_stb_count_more_than_one = (2422, "wafer stb count more than one")
    private OmCode waferFPCRegistered;              // wafer_fpc_registered = (2423, "Lot's wafer[%s] is registered for DOC.")
    private OmCode notCandidateLotForOperationStart;      //rc.not_candidate_lot_for_operation_start = (2800, "not candidate lot for move in")
    private OmCode notFoundControlJob;              //not_found_control_job = (2801, "not found control job")
    private OmCode notFoundDefaultMachineState;   //not_found_default_machine_state = (2803, "not found default pmcmg state")
    private OmCode notFoundLotInProcessLot;           //rc.not_found_lot_in_process_lot = (2805, "not found lot in process lot")
    private OmCode notFoundMachineOperationMode;  //not_found_machine_operation_mode = (2806, "not found pmcmg operation mode")
    private OmCode notResvedPortgrp;         //not_resved_portgrp = (2808, "not resved portgrp")
    /**
     * not_same_control_job_id = (2809,"Not Same Control Job Id")
     */
    private OmCode notSameControlJobId;
    private OmCode noPortInput;                   //rc.no_port_input = (2811, "no port input")
    private OmCode noCassetteOnPort;              //no_cassette_on_port = (2812, "not carrier on port")
    /**
     * not_enough_lot_for_flow_batch = (2813, "Not Enough lot For Flow Batch")
     */
    private OmCode notEnoughLotForFlowBatch;


    /**
     * rc.not_found_flowbatch_cand_lot = (2814, "not found flowbatch cand lot")
     */
    private OmCode notFoundFlowbatchCandLot;

    /**
     * not_found_in_processing_lot = (2815,"Not Found In Processing lot")
     */
    private OmCode notFoundInProcessingLot;
    /**
     * no_need_spec_check = (2816,"No Need Spec Check")
     */
    private OmCode noNeedSpecCheck;
    private OmCode waferInLotHaveContainerPosition;// wafer_in_lot_have_container_position = (2418, "wafer in lot have container position")
    private OmCode newLotIdBlank;                     //new_lot_id_blank = (2817, "new lot id blank")
    private OmCode needToSpecifyAllLotInCassette;   //need_to_specify_all_lot_in_cassette = (2819, "need to specify all lot in carrier")
    private OmCode notSameRecipeParamInfo;         //not_same_recipe_param_info = (2821, "not same recipe param info")

    private OmCode notEmptyReticlePodPosition;  //(2822,"Requested reticle pod [%s] is not empty in specified position [%s].")
    private OmCode notFoundReticlePod;  //not_found_reticle_pod = (2823, "Reticle Pod [%s] information has not been found.")
    private OmCode notFoundTestType; //not_found_test_type = (2835,"not found test type")
    private OmCode notFoundCdata; //rc.not_found_cdata = (2845,"not found cdata")
    private OmCode noResponseApc; //rc.no_response_apc = (2848,"no reply apc")
    private OmCode notAuthProduct;    // not_auth_product = (2849, "not auth product")
    private OmCode notAuthRoute;      // not_auth_route = (2850, "not auth process")
    private OmCode notAuthLot;        // not_auth_lot = (2851, "not auth lot")
    private OmCode notAuthMachineRecipe; //not_auth_machine_recipe = (2852, "not auth machine recipe")
    private OmCode notFoundBufferResource; //rc.not_found_buffer_resource = (2853, "not found buffer resource")
    private OmCode notFoundMaterialLocation; //rc.not_found_material_location = (2854, "not found material site")
    private OmCode notSpaceEqpSelf; //rc.not_space_eqp_self = (2855, "not space eqp self")

    /**
     * not_cleared_control_job = (2863, "Not Cleared Control Job")
     */
    private OmCode notClearedControlJob;
    private OmCode notQueryTransaction;  //not_query_translate = (2865, "not query transaction")
    /**
     * not_processed_by_other_lot_error = (2866,"Not Processed By Other lot Error")
     */
    private OmCode notProcessedByOtherLotError;
    /**
     * not_found_module_no = (2870, "not found module no")
     */
    private OmCode notFoundModuleNo;
    private OmCode notFoundExperimentalLotObj; //not_found_experimental_lot_obj = (2872, "No Found PSM Lot Object.")
    private OmCode notDynamicRoute;      //not_dynamic_route = (2876, "not dynamic process")
    /**
     * not_found_module = (2877, "Not Found Module")
     */
    private OmCode notFoundModule;
    private OmCode notFoundRouteOpe;              //not_found_route_ope = (2880, "not found process operation")
    private OmCode notFoundReturnOpe;                  //not_found_return_ope = (2885, "not found return ope")
    private OmCode notFoundNextOperation;               //.not_found_next_operation = (2886, "not found next operation")
    private OmCode notFoundEqpDispatcher;// not_found_eqp_dispatcher = (2887, "not found eqp dispatcher")
    private OmCode notCassetteOnPort ;               // not_cassette_on_port = (2888, "carrier [%s]'s is not found port.")
    private OmCode notFoundSourceWafer;                   //not_found_source_wafer = (2891, "not found source wafer")
    /**
     * not_found_process_flow_for_process_definition = (2892, "Not Found Process Flow For step")
     */
    private OmCode notFoundProcessFlowForProcessDefinition;
    /**
     * not_found_pos_for_pd = (2893, "Process Operation Specification on step is not found. [PD ID: %s/ PD Level: %s]")
     */
    private OmCode notFoundPosForPd;
    private OmCode newPasswordLengthOver;                 // new_password_length_over = (2899, "new password length over")
    private OmCode invalidCassettePortCombination;        //rc.invalid_cassette_port_combination = (2900, "invalid carrier port combination")
    private OmCode invalidInputCassetteCount;             //rc.invalid_input_cassette_count = (2901, "The number of cassettes is not correct for equipment")
    private OmCode invalidInputWaferCount;                //invalid_input_wafer_count = (2902, "invalid input wafer count")
    private OmCode invalidModeChangeType;                 //rc.invalid_mode_change_type = (2904, "invalid mode change type")
    private OmCode invalidPortCombination;                //rc.invalid_port_combination = (2906, "invalid port combination")
    private OmCode invalidPortLoadMode;                   //invalid_port_load_mode = (2907, "invalid port load mode")
    private OmCode invalidPortModeCombination;           //invalid_port_mode_combination = (2908, "invalid port mode combination")
    private OmCode invalidProcstateForMonitorgrping; //invalid_procstate_for_monitorgrping = (2909, "invalid procstate for monitorgrping")
    private OmCode invalidSourceLotProduct;               //invalid_source_lot_product = (2912, "invalid source lot product")
    private OmCode invalidProductMonitorCount;           //invalid_product_monitor_count = (2913, "invalid product monitor count")
    private OmCode invalidOrgWaferPosition;    // invalid_org_wafer_position = (2914, "Requested wafer [%s] is not in specified position [%s:%s].")
    /**
     * invalid_cassette_lot_relation = (2916, "Invalid carrier lot Relation")
     */
    private OmCode invalidCassetteLotRelation;
    private OmCode invalidSorterOperation ;           // invalid_sorter_operation = (2917, "invalid_sorter_operation")
    private OmCode invalidMachineId;                      //invalid_machine_id = (2918, "invalid Machine id")
    private OmCode invalidRecipeConditionForEqp;         //invalid_recipe_condition_for_eqp = (2929, "invalid recipe condition for eqp")
    private OmCode invalidRecipeParamChangeType;         //invalid_recipe_param_change_type = (2931, "invalid recipe param change type")
    private OmCode invalidWaferCount;                     //invalid_wafer_count = (2932, "invalid wafer count")
    private OmCode invalidReticlePodPosition;                     //rc.invalid_reticle_pod_position = (2933, "invalid reticle pod position")
    /**
     * invalid_reticlepod_stat = (2940, "Status [%s] of reticlePod [%s] is invalid. ")
     */
    private OmCode invalidReticlepodStat;
    /**
     * invalid_reticlepod_xfer_stat = (2941, "The reticlePod [%s] transfer status [%s] is invalid for the request. ")
     */
    private OmCode invalidReticlepodXferStat;
    private OmCode invalidLcData;                   //invalid_lc_data = (2942, "invalid lc data")
    private OmCode invalidParameterValueRange;           //invalid_parameter_value_range = (2943, "invalid parameter value range")
    private OmCode invalidParameterValueMustBeNull;      //invalid_parameter_value_must_be_null = (2946, "invalid parameter value must be null")
    private OmCode invalidBranchRouteId;        // invalid_branch_routeid = (2947, "invalid branch routeID")
    private OmCode internalBufferAlreadyReserved;        // rc.internal_buffer_already_reserved = (2948, "internal buffer already reserved")
    private OmCode invalidCategoryCheck;                  //rc.invalid_category_check = (2949, "invalid category check")
    private OmCode invalidActionCode;             // invalid_action_code = (2951,"invalid action code")
    private OmCode invalidParameter;                          //invalid_parameter = (2962, "invalid parameter")
    private OmCode invalidPortLoadPurposeForNpwTransfer;  //invalid_port_load_purpose_for_npw_transfer = (2965, "invalid port load purpose for npw transfer")
    private OmCode invalidLoadpurpose;  //rc.invalid_loadpurpose = (2966, "invalid loadpurpose")
    private OmCode invalidCurrentRoute   ;  //rc.invalid_current_route = (2974, "invalid current process")
    private OmCode invalidProcessBatchCount;  //rc.invalid_process_batch_count = (2971, "invalid process batch count")
    private OmCode invalidCurrentOperation   ;  //rc.invalid_current_operation = (2975, "invalid current operation")
    private OmCode invalidCurrentProduct   ;  //rc.invalid_current_product = (2976, "invalid current product")
    private OmCode invalidReworkOperation;                //invalid_rework_operation = (2977, "invalid rework operation")
    private OmCode invalidReasonCodeFromClient;               //invalid-reason-code-from-client = (2980, "invalid reason code from client")
    private OmCode invalidRouteType;                 //invalid_route_type = (2981, "invalid process type")
    private OmCode invalidRouteId;                   //invalid_route_id = (2982, "invalid process id")
    private OmCode invalidProductStat;            //invalid_product_stat = (2983, "invalid product stat")
    /**
     * invalid_input_wafer = (2986, "Input wafer is invalid. ")
     */
    private OmCode invalidInputWafer;
    /**
     * rc.invalid_flow_type = (2990, "invalid flow type")
     */
    private OmCode invalidFlowType;
    /**
     * rc.lot_backup_on_backupsite = (2996, "lot backup on backupsite")
     */
    private OmCode LotBackupOnBackupsite;
    /**
     * invalid_reticle_pod_postion_specified = (3009, "Specified position [%s] is invalid for Reticle Pod [%s]. ")
     */
    private OmCode invalidReticlePodPostionSpecified;
    /**
     * invalid_route_operation_specified = (3010, "invalid process operation specified")
     */
    private OmCode invalidRouteOperationSpecified;
    private OmCode invalidFlowbatchOperationcount; // rc.invalid_flowbatch_operationcount = (3011, "invalid flowbatch operationcount")
    private OmCode invalidControlJobActionType;  //invalid_control_job_action_type = (3017, "invalid control job action type")
    private OmCode invalidHoldtypeLength;             //invalid_holdtype_length = (3023, "invalid hold type length")
    /**
     * invalid_exec_condition = (3020, "Post Process Queue registration failed by invalid exec condition setting. Invalid exec condition is [%s]")
     */
    private OmCode invalidExecCondition;
    /**
     * ignore_recycle_smpl = (3024,"ignore_recycle_smpl")
     */
    private OmCode ignoreRecycleSmpl;
    /**
     * invalid_smpl_setting = (3025, "invalid smpl setting")
     */
    private OmCode invalidSmplSetting;

    /**
     * not_found_udata = (3028, "UserData information has not been found. stringifiedObjRef [%s],Class [%s], ID [%s], Name [%s]")
     */
    private OmCode notFoundUData;
    private OmCode invalidCassetteCombination;        //invalid_cassette_combination = (3035, "invalid carrier combination")
    private OmCode invalidInputCassetteId;             // invalid_input_cassette_id = (3036, "invalid input carrier id")
    private OmCode invalidProductCount;                //invalid_product_count = (3059, "invalid product count")
    /**
     * rc_interfab_invalid_xferstate = (3084, "Invalid InterFab Transfer State [%s].")
     */
    private OmCode interfabInvalidXferstate;
    /**
     * interfab_lotxfer_executed = (3085, "InterFab lot transfer action is executed.")
     */
    private OmCode interfabLotxferExecuted;
    private OmCode invalidBankId;                       // invalid_bank_id = (3089, "invalid bank id")
    /**
     * (3091, "The Lot [%s] InterFab Transfer State [%s] is invalid for this request.")
     */
    private OmCode interfabInvalidLotXferstateForReq;
    private OmCode interfabInvalidCassetteTransferStateForRequest;        //rc.interfab_invalid_cassette_transfer_state_for_request = (3092, "interfab invalid carrier transfer state for request")
    /**
     * rc.inprocess_lotinfo_update = (3111, "inprocess lotinfo update")
     */
    private OmCode inprocessLotinfoUpdate;
    private OmCode invalidRecipeForEqp;                     //invalid_recipe_for_eqp = (3115, "invalid recipe for eqp")
    private OmCode invalidEqpmonstatus;// invalid_invalid_eqpmonstatus = (3119, "invalid_invalid_eqpmonstatus")
    private OmCode invalidEqpmontype;   //invalid_eqpmontype = (3120, "auto monitor type [%s] is invalid for the request.")
    private OmCode invalidEqpMonitorJobStatus;            //invalid_eqp_monitor_job_status = (3121, "invalid auto monitor job status")
    private OmCode invalidOperationlabel;           // invalid_operationlabel = (3122, "auto monitor section label [%s] of current operation for Lot [%s] is invalid for the request.")
    private OmCode invalidEqpstateatstart;           // invalid_eqpstateatstart = (3123, "Equipment state on monitor start for auto monitor [%s] is invalid.")
    private OmCode invalidProductCategory;                // invalid_product_category = (3124, "Product Category [%s] is invalid for the request.")
    private OmCode invalidMonitorProduct;     //invalid_monitor_product = (3125, "Lot's product is unmatch with the product defined in auto monitor [%s].")
    private OmCode inhibitDurable;                   // (3127,"The durable [%s] cannot be processed by the following inhibition(s). %s.")
    private OmCode invalidOperationType;           // invalid_operation_type = (3128, "Invalid operation type [%s].")
    private OmCode invalidDcjstatus;            //rc.invalid_dcjstatus = (3130, "invalid dcjstatus")
    private OmCode invalidDurableCategory;            //rc.invalid_durable_category = (3133, "invalid durable category")
    private OmCode invalidDurableHoldStat;            //(3134, "Durable %s holdStatus %s is invalid for this request. If durable is held with 'LOCK' reason code, durable is locked by post process after durable moving. Please wait for a while and try again.")
    private OmCode invalidDurableProcStat;            //(3135,"Durable %s processStatus %s is invalid for this request.")
    private OmCode invalidDurableInventoryStat;  //(3136,"Durable %s inventoryStatus %s is invalid for this request. ")
    private OmCode noProcessJobExecFlag; // no_process_job_exec_flag = (3200, "no process job exec flag")
    /**
     * no_smpl_setting = (3201, "no smpl setting")
     */
    private OmCode noSmplSetting;
    /**
     * not_found_smpl_record = (3202, "not found smpl record")
     */
    private OmCode notFoundSmplRecord;
    /**
     * not_found_class_name = (3203, "Not Found Class Name")
     */
    private OmCode notFoundClassName;
    private OmCode notReworkBatchOpe;                  //rc.not_rework_batch_ope = (3218, "not rework batch ope")
    private OmCode notBranchBatchOpe;                   //rc.not_branch_batch_ope = (3219, "Branch Error. Reason : %s ")
    private OmCode notFoundEquipmentContainerPosition; //rc.not_found_equipment_container_position = (3222, "not found eqp container position")
    private OmCode notReservedCastSLM; //rc.not_reserved_cast_slm = (3223, "The carrier [%s] is not reserved for FMC.")
    private OmCode notFoundEquipmentContainer; // rc.not_found_equipment_container = (3225, "not found eqp container")
    private OmCode noSlotMapInFormAtion;      //rc.no_slot_map_in_form_ation = (3232, "no_slot_map_in_form_ation")
    private OmCode notFoundLotInControlJob;       //rc.not_found_lot_in_control_job = (3233, "not found lot in control job")
    private OmCode notFoundProductRequestForSourceLot; //not_found_product_request_for_source_lot = (3240, "not found product order for source lot")
    private OmCode notLocatetoBondingflowsection;   // not_locateto_bondingflowsection = (3242, "not locateto bondingflowsection")
    private OmCode notReworkBondingFlow;            // not_rework_bonding_flow = (3243, "not rework bonding flow")
    private OmCode notBranchBondingFlow;            //rc.not_branch_bongding_flow = (3244, "Branch Error. Reason : %s")
    private OmCode notFoundBondingGroup;            // not_found_bonding_group = (3246, "Bonding Group [%s] has been not found")
    private OmCode notFoundBondingGroupForLot;         //rc.not_found_bonding_group_for_lot = (3249, "not found bonding group for lot")
    private OmCode notFoundSomeObject;                   //not_found_some_object = (3254, "not found some object")
    private OmCode notFoundEqpMonitor;                  //not_found_eqp_monitor = (3256, "not found auto monitor")
    private OmCode notFoundEqpMonitorJob;              //not_found_eqp_monitor_job = (3257, "not found auto monitor job")
    private OmCode notAllowedEqpMonitorStat;        // not_allowed_eqp_monitor_stat = (3258, "not_allowed_eqp_monitor_stat")
    private OmCode notAllowedEqpMonitorjobStat;  // not_allowed_eqp_monitorjob_stat = (3259, "not allowed Auto Monitor Job status")
    private OmCode notFoundEqpMonitorJobLot ;              //not_found_eqp_monitor_job_lot = (3261, "Can not find the Auto Monitor Job Lot Info object.[%s]")
    private OmCode notReservedDctrljobPortgrp;      // not_reserved_dctrljob_portgrp = (3266, "Specified port is not reserved for durable control job.")
    private OmCode notFoundDctrljob;        //not_found_dctrljob = (3267, "DurableControlJob is not found.")

    /**
     * not_same_buffer_capacity = (3270, "Not Same Buffer Capacity")
     */
    private OmCode notSameBufferCapacity;
    private OmCode notCorrectEqpForOpestartForDurable;    // (3278, "These durables can not be processed by this equipment.")
    /**
     * not_found_po_for_durable = (3279, "not found po for durable")
     */
    private OmCode notFoundPoForDurable;
    /**
     * not_enough_decrease_capacity = (3280, "Not Enough Decrease Capacity")
     */
    private OmCode notEnoughDecreaseCapacity;
    /**
     * not_enough_increase_capacity = (3281, "Not Enough Increase Capacity")
     */
    private OmCode notEnoughIncreaseCapacity;

    private OmCode notSameLogicalRecipeForDurable; //  (3283,"Durable %s's process condition (logical recipe) is not the same as that for other durables.")

    /**
     * not_found_durable_substat = (3285, "not found durable substat")
     */
    private OmCode notFoundDurableSubstat;
    /**
     * same_durable_substat = (2500, "same durable substat : %s")
     */
    private OmCode sameDurableSubstat;
    /**
     * search_key_mismatch = (3501, "Search Key Mismatch")
     */
    private OmCode searchKeyMismatch;
    private OmCode notFoundStocker;              //rc.not_found_stocker = (4144, "Approproate Stocker has not been found")
    private OmCode tmsRecordNotFound;           //tms_record_not_found = (4181,"tms record not found")
    private OmCode undefinedStockerType;          //rc.undefined_stocker_type = (4211, "undefined stocker type")
    private OmCode tcsOMAlreadyOpeCompleted; //tcs_mm_already_ope_completed = (5433, "EAP already ope completed")
    private OmCode tcsOMAlreadyOpeCanceled; //tcs_mm_already_ope_canceled = (5434, "EAP already ope canceled")
    private OmCode tcsOMFetchCJError; //tcs_mm_fetch_cj_error = (5804, "EAP fetch control Job error")
    private OmCode tooManyFlowBatches; //rc.too_many_flow_batches = (5915, "Too many FlowBatches are found in the cassettes.")
    private OmCode alreadyExistFutureHoldRecord;  //already_exist_future_hold_record = (99996, "already exitst future hold record")
    private OmCode alreadyExistHoldRecord;     //rc.already_exist_hold_record = (99997, "already exitst hold record")


    private OmCode invalidPurposeTypeForProcessdurable;

    private OmCode notAvailableFixture;
    private OmCode notAvailableReticle;

    private OmCode cannotLoadWithoutCj;

    private OmCode existSameHold;

    /**
     * rc_invalid_search_condition
     */
    private OmCode invalidSearchCondition;  //3015;
    /**
     * rc_invalid_wildcard_position
     */
    private OmCode invalidWildcardPostion; //3012;

    private OmCode invalidStartTime;    //3014;

    private OmCode invalidEntityCount; //3019;

    private OmCode notFoundEntityInhibit;  //(1584, "Specified entity is not found. ")

    private OmCode invalidOperationNumber;        //3018;

    private OmCode notFoundFixture;           //1443;

    private OmCode notFoundReticleGrp;          //1489;

    private OmCode notFoundFixtureGrp;      //2837;

    private OmCode notFoundStage;      //1496;

    private OmCode inhibitNoClass;     //978;

    private OmCode someRouteDataError;              //2028;

    private OmCode duplicatedEntityInhibit;         //2291;

    private OmCode invalidOwnerIdentifier;

    private OmCode notFoundDurablePo;

    private OmCode notFoundLotOpeNote;           //not_found_lot_ope_note = (1597, "not found lot operation note")

    private OmCode invalidSourceLotCombination;      //invalid_source_lot_combination = (2911, "invalid source lot combination.")

    //private CimCode notInvalidCassetteState;

    private OmCode notFoundMachine; // 1453

    private OmCode samePreOperation; //2003;     //same pre operation = (2003,"same pre operation.");

    private OmCode notSameOperation; //1256;     //not same operation = (1256,"not same responsible operation.");

    private OmCode postprocLockHold; //1748;  //post process lock hold = (1748,"post process lock hold");

    private OmCode waitingForDataCollection; //2414; waiting for equipment data acquisition = (2414,"waiting for equipment data acquisition");

    private OmCode lotsNotSameFamily;    //1203; lots not same family = (1203,"lots not same family").

    private OmCode lotFlowBatchIdBlank; //1236; lot flow batch id blank = (1236, "lot flow batch id blank");

    private OmCode lotFlowBatchIdFilled; //1235; lot flow batch id filled = (1235,"lot flow batch id filled");

    private OmCode flowBatchLimitation; //625; flow batch limitation = (625, "flow batch limitation");

    private OmCode flowBatchMaxCountTypeError;  // 678;

    private OmCode flowBatchMaxCountIsSame;  // 677;

    private OmCode eqpReservedForSomeFlowBatch;  // 560;

    private OmCode differentQtimeInformation; // (433, "different qtime information.");

    private OmCode qtimeActionAlreadyDone; //(1805,"qtime action already done." ;

    private OmCode notFoundQTimeDefinition; //(3264,"not found qtime definition.");
    /**
     *  OM_EDC_MULTI_CORRESPOND_FLAG_mismatch = (3502, "OM_EDC_MULTI_CORRESPOND_FLAG is not matched. Check the environment variable of Server and Client.")
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/4/10 15:47
     * @param null -
     * @return
     */
    private OmCode omEdcMultiCorrespondFlag;
    private OmCode duplicateQTime; //(437,"duplicate qtime.");
    private OmCode invalidParameterWithMsg; //(3037,"invalid parameter with message");
    private OmCode notSameDispatchPrecedeTargetTime; //(3284, "not same dispatch precede target time." );
    private OmCode invalidPDLevel;//(2993,"invalid step level");
    private OmCode invalidPDType;//(2994,"invalid step type");
    private OmCode ftRwkDataInvalid; //(630,"future rework data invalid.");
    private OmCode duplicateEntityInhibitExceptLot; //(436,"duplicate manufacturing constraint except lot");
    private OmCode notFoundEntityInhibitExceptLot; //(3263, "not found manufacturing constraint except lot");
    private OmCode notFoundProcessJob; //(3252,"not found process job");
    private OmCode invalidCassetteCountForBatch;                //invalid_cassette_count_for_batch = (974,"invalid carrier count for batch")
    private OmCode invalidFlowBatchMinwaferCnt;                //invalid_flow_batch_minwafer_cnt = (2978,"invalid flow batch minwafer cnt")
    private OmCode notFoundMonitorLot; //(1598,"not found monitor lot");

    private OmCode definedDcSpecInfo; //(701,"defined equipment data acquisition spec info.");
    private OmCode notSameFlowCntOfBatch;    //1522; not_same_flow_cnt_of_batch = (1522,"not_same_flow_cnt_of_batch").
    private OmCode notSameTargetOperOfBatch;    //1536; not_same_target_oper_of_batch = (1536,"not_same_target_oper_of_batch").
    private OmCode notSameRecipeInTargetOper;    //1533; not_same_recipe_in_target_oper = (1533,"not_same_recipe_in_target_oper").
    private OmCode eqpmonjobExist;    //569; eqpmonjob_exist = (569,"eqpmonjob_exist").

    private OmCode notFoundExperimentalLotData;//not_found_psm_lot_data = (2873, "No PSM definition is found. The key items of target PSM are invalid.")
    private OmCode explotAlreadyDone; //explot_already_done= (554, "Target PSM already worked, so update is not allowed.[%s]")
    private OmCode processInBatchSection; //process_in_batch_section = (1727, "The process [routeID :%s openo: %s] is in the section of Flow Batch Control. ")
    private OmCode psmExecutionFail;//psm_execution_fail = (1732, "The PSM's execution is failed")
    private OmCode processInBondingFlowSection;//process_job_ctrl_not_available = (1757, "The split operation is included by a bonding flow section.")
    private OmCode interfabNotFoundXferPlan;//interfab_not_found_xfer_plan = (3060, "The InterFab Transfer Plan is not found.")
    private OmCode interfabProcessSkipError;//(3068;"inter fab process skip error")
    private OmCode psmOpeInvalid;//psm_ope_invalid = (1750, "PSM Operation Invalid")
    private OmCode notFoundExperimentalLotSubroute;//not_found_psm_lot_sub_route = (2874, "Branch process has to be related to split operation.")
    private OmCode invalidExperimentalLotMergePoint;//rc.invalid_experimental_lot_merge_point = (2973, "Invalid Experimental Lot Merge Point.")
    private OmCode schdresvExistedFutureOperation;//shd_resv_existed_future_operation = (2051, "the lot plan change reservation is existed.")
    private OmCode psmWaferInvalid;//psm_wafer_invalid = (1749, "The repetitive PSM's wafers are not specified by any beginning PSMs.")
    private OmCode foundInfoLimitOver;//found_info_limit_over = (681, "Reach to fetchLimitCount. break While-Loop.")


    private OmCode InvalidControlJobActionForCJStatus;  //invalid_control_job_action_for_cj_status = (3016,"The requested ControlJobAction is invalid for the current status [%s] of the ControlJob [%s].")
    private OmCode LotInvalidCollectData; //lot_invalid_collect_data = (1276,"Collected data is invalid. Lot [%s] ItemName[%s] MeasurementType[%s].")
    private OmCode NotFoundPrevDcDef; //not_found_prevdcdef = (1471,"equipment data acquisition definition %s for pre-measure / pre-process has not been found. ")
    private OmCode ProcessJobDataAlreadyReported;//process_job_data_already_reported = (1760,"PJ data is already reported.")
	private OmCode InvalidSLMStatusOfContainerPosition;//(3055;"invalid FMC status of container position"
    private OmCode notFoundEqpAuto3Setting;//(3238;"not found eqpAuto3Seeting"
    private OmCode duplicateBizObject; //99994
    private OmCode duplicateRecord; //99995, "[%s : %s] is already exist"
}
