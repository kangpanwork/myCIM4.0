package com.fa.cim.controller.fmc;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.bond.IBondController;
import com.fa.cim.controller.interfaces.fmc.IFMCController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.constraint.IConstraintService;
import com.fa.cim.service.fmc.IFMCService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>FMCController .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/21 11:03         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:03
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IBondController.class, confirmableKey = "FMCConfirm", cancellableKey = "FMCCancel")
@Listenable
@RestController
@RequestMapping("/fmc")
public class FMCController implements IFMCController {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IConstraintService mfgRestrictReqService;

    @Autowired
    private IConstraintService constraintService;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IFMCService fmcService;

    @ResponseBody
    @PostMapping(value = "/fmc_move_in_reserve/req")
    @Override
    @CimMapping(TransactionIDEnum.FMC_MOVE_IN_RESERVE_REQ)
    @EnablePostProcess
    public Response fmcMoveInReserveReq(@RequestBody Params.SLMStartLotsReservationReqInParams slmStartLotsReservationReqInParams) {
        final String transactionID = TransactionIDEnum.FMC_MOVE_IN_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == slmStartLotsReservationReqInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier equipmentID = slmStartLotsReservationReqInParams.getEquipmentID();

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, slmStartLotsReservationReqInParams.getUser());
        /*-----------------------------------------------------------------------*/
        /*   abstract LotIDs for PrivilegeCheck                                  */
        /*-----------------------------------------------------------------------*/
        log.info("abstract lotIDs for PrivilegeCheck!!");
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        Optional.ofNullable(slmStartLotsReservationReqInParams.getStartCassetteList()).ifPresent(list -> list.forEach(data -> {
            Optional.ofNullable(data.getLotInCassetteList()).ifPresent(lotInCassettes -> lotInCassettes.forEach(lotInCassette -> {
                if (lotInCassette.getMoveInFlag()) {
                    lotIDs.add(lotInCassette.getLotID());
                }
            }));
        }));
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams();
        checkInqParams.setEquipmentID(equipmentID);
        checkInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, checkInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        ObjectIdentifier retVal = null;
        AtomicReference<String> APCIFControlStatus = new AtomicReference<>();
        try {
            retVal = fmcService.sxSLMStartLotsReservationReq(objCommon, slmStartLotsReservationReqInParams, APCIFControlStatus);
        } catch (ServiceException e) {
            String env = StandardProperties.OM_APC_NOTIFY_RESEND.getValue();
            if (CimStringUtils.isNotEmpty(APCIFControlStatus.get()) && CimStringUtils.equals(env, "1")) {
                log.info(String.format("Resend ControlJobInformation to APC. notify status -> %s", APCIFControlStatus.get()));
                // todo: txAPCControlJobInfoRpt == txAPCCJRpt
            }

            //--------------------------------
            // Entity Inhibit
            //--------------------------------
            log.warn("small Tx return error. rc: " + e.getCode());
            //------------------------------------------
            //  Entity Inhibit enable check.
            //  OM_CONSTRAINT_APC_RPARM_CHG_ERROR
            //       0 --> Inhibit disable
            //       1 --> Inhibit enable
            //------------------------------------------

            String envStr = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
            if (CimStringUtils.isEmpty(envStr))
                throw new ServiceException("Cannot found the Environment.[OM_CONSTRAINT_APC_RPARM_CHG_ERRORew]");
            int envInhibitWhenAPC = Integer.parseInt(envStr);
            if (envInhibitWhenAPC == 1) {
                log.info("OM_CONSTRAINT_APC_RPARM_CHG_ERROR == 1");
                if (Validations.isEquals(e.getCode(), retCodeConfig.getNoResponseApc())
                        || Validations.isEquals(e.getCode(), retCodeConfig.getApcServerBindFail())
                        || Validations.isEquals(e.getCode(), retCodeConfig.getApcRuntimecapabilityError())
                        || Validations.isEquals(e.getCode(), retCodeConfig.getApcRecipeparameterreqError())
                        || Validations.isEquals(e.getCode(), retCodeConfig.getApcReturnDuplicateParametername())) {
                    log.info("APC IF Error.");
                    //-------------------------------------------
                    // Inhibit Equipment for APC I/F Errors
                    //-------------------------------------------
                    Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                    mfgRestrictReqParams.setUser(objCommon.getUser());
                    Infos.EntityInhibitDetailAttributes attributes = new Infos.EntityInhibitDetailAttributes();
                    attributes.setEntities(Collections.singletonList(new Infos.EntityIdentifier(BizConstant.SP_INHIBITCLASSID_EQUIPMENT, equipmentID, "")));
                    attributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

                    if (Validations.isEquals(e.getCode(), retCodeConfig.getNoResponseApc())
                            || Validations.isEquals(e.getCode(), retCodeConfig.getApcServerBindFail())) {
                        attributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                    }
                    if (Validations.isEquals(e.getCode(), retCodeConfig.getApcRuntimecapabilityError())
                            || Validations.isEquals(e.getCode(), retCodeConfig.getApcRecipeparameterreqError())) {
                        attributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                    }
                    if (Validations.isEquals(e.getCode(), retCodeConfig.getApcReturnDuplicateParametername())) {
                        attributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                    }
                    attributes.setOwnerID(objCommon.getUser().getUserID());
                    mfgRestrictReqParams.setEntityInhibitDetailAttributes(attributes);

//                    // Inhibit
//                    constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                    //Step5 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(attributes);
                    mfgRestrictReq_110Params.setClaimMemo("");
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                }
            }
            throw e;
        }

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        assert retVal != null;
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/fmc_wafer_retrieve_carrier_reserve/req")
    @Override
    @CimMapping(TransactionIDEnum.FMC_WAFER_RETRIEVE_CARRIER_RESERVE_REQ)
    public Response fmcWaferRetrieveCarrierReserveReq(@RequestBody Params.SLMWaferRetrieveCassetteReserveReqInParams slmWaferRetrieveCassetteReserveReqInParams) {
        final String transactionID = TransactionIDEnum.FMC_WAFER_RETRIEVE_CARRIER_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        log.info("【step0】check input params");
        Validations.check(null == slmWaferRetrieveCassetteReserveReqInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier equipmentID = slmWaferRetrieveCassetteReserveReqInParams.getEquipmentID();
        List<Infos.EventParameter> strEventParameter = new ArrayList<>();

        // Put Incoming Log.
        strEventParameter.add(new Infos.EventParameter("EQP_ID",slmWaferRetrieveCassetteReserveReqInParams.getEquipmentID().getValue()));
        strEventParameter.add(new Infos.EventParameter("CONTROL_JOB_ID",slmWaferRetrieveCassetteReserveReqInParams.getControlJobID().getValue()));
        strEventParameter.add(new Infos.EventParameter("CAST_ID",slmWaferRetrieveCassetteReserveReqInParams.getCassetteID().getValue()));
        strEventParameter.add(new Infos.EventParameter("LOT_ID",slmWaferRetrieveCassetteReserveReqInParams.getLotID().getValue()));
        strEventParameter.add(new Infos.EventParameter("DEST_PORT_ID",slmWaferRetrieveCassetteReserveReqInParams.getDestPortID().getValue()));
        strEventParameter.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));
        // Check privilege
        log.info("【step2】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, slmWaferRetrieveCassetteReserveReqInParams.getUser());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        List<ObjectIdentifier> lotList = new ArrayList<>();
        lotList.add(slmWaferRetrieveCassetteReserveReqInParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotList);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.SLMWaferRetrieveCassetteReserveReqResult slmWaferRetrieveCassetteReserveReqResult = new Results.SLMWaferRetrieveCassetteReserveReqResult();
        List<Infos.MtrlOutSpec> strMtrlOutSpecSeq = new ArrayList<>();
        try {
            slmWaferRetrieveCassetteReserveReqResult = fmcService.sxSLMWaferRetrieveCassetteReserveReq(objCommon, slmWaferRetrieveCassetteReserveReqInParams);
            strMtrlOutSpecSeq = slmWaferRetrieveCassetteReserveReqResult.getStrMtrlOutSpecSeq();
        } catch (ServiceException e) {
            if (retCodeConfigEx.getNotEnoughEmptycastFromAllaround().getCode() == e.getCode()){
                log.info("sxSLMWaferRetrieveCassetteReserveReq() rc != RC_OK && rc == RC_NOT_ENOUGH_EMPTYCAST_FROM_ALLAROUND");
                // set message for system message
                StringBuilder mailMsgStream = new StringBuilder();
                mailMsgStream.append(" No Available Retrieving Cassette Error \n");
                mailMsgStream.append("  (SLM) No Available Retrieving Cassette Error. \n");
                mailMsgStream.append("No available retrieving cassette is found for lot <").append(slmWaferRetrieveCassetteReserveReqInParams.getLotID().getValue())
                        .append("> on equipment <").append(slmWaferRetrieveCassetteReserveReqInParams.getEquipmentID().getValue()).append(">, ");
                mailMsgStream.append("please assign appropriate cassette manually through MMClient.");
                //TODO:txSystemMsgRpt
                //--------------------------------------------
                // txSystemMsgRpt
                //--------------------------------------------
                ObjectIdentifier dummyID = new ObjectIdentifier();
            }else {
                throw e;
            }
        }
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<ObjectIdentifier> dstCastIDs = new ArrayList<>();
        for (Infos.MtrlOutSpec mtrlOutSpec : strMtrlOutSpecSeq){
            List<Infos.SlmSlotMap> strDestinationMapSeq = mtrlOutSpec.getDestinationMapList();
            for (Infos.SlmSlotMap slmSlotMap : strDestinationMapSeq){
                boolean foundFlag = false;
                for (ObjectIdentifier dstCastID : dstCastIDs){
                    if (ObjectIdentifier.equalsWithValue(dstCastID,slmSlotMap.getCassetteID())){
                        foundFlag = true;
                        continue;
                    }
                }
                if (!foundFlag){
                    dstCastIDs.add(slmSlotMap.getCassetteID());
                }
            }
        }

        List<Infos.EventParameter> strResultParameter = new ArrayList<>();
        for (ObjectIdentifier dstCastID : dstCastIDs){
            strEventParameter.add(new Infos.EventParameter(null,dstCastID.getValue()));
        }
        strResultParameter.forEach(eventParameter -> log.info("DEST_CAST_ID : " + eventParameter.getParameterValue()));

        return Response.createSucc(transactionID, slmWaferRetrieveCassetteReserveReqResult);
    }

    @ResponseBody
    @PostMapping(value = "/fmc_process_job_status/rpt")
    @Override
    @CimMapping(TransactionIDEnum.FMC_PROCESS_JOB_STATUS_RPT)
    public Response fmcProcessJobStatusRpt(@RequestBody Params.SLMProcessJobStatusRptInParams slmProcessJobStatusRptInParams) {
        final String transactionID = TransactionIDEnum.FMC_PROCESS_JOB_STATUS_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        log.info("【step0】check input params");
        Validations.check(null == slmProcessJobStatusRptInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier equipmentID = slmProcessJobStatusRptInParams.getEquipmentID();
        List<Infos.EventParameter> strEventParameter = new ArrayList<>();

        // Put Incoming Log.
        strEventParameter.add(new Infos.EventParameter("EQP_ID", slmProcessJobStatusRptInParams.getEquipmentID().getValue()));
        strEventParameter.add(new Infos.EventParameter("CONTROL_JOB_ID", slmProcessJobStatusRptInParams.getControlJobID().getValue()));
        strEventParameter.add(new Infos.EventParameter("ACTION_CODE", slmProcessJobStatusRptInParams.getActionCode()));
        strEventParameter.add(new Infos.EventParameter("PROCESS_JOB_ID", slmProcessJobStatusRptInParams.getProcessJobID()));
        strEventParameter.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));

        // Check privilege
        log.info("【step2】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, slmProcessJobStatusRptInParams.getUser());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        fmcService.sxSLMProcessJobStatusRpt(objCommon, slmProcessJobStatusRptInParams);

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @PostMapping(value = "/fmc_wafer_store/rpt")
    @Override
    @CimMapping(TransactionIDEnum.FMC_WAFER_STORE_RPT)
    public Response fmcWaferStoreRpt(@RequestBody Params.SLMWaferStoreRptInParams slmWaferStoreRptInParams) {
        final String transactionID = TransactionIDEnum.FMC_WAFER_STORE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == slmWaferStoreRptInParams, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier equipmentID = slmWaferStoreRptInParams.getEquipmentID();

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, slmWaferStoreRptInParams.getUser());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();


        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        if(CimStringUtils.equals(tmpPrivilegeCheckCJValue, "1")) {
            List<ObjectIdentifier> lotIDs = controlJobMethod.controlJobLotIDListGetDR(objCommon, slmWaferStoreRptInParams.getControlJobID());
            accessControlCheckInqParams.setLotIDLists(lotIDs);
        }
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        // PrivilegeCheck
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        fmcService.sxFMCWaferStoreRpt(objCommon, slmWaferStoreRptInParams);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/fmc_wafer_retrieve/rpt")
    @Override
    @CimMapping(TransactionIDEnum.FMC_WAFER_RETRIEVE_RPT)
    public Response fmcWaferRetrieveRpt(@RequestBody Params.SLMWaferRetrieveRptInParams params) {
        final String transactionID = TransactionIDEnum.FMC_WAFER_RETRIEVE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        List<Infos.EventParameter> eventParameters = new ArrayList<>(4);
        eventParameters.add(new Infos.EventParameter("EQP_ID", ObjectIdentifier.fetchValue(params.getEquipmentID())));
        eventParameters.add(new Infos.EventParameter("CONTROL_JOB_ID", ObjectIdentifier.fetchValue(params.getControlJobID())));
        eventParameters.add(new Infos.EventParameter("PROCESS_JOB_ID", params.getProcessJobID()));
        StringBuilder stringBuilder = new StringBuilder();
        params.getSlmDestSlotMapList().forEach(data -> stringBuilder.append("[")
                .append("CAST_ID").append(ObjectIdentifier.fetchValue(data.getCassetteID()))
                .append("WAFER_ID").append(ObjectIdentifier.fetchValue(data.getWaferID()))
                .append("SLOT_NO").append(data.getSlotNumber())
                .append("]")
                .append(","));
        eventParameters.add(new Infos.EventParameter("DEST_SLOT_MAP", stringBuilder.substring(0, stringBuilder.length() - 1)));
        eventParameters.forEach(eventParameter -> log.info(eventParameter.toString()));
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, params.getUser());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        fmcService.sxFMCWaferRetrieveRpt(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @PostMapping(value = "/fmc_carrier_remove_from_cj/req")
    @Override
    @CimMapping(TransactionIDEnum.FMC_CARRIER_REMOVE_FROM_CJ_REQ)
    public Response fmcCarrierRemoveFromCJReq(@RequestBody Params.SLMCassetteDetachFromCJReqInParams slmCassetteDetachFromCJReqInParams) {
        final String transactionID = TransactionIDEnum.FMC_CARRIER_REMOVE_FROM_CJ_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        log.info("【step0】check input params");
        Validations.check(null == slmCassetteDetachFromCJReqInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier equipmentID = slmCassetteDetachFromCJReqInParams.getEquipmentID();
        List<Infos.EventParameter> strEventParameter = new ArrayList<>();

        // Put Incoming Log.
        strEventParameter.add(new Infos.EventParameter("EQP_ID", slmCassetteDetachFromCJReqInParams.getEquipmentID().getValue()));
        strEventParameter.add(new Infos.EventParameter("CONTROL_JOB_ID", slmCassetteDetachFromCJReqInParams.getControlJobID().getValue()));
        strEventParameter.add(new Infos.EventParameter("CAST_ID", slmCassetteDetachFromCJReqInParams.getCassetteID().getValue()));
        strEventParameter.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));

        // Check privilege
        log.info("【step2】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, slmCassetteDetachFromCJReqInParams.getUser());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        fmcService.sxSLMCassetteDetachFromCJReq(objCommon, slmCassetteDetachFromCJReqInParams);

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @PostMapping(value = "/fmc_carrier_unclamp/req")
    @Override
    @CimMapping(TransactionIDEnum.FMC_CARRIER_UNCLAMP_REQ)
    public Response fmcCarrierUnclampReq(@RequestBody Params.SLMCassetteUnclampReqInParams params) {
        final String transactionID = TransactionIDEnum.FMC_CARRIER_UNCLAMP_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        List<Infos.EventParameter> eventParameters = new ArrayList<>(4);
        eventParameters.add(new Infos.EventParameter("EQP_ID", ObjectIdentifier.fetchValue(params.getEquipmentID())));
        eventParameters.add(new Infos.EventParameter("PORT_ID", ObjectIdentifier.fetchValue(params.getPortID())));
        eventParameters.add(new Infos.EventParameter("CAST_ID", ObjectIdentifier.fetchValue(params.getCassetteID())));
        eventParameters.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, params.getUser());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        fmcService.sxFMCCassetteUnclampReq(objCommon, params);

        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/fmc_mode_change/req")
    @Override
    @CimMapping(TransactionIDEnum.FMC_MODE_CHANGE_REQ)
    public Response fmcModeChangeReq(@RequestBody Params.SLMSwitchUpdateReqInParams slmSwitchUpdateReqInParams) {
        final String transactionID = TransactionIDEnum.FMC_MODE_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == slmSwitchUpdateReqInParams, retCodeConfig.getInvalidParameter());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(slmSwitchUpdateReqInParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, slmSwitchUpdateReqInParams.getUser(), accessControlCheckInqParams);

        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        fmcService.sxFMCModeChangeReq(objCommon, slmSwitchUpdateReqInParams);
        return Response.createSucc(transactionID);
    }


    @ResponseBody
    @PostMapping(value = "/fmc_rsv_max_count_update/req")
    @Override
    @CimMapping(TransactionIDEnum.FMC_RSV_MAX_COUNT_UPDATE_REQ)
    public Response fmcRsvMaxCountUpdateReq(@RequestBody Params.FmcRsvMaxCountUpdateReqInParams fmcRsvMaxCountUpdateReqInParams) {
        final String transactionID = TransactionIDEnum.FMC_RSV_MAX_COUNT_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(fmcRsvMaxCountUpdateReqInParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, fmcRsvMaxCountUpdateReqInParams.getUser(), accessControlCheckInqParams);

        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        fmcService.sxFMCRsvMaxCountUpdateReq(objCommon, fmcRsvMaxCountUpdateReqInParams);
        return Response.createSucc(transactionID);
    }
}
