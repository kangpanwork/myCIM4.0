package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newcore.bo.pr.CimRecipeGroupPilotRunJob;
import com.fa.cim.newcore.bo.recipe.CimRecipeGroup;
import com.fa.cim.newcore.dto.pos.PilotRunSectionInfo;
import com.fa.cim.newcore.dto.pr.PilotRunInfo;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
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
 * @date: 2020/12/19 13:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPilotRunMethod {

    List<PilotRunInfo.Plan> getPilotRunList(Infos.ObjCommon common, Params.PilotRunInqParams params);

    void createPilotRunPlan(Infos.ObjCommon objCommon, Params.CreatePilotRunParams params);

    void deletePilotRunPlan(Infos.ObjCommon objCommon, ObjectIdentifier pilotRunID);

    void changePilotRunstatus(Infos.ObjCommon objCommon, Params.PilotRunStatusChangeParams params);

    PilotRunInfo.JobInfo getPilotJobInfo(Infos.ObjCommon objCommon, ObjectIdentifier pilotRunPlanID);

    List<PilotRunInfo.waferInfo> getPilotRunWaferInfo(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String PilotRunValue, PilotRunSectionInfo.SelectionRule waferSelectionRule);

    void createPilotRunJob(Infos.ObjCommon objCommon, ObjectIdentifier cildLotID, ObjectIdentifier pilotRunPlanID);

    void setForceMeasurementInfos(Infos.ObjCommon objCommon, ObjectIdentifier piLotrunPlanID, List<Infos.ForceMeasurementInfo> forceMeasurementInfos);

    void setSkipBlockInfo(Infos.ObjCommon objCommon, ObjectIdentifier pilotRunPlanID, List<Infos.ForceMeasurementInfo> forceMeasurementInfos);

    Infos.CheckPilotRunForEquipmentInfo checkPilotRunForEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassette);

    void checkPilotRunFail(Infos.ObjCommon objCommon, List<Infos.OpeCompLot> operationCompleteLot);

    void checkPilotRunCompleted(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes);

    List<PilotRunInfo.SectionSpec> getSectionSpecInfo(Infos.ObjCommon objCommon, List<PilotRunSectionInfo> pilotRunSectionInfo, ObjectIdentifier processPDID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/30 17:25                       Jerry               Create
     * return:0         success
     * return:1         job error
     * return:2         plan error
     *
     * @param objCommon
     * @param plans     -
     * @return java.lang.Integer
     * @author Jerry
     * @date 2020/12/30 17:25
     */

    PilotRunInfo.ErrorType checkPilotRunCompletedForFailed(Infos.ObjCommon objCommon, PilotRunInfo.Plan plans);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/2 9:16                       Jerry               Create
     *
     * @param common
     * @param lotID  -
     * @return java.util.List<com.fa.cim.newcore.dto.pr.PilotRunInfo.Plan>
     * @author Jerry
     * @date 2021/1/2 9:16
     */

    PilotRunInfo.Plan getPilotRunBylotID(Infos.ObjCommon common, ObjectIdentifier lotID);

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
     * @param equipmentID - equipment information
     * @return void
     */
    List<Results.JobInfo> getPilotJobListInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    void createPilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params);

    void updatePilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params);

    void deletePilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.DeletePilotRunRecipeGroupParams params);

    List<Results.PilotRunRecipeGroupResults> getPilotRunRecipeGroupList(Infos.ObjCommon objCommon, ObjectIdentifier recipeGroupID, ObjectIdentifier equipmentID);

    Results.RecipeResults getPilotRunRecipeList(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    void checkPMRun(ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassettes);

    /**
     * description: create recipe job from recipe group
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 14:33                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 14:33
     * @param objCommon - user permission information
     * @param recipeGroupID - the recipe group used to create recipe job
     * @return com.fa.cim.newcore.bo.pr.CimRecipeGroupPilotRunJob
     */
    CimRecipeGroupPilotRunJob createRecipeJob(Infos.ObjCommon objCommon, ObjectIdentifier recipeGroupID);

    /**
     * description: bind Lot to recipe job
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 15:15                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 15:15
     * @param objCommon - user permission information
     * @param param - recipe job binds the parameters required by lot
     * @return void
     */
    void recipeJobBindLot(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.RecipeJobBindLotParams param);

    List<CimRecipeGroup> getRecipeGroupByEqp(ObjectIdentifier equipmentID);

    List<Results.PilotRunRecipeGroupResults> getRecipeGroup(ObjectIdentifier recipeGroupID, ObjectIdentifier equipmentID);

    RecipeGroup.Info getRecipeGroup(ObjectIdentifier recipeGroupID);
}
