package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.durablectrljob.CimDurableControlJobDO;
import com.fa.cim.entity.runtime.reticlepod.CimReticlePodDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IDurableControlJobMethod;
import com.fa.cim.method.IDurableMethod;
import com.fa.cim.newcore.bo.durable.*;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimPortResource;
import com.fa.cim.newcore.bo.pd.CimDurableProcessOperation;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.dto.durable.DurableDTO;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.drblmngm.Durable;
import com.fa.cim.newcore.standard.mchnmngm.BufferResource;
import com.fa.cim.newcore.standard.mchnmngm.MaterialLocation;
import com.fa.cim.newcore.standard.mchnmngm.PortResource;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.List;

import static com.fa.cim.common.constant.BizConstant.*;

/**
 * <p>DurableControlJobMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/6/22 13:03         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/6/22 13:03
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@OmMethod
@Slf4j
public class DurableControlJobMethod implements IDurableControlJobMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    @Qualifier("DurableManagerCore")
    private DurableManager durableManager;

    @Autowired
    @Qualifier("PersonManagerCore")
    private PersonManager personManager;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IDurableMethod durableMethod;

    @Override
    public List<Infos.DurableControlJobListInfo> durableControlJobListGetDR(Infos.ObjCommon objCommon, Inputs.DurableControlJobListGetDRIn paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        List<Infos.DurableControlJobListInfo> retVal = null;

        log.info("in para durableID: " + ObjectIdentifier.fetchValue(paramIn.getDurableID()));
        log.info("in para durableJobID: " + ObjectIdentifier.fetchValue(paramIn.getDurableJobID()));
        log.info("in para equipmentID: " + ObjectIdentifier.fetchValue(paramIn.getEquipmentID()));
        log.info("in para durableCategory: " + paramIn.getDurableCategory());
        log.info("in para createUserID: " + paramIn.getCreateUserID());
        log.info("in para durableInfoFlag: " + paramIn.getDurableInfoFlag());

        /*-----------------------------------*/
        /*  Create dynamic SQL statement     */
        /*-----------------------------------*/
        StringBuilder sql = new StringBuilder("SELECT * FROM OMDCJ DCJ ");
        List<String> parameters = new ArrayList<>();
        boolean whereFlag = false;
        if (ObjectIdentifier.isNotEmptyWithValue(paramIn.getDurableID())) {
            sql.append(",OMDCJ_DRBL DCJ_DRBL WHERE DCJ_DRBL.PDRBL_ID LIKE ? AND DCJ_DRBL.REFKEY = DCJ.ID ");
            parameters.add(ObjectIdentifier.fetchValue(paramIn.getDurableID()));
            whereFlag = true;
        }

        if (ObjectIdentifier.isNotEmptyWithValue(paramIn.getDurableJobID())) {
            if (!whereFlag) {
                sql.append(" WHERE ");
                whereFlag = true;
            } else {
                sql.append(" AND ");
            }
            sql.append("DCJ.DCJ_ID LIKE ? ");
            parameters.add(ObjectIdentifier.fetchValue(paramIn.getDurableJobID()));
        }

        if (ObjectIdentifier.isNotEmptyWithValue(paramIn.getEquipmentID())) {
            if (!whereFlag) {
                sql.append(" WHERE ");
                whereFlag = true;
            } else {
                sql.append(" AND ");
            }
            sql.append("DCJ.EQP_ID LIKE ? ");
            parameters.add(ObjectIdentifier.fetchValue(paramIn.getEquipmentID()));
        }

        if (CimStringUtils.isNotEmpty(paramIn.getDurableCategory())) {
            if (!whereFlag) {
                sql.append(" WHERE ");
                whereFlag = true;
            } else {
                sql.append(" AND ");
            }
            sql.append("DCJ.DRBL_CATEGORY = ? ");
            parameters.add(paramIn.getDurableCategory());
        }

        if (ObjectIdentifier.isNotEmptyWithValue(paramIn.getCreateUserID())) {
            if (!whereFlag) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }
            sql.append("DCJ.LAST_TRX_USER_ID LIKE ? ");
            parameters.add(ObjectIdentifier.fetchValue(paramIn.getCreateUserID()));
        }
        log.info("SQL : " + sql.toString());

        List<CimDurableControlJobDO> results = cimJpaRepository.query(sql.toString(), CimDurableControlJobDO.class, parameters.toArray());
        if (CimArrayUtils.isNotEmpty(results)) {
            retVal = new ArrayList<>();
            for (CimDurableControlJobDO result : results) {
                Infos.DurableControlJobListInfo jobListInfo = new Infos.DurableControlJobListInfo();
                jobListInfo.setDurableControlJobID(ObjectIdentifier.build(result.getDurableControlJobID(), result.getId()));
                jobListInfo.setEquipmentID(ObjectIdentifier.build(result.getEquipmentID(), result.getEquipmentObj()));
                jobListInfo.setDurableCategory(result.getDurableCategory());
                jobListInfo.setStatus(result.getStatus());
                jobListInfo.setEstimatedCompletionTime(null == result.getEstimatedCMPTime() ? null : result.getEstimatedCMPTime().toString());
                jobListInfo.setLastClaimedUserID(ObjectIdentifier.buildWithValue(result.getClaimUserID()));
                jobListInfo.setLastClaimedTimeStamp(result.getClaimTime().toString());

                if (paramIn.getDurableInfoFlag()) {
                    List<Infos.StartDurable> startDurables = this.durableControlJobDurableListGetDR(objCommon, jobListInfo.getDurableControlJobID());
                    if (CimArrayUtils.isNotEmpty(startDurables)) {
                        List<Infos.DurableControlJobDurable> durableControlJobDurables = new ArrayList<>();
                        jobListInfo.setStrDurableControlJobDurables(durableControlJobDurables);

                        for (Infos.StartDurable startDurable : startDurables) {
                            Infos.DurableControlJobDurable durable = new Infos.DurableControlJobDurable();
                            durable.setDurableID(startDurable.getDurableId());
                            durable.setLoadPurposeType(startDurable.getStartDurablePort().getLoadPurposeType());
                            durable.setLoadPortID(startDurable.getStartDurablePort().getLoadPortID());
                            durable.setLoadSequenceNumber(startDurable.getStartDurablePort().getLoadSequenceNumber());
                            durable.setUnloadPortID(startDurable.getStartDurablePort().getUnloadPortID());
                            durable.setUnloadSequenceNumber(CimNumberUtils.longValue(startDurable.getStartDurablePort().getUnloadSequenceNumber()));

                            switch (result.getDurableCategory()) {
                                case SP_DURABLECAT_CASSETTE:
                                    CimCassetteDO example = new CimCassetteDO();
                                    example.setCassetteID(durable.getDurableID().getValue());
                                    CimCassetteDO aCassette = cimJpaRepository.findOne(Example.of(example)).orElse(null);
                                    if (null != aCassette) {
                                        durable.setStatus(aCassette.getDurableState());
                                        durable.setDescription(aCassette.getDescription());
                                    }
                                    break;
                                case SP_DURABLECAT_RETICLE:
                                    String tmp =
                                            "SELECT\n" +
                                                    "    DRBL.PDRBL_STATE,\n" +         // 0
                                                    "    DRBL.DESCRIPTION,\n" +        // 1
                                                    "    DRBLGRP.PDRBL_GRP_ID,\n" +      // 2
                                                    "    DRBLGRP.PDRBL_GRP_RKEY\n" +      // 3
                                                    "FROM\n" +
                                                    "    OMPDRBL DRBL,\n" +
                                                    "    OMPDRBL_PDRBLGRP DRBLGRP\n" +
                                                    "WHERE\n" +
                                                    "    OMPDRBL.DRBL_ID = ?\n" +
                                                    "AND OMPDRBL.ID = DRBLGRP.REFKEY";
                                    Object[] reticle = cimJpaRepository.queryOne(tmp, durable.getDurableID().getValue());
                                    if (null != reticle) {
                                        durable.setStatus(String.valueOf(reticle[0]));
                                        durable.setDescription(String.valueOf(reticle[1]));
                                        durable.setReticleGroupID(ObjectIdentifier.build(String.valueOf(reticle[2]), String.valueOf(reticle[3])));
                                    }
                                    break;
                                case SP_DURABLECAT_RETICLEPOD:
                                    CimReticlePodDO reticlePodExample = new CimReticlePodDO();
                                    reticlePodExample.setReticlePodID(durable.getDurableID().getValue());
                                    CimReticlePodDO aReticlePod = cimJpaRepository.findOne(Example.of(reticlePodExample)).orElse(null);
                                    if (null != aReticlePod) {
                                        durable.setStatus(aReticlePod.getDurableState());
                                        durable.setDescription(aReticlePod.getDescription());
                                    }
                                    break;
                                default:
                                    // do nothing
                                    break;
                            }
                            durableControlJobDurables.add(durable);
                        }
                    }
                }
                retVal.add(jobListInfo);
            }
        }
        return retVal;
    }

    @Override
    public List<Infos.StartDurable> durableControlJobDurableListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID) {
        Validations.check(null == objCommon || null == durableControlJobID, retCodeConfig.getInvalidInputParam());
        List<Infos.StartDurable> retVal = null;
        String sql =
                "SELECT\n" +
                        "    DCJ_DRBL.PDRBL_ID,\n" +                   // 0
                        "    DCJ_DRBL.PDRBL_RKEY,\n" +                  // 1
                        "    DCJ_DRBL.LOAD_PORT_ID,\n" +              // 2
                        "    DCJ_DRBL.LOAD_PORT_RKEY,\n" +             // 3
                        "    DCJ_DRBL.UNLOAD_PORT_ID,\n" +            // 4
                        "    DCJ_DRBL.UNLOAD_PORT_RKEY,\n" +           // 5
                        "    DCJ_DRBL.IDX_NO,\n" +               // 6
                        "    DCJ_DRBL.LOAD_USAGE_TYPE,\n" +          // 7
                        "    f2.PORT_GRP_UNLOAD_SEQ\n" +                 // 8
                        "FROM\n" +
                        "    OMDCJ DCJ,\n" +
                        "    OMDCJ_DRBL DCJ_DRBL,\n" +
                        "    OMPORT f2\n" +
                        "WHERE\n" +
                        "    DCJ.DCJ_ID = ?\n" +
                        "AND DCJ.ID = DCJ_DRBL.REFKEY\n" +
                        "AND f2.ID = DCJ_DRBL.UNLOAD_PORT_RKEY";
        List<Object[]> results = cimJpaRepository.query(sql, durableControlJobID.getValue());
        if (CimArrayUtils.isNotEmpty(results)) {
            retVal = new ArrayList<>();
            for (Object[] result : results) {
                Infos.StartDurable startDurable = new Infos.StartDurable();
                startDurable.setDurableId(ObjectIdentifier.build(String.valueOf(result[0]), String.valueOf(result[1])));

                Infos.StartDurablePort startDurablePort = new Infos.StartDurablePort();
                startDurable.setStartDurablePort(startDurablePort);

                startDurablePort.setLoadPortID(ObjectIdentifier.build(String.valueOf(result[2]), String.valueOf(result[3])));
                startDurablePort.setUnloadPortID(ObjectIdentifier.build(String.valueOf(result[4]), String.valueOf(result[5])));
                startDurablePort.setLoadSequenceNumber(CimNumberUtils.longValue((Number) result[6]));
                startDurablePort.setLoadPurposeType(String.valueOf(result[7]));
                startDurablePort.setUnloadSequenceNumber(CimNumberUtils.longValue((Number) result[8]));

                retVal.add(startDurable);
            }
        }
        return retVal;
    }

    @Override
    public Outputs.DurableControlJobStartReserveInformationGetOut durableControlJobStartReserveInformationGet(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID) {
        Validations.check(null == objCommon || null == durableControlJobID, retCodeConfig.getInvalidInputParam());

        Outputs.DurableControlJobStartReserveInformationGetOut retVal = new Outputs.DurableControlJobStartReserveInformationGetOut();
        //-----------------------------------//
        //  Get PosDurableControlJob object  //
        //-----------------------------------//
        CimDurableControlJob aDurableControlJob = baseCoreFactory.getBO(CimDurableControlJob.class, durableControlJobID);
        Validations.check(null == aDurableControlJob, retCodeConfigEx.getNotFoundDctrlJob());

        /*-------------------------------------------*/
        /*   Get PosStartDurables Info               */
        /*-------------------------------------------*/
        List<DurableDTO.StartDurableInfo> startDurableSequence = aDurableControlJob.getStartDurableInfo();

        /*-----------------------------*/
        /*   Get DurableCategory Info  */
        /*-----------------------------*/
        String durableCategory = aDurableControlJob.getDurableCategory();
        retVal.setDurableCategory(durableCategory);

        /*-----------------------------*/
        /*   Get Equipment Info        */
        /*-----------------------------*/
        CimMachine aMachine = aDurableControlJob.getMachine();

        boolean bCJMachineNil = false;
        if (null == aMachine) {
            bCJMachineNil = true;
        }

        List<MachineDTO.MachineCassette> strMachineCassetteSeq = null;
        if (!bCJMachineNil) {
            log.info("aMachine is not Null");
            strMachineCassetteSeq = aMachine.allCassettes();
            /*-----------------------------------------------*/
            /*      Set equipmentID to Return Structure      */
            /*-----------------------------------------------*/
            retVal.setEquipmentID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
        }

        //--------------------------------------------------//
        //  Set following informations to Return Structure  //
        //--------------------------------------------------//
        List<Infos.StartDurable> startDurables = new ArrayList<>();
        retVal.setStrStartDurables(startDurables);

        int loop = 0;
        ProcessDTO.ActualStartInformationForPO actualStartInfo = null;
        for (DurableDTO.StartDurableInfo startDurableInfo : startDurableSequence) {
            Infos.StartDurable startDurable = new Infos.StartDurable();
            startDurable.setDurableId(startDurableInfo.getDurableID());

            //*************************************//
            //   StartDurablePort                  //
            //*************************************//
            Infos.StartDurablePort startDurablePort = new Infos.StartDurablePort();
            startDurablePort.setLoadPurposeType(startDurableInfo.getLoadPurposeType());
            startDurablePort.setLoadPortID(startDurableInfo.getLoadPortID());
            startDurablePort.setLoadSequenceNumber(startDurableInfo.getLoadSequenceNumber());
            startDurablePort.setUnloadPortID(startDurableInfo.getUnloadPortID());

            //-----------------------------------------------------------------------------------------------------
            //  Get unloadPortID.
            //  Find the unloadPortID of strMachineCassetteSeq that CassetteID is the same as CassetteID of strMachineCassetteSeq
            //-----------------------------------------------------------------------------------------------------
            if (!bCJMachineNil) {
                if (CimArrayUtils.isNotEmpty(strMachineCassetteSeq)) {
                    for (MachineDTO.MachineCassette machineCassette : strMachineCassetteSeq) {
                        if (ObjectIdentifier.equalsWithValue(machineCassette.getCassetteID(), startDurableInfo.getDurableID())) {
                            log.info("Found!! unloadPortID");
                            startDurablePort.setUnloadPortID(machineCassette.getUnloadPortID());
                        }
                    }
                }
            }
            startDurable.setStartDurablePort(startDurablePort);

            //*************************************//
            //   StartOperationInfo                //
            //*************************************//
            Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
            boolean currentPOFlag = durableMethod.durableCheckConditionForDurablePO(objCommon, durableCategory, startDurable.getDurableId());

            CimDurableProcessOperation aPosDurablePO = null;
            if (currentPOFlag) {
                log.info("currentPOFlag == TRUE");
                switch (durableCategory) {
                    case SP_DURABLECAT_CASSETTE:
                        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startDurable.getDurableId());
                        Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
                        aPosDurablePO = aCassette.getDurableProcessOperation();
                        break;
                    case SP_DURABLECAT_RETICLEPOD:
                        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, startDurable.getDurableId());
                        Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod());
                        aPosDurablePO = aReticlePod.getDurableProcessOperation();
                        break;
                    case SP_DURABLECAT_RETICLE:
                        CimProcessDurable aDurable = baseCoreFactory.getBO(CimProcessDurable.class, startDurable.getDurableId());
                        Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                        aPosDurablePO = aDurable.getDurableProcessOperation();
                        break;
                }
            } else {
                log.info("currentPOFlag == FALSE");
                switch (durableCategory) {
                    case SP_DURABLECAT_CASSETTE:
                        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startDurable.getDurableId());
                        Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
                        aPosDurablePO = aCassette.getPreviousDurableProcessOperation();
                        break;
                    case SP_DURABLECAT_RETICLEPOD:
                        CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, startDurable.getDurableId());
                        Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod());
                        aPosDurablePO = aReticlePod.getPreviousDurableProcessOperation();
                        break;
                    case SP_DURABLECAT_RETICLE:
                        CimProcessDurable aDurable = baseCoreFactory.getBO(CimProcessDurable.class, startDurable.getDurableId());
                        Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                        aPosDurablePO = aDurable.getPreviousDurableProcessOperation();
                        break;
                }
            }

            Validations.check(null == aPosDurablePO, retCodeConfig.getNotFoundDurablePo());
            if (loop == 0 && aPosDurablePO != null) {
                actualStartInfo = aPosDurablePO.getActualStartInfo(true);
            }

            boolean isOnRoute = false;
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
            } catch (ServiceException e) {
                if (Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                    isOnRoute = true;
                }
            }
            if (isOnRoute) {
                log.info("durable is on route");
                //--------------------------//
                //   RouteID                //
                //--------------------------//
                CimProcessDefinition aMainPD = null;
                if (aPosDurablePO != null) {
                    aMainPD = aPosDurablePO.getMainProcessDefinition();
                }
                Validations.check(null == aMainPD, retCodeConfig.getNotFoundRoute());
                startOperationInfo.setProcessFlowID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));

                //--------------------------//
                //   PDID                   //
                //--------------------------//
                CimProcessDefinition aPD = aPosDurablePO.getProcessDefinition();
                Validations.check(null == aPD, retCodeConfig.getNotFoundProcessDefinition());
                startOperationInfo.setOperationID(ObjectIdentifier.build(aPD.getIdentifier(), aPD.getPrimaryKey()));

                //--------------------------//
                //   OperationNumber        //
                //--------------------------//
                startOperationInfo.setOperationNumber(aPosDurablePO.getOperationNumber());

                //--------------------------//
                //   PassCount              //
                //--------------------------//
                startOperationInfo.setPassCount(CimNumberUtils.intValue(aPosDurablePO.getPassCount()));
            }

            startDurable.setStartOperationInfo(startOperationInfo);
            startDurables.add(startDurable);
            loop++;
        }

        if (null != actualStartInfo) {
            Infos.DurableStartRecipe startRecipe = new Infos.DurableStartRecipe();
            startRecipe.setLogicalRecipeId(actualStartInfo.getAssignedLogicalRecipe());
            startRecipe.setMachineRecipeId(actualStartInfo.getAssignedMachineRecipe());
            startRecipe.setPhysicalRecipeId(actualStartInfo.getAssignedPhysicalRecipe());
            List<Infos.StartRecipeParameter> startRecipeParameters = new ArrayList<>();
            startRecipe.setStartRecipeParameterS(startRecipeParameters);

            boolean skipFlag = false;
            List<ProcessDTO.StartRecipeParameterSetInfo> parameterSets = actualStartInfo.getAssignedRecipeParameterSets();
            if (CimArrayUtils.isNotEmpty(parameterSets)) {
                ProcessDTO.StartRecipeParameterSetInfo parameterSetInfo = parameterSets.get(0);
                if (parameterSets.size() == 1) {
                    int parameterSize = CimArrayUtils.getSize(parameterSetInfo.getRecipeParameterList());
                    if (parameterSize == 1) {
                        ProcessDTO.StartRecipeParameter parameter = parameterSetInfo.getRecipeParameterList().get(0);
                        if (CimStringUtils.isEmpty(parameter.getParameterName())
                                && CimStringUtils.isEmpty(parameter.getParameterValue())
                                && CimStringUtils.isEmpty(parameter.getTargetValue())) {
                            skipFlag = true;
                        }
                    }
                }

                if (!skipFlag) {
                    for (ProcessDTO.StartRecipeParameter parameter : parameterSetInfo.getRecipeParameterList()) {
                        Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                        startRecipeParameter.setUseCurrentSettingValueFlag(parameter.getUseCurrentSettingValueFlag());
                        startRecipeParameter.setTargetValue(parameter.getTargetValue());
                        startRecipeParameter.setParameterValue(parameter.getParameterValue());
                        startRecipeParameter.setParameterName(parameter.getParameterName());
                        startRecipeParameters.add(startRecipeParameter);
                    }
                }
            }

            retVal.setStrDurableStartRecipe(startRecipe);
        }
        return retVal;
    }

    @Override
    public ObjectIdentifier durableControlJobCreate(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String durableCategory, List<Infos.StartDurable> strStartDurables) {
        User user = objCommon.getUser();
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class,user.getUserID());
        /*--------------------------*/
        /*   Get Equipment Object   */
        /*--------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class,equipmentID);
        /*------------------------------------*/
        /*   Get New Durable Control Job ID   */
        /*------------------------------------*/
        String newID = aMachine.getNextControlJobIdentifier();
        /*--------------------------------*/
        /*   Create Durable Control Job   */
        /*--------------------------------*/
        CimDurableControlJob aDurableCJ;
        aDurableCJ = durableManager.createDurableControlJob(newID);
        if (CimObjectUtils.isEmpty(aDurableCJ)){
            throw new ServiceException(retCodeConfig.getNotFoundControlJob());
        }
        /*----------------------------------------------------------*/
        /*   Set Equipment ID and Category to Durable Control Job   */
        /*----------------------------------------------------------*/
        aDurableCJ.setMachine(aMachine);
        aDurableCJ.setDurableCategory(durableCategory);
        aDurableCJ.setOwner(aPerson);
        /*-------------------------------------------------*/
        /*   Prepare Start Durable Info for Control Job    */
        /*-------------------------------------------------*/
        List<DurableDTO.StartDurableInfo> startDurableInfos = new ArrayList<>();
        for (Infos.StartDurable startDurable : strStartDurables){
            Infos.StartDurablePort startDurablePort = startDurable.getStartDurablePort();
            DurableDTO.StartDurableInfo startDurableInfo = new DurableDTO.StartDurableInfo(startDurable.getDurableId(),
                                                                                 startDurablePort.getLoadPortID(),
                                                                                 startDurablePort.getUnloadPortID(),
                                                                                 startDurablePort.getLoadSequenceNumber(),
                                                                                 startDurablePort.getLoadPurposeType());
            startDurableInfos.add(startDurableInfo);
            if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)
                    && ObjectIdentifier.isEmptyWithValue(startDurablePort.getUnloadPortID())
                    && ObjectIdentifier.isNotEmptyWithValue(startDurablePort.getLoadPortID())) {
                PortResource aPs = aMachine.findPortResourceNamed(startDurablePort.getLoadPortID().getValue());
                CimPortResource aLoadPort = (CimPortResource) aPs;
                CimPortResource anUnloadPort = aLoadPort.getAssociatedPort();
                Validations.check(null == anUnloadPort, retCodeConfig.getNotFoundPort());
                startDurableInfo.setUnloadPortID(ObjectIdentifier.build(anUnloadPort.getIdentifier(), anUnloadPort.getPrimaryKey()));
            }
        }
        /*---------------------------------------*/
        /*   Set Start Cassette to Control Job   */
        /*---------------------------------------*/
        aDurableCJ.setStartDurableInfo(startDurableInfos);
        /*--------------------------*/
        /*   Set Return Structure   */
        /*--------------------------*/
        return ObjectIdentifier.build(aDurableCJ.getIdentifier(),aDurableCJ.getPrimaryKey());
    }

    @Override
    public void durableDurableControlJobIDSet(Infos.ObjCommon objCommon, ObjectIdentifier durableID, ObjectIdentifier durableControlJobID, String durableCategory) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        CimPerson aPerson = personManager.findPersonNamed(objCommon.getUser().getUserID().getValue());
        /*----------------------------------*/
        /*   Get DurableControlJob Object   */
        /*----------------------------------*/
        CimDurableControlJob aDurableControlJob = null;
        if (ObjectIdentifier.isNotEmptyWithValue(durableControlJobID)) {
            aDurableControlJob = baseCoreFactory.getBO(CimDurableControlJob.class, durableControlJobID);
        }
        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                break;
        }
        Validations.check(null == aDurable, retCodeConfig.getNotFoundDurable());
        if (aDurable != null) {
            aDurable.setDurableControlJob(aDurableControlJob);
            aDurable.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aDurable.setLastClaimedPerson(aPerson);
        }
    }

    @Override
    public void durableControlJobStatusChange(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID, String strDurableControlStatus) {
        CimDurableControlJob aDurableControlJob = baseCoreFactory.getBO(CimDurableControlJob.class,durableControlJobID);
        // Set durable control job status
        aDurableControlJob.setControlJobStatus(strDurableControlStatus);
        // Set last claimed userID
        aDurableControlJob.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        // Set last claimed userID
        aDurableControlJob.setLastClaimedUserID(objCommon.getUser().getUserID().getValue());

    }

    @Override
    public void durableControlJobDelete(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID, String actionType) {
        Validations.check(null == objCommon || null == durableControlJobID || null == actionType, retCodeConfig.getInvalidInputParam());
        /*----------------------------------*/
        /*   Get DurableControlJob Object   */
        /*----------------------------------*/
        CimDurableControlJob aDurableControlJob = baseCoreFactory.getBO(CimDurableControlJob.class, durableControlJobID);
        Validations.check(null == aDurableControlJob, retCodeConfig.getNotFoundControlJob());
        if (CimStringUtils.equals(SP_CONTROLJOBACTION_TYPE_DELETE, actionType)) {
            /*--------------------------*/
            /*   Get Machine Object     */
            /*--------------------------*/
            CimMachine aMachine = aDurableControlJob.getMachine();
            Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());
            /*--------------------------------------*/
            /*   Get PosStartDurableSequence Info   */
            /*--------------------------------------*/
            List<DurableDTO.StartDurableInfo> startDurableInfos = aDurableControlJob.getStartDurableInfo();
            /*-----------------------------*/
            /*   Get DurableCategory Info  */
            /*-----------------------------*/
            String durableCategory = aDurableControlJob.getDurableCategory();
            for (DurableDTO.StartDurableInfo startDurableInfo : startDurableInfos) {
                Durable aDurable = null;
                switch (durableCategory) {
                    case SP_DURABLECAT_CASSETTE:
                        aDurable = baseCoreFactory.getBO(CimCassette.class, startDurableInfo.getDurableID());
                        break;
                    case SP_DURABLECAT_RETICLEPOD:
                        aDurable = baseCoreFactory.getBO(CimReticlePod.class, startDurableInfo.getDurableID());
                        break;
                    case SP_DURABLECAT_RETICLE:
                        aDurable = baseCoreFactory.getBO(CimProcessDurable.class, startDurableInfo.getDurableID());
                        break;
                }
                Validations.check(null == aDurable, retCodeConfig.getNotFoundDurable());
                aDurable.setDurableControlJob(null);

                /*-------------------------------*/
                /*   Clear ControlJobID of Durable   */
                /*-------------------------------*/
                List<BufferResource> aBufferResourceList = aMachine.allBufferResources();
                if (!CimObjectUtils.isEmpty(aBufferResourceList)){
                    for (BufferResource bufferResource : aBufferResourceList){
                        Validations.check(null == bufferResource, retCodeConfig.getNotFoundBufferResource());
                        List<MaterialLocation> aMaterialLocationList = bufferResource.allMaterialLocations();
                        for (MaterialLocation materialLocation : aMaterialLocationList){
                            com.fa.cim.newcore.bo.machine.CimMaterialLocation aMaterialLocation = (com.fa.cim.newcore.bo.machine.CimMaterialLocation) materialLocation;
                            Validations.check(null == aMaterialLocation, retCodeConfig.getNotFoundMaterialLocation());
                            ObjectIdentifier containedCassetteID = null;
                            ObjectIdentifier allocatedCassetteID = null;
                            Material aMaterialLocationCassette = aMaterialLocation.getAllocatedMaterial();
                            if (aMaterialLocationCassette == null){
                                Material aContainedMaterial = aMaterialLocation.getMaterial();
                                if (aContainedMaterial != null){
                                    containedCassetteID = new ObjectIdentifier(aContainedMaterial.getIdentifier(), aContainedMaterial.getPrimaryKey());
                                }
                            } else {
                                //--- Get allocatedCassette
                                allocatedCassetteID = new ObjectIdentifier(aMaterialLocationCassette.getIdentifier(), aMaterialLocationCassette.getPrimaryKey());
                            }
                            if (!ObjectIdentifier.equalsWithValue(startDurableInfo.getDurableID(), containedCassetteID)
                                    && !ObjectIdentifier.equalsWithValue(startDurableInfo.getDurableID(), allocatedCassetteID)){
                                continue;
                            }
                            //---   Check Control Job of Material Location
                            CimDurableControlJob aMaterialLocationControlJob = aMaterialLocation.getDurableControlJob();
                            if (aMaterialLocationControlJob == null){
                                continue;
                            }
                            String materialControlJobIdent = aMaterialLocationControlJob.getIdentifier();
                            if (!ObjectIdentifier.equalsWithValue(durableControlJobID, materialControlJobIdent)){
                                //---  If controlJobID is differrent, try next one
                                continue;
                            }
                            /*---------------------------------------------*/
                            /*   Delete controlJob of MaterialLocation     */
                            /*---------------------------------------------*/
                            aMaterialLocation.setControlJob(null);
                        }
                    }
                }
            }
            //------------------------------//
            //   Delete DurableControlJob   //
            //------------------------------//
            durableManager.removeDurableControlJob(aDurableControlJob);
        } else {
            log.error("Invalid Action Type for CJ deletion is passed.");
            Validations.check(retCodeConfig.getInvalidParameter());
        }

    }
}
