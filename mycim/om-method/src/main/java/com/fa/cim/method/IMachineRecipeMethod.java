package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * <p>This file use to define the IMachineRecipeMethod interface.<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2018/12/6         ********             ZQI               create file
 *
 * @author: ZQI
 * @date: 2018/12/6 13:27
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IMachineRecipeMethod {

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/14                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/14 10:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Outputs.MachineRecipe> machineRecipeGetListForFPCDR(Infos.ObjCommon objCommon, ObjectIdentifier machineRecipeID, String fpcCategory, String whiteDefSearchCriteria);
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/14                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/14 10:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Outputs.MachineRecipe> machineRecipeGetListByPDForFPC(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID, ObjectIdentifier pdID, String fpcCategory, String whiteDefSearchCriteria);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/3 15:02
     * @param objCommon
     * @param equipmentID
     * @param machineRecipeID -
     * @return void
     */
    void machineRecipeDownloadingInfoSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier machineRecipeID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/4 14:39
     * @param objCommon
     * @param equipmentID -
     * @return java.util.List<com.fa.cim.dto.Infos.MachineRecipeInfo>
     */
    List<Infos.MachineRecipeInfo> machineRecipeGetListByEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/4 15:39
     * @param objCommon
     * @param machineRecipeID -
     * @return void
     */
    void machineRecipeDeletionInfoSet(Infos.ObjCommon objCommon, ObjectIdentifier machineRecipeID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/24 10:44
     * @param objCommon
     * @param equipmentID
     * @param machineRecipeID
     * @param fileName
     * @param formatFlag -
     * @return void
     */
    void machineRecipeUploadingInfoSet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier machineRecipeID, String fileName, boolean formatFlag);

   /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @author lightyh
    * @date 2019/10/14 22:30
    * @param objCommon
    * @param objEquipmentRecipeGetListForRecipeBodyManagementIn -
    * @return java.util.List<com.fa.cim.dto.Infos.RecipeBodyManagement>
    */
    List<Infos.RecipeBodyManagement> machineRecipeGetListForRecipeBodyManagement(
            Infos.ObjCommon objCommon, Inputs.ObjEquipmentRecipeGetListForRecipeBodyManagementIn objEquipmentRecipeGetListForRecipeBodyManagementIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/9/20 15:02
     * @param objCommon
     * @param machineRecipe
     * @return java.util.List<com.fa.cim.common.support.ObjectIdentifier>
     */
    List<ObjectIdentifier> machineRecipeAllEquipmentGetDR (Infos.ObjCommon objCommon, ObjectIdentifier machineRecipe);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon objCommon
     * @param equipmentId equipmentID
     * @param startCassettes startCassettes
     * @return ObjLotCheckConditionForAutoBankInOut
     * @author Paladin
     * @date 2018/8/17
     */
    List<Infos.RecipeBodyManagement> machineRecipeGetListForRecipeBodyManagement(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId, List<Infos.StartCassette> startCassettes);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strMachineRecipeGetListForRecipeBodyManagementForDurablein
     * @return java.util.List<com.fa.cim.dto.Infos.RecipeBodyManagement>
     * @exception
     * @author ho
     * @date 2020/7/3 13:00
     */
     List<Infos.RecipeBodyManagement> machineRecipeGetListForRecipeBodyManagementForDurable(
            Infos.ObjCommon                                                 strObjCommonIn,
            Infos.MachineRecipeGetListForRecipeBodyManagementForDurableIn strMachineRecipeGetListForRecipeBodyManagementForDurablein );
}
