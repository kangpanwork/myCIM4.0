package com.fa.cim.service.arhs.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.idp.tms.api.TmsService;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IReticleMethod;
import com.fa.cim.method.IStockerMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.arhs.IArhsInqService;
import com.fa.cim.service.durable.IDurableInqService;
import com.fa.cim.service.equipment.IEquipmentInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class ArhsInqServiceImpl implements IArhsInqService {

    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private IStockerMethod stockerMethod;
    @Autowired
    private IReticleMethod reticleMethod;
    @Autowired
    private IDurableInqService durableInqService;
    @Autowired
    private IEquipmentInqService equipmentInqService;
    @Autowired
    private TmsService tmsService;

    @Override
    public Results.WhatReticleActionListInqResult sxWhatReticleActionListInq(Infos.ObjCommon objCommon, Params.WhatReticleActionListInqParams params) {
        Results.WhatReticleActionListInqResult result = new Results.WhatReticleActionListInqResult();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.info("ARHS switch on / off  tmpArhs = {}", tmpArhs);

        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        log.info("Check condition of RTD I/F");
        log.info("step1 - RTD_switch_GetDR");
        // TODO: 2020/11/3  RTD_switch_GetDR
//        if ( strRTD_switch_GetDR_out.bRTDIFSwitch == FALSE )
//        {
//            PPT_METHODTRACE_V1("", "RTD I/F switch is OFF !!!!");
//            SET_MSG_RC( strWhatReticleActionListInqResult, MSG_RTD_INTERFACE_SWITCH_OFF, RC_RTD_INTERFACE_SWITCH_OFF );
//            return (RC_RTD_INTERFACE_SWITCH_OFF);
//        }

        //----------------------------------------------------------
        //  Check RTD I/F Switch
        //---------------------
        // -------------------------------------
        log.info("step2 - RTD_switch_GetDR");
        // TODO: 2020/11/3 RTD_switch_SetDR

        //------------------------------------------------------
        //   get FSRTDIF infomation for Equipment
        //------------------------------------------------------
        log.info("step3 - RTD_data_GetDR");
        // TODO: 2020/11/3 RTD_data_GetDR
        String functionCode = StandardProperties.SP_RTD_FUNCTION_CODE_RETICLEACTIONLISTINQ.getValue();
//        objRTD_data_GetDR_out strRTD_data_GetDR_out;
//        rc = RTD_data_GetDR( strRTD_data_GetDR_out, strObjCommonIn, functionCode, dispatchStationID );
//        if( rc != RC_OK )
//        {
//            PPT_METHODTRACE_V1("", "RTD_data_GetDR() rc != RC_OK");
//            SET_MSG_RC( strWhatReticleActionListInqResult, MSG_NOT_FOUND_RTD, RC_NOT_FOUND_RTD);
//            return RC_NOT_FOUND_RTD;
//        }
        log.info("Check dispatchStationID is ALL");
        if (!CimStringUtils.equals(BizConstant.SP_DISPATCH_STATIONID_ALL, params.getDispatchStationID())) {
            log.info("dispatchStationID != ALL, Check dispatchStationID is Eqp or Stocker.");

            log.info("step4 - equipment_machineType_CheckDR");
            ObjectIdentifier tempDispatchStationID = ObjectIdentifier.buildWithValue(params.getDispatchStationID());
            Boolean equipmentFlag = equipmentMethod.equipmentMachineTypeCheckDR(objCommon,
                    tempDispatchStationID);

            if (CimBooleanUtils.isFalse(equipmentFlag)) {
                log.info("equipmentFlag == FALSE, Get dispatchStationID's stockerType");

                log.info("step5 - stocker_type_GetDR");
                Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, tempDispatchStationID);

                if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLEPOD, stockerTypeGetDROut.getStockerType())
                        || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_BARERETICLE, stockerTypeGetDROut.getStockerType())) {
                    log.info("dispatchStationID stockerType == ReticlePod or BareReticle");
                } else {
                    log.info("dispatchStationID stockerType != ReticlePod or BareReticle");
                    Validations.check(true, retCodeConfigEx.getNotReticleStocker(), ObjectIdentifier.fetchValue(tempDispatchStationID));
                }
            } else {
                log.info("equipmentFlag == TRUE");
            }
        } else {
            log.info("dispatchStationID == ALL");
        }
        /*--------------------*/
        /*   Request to RTD   */
        /*--------------------*/
        List<String> tempStringList = new ArrayList<>();
        log.info("step6 - RTDDispatchListInq");
        // TODO: 2020/11/3 txRTDDispatchListInq
//        rc = txRTDDispatchListInq(strRTDDispatchListInqResult,
//                strObjCommonIn,
//                SP_RTD_Function_Code_ReticleActionListInq,
//                dispatchStationID,
//                tmpStringList);

        // set out structure
        result.setStrDispatchResult(null);
        Infos.DispatchResult tmpDispatchResult = null;

        /*----------------------*/
        /*   Convert RTD data   */
        /*----------------------*/
        log.info("step7 - RTD_dispatchDataToReticleActionList_Convert");
        // TODO: 2020/11/3 RTD_dispatchDataToReticleActionList_Convert
//        objRTD_dispatchDataToReticleActionList_Convert_out strRTD_dispatchDataToReticleActionList_Convert_out;
//        rc = RTD_dispatchDataToReticleActionList_Convert(strRTD_dispatchDataToReticleActionList_Convert_out,
//                strObjCommonIn,

        List<Infos.ReticleDispatchJob> strReticleDispatchJobRTDList = new ArrayList<>();
        int rtdLen = CimArrayUtils.getSize(strReticleDispatchJobRTDList);

        /*------------------*/
        /*   Get RDJ List   */
        /*------------------*/
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, "");
        int deleteLen = 0, insertLen = 0;

        //Make delete list
        List<Infos.ReticleDispatchJob> retcileDispatchDeleteList = new ArrayList<>();
        List<Infos.ReticleDispatchJob> reticleDispatchJobInsert = new ArrayList<>();
        result.setStrReticleDispatchJobDeleteList(retcileDispatchDeleteList);
        result.setStrReticleDispatchJobInsertList(reticleDispatchJobInsert);

        List<Infos.ReticleDispatchJob> strReticleDispatchJobExList = new ArrayList<>();

        int exLen = 0;
        log.info("Make delete list");
        if (CimArrayUtils.isNotEmpty(reticleDispatchJobList)) {
            for (Infos.ReticleDispatchJob reticleDispatchJob : reticleDispatchJobList) {
                if (CimStringUtils.equals(reticleDispatchJob.getDispatchStationID(), params.getDispatchStationID())
                        && (CimStringUtils.equals(BizConstant.SP_RDJ_STATUS_CREATED, reticleDispatchJob.getJobStatus())
                        || CimStringUtils.equals(BizConstant.SP_RDJ_STATUS_WAITTORELEASE, reticleDispatchJob.getJobStatus()))
                        && ObjectIdentifier.equalsWithValue(reticleDispatchJob.getRequestUserID(), objCommon.getUser().getUserID())) {
                    log.info("this record is same dispatchStationID and requested from RTD and create/waittorelease status.");
                    retcileDispatchDeleteList.add(reticleDispatchJob);
                    deleteLen++;
                } else {
                    log.info("this record leaved");
                    strReticleDispatchJobExList.add(reticleDispatchJob);
                    exLen++;
                }
            }
        }
        log.info("exLen: {}", exLen);

        //Check exist reticle/pod in other dispatchStation and toEquipment, priority or not, and reticle current location.
        log.info("Check exist reticle/pod in other dispatchStation or not.");
        // TODO: 2020/11/3 confrim the Tx is  necesary ? because it depend on RTD

        return null;
    }

    @Override
    public List<Infos.StoredReticle> sxWhatReticleRetrieveInq(Infos.ObjCommon objCommon, Params.WhatReticleRetrieveInqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.info("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        log.info("in-para equipmentID: {}", ObjectIdentifier.fetchValue(params.getEquipmentID()));
        return equipmentMethod.equipmentRetrieveReticleListGetDR(objCommon, params.getEquipmentID());
    }

    @Override
    public Results.WhereNextForReticlePodInqResult sxWhereNextForReticlePodInq(Infos.ObjCommon objCommon, Params.WhereNextForReticlePodInqParams params) {
        Results.WhereNextForReticlePodInqResult result = new Results.WhereNextForReticlePodInqResult();
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());
        /*-------------------------------------------------------------------------*/
        /*   Get current reticle information for return structure of this method   */
        /*-------------------------------------------------------------------------*/
        if (log.isDebugEnabled()){
            log.debug("step1 - durableInqService.sxReticlePodDetailInfoInq");
        }
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setReticlePodID(params.getReticlePodID());
        Results.ReticlePodDetailInfoInqResult reticlePodDetailInfoInqResult = durableInqService.sxReticlePodDetailInfoInq(objCommon, reticlePodDetailInfoInqParams);
        Infos.ReticlePodStatusInfo reticlePodStatusInfo = reticlePodDetailInfoInqResult.getReticlePodStatusInfo();

        ObjectIdentifier reticlePodStockerID = null;
        ObjectIdentifier bareReticleStockerID = null;
        if (reticlePodStatusInfo != null && ObjectIdentifier.isNotEmptyWithValue(reticlePodStatusInfo.getStockerID())) {
            if (log.isDebugEnabled()){
                log.debug("reticlePodStatusInfo.stockerID: {}", ObjectIdentifier.fetchValue(reticlePodStatusInfo.getStockerID()));
                log.debug("step2 - stockerMethod.stockerTypeGet");
            }
            Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = stockerMethod.stockerTypeGet(objCommon, reticlePodStatusInfo.getStockerID());

            if (log.isDebugEnabled()){
                log.debug("stockerType: {}", stockerTypeGetDROut.getStockerType());
            }
            if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_BARERETICLE,stockerTypeGetDROut.getStockerType())){
                if (log.isDebugEnabled()){
                    log.debug("stockerType = BareReticle ");
                }
                bareReticleStockerID = reticlePodStatusInfo.getStockerID();
            }else {
                if (log.isDebugEnabled()){
                    log.debug("stockerType = ReticlePod");
                }
                reticlePodStockerID = reticlePodStatusInfo.getStockerID();
            }
        }
        result.setReticlePodID(params.getReticlePodID());
        if (null != reticlePodStatusInfo) {
            result.setTransferStatus(reticlePodStatusInfo.getTransferStatus());
            result.setCurrentEquipmentID(reticlePodStatusInfo.getEquipmentID());
        }
        result.setCurrentReticlePodStockerID(reticlePodStockerID);
        result.setCurrentBareReticleStockerID(bareReticleStockerID);

        /*------------------------------------------------------------------------------------*/
        /*   Get reticlePod Xfer Job if exist                                                 */
        /*------------------------------------------------------------------------------------*/
        List<Infos.NextMachineForReticlePod> strNextMachineForReticlePod = new ArrayList<>();
        result.setNextMachineForReticlePodList(strNextMachineForReticlePod);
        if (log.isDebugEnabled()){
            log.debug("step3 - reticleMethod.reticleComponentJobListGetDR");
        }
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon,"");
        if (CimArrayUtils.isNotEmpty(reticleComponentJobList)){
            for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
                if (CimStringUtils.equals(BizConstant.SP_RCJ_JOBNAME_XFER,reticleComponentJob.getJobName())
                        && ObjectIdentifier.equalsWithValue(reticleComponentJob.getReticlePodID(), params.getReticlePodID())){
                    if (log.isDebugEnabled()){
                        log.debug("Found Xfer job for this Reticle Pod: {}.",ObjectIdentifier.fetchValue(reticleComponentJob.getReticlePodID()));
                    }
                    Infos.NextMachineForReticlePod nextMachineForReticlePod = new Infos.NextMachineForReticlePod();
                    strNextMachineForReticlePod.add(nextMachineForReticlePod);
                    if (CimStringUtils.equals(BizConstant.SP_MACHINE_TYPE_EQP,reticleComponentJob.getToEquipmentCategory())){
                        if (log.isDebugEnabled()){
                            log.debug("toEquipmentCategory: {}",reticleComponentJob.getToEquipmentCategory());
                        }
                        nextMachineForReticlePod.setEquipmentID(reticleComponentJob.getToEquipmentID());
                        nextMachineForReticlePod.setPortID(reticleComponentJob.getToReticlePodPortID());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_BARERETICLE,reticleComponentJob.getToEquipmentCategory())){
                        if (log.isDebugEnabled()){
                            log.debug("toEquipmentCategory: {}",reticleComponentJob.getToEquipmentCategory());
                        }
                        nextMachineForReticlePod.setBareReticleStockerID(reticleComponentJob.getToEquipmentID());
                        nextMachineForReticlePod.setResourceID(reticleComponentJob.getToReticlePodPortID());
                    }
                    if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLEPOD,reticleComponentJob.getToEquipmentCategory())){
                        if (log.isDebugEnabled()){
                            log.debug("toEquipmentCategory: {}",reticleComponentJob.getToEquipmentCategory());
                        }
                        nextMachineForReticlePod.setReticlePodStockerID(reticleComponentJob.getToEquipmentID());
                    }
                    return result;
                }
            }
        }
        /*---------------------------------------------------------------------------*/
        /*   If reticle is in reticle pod stoker or in bare reticle stoker           */
        /*   reticle pod stoker  case --> not set return struct                      */
        /*   bare reticle stoker case --> set return struct                          */
        /*---------------------------------------------------------------------------*/
        if (ObjectIdentifier.isNotEmptyWithValue(reticlePodDetailInfoInqResult.getReticlePodStatusInfo().getStockerID())){
            if (log.isDebugEnabled()){
                log.debug("reticlePodDetailInfoInqResult.getReticlePodStatusInfo().getStockerID() is not null");
            }
            /*-----------------------------*/
            /*  bare reticle stoker case.  */
            /*-----------------------------*/
            if (ObjectIdentifier.isNotEmptyWithValue(bareReticleStockerID)){
                if (log.isDebugEnabled()){
                    log.debug("bareReticleStockerID: {}",ObjectIdentifier.fetchValue(bareReticleStockerID));
                    log.debug("step4 - equipmentMethod.machineWorkAreaGet");
                }
                ObjectIdentifier areaID = equipmentMethod.machineWorkAreaGet(objCommon,bareReticleStockerID);
                if (log.isDebugEnabled()){
                    log.debug("areaID: {}",ObjectIdentifier.fetchValue(areaID));
                }

                Params.EqpListByBayInqInParm eqpListByBayInqInParm = new Params.EqpListByBayInqInParm();
                eqpListByBayInqInParm.setWorkArea(areaID);
                if (log.isDebugEnabled()){
                    log.debug("step5 - equipmentInqService.sxEqpListByBayInq");
                }
                Results.EqpListByBayInqResult eqpListByBayInqResult = equipmentInqService.sxEqpListByBayInq(objCommon, eqpListByBayInqInParm);

                if (null != eqpListByBayInqResult && CimArrayUtils.isNotEmpty(eqpListByBayInqResult.getStrAreaStocker())){
                    for (Infos.AreaStocker areaStocker : eqpListByBayInqResult.getStrAreaStocker()) {
                        if (log.isDebugEnabled()){
                            log.debug("stockerID: {}",ObjectIdentifier.fetchValue(areaStocker.getStockerID()));
                            log.debug("stockerType: {}",areaStocker.getStockerType());
                        }
                        if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLEPOD,areaStocker.getStockerType())){
                            Infos.NextMachineForReticlePod nextMachineForReticlePod = new Infos.NextMachineForReticlePod();
                            strNextMachineForReticlePod.add(nextMachineForReticlePod);
                            nextMachineForReticlePod.setReticlePodStockerID(areaStocker.getStockerID());
                        }
                    }
                }
            }
            return result;
        }
        /*---------------------------------------------------------------------------------*/
        /*   If reticle is in equipment, get bare reticle stocker list of that equipment   */
        /*---------------------------------------------------------------------------------*/
        if (log.isDebugEnabled()){
            log.debug("step6 - equipmentMethod.equipmentStockerInfoGetDR");
        }
        Infos.EqpStockerInfo eqpStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommon, result.getCurrentEquipmentID());
        if (null != eqpStockerInfo && CimArrayUtils.isNotEmpty(eqpStockerInfo.getEqpStockerStatusList())){
            for (Infos.EqpStockerStatus eqpStockerStatus : eqpStockerInfo.getEqpStockerStatusList()) {
                if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLEPOD,eqpStockerStatus.getStockerType())){
                    if (log.isDebugEnabled()){
                        log.debug("stockerType = {}",BizConstant.SP_STOCKER_TYPE_RETICLEPOD);
                    }
                    Infos.NextMachineForReticlePod nextMachineForReticlePod = new Infos.NextMachineForReticlePod();
                    strNextMachineForReticlePod.add(nextMachineForReticlePod);
                    nextMachineForReticlePod.setReticlePodStockerID(eqpStockerStatus.getStockerID());
                }
            }
        }
        return result;
    }

    @Override
    public List<Infos.ReticleComponentJob> sxReticleComponentJobListInq(Infos.ObjCommon objCommon, Params.ReticleComponentJobListInqParams params) {
        List<Infos.ReticleComponentJob> result = new ArrayList<>();
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        String reticleDispatchJobID = params.getReticleDispatchJobID();
        if (log.isDebugEnabled())
            log.debug("in-para reticleDispatchJobID: {}", reticleDispatchJobID);
        return reticleMethod.reticleComponentJobListGetDR(objCommon, reticleDispatchJobID);
    }

    @Override
    public List<Infos.ReticleDispatchJob> sxReticleDispatchJobListInq(Infos.ObjCommon objCommon) {
        List<Infos.ReticleDispatchJob> result = new ArrayList<>();
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());
        return reticleMethod.reticleDispatchJobListGetDR(objCommon, "");
    }

    @Override
    public Results.ReticlePodXferJobListInqResult sxReticlePodXferJobListInq(Infos.ObjCommon objCommon, Params.ReticlePodXferJobListInqParams params) {
        Results.ReticlePodXferJobListInqResult result = new Results.ReticlePodXferJobListInqResult();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier toMachineID = params.getToMachineID();
        ObjectIdentifier toStockerID = params.getToStockerID();
        ObjectIdentifier fromMachineID = params.getFromMachineID();
        ObjectIdentifier fromStockerID = params.getFromStockerID();

        //=========================================================================
        // Check input parameter
        // !!!!! Only 1 parameter can be filled !!!!!
        //=========================================================================

        int nInputValueCnt = 0;
        if (ObjectIdentifier.isNotEmptyWithValue(reticlePodID)) {
            nInputValueCnt++;
        }
        if (ObjectIdentifier.isNotEmptyWithValue(toMachineID)) {
            nInputValueCnt++;
        }
        if (ObjectIdentifier.isNotEmptyWithValue(toStockerID)) {
            nInputValueCnt++;
        }
        if (ObjectIdentifier.isNotEmptyWithValue(fromMachineID)) {
            nInputValueCnt++;
        }
        if (ObjectIdentifier.isNotEmptyWithValue(fromStockerID)) {
            nInputValueCnt++;
        }
        if (log.isDebugEnabled())
            log.debug("nInputValueCnt: {}", nInputValueCnt);

        if (nInputValueCnt != 1) {
            log.error("nInputValueCnt != 1");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        //=========================================================================
        // Inquire reticle pod current transfer job to TMS(R)
        //=========================================================================
        Inputs.SendRTMSTransportJobInqIn input = new Inputs.SendRTMSTransportJobInqIn();
        input.setStrObjCommonIn(objCommon);
        input.setRequestUserID(objCommon.getUser());
        input.setReticlePodID(reticlePodID);
        input.setToMachineID(toMachineID);
        input.setToStockerID(toStockerID);
        input.setFromMachineID(fromMachineID);
        input.setFromStockerID(fromStockerID);
        input.setDetailFlag(params.getDetailFlag());
        log.debug("step1 - tmsService.rtransportJobInq");
        Outputs.SendTransportJobInqOut sendTransportJobInqOut = tmsService.rtransportJobInq(input);

        //=========================================================================
        // Set returned structure into this method return structure
        //=========================================================================
        List<Infos.ReticlePodXferJob> strReticlePodXferJobs = new ArrayList<>();
        result.setReticlePodXferJobList(strReticlePodXferJobs);
        if (null != sendTransportJobInqOut && null != sendTransportJobInqOut.getStrTransportJobInqResult() && CimArrayUtils.isNotEmpty(sendTransportJobInqOut.getStrTransportJobInqResult().getJobInqData())) {
            for (Infos.TransportJobInqData jobInqData : sendTransportJobInqOut.getStrTransportJobInqResult().getJobInqData()) {
                Infos.ReticlePodXferJob reticlePodXferJob = new Infos.ReticlePodXferJob();
                strReticlePodXferJobs.add(reticlePodXferJob);
                reticlePodXferJob.setJobID(jobInqData.getJobID());
                reticlePodXferJob.setJobStatus(jobInqData.getJobStatus());
                reticlePodXferJob.setTransportType(jobInqData.getTransportType());

                List<Infos.ReticlePodJob> reticlePodJobList = new ArrayList<>();
                reticlePodXferJob.setReticlePodJobList(reticlePodJobList);

                if (CimArrayUtils.isNotEmpty(jobInqData.getCarrierJobInqInfo())) {
                    for (Infos.CarrierJobInqInfo carrierJobInqInfo : jobInqData.getCarrierJobInqInfo()) {
                        Infos.ReticlePodJob reticlePodJob = new Infos.ReticlePodJob();
                        reticlePodJobList.add(reticlePodJob);
                        reticlePodJob.setReticlePodJobID(carrierJobInqInfo.getCarrierJobID());
                        reticlePodJob.setReticlePodJobStatus(carrierJobInqInfo.getCarrierJobStatus());
                        reticlePodJob.setReticlePodID(carrierJobInqInfo.getCarrierID());
                        reticlePodJob.setFromMachineID(carrierJobInqInfo.getFromMachineID());
                        reticlePodJob.setFromPortID(carrierJobInqInfo.getFromPortID());
                        reticlePodJob.setToStockerGroup(carrierJobInqInfo.getToStockerGroup());
                        reticlePodJob.setToMachineID(carrierJobInqInfo.getToMachineID());
                        reticlePodJob.setToPortID(carrierJobInqInfo.getToPortID());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Results.WhatReticlePodForReticleXferInqResult sxWhatReticlePodForReticleXferInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        Results.WhatReticlePodForReticleXferInqResult result = new Results.WhatReticlePodForReticleXferInqResult();
        Outputs.ReticleReticlePodGetForXferOut reticleReticlePodGetForXferOut = reticleMethod.reticleReticlePodGetForXfer(objCommon, reticleID);
        if (!CimObjectUtils.isEmpty(reticleReticlePodGetForXferOut)) {
            result.setReticleID(reticleReticlePodGetForXferOut.getReticleID());
            result.setStrCandidateReticlePods(reticleReticlePodGetForXferOut.getStrCandidateReticlePods());
        }
        return result;
    }
}
