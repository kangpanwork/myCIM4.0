package com.fa.cim.controller.interfaces.pr;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/17          ********            jerry              create file
 *
 * @author: Jerry
 * @date: 2020/12/17 14:42
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

public interface IPilotRunController {

    Response pilotRunStatusChange(Params.PilotRunStatusChangeParams params);

    Response createPilotRunPlan(Params.CreatePilotRunParams params);

    Response createPilotRunJob(Params.CreatePilotRunJobParams params);

    Response pilotRunCancel(Params.PilotRunCancelParams params);

    Response createRecipeinfo(Params.CreateRecipeInfo params);

    Response createPilotRunRecipeGroupReq(com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params);

    Response deletePilotRunRecipeGroupReq(com.fa.cim.pr.Params.DeletePilotRunRecipeGroupParams params);

    Response updatePilotRunRecipeGroupReq(com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params);

    Response createRecipeJob(com.fa.cim.pr.Params.CreatePilotJobInfoParams params);

    Response deleteRecipeJob(com.fa.cim.pr.Params.DeletePilotJobInfoParams params);

    Response recipeJobBindLot(com.fa.cim.pr.Params.RecipeJobBindLotParams params);
}
