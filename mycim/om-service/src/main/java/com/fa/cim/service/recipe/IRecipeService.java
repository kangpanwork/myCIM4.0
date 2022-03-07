package com.fa.cim.service.recipe;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

public interface IRecipeService {
    void sxRecipeDeleteInFileReq(Infos.ObjCommon objCommon, Params.RecipeDeleteInFileReqParams params) ;
    void sxRecipeCompareReq(Infos.ObjCommon objCommon, Params.RecipeCompareReqParams params) ;
    void sxRecipeUploadReq(Infos.ObjCommon objCommon, Params.RecipeUploadReqParams params) ;
    void sxRecipeDeleteReq(Infos.ObjCommon objCommon, Params.RecipeDeleteReqParams params);
    Results.RecipeParamAdjustOnActivePJReqResult sxRecipeParamAdjustOnActivePJReq(Infos.ObjCommon objCommon, Params.RecipeParamAdjustOnActivePJReqParam params) ;
    void sxRecipeParamAdjustReq(Infos.ObjCommon objCommon,Params.RecipeParamAdjustReqParams params) ;
    void sxRecipeParamAdjustRpt(Infos.ObjCommon objCommon, Params.RecipeParamAdjustRptParams params) ;
    void sxRecipeDownloadReq(Infos.ObjCommon objCommon, Params.RecipeDownloadReqParams params) ;
}
