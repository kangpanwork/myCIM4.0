package com.fa.cim.service.pr;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/19          ********            jerry              create file
 *
 * @author: Jerry
 * @date: 2020/12/19 9:32
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPilotRunService {

    void sxPilotRunStatusChange(Infos.ObjCommon objCommon, Params.PilotRunStatusChangeParams params);

    void sxCreatePilotRunPlan(Infos.ObjCommon objCommon, Params.CreatePilotRunParams params);

    void sxCreatePilotRunJob(Infos.ObjCommon objCommon, Params.CreatePilotRunJobParams params);

    void sxPilotRunCancel(Infos.ObjCommon objCommon, Params.PilotRunCancelParams params);

    void sxCreatePilotRunRecipeGroup(Infos.ObjCommon objCommon,com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params);

    void sxUpdatePilotRunRecipeGroup(Infos.ObjCommon objCommon,com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params);

    void sxDeletePilotRunRecipeGroup(Infos.ObjCommon objCommon,com.fa.cim.pr.Params.DeletePilotRunRecipeGroupParams params);

    /**
     * description: create recipe job in batches from multiple recipe group, and add the creation event of recipe job
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 14:47                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 14:47
     * @param objCommon - user permission information
     * @param param - create parameters for recipe job
     * @return void
     */
    void sxCreateRecipeJob(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.CreatePilotJobInfoParams param);

    /**
     * description: batch delete recipe job from multiple recipe group, and add the delete event of recipe job
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 14:51                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 14:51
     * @param objCommon - user permission information
     * @param param - delete parameters for recipe job
     * @return void
     */
    void sxDeleteRecipeJob(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.DeletePilotJobInfoParams param);

    /**
     * description: recipe group binding lot, and add the binding event of recipe job
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 15:04                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 15:04
     * @param objCommon - user permission information
     * @param param - recipe job binds the parameters required by lot
     * @return void
     */
    void sxRecipeJobBindLot(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.RecipeJobBindLotParams param);

    /**
     * description: When the device state before and after the switch, in accordance with the provisions of recipe group, then create recipe job
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/20 15:12                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/20 15:12
     * @param objCommon
     * @param eqpStatusChangeReqParams
     * @param curEqpState -
     * @return void
     */
    void sxCreateRecipeJobByEqpStatusCut(Infos.ObjCommon objCommon, Params.EqpStatusChangeReqParams eqpStatusChangeReqParams, String curEqpState);
}
