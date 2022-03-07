package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 15:22:19
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class LotOperationHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationRecord
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 15:13:09
     */
    public Response insertLotOperationHistory(Infos.Ohopehs lotOperationRecord) {
        String buff;
        String hFHOPEHS_LOT_ID       ;
        String hFHOPEHS_LOT_TYPE     ;
        String hFHOPEHS_SUB_LOT_TYPE ;
        String hFHOPEHS_CAST_ID      ;
        String hFHOPEHS_CAST_CATEGORY;
        String hFHOPEHS_MAINPD_ID    ;
        String hFHOPEHS_OPE_NO       ;
        String hFHOPEHS_PD_ID        ;
        Integer hFHOPEHS_OPE_PASS_COUNT;
        String hFHOPEHS_PD_NAME    ;
        String hFHOPEHS_CLAIM_TIME ;
        Double  hFHOPEHS_CLAIM_SHOP_DATE;
        String hFHOPEHS_CLAIM_USER_ID;
        String hFHOPEHS_MOVE_TYPE    ;
        String hFHOPEHS_OPE_CATEGORY ;
        String hFHOPEHS_PROD_TYPE    ;
        String hFHOPEHS_TEST_TYPE    ;
        String hFHOPEHS_MFG_LAYER    ;
        Integer hFHOPEHS_EXT_PRIORITY;
        Integer hFHOPEHS_PRIORITY_CLASS;
        String hFHOPEHS_PREV_PRODSPEC_ID;
        String hFHOPEHS_PRODSPEC_ID     ;
        String hFHOPEHS_PRODGRP_ID      ;
        String hFHOPEHS_TECH_ID         ;
        String hFHOPEHS_CUSTOMER_ID     ;
        String hFHOPEHS_CUSTPROD_ID     ;
        String hFHOPEHS_ORDER_NO        ;
        String hFHOPEHS_STAGE_ID        ;
        String hFHOPEHS_STAGEGRP_ID     ;
        String hFHOPEHS_PHOTO_LAYER     ;
        String hFHOPEHS_LOCATION_ID     ;
        String hFHOPEHS_AREA_ID         ;
        String hFHOPEHS_EQP_ID          ;
        String hFHOPEHS_EQP_NAME        ;
        String hFHOPEHS_OPE_MODE        ;
        String hFHOPEHS_LC_RECIPE_ID    ;
        String hFHOPEHS_RECIPE_ID       ;
        String hFHOPEHS_PH_RECIPE_ID    ;
        Integer hFHOPEHS_RETICLE_COUNT;
        Integer hFHOPEHS_FIXTURE_COUNT;
        Integer hFHOPEHS_RPARM_COUNT;
        Integer hFHOPEHS_INIT_HOLD_FLAG;
        Integer hFHOPEHS_LAST_HLDREL_FLAG;
        String hFHOPEHS_HOLD_STATE;
        String hFHOPEHS_HOLD_TIME ;
        Double hFHOPEHS_HOLD_SHOP_DATE;
        String hFHOPEHS_HOLD_USER_ID      ;
        String hFHOPEHS_HOLD_TYPE         ;
        String hFHOPEHS_HOLD_REASON_CODE  ;
        String hFHOPEHS_HOLD_REASON_DESC  ;
        String hFHOPEHS_REASON_CODE       ;
        String hFHOPEHS_REASON_DESCRIPTION;
        String hFHOPEHS_BANK_ID           ;
        String hFHOPEHS_PREV_BANK_ID      ;
        String hFHOPEHS_PREV_MAINPD_ID    ;
        String hFHOPEHS_PREV_OPE_NO       ;
        String hFHOPEHS_PREV_PD_ID        ;
        Integer hFHOPEHS_PREV_PASS_COUNT;
        String hFHOPEHS_PREV_PD_NAME    ;
        String hFHOPEHS_PREV_PHOTO_LAYER;
        String hFHOPEHS_PREV_STAGE_ID   ;
        String hFHOPEHS_PREV_STAGEGRP_ID;
        String hFHOPEHS_FLOWBATCH_ID    ;
        String hFHOPEHS_CTRL_JOB;
        Integer hFHOPEHS_REWORK_COUNT;
        Integer hFHOPEHS_ORG_WAFER_QTY;
        Integer hFHOPEHS_CUR_WAFER_QTY;
        Integer hFHOPEHS_PROD_WAFER_QTY;
        Integer hFHOPEHS_CNTL_WAFER_QTY;
        Integer hFHOPEHS_CLAIM_PROD_QTY;
        Integer hFHOPEHS_CLAIM_CNTL_QTY;
        Integer hFHOPEHS_TOTAL_GOOD_UNIT;
        Integer hFHOPEHS_TOTAL_FAIL_UNIT;
        String hFHOPEHS_LOT_OWNER_ID ;
        String hFHOPEHS_PLAN_END_TIME;
        String hFHOPEHS_WFRHS_TIME   ;
        String hFHOPEHS_CLAIM_MEMO   ;
        Integer hFHOPEHS_CRITERIA_FLAG;
        String hFHOPEHS_RPARM_CHANGE_TYPE  ;
        String hFHOPEHS_EVENT_CREATE_TIME  ;
        String hFHOPEHS_HOLD_OPE_NO        ;
        String hFHOPEHS_HOLD_REASON_OPE_NO ;
        String hFHOPEHS_ORIGINAL_FAB_ID;
        String hFHOPEHS_DESTINATION_FAB_ID;
        String hFHOPEHS_RELATED_LOT_ID;
        String hFHOPEHS_BOND_GRP_ID;
        String hFHOPEHS_EQPMONJOB_ID;
        String hFHOPEHS_PD_TYPE;
        String hFHOPEHS_PREV_PD_TYPE;
        String hDPT_NAME_PLATE;
        String hMON_GRP_ID;


        hFHOPEHS_LOT_ID = lotOperationRecord.getLot_id();
        hFHOPEHS_LOT_TYPE = lotOperationRecord.getLot_type();
        hFHOPEHS_SUB_LOT_TYPE = lotOperationRecord.getSub_lot_type();
        hFHOPEHS_CAST_ID = lotOperationRecord.getCast_id();
        hFHOPEHS_CAST_CATEGORY = lotOperationRecord.getCast_category();
        hFHOPEHS_MAINPD_ID = lotOperationRecord.getMainpd_id();
        hFHOPEHS_OPE_NO = lotOperationRecord.getOpe_no();
        hFHOPEHS_PD_ID = lotOperationRecord.getPd_id();
        hFHOPEHS_OPE_PASS_COUNT =     lotOperationRecord.getOpe_pass_count();
        hFHOPEHS_PD_NAME = lotOperationRecord.getPd_name();
        hFHOPEHS_HOLD_STATE = lotOperationRecord.getHold_state();
        hFHOPEHS_CLAIM_TIME = lotOperationRecord.getClaim_time();
        hFHOPEHS_CLAIM_SHOP_DATE  =   lotOperationRecord.getClaim_shop_date();
        hFHOPEHS_CLAIM_USER_ID = lotOperationRecord.getClaim_user_id();
        hFHOPEHS_MOVE_TYPE = lotOperationRecord.getMove_type();
        hFHOPEHS_OPE_CATEGORY = lotOperationRecord.getOpe_category();
        hFHOPEHS_PROD_TYPE = lotOperationRecord.getProd_type();
        hFHOPEHS_TEST_TYPE = lotOperationRecord.getTest_type();
        hFHOPEHS_MFG_LAYER = lotOperationRecord.getMfg_layer();
        hFHOPEHS_EXT_PRIORITY    =    lotOperationRecord.getExt_priority();
        hFHOPEHS_PRIORITY_CLASS  =    lotOperationRecord.getPriority_class();
        hFHOPEHS_PREV_PRODSPEC_ID = lotOperationRecord.getPrev_prodspec_id();
        hFHOPEHS_PRODSPEC_ID = lotOperationRecord.getProdspec_id();
        hFHOPEHS_PRODGRP_ID = lotOperationRecord.getProdgrp_id();
        hFHOPEHS_TECH_ID = lotOperationRecord.getTech_id();
        hFHOPEHS_CUSTOMER_ID = lotOperationRecord.getCustomer_id();
        hFHOPEHS_CUSTPROD_ID = lotOperationRecord.getCustprod_id();
        hFHOPEHS_ORDER_NO = lotOperationRecord.getOrder_no();
        hFHOPEHS_STAGE_ID = lotOperationRecord.getStage_id();
        hFHOPEHS_STAGEGRP_ID = lotOperationRecord.getStagegrp_id();
        hFHOPEHS_PHOTO_LAYER = lotOperationRecord.getPhoto_layer();
        hFHOPEHS_LOCATION_ID = lotOperationRecord.getLocation_id();
        hFHOPEHS_AREA_ID = lotOperationRecord.getArea_id();
        hFHOPEHS_EQP_ID = lotOperationRecord.getEqp_id();
        hFHOPEHS_EQP_NAME = lotOperationRecord.getEqp_name();
        hFHOPEHS_OPE_MODE = lotOperationRecord.getOpe_mode();
        hFHOPEHS_LC_RECIPE_ID = lotOperationRecord.getLc_recipe_id();
        hFHOPEHS_RECIPE_ID = lotOperationRecord.getRecipe_id();
        hFHOPEHS_PH_RECIPE_ID = lotOperationRecord.getPh_recipe_id();
        hFHOPEHS_RETICLE_COUNT   =    lotOperationRecord.getReticle_count();
        hFHOPEHS_FIXTURE_COUNT   =    lotOperationRecord.getFixture_count();
        hFHOPEHS_RPARM_COUNT     =    lotOperationRecord.getRparm_count();
        hFHOPEHS_INIT_HOLD_FLAG  =    lotOperationRecord.getInit_hold_flag();
        hFHOPEHS_LAST_HLDREL_FLAG=    lotOperationRecord.getLast_hldrel_flag();
        hFHOPEHS_HOLD_TIME = lotOperationRecord.getHold_time();
        hFHOPEHS_HOLD_SHOP_DATE  =    lotOperationRecord.getHold_shop_date();
        hFHOPEHS_HOLD_USER_ID = lotOperationRecord.getHold_user_id();
        hFHOPEHS_HOLD_TYPE = lotOperationRecord.getHold_type();
        hFHOPEHS_HOLD_REASON_CODE = lotOperationRecord.getHold_reason_code();
        hFHOPEHS_HOLD_REASON_DESC = lotOperationRecord.getHold_reason_desc();
        hFHOPEHS_REASON_CODE = lotOperationRecord.getReason_code();
        hFHOPEHS_REASON_DESCRIPTION = lotOperationRecord.getReason_description();
        hFHOPEHS_BANK_ID = lotOperationRecord.getBank_id();
        hFHOPEHS_PREV_BANK_ID = lotOperationRecord.getPrev_bank_id();
        hFHOPEHS_PREV_MAINPD_ID = lotOperationRecord.getPrev_mainpd_id();
        hFHOPEHS_PREV_OPE_NO = lotOperationRecord.getPrev_ope_no();
        hFHOPEHS_PREV_PD_ID = lotOperationRecord.getPrev_pd_id();
        hFHOPEHS_PREV_PASS_COUNT =    lotOperationRecord.getPrev_pass_count();
        hFHOPEHS_PREV_PD_NAME = lotOperationRecord.getPrev_pd_name();
        hFHOPEHS_PREV_PHOTO_LAYER = lotOperationRecord.getPrev_photo_layer();
        hFHOPEHS_PREV_STAGE_ID = lotOperationRecord.getPrev_stage_id();
        hFHOPEHS_PREV_STAGEGRP_ID = lotOperationRecord.getPrev_stagegrp_id();
        hFHOPEHS_FLOWBATCH_ID = lotOperationRecord.getFlowbatch_id();
        hFHOPEHS_CTRL_JOB  = lotOperationRecord.getCtrl_job();
        hFHOPEHS_REWORK_COUNT    =    lotOperationRecord.getRework_count();
        hFHOPEHS_ORG_WAFER_QTY   =    lotOperationRecord.getOrg_wafer_qty();
        hFHOPEHS_CUR_WAFER_QTY   =    lotOperationRecord.getCur_wafer_qty();
        hFHOPEHS_PROD_WAFER_QTY  =    lotOperationRecord.getProd_wafer_qty();
        hFHOPEHS_CNTL_WAFER_QTY  =    lotOperationRecord.getCntl_wafer_qty();
        hFHOPEHS_CLAIM_PROD_QTY  =    lotOperationRecord.getClaim_prod_qty();
        hFHOPEHS_CLAIM_CNTL_QTY  =    lotOperationRecord.getClaim_cntl_qty();
        hFHOPEHS_TOTAL_GOOD_UNIT =    lotOperationRecord.getTotal_good_unit();
        hFHOPEHS_TOTAL_FAIL_UNIT =    lotOperationRecord.getTotal_fail_unit();
        hFHOPEHS_LOT_OWNER_ID = lotOperationRecord.getLot_owner_id();
        hFHOPEHS_PLAN_END_TIME = lotOperationRecord.getPlan_end_time();
        hFHOPEHS_WFRHS_TIME = lotOperationRecord.getWfrhs_time();
        hFHOPEHS_CLAIM_MEMO = lotOperationRecord.getClaim_memo();
        hFHOPEHS_CRITERIA_FLAG   =    lotOperationRecord.getCriteria_flag();
        hFHOPEHS_RPARM_CHANGE_TYPE = lotOperationRecord.getRparm_change_type();
        hFHOPEHS_EVENT_CREATE_TIME = lotOperationRecord.getEvent_create_time();
        hFHOPEHS_HOLD_OPE_NO = lotOperationRecord.getHold_ope_no();
        hFHOPEHS_HOLD_REASON_OPE_NO = lotOperationRecord.getHold_reason_ope_no();

        hFHOPEHS_ORIGINAL_FAB_ID = lotOperationRecord.getOriginalFabID();
        hFHOPEHS_DESTINATION_FAB_ID = lotOperationRecord.getDestinationFabID();

        hFHOPEHS_RELATED_LOT_ID = lotOperationRecord.getRelatedLotID();
        hFHOPEHS_BOND_GRP_ID = lotOperationRecord.getBondingGroupID();
        hFHOPEHS_EQPMONJOB_ID = lotOperationRecord.getEqpMonJobID();
        hFHOPEHS_PD_TYPE = lotOperationRecord.getPd_type();
        hFHOPEHS_PREV_PD_TYPE = lotOperationRecord.getPrev_pd_type();

        hDPT_NAME_PLATE = lotOperationRecord.getDpt_name_plate();
        hMON_GRP_ID = lotOperationRecord.getMon_grp_id();


        buff="INSERT INTO OHLOTOPE\n" +
                "                ( ID,  LOT_ID,\n" +
                "                        LOT_TYPE,\n" +
                "                        SUB_LOT_TYPE,\n" +
                "                        CARRIER_ID,\n" +
                "                        CARRIER_CATEGORY,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        STEP_ID,\n" +
                "                        OPE_PASS_COUNT,\n" +
                "                        STEP_NAME,\n" +
                "                        TRX_TIME,\n" +
                "                        TRX_WORK_DATE,\n" +
                "                        TRX_USER_ID,\n" +
                "                        MOVE_TYPE,\n" +
                "                        OPE_CATEGORY,\n" +
                "                        PROD_TYPE,\n" +
                "                        TEST_TYPE,\n" +
                "                        MFG_LAYER,\n" +
                "                        LOT_PRIORITY,\n" +
                "                        PREV_PROD_ID,\n" +
                "                        PROD_ID,\n" +
                "                        PRODFMLY_ID,\n" +
                "                        TECH_ID,\n" +
                "                        CUSTOMER_ID,\n" +
                "                        CUSTPROD_ID,\n" +
                "                        MFG_ORDER_NO,\n" +
                "                        STAGE_ID,\n" +
                "                        STAGE_GRP_ID,\n" +
                "                        PHOTO_LAYER,\n" +
                "                        LOCATION_ID,\n" +
                "                        BAY_ID,\n" +
                "                        EQP_ID,\n" +
                "                        EQP_NAME,\n" +
                "                        OPE_MODE,\n" +
                "                        LRCP_ID,\n" +
                "                        MRCP_ID,\n" +
                "                        PRCP_ID,\n" +
                "                        RETICLE_COUNT,\n" +
                "                        FIXTURE_COUNT,\n" +
                "                        RPARAM_COUNT,\n" +
                "                        INITIAL_HOLD_FLAG,\n" +
                "                        LAST_HOLD_REL_FLAG,\n" +
                "                        HOLD_STATE,\n" +
                "                        HOLD_TRX_TIME,\n" +
                "                        HOLD_USER_ID,\n" +
                "                        HOLD_TYPE,\n" +
                "                        HOLD_REASON_CODE,\n" +
                "                        HOLD_REASON_DESC,\n" +
                "                        REASON_CODE,\n" +
                "                        REASON_DESC,\n" +
                "                        BANK_ID,\n" +
                "                        PREV_BANK_ID,\n" +
                "                        PREV_PROCESS_ID,\n" +
                "                        PREV_OPE_NO,\n" +
                "                        PREV_STEP_ID,\n" +
                "                        PREV_PASS_COUNT,\n" +
                "                        PREV_STEP_NAME,\n" +
                "                        PREV_PHOTO_LAYER,\n" +
                "                        PREV_STAGE_ID,\n" +
                "                        PREV_STAGE_GRP_ID,\n" +
                "                        FLOWB_ID,\n" +
                "                        CJ_ID,\n" +
                "                        REWORK_COUNT,\n" +
                "                        ORIG_WAFER_QTY,\n" +
                "                        CUR_WAFER_QTY,\n" +
                "                        PROD_WAFER_QTY,\n" +
                "                        NPW_WAFER_QTY,\n" +
                "                        CLAIM_PROD_QTY,\n" +
                "                        TRX_NPW_QTY,\n" +
                "                        TOTAL_GOOD_UNIT,\n" +
                "                        TOTAL_FAIL_UNIT,\n" +
                "                        LOT_OWNER_ID,\n" +
                "                        PLAN_END_TIME,\n" +
                "                        WAFER_HIS_TIME,\n" +
                "                        TRX_MEMO,\n" +
                "                        CRITERIA_FLAG,\n" +
                "                        RPARAM_CHG_TYPE,\n" +
                "                        EVENT_CREATE_TIME,\n" +
                "                        HOLD_OPE_NO,\n" +
                "                        HOLD_REASON_OPE_NO,\n" +
                "                        ORIGINAL_FAB_ID,\n" +
                "                        DESTINATION_FAB_ID,\n" +
                "                        RELATED_LOT_ID,\n" +
                "                        BOND_GRP_ID,\n" +
                "                        AM_JOB_ID,\n" +
                "                        STEP_TYPE,\n" +
                "                        PREV_STEP_TYPE,\n" +
                "                        DPT_NAME_PLATE,\n" +
                "                        MON_GRP_ID,\n" +
                "                        STORE_TIME   )\n" +
                "        Values\n" +
                "                ( ?,  ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                    ?," +
                "                CURRENT_TIMESTAMP   )";
        baseCore.insert(buff,generateID(lotOperationRecord.getClass()),hFHOPEHS_LOT_ID,
                hFHOPEHS_LOT_TYPE,
                hFHOPEHS_SUB_LOT_TYPE,
                hFHOPEHS_CAST_ID,
                hFHOPEHS_CAST_CATEGORY,
                hFHOPEHS_MAINPD_ID,
                hFHOPEHS_OPE_NO,
                hFHOPEHS_PD_ID,
                hFHOPEHS_OPE_PASS_COUNT,
                hFHOPEHS_PD_NAME,
                convert(hFHOPEHS_CLAIM_TIME),
                hFHOPEHS_CLAIM_SHOP_DATE,
                hFHOPEHS_CLAIM_USER_ID,
                hFHOPEHS_MOVE_TYPE,
                hFHOPEHS_OPE_CATEGORY,
                hFHOPEHS_PROD_TYPE,
                hFHOPEHS_TEST_TYPE,
                hFHOPEHS_MFG_LAYER,
                hFHOPEHS_PRIORITY_CLASS,
                hFHOPEHS_PREV_PRODSPEC_ID,
                hFHOPEHS_PRODSPEC_ID,
                hFHOPEHS_PRODGRP_ID,
                hFHOPEHS_TECH_ID,
                hFHOPEHS_CUSTOMER_ID,
                hFHOPEHS_CUSTPROD_ID,
                hFHOPEHS_ORDER_NO,
                hFHOPEHS_STAGE_ID,
                hFHOPEHS_STAGEGRP_ID,
                hFHOPEHS_PHOTO_LAYER,
                hFHOPEHS_LOCATION_ID,
                hFHOPEHS_AREA_ID,
                hFHOPEHS_EQP_ID,
                hFHOPEHS_EQP_NAME,
                hFHOPEHS_OPE_MODE,
                hFHOPEHS_LC_RECIPE_ID,
                hFHOPEHS_RECIPE_ID,
                hFHOPEHS_PH_RECIPE_ID,
                hFHOPEHS_RETICLE_COUNT,
                hFHOPEHS_FIXTURE_COUNT,
                hFHOPEHS_RPARM_COUNT,
                hFHOPEHS_INIT_HOLD_FLAG,
                hFHOPEHS_LAST_HLDREL_FLAG,
                hFHOPEHS_HOLD_STATE,
                convert(hFHOPEHS_HOLD_TIME),
                hFHOPEHS_HOLD_USER_ID,
                hFHOPEHS_HOLD_TYPE,
                hFHOPEHS_HOLD_REASON_CODE,
                hFHOPEHS_HOLD_REASON_DESC,
                hFHOPEHS_REASON_CODE,
                hFHOPEHS_REASON_DESCRIPTION,
                hFHOPEHS_BANK_ID,
                hFHOPEHS_PREV_BANK_ID,
                hFHOPEHS_PREV_MAINPD_ID,
                hFHOPEHS_PREV_OPE_NO,
                hFHOPEHS_PREV_PD_ID,
                hFHOPEHS_PREV_PASS_COUNT,
                hFHOPEHS_PREV_PD_NAME,
                hFHOPEHS_PREV_PHOTO_LAYER,
                hFHOPEHS_PREV_STAGE_ID,
                hFHOPEHS_PREV_STAGEGRP_ID,
                hFHOPEHS_FLOWBATCH_ID,
                hFHOPEHS_CTRL_JOB,
                hFHOPEHS_REWORK_COUNT,
                hFHOPEHS_ORG_WAFER_QTY,
                hFHOPEHS_CUR_WAFER_QTY,
                hFHOPEHS_PROD_WAFER_QTY,
                hFHOPEHS_CNTL_WAFER_QTY,
                hFHOPEHS_CLAIM_PROD_QTY,
                hFHOPEHS_CLAIM_CNTL_QTY,
                hFHOPEHS_TOTAL_GOOD_UNIT,
                hFHOPEHS_TOTAL_FAIL_UNIT,
                hFHOPEHS_LOT_OWNER_ID,
                convert(hFHOPEHS_PLAN_END_TIME),
                convert(hFHOPEHS_WFRHS_TIME),
                hFHOPEHS_CLAIM_MEMO,
                hFHOPEHS_CRITERIA_FLAG,
                hFHOPEHS_RPARM_CHANGE_TYPE,
                convert(hFHOPEHS_EVENT_CREATE_TIME),
                hFHOPEHS_HOLD_OPE_NO,
                hFHOPEHS_HOLD_REASON_OPE_NO,
                hFHOPEHS_ORIGINAL_FAB_ID,
                hFHOPEHS_DESTINATION_FAB_ID,
                hFHOPEHS_RELATED_LOT_ID,
                hFHOPEHS_BOND_GRP_ID,
                hFHOPEHS_EQPMONJOB_ID,
                hFHOPEHS_PD_TYPE,
                hFHOPEHS_PREV_PD_TYPE,
                hDPT_NAME_PLATE,
                hMON_GRP_ID);
        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationReticleRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/4/18 17:13
     */
    public Response insertLotOperationReticleHistory(Infos.OhopehsReticle lotOperationReticleRecord) {
        String hFHOPEHS_RETICLE_LOT_ID   ;
        String hFHOPEHS_RETICLE_MAINPD_ID;
        String hFHOPEHS_RETICLE_OPE_NO   ;
        Integer hFHOPEHS_RETICLE_OPE_PASS_COUNT;
        String hFHOPEHS_RETICLE_CLAIM_TIME  ;
        String hFHOPEHS_RETICLE_OPE_CATEGORY;
        String hFHOPEHS_RETICLE_RETICLE_ID  ;

        hFHOPEHS_RETICLE_LOT_ID = lotOperationReticleRecord.getLot_id();
        hFHOPEHS_RETICLE_MAINPD_ID = lotOperationReticleRecord.getMainpd_id();
        hFHOPEHS_RETICLE_OPE_NO = lotOperationReticleRecord.getOpe_no();
        hFHOPEHS_RETICLE_OPE_PASS_COUNT = lotOperationReticleRecord.getOpe_pass_count();
        hFHOPEHS_RETICLE_CLAIM_TIME = lotOperationReticleRecord.getClaim_time();
        hFHOPEHS_RETICLE_OPE_CATEGORY = lotOperationReticleRecord.getOpe_category();
        hFHOPEHS_RETICLE_RETICLE_ID = lotOperationReticleRecord.getReticle_id();

        baseCore.insert("INSERT INTO OHLOTOPE_RTCL\n" +
                "                ( ID, LOT_ID,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        OPE_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        OPE_CATEGORY,\n" +
                "                        RTCL_ID)\n" +
                "        Values\n" +
                "                (  ?,?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?)",generateID(lotOperationReticleRecord.getClass()),hFHOPEHS_RETICLE_LOT_ID,
                hFHOPEHS_RETICLE_MAINPD_ID,
                hFHOPEHS_RETICLE_OPE_NO,
                hFHOPEHS_RETICLE_OPE_PASS_COUNT,
                convert(hFHOPEHS_RETICLE_CLAIM_TIME),
                hFHOPEHS_RETICLE_OPE_CATEGORY,
                hFHOPEHS_RETICLE_RETICLE_ID);

        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationFixtureRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/4/18 17:15
     */
    public Response insertLotOperationFixtureHistory(Infos.OhopehsFixture lotOperationFixtureRecord) {
        String hFHOPEHS_FIXTURE_LOT_ID   ;
        String hFHOPEHS_FIXTURE_MAINPD_ID;
        String hFHOPEHS_FIXTURE_OPE_NO   ;
        Integer hFHOPEHS_FIXTURE_OPE_PASS_COUNT;
        String hFHOPEHS_FIXTURE_CLAIM_TIME  ;
        String hFHOPEHS_FIXTURE_OPE_CATEGORY;
        String hFHOPEHS_FIXTURE_FIXTURE_ID  ;

        hFHOPEHS_FIXTURE_LOT_ID = lotOperationFixtureRecord.getLot_id();
        hFHOPEHS_FIXTURE_MAINPD_ID = lotOperationFixtureRecord.getMainpd_id();
        hFHOPEHS_FIXTURE_OPE_NO = lotOperationFixtureRecord.getOpe_no();
        hFHOPEHS_FIXTURE_OPE_PASS_COUNT = lotOperationFixtureRecord.getOpe_pass_count();
        hFHOPEHS_FIXTURE_CLAIM_TIME = lotOperationFixtureRecord.getClaim_time();
        hFHOPEHS_FIXTURE_OPE_CATEGORY = lotOperationFixtureRecord.getOpe_category();
        hFHOPEHS_FIXTURE_FIXTURE_ID = lotOperationFixtureRecord.getFixture_id();

        baseCore.insert("INSERT INTO OHLOTOPE_FIXT\n" +
                "                (  ID,LOT_ID,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        OPE_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        OPE_CATEGORY,\n" +
                "                        RTCL_ID)\n" +
                "        Values\n" +
                "                (  ?,?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?)",generateID(lotOperationFixtureRecord.getClass()),hFHOPEHS_FIXTURE_LOT_ID,
                hFHOPEHS_FIXTURE_MAINPD_ID,
                hFHOPEHS_FIXTURE_OPE_NO,
                hFHOPEHS_FIXTURE_OPE_PASS_COUNT,
                convert(hFHOPEHS_FIXTURE_CLAIM_TIME),
                hFHOPEHS_FIXTURE_OPE_CATEGORY,
                hFHOPEHS_FIXTURE_FIXTURE_ID);

        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationRparmRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/4/18 17:15
     */
    public Response insertLotOperationRparmHistory(Infos.OhopehsRparm lotOperationRparmRecord) {
        String hFHOPEHS_RPARM_LOT_ID   ;
        String hFHOPEHS_RPARM_MAINPD_ID;
        String hFHOPEHS_RPARM_OPE_NO   ;
        Integer hFHOPEHS_RPARM_OPE_PASS_COUNT;
        String hFHOPEHS_RPARM_CLAIM_TIME  ;
        String hFHOPEHS_RPARM_OPE_CATEGORY;
        String hFHOPEHS_RPARM_RPARM_NAME  ;
        String hFHOPEHS_RPARM_RPARM_VALUE ;

        hFHOPEHS_RPARM_LOT_ID = lotOperationRparmRecord.getLot_id();
        hFHOPEHS_RPARM_MAINPD_ID = lotOperationRparmRecord.getMainpd_id();
        hFHOPEHS_RPARM_OPE_NO = lotOperationRparmRecord.getOpe_no();
        hFHOPEHS_RPARM_OPE_PASS_COUNT = lotOperationRparmRecord.getOpe_pass_count();
        hFHOPEHS_RPARM_CLAIM_TIME = lotOperationRparmRecord.getClaim_time();
        hFHOPEHS_RPARM_OPE_CATEGORY = lotOperationRparmRecord.getOpe_category();
        hFHOPEHS_RPARM_RPARM_NAME = lotOperationRparmRecord.getRparm_name();
        hFHOPEHS_RPARM_RPARM_VALUE = lotOperationRparmRecord.getRparm_value();

        baseCore.insert("INSERT INTO OHLOTOPE_RPARAM\n" +
                "                (  ID,LOT_ID,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        OPE_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        OPE_CATEGORY,\n" +
                "                        RPARAM_NAME,\n" +
                "                        RPARAM_VAL)\n" +
                "        Values\n" +
                "                ( ?, ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?)",generateID(lotOperationRparmRecord.getClass()),hFHOPEHS_RPARM_LOT_ID,
                hFHOPEHS_RPARM_MAINPD_ID,
                hFHOPEHS_RPARM_OPE_NO,
                hFHOPEHS_RPARM_OPE_PASS_COUNT,
                convert(hFHOPEHS_RPARM_CLAIM_TIME),
                hFHOPEHS_RPARM_OPE_CATEGORY,
                hFHOPEHS_RPARM_RPARM_NAME,
                hFHOPEHS_RPARM_RPARM_VALUE);

        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationRparmWaferRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/4/18 17:14
     */
    public Response insertLotOperationRparmWaferHistory(Infos.OhopehsRparmWafer lotOperationRparmWaferRecord) {
        String hFHOPEHS_RPARM_WAFER_LOT_ID   ;
        String hFHOPEHS_RPARM_WAFER_WAFER_ID ;
        String hFHOPEHS_RPARM_WAFER_MAINPD_ID;
        String hFHOPEHS_RPARM_WAFER_OPE_NO   ;
        Integer hFHOPEHS_RPARM_WAFER_OPE_PASS_COUNT;
        String hFHOPEHS_RPARM_WAFER_CLAIM_TIME       ;
        String hFHOPEHS_RPARM_WAFER_OPE_CATEGORY     ;
        String hFHOPEHS_RPARM_WAFER_MACHINE_RECIPE_ID;
        String hFHOPEHS_RPARM_WAFER_RPARM_NAME       ;
        String hFHOPEHS_RPARM_WAFER_RPARM_VALUE      ;

        hFHOPEHS_RPARM_WAFER_LOT_ID = lotOperationRparmWaferRecord.getLot_id();
        hFHOPEHS_RPARM_WAFER_WAFER_ID = lotOperationRparmWaferRecord.getWafer_id();
        hFHOPEHS_RPARM_WAFER_MAINPD_ID = lotOperationRparmWaferRecord.getMainpd_id();
        hFHOPEHS_RPARM_WAFER_OPE_NO = lotOperationRparmWaferRecord.getOpe_no();
        hFHOPEHS_RPARM_WAFER_OPE_PASS_COUNT   = lotOperationRparmWaferRecord.getOpe_pass_count();
        hFHOPEHS_RPARM_WAFER_CLAIM_TIME = lotOperationRparmWaferRecord.getClaim_time();
        hFHOPEHS_RPARM_WAFER_OPE_CATEGORY = lotOperationRparmWaferRecord.getOpe_category();
        hFHOPEHS_RPARM_WAFER_MACHINE_RECIPE_ID = lotOperationRparmWaferRecord.getMachine_recipe_id();
        hFHOPEHS_RPARM_WAFER_RPARM_NAME = lotOperationRparmWaferRecord.getRparm_name();
        hFHOPEHS_RPARM_WAFER_RPARM_VALUE = lotOperationRparmWaferRecord.getRparm_value();

        baseCore.insert("INSERT INTO OHLOTOPE_RPARAM_WAFER\n" +
                "                ( ID,LOT_ID,\n" +
                "                        WAFER_ID,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        OPE_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        OPE_CATEGORY,\n" +
                "                        MRCP_ID,\n" +
                "                        RPARAM_NAME,\n" +
                "                        RPARAM_VAL )\n" +
                "        VALUES\n" +
                "                ( ?,?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?)",generateID(lotOperationRparmWaferRecord.getClass()),hFHOPEHS_RPARM_WAFER_LOT_ID,
                hFHOPEHS_RPARM_WAFER_WAFER_ID,
                hFHOPEHS_RPARM_WAFER_MAINPD_ID,
                hFHOPEHS_RPARM_WAFER_OPE_NO,
                hFHOPEHS_RPARM_WAFER_OPE_PASS_COUNT,
                convert(hFHOPEHS_RPARM_WAFER_CLAIM_TIME),
                hFHOPEHS_RPARM_WAFER_OPE_CATEGORY,
                hFHOPEHS_RPARM_WAFER_MACHINE_RECIPE_ID,
                hFHOPEHS_RPARM_WAFER_RPARM_NAME,
                hFHOPEHS_RPARM_WAFER_RPARM_VALUE );

        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.Custom.List<com.fa.cim.dto.Infos.LotOperationStartEventRecord>
     * @exception
     * @author Ho
     * @date 2019/4/18 17:44
     */
    public Infos.LotOperationCompleteEventRecord getLotOperationCompleteEventRecord(String id) {
        String sql="SELECT\n" +
                "\tID,\n" +
                "\tLOT_ID temp1,\n" +
                "\tLOT_ID temp2,\n" +
                "\tLOT_ID temp3,\n" +
                "\tLOT_ID,\n" +
                "\tLOT_TYPE,\n" +
                "\tCARRIER_ID,\n" +
                "\tLOT_STATUS,\n" +
                "\tCUSTOMER_ID,\n" +
                "\tLOT_PRIORITY,\n" +
                "\tPROD_ID,\n" +
                "\tORIGINAL_QTY,\n" +
                "\tCUR_QTY,\n" +
                "\tPROD_QTY,\n" +
                "\tNPW_QTY,\n" +
                "\tLOT_HOLD_STATE,\n" +
                "\tBANK_ID,\n" +
                "\tPROCESS_ID,\n" +
                "\tOPE_NO,\n" +
                "\tSTEP_ID,\n" +
                "\tPASS_COUNT,\n" +
                "\tPRSS_RKEY,\n" +
                "\tMROUTE_PRF_RKEY,\n" +
                "\tROUTE_PRSS_RKEY,\n" +
                "\tWAFER_HIS_TIME,\n" +
                "\tPROPE_RKEY,\n" +
                "\tEQP_ID,\n" +
                "\tOPE_MODE,\n" +
                "\tLRCP_ID,\n" +
                "\tMRCP_ID,\n" +
                "\tPRCP_ID,\n" +
                "\tPREV_PROCESS_ID,\n" +
                "\tPREV_OPE_NO,\n" +
                "\tPREV_STEP_ID,\n" +
                "\tPREV_PASS_COUNT,\n" +
                "\tPREV_PRSS_RKEY,\n" +
                "\tPREV_MROUTE_PRF_RKEY,\n" +
                "\tPREV_ROUTE_PRSS_RKEY,\n" +
                "\tFLOWB_ID,\n" +
                "\tCJ_ID,\n" +
                "\tSKIP_BACK,\n" +
                "\tTEST_CRITERIA,\n" +
                "\tTRX_ID,\n" +
                "\tEVENT_TIME,\n" +
                "\t'',\n" +
                "\tTRX_USER_ID,\n" +
                "\tTRX_MEMO,\n" +
                "\tEVENT_CREATE_TIME,\n" +
                "\tENTITY_MGR\n" +
                "FROM\n" +
                "\tOMEVMVO\n" +
                "WHERE ID = ?";
        Object[] object = baseCore.queryOne(sql, id);
        Infos.LotOperationCompleteEventRecord eventRecord=new Infos.LotOperationCompleteEventRecord();
        Infos.LotEventData lotData=new Infos.LotEventData();
        eventRecord.setLotData(lotData);
        lotData.setLotID(convert(object[4]));
        lotData.setLotType(convert(object[5]));
        lotData.setCassetteID(convert(object[6]));
        lotData.setLotStatus(convert(object[7]));
        lotData.setCustomerID(convert(object[8]));
        lotData.setPriorityClass(convertL(object[9]));
        lotData.setProductID(convert(object[10]));
        lotData.setOriginalWaferQuantity(convertI(object[11]));
        lotData.setCurrentWaferQuantity(convertI(object[12]));
        lotData.setProductWaferQuantity(convertI(object[13]));
        lotData.setControlWaferQuantity(convertI(object[14]));
        lotData.setHoldState(convert(object[15]));
        lotData.setBankID(convert(object[16]));
        lotData.setRouteID(convert(object[17]));
        lotData.setOperationNumber(convert(object[18]));
        lotData.setOperationID(convert(object[19]));
        lotData.setOperationPassCount(convertI(object[20]));
        lotData.setObjrefPOS(convert(object[21]));
        lotData.setWaferHistoryTimeStamp(convert(object[24]));
        lotData.setObjrefPO(convert(object[25]));
        lotData.setObjrefMainPF(convert(object[22]));
        lotData.setObjrefModulePOS(convert(object[23]));
        eventRecord.setEquipmentID(convert(object[26]));
        eventRecord.setOperationMode(convert(object[27]));
        eventRecord.setLogicalRecipeID(convert(object[28]));
        eventRecord.setMachineRecipeID(convert(object[29]));
        eventRecord.setPhysicalRecipeID(convert(object[30]));
        List<String> reticleIDs=new ArrayList<>();
        eventRecord.setReticleIDs(reticleIDs);
        sql="SELECT RTCL_ID,REFKEY FROM OMEVMVO_RTCL WHERE REFKEY=?";
        List<Object[]> reticles = baseCore.queryAll(sql, object[0]);
        reticles.forEach(obj->reticleIDs.add(convert(obj[0])));
        List<String> fixtureIDs=new ArrayList<>();
        eventRecord.setFixtureIDs(fixtureIDs);
        sql="SELECT FIXTURE_ID,REFKEY FROM OMEVMVO_FIXT WHERE REFKEY=?";
        List<Object[]> fixtures = baseCore.queryAll(sql, object[0]);
        fixtures.forEach(obj->fixtureIDs.add(convert(obj[0])));
        List<Infos.RecipeParmEventData> recipeParameters=new ArrayList<>();
        eventRecord.setRecipeParameters(recipeParameters);
        sql="SELECT ID,  IDX_NO, RPARAM_NAME, RPARAM_VAL, REFKEY\n" +
                "FROM OMEVMVO_RPARAM WHERE REFKEY=?";
        List<Object[]> parameters = baseCore.queryAll(sql, object[0]);
        for (Object[] parameter:parameters) {
            Infos.RecipeParmEventData recipeParmEventData=new Infos.RecipeParmEventData();
            recipeParameters.add(recipeParmEventData);
            recipeParmEventData.setParameterName(convert(parameter[2]));
            recipeParmEventData.setParameterValue(convert(parameter[3]));
        }
        List<Infos.WaferLevelRecipeEventData> waferLevelRecipes=new ArrayList<>();
        eventRecord.setWaferLevelRecipe(waferLevelRecipes);
        sql="SELECT WAFER_ID,RECIPE_ID,IDX_NO FROM OMEVMVO_WAFER WHERE REFKEY=?";
        List<Object[]> wafers = baseCore.queryAll(sql, object[0]);
        for (Object[] wafer:wafers) {
            Infos.WaferLevelRecipeEventData waferLevelRecipe=new Infos.WaferLevelRecipeEventData();
            waferLevelRecipes.add(waferLevelRecipe);
            waferLevelRecipe.setWaferID(convert(wafer[0]));
            waferLevelRecipe.setMachineRecipeID(convert(wafer[1]));
            List<Infos.WaferRecipeParmEventData> waferRecipeParameters=new ArrayList<>();
            waferLevelRecipe.setWaferRecipeParameters(waferRecipeParameters);
            sql="SELECT RPARAM_NAME,RPARAM_VAL FROM OMEVMVO_WAFER_RPARAM WHERE REFKEY=? AND LINK_MARKER =?";
            parameters = baseCore.queryAll(sql, object[0],wafer[2]);
            for (Object[] parameter:parameters) {
                Infos.WaferRecipeParmEventData waferRecipeParameter=new Infos.WaferRecipeParmEventData();
                waferRecipeParameters.add(waferRecipeParameter);
                waferRecipeParameter.setParameterName(convert(parameter[0]));
                waferRecipeParameter.setParameterValue(convert(parameter[1]));
            }
        }
        eventRecord.setPreviousRouteID(convert(object[31]));
        eventRecord.setPreviousOperationNumber(convert(object[32]));
        eventRecord.setPreviousOperationID(convert(object[33]));
        eventRecord.setPreviousOperationPassCount(convertI(object[34]));
        eventRecord.setPreviousObjrefPOS(convert(object[35]));
        eventRecord.setPreviousObjrefMainPF(convert(object[36]));
        eventRecord.setPreviousObjrefModulePOS(convert(object[37]));
        eventRecord.setBatchID(convert(object[38]));
        eventRecord.setControlJobID(convert(object[39]));
        eventRecord.setLocateBackFlag(convertB(object[40]));
        eventRecord.setTestCriteriaResult(convertB(object[41]));
        Infos.EventData eventCommon=new Infos.EventData();
        eventRecord.setEventCommon(eventCommon);
        eventCommon.setTransactionID(convert(object[42]));
        eventCommon.setEventTimeStamp(convert(object[43]));
        eventCommon.setEventShopDate(convertD(object[44]));
        eventCommon.setUserID(convert(object[45]));
        eventCommon.setEventMemo(convert(object[46]));
        eventCommon.setEventCreationTimeStamp(convert(object[47]));
        List<Infos.WaferPassCountNoPreviousEventData> processWafers=new ArrayList<>();
        eventRecord.setProcessWafers(processWafers);
        sql="SELECT WAFER_ID,CUR_OPE_FLAG,PASS_COUNT FROM OMEVMVO_PASSCNT WHERE REFKEY=?";
        List<Object[]> passCounts = baseCore.queryAll(sql, object[0]);
        for (Object[] passCount:passCounts) {
            Infos.WaferPassCountNoPreviousEventData processWafer=new Infos.WaferPassCountNoPreviousEventData();
            processWafers.add(processWafer);
            processWafer.setWaferID(convert(passCount[0]));
            processWafer.setCurrentOperationFlag(convertB(passCount[1]));
            processWafer.setPassCount(convertI(passCount[2]));
        }
        return eventRecord;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.LotOperationStartEventRecord
     * @exception
     * @author Ho
     * @date 2019/4/19 15:48
     */
    public Infos.LotOperationStartEventRecord getLotOperationStartEventRecord(String id) {
        String sql="SELECT\n" +
                "\tID,\n" +
                "\tLOT_ID temp1,\n" +
                "\tLOT_ID temp2,\n" +
                "\tLOT_ID temp3,\n" +
                "\tLOT_ID,\n" +
                "\tLOT_TYPE,\n" +
                "\tCARRIER_ID,\n" +
                "\tLOT_STATUS,\n" +
                "\tCUSTOMER_ID,\n" +
                "\tLOT_PRIORITY,\n" +
                "\tPROD_ID,\n" +
                "\tORIGINAL_QTY,\n" +
                "\tCUR_QTY,\n" +
                "\tPROD_QTY,\n" +
                "\tNPW_QTY,\n" +
                "\tLOT_HOLD_STATE,\n" +
                "\tBANK_ID,\n" +
                "\tPROCESS_ID,\n" +
                "\tOPE_NO,\n" +
                "\tSTEP_ID,\n" +
                "\tPASS_COUNT,\n" +
                "\tPRSS_RKEY,\n" +
                "\tMROUTE_PRF_RKEY,\n" +
                "\tROUTE_PRSS_RKEY,\n" +
                "\tWAFER_HIS_TIME,\n" +
                "\tPROPE_RKEY,\n" +
                "\tEQP_ID,\n" +
                "\tOPE_MODE,\n" +
                "\tLRCP_ID,\n" +
                "\tMRCP_ID,\n" +
                "\tPRCP_ID,\n" +
                "\tPREV_PROCESS_ID,\n" +
                "\tPREV_OPE_NO,\n" +
                "\tPREV_STEP_ID,\n" +
                "\tPREV_PASS_COUNT,\n" +
                "\tPREV_PRSS_RKEY,\n" +
                "\tPREV_MROUTE_PRF_RKEY,\n" +
                "\tPREV_ROUTE_PRSS_RKEY,\n" +
                "\tFLOWB_ID,\n" +
                "\tCJ_ID,\n" +
                "\tSKIP_BACK,\n" +
                "\tTEST_CRITERIA,\n" +
                "\tTRX_ID,\n" +
                "\tEVENT_TIME,\n" +
                "\t'',\n" +
                "\tTRX_USER_ID,\n" +
                "\tTRX_MEMO,\n" +
                "\tEVENT_CREATE_TIME,\n" +
                "\tENTITY_MGR\n" +
                "FROM\n" +
                "\tOMEVMVI\n" +
                "WHERE ID = ?";
        Object[] object = baseCore.queryOne(sql, id);
        Infos.LotOperationStartEventRecord eventRecord=new Infos.LotOperationStartEventRecord();
        Infos.LotEventData lotData=new Infos.LotEventData();
        eventRecord.setLotData(lotData);
        lotData.setLotID(convert(object[4]));
        lotData.setLotType(convert(object[5]));
        lotData.setCassetteID(convert(object[6]));
        lotData.setLotStatus(convert(object[7]));
        lotData.setCustomerID(convert(object[8]));
        lotData.setPriorityClass(convertL(object[9]));
        lotData.setProductID(convert(object[10]));
        lotData.setOriginalWaferQuantity(convertI(object[11]));
        lotData.setCurrentWaferQuantity(convertI(object[12]));
        lotData.setProductWaferQuantity(convertI(object[13]));
        lotData.setControlWaferQuantity(convertI(object[14]));
        lotData.setHoldState(convert(object[15]));
        lotData.setBankID(convert(object[16]));
        lotData.setRouteID(convert(object[17]));
        lotData.setOperationNumber(convert(object[18]));
        lotData.setOperationID(convert(object[19]));
        lotData.setOperationPassCount(convertI(object[20]));
        lotData.setObjrefPOS(convert(object[21]));
        lotData.setObjrefMainPF(convert(object[22]));
        lotData.setObjrefModulePOS(convert(object[23]));
        lotData.setWaferHistoryTimeStamp(convert(object[24]));
        lotData.setObjrefPO(convert(object[25]));
        eventRecord.setEquipmentID(convert(object[26]));
        eventRecord.setOperationMode(convert(object[27]));
        eventRecord.setLogicalRecipeID(convert(object[28]));
        eventRecord.setMachineRecipeID(convert(object[29]));
        eventRecord.setPhysicalRecipeID(convert(object[30]));
        List<String> reticleIDs=new ArrayList<>();
        eventRecord.setReticleIDs(reticleIDs);
        sql="SELECT RTCL_ID,REFKEY FROM OMEVMVI_RTCL WHERE REFKEY=?";
        List<Object[]> reticles = baseCore.queryAll(sql, object[0]);
        reticles.forEach(obj->reticleIDs.add(convert(obj[0])));
        List<String> fixtureIDs=new ArrayList<>();
        eventRecord.setFixtureIDs(fixtureIDs);
        sql="SELECT FIXTURE_ID,REFKEY FROM OMEVMVI_FIXT WHERE REFKEY=?";
        List<Object[]> fixtures = baseCore.queryAll(sql, object[0]);
        fixtures.forEach(obj->fixtureIDs.add(convert(obj[0])));
        List<Infos.RecipeParmEventData> recipeParameters=new ArrayList<>();
        eventRecord.setRecipeParameters(recipeParameters);
        sql="SELECT ID, IDX_NO, RPARAM_NAME, RPARAM_VAL, REFKEY\n" +
                "FROM OMEVMVI_RPARAM WHERE REFKEY=?";
        List<Object[]> parameters = baseCore.queryAll(sql, object[0]);
        for (Object[] parameter:parameters) {
            Infos.RecipeParmEventData recipeParmEventData=new Infos.RecipeParmEventData();
            recipeParameters.add(recipeParmEventData);
            recipeParmEventData.setParameterName(convert(parameter[3]));
            recipeParmEventData.setParameterValue(convert(parameter[4]));
        }
        List<Infos.WaferLevelRecipeEventData> waferLevelRecipes=new ArrayList<>();
        eventRecord.setWaferLevelRecipe(waferLevelRecipes);
        sql="SELECT WAFER_ID,RECIPE_ID,IDX_NO FROM OMEVMVI_WAFER WHERE REFKEY=?";
        List<Object[]> wafers = baseCore.queryAll(sql, object[0]);
        for (Object[] wafer:wafers) {
            Infos.WaferLevelRecipeEventData waferLevelRecipe=new Infos.WaferLevelRecipeEventData();
            waferLevelRecipes.add(waferLevelRecipe);
            waferLevelRecipe.setWaferID(convert(wafer[0]));
            waferLevelRecipe.setMachineRecipeID(convert(wafer[1]));
            List<Infos.WaferRecipeParmEventData> waferRecipeParameters=new ArrayList<>();
            waferLevelRecipe.setWaferRecipeParameters(waferRecipeParameters);
            sql="SELECT RPARAM_NAME,RPARAM_VAL FROM OMEVMVI_WAFER_RPARAM WHERE REFKEY=? AND LINK_MARKER=?";
            parameters = baseCore.queryAll(sql, object[0],wafer[2]);
            for (Object[] parameter:parameters) {
                Infos.WaferRecipeParmEventData waferRecipeParameter=new Infos.WaferRecipeParmEventData();
                waferRecipeParameters.add(waferRecipeParameter);
                waferRecipeParameter.setParameterName(convert(parameter[0]));
                waferRecipeParameter.setParameterValue(convert(parameter[1]));
            }
        }
        eventRecord.setPreviousRouteID(convert(object[31]));
        eventRecord.setPreviousOperationNumber(convert(object[32]));
        eventRecord.setPreviousOperationID(convert(object[33]));
        eventRecord.setPreviousOperationPassCount(convertL(object[34]));
        eventRecord.setPreviousObjrefPOS(convert(object[35]));
        eventRecord.setPreviousObjrefMainPF(convert(object[36]));
        eventRecord.setPreviousObjrefModulePOS(convert(object[37]));
        eventRecord.setBatchID(convert(object[38]));
        eventRecord.setControlJobID(convert(object[39]));
        eventRecord.setLocateBackFlag(convertB(object[40]));
        eventRecord.setTestCriteriaResult(convertB(object[41]));
        Infos.EventData eventCommon=new Infos.EventData();
        eventRecord.setEventCommon(eventCommon);
        eventCommon.setTransactionID(convert(object[42]));
        eventCommon.setEventTimeStamp(convert(object[43]));
        eventCommon.setEventShopDate(convertD(object[44]));
        eventCommon.setUserID(convert(object[45]));
        eventCommon.setEventMemo(convert(object[46]));
        eventCommon.setEventCreationTimeStamp(convert(object[47]));
        List<String> samplingWafers=new ArrayList<>();
        eventRecord.setSamplingWafers(samplingWafers);
        sql="SELECT SAMPLE_WAFER_ID,REFKEY FROM OMEVMVI_SAMPLE WHERE REFKEY=?";
        List<Object[]> samplings = baseCore.queryAll(sql, object[0]);
        samplings.forEach(sampling->samplingWafers.add(convert(sampling[0])));
        return eventRecord;
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
     * @date 2019/4/19 15:42
     */
    public List<Infos.UserDataSet> getLotOperationStartEventUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVMVI_CDA WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets=new ArrayList<>();
        for (Map<String,Object> uData:uDatas) {
            Infos.UserDataSet userDataSet=new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(uData.get("NAME")));
            userDataSet.setType(convert(uData.get("TYPE")));
            userDataSet.setValue(convert(uData.get("VALUE")));
            userDataSet.setOriginator(convert(uData.get("ORIG")));
        }
        return userDataSets;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName,String refKey) {
        String sql=String.format("DELETE %s WHERE REFKEY=?",tableName);
        baseCore.insert(sql,refKey);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param refKey
     * @return com.fa.cim.Custom.List<com.fa.cim.dto.Infos.UserDataSet>
     * @exception
     * @author Ho
     * @date 2019/4/19 16:34
     */
    public List<Infos.UserDataSet> getLotOperationCompleteEventUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVMVO_CDA WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets=new ArrayList<>();
        for (Map<String,Object> uData:uDatas) {
            Infos.UserDataSet userDataSet=new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(uData.get("NAME")));
            userDataSet.setType(convert(uData.get("TYPE")));
            userDataSet.setValue(convert(uData.get("VALUE")));
            userDataSet.setOriginator(convert(uData.get("ORIG")));
        }
        return userDataSets;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return com.fa.cim.Custom.List<java.lang.String>
     * @exception
     * @author Ho
     * @date 2019/4/19 17:41
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> getEventFIFO(String tableName){
        String sql=String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE AND ID!='-1' ORDER BY EVENT_TIME ASC FOR UPDATE",tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events=new ArrayList<>();
        fifos.forEach(fifo->events.add(convert(fifo[0])));
        markFIFO(tableName);
        return events;
    }

    private void markFIFO(String tableName) {
        String sql=String.format("UPDATE %s SET ID='-1' WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE",tableName);
        baseCore.insert(sql);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationWaferSamplingRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/4/18 17:13
     */
    public Response insertLotOperationWaferSamplingHistory(Infos.OhopehsWafersampling lotOperationWaferSamplingRecord) {
        String hFHOPEHS_WAFERSAMPLING_LOT_ID   ;
        String hFHOPEHS_WAFERSAMPLING_MAINPD_ID;
        String hFHOPEHS_WAFERSAMPLING_OPE_NO   ;
        Integer hFHOPEHS_WAFERSAMPLING_OPE_PASS_COUNT;
        String hFHOPEHS_WAFERSAMPLING_CLAIM_TIME   ;
        String hFHOPEHS_WAFERSAMPLING_OPE_CATEGORY ;
        String hFHOPEHS_WAFERSAMPLING_SMPL_WAFER_ID;

        hFHOPEHS_WAFERSAMPLING_LOT_ID = lotOperationWaferSamplingRecord.getLot_id();
        hFHOPEHS_WAFERSAMPLING_MAINPD_ID = lotOperationWaferSamplingRecord.getMainpd_id();
        hFHOPEHS_WAFERSAMPLING_OPE_NO = lotOperationWaferSamplingRecord.getOpe_no();
        hFHOPEHS_WAFERSAMPLING_OPE_PASS_COUNT = lotOperationWaferSamplingRecord.getOpe_pass_count();
        hFHOPEHS_WAFERSAMPLING_CLAIM_TIME = lotOperationWaferSamplingRecord.getClaim_time();
        hFHOPEHS_WAFERSAMPLING_OPE_CATEGORY = lotOperationWaferSamplingRecord.getOpe_category();
        hFHOPEHS_WAFERSAMPLING_SMPL_WAFER_ID = lotOperationWaferSamplingRecord.getSmpl_wafer_id();

        baseCore.insert("INSERT INTO OHLOTOPE_SAMPLE\n" +
                "                ( ID, LOT_ID,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        OPE_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        OPE_CATEGORY,\n" +
                "                        SAMPLE_WAFER_ID)\n" +
                "        Values\n" +
                "                (  ?,?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?,"+
                    "               ?)",generateID(lotOperationWaferSamplingRecord.getClass()),hFHOPEHS_WAFERSAMPLING_LOT_ID,
                hFHOPEHS_WAFERSAMPLING_MAINPD_ID,
                hFHOPEHS_WAFERSAMPLING_OPE_NO,
                hFHOPEHS_WAFERSAMPLING_OPE_PASS_COUNT,
                convert(hFHOPEHS_WAFERSAMPLING_CLAIM_TIME),
                hFHOPEHS_WAFERSAMPLING_OPE_CATEGORY,
                hFHOPEHS_WAFERSAMPLING_SMPL_WAFER_ID);

        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/11 15:16
     */
    public Response insertLotWaferHistory(Infos.Ohwlths lotWaferRecord ) {

        String hFHWLTHS_WAFER_ID           ;
        String hFHWLTHS_CUR_LOT_ID         ;
        String hFHWLTHS_CUR_CAST_ID        ;
        String hFHWLTHS_CUR_CAST_CATEGORY  ;
        Integer hFHWLTHS_CUR_CAST_SLOT_NO;
        String hFHWLTHS_CLAIM_USER_ID      ;
        String hFHWLTHS_CLAIM_TIME         ;
        Double hFHWLTHS_CLAIM_SHOP_DATE;
        String hFHWLTHS_OPE_CATEGORY       ;
        String hFHWLTHS_APPLY_WAFER_FLAG   ;
        String hFHWLTHS_PRODSPEC_ID        ;
        Integer hFHWLTHS_GOOD_UNIT_COUNT;
        Integer hFHWLTHS_REPAIR_UNIT_COUNT;
        Integer hFHWLTHS_FAIL_UNIT_COUNT;
        String hFHWLTHS_EXIST_FLAG         ;
        Boolean hFHWLTHS_CONTROL_WAFER;
        String hFHWLTHS_PREV_LOT_ID        ;
        String hFHWLTHS_PREV_CAST_ID       ;
        String hFHWLTHS_PREV_CAST_CATEGORY ;
        Integer hFHWLTHS_PREV_CAST_SLOT_NO;
        String hFHWLTHS_REASON_CODE        ;
        String hFHWLTHS_REASON_DESCRIPTION ;
        String hFHWLTHS_ORG_WAFER_ID       ;
        String hFHWLTHS_ORG_PRODSPEC_ID    ;
        String hFHWLTHS_ALIAS_WAFER_NAME   ;
        String hFHWLTHS_EVENT_CREATE_TIME  ;
        String hFHWLTHS_EQP_ID             ;
        String hFHWLTHS_SORTER_JOB_ID      ;
        String hFHWLTHS_COMPONENT_JOB_ID   ;
        String hFHWLTHS_ORG_ALIAS_WAFER_NAME ;
        String hFHWLTHS_RELATED_WAFER_ID     ;

        hFHWLTHS_WAFER_ID = lotWaferRecord.getWafer_id            ();
        hFHWLTHS_CUR_LOT_ID = lotWaferRecord.getCur_lot_id          ();
        hFHWLTHS_CUR_CAST_ID = lotWaferRecord.getCur_cast_id         ();
        hFHWLTHS_CUR_CAST_CATEGORY = lotWaferRecord.getCur_cast_category   ();
        hFHWLTHS_CUR_CAST_SLOT_NO   = lotWaferRecord.getCur_cast_slot_no();
        hFHWLTHS_CLAIM_USER_ID = lotWaferRecord.getClaim_user_id       ();
        hFHWLTHS_CLAIM_TIME = lotWaferRecord.getClaim_time          ();
        hFHWLTHS_CLAIM_SHOP_DATE    = lotWaferRecord.getClaim_shop_date();
        hFHWLTHS_OPE_CATEGORY = lotWaferRecord.getOpe_category        ();
        hFHWLTHS_APPLY_WAFER_FLAG = lotWaferRecord.getApply_wafer_flag    ();
        hFHWLTHS_PRODSPEC_ID = lotWaferRecord.getProdspec_id         ();
        hFHWLTHS_GOOD_UNIT_COUNT   = lotWaferRecord.getGood_unit_count();
        hFHWLTHS_REPAIR_UNIT_COUNT = lotWaferRecord.getRepair_unit_count();
        hFHWLTHS_FAIL_UNIT_COUNT   = lotWaferRecord.getFail_unit_count();
        hFHWLTHS_EXIST_FLAG= lotWaferRecord.getExist_flag          ();
        hFHWLTHS_CONTROL_WAFER     = lotWaferRecord.getControl_wafer();
        hFHWLTHS_PREV_LOT_ID= lotWaferRecord.getPrev_lot_id         ();
        hFHWLTHS_PREV_CAST_ID= lotWaferRecord.getPrev_cast_id        ();
        hFHWLTHS_PREV_CAST_CATEGORY= lotWaferRecord.getPrev_cast_category  ();
        hFHWLTHS_PREV_CAST_SLOT_NO = lotWaferRecord.getPrev_cast_slot_no();
        hFHWLTHS_REASON_CODE= lotWaferRecord.getReason_code         ();
        hFHWLTHS_REASON_DESCRIPTION= lotWaferRecord.getReason_description  ();
        hFHWLTHS_ORG_WAFER_ID= lotWaferRecord.getOrg_wafer_id        ();
        hFHWLTHS_ORG_PRODSPEC_ID= lotWaferRecord.getOrg_prodspec_id     ();
        hFHWLTHS_ALIAS_WAFER_NAME= lotWaferRecord.getAlias_wafer_name    ();
        hFHWLTHS_EVENT_CREATE_TIME= lotWaferRecord.getEvent_create_time   ();
        hFHWLTHS_EQP_ID          = lotWaferRecord.getEquipmentID          ();
        hFHWLTHS_SORTER_JOB_ID   = lotWaferRecord.getSorterJobID          ();
        hFHWLTHS_COMPONENT_JOB_ID= lotWaferRecord.getComponentJobID       ();
        hFHWLTHS_ORG_ALIAS_WAFER_NAME= lotWaferRecord.getOrg_alias_wafer_name();
        hFHWLTHS_RELATED_WAFER_ID    = lotWaferRecord.getRelated_wafer_id    ();

        baseCore.insert("INSERT INTO OHWFRLOTHIS\n" +
                "            (  ID,WAFER_ID,\n" +
                "                    CUR_LOT_ID,\n" +
                "                    CUR_CARRIER_ID,\n" +
                "                    CUR_CARRIER_CATEGORY,\n" +
                "                    CUR_CARRIER_SLOT_NO,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    OPE_CATEGORY,\n" +
                "                    WAFER_COND_CHG,\n" +
                "                    PROD_ID,\n" +
                "                    GOOD_UNIT_COUNT,\n" +
                "                    REPAIR_UNIT_COUNT,\n" +
                "                    FAIL_UNIT_COUNT,\n" +
                "                    EXIST_FLAG,\n" +
                "                    NPW_WAFER,\n" +
                "                    PREV_LOT_ID,\n" +
                "                    PREV_CARRIER_ID,\n" +
                "                    PREV_CARRIER_CATEGORY,\n" +
                "                    PREV_CARRIER_SLOT_NO,\n" +
                "                    REASON_CODE,\n" +
                "                    REASON_DESC,\n" +
                "                    ORIG_WAFER_ID,\n" +
                "                    ORIG_PROD_ID,\n" +
                "                    STORE_TIME,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    EQP_ID,\n" +
                "                    SORTER_JOB_ID,\n" +
                "                    COMPONENT_JOB_ID,\n" +
                "                    ORIG_ALIAS_WAFER_NAME,\n" +
                "                    RELATED_WAFER_ID,\n" +
                "                    ALIAS_WAFER_NAME )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "            CURRENT_TIMESTAMP,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?)",generateID(Infos.Ohwlths.class)
                , hFHWLTHS_WAFER_ID
                , hFHWLTHS_CUR_LOT_ID
                , hFHWLTHS_CUR_CAST_ID
                , hFHWLTHS_CUR_CAST_CATEGORY
                , hFHWLTHS_CUR_CAST_SLOT_NO
                , hFHWLTHS_CLAIM_USER_ID
                , convert(hFHWLTHS_CLAIM_TIME)
                , hFHWLTHS_CLAIM_SHOP_DATE
                , hFHWLTHS_OPE_CATEGORY
                , hFHWLTHS_APPLY_WAFER_FLAG
                , hFHWLTHS_PRODSPEC_ID
                , hFHWLTHS_GOOD_UNIT_COUNT
                , hFHWLTHS_REPAIR_UNIT_COUNT
                , hFHWLTHS_FAIL_UNIT_COUNT
                , hFHWLTHS_EXIST_FLAG
                , hFHWLTHS_CONTROL_WAFER
                , hFHWLTHS_PREV_LOT_ID
                , hFHWLTHS_PREV_CAST_ID
                , hFHWLTHS_PREV_CAST_CATEGORY
                , hFHWLTHS_PREV_CAST_SLOT_NO
                , hFHWLTHS_REASON_CODE
                , hFHWLTHS_REASON_DESCRIPTION
                , hFHWLTHS_ORG_WAFER_ID
                , hFHWLTHS_ORG_PRODSPEC_ID
                , convert(hFHWLTHS_EVENT_CREATE_TIME)
                , hFHWLTHS_EQP_ID
                , hFHWLTHS_SORTER_JOB_ID
                , hFHWLTHS_COMPONENT_JOB_ID
                , hFHWLTHS_ORG_ALIAS_WAFER_NAME
                , hFHWLTHS_RELATED_WAFER_ID
                , hFHWLTHS_ALIAS_WAFER_NAME );


        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotOperationPasscntRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/4/18 17:14
     */
    public Response insertLotOperationPasscntHistory(Infos.OhopehsPasscnt lotOperationPasscntRecord) {
        String hFHOPEHS_PASSCNT_LOT_ID    ;
        String hFHOPEHS_PASSCNT_MAINPD_ID ;
        String hFHOPEHS_PASSCNT_OPE_NO    ;
        String hFHOPEHS_PASSCNT_CLAIM_TIME;
        Integer hFHOPEHS_PASSCNT_OPE_PASS_COUNT;
        String hFHOPEHS_PASSCNT_MOVE_TYPE   ;
        String hFHOPEHS_PASSCNT_OPE_CATEGORY;
        String hFHOPEHS_PASSCNT_WAFER_ID    ;
        Integer hFHOPEHS_PASSCNT_PASS_COUNT;

        hFHOPEHS_PASSCNT_LOT_ID = lotOperationPasscntRecord.getLot_id();
        hFHOPEHS_PASSCNT_MAINPD_ID = lotOperationPasscntRecord.getMainpd_id();
        hFHOPEHS_PASSCNT_OPE_NO = lotOperationPasscntRecord.getOpe_no();
        hFHOPEHS_PASSCNT_CLAIM_TIME = lotOperationPasscntRecord.getClaim_time();
        hFHOPEHS_PASSCNT_OPE_PASS_COUNT = lotOperationPasscntRecord.getOpe_pass_count();
        hFHOPEHS_PASSCNT_MOVE_TYPE = lotOperationPasscntRecord.getMove_type();
        hFHOPEHS_PASSCNT_OPE_CATEGORY = lotOperationPasscntRecord.getOpe_category();
        hFHOPEHS_PASSCNT_WAFER_ID = lotOperationPasscntRecord.getWafer_id();
        hFHOPEHS_PASSCNT_PASS_COUNT     = lotOperationPasscntRecord.getPass_count();

        baseCore.insert("INSERT INTO OHLOTOPE_PASSCNT\n" +
                "                ( ID,LOT_ID,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        TRX_TIME,\n" +
                "                        OPE_PASS_COUNT,\n" +
                "                        MOVE_TYPE,\n" +
                "                        OPE_CATEGORY,\n" +
                "                        WAFER_ID,\n" +
                "                        PASS_COUNT )\n" +
                "        VALUES\n" +
                "                ( ?, ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?,"+
                    "              ?)",generateID(lotOperationPasscntRecord.getClass()),hFHOPEHS_PASSCNT_LOT_ID,
                hFHOPEHS_PASSCNT_MAINPD_ID,
                hFHOPEHS_PASSCNT_OPE_NO,
                convert(hFHOPEHS_PASSCNT_CLAIM_TIME),
                hFHOPEHS_PASSCNT_OPE_PASS_COUNT,
                hFHOPEHS_PASSCNT_MOVE_TYPE,
                hFHOPEHS_PASSCNT_OPE_CATEGORY,
                hFHOPEHS_PASSCNT_WAFER_ID,
                hFHOPEHS_PASSCNT_PASS_COUNT );

        return returnOK();
    }
}
