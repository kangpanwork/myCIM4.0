package com.fa.cim.controller.tms;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.transferManagementSystem.ITransferManagementSystemController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.IMessageMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.constraint.IConstraintService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.system.ISystemService;
import com.fa.cim.service.tms.ITransferManagementSystemService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:05
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = ITransferManagementSystemController.class, confirmableKey = "TransferManagementSystemConfirm", cancellableKey = "TransferManagementSystemCancel")
@RequestMapping("/tms")
@Listenable
public class TransferManagementSystemController implements ITransferManagementSystemController {

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IUtilsComp utilsCompService;
    @Autowired
    private ICassetteMethod cassetteMethod;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IConstraintService constraintService;
    @Autowired
    private ISystemService systemService;
    @Autowired
    private IMessageMethod messageMethod;
    @Autowired
    private IDurableService durableService;
    @Autowired
    private ITransferManagementSystemService transferManagementSystemService;
    @ResponseBody
    @RequestMapping(value = "/carrier_reserve_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CASSETTE_RESERVE_CANCEL_REQ)
    public Response carrierReserveCancelReq(@RequestBody Params.LotCassetteReserveCancelParams params) {
        final String transactionID = TransactionIDEnum.LOT_CASSETTE_RESERVE_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        List<Infos.ReserveCancelLotCarrier> reserveCancelLotCarriers = params.getReserveCancelLotCarriers();
        Validations.check(user == null, "user can not be null");

        //Step1 - calendar_GetCurrentTimeDR
        Infos.ObjCommon objCommon = utilsCompService.setObjCommon(transactionID, user);

        //Step2 - cassette_lotIDList_GetDR
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue) && CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1")) {
            int cassetteNum = CimArrayUtils.getSize(reserveCancelLotCarriers);
            for (int i = 0; i < cassetteNum; i++) {
                Infos.LotListInCassetteInfo lotIDListGetDR = cassetteMethod.cassetteLotIDListGetDR(objCommon, reserveCancelLotCarriers.get(i).getCarrierID());
                if (null != lotIDListGetDR) {
                    List<ObjectIdentifier> lotIDList = lotIDListGetDR.getLotIDList();
                    if (lotIDList != null) {
                        lotIDs.addAll(lotIDList);
                    }
                }
            }
        }
        //Step3 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, privilegeCheckParams);

        //Step4 - txCarrierReserveCancelReq
        Results.CarrierReserveCancelReqResult carrierReserveCancelReqResult = transferManagementSystemService.sxCarrierReserveCancelReq(objCommon, params.getReserveCancelLotCarriers(), params.getClaimMemo());

        //Step5 - messageQueue_Put
        int nLen = CimArrayUtils.getSize(reserveCancelLotCarriers);
        for (int i = 0; i < nLen; i++) {
            /*--------------------------------------------------------------*/
            /*   Put Message into Message Queue for Full-Auto               */
            /*                                                              */
            /*     objMessageQueue_Put_out&  strMessageQueue_Put_out        */
            /*     const pptObjCommonIn&     strObjCommonIn                 */
            /*     const objectIdentifier&   equipmentID                    */
            /*     const char *              equipmentMode                  */
            /*     const objectIdentifier&   equipmentStatusCode            */
            /*     const objectIdentifier&   lotID                          */
            /*     const char *              lotProcessState                */
            /*     const char *              lotHoldState                   */
            /*     const objectIdentifier&   cassetteID                     */
            /*     const char *              cassetteTransferState          */
            /*     CORBA::Boolean            cassetteTransferReserveFlag    */
            /*     CORBA::Boolean            cassetteDispatchReserveFlag    */
            /*     const objectIdentifier&   durableID                      */
            /*     const char *              durableTransferState           */
            /*--------------------------------------------------------------*/
            Inputs.MessageQueuePutIn messageQueuePutIn = new Inputs.MessageQueuePutIn();
            messageQueuePutIn.setLotID(reserveCancelLotCarriers.get(i).getLotID());
            messageQueuePutIn.setCassetteDispatchReserveFlag(false);
            messageQueuePutIn.setCassetteTransferReserveFlag(false);
            messageMethod.messageQueuePut(objCommon, messageQueuePutIn);
        }


        return Response.createSucc(transactionID, carrierReserveCancelReqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_reserve/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CASSETTE_RESERVE_REQ)
    public Response carrierReserveReq(@RequestBody Params.CarrierReserveReqParam params) {
        final String transactionID = TransactionIDEnum.LOT_CASSETTE_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // Step 1: @TODO eventLog_Put


        // Step 3: txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), privilegeCheckParams);

        // Step 4: txCarrierReserveReq
        Results.CarrierReserveReqResult result = transferManagementSystemService.sxCarrierReserveReq(objCommon, params);

        // Step 5: @TODO Post-Processing - evenLog_Put;

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_transfer_job_end/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_ASSETTE_XFER_JOB_COMP_RPT)
    public Response carrierTransferJobEndRpt(@RequestBody Params.CarrierTransferJobEndRptParams carrierTransferJobEndRptParams) {
        log.info("input drbCarrierTransferJobEndRpt...  params: " + carrierTransferJobEndRptParams);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        RetCode<Object> result = new RetCode<>();
        final String txId = TransactionIDEnum.LOT_ASSETTE_XFER_JOB_COMP_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = carrierTransferJobEndRptParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsCompService.setObjCommon(txId, user);

        //Step2 - cassette_lotIDList_GetDR，cassetteLotIDList will used in txAccessControlCheckInq
        int cassetteNum = carrierTransferJobEndRptParams.getStrXferJob().size();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            for (int i = 0; i < cassetteNum; i++) {
                Infos.LotListInCassetteInfo cassetteLotIDList = cassetteMethod.cassetteLotIDListGetDR(objCommon, carrierTransferJobEndRptParams.getStrXferJob().get(i).getCarrierID());
                if (cassetteLotIDList == null) {
                    return Response.createError(objCommon.getTransactionID(), "not found ");
                }
                lotIDs = cassetteLotIDList.getLotIDList();
            }
        }
        // 【Step3】  txAccessControlCheckInq(...)
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs = new ArrayList<>();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        transferManagementSystemService.sxCarrierTransferJobEndRpt(objCommon, carrierTransferJobEndRptParams);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        log.info("exit CarrierTransferJobEndRpt...");
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_transfer_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT)
    public Response carrierTransferStatusChangeRpt(@RequestBody Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams) {
        String txId = TransactionIDEnum.EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT.getValue();

        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        User user = carrierTransferStatusChangeRptParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar
        Infos.ObjCommon objCommon = utilsCompService.setObjCommon(txId, user);
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        AtomicReference<List<ObjectIdentifier>> lotIDs = new AtomicReference<>();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            // step2 - cassette_lotIDList_GetDR
            Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteLotIDListGetDR(objCommon, carrierTransferStatusChangeRptParams.getCarrierID());
            Optional.ofNullable(lotListInCassetteInfo).ifPresent(lotListInCassetteInfo1 -> {
                lotIDs.set(lotListInCassetteInfo1.getLotIDList());
            });
        }
        //Step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(lotIDs.get());
        if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, carrierTransferStatusChangeRptParams.getXferStatus())
                || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT, carrierTransferStatusChangeRptParams.getXferStatus())) {
            accessControlCheckInqParams.setEquipmentID(carrierTransferStatusChangeRptParams.getMachineID());
        } else {
            accessControlCheckInqParams.setStockerID(carrierTransferStatusChangeRptParams.getMachineID());
        }
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //Step3 - txCarrierTransferStatusChangeRpt
        Results.CarrierTransferStatusChangeRptResult result = transferManagementSystemService.sxCarrierTransferStatusChangeRpt(objCommon, carrierTransferStatusChangeRptParams);

        //Step4 - messageQueue_Put, call message to EI
        Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
        msgQueuePut.setCassetteID(carrierTransferStatusChangeRptParams.getCarrierID());
        msgQueuePut.setCassetteTransferState(result.getStrXferLot().get(0).getTransferStatus());
        msgQueuePut.setCassetteDispatchReserveFlag(false);
        msgQueuePut.setCassetteTransferReserveFlag(false);
        messageMethod.messageQueuePut(objCommon, msgQueuePut);
        //TODO: Step5 - txInterFabXferArrivalListInq

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_transfer_job_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CASSETTE_XFER_JOB_DELETE_REQ)
    public Response carrierTransferJobDeleteReq(@RequestBody Params.CarrierTransferJobDeleteReqParam params) {
        //【Step-0】:Initialize Parameters;
        final String transactionID = TransactionIDEnum.LOT_CASSETTE_XFER_JOB_DELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        User user = params.getUser();
        Validations.check(null == user, "the user is null ...");


        //【Step-2】: cassette_lotIDList_GetDR
        log.debug("【Step-2】cassette_lotIDList_GetDR(...)");
        int cassetteNum = CimArrayUtils.getSize(params.getStrDelCarrierJob());
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (int i = 0; i < cassetteNum; i++) {
            Infos.LotListInCassetteInfo cassetteLotIDListGetDR = cassetteMethod.cassetteLotIDListGetDR(null, params.getStrDelCarrierJob().get(i).getCarrierID());
            if (null != cassetteLotIDListGetDR) {
                if (cassetteLotIDListGetDR.getLotIDList() != null) {
                    lotIDs.addAll(cassetteLotIDListGetDR.getLotIDList());
                }
            }
        }
        //【Step-3】:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step-4】:txCarrierTransferJobDeleteReq
        log.debug("【Step-4】txCarrierTransferJobDeleteReq(...)");
        transferManagementSystemService.sxLotCassetteXferDeleteReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_transfer_for_ib/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_DELIVERY_FOR_INTERNAL_BUFFER_REQ)
    public Response carrierTransferForIBReq(@RequestBody Params.CastDeliveryReqParam params) {
        //log.info(">>>>>>>> Pretending to do the cassette_delivery_req_for_internal_buffer <<<<<<<<<<");
        //【Step-0】:Initialize Parameters;
        final String transactionID = TransactionIDEnum.CASSETTE_DELIVERY_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        int rc = 0;

        //【Step-2】:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【Step-3】: txCarrierTransferForIBReq
        log.debug("【Step-3】txCarrierTransferForIBReq(...)");
        Results.CarrierTransferReqResult result = null;
        try {
            result = transferManagementSystemService.sxCarrierTransferForIBReq(objCommon, params.getEquipmentID());
        } catch (ServiceException e) {
            rc = e.getCode();
            result = e.getData(Results.CarrierTransferReqResult.class);
            if (!CimObjectUtils.isEmpty(result)){
                //when some error occur,check error sysMsg
                if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())
                        && !Validations.isEquals(retCodeConfig.getInvalidSmplSetting(), e.getCode())) {
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
                    long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
                    if (lenSysMsg > 0) {
                        int saveRc = rc;
                        for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                            String sysMsg = sysMsgStockInfo.getSystemMessageText();
                            if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                                sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                            alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                            alertMessageRptParams.setSystemMessageText(sysMsg);
                            alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                            alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                            alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                            alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                            alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                            alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                            alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                            alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                            alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                            alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                            alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                            alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                            alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        }
                        //【step5】 - txMfgRestrictReq__101
                        log.debug("【Step-3】txMfgRestrictReq__101(...)");
                        int envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getIntValue();
                        if (envInhibitWhenAPC == 1) {
                            if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                                Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                strEntityInhibitions.setEntities(entities);
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setObjectID(params.getEquipmentID());
                                entityIdentifier.setAttribution("");
                                strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                                } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                                } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                                }
                                strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                                strEntityInhibitions.setClaimedTimeStamp("");

                                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                                mfgRestrictReqParams.setUser(objCommon.getUser());
                                mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                                mfgRestrictReqParams.setClaimMemo("");

//                                //Step5 - txMfgRestrictReq__101
//                                constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                                //Step5 - txMfgRestrictReq__110
                                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                                entityInhibitDetailAttributeList.add(strEntityInhibitions);
                                mfgRestrictReq_110Params.setClaimMemo("");
                                mfgRestrictReq_110Params.setUser(objCommon.getUser());
                                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                            }
                        }
                    }
                    //roll back the cassetteDeliveryForIb
                    throw e;
                }
            }else {
                throw e;
            }
        }
        // Call System Message
        List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
        long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
        if (lenSysMsg > 0) {
            int saveRc = rc;
            for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                String sysMsg = sysMsgStockInfo.getSystemMessageText();
                if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                    sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                }
                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                alertMessageRptParams.setSystemMessageText(sysMsg);
                alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            }
            //【step5】 - txMfgRestrictReq__101
            log.debug("【Step-3】txMfgRestrictReq__101(...)");
            int envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getIntValue();
            if (envInhibitWhenAPC == 1) {
                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                        Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                        Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                        Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                        Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                    Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    strEntityInhibitions.setEntities(entities);
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                    entityIdentifier.setObjectID(params.getEquipmentID());
                    entityIdentifier.setAttribution("");
                    strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                            || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                        strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                    } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                            || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                        strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                    } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                        strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                    }
                    strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                    strEntityInhibitions.setClaimedTimeStamp("");

                    Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                    mfgRestrictReqParams.setUser(objCommon.getUser());
                    mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                    mfgRestrictReqParams.setClaimMemo("");

//                    //Step5 - txMfgRestrictReq__101
//                    constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                    //Step5 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(strEntityInhibitions);
                    mfgRestrictReq_110Params.setClaimMemo("");
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                }
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_transfer/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_DELIVERY_REQ)
    public Response carrierTransferReq(@RequestBody Params.CastDeliveryReqParam params) {
        //log.info(">>>>>>>> Pretending to do the cassette_delivery_req <<<<<<<<<<");
        //return Response.createSucc(TransactionIDEnum.CASSETTE_DELIVERY_REQ.getValue(), null);
        //【Step-0】:Initialize Parameters;
        final String transactionID = TransactionIDEnum.CASSETTE_DELIVERY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        int rc = 0;

        //【Step-2】:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【Step-3】: txCarrierTransferReq
        log.debug("【Step-3】txCarrierTransferReq(...)");
        Results.CarrierTransferReqResult result = null;
        try {
            result = transferManagementSystemService.sxCarrierTransferReq(objCommon, params.getEquipmentID());
        } catch (ServiceException e) {
            rc = e.getCode();
            result = e.getData(Results.CarrierTransferReqResult.class);
            if (!CimObjectUtils.isEmpty(result)){
                if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())
                        && !Validations.isEquals(retCodeConfig.getInvalidSmplSetting(), e.getCode())) {
                    //when some error occur,check error sysMsg
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
                    long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
                    if (lenSysMsg > 0) {
                        int saveRc = rc;
                        for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                            String sysMsg = sysMsgStockInfo.getSystemMessageText();
                            if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                                sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                            alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                            alertMessageRptParams.setSystemMessageText(sysMsg);
                            alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                            alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                            alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                            alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                            alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                            alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                            alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                            alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                            alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                            alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                            alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                            alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                            alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        }
                        //【step5】 - txMfgRestrictReq__101
                        log.debug("【Step-3】txMfgRestrictReq__101(...)");
                        String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                        Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                        if (envInhibitWhenAPC == 1) {
                            if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                                Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                strEntityInhibitions.setEntities(entities);
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setObjectID(params.getEquipmentID());
                                entityIdentifier.setAttribution("");
                                strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                                } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                                } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                                }
                                strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                                strEntityInhibitions.setClaimedTimeStamp("");

                                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                                mfgRestrictReqParams.setUser(objCommon.getUser());
                                mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                                mfgRestrictReqParams.setClaimMemo("");

//                                //Step5 - txMfgRestrictReq__101
//                                constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                                //Step5 - txMfgRestrictReq__110
                                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                                entityInhibitDetailAttributeList.add(strEntityInhibitions);
                                mfgRestrictReq_110Params.setClaimMemo("");
                                mfgRestrictReq_110Params.setUser(objCommon.getUser());
                                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                            }
                        }
                    }
                    //roll back the cassetteDelivery
                    throw e;
                }
            }else {
                throw e;
            }
        }
        //when success check error sysMsg
        if (!CimObjectUtils.isEmpty(result)){
            List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
            long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
            if (lenSysMsg > 0) {
                int saveRc = rc;
                for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                    String sysMsg = sysMsgStockInfo.getSystemMessageText();
                    if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                        sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                    }
                    Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                    alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                    alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                    alertMessageRptParams.setSystemMessageText(sysMsg);
                    alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                    alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                    alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                    alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                    alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                    alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                    alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                    alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                    alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                    alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                    alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                    alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                    alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                    systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                }
                //【step5】 - txMfgRestrictReq__101
                log.debug("【Step-3】txMfgRestrictReq__101(...)");
                String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                if (envInhibitWhenAPC == 1) {
                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                        Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                        List<Infos.EntityIdentifier> entities = new ArrayList<>();
                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                        strEntityInhibitions.setEntities(entities);
                        entities.add(entityIdentifier);
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                        entityIdentifier.setObjectID(params.getEquipmentID());
                        entityIdentifier.setAttribution("");
                        strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                        if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                        } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                        } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                        }
                        strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                        strEntityInhibitions.setClaimedTimeStamp("");

                        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                        mfgRestrictReqParams.setUser(objCommon.getUser());
                        mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                        mfgRestrictReqParams.setClaimMemo("");

//                        //Step5 - txMfgRestrictReq__101
//                        constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                        //Step5 - txMfgRestrictReq__110
                        Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                        entityInhibitDetailAttributeList.add(strEntityInhibitions);
                        mfgRestrictReq_110Params.setClaimMemo("");
                        mfgRestrictReq_110Params.setUser(objCommon.getUser());
                        mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                        constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                    }
                }
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/fmc_carrier_transfer/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FMC_CASSETTE_DELIVERY_REQ)
    public Response fmcCarrierTransferReq(@RequestBody Params.CastDeliveryReqParam params) {

        //【Step-1】:Initialize Parameters;
        final String transactionID = TransactionIDEnum.FMC_CASSETTE_DELIVERY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        int rc = 0;

        //【Step-2】:checkPrivilegeAndGetObjCommon;
        log.debug("【Step-2】checkPrivilegeAndGetObjCommon(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【Step-3】: FMCCarrierTransferReq
        log.debug("【Step-3】FMCCarrierTransferReq(...)");
        Results.CarrierTransferReqResult result = null;
        try {
            result = transferManagementSystemService.sxFMCCarrierTransferReq(objCommon, params.getEquipmentID());
        } catch (ServiceException e) {
            rc = e.getCode();
            result = e.getData(Results.CarrierTransferReqResult.class);
            if (!CimObjectUtils.isEmpty(result)){
                if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())
                        && !Validations.isEquals(retCodeConfig.getInvalidSmplSetting(), e.getCode())) {
                    //when some error occur,check error sysMsg
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
                    long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
                    if (lenSysMsg > 0) {
                        int saveRc = rc;
                        for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                            String sysMsg = sysMsgStockInfo.getSystemMessageText();
                            if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                                sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                            alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                            alertMessageRptParams.setSystemMessageText(sysMsg);
                            alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                            alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                            alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                            alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                            alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                            alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                            alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                            alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                            alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                            alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                            alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                            alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                            alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                            //【Step-4】: AlertMessageRpt
                            log.debug("【Step-4】AlertMessageRpt(...)");
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        }
                        //【step5】 - MfgRestrictReq
                        log.debug("【Step-5】MfgRestrictReq(...)");
                        String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                        Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                        if (envInhibitWhenAPC == 1) {
                            if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                                Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                strEntityInhibitions.setEntities(entities);
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setObjectID(params.getEquipmentID());
                                entityIdentifier.setAttribution("");
                                strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                                } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                                } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                                }
                                strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                                strEntityInhibitions.setClaimedTimeStamp("");

                                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                                mfgRestrictReqParams.setUser(objCommon.getUser());
                                mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                                mfgRestrictReqParams.setClaimMemo("");

//                                constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                                //Step5 - txMfgRestrictReq__110
                                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                                entityInhibitDetailAttributeList.add(strEntityInhibitions);
                                mfgRestrictReq_110Params.setClaimMemo("");
                                mfgRestrictReq_110Params.setUser(objCommon.getUser());
                                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                            }
                        }
                    }
                    //roll back the cassetteDelivery
                    throw e;
                }
            }else {
                throw e;
            }
        }
        //when success check error sysMsg
        if (!CimObjectUtils.isEmpty(result)){
            List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
            long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
            if (lenSysMsg > 0) {
                int saveRc = rc;
                for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                    String sysMsg = sysMsgStockInfo.getSystemMessageText();
                    if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                        sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                    }
                    Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                    alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                    alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                    alertMessageRptParams.setSystemMessageText(sysMsg);
                    alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                    alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                    alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                    alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                    alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                    alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                    alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                    alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                    alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                    alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                    alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                    alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                    alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                    //【Step-4】: AlertMessageRpt
                    log.debug("【Step-4】AlertMessageRpt(...)");
                    systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                }
                //【step5】 - MfgRestrictReq
                log.debug("【Step-5】MfgRestrictReq(...)");
                String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                if (envInhibitWhenAPC == 1) {
                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                        Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                        List<Infos.EntityIdentifier> entities = new ArrayList<>();
                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                        strEntityInhibitions.setEntities(entities);
                        entities.add(entityIdentifier);
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                        entityIdentifier.setObjectID(params.getEquipmentID());
                        entityIdentifier.setAttribution("");
                        strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                        if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                        } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                        } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                        }
                        strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                        strEntityInhibitions.setClaimedTimeStamp("");

                        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                        mfgRestrictReqParams.setUser(objCommon.getUser());
                        mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                        mfgRestrictReqParams.setClaimMemo("");

//                        constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                        //Step5 - txMfgRestrictReq__110
                        Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                        entityInhibitDetailAttributeList.add(strEntityInhibitions);
                        mfgRestrictReq_110Params.setClaimMemo("");
                        mfgRestrictReq_110Params.setUser(objCommon.getUser());
                        mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                        constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                    }
                }
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/single_carrier_transfer/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SINGLE_CARRIER_XFER_REQ)
    public Response singleCarrierTransferReq(@RequestBody Params.SingleCarrierTransferReqParam params) {

        final String transactionID = TransactionIDEnum.SINGLE_CARRIER_XFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // Step 2: txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), privilegeCheckParams);

        // Step 3: txSingleCarrierTransferReq
        Results.SingleCarrierTransferReqResult rc = transferManagementSystemService.sxSingleCarrierTransferReq(objCommon, params);

        return Response.createSucc(transactionID, rc);
    }

    @ResponseBody
    @RequestMapping(value = "/multiple_carrier_transfer/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MULTI_CARRIER_XFER_REQ)
    public Response multipleCarrierTransferReq(@RequestBody Params.MultipleCarrierTransferReqParam params) {
        final String transactionID = TransactionIDEnum.MULTI_CARRIER_XFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        //【Step-1】:txCalendar_GetCurrentTimeDR;
        log.debug("【Step-1】txCalendar_GetCurrentTimeDR(...)");
        Infos.ObjCommon objCommon = utilsCompService.setObjCommon(transactionID, user);

        // Step 2: txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        privilegeCheckParams.setLotIDLists(lotIDs);
        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        List<Infos.CarrierXferReq> strCarrierXferReq = params.getStrCarrierXferReq();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue) && CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1")) {
            int cassetteNum = CimArrayUtils.getSize(strCarrierXferReq);
            for (int i = 0; i < cassetteNum; i++) {
                Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteLotIDListGetDR(objCommon, strCarrierXferReq.get(i).getCarrierID());
                Optional.ofNullable(lotListInCassetteInfo).ifPresent(lotListInCassetteInfo1 -> {
                    if (!CimArrayUtils.isEmpty(lotListInCassetteInfo1.getLotIDList())) {
                        lotIDs.addAll(lotListInCassetteInfo1.getLotIDList());
                    }
                });
            }
        }
        accessInqService.sxAccessControlCheckInq(objCommon, privilegeCheckParams);

        // Step 3: txMultipleCarrierTransferReq
        transferManagementSystemService.sxMultipleCarrierTransferReq(objCommon, params.isRerouteFlag(), params.getTransportType(), params.getStrCarrierXferReq());
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/durable_transfer_job_status/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DUARBLE_XFER_JOB_STATUS_RPT)
    public Response durableTransferJobStatusRpt(@RequestBody Params.DurableTransferJobStatusRptParams params) {
        //======Pre Process======
        String txId = TransactionIDEnum.DUARBLE_XFER_JOB_STATUS_RPT.getValue();

        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step3 - txDurableTransferJobStatusRpt
        durableService.sxDurableTransferJobStatusRpt(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/stocker_inventory_upload/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STOCKER_INVENTORY_REQ)
    public Response stockerInventoryUploadReq(@RequestBody Params.StockerInventoryUploadReqParam params) {
        final String transactionID = TransactionIDEnum.STOCKER_INVENTORY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step3 - txStockerInventoryUploadReq
        Results.StockerInventoryUploadReqResult result = transferManagementSystemService.sxStockerInventoryUploadReq(objCommon, params.getMachineID(), params.getShelfPosition(), params.getClaimMemo());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/stocker_inventory/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STOCKER_INVENTORY_RPT)
    public Response stockerInventoryRpt(@RequestBody Params.StockerInventoryRptParam params) {
        final String transactionID = TransactionIDEnum.STOCKER_INVENTORY_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step3 - txStockerInventoryUploadReq
        Results.StockerInventoryRptResult result = transferManagementSystemService.sxStockerInventoryRpt(objCommon, params.getStockerID(), params.getShelfPosition(), params.getInventoryLotInfos(), params.getClaimMemo());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/stocker_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STOCKER_STATUS_CHANGE_RPT)
    public Response stockerStatusChangeRpt(@RequestBody Params.StockerStatusChangeRptParams params) {
        final String transactionID = TransactionIDEnum.STOCKER_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setStockerID(params.getStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step3 - StockerStatusChangeRptService
        ObjectIdentifier stockerID = transferManagementSystemService.sxStockerStatusChangeRpt(objCommon, params.getStockerID(), params.getStockerStatusCode(), params.getClaimMemo());
        return Response.createSucc(transactionID, stockerID);
    }

    @ResponseBody
    @RequestMapping(value = "/npw_carrier_reserve_for_ib/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_FOR_INTERNAL_BUFFER_REQ)
    public Response npwCarrierReserveForIBReq(@RequestBody Params.NPWCarrierReserveForIBReqParams params) {
        final String transactionID = TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        List<Infos.StartCassette> strStartCassette = params.getStartCassetteList();
        accessControlCheckInqParams.setLotIDLists(strStartCassette.stream().flatMap(x -> x.getLotInCassetteList().stream().map(Infos.LotInCassette::getLotID)).collect(Collectors.toList()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxNPWCarrierReserveForIBReq");
        transferManagementSystemService.sxNPWCarrierReserveForIBReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/npw_carrier_reserve/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_REQ)
    public Response npwCarrierReserveReq(@RequestBody Params.NPWCarrierReserveReqParams params) {
        final String transactionID = TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("call txAccessControlCheckInq(...) and get schedule from calendar");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        List<Infos.StartCassette> strStartCassette = params.getStartCassetteList();
        accessControlCheckInqParams.setLotIDLists(strStartCassette.stream().flatMap(x -> x.getLotInCassetteList().stream().map(Infos.LotInCassette::getLotID)).collect(Collectors.toList()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("call sxNPWCarrierReserveReq");
        transferManagementSystemService.sxNPWCarrierReserveReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/npw_carrier_reserve_cancel_for_ib/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_FOR_INTERNAL_BUFFER_REQ)
    public Response npwCarrierReserveCancelForIBReq(@RequestBody Params.NPWCarrierReserveCancelReqParm params) {
        //【step1】 - init params
        final String txId = TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue();
        log.info("NPWCarrierReserveCancelReqParm : {}", params);
        User user = params.getUser();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);


        String tmpPrivilegeCheckCASTValue = StandardProperties.OM_ACCESS_CHECK_FOR_CARRIER.getValue();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        if ((!CimStringUtils.isEmpty(tmpPrivilegeCheckCASTValue)) && (CimStringUtils.equals(tmpPrivilegeCheckCASTValue, "1"))) {
            //【step2】 - cassetteLotIDListGetDR
            for (int i = 0; i < CimArrayUtils.getSize(params.getNpwTransferCassetteList()); i++) {
                Infos.LotListInCassetteInfo cassetteLotIDList = cassetteMethod.cassetteLotIDListGetDR(objCommon, params.getNpwTransferCassetteList().get(i).getCassetteID());
                if (CimObjectUtils.isEmpty(cassetteLotIDList)) {
                    return Response.createError(objCommon.getTransactionID(), "not found");
                }
                lotIDs = cassetteLotIDList.getLotIDList();
            }
        }

        //【step3】 -  call sxAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        //【step4】 - call sxNPWCarrierReserveCancelForIBReq(...)
        transferManagementSystemService.sxNPWCarrierReserveCancelForIBReq(objCommon, params.getEquipmentID(), params.getPortGroup(), params.getNpwTransferCassetteList(), params.getNotifyToEAPFlag(), params.getOpeMemo());
        Response response = Response.createSucc(txId, null);

        //【step5】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/npw_carrier_reserve_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_REQ)
    public Response npwCarrierReserveCancelReq(@RequestBody Params.NPWCarrierReserveCancelReqParm params) {
        // init params
        final String txId = TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_REQ.getValue();
        log.info("NPWCarrierReserveCancelReqParm : {}", params);
        User user = params.getUser();

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        transferManagementSystemService.sxNPWCarrierReserveCancelReq(objCommon, params.getEquipmentID(), params.getPortGroup(), params.getNpwTransferCassetteList(), params.getNotifyToEAPFlag(), params.getOpeMemo());

        Response response = Response.createSucc(txId, null);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/dms_transfer/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DMS_DELIVERY_REQ)
    public Response dmsTransferReq(@RequestBody Params.CastDeliveryReqParam params) {
        //【Step-1】:Initialize Parameters;
        final String transactionID = TransactionIDEnum.DMS_DELIVERY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        int rc = 0;
        //【Step-2】:txAccessControlCheckInq;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【Step-3】: sxDurableTransferReq
        Results.CarrierTransferReqResult result = null;
        try {
            result = transferManagementSystemService.sxDmsTransferReq(objCommon, params.getEquipmentID());
        } catch (ServiceException e) {
            rc = e.getCode();
            result = e.getData(Results.CarrierTransferReqResult.class);
            if (!CimObjectUtils.isEmpty(result)){
                if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())
                        && !Validations.isEquals(retCodeConfig.getInvalidSmplSetting(), e.getCode())) {
                    //when some error occur,check error sysMsg
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
                    long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
                    if (lenSysMsg > 0) {
                        int saveRc = rc;
                        for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                            String sysMsg = sysMsgStockInfo.getSystemMessageText();
                            if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                                sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                            alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                            alertMessageRptParams.setSystemMessageText(sysMsg);
                            alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                            alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                            alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                            alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                            alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                            alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                            alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                            alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                            alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                            alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                            alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                            alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                            alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                            //【step4】 - sxAlertMessageRpt
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        }
                        //【step5】 - sxMfgRestrictReq
                        String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                        Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                        if (envInhibitWhenAPC == 1) {
                            if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                                Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                strEntityInhibitions.setEntities(entities);
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setObjectID(params.getEquipmentID());
                                entityIdentifier.setAttribution("");
                                strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                                } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                                } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                                }
                                strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                                strEntityInhibitions.setClaimedTimeStamp("");

                                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                                mfgRestrictReqParams.setUser(objCommon.getUser());
                                mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                                mfgRestrictReqParams.setClaimMemo("");

//                                constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                                //Step5 - txMfgRestrictReq__110
                                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                                entityInhibitDetailAttributeList.add(strEntityInhibitions);
                                mfgRestrictReq_110Params.setClaimMemo("");
                                mfgRestrictReq_110Params.setUser(objCommon.getUser());
                                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                            }
                        }
                    }
                    //roll back the cassetteDelivery
                    throw e;
                }
            }else {
                throw e;
            }
        }
        //when success check error sysMsg
        if (!CimObjectUtils.isEmpty(result)){
            List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
            long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
            if (lenSysMsg > 0) {
                int saveRc = rc;
                for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                    String sysMsg = sysMsgStockInfo.getSystemMessageText();
                    if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                        sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                    }
                    Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                    alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                    alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                    alertMessageRptParams.setSystemMessageText(sysMsg);
                    alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                    alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                    alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                    alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                    alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                    alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                    alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                    alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                    alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                    alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                    alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                    alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                    alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                    //【step6】 - sxAlertMessageRpt
                    systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                }
                //【step7】 - sxMfgRestrictReq
                String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                if (envInhibitWhenAPC == 1) {
                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                        Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                        List<Infos.EntityIdentifier> entities = new ArrayList<>();
                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                        strEntityInhibitions.setEntities(entities);
                        entities.add(entityIdentifier);
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                        entityIdentifier.setObjectID(params.getEquipmentID());
                        entityIdentifier.setAttribution("");
                        strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                        if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                        } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                        } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                        }
                        strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                        strEntityInhibitions.setClaimedTimeStamp("");

                        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                        mfgRestrictReqParams.setUser(objCommon.getUser());
                        mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                        mfgRestrictReqParams.setClaimMemo("");

//                        constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                        //Step5 - txMfgRestrictReq__110
                        Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                        entityInhibitDetailAttributeList.add(strEntityInhibitions);
                        mfgRestrictReq_110Params.setClaimMemo("");
                        mfgRestrictReq_110Params.setUser(objCommon.getUser());
                        mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                        constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                    }
                }
            }
        }
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/dms_transfer_for_ib/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DMS_DELIVERY_REQ_FOR_INTERNAL_BUFFER_REQ)
    public Response dmsTransferForIBReq(@RequestBody Params.CastDeliveryReqParam params) {
        //【Step-1】:Initialize Parameters;
        final String transactionID = TransactionIDEnum.DMS_DELIVERY_REQ_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        int rc = 0;
        //【Step-2】:txAccessControlCheckInq;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【Step-3】: sxDmsTransferForIBReq
        Results.CarrierTransferReqResult result = null;
        try {
            result = transferManagementSystemService.sxDmsTransferForIBReq(objCommon, params.getEquipmentID());
        } catch (ServiceException e) {
            rc = e.getCode();
            result = e.getData(Results.CarrierTransferReqResult.class);
            if (!CimObjectUtils.isEmpty(result)){
                if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())
                        && !Validations.isEquals(retCodeConfig.getInvalidSmplSetting(), e.getCode())) {
                    //when some error occur,check error sysMsg
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
                    long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
                    if (lenSysMsg > 0) {
                        int saveRc = rc;
                        for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                            String sysMsg = sysMsgStockInfo.getSystemMessageText();
                            if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                                sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                            }
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                            alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                            alertMessageRptParams.setSystemMessageText(sysMsg);
                            alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                            alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                            alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                            alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                            alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                            alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                            alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                            alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                            alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                            alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                            alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                            alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                            alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                            //【step4】 - sxAlertMessageRpt
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        }
                        //【step5】 - sxMfgRestrictReq
                        String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                        Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                        if (envInhibitWhenAPC == 1) {
                            if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                                    Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                                Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                strEntityInhibitions.setEntities(entities);
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setObjectID(params.getEquipmentID());
                                entityIdentifier.setAttribution("");
                                strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                                } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                        || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                                } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                                    strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                                }
                                strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                                strEntityInhibitions.setClaimedTimeStamp("");

                                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                                mfgRestrictReqParams.setUser(objCommon.getUser());
                                mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                                mfgRestrictReqParams.setClaimMemo("");

//                                constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                                //Step5 - txMfgRestrictReq__110
                                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                                entityInhibitDetailAttributeList.add(strEntityInhibitions);
                                mfgRestrictReq_110Params.setClaimMemo("");
                                mfgRestrictReq_110Params.setUser(objCommon.getUser());
                                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                            }
                        }
                    }
                    //roll back the cassetteDelivery
                    throw e;
                }
            }else {
                throw e;
            }
        }
        //when success check error sysMsg
        if (!CimObjectUtils.isEmpty(result)){
            List<Infos.SysMsgStockInfo> sysMstStockInfoList = result.getSysMstStockInfoList();
            long lenSysMsg = CimArrayUtils.getSize(sysMstStockInfoList);
            if (lenSysMsg > 0) {
                int saveRc = rc;
                for (Infos.SysMsgStockInfo sysMsgStockInfo : sysMstStockInfoList) {
                    String sysMsg = sysMsgStockInfo.getSystemMessageText();
                    if (CimStringUtils.length(sysMsgStockInfo.getSystemMessageText()) > 1020) {
                        sysMsg = sysMsgStockInfo.getSystemMessageText().substring(0, 1020) + "...";
                    }
                    Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                    alertMessageRptParams.setSubSystemID(sysMsgStockInfo.getSubSystemID());
                    alertMessageRptParams.setSystemMessageCode(sysMsgStockInfo.getSystemMessageCode());
                    alertMessageRptParams.setSystemMessageText(sysMsg);
                    alertMessageRptParams.setNotifyFlag(sysMsgStockInfo.getNotifyFlag());
                    alertMessageRptParams.setEquipmentID(sysMsgStockInfo.getEquipmentID());
                    alertMessageRptParams.setEquipmentStatus(sysMsgStockInfo.getEquipmentStatus());
                    alertMessageRptParams.setStockerID(sysMsgStockInfo.getStockerID());
                    alertMessageRptParams.setStockerStatus(sysMsgStockInfo.getStockerStatus());
                    alertMessageRptParams.setAGVID(sysMsgStockInfo.getAGVID());
                    alertMessageRptParams.setAGVStatus(sysMsgStockInfo.getAGVStatus());
                    alertMessageRptParams.setLotID(sysMsgStockInfo.getLotID());
                    alertMessageRptParams.setLotStatus(sysMsgStockInfo.getLotStatus());
                    alertMessageRptParams.setRouteID(sysMsgStockInfo.getRouteID());
                    alertMessageRptParams.setOperationID(sysMsgStockInfo.getOperationID());
                    alertMessageRptParams.setOperationNumber(sysMsgStockInfo.getOperationNumber());
                    alertMessageRptParams.setSystemMessageTimeStamp(sysMsgStockInfo.getSystemMessageTimeStamp());
                    //【step6】 - sxAlertMessageRpt
                    systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                }
                //【step7】 - sxMfgRestrictReq
                String cimEnvironmentVariableBO = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
                Long envInhibitWhenAPC = Long.valueOf(cimEnvironmentVariableBO);
                if (envInhibitWhenAPC == 1) {
                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc) ||
                            Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {

                        Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                        List<Infos.EntityIdentifier> entities = new ArrayList<>();
                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                        strEntityInhibitions.setEntities(entities);
                        entities.add(entityIdentifier);
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                        entityIdentifier.setObjectID(params.getEquipmentID());
                        entityIdentifier.setAttribution("");
                        strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                        if (Validations.isEquals(retCodeConfig.getNoResponseApc(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcServerBindFail(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                        } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), saveRc)
                                || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                        } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), saveRc)) {
                            strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                        }
                        strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                        strEntityInhibitions.setClaimedTimeStamp("");

                        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                        mfgRestrictReqParams.setUser(objCommon.getUser());
                        mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
                        mfgRestrictReqParams.setClaimMemo("");

//                        constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                        //Step5 - txMfgRestrictReq__110
                        Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                        entityInhibitDetailAttributeList.add(strEntityInhibitions);
                        mfgRestrictReq_110Params.setClaimMemo("");
                        mfgRestrictReq_110Params.setUser(objCommon.getUser());
                        mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                        constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                    }
                }
            }
        }
        return Response.createSucc(transactionID, result);
    }
}
