package com.fa.cim.fsm;

import lombok.Data;

import java.util.List;

public class Infos {

    @Data
    public static class FutureSplitEventRecord {

        private String action;
        private String lotFamilyID;
        private String splitRouteID;
        private String splitOperationNumber;
        private String originalRouteID;
        private String originalOperationNumber;
        private Boolean actionEMail;
        private Boolean actionHold;
        private List<com.fa.cim.fsm.Infos.FutureSplitRouteEventData> Routes;
        private com.fa.cim.dto.Infos.EventData eventCommon;
        //add psmJobID for history
        private String fsmJobID;
    }

    @Data
    public static class FutureSplitRouteEventData {

        private String RouteID;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private String parentLotID;
        private String childLotID;
        private String memo;
        private List<com.fa.cim.fsm.Infos.FutureSplitedWaferEventData> wafers;

    }

    @Data
    public static class FutureSplitedWaferEventData {

        private String waferID;
        private String successFlag;
        private String groupNo;

    }

    @Data
    public static class  OhfuturehsWafer {
        private String  action_code;
        private String  lotfamily_id;
        private String  split_route_id;
        private String  split_ope_no;
        private String  original_route_id;
        private String  original_ope_no;

        private Integer  seq_no;
        private String  route_id;
        private String  parent_lot_id;
        private String  child_lot_id;
        private String  wafer_id;
        private String  group_no;
        private String  success_flag;
        private String  claim_time;
        //add psmJobID for history
        private String fusplitjob_id;
    }

    @Data
    public static class OhPilotJobhsRecipe {
        private String recipe_id;
        private String refkey;
    }

    @Data
    public static class  Ohfuturehs {
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
        private String    route_id;
        private String    parent_lot_id;
        private String    child_lot_id;
        private String    memo;
        private String    claim_time;
        private Double  claim_shop_date;
        private String    claim_user_id;
        private String    claim_memo;
        private String    event_create_time;

        //add psmJobID for history
        private String fusplitjob_id;
    }

    @Data
    public static class OhPilotJobhs {
        private String action_code;
        private String recipe_group_id;
        private String lot_id;
        private String status;
        private String eqp_id;
        private String pr_type;
        private Integer pilot_wafer_count;
        private Integer cover_level;
        private Integer cover_recipe;
        private String from_eqp_state;
        private String to_eqp_state;
//        private String create_time;
        private String event_create_time;
        private String claim_user_id;
        private String claim_memo;
    }

    @Data
    public static class PilotEventRecord {
        private String action;
        private String recipeGroupID;
        private String lotID;
        private String status;
        private String eqpID;
        private String prType;
        private Integer piLotWaferCount;
        private Integer coverLevel;
        private Integer coverRecipe;
        private String fromEqpState;
        private String toEqpState;
        private com.fa.cim.dto.Infos.EventData eventCommon;
        private List<Infos.PilotRecipeEventData> subRecipes;
    }

    @Data
    public static class PilotRecipeEventData {
        private String recipeID;
    }

}

