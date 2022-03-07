package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.durablectrljob.CimDurableControlJobDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.code.CimE10State;
import com.fa.cim.newcore.bo.code.CimMachineState;
import com.fa.cim.newcore.bo.code.CodeManager;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimReticlePodPortResource;
import com.fa.cim.newcore.bo.machine.MachineManager;
import com.fa.cim.newcore.bo.restrict.RestrictionManager;
import com.fa.cim.newcore.dto.dispatch.DispatcherDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.List;

import static com.fa.cim.common.constant.TransactionIDEnum.*;

/**
 * <p>EquipmentForDurableMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/6/17 14:35         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/6/17 14:35
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@OmMethod
@Slf4j
public class EquipmentForDurableMethod implements IEquipmentForDurableMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IProcessMethod processMethod;

//    @Autowired
//    private EntityInhibitManager entityInhibitManager;

    @Autowired
    private RestrictionManager entityInhibitManager;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private CodeManager codeManager;

    @Autowired
    private IDurableControlJobMethod durableControlJobMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private MachineManager machineManager;

    @Autowired
    private IStockerMethod stockerMethod;

    @Override
    public void equipmentSpecialControlVsTxIDCheckCombination(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        Validations.check(null == objCommon || null == equipmentID, retCodeConfig.getInvalidInputParam());

        log.info(" in-parm equipmentID : " + equipmentID.getValue());

        //--------------------------------------------------------
        //  Get Equipment Special Control
        //--------------------------------------------------------
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());

        List<String> specialEquipmentControls = aMachine.getSpecialEquipmentControls();

        //--------------------------------------------------------------------------------------------------
        // These TransactionIDs must be done when 'Durable' is included in SpecialControl of the equipment
        //--------------------------------------------------------------------------------------------------
        String transactionID = objCommon.getTransactionID();
        if (TransactionIDEnum.equals(transactionID, START_DURABLES_RESERVATION_REQ)
                || TransactionIDEnum.equals(transactionID, START_DURABLES_RESERVATION_CANCEL_REQ)
                || TransactionIDEnum.equals(transactionID, DURABLES_INFO_FOR_OPE_START_INQ)
                || TransactionIDEnum.equals(transactionID, DURABLE_OPERATION_START_REQ)
                || TransactionIDEnum.equals(transactionID, DURABLE_OPERATION_START_CANCEL_REQ)
                || TransactionIDEnum.equals(transactionID, DURABLES_INFO_FOR_START_RESERVATION_INQ)
                || TransactionIDEnum.equals(transactionID, DURABLE_OPE_COMP_REQ)) {
            log.info("Durable transaction.");
            boolean bFound = false;
            if (CimArrayUtils.isNotEmpty(specialEquipmentControls)) {
                for (String control : specialEquipmentControls) {
                    if (CimStringUtils.equals(control, BizConstant.SP_MC_SPECIALEQUIPMENTCONTROL_DURABLECARRIER)
                            || CimStringUtils.equals(control, BizConstant.SP_MC_SPECIALEQUIPMENTCONTROL_DURABLERETICLE)
                            || CimStringUtils.equals(control, BizConstant.SP_MC_SPECIALEQUIPMENTCONTROL_DURABLERETICLEPOD)) {
                        bFound = true;
                        break;
                    }
                }
            }
            Validations.check(!bFound, retCodeConfigEx.getEqpSpecialControlTxIdMismatch(), transactionID);
        }
    }


    @Override
    public ObjectIdentifier equipmentRecoverStateGetManufacturingForDurable(Infos.ObjCommon objCommon,
                                                                            String operationType,
                                                                            ObjectIdentifier equipmentID,
                                                                            ObjectIdentifier durableControlJobID) {
        Validations.check(null == objCommon
                || null == operationType
                || null == equipmentID
                || null == durableControlJobID, retCodeConfig.getInvalidInputParam());
        /*------------------------*/
        /*   Get Machine Object   */
        /*------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());

        /*-------------------------*/
        /*   Get E10State Object   */
        /*-------------------------*/
        CimE10State anE10State = null;
        if (CimStringUtils.equals(operationType, BizConstant.SP_OPERATION_OPESTART)) {
            anE10State = codeManager.findE10StateNamed(CIMStateConst.CIM_E10_PRODUCTIVE);
        } else if (CimStringUtils.equals(operationType, BizConstant.SP_OPERATION_OPESTARTCANCEL)
                || CimStringUtils.equals(operationType, BizConstant.SP_OPERATION_OPERATIONCOMP)) {
            Inputs.DurableControlJobListGetDRIn paramIn = new Inputs.DurableControlJobListGetDRIn();
            paramIn.setEquipmentID(equipmentID);
            paramIn.setDurableInfoFlag(false);
            List<Infos.DurableControlJobListInfo> durableControlJobListInfos = durableControlJobMethod.durableControlJobListGetDR(objCommon, paramIn);

            boolean bInProcess = false;
            if(CimArrayUtils.isNotEmpty(durableControlJobListInfos)) {
                for (Infos.DurableControlJobListInfo durableControlJobInfo : durableControlJobListInfos) {
                    if(ObjectIdentifier.equalsWithValue(durableControlJobID, durableControlJobInfo.getDurableControlJobID())) continue;
                    if(!CimStringUtils.equals(durableControlJobInfo.getStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED)) {
                        bInProcess = true;
                        break;
                    }
                }
            }
            if(!bInProcess) {
                anE10State = codeManager.findE10StateNamed(CIMStateConst.CIM_E10_STANDBY);
            } else {
                anE10State = codeManager.findE10StateNamed(CIMStateConst.CIM_E10_PRODUCTIVE);
            }
        } else {
            log.info("normal case. continue to the following procedure.");
            // normal case. continue to the following procedure.
        }

        Validations.check(null == anE10State, retCodeConfig.getNotFoundE10State());
        /*-----------------------------*/
        /*   Get Default Status Code   */
        /*-----------------------------*/
        CimMachineState aMachineState = anE10State.getDefaultMachineState();
        Validations.check(null == aMachineState, retCodeConfig.getNotFoundDefaultMachineState());
        return ObjectIdentifier.build(aMachineState.getIdentifier(), aMachineState.getPrimaryKey());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strEquipmentdurablesWhatNextDRin
     * @return com.fa.cim.dto.Infos.EquipmentDurablesWhatNextDROut
     * @exception 
     * @author ho
     * @date 2020/6/30 11:25
     */
    @Override
    public Infos.EquipmentDurablesWhatNextDROut equipmentDurablesWhatNextDR(
            Infos.EquipmentDurablesWhatNextDROut strEquipmentdurablesWhatNextDRout,
            Infos.ObjCommon                       strObjCommonIn,
            Infos.EquipmentDurablesWhatNextDRIn strEquipmentdurablesWhatNextDRin ) {

        //---------------------------------------------
        //  Set input parameters into local variable
        //---------------------------------------------
        ObjectIdentifier equipmentID;
        String durableCategory;
        String selectCriteria;

        equipmentID     = strEquipmentdurablesWhatNextDRin.getEquipmentID();
        durableCategory = strEquipmentdurablesWhatNextDRin.getDurableCategory();
        selectCriteria  = strEquipmentdurablesWhatNextDRin.getSelectCriteria();

        long searchCondition = 0L;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if(CimStringUtils.length(searchCondition_var) > 0) {
            searchCondition = CimNumberUtils.longValue(searchCondition_var);
        }

        String hFREQPEQP_ID = equipmentID.getValue();

        String hFRMRCP_EQPd_theSystemKey__160=null;
        String hDRBL_OBJ=null;
        String hDRBLd_theSystemKey=null;
        String hDRBL_FINISHED_STATE=null;
        String hDRBL_ID=null;
        Boolean hDRBLPOST_PROCESS_FLAG=null;
        String hDRBLDCTRLJOB_ID=null;
        String hDRBLDCTRLJOB_OBJ=null;
        String hDRBLPO_OBJ=null;
        String hDRBLPFX_OBJ=null;
        String hDRBLMAINPD_ID=null;
        String hDRBLMAINPD_OBJ=null;
        String hDRBLOPE_NO=null;
        String hDRBLCLAIM_TIME=null;
        String hDRBLSTATE_CHG_TIME=null;
        String hDRBLQUEUED_TIME=null;
        String hDRBLINV_CHG_TIME=null;
        String hDRBL_PROCESS_STATE=null;
        String hDRBL_INV_STATE=null;
        String hDRBLTRANS_STATE=null;
        String hDRBLRESRV_USER_ID=null;
        String hDRBLRESRV_USER_OBJ=null;
        String hDRBLEQP_ID=null;
        String hDRBLEQP_OBJ=null;
        String hDRBL_STATE=null;
        String hDRBL_CONTENT_CATEGORY=null;

        //-------------------------------------
        // select Machine information
        //-------------------------------------
        String hFREQPEQP_OBJ         = "";
        String hFREQPUSED_RECIPE_ID  = "";
        String hFREQPUSED_RECIPE_OBJ = "";
        Long hFREQPBATCH_SIZE_MAX = 0L;
        Long hFREQPBATCH_SIZE_MIN = 0L;
        String hFREQPDISP_OBJ        = "";
        String hFREQPCUR_STATE_ID    = "";
        String hFREQPCUR_STATE_OBJ   = "";
        Boolean hFREQPRETICLE_REQ = false;
        String hFREQPMLTRCP_CAPA     = "";
        String hFREQPEQP_CATEGORY    = "";

        Object[] one = cimJpaRepository.queryOne("SELECT OMEQP.ID,\n" +
                "                OMEQP.LAST_USED_RECIPE_ID,\n" +
                "                OMEQP.LAST_USED_RECIPE_RKEY,\n" +
                "                OMEQP.MAX_BATCH_SIZE,\n" +
                "                OMEQP.MIN_BATCH_SIZE,\n" +
                "                OMEQP.DISP_RKEY,\n" +
                "                OMEQP.EQP_STATE_ID,\n" +
                "                OMEQP.EQP_STATE_RKEY,\n" +
                "                OMEQP.RTCL_NEED_FLAG,\n" +
                "                OMEQP.MULTI_RECIPE_CAPABLE,\n" +
                "                OMEQP.EQP_CATEGORY\n" +
                "        FROM OMEQP\n" +
                "        WHERE OMEQP.EQP_ID = ?", hFREQPEQP_ID);

        Validations.check(one==null,retCodeConfig.getNotFoundEqp(),hFREQPEQP_ID);

        hFREQPEQP_OBJ = CimObjectUtils.toString(one[0]);
        hFREQPUSED_RECIPE_ID = CimObjectUtils.toString(one[1]);
        hFREQPUSED_RECIPE_OBJ = CimObjectUtils.toString(one[2]);
        hFREQPBATCH_SIZE_MAX = CimLongUtils.longValue(one[3]);
        hFREQPBATCH_SIZE_MIN = CimLongUtils.longValue(one[4]);
        hFREQPDISP_OBJ = CimObjectUtils.toString(one[5]);
        hFREQPCUR_STATE_ID = CimObjectUtils.toString(one[6]);
        hFREQPCUR_STATE_OBJ = CimObjectUtils.toString(one[7]);
        hFREQPRETICLE_REQ = CimBooleanUtils.convert(one[8]);
        hFREQPMLTRCP_CAPA = CimObjectUtils.toString(one[9]);
        hFREQPEQP_CATEGORY = CimObjectUtils.toString(one[10]);

        strEquipmentdurablesWhatNextDRout.setEquipmentID                             (ObjectIdentifier.build(hFREQPEQP_ID,hFREQPEQP_OBJ));
        strEquipmentdurablesWhatNextDRout.setEquipmentCategory                       (hFREQPEQP_CATEGORY);
        strEquipmentdurablesWhatNextDRout.setLastRecipeID                            (ObjectIdentifier.build("",""));
        strEquipmentdurablesWhatNextDRout.setProcessRunSizeMaximum                   (hFREQPBATCH_SIZE_MAX);
        strEquipmentdurablesWhatNextDRout.setProcessRunSizeMinimum                   (hFREQPBATCH_SIZE_MIN);

        //-------------------------------------
        // select Dispatching Rule information
        //-------------------------------------
        String hFRDISPCUR_WT_PARAMS= "";

        hFRDISPCUR_WT_PARAMS = CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique("SELECT OMWNXT.WNXT_RULE_EXPR\n" +
                "        FROM OMWNXT\n" +
                "        WHERE OMWNXT.ID = ?", hFREQPDISP_OBJ));

        strEquipmentdurablesWhatNextDRout.setDispatchRule  (hFRDISPCUR_WT_PARAMS);

        if( CimStringUtils.equals(strEquipmentdurablesWhatNextDRout.getDispatchRule(), "INT-PRIORITY") ) {
            strEquipmentdurablesWhatNextDRout.setDispatchRule(BizConstant.SP_DEFAULTDURABLEDISPATCHRULE);
        }

        Boolean bMachineIsAvailableForDurable = false;

        //-----------------------------------
        // Check available State eqp itself
        //-----------------------------------

        String hFREQPSTd_theSystemKey = "";
        Boolean hFREQPSTAVLBL_FLG = false;

        one = cimJpaRepository.queryOne("SELECT OMEQPST.ID,\n" +
                "                OMEQPST.EQP_AVAIL_FLAG\n" +
                "        FROM OMEQPST\n" +
                "        WHERE OMEQPST.ID =?", hFREQPCUR_STATE_OBJ);


        hFREQPSTd_theSystemKey = CimObjectUtils.toString(one[0]);
        hFREQPSTAVLBL_FLG = CimBooleanUtils.convert(one[1]);

        bMachineIsAvailableForDurable = hFREQPSTAVLBL_FLG;

        if(CimBooleanUtils.isFalse(hFREQPSTAVLBL_FLG)) {
            //------------------------------------------------------------------------------------------------
            // If select criteria is "SP_DP_SelectCriteria_CanBeProcessed" or "SP_DP_SelectCriteria_Auto3",
            // then it means all Lots are NOT operable on this Equipment.
            // What'sNext Process is not necessary to continue any more.
            //------------------------------------------------------------------------------------------------
            if ( CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)
                    || CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_AUTO3) ) {

                strEquipmentdurablesWhatNextDRout.setStrWhatNextDurableAttributes(new ArrayList<>(0));
                return strEquipmentdurablesWhatNextDRout;
            }
        }

        //----------------------------------------------
        // Use Framework Method in order to retrieve
        // Lot Sequence which is sorted by sort logic
        //----------------------------------------------
        CimMachine aMachine;
        aMachine=baseCoreFactory.getBO(CimMachine.class,hFREQPEQP_OBJ);

        CimDispatcher aDispatcher;
        aDispatcher=baseCoreFactory.getBO(CimDispatcher.class,hFREQPDISP_OBJ);

        Validations.check(aDispatcher==null,retCodeConfig.getNotFoundEqpDispatcher(),equipmentID.getValue());

        List<DispatcherDTO.DispatchDecision> aDispatchDecisionSequence = null;
        int nCandidateDurableCount = 0;
        if(CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_HOLD)) {

            if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                    || (0 == CimStringUtils.length(durableCategory) && 0 == nCandidateDurableCount ) ) {
                aDispatchDecisionSequence = aDispatcher.whatNextHoldDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_CASSETTE);

                nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
                if(nCandidateDurableCount > 0 && 0 == CimStringUtils.length(durableCategory)) {
                    durableCategory = BizConstant.SP_DURABLECAT_CASSETTE;
                }
            }

            if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                    || (0 == CimStringUtils.length(durableCategory) && 0 == nCandidateDurableCount ) ) {
                aDispatchDecisionSequence = aDispatcher.whatNextHoldDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_RETICLEPOD);

                nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
                if(nCandidateDurableCount > 0 && 0 == CimStringUtils.length(durableCategory)) {
                    durableCategory = BizConstant.SP_DURABLECAT_RETICLEPOD;
                }
            }

            if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)
                    || (0 == CimStringUtils.length(durableCategory) && 0 == nCandidateDurableCount ) ) {
                aDispatchDecisionSequence = aDispatcher.whatNextHoldDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_RETICLE);

                nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
                if(nCandidateDurableCount > 0 && 0 == CimStringUtils.length(durableCategory)) {
                    durableCategory = BizConstant.SP_DURABLECAT_RETICLE;
                }
            }
        } else {

            if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                    || (0 == CimStringUtils.length(durableCategory) && 0 == nCandidateDurableCount ) ) {
                aDispatchDecisionSequence = aDispatcher.whatNextDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_CASSETTE);

                nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
                if(nCandidateDurableCount > 0 && 0 == CimStringUtils.length(durableCategory)) {
                    durableCategory = BizConstant.SP_DURABLECAT_CASSETTE;
                }
            }

            if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                    || (0 == CimStringUtils.length(durableCategory) && 0 == nCandidateDurableCount ) ) {
                aDispatchDecisionSequence = aDispatcher.whatNextDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_RETICLEPOD);

                nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
                if(nCandidateDurableCount > 0 && 0 == CimStringUtils.length(durableCategory)) {
                    durableCategory = BizConstant.SP_DURABLECAT_RETICLEPOD;
                }
            }

            if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)
                    || (0 == CimStringUtils.length(durableCategory) && 0 == nCandidateDurableCount ) ) {
                aDispatchDecisionSequence = aDispatcher.whatNextDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_RETICLE);

                nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
                if(nCandidateDurableCount > 0 && 0 == CimStringUtils.length(durableCategory)) {
                    durableCategory = BizConstant.SP_DURABLECAT_RETICLE;
                }
            }
        }


        //------------------------------------------
        // set length of output durable info structure
        //------------------------------------------
        List<Infos.WhatNextDurableAttributes> tmpWhatNextAttributes;
        List<Infos.DefaultRecipeSetting> strDefaultRecipeSetSeq;
        List<String> reticleSetSeq;
        List<Boolean> bDurableHasAssignedMRecipeSeq;
        int nWhatNextDurableCount = 0;

        tmpWhatNextAttributes = new ArrayList<>(nCandidateDurableCount);
        strDefaultRecipeSetSeq = new ArrayList<>(nCandidateDurableCount);
        reticleSetSeq = new ArrayList<>(nCandidateDurableCount);
        bDurableHasAssignedMRecipeSeq = new ArrayList<>(nCandidateDurableCount);

        for(int nAsnMRcp = 0; nAsnMRcp < nCandidateDurableCount; nAsnMRcp++) {
            bDurableHasAssignedMRecipeSeq.add(true);
        }

        for(int i = 0; i < nCandidateDurableCount; i++) {
            log.info("{} {}", "Loop for WIP Durable start. round", i);

            ProcessDTO.ProcessActivity aProcessActivity = null;

            if ( !(aDispatchDecisionSequence.get(i).getActivity() instanceof ProcessDTO.ProcessActivity) ) {
                Validations.check(true, retCodeConfig.getSystemError());
            }

            aProcessActivity=(ProcessDTO.ProcessActivity)aDispatchDecisionSequence.get(i).getActivity();

            hDRBL_OBJ = ObjectIdentifier.fetchReferenceKey(aProcessActivity.getProcessMaterialGroups().get(0));
            String dTheSystemKeyPos = hDRBL_OBJ;//BaseStaticMethod.strrchr(hDRBL_OBJ, "#");
            hDRBLd_theSystemKey = "";
            if(dTheSystemKeyPos == null) {
                hDRBLd_theSystemKey= "";
            } else {
                // dTheSystemKeyPos++;
                hDRBLd_theSystemKey= dTheSystemKeyPos;
            }

            log.info("{} {}", "hDRBLd_theSystemKey", hDRBLd_theSystemKey);

            //-------------------------------------
            // select durable info
            //-------------------------------------
            hDRBLDCTRLJOB_ID      = "";
            hDRBLDCTRLJOB_OBJ     = "";
            hDRBLPO_OBJ           = "";
            hDRBLPFX_OBJ          = "";
            hDRBL_ID              = "";
            hDRBL_OBJ             = "";
            hDRBLMAINPD_ID        = "";
            hDRBLMAINPD_OBJ       = "";
            hDRBLOPE_NO           = "";
            hDRBLCLAIM_TIME       = "";
            hDRBLSTATE_CHG_TIME   = "";
            hDRBLQUEUED_TIME      = "";
            hDRBLINV_CHG_TIME     = "";
            hDRBL_FINISHED_STATE  = "";
            hDRBLPOST_PROCESS_FLAG = false;
            hDRBL_PROCESS_STATE   = "";
            hDRBL_INV_STATE       = "";
            hDRBL_CONTENT_CATEGORY = "";

            if(CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
                one= cimJpaRepository.queryOne("SELECT OMCARRIER.DCJ_ID,\n" +
                        "                    OMCARRIER.DCJ_RKEY,\n" +
                        "                    OMCARRIER.DRBL_PROPE_RKEY,\n" +
                        "                    OMCARRIER.DRBL_PRFCX_RKEY,\n" +
                        "                    OMCARRIER.CARRIER_ID,\n" +
                        "                    OMCARRIER.ID,\n" +
                        "                    OMCARRIER.MAIN_PROCESS_ID,\n" +
                        "                    OMCARRIER.MAIN_PROCESS_RKEY,\n" +
                        "                    OMCARRIER.OPE_NO,\n" +
                        "                    OMCARRIER.LAST_TRX_TIME,\n" +
                        "                    OMCARRIER.STATE_CHG_TIME,\n" +
                        "                    OMCARRIER.QUEUED_TIME,\n" +
                        "                    OMCARRIER.INV_CHG_TIME,\n" +
                        "                    OMCARRIER.DRBL_FINISHED_STATE,\n" +
                        "                    OMCARRIER.PP_FLAG,\n" +
                        "                    OMCARRIER.DRBL_PROCESS_STATE,\n" +
                        "                    OMCARRIER.DRBL_INV_STATE,\n" +
                        "                    OMCARRIER.XFER_STATE,\n" +
                        "                    OMCARRIER.RSV_USER_ID,\n" +
                        "                    OMCARRIER.RSV_USER_RKEY,\n" +
                        "                    OMCARRIER.EQP_ID,\n" +
                        "                    OMCARRIER.EQP_RKEY,\n" +
                        "                    OMCARRIER.CARRIER_STATE,\n" +
                        "                    OMCARRIER.CARRIER_CATEGORY\n" +
                        "                FROM OMCARRIER\n" +
                        "                WHERE OMCARRIER.ID = ?",hDRBLd_theSystemKey);
                Validations.check(one==null,retCodeConfig.getSqlNotFound());
                hDRBLDCTRLJOB_ID = CimObjectUtils.toString(one[0]);
                hDRBLDCTRLJOB_OBJ = CimObjectUtils.toString(one[1]);
                hDRBLPO_OBJ = CimObjectUtils.toString(one[2]);
                hDRBLPFX_OBJ = CimObjectUtils.toString(one[3]);
                hDRBL_ID = CimObjectUtils.toString(one[4]);
                hDRBL_OBJ = CimObjectUtils.toString(one[5]);
                hDRBLMAINPD_ID = CimObjectUtils.toString(one[6]);
                hDRBLMAINPD_OBJ = CimObjectUtils.toString(one[7]);
                hDRBLOPE_NO = CimObjectUtils.toString(one[8]);
                hDRBLCLAIM_TIME = CimObjectUtils.toString(one[9]);
                hDRBLSTATE_CHG_TIME = CimObjectUtils.toString(one[10]);
                hDRBLQUEUED_TIME = CimObjectUtils.toString(one[11]);
                hDRBLINV_CHG_TIME = CimObjectUtils.toString(one[12]);
                hDRBL_FINISHED_STATE = CimObjectUtils.toString(one[13]);
                hDRBLPOST_PROCESS_FLAG = CimBooleanUtils.convert(one[14]);
                hDRBL_PROCESS_STATE = CimObjectUtils.toString(one[15]);
                hDRBL_INV_STATE = CimObjectUtils.toString(one[16]);
                hDRBLTRANS_STATE = CimObjectUtils.toString(one[17]);
                hDRBLRESRV_USER_ID = CimObjectUtils.toString(one[18]);
                hDRBLRESRV_USER_OBJ = CimObjectUtils.toString(one[19]);
                hDRBLEQP_ID = CimObjectUtils.toString(one[20]);
                hDRBLEQP_OBJ = CimObjectUtils.toString(one[21]);
                hDRBL_STATE = CimObjectUtils.toString(one[22]);
                hDRBL_CONTENT_CATEGORY = CimObjectUtils.toString(one[23]);

            } else if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD) ) {
                one=cimJpaRepository.queryOne("SELECT OMRTCLPOD.DCJ_ID,\n" +
                        "                    OMRTCLPOD.DCJ_RKEY,\n" +
                        "                    OMRTCLPOD.DRBL_PROPE_RKEY,\n" +
                        "                    OMRTCLPOD.DRBL_PRFCX_RKEY,\n" +
                        "                    OMRTCLPOD.RTCLPOD_ID,\n" +
                        "                    OMRTCLPOD.ID,\n" +
                        "                    OMRTCLPOD.MAIN_PROCESS_ID,\n" +
                        "                    OMRTCLPOD.MAIN_PROCESS_RKEY,\n" +
                        "                    OMRTCLPOD.OPE_NO,\n" +
                        "                    OMRTCLPOD.LAST_TRX_TIME,\n" +
                        "                    OMRTCLPOD.STATE_CHG_TIME,\n" +
                        "                    OMRTCLPOD.QUEUED_TIME,\n" +
                        "                    OMRTCLPOD.INV_CHG_TIME,\n" +
                        "                    OMRTCLPOD.DRBL_FINISHED_STATE,\n" +
                        "                    OMRTCLPOD.POST_PROCESS_FLAG,\n" +
                        "                    OMRTCLPOD.DRBL_PROCESS_STATE,\n" +
                        "                    OMRTCLPOD.DRBL_INV_STATE,\n" +
                        "                    OMRTCLPOD.XFER_STATE,\n" +
                        "                    OMRTCLPOD.RSV_USER_ID,\n" +
                        "                    OMRTCLPOD.RSV_USER_RKEY,\n" +
                        "                    OMRTCLPOD.EQP_ID,\n" +
                        "                    OMRTCLPOD.EQP_RKEY,\n" +
                        "                    OMRTCLPOD.DRBL_STATE,\n" +
                        "                    OMRTCLPOD.RTCLPOD_TYPE_ID\n" +
                        "                FROM OMRTCLPOD\n" +
                        "                WHERE OMRTCLPOD.ID = ?",hDRBLd_theSystemKey);

                Validations.check(one==null,retCodeConfig.getSqlNotFound());
                hDRBLDCTRLJOB_ID = CimObjectUtils.toString(one[0]);
                hDRBLDCTRLJOB_OBJ = CimObjectUtils.toString(one[1]);
                hDRBLPO_OBJ = CimObjectUtils.toString(one[2]);
                hDRBLPFX_OBJ = CimObjectUtils.toString(one[3]);
                hDRBL_ID = CimObjectUtils.toString(one[4]);
                hDRBL_OBJ = CimObjectUtils.toString(one[5]);
                hDRBLMAINPD_ID = CimObjectUtils.toString(one[6]);
                hDRBLMAINPD_OBJ = CimObjectUtils.toString(one[7]);
                hDRBLOPE_NO = CimObjectUtils.toString(one[8]);
                hDRBLCLAIM_TIME = CimObjectUtils.toString(one[9]);
                hDRBLSTATE_CHG_TIME = CimObjectUtils.toString(one[10]);
                hDRBLQUEUED_TIME = CimObjectUtils.toString(one[11]);
                hDRBLINV_CHG_TIME = CimObjectUtils.toString(one[12]);
                hDRBL_FINISHED_STATE = CimObjectUtils.toString(one[13]);
                hDRBLPOST_PROCESS_FLAG = CimBooleanUtils.convert(one[14]);
                hDRBL_PROCESS_STATE = CimObjectUtils.toString(one[15]);
                hDRBL_INV_STATE = CimObjectUtils.toString(one[16]);
                hDRBLTRANS_STATE = CimObjectUtils.toString(one[17]);
                hDRBLRESRV_USER_ID = CimObjectUtils.toString(one[18]);
                hDRBLRESRV_USER_OBJ = CimObjectUtils.toString(one[19]);
                hDRBLEQP_ID = CimObjectUtils.toString(one[20]);
                hDRBLEQP_OBJ = CimObjectUtils.toString(one[21]);
                hDRBL_STATE = CimObjectUtils.toString(one[22]);
                hDRBL_CONTENT_CATEGORY = CimObjectUtils.toString(one[23]);

            } else if( CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE) ) {
                one = cimJpaRepository.queryOne("SELECT OMPDRBL.DCJ_ID,\n" +
                        "                    OMPDRBL.DCJ_RKEY,\n" +
                        "                    OMPDRBL.DRBL_PROPE_RKEY,\n" +
                        "                    OMPDRBL.DRBL_PRFCX_RKEY,\n" +
                        "                    OMPDRBL.PDRBL_ID,\n" +
                        "                    OMPDRBL.ID,\n" +
                        "                    OMPDRBL.MAIN_PROCESS_ID,\n" +
                        "                    OMPDRBL.MAIN_PROCESS_RKEY,\n" +
                        "                    OMPDRBL.OPE_NO,\n" +
                        "                    OMPDRBL.LAST_TRX_TIME,\n" +
                        "                    OMPDRBL.STATE_CHG_TIME,\n" +
                        "                    OMPDRBL.QUEUED_TIME,\n" +
                        "                    OMPDRBL.INV_CHG_TIME,\n" +
                        "                    OMPDRBL.DRBL_FINISHED_STATE,\n" +
                        "                    OMPDRBL.POST_PROCESS_FLAG,\n" +
                        "                    OMPDRBL.DRBL_PROCESS_STATE,\n" +
                        "                    OMPDRBL.DRBL_INV_STATE,\n" +
                        "                    OMPDRBL.XFER_STATE,\n" +
                        "                    OMPDRBL.RSV_USER_ID,\n" +
                        "                    OMPDRBL.RSV_USER_RKEY,\n" +
                        "                    OMPDRBL.EQP_ID,\n" +
                        "                    OMPDRBL.EQP_RKEY,\n" +
                        "                    OMPDRBL.PDRBL_STATE,\n" +
                        "                    OMPDRBL_PDRBLGRP.PDRBL_GRP_ID\n" +
                        "                FROM OMPDRBL, OMPDRBL_PDRBLGRP\n" +
                        "                WHERE OMPDRBL.ID = ?\n" +
                        "                AND OMPDRBL.ID = OMPDRBL_PDRBLGRP.REFKEY",hDRBLd_theSystemKey);

                Validations.check(one==null,retCodeConfig.getSqlNotFound());
                hDRBLDCTRLJOB_ID = CimObjectUtils.toString(one[0]);
                hDRBLDCTRLJOB_OBJ = CimObjectUtils.toString(one[1]);
                hDRBLPO_OBJ = CimObjectUtils.toString(one[2]);
                hDRBLPFX_OBJ = CimObjectUtils.toString(one[3]);
                hDRBL_ID = CimObjectUtils.toString(one[4]);
                hDRBL_OBJ = CimObjectUtils.toString(one[5]);
                hDRBLMAINPD_ID = CimObjectUtils.toString(one[6]);
                hDRBLMAINPD_OBJ = CimObjectUtils.toString(one[7]);
                hDRBLOPE_NO = CimObjectUtils.toString(one[8]);
                hDRBLCLAIM_TIME = CimObjectUtils.toString(one[9]);
                hDRBLSTATE_CHG_TIME = CimObjectUtils.toString(one[10]);
                hDRBLQUEUED_TIME = CimObjectUtils.toString(one[11]);
                hDRBLINV_CHG_TIME = CimObjectUtils.toString(one[12]);
                hDRBL_FINISHED_STATE = CimObjectUtils.toString(one[13]);
                hDRBLPOST_PROCESS_FLAG = CimBooleanUtils.convert(one[14]);
                hDRBL_PROCESS_STATE = CimObjectUtils.toString(one[15]);
                hDRBL_INV_STATE = CimObjectUtils.toString(one[16]);
                hDRBLTRANS_STATE = CimObjectUtils.toString(one[17]);
                hDRBLRESRV_USER_ID = CimObjectUtils.toString(one[18]);
                hDRBLRESRV_USER_OBJ = CimObjectUtils.toString(one[19]);
                hDRBLEQP_ID = CimObjectUtils.toString(one[20]);
                hDRBLEQP_OBJ = CimObjectUtils.toString(one[21]);
                hDRBL_STATE = CimObjectUtils.toString(one[22]);
                hDRBL_CONTENT_CATEGORY = CimObjectUtils.toString(one[23]);

            }

            //-------------------------------
            // Durable Finished Status Check
            //-------------------------------
            if(CimStringUtils.equals(hDRBL_FINISHED_STATE, BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                log.info( "{} {} {}","hDRBL_FINISHED_STATE is Completed. So Continue.", hDRBL_ID, hDRBL_FINISHED_STATE);
                continue;
            }

            //-------------------------------------------------------------------------
            //  When Durable is in post process, it is not included in return structure
            //-------------------------------------------------------------------------
            if(hDRBLPOST_PROCESS_FLAG) {
                log.info("{} {}", "Durable is in PostProcess. ", hDRBL_ID);

                if( CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)
                        || CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_AUTO3) ) {
                    log.info("{} {}", "SelectCriteria is CanBeProcessed or Auto3. ", selectCriteria);
                    continue;
                }
            }

            Infos.WhatNextDurableAttributes whatNextDurableAttributes = new Infos.WhatNextDurableAttributes();
            whatNextDurableAttributes.setOperableFlagForCurrentMachineState    (bMachineIsAvailableForDurable);
            whatNextDurableAttributes.setDurableID                 (ObjectIdentifier.build(hDRBL_ID,hDRBL_OBJ));
            whatNextDurableAttributes.setDurableCategory                       (durableCategory);
            whatNextDurableAttributes.setRouteID                   (ObjectIdentifier.build(hDRBLMAINPD_ID,hDRBLMAINPD_OBJ));
            whatNextDurableAttributes.setOperationNumber                       (hDRBLOPE_NO);
            whatNextDurableAttributes.setLastClaimedTimeStamp                  (hDRBLCLAIM_TIME);
            whatNextDurableAttributes.setStateChangeTimeStamp                  (hDRBLSTATE_CHG_TIME);
            whatNextDurableAttributes.setQueuedTimeStamp                       (hDRBLQUEUED_TIME);
            whatNextDurableAttributes.setInventoryChangeTimeStamp              (hDRBLINV_CHG_TIME);
            whatNextDurableAttributes.setInPostProcessFlagOfDurable            (hDRBLPOST_PROCESS_FLAG);
            whatNextDurableAttributes.setTransferStatus                        (hDRBLTRANS_STATE);
            whatNextDurableAttributes.setTransferReserveUserID      (ObjectIdentifier.build(hDRBLRESRV_USER_ID,hDRBLRESRV_USER_OBJ));
            whatNextDurableAttributes.setContentCategory                       (hDRBL_CONTENT_CATEGORY);

            log.info("{} {}", "transferStatus", hDRBLTRANS_STATE);
            whatNextDurableAttributes.setEquipmentID                 (ObjectIdentifier.build("",""));
            whatNextDurableAttributes.setStockerID                   (ObjectIdentifier.build("",""));

            if(BaseStaticMethod.memcmp(hDRBLTRANS_STATE, "E", 1)) {
                whatNextDurableAttributes.setEquipmentID(ObjectIdentifier.build(hDRBLEQP_ID,hDRBLEQP_OBJ));
            } else {
                whatNextDurableAttributes.setStockerID(ObjectIdentifier.build(hDRBLEQP_ID,hDRBLEQP_OBJ));
            }

            if(CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_AUTO3)) {
                log.info("{}", "selectCriteria == Auto3");

                if(CimStringUtils.length(hDRBLRESRV_USER_ID) > 0) {
                    log.info("{}", "Durable is already reserved for transferring. now passing through");
                    continue;
                }

                if( !CimStringUtils.equals(hDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_MANUALIN      )
                        && !CimStringUtils.equals(hDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_STATIONIN     )
                        && !CimStringUtils.equals(hDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_BAYIN         )
                        && !CimStringUtils.equals(hDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_INTERMEDIATEIN)
                        && !CimStringUtils.equals(hDRBLTRANS_STATE, BizConstant.SP_TRANSSTATE_ABNORMALIN    ) ) {
                    log.info("{} {}", "transferStatus is not MI, SI, BI, II, AI", hDRBLTRANS_STATE);
                    continue;
                }
            }

            //-------------------------------------
            // select DCTRLJOB information
            //-------------------------------------
            String hFRDCTRLJOBDCTRLJOB_ID   = "";
            String hFRDCTRLJOBDCTRLJOB_OBJ  = "";
            String hFRDCTRLJOBEQP_ID        = "";
            String hFRDCTRLJOBEQP_OBJ       = "";
            String hFRDCTRLJOBOWNER_ID      = "";
            String hFRDCTRLJOBOWNER_OBJ     = "";

            dTheSystemKeyPos = hDRBLDCTRLJOB_OBJ;//BaseStaticMethod.strrchr(hDRBLDCTRLJOB_OBJ, "#");
            String hFRDCTRLJOBd_theSystemKey=null;
            if(dTheSystemKeyPos == null) {
                String hFRCTRLJOBd_theSystemKey = "";
            } else {
//                dTheSystemKeyPos++;
                hFRDCTRLJOBd_theSystemKey = dTheSystemKeyPos;
            }

            log.info("{} {}", "OMDCJ.ID", hFRDCTRLJOBd_theSystemKey);
            CimDurableControlJobDO cimDurableControlJobExam = new CimDurableControlJobDO();
            cimDurableControlJobExam.setId(hFRDCTRLJOBd_theSystemKey);
            CimDurableControlJobDO dcjData = cimJpaRepository.findOne(Example.of(cimDurableControlJobExam)).orElse(null);

            if (dcjData!=null){
                hFRDCTRLJOBDCTRLJOB_ID =dcjData.getDurableControlJobID();
                hFRDCTRLJOBDCTRLJOB_OBJ = dcjData.getId();
                hFRDCTRLJOBEQP_ID = dcjData.getEquipmentID();
                hFRDCTRLJOBEQP_OBJ = dcjData.getEquipmentObj();
                hFRDCTRLJOBOWNER_ID = dcjData.getOwnerID();
                hFRDCTRLJOBOWNER_OBJ = dcjData.getOwnerObj();
            }

            whatNextDurableAttributes.setDurableControlJob (ObjectIdentifier.build(hFRDCTRLJOBDCTRLJOB_ID,hFRDCTRLJOBDCTRLJOB_OBJ));
            whatNextDurableAttributes.setProcessReserveEquipmentID(ObjectIdentifier.build(hFRDCTRLJOBEQP_ID,hFRDCTRLJOBEQP_OBJ));
            whatNextDurableAttributes.setProcessReserveUserID(ObjectIdentifier.build(hFRDCTRLJOBOWNER_ID,hFRDCTRLJOBOWNER_OBJ));

            if( CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_AUTO3)
                    || CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED) ) {
                log.info("{}", "selectCriteria == Auto3");

                if(CimStringUtils.length(hFRDCTRLJOBDCTRLJOB_ID) > 0) {
                    log.info("{}", "controlJob is not nil");
                    continue;
                }
            }
            // end of control job

            //-------------------------------------
            // select FRDRBLPO information
            //-------------------------------------
            String hFRDRBLPOPOS_OBJ            = "";
            String hFRDRBLPOPD_ID              = "";
            String hFRDRBLPOPD_OBJ             = "";
            String hFRDRBLPOPLAN_START_TIME    = "";
            String hFRDRBLPOPLAN_END_TIME      = "";
            String hFRDRBLPOPLAN_EQP_ID        = "";
            String hFRDRBLPOPLAN_EQP_OBJ       = "";
            Long hFRDRBLPOREMAIN_CYCLE_TIME = 0L;
            String hFRDRBLPOASGN_LCRECIPE_ID   = "";
            String hFRDRBLPOASGN_LCRECIPE_OBJ  = "";
            String hFRDRBLPOASGN_RECIPE_ID     = "";
            String hFRDRBLPOASGN_RECIPE_OBJ    = "";
            dTheSystemKeyPos = hDRBLPO_OBJ;//BaseStaticMethod.strrchr(hDRBLPO_OBJ, "#");
            String hFRDRBLPOd_theSystemKey     = "";

            if(dTheSystemKeyPos != null) {
//                dTheSystemKeyPos++;
                hFRDRBLPOd_theSystemKey = dTheSystemKeyPos;
            }

            log.info("{} {}", "OMDRBLPROPE.d_theSystemKey", hFRDRBLPOd_theSystemKey);

            one = cimJpaRepository.queryOne("SELECT MPROCESS_PRSS_RKEY,\n" +
                    "                ROUTE_ID,\n" +
                    "                ROUTE_PRSS_RKEY,\n" +
                    "                ROUTE_NO,\n" +
                    "                ROUTE_OPE_NO,\n" +
                    "                ROUTE_PRF_RKEY,\n" +
                    "                MROUTE_PRF_RKEY,\n" +
                    "                STEP_ID,\n" +
                    "                STEP_RKEY,\n" +
                    "                PLAN_START_TIME,\n" +
                    "                PLAN_END_TIME,\n" +
                    "                PLAN_EQP_ID,\n" +
                    "                PLAN_EQP_RKEY,\n" +
                    "                REMAIN_CYCLE_TIME,\n" +
                    "                MPROCESS_PRF_RKEY,\n" +
                    "                OPE_NO,\n" +
                    "                ALLOC_LRCP_ID,\n" +
                    "                ALLOC_LRCP_RKEY,\n" +
                    "                ALLOC_MRCP_ID,\n" +
                    "                ALLOC_MRCP_RKEY,\n" +
                    "                ALLOC_PRCP_ID\n" +
                    "            FROM OMDRBLPROPE\n" +
                    "            WHERE ID = ?",hFRDRBLPOd_theSystemKey);

            Validations.check(one==null,retCodeConfig.getSqlNotFound());

            hFRDRBLPOPOS_OBJ = CimObjectUtils.toString(one[0]);
            String hFRDRBLPOMODULEPD_ID = CimObjectUtils.toString(one[1]);
            String hFRDRBLPOMODPOS_OBJ = CimObjectUtils.toString(one[2]);
            String hFRDRBLPOMODULE_NO = CimObjectUtils.toString(one[3]);
            String hFRDRBLPOMODULE_OPE_NO = CimObjectUtils.toString(one[4]);
            String hFRDRBLPOMODULE_PF_OBJ = CimObjectUtils.toString(one[5]);
            String hFRDRBLPOMAIN_PF_OBJ = CimObjectUtils.toString(one[6]);
            hFRDRBLPOPD_ID = CimObjectUtils.toString(one[7]);
            hFRDRBLPOPD_OBJ = CimObjectUtils.toString(one[8]);
            hFRDRBLPOPLAN_START_TIME = CimObjectUtils.toString(one[9]);
            hFRDRBLPOPLAN_END_TIME = CimObjectUtils.toString(one[10]);
            hFRDRBLPOPLAN_EQP_ID = CimObjectUtils.toString(one[11]);
            hFRDRBLPOPLAN_EQP_OBJ = CimObjectUtils.toString(one[12]);
            hFRDRBLPOREMAIN_CYCLE_TIME = CimLongUtils.longValue(one[13]);
            String hFRDRBLPOPF_OBJ = CimObjectUtils.toString(one[14]);
            String hFRDRBLPOOPE_NO = CimObjectUtils.toString(one[15]);
            hFRDRBLPOASGN_LCRECIPE_ID = CimObjectUtils.toString(one[16]);
            hFRDRBLPOASGN_LCRECIPE_OBJ = CimObjectUtils.toString(one[17]);
            hFRDRBLPOASGN_RECIPE_ID = CimObjectUtils.toString(one[18]);
            hFRDRBLPOASGN_RECIPE_OBJ = CimObjectUtils.toString(one[19]);
            String hFRDRBLPOASGN_PHRECIPE_ID = CimObjectUtils.toString(one[20]);


            log.info("{} {} {}", "Assigned LogicalRecipe at PO", hFRDRBLPOASGN_LCRECIPE_ID, hFRDRBLPOASGN_LCRECIPE_OBJ);
            log.info("{} {} {}", "Assigned MachineRecipe at PO", hFRDRBLPOASGN_RECIPE_ID,   hFRDRBLPOASGN_RECIPE_OBJ);

            if(0 == CimStringUtils.length(hFRDRBLPOASGN_RECIPE_ID)) {
                bDurableHasAssignedMRecipeSeq.set(nWhatNextDurableCount, false);
            }
            log.info("{} {} {}", "bDurableHasAssignedMRecipeSeq[nWhatNextDurableCount]", nWhatNextDurableCount, bDurableHasAssignedMRecipeSeq.get(nWhatNextDurableCount));

            //-------------------------------------------------------------------------------------------------
            //  if current module PF is inactive, search current module POS and PD_ID from an active module PF
            //-------------------------------------------------------------------------------------------------
            //--- Get theSystemkey of current module PF
            String hFRMODULEPFd_theSystemKey;
            String hFRPFd_theSystemKey = "";
            String dTheSystemKeyModulePF = hFRDRBLPOMODULE_PF_OBJ;//BaseStaticMethod.strrchr(hFRDRBLPOMODULE_PF_OBJ, "#");
            if(dTheSystemKeyModulePF != null) {
//                dTheSystemKeyModulePF++;
                hFRPFd_theSystemKey = dTheSystemKeyModulePF;
                hFRMODULEPFd_theSystemKey = hFRPFd_theSystemKey;
            }

            //--- Check state of current module PF
            
            one = cimJpaRepository.queryOne("SELECT PRP_ID, ACTIVE_FLAG\n" +
                    "            FROM OMPRF\n" +
                    "            WHERE ID = ?",hFRPFd_theSystemKey);
            String hFRPFMAINPD_ID = CimObjectUtils.toString(one[0]);
            Integer hFRPFSTATE = CimNumberUtils.intValue(one[1]);

            //--- if current module PF is inactive, get an active module POS in active current module PF
            String hFRPOSPD_OBJ = "";
            String hFRPF_POSLISTPOS_OBJ;
            if(hFRPFSTATE == 0) {
                String hFRPFPD_LEVEL = "";
                hFRPFPD_LEVEL=BizConstant.SP_PD_FLOWLEVEL_MODULE;

                one=cimJpaRepository.queryOne("SELECT OMPRF_PRSSSEQ.PRSS_RKEY,OMPRF.PRP_ID\n" +
                        "                FROM OMPRF, OMPRF_PRSSSEQ\n" +
                        "                WHERE OMPRF.PRP_ID = ?\n" +
                        "                AND OMPRF.ACTIVE_FLAG = 1\n" +
                        "                AND OMPRF.ID = OMPRF_PRSSSEQ.REFKEY\n" +
                        "                AND OMPRF_PRSSSEQ.LINK_KEY  = ?\n" +
                        "                AND OMPRF.PRP_LEVEL       = ?",hFRPFMAINPD_ID,
                        hFRDRBLPOMODULE_OPE_NO,
                        hFRPFPD_LEVEL);
                if (one!=null){
                    hFRPF_POSLISTPOS_OBJ= CimObjectUtils.toString(one[0]);
                    hFRDRBLPOMODPOS_OBJ = hFRPF_POSLISTPOS_OBJ;
                }

            } else {
                log.info( "current module PF is active");
                // do nothing
            }

            whatNextDurableAttributes.setOperationID(ObjectIdentifier.build(hFRDRBLPOPD_ID,hFRDRBLPOPD_OBJ));

            //-------------------------------------
            // select previous PO information
            //-------------------------------------
            String hFRDRBLPFXd_theSystemKey = "";
            Long hFRDRBLPFX_DRBLPOLISTd_SeqNo = 0L;

            log.info("{} {}", "OMLOT.PRFCX_RKEY", hDRBLPFX_OBJ);

            one=cimJpaRepository.queryOne("SELECT OMDRBLPRFCX_PROPESEQ.REFKEY,\n" +
                    "                OMDRBLPRFCX_PROPESEQ.IDX_NO\n" +
                    "            FROM OMDRBLPRFCX_PROPESEQ,\n" +
                    "                    OMDRBLPRFCX\n" +
                    "            WHERE  OMDRBLPRFCX_PROPESEQ.PROPE_RKEY = OMDRBLPRFCX.CUR_PROPE_RKEY AND\n" +
                    "            OMDRBLPRFCX_PROPESEQ.REFKEY = OMDRBLPRFCX.ID AND\n" +
                    "            OMDRBLPRFCX.ID = ?",hDRBLPFX_OBJ);

            hFRDRBLPFXd_theSystemKey=String.valueOf(one[0]);
            hFRDRBLPFX_DRBLPOLISTd_SeqNo= CimLongUtils.longValue(one[1]);

            hFRDRBLPFX_DRBLPOLISTd_SeqNo--;

            log.info("{} {}", "OMPRFCX_PROPESEQ.D_SEQNO--", hFRDRBLPFX_DRBLPOLISTd_SeqNo );

            String hFRDRBLPOACTUAL_END_TIME = "";

            one=cimJpaRepository.queryOne("SELECT OMDRBLPROPE.ACTUAL_MOVOUT_TIME,OMDRBLPRFCX_PROPESEQ.IDX_NO \n" +
                    "            FROM OMDRBLPROPE,\n" +
                    "                    OMDRBLPRFCX_PROPESEQ\n" +
                    "            WHERE OMDRBLPRFCX_PROPESEQ.REFKEY = ?\n" +
                    "            AND OMDRBLPRFCX_PROPESEQ.IDX_NO = ?\n" +
                    "            AND OMDRBLPROPE.ID = OMDRBLPRFCX_PROPESEQ.PROPE_RKEY",hFRDRBLPFXd_theSystemKey,
                    hFRDRBLPFX_DRBLPOLISTd_SeqNo);

            if (one!=null){
                hFRDRBLPOACTUAL_END_TIME= CimObjectUtils.toString(one[0]);
            }

            whatNextDurableAttributes.setPreOperationCompTimeStamp(hFRDRBLPOACTUAL_END_TIME);
            // end of previous PO

            //-----------------------------------------
            // select POS information (get photo_layer)
            //-----------------------------------------
            hFRPF_POSLISTPOS_OBJ ="";
            dTheSystemKeyPos = hFRDRBLPOPF_OBJ;//BaseStaticMethod.strrchr(hFRDRBLPOPF_OBJ, "#");
            hFRPFd_theSystemKey ="";
            if(dTheSystemKeyPos != null) {
//                dTheSystemKeyPos++;
                hFRPFd_theSystemKey = dTheSystemKeyPos;
            }
            log.info("{} {}", "OMPRF_PRSSSEQ.d_theSystemKey", hFRPFd_theSystemKey);

            hFRPF_POSLISTPOS_OBJ= CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique("SELECT PRSS_RKEY FROM OMPRF_PRSSSEQ WHERE REFKEY = ? AND LINK_KEY=?", hFRPFd_theSystemKey
                    , hFRDRBLPOOPE_NO));

            dTheSystemKeyPos = hFRPF_POSLISTPOS_OBJ;//BaseStaticMethod.strrchr(hFRPF_POSLISTPOS_OBJ, "#");
            String hFRPOSd_theSystemKey = "";
            if(dTheSystemKeyPos != null) {
//                dTheSystemKeyPos++;
                hFRPOSd_theSystemKey= dTheSystemKeyPos;
            }

            log.info("{} {}", "OMPRSS.d_theSystemKey", hFRPOSd_theSystemKey);

            String hFRPOSPHOTO_LAYER = "";

            hFRPOSPHOTO_LAYER= CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique("SELECT PHOTO_LAYER\n" +
                    "            FROM OMPRSS\n" +
                    "            WHERE OMPRSS.ID = ?", hFRPOSd_theSystemKey));

            //---------------------------------------------------
            // select Module POS information (get mandatory_flag)
            //---------------------------------------------------
            hFRPOSd_theSystemKey="";

            dTheSystemKeyPos = hFRDRBLPOMODPOS_OBJ;//BaseStaticMethod.strrchr(hFRDRBLPOMODPOS_OBJ, "#");
            if(dTheSystemKeyPos == null) {
                hFRPOSd_theSystemKey="";
            } else {
//                dTheSystemKeyPos++;
                hFRPOSd_theSystemKey= dTheSystemKeyPos;
            }
            log.info("{} {}", "OMPRSS.d_theSystemKey(Module)", hFRPOSd_theSystemKey);

            Boolean hFRPOSMANDATORY_FLAG = false;

            hFRPOSMANDATORY_FLAG= CimBooleanUtils.convert(cimJpaRepository.queryOneColumnAndUnique("SELECT COMPULSORY_FLAG\n" +
                    "            FROM OMPRSS\n" +
                    "            WHERE OMPRSS.ID = ?", hFRPOSd_theSystemKey));


            whatNextDurableAttributes.setMandatoryOperationFlag(hFRPOSMANDATORY_FLAG);

            //------------------------------------------------------------------------------
            // select Main PF information (get stage_id, stage_obj, current seqno in mainPF)
            //------------------------------------------------------------------------------
            hFRPFd_theSystemKey="";
            String hFRMAINPFd_theSystemKey = null;

            dTheSystemKeyPos = hFRDRBLPOMAIN_PF_OBJ;//BaseStaticMethod.strrchr(hFRDRBLPOMAIN_PF_OBJ, "#");
            if(dTheSystemKeyPos != null) {
//                dTheSystemKeyPos++;
                hFRPFd_theSystemKey= dTheSystemKeyPos;
                hFRMAINPFd_theSystemKey = hFRPFd_theSystemKey;
            }
            log.info("{} {}", "OMPRF.d_theSystemKey", hFRMAINPFd_theSystemKey);

            String hFRPF_PDLISTSTAGE_ID="";
            String hFRPF_PDLISTSTAGE_OBJ="";
            Integer hFRPF_PDLISTd_SeqNo = 0;

            one=cimJpaRepository.queryOne("SELECT IDX_NO, STAGE_ID, STAGE_RKEY\n" +
                    "            FROM OMPRF_ROUTESEQ\n" +
                    "            WHERE REFKEY = ?\n" +
                    "            AND LINK_KEY = ?",hFRPFd_theSystemKey,
                    hFRDRBLPOMODULE_NO);

            hFRPF_PDLISTd_SeqNo= CimNumberUtils.intValue(one[0]);
            hFRPF_PDLISTSTAGE_ID= CimObjectUtils.toString(one[1]);
            hFRPF_PDLISTSTAGE_OBJ= CimObjectUtils.toString(one[2]);

            whatNextDurableAttributes.setStageID(ObjectIdentifier.build(hFRPF_PDLISTSTAGE_ID,
                    hFRPF_PDLISTSTAGE_OBJ));

            //-------------------------------------------------------
            // check durables' routeID and operation NO
            //-------------------------------------------------------
            boolean bEntityInhibitInfoCollected = false;
            boolean bNextOperationInfoRetrieved = false;

            for(int wnCnt = 0; wnCnt < nWhatNextDurableCount; wnCnt++) {
                log.info("{} {}", "Check Round", wnCnt);

                if( 0 < CimStringUtils.length(ObjectIdentifier.fetchValue(tmpWhatNextAttributes.get(wnCnt).getDurableControlJob()))
                        || 0 < CimStringUtils.length(ObjectIdentifier.fetchValue(whatNextDurableAttributes.getDurableControlJob())) ) {
                    log.info("{}", "Exist Control Job, Continue !");
                    continue;
                }

            }
            // end of route and operation and product

            //-------------------------------------
            // select next 2 EQP information
            //-------------------------------------
            log.info("{}", "bNextOperationInfoRetrieved == FALSE");

            ObjectIdentifier searchRouteID_dummy=null;
            List<Infos.DurableOperationNameAttributes> strProcessOperationListForDurableDRout = null;
            Params.ProcessOperationListForDurableDRParams strProcessOperationListForDurableDRin=new Params.ProcessOperationListForDurableDRParams();
            strProcessOperationListForDurableDRin.setDurableCategory        (durableCategory);
            strProcessOperationListForDurableDRin.setDurableID              (ObjectIdentifier.buildWithValue(hDRBL_ID));
            strProcessOperationListForDurableDRin.setSearchDirection        (true);
            strProcessOperationListForDurableDRin.setPosSearchFlag          (true);
            strProcessOperationListForDurableDRin.setCurrentFlag            (false);
            strProcessOperationListForDurableDRin.setSearchCount            (1);
            strProcessOperationListForDurableDRin.setSearchRouteID          (searchRouteID_dummy);
            strProcessOperationListForDurableDRin.setSearchOperationNumber  ("");
            try {
                strProcessOperationListForDurableDRout = processMethod.processOperationListForDurableDR( strObjCommonIn, strProcessOperationListForDurableDRin);
            } catch (ServiceException ex){
                if (!Validations.isEquals(ex.getCode(),retCodeConfig.getSomeopelistDataError())){
                    throw ex;
                }
            }

            int nameAttributeLen = CimArrayUtils.getSize(strProcessOperationListForDurableDRout);

            //--- Get module POS information (get PD_OBJ)
            hFRPOSd_theSystemKey="";

            if(nameAttributeLen != 0) {
                dTheSystemKeyPos = strProcessOperationListForDurableDRout.get(0).getProcessRef().getModulePOS();//BaseStaticMethod.strrchr(strProcessOperationListForDurableDRout.get(0).getProcessRef().getModulePOS(), "#");
            } else {
                dTheSystemKeyPos = null;
            }

            if(dTheSystemKeyPos != null) {
//                dTheSystemKeyPos++;
                hFRPOSd_theSystemKey= dTheSystemKeyPos;
            }

            log.info("{} {}", "Module OMPRSS.d_theSystemKey", hFRPOSd_theSystemKey);

            hFRPOSPD_OBJ= CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique("SELECT STEP_RKEY\n" +
                    "            FROM OMPRSS\n" +
                    "            WHERE OMPRSS.ID = ?", hFRPOSd_theSystemKey));


            dTheSystemKeyPos = hFRPOSPD_OBJ;//BaseStaticMethod.strrchr(hFRPOSPD_OBJ, "#");
            String hFRPDd_theSystemKey = "";

            if(dTheSystemKeyPos != null) {
//                dTheSystemKeyPos++;
                hFRPDd_theSystemKey= dTheSystemKeyPos;
            }

            log.info("{} {}", "OMPRP.d_theSystemKey", hFRPDd_theSystemKey );

            String hFRPD_LCRECIPERECIPE_ID="";
            String hFRPD_LCRECIPERECIPE_OBJ="";

            one=cimJpaRepository.queryOne("SELECT LRCP_ID,\n" +
                    "                LRCP_RKEY\n" +
                    "            FROM OMPRP\n" +
                    "            WHERE ID = ?",hFRPDd_theSystemKey);

            if (one!=null){
                hFRPD_LCRECIPERECIPE_ID= CimObjectUtils.toString(one[0]);
                hFRPD_LCRECIPERECIPE_OBJ= CimObjectUtils.toString(one[1]);
            }

            if(CimStringUtils.length(hFRPD_LCRECIPERECIPE_ID) > 0) {
                log.info("{}", "OMPRP was FOUND");
            }

            String hFRLRCPLCRECIPE_ID=null;
            String hFRLRCPLCRECIPE_OBJ=null;
            if(CimStringUtils.length(hFRPD_LCRECIPERECIPE_ID) > 0) {
                String version;
                version = BaseStaticMethod.extractVersionFromID(hFRPD_LCRECIPERECIPE_ID);
                if(CimStringUtils.equals(version, BizConstant.SP_ACTIVE_VERSION)) {
                    one=cimJpaRepository.queryOne("SELECT B.LRCP_ID, B.ID\n" +
                            "                    FROM OMLRCP A, OMLRCP B\n" +
                            "                    WHERE A.LRCP_ID = ?\n" +
                            "                    AND B.LRCP_ID = A.ACTIVE_VER",hFRPD_LCRECIPERECIPE_ID);
                    hFRLRCPLCRECIPE_ID= CimObjectUtils.toString(one[0]);
                    hFRLRCPLCRECIPE_OBJ= CimObjectUtils.toString(one[1]);

                    hFRPD_LCRECIPERECIPE_ID  ="";
                    hFRPD_LCRECIPERECIPE_OBJ ="";

                    hFRPD_LCRECIPERECIPE_ID  = hFRLRCPLCRECIPE_ID;
                    hFRPD_LCRECIPERECIPE_OBJ = hFRLRCPLCRECIPE_OBJ;
                }
            }

            String hFRLRCPd_theSystemKey=null;
            if(CimStringUtils.length(hFRPD_LCRECIPERECIPE_OBJ) > 0) {
                log.info("{}", "OMPRP_LRPRD.RECIPE_OBJ is not nil" );

                dTheSystemKeyPos = hFRPD_LCRECIPERECIPE_OBJ;//BaseStaticMethod.strrchr(hFRPD_LCRECIPERECIPE_OBJ, "#");

                if(dTheSystemKeyPos == null) {
                    hFRLRCPd_theSystemKey="";
                } else {
//                    dTheSystemKeyPos++;
                    hFRLRCPd_theSystemKey= dTheSystemKeyPos;
                }

                log.info("{} {}", "OMLRCP.d_theSystemKey", hFRLRCPd_theSystemKey);

                List<Object[]> cFRPD_EQP1 = cimJpaRepository.query("SELECT OMPRP_RESTRICTEQP.EQP_ID,\n" +
                        "                    OMPRP_RESTRICTEQP.EQP_RKEY\n" +
                        "                FROM OMPRP_RESTRICTEQP\n" +
                        "                WHERE OMPRP_RESTRICTEQP.REFKEY = ?\n" +
                        "                ORDER BY OMPRP_RESTRICTEQP.IDX_NO", hFRPDd_theSystemKey);

                boolean bNextEquipmentFoundFlag = false;

                if (CimArrayUtils.isEmpty(cFRPD_EQP1)){

                    log.info("{} {}", "OMPRP_RESTRICTEQP is NOT_FOUND. SELECT OMLRCP", hFRPD_LCRECIPERECIPE_ID);

                    //-----------------------------------------------------
                    //  Get Eqp from logicalRecipe(MachineRecipe's EQP)
                    //-----------------------------------------------------
                    hFRLRCPd_theSystemKey= CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique("SELECT ID\n" +
                            "                    FROM OMLRCP\n" +
                            "                    WHERE LRCP_ID = ?", hFRPD_LCRECIPERECIPE_ID));

                    Validations.check(hFRLRCPd_theSystemKey==null,retCodeConfig.getNotFoundLogicalRecipe());


                    log.info("{} {}", "OMLRCP D_THESYSTEMKEY", hFRLRCPd_theSystemKey);

                    List<Object> cFRLRCP_DSET1 = cimJpaRepository.queryOneColumn("SELECT RECIPE_RKEY\n" +
                            "                    FROM OMLRCP_DFLT\n" +
                            "                    WHERE REFKEY = ?", hFRLRCPd_theSystemKey);


                    String hFRLRCP_DSETRECIPE_OBJ = "";

                    hFRLRCP_DSETRECIPE_OBJ= CimObjectUtils.toString(cFRLRCP_DSET1.get(0));


                    String sysKey = hFRLRCP_DSETRECIPE_OBJ;//BaseStaticMethod.strrchr(hFRLRCP_DSETRECIPE_OBJ, "#");
                    if(sysKey!=null) {
//                        sysKey++;
                        hFRMRCP_EQPd_theSystemKey__160 = sysKey;
                    }
                    log.info("{} {}", " #### Generated key = ", hFRMRCP_EQPd_theSystemKey__160);

                    List<Object[]> cFRMRCP_EQP1 = cimJpaRepository.query("SELECT DISTINCT(EQP_ID) EQP_ID, EQP_RKEY\n" +
                            "                    FROM OMRCP_EQP\n" +
                            "                    WHERE REFKEY = ?", hFRMRCP_EQPd_theSystemKey__160);


                    String hFRMRCP_EQPEQP_ID  ="";
                    String hFRMRCP_EQPEQP_OBJ ="";

                    hFRMRCP_EQPEQP_ID= CimObjectUtils.toString(cFRMRCP_EQP1.get(0)[0]);
                    hFRMRCP_EQPEQP_OBJ= CimObjectUtils.toString(cFRMRCP_EQP1.get(0)[1]);

                    whatNextDurableAttributes.setNext2EquipmentID                     (ObjectIdentifier.build(hFRMRCP_EQPEQP_ID,hFRMRCP_EQPEQP_OBJ));
                    whatNextDurableAttributes.setNext2LogicalRecipeID                 (ObjectIdentifier.build(hFRPD_LCRECIPERECIPE_ID,hFRPD_LCRECIPERECIPE_OBJ));

//                    break;
                }

                for (Object[] objects : cFRPD_EQP1) {
                    String hFRPD_EQPEQP_ID  ="";
                    String hFRPD_EQPEQP_OBJ ="";

                    hFRPD_EQPEQP_ID= CimObjectUtils.toString(objects[0]);
                    hFRPD_EQPEQP_OBJ= CimObjectUtils.toString(objects[1]);

                    Integer totalcount= CimNumberUtils.intValue(cimJpaRepository.queryOneColumnAndUnique("SELECT COUNT(OMRCP.ID)\n" +
                                    "                    FROM OMLRCP_DFLT,\n" +
                                    "                            OMRCP,\n" +
                                    "                            OMRCP_EQP\n" +
                                    "                    WHERE OMLRCP_DFLT.ID = ?\n" +
                                    "                    AND OMLRCP_DFLT.RECIPE_RKEY = OMRCP.RECIPE_RKEY\n" +
                                    "                    AND OMRCP.ID = OMRCP_EQP.REFKEY\n" +
                                    "                    AND OMRCP_EQP.EQP_ID = ?", hFRLRCPd_theSystemKey,
                            hFRPD_EQPEQP_ID));

                    log.info("{} {}", "SQL SELECT COUNT(*) FROM OMLRCP_DFLT, OMRCP, OMRCP_EQP", totalcount);

                    if(totalcount!=0) {
                        log.info("{}", "totalCount > 0" );
                        log.info("{}", "bNextEquipmentFoundFlag = TRUE" );

                        whatNextDurableAttributes.setNext2EquipmentID(ObjectIdentifier.build(hFRPD_EQPEQP_ID,hFRPD_EQPEQP_OBJ));
                        whatNextDurableAttributes.setNext2LogicalRecipeID(ObjectIdentifier.build(hFRPD_LCRECIPERECIPE_ID,hFRPD_LCRECIPERECIPE_OBJ));

                        bNextEquipmentFoundFlag = true;
                        break;
                    }
                }

            }
            // end of next 2 equipment

            //-------------------------------------
            // select PD information
            //-------------------------------------
            dTheSystemKeyPos = hFRDRBLPOPD_OBJ;//BaseStaticMethod.strrchr(hFRDRBLPOPD_OBJ, "#");

            if(dTheSystemKeyPos == null) {
                hFRPDd_theSystemKey="";
            } else {
//                dTheSystemKeyPos++;
                hFRPDd_theSystemKey= dTheSystemKeyPos;
            }
            log.info("{} {}", "OMPRP.d_theSystemKey", hFRPDd_theSystemKey);

            String hFRPDINSPECTION_TYPE = "";
            hFRPD_LCRECIPERECIPE_ID  ="";
            hFRPD_LCRECIPERECIPE_OBJ ="";
            Double hFRPDSTD_PROC_TIME = 0D;

            //----------------------------------------------------------------------------------
            // Search Default Logical Recipe
            //----------------------------------------------------------------------------------
            one=cimJpaRepository.queryOne("SELECT OMPRP.PRP_ID,\n" +
                    "                OMPRP.STD_PROCESS_TIME,\n" +
                    "                OMPRP.LRCP_ID,\n" +
                    "                OMPRP.LRCP_RKEY\n" +
                    "            FROM OMPRP\n" +
                    "            WHERE ID = ?",hFRPDd_theSystemKey);

            hFRPDINSPECTION_TYPE= CimObjectUtils.toString(one[0]);  // PRP_ID 代替
            hFRPDSTD_PROC_TIME= CimDoubleUtils.doubleValue(one[1]);
            hFRPD_LCRECIPERECIPE_ID= CimObjectUtils.toString(one[2]);
            hFRPD_LCRECIPERECIPE_OBJ= CimObjectUtils.toString(one[3]);

            if(CimStringUtils.length(hFRPD_LCRECIPERECIPE_ID) > 0) {
                String version;
                version = BaseStaticMethod.extractVersionFromID(hFRPD_LCRECIPERECIPE_ID);
                if(CimStringUtils.equals(version, BizConstant.SP_ACTIVE_VERSION)) {
                    one=cimJpaRepository.queryOne("SELECT B.LRCP_ID, B.ID\n" +
                            "                    FROM OMLRCP A, OMLRCP B\n" +
                            "                    WHERE A.LRCP_ID = ? AND\n" +
                            "                    B.LRCP_ID = A.ACTIVE_VER",hFRPD_LCRECIPERECIPE_ID);
                    hFRLRCPLCRECIPE_ID= CimObjectUtils.toString(one[0]);
                    hFRLRCPLCRECIPE_OBJ= CimObjectUtils.toString(one[1]);

                    hFRPD_LCRECIPERECIPE_ID ="";
                    hFRPD_LCRECIPERECIPE_OBJ ="";
                    hFRPD_LCRECIPERECIPE_ID  =hFRLRCPLCRECIPE_ID;
                    hFRPD_LCRECIPERECIPE_OBJ =hFRLRCPLCRECIPE_OBJ;
                }
            }

            whatNextDurableAttributes.setInspectionType  (hFRPDINSPECTION_TYPE);
            whatNextDurableAttributes.setStandardProcessTime  (hFRPDSTD_PROC_TIME);

            //-------------------------
            //Durable Reservation Check
            //-------------------------
            if(CimStringUtils.length(hDRBLDCTRLJOB_OBJ) > 0) {
                log.info("{} {}", "Durable was Reserved. So set Assigned LogicalRecipe From PO", hDRBLDCTRLJOB_OBJ);

                whatNextDurableAttributes.setLogicalRecipeID(ObjectIdentifier.build(hFRDRBLPOASGN_LCRECIPE_ID,hFRDRBLPOASGN_LCRECIPE_OBJ));
            } else {
                whatNextDurableAttributes.setLogicalRecipeID(ObjectIdentifier.build(hFRPD_LCRECIPERECIPE_ID,hFRPD_LCRECIPERECIPE_OBJ));
            }
            // end of PD

            //-------------------------------------------------------------
            // select Logical and Machine and Physical Recipe information
            //-------------------------------------------------------------
            dTheSystemKeyPos = hFRPD_LCRECIPERECIPE_OBJ;//BaseStaticMethod.strrchr(hFRPD_LCRECIPERECIPE_OBJ, "#");

            if(dTheSystemKeyPos == null) {
                hFRLRCPd_theSystemKey="";
            } else {
//                dTheSystemKeyPos++;
                hFRLRCPd_theSystemKey= dTheSystemKeyPos;
            }

            log.info("{} {}", "OMLRCP.d_theSystemKey", hFRLRCPd_theSystemKey);

            String hFRLRCPMNTR_PRODSPEC_ID  ="";
            String hFRLRCPMNTR_PRODSPEC_OBJ ="";
            String hFRLRCPTESTTYPE_ID       ="";
            String hFRLRCPTESTTYPE_OBJ      ="";
            String hFRMRCPRECIPE_ID         ="";
            String hFRMRCPRECIPE_OBJ        ="";
            String hFRMRCPPHYSICAL_RECIPE_ID="";
            Integer hFRLRCP_DSETd_SeqNo = 0;

            List<Object[]> cLRCP_MRCP1 = cimJpaRepository.query("SELECT OMLRCP.MON_PROD_ID,\n" +
                            "                OMLRCP.MON_PROD_RKEY,\n" +
                            "                OMLRCP.TEST_TYPE_ID,\n" +
                            "                OMLRCP.TEST_TYPE_RKEY,\n" +
                            "                OMRCP.RECIPE_ID,\n" +
                            "                OMRCP.ID,\n" +
                            "                OMRCP.PHY_RECIPE_ID,\n" +
                            "                OMLRCP_DFLT.IDX_NO\n" +
                            "            FROM OMLRCP,\n" +
                            "                    OMLRCP_DFLT,\n" +
                            "                    OMRCP,\n" +
                            "                    OMRCP_EQP\n" +
                            "            WHERE OMLRCP.ID      = ?\n" +
                            "            AND OMLRCP_DFLT.REFKEY = ?\n" +
                            "            AND OMLRCP_DFLT.RECIPE_RKEY     = OMRCP.ID\n" +
                            "            AND OMRCP.ID      = OMRCP_EQP.REFKEY\n" +
                            "            AND OMRCP_EQP.EQP_ID          = ?", hFRLRCPd_theSystemKey,
                    hFRLRCPd_theSystemKey,
                    hFREQPEQP_ID);
            if(CimArrayUtils.isNotEmpty(cLRCP_MRCP1)) {
                hFRLRCPMNTR_PRODSPEC_ID= CimObjectUtils.toString(cLRCP_MRCP1.get(0)[0]);
                hFRLRCPMNTR_PRODSPEC_OBJ= CimObjectUtils.toString(cLRCP_MRCP1.get(0)[1]);
                hFRLRCPTESTTYPE_ID= CimObjectUtils.toString(cLRCP_MRCP1.get(0)[2]);
                hFRLRCPTESTTYPE_OBJ= CimObjectUtils.toString(cLRCP_MRCP1.get(0)[3]);
                hFRMRCPRECIPE_ID= CimObjectUtils.toString(cLRCP_MRCP1.get(0)[4]);
                hFRMRCPRECIPE_OBJ= CimObjectUtils.toString(cLRCP_MRCP1.get(0)[5]);
                hFRMRCPPHYSICAL_RECIPE_ID= CimObjectUtils.toString(cLRCP_MRCP1.get(0)[6]);
                hFRLRCP_DSETd_SeqNo= CimNumberUtils.intValue(cLRCP_MRCP1.get(0)[7]);
            }


            if(CimStringUtils.length(hFRMRCPRECIPE_ID) > 0) {
                String version;
                version = BaseStaticMethod.extractVersionFromID(hFRMRCPRECIPE_ID);
                if(CimStringUtils.equals(version,BizConstant.SP_ACTIVE_VERSION)) {
                    one=cimJpaRepository.queryOne("SELECT B.RECIPE_ID, B.ID\n" +
                            "                    FROM OMRCP A, OMRCP B\n" +
                            "                    WHERE A.RECIPE_ID = ?\n" +
                            "                    AND B.RECIPE_ID = A.ACTIVE_ID",hFRMRCPRECIPE_ID);
                    hFRMRCPRECIPE_ID= CimObjectUtils.toString(one[0]);
                    hFRMRCPRECIPE_OBJ= CimObjectUtils.toString(one[1]);

                }
            }

            whatNextDurableAttributes.setRecipeAvailableFlag                   (true);
            whatNextDurableAttributes.setTestTypeID(ObjectIdentifier.build(hFRLRCPTESTTYPE_ID,hFRLRCPTESTTYPE_OBJ));

            //---------------------
            //Durable Reservation Check
            //---------------------
            if(CimStringUtils.length(hDRBLDCTRLJOB_OBJ) > 0) {
                log.info("{} {}","Durable was Reserved. So set Assigned MachineRecipe From PO", hDRBLDCTRLJOB_OBJ);

                whatNextDurableAttributes.setMachineRecipeID(ObjectIdentifier.build(hFRDRBLPOASGN_RECIPE_ID,hFRDRBLPOASGN_RECIPE_OBJ));
                whatNextDurableAttributes.setPhysicalRecipeID                           (hFRDRBLPOASGN_PHRECIPE_ID);
            } else {
                //------------------------------------------------------------------------------------------------------
                //  Even if the Durable does not have relation with any logical recipe, "Recipe Available Flag" is "Yes".
                //  There is no problem in Auto-3 mode, but this flag should be "No" in case of other mode.
                //  Therefore, we changed to turn the flag "No" by condition.
                //
                //   1. Logical Recipe(Machine Recipe) is not found.
                //   2. And WhatNext mode is not "Auto-3".
                //   3. And the Lot does not have control job.
                //------------------------------------------------------------------------------------------------------
                if(0 == CimStringUtils.length(hFRMRCPRECIPE_ID) && !CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_AUTO3)) {
                    whatNextDurableAttributes.setRecipeAvailableFlag  (false);
                }
                whatNextDurableAttributes.setMachineRecipeID(ObjectIdentifier.build(hFRMRCPRECIPE_ID,hFRMRCPRECIPE_OBJ));
                whatNextDurableAttributes.setPhysicalRecipeID                           (hFRMRCPPHYSICAL_RECIPE_ID);
            }

            if( CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)
                    || CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_AUTO3) ) {
                if(CimBooleanUtils.isFalse(whatNextDurableAttributes.getRecipeAvailableFlag())) {
                    log.info("{}", "recipeAvailableFlag is not TRUE");
                    continue; //Loop of candidate Durables
                }
            }

            //---------------------------------------
            // Get entity inhibit information for durable
            //---------------------------------------
            log.info("{}", "check entityInhibit record start" );

            List<Infos.EntityInhibitInfo> entityInhibitInfoSeq;
            entityInhibitInfoSeq=new ArrayList<>(0);

            // setting entity information for checking entity inhibit in current operation
            // initializing entity information
            List<Constrain.EntityIdentifier> entities;
            int ent_len = 20;
            int INCREMENTAL_DEGREE = 10;
            entities=new ArrayList<>(ent_len);
            int numOfEntities = 0;

            if(CimStringUtils.length(ObjectIdentifier.fetchValue(whatNextDurableAttributes.getRouteID())) > 0) {
                if(numOfEntities >= ent_len) {
                    ent_len += INCREMENTAL_DEGREE;
                    log.info("{} {}", " #### expand entities length => ", ent_len);
//                    entities.length(ent_len);
                }
                entities.add(new Constrain.EntityIdentifier());
                entities.get(numOfEntities).setClassName  (BizConstant.SP_INHIBITCLASSID_ROUTE);
                entities.get(numOfEntities).setObjectId   (ObjectIdentifier.fetchValue(whatNextDurableAttributes.getRouteID()));
                if(CimStringUtils.length(whatNextDurableAttributes.getOperationNumber()) > 0) {
                    entities.get(numOfEntities).setAttrib (whatNextDurableAttributes.getOperationNumber());
                } else {
                    entities.get(numOfEntities).setAttrib ("");
                }
                log.info("{} {}", "entity.routeID   =", entities.get(numOfEntities).getObjectId());
                log.info("{} {}", "entity.operation =", entities.get(numOfEntities).getAttrib());
                ++numOfEntities;
            }

            if(CimStringUtils.length(ObjectIdentifier.fetchValue(whatNextDurableAttributes.getOperationID())) > 0) {
                if(numOfEntities >= ent_len) {
                    ent_len += INCREMENTAL_DEGREE;
                    log.info("{} {}", " #### expand entities length => ", ent_len);
//                    entities.length(ent_len);whatNextDurableAttributes.getOperationID()
                }
                entities.add(new Constrain.EntityIdentifier());
                entities.get(numOfEntities).setClassName  (BizConstant.SP_INHIBITCLASSID_PROCESS);
                entities.get(numOfEntities).setObjectId   (ObjectIdentifier.fetchValue(whatNextDurableAttributes.getOperationID()));
                entities.get(numOfEntities).setAttrib     ("");
                ++numOfEntities;
            }

            if(CimStringUtils.length(hFRDRBLPOMODULEPD_ID) > 0) {
                if(numOfEntities >= ent_len) {
                    ent_len += INCREMENTAL_DEGREE;
                    log.info("{} {}", " #### expand entities length => ", ent_len);
//                    entities.length(ent_len);
                }
                entities.add(new Constrain.EntityIdentifier());
                entities.get(numOfEntities).setClassName(BizConstant.SP_INHIBITCLASSID_MODULEPD);
                entities.get(numOfEntities).setObjectId(hFRDRBLPOMODULEPD_ID);
                entities.get(numOfEntities).setAttrib("");
                ++numOfEntities;
            }

            if(CimStringUtils.length(ObjectIdentifier.fetchValue(equipmentID)) > 0) {
                if(numOfEntities >= ent_len) {
                    ent_len += INCREMENTAL_DEGREE;
                    log.info("{} {}", " #### expand entities length => ", ent_len);
//                    entities.length(ent_len);
                }
                entities.add(new Constrain.EntityIdentifier());
                entities.get(numOfEntities).setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                entities.get(numOfEntities).setObjectId (hFREQPEQP_ID);
                entities.get(numOfEntities).setAttrib("");
                ++numOfEntities;
            }

            if( CimStringUtils.length(ObjectIdentifier.fetchValue(whatNextDurableAttributes.getMachineRecipeID())) > 0
                    && !CimStringUtils.equals(ObjectIdentifier.fetchValue(whatNextDurableAttributes.getMachineRecipeID()), "*")) {
                if(numOfEntities >= ent_len) {
                    ent_len += INCREMENTAL_DEGREE;
                    log.info("{} {}", " #### expand entities length => ", ent_len);
//                    entities.length(ent_len);
                }
                entities.add(new Constrain.EntityIdentifier());
                entities.get(numOfEntities).setClassName  (BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                entities.get(numOfEntities).setObjectId   (ObjectIdentifier.fetchValue(whatNextDurableAttributes.getMachineRecipeID()));
                entities.get(numOfEntities).setAttrib     ("");
                ++numOfEntities;
            }

            //---------------------
            // Reticle
            //---------------------
            if(CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
                if(numOfEntities >= ent_len) {
                    ent_len += INCREMENTAL_DEGREE;
                    log.info("{} {}", " #### expand entities length => ", ent_len);
//                    entities.length(ent_len);
                }
                entities.add(new Constrain.EntityIdentifier());
                entities.get(numOfEntities).setClassName(BizConstant.SP_INHIBITCLASSID_RETICLE);
                entities.get(numOfEntities).setObjectId(hDRBL_ID);
                entities.get(numOfEntities).setAttrib("");
                ++numOfEntities;
            }

            if(CimStringUtils.length(ObjectIdentifier.fetchValue(whatNextDurableAttributes.getStageID())) > 0) {
                if(numOfEntities >= ent_len) {
                    ent_len += INCREMENTAL_DEGREE;
                    log.info("{} {}", " #### expand entities length => ", ent_len);
//                    entities.length(ent_len);
                }
                entities.add(new Constrain.EntityIdentifier());
                entities.get(numOfEntities).setClassName (BizConstant.SP_INHIBITCLASSID_STAGE);
                entities.get(numOfEntities).setObjectId  (ObjectIdentifier.fetchValue(whatNextDurableAttributes.getStageID()));
                entities.get(numOfEntities).setAttrib    ("");
                ++numOfEntities;
            }

            // setting lengh of entity information
//            entities.length(numOfEntities);

            // checking inhibit with entity information
            List<Constrain.EntityInhibitRecord> inhibitRecords = entityInhibitManager.allEntityInhibitRecordsForDurableEntities(entities);
            List<Constrain.EntityInhibitRecord> inhibitRecords_var = inhibitRecords;

            // set entity inhibit information into return value
            int numOfInhibits = CimArrayUtils.getSize(inhibitRecords);
            log.info("{} {}", "numOfInhibits", numOfInhibits);
            entityInhibitInfoSeq=new ArrayList<>(numOfInhibits);

            setPptstructFromPosstructForEntityinhibitrecords(entityInhibitInfoSeq, inhibitRecords);

            for(int n2 = 0; n2 < numOfInhibits; n2++) {
                // getting reason desc
                String hFRCODECATEGORY_ID = BizConstant.SP_REASONCAT_ENTITYINHIBIT;
                String hFRCODECODE_ID = entityInhibitInfoSeq.get(n2).getEntityInhibitAttributes().getReasonCode();

                String hFRCODEDESCRIPTION = CimObjectUtils.toString(cimJpaRepository.queryOneColumnAndUnique("SELECT DESCRIPTION\n" +
                                "                FROM OMCODE\n" +
                                "                WHERE OMCODE.CODETYPE_ID = ? AND\n" +
                                "                OMCODE.CODE_ID     = ?", hFRCODECATEGORY_ID,
                        hFRCODECODE_ID));

                if(hFRCODEDESCRIPTION!=null) {
                    entityInhibitInfoSeq.get(n2).getEntityInhibitAttributes().setReasonDesc (hFRCODEDESCRIPTION);
                } else {
                    entityInhibitInfoSeq.get(n2).getEntityInhibitAttributes().setReasonDesc ("");
                }
            }

            whatNextDurableAttributes.setEntityInhibitions(new ArrayList<>(numOfInhibits));
            for(int m = 0; m < numOfInhibits; m++) {
                log.info("{} {}", "Loop for Inhibits", m);
                whatNextDurableAttributes.getEntityInhibitions().add(entityInhibitInfoSeq.get(m).getEntityInhibitAttributes());
            }

            if(CimArrayUtils.getSize(entityInhibitInfoSeq) > 0) {
                log.info("PPTManager_i::equipment_lots_WhatNext","entityInhibitInfoSeq.length() > 0");

                if( CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)
                        || CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_AUTO3) ) {
                    log.info("PPTManager_i::equipment_lots_WhatNext","found entity inhibition, but ignore this lot because of Auto3 mode");
                    continue;
                }
            }

            log.info("{}", "check entityInhibit record end" );
            // end of entity inhibit

            //------------------------
            // Check Select Criteria
            //------------------------
            log.info("{} {}", "Input selectCriteria", selectCriteria);
            tmpWhatNextAttributes.add(nWhatNextDurableCount,whatNextDurableAttributes);
            nWhatNextDurableCount++;
        }

        log.info("{} {}", "nWhatNextDurableCount", nWhatNextDurableCount);
//        tmpWhatNextAttributes.length(nWhatNextDurableCount);
        strEquipmentdurablesWhatNextDRout.setStrWhatNextDurableAttributes(tmpWhatNextAttributes);

        log.info("PPTManager_i::equipment_durables_WhatNextDR");
        return strEquipmentdurablesWhatNextDRout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param pptStruct
     * @param posStruct
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/1 17:54
     */
    private void setPptstructFromPosstructForEntityinhibitrecords(List<Infos.EntityInhibitInfo> pptStruct,List<Constrain.EntityInhibitRecord> posStruct) {
        int numOfInhibits = CimArrayUtils.getSize(posStruct);
        for ( int iterator1 = 0 ; iterator1 < numOfInhibits ; iterator1++ ) {
            int numOfEntities = CimArrayUtils.getSize(posStruct.get(iterator1).getEntities());
            pptStruct.add(new Infos.EntityInhibitInfo());
            pptStruct.get(iterator1).setEntityInhibitAttributes(new Infos.EntityInhibitAttributes());
            pptStruct.get(iterator1).getEntityInhibitAttributes().setEntities(new ArrayList<>(numOfEntities));

            for ( int iterator2 = 0 ; iterator2 < numOfEntities ; iterator2++ ) {
                pptStruct.get(iterator1).getEntityInhibitAttributes().getEntities().add(new Infos.EntityIdentifier());
                pptStruct.get(iterator1).getEntityInhibitAttributes().getEntities().get(iterator2).setClassName  (posStruct.get(iterator1).getEntities().get(iterator2).getClassName());
                pptStruct.get(iterator1).getEntityInhibitAttributes().getEntities().get(iterator2).setObjectID   (new ObjectIdentifier(posStruct.get(iterator1).getEntities().get(iterator2).getObjectId()));
                pptStruct.get(iterator1).getEntityInhibitAttributes().getEntities().get(iterator2).setAttribution(posStruct.get(iterator1).getEntities().get(iterator2).getAttrib());
            }

            pptStruct.get(iterator1).setEntityInhibitID(ObjectIdentifier.build(posStruct.get(iterator1).getId(),
                    posStruct.get(iterator1).getReferenceKey()));
            pptStruct.get(iterator1).getEntityInhibitAttributes().setSubLotTypes        (posStruct.get(iterator1).getSubLotTypes());
            pptStruct.get(iterator1).getEntityInhibitAttributes().setStartTimeStamp     (CimDateUtils.getTimestampAsString(posStruct.get(iterator1).getStartTimeStamp()));
            pptStruct.get(iterator1).getEntityInhibitAttributes().setEndTimeStamp       (CimDateUtils.getTimestampAsString(posStruct.get(iterator1).getEndTimeStamp()));
            pptStruct.get(iterator1).getEntityInhibitAttributes().setClaimedTimeStamp   (CimDateUtils.getTimestampAsString(posStruct.get(iterator1).getChangedTimeStamp()));
            pptStruct.get(iterator1).getEntityInhibitAttributes().setReasonCode         (ObjectIdentifier.fetchValue(posStruct.get(iterator1).getReasonCode()));
            pptStruct.get(iterator1).getEntityInhibitAttributes().setOwnerID            (posStruct.get(iterator1).getOwner());
            pptStruct.get(iterator1).getEntityInhibitAttributes().setMemo               (posStruct.get(iterator1).getClaimMemo());
        }
    }

    @Override
    public void machineReticlePodPortReserveCancel(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier portID) {
        log.debug("step1- equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(strObjCommonIn, equipmentID);
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("bStorageMachineFlag == FALSE");
            //------------------------------
            //       Get Equipment Object
            //------------------------------
            CimMachine aMachine = null;
            if (ObjectIdentifier.isEmptyWithRefKey(equipmentID)) {
                Validations.check(ObjectIdentifier.isEmptyWithValue(equipmentID), retCodeConfig.getNotFoundEqp(), "*****");
                aMachine = machineManager.findMachineNamed(equipmentID.getValue());
            } else {
                aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID.getReferenceKey());
            }
            Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));

            //--------------------------------------
            //       Get Port Resource Object
            //--------------------------------------
            CimReticlePodPortResource aPosReticlePodPortResource = aMachine.findReticlePodPortResourceNamed(ObjectIdentifier.fetchValue(portID));
            Validations.check(null == aPosReticlePodPortResource, retCodeConfig.getRspportNotFound(), "");

            //--------------------------------------
            //       Get Port ReticlePod Object
            //--------------------------------------
            aPosReticlePodPortResource.setDispatchState(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED);
            aPosReticlePodPortResource.setReserveState(BizConstant.SP_RETICLEPODPORT_NOTRESERVED);
            aPosReticlePodPortResource.setTransferReservedReticlePod(null);
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)
                || CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("step2- stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(strObjCommonIn, machineTypeGetOut.getStockerID());
            if (CimArrayUtils.isNotEmpty(reticlePodPortInfoList)) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), portID)) {
                        reticlePodPortInfo.setReservedReticlePodID(ObjectIdentifier.buildWithValue(""));
                        reticlePodPortInfo.setTransferReserveStatus(BizConstant.SP_RETICLEPODPORT_NOTRESERVED);
                        reticlePodPortInfo.setTransferReserveTimestamp(CimDateUtils.convertToSpecString(strObjCommonIn.getTimeStamp().getReportTimeStamp()));
                        reticlePodPortInfo.setDispatchStatus(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED);
                        reticlePodPortInfo.setDispatchTimestamp(CimDateUtils.convertToSpecString(strObjCommonIn.getTimeStamp().getReportTimeStamp()));

                        Inputs.StockerReticlePodPortDispatchStateChangeIn stockerReticlePodPortDispatchStateChangeIn = new Inputs.StockerReticlePodPortDispatchStateChangeIn();
                        stockerReticlePodPortDispatchStateChangeIn.setStockerID(equipmentID);
                        stockerReticlePodPortDispatchStateChangeIn.setStrReticlePodPortInfo(reticlePodPortInfo);
                        log.debug("step3- stockerMethod.stockerReticlePodPortDispatchStateChange");
                        stockerMethod.stockerReticlePodPortDispatchStateChange(strObjCommonIn, stockerReticlePodPortDispatchStateChangeIn);
                        break;
                    }
                }
            }
        } else {
            log.error("stockerType {} is invalide", machineTypeGetOut.getStockerType());
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }
    }

    @Override
    public Outputs.EquipmentTargetPortPickupOut durableEquipmentTargetPortPickup(Infos.ObjCommon objCommonIn, Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroup, Infos.EqpBrInfo eqpBrInfo, Infos.EqpPortInfo eqpPortInfo) {
        Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut = new Outputs.EquipmentTargetPortPickupOut();
        Infos.EqpTargetPortInfo eqpTargetPortInfo = new Infos.EqpTargetPortInfo();
        equipmentTargetPortPickupOut.setEqpTargetPortInfo(eqpTargetPortInfo);
        eqpTargetPortInfo.setEquipmentID(eqpPortInfoOrderByGroup.getEquipmentID());
        equipmentTargetPortPickupOut.setWhatsNextRequireFlag(false);
        equipmentTargetPortPickupOut.setEmptyCassetteRequireFlag(false);


        String tmpUseCDRForAutoDispatchFlag = StandardProperties.OM_XFER_CARRIER_WITH_AUTO3_DISPATCH.getValue();

        boolean unloadEventFlag = false;
        boolean loadEventFlag = false;

        if (CimStringUtils.equals("1", tmpUseCDRForAutoDispatchFlag)) {
            List<ObjectIdentifier> equipmentIDs = new ArrayList<>(1);
            equipmentIDs.add(eqpPortInfoOrderByGroup.getEquipmentID());
            List<Infos.EqpAuto3SettingInfo>  eqpAuto3SettingInfoList = equipmentMethod.equipmentAuto3DispatchSettingListGetDR(objCommonIn, equipmentIDs);

            for (Infos.EqpAuto3SettingInfo eqpAuto3SettingInfo : eqpAuto3SettingInfoList) {
                if (CimStringUtils.equals(BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_UNLOADREQ,eqpAuto3SettingInfo.getCarrierTransferRequestEvent())){
                    log.debug("Existing auto3Setting for UnloadReq");
                    unloadEventFlag = true;
                }else if (CimStringUtils.equals(eqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_LOADREQ)){
                    log.debug("Existing auto3Setting for LoadReq");
                    loadEventFlag = true;
                }
            }
            Validations.check(CimBooleanUtils.isFalse(unloadEventFlag) && CimBooleanUtils.isFalse(loadEventFlag), retCodeConfig.getNotFoundTargetPort());
        }
        CimMachine posMachine = baseCoreFactory.getBO(CimMachine.class, eqpPortInfoOrderByGroup.getEquipmentID());
        boolean bFurnace = posMachine.isFurnace();
        String variableLoadState = !bFurnace ? BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ : BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL;


        //----------------------------------------------------------------------
        //  Sort <PortGroup> by dispatchState_TimeStamp field of one strPortID
        //  whose dispatchState_TimeStamp is earliest in a PortGroup.
        //----------------------------------------------------------------------

        // Sort the PortIDs in each PortGroup ascending by Dispatch_State_Timestamp
        // then sort each PortGroup by the max Dispatch_state_timestamp of the PortID
        Infos.EqpPortInfoOrderByGroup tmpEqpPortInfoOrderByGroup = new Infos.EqpPortInfoOrderByGroup();
        tmpEqpPortInfoOrderByGroup.setStrPortGroup(new ArrayList<>());
        String tmpTimeStamp = null;
        List<String> tmpTimeStapList = new ArrayList<>();
        int a = 0;
        int nInputPortGrpLen = CimArrayUtils.getSize(eqpPortInfoOrderByGroup.getStrPortGroup());
        for (a = 0; a < nInputPortGrpLen; a++) {
            int b = 0;
            int nInputPortLen = CimArrayUtils.getSize(eqpPortInfoOrderByGroup.getStrPortGroup().get(a).getStrPortID());
            for (b = 0; b < nInputPortLen; b++) {
                int tb = 0;
                int nInputLotLen = CimArrayUtils.getSize(eqpPortInfoOrderByGroup.getStrPortGroup().get(a).getStrPortID().get(tb).getLotInfoOnPortList());
                for (tb = 0; tb < nInputLotLen; tb++) {

                }
                if (b == 0) {
                    tmpTimeStamp = eqpPortInfoOrderByGroup.getStrPortGroup().get(a).getStrPortID().get(0).getDispatchStateTimeStamp();
                } else {
                    if (0 < CimDateUtils.convertToOrInitialTime(tmpTimeStamp).compareTo(
                            CimDateUtils.convertToOrInitialTime(eqpPortInfoOrderByGroup.getStrPortGroup().get(a).getStrPortID().get(b).getDispatchStateTimeStamp()))) {
                        tmpTimeStamp = eqpPortInfoOrderByGroup.getStrPortGroup().get(a).getStrPortID().get(b).getDispatchStateTimeStamp();
                    }
                }
            }

            boolean bTmpAddFlag = false;
            int c = 0;
            for (c = 0; c < a; c++) {
                if (a > 0) {
                    if (0 > CimDateUtils.convertToOrInitialTime(tmpTimeStamp).compareTo(
                            CimDateUtils.convertToOrInitialTime(tmpTimeStapList.get(c))
                    )) {
                        int d = 0;
                        for (d = a; d > c; d--) {
                            tmpTimeStapList.add(d, tmpTimeStapList.get(d - 1));
                            tmpEqpPortInfoOrderByGroup.getStrPortGroup().add(d, tmpEqpPortInfoOrderByGroup.getStrPortGroup().get(d - 1));
                        }
                        tmpTimeStapList.set(c, tmpTimeStamp);
                        tmpEqpPortInfoOrderByGroup.getStrPortGroup().set(c, eqpPortInfoOrderByGroup.getStrPortGroup().get(a));
                        bTmpAddFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bTmpAddFlag)) {
                tmpTimeStapList.add(a, tmpTimeStamp);
                tmpEqpPortInfoOrderByGroup.getStrPortGroup().add(a, eqpPortInfoOrderByGroup.getStrPortGroup().get(a));
            }
        }

        for (Infos.PortGroup portGroup : tmpEqpPortInfoOrderByGroup.getStrPortGroup()) {
            log.debug(String.format("[port Group ID] : [ %s ]", portGroup.getPortGroup()));
        }

        //-------------------------------------------------------------------------------------
        //  Search port Group which is much for Load conditions or Unload Conditions
        //  At first, Program looking for port Group which has port under UnloadReq Condition
        //-------------------------------------------------------------------------------------
        boolean bLoadPortFoundFlag = false;
        boolean bUnloadPortFoundFlag = false;
        int nOutPortGrpLen = 0;
        int i = 0;
        int nPortGrpLen = CimArrayUtils.getSize(tmpEqpPortInfoOrderByGroup.getStrPortGroup());

        if (!CimStringUtils.equals(tmpUseCDRForAutoDispatchFlag, "1")
                || (CimStringUtils.equals(tmpUseCDRForAutoDispatchFlag, "1") && CimBooleanUtils.isTrue(unloadEventFlag))) {
            for (i = 0; i < nPortGrpLen; i++) {
                boolean bPortGrpAddFlg = false;
                int nOutPortLen = 0;

                int j = 0;
                int nPortLen = CimArrayUtils.getSize(tmpEqpPortInfoOrderByGroup.getStrPortGroup().get(i).getStrPortID());

                Infos.PortGroup iTmpPortGroup = tmpEqpPortInfoOrderByGroup.getStrPortGroup().get(i);

                for (j = 0; j < nPortLen; j++) {
                    int tj = 0;
                    Infos.PortID jTmpPortID = iTmpPortGroup.getStrPortID().get(j);
                    int nLotLen = jTmpPortID.getLotInfoOnPortList().size();
                    if (!CimStringUtils.equals(jTmpPortID.getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
                        log.debug("port State is not 'UnloadReq'");
                        continue;
                    }

                    if (CimStringUtils.equals(jTmpPortID.getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
                        log.debug("port State is 'UnloadReq'");
                        boolean bCheckDispatchMode = true;
                        int k;
                        int nEqpPortGrpLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                        for (k = 0; k < nEqpPortGrpLen; k++) {
                            Infos.EqpPortStatus kEqpPortStatus = eqpPortInfo.getEqpPortStatuses().get(k);
                            if (CimStringUtils.equals(iTmpPortGroup.getPortGroup(), kEqpPortStatus.getPortGroup())) {
                                if (!CimStringUtils.equals(kEqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                        || CimStringUtils.equals(kEqpPortStatus.getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE))
                                    bCheckDispatchMode = false;
                                break;
                            } else {
                                log.debug("No Check Ignore");
                            }
                        }
                        if (CimBooleanUtils.isFalse(bCheckDispatchMode)) {
                            log.debug("Add Check PortGroup NG");
                            continue;
                        } else {
                            log.debug("Add Check PortGroup OK");
                        }

                        if (!CimStringUtils.equals(jTmpPortID.getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_OUTPUT)
                                && !CimStringUtils.equals(jTmpPortID.getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_INPUTOUTPUT)) {
                            log.debug("port Usage is not Output or InputOutput");
                            continue;
                        }

                        if (!CimStringUtils.equals(jTmpPortID.getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED)) {
                            log.debug("dispatch State is not 'Required'");
                            continue;
                        }

                        if (ObjectIdentifier.isEmpty(jTmpPortID.getCassetteID())) {
                            log.debug("loadedCassetteID is null");
                            continue;
                        }
                        //check durable processing state
                        ObjectIdentifier cassetteID = jTmpPortID.getCassetteID();
                        String durableProcessStateGetResult = durableMethod.durableProcessStateGet(objCommonIn, BizConstant.SP_DURABLECAT_CASSETTE, cassetteID);
                        if (BizConstant.equalsIgnoreCase(durableProcessStateGetResult, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING)) {
                            log.debug("lot is Processing continue");
                            continue;
                        }

                        bUnloadPortFoundFlag = true;

                        if (CimBooleanUtils.isFalse(bPortGrpAddFlg)) {
                            eqpTargetPortInfo.setPortGroups(new ArrayList<>(nOutPortGrpLen + 1));
                            Infos.PortGroup portGroup = new Infos.PortGroup();
                            eqpTargetPortInfo.getPortGroups().add(nOutPortGrpLen,portGroup);
                            eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).setPortGroup(tmpEqpPortInfoOrderByGroup.getStrPortGroup().get(i).getPortGroup());
                            eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).setStrPortID(new ArrayList<>(0));

                            nOutPortGrpLen++;
                            bPortGrpAddFlg = true;
                        }
                        if (CimArrayUtils.getSize(eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen - 1).getStrPortID()) == 0){
                            eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen - 1).setStrPortID(new ArrayList<>());
                        }

                        boolean bPortAddFlg = false;

                        for (k = 0; k < nOutPortLen; k++) {
                            if (eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen - 1).getStrPortID().get(k).getLoadSequenceNoInPortGroup()
                                    > jTmpPortID.getLoadSequenceNoInPortGroup()) {
                                int l = k;
                                for (l = nOutPortLen; l > k; l--) {
                                    eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen - 1).getStrPortID().set(l,
                                            eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen - 1).getStrPortID().get(l - 1));
                                }
                                eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen - 1).getStrPortID().set(k, jTmpPortID);
                                bPortAddFlg = true;
                                break;
                            }
                        }

                        if (CimBooleanUtils.isFalse(bPortAddFlg)) {
                            eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen - 1).getStrPortID().add(nOutPortLen, jTmpPortID);
                        }
                        nOutPortLen++;
                    }
                }
                if (CimBooleanUtils.isTrue(bUnloadPortFoundFlag)) {
                    break;
                }
            }
        }

        if (CimBooleanUtils.isFalse(bUnloadPortFoundFlag)) {

            if (!CimStringUtils.equals(tmpUseCDRForAutoDispatchFlag, "1") ||
                    (CimStringUtils.equals(tmpUseCDRForAutoDispatchFlag, "1")
                            && loadEventFlag)) {
                for (i = 0; i < nPortGrpLen; i++) {

                    boolean bCheckDispatchMode = true;
                    int j;
                    int nEqpPortGrpLen = eqpPortInfo.getEqpPortStatuses().size();
                    Infos.PortGroup iTmpPortGroup = tmpEqpPortInfoOrderByGroup.getStrPortGroup().get(i);
                    for (j = 0; j < nEqpPortGrpLen; j++) {
                        Infos.EqpPortStatus eqpPortStatus = eqpPortInfo.getEqpPortStatuses().get(j);

                        if (CimStringUtils.equals(iTmpPortGroup.getPortGroup(), eqpPortStatus.getPortGroup())) {
                            if (!CimStringUtils.equals(eqpPortStatus.getDispatchMode(), BizConstant.SP_EQP_DISPATCHMODE_AUTO)
                                    || !CimStringUtils.equals(eqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                    || CimStringUtils.equals(eqpPortStatus.getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                                bCheckDispatchMode = false;
                                break;
                            }
                        } else {
                            log.debug("NoCheck ignore");
                        }
                    }

                    if (CimBooleanUtils.isFalse(bCheckDispatchMode)) {
                        log.debug("Add Check PortGroup NG");
                        continue;
                    } else {
                        log.debug("Add Check PortGroup OK");
                    }

                    int nOutPortLen = 0; // Not Used ?
                    int nPortLen = CimArrayUtils.getSize(iTmpPortGroup.getStrPortID());

                    int m = 0;
                    int nLoadReqPortCnt = 0;
                    for (m = 0; m < nPortLen; m++) {
                        int tm = 0;
                        Infos.PortID mTmpPortID = tmpEqpPortInfoOrderByGroup.getStrPortGroup().get(i).getStrPortID().get(m);
                        int nOutLotLen = CimArrayUtils.getSize(mTmpPortID.getLotInfoOnPortList());

                        for (tm = 0; tm < nOutLotLen; tm++) {
                            log.debug("Check Input lot On port State");
                        }

                        if (!CimStringUtils.equals(mTmpPortID.getPortState(), variableLoadState)) {
                            log.debug("port State is not Good");
                            break;
                        }

                        if (!CimStringUtils.equals(mTmpPortID.getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_INPUT)
                                && !CimStringUtils.equals(mTmpPortID.getPortUsage(), CIMStateConst.CIM_PORT_RESOURCE_INPUTOUTPUT)) {
                            log.debug("port Usage is not Input or InputOutput");
                            break;
                        }

                        //【Neyo】: change to check Other PurposeType because of DMS only support other type
                        if (!CimStringUtils.equals(mTmpPortID.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_OTHER)){
                            log.debug("loadPurposeType is not [Other]");
                            break;
                        }

                        if (!CimStringUtils.equals(mTmpPortID.getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED)) {
                            log.debug("dispatch State is not 'Required'");
                            break;
                        }

                        nLoadReqPortCnt++;

                        if (nLoadReqPortCnt == nPortLen) {
                            if (null == eqpTargetPortInfo.getPortGroups()){
                                eqpTargetPortInfo.setPortGroups(new ArrayList<>());
                            }
                            Infos.PortGroup _portGroup = new Infos.PortGroup();
                            _portGroup.setPortGroup(iTmpPortGroup.getPortGroup());
                            eqpTargetPortInfo.getPortGroups().add(nOutPortGrpLen, _portGroup);
                            eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).setStrPortID(new ArrayList<>(0));

                            int n = 0;
                            int nTmpPortLen = iTmpPortGroup.getStrPortID().size();

                            for (n = 0; n < nTmpPortLen; n++) {
                                boolean bTmpAddFlg = false;
                                int o = 0;
                                Infos.PortID nTmpPortID = iTmpPortGroup.getStrPortID().get(n);
                                for (o = 0; o < n; o++) {
                                    if (nTmpPortID.getLoadSequenceNoInPortGroup()
                                            < eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).getStrPortID().get(o).getLoadSequenceNoInPortGroup()) {

                                        int p = 0;
                                        for (p = n; p > o; p--) {
                                            eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).getStrPortID().set(p,
                                                    eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).getStrPortID().get(p - 1));
                                            bTmpAddFlg = true;
                                        }
                                        eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).getStrPortID().add(o, nTmpPortID);
                                    }
                                }
                                if (CimBooleanUtils.isFalse(bTmpAddFlg)) {
                                    eqpTargetPortInfo.getPortGroups().get(nOutPortGrpLen).getStrPortID().add(n, nTmpPortID);
                                }
                            }
                            nOutPortGrpLen++;
                            bLoadPortFoundFlag = true;
                        }
                    }
                }
            }
        }
        Validations.check(CimBooleanUtils.isFalse(bLoadPortFoundFlag) && CimBooleanUtils.isFalse(bUnloadPortFoundFlag), retCodeConfig.getNotFoundTargetPort());

        if (CimBooleanUtils.isTrue(bLoadPortFoundFlag)) {
            equipmentTargetPortPickupOut.setWhatsNextRequireFlag(true);
            equipmentTargetPortPickupOut.setEmptyCassetteRequireFlag(true);
            equipmentTargetPortPickupOut.setTargetPortType(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ);
        }

        if (CimBooleanUtils.isTrue(bUnloadPortFoundFlag)) {
            equipmentTargetPortPickupOut.setTargetPortType(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ);
        }
        return equipmentTargetPortPickupOut;
    }
}
