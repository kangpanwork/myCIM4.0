package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeEqpDO;
import com.fa.cim.entity.runtime.pr.CimPilotRunJobDO;
import com.fa.cim.entity.runtime.pr.CimPilotRunPlanDO;
import com.fa.cim.entity.runtime.rcpprjob.CimRecipePilotRunJobDO;
import com.fa.cim.entity.runtime.recipegroup.CimRecipeGroupPilotRunDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IPilotRunMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.pr.CimPilotRunJob;
import com.fa.cim.newcore.bo.pr.CimPilotRunPlan;
import com.fa.cim.newcore.bo.pr.CimRecipeGroupPilotRunJob;
import com.fa.cim.newcore.bo.pr.PilotRunManager;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.bo.recipe.CimRecipeGroup;
import com.fa.cim.newcore.bo.recipe.RecipeManager;
import com.fa.cim.newcore.dto.pfx.SkipBlockInfo;
import com.fa.cim.newcore.dto.pos.PilotRunSectionInfo;
import com.fa.cim.newcore.dto.pr.PilotRunInfo;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import com.fa.cim.newcore.dto.rcppr.RecipePilotRun;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.pr.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
 * @date: 2020/12/19 13:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmMethod
@Slf4j
public class PilotRunMethod implements IPilotRunMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private PilotRunManager pilotRunManager;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private RecipeManager recipeManager;

    @Autowired
    private GenericCoreFactory genericCoreFactory;

    @Autowired
    private ObjectLockMethod objectLockMethod;

    @Override
    public List<PilotRunInfo.Plan> getPilotRunList(Infos.ObjCommon objCommon, Params.PilotRunInqParams params) {
        String pilotRunPlanSQL = "SELECT ID,PRUN_ID FROM OMPR ";
        boolean bFirstCondition = true;
        if (ObjectIdentifier.isNotEmpty(params.getCarrierID())) {
            pilotRunPlanSQL += " WHERE ";
            pilotRunPlanSQL += String.format(" CARRIER_ID = '%s' ", ObjectIdentifier.fetchValue(params.getCarrierID()));
            bFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(params.getLotID())) {
            if (bFirstCondition) {
                pilotRunPlanSQL += " WHERE ";
            } else pilotRunPlanSQL += "AND";
            pilotRunPlanSQL += String.format(" LOT_ID = '%s' ", ObjectIdentifier.fetchValue(params.getLotID()));
            bFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(params.getEquipmentID())) {
            if (bFirstCondition) {
                pilotRunPlanSQL += " WHERE ";
            } else pilotRunPlanSQL += "AND";
            pilotRunPlanSQL += String.format(" TAG_EQP_ID = '%s' ", ObjectIdentifier.fetchValue(params.getEquipmentID()));
            bFirstCondition = false;
        }
        if (ObjectIdentifier.isNotEmpty(params.getPilotRunPlanID())) {
            if (bFirstCondition) {
                pilotRunPlanSQL += " WHERE ";
            } else pilotRunPlanSQL += "AND";
            pilotRunPlanSQL += String.format(" PRUN_ID = '%s' ", ObjectIdentifier.fetchValue(params.getPilotRunPlanID()));
        }
        if (CimStringUtils.isNotEmpty(params.getPilotRunStatus())) {
            if (bFirstCondition) {
                pilotRunPlanSQL += " WHERE ";
            } else pilotRunPlanSQL += "AND";
            pilotRunPlanSQL += String.format(" STATUS = '%s' ", params.getPilotRunStatus());
        }
        //get plan
        List<PilotRunInfo.Plan> plans = new ArrayList<>();
        List<Object[]> pilotRunPlanDOList = cimJpaRepository.query(pilotRunPlanSQL);
        if (CimArrayUtils.isNotEmpty(pilotRunPlanDOList)) {
            for (Object[] objects : pilotRunPlanDOList) {
                CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, ObjectIdentifier.build(objects[1].toString(), objects[0].toString()));
                PilotRunInfo.Plan pilotRunPlanInfo = pilotRunPlanBO.getPilotRunPlanInfo();
                List<PilotRunInfo.Job> pilotRunJobs = pilotRunPlanInfo.getPilotRunJobs();
                if (CimArrayUtils.isNotEmpty(pilotRunJobs)) {
                    for (PilotRunInfo.Job pilotRunJob : pilotRunJobs) {
                        List<Infos.LotWaferMap> lotWaferMaps = lotMethod.lotWaferMapGet(objCommon, pilotRunJob.getPlannedLotID());

                        if (CimArrayUtils.isNotEmpty(lotWaferMaps)) {
                            List<ObjectIdentifier> wafers = new ArrayList<>();
                            for (Infos.LotWaferMap lotWaferMap : lotWaferMaps) {
                                wafers.add(lotWaferMap.getWaferID());
                            }
                            pilotRunJob.setWafers(wafers);
                        }
                    }
                }
                plans.add(pilotRunPlanInfo);
            }
        }
        return plans;
    }

    @Override
    public void createPilotRunPlan(Infos.ObjCommon objCommon, Params.CreatePilotRunParams params) {
        CimPilotRunPlan pilotRunplan = pilotRunManager.createPilotRunPlanByLotName(ObjectIdentifier.fetchValue(params.getLotID()));

        PilotRunInfo.Category category = null;
        try {
            category = PilotRunInfo.Category.valueOf(params.getCategory());
        } catch (IllegalArgumentException e) {
            Validations.check(retCodeConfigEx.getInputParameterCategorError(), params.getCategory());
        }
        pilotRunplan.setCategory(category);
        CimMachine machine = baseCoreFactory.getBO(CimMachine.class, params.getTagEquipment());
        Validations.check(null == machine, retCodeConfig.getNotFoundMachine());
        pilotRunplan.setTaggedEquipment(machine);
        CimCassette cassetteBO = baseCoreFactory.getBO(CimCassette.class, params.getCarrierID());
        Validations.check(null == cassetteBO, retCodeConfig.getNotFoundCassette());
        pilotRunplan.setCarrier(cassetteBO);
        pilotRunplan.makeWaiting();
        pilotRunplan.setMaxPilotRunCount(params.getPilotCount());
        CimLot lotBO = baseCoreFactory.getBO(CimLot.class, params.getLotID());
        Validations.check(null == lotBO, retCodeConfig.getNotFoundLot());
        pilotRunplan.setPlannedPilotLot(lotBO);
        CimPerson personBO = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == personBO, retCodeConfig.getNotFoundPerson());
        pilotRunplan.setCreatedUser(personBO);
        pilotRunplan.setCreatedTimestamp(objCommon.getTimeStamp().getReportTimeStamp());
        pilotRunplan.setAction(PilotRunInfo.Action.valueOf(params.getAction()));
    }

    @Override
    public void deletePilotRunPlan(Infos.ObjCommon objCommon, ObjectIdentifier pilotRunID) {
        //delete SkipBlock
        CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, pilotRunID);
        Validations.check(null == pilotRunPlanBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
        PilotRunInfo.Plan pilotRunPlanInfo = pilotRunPlanBO.getPilotRunPlanInfo();
        if (null != pilotRunPlanInfo && CimArrayUtils.isNotEmpty(pilotRunPlanInfo.getPilotRunJobs())) {
            //job
            for (PilotRunInfo.Job pilotRunJob : pilotRunPlanInfo.getPilotRunJobs()) {
                CimLot lot = baseCoreFactory.getBO(CimLot.class, pilotRunJob.getPlannedLotID());
                Validations.check(null == lot, retCodeConfig.getNotFoundLot());

                CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
                Validations.check(null == processFlowContext, retCodeConfig.getNotFoundPfx());
                processFlowContext.removeAllSkipBlocks();
            }
            // plan
            CimLot lot = baseCoreFactory.getBO(CimLot.class, pilotRunPlanInfo.getPlannedLotID());
            Validations.check(null == lot, retCodeConfig.getNotFoundLot());

            CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
            Validations.check(null == processFlowContext, retCodeConfig.getNotFoundPfx());
            processFlowContext.removeAllSkipBlocks();
        }

        // delete pr
        pilotRunManager.removePilotRunPlanByName(ObjectIdentifier.fetchValue(pilotRunID));
    }


    @Override
    public void changePilotRunstatus(Infos.ObjCommon objCommon, Params.PilotRunStatusChangeParams params) {
        //check entity
        PilotRunInfo.EntityType entityType = null;
        try {
            entityType = PilotRunInfo.EntityType.valueOf(params.getEntityType());
        } catch (IllegalArgumentException e) {
            Validations.check(retCodeConfigEx.getInputParameterEntityTypeError(), params.getEntityType());
        }

        //check status
        PilotRunInfo.Status status = null;
        try {
            status = PilotRunInfo.Status.valueOf(params.getStatus());
        } catch (IllegalArgumentException e) {
            Validations.check(retCodeConfigEx.getInputParameterStatusError(), params.getStatus());
        }
        //status change
        if (entityType != null && CimStringUtils.equals(entityType.getValue(), PilotRunInfo.EntityType.PiLotRunPlan.getValue())) {
            CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, params.getEntity());
            Validations.check(null == pilotRunPlanBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
            CimPilotRunJob activeJob = pilotRunPlanBO.getActiveJob();
            if (null != status) {
                switch (status) {
                    case Waiting:
                        pilotRunPlanBO.makeWaiting();
                        break;
                    case Ongoing:
                        pilotRunPlanBO.makeOngoing();
                        break;
                    case Aborted:
                        Validations.check(CimStringUtils.equals(pilotRunPlanBO.getStatus().getValue(), PilotRunInfo.Status.Waiting.getValue())
                                , retCodeConfigEx.getPrPlanStatusNotAllowedToChange(), PilotRunInfo.Status.Waiting.getValue(), PilotRunInfo.Status.Aborted.getValue());
                        // retCodeConfig.getError(), "plan status is Waiting not change Aborted"
                        pilotRunPlanBO.makeFailed();
                        if (null != activeJob) {
                            activeJob.makeFailed();
                        }
                        break;
                    case Completed:
                        // "plan status is Waiting not change Completed"
                        Validations.check(PilotRunInfo.Status.Waiting.equals(pilotRunPlanBO.getStatus().getValue()), retCodeConfigEx.getPrPlanStatusNotAllowedToChange(), PilotRunInfo.Status.Waiting.getValue(), PilotRunInfo.Status.Completed.getValue());
                        // "plan status is Failed not change Completed"
                        Validations.check(PilotRunInfo.Status.Failed.equals(pilotRunPlanBO.getStatus().getValue()), retCodeConfigEx.getPrPlanStatusNotAllowedToChange(), PilotRunInfo.Status.Failed.getValue(), PilotRunInfo.Status.Completed.getValue());
                        // bug-6765
                        String operationNumber = pilotRunPlanBO.getPlannedPilotLot().getOperationNumber();
                        Map<String, String> stepLabMap = pilotRunPlanBO.getPilotRunSection().stream().filter(sectionSpec -> sectionSpec.getStepLabel().getValue().equals("E")).collect(Collectors.toMap(o -> o.getStepLabel().getValue(), o -> o.getStepNumber()));
                        String stepLab = stepLabMap.get("E");// “E” ,Check the completed work step
                        // the current step cannot be changed to Completed
                        Validations.check(stepLab == null || !operationNumber.equals(stepLab), retCodeConfigEx.getPrCurrentStepNotAllowedToChange(), stepLab);
                        pilotRunPlanBO.makeCompleted();
                        if (null != activeJob) {
                            activeJob.makeClosed();
                        }
                        break;
                }
            }

        } else if (entityType != null && CimStringUtils.equals(entityType.getValue(), PilotRunInfo.EntityType.PiLotRunJob.getValue())) {
            CimPilotRunJob pilotRunJobBO = baseCoreFactory.getBO(CimPilotRunJob.class, params.getEntity());
            Validations.check(null == pilotRunJobBO, retCodeConfigEx.getNotFoundPiLotRunJob());
            if (null != status) {
                switch (status) {
                    case Created:
                        pilotRunJobBO.makeCreate();
                        break;
                    case Ongoing:
                        pilotRunJobBO.makeOngoing();
                        break;
                    case Failed:
                        pilotRunJobBO.makeFailed();
                        break;
                    case Pass:
                        pilotRunJobBO.makePass();
                        break;
                    case Closed:
                        pilotRunJobBO.makeClosed();
                        break;
                }
            }
        }
    }

    @Override
    public PilotRunInfo.JobInfo getPilotJobInfo(Infos.ObjCommon objCommon, ObjectIdentifier pilotRunPlanID) {
        CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, pilotRunPlanID);
        CimLot plannedPilotLot = pilotRunPlanBO.getPlannedPilotLot();
        Validations.check(null == plannedPilotLot, retCodeConfig.getNotFoundLot(), plannedPilotLot.getIdentifier());
        // set common
        PilotRunInfo.JobInfo jobInfo = new PilotRunInfo.JobInfo();
        jobInfo.setLotID(plannedPilotLot.getLotID());
        //get carrierID
        ObjectIdentifier carrierID = lotMethod.lotCassetteGet(objCommon, plannedPilotLot.getLotID());
        Validations.check(ObjectIdentifier.isEmpty(carrierID), retCodeConfig.getNotFoundCassette());
        jobInfo.setCarrierID(carrierID);

        CimProcessOperation processOperation = plannedPilotLot.getProcessOperation();
        Validations.check(null == processOperation, retCodeConfig.getNotFoundProcessOperation());
        CimProcessDefinition mainProcessDefinition = processOperation.getMainProcessDefinition();
        CimProcessFlow mainProcessFlow = processOperation.getMainProcessFlow();
        jobInfo.setProcessFlowID(ObjectIdentifier.build(mainProcessFlow.getRootProcessDefinition().getIdentifier(), mainProcessFlow.getRootProcessDefinition().getPrimaryKey()));

        jobInfo.setStepNO(processOperation.getOperationNumber());
        jobInfo.setStepID(ObjectIdentifier.build(processOperation.getProcessDefinition().getIdentifier(), processOperation.getProcessDefinition().getPrimaryKey()));

        //set force forceMeasurement
        String pilotRunValue = null;
        PilotRunSectionInfo.SelectionRule waferSelectionRule = null;
        List<PilotRunInfo.SectionSpec> pilotRunSection = pilotRunPlanBO.getPilotRunSection();
        if (CimArrayUtils.isEmpty(pilotRunSection)) {
            CimProcessOperationSpecification processOperationSpecification = processOperation.getProcessOperationSpecification();
            List<PilotRunInfo.SectionSpec> sectionSpecs = null;
            if (null != processOperationSpecification && processOperationSpecification.isPilotRunRequired()) {
                List<PilotRunSectionInfo> pilotRunSectionInfo = processOperationSpecification.getPilotRunSectionInfo();
                if (CimArrayUtils.isNotEmpty(pilotRunSectionInfo)) {
                    sectionSpecs = this.getSectionSpecInfo(objCommon, pilotRunSectionInfo, ObjectIdentifier.build(mainProcessDefinition.getIdentifier(), mainProcessDefinition.getPrimaryKey()));
                    if (CimArrayUtils.isNotEmpty(sectionSpecs)) {
                        for (PilotRunInfo.SectionSpec sectionSpec : sectionSpecs) {
                            for (PilotRunSectionInfo runSectionInfo : pilotRunSectionInfo) {
                                if (CimStringUtils.equals(runSectionInfo.getStepLabel().getValue(), PilotRunInfo.Label.S.getValue())) {
                                    pilotRunValue = runSectionInfo.getWaferSelectionValue();
                                    waferSelectionRule = runSectionInfo.getWaferSelectionRule();
                                }
                                if (CimStringUtils.equals(runSectionInfo.getStepNumber(), sectionSpec.getStepNumber())) {
                                    sectionSpec.setStepLabel(runSectionInfo.getStepLabel());
                                }
                            }
                        }
                    }
                }
            }
            if (CimArrayUtils.isNotEmpty(sectionSpecs)) {
                sectionSpecs.sort(Comparator.comparingDouble(x -> Double.parseDouble(x.getStepNumber())));
            }
            jobInfo.setPilotRunSection(sectionSpecs);
        } else {
            pilotRunSection.sort(Comparator.comparingDouble(x -> Double.parseDouble(x.getStepNumber())));
            jobInfo.setPilotRunSection(pilotRunSection);
        }

        //set wafers
        jobInfo.setWaferInfos(this.getPilotRunWaferInfo(objCommon, ObjectIdentifier.build(plannedPilotLot.getIdentifier(), plannedPilotLot.getPrimaryKey()), pilotRunValue, waferSelectionRule));
        return jobInfo;
    }

    @Override
    public List<PilotRunInfo.waferInfo> getPilotRunWaferInfo(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String PilotRunValue, PilotRunSectionInfo.SelectionRule waferSelectionRule) {
        List<Infos.LotWaferAttributes> lotWaferAttributes = lotMethod.lotMaterialsGetWafers(objCommon, lotID);
        List<PilotRunInfo.waferInfo> waferInfos = new ArrayList<>();
        if (null == PilotRunValue || null == waferSelectionRule) {
            if (CimArrayUtils.isNotEmpty(lotWaferAttributes)) {
                for (Infos.LotWaferAttributes lotWaferAttribute : lotWaferAttributes) {
                    PilotRunInfo.waferInfo waferInfo = new PilotRunInfo.waferInfo();
                    waferInfo.setWaferID(lotWaferAttribute.getWaferID());
                    waferInfo.setSlotNO(lotWaferAttribute.getSlotNumber());
                    waferInfos.add(waferInfo);
                }
            }
            // return defule
            return waferInfos;
        }

        // 按照需求return
        if (CimArrayUtils.isNotEmpty(lotWaferAttributes)) {
            int lotWaferSize = lotWaferAttributes.size();
            int pilotRunCount = CimNumberUtils.intValue(PilotRunValue);
            AtomicInteger selectCount = new AtomicInteger(0);
            switch (waferSelectionRule) {
                case Top: {
                    waferInfos = lotWaferAttributes.stream().sorted(Comparator.comparing(Infos.LotWaferAttributes::getSlotNumber)).map(x -> {
                        PilotRunInfo.waferInfo waferInfo = new PilotRunInfo.waferInfo(x.getWaferID(), x.getSlotNumber(), false);
                        int sCount = selectCount.incrementAndGet();
                        if (sCount <= pilotRunCount) {
                            waferInfo.setStartFlag(true);
                        }
                        return waferInfo;
                    }).collect(Collectors.toList());
                }
                break;
                case Bottom: {
                    waferInfos = lotWaferAttributes.stream().sorted(Comparator.comparing(Infos.LotWaferAttributes::getSlotNumber)).map(x -> {
                        PilotRunInfo.waferInfo waferInfo = new PilotRunInfo.waferInfo(x.getWaferID(), x.getSlotNumber(), false);
                        int sCount = selectCount.incrementAndGet();
                        if (lotWaferSize - sCount < pilotRunCount) {
                            waferInfo.setStartFlag(true);
                        }
                        return waferInfo;
                    }).collect(Collectors.toList());
                }
                break;
                case Random: {
                    //生成数组长度为[pilotRunCount]且value不重复的随机数组
                    Set<Integer> randoms = new HashSet<>(); //过滤重复value
                    int randomSize = 0;
                    while (randomSize < pilotRunCount) {
                        randoms.add(new Random().nextInt(lotWaferSize - 1)); //random 0到lotWaferSize, 不包含lotWaferSize
                        randomSize++;
                    }

                    waferInfos = lotWaferAttributes.stream().sorted(Comparator.comparing(Infos.LotWaferAttributes::getSlotNumber)).map(x -> {
                        PilotRunInfo.waferInfo waferInfo = new PilotRunInfo.waferInfo(x.getWaferID(), x.getSlotNumber(), false);
                        int sCount = selectCount.incrementAndGet();
                        for (Integer index : randoms) {
                            if (sCount - 1 == index) { //当前遍历对象角标与随机选中角标一致，则默认start flag = true.
                                waferInfo.setStartFlag(true);
                                break;
                            }
                        }
                        return waferInfo;
                    }).collect(Collectors.toList());
                }
                break;
                case Slot:
                    String[] tokens = PilotRunValue.split(",");
                    int tokensSize = CimObjectUtils.isEmpty(tokens) ? 0 : tokens.length;

                    for (Infos.LotWaferAttributes lotWaferAttribute : lotWaferAttributes) {
                        if (ObjectIdentifier.isEmpty(lotWaferAttribute.getWaferID())) {
                            continue;
                        }
                        PilotRunInfo.waferInfo waferInfo = new PilotRunInfo.waferInfo();
                        int i = 0;
                        while (i++ < tokensSize) {
                            Integer attributeNumber = Integer.valueOf(tokens[i - 1]);
                            waferInfo.setWaferID(lotWaferAttributes.get(attributeNumber).getWaferID());
                            waferInfo.setSlotNO(lotWaferAttributes.get(attributeNumber).getSlotNumber());
                            waferInfo.setStartFlag(true);
                            waferInfos.add(waferInfo);
                            break;
                        }
                        waferInfo.setWaferID(lotWaferAttribute.getWaferID());
                        waferInfo.setSlotNO(lotWaferAttribute.getSlotNumber());
                        waferInfo.setStartFlag(false);
                        waferInfos.add(waferInfo);
                    }
                    break;
            }
        }

        return waferInfos;
    }

    @Override
    public void createPilotRunJob(Infos.ObjCommon objCommon, ObjectIdentifier cildLotID, ObjectIdentifier pilotRunPlanID) {
        CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, pilotRunPlanID);
        Validations.check(null == pilotRunPlanBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
        CimLot cildLotBO = baseCoreFactory.getBO(CimLot.class, cildLotID);
        CimPilotRunJob pilotRunJob = pilotRunPlanBO.createPilotRunJob(cildLotBO);
        PilotRunInfo.Job job = new PilotRunInfo.Job();
        job.setStatus(PilotRunInfo.Status.Created);
        job.setPlannedLotID(cildLotID);
        job.setPilotRunPlanID(pilotRunPlanID);
        job.setCreateTimestamp(objCommon.getTimeStamp().getReportTimeStamp());
        job.setCreateUserID(objCommon.getUser().getUserID());
        pilotRunJob.setPilotRunJobInfo(job);
        pilotRunPlanBO.setActiveJob(pilotRunJob);
        pilotRunPlanBO.setPilotRunCount(pilotRunPlanBO.getPilotRunCount() + 1);
        pilotRunPlanBO.makeOngoing();
    }

    @Override
    public void setForceMeasurementInfos(Infos.ObjCommon objCommon, ObjectIdentifier piLotrunPlanID, List<Infos.ForceMeasurementInfo> forceMeasurementInfos) {
        CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, piLotrunPlanID);
        Validations.check(null == pilotRunPlanBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
        List<PilotRunInfo.SectionSpec> pilotRunSection = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(forceMeasurementInfos)) {
            for (Infos.ForceMeasurementInfo forceMeasurementInfo : forceMeasurementInfos) {
                PilotRunInfo.SectionSpec sectionSpec = new PilotRunInfo.SectionSpec();
                sectionSpec.setStepID(forceMeasurementInfo.getStepID());
                PilotRunInfo.Label label = PilotRunInfo.Label.valueOf(forceMeasurementInfo.getStepLabel());
                sectionSpec.setStepLabel(label);
                sectionSpec.setForceMeasurementFlag(forceMeasurementInfo.getForceMeasurementFlag());
                sectionSpec.setStepNumber(forceMeasurementInfo.getStepNo());
                pilotRunSection.add(sectionSpec);
            }
        }
        pilotRunPlanBO.setPilotRunSection(pilotRunSection);
    }

    @Override
    public void setSkipBlockInfo(Infos.ObjCommon objCommon, ObjectIdentifier pilotRunPlanID, List<Infos.ForceMeasurementInfo> forceMeasurementInfos) {
        CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, pilotRunPlanID);
        Validations.check(null == pilotRunPlanBO, retCodeConfigEx.getNotFoundPiLotRunPlan());

        PilotRunInfo.Job pilotRunJobInfo = pilotRunPlanBO.getActiveJob().getPilotRunJobInfo();
        CimLot cildLotDO = baseCoreFactory.getBO(CimLot.class, pilotRunJobInfo.getPlannedLotID());
        Validations.check(null == cildLotDO, retCodeConfig.getNotFoundLot());
        CimProcessFlowContext processFlowContext = cildLotDO.getProcessFlowContext();
        Validations.check(null == processFlowContext, retCodeConfig.getNotFoundPfx());

        for (Infos.ForceMeasurementInfo forceMeasurementInfo : forceMeasurementInfos) {
            if (CimBooleanUtils.isTrue(forceMeasurementInfo.getForceMeasurementFlag())) {
                SkipBlockInfo skipBlockInfo = new SkipBlockInfo();
                skipBlockInfo.setType(SkipBlockInfo.Type.Forward);
                skipBlockInfo.setAction(SkipBlockInfo.Action.PR_ForceMeasurement);
                skipBlockInfo.setFlowObj(processFlowContext.getMainProcessFlow().getPrimaryKey());
                skipBlockInfo.setRouteNumber(BaseStaticMethod.convertOpeNoToModuleNo(forceMeasurementInfo.getStepNo()));
                skipBlockInfo.setStepNumber(BaseStaticMethod.convertOpeNoToModuleOpeNo(forceMeasurementInfo.getStepNo()));
                processFlowContext.addSkipBlock(skipBlockInfo);
            }
        }
    }

    @Override
    public Infos.CheckPilotRunForEquipmentInfo checkPilotRunForEquipment(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassette) {
        Infos.CheckPilotRunForEquipmentInfo out = new Infos.CheckPilotRunForEquipmentInfo();

        Params.PilotRunInqParams params = new Params.PilotRunInqParams();
        params.setEquipmentID(equipmentID);
        List<PilotRunInfo.Plan> pilotRunList = this.getPilotRunList(objCommon, params);

        //check pirun list
        if (CimArrayUtils.isEmpty(pilotRunList)) return out;

        //get start lot and machineRecipeID
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        ObjectIdentifier machineRecipeID = null;
        if (CimArrayUtils.isNotEmpty(startCassette)) {
            for (Infos.StartCassette cassette : startCassette) {
                List<Infos.LotInCassette> lotInCassetteList = cassette.getLotInCassetteList();
                if (CimArrayUtils.isNotEmpty(lotInCassetteList)) {
                    for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                        if (lotInCassette.getMoveInFlag()) {
                            lotIDs.add(lotInCassette.getLotID());
                            machineRecipeID = lotInCassette.getStartRecipe().getMachineRecipeID();
                        }
                    }
                }
            }
        }
        for (PilotRunInfo.Plan plan : pilotRunList) {
            if (plan.getStatus().equals(PilotRunInfo.Status.Completed)) continue;
            if (plan.getStatus().equals(PilotRunInfo.Status.Failed)) continue;
            Validations.check(plan.getStatus().equals(PilotRunInfo.Status.Waiting), retCodeConfigEx.getPrPilotRunJobNotBeCreated());
            if (plan.getStatus().equals(PilotRunInfo.Status.Ongoing)) {
                if (plan.getCategory().equals(PilotRunInfo.Category.Adhoc)) {
                    PilotRunInfo.Job activeJob = plan.getActiveJob();
                    // "pls create pirunjob"
                    Validations.check(null == activeJob, retCodeConfigEx.getPrPilotRunJobNotBeCreated());
                    //Validations.check(PilotRunInfo.Status.Ongoing.equals(activeJob.getStatus().getValue()), retCodeConfig.getError(), "pilot job status is Ongoing");
                    // pls repilot run
                    Validations.check(PilotRunInfo.Status.Failed.equals(activeJob.getStatus().getValue()), retCodeConfigEx.getPrRepilotRun());
                    for (ObjectIdentifier lotID : lotIDs) {
                        if (PilotRunInfo.Status.Pass.equals(activeJob.getStatus().getValue())) {
                            //判断当前加工的母lot 是不是 plan 的lot
                            if (ObjectIdentifier.equalsWithValue(plan.getPlannedLotID(), lotID)) {
                                return out;
                            } else {
                                // Validations.check(retCodeConfig.getError(), "pls select the : " + ObjectUtils.getObjectValue(plan.getPlannedLotID()) + " pilot");
                                Validations.check(retCodeConfigEx.getPrPlsSelectPilot(), ObjectIdentifier.fetchValue(plan.getPlannedLotID()));
                            }
                        }
                        if (PilotRunInfo.Status.Created.equals(activeJob.getStatus().getValue())
                                || PilotRunInfo.Status.Ongoing.equals(activeJob.getStatus().getValue())) {
                            //判断当前加工的lot 是不是 job 的lot
                            if (ObjectIdentifier.equalsWithValue(activeJob.getPlannedLotID(), lotID)) {
                                out.setPilotRunJobID(activeJob.getPilotRunJobID());
                                return out;
                            } else {
                                Validations.check(retCodeConfigEx.getPrPlsSelectPilot(), ObjectIdentifier.fetchValue(activeJob.getPlannedLotID()));
                            }
                        } else if (activeJob.getStatus().equals(PilotRunInfo.Status.Failed)) {
                            // PilotRunJob status is Failed
                            Validations.check(true, retCodeConfigEx.getPrPilotStatusError(), PilotRunInfo.Status.Failed.getValue());
                        }
                    }
                } else if (plan.getCategory().equals(PilotRunInfo.Category.Recipe)) {
                    return out;
                }
            }
        }
        return out;
    }

    @Override
    public void checkPilotRunFail(Infos.ObjCommon objCommon, List<Infos.OpeCompLot> operationCompleteLot) {
        int opeCompLotSize = CimArrayUtils.getSize(operationCompleteLot);
        for (int iCnt = 0; iCnt < opeCompLotSize; iCnt++) {
            if (!CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_OK, operationCompleteLot.get(iCnt).getSpecificationCheckResult())
                    && CimStringUtils.isNotEmpty(operationCompleteLot.get(iCnt).getSpecificationCheckResult())
                    && !CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1ASTERISK, operationCompleteLot.get(iCnt).getSpcCheckResult())
                    && !CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_ERROR, operationCompleteLot.get(iCnt).getSpcCheckResult())) {

                PilotRunInfo.Plan plan = this.getPilotRunBylotID(objCommon, operationCompleteLot.get(iCnt).getLotID());
                if (null == plan) continue;

                //check all pilot run
                Boolean allPilotRun = this.checkAllPilotRun(objCommon, plan);

                PilotRunInfo.Job activeJob = plan.getActiveJob();
                if (allPilotRun) {
                    if (plan.getStatus().equals(PilotRunInfo.Status.Ongoing)
                            && activeJob.getStatus().equals(PilotRunInfo.Status.Ongoing)) {
                        CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, plan.getPilotRunPlanID());
                        Validations.check(null == pilotRunPlanBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
                        pilotRunPlanBO.makeExited();
                        // bug-6763 find in bug 6763,erro:not found plan,this is a plan job not a plan
                        CimPilotRunJob pilotRunJobBO = baseCoreFactory.getBO(CimPilotRunJob.class, activeJob.getPilotRunJobID());
                        Validations.check(null == pilotRunJobBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
                        pilotRunJobBO.makeExited();
                        continue;
                    }
                }
                // "pilot run job status 应该为ongoing"
                Validations.check(!activeJob.getStatus().equals(PilotRunInfo.Status.Ongoing), retCodeConfigEx.getPrPilotRunJobStatusError(), PilotRunInfo.Status.Ongoing.getValue());
                if (CimObjectUtils.equals(activeJob.getPlannedLotID(), operationCompleteLot.get(iCnt).getLotID())) {
                    CimPilotRunJob pilotRunJobBO = baseCoreFactory.getBO(CimPilotRunJob.class, activeJob.getPilotRunJobID());
                    Validations.check(null == pilotRunJobBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
                    pilotRunJobBO.makeExited();
                    continue;
                }
                // "pilot run plan status 应该为ongoing"
                Validations.check(!plan.getStatus().equals(PilotRunInfo.Status.Ongoing), retCodeConfigEx.getCreatePilotRunPlanStatuesError(), PilotRunInfo.Status.Ongoing.getValue());
                if (CimObjectUtils.equals(plan.getPlannedLotID(), operationCompleteLot.get(iCnt).getLotID())) {
                    CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, plan.getPilotRunPlanID());
                    Validations.check(null == pilotRunPlanBO, retCodeConfigEx.getNotFoundPiLotRunPlan());
                    pilotRunPlanBO.makeExited();
                }
            }
        }
    }

    @Override
    public void checkPilotRunCompleted(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes) {

        // get pilot run info
        List<Infos.checkPilotRunCompletedInfo> checkPilotRunCompletedInfos = new ArrayList<>();

        if (CimArrayUtils.isNotEmpty(startCassettes)) {
            for (Infos.StartCassette startCassette : startCassettes) {
                if (CimArrayUtils.isNotEmpty(startCassette.getLotInCassetteList())) {
                    for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                        if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                            Infos.checkPilotRunCompletedInfo checkPilotRunCompletedInfo = new Infos.checkPilotRunCompletedInfo();
                            checkPilotRunCompletedInfo.setLotID(lotInCassette.getLotID());
                            checkPilotRunCompletedInfo.setOperationNumber(lotInCassette.getStartOperationInfo().getOperationNumber());
                            checkPilotRunCompletedInfos.add(checkPilotRunCompletedInfo);
                        }
                    }
                }
            }
        }


        for (Infos.checkPilotRunCompletedInfo checkPilotRunCompletedInfo : checkPilotRunCompletedInfos) {

            PilotRunInfo.Plan plan = this.getPilotRunBylotID(objCommon, checkPilotRunCompletedInfo.getLotID());
            if (null == plan) continue;

            List<PilotRunInfo.SectionSpec> pilotRunSection = plan.getPilotRunSection();
            //get end OperationNumber
            String endOperationNumber = null;
            for (int i = 0; i < CimArrayUtils.getSize(pilotRunSection); i++) {
                if (pilotRunSection.get(i).getStepLabel().equals(PilotRunInfo.Label.E)) {
                    endOperationNumber = pilotRunSection.get(i - 1).getStepNumber();
                }
            }

            PilotRunInfo.ErrorType errorNumber = this.checkPilotRunCompletedForFailed(objCommon, plan);
            switch (errorNumber) {
                case Success:
                    //get job status and end step number
                    PilotRunInfo.Job activeJob = plan.getActiveJob();
                    Validations.check(!activeJob.getStatus().equals(PilotRunInfo.Status.Ongoing)
                            && !activeJob.getStatus().equals(PilotRunInfo.Status.Pass), retCodeConfig.getError());
                    if (CimObjectUtils.equals(activeJob.getPlannedLotID(), checkPilotRunCompletedInfo.getLotID())
                            && CimStringUtils.equals(endOperationNumber, checkPilotRunCompletedInfo.getOperationNumber())) {
                        Params.PilotRunStatusChangeParams pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
                        pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Pass.getValue());
                        pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunJob.getValue());
                        pilotRunStatusChangeParams.setEntity(activeJob.getPilotRunJobID());
                        this.changePilotRunstatus(objCommon, pilotRunStatusChangeParams);
                        continue;
                    }

                    //get plan status and end step number
                    Validations.check(!plan.getStatus().equals(PilotRunInfo.Status.Ongoing), retCodeConfig.getError());
                    if (CimObjectUtils.equals(plan.getPlannedLotID(), checkPilotRunCompletedInfo.getLotID())
                            && CimStringUtils.equals(endOperationNumber, checkPilotRunCompletedInfo.getOperationNumber())) {
                        Params.PilotRunStatusChangeParams pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
                        pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Completed.getValue());
                        pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunPlan.getValue());
                        pilotRunStatusChangeParams.setEntity(plan.getPilotRunPlanID());
                        this.changePilotRunstatus(objCommon, pilotRunStatusChangeParams);
                    }
                    break;

                case PilotJobError:
                    Params.PilotRunStatusChangeParams pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
                    pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Failed.getValue());
                    pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunJob.getValue());
                    pilotRunStatusChangeParams.setEntity(plan.getActiveJob().getPilotRunJobID());
                    this.changePilotRunstatus(objCommon, pilotRunStatusChangeParams);
                    break;

                case PilotPlanError:
                    pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
                    pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Failed.getValue());
                    pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunPlan.getValue());
                    pilotRunStatusChangeParams.setEntity(plan.getPilotRunPlanID());
                    this.changePilotRunstatus(objCommon, pilotRunStatusChangeParams);
                    break;

                case AllPilotError:
                    pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
                    pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Failed.getValue());
                    pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunJob.getValue());
                    pilotRunStatusChangeParams.setEntity(plan.getActiveJob().getPilotRunJobID());
                    this.changePilotRunstatus(objCommon, pilotRunStatusChangeParams);

                    pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
                    pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Failed.getValue());
                    pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunPlan.getValue());
                    pilotRunStatusChangeParams.setEntity(plan.getPilotRunPlanID());
                    this.changePilotRunstatus(objCommon, pilotRunStatusChangeParams);
                    break;
            }
        }
    }

    @Override
    public List<PilotRunInfo.SectionSpec> getSectionSpecInfo(Infos.ObjCommon objCommon, List<PilotRunSectionInfo> pilotRunSectionInfo, ObjectIdentifier processPDID) {
        List<PilotRunInfo.SectionSpec> sectionSpecs = new ArrayList<>();
        String tmpSql = "WITH\n" +
                "    PF_REST AS\n" +
                "    (\n" +
                "        SELECT\n" +
                "            i.module_no || '.' || i.ope_no AS MODULE_OPER,\n" +
                "            i.STEP_ID,\n" +
                "            i.PRP_TYPE\n" +
                "        FROM\n" +
                "            (\n" +
                "                SELECT\n" +
                "                    c.ROUTE_NO AS module_no,\n" +
                "                    g.ope_no    AS ope_no,\n" +
                "                    g.STEP_ID,\n" +
                "                    h.PRP_TYPE\n" +
                "                FROM\n" +
                "                    OMPRP a,\n" +
                "                    OMPRF b,\n" +
                "                    OMPRF_ROUTESEQ c,\n" +
                "                    OMPRP d,\n" +
                "                    OMPRF e,\n" +
                "                    OMPRF_PRSSSEQ f,\n" +
                "                    OMPRSS g,\n" +
                "                    OMPRP h\n" +
                "                WHERE\n" +
                "                    a.ACTIVE_MROUTE_PRF_RKEY = b.id\n" +
                "                AND b.id = c.refkey\n" +
                "                AND c.ROUTE_RKEY = d.id\n" +
                "                AND d.ACTIVE_PRF_RKEY = e.id\n" +
                "                AND e.id = f.refkey\n" +
                "                AND f.PRSS_RKEY = g.id\n" +
                "                AND a.PRP_LEVEL = 'Main'\n" +
                "                AND a.PRP_ID = ?1 \n" +
                "                AND g.STEP_RKEY = h.ID) i\n" +
                "    )\n" +
                "    ,\n" +
                "    TEMP_TABLE AS\n" +
                "    (";

        String preSql = "    )\n" +
                "SELECT\n" +
                "    PF_REST.*\n" +
                "FROM\n" +
                "    PF_REST,\n" +
                "    TEMP_TABLE\n" +
                "WHERE\n" +
                "    PF_REST.MODULE_OPER = TEMP_TABLE.STEP_NO ";

        StringBuilder buffSql = new StringBuilder();
        for (int i = 0; i < CimArrayUtils.getSize(pilotRunSectionInfo); i++) {
            String stepNumber = pilotRunSectionInfo.get(i).getStepNumber();
            if (CimStringUtils.isNotEmpty(stepNumber)) {
                String tmpbuffSql = String.format("SELECT\n" +
                        "            '%s' AS step_no\n" +
                        "        FROM\n" +
                        "            dual", stepNumber);
                if (CimArrayUtils.getSize(pilotRunSectionInfo) - 1 != i) {
                    tmpbuffSql += " UNION ";
                }
                buffSql.append(tmpbuffSql);
            }
        }
        String sql = tmpSql + buffSql + preSql;
        List<Object[]> query = cimJpaRepository.query(sql, ObjectIdentifier.fetchValue(processPDID));
        if (CimArrayUtils.isNotEmpty(query)) {
            for (Object[] objects : query) {
                PilotRunInfo.SectionSpec sectionSpec = new PilotRunInfo.SectionSpec();
                sectionSpec.setStepNumber(objects[0].toString());
                sectionSpec.setStepID(ObjectIdentifier.build(objects[1].toString(), ""));
                sectionSpec.setStepType(ObjectIdentifier.build(objects[2].toString(), ""));
                sectionSpecs.add(sectionSpec);
            }
        }
        return sectionSpecs;
    }

    @Override
    public PilotRunInfo.ErrorType checkPilotRunCompletedForFailed(Infos.ObjCommon objCommon, PilotRunInfo.Plan plan) {
        Boolean allPilotRun = this.checkAllPilotRun(objCommon, plan);

        PilotRunInfo.Job activeJob = plan.getActiveJob();
        if (allPilotRun) {
            CimPilotRunJob jobBO = baseCoreFactory.getBO(CimPilotRunJob.class, activeJob.getPilotRunJobID());
            CimPilotRunPlan planBO = baseCoreFactory.getBO(CimPilotRunPlan.class, plan.getPilotRunPlanID());
            if (jobBO.isExited() && planBO.isExited()) {
                return PilotRunInfo.ErrorType.AllPilotError;
            }
        }

        //get job status and end step number
        // "pilot run job status 应该为ongoing"
        Validations.check(!activeJob.getStatus().equals(PilotRunInfo.Status.Ongoing)
                && !activeJob.getStatus().equals(PilotRunInfo.Status.Pass), retCodeConfigEx.getPrPilotRunJobStatusError(), PilotRunInfo.Status.Ongoing.getValue());
        CimPilotRunJob jobBO = baseCoreFactory.getBO(CimPilotRunJob.class, activeJob.getPilotRunJobID());
        if (jobBO.isExited()) {
            return PilotRunInfo.ErrorType.PilotJobError;
        }

        //get plan status and end step number
        // "pilot run plan status 应该为ongoing"
        Validations.check(!plan.getStatus().equals(PilotRunInfo.Status.Ongoing), retCodeConfigEx.getPrPilotRunPlanStatusError(), PilotRunInfo.Status.Ongoing.getValue());
        CimPilotRunPlan planBO = baseCoreFactory.getBO(CimPilotRunPlan.class, plan.getPilotRunPlanID());
        if (planBO.isExited()) {
            return PilotRunInfo.ErrorType.PilotPlanError;
        }

        return PilotRunInfo.ErrorType.Success;
    }

    private Boolean checkAllPilotRun(Infos.ObjCommon objCommon, PilotRunInfo.Plan plan) {
        PilotRunInfo.Job activeJob = plan.getActiveJob();
        if (!CimObjectUtils.isEmpty(activeJob) && CimObjectUtils.equals(plan.getPlannedLotID(), activeJob.getPlannedLotID())) {
            return true;
        }
        return false;
    }

    @Override
    public PilotRunInfo.Plan getPilotRunBylotID(Infos.ObjCommon common, ObjectIdentifier lotID) {
        PilotRunInfo.Plan plan = null;
        String jobSql = " SELECT * FROM OMPRJOB WHERE LOT_ID = ?1 AND STATUS = ?2";
        CimPilotRunJobDO pilotRunJobDO = cimJpaRepository.queryOne(jobSql, CimPilotRunJobDO.class, ObjectIdentifier.fetchValue(lotID), PilotRunInfo.Status.Ongoing.getValue());
        if (null != pilotRunJobDO) {
            CimPilotRunJob pilotRunJobBO = baseCoreFactory.getBO(CimPilotRunJob.class, pilotRunJobDO.getId());
            plan = pilotRunJobBO.getPilotRunPlan().getPilotRunPlanInfo();
        }

        String planSql = " SELECT * FROM OMPR WHERE LOT_ID = ?1 AND STATUS = ?2";
        CimPilotRunPlanDO pilotRunPlanDO = cimJpaRepository.queryOne(planSql, CimPilotRunPlanDO.class, ObjectIdentifier.fetchValue(lotID), PilotRunInfo.Status.Ongoing.getValue());
        if (null != pilotRunPlanDO) {
            CimPilotRunPlan pilotRunPlanBO = baseCoreFactory.getBO(CimPilotRunPlan.class, pilotRunPlanDO.getId());
            plan = pilotRunPlanBO.getPilotRunPlanInfo();
        }

        if (null != plan) {
            plan.getPilotRunSection().sort(Comparator.comparing(PilotRunInfo.SectionSpec::getStepNumber));
        }

        return plan;
    }

    @Override
    public List<Results.JobInfo> getPilotJobListInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        log.debug("step1 - Check for the presence of equipment");
        CimMachine eqpBo = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(eqpBo == null, retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));

        log.debug("step2 - query recipe job from equipment");
        List<CimRecipeGroupPilotRunJob> recipePilotRunJobByEqp = pilotRunManager.findRecipePilotRunJobByEqp(equipmentID);

        log.debug("step3 - Determines whether the query result is null");
        if (CimArrayUtils.isEmpty(recipePilotRunJobByEqp)) {
            log.info("Query recipe group job from equipment {}, the result is empty", equipmentID);
            return Collections.emptyList();
        }

        log.debug("step4 - Assemble the query result into the output parameter");
        List<Results.JobInfo> jobInfos = new ArrayList<>();
        for (CimRecipeGroupPilotRunJob cimRecipeGroupPilotRunJob : recipePilotRunJobByEqp) {
            Results.JobInfo jobInfo = new Results.JobInfo();
            jobInfos.add(jobInfo);

            ObjectIdentifier recipeGroupID = cimRecipeGroupPilotRunJob.getRecipePilotRunJobInfo().getRecipeGroupID();
            String subRecipeGroupValue = CimStringUtils.subAfter(ObjectIdentifier.fetchValue(recipeGroupID), ".", false);
            recipeGroupID.setValue(subRecipeGroupValue);
            jobInfo.setRecipeGroupID(recipeGroupID);

            jobInfo.setPilotJobID(ObjectIdentifier.build(cimRecipeGroupPilotRunJob.getIdentifier(), cimRecipeGroupPilotRunJob.getPrimaryKey()));
            jobInfo.setLotID(cimRecipeGroupPilotRunJob.getRecipePilotRunJobInfo().getLotID());
            jobInfo.setStatus(cimRecipeGroupPilotRunJob.getStatus().name());
            jobInfo.setEquipmentID(cimRecipeGroupPilotRunJob.getRecipePilotRunJobInfo().getEquipmentID());
            jobInfo.setPrType(cimRecipeGroupPilotRunJob.getType().name());
            jobInfo.setPilotWaferCount(cimRecipeGroupPilotRunJob.getPilotWaferCount());
            jobInfo.setCoverLevel(cimRecipeGroupPilotRunJob.getCoverLevel());
            jobInfo.setCoverRecipeFlag(cimRecipeGroupPilotRunJob.isCoverRecipe());
            jobInfo.setFromEqpState(cimRecipeGroupPilotRunJob.getFromEquipmentState());
            jobInfo.setToEqpState(cimRecipeGroupPilotRunJob.getToEquipmentState());
            jobInfo.setClaimMemo(cimRecipeGroupPilotRunJob.getClaimedMemo());
            jobInfo.setRecipeIDs(cimRecipeGroupPilotRunJob.getRecipePilotRunJobInfo().getRecipes());
            jobInfo.setCreateTime(cimRecipeGroupPilotRunJob.getCreateTime());
        }
        return jobInfos;
    }

    @Override
    public void createPilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params) {
        log.info("recipe group create start . PilotRunMethod : createPilotRunRecipeGroup()");
        log.info("create recipeGroup of recipe group ID is {}", params.getRecipeGroupID());

        List<ObjectIdentifier> recipeInGroup = new ArrayList<>();
        log.info("check if equipmentID or equipmentID refKey is empty");
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID()), retCodeConfig.getInvalidInputParam());

        // get recipe info in the recipe group
        log.info("get recipe info in the recipe group of equipment");
        CimRecipeGroupPilotRunDO cimRecipeGroupPilotRunDO = new CimRecipeGroupPilotRunDO();
        cimRecipeGroupPilotRunDO.setEquipmentId(params.getEquipmentID().getValue());
        cimRecipeGroupPilotRunDO.setEquipmentObj(params.getEquipmentID().getReferenceKey());

        List<CimRecipeGroupPilotRunDO> cimRecipeGroupPilotRunDOList = cimJpaRepository.findAll(Example.of(cimRecipeGroupPilotRunDO));
        Optional.ofNullable(cimRecipeGroupPilotRunDOList).orElseGet(() -> Collections.emptyList())
                .parallelStream()
                .map(DO -> baseCoreFactory.getBO(CimRecipeGroup.class, DO.getReferenceKey()))
                .forEach(BO -> {
                    log.info("check if recipeGroupID already exists");
                    if (ObjectIdentifier.equalsWithValue(params.getRecipeGroupID(), BO.getRecipeGroupInfo().getRecipeGroupID())) {
                        RecipeGroup.Type type = BO.getType();
                        Validations.check(true, retCodeConfigEx.getPrRecipeGroupAlreadyExists(), type);
                    }
                    List<ObjectIdentifier> recipes = BO.getRecipeGroupInfo().getRecipeInfos();
                    Optional.ofNullable(recipes).orElseGet(() -> Collections.emptyList()).parallelStream().forEach(recipe -> recipeInGroup.add(recipe));
                });

        log.info("Verify that the recipe is included in an existing group");
        params.getRecipeIDs().parallelStream().forEach(rec -> {
            Validations.check(recipeInGroup.contains(rec), retCodeConfigEx.getPrRecipeIncludedInGroup(), rec.getValue());
        });

        log.info("start create recipeGroup");
        CimRecipeGroup recipeGroupNamed = recipeManager.createRecipeGroupNamed(ObjectIdentifier.fetchValue(params.getRecipeGroupID()));
        RecipeGroup.Info coreParam = convertToCoreInfo(objCommon, params);
        log.info("save recipe group data");
        recipeGroupNamed.setRecipeGroupInfo(coreParam);
    }

    @Override
    public void updatePilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params) {
        log.info("recipe group update start . PilotRunMethod : updatePilotRunRecipeGroup()");
        log.info("update recipeGroup of recipe group ID is {}", params.getRecipeGroupID());

        //check recipe is not existed the group
        Validations.check(getRecipeGroup(params.getRecipeGroupID(), params.getEquipmentID()) == null, retCodeConfigEx.getPrCannotFoundRecipeGroup(), params.getRecipeGroupID().getValue().replaceAll(String.format("%s.", RecipeGroup.Type.PMPilotRun), ""));

        log.info("find the entity of the recipeGroup {}", params.getRecipeGroupID());
        CimRecipeGroup cimRecipeGroup = baseCoreFactory.getBO(CimRecipeGroup.class, params.getRecipeGroupID());

        log.info("change the constructure params to core");
        RecipeGroup.Info coreInfo = convertToCoreInfo(objCommon, params);
        // get createTime
        Timestamp createTime = cimRecipeGroup.getCreateTime();
        // get CreateUser
        CimPerson createUser = cimRecipeGroup.getCreateUser();
        log.info("update recipeGroup data");
        if (createTime != null) {
            coreInfo.setCreateTime(createTime);
        }
        if (createUser != null) {
            coreInfo.setCreateUserID(new ObjectIdentifier(createUser.getIdentifier(), createUser.getPrimaryKey()));
        }
        ;
        cimRecipeGroup.setRecipeGroupInfo(coreInfo);

    }

    @Override
    public void deletePilotRunRecipeGroup(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.DeletePilotRunRecipeGroupParams params) {
        log.info("recipe group delete start . PilotRunMethod : deletePilotRunRecipeGroup()");
        log.info("delete recipeGroup of recipe group ID is {}", params.getGroupIDs());

        Validations.check(params.getGroupIDs() == null || params.getGroupIDs().size() == 0, retCodeConfigEx.getRecipeGroupIdError());
        List<ObjectIdentifier> groupIDs = params.getGroupIDs();
        groupIDs.forEach(groupID -> {
            CimRecipeGroupPilotRunJob recipePilotRunJobByRecipeGroup = pilotRunManager.findRecipePilotRunJobByRecipeGroup(groupID);

            log.info("Check if the group has a job");
            Validations.check(recipePilotRunJobByRecipeGroup != null, retCodeConfigEx.getPrRecipeGrouphasJob());

            log.info("find the entity for groupID {}", groupID);
            CimRecipeGroup recipeGroupBO = baseCoreFactory.getBO(CimRecipeGroup.class, groupID);

            log.info("delete the entity of group {}", groupID);
            recipeManager.removeRecipeGroup(recipeGroupBO);
        });

    }

    @Override
    public List<Results.PilotRunRecipeGroupResults> getPilotRunRecipeGroupList(Infos.ObjCommon objCommon, ObjectIdentifier recipeGroupID, ObjectIdentifier equipmentID) {
        log.info("get recipe group list start . PilotRunMethod : getPilotRunRecipeGroupList()");
        log.info("get recipe group list form the equipment{}", equipmentID);
        //change recipeGroupID constructure ,add prefix PR
        if (!ObjectIdentifier.isEmpty(recipeGroupID)) {
            //change recipeGroupID constructure ,add prefix PR
            recipeGroupID.setValue(String.format("%s.%s", RecipeGroup.Type.PMPilotRun, recipeGroupID.getValue()));
        }
        // check param
        log.info("check equipmentID or equipmentID refKey is not empty");
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), "equipmentID cannot be empty");

        // maske the result list for the data
        List<Results.PilotRunRecipeGroupResults> pilotRunRecipeGroupResults;
        // find all recipeGroup of equipment
        log.info(" find all recipeGroup of the equipment {}", equipmentID.getValue());
        CimRecipeGroupPilotRunDO cimRecipeGroupPrDO = new CimRecipeGroupPilotRunDO();
        cimRecipeGroupPrDO.setEquipmentId(equipmentID.getValue());
        cimRecipeGroupPrDO.setEquipmentObj(equipmentID.getReferenceKey());

        log.info("find all entity of the equipment");
        List<CimRecipeGroupPilotRunDO> cimRecipeGroupPilotRunDOList = cimJpaRepository.findAll(Example.of(cimRecipeGroupPrDO));
        List<CimRecipeGroup> cimRecipeGrouplist = new ArrayList<>();
        Optional.ofNullable(cimRecipeGroupPilotRunDOList).orElseGet(Collections::emptyList)
                .parallelStream().map(DO -> baseCoreFactory.getBO(CimRecipeGroup.class, DO.getReferenceKey()))
                .filter(BO -> RecipeGroup.Type.PMPilotRun.equals(BO.getType()))
                .forEach(cimRecipeGrouplist::add);

        // change constructure BO to DTO
        if (CimStringUtils.isNotEmpty(recipeGroupID.getValue())) {
            log.info("if recipeGroupID is not null ,to receive data with recipeID not equal to  {}", recipeGroupID.getValue());
            List<CimRecipeGroup> filterimRecipeGrouplist = Optional.of(cimRecipeGrouplist).orElseGet(Collections::emptyList)
                    .parallelStream().filter(BO -> ObjectIdentifier.equalsWithValue(recipeGroupID, BO.getRecipeGroupInfo().getRecipeGroupID())).collect(Collectors.toList());
            pilotRunRecipeGroupResults = convertToResult(filterimRecipeGrouplist);
        } else {
            pilotRunRecipeGroupResults = convertToResult(cimRecipeGrouplist);
        }
        return pilotRunRecipeGroupResults;
    }

    @Override
    public Results.RecipeResults getPilotRunRecipeList(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        log.info("get recipe list start . PilotRunMethod : getPilotRunRecipeList()");
        log.info("get recipe list form the equipment{}", equipmentID);

        //make result list
        Results.RecipeResults recipeResults = new Results.RecipeResults();
        log.info("check equipmentID or equipmentID refKey is not empty");
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), "equipmentID cannot be empty");

        // find all the recipe in the equipment
        log.info("find all recipe in the equipment");
        CimMachineRecipeEqpDO cimMachineRecipeEqpDO = new CimMachineRecipeEqpDO();
        cimMachineRecipeEqpDO.setEquipmentID(equipmentID.getValue());
        cimMachineRecipeEqpDO.setEquipmentObj(equipmentID.getReferenceKey());
        List<CimMachineRecipeEqpDO> cimMachineRecipeEqpDOList = cimJpaRepository.findAll(Example.of(cimMachineRecipeEqpDO));
        // make a list for recipe
        List<ObjectIdentifier> recipeInEquipment = new ArrayList<>();

        // get the all recipe info in the equipment
        log.info("get the all recipe info in the equipment");
        Optional.ofNullable(cimMachineRecipeEqpDOList).orElseGet(Collections::emptyList).parallelStream().map(DO -> baseCoreFactory.getBO(CimMachineRecipe.class, DO.getReferenceKey())).forEach(BO -> {
            ObjectIdentifier machineRecipeID = BO.getMachineRecipeID();
            recipeInEquipment.add(machineRecipeID);
        });

        // make a lisit for recipe in the recipe group
        List<ObjectIdentifier> recipeInGroup = new ArrayList<>();

        // get recipe info in the recipe group
        log.info("get recipe info in the recipe group of equipment");
        CimRecipeGroupPilotRunDO cimRecipeGroupPilotRunDO = new CimRecipeGroupPilotRunDO();
        cimRecipeGroupPilotRunDO.setEquipmentId(equipmentID.getValue());
        cimRecipeGroupPilotRunDO.setEquipmentObj(equipmentID.getReferenceKey());
        List<CimRecipeGroupPilotRunDO> cimRecipeGroupPilotRunDOList = cimJpaRepository.findAll(Example.of(cimRecipeGroupPilotRunDO));
        Optional.ofNullable(cimRecipeGroupPilotRunDOList).orElseGet(Collections::emptyList)
                .parallelStream()
                .map(DO -> baseCoreFactory.getBO(CimRecipeGroup.class, DO.getReferenceKey()))
                .filter(BO -> CimObjectUtils.equals(RecipeGroup.Type.PMPilotRun, BO.getRecipeGroupInfo().getType()))
                .forEach(BO -> {
                    List<ObjectIdentifier> recipes = BO.getRecipeGroupInfo().getRecipeInfos();
                    Optional.ofNullable(recipes).orElseGet(Collections::emptyList).parallelStream().forEach(recipeInGroup::add);
                });

        // remove duplicates from the list
        log.info(" remove duplicates from the list");
        recipeInEquipment.removeAll(recipeInGroup);

        log.info("save the usefull recipe");
        recipeResults.setRecipeIDs(recipeInEquipment);

        return recipeResults;
    }

    @Override
    public CimRecipeGroupPilotRunJob createRecipeJob(Infos.ObjCommon objCommon, ObjectIdentifier recipeGroupID) {
        log.debug("step1 - check if recipe group {} exists", recipeGroupID);
        CimRecipeGroup recipeGroup = baseCoreFactory.getBO(CimRecipeGroup.class, recipeGroupID);
        Validations.check(null == recipeGroup, "recipeGroup has not been found");

        log.debug("step2 - Add a lock to recipe group {}",recipeGroupID);
        objectLockMethod.objectLock(objCommon,CimRecipeGroup.class,recipeGroupID);

        log.debug("step3 - check if recipe job has been created");
        CimRecipeGroupPilotRunJob recipePilotRunJobByRecipeGroup = pilotRunManager.findRecipePilotRunJobByRecipeGroup(recipeGroupID);

        log.trace("step4 - check to see if recipe job exists {}",recipePilotRunJobByRecipeGroup != null);
        if (recipePilotRunJobByRecipeGroup != null) {
            log.debug("Check that {} has created recipe group job {}", recipeGroupID, recipePilotRunJobByRecipeGroup.getIdentifier());
            return null;
        }

        log.debug("step5 - stitch the id of recipe job with the id of recipe group");
        Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
        String identifier = new StringJoiner(".")
                .add(ObjectIdentifier.fetchValue(recipeGroupID))
                .add(currentTimeStamp.toString())
                .toString();
        CimRecipeGroupPilotRunJob recipePilotRunJobNamed = pilotRunManager.createRecipePilotRunJobNamed(identifier);

        log.debug("step6 - set param to recipe job");
        recipePilotRunJobNamed.setIdentifier(identifier);
        recipePilotRunJobNamed.setRecipeGroup(recipeGroup);
        recipePilotRunJobNamed.setStatus(RecipePilotRun.JobStatus.Open);
        recipePilotRunJobNamed.setEquipment(recipeGroup.getRecipePilotRunInfo().getEquipmentID());
        recipePilotRunJobNamed.setType(recipeGroup.getRecipePilotRunInfo().getPilotRunType());
        recipePilotRunJobNamed.setPilotWaferCount(recipeGroup.getRecipePilotRunInfo().getPilotRunWaferCount());
        recipePilotRunJobNamed.setCoverLevel(recipeGroup.getRecipePilotRunInfo().getCoverLevel());

        log.trace("recipe group job cover recipe is {}",recipeGroup.getRecipePilotRunInfo().getCoverRecipeFlag());
        if (recipeGroup.getRecipePilotRunInfo().getCoverRecipeFlag() != null) {
            if (recipeGroup.getRecipePilotRunInfo().getCoverRecipeFlag()) {
                recipePilotRunJobNamed.makeCoverRecipe();
            } else {
                recipePilotRunJobNamed.makeNotCoverRecipe();
            }
        }
        recipePilotRunJobNamed.setFromEquipmentState(recipeGroup.getRecipePilotRunInfo().getFromEqpState());
        recipePilotRunJobNamed.setToEquipmentState(recipeGroup.getRecipePilotRunInfo().getToEqpState());
        recipePilotRunJobNamed.setClaimedMemo(recipeGroup.getRecipePilotRunInfo().getClaimMemo());
        CimPerson personBO = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        recipePilotRunJobNamed.setCreateUser(personBO);
        recipePilotRunJobNamed.setCreateTime(currentTimeStamp);

        log.debug("set recipe to recipe job");
        List<ObjectIdentifier> recipes = recipeGroup.allRecipes();
        List<CimMachineRecipe> recipeList = Optional.ofNullable(recipes).orElse(Collections.emptyList()).parallelStream()
                .map(recipeID -> genericCoreFactory.getBO(CimMachineRecipe.class, recipeID))
                .collect(Collectors.toList());
        recipePilotRunJobNamed.setJobRecipes(recipeList);
        return recipePilotRunJobNamed;
    }

    @Override
    public void recipeJobBindLot(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.RecipeJobBindLotParams param) {
        log.debug("check for presence of recipe job and lot");
        CimLot lotBO = baseCoreFactory.getBO(CimLot.class, param.getLotID());
        Validations.check(lotBO == null, retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(param.getLotID()));
        CimRecipeGroupPilotRunJob recipeJobBo = baseCoreFactory.getBO(CimRecipeGroupPilotRunJob.class, param.getRecipeJobID());
        Validations.check(recipeJobBo == null, retCodeConfig.getNotFoundRecipeJob(), ObjectIdentifier.fetchValue(param.getRecipeJobID()));

        log.debug("check whether recipe job of wafer count meets the requirements");
        List<ProductDTO.WaferInfo> allWaferInfo = lotBO.getAllWaferInfo();
        int size = CimArrayUtils.getSize(allWaferInfo);
        log.trace("lot of wafer count is {},recipe job wafer count is {}",size,recipeJobBo.getPilotWaferCount());
        Validations.check(size <= 0, retCodeConfigEx.getPrLotWaferCountNotZero());
        Validations.check(size != recipeJobBo.getPilotWaferCount(), retCodeConfigEx.getPrWaferCountError());

        log.debug("set param of recipe job");
        recipeJobBo.setLot(lotBO);
        log.debug("get person information");
        CimPerson personBO = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        recipeJobBo.setLatestOperatedUser(personBO);
        recipeJobBo.setLatestOperatedTimestamp(CimDateUtils.getCurrentTimeStamp());
    }

    @Override
    public List<CimRecipeGroup> getRecipeGroupByEqp(ObjectIdentifier equipmentID) {
        String sql = "SELECT * FROM OMRECIPEGROUP_PR WHERE EQP_RKEY = ?";
        List<CimRecipeGroupPilotRunDO> recipeGroupPrDos = cimJpaRepository.query(sql, CimRecipeGroupPilotRunDO.class, ObjectIdentifier.fetchReferenceKey(equipmentID));
        List<CimRecipeGroup> recipeGroupBOs = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(recipeGroupPrDos)) {
            for (CimRecipeGroupPilotRunDO recipeGroupPrDo : recipeGroupPrDos) {
                CimRecipeGroup recipeGroupBO = baseCoreFactory.getBO(CimRecipeGroup.class, recipeGroupPrDo.getLinkKey());
                recipeGroupBOs.add(recipeGroupBO);
            }
        }
        return recipeGroupBOs;
    }

    private RecipeGroup.Info convertToCoreInfo(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.CreatePilotRunRecipeGroupParams params) {

        // set core params
        RecipePilotRun.Info recipeGroupPrInfo = new RecipePilotRun.Info();

        // set recipeGroupRilotRunInfo
        recipeGroupPrInfo.setEquipmentID(params.getEquipmentID());
        recipeGroupPrInfo.setPilotRunType(RecipePilotRun.Type.PM);
        recipeGroupPrInfo.setPilotRunWaferCount(params.getWaferCount());
        recipeGroupPrInfo.setCoverLevel(params.getCoverLevel());
        recipeGroupPrInfo.setCoverRecipeFlag(params.getCoverRecipeFlag());
        recipeGroupPrInfo.setFromEqpState(params.getFromEqpState());
        recipeGroupPrInfo.setModifyFlag(false);
        recipeGroupPrInfo.setToEqpState(params.getToEqpState());
        recipeGroupPrInfo.setClaimMemo(params.getClaimMemo());

        // set recipeGroupRecipeInfo
        RecipeGroup.Info info = new RecipeGroup.Info();
        info.setType(RecipeGroup.Type.PMPilotRun);
        info.setRecipeInfos(params.getRecipeIDs());
        info.setRecipePilotRunInfo(recipeGroupPrInfo);
        info.setRecipeGroupID(params.getRecipeGroupID());
        info.setDescription(params.getClaimMemo());
        info.setCreateUserID(objCommon.getUser().getUserID());
        info.setCreateTime(objCommon.getTimeStamp().getReportTimeStamp());
        info.setLastClaimedUserID(objCommon.getUser().getUserID());
        info.setLastClaimedTime(objCommon.getTimeStamp().getReportTimeStamp());
        return info;
    }

    @Override
    public RecipeGroup.Info getRecipeGroup(ObjectIdentifier recipeGroupID) {
        CimRecipeGroup recipeGroup = baseCoreFactory.getBO(CimRecipeGroup.class, recipeGroupID);
        if (recipeGroup == null) {
            return null;
        }
        return convertToOmDTO(recipeGroup);
    }

    @Override
    public List<Results.PilotRunRecipeGroupResults> getRecipeGroup(ObjectIdentifier recipeGroupID, ObjectIdentifier equipmentID) {
        CimRecipeGroupPilotRunDO cimRecipeGroupPilotRunDO = new CimRecipeGroupPilotRunDO();
        cimRecipeGroupPilotRunDO.setEquipmentObj(equipmentID.getReferenceKey());
        cimRecipeGroupPilotRunDO.setEquipmentId(equipmentID.getValue());
        List<CimRecipeGroupPilotRunDO> cimRecipeGroupPilotRunDOs = cimJpaRepository.findAll(Example.of(cimRecipeGroupPilotRunDO));
        List<CimRecipeGroup> CimRecipeGroupBOs = Optional.ofNullable(cimRecipeGroupPilotRunDOs).orElseGet(Collections::emptyList).parallelStream().
                map(DO -> baseCoreFactory.getBO(CimRecipeGroup.class, DO.getReferenceKey()))
                .filter(BO -> CimObjectUtils.equals(BO.getRecipeGroupInfo().getType(), RecipeGroup.Type.PMPilotRun) && ObjectIdentifier.equalsWithValue(recipeGroupID, BO.getRecipeGroupInfo().getRecipeGroupID())).collect(Collectors.toList());
        if (CimArrayUtils.isEmpty(CimRecipeGroupBOs)) {
            return null;
        }
        return convertToResult(CimRecipeGroupBOs);
    }

    private RecipeGroup.Info convertToOmDTO(CimRecipeGroup recipeGroup) {
        RecipeGroup.Info coreInfo = recipeGroup.getRecipeGroupInfo();
        RecipeGroup.Info info = new RecipeGroup.Info();
        info.setRecipePilotRunInfo(coreInfo.getRecipePilotRunInfo());
        info.setDescription(coreInfo.getDescription());
        info.setRecipeGroupID(coreInfo.getRecipeGroupID());
        info.setRecipeInfos(coreInfo.getRecipeInfos());
        return info;
    }


    private List<Results.PilotRunRecipeGroupResults> convertToResult(List<CimRecipeGroup> recipeGroups) {

        List<Results.PilotRunRecipeGroupResults> pilotRunRecipeGroupList = new ArrayList<>();
        recipeGroups.stream().forEach(recipeGroup -> {

            // change constructure from DO to Result(DTO)
            Results.PilotRunRecipeGroupResults pilotRunRecipeGroupResults = new Results.PilotRunRecipeGroupResults();
            pilotRunRecipeGroupResults.setEquipmentID(recipeGroup.getRecipePilotRunInfo().getEquipmentID());
            pilotRunRecipeGroupResults.setPilotType(recipeGroup.getRecipePilotRunInfo().getPilotRunType().toString());
            pilotRunRecipeGroupResults.setCoverLevel(recipeGroup.getRecipePilotRunInfo().getCoverLevel());
            pilotRunRecipeGroupResults.setCoverRecipe(recipeGroup.getRecipePilotRunInfo().getCoverRecipeFlag());
            pilotRunRecipeGroupResults.setWaferCount(recipeGroup.getRecipePilotRunInfo().getPilotRunWaferCount());
            pilotRunRecipeGroupResults.setFromEqpState(recipeGroup.getRecipePilotRunInfo().getFromEqpState());
            pilotRunRecipeGroupResults.setToEqpState(recipeGroup.getRecipePilotRunInfo().getToEqpState());
            pilotRunRecipeGroupResults.setClaimMemo(recipeGroup.getDescription());
            pilotRunRecipeGroupResults.setRecipeIDs(recipeGroup.getRecipeGroupInfo().getRecipeInfos());

            //change constructrue for RecipeGroupID,delete prefix PR
            ObjectIdentifier recipeGroupID = new ObjectIdentifier();
            recipeGroupID.setReferenceKey(recipeGroup.getRecipeGroupInfo().getRecipeGroupID().getReferenceKey());
            recipeGroupID.setValue(recipeGroup.getRecipeGroupInfo().getRecipeGroupID().getValue().replace(String.format("%s.", RecipeGroup.Type.PMPilotRun), ""));

            pilotRunRecipeGroupResults.setRecipeGroupID(recipeGroupID);
            pilotRunRecipeGroupResults.setCreateTime(recipeGroup.getCreateTime());

            pilotRunRecipeGroupList.add(pilotRunRecipeGroupResults);
        });
        if (CimArrayUtils.isNotEmpty(pilotRunRecipeGroupList)) {
            return pilotRunRecipeGroupList.stream().sorted(Comparator.comparing(Results.PilotRunRecipeGroupResults::getCreateTime)).collect(Collectors.toList());
        }
        return pilotRunRecipeGroupList;
    }

    private RecipeGroup.Info convertToCoreInfo(Infos.ObjCommon objCommon, com.fa.cim.pr.Params.UpdatePilotRunRecipeGroupParams params) {

        // set core params
        RecipePilotRun.Info recipeGroupPrInfo = new RecipePilotRun.Info();

        // set recipeGroupRilotRunInfo
        recipeGroupPrInfo.setEquipmentID(params.getEquipmentID());
        recipeGroupPrInfo.setPilotRunType(RecipePilotRun.Type.PM);
        recipeGroupPrInfo.setPilotRunWaferCount(params.getWaferCount());
        recipeGroupPrInfo.setCoverLevel(params.getCoverLevel());
        recipeGroupPrInfo.setCoverRecipeFlag(params.getCoverRecipeFlag());
        recipeGroupPrInfo.setFromEqpState(params.getFromEqpState());
        // change modify flag
        recipeGroupPrInfo.setModifyFlag(true);
        recipeGroupPrInfo.setToEqpState(params.getToEqpState());
        recipeGroupPrInfo.setClaimMemo(params.getClaimMemo());

        // set recipeGroupRecipeInfo
        RecipeGroup.Info info = new RecipeGroup.Info();
        info.setType(RecipeGroup.Type.PMPilotRun);
        info.setRecipeInfos(params.getRecipeIDs());
        info.setRecipePilotRunInfo(recipeGroupPrInfo);
        info.setRecipeGroupID(params.getRecipeGroupID());
        info.setDescription(params.getClaimMemo());
        info.setCreateUserID(objCommon.getUser().getUserID());
        info.setCreateTime(objCommon.getTimeStamp().getReportTimeStamp());
        info.setLastClaimedUserID(objCommon.getUser().getUserID());
        info.setLastClaimedTime(objCommon.getTimeStamp().getReportTimeStamp());
        return info;
    }

    public void checkPMRun(ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassettes) {
        for (Infos.StartCassette startCassette : startCassettes) {
            /*-------------------------*/
            /*   Omit Empty Cassette   */
            /*-------------------------*/
            if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                continue;
            }
            List<Infos.LotInCassette> lotInCassettes = startCassette.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                    checkPMRun(equipmentID, lotInCassette.getLotID(), CimArrayUtils.getSize(lotInCassette.getLotWaferList()), lotInCassette.getStartRecipe().getMachineRecipeID());
                }
            }
        }
    }

    private void checkPMRun(ObjectIdentifier equipmentID, ObjectIdentifier lotID, int lotWaferCount, ObjectIdentifier machineRecipeID) {
        CimRecipePilotRunJobDO jobDO = new CimRecipePilotRunJobDO();
        jobDO.setEquipmentId(ObjectIdentifier.fetchValue(equipmentID));
        log.debug("Get jobs according to equipmentID");
        List<CimRecipePilotRunJobDO> jobDOs = cimJpaRepository.findAll(Example.of(jobDO));
        log.debug("If the job is empty, ignore the check");
        if (CimArrayUtils.isEmpty(jobDOs)) return;

        log.debug("Check if job is bound to lot");
        Optional<CimRecipePilotRunJobDO> jobOpt = jobDOs.stream().filter(x -> ObjectIdentifier.equalsWithValue(x.getLotId(), lotID)).findFirst();
        if (jobOpt.isPresent()) {
            log.debug("job is bound to lot");
            CimRecipeGroupPilotRunJob job = baseCoreFactory.getBO(CimRecipeGroupPilotRunJob.class, jobOpt.get());

            log.debug("Check whether the lot wafer count is greater than or equal to the PMPilotRun wafer count, if it is less, " +
                    "an exception is thrown: [PMPilotRun] lot wafer count %s is less than the PM PilotRun wafer count %s");
            Validations.check(lotWaferCount != job.getPilotWaferCount(), retCodeConfigEx.getPrWaferCountError(), lotWaferCount, job.getPilotWaferCount());

            log.debug("Check whether the recipe of the lot matches the recipe of the job, if it does not match, " +
                    "an exception is thrown: [PMPilotRun] lot recipe %s and job recipes %s do not match");
            Validations.check(job.allJobRecipes().stream().noneMatch(x -> ObjectIdentifier.equalsWithValue(x.getMachineRecipeID(), machineRecipeID)),
                    retCodeConfigEx.getPrRecipeNotMatch(),
                    machineRecipeID, job.allJobRecipes().stream().map(x -> ObjectIdentifier.fetchValue(x.getMachineRecipeID())).collect(Collectors.toList()));
        } else {
            log.debug("job is not bound to lot");
            log.debug("Check whether the recipe of the lot matches the recipe of the job, if it matches, " +
                    "an exception is thrown: [PMPilotRun] Do PM PilotRun first");
            // bug-1553
            boolean isMatchJobRecipe = jobDOs.stream()
                    .map(x -> baseCoreFactory.getBO(CimRecipeGroupPilotRunJob.class, x))
                    .flatMap(x -> x.allJobRecipes().stream())
                    .filter(x->Optional.ofNullable(x).isPresent())
                    .anyMatch(x -> ObjectIdentifier.equalsWithValue(x.getMachineRecipeID(), machineRecipeID));
            Validations.check(isMatchJobRecipe, retCodeConfigEx.getPrFirstRun());
        }
    }
}
