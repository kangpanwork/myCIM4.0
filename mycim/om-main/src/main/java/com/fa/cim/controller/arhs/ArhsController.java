package com.fa.cim.controller.arhs;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.arhs.IArhsController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IMessageMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.arhs.IArhsInqService;
import com.fa.cim.service.arhs.IArhsService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IArhsController.class, confirmableKey = "ArhsConfrim", cancellableKey = "ArhsCancel")
@RequestMapping("/arhs")
@Listenable
public class ArhsController implements IArhsController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IArhsService arhsService;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private ISystemService systemService;
    @Autowired
    private IMessageMethod messageMethod;
    @Autowired
    private IArhsInqService arhsInqService;

    @RequestMapping(value = "/reticle_dispatch_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.ASYNC_RETICLE_XFER_JOB_CREATE_REQ)
    @Override
    public Response reticleDispatchJobCreateReq(@RequestBody Params.AsyncReticleXferJobCreateReqParams params) {
        String txId = TransactionIDEnum.ASYNC_RETICLE_XFER_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());

        ObjectIdentifier equipmentID = null;
        ObjectIdentifier stockerID = null;
        String machineType = null;
        try {
            machineType = equipmentMethod.equipmentGetTypeDR(objCommon, params.getToMachineID());
            log.debug("toMachineID is equipmentID.");
            equipmentID = params.getToMachineID();
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                log.debug("toMachineID is stockerID.");
                stockerID = params.getToMachineID();
            } else {
                throw e;
            }
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        arhsService.sxAsyncReticleXferJobCreateReq(objCommon, params);
        return Response.createSucc(txId, null);
    }


    @RequestMapping(value = "/rpod_xfer_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RSP_XFER_STATUS_CHANGE_RPT)
    @Override
    public Response rpodXferStatusChangeRpt(@RequestBody Params.RSPXferStatusChangeRptParams params) {
        String txId = TransactionIDEnum.RSP_XFER_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        ObjectIdentifier equipmentID = null, stockerID = null;
        ObjectIdentifier machineID = params.getMachineID();
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, machineID);
            equipmentID = machineID;
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEqp(), ex.getCode())) {
                throw ex;
            }
            stockerID = machineID;
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);
        if (!ObjectIdentifier.isEmpty(machineID)) {
            if (CimStringUtils.equals(params.getXferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                /*--------------------------------------------------*/
                /*   Put Message into Message Queue for Full-Auto   */
                /*--------------------------------------------------*/
                log.info("call messageQueue_Put()");
                Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
                msgQueuePut.setEquipmentID(machineID);
                msgQueuePut.setCassetteDispatchReserveFlag(false);
                msgQueuePut.setCassetteTransferReserveFlag(false);
                messageMethod.messageQueuePut(objCommon, msgQueuePut);
            }
        }

        return Response.createSucc(txId, arhsService.sxRSPXferStatusChangeRpt(objCommon, params));
    }

    @RequestMapping(value = "/reticle_compt_job_create_err_hdlr/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_ACTION_RELEASE_ERROR_REQ)
    @Override
    public Response reticleComptJobCreateErrHdlrReq(@RequestBody Params.ReticleActionReleaseErrorReqParams params) {
        String txId = TransactionIDEnum.RETICLE_ACTION_RELEASE_ERROR_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());

        ObjectIdentifier equipmentID = null;
        ObjectIdentifier stockerID = null;
        String machineType = null;
        try {
            machineType = equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            log.info("machineID is equipmentID.");
            equipmentID = params.getMachineID();
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                log.info("machineID is stockerID.");
                stockerID = params.getMachineID();
            } else {
                throw e;
            }
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        arhsService.sxReticleActionReleaseErrorReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_compt_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_ACTION_RELEASE_REQ)
    @Override
    public Response reticleComptJobCreateReq(@RequestBody Params.ReticleActionReleaseReqParams params) {
        String txId = TransactionIDEnum.RETICLE_ACTION_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        Infos.ReticleXferJob reticleXferJob = null;
        try {
            reticleXferJob = arhsService.sxReticleActionReleaseReq(objCommon, params);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfigEx.getInvalidRdjRecord(), e.getCode())) {
                Params.ReticleDispatchJobCancelReqParams reticleDispatchJobCancelReqParams = new Params.ReticleDispatchJobCancelReqParams();
                reticleDispatchJobCancelReqParams.setDispatchJobID(params.getReticleDispatchJobID());
                reticleDispatchJobCancelReqParams.setOpeMemo(params.getOpeMemo());
                arhsService.sxReticleDispatchJobCancelReq(objCommon, reticleDispatchJobCancelReqParams);

                StringBuilder sb = new StringBuilder();
                sb.append("Transaction ID      : ").append(objCommon.getTransactionID()).append(".\n")
                        .append("Return Code         : ").append(retCodeConfigEx.getArhsCannotCreateRcj().getCode()).append(".\n")
                        .append("Message ID          : .\n")
                        .append("Message Text        : ").append(retCodeConfigEx.getArhsCannotCreateRcj().getMessage()).append(".\n")
                        .append("Reason Text         : Request System Engineer to do problem determination and analysis of Reticle Action watchdog trace log.\n")
                        .append("------------------------------------------------------------.\n")
                        .append("RDJ ID              : ").append(params.getReticleDispatchJobID()).append(".\n")
                        .append("From Machine ID     : .\n")
                        .append("From Machine Port   : .\n")
                        .append("To Machine ID       : .\n")
                        .append("To Machine Port     : .\n")
                        .append("Reticle Pod ID      : .\n")
                        .append("Reticle ID          : .\n");
                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_ARMSERROR);
                alertMessageRptParams.setSystemMessageText(sb.toString());
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);

            } else if (Validations.isEquals(retCodeConfigEx.getRdjStatusError(), e.getCode())) {
                throw e;
            } else {
                ObjectIdentifier fromEquipmentID = null;
                if (reticleXferJob != null) {
                    fromEquipmentID = reticleXferJob.getFromEquipmentID();
                }
                Params.ReticleActionReleaseErrorReqParams reticleActionReleaseErrorReqParams = new Params.ReticleActionReleaseErrorReqParams();
                reticleActionReleaseErrorReqParams.setReticleDispatchJobID(params.getReticleDispatchJobID());
                reticleActionReleaseErrorReqParams.setMachineID(fromEquipmentID);
                arhsService.sxReticleActionReleaseErrorReq(objCommon, reticleActionReleaseErrorReqParams);
            }
        }

        return Response.createSucc(txId, reticleXferJob);
    }

    @RequestMapping(value = "/reticle_dispatch_job_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_DISPATCH_JOB_CANCEL_REQ)
    @Override
    public Response reticleDispatchJobCancelReq(@RequestBody Params.ReticleDispatchJobCancelReqParams params) {
        String txId = TransactionIDEnum.RETICLE_DISPATCH_JOB_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticleDispatchJobCancelReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_compt_job_retry/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_COMPONENT_JOB_RETRY_REQ)
    @Override
    public Response reticleComptJobRetryReq(@RequestBody Params.ReticleComponentJobRetryReqParams params) {
        String txId = TransactionIDEnum.RETICLE_COMPONENT_JOB_RETRY_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticleComponentJobRetryReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_compt_job_skip/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_COMPONENT_JOB_SKIP_REQ)
    @Override
    public Response reticleComptJobSkipReq(@RequestBody Params.ReticleComponentJobSkipReqParams params) {
        String txId = TransactionIDEnum.RETICLE_COMPONENT_JOB_SKIP_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticleComponentJobSkipReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_dispatch_job_delete/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_DISPATCH_JOB_DELETE_REQ)
    @Override
    public Response reticleDispatchJobDeleteReq(@RequestBody Params.ReticleDispatchJobDeleteReqParams params) {
        String txId = TransactionIDEnum.RETICLE_DISPATCH_JOB_DELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticleDispatchJobDeleteReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_dispatch_job_add/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_DISPATCH_JOB_INSERT_REQ)
    @Override
    public Response reticleDispatchJobAddReq(@RequestBody Params.ReticleDispatchJobInsertReqParams params) {
        String txId = TransactionIDEnum.RETICLE_DISPATCH_JOB_INSERT_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticleDispatchJobInsertReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_retrieve_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_RETRIEVE_JOB_CREATE_REQ)
    @Override
    public Response reticleRetrieveJobCreateReq(@RequestBody Params.ReticleRetrieveJobCreateReqParams params) {
        String txId = TransactionIDEnum.RETICLE_RETRIEVE_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());

        ObjectIdentifier equipmentID = null;
        ObjectIdentifier stockerID = null;
        String machineType = null;
        try {
            machineType = equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            log.info("machineID is equipmentID.");
            equipmentID = params.getMachineID();
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                log.info("machineID is stockerID.");
                stockerID = params.getMachineID();
            } else {
                throw e;
            }
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        arhsService.sxReticleRetrieveJobCreateReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_store_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_STORE_JOB_CREATE_REQ)
    @Override
    public Response reticleStoreJobCreateReq(@RequestBody Params.ReticleStoreJobCreateReqParams params) {
        String txId = TransactionIDEnum.RETICLE_STORE_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        ObjectIdentifier equipmentID = null, stockerID = null;
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            equipmentID = params.getMachineID();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEqp(), ex.getCode())) {
                throw ex;
            }
            stockerID = params.getMachineID();
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        arhsService.sxReticleStoreJobCreateReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/rpod_xfer/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_XFER_REQ)
    @Override
    public Response rpodXferReq(@RequestBody Params.ReticlePodXferReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_XFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        ObjectIdentifier equipmentID = null, stockerID = null;
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, params.getToMachineID());
            equipmentID = params.getToMachineID();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEqp(), ex.getCode())) {
                throw ex;
            }
            stockerID = params.getToMachineID();
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        Results.ReticlePodXferReqResult result = null;
        try {
            result = arhsService.sxReticlePodXferReq(objCommon, params);
        } catch (ServiceException e) {
            Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
            RetCode<Object> retCode = new RetCode<>();
            retCode.setReturnCode(new OmCode(e.getCode(),e.getMessage()));
            retCode.setMessageID(CimObjectUtils.toString(e.getCode()));
            retCode.setTransactionID(e.getTransactionID());
            retCode.setMessageText(e.getMessage());
            retCode.setReasonText(e.getReasonText());
            reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
            reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(true);
            reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID(params.getReticleDispatchJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID(params.getReticleComponentJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_XFER);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(null);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(params.getReticlePodID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(params.getFromMachineID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(params.getFromPortID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(params.getToMachineID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(params.getToPortID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getClaimMemo());
            arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon,reticleDispatchAndComponentJobStatusChangeReqParams);
        }
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/reticle_delivery_status_change/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_DISPATCH_AND_COMPONENT_JOB_STATUS_CHANGE_REQ)
    @Override
    public Response reticleDeliveryStatusChangeReq(@RequestBody Params.ReticleDispatchAndComponentJobStatusChangeReqParams params) {
        String txId = TransactionIDEnum.RETICLE_DISPATCH_AND_COMPONENT_JOB_STATUS_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_xfer_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_XFER_JOB_CREATE_REQ)
    @Override
    public Response reticleXferJobCreateReq(@RequestBody Params.ReticleXferJobCreateReqParams params) {
        String txId = TransactionIDEnum.RETICLE_XFER_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticleXferJobCreateReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/what_reticle_action_list/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WHAT_RETICLE_ACTION_LIST_REQ)
    @Override
    public Response whatReticleActionListReq(@RequestBody Params.WhatReticleActionListReqParams params) {
        String txId = TransactionIDEnum.WHAT_RETICLE_ACTION_LIST_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   RTD Interface. Get WhatReticleActionList                            */
        /*-----------------------------------------------------------------------*/
        Params.WhatReticleActionListInqParams whatReticleActionListInqParams = new Params.WhatReticleActionListInqParams();
        whatReticleActionListInqParams.setDispatchStationID(params.getDispatchStationID());
        Results.WhatReticleActionListInqResult whatReticleActionListInqResult = arhsInqService.sxWhatReticleActionListInq(objCommon, whatReticleActionListInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Delete RDJ                                                          */
        /*-----------------------------------------------------------------------*/
        log.info("Delete RDJ");
        List<Infos.ReticleDispatchJob> strReticleDispatchJobDeleteList = whatReticleActionListInqResult.getStrReticleDispatchJobDeleteList();
        for (Infos.ReticleDispatchJob reticleDispatchJob : strReticleDispatchJobDeleteList) {
            Params.ReticleDispatchJobDeleteReqParams reticleDispatchJobDeleteReqParams = new Params.ReticleDispatchJobDeleteReqParams();
            reticleDispatchJobDeleteReqParams.setReticleDispatchJobID(reticleDispatchJob.getReticleDispatchJobID());
            reticleDispatchJobDeleteReqParams.setClaimMemo(params.getClaimMemo());
            arhsService.sxReticleDispatchJobDeleteReq(objCommon, reticleDispatchJobDeleteReqParams);
        }

        /*-----------------------------------------------------------------------*/
        /*   Insert RDJ                                                          */
        /*-----------------------------------------------------------------------*/
        log.info("Insert RDJ");
        List<Infos.ReticleDispatchJob> strReticleDispatchJobInsertList = whatReticleActionListInqResult.getStrReticleDispatchJobInsertList();
        for (Infos.ReticleDispatchJob reticleDispatchJob : strReticleDispatchJobInsertList) {
            Params.ReticleDispatchJobInsertReqParams reticleDispatchJobDeleteReqParams = new Params.ReticleDispatchJobInsertReqParams();
            reticleDispatchJobDeleteReqParams.setStrReticleDispatchJob(reticleDispatchJob);
            reticleDispatchJobDeleteReqParams.setClaimMemo(params.getClaimMemo());
            arhsService.sxReticleDispatchJobInsertReq(objCommon, reticleDispatchJobDeleteReqParams);
        }
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/rpod_xfer_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_XFER_JOB_CREATE_REQ)
    @Override
    public Response rpodXferJobCreateReq(@RequestBody Params.ReticlePodXferJobCreateReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_XFER_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticlePodXferJobCreateReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/rpod_xfer_job_comp/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_XFER_JOB_COMP_RPT)
    @Override
    public Response rpodXferJobCompRpt(@RequestBody Params.ReticlePodXferJobCompRptParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_XFER_JOB_COMP_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        List<Infos.ReticlePodXferJobCompInfo> result = null;
        try {
            result = arhsService.sxReticlePodXferJobCompRpt(objCommon, params);
        } catch (ServiceException e) {
            result = e.getData(List.class);
            if (CimArrayUtils.isNotEmpty(result)) {
                for (Infos.ReticlePodXferJobCompInfo reticlePodXferJobCompInfo : result) {
                    Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
                    RetCode<Object> retCode = new RetCode<>();
                    retCode.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                    retCode.setMessageID(CimObjectUtils.toString(e.getCode()));
                    retCode.setTransactionID(e.getTransactionID());
                    retCode.setMessageText(e.getMessage());
                    retCode.setReasonText(e.getReasonText());
                    reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
                    reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(false);
                    reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID("");
                    reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID("");
                    reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_XFER);
                    reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(null);
                    reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(reticlePodXferJobCompInfo.getReticlePodID());
                    reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(null);
                    reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(null);
                    reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(reticlePodXferJobCompInfo.getToMachineID());
                    reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(reticlePodXferJobCompInfo.getToPortID());
                    reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getClaimMemo());
                    arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon, reticleDispatchAndComponentJobStatusChangeReqParams);
                }
            }
        }
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/rpod_unclamp/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_UNCLAMP_RPT)
    @Override
    public Response rpodUnclampRpt(@RequestBody Params.ReticlePodUnclampRptParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_UNCLAMP_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());

        ObjectIdentifier equipmentID = null, stockerID = null;
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            equipmentID = params.getMachineID();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEqp(), ex.getCode())) {
                throw ex;
            }
            stockerID = params.getMachineID();
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        try {
            arhsService.sxReticlePodUnclampRpt(objCommon, params);
        } catch (ServiceException e) {
            Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
            RetCode<Object> retCode = new RetCode<>();
            retCode.setReturnCode(new OmCode(e.getCode(),e.getMessage()));
            retCode.setMessageID(CimObjectUtils.toString(e.getCode()));
            retCode.setTransactionID(e.getTransactionID());
            retCode.setMessageText(e.getMessage());
            retCode.setReasonText(e.getReasonText());
            reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
            reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(false);
            reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID("");
            reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID("");
            reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_UNCLAMP);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(null);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(params.getReticlePodID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(params.getMachineID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(params.getPortID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(params.getMachineID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(params.getPortID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
            arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon,reticleDispatchAndComponentJobStatusChangeReqParams);
        }
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/rpod_unclamp/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_UNCLAMP_REQ)
    @Override
    public Response rpodUnclampReq(@RequestBody Params.ReticlePodUnclampReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_UNCLAMP_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());

        ObjectIdentifier equipmentID = null, stockerID = null;
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            equipmentID = params.getMachineID();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEqp(), ex.getCode())) {
                throw ex;
            }
            stockerID = params.getMachineID();
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        try {
            arhsService.sxReticlePodUnclampReq(objCommon, params);
        } catch (ServiceException e) {
            Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
            RetCode<Object> retCode = new RetCode<>();
            retCode.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
            retCode.setMessageID(CimObjectUtils.toString(e.getCode()));
            retCode.setTransactionID(e.getTransactionID());
            retCode.setMessageText(e.getMessage());
            retCode.setReasonText(e.getReasonText());
            reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
            reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(true);
            reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID(params.getReticleDispatchJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID(params.getReticleComponentJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_UNCLAMP);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(null);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(params.getReticlePodID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(params.getMachineID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(params.getPortID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(params.getMachineID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(params.getPortID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
            arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon, reticleDispatchAndComponentJobStatusChangeReqParams);
        }
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/rpod_unclamp_and_transfer_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_UNCLAMP_AND_XFER_JOB_CREATE_REQ)
    @Override
    public Response rpodUnclampAndXferJobCreateReq(@RequestBody Params.ReticlePodUnclampAndXferJobCreateReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_UNCLAMP_AND_XFER_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        ObjectIdentifier equipmentID = null, stockerID = null;
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, params.getFromMachineID());
            equipmentID = params.getFromMachineID();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEqp(), ex.getCode())) {
                throw ex;
            }
            stockerID = params.getFromMachineID();
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        arhsService.sxReticlePodUnclampAndXferJobCreateReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/rpod_unclamp_job_create/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_UNCLAMP_JOB_CREATE_REQ)
    @Override
    public Response rpodUnclampJobCreateReq(@RequestBody Params.ReticlePodUnclampJobCreateReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_UNCLAMP_JOB_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        ObjectIdentifier equipmentID = null, stockerID = null;
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            equipmentID = params.getMachineID();
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEqp(), ex.getCode())) {
                throw ex;
            }
            stockerID = params.getMachineID();
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        arhsService.sxReticlePodUnclampJobCreateReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/rpod_xfer_job_delete/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_XFER_JOB_DELETE_REQ)
    @Override
    public Response rpodXferJobDeleteReq(@RequestBody Params.ReticlePodXferJobDeleteReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_XFER_JOB_DELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        arhsService.sxReticlePodXferJobDeleteReq(objCommon, params);
        return Response.createSucc(txId, null);
    }
}
