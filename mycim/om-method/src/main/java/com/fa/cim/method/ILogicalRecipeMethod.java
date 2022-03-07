package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;

import java.util.List;

/**
 * description:
 * This file use to define the LogicalRecipeDefaultSettingDao interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/26        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/26 14:35
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILogicalRecipeMethod {
    /**
     * description:
     *      process_logicalRecipe_GetDR
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/12                             Nyx
     * @author panda
     * @date 2018/6/29
     * @param objCommon
     * @param productID
     * @param operationID -
     * @return com.fa.cim.pojo.Outputs.ObjProcessLogicalRecipeGetDROut
     */
    ObjectIdentifier processLogicalRecipeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier productID, ObjectIdentifier operationID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/7/25 14:11
     * @param objCommon
     * @param objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn -
     * @return com.fa.cim.pojo.Outputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut
     */
    Outputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut logicalRecipeCandidateChamberInfoGetByMachineRecipe(
            Infos.ObjCommon objCommon, Inputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn);

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/27                          Wind
      * @param objCommon
      * @param params
      * @return RetCode<List<Infos.RecipeParameterInfo>>
      * @author Wind
      * @date 2018/11/27 21:18
      */
    List<Infos.RecipeParameterInfo> logicalRecipeRecipeParameterInfoGetByPD(Infos.ObjCommon objCommon, Params.EqpRecipeParameterListInq params);

    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/4/23 12:07
     * @param objCommon - 
     * @param logicalRecipeID - 
     * @param lotID - 
     * @param equipmentID -  
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.common.support.ObjectIdentifier>
     */
    ObjectIdentifier logicalRecipeMachineRecipeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier logicalRecipeID, ObjectIdentifier lotID, ObjectIdentifier equipmentID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 11:29
     * @param objCommon
     * @param lotID
     * @param equipmentID
     * @param logicalRecipeID -
     * @return com.fa.cim.dto.Infos.DefaultRecipeSetting
     */
    Infos.DefaultRecipeSetting logicalRecipeDefaultRecipeSettingGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID, ObjectIdentifier logicalRecipeID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 11:29
     * @param objCommon
     * @param lotID
     * @param equipmentID
     * @param logicalRecipeID
     * @param processResourceReqFlag
     * @param recipeParameterReqFlag
     * @param fixtureRequireFlag -
     * @return com.fa.cim.dto.Infos.DefaultRecipeSetting
     */
    Infos.DefaultRecipeSetting logicalRecipeDefaultRecipeSettingGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID, ObjectIdentifier logicalRecipeID, Boolean processResourceReqFlag, Boolean recipeParameterReqFlag, Boolean fixtureRequireFlag);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/22 11:29
     * @param objCommon
     * @param logicalRecipeID
     * @param processResourceReqFlag
     * @param recipeParameterReqFlag
     * @param fixtureRequireFlag -
     * @return java.util.List<com.fa.cim.dto.Infos.DefaultRecipeSetting>
     */
    List<Infos.DefaultRecipeSetting> logicalRecipeAllDefaultRecipeSettingGetDR(Infos.ObjCommon objCommon, ObjectIdentifier logicalRecipeID, Boolean processResourceReqFlag, Boolean recipeParameterReqFlag, Boolean fixtureRequireFlag);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/8                          Wind
     * @param objCommon
     * @param operationID
     * @return RetCode<ObjectIdentifier>
     * @author Wind
     * @date 2018/11/8 17:21
     */
    ObjectIdentifier processDefaultLogicalRecipeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier operationID);

    Outputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDROut logicalRecipeMachineRecipeForSubLotTypeGetDR (Infos.ObjCommon objCommon,
                                                                                                             Inputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDRIn objLogicalRecipeMachineRecipeForSubLotTypeGetDRIn);

    /**
     * description: 通过logicalRecipeID 和subLotType 查找配置的machineRecipeID
     * change history:
     * date             defect#             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/8/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/8/20 14:49
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    ObjectIdentifier findBaseMachineRecipe(Infos.ObjCommon objCommon,
                                           ObjectIdentifier logicalRecipeID,
                                           ObjectIdentifier lotID,
                                           ObjectIdentifier equipmentID);
}
