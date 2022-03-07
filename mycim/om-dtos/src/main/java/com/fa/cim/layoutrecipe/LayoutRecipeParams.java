package com.fa.cim.layoutrecipe;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * description: batch layout recipe
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/2 0002        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/3/2 0002 15:35
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class LayoutRecipeParams {

    @Data
    private static class UserParams {
        private User user; // user
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EquipmentFurnaceSearchParams extends UserParams {

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EquipmentFurnaceBindRecipeSearchParams extends UserParams {
        private ObjectIdentifier equipmentId; // eqp id
        private ObjectIdentifier recipeId; // recipe id
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EquipmentFurnaceBindRecipeParams extends UserParams {
        private ObjectIdentifier equipmentId; // eqp id
        private ObjectIdentifier machineRecipeId; // machine recipe Id
        private Integer lowerLimit; // lower limit
        private Integer upperLimit; // upper limit
        private ObjectIdentifier replaceRecipeId; // replace recipe id
        private String memo; // configuration memo
        private String operationMemo; // operation memo
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EquipmentFurnaceRecipeChangeParams extends UserParams {
        private String layoutRecipeId;// layout recipe id
        private ObjectIdentifier equipmentId; // eqp id
        private ObjectIdentifier machineRecipeId; // machine recipe
        private Integer lowerLimit; // lower limit
        private Integer upperLimit; // upper limit
        private ObjectIdentifier replaceRecipeId; // replace recipe id
        private String memo; // configuration memo
        private String operationMemo; // operation memo
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EquipmentFurnaceRecipeClearParams extends UserParams {
        private List<String> layoutRecipeIds; // layout recipe ids
        private String operationMemo; // operation memo
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ConvertEquipmentFurnaceRecipeParams extends UserParams {
        // 在move in reserve / move in 时， 如果进行过手动点击 UI 进行过furnace recipe的改造，那么不再进行数据转换
        private boolean isDesignatedFurnaceControl;
        private ObjectIdentifier equipmentId; // eqp id
        private List<Infos.StartCassette> startCassettes; //start cassette

        public static final String TX_ID = "ELRPQ005";
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EquipmentFurnaceLayoutRecipeHistorySearchParams extends UserParams {
        private String equipmentId; // eqp id
        private String machineRecipeId; // machind recipe id
        private String startTime; // start time
        private String endTime; // end time
    }

    @Data
    public static class LayoutRecipeEventParams {
        private String equipmentID;
        private String equipmentObj;
        private String recipeID;
        private String recipeObj;
        private String replaceRecipeID;
        private String replaceRecipeObj;
        private Integer lowerLimit;
        private Integer upperLimit;
        private String memo;
        private String claimMemo;
        private String category;
        private Integer lastLowerLimit;
        private Integer lastUpperLimit;
        private String lastReplaceRecipeObj;
        private String lastReplaceRecipeID;
        private String lastMemo;
        private String lastOperationMemo;
    }

    @Getter
    public enum LayoutRecipeOperationCategoryEnum {
        Add,
        Modify,
        Delete,
        ;
    }

}