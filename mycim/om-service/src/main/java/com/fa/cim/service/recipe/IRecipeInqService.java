package com.fa.cim.service.recipe;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

public interface IRecipeInqService {
    /**
     * The Method is txUploadedRecipeIdListByEqpInq.
     *
     * @param objCommon   objCommon
     * @param equipmentID equipmentID
     * @return RetCode<Results.UploadedRecipeIdListByEqpInqResult>
     */
    Results.UploadedRecipeIdListByEqpInqResult sxUploadedRecipeIdListByEqpInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) ;
    Results.RecipeDirectoryInqResult sxRecipeDirectoryInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<List<ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */

    List<ObjectIdentifier> sxAllRecipeIdListInq(Infos.ObjCommon objCommon);

    Results.RecipeIdListForDOCInqResult sxRecipeIdListForDOCInq(Infos.ObjCommon objCommon, Params.RecipeIdListForDOCInqParams recipeIdListForDOCInqParams) ;
}
