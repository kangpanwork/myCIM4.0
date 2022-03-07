package com.fa.cim.pr;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.newcore.dto.event.RecipeGroupEvent;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import lombok.Data;

import java.security.PrivateKey;
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
    public static class ExperimentalGroupInfo{
        private String action;
        private ObjectIdentifier recipeGroupID;
        private String type;
        private ExperimentalRecipeGroupInfo recipeGroupEventData;
        private List<ExperimentalRecipeInfo> recipeIDs;
        private String cLaimMemo;

    }

    @Data
    public static class ExperimentalRecipeGroupInfo{
        private ObjectIdentifier equipmentID;
        private String pilotRunType;
        private Integer pilotWaferCount;
        private Integer coverLevel;
        private Boolean coverRecipe;
        private String fromEqpState;
        private String toEqpState;
        private String claimMemo;

    }

    @Data
    public static class ExperimentalRecipeInfo{
      private ObjectIdentifier recipeID;
    }

    @Data
    public static class RecipeGroupInfo{
        private Infos.ObjCommon common;
        private ObjectIdentifier recipeGroupID;
        private RecipeGroup.Type type;
        private RecipeGroupPrInfo recipeGroupEventData;
        private List<RecipeGroupRecipeInfo> recipeIDs;
        private String description; // description

    }

    @Data
    public static class RecipeGroupPrInfo{
        private ObjectIdentifier equipmentID;
        private String pilotRunType;
        private Integer pilotWaferCount;
        private Integer coverLevel;
        private Boolean coverRecipe;
        private String fromEqpState;
        private String toEqpState;
        private Boolean modifyFlag;
        private String claimMemo;

    }

    @Data
    public static class RecipeGroupRecipeInfo{
        private ObjectIdentifier recipeID;
    }

}
