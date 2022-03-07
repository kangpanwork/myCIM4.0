package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.pr.IPilotRunController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/17          ********            jerry              create file
 *
 * @author: Jerry
 * @date: 2020/12/17 14:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@Service("PilotRunConfirm")
@Transactional(rollbackFor = Exception.class)
public class PilotRunConfirm implements IPilotRunController {
    @Override
    public Response pilotRunStatusChange(Params.PilotRunStatusChangeParams params) {
        return null;
    }

    @Override
    public Response createPilotRunPlan(Params.CreatePilotRunParams params) {
        return null;
    }

    @Override
    public Response createPilotRunJob(Params.CreatePilotRunJobParams params) {
        return null;
    }

    @Override
    public Response pilotRunCancel(Params.PilotRunCancelParams params) {
        return null;
    }

    @Override
    public Response createRecipeinfo(Params.CreateRecipeInfo params) {
        return null;
    }

    @Override
    public Response createPilotRunRecipeGroupReq(com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params) { return null; }

    @Override
    public Response deletePilotRunRecipeGroupReq(com.fa.cim.pr.Params.DeletePilotRunRecipeGroupParams params) { return null; }

    @Override
    public Response updatePilotRunRecipeGroupReq(com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params) { return null; }

    @Override
    public Response createRecipeJob(com.fa.cim.pr.Params.CreatePilotJobInfoParams params) {
        return null;
    }

    @Override
    public Response deleteRecipeJob(com.fa.cim.pr.Params.DeletePilotJobInfoParams params) {
        return null;
    }

    @Override
    public Response recipeJobBindLot(com.fa.cim.pr.Params.RecipeJobBindLotParams params) {
        return null;
    }

}
