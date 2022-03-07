package com.fa.cim.service.pr.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IPilotRecipeJobEventMethod;
import com.fa.cim.method.IPilotRunMethod;
import com.fa.cim.method.impl.ObjectLockMethod;
import com.fa.cim.method.impl.RecipeGroupEventMethod;
import com.fa.cim.newcore.bo.code.CimE10State;
import com.fa.cim.newcore.bo.code.CimMachineState;
import com.fa.cim.newcore.bo.code.CodeManager;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.pr.CimRecipeGroupPilotRunJob;
import com.fa.cim.newcore.bo.pr.PilotRunManager;
import com.fa.cim.newcore.bo.recipe.CimRecipeGroup;
import com.fa.cim.newcore.bo.recipe.RecipeManager;
import com.fa.cim.newcore.dto.pr.PilotRunInfo;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import com.fa.cim.newcore.dto.rcppr.RecipePilotRun;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.pr.Info;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.pr.IPilotRunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/19          ********            jerry              create file
 *
 * @author: Jerry
 * @date: 2020/12/19 9:34
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmService
@Slf4j
public class PilotRunServiceImpl implements IPilotRunService {

    @Autowired
    private IPilotRunMethod pilotRunMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RecipeGroupEventMethod recipeGroupEventMethod;

    @Autowired
    private IPilotRecipeJobEventMethod pilotRecipeJobEventMethod;

    @Autowired
    private RecipeManager recipeManager;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private PilotRunManager pilotRunManager;

    @Autowired
    private CodeManager codeManager;

    @Autowired
    private ObjectLockMethod objectLockMethod;

    @Override
    public void sxPilotRunStatusChange(Infos.ObjCommon objCommon, Params.PilotRunStatusChangeParams params) {
        //check status
        if (CimStringUtils.equals(PilotRunInfo.EntityType.PiLotRunPlan.getValue(), params.getEntityType())) {
            /*
            Params.PilotRunInqParams pilotRunInqParams = new Params.PilotRunInqParams();
            pilotRunInqParams.setPilotRunPlanID(params.getEntity());
            List<PilotRunInfo.Plan> pilotRunPlans = pilotRunMethod.getPilotRunList(objCommon, pilotRunInqParams);
            if (ArrayUtils.isNotEmpty(pilotRunPlans)) {
                for (PilotRunInfo.Plan pilotRunPlan : pilotRunPlans) {
                    Validations.check(StringUtils.equals(pilotRunPlan.getStatus().getValue(), PilotRunInfo.Status.Waiting.getValue())
                            , retCodeConfigEx.getChangePiLotRunPlanstatusError());
                    for (PilotRunInfo.Job pilotRunJob : pilotRunPlan.getPilotRunJobs()) {
                        Validations.check(StringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Created.getValue())
                                        || StringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Ongoing.getValue())
                                , retCodeConfigEx.getChangePiLotRunJobstatusError());
                    }
                }
            }
            */

            //change
            pilotRunMethod.changePilotRunstatus(objCommon, params);

        } else if (CimStringUtils.equals(PilotRunInfo.EntityType.PiLotRunJob.getValue(), params.getEntityType())) {
           /* PilotRunInfo.Job piLotRunJobInfo = pilotRunMethod.getPiLotRunJobInfo(objCommon, params.getEntity());
            Validations.check(StringUtils.equals(piLotRunJobInfo.getStatus().getValue(), PilotRunInfo.Status.Created.getValue())
                            || StringUtils.equals(piLotRunJobInfo.getStatus().getValue(), PilotRunInfo.Status.Ongoing.getValue())
                    , retCodeConfigEx.getChangePiLotRunJobstatusError());*/

            //change
            pilotRunMethod.changePilotRunstatus(objCommon, params);

        } else {
            Validations.check(retCodeConfigEx.getInputParameterEntityTypeError(), params.getEntityType());
        }

        //Make event

    }

    @Override
    public void sxCreatePilotRunPlan(Infos.ObjCommon objCommon, Params.CreatePilotRunParams params) {
        //check status
        Params.PilotRunInqParams pilotRunInqParams = new Params.PilotRunInqParams();
        pilotRunInqParams.setEquipmentID(params.getTagEquipment());
        List<PilotRunInfo.Plan> pilotRunPlans = pilotRunMethod.getPilotRunList(objCommon, pilotRunInqParams);
        if (CimArrayUtils.isNotEmpty(pilotRunPlans)) {
            for (PilotRunInfo.Plan pilotRunPlan : pilotRunPlans) {
                Validations.check(CimStringUtils.equals(pilotRunPlan.getStatus().getValue(), PilotRunInfo.Status.Waiting.getValue())
                                || CimStringUtils.equals(pilotRunPlan.getStatus().getValue(), PilotRunInfo.Status.Ongoing.getValue()),
                        retCodeConfigEx.getCreatePilotRunPlanStatuesError(), pilotRunPlan.getStatus().getValue());
                for (PilotRunInfo.Job pilotRunJob : pilotRunPlan.getPilotRunJobs()) {
                    Validations.check(CimStringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Waiting.getValue())
                                    || CimStringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Ongoing.getValue())
                                    || CimStringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Created.getValue()),
                            retCodeConfigEx.getCreatePilotRunPlanJobStatuesError(), pilotRunJob.getStatus().getValue());
                }
            }
        }
        // create plan
        pilotRunMethod.createPilotRunPlan(objCommon, params);

        //Make event
    }

    @Override
    public void sxCreatePilotRunJob(Infos.ObjCommon objCommon, Params.CreatePilotRunJobParams params) {
        // check status
        Params.PilotRunInqParams pilotRunInqParams = new Params.PilotRunInqParams();
        pilotRunInqParams.setPilotRunPlanID(params.getPiLotrunPlanID());
        List<PilotRunInfo.Plan> pilotRunPlans = pilotRunMethod.getPilotRunList(objCommon, pilotRunInqParams);
        if (CimArrayUtils.isNotEmpty(pilotRunPlans)) {
            for (PilotRunInfo.Plan pilotRunPlan : pilotRunPlans) {
                Validations.check(CimStringUtils.equals(pilotRunPlan.getStatus().getValue(), PilotRunInfo.Status.Completed.getValue())
                                || CimStringUtils.equals(pilotRunPlan.getStatus().getValue(), PilotRunInfo.Status.Failed.getValue())
                        , retCodeConfigEx.getChangePiLotRunPlanstatusError());
                for (PilotRunInfo.Job pilotRunJob : pilotRunPlan.getPilotRunJobs()) {
                    Validations.check(CimStringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Created.getValue())
                                    || CimStringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Ongoing.getValue())
                            , retCodeConfigEx.getChangePiLotRunJobstatusError());
                }
            }
        }

        // spilt and future hold
        ObjectIdentifier cildLotID = null;
        List<Infos.LotWaferAttributes> lotWaferAttributes = lotMethod.lotMaterialsGetWafers(objCommon, params.getParentLotID());
        //check all pi
        if (CimArrayUtils.getSize(lotWaferAttributes) == CimArrayUtils.getSize(params.getChildWaferIDs())) {
            cildLotID = params.getParentLotID();
        } else {
            Params.SplitLotReqParams splitLotReqParams = new Params.SplitLotReqParams();
            splitLotReqParams.setParentLotID(params.getParentLotID());
            splitLotReqParams.setChildWaferIDs(params.getChildWaferIDs());
            splitLotReqParams.setFutureMergeFlag(true);
            splitLotReqParams.setMergedOperationNumber(params.getMergedOperationNumber());
            splitLotReqParams.setMergedRouteID(params.getMergedRouteID());
            cildLotID = lotService.sxSplitLotReq(objCommon, splitLotReqParams).getChildLotID();
        }

        //create pilot run job
        pilotRunMethod.createPilotRunJob(objCommon, cildLotID, params.getPiLotrunPlanID());

        // set pilotRun FM
        pilotRunMethod.setForceMeasurementInfos(objCommon, params.getPiLotrunPlanID(), params.getForceMeasurementInfos());

        //set BL
        pilotRunMethod.setSkipBlockInfo(objCommon, pilotRunInqParams.getPilotRunPlanID(), params.getForceMeasurementInfos());

    }

    @Override
    public void sxPilotRunCancel(Infos.ObjCommon objCommon, Params.PilotRunCancelParams params) {
        //check status
        Params.PilotRunInqParams pilotRunInqParams = new Params.PilotRunInqParams();
        pilotRunInqParams.setPilotRunPlanID(params.getPilotRunID());
        List<PilotRunInfo.Plan> pilotRunPlans = pilotRunMethod.getPilotRunList(objCommon, pilotRunInqParams);
        if (CimArrayUtils.isNotEmpty(pilotRunPlans)) {
            for (PilotRunInfo.Plan pilotRunPlan : pilotRunPlans) {
                Validations.check(CimStringUtils.equals(pilotRunPlan.getStatus().getValue(), PilotRunInfo.Status.Ongoing.getValue()),
                        retCodeConfigEx.getCreatePilotRunPlanStatuesError(), pilotRunPlan.getStatus().getValue());
                for (PilotRunInfo.Job pilotRunJob : pilotRunPlan.getPilotRunJobs()) {
                    Validations.check(CimStringUtils.equals(pilotRunJob.getStatus().getValue(), PilotRunInfo.Status.Ongoing.getValue()),
                            retCodeConfigEx.getCreatePilotRunPlanJobStatuesError(), pilotRunJob.getStatus().getValue());
                }
            }
        }

        //delete pilot
        pilotRunMethod.deletePilotRunPlan(objCommon, params.getPilotRunID());

        //Make event
    }

    @Override
    public void sxCreatePilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params) {
        // make txID
        String txID = TransactionIDEnum.CREATE_PILOT_RUN_RECIPE_GROUP.getValue();
        // check params
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(ObjectIdentifier.isEmptyWithValue(equipmentID), retCodeConfig.getNotFoundEqp(), equipmentID.getValue());
        ObjectIdentifier recipeGroupID = params.getRecipeGroupID();
        Validations.check(ObjectIdentifier.isEmptyWithValue(recipeGroupID), retCodeConfig.getInvalidRecipeForEqp(), recipeGroupID.getValue());

        //change recipeGroupID constructure ,add prefix PMPilotRun
        recipeGroupID.setValue(String.format("%s.%s", RecipeGroup.Type.PMPilotRun, recipeGroupID.getValue()));

        log.info("find the entity of the recipeGroup {}", params.getRecipeGroupID());
        RecipeGroup.Info info = pilotRunMethod.getRecipeGroup(recipeGroupID);

        log.info("check if recipe group entity is eixst");
        Validations.check(info != null, retCodeConfigEx.getPrRecipeGroupAlreadyExists(), Optional.ofNullable(info).isPresent() ? info.getRecipePilotRunInfo().getEquipmentID() : "",
                params.getRecipeGroupID().getValue().replaceAll(String.format("%s.", RecipeGroup.Type.PMPilotRun), ""));

        //check recipelist size more than 0
        Validations.check(CimArrayUtils.isEmpty(params.getRecipeIDs()), retCodeConfigEx.getPrRecipeIsEmpty());
        // create recipeGroup
        pilotRunMethod.createPilotRunRecipeGroup(objCommon, params);

        //create history
        Info.ExperimentalGroupInfo experimentalGroupInfo = new Info.ExperimentalGroupInfo();
        Info.ExperimentalRecipeGroupInfo experimentalRecipeGroupInfo = new Info.ExperimentalRecipeGroupInfo();
        ArrayList<Info.ExperimentalRecipeInfo> recipIDs = new ArrayList<>();

        //set event data for group
        experimentalGroupInfo.setRecipeIDs(recipIDs);
        experimentalGroupInfo.setAction("CREATE");
        experimentalGroupInfo.setRecipeGroupID(params.getRecipeGroupID());
        experimentalGroupInfo.setType(RecipeGroup.Type.PMPilotRun.toString());
        experimentalGroupInfo.setCLaimMemo(params.getClaimMemo());

        //set event data for recipeGroup detail
        experimentalRecipeGroupInfo.setEquipmentID(params.getEquipmentID());
        experimentalRecipeGroupInfo.setPilotRunType(params.getPilotType());
        experimentalRecipeGroupInfo.setPilotWaferCount(params.getWaferCount());
        experimentalRecipeGroupInfo.setCoverLevel(params.getCoverLevel());
        experimentalRecipeGroupInfo.setCoverRecipe(params.getCoverRecipeFlag());
        experimentalRecipeGroupInfo.setFromEqpState(params.getFromEqpState());
        experimentalRecipeGroupInfo.setToEqpState(params.getToEqpState());
        experimentalRecipeGroupInfo.setClaimMemo(params.getClaimMemo());

        // set recipe data detail
        params.getRecipeIDs().stream().parallel().forEach(re -> {
            Info.ExperimentalRecipeInfo experimentalRecipeInfo = new Info.ExperimentalRecipeInfo();
            experimentalRecipeInfo.setRecipeID(re);
            recipIDs.add(experimentalRecipeInfo);
        });

        experimentalGroupInfo.setRecipeGroupEventData(experimentalRecipeGroupInfo);
        experimentalGroupInfo.setRecipeIDs(recipIDs);

        recipeGroupEventMethod.experimentalRecipeGroupRegistEventMake(objCommon, txID, params.getClaimMemo(), experimentalGroupInfo);
    }

    @Override
    public void sxUpdatePilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params) {
        // make txID
        String txID = TransactionIDEnum.UPDATE_PILOT_RUN_RECIPE_GROUP.getValue();
        // check params
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(ObjectIdentifier.isEmptyWithValue(equipmentID), retCodeConfig.getNotFoundEqp(), equipmentID.getValue());
        ObjectIdentifier recipeGroupID = params.getRecipeGroupID();
        Validations.check(ObjectIdentifier.isEmptyWithValue(recipeGroupID), retCodeConfig.getInvalidRecipeForEqp(), recipeGroupID.getValue());
        //check recipelist size more than 0
        Validations.check(CimArrayUtils.isEmpty(params.getRecipeIDs()), retCodeConfigEx.getPrRecipeIsEmpty());

        //change recipeGroupID constructure ,add prefix PMPilotRun
        recipeGroupID.setValue(String.format("%s.%s", RecipeGroup.Type.PMPilotRun, recipeGroupID.getValue()));

        log.info("find the entity of the recipeGroup {}", recipeGroupID);
        RecipeGroup.Info info = pilotRunMethod.getRecipeGroup(recipeGroupID);

        log.info("check if recipe group entity is eixst");
        Validations.check(info == null, retCodeConfigEx.getPrCannotFoundRecipeGroup(), recipeGroupID.getValue().replaceAll(String.format("%s.", RecipeGroup.Type.PMPilotRun), ""));

        // if need lock
        log.debug("object lock bo");
        objectLockMethod.objectLock(objCommon, CimRecipeGroup.class, params.getRecipeGroupID());

        // create recipeGroup

        pilotRunMethod.updatePilotRunRecipeGroup(objCommon, params);

        //create history
        Info.ExperimentalGroupInfo experimentalGroupInfo = new Info.ExperimentalGroupInfo();
        Info.ExperimentalRecipeGroupInfo experimentalRecipeGroupInfo = new Info.ExperimentalRecipeGroupInfo();
        ArrayList<Info.ExperimentalRecipeInfo> recipIDs = new ArrayList<>();

        //set event data for group
        experimentalGroupInfo.setRecipeIDs(recipIDs);
        experimentalGroupInfo.setAction("UPDATE");
        experimentalGroupInfo.setRecipeGroupID(params.getRecipeGroupID());
        experimentalGroupInfo.setType(RecipeGroup.Type.PMPilotRun.toString());
        experimentalGroupInfo.setCLaimMemo(params.getClaimMemo());

        //set event data for recipeGroup detail
        experimentalRecipeGroupInfo.setEquipmentID(params.getEquipmentID());
        experimentalRecipeGroupInfo.setPilotRunType(params.getPilotType());
        experimentalRecipeGroupInfo.setPilotWaferCount(params.getWaferCount());
        experimentalRecipeGroupInfo.setCoverLevel(params.getCoverLevel());
        experimentalRecipeGroupInfo.setCoverRecipe(params.getCoverRecipeFlag());
        experimentalRecipeGroupInfo.setFromEqpState(params.getFromEqpState());
        experimentalRecipeGroupInfo.setToEqpState(params.getToEqpState());
        experimentalRecipeGroupInfo.setClaimMemo(params.getClaimMemo());

        // set recipe data detail
        params.getRecipeIDs().stream().parallel().forEach(re -> {
            Info.ExperimentalRecipeInfo experimentalRecipeInfo = new Info.ExperimentalRecipeInfo();
            experimentalRecipeInfo.setRecipeID(re);
            recipIDs.add(experimentalRecipeInfo);
        });

        experimentalGroupInfo.setRecipeGroupEventData(experimentalRecipeGroupInfo);
        experimentalGroupInfo.setRecipeIDs(recipIDs);

        recipeGroupEventMethod.experimentalRecipeGroupRegistEventMake(objCommon, txID, params.getClaimMemo(), experimentalGroupInfo);
    }

    @Override
    public void sxDeletePilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.DeletePilotRunRecipeGroupParams params) {
        // make txID
        String txID = TransactionIDEnum.DELETE_PILOT_RUN_RECIPE_GROUP.getValue();
        // check params
        log.info("check groupID is not null");
        Validations.check(CimArrayUtils.isEmpty(params.getGroupIDs()), retCodeConfig.getInvalidParameter());

        // maske the result list for the data

        // find the need delete data
        List<ObjectIdentifier> groupIDs = params.getGroupIDs();
        //make history data
        ArrayList<Info.ExperimentalGroupInfo> experimentalGroupInfos = new ArrayList<>();
        groupIDs.forEach(groupID -> {
            log.info("find entity for groupID", groupID);
            if (ObjectIdentifier.isNotEmpty(groupID)) {

                //change recipeGroupID constructure ,add prefix PMPilotRun
                groupID.setValue(String.format("%s.%s", RecipeGroup.Type.PMPilotRun, groupID.getValue()));
            }

            log.info("find the entity of the recipeGroup {}", groupID);
            RecipeGroup.Info info = pilotRunMethod.getRecipeGroup(groupID);

            log.info("check if recipe group entity is eixst");
            Validations.check(info == null, retCodeConfigEx.getPrNotFoundRecipeGroup(), groupID.getValue().replaceAll(String.format("%s.", RecipeGroup.Type.PMPilotRun), ""));

            log.debug("object lock bo");
            objectLockMethod.objectLock(objCommon, CimRecipeGroup.class, groupID);
            CimRecipeGroup recipeGroup = baseCoreFactory.getBO(CimRecipeGroup.class, groupID);

            log.info("start create event data");
            Info.ExperimentalGroupInfo experimentalGroupInfo = new Info.ExperimentalGroupInfo();
            Info.ExperimentalRecipeGroupInfo experimentalRecipeGroupInfo = new Info.ExperimentalRecipeGroupInfo();
            ArrayList<Info.ExperimentalRecipeInfo> recipIDs = new ArrayList<>();

            //set event data for group
            log.info("set event data for group");
            experimentalGroupInfo.setRecipeIDs(recipIDs);
            experimentalGroupInfo.setAction("DELETE");
            experimentalGroupInfo.setRecipeGroupID(recipeGroup.getRecipeGroupInfo().getRecipeGroupID());
            experimentalGroupInfo.setType(recipeGroup.getRecipeGroupInfo().getType().toString());
            experimentalGroupInfo.setCLaimMemo(recipeGroup.getRecipeGroupInfo().getDescription());

            //set event data for recipeGroup PR detail
            log.info("set event data for recipeGroup PR detail");
            experimentalRecipeGroupInfo.setEquipmentID(recipeGroup.getRecipePilotRunInfo().getEquipmentID());
            experimentalRecipeGroupInfo.setPilotRunType(recipeGroup.getRecipePilotRunInfo().getPilotRunType().toString());
            experimentalRecipeGroupInfo.setPilotWaferCount(recipeGroup.getRecipePilotRunInfo().getPilotRunWaferCount());
            experimentalRecipeGroupInfo.setCoverLevel(recipeGroup.getRecipePilotRunInfo().getCoverLevel());
            experimentalRecipeGroupInfo.setCoverRecipe(recipeGroup.getRecipePilotRunInfo().getCoverRecipeFlag());
            experimentalRecipeGroupInfo.setFromEqpState(recipeGroup.getRecipePilotRunInfo().getFromEqpState());
            experimentalRecipeGroupInfo.setToEqpState(recipeGroup.getRecipePilotRunInfo().getToEqpState());
            experimentalRecipeGroupInfo.setClaimMemo(recipeGroup.getRecipePilotRunInfo().getClaimMemo());

            // set recipe data detail
            log.info("set recipe data detail");
            recipeGroup.getRecipeGroupInfo().getRecipeInfos().stream().parallel().forEach(re -> {
                Info.ExperimentalRecipeInfo experimentalRecipeInfo = new Info.ExperimentalRecipeInfo();
                experimentalRecipeInfo.setRecipeID(re);
                recipIDs.add(experimentalRecipeInfo);
            });

            experimentalGroupInfo.setRecipeGroupEventData(experimentalRecipeGroupInfo);
            experimentalGroupInfo.setRecipeIDs(recipIDs);
            experimentalGroupInfos.add(experimentalGroupInfo);
        });

        // if need lock

        // delete recipeGroup
        log.info("PilotMethod::deletePilotRunRecipeGroup");
        pilotRunMethod.deletePilotRunRecipeGroup(objCommon, params);

        // create history
        log.info("start save event data");
        experimentalGroupInfos.forEach(experimentalGroupInfo -> {
            recipeGroupEventMethod.experimentalRecipeGroupRegistEventMake(objCommon, txID, params.getClaimMemo(), experimentalGroupInfo);
        });


    }

    @Override
    public void sxCreateRecipeJob(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.CreatePilotJobInfoParams param) {
        String txID = TransactionIDEnum.CREATE_RECIPE_JOB.getValue();

        log.debug("step1 - check params");
        List<ObjectIdentifier> recipeGroupIDs = param.getRecipeGroupIDs();
        Validations.check(CimArrayUtils.isEmpty(recipeGroupIDs), "recipeGroupIDs cannot be empty");

        for (ObjectIdentifier recipeGroupID : recipeGroupIDs) {
            log.debug("step2 - create recipe job");
            CimRecipeGroupPilotRunJob recipeJob = pilotRunMethod.createRecipeJob(objCommon, recipeGroupID);

            log.debug("step3 - when only one job is created at a time, and the job is already created, then an error is thrown for that job that was already created");
            Validations.check(CimArrayUtils.getSize(param.getRecipeGroupIDs()) == 1 && recipeJob == null,retCodeConfigEx.getPilotJobExistError());

            log.debug("step4 - recipe group job was created successfully, creating the history of recipe group job");
            if (recipeJob != null) {
                com.fa.cim.fsm.Infos.PilotEventMakeInfo pilotEventMakeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeInfo();

                log.debug("set event data for job");
                pilotEventMakeInfo.setAction(BizConstant.SP_PILOT_RECIPE_JOB_CREATE);
                pilotEventMakeInfo.setRecipeGroupID(recipeGroupID);
                pilotEventMakeInfo.setLotID(recipeJob.getRecipePilotRunJobInfo().getLotID());
                pilotEventMakeInfo.setStatus(recipeJob.getStatus().name());
                pilotEventMakeInfo.setEqpID(recipeJob.getRecipePilotRunJobInfo().getEquipmentID());
                pilotEventMakeInfo.setPrType(recipeJob.getType().name());
                pilotEventMakeInfo.setPiLotWaferCount(recipeJob.getPilotWaferCount());
                pilotEventMakeInfo.setCoverLevel(recipeJob.getCoverLevel());
                pilotEventMakeInfo.setCoverRecipe(recipeJob.isCoverRecipe() ? 1 : 0);
                pilotEventMakeInfo.setFromEqpState(recipeJob.getFromEquipmentState());
                pilotEventMakeInfo.setToEqpState(recipeJob.getToEquipmentState());

                log.debug("set recipe event for job recipe");
                List<ObjectIdentifier> recipes = recipeJob.getRecipePilotRunJobInfo().getRecipes();
                List<com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo> piLotRecipeIDs = Optional.ofNullable(recipes).orElseGet(Collections::emptyList).stream().map(ele -> {
                    com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo pilotEventMakeRecipeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo();
                    pilotEventMakeRecipeInfo.setRecipeIDs(ele);
                    return pilotEventMakeRecipeInfo;
                }).collect(Collectors.toList());
                pilotEventMakeInfo.setPiLotRecipeIDs(piLotRecipeIDs);

                log.debug("step5 - job create event");
                pilotRecipeJobEventMethod.pilotEventMake(objCommon, txID, recipeJob.getClaimedMemo(), pilotEventMakeInfo);
            }
        }
    }

    @Override
    public void sxDeleteRecipeJob(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.DeletePilotJobInfoParams param) {
        String txID = TransactionIDEnum.DELETE_RECIPE_JOB.getValue();

        log.debug("step1 - check params");
        List<ObjectIdentifier> recipeJobIDs = param.getRecipeJobIDs();
        Validations.check(CimArrayUtils.isEmpty(recipeJobIDs), retCodeConfig.getNotFoundRecipeJob());
        ObjectIdentifier equipmentID = param.getEquipmentID();
        Validations.check(equipmentID == null, retCodeConfig.getNotFoundEqp());

        log.debug("step2 - query job from equipment");
        List<CimRecipeGroupPilotRunJob> recipePilotRunJobByEqp = pilotRunManager.findRecipePilotRunJobByEqp(param.getEquipmentID());
        List<CimRecipeGroupPilotRunJob> jobs = new ArrayList<>();
        for (ObjectIdentifier recipeJobID : recipeJobIDs) {
            log.debug("query job from jobId {}",recipeJobID);
            CimRecipeGroupPilotRunJob recipePilotRunJobNamed = pilotRunManager.findRecipePilotRunJobNamed(ObjectIdentifier.fetchValue(recipeJobID));

            log.debug("step3 - when recipe job has cover recipe is true and the equipment has a lower cover level than recipe job, it is added to the list to be removed");
            if (recipePilotRunJobNamed.isCoverRecipe() && CimArrayUtils.isNotEmpty(recipePilotRunJobByEqp)) {
                for (CimRecipeGroupPilotRunJob cimRecipePilotRunJob : recipePilotRunJobByEqp) {
                    if (recipePilotRunJobNamed.getCoverLevel() <= cimRecipePilotRunJob.getCoverLevel()) {
                        jobs.add(cimRecipePilotRunJob);
                    }
                }
            }
            jobs.add(recipePilotRunJobNamed);
        }

        log.debug("step4 - deduplicate the list of recipe job that need to be removed");
        List<CimRecipeGroupPilotRunJob> needDeleteJobs = jobs.stream().distinct().collect(Collectors.toList());

        log.debug("step5 - create history");
        if (CimArrayUtils.isNotEmpty(needDeleteJobs)) {
            for (CimRecipeGroupPilotRunJob job : needDeleteJobs) {
                log.debug("recipe group job was deleted successfully, creating the history of recipe group job : {}", job);
                com.fa.cim.fsm.Infos.PilotEventMakeInfo pilotEventMakeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeInfo();
                List<com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo> piLotRecipeIDs = new ArrayList<>();
                pilotEventMakeInfo.setPiLotRecipeIDs(piLotRecipeIDs);

                log.debug("set event param for job");
                pilotEventMakeInfo.setAction(BizConstant.SP_PILOT_RECIPE_JOB_DELETE);
                pilotEventMakeInfo.setRecipeGroupID(job.getRecipePilotRunJobInfo().getRecipeGroupID());
                pilotEventMakeInfo.setLotID(job.getRecipePilotRunJobInfo().getLotID());
                pilotEventMakeInfo.setStatus(RecipePilotRun.JobStatus.Close.name());
                pilotEventMakeInfo.setEqpID(job.getRecipePilotRunJobInfo().getEquipmentID());
                pilotEventMakeInfo.setPrType(job.getType().name());
                pilotEventMakeInfo.setPiLotWaferCount(job.getPilotWaferCount());
                pilotEventMakeInfo.setCoverLevel(job.getCoverLevel());
                pilotEventMakeInfo.setCoverRecipe(job.isCoverRecipe() ? 1 : 0);
                pilotEventMakeInfo.setFromEqpState(job.getFromEquipmentState());
                pilotEventMakeInfo.setToEqpState(job.getToEquipmentState());

                log.debug("set recipe event for job recipe");
                List<ObjectIdentifier> recipes = job.getRecipePilotRunJobInfo().getRecipes();
                if (CimArrayUtils.isNotEmpty(recipes)) {
                    for (ObjectIdentifier recipe : recipes) {
                        com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo pilotEventMakeRecipeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo();
                        piLotRecipeIDs.add(pilotEventMakeRecipeInfo);
                        pilotEventMakeRecipeInfo.setRecipeIDs(recipe);
                    }
                }
                String claimedMemo = job.getClaimedMemo();

                log.debug("step6 - add a lock to recipe job {}",job.getRecipePilotRunJobInfo().getPilotRunJobID());
                objectLockMethod.objectLock(objCommon,CimRecipeGroupPilotRunJob.class,job.getRecipePilotRunJobInfo().getPilotRunJobID());

                log.debug("step7 - close {} recipe group job", job.getPrimaryKey());
                job.remove();

                log.debug("step8 - job create event");
                pilotRecipeJobEventMethod.pilotEventMake(objCommon, txID, claimedMemo, pilotEventMakeInfo);
            }
        }
    }

    @Override
    public void sxRecipeJobBindLot(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.RecipeJobBindLotParams param) {
        String txID = TransactionIDEnum.RECIPE_JOB_BIND_LOT.getValue();

        log.debug("step1 - check params");
        Validations.check(param.getRecipeJobID() == null, retCodeConfigEx.getPrNotFoundRecipeGroup());
        Validations.check(param.getLotID() == null, retCodeConfig.getNotFoundLot());

        log.debug("step2 - add a lock to recipe job {}",param.getRecipeJobID());
        objectLockMethod.objectLock(objCommon,CimRecipeGroupPilotRunJob.class,param.getRecipeJobID());

        log.debug("step3 - job ind lot");
        pilotRunMethod.recipeJobBindLot(objCommon, param);

        log.debug("step4 - get recipe job information");
        CimRecipeGroupPilotRunJob jobBO = baseCoreFactory.getBO(CimRecipeGroupPilotRunJob.class, param.getRecipeJobID());

        log.debug("step5 - create history recipe job");
        com.fa.cim.fsm.Infos.PilotEventMakeInfo pilotEventMakeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeInfo();

        log.info("set event data for job");
        pilotEventMakeInfo.setAction(BizConstant.SP_PILOT_RECIPE_JOB_BIND_LOT);
        pilotEventMakeInfo.setRecipeGroupID(jobBO.getRecipePilotRunJobInfo().getRecipeGroupID());
        pilotEventMakeInfo.setLotID(jobBO.getRecipePilotRunJobInfo().getLotID());
        pilotEventMakeInfo.setStatus(jobBO.getStatus().name());
        pilotEventMakeInfo.setEqpID(jobBO.getRecipePilotRunJobInfo().getEquipmentID());
        pilotEventMakeInfo.setPrType(jobBO.getType().name());
        pilotEventMakeInfo.setPiLotWaferCount(jobBO.getPilotWaferCount());
        pilotEventMakeInfo.setCoverLevel(jobBO.getCoverLevel());
        pilotEventMakeInfo.setCoverRecipe(jobBO.isCoverRecipe() ? 1 : 0);
        pilotEventMakeInfo.setFromEqpState(jobBO.getFromEquipmentState());
        pilotEventMakeInfo.setToEqpState(jobBO.getToEquipmentState());

        log.info("set recipe event for job recipe");
        List<ObjectIdentifier> recipes = jobBO.getRecipePilotRunJobInfo().getRecipes();
        List<com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo> piLotRecipeIDs = Optional.ofNullable(recipes).orElseGet(Collections::emptyList).stream().map(ele -> {
            com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo pilotEventMakeRecipeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo();
            pilotEventMakeRecipeInfo.setRecipeIDs(ele);
            return pilotEventMakeRecipeInfo;
        }).collect(Collectors.toList());
        pilotEventMakeInfo.setPiLotRecipeIDs(piLotRecipeIDs);

        log.info("step6 - job create event");
        pilotRecipeJobEventMethod.pilotEventMake(objCommon, txID, jobBO.getClaimedMemo(), pilotEventMakeInfo);
    }

    @Override
    public void sxCreateRecipeJobByEqpStatusCut(Infos.ObjCommon objCommon, Params.EqpStatusChangeReqParams eqpStatusChangeReqParams, String curEqpState) {
        log.info("equipment {} initial state is {},switch state to {}"
                , eqpStatusChangeReqParams.getEquipmentID().getValue()
                , curEqpState
                , eqpStatusChangeReqParams.getEquipmentStatusCode().getValue());
        // make txID
        String txID = TransactionIDEnum.EQP_STATUS_CHANGE_REQ.getValue();

        //check params
        log.info("check equipment can't be without");
        Validations.check(ObjectIdentifier.isEmpty(eqpStatusChangeReqParams.getEquipmentID()) || ObjectIdentifier.isEmpty(eqpStatusChangeReqParams.getEquipmentStatusCode()), "equipmentID and equipmentStatusCode can not be empty !");
        CimMachine machineBO = baseCoreFactory.getBO(CimMachine.class, eqpStatusChangeReqParams.getEquipmentID());
        CimStorageMachine cimStorageMachineBO = baseCoreFactory.getBO(CimStorageMachine.class, eqpStatusChangeReqParams.getEquipmentID());
        Validations.check(machineBO == null && cimStorageMachineBO == null, retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(eqpStatusChangeReqParams.getEquipmentID()));

        log.info("query recipe group from equipment");
        List<CimRecipeGroup> recipeGroups = pilotRunMethod.getRecipeGroupByEqp(eqpStatusChangeReqParams.getEquipmentID());
        if (CimArrayUtils.isNotEmpty(recipeGroups)) {

            //get the state of the eqp switch
            CimMachineState aMachineState = null;
            List<CimE10State> anE10StateSeq = codeManager.allE10States();
            int nLen = CimArrayUtils.getSize(anE10StateSeq);
            for (int i = 0; i < nLen; i++) {
                aMachineState = anE10StateSeq.get(i).findMachineStateNamed(eqpStatusChangeReqParams.getEquipmentStatusCode().getValue());
                if (aMachineState != null) {
                    log.info("select e10 [{}] from status [{}]", aMachineState, eqpStatusChangeReqParams.getEquipmentStatusCode().getValue());
                    break;
                }
            }
            Validations.check(aMachineState == null, retCodeConfig.getNotFoundEqpState());
            CimE10State e10State = aMachineState.getE10State();
            String e10StateName = e10State.getIdentifier();
            String eqpStatusName = new StringJoiner(".").add(e10StateName).add(ObjectIdentifier.fetchValue(eqpStatusChangeReqParams.getEquipmentStatusCode())).toString();

            for (CimRecipeGroup recipeGroup : recipeGroups) {
                //check if the device state meets the creation requirements
                if (recipeGroup.getRecipePilotRunInfo().getToEqpState().equals(eqpStatusName)
                        && ("ALL".equals(recipeGroup.getRecipePilotRunInfo().getFromEqpState())
                        || recipeGroup.getRecipePilotRunInfo().getFromEqpState().equals(curEqpState))) {
                    log.info("create recipe group job");
                    CimRecipeGroupPilotRunJob recipeJob = pilotRunMethod.createRecipeJob(objCommon, recipeGroup.getRecipeGroupInfo().getRecipeGroupID());

                    if (recipeJob != null) {
                        //create history recipe job
                        com.fa.cim.fsm.Infos.PilotEventMakeInfo pilotEventMakeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeInfo();
                        List<com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo> piLotRecipeIDs = new ArrayList<>();
                        pilotEventMakeInfo.setPiLotRecipeIDs(piLotRecipeIDs);

                        log.info("set event data for job");
                        pilotEventMakeInfo.setAction(BizConstant.SP_PILOT_RECIPE_JOB_CREATE);
                        pilotEventMakeInfo.setRecipeGroupID(recipeGroup.getRecipeGroupInfo().getRecipeGroupID());
                        pilotEventMakeInfo.setLotID(recipeJob.getRecipePilotRunJobInfo().getLotID());
                        pilotEventMakeInfo.setStatus(recipeJob.getStatus().name());
                        pilotEventMakeInfo.setEqpID(recipeJob.getRecipePilotRunJobInfo().getEquipmentID());
                        pilotEventMakeInfo.setPrType(recipeJob.getType().name());
                        pilotEventMakeInfo.setPiLotWaferCount(recipeJob.getPilotWaferCount());
                        pilotEventMakeInfo.setCoverLevel(recipeJob.getCoverLevel());
                        pilotEventMakeInfo.setCoverRecipe(recipeJob.isCoverRecipe() ? 1 : 0);
                        pilotEventMakeInfo.setFromEqpState(recipeJob.getFromEquipmentState());
                        pilotEventMakeInfo.setToEqpState(recipeJob.getToEquipmentState());

                        //create history recipes
                        log.info("set recipe event for job recipe");
                        List<ObjectIdentifier> recipes = recipeJob.getRecipePilotRunJobInfo().getRecipes();
                        if (CimArrayUtils.isNotEmpty(recipes)) {
                            for (ObjectIdentifier recipe : recipes) {
                                com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo pilotEventMakeRecipeInfo = new com.fa.cim.fsm.Infos.PilotEventMakeRecipeInfo();
                                piLotRecipeIDs.add(pilotEventMakeRecipeInfo);
                                pilotEventMakeRecipeInfo.setRecipeIDs(recipe);
                            }
                        }

                        log.info("job create event");
                        pilotRecipeJobEventMethod.pilotEventMake(objCommon, txID, recipeJob.getClaimedMemo(), pilotEventMakeInfo);
                    }
                }
            }
        }
    }
}
