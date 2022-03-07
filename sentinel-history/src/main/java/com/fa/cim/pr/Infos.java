package com.fa.cim.pr;

import lombok.Data;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/3        ********            Aoki                create file
 *
 * @author: Aoki
 * @date: 2021/3/3 19:06
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Infos {
    @Data
    public static class RecipeGroupEventRecord {
        private String action;
        private String recipeGroupID;
        private String type;
        private RecipeGroupEventData recipeGroupEventData;
        private List<RecipeEventData> RecipeEventData;
        private com.fa.cim.dto.Infos.EventData eventCommon;
        private String claimMemo;

    }
    @Data
    public static class RecipeGroupEventData {
        private String equipmentID;
        private String pilotRunType;
        private Integer pilotWaferCount;
        private Integer coverLevel;
        private Boolean coverRecipe;
        private String fromEqpState;
        private String toEqpState;
        private String claimMemo;

    }
    @Data
    public static class RecipeEventData{
        private String recipeID;
    }
    @Data
    public static class RecipeGroupHs{
        private String action;
        private String recipeGroupID;
        private String type;

        private String eventCreateTime;
        private String createTime;
        private String claimMemo;
        private String claimUserID;
    }
    @Data
    public static class RecipeGroupHsPr{
        private String equipmentID;
        private String pilotRunType;
        private Integer pilotWaferCount;
        private Integer coverLevel;
        private Boolean coverRecipe;
        private String fromEqpState;
        private String toEqpState;
        private String claimMemo;
    }
    @Data
    public static class RecipeGroupHsRecipe{
        private String recipeID;
    }
}
