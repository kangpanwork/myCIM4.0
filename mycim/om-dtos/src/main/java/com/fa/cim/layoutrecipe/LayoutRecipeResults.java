package com.fa.cim.layoutrecipe;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * description: layout recipe results
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/2 0002        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/3/2 0002 16:17
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class LayoutRecipeResults {

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentFurnaceResult {
        private ObjectIdentifier equipmentId;// eqp id
        private List<ObjectIdentifier> recipeList; //recipe list
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentFurnaceBindRecipeResult {
        private String layoutRecipeId;
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier machineRecipeId;
        private Integer lowerLimit;
        private Integer upperLimit;
        private ObjectIdentifier replaceRecipeId;
        private String memo;
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentFurnaceLayoutRecipeHistoryResult {
        private String equipmentId;
        private String machineRecipeId;
        private String operation;
        private String operator;
        private String reportTime;
        private String operationMemo;
        private Long lowerLimit;
        private Long lastLowerLimit;
        private Long upperLimit;
        private Long lastUpperLimit;
        private String replaceRecipeId;
        private String lastReplaceRecipeId;
        private String memo;
        private String lastMemo;
    }

}