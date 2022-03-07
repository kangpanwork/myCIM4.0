package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IDurableMethod;
import com.fa.cim.method.IProcessForDurableMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.newcore.bo.dispatch.DispatchingManager;
import com.fa.cim.newcore.bo.durable.*;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.Durable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.fa.cim.common.constant.BizConstant.*;

/**
 * <p>ProcessForDurableMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/6/17 14:27         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/6/17 14:27
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@OmMethod
@Slf4j
public class ProcessForDurableMethod implements IProcessForDurableMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private BaseCoreFactory baseCoreFactory;


    @Autowired
    private DispatchingManager dispatchingManager;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;

    @Override
    public Outputs.ProcessStartDurablesReserveInformationGetBaseInfoForClientOut processStartDurablesReserveInformationGetBaseInfoForClient(Infos.ObjCommon objCommon,
                                                                                                                                            ObjectIdentifier equipmentID,
                                                                                                                                            String durableCategory,
                                                                                                                                            List<ObjectIdentifier> durableIDs) {
        Validations.check(null == objCommon || null == equipmentID || null == durableCategory || null == durableIDs, retCodeConfig.getInvalidInputParam());
        Outputs.ProcessStartDurablesReserveInformationGetBaseInfoForClientOut retVal = new Outputs.ProcessStartDurablesReserveInformationGetBaseInfoForClientOut();

        /*--------------------*/
        /*   Get EQP Object   */
        /*--------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());

        /*---------------------------------------------*/
        /*   Set Return Structure's                    */
        /*---------------------------------------------*/
        retVal.setEquipmentID(equipmentID);
        retVal.setDurableCategory(durableCategory);

        List<Infos.StartDurable> startDurables = new ArrayList<>();
        retVal.setStrStartDurables(startDurables);

        /*----------------------------*/
        /*   Loop for durable         */
        /*----------------------------*/
        for (ObjectIdentifier durableID : durableIDs) {
            CimDurableProcessOperation durablePO = null;
            Infos.StartDurable startDurable = new Infos.StartDurable();

            switch (durableCategory) {
                case SP_DURABLECAT_CASSETTE:
                    log.info(">>> DurableCategory is Cassette");
                    /*===== Get Durable Object =====*/
                    CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
                    Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());

                    startDurable.setDurableId(ObjectIdentifier.build(aCassette.getIdentifier(), aCassette.getPrimaryKey()));
                    /*===== Get PO object =====*/
                    durablePO = aCassette.getDurableProcessOperation();
                    break;
                case SP_DURABLECAT_RETICLEPOD:
                    log.info("DurableCategory is ReticlePod");
                    /*===== Get Durable Object =====*/
                    CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                    Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod());

                    startDurable.setDurableId(ObjectIdentifier.build(aReticlePod.getIdentifier(), aReticlePod.getPrimaryKey()));
                    /*===== Get PO object =====*/
                    durablePO = aReticlePod.getDurableProcessOperation();
                    break;
                case SP_DURABLECAT_RETICLE:
                    log.info("DurableCategory is Reticle");
                    CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                    Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());

                    startDurable.setDurableId(ObjectIdentifier.build(aReticle.getIdentifier(), aReticle.getPrimaryKey()));
                    /*===== Get PO object =====*/
                    durablePO = aReticle.getDurableProcessOperation();
                    break;
                default:
                    break;
            }
            Validations.check(null == durablePO, retCodeConfig.getNotFoundDurablePo());

            /*-----------------*/
            /*   Set RouteID   */
            /*-----------------*/
            CimProcessDefinition aMainPD = durablePO.getMainProcessDefinition();
            Validations.check(null == aMainPD, retCodeConfig.getNotFoundRoute());

            Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
            startDurable.setStartOperationInfo(startOperationInfo);
            startOperationInfo.setProcessFlowID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));

            /*---------------------*/
            /*   Set OperationID   */
            /*---------------------*/
            CimProcessDefinition aPD = durablePO.getProcessDefinition();
            Validations.check(null == aPD, retCodeConfig.getNotFoundProcessDefinition());
            startOperationInfo.setOperationID(ObjectIdentifier.build(aPD.getIdentifier(), aPD.getPrimaryKey()));

            /*-------------------------*/
            /*   Set OperationNumber   */
            /*-------------------------*/
            startOperationInfo.setOperationNumber(durablePO.getOperationNumber());

            /*-------------------*/
            /*   Set PassCount   */
            /*-------------------*/
            startOperationInfo.setPassCount(CimNumberUtils.intValue(durablePO.getPassCount()));

            if (CimArrayUtils.isEmpty(startDurables)) {
                Infos.DurableStartRecipe durableStartRecipe = new Infos.DurableStartRecipe();
                /*===== get LogicalRecipe object and set objectIdentifier =====*/
                CimLogicalRecipe aLogicalRecipe = durablePO.findLogicalRecipeFor(null);
                Validations.check(null == aLogicalRecipe, retCodeConfig.getNotFoundLogicRecipe());
                durableStartRecipe.setLogicalRecipeId(ObjectIdentifier.build(aLogicalRecipe.getIdentifier(), aLogicalRecipe.getPrimaryKey()));

                /*===== get MachineRecipe object and set objectIdentifier =====*/
                CimMachineRecipe aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, null);
                Validations.check(null == aMachineRecipe, retCodeConfig.getNotFoundMachineRecipe());
                durableStartRecipe.setMachineRecipeId(ObjectIdentifier.build(aMachineRecipe.getIdentifier(), aMachineRecipe.getPrimaryKey()));

                /*===== get / set PhysicalRecipeID =====*/
                durableStartRecipe.setPhysicalRecipeId(aMachineRecipe.getPhysicalRecipeId());

                /*===== Get Recipe parameter =====*/
                List<RecipeDTO.RecipeParameter> recipeParameterSeqVar = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine, aMachineRecipe, null);
                if (CimArrayUtils.isNotEmpty(recipeParameterSeqVar)) {
                    List<Infos.StartRecipeParameter> startRecipeParameters = new ArrayList<>();
                    for (RecipeDTO.RecipeParameter data : recipeParameterSeqVar) {
                        Infos.StartRecipeParameter parameter = new Infos.StartRecipeParameter();
                        parameter.setParameterName(data.getParameterName());
                        if (!data.getUseCurrentValueFlag()) {
                            parameter.setParameterValue(data.getDefaultValue());
                        }
                        parameter.setTargetValue(data.getDefaultValue());
                        parameter.setUseCurrentSettingValueFlag(data.getUseCurrentValueFlag());
                        startRecipeParameters.add(parameter);
                    }
                    durableStartRecipe.setStartRecipeParameterS(startRecipeParameters);
                }

                retVal.setStrDurableStartRecipe(durableStartRecipe);
            }
            startDurables.add(startDurable);
        }
        return retVal;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessOperationListForDurableFromHistoryDRIn
     * @return java.util.List<com.fa.cim.dto.Infos.OperationNameAttributesFromHistory>
     * @throws
     * @author ho
     * @date 2020/6/22 13:21
     */
    public List<Infos.OperationNameAttributesFromHistory> processOperationListForDurableFromHistoryDR(Infos.ObjCommon strObjCommonIn, Infos.ProcessOperationListForDurableFromHistoryDRIn strProcessOperationListForDurableFromHistoryDRIn) {
        log.info("PPTManager_i::process_operationListForDurableFromHistoryDR");

        //-----------------------------------------------------------------------------
        // Check input parameter.
        //-----------------------------------------------------------------------------
        Validations.check(strProcessOperationListForDurableFromHistoryDRIn.getSearchCount() <= 0, retCodeConfig.getInvalidInputParam());

        //-----------------------------------------------------------------------------
        // OHDUROPE Select
        //-----------------------------------------------------------------------------
        String hFHDRBLOPEHSDRBL_CATEGORY = "";
        hFHDRBLOPEHSDRBL_CATEGORY = strProcessOperationListForDurableFromHistoryDRIn.getDurableCategory();

        String hFHDRBLOPEHSDURABLE_ID = "";
        hFHDRBLOPEHSDURABLE_ID = strProcessOperationListForDurableFromHistoryDRIn.getDurableID().getValue();

        List<Object[]> cFHDRBLOPEHS1 = cimJpaRepository.query("SELECT PROCESS_ID, STEP_ID, OPE_NO, OPE_PASS_COUNT, max(TRX_TIME) a, max(EVENT_CREATE_TIME) b, max(STORE_TIME) c\n" +
                        "        FROM OHDUROPE\n" +
                        "        WHERE DRBL_ID    = ?\n" +
                        "        AND DRBL_CATEGORY = ?\n" +
                        "        AND OPE_PASS_COUNT > 0\n" +
                        "        AND MOVE_TYPE    != 'BackwardOperation'\n" +
                        "        AND MOVE_TYPE    != 'BackwardStage'\n" +
                        "        GROUP BY PROCESS_ID, STEP_ID, OPE_NO, OPE_PASS_COUNT\n" +
                        "        ORDER BY a desc, b desc, c desc", hFHDRBLOPEHSDURABLE_ID,
                hFHDRBLOPEHSDRBL_CATEGORY);

        int t_len = 1000;
        int aRowIndex = 0;
        List<Infos.OperationNameAttributesFromHistory> strProcessOperationListForDurableFromHistoryDROut = new ArrayList<>(t_len);

        for (Object[] objects : cFHDRBLOPEHS1) {
            //-----------------------------------------------------------------------------
            // searchCount Over Check
            //-----------------------------------------------------------------------------
            if (aRowIndex == strProcessOperationListForDurableFromHistoryDRIn.getSearchCount()) {
                break;
            }

            //-----------------------------------------------------------------------------
            // OHDUROPE Fetch
            //-----------------------------------------------------------------------------
            String hFHDRBLOPEHSMAINPD_ID = "";
            String hFHDRBLOPEHSPD_ID = "";
            String hFHDRBLOPEHSOPE_NO = "";
            Integer hFHDRBLOPEHSOPE_PASS_COUNT = 0;
            String hFHDRBLOPEHSCLAIM_TIME = "";
            String hFHDRBLOPEHSEVENT_CREATE_TIME = "";
            String hFHDRBLOPEHSSTORE_TIME = "";

            hFHDRBLOPEHSMAINPD_ID = CimObjectUtils.toString(objects[0]);
            hFHDRBLOPEHSPD_ID = CimObjectUtils.toString(objects[1]);
            hFHDRBLOPEHSOPE_NO = CimObjectUtils.toString(objects[2]);
            hFHDRBLOPEHSOPE_PASS_COUNT = CimNumberUtils.intValue(objects[3]);
            hFHDRBLOPEHSCLAIM_TIME = CimObjectUtils.toString(objects[4]);
            hFHDRBLOPEHSEVENT_CREATE_TIME = CimObjectUtils.toString(objects[5]);
            hFHDRBLOPEHSSTORE_TIME = CimObjectUtils.toString(objects[6]);

            if (aRowIndex >= t_len) {
                t_len = t_len + 500;
            }

            log.info("", "******* aRowIndex *******", aRowIndex);
            log.info("", "FETCH DRBL_ID", hFHDRBLOPEHSDURABLE_ID);
            log.info("", "FETCH DRBL_CATEGORY", hFHDRBLOPEHSDRBL_CATEGORY);
            log.info("", "FETCH PROCESS_ID", hFHDRBLOPEHSMAINPD_ID);
            log.info("", "FETCH STEP_ID", hFHDRBLOPEHSPD_ID);
            log.info("", "FETCH OPE_NO", hFHDRBLOPEHSOPE_NO);
            log.info("", "FETCH OPE_PASS_COUNT", hFHDRBLOPEHSOPE_PASS_COUNT);
            log.info("", "FETCH TRX_TIME", hFHDRBLOPEHSCLAIM_TIME);
            log.info("", "FETCH EVENT_CREATE_TIME", hFHDRBLOPEHSEVENT_CREATE_TIME);
            log.info("", "FETCH STORE_TIME", hFHDRBLOPEHSSTORE_TIME);

            //-----------------------------------------------------------------------------
            // FRPD Select
            //-----------------------------------------------------------------------------
            String hFRPDOPE_NAME = "";
            String hFRPDPD_LEVEL = "";
            hFRPDPD_LEVEL = BizConstant.SP_PD_FLOWLEVEL_OPERATION;

            Object[] objects1 = cimJpaRepository.queryOne("SELECT OPE_NAME, PRP_ID\n" +
                            "            FROM OMPRP\n" +
                            "            WHERE PRP_ID = ?\n" +
                            "            AND PRP_LEVEL = ?", hFHDRBLOPEHSPD_ID,
                    hFRPDPD_LEVEL);

            if (objects1 != null) {
                hFRPDOPE_NAME = CimObjectUtils.toString(objects1[0]);
            }

            log.info("", "SELECT DATA OPE_NAME", hFRPDOPE_NAME);

            strProcessOperationListForDurableFromHistoryDROut.add(new Infos.OperationNameAttributesFromHistory());
            strProcessOperationListForDurableFromHistoryDROut.get(aRowIndex).setSeqno(aRowIndex * 1L);
            strProcessOperationListForDurableFromHistoryDROut.get(aRowIndex).setRouteID(ObjectIdentifier.buildWithValue(hFHDRBLOPEHSMAINPD_ID));
            strProcessOperationListForDurableFromHistoryDROut.get(aRowIndex).setOperationID(ObjectIdentifier.buildWithValue(hFHDRBLOPEHSPD_ID));
            strProcessOperationListForDurableFromHistoryDROut.get(aRowIndex).setOperationNumber(hFHDRBLOPEHSOPE_NO);
            strProcessOperationListForDurableFromHistoryDROut.get(aRowIndex).setOperationName(hFRPDOPE_NAME);
            strProcessOperationListForDurableFromHistoryDROut.get(aRowIndex).setOperationPass(CimObjectUtils.toString(hFHDRBLOPEHSOPE_PASS_COUNT));

            aRowIndex++;
        }
        log.info("", "count=", aRowIndex);

        log.info("PPTManager_i::process_operationListForDurableFromHistoryDR");
        return strProcessOperationListForDurableFromHistoryDROut;
    }

    @Override
    public void processStartDurablesReserveInformationSet(Infos.ObjCommon objCommon, Inputs.ProcessStartDurablesReserveInformationSetIn paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());

        CimDurableControlJob aDurableControlJob = baseCoreFactory.getBO(CimDurableControlJob.class, paramIn.getDurableControlJobID());
        Validations.check(null == aDurableControlJob, retCodeConfig.getNotFoundDctrljob());

        Optional.ofNullable(paramIn.getStrStartDurables()).ifPresent(list -> list.forEach(startDurable -> {
            CimDurableProcessOperation aDurablePO = null;
            switch (paramIn.getDurableCategory()) {
                case SP_DURABLECAT_CASSETTE:
                    log.info("durableCategory is Cassette");
                    CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startDurable.getDurableId());
                    Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
                    aDurablePO = aCassette.getDurableProcessOperation();
                    break;
                case SP_DURABLECAT_RETICLEPOD:
                    log.info("durableCategory is ReticlePod");
                    CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, startDurable.getDurableId());
                    Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod());
                    aDurablePO = aReticlePod.getDurableProcessOperation();
                    break;
                case SP_DURABLECAT_RETICLE:
                    log.info("durableCategory is Reticle");
                    CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, startDurable.getDurableId());
                    Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());
                    aDurablePO = aReticle.getDurableProcessOperation();
                    break;
                default:
                    // do nothing
                    break;
            }
            Validations.check(null == aDurablePO, retCodeConfig.getNotFoundDurablePo());

            ProcessDTO.ActualStartInformationForPO actualStartInfo = new ProcessDTO.ActualStartInformationForPO();

            /*===== set Machine Related Info =====*/
            CimMachine machine = baseCoreFactory.getBO(CimMachine.class, paramIn.getEquipmentID());
            Validations.check(null == machine, retCodeConfig.getNotFoundMachine());

            actualStartInfo.setAssignedMachine(ObjectIdentifier.build(machine.getIdentifier(), machine.getPrimaryKey()));
            actualStartInfo.setAssignedPortGroup(paramIn.getPortGroupID());

            /*===== set Recipe Info =====*/
            actualStartInfo.setAssignedLogicalRecipe(paramIn.getStrDurableStartRecipe().getLogicalRecipeId());
            actualStartInfo.setAssignedMachineRecipe(paramIn.getStrDurableStartRecipe().getMachineRecipeId());
            actualStartInfo.setAssignedPhysicalRecipe(paramIn.getStrDurableStartRecipe().getPhysicalRecipeId());

            /*---------------------------------------*/
            /*   Prepare Recipe Parameter Set Info   */
            /*---------------------------------------*/
            List<ProcessDTO.StartRecipeParameterSetInfo> rParmSets = new ArrayList<>();

            List<Infos.StartRecipeParameter> startRecipeParameters = paramIn.getStrDurableStartRecipe().getStartRecipeParameterS();
            if (CimArrayUtils.isEmpty(startRecipeParameters)) {
                ProcessDTO.StartRecipeParameterSetInfo parameterSetInfo = new ProcessDTO.StartRecipeParameterSetInfo();
                parameterSetInfo.setSetNumber(1);

                List<ProcessDTO.StartRecipeParameter> recipeParameterList = new ArrayList<>();
                parameterSetInfo.setRecipeParameterList(recipeParameterList);

                ProcessDTO.StartRecipeParameter parameter = new ProcessDTO.StartRecipeParameter();
                parameter.setUseCurrentSettingValueFlag(false);
                recipeParameterList.add(parameter);

                rParmSets.add(parameterSetInfo);
            } else {
                ProcessDTO.StartRecipeParameterSetInfo parameterSetInfo = new ProcessDTO.StartRecipeParameterSetInfo();
                parameterSetInfo.setSetNumber(1);

                List<ProcessDTO.StartRecipeParameter> recipeParameterList = new ArrayList<>();
                parameterSetInfo.setRecipeParameterList(recipeParameterList);
                for (Infos.StartRecipeParameter startRecipeParameter : startRecipeParameters) {
                    ProcessDTO.StartRecipeParameter parameter = new ProcessDTO.StartRecipeParameter();
                    parameter.setParameterName(startRecipeParameter.getParameterName());
                    parameter.setParameterValue(startRecipeParameter.getParameterValue());
                    parameter.setTargetValue(startRecipeParameter.getTargetValue());
                    parameter.setUseCurrentSettingValueFlag(startRecipeParameter.getUseCurrentSettingValueFlag());
                    recipeParameterList.add(parameter);
                }
                rParmSets.add(parameterSetInfo);
            }

            /*===== set Recipe Parameter Info =====*/
            actualStartInfo.setAssignedRecipeParameterSets(rParmSets);

            /*===== set prepared data to PO =====*/
            aDurablePO.setActualStartInfo(actualStartInfo);

            /*===== set durableControlJobID to PO =====*/
            aDurablePO.setAssignedDurableControlJob(aDurableControlJob);

            /*===== Set theActualStartTimeStamp =====*/
            aDurablePO.setActualStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        }));
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessStartDurablesReserveInformationClearin
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/24 15:06
     */
    public void processStartDurablesReserveInformationClear(
            Infos.ObjCommon strObjCommonIn,
            Infos.ProcessStartDurablesReserveInformationClearIn strProcessStartDurablesReserveInformationClearin) {
        log.info("PPTManager_i::process_startDurablesReserveInformation_Clear");

        //--------------//
        //  Initialize  //
        //--------------//
        Infos.ProcessStartDurablesReserveInformationClearIn strInParm = strProcessStartDurablesReserveInformationClearin;

        //Trace InParameters
        log.info("{} {}", "in-parm durableCategory      ", strInParm.getDurableCategory());

        for (int iCnt1 = 0; iCnt1 < CimArrayUtils.getSize(strInParm.getStrStartDurables()); iCnt1++) {
            log.info("{} {}", "loop to strInParm.strStartDurables.length()", iCnt1);

            CimDurableProcessOperation aDurablePO = null;
            if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
                log.info("{}", "durableCategory is Cassette");
                CimCassette aCassette;
                aCassette = baseCoreFactory.getBO(CimCassette.class,
                        strInParm.getStrStartDurables().get(iCnt1).getDurableId());

                aDurablePO = aCassette.getDurableProcessOperation();
            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                log.info("{}", "durableCategory is ReticlePod");
                CimReticlePod aReticlePod;
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                        strInParm.getStrStartDurables().get(iCnt1).getDurableId());

                aDurablePO = aReticlePod.getDurableProcessOperation();
            } else if (CimStringUtils.equals(strInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
                log.info("{}", "durableCategory is Reticle");
                CimProcessDurable aReticle;
                aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                        strInParm.getStrStartDurables().get(iCnt1).getDurableId());
                aDurablePO = aReticle.getDurableProcessOperation();
            }

            Validations.check(null == aDurablePO, retCodeConfig.getNotFoundDurablePo(), strInParm.getStrStartDurables().get(iCnt1).getDurableId().getValue());
            ProcessDTO.ActualStartInformationForPO actualStartInfo = new ProcessDTO.ActualStartInformationForPO();
            actualStartInfo.setAssignedDataCollectionFlag(false);
            aDurablePO.setActualStartInfo(actualStartInfo);
            aDurablePO.setAssignedDurableControlJob(null);
            aDurablePO.setActualStartTimeStamp(null);
        }
    }

    @Override
    public void durableProcessActualCompInformationSet(Infos.ObjCommon objCommon, String durableCategory, List<Infos.StartDurable> startDurables) {
        Validations.check(null == objCommon || null == durableCategory, retCodeConfig.getInvalidInputParam());
        Optional.ofNullable(startDurables).ifPresent(list -> list.forEach(startDurable -> {
            Durable aDurable = null;
            switch (durableCategory) {
                case SP_DURABLECAT_CASSETTE:
                    aDurable = baseCoreFactory.getBO(CimCassette.class, startDurable.getDurableId());
                    break;
                case SP_DURABLECAT_RETICLEPOD:
                    aDurable = baseCoreFactory.getBO(CimReticlePod.class, startDurable.getDurableId());
                    break;
                case SP_DURABLECAT_RETICLE:
                    aDurable = baseCoreFactory.getBO(CimProcessDurable.class, startDurable.getDurableId());
                    break;
            }
            Validations.check(null == aDurable, retCodeConfig.getNotFoundDurable());
            CimDurableProcessOperation aDurablePO = aDurable.getDurableProcessOperation();
            Validations.check(null == aDurablePO, retCodeConfig.getNotFoundDurablePo());

            aDurablePO.setActualCompTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

            CimMachine aMachine = aDurablePO.getAssignedMachine();
            Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());
        }));
    }

    @Override
    public Boolean durableProcessMove(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableId) {
        Validations.check(null == objCommon || null == durableCategory || null == durableId, retCodeConfig.getInvalidInputParam());
        log.info("in-parm durableCategory  : " + durableCategory);
        log.info("in-parm durableID        : " + ObjectIdentifier.fetchValue(durableId));

        if (!CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        boolean retVal = false;
        boolean onRouteFlag = false;

        try {
            durableMethod.durableOnRouteCheck(objCommon, durableCategory, durableId);
        } catch (ServiceException e) {
            if (Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                onRouteFlag = true;
                dispatchingManager.removeFromDurableQueue(durableCategory, durableId);
            }
        }

        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());

        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableId);
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableId);
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableId);
                break;
        }
        Validations.check(null == aDurable, retCodeConfig.getNotFoundDurable());

        aDurable.beginNextDurableProcessOperation();

        if (onRouteFlag) {
            CimDurableProcessOperation aDurablePO = aDurable.getDurableProcessOperation();
            boolean autoBankInFlag = false;
            boolean bankInRequiredFlag = false;

            if (null != aDurablePO) {
                autoBankInFlag = aDurablePO.isAutoBankInRequired();
            }

            if (autoBankInFlag) {
                bankInRequiredFlag = aDurable.isBankInRequired();
            }
            retVal = autoBankInFlag && bankInRequiredFlag;

            boolean addToQueueErrFlag = false;
            if (TransactionIDEnum.equals(TransactionIDEnum.DURABLE_GATE_PASS_REQ, objCommon.getTransactionID())) {
                dispatchingManager.addToDurableQueue(durableCategory, durableId);
            } else {
                try {
                    dispatchingManager.addToDurableQueue(durableCategory, durableId);
                } catch (ServiceException e) {
                    addToQueueErrFlag = true;
                }
            }

            aDurable.changeProductionStateBy(objCommon.getTimeStamp().getReportTimeStamp(), aPerson);
            if (addToQueueErrFlag) {
                Validations.check(retCodeConfig.getAddToQueueFail());
            }
        }

        aDurable.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aDurable.setLastClaimedPerson(aPerson);
        return retVal;
    }

    @Override
    public Outputs.ObjProcessLagTimeGetOut processDurableProcessLagTimeGet(Infos.ObjCommon objCommon, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm) {
        Outputs.ObjProcessLagTimeGetOut objProcessLagTimeGetOut = new Outputs.ObjProcessLagTimeGetOut();
        log.info("PPTManager_i::process_durableProcessLagTime_Get");

        //---------------------------------
        //   Get Previous PO Object
        //---------------------------------
        CimDurableProcessOperation cimDurableProcessOperation = null;
        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, durableProcessLagTimeUpdateReqInParm.getDurableID());
            Validations.check(cassette == null, retCodeConfig.getNotFoundCassette());
            cimDurableProcessOperation = cassette.getPreviousDurableProcessOperation();
        }

        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");
            CimReticlePod reticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableProcessLagTimeUpdateReqInParm.getDurableID());
            cimDurableProcessOperation = reticlePod.getPreviousDurableProcessOperation();
        }

        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is Reticle");
            CimProcessDurable reticle = baseCoreFactory.getBO(CimProcessDurable.class, durableProcessLagTimeUpdateReqInParm.getDurableID());
            cimDurableProcessOperation = reticle.getPreviousDurableProcessOperation();
        }


        if (cimDurableProcessOperation == null) {
            Validations.check(true, retCodeConfig.getNotFoundDurablePo(), durableProcessLagTimeUpdateReqInParm.getDurableID());
        }    //------------------------------------
        //   Get ProcessLagTime Information
        //------------------------------------
        ProcessDTO.ProcessLagTimeData procLagTimeData = cimDurableProcessOperation.findDefaultProcessLagTimeData();
        //---------------------------------------------
        //   Set ProcessLagTime to Return Structure
        //---------------------------------------------
        objProcessLagTimeGetOut.setExpriedTimeDuration(procLagTimeData.getExpiredTimeDuration());
        if (CimNumberUtils.longValue(procLagTimeData.getExpiredTimeDuration()) == 0) {
            objProcessLagTimeGetOut.setProcessLagTimeStamp(CimDateUtils.convertToOrInitialTime(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING));
        } else {
            long times = System.currentTimeMillis() + objProcessLagTimeGetOut.getExpriedTimeDuration().longValue();
            objProcessLagTimeGetOut.setProcessLagTimeStamp(new Timestamp(times));
        }
        return objProcessLagTimeGetOut;
    }

    @Override
    public void DurableProcessLagTimeSet(Infos.ObjCommon objCommon, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm, Outputs.ObjProcessLagTimeGetOut objProcessLagTimeGetOut) {

        log.info("PPTManager_i::durable_processLagTime_Set");

        //---------------------------------
        //   Get Previous PO Object
        //---------------------------------
        CimDurableProcessOperation cimDurableProcessOperation = null;
        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, durableProcessLagTimeUpdateReqInParm.getDurableID());
            Validations.check(cassette == null, retCodeConfig.getNotFoundCassette());
            cassette.setProcessLagTime(objProcessLagTimeGetOut.getProcessLagTimeStamp());
        }

        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");
            CimReticlePod reticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableProcessLagTimeUpdateReqInParm.getDurableID());
            reticlePod.setProcessLagTime(objProcessLagTimeGetOut.getProcessLagTimeStamp());
        }

        if (CimStringUtils.equals(durableProcessLagTimeUpdateReqInParm.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is Reticle");
            CimProcessDurable reticle = baseCoreFactory.getBO(CimProcessDurable.class, durableProcessLagTimeUpdateReqInParm.getDurableID());
            reticle.setProcessLagTime(objProcessLagTimeGetOut.getProcessLagTimeStamp());
        }
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessGetTargetOperationForDurablein
     * @return com.fa.cim.dto.Infos.ProcessRef
     * @throws
     * @author ho
     * @date 2020/6/28 15:17
     */
    public Infos.ProcessRef processGetTargetOperationForDurable(
            Infos.ObjCommon strObjCommonIn,
            Infos.ProcessGetTargetOperationForDurableIn strProcessGetTargetOperationForDurablein) {

        Infos.ProcessRef strProcessGetTargetOperationForDurableout = new Infos.ProcessRef();

        CimDurableProcessFlowContext durablePFX = null;

        if (CimStringUtils.equals(strProcessGetTargetOperationForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strProcessGetTargetOperationForDurablein.getDurableID());

            durablePFX = aCassette.getDurableProcessFlowContext();
        } else if (CimStringUtils.equals(strProcessGetTargetOperationForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strProcessGetTargetOperationForDurablein.getDurableID());

            durablePFX = aReticlePod.getDurableProcessFlowContext();
        } else if (CimStringUtils.equals(strProcessGetTargetOperationForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strProcessGetTargetOperationForDurablein.getDurableID());

            durablePFX = aReticle.getDurableProcessFlowContext();
        }

        Validations.check(null == durablePFX, retCodeConfig.getNotFoundPfx());

        CimProcessDefinition mainPD = baseCoreFactory.getBO(CimProcessDefinition.class, strProcessGetTargetOperationForDurablein.getRouteID());

        Validations.check(mainPD == null, retCodeConfig.getNotFoundRoute(), ObjectIdentifier.fetchValue(strProcessGetTargetOperationForDurablein.getDurableID()));

        //---------------------------------------------------------------------------------------
        //  Find the target ProcessOpeationSpecification using by routeID and operationNumber
        //---------------------------------------------------------------------------------------
        ProcessDTO.ProcessRef procRef = durablePFX.findProcessOperationSpecificationFor(
                strProcessGetTargetOperationForDurablein.getLocateDirection(),
                mainPD,
                strProcessGetTargetOperationForDurablein.getOperationNumber());

        Validations.check(null == procRef,
                retCodeConfigEx.getNotFoundPosForDurable(),
                strProcessGetTargetOperationForDurablein.getDurableID().getValue());

        if (CimStringUtils.length(procRef.getModulePOS()) != 0) {
            log.info("PPTManager_i::process_GetTargetOperationForDurable", "CIMFWStrLen(procRef->modulePOS) != 0");
            log.info("{} {}", "procRef->processFlow = ", procRef.getProcessFlow());
            log.info("{} {}", "procRef->processOperationSpecification = ", procRef.getProcessOperationSpecification());
            log.info("{} {}", "procRef->mainProcessFlow = ", procRef.getMainProcessFlow());
            log.info("{} {}", "procRef->moduleNumber = ", procRef.getModuleNumber());
            log.info("{} {}", "procRef->moduleProcessFlow = ", procRef.getModuleProcessFlow());
            log.info("{} {}", "procRef->modulePOS = ", procRef.getModulePOS());
            strProcessGetTargetOperationForDurableout.setProcessFlow(procRef.getProcessFlow());
            strProcessGetTargetOperationForDurableout.setProcessOperationSpecification(procRef.getProcessOperationSpecification());
            strProcessGetTargetOperationForDurableout.setMainProcessFlow(procRef.getMainProcessFlow());
            strProcessGetTargetOperationForDurableout.setModuleNumber(procRef.getModuleNumber());
            strProcessGetTargetOperationForDurableout.setModuleProcessFlow(procRef.getModuleProcessFlow());
            strProcessGetTargetOperationForDurableout.setModulePOS(procRef.getModulePOS());
        } else {
            log.info("PPTManager_i::process_GetTargetOperationForDurable", "CIMFWStrLen(procRef->modulePOS) == 0");
            Validations.check(true,
                    retCodeConfigEx.getNotFoundPosForDurable(),
                    strProcessGetTargetOperationForDurablein.getDurableID().getValue());
            log.info("PPTManager_i::process_GetTargetOperationForDurable");
        }

        return strProcessGetTargetOperationForDurableout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessLocateForDurablein
     * @return com.fa.cim.dto.Infos.ProcessLocateForDurableOut
     * @throws
     * @author ho
     * @date 2020/6/28 15:38
     */
    public Infos.ProcessLocateForDurableOut processLocateForDurable(
            Infos.ObjCommon strObjCommonIn,
            Infos.ProcessLocateForDurableIn strProcessLocateForDurablein) {

        Infos.ProcessLocateForDurableOut strProcessLocateForDurableout = new Infos.ProcessLocateForDurableOut();
        log.info("PPTManager_i::process_LocateForDurable");

        //--------------------------------------------------------------------------------------------------
        // (1) Preparation  & Set out parameter
        //--------------------------------------------------------------------------------------------------
        CimProcessFlow aMainPF, aModulePF;
        CimProcessOperationSpecification aModulePOS;
        String moduleNumber;

        log.info("{} {}", "in parameter strProcessRef.mainProcessFlow = ", strProcessLocateForDurablein.getStrProcessRef().getMainProcessFlow());
        aMainPF = baseCoreFactory.getBO(CimProcessFlow.class, strProcessLocateForDurablein.getStrProcessRef().getMainProcessFlow());
        Validations.check(aMainPF == null, retCodeConfig.getNotFoundPfForDurable(), "");

        aModulePF = baseCoreFactory.getBO(CimProcessFlow.class, strProcessLocateForDurablein.getStrProcessRef().getModuleProcessFlow());
        Validations.check(aModulePF == null, retCodeConfig.getNotFoundPfForDurable(), "");

        aModulePOS = baseCoreFactory.getBO(CimProcessOperationSpecification.class,
                strProcessLocateForDurablein.getStrProcessRef().getModulePOS());
        Validations.check(aModulePOS == null, retCodeConfigEx.getNotFoundPosForDurable(), strProcessLocateForDurablein.getDurableID().getValue());

        moduleNumber = strProcessLocateForDurablein.getStrProcessRef().getModuleNumber();
        Validations.check(CimStringUtils.isEmpty(moduleNumber), retCodeConfig.getNotFoundModuleNo(), "");

        CimDurableProcessOperation aDurablePO;
        ProcessDTO.PosProcessOperationEventData oldPOEventData;
        CimPerson aPerson;
        aPerson = baseCoreFactory.getBO(CimPerson.class, strObjCommonIn.getUser().getUserID());

        if (CimStringUtils.equals(strProcessLocateForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, strProcessLocateForDurablein.getDurableID());

            aDurablePO = aCassette.getDurableProcessOperation();
            Validations.check(aDurablePO == null, retCodeConfig.getNotFoundOperation());
            oldPOEventData = aDurablePO.getEventData();

            strProcessLocateForDurableout.setStrOldCurrentPOData(new Inputs.OldCurrentPOData());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setRouteID(oldPOEventData.getRouteID());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationNumber(oldPOEventData.getOperationNumber());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationID(oldPOEventData.getOperationID());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationPassCount(oldPOEventData.getOperationPassCount());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefPOS(oldPOEventData.getObjrefPOS());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefMainPF(oldPOEventData.getObjrefMainPF());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefModulePOS(oldPOEventData.getObjrefModulePOS());

            strProcessLocateForDurableout.setDurableHoldState(aCassette.getDurableHoldState());

            //--------------------------------------------------------------------------------------------------
            // (2) Remove aCassette from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            //Operation for Held Cassette
            if (!CimStringUtils.equals(strProcessLocateForDurableout.getDurableHoldState(), BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                dispatchingManager.removeFromDurableHoldQueue(strProcessLocateForDurablein.getDurableCategory(),
                        strProcessLocateForDurablein.getDurableID());
            }

            dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                    strProcessLocateForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (3) Locate the Cassette to the specified operation.
            //--------------------------------------------------------------------------------------------------
            aCassette.locateTo(aMainPF, moduleNumber, aModulePF, aModulePOS, strProcessLocateForDurablein.getSeqno());

            //--------------------------------------------------------------------------------------------------
            //  Check autoBankInRequired on target operation
            //--------------------------------------------------------------------------------------------------
            aDurablePO = aCassette.getDurableProcessOperation();

            Validations.check(aDurablePO == null, retCodeConfig.getNotFoundOperation());

            boolean autoBankInFlag = false;
            boolean bankInRequiredFlag = false;

            autoBankInFlag = aDurablePO.isAutoBankInRequired();

            if (autoBankInFlag) {
                bankInRequiredFlag = aCassette.isBankInRequired();
            }

            if (autoBankInFlag && bankInRequiredFlag) {
                strProcessLocateForDurableout.setAutoBankInFlag(true);
            } else {
                strProcessLocateForDurableout.setAutoBankInFlag(false);
            }

            //--------------------------------------------------------------------------------------------------
            // (4) Add a Cassette into new current dispatching queue.
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                    strProcessLocateForDurablein.getDurableID());

            //Operation for Held Cassette
            if (!CimStringUtils.equals(strProcessLocateForDurableout.getDurableHoldState(), BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                int holdEqpUpdateFlag = StandardProperties.OM_EQP_HOLD_UPDATE_MODE.getIntValue();
                if (holdEqpUpdateFlag != 0) {
                    dispatchingManager.addToDurableHoldQueue(strProcessLocateForDurablein.getDurableCategory(),
                            strProcessLocateForDurablein.getDurableID());

                    dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                            strProcessLocateForDurablein.getDurableID());
                }
            }

            try {
                aCassette.changeProductionStateBy(strObjCommonIn.getTimeStamp().getReportTimeStamp(), aPerson);
            } catch (CoreFrameworkException ex/*InvalidStateTransitionSignal*/) {
                Validations.check(retCodeConfig.getInvalidStateTrans());
            }

            //--------------------------------------------------------------------------------------------------
            // (6) Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aCassette.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

            aCassette.setLastClaimedPerson(aPerson);
        } else if (CimStringUtils.equals(strProcessLocateForDurablein.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strProcessLocateForDurablein.getDurableID());

            aDurablePO = aReticlePod.getDurableProcessOperation();


            Validations.check(null == aDurablePO, retCodeConfig.getNotFoundOperation());

            oldPOEventData = aDurablePO.getEventData();

            strProcessLocateForDurableout.getStrOldCurrentPOData().setRouteID(oldPOEventData.getRouteID());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationNumber(oldPOEventData.getOperationNumber());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationID(oldPOEventData.getOperationID());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationPassCount(oldPOEventData.getOperationPassCount());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefPOS(oldPOEventData.getObjrefPOS());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefMainPF(oldPOEventData.getObjrefMainPF());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefModulePOS(oldPOEventData.getObjrefModulePOS());

            strProcessLocateForDurableout.setDurableHoldState(aReticlePod.getDurableHoldState());

            //--------------------------------------------------------------------------------------------------
            // (2) Remove aReticlePod from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            //Operation for Held ReticlePod
            if (!CimStringUtils.equals(strProcessLocateForDurableout.getDurableHoldState(), SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                        strProcessLocateForDurablein.getDurableID());
            }

            dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                    strProcessLocateForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (3) Locate the ReticlePod to the specified operation.
            //--------------------------------------------------------------------------------------------------
            aReticlePod.locateTo(aMainPF, moduleNumber, aModulePF, aModulePOS, strProcessLocateForDurablein.getSeqno());

            //--------------------------------------------------------------------------------------------------
            //  Check autoBankInRequired on target operation
            //--------------------------------------------------------------------------------------------------
            aDurablePO = aReticlePod.getDurableProcessOperation();

            Validations.check(aDurablePO == null, retCodeConfig.getNotFoundOperation(), "");

            boolean autoBankInFlag = false;
            boolean bankInRequiredFlag = false;

            autoBankInFlag = aDurablePO.isAutoBankInRequired();

            if (autoBankInFlag) {
                bankInRequiredFlag = aReticlePod.isBankInRequired();
            }

            if (autoBankInFlag && bankInRequiredFlag) {
                strProcessLocateForDurableout.setAutoBankInFlag(true);
            } else {
                strProcessLocateForDurableout.setAutoBankInFlag(false);
            }

            //--------------------------------------------------------------------------------------------------
            // (4) Add a ReticlePod into new current dispatching queue.
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                    strProcessLocateForDurablein.getDurableID());

            //Operation for Held ReticlePod
            if (!CimStringUtils.equals(strProcessLocateForDurableout.getDurableHoldState(), SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                int holdEqpUpdateFlag = StandardProperties.OM_EQP_HOLD_UPDATE_MODE.getIntValue();
                if (holdEqpUpdateFlag != 0) {
                    dispatchingManager.addToDurableHoldQueue(strProcessLocateForDurablein.getDurableCategory(),
                            strProcessLocateForDurablein.getDurableID());

                    dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                            strProcessLocateForDurablein.getDurableID());
                }
            }

            try {
                aReticlePod.changeProductionStateBy(strObjCommonIn.getTimeStamp().getReportTimeStamp(), aPerson);
            } catch (CoreFrameworkException ex/*InvalidStateTransitionSignal*/) {
                Validations.check(true, retCodeConfig.getInvalidStateTrans());
            }

            //--------------------------------------------------------------------------------------------------
            // (6) Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aReticlePod.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

            aReticlePod.setLastClaimedPerson(aPerson);
        } else if (CimStringUtils.equals(strProcessLocateForDurablein.getDurableCategory(), SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strProcessLocateForDurablein.getDurableID());

            aDurablePO = aReticle.getDurableProcessOperation();

            Validations.check(aDurablePO == null, retCodeConfig.getNotFoundOperation(), "");

            oldPOEventData = aDurablePO.getEventData();

            strProcessLocateForDurableout.getStrOldCurrentPOData().setRouteID(oldPOEventData.getRouteID());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationNumber(oldPOEventData.getOperationNumber());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationID(oldPOEventData.getOperationID());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setOperationPassCount(oldPOEventData.getOperationPassCount());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefPOS(oldPOEventData.getObjrefPOS());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefMainPF(oldPOEventData.getObjrefMainPF());
            strProcessLocateForDurableout.getStrOldCurrentPOData().setObjrefModulePOS(oldPOEventData.getObjrefModulePOS());

            strProcessLocateForDurableout.setDurableHoldState(aReticle.getDurableHoldState());

            //--------------------------------------------------------------------------------------------------
            // (2) Remove aReticle from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            //Operation for Held Reticle
            if (!CimStringUtils.equals(strProcessLocateForDurableout.getDurableHoldState(), SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                        strProcessLocateForDurablein.getDurableID());
            }

            dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                    strProcessLocateForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (3) Locate the Reticle to the specified operation.
            //--------------------------------------------------------------------------------------------------
            aReticle.locateTo(aMainPF, moduleNumber, aModulePF, aModulePOS, strProcessLocateForDurablein.getSeqno());

            //--------------------------------------------------------------------------------------------------
            //  Check autoBankInRequired on target operation
            //--------------------------------------------------------------------------------------------------
            aDurablePO = aReticle.getDurableProcessOperation();

            Validations.check(aDurablePO == null, retCodeConfig.getNotFoundOperation(), "");

            boolean autoBankInFlag = false;
            boolean bankInRequiredFlag = false;

            autoBankInFlag = aDurablePO.isAutoBankInRequired();

            if (autoBankInFlag) {
                bankInRequiredFlag = aReticle.isBankInRequired();
            }

            if (autoBankInFlag && bankInRequiredFlag) {
                strProcessLocateForDurableout.setAutoBankInFlag(true);
            } else {
                strProcessLocateForDurableout.setAutoBankInFlag(false);
            }

            //--------------------------------------------------------------------------------------------------
            // (4) Add a Reticle into new current dispatching queue.
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                    strProcessLocateForDurablein.getDurableID());

            //Operation for Held Reticle
            if (!CimStringUtils.equals(strProcessLocateForDurableout.getDurableHoldState(), SP_DURABLE_HOLDSTATE_NOTONHOLD)) {
                int holdEqpUpdateFlag = StandardProperties.OM_EQP_HOLD_UPDATE_MODE.getIntValue();
                if (holdEqpUpdateFlag != 0) {
                    dispatchingManager.addToDurableHoldQueue(strProcessLocateForDurablein.getDurableCategory(),
                            strProcessLocateForDurablein.getDurableID());

                    dispatchingManager.removeFromDurableQueue(strProcessLocateForDurablein.getDurableCategory(),
                            strProcessLocateForDurablein.getDurableID());
                }
            }

            try {
                aReticle.changeProductionStateBy(strObjCommonIn.getTimeStamp().getReportTimeStamp(), aPerson);
            } catch (CoreFrameworkException ex/*InvalidStateTransitionSignal*/) {
                Validations.check(true, retCodeConfig.getInvalidStateTrans());
            }

            //--------------------------------------------------------------------------------------------------
            // (6) Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aReticle.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

            aReticle.setLastClaimedPerson(aPerson);
        }

        return strProcessLocateForDurableout;
    }

    @Override
    public Results.DurableOperationListInqResult processOperationListForDurableDR(Infos.ObjCommon objCommon, Params.ProcessOperationListForDurableDRInParam strProcess_OperationListForDurableDR_in) {
        Results.DurableOperationListInqResult strProcess_OperationListForDurableDR_out = new Results.DurableOperationListInqResult();
        strProcess_OperationListForDurableDR_out.setDurableCategory(strProcess_OperationListForDurableDR_in.getDurableCategory());
        strProcess_OperationListForDurableDR_out.setDurableID(strProcess_OperationListForDurableDR_in.getDurableID());
        Long branchCheckMode = 0l;
        String branchCheckMode_var = StandardProperties.OM_BRANCH_RETURN_ACTIVE_ROUTE.getValue();
        if (CimStringUtils.isNotEmpty(branchCheckMode_var)) {
            branchCheckMode = Long.valueOf(branchCheckMode_var);
        }
        if (branchCheckMode > 1) {
            branchCheckMode = 0l;
        }
        //-----------------------------------------------------------------------------
        // Check input parameter.
        //-----------------------------------------------------------------------------
        if (strProcess_OperationListForDurableDR_in.getSearchCount() <= 0) {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        String durableCategory = strProcess_OperationListForDurableDR_in.getDurableCategory();
        ObjectIdentifier durableID = strProcess_OperationListForDurableDR_in.getDurableID();

        long opeCount = 0l;
        int count, count2, count3 = 0;
        String tmpKey = null;
        long retSqlCode = 0l;
        String current_PO_obj = "";
        Infos.DurableOperationNameAttributes lastValue = new Infos.DurableOperationNameAttributes();
        List<Infos.DurableOperationNameAttributes> strDurableOperationNameAttributes = new ArrayList<>();
        strProcess_OperationListForDurableDR_out.setStrDurableOperationNameAttributes(strDurableOperationNameAttributes);
        //-----------------------------------------------------------------------------
        // (1) Get current process information
        //-----------------------------------------------------------------------------
        Durable durable = null;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            durable = baseCoreFactory.getBOByIdentifier(CimCassette.class, durableID.getValue());
            Validations.check(CimObjectUtils.isEmpty(durable), retCodeConfig.getNotFoundCassette());
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            durable = baseCoreFactory.getBOByIdentifier(CimReticlePod.class, durableID.getValue());
            Validations.check(CimObjectUtils.isEmpty(durable), retCodeConfig.getNotFoundCassette());
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            durable = baseCoreFactory.getBOByIdentifier(CimProcessDurable.class, durableID.getValue());
            Validations.check(CimObjectUtils.isEmpty(durable), retCodeConfig.getNotFoundCassette());
        }
        String hFRCASTDRBLPFX_OBJ = null;
        String hFRCASTDRBLPO_OBJ = null;
        if (!CimObjectUtils.isEmpty(durable.getDurableProcessFlowContext())) {
            hFRCASTDRBLPFX_OBJ = durable.getDurableProcessFlowContext().getPrimaryKey();
        }
        if (!CimObjectUtils.isEmpty(durable.getDurableProcessOperation())) {
            hFRCASTDRBLPO_OBJ = durable.getDurableProcessOperation().getPrimaryKey();
        }
        String hFRDRBLPFXd_theSystemKey = hFRCASTDRBLPFX_OBJ;
        String hFRDRBLPOd_theSystemKey = hFRCASTDRBLPO_OBJ;
        current_PO_obj = hFRCASTDRBLPO_OBJ;

        log.info("### Set current_PO_obj = ", current_PO_obj);

        if (CimObjectUtils.isEmpty(current_PO_obj)) {
            return strProcess_OperationListForDurableDR_out;
        }
        //--- Get current PO information
        String sql1 = "SELECT MPROCESS_PRSS_RKEY,\n" +
                "      ROUTE_PRSS_RKEY,\n" +
                "      MPROCESS_PRF_RKEY,\n" +
                "      MROUTE_PRF_RKEY,\n" +
                "      ROUTE_PRF_RKEY,\n" +
                "      ROUTE_NO,\n" +
                "      ROUTE_OPE_NO,\n" +
                "      OPE_NO" +
                "      FROM OMDRBLPROPE\n" +
                "      WHERE ID = ?1";
        Object[] FRDRBLPO = cimJpaRepository.queryOne(sql1, hFRDRBLPOd_theSystemKey);
        Validations.check(CimObjectUtils.isEmpty(FRDRBLPO), retCodeConfig.getNotFoundDurablePo());
        String current_POS_obj = CimObjectUtils.toString(FRDRBLPO[0]);
        String current_modulePOS_obj = CimObjectUtils.toString(FRDRBLPO[1]);
        String current_PF_obj = CimObjectUtils.toString(FRDRBLPO[2]);
        String current_mainPF_obj = CimObjectUtils.toString(FRDRBLPO[3]);
        String current_modulePF_obj = CimObjectUtils.toString(FRDRBLPO[4]);
        String current_moduleNo = CimObjectUtils.toString(FRDRBLPO[5]);
        String current_moduleOpeNo = CimObjectUtils.toString(FRDRBLPO[6]);
        String current_opeNo = CimObjectUtils.toString(FRDRBLPO[7]);
        String current_mainPF_systemkey;
        String current_modulePF_systemkey;

        if (CimObjectUtils.isEmpty(current_mainPF_obj)) {
            return strProcess_OperationListForDurableDR_out;
        }
        current_mainPF_systemkey = current_mainPF_obj;
        current_modulePF_systemkey = current_modulePF_obj;

        //--- Get main PF information
        String sql2 = "SELECT A.PRP_ID,\n" +
                "A.PRP_RKEY,\n" +
                "A.ACTIVE_FLAG,\n" +
                "B.PRP_TYPE\n" +
                "FROM OMPRF A, OMPRP B\n" +
                "WHERE A.ID = ?1 \n" +
                "AND A.PRP_ID = B.PRP_ID \n" +
                "AND B.PRP_LEVEL = 'Main'";
        Object[] FRDRBLPF = cimJpaRepository.queryOne(sql2, current_mainPF_systemkey);
        Validations.check(CimObjectUtils.isEmpty(FRDRBLPF), retCodeConfig.getNotFoundPfForDurable());
        String current_mainPDID = CimObjectUtils.toString(FRDRBLPF[0]);
        String current_mainPDOBJ = CimObjectUtils.toString(FRDRBLPF[1]);
        String current_mainPDTYPE = CimObjectUtils.toString(FRDRBLPF[3]);
        Long mainPF_state = CimNumberUtils.longValue((Number) FRDRBLPF[2]);

        //--- Get seq_no & stage of current module from main PF
        String sql3 = "SELECT IDX_NO,\n" +
                "      STAGE_ID,\n" +
                "      STAGE_RKEY\n" +
                "      FROM OMPRF_ROUTESEQ\n" +
                "      WHERE REFKEY = ?1 \n" +
                "      AND LINK_KEY  = ?2 ";
        Object[] FRPF_PDLIST = cimJpaRepository.queryOne(sql3, current_mainPF_systemkey, current_moduleNo);
        Validations.check(CimObjectUtils.isEmpty(FRPF_PDLIST), retCodeConfig.getNotFoundPfForDurable());
        Long current_mainPF_PDLIST_seqno = CimNumberUtils.longValue((Number) FRPF_PDLIST[0]);
        String current_stageID = CimObjectUtils.toString(FRPF_PDLIST[1]);
        String current_stageOBJ = CimObjectUtils.toString(FRPF_PDLIST[2]);

        //--- Get module PF information
        String sql4 = "SELECT PRP_ID, ACTIVE_FLAG\n" +
                "    FROM OMPRF\n" +
                "    WHERE ID = ?1 ";
        Object[] modulePF = cimJpaRepository.queryOne(sql4, current_modulePF_systemkey);
        Validations.check(CimObjectUtils.isEmpty(modulePF), retCodeConfig.getNotFoundPfForDurable());

        String current_modulePDID = CimObjectUtils.toString(modulePF[0]);
        Long modulePF_state = CimNumberUtils.longValue((Number) modulePF[1]);

        //--- Get seq_no of current process from module PF
        String sql5 = "SELECT IDX_NO\n" +
                "    FROM OMPRF_PRSSSEQ\n" +
                "    WHERE REFKEY = ?1\n" +
                "    AND LINK_KEY = ?2 ";
        Object[] FRPF_POSLIST = cimJpaRepository.queryOne(sql5, current_modulePF_systemkey, current_moduleOpeNo);
        Validations.check(CimObjectUtils.isEmpty(FRPF_POSLIST), retCodeConfig.getNotFoundPfForDurable());
        Long current_modulePF_POSLIST_seqno = CimNumberUtils.longValue((Number) FRPF_POSLIST[0]);

        //-----------------------------------------------------------------------------
        // (2) Set the current operation information
        //-----------------------------------------------------------------------------
        if (strProcess_OperationListForDurableDR_in.isCurrentFlag()) {
            log.info("Set the current operation information");
            Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
            strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
            strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
            strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(current_opeNo);
            strProcess_OperationListForDurable_HelperDR_out.setObjrefPO(current_PO_obj);
            strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(current_stageID, current_stageOBJ));
            Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
            strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
            tmpProcessRef.setProcessFlow(current_PF_obj);
            tmpProcessRef.setProcessOperationSpecification(current_POS_obj);
            tmpProcessRef.setMainProcessFlow(current_mainPF_obj);
            tmpProcessRef.setModuleNumber(current_moduleNo);
            tmpProcessRef.setModuleProcessFlow(current_modulePF_obj);
            tmpProcessRef.setModulePOS(current_modulePOS_obj);
            this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
            strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
            strProcess_OperationListForDurable_HelperDR_out.setObjrefPO("");
            opeCount++;
            lastValue = strProcess_OperationListForDurable_HelperDR_out;
        }
        if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
            return strProcess_OperationListForDurableDR_out;
        }

        //-----------------------------------------------------------------------------
        // (3) Get forward process information
        //-----------------------------------------------------------------------------
        String modulePF_obj = "";
        String modulePF_systemkey = "";
        Long modulePF_POSLIST_seqno = 0l;
        String hFRPFMAINPD_ID = "";
        String hFRPFPD_LEVEL = "";
        String active_modulePF_systemkey = "";
        String next_moduleOpeNo = "";
        String next_modulePOS_obj = "";
        String active_modulePF_obj = "";
        String hFRPF_POSLISTPOS_OBJ = "";
        boolean FIND_FLAG = false;
        Long hFRPF_POSLISTd_SeqNo = 0l;
        int i = 0;
        if (strProcess_OperationListForDurableDR_in.isSearchDirection()) {
            //-----------------------------------------------------------------------------
            // (3-1) Get next POS from current/active module PF
            //-----------------------------------------------------------------------------
            // If module PF is active, set next POS's seq_no from current module PF
            if (modulePF_state == 1) {
                modulePF_obj = current_modulePF_obj;
                modulePF_systemkey = current_modulePF_systemkey;
                modulePF_POSLIST_seqno = current_modulePF_POSLIST_seqno + 1;
                FIND_FLAG = true;
            } else {
                FIND_FLAG = false;
                //--- Get active module PF information
                //--- If module PDID's version is "##", convert it to real PDID
                String str_version_id = cimFrameWorkGlobals.extractVersionFromID(current_modulePDID);
                if (!CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                    hFRPFMAINPD_ID = current_modulePDID;
                } else {
                    String sql6 = "SELECT ACTIVE_VER_ID \n" +
                            "      FROM OMPRP \n" +
                            "      WHERE PRP_ID = ?1 \n" +
                            "      AND PRP_LEVEL = ?2 ";
                    Object FRPD = cimJpaRepository.queryOneColumnAndUnique(sql6, current_modulePDID, SP_PD_FLOWLEVEL_MODULE);
                    if (CimObjectUtils.isEmpty(FRPD)) {
                        Validations.check(retCodeConfig.getNotFoundProcessDefinition());
                    } else {
                        hFRPFMAINPD_ID = CimObjectUtils.toString(FRPD);
                    }
                }
                String sql7 = "SELECT ID \n" +
                        "    FROM OMPRF\n" +
                        "    WHERE PRP_ID = ?1\n" +
                        "    AND PRP_LEVEL  = ?2 \n" +
                        "    AND ACTIVE     = '1' ";
                Object FRPF = cimJpaRepository.queryOneColumnAndUnique(sql7, hFRPFMAINPD_ID, SP_PD_FLOWLEVEL_MODULE);
                if (CimObjectUtils.isEmpty(FRPF)) {
                    Validations.check(retCodeConfig.getNotFoundPfForDurable());
                } else {
                    active_modulePF_systemkey = CimObjectUtils.toString(FRPF);
                    active_modulePF_obj = CimObjectUtils.toString(FRPF);
                }
                i = 0;
                while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    // Search the next operation by finding the current module openo on active module PF
                    if (i == 0) {
                        hFRPF_POSLISTd_SeqNo = 0l;
                        String sql8 = "SELECT IDX_NO\n" +
                                "     FROM OMPRF_PRSSSEQ\n" +
                                "     WHERE REFKEY = ?1 " +
                                "     AND LINK_KEY    = ?2 ";
                        Object D_SEQNO = cimJpaRepository.queryOneColumnAndUnique(sql8, active_modulePF_systemkey, current_moduleOpeNo);
                        if (CimObjectUtils.isEmpty(D_SEQNO)) {
                            //--- Do nothing...
                        } else {
                            modulePF_obj = active_modulePF_obj;
                            modulePF_systemkey = active_modulePF_systemkey;
                            modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo + 1;
                            FIND_FLAG = true;
                            break;
                        }
                    } else {
                        // Search the next operation by finding the next module openo on active module PF
                        //--- Get the next operation seq_no on current module PF
                        current_modulePF_POSLIST_seqno++;
                        String sql9 = "SELECT LINK_KEY,  PRSS_RKEY\n" +
                                "     FROM OMPRF_PRSSSEQ\n" +
                                "     WHERE REFKEY = ?1 \n" +
                                "     AND IDX_NO  = ?2 ";
                        Object[] FRPF_POSLIST1 = cimJpaRepository.queryOne(sql7, current_modulePF_systemkey, current_modulePF_POSLIST_seqno);
                        if (CimObjectUtils.isEmpty(FRPF_POSLIST1)) {
                            // next operation is nil on current module PF
                            break;
                        } else {
                            //--- Keep the next operation information on current module PF
                            String hFRPFd_theSystemKey = CimObjectUtils.toString(FRPF_POSLIST1[0]);
                            String hFRPF_POSLISTd_key = CimObjectUtils.toString(FRPF_POSLIST1[2]);
                            //--- Search the corresponding next module openo from active module PF
                            String sql10 = "SELECT IDX_NO FROM OMPRF_PRSSSEQ\n" +
                                    "       WHERE REFKEY = ?1 \n" +
                                    "       AND LINK_KEY    = ?2 ";
                            Object hFRPF_POSLISTd_SeqNo1 = cimJpaRepository.queryOneColumnAndUnique(sql10, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                            if (!CimObjectUtils.isEmpty(hFRPF_POSLISTd_SeqNo1)) {
                                modulePF_obj = active_modulePF_obj;
                                modulePF_systemkey = active_modulePF_systemkey;
                                modulePF_POSLIST_seqno = CimNumberUtils.longValue((Number) hFRPF_POSLISTd_SeqNo1);
                                FIND_FLAG = true;
                                break;
                            } else {
                                //---------------------------------------------------------------------------
                                // Set next POS from inactive current module PF
                                //---------------------------------------------------------------------------
                                Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                                strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                                strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(current_moduleNo, hFRPF_POSLISTd_key));
                                strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(current_stageID, current_stageOBJ));
                                Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                tmpProcessRef.setProcessFlow(current_PF_obj);
                                tmpProcessRef.setProcessOperationSpecification("");
                                tmpProcessRef.setMainProcessFlow(current_mainPF_obj);
                                tmpProcessRef.setModuleNumber(current_moduleNo);
                                tmpProcessRef.setModuleProcessFlow(current_modulePF_obj);
                                tmpProcessRef.setModulePOS(current_modulePOS_obj);
                                this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                opeCount++;
                                if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                        && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                        && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                    log.info("The route and operation to search are found.");
                                    opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                }
                            }
                        }
                    }
                    i++;
                }
            }
            if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                return strProcess_OperationListForDurableDR_out;
            }
            //-----------------------------------------------------------------------------
            // Set next POS from active current module PF
            //-----------------------------------------------------------------------------
            if (FIND_FLAG) {

                String sql11 = "SELECT LINK_KEY, PRSS_RKEY\n" +
                        "      FROM OMPRF_PRSSSEQ\n" +
                        "      WHERE REFKEY = ?1 \n" +
                        "      AND IDX_NO >= ?2 \n" +
                        "      ORDER BY IDX_NO";
                List<Object[]> cFRPF_POSLISTS = cimJpaRepository.query(sql11, modulePF_systemkey, modulePF_POSLIST_seqno);
                count = 0;
                while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                    strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                    strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                    strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(current_stageID, current_stageOBJ));
                    Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                    strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                    tmpProcessRef.setProcessFlow(current_PF_obj);
                    tmpProcessRef.setMainProcessFlow(current_mainPF_obj);
                    tmpProcessRef.setModuleNumber(current_moduleNo);
                    tmpProcessRef.setModuleProcessFlow(current_modulePF_obj);
                    if (CimObjectUtils.isEmpty(cFRPF_POSLISTS) || count == cFRPF_POSLISTS.size()) {
                        break;
                    }
                    Object[] cFRPF_POSLIST1 = cFRPF_POSLISTS.get(count);
                    String hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST1[0]);
                    hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST1[1]);
                    //--- Set next operation information on active current module PF
                    strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(current_moduleNo, hFRPF_POSLISTd_key));
                    strProcess_OperationListForDurable_HelperDR_out.getProcessRef().setModulePOS(hFRPF_POSLISTPOS_OBJ);
                    strProcess_OperationListForDurable_HelperDR_out.getProcessRef().setProcessOperationSpecification("");
                    this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                    strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                    opeCount++;
                    count++;
                    if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                            && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                            && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                        log.info("The route and operation to search are found.");
                        opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                    }
                }
            }
            if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                return strProcess_OperationListForDurableDR_out;
            }
            //-----------------------------------------------------------------------------
            // (3-2) Get next module PD from current/active main PF
            //-----------------------------------------------------------------------------
            String mainPF_obj = "", mainPF_systemkey = "", active_mainPF_systemkey, active_mainPF_obj, next_modulePDID,
                    next_moduleNo, next_stageID, next_stageOBJ, hFRPF_POSLISTd_key, hFRPFd_theSystemKey, hFRPFPF_OBJ, hFRPF_PDLISTd_key,
                    hFRPF_PDLISTPD_ID = "", hFRPF_PDLISTMODULE_NO, hFRPF_PDLISTSTAGE_ID, hFRPF_PDLISTSTAGE_OBJ;
            Long mainPF_PDLIST_seqno = 0l, hFRPF_PDLISTd_SeqNo = 0l;
            // If main PF is active, set next module PD's seq_no from current main PF
            if (mainPF_state == 1) {
                mainPF_obj = current_mainPF_obj;
                mainPF_systemkey = current_mainPF_systemkey;
                mainPF_PDLIST_seqno = current_mainPF_PDLIST_seqno + 1;
                FIND_FLAG = true;
            } else {
                // If main PF is NOT active, search next module PD's seq_no from active main PF
                FIND_FLAG = false;
                //--- Get active main PF information
                String sql12 = "SELECT ID \n" +
                        "       FROM OMPRF\n" +
                        "       WHERE PRP_ID = ?1 \n" +
                        "       AND PRP_LEVEL  = ?2 \n" +
                        "       AND ACTIVE_FLAG     = '1' ";
                Object activeMainPF = cimJpaRepository.queryOneColumnAndUnique(sql12, current_mainPDID, SP_PD_FLOWLEVEL_MAIN_FOR_MODULE);
                Validations.check(CimObjectUtils.isEmpty(activeMainPF), retCodeConfig.getNotFoundPfForDurable());
                active_mainPF_systemkey = hFRPFd_theSystemKey = CimObjectUtils.toString(activeMainPF);
                active_mainPF_obj = hFRPFPF_OBJ = CimObjectUtils.toString(activeMainPF);
                i = 0;
                while (true) {
                    // Search the next module PD by finding the current module number on active main PF
                    if (i == 0) {
                        String sql13 = "SELECT IDX_NO, ROUTE_ID\n" +
                                "       FROM OMPRF_ROUTESEQ\n" +
                                "       WHERE REFKEY =  ?1\n" +
                                "       AND LINK_KEY  = ?2 ";
                        Object[] nextModulePD = cimJpaRepository.queryOne(sql13, hFRPFd_theSystemKey, current_moduleNo);
                        if (CimObjectUtils.isEmpty(nextModulePD)) {
                            // do nothing
                        } else {
                            hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) nextModulePD[0]);
                            hFRPF_PDLISTPD_ID = CimObjectUtils.toString(nextModulePD[1]);
                            mainPF_obj = active_mainPF_obj;
                            mainPF_systemkey = active_mainPF_systemkey;
                            mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo + 1;
                            FIND_FLAG = true;
                            break;
                        }
                    } else {
                        // Search the next module PD by finding the next module number on active main PF
                        //--- Get the next module PD's seq_no on current main PF
                        current_mainPF_PDLIST_seqno++;
                        hFRPF_PDLISTd_SeqNo = current_mainPF_PDLIST_seqno;

                        String sql14 = "SELECT ROUTE_ID,\n" +
                                "              ROUTE_NO,\n" +
                                "              STAGE_ID,\n" +
                                "              STAGE_RKEY\n" +
                                "              FROM OMPRF_ROUTESEQ\n" +
                                "              WHERE REFKEY = ?1 \n" +
                                "              AND IDX_ NO  = ?2 ";
                        Object[] nextModulePD = cimJpaRepository.queryOne(sql14, current_mainPF_systemkey, current_mainPF_PDLIST_seqno);
                        if (CimObjectUtils.isEmpty(nextModulePD)) {
                            // next module PD is nil
                            break;
                        } else {
                            //--- Keep the next module information on current main PF
                            //--- Search the corresponding next module number from the active main PF
                            next_modulePDID = CimObjectUtils.toString(nextModulePD[0]);
                            next_moduleNo = CimObjectUtils.toString(nextModulePD[1]);
                            next_stageID = CimObjectUtils.toString(nextModulePD[2]);
                            next_stageOBJ = CimObjectUtils.toString(nextModulePD[3]);
                            //--- Search the corresponding next module number from the active main PF
                            hFRPF_PDLISTd_SeqNo = 0l;
                            String sql15 = "SELECT IDX_NO\n" +
                                    "      FROM OMPRF_ROUTESEQ\n" +
                                    "      WHERE REFKEY = ?1 \n" +
                                    "      AND LINK_KEY    = ?2 ";
                            Object D_SEQNO = cimJpaRepository.queryOneColumnAndUnique(sql15, active_mainPF_systemkey, next_moduleNo);
                            if (!CimObjectUtils.isEmpty(D_SEQNO)) {
                                mainPF_obj = active_mainPF_obj;
                                mainPF_systemkey = active_mainPF_systemkey;
                                mainPF_PDLIST_seqno = CimNumberUtils.longValue((Number) D_SEQNO);
                                FIND_FLAG = true;
                                break;
                            } else {
                                //---------------------------------------------------------------------------
                                // Set next POS from active next module PF and current(inactive) main PF
                                //---------------------------------------------------------------------------
                                //--- Get active module PF information
                                //--- If module PDID's version is "##", convert it to real PDID
                                String str_version_id = cimFrameWorkGlobals.extractVersionFromID(next_modulePDID);
                                if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                                    String sql16 = "SELECT ACTIVE_VER_ID \n" +
                                            "       FROM OMPRP \n" +
                                            "       WHERE PRP_ID  = ?1 \n" +
                                            "       AND PRP_LEVEL = ?2 ";
                                    Object ACTIVE_ID = cimJpaRepository.queryOneColumnAndUnique(sql16, next_modulePDID, SP_PD_FLOWLEVEL_MODULE);
                                    Validations.check(CimObjectUtils.isEmpty(ACTIVE_ID), retCodeConfig.getMsgNotFoundPd());
                                    hFRPFMAINPD_ID = CimObjectUtils.toString(ACTIVE_ID);
                                } else {
                                    hFRPFMAINPD_ID = next_modulePDID;
                                }
                                String sql17 = "SELECT ID \n" +
                                        "       FROM OMPRF\n" +
                                        "       WHERE PRP_ID = ?1 \n" +
                                        "       AND PRP_LEVEL  = ?2 \n" +
                                        "       AND ACTIVE_FLAG     = '1' ";
                                Object PF = cimJpaRepository.queryOneColumnAndUnique(sql17, next_modulePDID, SP_PD_FLOWLEVEL_MODULE);
                                Validations.check(CimObjectUtils.isEmpty(PF), retCodeConfig.getNotFoundPfForDurable());
                                hFRPFd_theSystemKey = CimObjectUtils.toString(PF);
                                hFRPFPF_OBJ = CimObjectUtils.toString(PF);
                                String next_modulePF_systemkey = hFRPFd_theSystemKey;
                                String next_modulePF_obj = hFRPFPF_OBJ;

                                //--- Set next operation information on active module PF
                                hFRPFd_theSystemKey = next_modulePF_systemkey;
                                String sql18 = "SELECT LINK_KEY, PRSS_RKEY\n" +
                                        "       FROM OMPRF_PRSSSEQ\n" +
                                        "       WHERE REFKEY = ?1 \n" +
                                        "       ORDER BY IDX_NO ";
                                List<Object[]> FRPF_POSLISTS = cimJpaRepository.query(sql18, modulePF_systemkey, modulePF_POSLIST_seqno);
                                count = 0;
                                while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                                    Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                    strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                                    strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                                    strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(next_stageID, next_stageOBJ));
                                    Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                    strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                    tmpProcessRef.setProcessFlow(current_PF_obj);
                                    tmpProcessRef.setMainProcessFlow(current_mainPF_obj);
                                    tmpProcessRef.setModuleNumber(next_moduleNo);
                                    tmpProcessRef.setModuleProcessFlow(next_modulePF_obj);
                                    if (CimObjectUtils.isEmpty(FRPF_POSLISTS) || count == FRPF_POSLISTS.size()) {
                                        break;
                                    }

                                    Object[] cFRPF_POSLIST2 = FRPF_POSLISTS.get(count);
                                    hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST2[0]);
                                    hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST2[1]);
                                    //--- Set next operation information on active module PF
                                    strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(next_moduleNo, hFRPF_POSLISTd_key));
                                    strProcess_OperationListForDurable_HelperDR_out.getProcessRef().setModulePOS(hFRPF_POSLISTPOS_OBJ);
                                    strProcess_OperationListForDurable_HelperDR_out.getProcessRef().setProcessOperationSpecification("");
                                    this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                    strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                    opeCount++;
                                    count++;
                                    if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                            && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                            && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                        log.info("The route and operation to search are found.");
                                        opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                    }
                                }
                            }
                        }
                    }
                    i++;
                }
            }
            if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                return strProcess_OperationListForDurableDR_out;
            }
            //-----------------------------------------------------------------------------
            // Set next POS from active next module PF and active main PF
            //-----------------------------------------------------------------------------
            if (FIND_FLAG) {
                //--- Get next module pdid, module_no and stage from active main PF
                String sql19 = "SELECT ROUTE_ID,\n" +
                        "       ROUTE_NO,\n" +
                        "       STAGE_ID,\n" +
                        "       STAGE_RKEY\n" +
                        "       FROM OMPRF_ROUTESEQ\n" +
                        "       WHERE REFKEY = ?1 \n" +
                        "       AND IDX_NO >= ?2 \n" +
                        "       ORDER BY IDX_NO";
                List<Object[]> FRPF_POSLISTS = cimJpaRepository.query(sql19, mainPF_systemkey, mainPF_PDLIST_seqno);
                Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                tmpProcessRef.setProcessFlow(current_PF_obj);
                tmpProcessRef.setMainProcessFlow(mainPF_obj);
                count = 0;
                while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    if (CimObjectUtils.isEmpty(FRPF_POSLISTS) || count == FRPF_POSLISTS.size()) {
                        break;
                    }
                    Object[] cFRPF_PDLIST1 = FRPF_POSLISTS.get(count);
                    hFRPF_PDLISTPD_ID = CimObjectUtils.toString(cFRPF_PDLIST1[0]);
                    hFRPF_PDLISTMODULE_NO = CimObjectUtils.toString(cFRPF_PDLIST1[1]);
                    hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(cFRPF_PDLIST1[2]);
                    hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(cFRPF_PDLIST1[3]);

                    tmpProcessRef.setModuleNumber(hFRPF_PDLISTMODULE_NO);
                    strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(hFRPF_PDLISTSTAGE_ID, hFRPF_PDLISTSTAGE_OBJ));

                    //--- Get active module PF information
                    hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                    //--- If module PDID's version is "##", convert it to real PDID
                    String str_version_id = cimFrameWorkGlobals.extractVersionFromID(hFRPF_PDLISTPD_ID);
                    if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                        String sql20 = "SELECT ACTIVE_VER_ID \n" +
                                "       FROM OMPRP \n" +
                                "       WHERE PRP_ID  = ?1 \n" +
                                "       AND PRP_LEVEL = ?2 ";
                        Object hFRPDACTIVE_ID = cimJpaRepository.queryOneColumnAndUnique(sql20, hFRPF_PDLISTPD_ID, hFRPFPD_LEVEL);
                        Validations.check(CimObjectUtils.isEmpty(hFRPDACTIVE_ID), retCodeConfig.getNotFoundProcessDefinition());
                        hFRPFMAINPD_ID = CimObjectUtils.toString(hFRPDACTIVE_ID);
                    } else {
                        hFRPFMAINPD_ID = hFRPF_PDLISTPD_ID;
                    }
                    String sql21 = "SELECT ID \n" +
                            "       FROM OMPRF \n" +
                            "       WHERE PRP_ID = ?\n" +
                            "       AND PRP_LEVEL  = ?\n" +
                            "       AND ACTIVE_FLAG     = 1";
                    Object hFRPDACTIVE_ID = cimJpaRepository.queryOneColumnAndUnique(sql21, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                    Validations.check(CimObjectUtils.isEmpty(hFRPDACTIVE_ID), retCodeConfig.getSqlNotFound());
                    hFRPFd_theSystemKey = CimObjectUtils.toString(hFRPDACTIVE_ID);
                    hFRPFPF_OBJ = CimObjectUtils.toString(hFRPDACTIVE_ID);
                    //--- Set next operation information on active module PF
                    tmpProcessRef.setModuleProcessFlow(hFRPFPF_OBJ);
                    String sql22 = "SELECT LINK_KEY, PRSS_RKEY\n" +
                            "      FROM OMPRF_PRSSSEQ\n" +
                            "      WHERE REFKEY =  ?1 \n" +
                            "      ORDER BY IDX_NO";
                    List<Object[]> tmpFRPF_POSLIST = cimJpaRepository.query(sql22, hFRPFd_theSystemKey);
                    count2 = 0;
                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        if (CimObjectUtils.isEmpty(tmpFRPF_POSLIST) || count2 == tmpFRPF_POSLIST.size()) {
                            break;
                        }
                        Object[] cFRPF_POSLIST3 = tmpFRPF_POSLIST.get(count2);
                        hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST3[0]);
                        hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST3[1]);
                        Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out1 = new Infos.DurableOperationNameAttributes();
                        Infos.ProcessRef tmpProcessRef1 = new Infos.ProcessRef();
                        tmpProcessRef1.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                        tmpProcessRef1.setProcessFlow(current_PF_obj);
                        tmpProcessRef1.setMainProcessFlow(mainPF_obj);
                        tmpProcessRef1.setModuleNumber(hFRPF_PDLISTMODULE_NO);
                        tmpProcessRef1.setModuleProcessFlow(hFRPFPF_OBJ);
                        strProcess_OperationListForDurable_HelperDR_out1.setProcessRef(tmpProcessRef1);
                        strProcess_OperationListForDurable_HelperDR_out1.setRouteID(strProcess_OperationListForDurable_HelperDR_out.getRouteID());
                        strProcess_OperationListForDurable_HelperDR_out1.setStageID(strProcess_OperationListForDurable_HelperDR_out.getStageID());
                        strProcess_OperationListForDurable_HelperDR_out1.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(hFRPF_PDLISTMODULE_NO, hFRPF_POSLISTd_key));
                        opeCount++;
                        count2++;
                        this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out1);
                        strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out1);
                        lastValue = strProcess_OperationListForDurable_HelperDR_out1;
                        if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                            opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                        }
                    }
                    count++;
                }
            }
            if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                return strProcess_OperationListForDurableDR_out;
            }
            //---------------------------------------------------------------------------------------------------------------
            // (3-3) If the Durable is on sub/rework route, set the return operaion and the following operations from return flow
            //---------------------------------------------------------------------------------------------------------------
            String before_mainPDID = "", before_mainPDOBJ = "", before_mainPDTYPE = "";
            String sql23 = "SELECT IDX_NO,\n" +
                    "             OPE_NO,\n" +
                    "             MPROCESS_PRF_RKEY,\n" +
                    "             MROUTE_PRF_RKEY,\n" +
                    "             ROUTE_PRF_RKEY\n" +
                    "      FROM OMDRBLPRFCX_RTNSEQ\n" +
                    "      WHERE REFKEY = ?1 \n" +
                    "      ORDER BY IDX_NO";
            List<Object[]> cFRDRBLPFX_RETNLISTS = cimJpaRepository.query(sql23, hFRDRBLPFXd_theSystemKey);
            count = 0;
            while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                if (CimObjectUtils.isEmpty(cFRDRBLPFX_RETNLISTS) || count == cFRDRBLPFX_RETNLISTS.size()) {
                    break;
                }
                Object[] cFRDRBLPFX_RETNLIST = cFRDRBLPFX_RETNLISTS.get(count);
                int hFRDRBLPFX_RETNLISTd_SeqNo = CimNumberUtils.intValue((Number) cFRDRBLPFX_RETNLIST[0]);
                String hFRDRBLPFX_RETNLISTOPE_NO = CimObjectUtils.toString(cFRDRBLPFX_RETNLIST[1]);
                String hFRDRBLPFX_RETNLISTPF_OBJ = CimObjectUtils.toString(cFRDRBLPFX_RETNLIST[2]);
                String hFRDRBLPFX_RETNLISTMAIN_PF_OBJ = CimObjectUtils.toString(cFRDRBLPFX_RETNLIST[3]);
                String hFRDRBLPFX_RETNLISTMODULE_PF_OBJ = CimObjectUtils.toString(cFRDRBLPFX_RETNLIST[4]);
                //--- Keep return operation information
                Long return_seqNo = Long.valueOf(hFRDRBLPFX_RETNLISTd_SeqNo);
                String return_opeNo = hFRDRBLPFX_RETNLISTOPE_NO;
                String return_moduleNo = BaseStaticMethod.convertOpeNoToModuleNo(hFRDRBLPFX_RETNLISTOPE_NO);
                String return_moduleOpeNo = BaseStaticMethod.convertOpeNoToModuleOpeNo(hFRDRBLPFX_RETNLISTOPE_NO);
                String return_PF_obj = hFRDRBLPFX_RETNLISTPF_OBJ;
                String return_mainPF_obj = hFRDRBLPFX_RETNLISTMAIN_PF_OBJ;
                String return_modulePF_obj = hFRDRBLPFX_RETNLISTMODULE_PF_OBJ;
                String return_mainPF_systemkey = "";
                String return_modulePF_systemkey = "";
                if (CimStringUtils.isNotEmpty(hFRDRBLPFX_RETNLISTMAIN_PF_OBJ)) {
                    return_mainPF_systemkey = hFRDRBLPFX_RETNLISTMAIN_PF_OBJ;
                }
                if (CimStringUtils.isNotEmpty(hFRDRBLPFX_RETNLISTMODULE_PF_OBJ)) {
                    return_modulePF_systemkey = hFRDRBLPFX_RETNLISTMODULE_PF_OBJ;
                }
                String mainPDID = "", mainPDOBJ = "", mainPDTYPE = "";
                if (count == 0) {
                    mainPDID = current_mainPDID;
                    mainPDOBJ = current_mainPDOBJ;
                    mainPDTYPE = current_mainPDTYPE;
                } else {
                    mainPDID = before_mainPDID;
                    mainPDOBJ = before_mainPDOBJ;
                    mainPDTYPE = before_mainPDTYPE;
                }
                hFRPFd_theSystemKey = return_mainPF_systemkey;
                String sql24 = "SELECT A.PRP_ID,\n" +
                        "             A.PRP_RKEY,\n" +
                        "             B.PRP_TYPE\n" +
                        "             FROM OMPRF A, OMPRP B\n" +
                        "             WHERE A.ID = ?1 \n" +
                        "             AND A.PRP_ID = B.PRP_ID \n" +
                        "             AND B.PRP_LEVEL = 'Main'";
                Object[] FRPFFRPD = cimJpaRepository.queryOne(sql24, current_mainPF_systemkey);
                Validations.check(CimObjectUtils.isEmpty(FRPFFRPD), retCodeConfig.getNotFoundPfForDurable());
                hFRPFMAINPD_ID = CimObjectUtils.toString(FRPFFRPD[0]);
                String hFRPFMAINPD_OBJ = CimObjectUtils.toString(FRPFFRPD[1]);
                String hFRPDPD_TYPE = CimObjectUtils.toString(FRPFFRPD[2]);
                before_mainPDID = hFRPFMAINPD_ID;
                before_mainPDOBJ = hFRPFMAINPD_OBJ;
                before_mainPDTYPE = hFRPDPD_TYPE;
                String original_return_mainPF_obj = return_mainPF_obj;
                String original_return_modulePF_obj = return_modulePF_obj;
                String original_return_mainPF_systemkey = return_mainPF_systemkey;
                String original_return_modulePF_systemkey = return_modulePF_systemkey;
                boolean bSameModuleFlag = true;
                if (branchCheckMode == 1) {
                    log.info("###### Select from OMDRBLPRFCX_BCKPROPESEQ");
                    String sql25 = "SELECT PROPE_RKEY, RWKOUT_STRING\n" +
                            "      FROM OMDRBLPRFCX_BCKPROPESEQ\n" +
                            "      WHERE REFKEY = ?1 \n" +
                            "      AND IDX_NO  = ?2 ";
                    Object[] FRDRBLPFX_BACKPOLIST = cimJpaRepository.queryOne(sql25, hFRDRBLPFXd_theSystemKey, hFRDRBLPFX_RETNLISTd_SeqNo);
                    Validations.check(CimObjectUtils.isEmpty(FRDRBLPFX_BACKPOLIST), retCodeConfig.getNotFoundPfForDurable());
                    String hFRDRBLPFX_BACKPOLISTPO_OBJ = CimObjectUtils.toString(FRDRBLPFX_BACKPOLIST[0]);
                    String hFRDRBLPFX_BACKPOLISTREWORKOUT_KEY = CimObjectUtils.toString(FRDRBLPFX_BACKPOLIST[1]);
                    hFRDRBLPOd_theSystemKey = hFRDRBLPFX_BACKPOLISTPO_OBJ;
                    String backup_PO_obj = hFRDRBLPFX_BACKPOLISTPO_OBJ;
                    String sql26 = "SELECT ROUTE_NO\n" +
                            "      FROM OMDRBLPROPE\n" +
                            "      WHERE ID = ?1 ";
                    Object MODULE_NO = cimJpaRepository.queryOneColumnAndUnique(sql26, hFRDRBLPOd_theSystemKey);
                    Validations.check(CimObjectUtils.isEmpty(MODULE_NO), retCodeConfig.getNotFoundPfForDurable());
                    String hFRDRBLPOMODULE_NO = CimObjectUtils.toString(MODULE_NO);
                    String backup_moduleNo = hFRDRBLPOMODULE_NO;
                    if (!CimStringUtils.equals(mainPDTYPE, SP_MAINPDTYPE_DURABLEREWORK)) {
                        if (CimStringUtils.isNotEmpty(hFRDRBLPFX_BACKPOLISTREWORKOUT_KEY)) {
                            String reworkOutKey = hFRDRBLPFX_BACKPOLISTREWORKOUT_KEY;
                            int keyCount = CimStringUtils.count(reworkOutKey, ".");
                            Validations.check(keyCount != 3, retCodeConfig.getInvalidRouteId());
                        } else {
                            // get backup PO object
                            CimProcessOperation aBackupPO = baseCoreFactory.getBO(CimProcessOperation.class, backup_PO_obj);
                            Validations.check(CimObjectUtils.isEmpty(aBackupPO), retCodeConfig.getNotFoundOperation());
                            // get MainPF from backup PO
                            CimProcessFlow aMainPF = aBackupPO.getMainProcessFlow();
                            Validations.check(CimObjectUtils.isEmpty(aMainPF), retCodeConfig.getNotFoundProcessFlow());
                            // get Module number from backup PO
                            String aModuleNo = aBackupPO.getModuleNumber();
                            CimProcessFlow aModulePF = aBackupPO.getModuleProcessFlow();
                            Validations.check(CimObjectUtils.isEmpty(aModulePF), retCodeConfig.getNotFoundProcessFlow());
                            // get ModulePOS from backup PO
                            CimProcessOperationSpecification aModulePOS = aBackupPO.getModuleProcessOperationSpecification();
                            Validations.check(CimObjectUtils.isEmpty(aModulePOS), retCodeConfig.getNotFoundPos());
                            // get previous ModulePOS
                            AtomicReference<CimProcessFlow> outMainProcessFlow = new AtomicReference<>();
                            AtomicReference<CimProcessFlow> outModuleProcessFlow = new AtomicReference<>();
                            String outModuleNumber;
                            AtomicReference<String> chr_outModuleNumber = new AtomicReference<>();
                            CimProcessOperationSpecification aPrevPOS = aMainPF.getPreviousProcessOperationSpecificationFor(aModuleNo,
                                    aModulePF,
                                    aModulePOS,
                                    outMainProcessFlow,
                                    chr_outModuleNumber,
                                    outModuleProcessFlow);
                            outModuleNumber = CimObjectUtils.toString(chr_outModuleNumber);
                            Validations.check(CimObjectUtils.isEmpty(aPrevPOS), retCodeConfig.getNotFoundPos());
                            backup_moduleNo = outModuleNumber;
                        }
                    }
                    // Compare ModuleNumber
                    if (!CimStringUtils.equals(return_moduleNo, backup_moduleNo)) {
                        bSameModuleFlag = false;
                    }
                }
                //--- Get return main PF information
                hFRPFd_theSystemKey = return_mainPF_systemkey;
                String sql27 = "SELECT PRP_ID,\n" +
                        "             PRP_RKEY,\n" +
                        "             ACTIVE_FLAG\n" +
                        "      FROM OMPRF\n" +
                        "      WHERE ID = ?1 ";
                Object[] returnMainPF = cimJpaRepository.queryOne(sql27, hFRPFd_theSystemKey);
                Validations.check(CimObjectUtils.isEmpty(returnMainPF), retCodeConfig.getSqlNotFound());
                hFRPFMAINPD_ID = CimObjectUtils.toString(returnMainPF[0]);
                hFRPFMAINPD_OBJ = CimObjectUtils.toString(returnMainPF[1]);
                int hFRPFSTATE = CimNumberUtils.intValue((Number) returnMainPF[2]);
                String return_mainPDID = hFRPFMAINPD_ID;
                String return_mainPDOBJ = hFRPFMAINPD_OBJ;
                long return_mainPF_state = hFRPFSTATE;
                String return_stageID, return_stageOBJ, return_modulePOS_obj = "", return_modulePDID;
                Long return_mainPF_PDLIST_seqno = 0l, return_modulePF_POSLIST_seqno = 0l, return_modulePF_state = 0l;
                if (bSameModuleFlag) {
                    //=========================================================================
                    //
                    // In this case,
                    //    Out-ModuleNumber and In-ModuleNumber is the same.
                    //    (Mod-A:10)
                    // ---10.10--10.20--10.30--10.40--(Main route)
                    //      |             ^
                    //      |             |
                    //      +-------------+ (Sub route)
                    //
                    //=========================================================================
                    //--- Get seq_no & stage of return module from main PF
                    hFRPFd_theSystemKey = return_mainPF_systemkey;
                    hFRPF_PDLISTd_key = return_moduleNo;
                    String sql28 = "SELECT IDX_NO,\n" +
                            "             STAGE_ID,\n" +
                            "             STAGE_RKEY\n" +
                            "             FROM OMPRF_ROUTESEQ\n" +
                            "             WHERE REFKEY = ?1 \n" +
                            "             AND LINK_KEY    = ?2 ";
                    Object[] mainPF = cimJpaRepository.queryOne(sql28, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);
                    Validations.check(CimObjectUtils.isEmpty(mainPF), retCodeConfig.getSqlNotFound());
                    hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) mainPF[0]);
                    hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(mainPF[1]);
                    hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(mainPF[2]);
                    return_mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo;
                    return_stageID = hFRPF_PDLISTSTAGE_ID;
                    return_stageOBJ = hFRPF_PDLISTSTAGE_OBJ;
                    //--- Get return module PF information
                    hFRPFd_theSystemKey = return_modulePF_systemkey;
                    String sql29 = "SELECT PRP_ID, ACTIVE_FLAG\n" +
                            "       FROM OMPRF\n" +
                            "       WHERE ID = ?1 ";
                    Object[] returnModule = cimJpaRepository.queryOne(sql29, hFRPFd_theSystemKey);
                    Validations.check(CimObjectUtils.isEmpty(returnModule), retCodeConfig.getSqlNotFound());
                    return_modulePDID = CimObjectUtils.toString(returnModule[0]);
                    return_modulePF_state = CimNumberUtils.longValue((Number) returnModule[1]);
                    //--- Get seq_no of return process from module PF
                    hFRPF_POSLISTd_SeqNo = 0l;
                    String sql30 = "SELECT IDX_NO, PRSS_RKEY\n" +
                            "     FROM OMPRF_PRSSSEQ\n" +
                            "     WHERE REFKEY = ?1 \n" +
                            "     AND LINK_KEY    = ?2 ";
                    Object[] modulePF1 = cimJpaRepository.queryOne(sql30, return_modulePF_systemkey, return_moduleOpeNo);
                    Validations.check(CimObjectUtils.isEmpty(modulePF1), retCodeConfig.getSqlNotFound());
                    hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) modulePF1[0]);
                    hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(modulePF1[1]);
                    return_modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                    return_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;
                } else {
                    boolean bFindFlag = true;
                    //=========================================================================
                    //
                    // In this case,
                    //   Out-ModuleNumber and In-ModuleNumber is different.
                    //   (Mod-A:10)           (Mod-B:20)
                    // ---10.10--10.20- -20.10--20.20---(Main route)
                    //             |              ^
                    //             |              |
                    //             +--------------+ (Sub route)
                    //
                    //=========================================================================

                    //=================================================================
                    // Step1
                    //  Check STATE of MAIN_PF_OBJ(MainPF-A) in FRPXF_RETNLIST
                    //=================================================================
                    if (return_mainPF_state == 0) {
                        //=================================================================
                        // Step2
                        //  When STATE of MAIN_PF_OBJ is 0,
                        //   Get the latest MAIN_PF_OBJ(MainPF-B) from MainPD
                        //=================================================================
                        String sql31 = "SELECT ID FROM OMPRF\n" +
                                "     WHERE PRP_ID = ?1\n" +
                                "     AND PRP_LEVEL  = ?2 \n" +
                                "     AND ACTIVE_FLAG     = '1' ";
                        Object MAIN_PF_OBJ = cimJpaRepository.queryOneColumnAndUnique(sql31, return_mainPDID, SP_PD_FLOWLEVEL_MAIN_FOR_MODULE);
                        Validations.check(CimObjectUtils.isEmpty(MAIN_PF_OBJ), retCodeConfig.getSqlNotFound());
                        return_mainPF_systemkey = return_mainPF_obj = hFRPFd_theSystemKey = hFRPFPF_OBJ = CimObjectUtils.toString(MAIN_PF_OBJ);
                        //=================================================================
                        // Step2-1
                        //   Get ModulePD with return ModuleNo from the latest MAIN_PF_OBJ(MainPF-B)
                        //=================================================================
                        String sql32 = "SELECT ROUTE_ID\n" +
                                "     FROM OMPRF_ROUTESEQ\n" +
                                "     WHERE REFKEY = ?1 \n" +
                                "     AND LINK_KEY    = ?2 ";
                        Object ModulePD = cimJpaRepository.queryOneColumnAndUnique(sql32, return_mainPF_systemkey, return_moduleNo);
                        Validations.check(CimObjectUtils.isEmpty(ModulePD), retCodeConfig.getSqlNotFound());
                        hFRPF_PDLISTPD_ID = CimObjectUtils.toString(ModulePD);
                        if (bFindFlag) {
                            return_modulePDID = hFRPF_PDLISTPD_ID;
                            //=================================================================
                            // Step2-2
                            //   Get MOUDLE_PF_OBJ(ModulePD-B) from ModulePD
                            //=================================================================
                            String str_version_id = cimFrameWorkGlobals.extractVersionFromID(return_modulePDID);
                            hFRPFMAINPD_ID = return_modulePDID;
                            hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                            //--- If module PDID's version is "##", convert it to real PDID
                            if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                                String hFRPDPD_ID = return_modulePDID;
                                String sql33 = "SELECT ACTIVE_VER_ID" +
                                        "     FROM OMPRP " +
                                        "     WHERE PRP_ID = ?1 " +
                                        "     AND PRP_LEVEL = ?2 ";
                                Object realPD = cimJpaRepository.queryOneColumnAndUnique(sql33, hFRPDPD_ID, hFRPFPD_LEVEL);
                                Validations.check(CimObjectUtils.isEmpty(realPD), retCodeConfig.getNotFoundProcessDefinition());
                                String hFRPDACTIVE_ID = CimObjectUtils.toString(realPD);
                                hFRPFMAINPD_ID = hFRPDACTIVE_ID;
                            }
                            String sql34 = "SELECT ID,\n" +
                                    "       FROM OMPRF\n" +
                                    "       WHERE PRP_ID = ?1 \n" +
                                    "       AND PRP_LEVEL  = ?2 \n" +
                                    "       AND ACTIVE_FLAG     = '1' ";
                            Object PFID = cimJpaRepository.queryOneColumnAndUnique(sql34, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                            Validations.check(CimObjectUtils.isEmpty(PFID), retCodeConfig.getSqlNotFound());
                            hFRPFd_theSystemKey = CimObjectUtils.toString(PFID);
                            hFRPFPF_OBJ = CimObjectUtils.toString(PFID);
                            return_modulePF_systemkey = hFRPFd_theSystemKey;
                            return_modulePF_obj = hFRPFPF_OBJ;
                            //=================================================================
                            // Step2-3
                            //   Get ModulePOS with return ModuleOpeNo from MOUDLE_PF_OBJ(ModulePD-B)
                            //=================================================================
                            hFRPF_POSLISTd_SeqNo = 0l;
                            hFRPFd_theSystemKey = return_modulePF_systemkey;
                            hFRPF_POSLISTd_key = return_moduleOpeNo;
                            String sql35 = "SELECT IDX_NO,\n" +
                                    "           PRSS_RKEY\n" +
                                    "     FROM OMPRF_PRSSSEQ\n" +
                                    "     WHERE REFKEY = ?1 \n" +
                                    "     AND LINK_KEY    = ?2 ";
                            Object[] FRPF_POSLIST3 = cimJpaRepository.queryOne(sql35, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                            if (CimObjectUtils.isEmpty(FRPF_POSLIST3)) {
                                return_mainPF_systemkey = original_return_mainPF_systemkey;
                                return_mainPF_obj = original_return_mainPF_obj;
                                return_modulePF_systemkey = original_return_modulePF_systemkey;
                                return_modulePF_obj = original_return_modulePF_obj;
                                bFindFlag = false;
                            } else {
                                hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLIST3[0]);
                                hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLIST3[1]);
                                return_modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                                return_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;
                            }
                        }
                    }
                    if (return_mainPF_state == 1 || !bFindFlag) {
                        //=================================================================
                        // Step3
                        //  When STATE of MAIN_PF_OBJ is 1 or ModulePOS does not found,
                        //=================================================================

                        //=================================================================
                        // Step3-1
                        //   Get ModulePD with return ModuleNo from MAINP_PF_OBJ(MainPF-A)
                        //    if ModulePD does not found, Go to Step4.
                        //=================================================================
                        hFRPFd_theSystemKey = return_mainPF_systemkey;
                        hFRPF_PDLISTd_key = return_moduleNo;
                        String sql36 = "SELECT ROUTE_ID\n" +
                                "       FROM OMPRF_ROUTESEQ\n" +
                                "       WHERE REFKEY = ?1 \n" +
                                "       AND LINK_KEY    = ?2  ";
                        Object hFRPF_PDLISTPD_ID1 = cimJpaRepository.queryOneColumnAndUnique(sql36, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);
                        bFindFlag = true;
                        if (CimObjectUtils.isEmpty(hFRPF_PDLISTPD_ID1)) {
                            bFindFlag = false;
                        }
                        if (bFindFlag) {
                            return_modulePDID = CimObjectUtils.toString(hFRPF_PDLISTPD_ID1);
                            //=================================================================
                            // Step3-2
                            //   Get the latest ModulePF(ModulePF-C) from ModulePD
                            //=================================================================
                            hFRPFMAINPD_ID = return_modulePDID;
                            //--- If module PDID's version is "##", convert it to real PDID
                            String str_version_id = cimFrameWorkGlobals.extractVersionFromID(return_modulePDID);
                            if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                                String hFRPDPD_ID = return_modulePDID;
                                String sql37 = "SELECT ACTIVE_VER_ID" +
                                        "     FROM OMPRP " +
                                        "     WHERE PRP_ID = ?1 " +
                                        "     AND PRP_LEVEL = ?2 ";
                                Object realPD = cimJpaRepository.queryOneColumnAndUnique(sql37, hFRPDPD_ID, SP_PD_FLOWLEVEL_MODULE);
                                Validations.check(CimObjectUtils.isEmpty(realPD), retCodeConfig.getNotFoundProcessDefinition());
                                String hFRPDACTIVE_ID = CimObjectUtils.toString(realPD);
                                hFRPFMAINPD_ID = hFRPDACTIVE_ID;
                            }
                            String sql38 = "SELECT ID,\n" +
                                    "       FROM OMPRF\n" +
                                    "       WHERE PRP_ID = ?1 \n" +
                                    "       AND PRP_LEVEL  = ?2 \n" +
                                    "       AND ACTIVE_FLAG     = '1' ";
                            Object PFID = cimJpaRepository.queryOneColumnAndUnique(sql38, hFRPFMAINPD_ID, SP_PD_FLOWLEVEL_MODULE);
                            Validations.check(CimObjectUtils.isEmpty(PFID), retCodeConfig.getSqlNotFound());
                            hFRPFd_theSystemKey = CimObjectUtils.toString(PFID);
                            hFRPFPF_OBJ = CimObjectUtils.toString(PFID);
                            return_modulePF_systemkey = hFRPFd_theSystemKey;
                            return_modulePF_obj = hFRPFPF_OBJ;

                            //=================================================================
                            // Step3-3
                            //   Get ModulePOS from the latest ModulePF(ModulePF-C)
                            //=================================================================
                            hFRPF_POSLISTPOS_OBJ = "";
                            String sql35 = "SELECT IDX_NO,\n" +
                                    "           PRSS_RKEY\n" +
                                    "     FROM OMPRF_PRSSSEQ\n" +
                                    "     WHERE REFKEY = ?1 \n" +
                                    "     AND LINK_KEY    = ?2 ";
                            Object[] FRPF_POSLIST4 = cimJpaRepository.queryOne(sql35, return_modulePF_systemkey, return_moduleOpeNo);
                            if (CimObjectUtils.isEmpty(FRPF_POSLIST4)) {
                                return_modulePF_systemkey = original_return_modulePF_systemkey;
                                return_modulePF_obj = original_return_modulePF_obj;
                                bFindFlag = false;
                            } else {
                                hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLIST4[0]);
                                hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLIST4[1]);
                            }
                            if (bFindFlag) {
                                return_modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                                return_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;
                            }
                        }
                    }
                    //=================================================================
                    // Step4
                    //  Check STATE of MODULE_PF_OBJ(ModlePF-A) in FRPXF_RETNLIST
                    //=================================================================
                    if (!bFindFlag) {
                        //=================================================================
                        // Get STATE of MODULE_PF_OBJ(ModlePF-A)
                        //=================================================================
                        hFRPFSTATE = 0;
                        hFRPFd_theSystemKey = return_modulePF_systemkey;
                        String sql36 = "SELECT PRP_ID,\n" +
                                "             ACTIVE_FLAG\n" +
                                "       FROM OMPRF\n" +
                                "       WHERE ID = ";
                        Object[] MODULE_PF_OBJ = cimJpaRepository.queryOne(sql36, hFRPFd_theSystemKey);
                        Validations.check(CimObjectUtils.isEmpty(MODULE_PF_OBJ), retCodeConfig.getSqlNotFound());
                        hFRPFMAINPD_ID = CimObjectUtils.toString(MODULE_PF_OBJ[0]);
                        hFRPFSTATE = CimNumberUtils.intValue(CimObjectUtils.toString(MODULE_PF_OBJ[1]));
                        return_modulePDID = hFRPFMAINPD_ID;
                        return_modulePF_state = Long.valueOf(hFRPFSTATE);
                        if (return_modulePF_state == 0) {
                            //=================================================================
                            // Step5
                            //  When STATE of MODULE_PF_OBJ is 0,
                            //   Get ModulePD from MODULE_PF_OBJ(ModlePF-A)
                            //=================================================================
                            hFRPFd_theSystemKey = return_modulePF_systemkey;
                            String sql37 = "SELECT PRP_ID\n" +
                                    "       FROM OMPRF\n" +
                                    "       WHERE ID = ?1 ";
                            Object ModulePD = cimJpaRepository.queryOneColumnAndUnique(sql37, hFRPFd_theSystemKey);
                            Validations.check(CimObjectUtils.isEmpty(ModulePD), retCodeConfig.getSqlNotFound());
                            hFRPFMAINPD_ID = CimObjectUtils.toString(ModulePD);
                            return_modulePDID = hFRPFMAINPD_ID;
                            //=================================================================
                            // Get new MODULE_PF_OBJ(ModlePF-D) from ModulePD
                            //=================================================================
                            hFRPFMAINPD_ID = return_modulePDID;
                            hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                            String sql38 = "SELECT ID,\n" +
                                    "       FROM OMPRF\n" +
                                    "       WHERE PRP_ID = ?1 \n" +
                                    "       AND PRP_LEVEL  = ?2 \n" +
                                    "       AND ACTIVE_FLAG     = '1' ";
                            Object PFID = cimJpaRepository.queryOneColumnAndUnique(sql38, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                            bFindFlag = true;
                            if (CimObjectUtils.isEmpty(PFID)) {
                                bFindFlag = false;
                            }
                            //=================================================================
                            // Step5-1
                            // Get ModulePOS with return ModuleOpeNo from new MODULE_PF_OBJ(ModlePF-D)
                            //  if ModulePOS is not found, Go to Step6.
                            //=================================================================
                            if (bFindFlag) {
                                hFRPFd_theSystemKey = hFRPFPF_OBJ = CimObjectUtils.toString(PFID);
                                return_modulePF_systemkey = hFRPFd_theSystemKey;
                                return_modulePF_obj = hFRPFPF_OBJ;
                                //--- Get seq_no of return process from module PF
                                hFRPFd_theSystemKey = return_modulePF_systemkey;
                                hFRPF_POSLISTd_key = return_moduleOpeNo;
                                hFRPF_POSLISTPOS_OBJ = "";
                                String sql39 = "SELECT IDX_NO,\n" +
                                        "           PRSS_RKEY\n" +
                                        "     FROM OMPRF_PRSSSEQ\n" +
                                        "     WHERE REFKEY = ?1 \n" +
                                        "     AND LINK_KEY    = ?2 ";
                                Object[] FRPF_POSLIST4 = cimJpaRepository.queryOne(sql39, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                                if (CimObjectUtils.isEmpty(FRPF_POSLIST4)) {
                                    bFindFlag = false;
                                } else {
                                    hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLIST4[0]);
                                    hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLIST4[1]);
                                }
                                if (bFindFlag) {
                                    return_modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                                    return_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;
                                }
                            }
                        }
                        if (return_modulePF_state == 1 || !bFindFlag) {
                            //=================================================================
                            // Step6
                            //  When STATE of MODULE_PF_OBJ is 1 or ModulePOS does not found,
                            //   Get ModulePOS with return ModuleOpeNo from new MODULE_PF_OBJ(ModlePF-A)
                            //=================================================================
                            hFRPFd_theSystemKey = return_modulePF_systemkey;
                            hFRPF_POSLISTd_key = return_moduleOpeNo;
                            String sql40 = "SELECT IDX_NO,\n" +
                                    "           PRSS_RKEY\n" +
                                    "     FROM OMPRF_PRSSSEQ\n" +
                                    "     WHERE REFKEY = ?1 \n" +
                                    "     AND LINK_KEY    = ?2 ";
                            Object[] FRPF_POSLIST5 = cimJpaRepository.queryOne(sql40, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                            Validations.check(CimObjectUtils.isEmpty(FRPF_POSLIST5), retCodeConfig.getSqlNotFound());
                            hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLIST5[0]);
                            hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLIST5[1]);
                            return_modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                            return_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;
                        }
                    }
                    //--- Get seq_no & stage of return module from main PF
                    hFRPFd_theSystemKey = return_mainPF_systemkey;
                    hFRPF_PDLISTd_key = return_moduleNo;
                    String sql41 = "SELECT IDX_NO,\n" +
                            "      STAGE_ID,\n" +
                            "      STAGE_RKEY\n" +
                            "      FROM OMPRF_ROUTESEQ\n" +
                            "      WHERE REFKEY = ?1 \n" +
                            "      AND LINK_KEY  = ?2 ";
                    Object[] mainPF = cimJpaRepository.queryOne(sql41, current_mainPF_systemkey, current_moduleNo);
                    Validations.check(CimObjectUtils.isEmpty(mainPF), retCodeConfig.getNotFoundPfForDurable());
                    hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) mainPF[0]);
                    hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(mainPF[1]);
                    hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(mainPF[2]);

                    return_mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo;
                    return_stageID = hFRPF_PDLISTSTAGE_ID;
                    return_stageOBJ = hFRPF_PDLISTSTAGE_OBJ;
                    //--- Get return module PF information
                    hFRPFSTATE = 0;
                    hFRPFd_theSystemKey = return_modulePF_systemkey;
                    String sql42 = "SELECT PRP_ID,\n" +
                            "             ACTIVE_FLAG\n" +
                            "       FROM OMPRF\n" +
                            "       WHERE ID = ?1 ";
                    Object[] MODULE_PF_OBJ = cimJpaRepository.queryOne(sql42, hFRPFd_theSystemKey);
                    Validations.check(CimObjectUtils.isEmpty(MODULE_PF_OBJ), retCodeConfig.getSqlNotFound());
                    hFRPFMAINPD_ID = CimObjectUtils.toString(MODULE_PF_OBJ[0]);
                    hFRPFSTATE = CimNumberUtils.intValue(CimObjectUtils.toString(MODULE_PF_OBJ[1]));
                    return_modulePDID = hFRPFMAINPD_ID;
                    return_modulePF_state = Long.valueOf(hFRPFSTATE);
                    //--- Get seq_no of return process from module PF
                }
                //-----------------------------------------------------------------------------
                // (3-3-1) Get next POS from return/active module PF
                //-----------------------------------------------------------------------------
                if (return_modulePF_state != 0) {
                    // Module PF is active, set return POS's seq_no from return module PF
                    modulePF_obj = return_modulePF_obj;
                    modulePF_systemkey = return_modulePF_systemkey;
                    modulePF_POSLIST_seqno = return_modulePF_POSLIST_seqno;
                    FIND_FLAG = true;
                } else {
                    // Module PF is NOT active, search return POS's seq_no from active module PF
                    FIND_FLAG = false;
                    //--- Get active module PF information
                    hFRPFMAINPD_ID = return_modulePDID;
                    hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                    String sql43 = "SELECT ID,\n" +
                            "       FROM OMPRF\n" +
                            "       WHERE PRP_ID = ?1 \n" +
                            "       AND PRP_LEVEL  = ?2 \n" +
                            "       AND ACTIVE_FLAG     = '1' ";
                    Object PFID = cimJpaRepository.queryOneColumnAndUnique(sql43, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                    Validations.check(CimObjectUtils.isEmpty(PFID), retCodeConfig.getSqlNotFound());
                    hFRPFd_theSystemKey = hFRPFPF_OBJ = CimObjectUtils.toString(PFID);
                    active_modulePF_systemkey = hFRPFd_theSystemKey;
                    active_modulePF_obj = hFRPFPF_OBJ;
                    i = 0;
                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        // Search the return operation by finding the return module openo on active module PF
                        if (i == 0) {
                            hFRPF_POSLISTd_SeqNo = 0l;
                            hFRPFd_theSystemKey = active_modulePF_systemkey;
                            hFRPF_POSLISTd_key = return_moduleOpeNo;
                            String sql44 = "SELECT IDX_NO\n" +
                                    "    FROM OMPRF_PRSSSEQ\n" +
                                    "    WHERE REFKEY = :? 1\n" +
                                    "    AND LINK_KEY = ?2 ";
                            Object D_SEQNO = cimJpaRepository.queryOneColumnAndUnique(sql44, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                            if (!CimObjectUtils.isEmpty(D_SEQNO)) {
                                hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) D_SEQNO);
                                modulePF_obj = active_modulePF_obj;
                                modulePF_systemkey = active_modulePF_systemkey;
                                modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                                FIND_FLAG = true;
                                break;
                            } else {
                                //---------------------------------------------------------------------------
                                // Set return POS from inactive return module PF
                                //---------------------------------------------------------------------------
                                Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                strProcess_OperationListForDurable_HelperDR_out.setSeqno(Math.toIntExact(return_seqNo));
                                strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(return_mainPDID, return_mainPDOBJ));
                                strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(return_opeNo);
                                strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(return_stageID, return_stageOBJ));
                                Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                tmpProcessRef.setProcessFlow(return_PF_obj);
                                tmpProcessRef.setMainProcessFlow(return_mainPF_obj);
                                tmpProcessRef.setModuleNumber(return_moduleNo);
                                tmpProcessRef.setModuleProcessFlow(return_modulePF_obj);
                                tmpProcessRef.setModulePOS(return_modulePOS_obj);
                                tmpProcessRef.setProcessOperationSpecification("");
                                this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                opeCount++;
                                lastValue = strProcess_OperationListForDurable_HelperDR_out;
                                if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                        && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                        && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                    opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                }
                            }
                        } else {
                            // Search the next operation by finding the next module openo on active module PF
                            //--- Get the next operation seq_no on return module PF
                            hFRPF_POSLISTd_SeqNo = 0l;
                            hFRPFd_theSystemKey = return_modulePF_systemkey;
                            return_modulePF_POSLIST_seqno++;
                            hFRPF_POSLISTd_SeqNo = return_modulePF_POSLIST_seqno;
                            String sql45 = "SELECT LINK_KEY,\n" +
                                    "           PRSS_RKEY\n" +
                                    "     FROM OMPRF_PRSSSEQ\n" +
                                    "     WHERE REFKEY = ?1 \n" +
                                    "     AND LINK_KEY    = ?2 ";
                            Object[] FRPF_POSLIST6 = cimJpaRepository.queryOne(sql45, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);
                            if (!CimObjectUtils.isEmpty(FRPF_POSLIST6)) {
                                hFRPF_POSLISTd_key = CimObjectUtils.toString(FRPF_POSLIST6[0]);
                                hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLIST6[1]);
                                //--- Keep the next operation information on return module PF
                                next_moduleOpeNo = hFRPF_POSLISTd_key;
                                next_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;
                                //--- Search the corresponding next module openo from active module PF
                                hFRPFd_theSystemKey = active_modulePF_systemkey;
                                hFRPF_POSLISTd_key = next_moduleOpeNo;
                                String sql46 = "SELECT IDX_NO\n" +
                                        "      FROM OMPRF_PRSSSEQ\n" +
                                        "      WHERE REFKEY = ?1 \n" +
                                        "      AND LINK_KEY    = ?2 ";
                                Object nextModuleOpeno = cimJpaRepository.queryOneColumnAndUnique(sql45, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);
                                if (!CimObjectUtils.isEmpty(nextModuleOpeno)) {
                                    modulePF_obj = active_modulePF_obj;
                                    modulePF_systemkey = active_modulePF_systemkey;
                                    modulePF_POSLIST_seqno = CimNumberUtils.longValue((Number) nextModuleOpeno);
                                    FIND_FLAG = true;
                                    break;
                                } else {
                                    //---------------------------------------------------------------------------
                                    // Set next POS from inactive return module PF
                                    //---------------------------------------------------------------------------
                                    Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                    strProcess_OperationListForDurable_HelperDR_out.setSeqno(Math.toIntExact(return_seqNo));
                                    strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(return_mainPDID, return_mainPDOBJ));
                                    strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(return_moduleNo, next_moduleOpeNo));
                                    strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(return_stageID, return_stageOBJ));
                                    Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                    strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                    tmpProcessRef.setProcessFlow(return_PF_obj);
                                    tmpProcessRef.setMainProcessFlow(return_mainPF_obj);
                                    tmpProcessRef.setModuleNumber(return_moduleNo);
                                    tmpProcessRef.setModuleProcessFlow(return_modulePF_obj);
                                    tmpProcessRef.setModulePOS(next_modulePOS_obj);
                                    tmpProcessRef.setProcessOperationSpecification("");
                                    opeCount++;
                                    this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                    strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                    lastValue = strProcess_OperationListForDurable_HelperDR_out;
                                    if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                            && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                            && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                        opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                    }
                                }
                            }
                        }
                        i++;
                    }
                }
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
                //-----------------------------------------------------------------------------
                // Set next POS from active return module PF
                //-----------------------------------------------------------------------------
                if (FIND_FLAG) {

                    hFRPFd_theSystemKey = modulePF_systemkey;
                    hFRPF_POSLISTd_SeqNo = modulePF_POSLIST_seqno;
                    String sql47 = "SELECT LINK_KEY, PRSS_RKEY\n" +
                            "      FROM OMPRF_PRSSSEQ\n" +
                            "      WHERE REFKEY = ?1 \n" +
                            "      AND IDX_NO   >= ?2 \n" +
                            "      ORDER BY IDX_NO";
                    List<Object[]> FRPF_POSLIST7 = cimJpaRepository.query(sql47, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);
                    count2 = 0;
                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                        strProcess_OperationListForDurable_HelperDR_out.setSeqno(Math.toIntExact(return_seqNo));
                        strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(return_mainPDID, return_mainPDOBJ));
                        strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(return_stageID, return_stageOBJ));
                        Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                        strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                        tmpProcessRef.setProcessFlow(return_PF_obj);
                        tmpProcessRef.setMainProcessFlow(return_mainPF_obj);
                        tmpProcessRef.setModuleNumber(return_moduleNo);
                        tmpProcessRef.setModuleProcessFlow(modulePF_obj);
                        if (CimObjectUtils.isEmpty(FRPF_POSLIST7) || count2 == FRPF_POSLIST7.size()) {
                            break;
                        }
                        Object[] cFRPF_POSLIST4 = FRPF_POSLIST7.get(count2);

                        hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST4[0]);
                        hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST4[1]);
                        //--- Set next operation information on active return module PF
                        strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(return_moduleNo, hFRPF_POSLISTd_key));
                        tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                        tmpProcessRef.setProcessOperationSpecification("");
                        this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                        strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                        opeCount++;
                        count2++;
                        lastValue = strProcess_OperationListForDurable_HelperDR_out;
                        if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                            opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                        }
                    }
                }
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
                //-----------------------------------------------------------------------------
                // (3-3-2) Get next module PD from return/active main PF
                //-----------------------------------------------------------------------------
                // If main PF is active, set next module PD's seq_no from return main PF
                if (return_mainPF_state != 0) {
                    mainPF_obj = return_mainPF_obj;
                    mainPF_systemkey = return_mainPF_systemkey;
                    mainPF_PDLIST_seqno = return_mainPF_PDLIST_seqno + 1;
                    FIND_FLAG = true;
                } else {
                    // If main PF is NOT active, search next module PD's seq_no from active main PF
                    FIND_FLAG = false;
                    //--- Get active main PF information
                    hFRPFMAINPD_ID = return_mainPDID;
                    hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MAIN_FOR_MODULE;
                    String sql48 = "SELECT ID,\n" +
                            "       FROM OMPRF\n" +
                            "       WHERE PRP_ID = ?1 \n" +
                            "       AND PRP_LEVEL  = ?2 \n" +
                            "       AND ACTIVE_FLAG     = '1' ";
                    Object PFID = cimJpaRepository.queryOneColumnAndUnique(sql48, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                    Validations.check(CimObjectUtils.isEmpty(PFID), retCodeConfig.getSqlNotFound());
                    hFRPFd_theSystemKey = hFRPFPF_OBJ = CimObjectUtils.toString(PFID);
                    active_mainPF_systemkey = hFRPFd_theSystemKey;
                    active_mainPF_obj = hFRPFPF_OBJ;
                    i = 0;
                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        // Search the next module PD by finding the return module number on active main PF
                        if (i == 0) {
                            hFRPFd_theSystemKey = active_mainPF_systemkey;
                            hFRPF_PDLISTd_key = return_moduleNo;
                            String sql49 = "SELECT IDX_NO,\n" +
                                    "             ROUTE_ID\n" +
                                    "       FROM OMPRF_ROUTESEQ\n" +
                                    "       WHERE REFKEY = ?1 \n" +
                                    "       AND LINK_KEY    = ?2 ";
                            Object[] activeMainPF = cimJpaRepository.queryOne(sql49, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);
                            if (CimObjectUtils.isEmpty(activeMainPF)) {
                                // do nothing
                            } else {
                                //--- Set the next module PD's seq_no on active module PF
                                hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) activeMainPF[0]);
                                hFRPF_PDLISTPD_ID = CimObjectUtils.toString(activeMainPF[1]);
                                mainPF_obj = active_mainPF_obj;
                                mainPF_systemkey = active_mainPF_systemkey;
                                mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo + 1;
                                FIND_FLAG = true;
                                break;
                            }
                        } else {
                            // Search the next module PD by finding the next module number on active main PF
                            //--- Get the next module PD's seq_no on return main PF
                            hFRPFd_theSystemKey = return_mainPF_systemkey;
                            return_mainPF_PDLIST_seqno++;
                            hFRPF_PDLISTd_SeqNo = return_mainPF_PDLIST_seqno;
                            String sql50 = "SELECT ROUTE_ID,\n" +
                                    "              ROUTE_NO,\n" +
                                    "              STAGE_ID,\n" +
                                    "              STAGE_RKEY\n" +
                                    "              FROM OMPRF_ROUTESEQ\n" +
                                    "              WHERE REFKEY = ?1 \n" +
                                    "              AND IDX_NO  = ?2 ";
                            Object[] FRPF_PDLIST6 = cimJpaRepository.queryOne(sql50, hFRPFd_theSystemKey, hFRPF_PDLISTd_SeqNo);
                            if (CimObjectUtils.isEmpty(FRPF_PDLIST6)) {
                                // next module PD is nil
                                break;
                            } else {
                                //--- Keep the next module information on return main PF
                                next_modulePDID = hFRPF_PDLISTPD_ID = CimObjectUtils.toString(FRPF_PDLIST6[0]);
                                next_moduleNo = hFRPF_PDLISTMODULE_NO = CimObjectUtils.toString(FRPF_PDLIST6[1]);
                                next_stageID = hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(FRPF_PDLIST6[2]);
                                next_stageOBJ = hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(FRPF_PDLIST6[3]);
                                //--- Search the corresponding next module number from the active main PF
                                hFRPFd_theSystemKey = active_mainPF_systemkey;
                                hFRPF_PDLISTd_key = next_moduleNo;
                                String sql51 = "SELECT IDX_NO\n" +
                                        "      FROM OMPRF_ROUTESEQ\n" +
                                        "      WHERE REFKEY = ?1 \n" +
                                        "      AND LINK_KEY    = ?2 ";
                                Object FRPF_PDLIST7 = cimJpaRepository.queryOneColumnAndUnique(sql51, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);
                                if (!CimObjectUtils.isEmpty(FRPF_PDLIST7)) {
                                    hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_PDLIST7);
                                    mainPF_obj = active_mainPF_obj;
                                    mainPF_systemkey = active_mainPF_systemkey;
                                    mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo;
                                    FIND_FLAG = true;
                                    break;
                                } else {
                                    //---------------------------------------------------------------------------
                                    // Set next POS from active next module PF and return(inactive) main PF
                                    //---------------------------------------------------------------------------
                                    //--- Get active module PF information
                                    hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                                    //--- If module PDID's version is "##", convert it to real PDID
                                    String str_version_id = cimFrameWorkGlobals.extractVersionFromID(next_modulePDID);
                                    if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                                        String hFRPDPD_ID = next_modulePDID;
                                        String sql52 = "SELECT ACTIVE_VER_ID" +
                                                "     FROM OMPRP " +
                                                "     WHERE PRP_ID = ?1 " +
                                                "     AND PRP_LEVEL = ?2 ";
                                        Object realPD = cimJpaRepository.queryOneColumnAndUnique(sql52, hFRPDPD_ID, hFRPFPD_LEVEL);
                                        Validations.check(CimObjectUtils.isEmpty(realPD), retCodeConfig.getNotFoundProcessDefinition());
                                        String hFRPDACTIVE_ID = CimObjectUtils.toString(realPD);
                                        hFRPFMAINPD_ID = hFRPDACTIVE_ID;
                                    } else {
                                        hFRPFMAINPD_ID = next_modulePDID;
                                    }
                                    String sql53 = "SELECT ID,\n" +
                                            "       FROM OMPRF\n" +
                                            "       WHERE PRP_ID = ?1 \n" +
                                            "       AND PRP_LEVEL  = ?2 \n" +
                                            "       AND ACTIVE_FLAG     = '1' ";
                                    Object PFID1 = cimJpaRepository.queryOneColumnAndUnique(sql53, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                                    Validations.check(CimObjectUtils.isEmpty(PFID1), retCodeConfig.getSqlNotFound());
                                    hFRPFd_theSystemKey = CimObjectUtils.toString(PFID1);
                                    hFRPFPF_OBJ = CimObjectUtils.toString(PFID1);
                                    String next_modulePF_systemkey = hFRPFd_theSystemKey;
                                    String next_modulePF_obj = hFRPFPF_OBJ;

                                    //--- Set next operation information on active module PF
                                    hFRPFd_theSystemKey = next_modulePF_systemkey;
                                    String sql54 = "SELECT LINK_KEY,\n" +
                                            "            PRSS_RKEY\n" +
                                            "      FROM OMPRF_PRSSSEQ\n" +
                                            "      WHERE REFKEY = ?1 \n" +
                                            "      ORDER BY IDX_NO";
                                    List<Object[]> FRPF_POSLISTS = cimJpaRepository.query(sql54, hFRPFd_theSystemKey);
                                    count2 = 0;
                                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                                        Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                        strProcess_OperationListForDurable_HelperDR_out.setSeqno(Math.toIntExact(return_seqNo));
                                        strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(return_mainPDID, return_mainPDOBJ));
                                        strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(next_stageID, next_stageOBJ));
                                        Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                        strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                        tmpProcessRef.setProcessFlow(return_PF_obj);
                                        tmpProcessRef.setMainProcessFlow(return_mainPF_obj);
                                        tmpProcessRef.setModuleNumber(next_moduleNo);
                                        tmpProcessRef.setModuleProcessFlow(next_modulePF_obj);

                                        if (CimObjectUtils.isEmpty(FRPF_POSLISTS) || count2 == FRPF_POSLISTS.size()) {
                                            break;
                                        }
                                        Object[] cFRPF_POSLIST5 = FRPF_POSLISTS.get(count2);
                                        hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST5[0]);
                                        hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST5[0]);
                                        //--- Set next operation information on active module PF
                                        strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(next_moduleNo, hFRPF_POSLISTd_key));
                                        tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                                        tmpProcessRef.setProcessOperationSpecification("");
                                        this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                        strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                        opeCount++;
                                        count2++;
                                        lastValue = strProcess_OperationListForDurable_HelperDR_out;
                                        if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                                && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                                && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                            opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                        }
                                    }
                                }
                            }
                        }
                        i++;
                    }
                }
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
                //-----------------------------------------------------------------------------
                // Set next POS from active next module PF and active main PF
                //-----------------------------------------------------------------------------
                if (FIND_FLAG) {

                    //--- Get next module pdid, module_no and stage from active main PF
                    hFRPFd_theSystemKey = mainPF_systemkey;
                    hFRPF_PDLISTd_SeqNo = mainPF_PDLIST_seqno;
                    String sql55 = " SELECT ROUTE_ID,\n" +
                            "              ROUTE_NO,\n" +
                            "              STAGE_ID,\n" +
                            "              STAGE_RKEY\n" +
                            "        FROM OMPRF_ROUTESEQ\n" +
                            "        WHERE REFKEY = ?1 \n" +
                            "        AND IDX_NO >= ?2 \n" +
                            "        ORDER BY IDX_NO";
                    List<Object[]> FRPF_POSLIST7 = cimJpaRepository.query(sql55, hFRPFd_theSystemKey, hFRPF_PDLISTd_SeqNo);
                    count2 = 0;
                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {

                        if (CimObjectUtils.isEmpty(FRPF_POSLIST7) || count2 == FRPF_POSLIST7.size()) {
                            break;
                        }
                        Object[] cFRPF_POSLIST4 = FRPF_POSLIST7.get(count2);
                        hFRPF_PDLISTPD_ID = CimObjectUtils.toString(cFRPF_POSLIST4[0]);
                        hFRPF_PDLISTMODULE_NO = CimObjectUtils.toString(cFRPF_POSLIST4[1]);
                        hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(cFRPF_POSLIST4[2]);
                        hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(cFRPF_POSLIST4[3]);

                        //--- Get active module PF information
                        hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                        //--- If module PDID's version is "##", convert it to real PDID
                        String str_version_id = cimFrameWorkGlobals.extractVersionFromID(hFRPF_PDLISTPD_ID);
                        if (!CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                            hFRPFMAINPD_ID = hFRPF_PDLISTPD_ID;
                        } else {
                            String hFRPDPD_ID = hFRPF_PDLISTPD_ID;
                            String sq56 = "SELECT ACTIVE_VER_ID \n" +
                                    "      FROM OMPRP \n" +
                                    "      WHERE PRP_ID = ?1 \n" +
                                    "      AND PRP_LEVEL = ?2 ";
                            Object FRPD = cimJpaRepository.queryOneColumnAndUnique(sq56, hFRPDPD_ID, hFRPFPD_LEVEL);
                            Validations.check(CimObjectUtils.isEmpty(FRPD), retCodeConfig.getNotFoundProcessDefinition());
                            hFRPFMAINPD_ID = CimObjectUtils.toString(FRPD);
                        }
                        String sql57 = "SELECT ID\n" +
                                "       FROM OMPRF\n" +
                                "       WHERE PRP_ID = ?1 \n" +
                                "       AND PRP_LEVEL  = ?2 \n" +
                                "       AND ACTIVE_FLAG     = '1' ";
                        Object PFID2 = cimJpaRepository.queryOneColumnAndUnique(sql57, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                        Validations.check(CimObjectUtils.isEmpty(PFID2), retCodeConfig.getSqlNotFound());
                        hFRPFd_theSystemKey = hFRPFPF_OBJ = CimObjectUtils.toString(PFID2);

                        //--- Set next operation information on active module PF
                        String sql58 = "SELECT LINK_KEY,\n" +
                                "            PRSS_RKEY\n" +
                                "      FROM OMPRF_PRSSSEQ\n" +
                                "      WHERE REFKEY = ?1 \n" +
                                "      ORDER BY IDX_NO";
                        List<Object[]> FRPF_POSLISTS2 = cimJpaRepository.query(sql58, hFRPFd_theSystemKey);
                        count3 = 0;
                        while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                            Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                            strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(hFRPF_PDLISTSTAGE_ID, hFRPF_PDLISTSTAGE_OBJ));
                            strProcess_OperationListForDurable_HelperDR_out.setSeqno(Math.toIntExact(return_seqNo));
                            strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(return_mainPDID, return_mainPDOBJ));
                            Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                            strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                            tmpProcessRef.setProcessFlow(return_PF_obj);
                            tmpProcessRef.setMainProcessFlow(mainPF_obj);
                            tmpProcessRef.setModuleNumber(hFRPF_PDLISTMODULE_NO);
                            tmpProcessRef.setModuleProcessFlow(hFRPFPF_OBJ);
                            if (CimObjectUtils.isEmpty(FRPF_POSLISTS2) || count3 == FRPF_POSLISTS2.size()) {
                                break;
                            }

                            Object[] cFRPF_POSLIST6 = FRPF_POSLISTS2.get(count3);
                            hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST6[0]);
                            hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST6[1]);
                            strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(hFRPF_PDLISTMODULE_NO, hFRPF_POSLISTd_key));
                            tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                            tmpProcessRef.setProcessOperationSpecification("");
                            this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                            strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                            opeCount++;
                            count3++;
                            lastValue = strProcess_OperationListForDurable_HelperDR_out;
                            if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                    && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                    && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                            }
                        }
                        count2++;
                    }
                }
                count++;
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
            }
        } else {
            //-----------------------------------------------------------------------------
            // (4) Get backward process information         if(searchDirection == false)
            //-----------------------------------------------------------------------------
            //-----------------------------------------------------------------------------
            // (4-1) Get backward process from FRDRBLPO, if(posSearchFlag == false)
            //-----------------------------------------------------------------------------
            if (!strProcess_OperationListForDurableDR_in.isPosSearchFlag()) {
                //Get a sequence number of current operation in FRDRBLPFX_DRBLPOLIST
                Long hFRDRBLPFX_DRBLPOLISTd_SeqNo = 0l;
                String hFRDRBLPFX_DRBLPOLISTPO_OBJ = current_PO_obj;
                String sql59 = "SELECT IDX_NO\n" +
                        "       FROM OMDRBLPRFCX_PROPESEQ\n" +
                        "       WHERE REFKEY = ?1 \n" +
                        "       AND PROPE_RKEY   = ?2 ";
                Object seqNum = cimJpaRepository.queryOneColumnAndUnique(sql59, hFRDRBLPFXd_theSystemKey, hFRDRBLPFX_DRBLPOLISTPO_OBJ);
                Validations.check(CimObjectUtils.isEmpty(seqNum), retCodeConfig.getNotFoundPfForDurable());
                hFRDRBLPFX_DRBLPOLISTd_SeqNo = CimNumberUtils.longValue((Number) seqNum);
                //Get the backward operations from FRDRBLPFX_DRBLPOLIST (It does not include current operation.)
                String sql60 = "SELECT PROPE_RKEY\n" +
                        "       FROM OMDRBLPRFCX_PROPESEQ\n" +
                        "       WHERE REFKEY = ?1 \n" +
                        "       AND IDX_NO  < ?2 \n" +
                        "       ORDER BY IDX_NO DESC ";
                List<Object> backwardOperations = cimJpaRepository.queryOneColumn(sql60, hFRDRBLPFXd_theSystemKey, hFRDRBLPFX_DRBLPOLISTd_SeqNo);
                count = 0;
                while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    if (CimObjectUtils.isEmpty(backwardOperations) || count == backwardOperations.size()) {
                        break;
                    }
                    Object cFRDRBLPFX_DRBLPOLIST = backwardOperations.get(count);
                    hFRDRBLPFX_DRBLPOLISTPO_OBJ = CimObjectUtils.toString(cFRDRBLPFX_DRBLPOLIST);
                    hFRDRBLPOd_theSystemKey = hFRDRBLPFX_DRBLPOLISTPO_OBJ;
                    // Get process information from FRDRBLPO
                    String sql61 = "SELECT MAIN_PROCESS_ID,\n" +
                            "              MAIN_PROCESS_RKEY,\n" +
                            "              OPE_NO,\n" +
                            "              MPROCESS_PRF_RKEY,\n" +
                            "              MPROCESS_PRSS_RKEY,\n" +
                            "              MROUTE_PRF_RKEY,\n" +
                            "              ROUTE_NO,\n" +
                            "              ROUTE_PRF_RKEY,\n" +
                            "              ROUTE_PRSS_RKEY\n" +
                            "       FROM   OMDRBLPROPE\n" +
                            "       WHERE  ID = ?1 ";
                    Object[] FRDRBLPO1 = cimJpaRepository.queryOne(sql61, hFRDRBLPOd_theSystemKey);
                    Validations.check(CimObjectUtils.isEmpty(FRDRBLPO), retCodeConfig.getSqlNotFound());
                    String hFRDRBLPOMAINPD_ID = CimObjectUtils.toString(FRDRBLPO1[0]);
                    String hFRDRBLPOMAINPD_OBJ = CimObjectUtils.toString(FRDRBLPO1[1]);
                    String hFRDRBLPOOPE_NO = CimObjectUtils.toString(FRDRBLPO1[2]);
                    String hFRDRBLPOPF_OBJ = CimObjectUtils.toString(FRDRBLPO1[3]);
                    String hFRDRBLPOPOS_OBJ = CimObjectUtils.toString(FRDRBLPO1[4]);
                    String hFRDRBLPOMAIN_PF_OBJ = CimObjectUtils.toString(FRDRBLPO1[5]);
                    String hFRDRBLPOMODULE_NO = CimObjectUtils.toString(FRDRBLPO1[6]);
                    String hFRDRBLPOMODULE_PF_OBJ = CimObjectUtils.toString(FRDRBLPO1[7]);
                    String hFRDRBLPOMODPOS_OBJ = CimObjectUtils.toString(FRDRBLPO1[8]);
                    String hFRPFd_theSystemKey = hFRDRBLPOMAIN_PF_OBJ;
                    // Get stage of current module from main PF
                    String hFRPF_PDLISTd_key = hFRDRBLPOMODULE_NO;
                    String sql62 = "SELECT STAGE_ID,\n" +
                            "             STAGE_RKEY\n" +
                            "      FROM OMPRF_ROUTESEQ\n" +
                            "      WHERE REFKEY = ?1 \n" +
                            "      AND LINK_KEY    = ?2 ";
                    Object[] currentModule = cimJpaRepository.queryOne(sql62, hFRDRBLPOd_theSystemKey, hFRPF_PDLISTd_key);
                    String hFRPF_PDLISTSTAGE_ID = "", hFRPF_PDLISTSTAGE_OBJ = "";
                    if (!CimObjectUtils.isEmpty(currentModule)) {
                        hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(currentModule[0]);
                        hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(currentModule[1]);
                    }
                    Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                    strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                    strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(hFRDRBLPOMAINPD_ID, hFRDRBLPOMAINPD_OBJ));
                    strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(hFRDRBLPOOPE_NO);
                    strProcess_OperationListForDurable_HelperDR_out.setObjrefPO(hFRDRBLPFX_DRBLPOLISTPO_OBJ);
                    strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(hFRPF_PDLISTSTAGE_ID, hFRPF_PDLISTSTAGE_OBJ));
                    Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                    strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                    tmpProcessRef.setProcessFlow(hFRDRBLPOPF_OBJ);
                    tmpProcessRef.setProcessOperationSpecification(hFRDRBLPOPOS_OBJ);
                    tmpProcessRef.setMainProcessFlow(hFRDRBLPOMAIN_PF_OBJ);
                    tmpProcessRef.setModuleNumber(hFRDRBLPOMODULE_NO);
                    tmpProcessRef.setModuleProcessFlow(hFRDRBLPOMODULE_PF_OBJ);
                    tmpProcessRef.setModulePOS(hFRDRBLPOMODPOS_OBJ);
                    this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                    strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                    opeCount++;
                    count++;
                    lastValue = strProcess_OperationListForDurable_HelperDR_out;
                    if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                            && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                            && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                        opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                    }
                }
            } else {
                //-----------------------------------------------------------------------------
                // (4-2) Get backward process from FRPOS, if(posSearchFlag == true)
                //-----------------------------------------------------------------------------
                //-----------------------------------------------------------------------------
                // (4-2-1) Get previous POS from current/active module PF
                //-----------------------------------------------------------------------------
                // If module PF is active, set previous POS's seq_no from current module PF
                if (modulePF_state != 0) {
                    log.info("", "###### Module PF is active, set previous POS's seq_no from current module PF");
                    modulePF_obj = current_modulePF_obj;
                    modulePF_systemkey = current_modulePF_systemkey;
                    modulePF_POSLIST_seqno = current_modulePF_POSLIST_seqno - 1;
                    FIND_FLAG = true;
                } else {
                    log.info("", "###### Module PF is NOT active, search previous POS's seq_no from active module PF");
                    FIND_FLAG = false;

                    //--- Get active module PF information
                    log.info("", "###### Get active module PF information");


                    hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;

                    //--- If module PDID's version is "##", convert it to real PDID
                    String str_version_id = cimFrameWorkGlobals.extractVersionFromID(current_modulePDID);

                    if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                        log.info("", "### VERSION_ID == '##'");
                        String hFRPDPD_ID = current_modulePDID;
                        String sql63 = "SELECT ACTIVE_VER_ID \n" +
                                "      FROM OMPRP \n" +
                                "      WHERE PRP_ID = ?1 \n" +
                                "      AND PRP_LEVEL = ?2 ";
                        Object FRPD = cimJpaRepository.queryOneColumn(sql63, hFRPDPD_ID, hFRPFPD_LEVEL).get(0);
                        Validations.check(CimObjectUtils.isEmpty(FRPD), retCodeConfig.getNotFoundProcessDefinition());
                        hFRPFMAINPD_ID = CimObjectUtils.toString(FRPD);
                    } else {
                        log.info("", "### VERSION_ID != '##'");
                        hFRPFMAINPD_ID = current_modulePDID;
                    }

                    String sql64 = "SELECT ID \n" +
                            "       FROM OMPRF\n" +
                            "       WHERE PRP_ID = ?1 \n" +
                            "       AND PRP_LEVEL  = ?2 \n" +
                            "       AND ACTIVE_FLAG     = '1' ";
                    Object FRPD = cimJpaRepository.queryOneColumnAndUnique(sql64, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                    Validations.check(CimObjectUtils.isEmpty(FRPD), retCodeConfig.getNotFoundPfForDurable());
                    String hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD);
                    String hFRPFPF_OBJ = CimObjectUtils.toString(FRPD);


                    active_modulePF_systemkey = hFRPFd_theSystemKey;
                    active_modulePF_obj = hFRPFPF_OBJ;


                    i = 0;
                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        // Search the previous operation by finding the current module openo on active module PF
                        if (i == 0) {
                            log.info("", "###### Search the previous operation by finding the current module openo on active module PF");
                            hFRPF_POSLISTd_SeqNo = 0l;

                            hFRPFd_theSystemKey = active_modulePF_systemkey;
                            String hFRPF_POSLISTd_key = current_moduleOpeNo;

                            String sql65 = "SELECT IDX_NO\n" +
                                    "      FROM OMPRF_PRSSSEQ\n" +
                                    "      WHERE REFKEY = ?1 \n" +
                                    "      AND LINK_KEY    = ?2 ";
                            Object nextModuleOpeno = cimJpaRepository.queryOneColumnAndUnique(sql65, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                            if (CimObjectUtils.isEmpty(nextModuleOpeno)) {
                                //--- Do nothing...
                            } else {
                                //--- Set the previous operation seq_no on active module PF
                                log.info("", "Set the previous operation seq_no on active module PF");
                                hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) nextModuleOpeno);
                                modulePF_obj = active_modulePF_obj;
                                modulePF_systemkey = active_modulePF_systemkey;
                                modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo - 1;
                                FIND_FLAG = true;
                                break;
                            }
                        } else {
                            // Search the previous operation by finding the previous module openo on active module PF
                            log.info("", "###### Search the previous operation by finding the previous module openo on active module PF");

                            //--- Get the previous operation seq_no on current module PF
                            log.info("", "###### Get the previous operation seq_no on current module PF");
                            hFRPF_POSLISTd_SeqNo = 0l;

                            hFRPFd_theSystemKey = current_modulePF_systemkey;
                            current_modulePF_POSLIST_seqno--;
                            hFRPF_POSLISTd_SeqNo = current_modulePF_POSLIST_seqno;

                            String sql66 = "SELECT LINK_KEY,\n" +
                                    "          PRSS_RKEY\n" +
                                    "      FROM OMPRF_PRSSSEQ\n" +
                                    "      WHERE REFKEY = ?1 \n" +
                                    "      AND IDX_NO  = ?2 ";
                            Object[] FRPF_POSLISTS3 = cimJpaRepository.queryOne(sql66, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);
                            if (CimObjectUtils.isEmpty(FRPF_POSLISTS3)) {
                                // previous operation is nil on current module PF
                                break;
                            } else {
                                String hFRPF_POSLISTd_key = CimObjectUtils.toString(FRPF_POSLISTS3[0]);
                                hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLISTS3[1]);
                                //--- Keep the previous operation information on current module PF
                                log.info("", "###### Keep the previous operation information on current module PF");
                                String previous_moduleOpeNo = hFRPF_POSLISTd_key;
                                String previous_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;

                                //--- Search the corresponding previous module openo from active module PF
                                log.info("", "###### Search the corresponding previous module openo from active module PF");

                                hFRPF_POSLISTd_SeqNo = 0l;

                                hFRPFd_theSystemKey = active_modulePF_systemkey;
                                hFRPF_POSLISTd_key = previous_moduleOpeNo;

                                String sql67 = "SELECT IDX_NO\n" +
                                        "      FROM OMPRF_PRSSSEQ\n" +
                                        "      WHERE REFKEY = ?1 \n" +
                                        "      AND LINK_KEY    = ?2 ";
                                Object FRPF_POSLISTS4 = cimJpaRepository.queryOneColumnAndUnique(sql67, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);

                                if (!CimObjectUtils.isEmpty(FRPF_POSLISTS4)) {
                                    hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLISTS4);
                                    modulePF_obj = active_modulePF_obj;
                                    modulePF_systemkey = active_modulePF_systemkey;
                                    modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                                    FIND_FLAG = true;
                                    break;
                                } else {
                                    log.info("", "###### SQLCODE == SP_SQL_NOT_FOUND");
                                    //---------------------------------------------------------------------------
                                    // Set previous POS from inactive current module PF
                                    //---------------------------------------------------------------------------
                                    log.info("", "###### Set previous POS from inactive current module PF");
                                    Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                    strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                                    strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                                    strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(current_moduleNo, previous_moduleOpeNo));
                                    strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(current_stageID, current_stageOBJ));
                                    Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                    strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                    tmpProcessRef.setProcessFlow(current_PF_obj);
                                    tmpProcessRef.setProcessOperationSpecification("");
                                    tmpProcessRef.setMainProcessFlow(current_mainPF_obj);
                                    tmpProcessRef.setModuleNumber(current_moduleNo);
                                    tmpProcessRef.setModuleProcessFlow(current_modulePF_obj);
                                    tmpProcessRef.setModulePOS(previous_modulePOS_obj);
                                    this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                    strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                    opeCount++;
                                    lastValue = strProcess_OperationListForDurable_HelperDR_out;
                                    if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                            && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                            && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                        opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                    }
                                }
                            }
                        }// end of else if(i == 0)
                        i++;
                    }// end of while
                }
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
                //-----------------------------------------------------------------------------
                // Set previous POS from active current module PF
                //-----------------------------------------------------------------------------
                if (FIND_FLAG) {

                    hFRPF_POSLISTd_SeqNo = 0l;

                    String hFRPFd_theSystemKey = modulePF_systemkey;
                    hFRPF_POSLISTd_SeqNo = modulePF_POSLIST_seqno;

                    String sql66 = "SELECT LINK_KEY,\n" +
                            "          PRSS_RKEY\n" +
                            "      FROM OMPRF_PRSSSEQ\n" +
                            "      WHERE REFKEY = ?1 \n" +
                            "      AND IDX_NO     <= ?2 \n" +
                            "      ORDER BY IDX_NO DESC ";
                    List<Object[]> FRPF_POSLISTS3 = cimJpaRepository.query(sql66, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);

                    count = 0;
                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                        strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                        strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                        strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(current_stageID, current_stageOBJ));
                        Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                        strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                        tmpProcessRef.setProcessFlow(current_PF_obj);
                        tmpProcessRef.setMainProcessFlow(current_mainPF_obj);
                        tmpProcessRef.setModuleNumber(current_moduleNo);
                        tmpProcessRef.setModuleProcessFlow(modulePF_obj);
                        if (CimObjectUtils.isEmpty(FRPF_POSLISTS3) || count == FRPF_POSLISTS3.size()) {
                            break;
                        }
                        Object[] cFRPF_POSLIST7 = FRPF_POSLISTS3.get(count);
                        String hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST7[0]);
                        hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST7[1]);
                        log.info("", "###### Set previous operation information on active current module PF");
                        strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(current_moduleNo, hFRPF_POSLISTd_key));
                        tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                        tmpProcessRef.setProcessOperationSpecification("");
                        this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                        strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                        opeCount++;
                        count++;

                        lastValue = strProcess_OperationListForDurable_HelperDR_out;
                        if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                            opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                        }
                    }
                }
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
                //-----------------------------------------------------------------------------
                // (4-2-2) Get previous module PD from current/active main PF
                //-----------------------------------------------------------------------------
                String mainPF_obj = "", mainPF_systemkey = "";
                String active_mainPF_systemkey, active_mainPF_obj;
                String previous_modulePDID = "", previous_moduleNo, previous_stageID, previous_stageOBJ, hFRPFPF_OBJ, hFRPFd_theSystemKey;
                Long mainPF_PDLIST_seqno = 0l, hFRPF_PDLISTd_SeqNo = 0l;
                ;
                if (mainPF_state != 0) {
                    log.info("", "###### Main PF is active, set previous module PD's seq_no from current main PF");
                    mainPF_obj = current_mainPF_obj;
                    mainPF_systemkey = current_mainPF_systemkey;
                    mainPF_PDLIST_seqno = current_mainPF_PDLIST_seqno - 1;
                    FIND_FLAG = true;
                }
                // If main PF is NOT active, search previous module PD's seq_no from active main PF
                else {
                    log.info("", "###### main PF is NOT active, search previous module PD's seq_no from active main PF");
                    FIND_FLAG = false;

                    //--- Get active main PF information
                    log.info("", "###### Get active main PF information");

                    hFRPFMAINPD_ID = current_mainPDID;
                    hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MAIN_FOR_MODULE;

                    String sql64 = "SELECT ID \n" +
                            "       FROM OMPRF\n" +
                            "       WHERE PRP_ID = ?1 \n" +
                            "       AND PRP_LEVEL  = ?2 \n" +
                            "       AND ACTIVE_FLAG     = '1' ";
                    Object FRPD = cimJpaRepository.queryOneColumnAndUnique(sql64, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                    Validations.check(CimObjectUtils.isEmpty(FRPD), retCodeConfig.getNotFoundPfForDurable());
                    hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD);
                    hFRPFPF_OBJ = CimObjectUtils.toString(FRPD);


                    active_mainPF_systemkey = hFRPFd_theSystemKey;
                    active_mainPF_obj = hFRPFPF_OBJ;


                    i = 0;
                    while (1 != 0) {
                        // Search the previous module PD by finding the current module number on active main PF
                        if (i == 0) {
                            log.info("", "###### Search the previous module PD by finding the current module number on active main PF");
                            hFRPF_PDLISTd_SeqNo = 0l;

                            hFRPFd_theSystemKey = active_mainPF_systemkey;
                            String hFRPF_PDLISTd_key = current_moduleNo;

                            String sql65 = "SELECT IDX_NO,\n" +
                                    "             ROUTE_ID\n" +
                                    "       FROM OMPRF_ROUTESEQ\n" +
                                    "       WHERE REFKEY = ?1 \n" +
                                    "       AND LINK_KEY    = ?2 ";
                            Object[] activeMainPF = cimJpaRepository.queryOne(sql65, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);
                            if (CimObjectUtils.isEmpty(activeMainPF)) {
                                // do nothing
                            } else {
                                //--- Set the next module PD's seq_no on active module PF
                                hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) activeMainPF[0]);
                                String hFRPF_PDLISTPD_ID = CimObjectUtils.toString(activeMainPF[1]);
                                mainPF_obj = active_mainPF_obj;
                                mainPF_systemkey = active_mainPF_systemkey;
                                mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo - 1;
                                FIND_FLAG = true;
                                break;
                            }
                        }
                        // Search the previous PD by finding the previous module number on active main PF
                        else {
                            log.info("", "###### Search the previous module PD by finding the previous module number on active main PF");
                            //--- Get the previous module PD's seq_no on current main PF
                            log.info("", "###### Get the previous module PD's seq_no on current main PF");
                            hFRPF_PDLISTd_SeqNo = 0l;

                            hFRPFd_theSystemKey = current_mainPF_systemkey;
                            current_mainPF_PDLIST_seqno--;
                            hFRPF_PDLISTd_SeqNo = current_mainPF_PDLIST_seqno;

                            String sql66 = " SELECT ROUTE_ID,\n" +
                                    "              ROUTE_ID,\n" +
                                    "              STAGE_ID,\n" +
                                    "              STAGE_RKEY\n" +
                                    "        FROM OMPRF_ROUTESEQ\n" +
                                    "        WHERE REFKEY = ?1 \n" +
                                    "        AND IDX_NO  = ?2  ";
                            Object[] FRPF_POSLIST7 = cimJpaRepository.queryOne(sql66, hFRPFd_theSystemKey, hFRPF_PDLISTd_SeqNo);
                            if (CimObjectUtils.isEmpty(FRPF_POSLIST7)) {
                                // previous module PD is nil
                                log.info("", "###### previous module PD is nil");
                                break;
                            } else {
                                String hFRPF_PDLISTPD_ID = CimObjectUtils.toString(FRPF_POSLIST7[0]);
                                String hFRPF_PDLISTMODULE_NO = CimObjectUtils.toString(FRPF_POSLIST7[0]);
                                String hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(FRPF_POSLIST7[0]);
                                String hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(FRPF_POSLIST7[0]);
                                //--- Keep the previous module information on current main PF
                                log.info("", "###### Keep the previous module information on current main PF");
                                previous_modulePDID = hFRPF_PDLISTPD_ID;
                                previous_moduleNo = hFRPF_PDLISTMODULE_NO;
                                previous_stageID = hFRPF_PDLISTSTAGE_ID;
                                previous_stageOBJ = hFRPF_PDLISTSTAGE_OBJ;

                                //--- Search the corresponding previous module number from the active main PF
                                log.info("", "###### Search the corresponding previous module number from the active main PF");
                                hFRPF_PDLISTd_SeqNo = 0l;

                                hFRPFd_theSystemKey = active_mainPF_systemkey;
                                String hFRPF_PDLISTd_key = previous_moduleNo;

                                String sql67 = "SELECT IDX_NO\n" +
                                        "      FROM OMPRF_ROUTESEQ\n" +
                                        "      WHERE REFKEY = ?1 \n" +
                                        "      AND LINK_KEY    = ?2 ";
                                Object FRPF_PDLIST7 = cimJpaRepository.queryOneColumnAndUnique(sql67, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);

                                if (!CimObjectUtils.isEmpty(FRPF_PDLIST7)) {
                                    hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_PDLIST7);
                                    log.info("", "###### SQLCODE == SQL_RC_OK");
                                    mainPF_obj = active_mainPF_obj;
                                    mainPF_systemkey = active_mainPF_systemkey;
                                    mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo;
                                    FIND_FLAG = true;
                                    break;
                                } else {
                                    log.info("", "###### SQLCODE == SP_SQL_NOT_FOUND");
                                    //---------------------------------------------------------------------------
                                    // Set previous POS from active previous module PF and current(inactive) main PF
                                    //---------------------------------------------------------------------------
                                    //--- Get active module PF information
                                    log.info("", "###### Get active module PF information");

                                    hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;

                                    //--- If module PDID's version is "##", convert it to real PDID
                                    String str_version_id = cimFrameWorkGlobals.extractVersionFromID(previous_modulePDID);

                                    if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                                        log.info("", "### VERSION_ID == '##'");
                                        String hFRPDPD_ID = previous_modulePDID;
                                        String sql68 = "SELECT ACTIVE_VER_ID \n" +
                                                "      FROM OMPRP \n" +
                                                "      WHERE PRP_ID = ?1 \n" +
                                                "      AND PRP_LEVEL = ?2 ";
                                        Object hFRPDACTIVE_ID = cimJpaRepository.queryOneColumnAndUnique(sql68, hFRPDPD_ID, hFRPFPD_LEVEL);
                                        Validations.check(CimObjectUtils.isEmpty(hFRPDACTIVE_ID), retCodeConfig.getNotFoundProcessDefinition());
                                        hFRPFMAINPD_ID = CimObjectUtils.toString(hFRPDACTIVE_ID);
                                    } else {
                                        log.info("", "### VERSION_ID != '##'");
                                        hFRPFMAINPD_ID = previous_modulePDID;
                                    }

                                    String sql69 = "SELECT ID \n" +
                                            "       FROM OMPRF\n" +
                                            "       WHERE PRP_ID = ?1 \n" +
                                            "       AND PRP_LEVEL  = ?2 \n" +
                                            "       AND ACTIVE_FLAG     = '1' ";
                                    Object FRPD3 = cimJpaRepository.queryOneColumnAndUnique(sql69, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                                    Validations.check(CimObjectUtils.isEmpty(FRPD3), retCodeConfig.getNotFoundPfForDurable());
                                    hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD3);
                                    hFRPFPF_OBJ = CimObjectUtils.toString(FRPD3);


                                    String previous_modulePF_systemkey = hFRPFd_theSystemKey;
                                    String previous_modulePF_obj = hFRPFPF_OBJ;


                                    log.info("", "###### Get active module PF information");

                                    //--- Set previous operation information on active module PF
                                    log.info("", "###### Set previous operation information on active module PF");
                                    hFRPFd_theSystemKey = previous_modulePF_systemkey;

                                    //--- Set next operation information on active module PF
                                    String sql70 = "SELECT LINK_KEY,\n" +
                                            "            PRSS_RKEY\n" +
                                            "      FROM OMPRF_PRSSSEQ\n" +
                                            "      WHERE REFKEY = ?1 \n" +
                                            "      ORDER BY IDX_NO";
                                    List<Object[]> FRPF_POSLISTS2 = cimJpaRepository.query(sql70, hFRPFd_theSystemKey);
                                    count = 0;
                                    while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                                        Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                        strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                                        strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                                        strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(previous_stageID, previous_stageOBJ));
                                        Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                        strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                        tmpProcessRef.setProcessFlow(current_PF_obj);
                                        tmpProcessRef.setMainProcessFlow(current_mainPF_obj);
                                        tmpProcessRef.setModuleNumber(previous_moduleNo);
                                        tmpProcessRef.setModuleProcessFlow(previous_modulePF_obj);
                                        if (CimObjectUtils.isEmpty(FRPF_POSLISTS2) || count == FRPF_POSLISTS2.size()) {
                                            break;
                                        }

                                        Object[] cFRPF_POSLIST8 = FRPF_POSLISTS2.get(count3);
                                        String hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST8[0]);
                                        hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST8[1]);
                                        strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(previous_moduleNo, hFRPF_POSLISTd_key));
                                        tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                                        tmpProcessRef.setProcessOperationSpecification("");
                                        this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                        strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                        opeCount++;
                                        count++;
                                        lastValue = strProcess_OperationListForDurable_HelperDR_out;
                                        if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                                && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                                && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                            opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                        }
                                    }
                                }
                            }
                        }// end of else if(i == 0)
                        i++;
                    }// end of while
                }
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
                //-----------------------------------------------------------------------------
                // Set previous POS from active previous module PF and active main PF
                //-----------------------------------------------------------------------------
                if (FIND_FLAG) {
                    log.info("", "###### FIND_FLAG == true");

                    //--- Get previous module pdid, module_no and stage from active main PF
                    log.info("", "###### Get previous module pdid, module_no and stage from active main PF");
                    hFRPF_PDLISTd_SeqNo = 0l;

                    hFRPFd_theSystemKey = mainPF_systemkey;
                    hFRPF_PDLISTd_SeqNo = mainPF_PDLIST_seqno;

                    String sql71 = " SELECT ROUTE_ID,\n" +
                            "              ROUTE_NO,\n" +
                            "              STAGE_ID,\n" +
                            "              STAGE_RKEY\n" +
                            "        FROM OMPRF_ROUTESEQ\n" +
                            "        WHERE REFKEY = ?1 \n" +
                            "        AND IDX_NO  <= ?2 \n" +
                            "        ORDER BY IDX_NO ";
                    List<Object[]> FRPF_POSLIST7 = cimJpaRepository.query(sql71, hFRPFd_theSystemKey, hFRPF_PDLISTd_SeqNo);

                    count = 0;
                    while (true) {
                        if (CimObjectUtils.isEmpty(FRPF_POSLIST7) || count == FRPF_POSLIST7.size()) {
                            break;
                        }
                        Object[] cFRPF_PDLIS4 = FRPF_POSLIST7.get(count);
                        String hFRPF_PDLISTPD_ID = CimObjectUtils.toString(cFRPF_PDLIS4[0]);
                        String hFRPF_PDLISTMODULE_NO = CimObjectUtils.toString(cFRPF_PDLIS4[1]);
                        String hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(cFRPF_PDLIS4[2]);
                        String hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(cFRPF_PDLIS4[3]);

                        //--- Get active module PF information
                        log.info("", "### Get active module PF information");

                        hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;

                        //--- If module PDID's version is "##", convert it to real PDID
                        String str_version_id = cimFrameWorkGlobals.extractVersionFromID(hFRPF_PDLISTPD_ID);

                        if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                            log.info("", "### VERSION_ID == '##'");
                            String hFRPDPD_ID = hFRPF_PDLISTPD_ID;
                            String sql72 = "SELECT ACTIVE_VER_ID \n" +
                                    "      FROM OMPRP \n" +
                                    "      WHERE PRP_ID = ?1 \n" +
                                    "      AND PRP_LEVEL = ?2 ";
                            Object hFRPDACTIVE_ID = cimJpaRepository.queryOneColumnAndUnique(sql72, hFRPDPD_ID, hFRPFPD_LEVEL);
                            Validations.check(CimObjectUtils.isEmpty(hFRPDACTIVE_ID), retCodeConfig.getNotFoundProcessDefinition());
                            hFRPFMAINPD_ID = CimObjectUtils.toString(hFRPDACTIVE_ID);
                        } else {
                            log.info("", "### VERSION_ID != '##'");
                            hFRPFMAINPD_ID = hFRPF_PDLISTPD_ID;
                        }

                        String sql73 = "SELECT ID \n" +
                                "       FROM OMPRF\n" +
                                "       WHERE PRP_ID = ?1 \n" +
                                "       AND PRP_LEVEL  = ?2 \n" +
                                "       AND ACTIVE_FLAG     = '1' ";
                        Object FRPD3 = cimJpaRepository.queryOneColumnAndUnique(sql73, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                        Validations.check(CimObjectUtils.isEmpty(FRPD3), retCodeConfig.getNotFoundPfForDurable());
                        hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD3);
                        hFRPFPF_OBJ = CimObjectUtils.toString(FRPD3);

                        log.info("", "###### OMPRF_ROUTESEQ select result ");

                        //--- Set previous operation information on active module PF
                        log.info("", "###### Set previous operation information on active module PF");
                        String sql74 = "SELECT LINK_KEY,\n" +
                                "            PRSS_RKEY\n" +
                                "      FROM OMPRF_PRSSSEQ\n" +
                                "      WHERE REFKEY = ?1 \n" +
                                "      ORDER BY IDX_NO DESC";
                        List<Object[]> FRPF_POSLISTS2 = cimJpaRepository.query(sql74, hFRPFd_theSystemKey);

                        count2 = 0;
                        while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {

                            Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                            strProcess_OperationListForDurable_HelperDR_out.setSeqno(-1);
                            strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(current_mainPDID, current_mainPDOBJ));
                            Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                            strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(hFRPF_PDLISTSTAGE_ID, hFRPF_PDLISTSTAGE_OBJ));
                            tmpProcessRef.setModuleNumber(hFRPF_PDLISTMODULE_NO);
                            tmpProcessRef.setModuleProcessFlow(hFRPFPF_OBJ);
                            strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                            tmpProcessRef.setProcessFlow(current_PF_obj);
                            tmpProcessRef.setMainProcessFlow(mainPF_obj);

                            if (CimObjectUtils.isEmpty(FRPF_POSLISTS2) || count2 == FRPF_POSLISTS2.size()) {
                                break;
                            }

                            Object[] cFRPF_POSLIST9 = FRPF_POSLISTS2.get(count2);
                            String hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST9[0]);
                            hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST9[1]);

                            strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(hFRPF_PDLISTMODULE_NO, hFRPF_POSLISTd_key));
                            tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                            tmpProcessRef.setProcessOperationSpecification("");
                            this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                            strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                            opeCount++;
                            count2++;
                            lastValue = strProcess_OperationListForDurable_HelperDR_out;
                            if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                    && ObjectIdentifier.equalsWithValue(lastValue.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                    && ObjectIdentifier.equalsWithValue(lastValue.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                            }
                        }
                        count++;
                    }
                }
                if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    return strProcess_OperationListForDurableDR_out;
                }
                //---------------------------------------------------------------------------------------------------------------
                // (4-2-3) If the durable is on sub/rework route, set the backup operaion and the following operations from backup flow
                //---------------------------------------------------------------------------------------------------------------
                String sql75 = "SELECT IDX_NO, PROPE_RKEY\n" +
                        "      FROM OMDRBLPRFCX_BCKPROPESEQ\n" +
                        "      WHERE REFKEY = ?1 \n" +
                        "      ORDER BY IDX_NO ";
                List<Object[]> FRDRBLPFX_BACKPOLIST = cimJpaRepository.query(sql75, hFRDRBLPFXd_theSystemKey);
                count = 0;
                while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                    log.info("", "###### Fetch from OMDRBLPRFCX_BCKPROPESEQ ## count = ", count);
                    long hFRDRBLPFX_BACKPOLISTd_SeqNo = 0l;
                    if (CimObjectUtils.isEmpty(FRDRBLPFX_BACKPOLIST) || count == FRDRBLPFX_BACKPOLIST.size()) {
                        break;
                    }
                    Object[] cFRDRBLPFX_BACKPOLIST1 = FRDRBLPFX_BACKPOLIST.get(count);
                    hFRDRBLPFX_BACKPOLISTd_SeqNo = CimNumberUtils.longValue((Number) cFRDRBLPFX_BACKPOLIST1[0]);
                    String hFRDRBLPFX_BACKPOLISTPO_OBJ = CimObjectUtils.toString(cFRDRBLPFX_BACKPOLIST1[1]);

                    log.info("", "###### Fetch from OMDRBLPRFCX_BCKPROPESEQ result ");
                    log.info("", "### hOMDRBLPRFCX_BCKPROPESEQd_SeqNo = ", hFRDRBLPFX_BACKPOLISTd_SeqNo);
                    log.info("", "### hOMDRBLPRFCX_BCKPROPESEQPO_OBJ  = ", hFRDRBLPFX_BACKPOLISTPO_OBJ);

                    //--- Keep backup operation information
                    log.info("", "###### Keep backup operation information");
                    long backup_seqNo = hFRDRBLPFX_BACKPOLISTd_SeqNo;

                    String backup_PO_systemkey = hFRDRBLPFX_BACKPOLISTPO_OBJ;

                    // Get process information from FRDRBLPO
                    log.info("", "###### Get process information from OMDRBLPROPE");

                    hFRDRBLPOd_theSystemKey = backup_PO_systemkey;
                    String sql76 = "SELECT OPE_NO, ROUTE_NO, ROUTE_OPE_NO, MPROCESS_PRF_RKEY, MPROCESS_PRSS_RKEY, MROUTE_PRF_RKEY, ROUTE_PRF_RKEY, ROUTE_PRSS_RKEY FROM OMDRBLPROPE WHERE  ID = ?1 ";
                    Object[] FRDRBLPO1 = cimJpaRepository.queryOne(sql76, hFRDRBLPOd_theSystemKey);
                    Validations.check(CimObjectUtils.isEmpty(FRDRBLPO), retCodeConfig.getSqlNotFound());
                    String hFRDRBLPOOPE_NO = CimObjectUtils.toString(FRDRBLPO1[0]);
                    String hFRDRBLPOMODULE_NO = CimObjectUtils.toString(FRDRBLPO1[1]);
                    String hFRDRBLPOMODULE_OPE_NO = CimObjectUtils.toString(FRDRBLPO1[2]);
                    String hFRDRBLPOPF_OBJ = CimObjectUtils.toString(FRDRBLPO1[3]);
                    String hFRDRBLPOPOS_OBJ = CimObjectUtils.toString(FRDRBLPO1[4]);
                    String hFRDRBLPOMAIN_PF_OBJ = CimObjectUtils.toString(FRDRBLPO1[5]);
                    String hFRDRBLPOMODULE_PF_OBJ = CimObjectUtils.toString(FRDRBLPO1[6]);
                    String hFRDRBLPOMODPOS_OBJ = CimObjectUtils.toString(FRDRBLPO1[7]);

                    //--- Keep backup operation information
                    String backup_opeNo = hFRDRBLPOOPE_NO;
                    String backup_moduleNo = hFRDRBLPOMODULE_NO;
                    String backup_moduleOpeNo = hFRDRBLPOMODULE_OPE_NO;
                    String backup_PF_obj = hFRDRBLPOPF_OBJ;
                    String backup_POS_obj = hFRDRBLPOPOS_OBJ;
                    String backup_mainPF_obj = hFRDRBLPOMAIN_PF_OBJ;
                    String backup_modulePF_obj = hFRDRBLPOMODULE_PF_OBJ;
                    String backup_modulePOS_obj = hFRDRBLPOMODPOS_OBJ;
                    String backup_mainPF_systemkey = hFRDRBLPOMAIN_PF_OBJ;
                    String backup_modulePF_systemkey = hFRDRBLPOMODULE_PF_OBJ;

                    log.info("", "###### Select from OMDRBLPROPE result");
                    log.info("", "### backup_opeNo         = ", backup_opeNo);
                    log.info("", "### backup_moduleNo      = ", backup_moduleNo);
                    log.info("", "### backup_moduleOpeNo   = ", backup_moduleOpeNo);
                    log.info("", "### backup_PF_obj        = ", backup_PF_obj);
                    log.info("", "### backup_POS_obj       = ", backup_POS_obj);
                    log.info("", "### backup_mainPF_obj    = ", backup_mainPF_obj);
                    log.info("", "### backup_modulePF_obj  = ", backup_modulePF_obj);
                    log.info("", "### backup_modulePOS_obj = ", backup_modulePOS_obj);
                    log.info("", "### Generated Key backup_mainPF_systemkey   = ", backup_mainPF_systemkey);
                    log.info("", "### Generated Key backup_modulePF_systemkey = ", backup_modulePF_systemkey);

                    //--- Get backup main PF information
                    log.info("", "###### Get backup main PF information");
                    int hFRPFSTATE = 0;

                    hFRPFd_theSystemKey = backup_mainPF_systemkey;

                    String sql77 = "SELECT PRP_ID,\n" +
                            "           PRP_RKEY,\n" +
                            "           ACTIVE_FLAG\n" +
                            "       FROM OMPRF\n" +
                            "       WHERE ID = ?1 ";
                    Object[] FRPF3 = cimJpaRepository.queryOne(sql77, hFRPFd_theSystemKey);
                    Validations.check(CimObjectUtils.isEmpty(FRPF3), retCodeConfig.getSqlNotFound());
                    hFRPFMAINPD_ID = CimObjectUtils.toString(FRPF3[0]);
                    String hFRPFMAINPD_OBJ = CimObjectUtils.toString(FRPF3[1]);
                    hFRPFSTATE = CimNumberUtils.intValue(CimObjectUtils.toString(FRPF3[2]));

                    String backup_mainPDID = hFRPFMAINPD_ID;
                    String backup_mainPDOBJ = hFRPFMAINPD_OBJ;
                    Long backup_mainPF_state = Long.valueOf(hFRPFSTATE);

                    log.info("", "###### Select from OMPRF result");
                    log.info("", "### backup_mainPDID     = ", backup_mainPDID);
                    log.info("", "### backup_mainPDOBJ    = ", backup_mainPDOBJ);
                    log.info("", "### backup_mainPF_state = ", backup_mainPF_state);

                    //--- Get seq_no & stage of backup module from main PF
                    log.info("", "###### Get seq_no & stage of backup module from main PF");
                    hFRPF_PDLISTd_SeqNo = 0l;

                    hFRPFd_theSystemKey = backup_mainPF_systemkey;
                    String hFRPF_PDLISTd_key = backup_moduleNo;

                    String sql78 = "SELECT IDX_NO,\n" +
                            "      STAGE_ID,\n" +
                            "      STAGE_RKEY\n" +
                            "      FROM OMPRF_ROUTESEQ\n" +
                            "      WHERE REFKEY = ?1 \n" +
                            "      AND LINK_KEY  = ?2 ";
                    Object[] mainPF = cimJpaRepository.queryOne(sql78, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);
                    Validations.check(CimObjectUtils.isEmpty(mainPF), retCodeConfig.getNotFoundPfForDurable());
                    hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) mainPF[0]);
                    String hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(mainPF[1]);
                    String hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(mainPF[2]);


                    Long backup_mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo;
                    String backup_stageID = hFRPF_PDLISTSTAGE_ID;
                    String backup_stageOBJ = hFRPF_PDLISTSTAGE_OBJ;

                    log.info("", "###### Select from OMPRF_ROUTESEQ result");
                    log.info("", "### backup_mainPF_PDLIST_seqno = ", backup_mainPF_PDLIST_seqno);
                    log.info("", "### backup_stageID             = ", backup_stageID);
                    log.info("", "### backup_stageOBJ            = ", backup_stageOBJ);

                    //--- Get backup module PF information
                    log.info("", "###### Get backup module PF information");
                    hFRPFSTATE = 0;

                    hFRPFd_theSystemKey = backup_modulePF_systemkey;

                    String sql79 = "SELECT PRP_ID,\n" +
                            "          ACTIVE_FLAG\n" +
                            "      FROM OMPRF\n" +
                            "      WHERE ID = ?1 ";
                    Object[] FRPF6 = cimJpaRepository.queryOne(sql79, hFRPFd_theSystemKey);
                    Validations.check(CimObjectUtils.isEmpty(FRPF6), retCodeConfig.getNotFoundPfForDurable());
                    hFRPFMAINPD_ID = CimObjectUtils.toString(FRPF6[0]);
                    hFRPFSTATE = CimNumberUtils.intValue(CimObjectUtils.toString(FRPF6[1]));

                    String backup_modulePDID = hFRPFMAINPD_ID;
                    Long backup_modulePF_state = Long.valueOf(hFRPFSTATE);

                    log.info("", "###### Select from OMPRF result");
                    log.info("", "### backup_modulePDID = ", backup_modulePDID);
                    log.info("", "### backup_modulePF_state = ", backup_modulePF_state);

                    //--- Get seq_no of backup process from module PF
                    log.info("", "###### Get seq_no of backup process from module PF");
                    hFRPF_POSLISTd_SeqNo = 0l;

                    hFRPFd_theSystemKey = backup_modulePF_systemkey;
                    String hFRPF_POSLISTd_key = backup_moduleOpeNo;


                    String sql80 = "SELECT IDX_NO\n" +
                            "      FROM OMPRF_PRSSSEQ\n" +
                            "      WHERE REFKEY = ?1 \n" +
                            "      AND LINK_KEY          = ?2 ";
                    Object FRPF_POSLISTS4 = cimJpaRepository.queryOneColumnAndUnique(sql80, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                    Validations.check(CimObjectUtils.isEmpty(FRPF_POSLISTS4), retCodeConfig.getNotFoundPfForDurable());
                    hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLISTS4);

                    Long backup_modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;

                    log.info("", "###### Select from OMPRF_PRSSSEQ result");
                    log.info("", "### backup_modulePF_POSLIST_seqno = ", backup_modulePF_POSLIST_seqno);

                    //-----------------------------------------------------------------------------
                    // (4-2-3-1) Get previous POS from backup/active module PF
                    //-----------------------------------------------------------------------------
                    log.info("", "###### Get previous POS from backup/active module PF");

                    if (backup_modulePF_state != 0) {
                        // Module PF is active, set backup POS's seq_no from backup module PF
                        log.info("", "###### Module PF is active, set backup POS's seq_no from backup module PF");
                        modulePF_obj = backup_modulePF_obj;
                        modulePF_systemkey = backup_modulePF_systemkey;
                        modulePF_POSLIST_seqno = backup_modulePF_POSLIST_seqno;
                        FIND_FLAG = true;
                    } else {
                        // Module PF is NOT active, search backup POS's seq_no from active module PF
                        log.info("", "###### Module PF is NOT active, search backup POS's seq_no from active module PF");

                        FIND_FLAG = false;

                        //--- Get active module PF information
                        log.info("", "###### Get active module PF information");

                        hFRPFMAINPD_ID = backup_modulePDID;
                        hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;

                        String sql81 = "SELECT ID \n" +
                                "       FROM OMPRF\n" +
                                "       WHERE PRP_ID = ?1 \n" +
                                "       AND PRP_LEVEL  = ?2 \n" +
                                "       AND ACTIVE_FLAG     = '1' ";
                        Object FRPD3 = cimJpaRepository.queryOneColumnAndUnique(sql81, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                        Validations.check(CimObjectUtils.isEmpty(FRPD3), retCodeConfig.getNotFoundPfForDurable());
                        hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD3);
                        hFRPFPF_OBJ = CimObjectUtils.toString(FRPD3);


                        active_modulePF_systemkey = hFRPFd_theSystemKey;
                        active_modulePF_obj = hFRPFPF_OBJ;

                        log.info("", "###### Set active module PF information");
                        log.info("", "### active_modulePF_systemkey = ", active_modulePF_systemkey);
                        log.info("", "### active_modulePF_obj       = ", active_modulePF_obj);

                        i = 0;
                        while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                            // Search the backup operation by finding the backup module openo on active module PF
                            if (i == 0) {
                                log.info("", "###### Search the backup operation by finding the backup module openo on active module PF");
                                hFRPF_POSLISTd_SeqNo = 0l;

                                hFRPFd_theSystemKey = active_modulePF_systemkey;
                                hFRPF_POSLISTd_key = backup_moduleOpeNo;

                                String sql82 = "SELECT IDX_NO\n" +
                                        "      FROM OMPRF_PRSSSEQ\n" +
                                        "      WHERE REFKEY = ?1 \n" +
                                        "      AND LINK_KEY          = ?2 ";
                                Object FRPF_POSLISTS5 = cimJpaRepository.queryOneColumnAndUnique(sql82, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                                if (!CimObjectUtils.isEmpty(FRPF_POSLISTS5)) {
                                    hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLISTS4);//--- Set the backup operation seq_no on active module PF
                                    log.info("", "###### Set the backup operation seq_no on active module PF");
                                    modulePF_obj = active_modulePF_obj;
                                    modulePF_systemkey = active_modulePF_systemkey;
                                    modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                                    FIND_FLAG = true;
                                    break;
                                } else {
                                    //---------------------------------------------------------------------------
                                    // Set backup POS from inactive backup module PF
                                    //---------------------------------------------------------------------------
                                    log.info("", "###### Set return POS from inactive return module PF");
                                    Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                    strProcess_OperationListForDurable_HelperDR_out.setSeqno(CimNumberUtils.intValue(backup_seqNo));
                                    strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(backup_mainPDID, backup_mainPDOBJ));
                                    strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(backup_stageID, backup_stageOBJ));
                                    strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(backup_opeNo);
                                    Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                    strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                    tmpProcessRef.setProcessFlow(backup_PF_obj);
                                    tmpProcessRef.setMainProcessFlow(backup_mainPF_obj);
                                    tmpProcessRef.setModuleNumber(backup_moduleNo);
                                    tmpProcessRef.setModuleProcessFlow(backup_modulePF_obj);
                                    tmpProcessRef.setModulePOS(backup_modulePOS_obj);
                                    tmpProcessRef.setProcessOperationSpecification(backup_POS_obj);
                                    this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                    strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                    opeCount++;
                                    if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                            && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                            && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                        log.info("The route and operation to search are found.");
                                        opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                    }
                                }
                            } else {
                                // Search the previous operation by finding the previous module openo on active module PF
                                log.info("", "###### Search the next operation by finding the next module openo on active module PF");

                                //--- Get the previous operation seq_no on backup module PF
                                log.info("", "###### Get the next operation seq_no on return module PF");
                                hFRPF_POSLISTd_SeqNo = 0l;

                                hFRPFd_theSystemKey = backup_modulePF_systemkey;
                                backup_modulePF_POSLIST_seqno--;
                                hFRPF_POSLISTd_SeqNo = backup_modulePF_POSLIST_seqno;
                                log.info("", "###### hOMPRF_PRSSSEQd_SeqNo = ", hFRPF_POSLISTd_SeqNo);


                                String sql83 = "SELECT LINK_KEY,\n" +
                                        "            PRSS_RKEY\n" +
                                        "      FROM OMPRF_PRSSSEQ\n" +
                                        "      WHERE REFKEY = ?1 \n" +
                                        "      AND IDX_NO  =  ?2 ";
                                Object[] FRPF_POSLISTS2 = cimJpaRepository.queryOne(sql83, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);
                                if (CimObjectUtils.isEmpty(FRPF_POSLISTS2)) {
                                    break;
                                } else {
                                    log.info("", "###### SQLCODE == SQL_RC_OK");
                                    hFRPF_POSLISTd_key = CimObjectUtils.toString(FRPF_POSLISTS2[0]);
                                    hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLISTS2[1]);
                                    //--- Keep the previous operation information on backup module PF
                                    log.info("", "###### Keep the previous operation information on backup module PF");
                                    String previous_moduleOpeNo = hFRPF_POSLISTd_key;
                                    String previous_modulePOS_obj = hFRPF_POSLISTPOS_OBJ;
                                    log.info("", "### previous_moduleOpeNo   = ", previous_moduleOpeNo);
                                    log.info("", "### previous_modulePOS_obj = ", previous_modulePOS_obj);

                                    //--- Search the corresponding previous module openo from active module PF
                                    log.info("", "###### Search the corresponding next module openo from active module PF");
                                    hFRPF_POSLISTd_SeqNo = 0l;

                                    hFRPFd_theSystemKey = active_modulePF_systemkey;
                                    hFRPF_POSLISTd_key = previous_moduleOpeNo;

                                    String sql84 = "SELECT IDX_NO\n" +
                                            "      FROM OMPRF_PRSSSEQ\n" +
                                            "      WHERE REFKEY = ?1 \n" +
                                            "      AND LINK_KEY          = ?2 ";
                                    Object FRPF_POSLISTS5 = cimJpaRepository.queryOneColumnAndUnique(sql84, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);

                                    if (!CimObjectUtils.isEmpty(FRPF_POSLISTS5)) {
                                        log.info("", "###### SQLCODE == SQL_RC_OK");
                                        hFRPF_POSLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_POSLISTS5);
                                        modulePF_obj = active_modulePF_obj;
                                        modulePF_systemkey = active_modulePF_systemkey;
                                        modulePF_POSLIST_seqno = hFRPF_POSLISTd_SeqNo;
                                        FIND_FLAG = true;
                                        break;
                                    } else {
                                        log.info("", "###### SQLCODE == SP_SQL_NOT_FOUND");
                                        //---------------------------------------------------------------------------
                                        // Set previous POS from inactive return module PF
                                        //---------------------------------------------------------------------------
                                        log.info("", "###### Set previous POS from inactive return module PF");
                                        Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                        strProcess_OperationListForDurable_HelperDR_out.setSeqno(CimNumberUtils.intValue(backup_seqNo));
                                        strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(backup_mainPDID, backup_mainPDOBJ));
                                        strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(backup_stageID, backup_stageOBJ));
                                        strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(backup_moduleNo, previous_moduleOpeNo));
                                        Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                        strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                        tmpProcessRef.setProcessFlow(backup_PF_obj);
                                        tmpProcessRef.setMainProcessFlow(backup_mainPF_obj);
                                        tmpProcessRef.setModuleNumber(backup_moduleNo);
                                        tmpProcessRef.setModuleProcessFlow(backup_modulePF_obj);
                                        tmpProcessRef.setModulePOS(previous_modulePOS_obj);
                                        tmpProcessRef.setProcessOperationSpecification("");
                                        this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                        strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                        opeCount++;
                                        if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                                && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                                && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                            log.info("The route and operation to search are found.");
                                            opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                        }
                                    }
                                }
                            }// end of else if(i == 0)
                            i++;
                        }// end of while
                    }//end of else if(backup_modulePF_state == true)

                    if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        return strProcess_OperationListForDurableDR_out;
                    }

                    //-----------------------------------------------------------------------------
                    // Set previous POS from active backup module PF
                    //-----------------------------------------------------------------------------
                    log.info("", "###### Set previous POS from active backup module PF");
                    if (FIND_FLAG) {
                        log.info("", "###### FIND_FLAG == true");

                        log.info("", "###### FETCH LINK_KEY and PRSS_RKEY FROM OMPRF_PRSSSEQ");
                        hFRPF_POSLISTd_SeqNo = 0l;

                        hFRPFd_theSystemKey = modulePF_systemkey;
                        hFRPF_POSLISTd_SeqNo = modulePF_POSLIST_seqno;

                        String sql85 = "SELECT LINK_KEY,\n" +
                                "          PRSS_RKEY\n" +
                                "      FROM OMPRF_PRSSSEQ\n" +
                                "      WHERE REFKEY = ?1 \n" +
                                "      AND IDX_NO     <= ?2 \n" +
                                "      ORDER BY IDX_NO DESC";
                        List<Object[]> FRPF_POSLISTS5 = cimJpaRepository.query(sql85, hFRPFd_theSystemKey, hFRPF_POSLISTd_SeqNo);

                        count2 = 0;
                        while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                            Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                            strProcess_OperationListForDurable_HelperDR_out.setSeqno(CimNumberUtils.intValue(backup_seqNo));
                            strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(backup_mainPDID, backup_mainPDOBJ));
                            strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(backup_stageID, backup_stageOBJ));
                            Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                            strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                            tmpProcessRef.setProcessFlow(backup_PF_obj);
                            tmpProcessRef.setMainProcessFlow(backup_mainPF_obj);
                            tmpProcessRef.setModuleNumber(backup_moduleNo);
                            tmpProcessRef.setModuleProcessFlow(modulePF_obj);
                            log.info("", "### OMPRF_PRSSSEQ Fetch count = ", count2);
                            if (CimObjectUtils.isEmpty(FRPF_POSLISTS5) || count2 == FRPF_POSLISTS5.size()) {
                                break;
                            }
                            Object[] cFRPF_POSLIST10 = FRPF_POSLISTS5.get(count2);
                            hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST10[0]);
                            hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST10[1]);

                            log.info("", "###### Fetch OMPRF_PRSSSEQ result");
                            log.info("", "### hOMPRF_PRSSSEQd_key   = ", hFRPF_POSLISTd_key);
                            log.info("", "### hOMPRF_PRSSSEQPOS_OBJ = ", hFRPF_POSLISTPOS_OBJ);

                            //--- Set previous operation information on active backup module PF
                            log.info("", "###### Set next operation information on active return module PF");
                            strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(backup_moduleNo, hFRPF_POSLISTd_key));
                            tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                            tmpProcessRef.setProcessOperationSpecification("");
                            this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                            strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                            opeCount++;
                            count2++;
                            if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                    && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                    && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                log.info("The route and operation to search are found.");
                                opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                            }
                        }
                    }

                    if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        return strProcess_OperationListForDurableDR_out;
                    }

                    //-----------------------------------------------------------------------------
                    // (4-2-3-2) Get previous module PD from backup/active main PF
                    //-----------------------------------------------------------------------------
                    log.info("", "#################################################");
                    log.info("", "###### Get previous module PD from backup/active main PF");

                    // If main PF is active, set previous module PD's seq_no from backup main PF
                    if (backup_mainPF_state != 0) {
                        log.info("", "###### main PF is active, set previous module PD's seq_no from backup main PF");
                        mainPF_obj = backup_mainPF_obj;
                        mainPF_systemkey = backup_mainPF_systemkey;
                        mainPF_PDLIST_seqno = backup_mainPF_PDLIST_seqno - 1;
                        FIND_FLAG = true;
                    } else {
                        // If main PF is NOT active, search previous module PD's seq_no from active main PF
                        log.info("", "###### main PF is NOT active, search next module PD's seq_no from active main PF");
                        FIND_FLAG = false;

                        //--- Get active main PF information
                        log.info("", "###### Get active main PF information");

                        hFRPFMAINPD_ID = backup_mainPDID;
                        hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MAIN_FOR_MODULE;


                        String sql86 = "SELECT ID \n" +
                                "       FROM OMPRF\n" +
                                "       WHERE PRP_ID = ?1 \n" +
                                "       AND PRP_LEVEL  = ?2 \n" +
                                "       AND ACTIVE_FLAG     = '1' ";
                        Object FRPD3 = cimJpaRepository.queryOneColumnAndUnique(sql86, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                        Validations.check(CimObjectUtils.isEmpty(FRPD3), retCodeConfig.getNotFoundPfForDurable());
                        hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD3);
                        hFRPFPF_OBJ = CimObjectUtils.toString(FRPD3);

                        active_mainPF_systemkey = hFRPFd_theSystemKey;
                        active_mainPF_obj = hFRPFPF_OBJ;

                        log.info("", "###### Set active main PF information");
                        log.info("", "### active_mainPF_systemkey = ", active_mainPF_systemkey);
                        log.info("", "### active_mainPF_obj       = ", active_mainPF_obj);

                        i = 0;
                        while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                            // Search the previous module PD by finding the backup module number on active main PF
                            if (i == 0) {
                                log.info("", "###### Search the previous module PD by finding the backup module number on active main PF");
                                hFRPF_PDLISTd_SeqNo = 0l;

                                hFRPFd_theSystemKey = active_mainPF_systemkey;
                                hFRPF_PDLISTd_key = backup_moduleNo;

                                String sql87 = "SELECT IDX_NO,\n" +
                                        "             ROUTE_ID\n" +
                                        "       FROM OMPRF_ROUTESEQ\n" +
                                        "       WHERE REFKEY = ?1 \n" +
                                        "       AND LINK_KEY    = ?2 ";
                                Object[] activeMainPF = cimJpaRepository.queryOne(sql87, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);

                                if (!CimObjectUtils.isEmpty(activeMainPF)) {
                                    //--- Set the next module PD's seq_no on active module PF
                                    hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) activeMainPF[0]);
                                    String hFRPF_PDLISTPD_ID = CimObjectUtils.toString(activeMainPF[1]);
                                    log.info("", "###### Set the next module PD's seq_no on active module PF");
                                    mainPF_obj = active_mainPF_obj;
                                    mainPF_systemkey = active_mainPF_systemkey;
                                    mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo - 1;
                                    FIND_FLAG = true;
                                    break;
                                } else {
                                    log.info("", "###### do nothing...");
                                    // do nothing
                                }
                            }
                            // Search the previous module PD by finding the previous module number on active main PF
                            else {
                                log.info("", "###### Search the previous module PD by finding the previous module number on active main PF");
                                //--- Get the previous module PD's seq_no on backup main PF
                                log.info("", "###### Get the previous module PD's seq_no on backup main PF");
                                hFRPF_PDLISTd_SeqNo = 0l;

                                hFRPFd_theSystemKey = backup_mainPF_systemkey;
                                backup_mainPF_PDLIST_seqno--;
                                hFRPF_PDLISTd_SeqNo = backup_mainPF_PDLIST_seqno;
                                log.info("", "###### hOMPRF_ROUTESEQd_SeqNo = ", hFRPF_PDLISTd_SeqNo);

                                String sql88 = " SELECT ROUTE_ID,\n" +
                                        "              ROUTE_NO,\n" +
                                        "              STAGE_ID,\n" +
                                        "              STAGE_RKEY\n" +
                                        "        FROM OMPRF_ROUTESEQ\n" +
                                        "        WHERE REFKEY = ?1 \n" +
                                        "        AND IDX_NO      = ?2  ";
                                Object[] FRPF_POSLIST7 = cimJpaRepository.queryOne(sql88, hFRPFd_theSystemKey, hFRPF_PDLISTd_SeqNo);

                                if (CimObjectUtils.isEmpty(FRPF_POSLIST7)) {
                                    // previous module PD is nil
                                    log.info("", "###### previous module PD is nil");
                                    break;
                                } else {
                                    //--- Keep the previous module information on backup main PF
                                    String hFRPF_PDLISTPD_ID = CimObjectUtils.toString(FRPF_POSLIST7[0]);
                                    String hFRPF_PDLISTMODULE_NO = CimObjectUtils.toString(FRPF_POSLIST7[1]);
                                    hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(FRPF_POSLIST7[2]);
                                    hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(FRPF_POSLIST7[3]);
                                    log.info("", "###### Keep the previous module information on backup main PF");
                                    previous_modulePDID = hFRPF_PDLISTPD_ID;
                                    previous_moduleNo = hFRPF_PDLISTMODULE_NO;
                                    previous_stageID = hFRPF_PDLISTSTAGE_ID;
                                    previous_stageOBJ = hFRPF_PDLISTSTAGE_OBJ;

                                    //--- Search the corresponding previous module number from the active main PF
                                    log.info("", "###### Search the corresponding previous module number from the active main PF");
                                    hFRPF_PDLISTd_SeqNo = 0l;

                                    hFRPFd_theSystemKey = active_mainPF_systemkey;
                                    hFRPF_PDLISTd_key = previous_moduleNo;

                                    String sql89 = "SELECT IDX_NO\n" +
                                            "      FROM OMPRF_ROUTESEQ\n" +
                                            "      WHERE REFKEY = ?1 \n" +
                                            "      AND LINK_KEY    = ?2 ";
                                    Object FRPF_PDLIST7 = cimJpaRepository.queryOneColumnAndUnique(sql89, hFRPFd_theSystemKey, hFRPF_PDLISTd_key);

                                    if (!CimObjectUtils.isEmpty(FRPF_PDLIST7)) {
                                        log.info("", "###### SQLCODE == SQL_RC_OK");
                                        hFRPF_PDLISTd_SeqNo = CimNumberUtils.longValue((Number) FRPF_PDLIST7);
                                        mainPF_obj = active_mainPF_obj;
                                        mainPF_systemkey = active_mainPF_systemkey;
                                        mainPF_PDLIST_seqno = hFRPF_PDLISTd_SeqNo;
                                        FIND_FLAG = true;
                                        break;
                                    } else {
                                        log.info("", "###### SQLCODE == SP_SQL_NOT_FOUND");
                                        //---------------------------------------------------------------------------
                                        // Set previous POS from active previous module PF and backup(inactive) main PF
                                        //---------------------------------------------------------------------------
                                        //--- Get active module PF information
                                        log.info("", "###### Get active module PF information");

                                        hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                                        //--- If module PDID's version is "##", convert it to real PDID
                                        String str_version_id = cimFrameWorkGlobals.extractVersionFromID(previous_modulePDID);

                                        if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                                            log.info("", "### VERSION_ID == '##'");
                                            String hFRPDPD_ID = hFRPF_PDLISTPD_ID;
                                            String sql90 = "SELECT ACTIVE_VER_ID \n" +
                                                    "      FROM OMPRP \n" +
                                                    "      WHERE PRP_ID = ?1 \n" +
                                                    "      AND PRP_LEVEL = ?2 ";
                                            Object hFRPDACTIVE_ID = cimJpaRepository.queryOneColumnAndUnique(sql90, hFRPDPD_ID, hFRPFPD_LEVEL);
                                            Validations.check(CimObjectUtils.isEmpty(hFRPDACTIVE_ID), retCodeConfig.getNotFoundProcessDefinition());
                                            hFRPFMAINPD_ID = CimObjectUtils.toString(hFRPDACTIVE_ID);
                                            log.info("", "### VERSION_ID == '##'");
                                        } else {
                                            hFRPFMAINPD_ID = previous_modulePDID;
                                        }

                                        log.info("", "### hOMPRFMAINPD_ID = ", hFRPFMAINPD_ID);

                                        String sql91 = "SELECT ID \n" +
                                                "       FROM OMPRF\n" +
                                                "       WHERE PRP_ID = ?1 \n" +
                                                "       AND PRP_LEVEL  = ?2 \n" +
                                                "       AND ACTIVE_FLAG     = '1' ";
                                        Object FRPD4 = cimJpaRepository.queryOneColumnAndUnique(sql91, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                                        Validations.check(CimObjectUtils.isEmpty(FRPD4), retCodeConfig.getNotFoundPfForDurable());
                                        hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD4);
                                        hFRPFPF_OBJ = CimObjectUtils.toString(FRPD4);

                                        String previous_modulePF_systemkey = hFRPFd_theSystemKey;
                                        String previous_modulePF_obj = hFRPFPF_OBJ;

                                        log.info("", "###### Get active module PF information");
                                        log.info("", "### previous_modulePF_systemkey = ", previous_modulePF_systemkey);
                                        log.info("", "### previous_modulePF_obj = ", previous_modulePF_obj);

                                        //--- Set previous operation information on active module PF
                                        log.info("", "###### Set previous operation information on active module PF");
                                        hFRPFd_theSystemKey = previous_modulePF_systemkey;

                                        String sql92 = "SELECT LINK_KEY,\n" +
                                                "            PRSS_RKEY\n" +
                                                "      FROM OMPRF_PRSSSEQ\n" +
                                                "      WHERE REFKEY = ?1 \n" +
                                                "      ORDER BY IDX_NO";
                                        List<Object[]> FRPF_POSLISTS6 = cimJpaRepository.query(sql92, hFRPFd_theSystemKey);


                                        count2 = 0;
                                        while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                                            Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                            strProcess_OperationListForDurable_HelperDR_out.setSeqno(CimNumberUtils.intValue(backup_seqNo));
                                            strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(backup_mainPDID, backup_mainPDOBJ));
                                            strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(previous_stageID, previous_stageOBJ));
                                            Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                            strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                            tmpProcessRef.setProcessFlow(backup_PF_obj);
                                            tmpProcessRef.setMainProcessFlow(backup_mainPF_obj);
                                            tmpProcessRef.setModuleNumber(previous_moduleNo);
                                            tmpProcessRef.setModuleProcessFlow(previous_modulePF_obj);
                                            if (CimObjectUtils.isEmpty(FRPF_POSLISTS6) || count2 == FRPF_POSLISTS6.size()) {
                                                break;
                                            }
                                            Object[] FRPF_POSLIST11 = FRPF_POSLISTS6.get(count2);
                                            hFRPF_POSLISTd_key = CimObjectUtils.toString(FRPF_POSLIST11[0]);
                                            hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(FRPF_POSLIST11[1]);

                                            log.info("", "###### Fetch OMPRF_PRSSSEQ result");
                                            log.info("", "### hOMPRF_PRSSSEQd_key   = ", hFRPF_POSLISTd_key);
                                            log.info("", "### hOMPRF_PRSSSEQPOS_OBJ = ", hFRPF_POSLISTPOS_OBJ);

                                            //--- Set next operation information on active module PF
                                            strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(previous_moduleNo, hFRPF_POSLISTd_key));
                                            tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                                            tmpProcessRef.setProcessOperationSpecification("");
                                            this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                            strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                            opeCount++;
                                            count2++;
                                            if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                                    && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                                    && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                                log.info("The route and operation to search are found.");
                                                opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                            }
                                        }
                                    }
                                }
                            }// end of else if(i == 0)
                            i++;
                        }// end of while
                    }// end of else if(mainPF_state == true)

                    if (opeCount == strProcess_OperationListForDurableDR_in.getSearchCount()) {
                        return strProcess_OperationListForDurableDR_out;
                    }

                    //-----------------------------------------------------------------------------
                    // Set previous POS from active previous module PF and active main PF
                    //-----------------------------------------------------------------------------
                    log.info("", "###### Set previous POS from active previous module PF and active main PF");
                    if (FIND_FLAG) {
                        log.info("", "###### FIND_FLAG == true");

                        //--- Get previous module pdid, module_no and stage from active main PF
                        log.info("", "###### Get previous module pdid, module_no and stage from active main PF");
                        hFRPF_PDLISTd_SeqNo = 0l;

                        hFRPFd_theSystemKey = mainPF_systemkey;
                        hFRPF_PDLISTd_SeqNo = mainPF_PDLIST_seqno;

                        String sql93 = "SELECT ROUTE_ID,\n" +
                                "       ROUTE_NO,\n" +
                                "       STAGE_ID,\n" +
                                "       STAGE_RKEY\n" +
                                "       FROM OMPRF_ROUTESEQ\n" +
                                "       WHERE REFKEY = ?1 \n" +
                                "       AND IDX_NO <= ?2 \n" +
                                "       ORDER BY IDX_NO DESC ";
                        List<Object[]> FRPF_POSLISTS = cimJpaRepository.query(sql93, hFRPFd_theSystemKey, hFRPF_PDLISTd_SeqNo);

                        count2 = 0;
                        while (true) {
                            log.info("", "###### OMPRF_ROUTESEQ Fetch count = ", count2);
                            if (CimObjectUtils.isEmpty(FRPF_POSLISTS) || count2 == FRPF_POSLISTS.size()) {
                                break;
                            }
                            Object[] cFRPF_PDLIST7 = FRPF_POSLISTS.get(count2);
                            String hFRPF_PDLISTPD_ID = CimObjectUtils.toString(cFRPF_PDLIST7[0]);
                            String hFRPF_PDLISTMODULE_NO = CimObjectUtils.toString(cFRPF_PDLIST7[1]);
                            hFRPF_PDLISTSTAGE_ID = CimObjectUtils.toString(cFRPF_PDLIST7[2]);
                            hFRPF_PDLISTSTAGE_OBJ = CimObjectUtils.toString(cFRPF_PDLIST7[3]);

                            log.info("", "###### OMPRF_ROUTESEQ Fetch result ");
                            log.info("", "### hOMPRF_ROUTESEQPD_ID     = ", hFRPF_PDLISTPD_ID);
                            log.info("", "### hOMPRF_ROUTESEQMODULE_NO = ", hFRPF_PDLISTMODULE_NO);
                            log.info("", "### hOMPRF_ROUTESEQSTAGE_ID  = ", hFRPF_PDLISTSTAGE_ID);
                            log.info("", "### hOMPRF_ROUTESEQSTAGE_OBJ = ", hFRPF_PDLISTSTAGE_OBJ);

                            //--- Get active module PF information
                            log.info("", "### Get active module PF information");

                            hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_MODULE;
                            //--- If module PDID's version is "##", convert it to real PDID

                            String str_version_id = cimFrameWorkGlobals.extractVersionFromID(hFRPF_PDLISTPD_ID);

                            if (CimStringUtils.equals(str_version_id, SP_ACTIVE_VERSION)) {
                                log.info("", "### VERSION_ID == '##'");
                                String hFRPDPD_ID = hFRPF_PDLISTPD_ID;
                                String sql94 = "SELECT ACTIVE_VER_ID \n" +
                                        "      FROM OMPRP \n" +
                                        "      WHERE PRP_ID = ?1 \n" +
                                        "      AND PRP_LEVEL = ?2 ";
                                Object hFRPDACTIVE_ID = cimJpaRepository.queryOneColumnAndUnique(sql94, hFRPDPD_ID, hFRPFPD_LEVEL);
                                Validations.check(CimObjectUtils.isEmpty(hFRPDACTIVE_ID), retCodeConfig.getNotFoundProcessDefinition());
                                hFRPFMAINPD_ID = CimObjectUtils.toString(hFRPDACTIVE_ID);
                                log.info("", "### VERSION_ID == '##'");
                            } else {
                                hFRPFMAINPD_ID = previous_modulePDID;
                            }
                            log.info("", "### hOMPRFMAINPD_ID = ", hFRPFMAINPD_ID);

                            String sql95 = "SELECT ID \n" +
                                    "       FROM OMPRF\n" +
                                    "       WHERE PRP_ID = ?1 \n" +
                                    "       AND PRP_LEVEL  = ?2 \n" +
                                    "       AND ACTIVE_FLAG     = '1' ";
                            Object FRPD4 = cimJpaRepository.queryOneColumnAndUnique(sql95, hFRPFMAINPD_ID, hFRPFPD_LEVEL);
                            Validations.check(CimObjectUtils.isEmpty(FRPD4), retCodeConfig.getNotFoundPfForDurable());
                            hFRPFd_theSystemKey = CimObjectUtils.toString(FRPD4);
                            hFRPFPF_OBJ = CimObjectUtils.toString(FRPD4);

                            log.info("", "###### OMPRF_ROUTESEQ select result ");
                            log.info("", "### hOMPRFPF_OBJ = ", hFRPFPF_OBJ);
                            log.info("", "### hOMPRFd_theSystemKey = ", hFRPFd_theSystemKey);

                            //--- Set previous operation information on active module PF
                            log.info("", "###### Set previous operation information on active module PF");


                            String sql96 = "SELECT LINK_KEY,\n" +
                                    "            PRSS_RKEY\n" +
                                    "      FROM OMPRF_PRSSSEQ\n" +
                                    "      WHERE REFKEY = ?1 \n" +
                                    "      ORDER BY IDX_NO";
                            List<Object[]> FRPF_POSLISTS6 = cimJpaRepository.query(sql96, hFRPFd_theSystemKey);

                            count3 = 0;
                            while (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()) {
                                Infos.DurableOperationNameAttributes strProcess_OperationListForDurable_HelperDR_out = new Infos.DurableOperationNameAttributes();
                                strProcess_OperationListForDurable_HelperDR_out.setSeqno(CimNumberUtils.intValue(backup_seqNo));
                                strProcess_OperationListForDurable_HelperDR_out.setRouteID(ObjectIdentifier.build(backup_mainPDID, backup_mainPDOBJ));
                                Infos.ProcessRef tmpProcessRef = new Infos.ProcessRef();
                                strProcess_OperationListForDurable_HelperDR_out.setProcessRef(tmpProcessRef);
                                tmpProcessRef.setProcessFlow(backup_PF_obj);
                                tmpProcessRef.setMainProcessFlow(mainPF_obj);
                                strProcess_OperationListForDurable_HelperDR_out.setStageID(ObjectIdentifier.build(hFRPF_PDLISTSTAGE_ID, hFRPF_PDLISTSTAGE_OBJ));
                                tmpProcessRef.setModuleNumber(hFRPF_PDLISTMODULE_NO);
                                tmpProcessRef.setModuleProcessFlow(hFRPFPF_OBJ);
                                if (CimObjectUtils.isEmpty(FRPF_POSLISTS6) || count3 == FRPF_POSLISTS6.size()) {
                                    break;
                                }
                                Object[] cFRPF_POSLIST12 = FRPF_POSLISTS6.get(count3);
                                hFRPF_POSLISTd_key = CimObjectUtils.toString(cFRPF_POSLIST12[0]);
                                hFRPF_POSLISTPOS_OBJ = CimObjectUtils.toString(cFRPF_POSLIST12[1]);
                                log.info("", "###### OMPRF_PRSSSEQ Fetch result ");
                                log.info("", "### hOMPRF_PRSSSEQd_key = ", hFRPF_POSLISTd_key);
                                log.info("", "### hOMPRF_PRSSSEQPOS_OBJ = ", hFRPF_POSLISTPOS_OBJ);

                                strProcess_OperationListForDurable_HelperDR_out.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(hFRPF_PDLISTMODULE_NO, hFRPF_POSLISTd_key));
                                tmpProcessRef.setModulePOS(hFRPF_POSLISTPOS_OBJ);
                                tmpProcessRef.setProcessOperationSpecification("");
                                this.processOperationListForDurableHelperDR(objCommon, strProcess_OperationListForDurable_HelperDR_out);
                                strDurableOperationNameAttributes.add(strProcess_OperationListForDurable_HelperDR_out);
                                opeCount++;
                                count3++;
                                if (opeCount < strProcess_OperationListForDurableDR_in.getSearchCount()
                                        && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getRouteID(), strProcess_OperationListForDurableDR_in.getSearchRouteID())
                                        && ObjectIdentifier.equalsWithValue(strProcess_OperationListForDurable_HelperDR_out.getOperationID(), strProcess_OperationListForDurableDR_in.getSearchOperationNumber())) {
                                    log.info("The route and operation to search are found.");
                                    opeCount = strProcess_OperationListForDurableDR_in.getSearchCount();
                                }
                            }
                            count2++;
                        }
                    }// end of if(FIND_FLAG == true)
                    count++;
                }
            }
        }
        return strProcess_OperationListForDurableDR_out;
    }

    @Override
    public void processOperationListForDurableHelperDR(Infos.ObjCommon objCommon, Infos.DurableOperationNameAttributes objProcessOperationListForDurableHelperDROut) {
        //-----------------------------------------------------------------------------
        //  Check input parameter's.
        //-----------------------------------------------------------------------------
        objProcessOperationListForDurableHelperDROut.setMandatoryOperationFlag(false);
        objProcessOperationListForDurableHelperDROut.setStandardCycleTime(0d);
        objProcessOperationListForDurableHelperDROut.setTestType("");
        String operationNumber = objProcessOperationListForDurableHelperDROut.getOperationNumber();
        //此处和源码不一致，源码调用的是完整的operationNumber和processFlow
        //根据我们的逻辑，我调用的是是moduleNum和ModuleProcessFlow
        String hFRPF_POSLISTd_key = operationNumber.substring(operationNumber.indexOf(".") + 1);
        Infos.ProcessRef processRef = objProcessOperationListForDurableHelperDROut.getProcessRef();

        //-----------------------------------------------------------------------------
        //  Get information from MainPOS.
        //-----------------------------------------------------------------------------
        log.info("###### Get information from MainPOS.");
        String hFRPOSd_theSystemKey = "";
        if (CimStringUtils.isEmpty(objProcessOperationListForDurableHelperDROut.getProcessRef().getProcessOperationSpecification())) {
            String hFRPFd_theSystemKey = objProcessOperationListForDurableHelperDROut.getProcessRef().getModuleProcessFlow();
            if (CimStringUtils.isNotEmpty(hFRPFd_theSystemKey)) {
                String sql1 = "SELECT PRSS_RKEY FROM OMPRF_PRSSSEQ\n" +
                        "     WHERE REFKEY = ?1 \n" +
                        "     AND LINK_KEY    = ?2 ";
                Object FRPF_POSLIST = cimJpaRepository.queryOneColumnAndUnique(sql1, hFRPFd_theSystemKey, hFRPF_POSLISTd_key);
                if (CimObjectUtils.isEmpty(FRPF_POSLIST)) {
                    processRef.setProcessOperationSpecification("*");
                } else {
                    hFRPOSd_theSystemKey = CimObjectUtils.toString(FRPF_POSLIST);
                    processRef.setProcessOperationSpecification(hFRPOSd_theSystemKey);
                }
            } else {
                processRef.setProcessOperationSpecification("*");
                Validations.check(retCodeConfig.getSomeopelistDataError());
            }
        } else {
            hFRPOSd_theSystemKey = objProcessOperationListForDurableHelperDROut.getProcessRef().getProcessOperationSpecification();
        }
        if (!CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.LOT_FUTURE_ACTION_LIST_INQ.getValue())) {
            log.info("Transaction ID is not LOT_FUTURE_ACTION_LIST_INQ.");
            if (CimStringUtils.isNotEmpty(hFRPOSd_theSystemKey)) {
                String sql2 = "SELECT PHOTO_LAYER FROM OMPRSS\n" +
                        "      WHERE ID = ?1 ";
                Object FRPF_POSLIST = cimJpaRepository.queryOneColumnAndUnique(sql2, hFRPOSd_theSystemKey);
                objProcessOperationListForDurableHelperDROut.setMaskLevel("*");
                if (!CimObjectUtils.isEmpty(FRPF_POSLIST)) {
                    String hFRPOSPHOTO_LAYER = CimObjectUtils.toString(FRPF_POSLIST);
                    objProcessOperationListForDurableHelperDROut.setMaskLevel(hFRPOSPHOTO_LAYER);
                }
            } else {
                objProcessOperationListForDurableHelperDROut.setMaskLevel("*");
            }
        }
        //-----------------------------------------------------------------------------
        //  Get process information from module POS
        //-----------------------------------------------------------------------------
        String tmpModulePOSKey = processRef.getModulePOS();
        String hFRPOSPD_ID = "";
        String hFRPOSPD_OBJ = "";
        boolean hFRPOSMANDATORY_FLAG = false;
        if (CimStringUtils.isNotEmpty(hFRPOSd_theSystemKey)) {
            String sql3 = "SELECT STEP_ID, STEP_RKEY, COMPULSORY_FLAG\n" +
                    "      FROM OMPRSS\n" +
                    "      WHERE ID = ?1 ";
            Object[] FRPOS = cimJpaRepository.queryOne(sql3, hFRPOSd_theSystemKey);
            if (!CimObjectUtils.isEmpty(FRPOS)) {
                hFRPOSPD_ID = CimObjectUtils.toString(FRPOS[0]);
                hFRPOSPD_OBJ = CimObjectUtils.toString(FRPOS[1]);
                hFRPOSMANDATORY_FLAG = CimBooleanUtils.isTrue(String.valueOf(FRPOS[2]));
            }
            objProcessOperationListForDurableHelperDROut.setMandatoryOperationFlag(hFRPOSMANDATORY_FLAG);
        } else {
            //*** Get PD_ID from FRDRBLPO if module POS is not filled ***//
            if (!CimObjectUtils.isEmpty(objProcessOperationListForDurableHelperDROut.getObjrefPO())) {
                String sql4 = "SELECT STEP_ID, STEP_RKEY\n" +
                        "     FROM OMDRBLPROPE\n" +
                        "     WHERE ID = ?1 ";
                Object[] FRDRBLPO = cimJpaRepository.queryOne(sql4, objProcessOperationListForDurableHelperDROut.getObjrefPO());
                if (!CimObjectUtils.isEmpty(FRDRBLPO)) {
                    String hFRDRBLPOPD_ID = CimObjectUtils.toString(FRDRBLPO[0]);
                    String hFRDRBLPOPD_OBJ = CimObjectUtils.toString(FRDRBLPO[1]);
                }
            }
        }
        //-----------------------------------------------------------------------------
        //  Get stage group
        //-----------------------------------------------------------------------------
        if (!CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.LOT_FUTURE_ACTION_LIST_INQ.getValue())) {
            ObjectIdentifier stageID = objProcessOperationListForDurableHelperDROut.getStageID();
            if (ObjectIdentifier.isNotEmpty(stageID)) {
                String hFRSTAGESTAGE_ID = stageID.getValue();
                String sql5 = "SELECT STAGE_GRP_ID, STAGE_GRP_RKEY\n" +
                        "     FROM OMSTAGE\n" +
                        "     WHERE STAGE_ID = ?1 ";
                Object[] OMSTAGE = cimJpaRepository.queryOne(sql5, hFRSTAGESTAGE_ID);
                objProcessOperationListForDurableHelperDROut.setStageGroupID(ObjectIdentifier.build("*", "*"));
                if (!CimObjectUtils.isEmpty(OMSTAGE)) {
                    String hFRSTAGESTAGEGRP_ID = CimObjectUtils.toString(OMSTAGE[0]);
                    String hFRSTAGESTAGEGRP_OBJ = CimObjectUtils.toString(OMSTAGE[1]);
                    objProcessOperationListForDurableHelperDROut.setStageGroupID(ObjectIdentifier.build(hFRSTAGESTAGEGRP_ID, hFRSTAGESTAGEGRP_OBJ));
                }

            } else {
                objProcessOperationListForDurableHelperDROut.setStageGroupID(ObjectIdentifier.build("*", "*"));
            }
        }
        //-----------------------------------------------------------------------------
        //  Get process information from Process Definition
        //-----------------------------------------------------------------------------
        String hFRPFPD_LEVEL = SP_PD_FLOWLEVEL_OPERATION;
        String hFRPDd_theSystemKey = "*";
        String hFRPDOPE_NAME = "*";
        String hFRPDINSPECTION_TYPE = "*";
        String hFRPDDEPARTMENT = "*";
        Double hFRPDSTD_CYCLE_TIME = 0d;
        String hFRPDVERSION_ID = "*";
        String hFRPDACTIVE_ID = "*";
        String hFRPDACTIVE_OBJ = "*";
        if (CimStringUtils.isNotEmpty(hFRPOSPD_ID)) {
            String sql6 = "SELECT ID, \n" +
                    "     OPE_NAME, \n" +
                    "     PRP_ID, \n" +   // PRP_ID 代替
                    "     DEPT, \n" +
                    "     STD_CYCLE_TIME,\n" +
                    "     VERSION_ID, \n" +
                    "     ACTIVE_VER_ID, \n" +
                    "     ACTIVE_VER_RKEY\n" +
                    "     FROM OMPRP\n" +
                    "     WHERE PRP_ID  = ?1 \n" +
                    "     AND PRP_LEVEL = ?2 ";
            Object[] FRPD = cimJpaRepository.queryOne(sql6, hFRPOSPD_ID, hFRPFPD_LEVEL);
            if (CimObjectUtils.isEmpty(FRPD)) {
                objProcessOperationListForDurableHelperDROut.setOperationID(ObjectIdentifier.build(hFRPOSPD_ID, hFRPOSPD_OBJ));
                objProcessOperationListForDurableHelperDROut.setOperationName("*");
                objProcessOperationListForDurableHelperDROut.setInspectionType("*");
                objProcessOperationListForDurableHelperDROut.setDepartmentNumber("*");
            } else {
                hFRPDd_theSystemKey = CimObjectUtils.toString(FRPD[0]);
                hFRPDOPE_NAME = CimObjectUtils.toString(FRPD[1]);
                hFRPDINSPECTION_TYPE = CimObjectUtils.toString(FRPD[2]);
                hFRPDDEPARTMENT = CimObjectUtils.toString(FRPD[3]);
                hFRPDSTD_CYCLE_TIME = CimNumberUtils.doubleValue((Number) FRPD[4]);
                hFRPDVERSION_ID = CimObjectUtils.toString(FRPD[5]);
                hFRPDACTIVE_ID = CimObjectUtils.toString(FRPD[6]);
                hFRPDACTIVE_OBJ = CimObjectUtils.toString(FRPD[7]);
                if (!CimStringUtils.equals(hFRPDVERSION_ID, SP_ACTIVE_VERSION)) {
                    objProcessOperationListForDurableHelperDROut.setOperationID(ObjectIdentifier.build(hFRPOSPD_ID, hFRPOSPD_OBJ));
                } else {
                    objProcessOperationListForDurableHelperDROut.setOperationID(ObjectIdentifier.build(hFRPDACTIVE_ID, hFRPDACTIVE_OBJ));
                }
                objProcessOperationListForDurableHelperDROut.setOperationName(hFRPDOPE_NAME);
                objProcessOperationListForDurableHelperDROut.setInspectionType(hFRPDINSPECTION_TYPE);
                objProcessOperationListForDurableHelperDROut.setDepartmentNumber(hFRPDDEPARTMENT);
                objProcessOperationListForDurableHelperDROut.setStandardCycleTime(hFRPDSTD_CYCLE_TIME);
            }
        } else {
            objProcessOperationListForDurableHelperDROut.setOperationID(ObjectIdentifier.build("*", "*"));
            objProcessOperationListForDurableHelperDROut.setOperationName("*");
            objProcessOperationListForDurableHelperDROut.setInspectionType("*");
            objProcessOperationListForDurableHelperDROut.setDepartmentNumber("*");
        }
        if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.LOT_FUTURE_ACTION_LIST_INQ.getValue())) {
            return;
        }
        //-----------------------------------------------------------------------------
        //  Get equipmentID
        //-----------------------------------------------------------------------------
        String eqpListFlag = StandardProperties.OM_STEP_EQP_LIST_INQ.getValue();
        if (!CimStringUtils.equals(eqpListFlag, "0")) {
            if (!CimStringUtils.equals(objProcessOperationListForDurableHelperDROut.getOperationID().getValue(), "*")) {
                List<ObjectIdentifier> equipmentIDSeq;
                equipmentIDSeq = processMethod.processDispatchEquipmentsForDurableGetDR(objCommon, objProcessOperationListForDurableHelperDROut.getOperationID());
                objProcessOperationListForDurableHelperDROut.setMachines(equipmentIDSeq);
            } else {
                objProcessOperationListForDurableHelperDROut.setMachines(new ArrayList<>());
            }
        } else {
            objProcessOperationListForDurableHelperDROut.setMachines(new ArrayList<>());
        }
        //-----------------------------------------------------------------------------
        //  Set Schedule Information
        //-----------------------------------------------------------------------------
        String hFRDRBLPOPASS_COUNT = "";
        String hFRDRBLPOACTUAL_START_TIME = "";
        String hFRDRBLPOACTUAL_END_TIME = "";
        String hFRDRBLPOASGN_EQP_ID = "";
        String hFRDRBLPOASGN_EQP_OBJ = "";
        if (!CimObjectUtils.isEmpty(objProcessOperationListForDurableHelperDROut.getObjrefPO())) {
            String sql7 = "SELECT PASS_COUNT, \n" +
                    "     ACTUAL_MOVIN_TIME, \n" +
                    "     ACTUAL_MOVOUT_TIME, \n" +
                    "     ALLOC_EQP_ID, \n" +
                    "     ALLOC_EQP_RKEY\n" +
                    "     FROM OMDRBLPROPE\n" +
                    "     WHERE  ID = ?1 ";
            Object[] FRDRBLPO = cimJpaRepository.queryOne(sql7, objProcessOperationListForDurableHelperDROut.getObjrefPO());
            if (CimObjectUtils.isEmpty(FRDRBLPO)) {
                Validations.check(retCodeConfig.getNotFoundPoForDurable(), "", "");
            } else {
                hFRDRBLPOPASS_COUNT = CimObjectUtils.toString(FRDRBLPO[0]);
                hFRDRBLPOACTUAL_START_TIME = CimObjectUtils.toString(FRDRBLPO[1]);
                hFRDRBLPOACTUAL_END_TIME = CimObjectUtils.toString(FRDRBLPO[2]);
                hFRDRBLPOASGN_EQP_ID = CimObjectUtils.toString(FRDRBLPO[3]);
                hFRDRBLPOASGN_EQP_OBJ = CimObjectUtils.toString(FRDRBLPO[4]);
                objProcessOperationListForDurableHelperDROut.setOperationPass(hFRDRBLPOPASS_COUNT);
                objProcessOperationListForDurableHelperDROut.setPlannedStartTime("");
                objProcessOperationListForDurableHelperDROut.setPlannedEndTime("");
                objProcessOperationListForDurableHelperDROut.setPlannedMachine(null);
                objProcessOperationListForDurableHelperDROut.setActualStartTime(hFRDRBLPOACTUAL_START_TIME);
                objProcessOperationListForDurableHelperDROut.setActualCompTime(hFRDRBLPOACTUAL_END_TIME);
                if (CimStringUtils.isEmpty(hFRDRBLPOASGN_EQP_ID) && CimStringUtils.isEmpty(hFRDRBLPOASGN_EQP_OBJ)) {
                    objProcessOperationListForDurableHelperDROut.setAssignedMachine(null);
                } else {
                    objProcessOperationListForDurableHelperDROut.setAssignedMachine(ObjectIdentifier.build(hFRDRBLPOASGN_EQP_ID, hFRDRBLPOASGN_EQP_OBJ));
                }
            }
        } else {
            objProcessOperationListForDurableHelperDROut.setObjrefPO("");
            objProcessOperationListForDurableHelperDROut.setOperationPass("");
            objProcessOperationListForDurableHelperDROut.setPlannedStartTime("");
            objProcessOperationListForDurableHelperDROut.setPlannedEndTime("");
            objProcessOperationListForDurableHelperDROut.setPlannedMachine(null);
            objProcessOperationListForDurableHelperDROut.setActualStartTime("");
            objProcessOperationListForDurableHelperDROut.setActualCompTime("");
            objProcessOperationListForDurableHelperDROut.setAssignedMachine(null);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessCheckGatePassForDurablein
     * @return void
     * @throws
     * @author ho
     * @date 2020/7/8 15:16
     */
    @Override
    public void processCheckGatePassForDurable(Infos.ObjCommon strObjCommonIn, Infos.ProcessCheckGatePassForDurableIn strProcessCheckGatePassForDurablein) {
        Validations.check(null == strObjCommonIn || null == strProcessCheckGatePassForDurablein, retCodeConfig.getInvalidInputParam());
        Durable aDurable = null;
        String durableCategory = strProcessCheckGatePassForDurablein.getDurableCategory();
        ObjectIdentifier durableID = strProcessCheckGatePassForDurablein.getDurableID();
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

        CimDurableProcessOperation aDurablePO = aDurable.getDurableProcessOperation();

        Validations.check(null == aDurablePO, retCodeConfig.getNotFoundDurablePo());
        //--------------------------------------------------------------------------------------------------
        //  Check Mandatory Operation
        //  First, check it for ProcessOperationSpecification, if it is brank, check it for ProcessDefinition
        //--------------------------------------------------------------------------------------------------
        if (aDurablePO.isMandatoryOperation()) {
            Validations.check(true, retCodeConfig.getCannotPassOperation(), ObjectIdentifier.fetchValue(durableID)
                    , CimObjectUtils.toString(aDurablePO.getOperationName()));
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessGetReturnOperationForDurablein
     * @return com.fa.cim.dto.Infos.ProcessGetReturnOperationForDurableOut
     * @throws
     * @author ho
     * @date 2020/7/10 15:38
     */
    @Override
    public Infos.ProcessGetReturnOperationForDurableOut processGetReturnOperationForDurable(
            Infos.ObjCommon strObjCommonIn,
            Infos.ProcessGetReturnOperationForDurableIn strProcessGetReturnOperationForDurablein) {
        Infos.ProcessGetReturnOperationForDurableOut strProcessGetReturnOperationForDurableout = new Infos.ProcessGetReturnOperationForDurableOut();
        log.info("PPTManager_i::process_GetReturnOperationForDurable");
        CimDurableProcessOperation aProcessOperation = null;

        //--------------------------------------------------------------------------------------------------
        // Get sub route information
        //--------------------------------------------------------------------------------------------------
        CimProcessDefinition aProcessDefinition;
        aProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class,
                strProcessGetReturnOperationForDurablein.getSubRouteID());

        if (CimStringUtils.equals(strProcessGetReturnOperationForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strProcessGetReturnOperationForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // Get current process information
            //--------------------------------------------------------------------------------------------------
            aProcessOperation = aCassette.getDurableProcessOperation();
        } else if (CimStringUtils.equals(strProcessGetReturnOperationForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strProcessGetReturnOperationForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // Get current process information
            //--------------------------------------------------------------------------------------------------
            aProcessOperation = aReticlePod.getDurableProcessOperation();
        } else if (CimStringUtils.equals(strProcessGetReturnOperationForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strProcessGetReturnOperationForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // Get current process information
            //--------------------------------------------------------------------------------------------------
            aProcessOperation = aReticle.getDurableProcessOperation();
        }

        if (aProcessOperation == null) {
            log.info("{}", "aProcessOperation is nil");
            Validations.check(true,
                    retCodeConfig.getNotFoundDurablePo(),
                    ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getDurableID()));
        }

        //--------------------------------------------------------------------------------------------------
        // Find sub route information from previous process if sub route is rework route,
        //--------------------------------------------------------------------------------------------------
        String routeType;
        routeType = aProcessDefinition.getProcessDefinitionType();

        strProcessGetReturnOperationForDurableout.setProcessDefinitionType(routeType);

        ProcessDTO.ProcessFlowConnection flowConnection = null;
        if (CimStringUtils.equals(routeType, SP_MAINPDTYPE_DURABLEREWORK)) {

            try {
                flowConnection = aProcessOperation.findReworkProcessFlowConnection(ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getSubRouteID()));
            } catch (CoreFrameworkException ex) {
                log.info("{}", "catch framework exceptions...");
                ;
                Validations.check(true,
                        retCodeConfigEx.getNotFoundDurableSubroute(),
                        ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getSubRouteID()),
                        ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getDurableID()));
            }
        } else {
            //--------------------------------------------------------------------------------------------------
            // Find sub route information from current process if sub route is branch route,
            //--------------------------------------------------------------------------------------------------
            try {
                flowConnection = aProcessOperation.findProcessFlowConnection(ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getSubRouteID()));
            } catch (CoreFrameworkException ex) {
                log.info("{}", "catch framework exceptions...");
                Validations.check(true,
                        retCodeConfigEx.getNotFoundDurableSubroute(),
                        ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getSubRouteID()),
                        ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getDurableID()));
            }
        }

        if (null != flowConnection) {
            if (CimStringUtils.length(flowConnection.getReturnOperationNumber()) == 0) {
                Validations.check(true,
                        retCodeConfigEx.getNotFoundDurableSubroute(),
                        ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getSubRouteID()),
                        ObjectIdentifier.fetchValue(strProcessGetReturnOperationForDurablein.getDurableID()));
            }
            strProcessGetReturnOperationForDurableout.setOperationNumber(flowConnection.getReturnOperationNumber());
        }

        log.info("PPTManager_i::process_GetReturnOperationForDurable");
        return strProcessGetReturnOperationForDurableout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessdurableReworkCountCheckin
     * @return void
     * @throws
     * @author ho
     * @date 2020/7/13 9:56
     */
    public void processDurableReworkCountCheck(
            Infos.ObjCommon strObjCommonIn,
            Infos.ProcessDurableReworkCountCheckIn strProcessdurableReworkCountCheckin) {
        log.info("PPTManager_i::process_durableReworkCount_Check");

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        CimDurableProcessFlowContext durablePFX;
        boolean bOverMaxReworkCheckFlag = false;
        boolean bOverMaxProcessCheckFlag = false;
        CimDurableProcessOperation aDurablePO;
        List<ProcessDTO.MaxProcessCount> maxProcessCountSeq;

        if (CimStringUtils.equals(strProcessdurableReworkCountCheckin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strProcessdurableReworkCountCheckin.getDurableID());

            /*---------------------------*/
            /* Get Process Flow Context  */
            /*---------------------------*/
            durablePFX = aCassette.getDurableProcessFlowContext();

            if (durablePFX == null) {
                log.info("{}", "durablePFX is nil");
                Validations.check(true,
                        retCodeConfig.getNotFoundPfx(), "");
            }

            bOverMaxReworkCheckFlag = aCassette.isOverDefaultMaxReworkCount();

            bOverMaxProcessCheckFlag = durablePFX.isOverDefaultMaxProcessCount();

            if (bOverMaxReworkCheckFlag || bOverMaxProcessCheckFlag) {
                log.info("{}", "bOverMaxReworkCheckFlag == true or bOverMaxProcessCheckFlag == true");
                aDurablePO = aCassette.getDurableProcessOperation();

                if (aDurablePO == null) {
                    log.info("{}", "aDurablePO is nil");
                    Validations.check(true, retCodeConfig.getNotFoundDurablePo(), ObjectIdentifier.fetchValue(strProcessdurableReworkCountCheckin.getDurableID()));
                }

                if (bOverMaxReworkCheckFlag) {
                    log.info("PPTManager_i::process_durableReworkCount_Check bOverMaxReworkCheckFlag() == true");
                    Long maxReworkCount;
                    maxReworkCount = aDurablePO.findDefaultMaxReworkCount();

                    Validations.check(retCodeConfigEx.getReachMaxDurableRework(), maxReworkCount);
                }

                if (bOverMaxProcessCheckFlag) {
                    log.info("PPTManager_i::process_durableReworkCount_Check bOverMaxProcessCheckFlag() == true");

                    maxProcessCountSeq = aDurablePO.findDefaultMaxProcessCount();

                    Validations.check(retCodeConfigEx.getReachMaxDurableRework2(), maxProcessCountSeq.get(0).getCount(), maxProcessCountSeq.get(0).getOperationNumber());
                }
            }
        } else if (CimStringUtils.equals(strProcessdurableReworkCountCheckin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strProcessdurableReworkCountCheckin.getDurableID());

            /*---------------------------*/
            /* Get Process Flow Context  */
            /*---------------------------*/
            durablePFX = aReticlePod.getDurableProcessFlowContext();

            if (durablePFX == null) {
                log.info("{}", "durablePFX is nil");
                Validations.check(true,
                        retCodeConfig.getNotFoundPfx(), "");
            }

            bOverMaxReworkCheckFlag = aReticlePod.isOverDefaultMaxReworkCount();

            bOverMaxProcessCheckFlag = durablePFX.isOverDefaultMaxProcessCount();

            if (bOverMaxReworkCheckFlag || bOverMaxProcessCheckFlag) {
                log.info("{}", "bOverMaxReworkCheckFlag == true or bOverMaxProcessCheckFlag == true");

                aDurablePO = aReticlePod.getDurableProcessOperation();

                if (aDurablePO == null) {
                    log.info("{}", "aDurablePO is nil");
                    Validations.check(true, retCodeConfig.getNotFoundDurablePo(), ObjectIdentifier.fetchValue(strProcessdurableReworkCountCheckin.getDurableID()));
                }

                if (bOverMaxReworkCheckFlag) {
                    log.info("PPTManager_i::process_durableReworkCount_Check", "bOverMaxReworkCheckFlag() == true");
                    Long maxReworkCount;
                    maxReworkCount = aDurablePO.findDefaultMaxReworkCount();

                    String maxReworkCountStr;
                    maxReworkCountStr = String.format("%ld", maxReworkCount);

                    Validations.check(true, retCodeConfigEx.getReachMaxDurableRework(), maxReworkCountStr);
                }

                if (bOverMaxProcessCheckFlag) {
                    log.info("PPTManager_i::process_durableReworkCount_Check", "bOverMaxProcessCheckFlag() == true");
//                    PosProcessOperationSpecificationSequence* responsiblePOSs = null ;
//                    PosProcessOperationSpecificationSequence_var responsiblePOSsVar;

                    maxProcessCountSeq = aDurablePO.findDefaultMaxProcessCount();

                    String maxProcessCountStr;
                    maxProcessCountStr = String.format("%ld", maxProcessCountSeq.get(0).getCount());
                    Validations.check(true, retCodeConfigEx.getReachMaxDurableRework2(), maxProcessCountStr, maxProcessCountSeq.get(0).getOperationNumber());
                }
            }
        } else if (CimStringUtils.equals(strProcessdurableReworkCountCheckin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strProcessdurableReworkCountCheckin.getDurableID());

            /*---------------------------*/
            /* Get Process Flow Context  */
            /*---------------------------*/
            durablePFX = aReticle.getDurableProcessFlowContext();

            if (durablePFX == null) {
                log.info("{}", "durablePFX is nil");
                Validations.check(true,
                        retCodeConfig.getNotFoundPfx(), "");
            }

            bOverMaxReworkCheckFlag = aReticle.isOverDefaultMaxReworkCount();

            bOverMaxProcessCheckFlag = durablePFX.isOverDefaultMaxProcessCount();

            if (bOverMaxReworkCheckFlag || bOverMaxProcessCheckFlag) {
                log.info("{}", "bOverMaxReworkCheckFlag == true or bOverMaxProcessCheckFlag == true");

                aDurablePO = aReticle.getDurableProcessOperation();

                if (aDurablePO == null) {
                    log.info("{}", "aDurablePO is nil");
                    Validations.check(true, retCodeConfig.getNotFoundDurablePo(), ObjectIdentifier.fetchValue(strProcessdurableReworkCountCheckin.getDurableID()));
                }

                if (bOverMaxReworkCheckFlag) {
                    log.info("PPTManager_i::process_durableReworkCount_Check", "bOverMaxReworkCheckFlag() == true");
                    Long maxReworkCount;
                    maxReworkCount = aDurablePO.findDefaultMaxReworkCount();

                    String maxReworkCountStr;
                    maxReworkCountStr = String.format("%ld", maxReworkCount);

                    Validations.check(true, retCodeConfigEx.getReachMaxDurableRework(), maxReworkCountStr);
                }

                if (bOverMaxProcessCheckFlag) {
                    log.info("PPTManager_i::process_durableReworkCount_Check", "bOverMaxProcessCheckFlag() == true");
//                    PosProcessOperationSpecificationSequence* responsiblePOSs = null ;
//                    PosProcessOperationSpecificationSequence_var responsiblePOSsVar;

                    maxProcessCountSeq = aDurablePO.findDefaultMaxProcessCount();

                    String maxProcessCountStr;
                    maxProcessCountStr = String.format("%ld", maxProcessCountSeq.get(0).getCount());
                    Validations.check(true, retCodeConfigEx.getReachMaxDurableRework2(), maxProcessCountStr, maxProcessCountSeq.get(0).getOperationNumber());
                }

            }
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessdurableReworkCountIncrementin
     * @return void
     * @throws
     * @author ho
     * @date 2020/7/13 10:03
     */
    public void processDurableReworkCountIncrement(
            Infos.ObjCommon strObjCommonIn,
            Infos.ProcessDurableReworkCountIncrementIn strProcessdurableReworkCountIncrementin) {
        log.info("PPTManager_i::process_durableReworkCount_Increment");
        if (CimStringUtils.equals(strProcessdurableReworkCountIncrementin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strProcessdurableReworkCountIncrementin.getDurableID());

            //-------------------------------------------------------------------------------------
            // Make the key for the check of max rework count.
            // The key is the stringifiedObjectReference of previous ProcessOperationSpecification.
            //-------------------------------------------------------------------------------------
            aCassette.increaseReworkCount();
        } else if (CimStringUtils.equals(strProcessdurableReworkCountIncrementin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strProcessdurableReworkCountIncrementin.getDurableID());

            //-------------------------------------------------------------------------------------
            // Make the key for the check of max rework count.
            // The key is the stringifiedObjectReference of previous ProcessOperationSpecification.
            //-------------------------------------------------------------------------------------
            aReticlePod.increaseReworkCount();
        } else if (CimStringUtils.equals(strProcessdurableReworkCountIncrementin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strProcessdurableReworkCountIncrementin.getDurableID());

            //-------------------------------------------------------------------------------------
            // Make the key for the check of max rework count.
            // The key is the stringifiedObjectReference of previous ProcessOperationSpecification.
            //-------------------------------------------------------------------------------------
            aReticle.increaseReworkCount();
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessBranchRouteForDurablein
     * @return com.fa.cim.dto.Inputs.OldCurrentPOData
     * @throws
     * @author ho
     * @date 2020/7/13 10:17
     */
    public Inputs.OldCurrentPOData processBranchRouteForDurable(
            Infos.ObjCommon strObjCommonIn,
            Infos.ProcessBranchRouteForDurableIn strProcessBranchRouteForDurablein) {
        log.info("PPTManager_i::process_BranchRouteForDurable");
        Inputs.OldCurrentPOData strProcessBranchRouteForDurableout = new Inputs.OldCurrentPOData();

        CimDurableProcessOperation aDurablePO;
        ProcessDTO.PosProcessOperationEventData oldPOEventData;
        CimDurableProcessFlowContext aDurablePFX;
        ProcessDTO.BackupOperation backupOperation;
        CimPerson aPerson;
        aPerson = baseCoreFactory.getBO(CimPerson.class, strObjCommonIn.getUser().getUserID());

        //--------------------------------------------------------------------------------------------------
        // (1) Get branch ProcessFlow ( Rework route or Sub route )
        //--------------------------------------------------------------------------------------------------
        CimProcessDefinition aProcessDefinition;
        aProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class,
                strProcessBranchRouteForDurablein.getSubRouteID());

        if (CimStringUtils.equals(strProcessBranchRouteForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strProcessBranchRouteForDurablein.getDurableID());

            aDurablePO = aCassette.getDurableProcessOperation();

            if (aDurablePO == null) {
                log.info("{}", "##### aDurablePO is Nil");
                Validations.check(true,
                        retCodeConfig.getNotFoundDurablePo(),
                        ObjectIdentifier.fetchValue(strProcessBranchRouteForDurablein.getDurableID()));
            }

            oldPOEventData = aDurablePO.getEventData();

            strProcessBranchRouteForDurableout.setRouteID(oldPOEventData.getRouteID());
            strProcessBranchRouteForDurableout.setOperationNumber(oldPOEventData.getOperationNumber());
            strProcessBranchRouteForDurableout.setOperationID(oldPOEventData.getOperationID());
            strProcessBranchRouteForDurableout.setOperationPassCount(oldPOEventData.getOperationPassCount());
            strProcessBranchRouteForDurableout.setObjrefPOS(oldPOEventData.getObjrefPOS());
            strProcessBranchRouteForDurableout.setObjrefMainPF(oldPOEventData.getObjrefMainPF());
            strProcessBranchRouteForDurableout.setObjrefModulePOS(oldPOEventData.getObjrefModulePOS());

            //--------------------------------------------------------------------------------------------------
            // (2) Remove durable from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.removeFromDurableQueue(strProcessBranchRouteForDurablein.getDurableCategory(),
                    strProcessBranchRouteForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (3) Branch Route
            //--------------------------------------------------------------------------------------------------
            aCassette.branchTo(aProcessDefinition, strProcessBranchRouteForDurablein.getReturnOperationNumber());

            //--------------------------------------------------------------------------------------------------
            // (4) Add durable into new current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(strProcessBranchRouteForDurablein.getDurableCategory(),
                    strProcessBranchRouteForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (5) Change durable production state.
            //--------------------------------------------------------------------------------------------------
            try {
                aCassette.changeProductionStateBy(strObjCommonIn.getTimeStamp().getReportTimeStamp(), aPerson);
            } catch (CoreFrameworkException ex) {
                Validations.check(true,
                        retCodeConfig.getInvalidStateTrans(),
                        "*****", "*****");
            }

            //--------------------------------------------------------------------------------------------------
            // (6) Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aCassette.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

            aCassette.setLastClaimedPerson(aPerson);

            //--------------------------------------------------------------------------------------------------
            // (7) If branch route's pd type is "Rework", set rework out operation to output parameter.
            //--------------------------------------------------------------------------------------------------
            //===== Get and check branch route's pd type =====//
            String processDefinitionType;
            processDefinitionType = aProcessDefinition.getProcessDefinitionType();

            if (CimStringUtils.equals(processDefinitionType, SP_MAINPDTYPE_DURABLEREWORK)) {
                log.info("{}", "** Branch route's PD type is 'Rework'");
                //===== Get and set rework out operation from backup operation =====//

                aDurablePFX = aCassette.getDurableProcessFlowContext();

                if (aDurablePFX == null) {
                    log.info("{}", "aDurablePFX is nil");
                    Validations.check(true, retCodeConfig.getNotFoundPfx(), "");
                }

                backupOperation = aDurablePFX.getBackupOperation();

                strProcessBranchRouteForDurableout.setReworkOutOperation(backupOperation.getReworkOutKey());
            }
        } else if (CimStringUtils.equals(strProcessBranchRouteForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strProcessBranchRouteForDurablein.getDurableID());

            aDurablePO = aReticlePod.getDurableProcessOperation();

            if (aDurablePO == null) {
                log.info("{}", "##### aDurablePO is Nil");
                Validations.check(true,
                        retCodeConfig.getNotFoundDurablePo(),
                        ObjectIdentifier.fetchValue(strProcessBranchRouteForDurablein.getDurableID()));
            }

            oldPOEventData = aDurablePO.getEventData();

            strProcessBranchRouteForDurableout.setRouteID(oldPOEventData.getRouteID());
            strProcessBranchRouteForDurableout.setOperationNumber(oldPOEventData.getOperationNumber());
            strProcessBranchRouteForDurableout.setOperationID(oldPOEventData.getOperationID());
            strProcessBranchRouteForDurableout.setOperationPassCount(oldPOEventData.getOperationPassCount());
            strProcessBranchRouteForDurableout.setObjrefPOS(oldPOEventData.getObjrefPOS());
            strProcessBranchRouteForDurableout.setObjrefMainPF(oldPOEventData.getObjrefMainPF());
            strProcessBranchRouteForDurableout.setObjrefModulePOS(oldPOEventData.getObjrefModulePOS());

            //--------------------------------------------------------------------------------------------------
            // (2) Remove durable from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.removeFromDurableQueue(strProcessBranchRouteForDurablein.getDurableCategory(),
                    strProcessBranchRouteForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (3) Branch Route
            //--------------------------------------------------------------------------------------------------
            aReticlePod.branchTo(aProcessDefinition, strProcessBranchRouteForDurablein.getReturnOperationNumber());

            //--------------------------------------------------------------------------------------------------
            // (4) Add durable into new current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(strProcessBranchRouteForDurablein.getDurableCategory(),
                    strProcessBranchRouteForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (5) Change durable production state.
            //--------------------------------------------------------------------------------------------------
            try {
                aReticlePod.changeProductionStateBy(strObjCommonIn.getTimeStamp().getReportTimeStamp(), aPerson);
            } catch (CoreFrameworkException ex) {
                Validations.check(true,
                        retCodeConfig.getInvalidStateTrans(),
                        "*****", "*****");
            }

            //--------------------------------------------------------------------------------------------------
            // (6) Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aReticlePod.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

            aReticlePod.setLastClaimedPerson(aPerson);

            //--------------------------------------------------------------------------------------------------
            // (7) If branch route's pd type is "Rework", set rework out operation to output parameter.
            //--------------------------------------------------------------------------------------------------
            //===== Get and check branch route's pd type =====//
            String processDefinitionType;
            processDefinitionType = aProcessDefinition.getProcessDefinitionType();

            if (CimStringUtils.equals(processDefinitionType, SP_MAINPDTYPE_DURABLEREWORK)) {
                log.info("{}", "** Branch route's PD type is 'Rework'");
                //===== Get and set rework out operation from backup operation =====//

                aDurablePFX = aReticlePod.getDurableProcessFlowContext();

                if (aDurablePFX == null) {
                    log.info("{}", "aDurablePFX is nil");
                    Validations.check(true, retCodeConfig.getNotFoundPfx(), "");
                }

                backupOperation = aDurablePFX.getBackupOperation();

                strProcessBranchRouteForDurableout.setReworkOutOperation(backupOperation.getReworkOutKey());
            }
        } else if (CimStringUtils.equals(strProcessBranchRouteForDurablein.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strProcessBranchRouteForDurablein.getDurableID());

            aDurablePO = aReticle.getDurableProcessOperation();

            if (aDurablePO == null) {
                log.info("{}", "##### aDurablePO is Nil");
                Validations.check(true,
                        retCodeConfig.getNotFoundDurablePo(),
                        ObjectIdentifier.fetchValue(strProcessBranchRouteForDurablein.getDurableID()));
            }

            oldPOEventData = aDurablePO.getEventData();

            strProcessBranchRouteForDurableout.setRouteID(oldPOEventData.getRouteID());
            strProcessBranchRouteForDurableout.setOperationNumber(oldPOEventData.getOperationNumber());
            strProcessBranchRouteForDurableout.setOperationID(oldPOEventData.getOperationID());
            strProcessBranchRouteForDurableout.setOperationPassCount(oldPOEventData.getOperationPassCount());
            strProcessBranchRouteForDurableout.setObjrefPOS(oldPOEventData.getObjrefPOS());
            strProcessBranchRouteForDurableout.setObjrefMainPF(oldPOEventData.getObjrefMainPF());
            strProcessBranchRouteForDurableout.setObjrefModulePOS(oldPOEventData.getObjrefModulePOS());

            //--------------------------------------------------------------------------------------------------
            // (2) Remove durable from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.removeFromDurableQueue(strProcessBranchRouteForDurablein.getDurableCategory(),
                    strProcessBranchRouteForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (3) Branch Route
            //--------------------------------------------------------------------------------------------------
            aReticle.branchTo(aProcessDefinition, strProcessBranchRouteForDurablein.getReturnOperationNumber());

            //--------------------------------------------------------------------------------------------------
            // (4) Add durable into new current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(strProcessBranchRouteForDurablein.getDurableCategory(),
                    strProcessBranchRouteForDurablein.getDurableID());

            //--------------------------------------------------------------------------------------------------
            // (5) Change durable production state.
            //--------------------------------------------------------------------------------------------------
            try {
                aReticle.changeProductionStateBy(strObjCommonIn.getTimeStamp().getReportTimeStamp(), aPerson);
            } catch (CoreFrameworkException ex) {
                Validations.check(true,
                        retCodeConfig.getInvalidStateTrans(),
                        "*****", "*****");
            }

            //--------------------------------------------------------------------------------------------------
            // (6) Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aReticle.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

            aReticle.setLastClaimedPerson(aPerson);

            //--------------------------------------------------------------------------------------------------
            // (7) If branch route's pd type is "Rework", set rework out operation to output parameter.
            //--------------------------------------------------------------------------------------------------
            //===== Get and check branch route's pd type =====//
            String processDefinitionType;
            processDefinitionType = aProcessDefinition.getProcessDefinitionType();

            if (CimStringUtils.equals(processDefinitionType, SP_MAINPDTYPE_DURABLEREWORK)) {
                log.info("{}", "** Branch route's PD type is 'Rework'");
                //===== Get and set rework out operation from backup operation =====//

                aDurablePFX = aReticle.getDurableProcessFlowContext();

                if (aDurablePFX == null) {
                    log.info("{}", "aDurablePFX is nil");
                    Validations.check(true, retCodeConfig.getNotFoundPfx(), "");
                }

                backupOperation = aDurablePFX.getBackupOperation();

                strProcessBranchRouteForDurableout.setReworkOutOperation(backupOperation.getReworkOutKey());
            }
        }

        return strProcessBranchRouteForDurableout;
    }
}
