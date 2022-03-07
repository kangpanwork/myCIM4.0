package com.fa.cim.service.pr;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newcore.dto.pr.PilotRunInfo;
import com.fa.cim.pr.Results;

import java.util.List;

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
public interface IPilotRunInqService {

    List<PilotRunInfo.Plan> sxPilotRunList(Infos.ObjCommon objCommon, Params.PilotRunInqParams params);


    PilotRunInfo.JobInfo sxPilotJobInfo(Infos.ObjCommon objCommon, Params.PilotRunJobInfoParams params);


    List<Results.PilotRunRecipeGroupResults> sxPilotRunRecipeGroupList(Infos.ObjCommon objCommon,com.fa.cim.pr.Params.PilotRunRecipeGroupParams params);

    /**
     * description: inquire information about recipe job from equipment
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 15:27                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 15:27
     * @param objCommon - user permission information
     * @param params - query the equipment parameters required by recipe job
     * @return java.util.List<com.fa.cim.pr.Results.JobInfo>
     */
    List<com.fa.cim.pr.Results.JobInfo> sxPilotJobListInq(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.PilotJobInfoParams params);

    Results.RecipeResults sxPilotRunRecipeList(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.EquipmentParams params);
}
