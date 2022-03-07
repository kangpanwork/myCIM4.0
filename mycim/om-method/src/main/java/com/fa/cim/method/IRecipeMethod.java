package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * description:
 * IRecipeMethod .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/30        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/7/30 17:19
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRecipeMethod {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param portGroupID
     * @param controlJobID
     * @param startCassetteList
     * @param processJobPauseFlag
     * @return
     * @author PlayBoy
     * @date 2018/7/30
     */
    void recipeParameterCheckConditionForOpeStart(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String
            portGroupID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList, boolean processJobPauseFlag);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strRecipeParameterAdjustHistoryGetDRIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.RecipeParameterAdjustHistoryGetDROut>
     * @throws
     * @author Ho
     * @date 2019/4/29 16:49
     */
    Outputs.RecipeParameterAdjustHistoryGetDROut recipeParameterAdjustHistoryGetDR(Infos.ObjCommon strObjCommonIn,
                                                                                   Infos.RecipeParameterAdjustHistoryGetDRIn strRecipeParameterAdjustHistoryGetDRIn);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<List < ObjectIdentifier>
     * @author PlayBoy
     * @date 2018/10/16
     */
    List<ObjectIdentifier> recipeRecipeIDGetDR(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/28 17:12
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @param strStartCassette
     * @param allProcessStartFlag -
     * @return java.util.List<com.fa.cim.dto.Infos.StartCassette>
     */
    List<Infos.StartCassette> recipeParameterAdjustConditionCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette, boolean allProcessStartFlag);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param controlJobID -
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @author Jerry
     * @date 2019/3/27 14:06
     */
    void recipeParameterCJPjConditionCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @param startCassettes -
     * @return java.util.List<com.fa.cim.dto.Infos.StartCassette>
     * @author Jerry
     * @date 2019/4/18 14:07
     */
    List<Infos.StartCassette> recipeParameterFillInTxTRC041(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassettes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.RecipeParameterCheckConditionForAdjustOut>
     * @author Jerry
     * @date 2019/7/23 14:58
     */
    Outputs.RecipeParameterCheckConditionForAdjustOut recipeParameterCheckConditionForAdjust(Infos.ObjCommon objCommon, Infos.RecipeParameterCheckConditionForAdjustIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param fileName  -
     * @return void
     * @author Nyx
     * @date 2019/7/24 10:28
     */
    void recipeBodyFileNameCheckNaming(Infos.ObjCommon objCommon, String fileName);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param machineRecipeID
     * @param fileLocation
     * @param fileName        -
     * @return void
     * @author Nyx
     * @date 2019/7/24 10:37
     */
    void recipeBodyFileNameCheckDuplicateDR(Infos.ObjCommon objCommon, ObjectIdentifier machineRecipeID, String fileLocation, String fileName);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/24 16:45
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjRecipeParameterCheckConditionForStoreOut recipeParameterCheckConditionForStore(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.LotWafer> strLotWaferSeq);

    /**
     * description:
     * <p>Get recipeBodyManageFlag of eqp, and set to out parameter.</p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * No such property: code for class: Script1
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @return RetCode<Boolean>
     * @author ZQI
     * @date 2018/12/6 11:30:30
     */

    Boolean recipeBodyManageFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/27                          Wind
     * @param objCommon
     * @param equipmentID
     * @return RetCode<List<Infos.RecipeParameterInfo>>
     * @author Wind
     * @date 2018/11/27 21:36
     */
    List<Infos.RecipeParameterInfo> recipeParameterInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

}
