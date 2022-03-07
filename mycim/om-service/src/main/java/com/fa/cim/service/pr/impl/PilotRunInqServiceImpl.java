package com.fa.cim.service.pr.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IPilotRunMethod;
import com.fa.cim.newcore.dto.pr.PilotRunInfo;
import com.fa.cim.pr.Results;
import com.fa.cim.service.pr.IPilotRunInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
 * @date: 2020/12/19 9:35
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmService
@Slf4j
public class PilotRunInqServiceImpl implements IPilotRunInqService {

    @Autowired
    private IPilotRunMethod pilotRunMethod;

    @Override
    public List<PilotRunInfo.Plan> sxPilotRunList(Infos.ObjCommon objCommon, Params.PilotRunInqParams params) {
        return pilotRunMethod.getPilotRunList(objCommon, params);
    }

    @Override
    public PilotRunInfo.JobInfo sxPilotJobInfo(Infos.ObjCommon objCommon, Params.PilotRunJobInfoParams params) {
        return pilotRunMethod.getPilotJobInfo(objCommon, params.getPiLotRunPlanID());
    }

    @Override
    public List<Results.PilotRunRecipeGroupResults> sxPilotRunRecipeGroupList(Infos.ObjCommon objCommon,com.fa.cim.pr.Params.PilotRunRecipeGroupParams params) {
        return pilotRunMethod.getPilotRunRecipeGroupList(objCommon, params.getRecipeGroupID(),params.getEquipmentID());
    }

    @Override
    public List<Results.JobInfo> sxPilotJobListInq(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.PilotJobInfoParams params) {
        return pilotRunMethod.getPilotJobListInq(objCommon,params.getEquipmentID());
    }

    @Override
    public Results.RecipeResults sxPilotRunRecipeList(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.EquipmentParams params) {
        return pilotRunMethod.getPilotRunRecipeList(objCommon, params.getEquipmentID());
    }
}
