package com.fa.cim.pr;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import lombok.Data;

import java.sql.Time;
import java.sql.Timestamp;
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
 * @date: 2021/3/1 14:45
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Results {

    @Data
    public static class JobInfo {
        private ObjectIdentifier pilotJobID;
        private ObjectIdentifier recipeGroupID;
        private ObjectIdentifier lotID;
        private String status;
        private ObjectIdentifier equipmentID;
        private String prType;
        private Integer pilotWaferCount;
        private Integer coverLevel;
        private Boolean coverRecipeFlag;
        private String fromEqpState;
        private String toEqpState;
        private String claimMemo;
        private List<ObjectIdentifier> recipeIDs;
        private Timestamp createTime;
    }

    @Data
    public static class PilotRunRecipeGroupResults {
        private String pilotType;
        private ObjectIdentifier equipmentID;
        private String fromEqpState;
        private String toEqpState;
        private ObjectIdentifier recipeGroupID;
        private Integer waferCount;
        private Integer coverLevel;
        private List<ObjectIdentifier> recipeIDs;
        private Boolean coverRecipe;
        private Timestamp createTime;
        private String claimMemo;
    }

    @Data
    public static class RecipeResults {
        private List<ObjectIdentifier> recipeIDs;
    }

}
