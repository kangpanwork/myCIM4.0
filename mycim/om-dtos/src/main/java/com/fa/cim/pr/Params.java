package com.fa.cim.pr;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * description:
 *
 * <p>change history: date defect# person comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/1 ******** Aoki create file
 *
 * @author: Aoki
 * @date: 2021/3/1 14:44
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Params {

    @Data
    public static class PilotRunRecipeGroupParams {
        private User user;
        @Valid
        private ObjectIdentifier recipeGroupID;
        @Valid
        private ObjectIdentifier equipmentID;
        private String claimMemo;
    }

    @Data
    public static class CreatePilotRunRecipeGroupParams {
        private User user;

        @NotBlank(message = "pilotType not be blank")
        private String pilotType;

        @Valid
        private ObjectIdentifier equipmentID;

        @NotBlank(message = "EqpState fromEqpState not be blank")
        private String fromEqpState;

        @NotBlank(message = "EqpState toEqpState not be blank")
        private String toEqpState;

        @Valid
        private ObjectIdentifier recipeGroupID;

        @Max(value = 25, message = "wafer count maust Less than 25")
        @Min(value = 1, message = "wafer count maust more than 0")
        @NotNull(message = "waferCount is blank")
        private Integer waferCount;

        @Min(value = 1, message = "coverLevel maust more than 0")
        @NotNull(message = "coverLevel is blank")
        private Integer coverLevel;

        @NotNull(message = "coverRecipeFlag not be blank")
        private Boolean coverRecipeFlag;

        private List<ObjectIdentifier> recipeIDs;
        private String claimMemo;
    }

    @Data
    public static class UpdatePilotRunRecipeGroupParams {
        private User user;

        @NotBlank(message = "pilotType not be blank")
        private String pilotType;

        @Valid
        private ObjectIdentifier equipmentID;

        @NotBlank(message = "EqpState fromEqpState not be blank")
        private String fromEqpState;

        @NotBlank(message = "EqpState toEqpState not be blank")
        private String toEqpState;

        @Valid
        private ObjectIdentifier recipeGroupID;

        @Max(value = 25, message = "wafer count maust Less than 25")
        @Min(value = 1, message = "wafer count maust more than 0")
        @NotNull(message = "waferCount is blank")
        private Integer waferCount;

        @Min(value = 1, message = "coverLevel maust more than 0")
        @NotNull(message = "coverLevel is blank")
        private Integer coverLevel;

        @NotNull(message = "coverRecipeFlag not be blank")
        private Boolean coverRecipeFlag;

        private List<ObjectIdentifier> recipeIDs;
        private String claimMemo;
    }

    @Data
    public static class DeletePilotRunRecipeGroupParams {
        private User user;
        private List<ObjectIdentifier> groupIDs;
        private String claimMemo;
    }

    @Data
    public static class PilotJobInfoParams {
        private User user;

        @Valid
        @NotNull(message = "equipmentID not be null")
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class CreatePilotJobInfoParams {
        private User user;
        @Valid
        private List<ObjectIdentifier> recipeGroupIDs;
    }

    @Data
    public static class DeletePilotJobInfoParams {
        private User user;
        @Valid
        private List<ObjectIdentifier> recipeJobIDs;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class RecipeJobBindLotParams {
        private User user;

        @Valid
        @NotNull(message = "recipeJobID not be null")
        private ObjectIdentifier recipeJobID;

        @Valid
        @NotNull(message = "lotID not be null")
        private ObjectIdentifier lotID;
    }

    @Data
    public static class EquipmentParams {
        private User user;
        private ObjectIdentifier equipmentID;
    }
}
