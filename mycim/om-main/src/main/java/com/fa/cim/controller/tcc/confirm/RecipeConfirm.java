package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.recipe.IRecipeController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 16:56
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("RecipeConfirm")
@Transactional(rollbackFor = Exception.class)
public class RecipeConfirm implements IRecipeController {
    @Override
    public Response recipeDownloadReq(Params.RecipeDownloadReqParams params) {
        return null;
    }

    @Override
    public Response recipeDeleteInFileReq(Params.RecipeDeleteInFileReqParams params) {
        return null;
    }

    @Override
    public Response recipeCompareReq(Params.RecipeCompareReqParams params) {
        return null;
    }

    @Override
    public Response recipeUploadReq(Params.RecipeUploadReqParams params) {
        return null;
    }

    @Override
    public Response recipeDeleteReq(Params.RecipeDeleteReqParams params) {
        return null;
    }

    @Override
    public Response RecipeParamAdjustOnActivePJReq(Params.RecipeParamAdjustOnActivePJReqParam params) {
        return null;
    }

    @Override
    public Response RecipeParamAdjustReq(Params.RecipeParamAdjustReqParams params) {
        return null;
    }

    @Override
    public Response recipeParamAdjustRpt(Params.RecipeParamAdjustRptParams params) {
        return null;
    }
}