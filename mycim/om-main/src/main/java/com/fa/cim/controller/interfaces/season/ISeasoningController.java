package com.fa.cim.controller.interfaces.season;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @return
 * @exception
 * @author ho
 * @date 2020/5/27 10:35
 */
public interface ISeasoningController {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonCreateReqPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 10:48
     */
    Response seasonPlanCreateReq(Params.SeasonPlanPrams seasonCreateReqPrams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonPlanReqPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 10:48
     */
    Response seasonPlanModifyReq(Params.SeasonPlanPrams seasonPlanReqPrams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonPlanPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 10:48
     */
    Response seasonPlanDeleteReq(Params.SeasonPlanPrams seasonPlanPrams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonPlansPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/4 16:48
     */
    Response seasonPlansDeleteReq(Params.SeasonPlansPrams seasonPlansPrams);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroupPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 10:50
     */
    Response seasonRecipeGroupCreateReq(Params.RecipeGroupPrams recipeGroupPrams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroupPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 10:51
     */
    Response seasonRecipeGroupModifyReq(Params.RecipeGroupPrams recipeGroupPrams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroupPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 10:52
     */
    Response seasonRecipeGroupDeleteReq(Params.RecipeGroupPrams recipeGroupPrams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonJobPrams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 10:56
     */
    Response seasonJobAbortReq(Params.SeasonJobPrams seasonJobPrams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonPriorityParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 13:24
     */
    Response seasonPriorityReq(Params.SeasonPriorityParams seasonPriorityParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonLotMoveInReserveReqParams
     * @return com.fa.cim.dto.Results.SeasonLotMoveInReserveReqResult
     * @exception
     * @author ho
     * @date 2020/6/10 14:06
     */
    Response seasonLotMoveInReserveReq(Params.SeasonLotMoveInReserveReqParams seasonLotMoveInReserveReqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonMoveInReserveReqParams
     * @return com.fa.cim.dto.Results.SeasonLotMoveInReserveReqResult
     * @exception
     * @author ho
     * @date 2020/6/10 14:06
     */
    Response seasonMoveInReserveReq(Params.SeasonMoveInReserveReqParams seasonMoveInReserveReqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param moveInReserveReqForSeasonParams
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/6/18 15:53
     */
    Response moveInReserveReq(Params.MoveInReserveReqForSeasonParams moveInReserveReqForSeasonParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param moveInReserveReqForSeasonParams
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/6/18 15:56
     */
    Response moveInReserveForIBReq(Params.MoveInReserveForIBReqForSeasonParams moveInReserveReqForSeasonParams);

}