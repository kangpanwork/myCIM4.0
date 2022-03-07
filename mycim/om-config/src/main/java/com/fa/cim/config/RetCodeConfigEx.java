package com.fa.cim.config;

import com.fa.cim.common.support.OmCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * description:
 * This Class is Reference to the cim-code.properties, when we add one data in cim-code.properties,
 * We must add the define in the ReCodeConfig Class, too. their reference satisfy the following rules. for example.
 * cim-code.properties                 RetCodeConfig
 * rc.succ = 0                                     private CimCode succ            // when we start the service, the succ's value is 0.
 * rc.not_found_bank = 1422                        private CimCode notFoundBank;   // when we start the service, the notFoundBank's value is 1422.
 * <p>
 * note: The statement of constants can't be modified, only the constants value could be modified.
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
@PropertySource("classpath:cim-code-ex.properties")
@ConfigurationProperties(prefix = "rc")
@Setter
@Getter
public class RetCodeConfigEx {
    /**
     * invalid_port_startmode = (3113, "Port [%s] Start Mode [%s] is invalid.")
     */
    private OmCode invalidPortStartmode;
    private OmCode succ;                          //succ = (0, "succ")
    private OmCode allrtclinvDataError;   // allrtclinv_data_error = (104, "All reticle information data have failed in being updated by Reticle-Inventory. ")
    private OmCode actioncodeUnmatch;   // actioncode_unmatch = (122, "Specified ActionCode[%s] does not match with request data. ")
    private OmCode apcInvalidBuildTimeOperation; // rc.apc_invalid_buildtime_operation = (130, "APC invalid buildtime operation.")
    private OmCode apcReturnInvalidParam; //  rc.apc_return_invalid_param = (142, "APC returns invalid parameter.")
    private OmCode carrierNotInUts;   // carrier_not_in_uts = (370, "The input cassetteID is not in any OHB. ")
    private OmCode cannotPriorityChange;   // cannot_priority_change = (373, "The order sequence of %s can not be changed. %s  ")
    private OmCode cannotSortJobStatusChange;   //rc.cannot_sort_job_status_change = (374, "Sort Job Status can not change %s to %s.")
    private OmCode actionCodeRotateAngle;    //rc.action_code_rotate_angle=(375, "The input physicalRecipeID is not valid")
    private OmCode cannotSortComponentJobStatusChange;    //rc.cannot_sort_component_job_status_change=(374, "Sort Component Job Status can not change %s to %s.")
    private OmCode invalidDurableStateTransition;             //invalid_state_transition = (963, "invalid state transition")
    private OmCode invalidInputVendorlotID;                     //invalid_input_vendorlotID = (959, "invalid input vendorlotID")
    private OmCode noProcessingTask;                     //No_processing_task = (960, "No processing task")
    private OmCode lotInCassetteNumExcessive;     // lot_in_cassette_num_excessive = (961, "Lot in carrier number is greater than 1")
    private OmCode ctrljobCastUnmatch;   //rc.ctrljob_cast_unmatch = (381, "The combination of control job [%s] / carrier [%s] is not valid.")
    private OmCode cjRelatedWfInCast;   //rc.cj_related_wf_in_cast = (386, "There are some controlJob related wafers in carrier.")
    private OmCode duplicateDispatchControlEntry;  // rc.duplicate_dispatch_control_entry = (393, "Requested Auto Dispatch Control entry already exists.")
    private OmCode duplicationOfSchedule;     // rc.duplication_of_schedule = (418, "The data with which the schedule term to [%s] overlaps exists.")
    private OmCode duplicateLocation;  // duplicate_location = (415, "Same Destination Port/Carrier/Slot Information is specified. ")
    private OmCode duplicateCarrier;     // duplicate_carrier = (416, "Same Carriers are specified in different Port. ")
    private OmCode differentCarrierInPort; //different_carrier_in_port = (417, "Different CarrierIDs were specified in a port. ")
    private OmCode dcsServerBindFail;   //("Binding to external server (DCS) is failed. Please wait for a while and try it again. If you face same error, please call system administrator. ")
    private OmCode diffRtclpodLoaded;   // diff_rtclpod_loaded = (425,"Reticle pod [%s] is currently loaded on equipment [%s] port [%s].  ")
    private OmCode durableCannotOffrouteReserve;   //rc.durable_cannot_offroute_reserve = (450, "Durable cannot be reserved by off process reserve. Because durable is on process.")
    private OmCode equipmentUnmatch;      // equipment_unmatch = (549, "Specified equipmentID[%s] does not match with request data. ")
    private OmCode executedActionAfterWaferIdRead;  //rc.executed_action_after_wafer_id_read = (552,"[%s] executed. Slot Map was changed after WaferIdRead. Should execute WaferIdRead again. ")
    private OmCode existSorterjobForCassette;       //rc.exist_sorterjob_for_cassette = (555, "carrier[%s] has SorterJob[%s].")
    private OmCode existSorterJobForEquipment;  //rc.exist_sorter_job_for_equipment=(556, "equipment[%s] has SorterJob[%s].")
    private OmCode eqpNotSupportTakeOutInFunc; //rc.eqp_not_support_takeout_in_func = (561, "Specified equipment doesn't support TakeOutIn transfer function. ");
    private OmCode eqpSlmCapabilityOff; //rc.eqp_slm_capability_off = (562, "FMC capability of equipment [%s] is OFF.");
    private OmCode ftrwkNotFound; //rc.ftrwk_not_found = (632, "The future rework request is not found.");
    private OmCode ftrwkRegistFail; //rc.ftrwk_regist_fail = (634, "The registration of future rework requests failed.")
    private OmCode invalidWaferPosition;      //     invalid_wafer_position = (971, "Slot Number is out of range.");
    private OmCode entityInhibitCreateFailed; // rc.entity_inhibit_create_failed = (977, "Entity Inhibition entry could not be created. The combinations of entities and sub-lot types for inhibitions are already registered or incorrect data is specified. ")
    private OmCode entityInhibitModifyFailed; // rc.entity_inhibit_modify_failed = (978, "Entity Inhibition entry Modify Failed. ")
    private OmCode entityInhibitModifyIllegal; // rc.entity_inhibit_modify_illegal = (979, "Entity Inhibition entry Modify Illegal, Inhibition has already started or finished ")
    private OmCode noEventdataReported;      //     RC_NO_EVENTDATA_REPORTED = 1001
    private OmCode mergeLotInAndNotInCast;  // rc.merge_lot_in_and_not_in_cast = (1129， "Either ChildLot or ParentLot is in the carrier. Another lot is not in the carrier.")
    private OmCode lotAlreadySTB;  // rc.lot_already_stb = (1207, "Lot %s has already Started and cannot be canceled. ")
    private OmCode lotNotInpostprocess;     // RC_LOT_NOT_INPOSTPROCESS             1255
    private OmCode lotNotInDataCollectionAction;     // rc.lot_not_in_data_collection_action = (1273, "The first PostProcess information of Lot [%s] is not 'CollectedDataAction'.")
    private OmCode monitorCreatNotReqd;         //rc.monitor_creat_not_reqd = (1304, "Monitor Lot STB after process not be claimed. ")
    private OmCode monitorCreatReqd;            //rc.monitor_creat_reqd = (1305, "Monitor Lot STB after process must be claimed. ")
    private OmCode notFilledFunc;
    private OmCode notFoundCustomer;            // rc.not_found_customer = (1431, "Customer Information %s has not been found.")
    private OmCode notFoundMsgDef;              //rc.not_found_msgdef = (1459, "Message definition information %s for message code %s has not been found.")
    private OmCode notFoundPrevpd;             //rc.not_found_prevpd = (1472, "step %s information for pre-measure / pre-process has not been found.")
    private OmCode notFoundSysMsgDef;           //rc.not_found_sys_msg_def = (1501, "System message definition information %s for sub system %s has not been found.")
    private OmCode notFoundEap;                 //rc.not_found_eap = (1503,"Appropriate EAP server has not been found.")
    private OmCode notReticleStocker;                   //rc.not_reticle_stocker    = (1520, "Stocker %s is not for reticle. ")
    private OmCode noNeedToSpcCheck;            // rc.no_need_to_spccheck = (1544, "Need not do SPC checking for these lots.")
    private OmCode noWipLot;                   //rc.no_wip_lot = (1550, "There is no waiting lot.")
    private OmCode notFoundEmptyCast;         //rc.no_found_empty_cast = (1552, "Empty carrier does not found.")
    private OmCode notFoundFilledCast;       //rc.not_found_filled_cast = (1553, "A Carrier for CarrierTransferReq is not available for delivery. (WIP lots' carrier count is less than required carrier count)");
    private OmCode notEnoughEmptyCassette;      //rc.not_enough_empty_cassette = (1556, "There is not enough empty carrier. ")
    private OmCode notFoundMsgQue;              //rc.not_found_msgque = (1566, "System environment value OM_DELIVERY_MESSAGE_QUEUE_PUT_ENABLE is not defined. Call system engineer.")
    private OmCode notFoundWaferInLot;          //rc.not_found_wafer_in_lot = (1576, "Described wafer is not exist in the lot. Call system engineer. ")
    private OmCode okNoIF;                      //rc.rc_ok_no_if = (1603, "Normal End. But no I/F with external system.")
    private OmCode portPortgroupUnmatch;       // port_portgroup_unmatch = (1724, "The combination of port/portgroup is not valid.")
    private OmCode postProcUnknownProcId;       //(1739, "The specified ID [%s] does not exist for the Post-Processing.")
    private OmCode postprocLockholdForDurable;       //rc.postproc_lockhold_for_durable = (1768,"[%s] is on LOCK HOLD. Please wait for a few minutes and retry the operation.\r\n If you get this message again, check the durables' post-processing queue.")
    private OmCode postProcDeleteError;         // The specified sequence number is not the smallest number in the records of the same d_key.
    private OmCode rtdInterfaceSwitchOff;               //rc.rtd_interface_switch_off = (1912, "RTD Interface-Switch is now OFF. ")
    private OmCode reportedNotMmdurable;               //rc.reported_not_mmdurable = (1938, "Inventory Upload is completed. But Durable not managed by OMS had reported. ")
    private OmCode requestRejectByTcs;          //rc.request_reject_by_tcs = (1965,"The request is rejected by EAP. ")
    private OmCode reachMaxDurableRework;          //rc.reach_max_durable_rework = (1968, "The durable has reached the max rework count %s at this operation. ")
    private OmCode rspportAccessmodeSame;     // rspport_accessmode_same = (1941, "Equipment [%s] reticle pod port [%s] current access mode is same as requested mode [%s]. ")
    private OmCode rtclHasCtrljob;  // rtcl_has_ctrljob = (1945, "Reticle %s has control job. ")
    private OmCode rtclpodNotLoaded;   // rtclpod_not_loaded = (1959, "Reticle Pod [%s] is not loaded on equipment [%s] / port [%s]. ")
    private OmCode reachMaxDurableRework2;          //rc.reach_max_durable_rework_2 = (1969, "The durable is over the max process count %s at the responsible operation %s. ")
    private OmCode somertclinvDataError;          //rc.somertclinv_data_error = (2029, "Some reticle. ")
    private OmCode spcNoResponse;   //rc.spc_check_error = (2039,"There was no response from SPC.")
    private OmCode sorterInvalidParameter;     // sorter_invalid_parameter = (2060, "Sorter job information is not normal. %s ")
    private OmCode stkTypeDifferent;           //rc.stk_type_different = (2063, "Stocker Type [%s] is not correct for this request.")
    private OmCode slmInvalidParameterForWaferDuplicate;     //rc.slm_invalid_parameter_for_wafer_duplicate = (2065, "Duplicate input parameter wafers.")
    private OmCode unexpectedNilObject;        // rc.unexpected_nil_Object =  (2206, "Inconsistency data is found in processing inquiry. Another transaction is now updating data. Try it again. If you face this message repeatedly, call system engineer.")
    private OmCode useridUnmatch;        //userid_unmatch = (2209, "UserID for WaferSorter is not valid. ")
    private OmCode startLotReservationFail;     //rc.start_lot_reservation_fail = (2042, "Start Lot Reservation Fail")
    private OmCode schdresvUnMatch;             //rc.shd_resv_un_match = (2052,"lot plan change Reservation is unmatch between parent lot [%s] and child lot [%s]. ")
    private OmCode waferSortAlreadyRunning;        //rc.wafer_sort_already_running = (2409, "There are running Job on WaferSorter. ");
    private OmCode waferSortPreviousJobNotFound;   //rc.wafer_sort_previous_job_not_found = (2410,"There are no job log for Equipment[%s]: PortGroup[%s], So no need to Execute EndSorter Operation .");
    private OmCode wafersorterSlotmapCompareError; // wafersorter_slotmap_compare_error = (2411, "The answer information of EAP isn't the same as the Request information.")
    private OmCode waitingForDataCollectionForDurable; // rc.waiting_for_data_collection_for_durable = (2424,"[%s] is on WDCH HOLD. The Durable is waiting for equipment data acquisition completion. Please wait for a few minutes and retry the operation. \r\n If you get this message again, please notify a system engineer.")
    private OmCode notFoundRTD;                 //rc.not_found_rtd = (2832, "RTD information has not been found.")
    private OmCode noNeedFixture;               //rc.no_need_fixture = (2836, "No Need tool.")
    private OmCode notFoundPhysicalRecipe;    // not_found_physical_recipe = (2856, "PhysicalRecipe not found. ")
    private OmCode notValidAct;             //rc.not_valid_act = (2857, "not valid act")
    private OmCode notFoundPortgroup;  // not_found_portgroup = (2858, "PortGroup[%s] is not found.")
    private OmCode notFoundSlot;       // not_found_slot = (2859, "Slot is not found. ")
    private OmCode notFoundScrap;        // not_found_scrap = (2860, "Scrap wafer is not found. ")
    private OmCode NotFoundSlotMapRecord;          //rc.not_found_slot_map_record = (2861,"The record which meets a reference condition isn't found. ");
    private OmCode notFoundWaferIdRead;        //rc.not_found_wafer_id_read = (2867, "It isn't dealing with WaferIdRead.")
    private OmCode notSelectAllFlowBatchLots; //rc.not_select_all_flow_batch_lots = (2875, "All Lots of FlowBatch aren't contained.")
    private OmCode noStockerForCurrentEqp;                  //rc.no_stocker_for_current_eqp = (2897, "Auto-Stocker is not found for the carrier on Port [%s] of Equipment [%s].")
    private OmCode noXferNeeded;                //rc.no_xfer_needed = (2898, "No Transfer Job is needed for the carrier on Port [%s] of Equipment [%s].")
    private OmCode invalidUseWildCard;           //rc.invalid_use_wildcard = (2979, Wildcard of [%s] is carried out in invalid use. )
    private OmCode invalidDestWaferPosition; // invalid_dest_wafer_position = (2915, "Requested wafer's [%s] destination slot is not empty. [%s:%s].")
    private OmCode invalidSlotnumberForWaferidminiread;  // invalid_slotnumber_for_waferidminiread = (2956, "Invalid slotnumber was specified for WaferIDMiniRead. ")
    private OmCode invalidSiviewflagForWaferidposchange;  // invalid_siviewflag_for_waferidposchange = (2957, "OMS Un-registered Carriers are specified for PositionChange.")
    private OmCode invalidSiviewflagForJustin;       // invalid_siviewflag_for_justin = (2958, "Destination carrier must be managed by SiView, and Original carrier must NOT be managed by SiView.")
    private OmCode invalidSiviewflagForJustout;       // invalid_siviewflag_for_justout = (2959, "Destination Cassette must NOT be managed by SiView, and Original Cassette must be managed by SiView.")
    private OmCode invalidDirection;        // invalid_direction = (2963, "WaferSorter Action Parameter(Direction) is Invalid. ")
    private OmCode invalidSorterstatus;    // invalid_sorterstatus = (2964, "WaferSorter Action Parameter(SorterStatus) is Invalid. ")
    private OmCode invalidMMResult;         //rc.invalid_mm_result = (2967, "There is return value from OMS is invalid.")
    private OmCode invalidTcsResult;       //rc.invalid_tcs_result = (2968, "There is return value from EAP is invalid.")
    private OmCode invalidUnregisteredCarrier;        // invalid_unregistered_carrier = (2969, "Specified Carrier is not OMS Unregistered Carrier. ")
    private OmCode invalidRegisteredCarrier;  // invalid_registered_carrier = (2970, "Specified Carrier is not OMS Unregistered Carrier. ")
    private OmCode invalidWaferIdReadResultForVLRP;   //rc.invalid_wafer_id_read_result_for_blrp = (2972,"Invalid WaferID Read Result For VendorLotReceiveAndPreparation. ");
    private OmCode invalidCastDispAttr;   // invalid_cast_disp_attr = (2984,"The carrier dispatch attribute[%s] is invalid. ")
    private OmCode lotHasMonitorGroup;                //rc.lot_has_monitor_group = (3008, "Lot [%s] has monitor group information. Please try again after 'Monitor Group Release'.")
    private OmCode invalidPortAccessMode;       //rc.invalid_port_access_mode = (3021, "Port [%s] Access Mode [%s] is invalid.")
    private OmCode invalidPortDispatchStatus;  //rc.invalid_port_dispatch_status = (3022, "Port [%s] Dispatch Status [%s] is invalid.")
    private OmCode inputSorterjobInformationDuplicate;  //input_sorter_job_information_duplicate = (3026, "Duplicate Sorter Job Information detected in input data.")
    private OmCode invalidComponentjobStatus;  // invalid_componentjob_status = (3027, "Component Job %s status % is invalid for the request.( Sort Job ID %s / Sorter Job Status %s )")
    private OmCode invalidPortCountInPortgoup;  // invalid_port_count_in_portgoup = (3028, "The number of Port in Port Group is more than %s.")
    private OmCode invalidPortOperationMode;   // invalid_port_operation_mode = (3029, "Specified port %s's port operation mode [%s] is invalid for this request. ")
    private OmCode invalidPurposeTypeForSorter;  // invalid_purpose_type_for_sorter = (3030, "Port loadPurposeType %s is invalid for Wafer Sorter.")
    private OmCode invalidSorterJobStatus;      //rc.invalid_sorter_job_status = (3032, "Sorter Job %s status %s is invalid for the request. ")
    private OmCode invalidSorterComponentJobStatus;     //rc.invalid_sorter_component_job_status=(3035, "Sorter Component Job %s status %s is invalid for the request. ")
    private OmCode invalidSorterJobType;        //rc.invalid_sorter_job_type = (3034, "Sorter Job Type %s is invalid data. ")
    private OmCode waferNotBelongLot;         //rc.wafer_not_belong_lot=(3036,"Wafer does not belong to the same Lot")
    private OmCode sorterDataConsistent;         //rc.sorter_data_consistent=("3037","DB and device data have been consistent")
    private OmCode invalidCjstatus;            // invalid_cjstatus = (3049, "Control job status [%s] is invalid.")
    private OmCode invalidParametervalueUsecurrent;   //invalid_parametervalue_usecurrent = (3112, "ParameterValue for [%s] cannot be changed when UseCurrentValueFlag is TRUE")
    private OmCode notFoundFssortjob;      // not_found_fssortjob = (3209, "Sorter Job %s Information has not been found. ")
    private OmCode interfabBranchCancelError; // interfab_branch_cancel_error = (3069, "There is the InterFab xfer plan on this process. ")
    private OmCode notFoundSorterjob;         // not_found_sorterjob = (3212, "Specified Sorter Job %s doesn't exist.")
    private OmCode notFoundSorterjobComponent;  // not_found_sorterjob_component = (3213, "Specified Sorter Component Job %s doesn't exist.")
    private OmCode notFoundSorterPostAct;      //rc.not_found_sorter_post_act=(3223,"Sorter postAct doesn't exist.")
    private OmCode noNeedWaferPreparation;         //rc.no_need_wafer_preparation = (3241,"All wafers had already been prepared. No need to prepare wafers.");
    private OmCode invalidDispatchControlInformation; //
    private OmCode explotAlreadyChanged;//explot_already_changed = (571, "Target PSM already changed.")
    /**
     * duplicate_recipefilename = (411, "The combination of file (site:%s + file name:%s) is already used for another machine recipe ID %s. ")
     */
    private OmCode duplicateRecipefilename;
    /**
     * invalid_recipefilename = (987, "Specified file name is not correct. %s. ")
     */
    private OmCode invalidRecipefilename;
    private OmCode interfabInvalidXferstateDeleting;    //interfab_invalid_xferstate_deleting = (3082, "Lot [%s] InterFab transfer state is [Deleting]. Therefore this request is rejected.")
    private OmCode cassetteNotAvailable;         // cassette_not_available = (352, "Destination carrier [%s] is not available. ")


    private OmCode commitMaxCountOver;      //rc.commit_max_count_over = (392, "TX_COMMIT count is limit over.")

    private OmCode someDataFailed;          //rc.some_data_failed = (2097, "Some object have failed in doing the request.")

    private OmCode noResponseDcs;     //(2889, "There was no response from DCS. DCS server manager may not be available.");
    private OmCode foundSlotmap;     //found_slotmap = (622, "Slotmap for carrier[%s] already exist.")
    private OmCode notFoundBondingMap;            // rc.not_found_bonding_map = (3247, "Bonding Map for Lot [%s] Wafer [%s] has not been found.")
    private OmCode bondgrpLotQuantityUnmatch;//rc.bondgrp_lot_quantity_unmatch=(209, "Unmatch the number of wafers in the Bonding Group with Quantity of Lot [%s].")
    private OmCode bondgrpStateInvalid;//rc.bondgrp_state_invalid=(210, "Bonding Group State [%s] is Invalid for Request [%s].")
    private OmCode bondgrpWaferDuplicate;//rc.bondgrp_wafer_duplicate=(211, "Wafer [%s] is duplicated in the Bonding Group.")
    private OmCode bondingTopProdspecMismatch;//rc.bonding_top_prodspec_mismatch=(212, "Product [%s] is not Top Product of Base Product [%s].")
    private OmCode bondmapStateInvalid;//rc.bondmap_state_invalid=(213, "Bonding Map State [%s] of Base Wafer [%s] is Invalid.")
    private OmCode bondgrpTargetEqpMismatch;//rc.bondgrp_target_eqp_mismatch=(214, "Equipment [%s] is different from Target Equipment [%s] for Bonding Group [%s].")
    private OmCode bondEqpCtrljobRequired;//rc.bond_eqp_ctrljob_required=(215, "Specified Operation [%s] cannot be performed without Start Lot reservation when Equipment Category is WaferBonding.")
    private OmCode bondingToplotNotSpecified;//rc.bonding_toplot_not_specified=(216, "Top Lot to Base Wafer [%s] is not specified.")
    private OmCode bondgrpWaferMissedForRelease;//rc.bondgrp_wafer_missed_for_release=(217, "Wafer [%s] in Lot [%s] should be released at the same time.")
    private OmCode bondingBaseLotExists;//rc.bonding_base_lot_exists=(218, "Base Lot of specified Lot exists.")
    private OmCode bondgrpInvalidEqp;//rc.bondgrp_invalid_eqp=(219, "Lot [%s] cannot be processed by Equipment [%s] at Bonding Operation [%s].")
    private OmCode bondgrpNotConfigEqpPcs;//rc.bondgrp_not_config_eqp_pcs=(220, "PCS property required for wafer bonding is not configured on the port of EQP.")
    private OmCode bondgrpNotMatchBaseOrTop;//rc.bondgrp_not_match_base_or_top=(221, "The current lot in Carrier does not match the port %s [%s].")
    private OmCode bondgrpNotAllReserved;//rc.bondgrp_not_all_reserved=(222, "All lots in the bonding group are not reserved at the same time.")
    private OmCode notSameRecipe;//not_same_recipe = (1531, "Lot %s's recipe is different from that of others'.")
    private OmCode toplotSpecifiedAsMonitoringlot;//toplot_specified_as_monitoringlot = (3251,"Top Lot [%s] cannot be specified as monitoring lot.")
    private OmCode toplotIncludedInMonitorgroup;//toplot_included_in_monitorgroup = (2114,"Top Lot [%s] is included in monitor group. Relation should be canceled before performing operation.")
    private OmCode alreadyBondingMapResultReported;//already_bonding_map_result_reported = (157, "Bonding Map Result for Lot [%s] Wafer [%s] is already reported.")
    private OmCode invalidParmForBondingMapReport;//invalid_parm_for_bonding_map_report = (3110,"Invalid Parameter. %s is not specified in Bonding Map.")
    private OmCode partsNotDefinedForProcess;//parts_not_defined_for_process = (1756,"Parts are not defined for Product [%s] Process [%s].")
    private OmCode recipeConfirmError; //recipe_confirm_error = (1927, "Recipe Body Confirmation error. The Recipe Body differs between Uploaded it to system and the owned it by equipment. %s")
    private OmCode noticeMaxcountOver; // (2895, "As a result of searching by the specified conditions, the information over the maximum number of cases was found.\nPlease search again after increasing the maximum number of cases or narrowing down information on reference conditions.")
    private OmCode invalidModeChangeForSorter;        //invalid_mode_change_for_sorter = (3033, "invalid mode change for sorter")
    private OmCode notBaseProdForBonding;//not_base_prod_for_bonding = (3245,"Input Product [%s] is not Base Product for Bonding Process.")
    private OmCode notSameFlowsection;//not_same_flowsection = (3250,"Flow Section of Lot [%s] and Lot [%s] is not same.")
    private OmCode notSameLogicalRecipe;//not_same_logical_recipe = (1524,"Lot %s's process condition (logical recipe) is not the same as that for other lots. ")
    private OmCode notFoundBbinsum;//not_found_binsum = (2840,"Bin Summary Data is not found. ")
    private OmCode tcsMMTapPPConfirmError; // tcs_mm_tap_pp_confirm_error = (5914, "EAP CONFIRM ERROR")
    private OmCode interfabXferplanSpecified; //interfab_xferplan_specified = (3095, "The InterFab xfer plan is specified on process[%s]/OpeNo[%s].")
    private OmCode cannotPassOperationForBondinggroup; //cannot_pass_operation_for_bondinggroup = (391, "The operation number [%s] is target operation of bonding flow section.")
    private OmCode checkDcdefAndRecipeSetting; //check_dcdef_and_recipe_setting = (397, "There is a problem in DCDef/Recipe setting for Logical recipe. Check the setting.")
    private OmCode blankInputParameter;//(207,"blank_input_parameter." );
    private OmCode notFoundCstInEqp;//not_found_cst_in_eqp = (1430, "carrier %s is in the process group, but not in the equipment. ")
    private OmCode notFoundCassetteInBuffer;//rc.not_found_cassette_in_buffer = (2864, "carrier [%s] is not found in Internal Buffer of equipment [%s]. ")
    private OmCode cassetteNotUnloadReserved;//rc.cassette_not_unload_reserved = (350, "carrier [%s] is not reserved for unloading. ")
    private OmCode failToResetReservedUnloadPort;//fail_to_reset_reserved_unload_port = (623, "carrier [%s] is not found in Internal Buffer of equipment [%s] . Failed to reset reserved unload port. ")
    private OmCode postprocKeyRequired;//postproc_key_required = (1761, "Please specify dKey when requesting additional information.")
    private OmCode invalidObjectType;//invalid_object_type = (3057, "Object Type [%s] is not valid.")
    private OmCode notFoundObjectId;//not_found_object_id = (3236, "ObjectID [%s] of ObjectType [%s] does not exist")
    private OmCode ftrwkUpdate; //ftrwk_update = (635, "The future rework request is updated.")
    private OmCode ftrwkReadded; //ftrwk_readded = (633, "The future rework request is registered two times or more at one operation.")
    private OmCode ftrwkDuplicate; //ftrwk_duplicate = (631, "More than two kinds of future rework requests are registered at one operation.")
    private OmCode ftrwkAlreadyExist; //ftrwk_already_exist = (629, "The future rework request already exists. So the registration is not done.")
    private OmCode ftrwkDataInvalid; //ftrwk_data_invalid = (630, "The item of future rework request is invalid. [%s: %s]")
    private OmCode duplicatePostprocFlt; //duplicate_postproc_flt = (429, "ObjectID [%s] of ObjectType [%s] is existing in database.")
    private OmCode notFoundStackedWafer; // rc.not_found_stacked_wafer = (3248, "Stacked Wafer Information has not been found.")
    private OmCode eqpNotInWaferStacking; // rc.eqp_not_in_wafer_stacking=(567, "The first PostProcess information of Equipment [%s] is not 'WaferStacking.'");
    private OmCode stackCancelOtherWaferStacked; // rc.stack_cancel_other_wafer_stacked = (2095, "Wafer [%s] is stacked after Top Wafer [%s] on Base Wafer [%s].")
    private OmCode notFoundContainers; // rc.not_found_containers = (3229, "The equipment [%s] does not have machine containers.")
    private OmCode noneSlmOpe; // rc.none_slm_ope = (3237, "This controlJob does not require FMC operation.")
    private OmCode notEnoughContainerPosSpace; // rc.not_enough_container_pos_space = (3228, "The starting wafer count [%s] is over equipment container position space [%s].")
    private OmCode wfRetrievedInCast; // rc.wf_retrieved_in_cast = (2416, "Some wafers [%s .etc] are retrieved from equipment container.")
    private OmCode invalidSlotNo; // rc.invalid_slot_no = (3090, "Slot number %s is not valid.)
    private OmCode slmCtrlJobRequired; // rc.slm_ctrljob_required = (2087, "move in cannot be performed without Start Lot reservation when FMC Capability is Yes.")
    private OmCode undefinedDestCastSlm; // rc.undefined_dest_cast_slm = (2214,"The carrier [%s] is not defined as destination carrier for FMC.")
    private OmCode notSpecifySlmInfo; // rc.not_specify_slm_info = (3224, "The retrieving information must be empty when equipment's FMC switch is OFF.")
    private OmCode notSpecifyEmptycastSlmrsv; // rc.not_specify_emptycast_slmrsv = (3226, "Empty carrier can NOT be specified when equipment's FMC switch is ON.")
    private OmCode totalWaferOverMaxrsvCount; // rc.total_wafer_over_maxrsv_count = (5916, "The starting wafers count [%s] and wafers that is already in equipment count [%s] exceed max reserve count [%s].")
    private OmCode slmInvalidParameterForSlotDuplicate; // rc.slm_invalid_parameter_for_slot_duplicate = (2064, "Duplicate input parameter slotmap.")
    private OmCode notUseSlot; // rc.not_use_slot = (3227, "Other wafer [%s] exist in slot [%s] of carrier [%s].")
    private OmCode slmInvalidDstMapForTopLot; // rc.slm_invalid_dstmap_for_toplot = (2093, "Destination Map cannot be changed for Top Lot.")
    private OmCode slmDstcastReservedAnotherCtrljob; // rc.slm_dstcast_reserved_another_ctrljob = (2081, "The specified destination carrier [%s] was reserved by another control job.")
    private OmCode mismatchDestCastCategory; // rc.mismatch_dest_cast_category = (1318, "The destination carrier [%s] category [%s] is not in agreement with required carrier category [%s] of Lot's current operation, or required carrier category [%s] of Lot's next operation.")
    private OmCode slmInvalidParameterSLMSwitch; // rc.slm_invalid_parameter_fmcmode = (2067, "The specified FMC Mode [%s] is invalid.")
    private OmCode slmSLMSwitchSame; //rc.slm_slmswitch_same = (2083, "The specified FMCMode [%s] is the same with current one. ")
    private OmCode notEnoughEmptycastFromAllaround; // rc.not_enough_emptycast_from_allaround = (3221, "There is not a enough empty carrier from all around.")
    private OmCode invalidSlmActionCode; // rc.invalid_slm_action_code = (3053, "The specified Action Code [%s] is invalid.")
    private OmCode slmInvalidParameterForPortrsv; // rc.slm_invalid_parameter_for_portrsv = (2070, "Invalid parameter. Action Code is PortReserve/PortReserveCancel for FMC Wafer Retrieving Reservation.\n cassetteID [%s] \n destPortID  [%s] \n controlJobID [%s] .")
    private OmCode slmEqpOnlinemodeForCastrsv; // rc.slm_eqp_onlinemode_for_castrsv = (2071, "Equipment online mode error Action Code is [%s] for FMC Wafer Retrieving Reservation.\n If controljob is blank, its mode must be Offline. ControlJob [%s] OnlineMode [%s].")
    private OmCode slmInvalidDstmapForCastrsv; // rc.slm_invalid_dstmap_for_castrsv = (2077, "The specified destination Map wafer of MtrlOutSpec is not in the specified control job [%s] 's Lot [%s](wafer).")
    private OmCode slmInvalidSrcmapForCastrsv; // rc.slm_invalid_srcmap_for_castrsv = (2075, "The specified source Map wafer of MtrlOutSpec is not in the specified control job [%s] 's Lot [%s](wafer).")
    private OmCode slmInvalidParameterForWafermap; // rc.slm_invalid_parameter_for_wafermap = (2066, "The specified lot's [%s] wafers and the specified wafers are different.")
    private OmCode invalidSlmStateRetrieved; // rc.invalid_slm_state_retrieved = (3050, "Some wafer's FMC State is [Retrieved].")
    private OmCode invalidSlmStateStored; // rc.invalid_slm_state_stored = (3051, "All FMC state of all wafers are not [Stored].")
    private OmCode invalidSlmStateReserved; // rc.invalid_slm_state_reserved = (3052, "All FMC state of all wafers are not [Reserved].")
    private OmCode invalidLottypeForEqpmonitor;  // invalid_lottype_for_eqpmonitor = (3126, "Lot Type [%s] of the lot is invalid for lot auto monitor. Lot's LotType must be 'Auto Monitor' or 'Dummy'.")
    private OmCode notReservedSlmAllwafers; // rc.not_reserved_slm_allwafers = (3230, "All the specified wafers are not in Equipment Container Position.")
    private OmCode alreadyReservedPortSlm; // rc.already_reserved_port_slm = (153, "The port [%s] is already reserved for FMC.")
    private OmCode slmEqpPortAccessMode; // rc.slm_eqp_port_access_mode = (2079, "Equipment [%s] / port [%s] access mode is [%s] now.")
    private OmCode carrierPortCarrierCategoryUnmatch; // rc.carrier_port_carrier_category_unmatch = (384, "Carrier whose category is [%s] cannot be loaded for that equipment port [%s/%s].")
    private OmCode slmSrcastNotExist; // rc.slm_srcast_not_exist = (2078, "The source carrier is blank in Equipment Container Position Information. controljob [%s], lotID [%s], waferID [%s]")
    private OmCode notFoundWorkarea; // rc.not_found_workarea = (1509, "Specified workarea %s has not been found. Please call system engineer.")
    private OmCode notFoundArea; // rc.not_found_area = (1420, "bay %s information has not been found. ")
    private OmCode notFoundApcIFPoint; // rc.not_found_apcifpoint = (2890, "Not Found APCIFPOINT.")
    private OmCode invalidLotSituation; // rc.rc_invalid_lot_situation = (935, "This Lot %s Condition is invalid for this request. ")
    private OmCode lotHasBondingGroup; // rc.rc_lot_has_bondinggroup = (1274, "Lot [%s] has bonding group [%s]. Specified operation cannot be executed.")
    private OmCode mtrlOutSpecCombinationError; // rc.mtrl_out_spec_combination_error = (1320, "The Material Out Spec (Source Map vs Destination Map) combination is not available.")
    private OmCode slmWaferNotFoundInPosition;  //rc.rc_slm_wafer_not_found_in_position = (2074, "The specified wafer [%s] is not found in equipment container.")
    private OmCode wrongCastForRetrievingWafer; //rc.rc_wrong_cast_for_retrieving_wafer = (2417, "The carrier [%s] isn't reserved for retrieving wafer [%s] in FMC operation.")
    private OmCode slotNoMismatchSlotmapEqpctnpst; // rc.rc_slot_no_mismatch_slotmap_eqpctnpst = (2082, "Slot numbers aren't match between slotmap and equipment container. WaferID [%s]")
    private OmCode eqpOffline; //rc.rc_eqp_offline = (513, "Equipment %s is now OFFLINE. Please check the On-Line-Status.")
    private OmCode invalidDataType; // rc.invalid_data_type = (909, "Invalid Data Type.")
    private OmCode someDataValBlank; // rc.some_dataval_blank = (453, "Some inputted data values are null. Please fill in and try again.")
    private OmCode noWipDurable; // rc.no_wip_durable = (3271,"There is no waiting durable.")
    private OmCode notDurableStartbank; // rc.not_durable_startbank = (3273, "The Bank %s is not start bank of the Durable process.")
    private OmCode notFoundPosForDurable; // rc.not_found_pos_for_durable = (3274, "Process operation specification information for durable %s has not been found. ")
    private OmCode notFoundDurableSubroute; // rc.not_found_durable_subroute = (3276,"Any subRoute connected to %s has not been found for durable %s. ")
    private OmCode notFoundDurablepo; // rc.not_found_durablepo = (3265, "Durable Process Operation information for Durable:%s is not found.")
    private OmCode invalidDurableStatBankincancel; // rc.invalid_durable_stat_bankincancel = (3140, "The request will be acceptable only when the status is NOTAVAILABLE/SCRAPPED because the object [%s] will return on floor. Please change the status first.")
    private OmCode notAllowedDelete; // rc.not_allowed_delete = (1573, "The object is not allowed to delete.")
    private OmCode allDurableFailed; // rc.all_durable_failed = (162, "All requested Durable-Changes have failed.")
    private OmCode invalidMaxrsvcount; // rc.invalid_maxrsvcount = (3054, "Input max reservable count of equipment container is out of valid range (0 - 999).")
    private OmCode invalidDurableReworkOperation; // rc.invalid_durable_rework_operation = (3139, "The durable cannot move to rework process at the first process operation.")
    private OmCode maxrsvcountOverCapacity; // rc.maxrsvcount_over_capacity = (1319, "Input FMC max reservable count [%s] is over the capacity [%s].")
    private OmCode invalidDctrljobactionForDcjstatus; // rc.invalid_dctrljobaction_for_dcjstatus = (3131, "The requested DurableControlJobAction is invalid for the current status [%s] of the DurableControlJob [%s].")
    private OmCode rdjNotFound; //rc.rdj_not_found = (1935, "Specified Reticle Dispatch Job has not been found.")
    private OmCode rdjDuplicate; //rc.rdj_duplicate = (1932, "Duplicate RDJ ID detected.")
    private OmCode rdjStatusError; //rc.rdj_status_error = (1937, "Request is refused because current status of RDJ is '%s'.")
    private OmCode mismatchRdjrcjStatus; //rc.mismatch_rdjrcj_status = (1314, "RDJ/RCJ status is mismatched.")
    private OmCode rdjNotMatchRequest; //rc.rdj_not_match_request = (1936, "Request is not match with RDJ.")
    private OmCode rcjNotFound; //rc.rcj_not_found = (1930, "Specified Reticle Component Job has not been found.")
    private OmCode rcjNotMatchRequest; //rc.rcj_not_match_request = (1931, "Request is not match with RCJ.")
    private OmCode armsTcsReportedError; //arms_tcs_reported_error = (149,"TCS reported reticle job result with error flag.")
    private OmCode rcjIncomplete; //rcj_incomplete = (1929,"Incomplete RCJ Record detected.")
    private OmCode diffRtclpodReserved; //diff_rtclpod_reserved = (427,"Equipment [%s] reticle pod port [%s] is reserved for different reticle pod [%s]. ")
    private OmCode diffRtclpodPortReserved; //diff_rtclpod_port_reserved = (426,"Reticle pod [%s] is reserved to different reticle pod port [%s]. ")
    private OmCode rtclpodDestDifferent; //rtclpod_dest_different = (1955,"Reticle pod [%s] is reserved for different destination equipment [%s]. ")
    private OmCode monitorbankStartbankCombinationError; // Equipment's Monitor Bank and STB Lot Start Bank is different. Please Check Spec. Manager data condition.

    //==================================================================//
    //                  OMS Defined PCS Error Code                      //
    //==================================================================//
    private OmCode pcsError; // rc.pcs_error = (20000, "PCS Server Error.")
    private OmCode pcsVariableUndefined; // rc.pcs_variable_undefined = (20001, "Script variable[%s] undefined.")
    private OmCode pcsVariableNameIsNull; // rc.pcs_variable_name_is_null = (20002, "Script variable name is null.")
    private OmCode pcsVariableTypeNotString; // rc.pcs_variable_type_not_string = (20003, "Variable[%s] type for script is not String.")
    private OmCode pcsVariableTypeNotInteger; // rc.pcs_variable_type_not_integer = (20004, "Variable[%s] type for script is not Integer.")
    private OmCode pcsVariableTypeNotDecimal; // rc.pcs_variable_type_not_decimal = (20005, "Variable[%s] type for script is not Decimal.")
    private OmCode pcsVariableTypeNotIntegerMap;  // rc.pcs_variable_type_not_integer_map = (20006, "Variable[%s] type for script is not Integer Map.")
    private OmCode pcsVariableTypeNotStringMap;  // rc.pcs_variable_type_not_string_map = (20007, "Variable[%s] type for script is not String Map.")
    private OmCode pcsVariableTypeNotDecimalMap;  // rc.pcs_variable_type_not_decimal_map = (20008, "Variable[%s] type for script is not Decimal Map.")
    private OmCode pcsVariableForSpecTypeUndefined;  // rc.pcs_variable_for_spec_type_undefined = (20009, "Variable[%s] for type[%s] undefined.")

    //==================================================================//
    //                  OMS Defined Season Error Code                      //
    //==================================================================//
    private OmCode seasonIdRequired; // rc.season_id_required = (30000, "Season Id is required.")
    private OmCode seasonProductRequired; // rc.season_product_required = (30001, "Season product is required.")
    private OmCode seasonProductEmpty; // rc.season_product_empty = (30002, "Season product can not be empty.")
    private OmCode invalidRefkey; // rc.invalid_refkey = (30003, "invalid refkey.")
    private OmCode seasonWaferCountEmpty; // rc.season_wafer_count_empty = (30004, "Season wafer count can not be empty.")
    private OmCode noIdleFlagError; // rc.no_idle_flag_error = (30005, "When season group flag is false, no idle flag cannot be true.")
    private OmCode recipeGroupParamError; // rc.recipe_group_param_error = (30006, "When season type is recipe group, from_recipe_group and to_recipe_group must be specified.")
    private OmCode recipeGroupIdError; // rc.recipe_group_id_error = (30007, "Wrong recipe group id.")
    private OmCode maxIdleTimeEmpty; // rc.max_idle_time_empty = (30008, "max_idle_time cannot be empty.")
    private OmCode intervalBetweenSeasonEmpty; // rc.interval_between_season_empty = (30009, "Interval Time cannot be empty.")
    private OmCode seasonNoExist; // rc.season_no_exist = (30010, "season plan does not exist.")
    private OmCode seasonRequired; // rc.season_required = (30011, "Season of %s is required.")
    private OmCode minWaferCountError; // rc.min_wafer_count_error = (30012, "season wafer count is less than %s")
    private OmCode productLotRequired; // rc.product_lot_required = (30013, "Product lot is required.")
    private OmCode equipmentRequired; // rc.equipment_required = (30014, "Equipment is required.")
    private OmCode lotNotMeetSeason; // rc.lot_not_meet_season = (30015, "This lot does not meet the season condition.")
    private OmCode equipmentDoingSeasoning; // rc.equipment_doing_seasoning = (30016, "The equipment is doing seasoning.")
    private OmCode lotOutOfCassette;                   //lot_out_of_cassette = (99998, "lot out of carrier")
    private OmCode durableCannotHoldReleaseWithLocr;                   //durable_cannot_hold_release_with_locr = (444, "This reason(LOCR) can be specified only for Internal Lock Release. Durable does not have LOCK Hold Record. ")
    private OmCode durableCannotHoldReleaseForLock;                   //durable_cannot_hold_release_for_lock = (445, "This hold record (LOCK) is for post-processing of task.\r\n And it's not released until some post-processings are completed. \r\n If you get this message again after you wait for a few minutes, please check the durable post-processing queue information. \n")
    private OmCode invalidDurableProdstat;                   //invalid_durable_prodstat = (3138, "Durable %s productionStatus %s is invalid for this request. ")
    private OmCode durableNotInSubroute;                   //durable_not_in_subroute = (446, "This Durable is not in a SubRoute. ")
    private OmCode noChamberEqpNotUniqueIdlePlan;    //rc.no_chamber_eqp_not_unique_idle_plan = (447, "No chamber equipment can only create one Idle Time Season Plan ")
    private OmCode noChamberEqpNotUniquePMPlan;    //rc.no_chamber_eqp_not_unique_pm_plan = (451, "No chamber equipment can only create one PM Season Plan ")
    private OmCode recipeIdleSeasonPlanSameRecipeAndTime;    //rc.recipe_idle_season_plan_same_recipe_and_time = (448, "different Recipe Idle time Season plan can't have same Recipe and idle time")
    private OmCode differentRecipeGroupPlanSameSetting;    //rc.different_recipe_group_plan_same_setting = (452, "Different Recipe Group Season plan can't have same Recipe Group direction settings ")
    private OmCode differentDilePlanSameChamber;                   //rc.different_dile_plan_same_chamber = (454, "different Season Plan cannot choose same chamber ")
    private OmCode differentDilePlanSameIdleTime;                   //rc.different_dile_plan_same_idle_time = (455, "different Season Plan cannot have same idle Time ")
    private OmCode idleSeasonPlanMultiChamber;                   //rc.idle_season_plan_multi_chamber = (456, "idle time season plan can only choose one chamber")
    private OmCode notAllowedSeasonSetting;                   //rc.not_allowed_season_setting = (457, "this season setting is not allowed")

    //-----------------------------------------------------------
    // DMS Error Code
    //-----------------------------------------------------------
    private OmCode eqpSpecialControlTxIdMismatch; // rc.eqp_specialcontrol_txid_mismatch = (572, Transaction ID [%s] and Special Equipment Control are mismatch.)
    private OmCode durableOnRouteStatNotSame; // rc.durable_onroute_stat_not_same = (449, "OnRoute state of each durable is not same.")
    private OmCode notMatchStartBankForDurable; // rc.not_match_startbank_for_durable = (3277, "The durable %s is not in start bank of the Durable process.")
    private OmCode invalidDurableStat; // rc.invalid_durable_stat = (3137, "Durable Status %s is invalid for the request.")
    private OmCode notFoundDctrlJob; // rc.not_found_dctrljob = (3267, "DurableControlJob is not found.")
    private OmCode durableStatusProcessing; // rc.durable_status_processing = (50000, "Lot Status is in-process.")


    //==================================================================//
    //                  OMS Defined RunCard Error Code                      //
    //==================================================================//
    private OmCode notFoundRunCard; //rc.not_found_run_card = (40000, "run card information not found")
    private OmCode noSettingApproveUser; //rc.no_setting_approve_user = (400001, "no setting approve user")
    private OmCode alreadyApprovedRunCard; //rc.already_approved_run_card = (400002, "the run card [%s] had already approve to active")
    private OmCode alreadyExsitRunCard; //rc.already_exsit_run_card  = (400003, "run card data have already exsit by the Lot [%s] ")
    private OmCode alreadySummitRunCard; //rc.already_submit_run_card = (400004,"the user [%s] aleray submit the run card [%s]")
    private OmCode noResponseFromFWP; //rc.no_response_from_fwp = (400005,"no response from fwp")
    private OmCode noAuthOperateRunCard; //rc.no_auth_operate_run_card = (400006,"[%s] have no auth to operate the run card [%s]")
    private OmCode invalidRunCardState; //rc.invalid_run_card_state = (400007,"the run card [%s] state [%s] is invalid")
    private OmCode noSettingPsmOrDoc;  //rc.no_setting_psm_or_doc =  (400008,"the run card [%s] must set psm or doc")
    private OmCode cannotModifyPsm;  //rc.can_not_modify_psm =  (400009,"can not modify psm because the psm had setting doc infomation")
    private OmCode disableDoc;  //rc.disable_doc =  (400010, "OM_DISABLE_DOC is true")
    private OmCode disablePsm;  //rc.disable_psm =  (400011, "OM_DISABLE_PSM is true")
    private OmCode disableRuncard;  //rc.disable_runcard =  (400012, "OM_DISABLE_RUNCARD is true")
    private OmCode notFoundRunCardApprovalUserGroup; //rc.not_found_run_card_approval_user_group = (400013,"not found run card approval user group setting")
    private OmCode alreadyExsitPsm; //rc.already_exsit_psm  = (400014, "PSM data have already exsit by the Lot [%s] ")
    private OmCode alreadyExsitDoc; //rc.already_exsit_doc  = (400015, "DOC data have already exsit by the Lot [%s] ")

    //==================================================================//
    //                  OMS Defined Job Status Code                     //
    //==================================================================//
    private OmCode notFoundJobStatus; // rc.not_found_job_status = (50001,"not found job status.")
    private OmCode durableJobStatusChangeFail; //rc.durable_job_status_change_fail = (50002,"cannot change status [%s] to [%s].")
    private OmCode notFoundFilledDurable; //rc.not_found_filled_durable = (50050, "A Durable for DurableCarrierTransferReq is not available for delivery . (durable count is less than required Durable count)");

    //==================================================================//
    //                  OMS Defined ARHS Status Code                    //
    //==================================================================//
    private OmCode unKnownReticlePosition;                          //rc.unknown_reticle_position = (2213, "Current reticle position is unknown.")
    private OmCode alreadyReservedReticle;                          //rc.already_reserved_reticle = (146, "Reticle has already been reserved.")
    private OmCode reticleHasControlJob;                            //rc.reticle_has_control_job = (1945, "Reticle %s has control job.")
    private OmCode invalidRdjRecord;                                //rc.invalid_rdj_record = (3043, "RDJ %s is invalid RDJ record.")
    private OmCode reticlePodNotInTheEqp;                           //rc.reticle_pod_not_in_the_eqp = (1918, "Required reticlePod [%s] is not in the equipment. Please check the transfer status.")
    private OmCode cannotUseRsp;                                    //rc.can_not_use_rsp = (380,"RSP status must be 'Available' or 'InUse' for this request.")
    private OmCode rspNotLoaded;                                    //rc.rsp_not_loaded = (1940,"RSP %s is in %s, but loaded RSPPort not found.")
    private OmCode eqpPortAccessMode;                               //rc.eqp_port_access_mode = (557,"Equipment [%s] / reticle pod port [%s] access mode is [%s] now.")
    private OmCode invalidMachinePortState;                         //rc.invalid_machine_port_state = (3039,"Specified port %s/%s's port state %s is invalid.")
    private OmCode rspNotEmpty;                                     //rc.rsp_not_empty = (1939,"Specified RSP %s is not empty. ")
    private OmCode rtclNotInReclPod;                                //rc.rtcl_not_in_recl_pod = (1948,"Reticle [%s] is not in reticle pod [%s] / slot [%s].")
    private OmCode notFoundSuitableRsp;                             //rc.not_found_suitable_rsp = (3216,"Not found suitable RSP for reticle xfer.")
    private OmCode invalidPriority;                                 //rc.invalid_priority = (3042,"Priority is Invalid. ")
    private OmCode rtclAlreadyHasSameDispStation;                   //rc.rtcl_already_has_same_disp_station = (1943,"Reticle has already had the same DispatchStation in other RDJ. ")
    private OmCode rtclAlreadyHasSameUser;                          //rc.rtcl_already_has_same_disp_user = (1944,"Reticle has already had the same User in other RDJ. ")
    private OmCode invalidRtclRdjStatus;                            //rc.invalid_rtcl_rdj_status = (3047,"Reticle RDJ Status is invalid. ")
    private OmCode arhsCannotCreateRcj;                             //rc.arhs_cannot_create_rcj = (147,"RCJ(Reticle Component Job) can not be generated because of invalid RDJ(Reticle Dispatch Job). ")
    private OmCode rdjIncomplete;                                   //rc.rdj_incomplete = (1934,"Incomplete RDJ Record detected. ")
    private OmCode foundInRcj;                                      //rc.found_in_rcj = (674,"Found some records in RCJ. Input parameters: %s ")
    private OmCode availRspPortNotFound;                            //rc.avail_rsp_port_not_found = (148,"Available RSP Port not found on %s. ")
    private OmCode rtclInRspStkWithNoRsp;                           //rc.rtcl_in_rsp_stk_with_no_rsp = (1946,"Reticle %s is in RSP Stocker %s without ReticlePod. ")
    private OmCode invalidRspPortStatus;                            //rc.invalid_rsp_port_status = (3046,"Status of RSP Port [%s/%s] is invalid for request. ")
    private OmCode eqpReticleCapacityOver;                          //rc.eqp_reticle_capacity_over = (558,"Equipment does not have enough capacity to store reticles. ")
    private OmCode invalidRequestToRdj;                             //rc.invalid_request_to_rdj = (3045,"This request invalid to RDJ.")
    private OmCode rtclPodDestInvalid;                              //rc.rtcl_pod_dest_invalid = (1956,"Reticle pod [%s] destination equipment [%s] type is invalid for this request.")
    private OmCode invalidMachinePortDispatchState;                 //rc.invalid_machine_port_dispatch_state = (3038,"Specified port %s/%s's port dispatch state %s is invalid.")
    private OmCode invalidMachinePortTransRsrvState;                //rc.invalid_machine_port_trans_rsrv_state = (3040,"Specified port %s/%s's port transferReserved state %s is invalid.")
    private OmCode rtclPodHasReticleJob;                            //rc.rtcl_pod_has_reticle_job = (1957,"Reticle pod [%s] still has some jobs with this equipment.")
    private OmCode rdjHaveNotExcuteJob;                             //rc.rdj_have_not_excute_job = (1933,"RDJ %s have %s job which is not executed yet.")
    private OmCode invalidRequestToRcj;                             //rc.invalid_request_to_rcj = (3044,"This request invalid to RCJ.")
    private OmCode invalidOnLineMode;                               //rc.invalid_on_line_mode = (3042,"Input parameter [%s] is invalid. Please fill 'Off-Line' or 'On-Line Local' or 'On-Line Remote'.")
    private OmCode onLineModeSame;                                  //rc.on_line_mode_same = (1605,"Current Online Mode [%s] of Bare Reticle Stocker [%s] is same as input parameter. ")
    private OmCode reticlePodNotOnRspPort;                          //rc.reticle_pod_not_on_rsp_port = (1963,"Specified port [%s] has a relation to ReticlePod.")
    private OmCode rtclPodResrved;                                  //rc.rtcl_pod_resrved = (1961,"Reticle pod [%s] has already been reserved for transfer. ")


    //==================================================================//
    //                  OMS Defined Contamination                       //
    //==================================================================//
    private OmCode contaminationLevelMatchState;                    // rc.contamination_level_match_state = (3281,"The contamination flag of lot do not match.")
    private OmCode contaminationPrFlagMatchState;                   // rc.contamination_pr_flag_match_state = (3282,"This equipment does not allow reservation of PR contamination lots.")
    private OmCode carrierNotEmpty;                   // rc.carrier_not_empty = (3283,"Carrier usage type is not allowed to be modified because the carrier is not empty.")
    private OmCode carrierUsageNotMatch;                   // rc.carrier_usage_not_match = (3284,"The usage type of this carrier does not match the Product")
    private OmCode contaminationChildNotMatchParentState; // rc.contamination_child_not_match_parent_state = (3285,"The contamination flags of child lot and parent lot do not match.")
    private OmCode contaminationWaferChangeState; // rc.contamination_wafer_change_state = (3286,"Lot contamination flags in the carrier are not consistent.")
    private OmCode destinationCarrierCategoryNotMatch; // rc.new_carrier_category_not_match = (3287,"Destination Carrier category do not match the requirement")
    private OmCode contaminationLevelMismatch;                    // rc.contamination_level_mismatch = (3288,"The contamination flag or the contamination out level of the lots in this CJ not match.")
    private OmCode cannotHoldWithCcmh;            //rc.cannot_hold_with_ccmh = (3289, "can't hold with CCMH")
    private OmCode reqCastCategoryNotAllowed;            //rc.req_cast_category_not_allowed= (3290, "required carrier category %s not allowed")
    private OmCode createPilotRunPlanStatuesError;   // rc.create_pilot_run_plan_statues_error = (9000,"当前状态错误应该需要其他plan状态为:Failed/Completed,才能创建，当前状态为:  %s  ")
    private OmCode createPilotRunPlanJobStatuesError;   // rc.create_pilot_run_plan_job_statues_error = (9001,"当前状态错误应该需要其他plan的job状态为:Failed/Completed,才能创建，当前状态为:  %s  ")
    private OmCode inputParameterCategorError;    //input_parameter_categor_error = (9002,"入参错误， 当前Category为:  %s  ")
    private OmCode inputParameterEntityTypeError;  //input_parameter_Entity_Type_error = (9003,"入参错误， 当前Entity Type为:  %s  ")
    private OmCode changePiLotRunPlanstatusError;    //input_parameter_categor_error = (9004,"change PiLot Run Plan status Error  当前Plan 状态是 Waiting " ")
    private OmCode changePiLotRunJobstatusError;  //input_parameter_Entity_Type_error = (9005,"change PiLot Run Job status Error 当前Plan 状态是 Created/Ongoing")
    private OmCode notFoundPiLotRunPlan; // rc.not_found_pi_lot_run_plan = (9006,"not found pilot run plan")
    private OmCode notFoundPiLotRunJob;  // rc.not_found_pi_lot_run_job = (9007,"not found pilot run job")
    private OmCode inputParameterStatusError;  //input_parameter_status_error = (9008,"入参错误， 当前Status为:  %s  ")
    private OmCode pilotJobExistError;  //rc.pilot_job_exist_error = (9009,"pilot run job already exists and cannot be created again")
    private OmCode InvalidSorterJobid;  //invalid_sorter_jobid = (3031,"SorterJobID %s is invalid data.")
    private OmCode invalidReticleLocation;       //rc.invalid_reticle_location = (9009, "reticle %s location %s is invalid for this operation")
    private OmCode repeatAliasName;       //rc.repeat_alias_name = (9010, "Wafer Alias Name %s already exist")
    private OmCode capabilityNotMatch;       //rc.capability_not_match = (9011, "Equipment's capability do not match this step requirement")
    private OmCode notFoundNextStep;   //not_found_next_step = (9012, "not found next step.")
    private OmCode notMatchOpeCastCategory; //not_match_ope_cast_category = (9013,"Current carrier category %s do not match the operation carrier category %s ")

    //########## OMS FSM ################ start
    private OmCode fsmWaferReserved;
    private OmCode fsmExecutionFail;
    private OmCode fsmOpeInvalid;
    private OmCode fsmExplotAlreadyChanged;
    //########## OMS FSM ################ end

    private OmCode lotNotSplitMerge; //lot_not_split_merge = (3255,"%s Lot is not allowed to do %s in %s")
    //==================================================================//
    //                  OMS Defined Department Authority                //
    //==================================================================//
    private OmCode departmentEqpStateNotAuthority; // rc.department_eqp_state_not_authority = (9201,"Modify device state without operation permission.")
    private OmCode departmentHoldNotAuthority; // rc.department_hold_not_authority = (9202, "")

    //########## OMS PILOT RUN ################ start
    private OmCode prPlanStatusNotAllowedToChange;
    private OmCode prCurrentStepNotAllowedToChange;
    private OmCode prPilotRunJobNotBeCreated;
    private OmCode prRepilotRun;
    private OmCode prPlsSelectPilot;
    private OmCode prPilotRunJobStatusError;
    private OmCode prPilotRunPlanStatusError;
    private OmCode prPilotStatusError;
    private OmCode prWaferCountError; //rc.pr_wafer_count_error = (9309, "[PMPilotRun] lot wafer count %s is not equal the PM PilotRun wafer count %s")
    private OmCode prRecipeNotMatch; //rc.pr_recipe_not_match = (9310, "[PMPilotRun] lot recipe %s and job recipes %s do not match")
    private OmCode prFirstRun; //rc.pr_first_run = (9311, "[PMPilotRun] Do PM PilotRun first")
    private OmCode prRecipeGroupAlreadyExists; // (9312,"recipeGroupID already exists in [%s], pls delete or update this [%s]")
    private OmCode prRecipeIncludedInGroup;// 9313,"the recipe is included in an existing group"
    private OmCode prRecipeGrouphasJob; // 9314,"this group has a job can not be deleted"
    private OmCode prCannotFoundRecipeGroup;// "can not find recipeGroup for the equipment [%s]"
    private OmCode prNotFoundRecipeGroup;//not_found_recipe_group = (9316, "Recipe Job %s information has not been found.")
    private OmCode prLotWaferCountNotZero;//not_found_recipe_group = (9317, "the number of wafer of lot [%s] cannot be zero.")
    private OmCode prRecipeIsEmpty;// (9318,"recipe not be empty,recipe must more than 0")
    //########## OMS PILOT RUN ################ end

    private OmCode ocapNoAndLotIdCanNotBeEmpty;//rc.ocapNo_and_lotId_can_not_be_empty = (60001,"OcapNo and LotId can not be Empty！")
    private OmCode ocapRemeasureAndAddmeasureConflict;//rc.ocap_remeasure_and_addmeasure_conflict = (60002,"ocap reMeasure can not operate with addMeasure at the same time！")
    private OmCode ocapSpecifiedEquipmentError;//rc.ocap_specified_equiment_error = (60003,"Ocap Specified Equiment Error！the ocap setting equipment is [%s]")
    private OmCode ocapMeasureSpecifiedWaferError;//rc.ocap_Measure_specified_wafer_error = (60004,"Ocap Measure Specified Water Error！the ocap setting wafers is [%s]")
    private OmCode ocapAddMeasureWaferIsEmpty;//rc.ocap_addMeasure_wafer_is_empty = (60005,"Ocap AddMeasure Wafer is Empty！")
    private OmCode ocapMeasureWaferListIsEmpty;//rc.ocap_measure_waferlist_is_empty = (60006,"ocap measure waferlist is empty！")
    private OmCode ocapFlowInfoNotExisted; //rc.ocap_flow_info_not_existed = (60007,"ocap flow information not existed.")
    private OmCode canNotReleaseWithOutOrlc; //rc.can_not_release_with_out_orlc = (60008,"Ocap Hold can't hold release with out ORLC")
    private OmCode canNotEditOcapHold; //rc.can_not_edit_ocap_hold = (60009,"Ocap Hold can't edit to others.")
    private OmCode ocapSpecifiedRecipeError;//rc.ocap_specified_recipe_error = (60010,"Ocap Specified Recipe Error！the ocap setting recipe is [%s]")

    //==================================================================//
    //                         sorter                                   //
    //==================================================================//
    private OmCode lotFlipAndFlipSorter;  //rc.lot_flip_and_flip_sorter=(70000, "Lot [%s] is flipped or has Flip Sorter")
    private OmCode sorterAdjustDirectionError;    //rc.sorter_adjust_direction_error=(70001, "adjust direction error")
    private OmCode sorterT7CodeReadLotNumberError;    //rc.sorter_t7_code_read_lot_number_error=(70002, "T7CodeRead the number of lot to be read cannot be greater than one")
    private OmCode sorterAdjustExist;     //rc.sorter_adjust_exist=(70003, "lot [%s] exist AdjustByMES Sorter")
    private OmCode sorterActionCodeError;      //rc.sorter_action_code_error=(70004, "The action in the current operation mode or online mode is invalid")
    private OmCode sorterCancelOnRouteError;   //rc.sorter_cancel_on_route_error=(70005, "The on_route sorter cannot cancel directly")

    //==================================================================//
    //                 Auto MoveInReserve                               //
    //==================================================================//
    private OmCode invalidEqpCategory;      //rc.invalid_eqp_category = (9204,"Equipment Category [%s] is invalid for the request")

    //########## NPW Wafer Usage Count and Recycle Count Control##########
    private OmCode npwCountOverLimit; //rc.npw_count_over_limit = (9203, "Lot %s 's Usage or Recycle count has exceed the limit")
    private OmCode notIbAndFurnaceEqp; //rc.not_ib_and_furance_eqp = (9206, "Only Internal Buffer and Furnace AB Batch eqp is allowed")
    private OmCode runningLotIbAndFurnaceEqp; //rc.running_lot_ib_and_furance_eqp = (9205, "Internal Buffer and Furnace AB Batch eqp can't move in if it has processing lot")


    //########## OMS Layout recipe ################ start
    private OmCode layoutRecipeLimitInvalidError; //rc.layout_recipe_limit_invalid_error = (9207, "The interval limit parameter is duplicated, please check.")
    private OmCode invalidLayoutControlCategoryError; //rc.invalid_layout_control_category_error = (9208, "The layout specific control Is a wrong type.")
    private OmCode layoutSpecificControlReserveError; //rc.layout_specific_control_reserve_error = (9209, "When using furnace specific control, there cannot be multiple lot recipes.")
    private OmCode layoutSpecificControlNotMatch; // rc.layout_specific_control_not_match=(9210, "The current processing sequence does not match furnace specific control.")
    //########## OMS Layout recipe ################ end


    private OmCode spMergeOperationEarly; // The merge point must be after the return operation of sub route.
    private OmCode routePdTypeDoesNotMatchProcess; // route_pd_type_does_not_match_process=(60101, "The ProcessDefinitionType[%s] of the route[%s] cannot be used for the [%s] process.")

    //########## OMS Sampling service ################ start   9300 - 9399
    private OmCode waferSamplingEdcConflict; // rc.wafer_sampling_edc_conflict=(9300, "conflict execute flag is Turn on, data collection conflicts with wafer sampling, reject.")
    //########## OMS Sampling service ################ end

    // When the lot executes Lot reserve or move in, it is rejected if it does not meet the minimum time limit
    private OmCode minQTimeLimitedToReject;
    // the chamber grouping configuration of the eqp from MDS is error
    private OmCode chamberGroupConfigError;

    private OmCode carrierParamNull;//rc.parameter_null_carrier = (3214, "Carrier parameter is null")
    private OmCode eqpIdParamNull; //rc.parameter_null_equipmentID = (3215, "The EquipmentID parameter is null")
    private OmCode portIdParamNull; //rc.parameter_null_portId = (3217, "The portId parameter is null")
    private OmCode eapReportedError; //rc.Eap_Reported_Error = (3218, "The Carrier reported by EAP error")
    private OmCode sortJobSelectError; //rc.sortjob_select_error = (3219, "The SortJob that was first processed was not selected correctly")
    private OmCode eapProcessError; //rc.eap_process_error = (3220, "The eapProcess is error")
    private OmCode aliasNameReadError; //rc.aliasname_read_error = (3221, "The AliasName Read error")
    private OmCode fosbIdQuantityExceedingLimit; //rc.fosbId_quantity_exceeding_limit = (3222, "fosbId quantity exceeding limit")
    private OmCode alreadyExistMonitorID; //9301,"this auto monitor ID [%s] is already exist,please try another one"
    private OmCode lotInSortJob; //9302,"Lot already exists in sortJob"
    private OmCode invalidTargetOperation; //(9304,"target operation [%s] must be after the tigger operation [%s]")

    //########## OMS CarrierOut Service #############
    private OmCode notFoundCarrierOutPort; //rc.not_found_carrier_out_port = (8000,"MES not found CarrierOut LP [%s]")
    private OmCode invalidCarrierOutPortSize; //rc.invalid_carrier_out_port_size = (8001,"Invalid carrier out port size [%s]")
}
