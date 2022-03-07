package com.fa.cim.controller.interfaces.recipe;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/4          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/4 14:00
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRecipeController {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/4 14:01
     */
    Response recipeDownloadReq(Params.RecipeDownloadReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/4 14:01
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response recipeDeleteInFileReq(Params.RecipeDeleteInFileReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/4 14:13
     */
    Response recipeCompareReq(Params.RecipeCompareReqParams params);

    /**
     * description:This function uploads Recipe Body in specified Equipment.
     * The uploaded Recipe Body is stored in Recipe Body File Server.
     * Recipe Body is used for comparison between MMServer and Equipment.
     * Equipment needs setting for Recipe Body Manage and the mode must be other than Off line.
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/23 16:59
     */
    Response recipeUploadReq(Params.RecipeUploadReqParams params);

    /**
     * description:This function deletes Recipe Body in Equipment.
     * When TRUE is specified for recipeFileDeleteFlag, Recipe Body File uploaded to system is also deleted.
     * Recipe Body is used for comparison between system and Equipment.
     * Equipment needs setting for Recipe Body Manage and the mode must be other than Off Line.
     * <p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/24 14:20
     */
    Response recipeDeleteReq(Params.RecipeDeleteReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/23       ********              Jerry             create file
     *
     * @author: Jerry
     * @date: 2019/7/23 13:44
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response RecipeParamAdjustOnActivePJReq(@RequestBody Params.RecipeParamAdjustOnActivePJReqParam params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/11       ********              Lin             create file
     *
     * @author: Lin
     * @date: 2018/12/11 17:35
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response RecipeParamAdjustReq(@RequestBody Params.RecipeParamAdjustReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/24 16:03
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response recipeParamAdjustRpt(@RequestBody Params.RecipeParamAdjustRptParams params);
}