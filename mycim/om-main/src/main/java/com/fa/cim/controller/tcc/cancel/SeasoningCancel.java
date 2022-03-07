package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.season.ISeasoningController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/5/27 10:39
 */
@Service("SeasoningCancel")
@Transactional(rollbackFor = Exception.class)
public class SeasoningCancel implements ISeasoningController {

    @Override
    public Response seasonPlanCreateReq(Params.SeasonPlanPrams seasonCreateReqPrams) {
        return null;
    }

    @Override
    public Response seasonPlanModifyReq(Params.SeasonPlanPrams seasonPlanReqPrams) {
        return null;
    }

    @Override
    public Response seasonPlanDeleteReq(Params.SeasonPlanPrams seasonPlanPrams) {
        return null;
    }

    @Override
    public Response seasonPlansDeleteReq(Params.SeasonPlansPrams seasonPlansPrams) {
        return null;
    }

    @Override
    public Response seasonRecipeGroupCreateReq(Params.RecipeGroupPrams recipeGroupPrams) {
        return null;
    }

    @Override
    public Response seasonRecipeGroupModifyReq(Params.RecipeGroupPrams recipeGroupPrams) {
        return null;
    }

    @Override
    public Response seasonRecipeGroupDeleteReq(Params.RecipeGroupPrams recipeGroupPrams) {
        return null;
    }

    @Override
    public Response seasonJobAbortReq(Params.SeasonJobPrams seasonJobPrams) {
        return null;
    }

    @Override
    public Response seasonPriorityReq(Params.SeasonPriorityParams seasonPriorityParams) {
        return null;
    }

    @Override
    public Response seasonLotMoveInReserveReq(Params.SeasonLotMoveInReserveReqParams seasonLotMoveInReserveReqParams) {
        return null;
    }

    @Override
    public Response seasonMoveInReserveReq(Params.SeasonMoveInReserveReqParams seasonMoveInReserveReqParams) {
        return null;
    }

    @Override
    public Response moveInReserveReq(Params.MoveInReserveReqForSeasonParams moveInReserveReqForSeasonParams) {
        return null;
    }

    @Override
    public Response moveInReserveForIBReq(Params.MoveInReserveForIBReqForSeasonParams moveInReserveReqForSeasonParams) {
        return null;
    }
}