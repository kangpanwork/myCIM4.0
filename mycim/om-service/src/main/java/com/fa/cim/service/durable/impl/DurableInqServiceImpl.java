package com.fa.cim.service.durable.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.entity.runtime.bank.CimBankDestBankDO;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.*;
import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.durable.IDurableInqService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.equipment.IEquipmentService;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 20:05
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class DurableInqServiceImpl implements IDurableInqService {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentForDurableMethod equipmentForDurableMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IBankForDurableMethod bankForDurableMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IDurableControlJobMethod durableControlJobMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IProcessForDurableMethod processForDurableMethod;

    @Autowired
    private IObjectLockMethod lockMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IEquipmentService equipmentService;

    @Autowired
    private IDurableService durableService;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private ISorterNewMethod sorterMethod;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private IStockerMethod stockerMethod;

    private Random random = new Random();


    @Override
    public Results.WhatNextDurableListInqResult sxDrbWhatNextListInq(Infos.ObjCommon strObjCommonIn, Params.WhatNextDurableListInqInParam strWhatNextDurableListInqInParam) {
        Results.WhatNextDurableListInqResult strWhatNextDurableListInqResult = new Results.WhatNextDurableListInqResult();

        /*------------------------------------------------------------------------*/
        /*   Set Inquired Data                                                    */
        /*------------------------------------------------------------------------*/

        Infos.EquipmentDurablesWhatNextDROut strEquipmentdurablesWhatNextDRout = new Infos.EquipmentDurablesWhatNextDROut();
        strEquipmentdurablesWhatNextDRout.setProcessRunSizeMaximum(0L);
        strEquipmentdurablesWhatNextDRout.setProcessRunSizeMinimum(0L);

        Infos.EquipmentDurablesWhatNextDRIn strEquipmentdurablesWhatNextDRin = new Infos.EquipmentDurablesWhatNextDRIn();
        strEquipmentdurablesWhatNextDRin.setEquipmentID(strWhatNextDurableListInqInParam.getEquipmentID());
        strEquipmentdurablesWhatNextDRin.setDurableCategory(strWhatNextDurableListInqInParam.getDurableCategory());
        strEquipmentdurablesWhatNextDRin.setSelectCriteria(strWhatNextDurableListInqInParam.getSelectCriteria());
        try {
            strEquipmentdurablesWhatNextDRout = equipmentForDurableMethod.equipmentDurablesWhatNextDR(
                    strEquipmentdurablesWhatNextDRout,
                    strObjCommonIn,
                    strEquipmentdurablesWhatNextDRin);
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfigEx.getNoWipDurable())) {
                throw ex;
            }
        }

        strWhatNextDurableListInqResult.setLastRecipeID(strEquipmentdurablesWhatNextDRout.getLastRecipeID());
        strWhatNextDurableListInqResult.setDispatchRule(strEquipmentdurablesWhatNextDRout.getDispatchRule());
        strWhatNextDurableListInqResult.setProcessRunSizeMaximum(strEquipmentdurablesWhatNextDRout.getProcessRunSizeMaximum());
        strWhatNextDurableListInqResult.setProcessRunSizeMinimum(strEquipmentdurablesWhatNextDRout.getProcessRunSizeMinimum());
        strWhatNextDurableListInqResult.setEquipmentCategory(strEquipmentdurablesWhatNextDRout.getEquipmentCategory());

        strWhatNextDurableListInqResult.setStrWhatNextDurableAttributes(strEquipmentdurablesWhatNextDRout.getStrWhatNextDurableAttributes());

        /*------------------------------------------------------------------------*/
        /*   Return                                                               */
        /*------------------------------------------------------------------------*/
        return strWhatNextDurableListInqResult;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableID
     * @return
     * @author Ho
     * @date 2018/9/26 14:18:06
     */
    @Override
    public Results.DurableStatusSelectionInqResult sxDurableStatusSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier durableID) {
        // step1 - durable_FillInTxPDQ011DR
        return durableMethod.durableFillInTxPDQ011DR(objCommon, durableID);
    }

    @Override
    public List<Infos.CandidateDurableSubStatusDetail> sxDurableSubStatusSelectionInq(Infos.ObjCommon objCommon, Params.DurableSubStatusSelectionInqParams params) {
        return durableMethod.durableFillInTxPDQ034DR(objCommon, params);
    }

    @Override
    public List<Infos.ConnectedRouteList> sxConnectedDurableRouteListInq(Infos.ObjCommon objCommon, Infos.ConnectedDurableRouteListInqInParam param) {
        return processMethod.processConnectedRouteListForDurable(objCommon, param.getDurableCategory(), param.getDurableID(), param.getRouteType());
    }

    @Override
    public List<Infos.DurableControlJobListInfo> sxDrbControlJobListInq(Infos.ObjCommon objCommon, Params.DurableControlJobListInqParams params) {
        Inputs.DurableControlJobListGetDRIn in = new Inputs.DurableControlJobListGetDRIn();
        in.setCreateUserID(params.getCreateUserID());
        in.setDurableCategory(params.getDurableCategory());
        in.setDurableID(params.getDurableID());
        in.setDurableInfoFlag(params.getDurableInfoFlag());
        in.setDurableJobID(params.getDurableJobID());
        in.setEquipmentID(params.getEquipmentID());
        return durableControlJobMethod.durableControlJobListGetDR(objCommon, in);
    }

    @Override
    public List<Infos.DurableHoldListAttributes> sxDrbHoldListInq(Infos.ObjCommon objCommon, Params.DurableHoldListInqInParam params) {
        //---------------------------------------
        // Call durable_FillInODRBQ019
        //---------------------------------------
        List<Infos.DurableHoldListAttributes> holdListAttributes = durableMethod.durableFillInODRBQ019(objCommon, params.getDurableCategory(), params.getDurableID());
        return holdListAttributes;
    }

    /**
     * This function returns Information of Start Durable.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/24 15:42
     */
    @Override
    public Results.DurablesInfoForOpeStartInqResult sxDrbInfoForMoveInInq(Infos.ObjCommon objCommon, Params.DurablesInfoForOpeStartInqInParam paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());

        Results.DurablesInfoForOpeStartInqResult retVal = new Results.DurablesInfoForOpeStartInqResult();
        /*--------------------------------------------------------------------------------*/
        /* 1.Transaction ID and SpecialControls of Equipment Category consistency Check.  */
        /*--------------------------------------------------------------------------------*/
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(objCommon, paramIn.getEquipmentID());

        /*---------------------------------------------*/
        /* 2.Get reserved durable control job list.    */
        /*---------------------------------------------*/
        List<ObjectIdentifier> reservedDurableControlJobIDs = equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, paramIn.getEquipmentID());

        /*---------------------------------------------*/
        /* 3.Get durable control job list.             */
        /*---------------------------------------------*/
        Inputs.DurableControlJobListGetDRIn durableControlJobListGetDRIn = new Inputs.DurableControlJobListGetDRIn();
        durableControlJobListGetDRIn.setEquipmentID(paramIn.getEquipmentID());
        durableControlJobListGetDRIn.setDurableJobID(paramIn.getDurableControlJobID());
        durableControlJobListGetDRIn.setDurableInfoFlag(false);
        List<Infos.DurableControlJobListInfo> durableControlJobListInfos = durableControlJobMethod.durableControlJobListGetDR(objCommon, durableControlJobListGetDRIn);
        Validations.check(CimArrayUtils.isEmpty(durableControlJobListInfos), retCodeConfig.getDurableControlJobBlank());

        /*-------------------------------------------------------------------------------------------------------*/
        /* 4.If durables durableControlJobID is not exist in equipment reserved durableControlJobs, error return */
        /*-------------------------------------------------------------------------------------------------------*/
        for (Infos.DurableControlJobListInfo durableControlJobListInfo : durableControlJobListInfos) {
            boolean findFlag = false;
            for (ObjectIdentifier reservedDurableControlJobID : reservedDurableControlJobIDs) {
                if (ObjectIdentifier.equalsWithValue(durableControlJobListInfo.getDurableControlJobID(), reservedDurableControlJobID)) {
                    findFlag = true;
                    break;
                }
            }
            if (!findFlag) {
                Validations.check(retCodeConfig.getNotReservedDctrljobPortgrp());
            }
        }

        /*--------------------------------------------------------------*/
        /* 5.Get start reservation information of durable control job.  */
        /*--------------------------------------------------------------*/
        Outputs.DurableControlJobStartReserveInformationGetOut reserveInformationGetOut = durableControlJobMethod.durableControlJobStartReserveInformationGet(objCommon, paramIn.getDurableControlJobID());
        assert null != reserveInformationGetOut;

        /*-----------------------------------------*/
        /* 6.durableCategory is Cassette           */
        /*-----------------------------------------*/
        if (CimStringUtils.equals(paramIn.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            /*---------------------*/
            /* A.Get PortGroupID   */
            /*---------------------*/
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, paramIn.getEquipmentID());

            /*---------------------------------------------------------------------------------------------*/
            /* B.Find the portGroupID of port information that CassetteID is the same as loadedCassetteID. */
            /*---------------------------------------------------------------------------------------------*/
            boolean findFlag = false;
            if (null != eqpPortInfo) {
                List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
                List<Infos.StartDurable> startDurables = reserveInformationGetOut.getStrStartDurables();
                for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses) {
                    for (Infos.StartDurable startDurable : startDurables) {
                        if (ObjectIdentifier.equalsWithValue(eqpPortStatus.getLoadedCassetteID(), startDurable.getDurableId())) {
                            /*---------------------------------------------*/
                            /* C.If same cassette exists, set portGroupID. */
                            /*---------------------------------------------*/
                            retVal.setPortGroupID(eqpPortStatus.getPortGroup());
                            findFlag = true;
                            break;
                        }
                    }
                    if (findFlag) {
                        break;
                    }
                }
            }
        }

        /*---------------------------------------------------*/
        /* 7.Set following informations to Return Structure. */
        /*---------------------------------------------------*/
        retVal.setEquipmentID(reserveInformationGetOut.getEquipmentID());
        retVal.setDurableCategory(reserveInformationGetOut.getDurableCategory());
        retVal.setStrStartDurables(reserveInformationGetOut.getStrStartDurables());
        retVal.setStrDurableStartRecipe(reserveInformationGetOut.getStrDurableStartRecipe());
        return retVal;
    }

    @Override
    public Results.DurablesInfoForStartReservationInqResult sxDrbInfoForMoveInReserveInq(Infos.ObjCommon objCommon, Params.DurablesInfoForStartReservationInqInParam param) {
        Validations.check(null == objCommon || null == param, retCodeConfig.getInvalidInputParam());
        Results.DurablesInfoForStartReservationInqResult retVal = null;

        //---------------------------------------
        // Check Durable Category
        //---------------------------------------
        String durableCategory = param.getDurableCategory();
        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        //---------------------------------------
        // Call equipment_SpecialControlVsTxID_CheckCombination
        //---------------------------------------
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(objCommon, param.getEquipmentID());

        //---------------------------------------
        // Get Information for Start Reservation
        //---------------------------------------
        // todo
        Outputs.ProcessStartDurablesReserveInformationGetBaseInfoForClientOut baseInfoForClientOut = processForDurableMethod.processStartDurablesReserveInformationGetBaseInfoForClient(objCommon,
                param.getEquipmentID(),
                durableCategory,
                param.getDurableIDs());
        if (null != baseInfoForClientOut) {
            retVal = new Results.DurablesInfoForStartReservationInqResult();
            retVal.setDurableCategory(baseInfoForClientOut.getDurableCategory());
            retVal.setEquipmentID(baseInfoForClientOut.getEquipmentID());
            retVal.setStrDurableStartRecipe(baseInfoForClientOut.getStrDurableStartRecipe());
            retVal.setStrStartDurables(baseInfoForClientOut.getStrStartDurables());
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
     * @param strDurableOperationHistoryInqInParam
     * @return java.util.List<com.fa.cim.dto.Infos.DurableOperationHisInfo>
     * @throws
     * @author ho
     * @date 2020/6/22 13:44
     */
    @Override
    public List<Infos.DurableOperationHisInfo> sxDrbStepHistoryInq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationHistoryInqInParam strDurableOperationHistoryInqInParam) {
        log.info("PPTManager_i:: txDurableOperationHistoryInq ");

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        log.info("", "in-parm durableCategory  ", strDurableOperationHistoryInqInParam.getDurableCategory());
        log.info("", "in-parm durableID        ", strDurableOperationHistoryInqInParam.getDurableID().getValue());
        log.info("", "in-parm operationID      ", strDurableOperationHistoryInqInParam.getOperationID().getValue());
        log.info("", "in-parm operationNumber  ", strDurableOperationHistoryInqInParam.getOperationNumber());
        log.info("", "in-parm operationPass    ", strDurableOperationHistoryInqInParam.getOperationPass());
        log.info("", "in-parm operationCategory", strDurableOperationHistoryInqInParam.getOperationCategory());
        log.info("", "in-parm pinPointFlag     ", strDurableOperationHistoryInqInParam.getPinPointFlag());

        //------------------------------------------
        //  Call durableOperationHistory_FillInODRBQ020DR
        //------------------------------------------
        List<Infos.DurableOperationHisInfo> strDurableOperationHistoryFillInODRBQ020DROut;
        Infos.DurableOperationHistoryFillInODRBQ020DRIn strDurableOperationHistoryFillInODRBQ020DRIn = new Infos.DurableOperationHistoryFillInODRBQ020DRIn();
        strDurableOperationHistoryFillInODRBQ020DRIn.setDurableCategory(strDurableOperationHistoryInqInParam.getDurableCategory());
        strDurableOperationHistoryFillInODRBQ020DRIn.setDurableID(strDurableOperationHistoryInqInParam.getDurableID());
        strDurableOperationHistoryFillInODRBQ020DRIn.setRouteID(strDurableOperationHistoryInqInParam.getRouteID());
        strDurableOperationHistoryFillInODRBQ020DRIn.setOperationID(strDurableOperationHistoryInqInParam.getOperationID());
        strDurableOperationHistoryFillInODRBQ020DRIn.setOperationNumber(strDurableOperationHistoryInqInParam.getOperationNumber());
        strDurableOperationHistoryFillInODRBQ020DRIn.setOperationPass(strDurableOperationHistoryInqInParam.getOperationPass());
        strDurableOperationHistoryFillInODRBQ020DRIn.setOperationCategory(strDurableOperationHistoryInqInParam.getOperationCategory());
        strDurableOperationHistoryFillInODRBQ020DRIn.setPinPointFlag(strDurableOperationHistoryInqInParam.getPinPointFlag());
        strDurableOperationHistoryFillInODRBQ020DROut = durableMethod.durableOperationHistoryFillInODRBQ020DR(
                strObjCommonIn,
                strDurableOperationHistoryFillInODRBQ020DRIn);

        //---------------------------------------
        // Return
        //---------------------------------------
        List<Infos.DurableOperationHisInfo> strDurableOperationHistoryInqResult = strDurableOperationHistoryFillInODRBQ020DROut;
        log.info("PPTManager_i:: txDurableOperationHistoryInq",
                "strDurableOperationHistoryInqResult.strDurableOperationHisInfo.length() = {}",
                CimArrayUtils.getSize(strDurableOperationHistoryInqResult));
        log.info("PPTManager_i:: txDurableOperationHistoryInq");
        return (strDurableOperationHistoryInqResult);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationListFromHistoryInqInParam
     * @return com.fa.cim.dto.Results.DurableOperationListFromHistoryInqResult
     * @throws
     * @author ho
     * @date 2020/6/22 11:08
     */
    @Override
    public Results.DurableOperationListFromHistoryInqResult sxDrbStepListFromHistoryInq(Infos.ObjCommon strObjCommonIn, Params.DurableOperationListFromHistoryInqInParam strDurableOperationListFromHistoryInqInParam) {
        log.info("PPTManager_i:: txDurableOperationListFromHistoryInq ");

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        log.info("", "in-parm durableCategory  ", strDurableOperationListFromHistoryInqInParam.getDurableCategory());
        log.info("", "in-parm durableID        ", ObjectIdentifier.fetchValue(strDurableOperationListFromHistoryInqInParam.getDurableID()));
        log.info("", "in-parm searchCount      ", strDurableOperationListFromHistoryInqInParam.getSearchCount());

        Results.DurableOperationListFromHistoryInqResult strDurableOperationListFromHistoryInqResult = new Results.DurableOperationListFromHistoryInqResult();

        strDurableOperationListFromHistoryInqResult.setDurableCategory(strDurableOperationListFromHistoryInqInParam.getDurableCategory());
        strDurableOperationListFromHistoryInqResult.setDurableID(strDurableOperationListFromHistoryInqInParam.getDurableID());

        //------------------------------------------
        //  Call process_operationListForDurableFromHistoryDR
        //------------------------------------------
        List<Infos.OperationNameAttributesFromHistory> strProcessOperationListForDurableFromHistoryDROut;
        Infos.ProcessOperationListForDurableFromHistoryDRIn strProcessOperationListForDurableFromHistoryDRIn = new Infos.ProcessOperationListForDurableFromHistoryDRIn();
        strProcessOperationListForDurableFromHistoryDRIn.setDurableCategory(strDurableOperationListFromHistoryInqInParam.getDurableCategory());
        strProcessOperationListForDurableFromHistoryDRIn.setDurableID(strDurableOperationListFromHistoryInqInParam.getDurableID());
        strProcessOperationListForDurableFromHistoryDRIn.setSearchCount(strDurableOperationListFromHistoryInqInParam.getSearchCount());
        strProcessOperationListForDurableFromHistoryDROut = processForDurableMethod.processOperationListForDurableFromHistoryDR(
                strObjCommonIn,
                strProcessOperationListForDurableFromHistoryDRIn);

        //---------------------------------------
        // Return
        //---------------------------------------
        strDurableOperationListFromHistoryInqResult.setStrOperationNameAttributes(strProcessOperationListForDurableFromHistoryDROut);
        log.info("PPTManager_i:: txDurableOperationListFromHistoryInq",
                "strDurableOperationListFromHistoryInqResult.strOperationNameAttributes.length() = {}",
                CimArrayUtils.getSize(strDurableOperationListFromHistoryInqResult.getStrOperationNameAttributes()));
        log.info("PPTManager_i:: txDurableOperationListFromHistoryInq");
        return strDurableOperationListFromHistoryInqResult;
    }

    @Override
    public Results.DurableOperationListInqResult sxDrbStepListInq(Infos.ObjCommon objCommon, Params.DurableOperationListInqInParam durableOperationListInqInParam) {
        User user = durableOperationListInqInParam.getUser();
        String durableCategory = durableOperationListInqInParam.getDurableCategory();
        ObjectIdentifier durableID = durableOperationListInqInParam.getDurableID();
        long searchCount = durableOperationListInqInParam.getSearchCount();
        boolean currentFlag = durableOperationListInqInParam.isCurrentFlag();
        boolean posSearchFlag = durableOperationListInqInParam.isPosSearchFlag();
        boolean searchDirection = durableOperationListInqInParam.isSearchDirection();

        Params.ProcessOperationListForDurableDRInParam param = new Params.ProcessOperationListForDurableDRInParam();
        param.setCurrentFlag(currentFlag);
        param.setDurableCategory(durableCategory);
        param.setDurableID(durableID);
        param.setPosSearchFlag(posSearchFlag);
        param.setSearchCount(searchCount);
        param.setSearchDirection(searchDirection);
        param.setSearchOperationNumber("");
        param.setUser(user);
        param.setSearchRouteID(new ObjectIdentifier());

        //------------------------------------------
        //  Call process_OperationListForDurableDR
        //------------------------------------------
        Results.DurableOperationListInqResult durableOperationListInqResult = new Results.DurableOperationListInqResult();
        try {
            durableOperationListInqResult = processForDurableMethod.processOperationListForDurableDR(objCommon, param);
        } catch (ServiceException e) {
            if (Validations.isEquals(e.getCode(), retCodeConfig.getSomeopelistDataError())
                    && Validations.isEquals(e.getCode(), retCodeConfig.getInvalidLcData())
                    && Validations.isEquals(e.getCode(), retCodeConfig.getFutureholdReservedUntilJoinpoint())
                    && Validations.isEquals(e.getCode(), retCodeConfig.getCannotSetLcSkipToLastOpe())) {
                log.info("process_OperationListForDurableDR() == RC_SOMEOPELIST_DATA_ERROR etc");
            } else {
                throw e;
            }
        }
        //---------------------------------------
        // Return
        //---------------------------------------
        durableOperationListInqResult.setDurableID(durableID);
        durableOperationListInqResult.setDurableCategory(durableCategory);

        return durableOperationListInqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strRouteOperationListForDurableInqInParam
     * @return java.util.List<com.fa.cim.dto.Infos.OperationNameAttributes>
     * @throws
     * @author ho
     * @date 2020/7/10 16:16
     */
    @Override
    public List<Infos.OperationNameAttributes> sxProcessFlowOperationListForDrbInq(Infos.ObjCommon strObjCommonIn, Params.RouteOperationListForDurableInqInParam strRouteOperationListForDurableInqInParam) {
        log.info("PPTManager_i::txRouteOperationListForDurableInq ");
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Params.RouteOperationListForDurableInqInParam strInParm = strRouteOperationListForDurableInqInParam;

        //----------------------------------------------------------------
        //  Get Durable State
        //----------------------------------------------------------------
        String strDurablestateGetout;
        strDurablestateGetout = durableMethod.durableStateGet(strObjCommonIn, strInParm.getDurableCategory(),
                strInParm.getDurableID());

        //----------------------------------------------------------------
        //  Get RouteID of Durable
        //----------------------------------------------------------------
        ObjectIdentifier strDurablerouteIDGetout;
        Infos.DurableRouteIDGetIn strDurablerouteIDGein = new Infos.DurableRouteIDGetIn();
        strDurablerouteIDGein.setDurableCategory(strInParm.getDurableCategory());
        strDurablerouteIDGein.setDurableID(strInParm.getDurableID());
        strDurablerouteIDGetout = durableMethod.durableRouteIDGet(strObjCommonIn, strDurablerouteIDGein);

        //----------------------------------------------------------------
        //  Get backward operation info of Durable
        //----------------------------------------------------------------
        Results.DurableOperationListInqResult strDurableOperationListInqResult_Backward;
        Params.DurableOperationListInqInParam strDurableOperationListInqInParam_Backward = new Params.DurableOperationListInqInParam();
        strDurableOperationListInqInParam_Backward.setSearchDirection(false);
        strDurableOperationListInqInParam_Backward.setPosSearchFlag(true);
        strDurableOperationListInqInParam_Backward.setSearchCount(9999);
        strDurableOperationListInqInParam_Backward.setCurrentFlag(false);
        strDurableOperationListInqInParam_Backward.setDurableCategory(strInParm.getDurableCategory());
        strDurableOperationListInqInParam_Backward.setDurableID(strInParm.getDurableID());
        strDurableOperationListInqResult_Backward = sxDrbStepListInq(strObjCommonIn, strDurableOperationListInqInParam_Backward);

        //----------------------------------------------------------------
        //  Get forward operation info of Durable
        //----------------------------------------------------------------
        Results.DurableOperationListInqResult strDurableOperationListInqResult_Forward;
        Params.DurableOperationListInqInParam strDurableOperationListInqInParam_Forward = new Params.DurableOperationListInqInParam();
        strDurableOperationListInqInParam_Forward.setSearchDirection(true);
        strDurableOperationListInqInParam_Forward.setPosSearchFlag(true);
        strDurableOperationListInqInParam_Forward.setSearchCount(9999);
        strDurableOperationListInqInParam_Forward.setCurrentFlag(true);
        strDurableOperationListInqInParam_Forward.setDurableCategory(strInParm.getDurableCategory());
        strDurableOperationListInqInParam_Forward.setDurableID(strInParm.getDurableID());
        strDurableOperationListInqResult_Forward = sxDrbStepListInq(strObjCommonIn, strDurableOperationListInqInParam_Forward);

        /*-----------------------*/
        /*   Set Output Struct   */
        /*-----------------------*/
        log.info("{}", "Set Output Struct");

        int lenBackwardOpeCnt = CimArrayUtils.getSize(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes());
        int lenForwardOpeCnt = CimArrayUtils.getSize(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes());
        log.info("{} {}", "lenBackwardOpeCnt", lenBackwardOpeCnt);
        log.info("{} {}", "lenForwardOpeCnt ", lenForwardOpeCnt);

        List<Infos.OperationNameAttributes> strRouteOperationListForDurableInqResult = new ArrayList<>(lenBackwardOpeCnt + lenForwardOpeCnt);

        int nSetIdx = 0;
        int nOpe = 0;
        log.info("{}", "Set Backward Info");
        for (nOpe = lenBackwardOpeCnt - 1; nOpe >= 0; nOpe--) {
            log.info("{} {}", "--------------------------------------------", nOpe);
            log.info("{} {}", "routeID..........", ObjectIdentifier.fetchValue(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getRouteID()));
            log.info("{} {}", "operationID......", ObjectIdentifier.fetchValue(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getOperationID()));
            log.info("{} {}", "operationNumber..", strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getOperationNumber());
            log.info("{} {}", "operationName....", strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getOperationName());

            if (ObjectIdentifier.equalsWithValue(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getRouteID(),
                    strDurablerouteIDGetout)) {
                log.info("{}", "strDurableOperationListInqResult_Backward.strDurableOperationNameAttributes[nOpe].routeID is same as strDurablerouteIDGetout.routeID");
                strRouteOperationListForDurableInqResult.add(new Infos.OperationNameAttributes());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setSequenceNumber(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getSeqno());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setRouteID(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getRouteID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationID(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getOperationID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationNumber(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getOperationNumber());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationName(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getOperationName());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationPass(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getOperationPass());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setObjrefPO(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getObjrefPO());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setProcessRef(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getProcessRef());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setTestType(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getTestType());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setInspectionType(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getInspectionType());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setStageID(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getStageID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setStageGroupID(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getStageGroupID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setMaskLevel(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getMaskLevel());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setDepartmentNumber(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getDepartmentNumber());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setMandatoryOperationFlag(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getMandatoryOperationFlag());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setStandardCycleTime(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getStandardCycleTime());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setPlannedStartTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getPlannedStartTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setPlannedEndTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getPlannedEndTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setPlannedMachine(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getPlannedMachine());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setActualStartTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getActualStartTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setActualCompTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getActualCompTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setAssignedMachine(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getAssignedMachine());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setMachineList(strDurableOperationListInqResult_Backward.getStrDurableOperationNameAttributes().get(nOpe).getMachines());

                nSetIdx++;
                log.info("{} {}", "@@@ Add @@@", nSetIdx);
            }
        }

        log.info("{}", "Set Forward Info");
        for (nOpe = 0; nOpe < lenForwardOpeCnt; nOpe++) {
            log.info("{} {}", "--------------------------------------------", nOpe);
            log.info("{} {}", "routeID..........", ObjectIdentifier.fetchValue(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getRouteID()));
            log.info("{} {}", "operationID......", ObjectIdentifier.fetchValue(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getOperationID()));
            log.info("{} {}", "operationNumber..", strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getOperationNumber());
            log.info("{} {}", "operationName....", strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getOperationName());

            if (ObjectIdentifier.equalsWithValue(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getRouteID(),
                    strDurablerouteIDGetout)) {
                log.info("{}", "strDurableOperationListInqResult_Forward.strDurableOperationNameAttributes[nOpe].routeID is same as strDurablerouteIDGetout.routeID");
                strRouteOperationListForDurableInqResult.add(new Infos.OperationNameAttributes());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setSequenceNumber(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getSeqno());

                strRouteOperationListForDurableInqResult.get(nSetIdx).setRouteID(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getRouteID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationID(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getOperationID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationNumber(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getOperationNumber());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationName(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getOperationName());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setOperationPass(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getOperationPass());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setObjrefPO(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getObjrefPO());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setProcessRef(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getProcessRef());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setTestType(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getTestType());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setInspectionType(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getInspectionType());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setStageID(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getStageID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setStageGroupID(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getStageGroupID());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setMaskLevel(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getMaskLevel());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setDepartmentNumber(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getDepartmentNumber());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setMandatoryOperationFlag(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getMandatoryOperationFlag());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setStandardCycleTime(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getStandardCycleTime());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setPlannedStartTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getPlannedStartTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setPlannedEndTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getPlannedEndTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setPlannedMachine(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getPlannedMachine());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setActualStartTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getActualStartTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setActualCompTime(CimDateUtils.convertTo(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getActualCompTime()));
                strRouteOperationListForDurableInqResult.get(nSetIdx).setAssignedMachine(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getAssignedMachine());
                strRouteOperationListForDurableInqResult.get(nSetIdx).setMachineList(strDurableOperationListInqResult_Forward.getStrDurableOperationNameAttributes().get(nOpe).getMachines());

                nSetIdx++;
                log.info("{} {}", "@@@ Add @@@", nSetIdx);
            }
        }

        log.info("{} {}", "nSetIdx", nSetIdx);

        return strRouteOperationListForDurableInqResult;
    }

    /**
     * This function notifies system that Durable is actual Operation has started on the Equipment.
     * <p> - Input parameter check( durableControlJobID is not empty )
     * <p> - Consistency check of Transaction ID and SpecialControl of Equipment Category
     * <p> - Check condition of durable( PFX is not Nil, Cassette's durableControlJob exists in Eqp, Transfer Status... )
     * <p> - Check status of durable( Should be NotAvailable )
     * <p> - Check equipment and port state for cassette and reticle pod( Input EqpID and durableControlJob's EqpID should match... )
     * <p> - Check cassette category
     * <p> - Change equipment status if needed
     * <p> - Change cassette dispatch reserved status
     * <p> - Make event
     * <p> - Send durable operation start notification request to TCS
     * <p> - Update durable controlJob status to Queued
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/18 17:02
     */
    @Override
    public Results.DurableOperationStartReqResult sxDrbMoveInReq(Infos.ObjCommon objCommon, Params.DurableOperationStartReqInParam param) {
        Validations.check(null == objCommon || null == param, retCodeConfig.getInvalidInputParam());

        Results.DurableOperationStartReqResult retVal = new Results.DurableOperationStartReqResult();

        String durableCategory = param.getDurableCategory();
        Infos.DurableStartRecipe durableStartRecipe = param.getStrDurableStartRecipe();
        List<Infos.StartDurable> startDurables = param.getStrStartDurables();
        String claimMemo = param.getClaimMemo();
        log.info("in-parm equipmentID          :" + ObjectIdentifier.fetchValue(param.getEquipmentID()));
        log.info("in-parm durableControlJobID  :" + ObjectIdentifier.fetchValue(param.getDurableControlJobID()));
        log.info("in-parm durableCategory      :" + durableCategory);

        if (ObjectIdentifier.isEmpty(param.getDurableControlJobID())) {
            Validations.check(retCodeConfig.getDurableControlJobBlank());
        }

        if (!CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            Validations.check(retCodeConfig.getInvalidDurableCategory());
        }

        if (CimArrayUtils.isEmpty(startDurables)) {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        //---------------------------------------------------------------------------------
        // Consistency check of Transaction ID and SpecialControl of Equipment Category
        //---------------------------------------------------------------------------------
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(objCommon, param.getEquipmentID());

        // Get required equipment lock mode
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(param.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        assert null != objLockModeOut;

        //  Lock EQP
        long lockMode = objLockModeOut.getLockMode();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(param.getEquipmentID());
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info("lockMode == SP_EQP_LOCK_MODE_WRITE");
            /*--------------------------------------------*/
            /*      Machine Object Lock Process           */
            /*--------------------------------------------*/
            lockMethod.objectLock(objCommon, CimMachine.class, param.getEquipmentID());
        }

        // Lock Carrier
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            if (CimStringUtils.equals(param.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
                List<String> keys = new ArrayList<>();
                Optional.of(param).flatMap(data ->
                        Optional.ofNullable(data.getStrStartDurables())).ifPresent(list ->
                        list.forEach(durable -> keys.add(durable.getDurableId().getValue())));
                // Lock Cassette Element (Read)
                Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                objAdvanceLockIn.setObjectID(param.getEquipmentID());
                objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                objAdvanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ));
                objAdvanceLockIn.setKeyList(keys);
                lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
            }
        }

        // Lock Durable Control Job
        lockMethod.objectLock(objCommon, CimDurableControlJob.class, param.getDurableControlJobID());

        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        Optional.of(param).flatMap(data ->
                Optional.ofNullable(data.getStrStartDurables())).ifPresent(list ->
                list.forEach(durable -> durableIDs.add(durable.getDurableId())));
        switch (durableCategory) {
            case BizConstant.SP_DURABLECAT_CASSETTE:
                lockMethod.objectSequenceLock(objCommon, CimCassette.class, durableIDs);
                break;
            case BizConstant.SP_DURABLECAT_RETICLEPOD:
                lockMethod.objectSequenceLock(objCommon, CimReticlePod.class, durableIDs);
                break;
            case BizConstant.SP_DURABLECAT_RETICLE:
                lockMethod.objectSequenceLock(objCommon, CimProcessDurable.class, durableIDs);
                break;
            default:
                // do nothing.
                break;
        }

        //-------------------------------------------
        // Durable Process Flow Control
        //-------------------------------------------
        boolean onRouteFlag = true;
        boolean tmpOnRouteFlag = true;

        //---------------------------------------
        //  Check Durable OnRoute
        //---------------------------------------
        int loop = 0;
        for (Infos.StartDurable startDurable : startDurables) {
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
            } catch (ServiceException e) {
                if (loop == 0) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute())) {
                        onRouteFlag = false;
                    }
                } else {
                    tmpOnRouteFlag = Validations.isEquals(e.getCode(), retCodeConfig.getDurableOnroute());
                    if (tmpOnRouteFlag != onRouteFlag) {
                        log.error("all durable OnRoute state is not same");
                        Validations.check(retCodeConfigEx.getDurableOnRouteStatNotSame());
                    }
                }
            }
            loop++;
        }

        if (onRouteFlag) {
            // todo:ZQI
            // txRunDurableBRScriptReq
        }

        String portGroupID = null;
        String onlineMode;
        ObjectIdentifier operationMode = null;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            ObjectIdentifier loadPortID = startDurables.get(0).getStartDurablePort().getLoadPortID();
            Inputs.ObjEquipmentPortGroupIDGetIn objEquipmentPortGroupIDGetIn = new Inputs.ObjEquipmentPortGroupIDGetIn();
            objEquipmentPortGroupIDGetIn.setEquipmentId(param.getEquipmentID());
            objEquipmentPortGroupIDGetIn.setPortId(loadPortID);
            Outputs.ObjEquipmentPortGroupIDGetOut groupIDGetOut = portMethod.equipmentPortGroupIDGet(objCommon, objEquipmentPortGroupIDGetIn);
            portGroupID = groupIDGetOut.getPortGroupId();

            /*---------------------------------*/
            /*   Get Equipment's Online Mode   */
            /*---------------------------------*/
            Outputs.ObjPortResourceCurrentOperationModeGetOut operationModeGetOut = portMethod.portResourceCurrentOperationModeGet(objCommon, param.getEquipmentID(), loadPortID);
            onlineMode = operationModeGetOut.getOperationMode().getOnlineMode();
            operationMode = operationModeGetOut.getOperationMode().getOperationMode();
        } else {
            log.info("durableCategory != SP_DurableCat_Cassette");
            onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, param.getEquipmentID());
        }

        //-----------------------------------------
        // Call durable_CheckConditionForOperation
        //-----------------------------------------
        Inputs.ObjDurableCheckConditionForOperationIn conditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
        conditionForOperationIn.setOperation(BizConstant.SP_OPERATION_OPESTART);
        conditionForOperationIn.setEquipmentId(param.getEquipmentID());
        conditionForOperationIn.setDurableCategory(durableCategory);
        conditionForOperationIn.setStartDurables(startDurables);
        conditionForOperationIn.setDurableStartRecipe(durableStartRecipe);
        durableMethod.durableCheckConditionForOperation(objCommon, conditionForOperationIn);

        for (Infos.StartDurable startDurable : startDurables) {
            // durable_status_CheckForOperation
            durableMethod.durableStatusCheckForOperation(objCommon, BizConstant.SP_OPERATION_OPESTART, startDurable.getDurableId(), durableCategory);
        }

        //----------------------------------------------------------
        // call equipment_and_portState_CheckForDurableOperation
        //----------------------------------------------------------
        Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn checkForDurableOperationIn = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
        checkForDurableOperationIn.setOperation(BizConstant.SP_OPERATION_OPESTART);
        checkForDurableOperationIn.setEquipmentId(param.getEquipmentID());
        checkForDurableOperationIn.setPortGroupId(portGroupID);
        checkForDurableOperationIn.setDurableCategory(durableCategory);
        checkForDurableOperationIn.setStartDurables(startDurables);
        equipmentMethod.equipmentAndPortStateCheckForDurableOperation(objCommon, checkForDurableOperationIn);

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            for (Infos.StartDurable startDurable : startDurables) {
                durableMethod.durableCassetteCategoryCheckForContaminationControl(objCommon, startDurable.getDurableId(), param.getEquipmentID(), startDurable.getStartDurablePort().getLoadPortID());
            }
        }

        //------------------------------------------------------------
        // call process_startDurablesReserveInformation_Set
        //------------------------------------------------------------
        Inputs.ProcessStartDurablesReserveInformationSetIn reserveInformationSetIn = new Inputs.ProcessStartDurablesReserveInformationSetIn();
        reserveInformationSetIn.setEquipmentID(param.getEquipmentID());
        reserveInformationSetIn.setPortGroupID(portGroupID);
        reserveInformationSetIn.setDurableControlJobID(param.getDurableControlJobID());
        reserveInformationSetIn.setDurableCategory(durableCategory);
        reserveInformationSetIn.setStrStartDurables(startDurables);
        reserveInformationSetIn.setStrDurableStartRecipe(durableStartRecipe);
        processForDurableMethod.processStartDurablesReserveInformationSet(objCommon, reserveInformationSetIn);


        /*-----------------------------------------------*/
        /*   Change Equipment's Status to 'PRODUCTIVE'   */
        /*-----------------------------------------------*/
        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
            log.info("onlineMode == SP_Eqp_OnlineMode_Offline");

            /*===== get StateChageableFlag ===*/
            Boolean manufacturingStateChangeableFlag = equipmentMethod.equipmentCurrentStateCheckToManufacturing(objCommon, param.getEquipmentID());

            if (manufacturingStateChangeableFlag) {
                log.info("ManufacturingStateChangeableFlag == TRUE");
                /*===== get Defaclt Status Code for Productive / Standby ===*/
                // call equipment_recoverState_GetManufacturingForDurable
                ObjectIdentifier equipmentStatusCode = equipmentForDurableMethod.equipmentRecoverStateGetManufacturingForDurable(objCommon,
                        BizConstant.SP_OPERATION_OPESTART,
                        param.getEquipmentID(),
                        param.getDurableControlJobID());

                /*---------------------------------*/
                /*   Call txEqpStatusChangeReq()   */
                /*---------------------------------*/
                try {
                    equipmentService.sxEqpStatusChangeReq(objCommon, param.getEquipmentID(), equipmentStatusCode, claimMemo);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getCurrentStateSame())) {
                        throw e;
                    }
                }
            }
        }

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory == SP_DurableCat_Cassette");
            Optional.of(startDurables).ifPresent(list -> list.forEach(data -> cassetteMethod.cassetteDispatchStateChange(objCommon, data.getDurableId(), false)));
        }

        if (onRouteFlag) {
            durableMethod.durableProcessStateMakeProcessing(objCommon, durableCategory, startDurables);
        }

        //---------------------------
        // Make Event
        //---------------------------
        Infos.DurableOperationStartEventMakeOpeStartIn eventMakeOpeStartIn = new Infos.DurableOperationStartEventMakeOpeStartIn();
        eventMakeOpeStartIn.setEquipmentID(param.getEquipmentID());
        eventMakeOpeStartIn.setOperationMode(ObjectIdentifier.fetchValue(operationMode));
        eventMakeOpeStartIn.setDurableControlJobID(param.getDurableControlJobID());
        eventMakeOpeStartIn.setDurableCategory(durableCategory);
        eventMakeOpeStartIn.setStrStartDurables(startDurables);
        eventMakeOpeStartIn.setStrDurableStartRecipe(durableStartRecipe);
        eventMakeOpeStartIn.setClaimMemo(claimMemo);
        eventMethod.durableOperationStartEventMakeOpeStart(objCommon, eventMakeOpeStartIn);

        /*-----------------------------------------------*/
        /*   Send DurableOpeStartReq() to TCS            */
        /*-----------------------------------------------*/
//        Inputs.SendDurableOpeStartReqIn durableOpeStartReqIn = new Inputs.SendDurableOpeStartReqIn();
//        durableOpeStartReqIn.setStrDurableOperationStartReqInParam(param);
//        tcsMethod.sendTCSReq(TCSReqEnum.sendDurableMoveInReq, durableOpeStartReqIn);

        /*----------------------------------------------------*/
        /*    Durable ControlJob Manage TYPE_QUEUE            */
        /*----------------------------------------------------*/
        Params.DurableControlJobManageReqInParam controlJobManageReqInParam = new Params.DurableControlJobManageReqInParam();
        controlJobManageReqInParam.setDurableControlJobID(param.getDurableControlJobID());
        controlJobManageReqInParam.setControlJobAction(BizConstant.SP_DURABLECONTROLJOBACTION_TYPE_QUEUE);

        Infos.DurableControlJobCreateRequest durableControlJobCreateRequest = new Infos.DurableControlJobCreateRequest();
        durableControlJobCreateRequest.setEquipmentID(param.getEquipmentID());
        durableControlJobCreateRequest.setDurableCategory(param.getDurableCategory());
        durableControlJobCreateRequest.setStrStartDurables(startDurables);
        controlJobManageReqInParam.setStrDurableControlJobCreateRequest(durableControlJobCreateRequest);
        controlJobManageReqInParam.setClaimMemo(param.getClaimMemo());
        controlJobManageReqInParam.setUser(param.getUser());
        durableService.sxDrbCJStatusChangeReq(objCommon, controlJobManageReqInParam);

        /*-----------------------------------------------*/
        /*    Set Return Structure                       */
        /*-----------------------------------------------*/
        retVal.setDurableControlJobID(param.getDurableControlJobID());
        retVal.setDurableCategory(durableCategory);
        retVal.setStrStartDurables(startDurables);
        retVal.setStrDurableStartRecipe(durableStartRecipe);
        return retVal;
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<List < ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */
    @Override
    public List<ObjectIdentifier> sxAllReticleGroupListInq(Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxAllReticleGroupListInq()");
        return durableMethod.durableCapabilityIDGetDR(objCommon, BizConstant.SP_DURABLECAT_RETICLE);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return RetCode<List < ObjectIdentifier>>
     * @author Sun
     * @date 2018/10/16
     */

    public List<ObjectIdentifier> sxAllReticleListInq(Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxAllReticleListInq()");
        return durableMethod.durableIDGetDR(objCommon, BizConstant.SP_DURABLECAT_RETICLE);
    }

    @Override
    public List<Infos.AvailableCarrierOut> sxAvailableCarrierListForLotStartInq(Infos.ObjCommon objCommon) {
        /*------------------------*/
        /*   Get Empty Cassette   */
        /*------------------------*/
        List<Infos.AvailableCarrierOut> val = new ArrayList<>();
        List<Infos.FoundCassette> strFoundCassette = new ArrayList<>();
        Map<String, Boolean> availableForDurableFlagList = new HashMap<>();

        String sql = " select * from (SELECT * FROM OMCARRIER WHERE DRBL_STATE = 'AVAILABLE' AND XFER_STATE = 'MI' AND CARRIER_USED_CAP = 0  order by XFER_STATE desc ) where rownum<=10";
        List<CimCassetteDO> queriedCasts = cimJpaRepository.query(sql, CimCassetteDO.class);

        if (CimArrayUtils.isNotEmpty(queriedCasts)) {
            for (CimCassetteDO cassette : queriedCasts) {

                List<Info.SortJobListAttributes> sortJobListAttributesList = null;
                com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
                objSorterJobListGetDRIn.setCarrierID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
                try {
                    sortJobListAttributesList = sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
                } catch (ServiceException e) {
                    break;
                }

                int sorterLen = CimArrayUtils.getSize(sortJobListAttributesList);
                if (sorterLen > 0) {
                    continue;
                }
                List<Infos.HashedInfo> hashedInfoList = new ArrayList<>();

                Infos.FoundCassette foundCassette = new Infos.FoundCassette();
                //--------------------------------------
                // Set InPostProcessFlag of Cassette
                //--------------------------------------
                foundCassette.setSorterJobExistFlag(sorterLen > 0);
                foundCassette.setCassetteID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
                foundCassette.setInPostProcessFlagOfCassette(cassette.getPostProcessingFlag());
                foundCassette.setDescription(cassette.getDescription());
                foundCassette.setCassetteCategory(cassette.getCassetteCategory());
                long cassetteUsedCapacity = null == cassette.getCastUsedCapacity() ? 0L : cassette.getCastUsedCapacity();
                foundCassette.setEmptyFlag(cassetteUsedCapacity == 0);
                foundCassette.setCassetteStatus(cassette.getDurableState());
                foundCassette.setTransferStatus(cassette.getTransferState());
                String zoneTypeNeedFlg = StandardProperties.OM_CARRIER_LIST_NEED_ZONE_TYPE.getValue();
                if (CimStringUtils.equals(zoneTypeNeedFlg, "1") && CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.CASSETTE_LIST_INQ.getValue())) {
                    Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOut = null;
                    try {
                        cassetteZoneTypeGetOut = cassetteMethod.cassetteZoneTypeGet(objCommon, foundCassette.getCassetteID());
                    } catch (ServiceException e) {
                        break;
                    }
                    foundCassette.setZoneType(cassetteZoneTypeGetOut == null ? null : cassetteZoneTypeGetOut.getZoneType());
                }
                foundCassette.setMultiLotType(cassette.getMultiLotType());

                String transferState = cassette.getTransferState();
                if (BizConstant.equalsIgnoreCase(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)
                        || BizConstant.equalsIgnoreCase(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT, transferState)) {
                    foundCassette.setEquipmentID(new ObjectIdentifier(cassette.getEquipmentID(), cassette.getEquipmentObj()));
                } else {
                    foundCassette.setStockerID(new ObjectIdentifier(cassette.getEquipmentID(), cassette.getEquipmentObj()));
                }
                foundCassette.setUsageCheckFlag(cassette.getUsageCheckReq());

                double cassetteDurationLimit = (null == cassette.getDurationLimit()
                        ? 0 : (cassette.getDurationLimit() < 0 ? 0 : cassette.getDurationLimit()));
                final int minutesToMilliSec = 60 * 1000; // millisec -> minutes
                foundCassette.setMaximumRunTime(String.valueOf(cassetteDurationLimit / minutesToMilliSec));     // milisec -> minutes
                foundCassette.setMaximumOperationStartCount(cassette.getTimesUsedLimit());
                foundCassette.setIntervalBetweenPM(cassette.getIntervalBetweenPM());
                foundCassette.setCapacity(cassette.getCassetteCapacity());
                foundCassette.setNominalSize(cassette.getWaferSize());
                foundCassette.setContents(cassette.getMaterialContents());
                foundCassette.setInstanceName(cassette.getInstanceName());
                foundCassette.setCurrentLocationFlag(cassette.getCurrentLocationFlag());
                foundCassette.setBackupState(cassette.getBackupState());
                foundCassette.setSlmReservedEquipmentID(new ObjectIdentifier(cassette.getSlmReservedEquipmentID(), cassette.getSlmReservedEquipmentObj()));
                foundCassette.setInterFabTransferState(cassette.getInterFabTransferState());
                foundCassette.setDurableControlJobID(new ObjectIdentifier(cassette.getDurableControlJobID(), cassette.getDurableControlJobObj()));
                boolean durableSTBFlag = !CimStringUtils.isEmpty(cassette.getDurableProcessFlowContextObj());
                foundCassette.setDurablesSTBFlag(durableSTBFlag);
                foundCassette.setDurableSubStatus(new ObjectIdentifier(cassette.getDurableSubStateID(), cassette.getDurableSubStateObj()));
                foundCassette.setProductUsage(cassette.getProductUsage());
                foundCassette.setCarrierType(cassette.getCarrierType());
                //--- Set availableForDurableFlag; ------------------------------------//
                boolean flag = false;
                if (!CimStringUtils.isEmpty(cassette.getDurableSubStateID())) {
                    if (!availableForDurableFlagList.containsKey(cassette.getDurableSubStateID())) {
                        CimDurableSubState aDurableSubState = baseCoreFactory.getBO(CimDurableSubState.class, cassette.getDurableSubStateID());
                        if (null != aDurableSubState) {
                            flag = aDurableSubState.isDurableProcessAvailable();
                            availableForDurableFlagList.put(cassette.getDurableSubStateID(), flag);
                        }
                    } else {
                        flag = availableForDurableFlagList.get(cassette.getDurableSubStateID());
                    }
                }
                foundCassette.setAvailableForDurableFlag(flag);
                //--- Set pptHashedInfoSequence strDurableStatusList; ----------------------//
                String durableFlowState = "";
                if (CIMStateConst.equals(CIMStateConst.CIM_DURABLE_SCRAPPED, cassette.getDurableState())) {
                    durableFlowState = cassette.getDurableState();
                } else if (CimStringUtils.equals(BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD, cassette.getDurableHoldState())) {
                    durableFlowState = cassette.getDurableHoldState();
                } else if (CimStringUtils.equals(BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED, cassette.getDurableFinishedState())) {
                    durableFlowState = cassette.getDurableFinishedState();
                } else {
                    durableFlowState = cassette.getDurableProcessState();
                }

                Infos.HashedInfo hashedInfo0 = new Infos.HashedInfo();
                //DurableStatusList[0]
                hashedInfo0.setHashKey("durable Flow State");
                hashedInfo0.setHashData(durableFlowState);
                hashedInfoList.add(hashedInfo0);

                String durableState = "";
                if (!CimStringUtils.isEmpty(cassette.getRouteID())) {
                    if (CIMStateConst.equals(CIMStateConst.CIM_DURABLE_SCRAPPED, cassette.getDurableState())
                            || BizConstant.equalsIgnoreCase(BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED, cassette.getDurableFinishedState())) {
                        durableState = BizConstant.SP_DURABLE_ONROUTESTATE_FINISHED;
                    } else {
                        durableState = BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE;
                    }
                }
                Infos.HashedInfo hashedInfo1 = new Infos.HashedInfo();
                //DurableStatusList[1]
                hashedInfo1.setHashKey("durable State");
                hashedInfo1.setHashData(durableState);
                hashedInfoList.add(hashedInfo1);

                //DurableStatusList[2]
                Infos.HashedInfo hashedInfo2 = new Infos.HashedInfo();
                hashedInfo2.setHashKey("durable Production State");
                hashedInfo2.setHashData(cassette.getDurableProductionState());
                hashedInfoList.add(hashedInfo2);

                //DurableStatusList[3]
                Infos.HashedInfo hashedInfo3 = new Infos.HashedInfo();
                hashedInfo3.setHashKey("durable Hold State");
                hashedInfo3.setHashData(cassette.getDurableHoldState());
                hashedInfoList.add(hashedInfo3);

                //DurableStatusList[4]
                Infos.HashedInfo hashedInfo4 = new Infos.HashedInfo();
                hashedInfo4.setHashKey("durable Finished State");
                hashedInfo4.setHashData(cassette.getDurableFinishedState());
                hashedInfoList.add(hashedInfo4);

                //DurableStatusList[5]
                Infos.HashedInfo hashedInfo5 = new Infos.HashedInfo();
                hashedInfo5.setHashKey("durable Process State");
                hashedInfo5.setHashData(cassette.getDurableProcessState());
                hashedInfoList.add(hashedInfo5);

                //DurableStatusList[6]
                Infos.HashedInfo hashedInfo6 = new Infos.HashedInfo();
                hashedInfo6.setHashKey("durable Inventory State");
                hashedInfo6.setHashData(cassette.getDurableInventoryState());
                hashedInfoList.add(hashedInfo6);
                foundCassette.setDurableStatusList(hashedInfoList);

                foundCassette.setBankID(new ObjectIdentifier(cassette.getBankID(), cassette.getBankObj()));
                foundCassette.setDueTimeStamp("");

                // get start bank id
                if (!CimStringUtils.isEmpty(cassette.getRouteID())) {
                    //--- Set objectIdentifier startBankID; ------------------------------------//
                    String processDefLevel = BizConstant.SP_PD_FLOWLEVEL_MAIN;
                    String sql1 = "SELECT START_BANK_ID, START_BANK_RKEY FROM OMPRP WHERE PRP_ID = ?1 AND PRP_LEVEL = ?2";
                    List<Object[]> objects = cimJpaRepository.query(sql1, cassette.getRouteID(), processDefLevel);
                    if (!CimArrayUtils.isEmpty(objects)) {
                        Object[] object = objects.get(0);
                        foundCassette.setStartBankID(new ObjectIdentifier((String) object[0], (String) object[1]));
                    }
                }

                foundCassette.setRouteID(new ObjectIdentifier(cassette.getRouteID(), cassette.getRouteObj()));
                foundCassette.setOperationNumber(cassette.getOperationNumber());
                foundCassette.setBankInRequiredFlag(cassette.getBankInRequired());

                //【step5】get durable hold record
                ObjectIdentifier durableID = foundCassette.getCassetteID();
                List<Infos.DurableHoldRecord> retCode = null;
                try {
                    retCode = durableMethod.durableHoldRecordGetDR(objCommon, durableID, BizConstant.SP_DURABLECAT_CASSETTE);
                } catch (ServiceException e) {
                    break;
                }
                //--- Set objectIdentifier holdReasonCodeID; -------------------------//
                int holdLen = CimArrayUtils.getSize(retCode);
                ObjectIdentifier holdReasonCodeID = new ObjectIdentifier();
                if (1 == holdLen) {
                    holdReasonCodeID = retCode.get(0).getHoldReasonCodeID();
                } else if (1 < holdLen) {
                    holdReasonCodeID = new ObjectIdentifier(String.format("%s*", retCode.get(0).getHoldReasonCodeID().getValue()));
                }
                foundCassette.setHoldReasonCodeID(holdReasonCodeID);

                strFoundCassette.add(foundCassette);
            }
        }

        /*-----------------------------------*/
        /*   Pick Up Target Empty Cassette   */
        /*-----------------------------------*/
        Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickupOutRetCode = cassetteMethod.cassetteListEmptyAvailablePickup(objCommon, strFoundCassette);
        if (CimArrayUtils.isNotEmpty(cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette())) {
            for (Infos.FoundCassette foundCassette : cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette()) {
                Infos.AvailableCarrierOut availableCarrierOut = new Infos.AvailableCarrierOut();
                availableCarrierOut.setFoupID(ObjectIdentifier.fetchValue(foundCassette.getCassetteID()));
                availableCarrierOut.setTransferStatus(foundCassette.getTransferStatus());
                val.add(availableCarrierOut);
            }
        }
        int n = random.nextInt(val.size());
        Infos.AvailableCarrierOut availableCarrierOut = val.get(n);
        val.clear();

        val.add(availableCarrierOut);
        return val;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/7                          Wind
     *
     * @param objCommon
     * @param params
     * @return RetCode<Results.ReticlePodDetailInfoInqResult>
     * @author Wind
     * @date 2018/11/7 18:02
     */
    @Override
    public Results.ReticlePodDetailInfoInqResult sxReticlePodDetailInfoInq(Infos.ObjCommon objCommon, Params.ReticlePodDetailInfoInqParams params) {
        log.info("Enter the sxReticlePodDetailInfoInq() !");

        Results.ReticlePodDetailInfoInqResult reticlePodDetailInfoInqResult = new Results.ReticlePodDetailInfoInqResult();


        /*================================================================================================*/
        /*   Get Reticle Pod Infomation                                                                   */
        /*================================================================================================*/

        //step1 - reticlePod_FillInTxPDQ013DR__170
        Outputs.ObjReticlePodFillInTxPDQ013DROut objReticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, params);

        reticlePodDetailInfoInqResult.setReticlePodID(objReticlePodFillInTxPDQ013DROut.getReticlePodID());
        reticlePodDetailInfoInqResult.setReticlePodBRInfo(objReticlePodFillInTxPDQ013DROut.getReticlePodBRInfo());
        reticlePodDetailInfoInqResult.setReticlePodStatusInfo(objReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo());
        reticlePodDetailInfoInqResult.setReticlePodPMInfo(objReticlePodFillInTxPDQ013DROut.getReticlePodPMInfo());
        reticlePodDetailInfoInqResult.setReticlePodLocationInfo(objReticlePodFillInTxPDQ013DROut.getReticlePodLocationInfo());
        reticlePodDetailInfoInqResult.setStrDurableOperationInfo(objReticlePodFillInTxPDQ013DROut.getStrDurableOperationInfo());
        reticlePodDetailInfoInqResult.setStrDurableWipOperationInfo(objReticlePodFillInTxPDQ013DROut.getStrDurableWipOperationInfo());

        /*================================================================================================*/
        /*   Get Reserved Reticle Infomation                                                              */
        /*================================================================================================*/

        //step2 - reticlePod_reservedReticleInfo_GetDR
        Infos.ReticlePodAdditionalAttribute reticlePodAdditionalAttribute = reticleMethod.reticlePodReservedReticleInfoGetDR(objCommon, params.getReticlePodID());
        reticlePodDetailInfoInqResult.setStrReticlePodAdditionalAttribute(reticlePodAdditionalAttribute);

        //step3 - durable_inPostProcessFlag_Get
        Boolean booleans = durableMethod.durableInPostProcessFlagGet(objCommon, CIMStateConst.SP_DURABLE_CAT_RETICLE_POD, params.getReticlePodID());

        reticlePodDetailInfoInqResult.getReticlePodStatusInfo().setInPostProcessFlagOfReticlePod(booleans);
        return reticlePodDetailInfoInqResult;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6                            Wind
     *
     * @param objCommon
     * @param params
     * @return RetCode<List < Infos.ReticlePodListInfo>>
     * @author Wind
     * @date 2018/11/6 13:10
     */
    @Override
    @Deprecated
    public List<Infos.ReticlePodListInfo> sxReticlePodListInq(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params) {
        log.info("Enter the sxReticlePodListInq() !");
        List<Infos.ReticlePodListInfo> result = reticleMethod.reticlePodFillInTxPDQ012DR(objCommon, params);
        return result;
    }
    @Override
    public Page<Infos.ReticlePodListInfo> sxPageReticlePodListInq(Infos.ObjCommon objCommon, Params.ReticlePodListInqParams params) {
        log.info("Enter the sxPageReticlePodListInq() !");
        return reticleMethod.pageReticlePodList(objCommon,params);
    }


    /**
     * description:
     * <p>This function gets the information of Reticle Pod base information with User Data.<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon        objCommon
     * @param reticlePodIDList reticlePodIDList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/10/31 11:05:53
     */
    @Override
    public List<Infos.DurableAttribute> sxReticlePodListWithBasicInfoInq(Infos.ObjCommon objCommon, List<ObjectIdentifier> reticlePodIDList) {
        List<Infos.DurableAttribute> data = new ArrayList<>();

        //Check Input parameter
        if (CimArrayUtils.isEmpty(reticlePodIDList)) {
            Validations.check(true, retCodeConfig.getInvalidParameter());
        }
        //Step1 - Get OMRTCLPOD
        for (ObjectIdentifier reticlePodID : reticlePodIDList) {
            //reticlePod_baseInfo_GetDR
            Infos.DurableAttribute durableAttribute = reticleMethod.reticlePodBaseInfoGetDr(objCommon, reticlePodID);
            // Set
            data.add(durableAttribute);
            //Get FRRTCLPOD_UDATA
            //reticlePod_userDataInfo_GetDR
            List<Infos.UserData> userDataList = reticleMethod.reticlePodUserDataInfoGetDr(objCommon, reticlePodID);
            // Set
            durableAttribute.setUserDatas(new ArrayList<>());
            if (!CimArrayUtils.isEmpty(userDataList)) {
                for (Infos.UserData userData : userDataList) {
                    // Copy user data which originator is "SM".
                    if (CimStringUtils.equals(userData.getOriginator(), CIMStateConst.SP_USERDATA_ORIG_SM)) {
                        durableAttribute.getUserDatas().add(userData);
                    }
                }
            }
        }
        return data;
    }

    @Override
    public Results.ReticleStocInfoInqResult sxReticleStocInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        log.info("Enter the sxReticlePodDetailInfoInq() !");
        Results.ReticleStocInfoInqResult reticleStocInfoInqResult = new Results.ReticleStocInfoInqResult();
        /*----------------------*/
        /*    StockerGet Type   */
        /*----------------------*/
        //step1 - stocker_type_GetDR
        Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, stockerID);
        /*------------------------*/
        /*   Check Stocker Type   */
        /*------------------------*/
        //step2-Check Stocker Type
        String stockerType = objStockerTypeGetDROut.getStockerType();
        if (CimStringUtils.equalsIn(stockerType,
                BizConstant.SP_STOCKER_TYPE_AUTO,
                BizConstant.SP_STOCKER_TYPE_INTERM,
                BizConstant.SP_STOCKER_TYPE_SHELF,
                BizConstant.SP_STOCKER_TYPE_RETICLE,
                BizConstant.SP_STOCKER_TYPE_FIXTURE,
                BizConstant.SP_STOCKER_TYPE_INTERBAY,
                BizConstant.SP_STOCKER_TYPE_INTRABAY,
                BizConstant.SP_STOCKER_TYPE_RETICLEPOD,
                BizConstant.SP_STOCKER_TYPE_BARERETICLE,
                BizConstant.SP_STOCKER_TYPE_ERACK)) {
            Validations.check(new OmCode(retCodeConfig.getInvalidStockerType(), stockerType));
        }
        /*-----------------------------------------------*/
        /*   Fill In Stocker BR and Status Information   */
        /*-----------------------------------------------*/
        // step3  Fill In Stocker BR and Status Information
        return stockerMethod.stockerFillInTxPDQ006DR(objCommon, stockerID);
    }

    @Override
    public List<Infos.DurableAttribute> sxCarrierBasicInfoInq(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDSeq) {


        //-------------------------------
        // Check Input parameter
        //-------------------------------
        Validations.check(CimObjectUtils.isEmpty(cassetteIDSeq), retCodeConfig.getInvalidParameter());
        List<Infos.DurableAttribute> strDurableAttributeSeq = new ArrayList<>();
        for (ObjectIdentifier cassetteID : cassetteIDSeq) {
            // step1 - cassette_baseInfo_GetDR
            Infos.DurableAttribute durableAttribute = cassetteMethod.cassetteBaseInfoGetDR(objCommon, cassetteID);
            strDurableAttributeSeq.add(durableAttribute);

            // step2 - cassette_userDataInfo_GetDR
            List<Infos.UserData> userDataList = cassetteMethod.cassetteUserDataInfoGetDR(objCommon, cassetteID);

            // Set
            List<Infos.UserData> strUserDataSeq = new ArrayList<>();
            durableAttribute.setUserDatas(strUserDataSeq);
            for (Infos.UserData userData : userDataList) {
                // Copy user data which originator is "SM".
                if (CimStringUtils.equals(userData.getOriginator(), BizConstant.SP_USERDATA_ORIG_SM)) {
                    strUserDataSeq.add(userData);
                }
            }
        }
        return strDurableAttributeSeq;
    }

    @Override
    public Results.ReticleDetailInfoInqResult sxReticleDetailInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID, boolean durableOperationInfoFlag, boolean durableWipOperationInfoFlag) {

        // setp1 - reticle_detailInfo_GetDR__170
        Results.ReticleDetailInfoInqResult strReticleDetailInfoInqResult = reticleMethod.reticleDetailInfoGetDR(objCommon, reticleID, durableOperationInfoFlag, durableWipOperationInfoFlag);

        // step2 - durable_inPostProcessFlag_Get
        Boolean strDurable_inPostProcessFlag_Get_out = durableMethod.durableInPostProcessFlagGet(objCommon, BizConstant.SP_DURABLECAT_RETICLE, reticleID);

        Infos.ReticleStatusInfo reticleStatusInfo=strReticleDetailInfoInqResult.getReticleStatusInfo();
        reticleStatusInfo.setInPostProcessFlagOfReticle(strDurable_inPostProcessFlag_Get_out);

        return( strReticleDetailInfoInqResult );

    }

    @Override
    public Results.ReticleListInqResult sxReticleListInq(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams) {
        // step1 - reticle_list_GetDR__170
        return reticleMethod.reticleListGetDR(objCommon, reticleListInqParams);
    }

    @Override
    public Results.PageReticleListInqResult sxPageReticleListInq(Infos.ObjCommon objCommon, Params.ReticleListInqParams reticleListInqParams) {
        // step1 - reticle_list_GetDR__170
        return reticleMethod.reticleListGetDRForPage(objCommon, reticleListInqParams);
    }

    @Override
    public Results.CandidateDurableJobStatusDetail sxDurableJobStatusSelectionInq(Infos.ObjCommon objCommon, Params.DurableJobStatusSelectionInqParams params) {
        Validations.check(null == objCommon || null == params, retCodeConfig.getInvalidInputParam());

        String durableCategory = params.getDurableCategory();
        ObjectIdentifier durableID = params.getDurableID();
        log.info("input param durable category: " + durableCategory);
        log.info("input param durableID       : " + ObjectIdentifier.fetchValue(durableID));
        return durableMethod.durableCandidateJobStatusGet(objCommon, durableCategory, durableID);
    }

    @Override
    public Results.ErackPodInfoInqResult sxErackPodInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        log.info("Enter the sxErackPodInfoInq() !");
        Results.ErackPodInfoInqResult erackPodInfoInqResult = new Results.ErackPodInfoInqResult();
        /*----------------------*/
        /*    StockerGet Type   */
        /*----------------------*/
        //step1 - stocker_type_GetDR
        Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, stockerID);
        /*------------------------*/
        /*   Check Stocker Type   */
        /*------------------------*/
        //step2-Check Stocker Type
        String stockerType = objStockerTypeGetDROut.getStockerType();
        Validations.check(!CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_ERACK), new OmCode(retCodeConfig.getInvalidStockerType(), stockerType));
        erackPodInfoInqResult.setStockerID(stockerID);
        erackPodInfoInqResult.setStockerType(stockerType);
        /*-----------------------------------------------*/
        /*   Fill In Stocker BR and Status Information   */
        /*-----------------------------------------------*/
        // step3  Fill In Stocker BR and Status Information
        erackPodInfoInqResult.setPodInEracks(durableMethod.podInErack(objCommon, stockerID));
        return erackPodInfoInqResult;
    }

    @Override
    public Results.ReticlePodStockerInfoInqResult sxReticlePodStockerInfoInq(Infos.ObjCommon objCommon, Params.ReticlePodStockerInfoInqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        Results.ReticlePodStockerInfoInqResult result = new Results.ReticlePodStockerInfoInqResult();
        //=========================================================================
        // Get basic informatoin of reticle pod stocker
        //=========================================================================
        Results.StockerInfoInqResult stockerInfoInqResult = stockerMethod.stockerBaseInfoGet(objCommon, params.getStockerID());
        if (!CimStringUtils.equals(stockerInfoInqResult.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
            Validations.check(retCodeConfigEx.getStkTypeDifferent(),stockerInfoInqResult.getStockerType());
        }

        result.setStockerID(params.getStockerID());
        result.setDescription(stockerInfoInqResult.getDescription());
        result.setStockerType(stockerInfoInqResult.getStockerType());
        result.setE10Status(stockerInfoInqResult.getE10Status());
        result.setStockerStatusCode(stockerInfoInqResult.getStockerStatusCode());
        result.setStatusName(stockerInfoInqResult.getStatusName());
        result.setStatusDescription(stockerInfoInqResult.getStatusDescription());
        result.setStatusChangeTimeStamp(CimDateUtils.convertToSpecString(stockerInfoInqResult.getStatusChangeTimeStamp()));
        result.setActualE10Status(stockerInfoInqResult.getActualE10Status());
        result.setActualStatusCode(stockerInfoInqResult.getActualStatusCode());
        result.setActualStatusName(stockerInfoInqResult.getActualStatusName());
        result.setActualStatusDescription(stockerInfoInqResult.getActualStatusDescription());
        result.setActualStatusChangeTimeStamp(CimDateUtils.convertToSpecString(stockerInfoInqResult.getActualStatusChangeTimeStamp()));
        result.setResourceInfoData(stockerInfoInqResult.getResourceInfoData());

        //=========================================================================
        // get reticle pod list in stocker
        //=========================================================================
        List<Infos.ReticlePodInfoInStocker> reticlePodInfoInStockerList = stockerMethod.stockerStoredReticlePodGetDR(objCommon, params.getStockerID());
        result.setReticlePodInfoInStockerList(reticlePodInfoInStockerList);
        return result;
    }

    @Override
    public Results.BareReticleStockerInfoInqResult sxBareReticleStockerInfoInq(Infos.ObjCommon objCommon, Params.BareReticleStockerInfoInqParams params) {
        Results.BareReticleStockerInfoInqResult result = new Results.BareReticleStockerInfoInqResult();
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        ObjectIdentifier stockerID = params.getStockerID();
        log.debug("in-para stockerID: {}", ObjectIdentifier.fetchValue(stockerID));

        //=========================================================================
        // Get basic informatoin of bare reticle stocker
        //=========================================================================
        log.debug("step1 - stockerMethod.stockerBaseInfoGet");
        Results.StockerInfoInqResult stockerInfoInqResult = stockerMethod.stockerBaseInfoGet(objCommon, stockerID);
        log.debug("stockerID: {}",ObjectIdentifier.fetchValue(stockerInfoInqResult.getStockerID()));
        log.debug("stockerType: {}",stockerInfoInqResult.getStockerType());
        if (!CimStringUtils.equals(stockerInfoInqResult.getStockerType(),BizConstant.SP_STOCKER_TYPE_BARERETICLE)){
            log.debug("stockerType != BareReticle");
            Validations.check(true,retCodeConfigEx.getStkTypeDifferent(),stockerInfoInqResult.getStockerType());
        }
        result.setStockerID(stockerID);
        result.setDescription(stockerInfoInqResult.getDescription());
        result.setStockerType(stockerInfoInqResult.getStockerType());
        result.setE10Status(stockerInfoInqResult.getE10Status());
        result.setStockerStatusCode(stockerInfoInqResult.getStockerStatusCode());
        result.setStatusName(stockerInfoInqResult.getStatusName());
        result.setStatusDescription(stockerInfoInqResult.getStatusDescription());
        result.setStatusChangeTimeStamp(CimDateUtils.convertToSpecString(stockerInfoInqResult.getStatusChangeTimeStamp()));
        result.setActualE10Status(stockerInfoInqResult.getActualE10Status());
        result.setActualStatusCode(stockerInfoInqResult.getActualStatusCode());
        result.setActualStatusName(stockerInfoInqResult.getActualStatusName());
        result.setReticleStoreMaxCount(CimNumberUtils.longValue(stockerInfoInqResult.getMaxReticleCapacity()));
        result.setActualStatusDescription(stockerInfoInqResult.getActualStatusDescription());
        result.setActualStatusChangeTimeStamp(CimDateUtils.convertToSpecString(stockerInfoInqResult.getActualStatusChangeTimeStamp()));
        result.setOnlineMode(stockerInfoInqResult.getOnlineMode());

        //=========================================================================
        // Get reticle pod port information
        //=========================================================================
        log.debug("step2 - stockerMethod.stockerReticlePodPortInfoGetDR");
        List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, stockerID);
        result.setStrReticlePodPortIDs(reticlePodPortInfoList);
        //=========================================================================
        // Get stored reticle information
        //=========================================================================
        log.debug("step3 - stockerMethod.stockerStoredReticleGetDR");
        List<Infos.StoredReticle> storedReticleList = stockerMethod.stockerStoredReticleGetDR(objCommon, stockerID);
        result.setStrStoredReticles(storedReticleList);
        return result;
    }

    @Override
    public Results.BankListInqResult sxDrbBankInListInq(Infos.ObjCommon objCommon, Params.BankListInqParams params) {
        log.debug("step1 - query bank from route of type is Durable ");
        CimProcessDefinitionDO example = new CimProcessDefinitionDO();
        example.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MAIN);
        example.setFlowType(BizConstant.SP_FLOWTYPE_MAIN);
        example.setProcessDefinitionType(BizConstant.SP_MAINPDTYPE_DURABLE);
        List<CimProcessDefinitionDO> cimProcessDefinitionDOS = cimJpaRepository.findAll(Example.of(example));

        log.debug("step2 - query bank information");
        List<CimBank> cimBankBOS = Optional.ofNullable(cimProcessDefinitionDOS).orElseGet(Collections::emptyList).stream()
                .filter(ele -> ele.getStartBankID() != null && ele.getStartBankObj() != null)
                .map(ele -> baseCoreFactory.getBO(CimBank.class, ObjectIdentifier.build(ele.getStartBankID(), ele.getStartBankObj())))
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());

        log.debug("step3 - set params to output param");
        List<Infos.BankAttributes> bankAttributesList = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(cimBankBOS)) {
            for (CimBank cimBankBO : cimBankBOS) {
                Infos.BankAttributes bankAttributes = new Infos.BankAttributes();
                bankAttributes.setBankID(cimBankBO.getBankID());
                bankAttributes.setBankInBankFlag(cimBankBO.isBankInBank());
                bankAttributes.setBankName(cimBankBO.getDescription());
                bankAttributes.setControlWaferBankFlag(cimBankBO.isControlWaferBank());
                bankAttributes.setProductBankFlag(cimBankBO.isProductionBank());
                bankAttributes.setProductType(cimBankBO.getProductType());
                bankAttributes.setReceiveBankFlag(cimBankBO.isReceiveBank());
                bankAttributes.setShipBankFlag(cimBankBO.isShipBank());
                bankAttributes.setRecyclenBankFlag(cimBankBO.isRecycleBank());
                bankAttributes.setStbBankFlag(cimBankBO.isSTBBank());
                bankAttributes.setWaferIdGenerateBankFlag(cimBankBO.isWaferIDAssignmentRequired());
                String mysql = "select * from OMBANK_DESTBANK where REFKEY = ?1";
                List<CimBankDestBankDO> query = cimJpaRepository.query(mysql, CimBankDestBankDO.class, cimBankBO.getPrimaryKey());
                List<ObjectIdentifier> nextBankID = new ArrayList<>();
                if (CimArrayUtils.isNotEmpty(query)) {
                    for(CimBankDestBankDO bankDestBankDO: query) {
                        nextBankID.add(new ObjectIdentifier(bankDestBankDO.getDestBankID(), bankDestBankDO.getDestBankObj()));
                    }
                }
                bankAttributes.setNextBankID(nextBankID);
                bankAttributesList.add(bankAttributes);
            }
        }

        log.debug("step4 - page the result");
        Results.BankListInqResult bankListInqResult = new Results.BankListInqResult();
        bankListInqResult.setBankAttributes(CimPageUtils.convertListToPage(bankAttributesList, params.getSearchCondition().getPage(), params.getSearchCondition().getSize()));
        return bankListInqResult;
    }
}
