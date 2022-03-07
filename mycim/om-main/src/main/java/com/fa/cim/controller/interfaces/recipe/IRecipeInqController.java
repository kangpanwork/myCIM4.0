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
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 16:18
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRecipeInqController {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/11 10:03
     */
    Response uploadedRecipeIdListByEqpInq(Params.UploadedRecipeIdListByEqpInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params -
     * @return com.fa.cim.common.support.Response
     * @author Nyx
     * @date 2019/7/23 15:59
     */
    Response recipeDirectoryInq(Params.RecipeDirectoryInqParam params);

    /**
     * description:
     *      The method use to define the AllRecipeIdListInqController.
     *      transaction ID: OCONW002
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/15        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/16 13:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response allRecipeIdListInq(@RequestBody Params.UserParams allRecipeIdListInqParams);
}