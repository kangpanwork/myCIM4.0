package com.fa.cim.controller.durable;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.durable.IDurableController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.method.impl.MessageMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.arhs.IArhsService;
import com.fa.cim.service.durable.IDurableInqService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/26          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/26 16:28
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IDurableController.class, confirmableKey = "DurableConfirm", cancellableKey = "DurableCancel")
@RequestMapping("/drb")
@Listenable
public class DurableController implements IDurableController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IPostService postService;

    @Autowired
    private PostController postController;

    @Autowired
    private MessageMethod messageMethod;

    @Autowired
    private IDurableService durableService;

    @Autowired
    private IDurableInqService durableInqService;

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IArhsService arhsService;

    @ResponseBody
    @RequestMapping(value = "/durable_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_REGIST_REQ)
    public Response durableSetReq(@RequestBody Params.DurableSetReqParams params) {
        String txId = TransactionIDEnum.DURABLE_REGIST_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //Step3 - txDurableSetReq
        List<Infos.ObjectResult> objectResults = new ArrayList<>();
        Boolean check = false;
        for (int i = 0; i < CimArrayUtils.getSize(params.getDurableRegistInfo().getDurableAttributeList()); i++) {
            Infos.DurableAttribute durableAttribute = params.getDurableRegistInfo().getDurableAttributeList().get(i);
            Infos.ObjectResult objectResult = new Infos.ObjectResult();
            try {
                durableService.sxDurableSetReq(objCommon, params.getDurableRegistInfo().getUpdateFlag(), params.getDurableRegistInfo().getClassName(), durableAttribute, params.getClaimMemo());
                objectResult.setObjectType(params.getDurableRegistInfo().getClassName());
                objectResult.setObjectID(durableAttribute.getDurableID());
                RetCode<Object> retCode = new RetCode<>();
                retCode.setReturnCode(retCodeConfig.getSucc());
                retCode.setTransactionID(txId);
                retCode.setMessageText(retCodeConfig.getSucc().getMessage());
                objectResult.setResult(retCode);
                objectResults.add(objectResult);
            } catch (ServiceException e) {
                objectResult.setObjectType(params.getDurableRegistInfo().getClassName());
                objectResult.setObjectID(durableAttribute.getDurableID());
                RetCode<Object> retCode = new RetCode<>();
                retCode.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                retCode.setTransactionID(txId);
                retCode.setMessageText(e.getMessage());
                objectResult.setResult(retCode);
                objectResults.add(objectResult);
                check = true;
            }
        }

        Validations.check(check, objectResults, retCodeConfig.getSomeRequestsFailed(), txId);

        return Response.createSucc(txId, objectResults);
    }

    @ResponseBody
    @RequestMapping(value = "/multi_durable_status_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping({TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_RETICLE,
            TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_RETICLE_POD,
            TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_CARRIER,})
    public Response multiDurableStatusChangeReq(@RequestBody Params.MultiDurableStatusChangeReqParams params) {

        User user = params.getUser();
        Validations.check(null == user, "the user info is null...");
        Infos.MultiDurableStatusChangeReqInParm parm = params.getParm();
        Validations.check(null == parm, "the MultiDurableStatusChangeReqInParm is null...");

        String durableCategory = parm.getDurableCategory();
        final String transactionID = BizConstant.SP_DURABLECAT_RETICLE.equals(durableCategory) ? TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_RETICLE.getValue()
                : (BizConstant.SP_DURABLECAT_RETICLEPOD.equals(durableCategory) ? TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_RETICLE_POD.getValue()
                : TransactionIDEnum.DURABLE_STATUS_MULTI_CHANGE_REQ_CARRIER.getValue());
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("【step1】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, new Params.AccessControlCheckInqParams(true));

        log.info("【step2】call sxMultiDurableStatusChangeReq");
        durableService.sxMultiDurableStatusChangeReq(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_all_in_out/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_JUST_IN_OUT_RPT)
    public Response reticleAllInOutRpt(@RequestBody Params.ReticleAllInOutRptParams params) {
        String txId = TransactionIDEnum.RETICLE_JUST_IN_OUT_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }
        //step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        //Step2 - txReticleAllInOutRpt
        durableService.sxReticleAllInOutRpt(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/multiple_reticle_pod_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_POD_MULTI_STATUS_CHANGE_RPT)
    public Response multipleReticlePodStatusChangeRpt(@RequestBody Params.MultipleReticlePodStatusChangeRptParam params) {
        //======Pre Process======
        String txId = TransactionIDEnum.RETICLE_POD_MULTI_STATUS_CHANGE_RPT.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");
        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, privilegeCheckParams);

        //Step2 - txReticlePodMaintInfoUpdateReq
        durableService.sxMultipleReticlePodStatusChangeRpt(objCommon, params.getUser(), params.getReticlePodStatus(), params.getReticlePodIDList());

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_pod_maint_info_update/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_POD_PMINFO_RESET_REQ)
    public Response reticlePodMaintInfoUpdateReq(@RequestBody Params.ReticlePodMaintInfoUpdateReqParam params) {
        //======Pre Process======
        String txId = TransactionIDEnum.RETICLE_POD_PMINFO_RESET_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        User user = params.getUser();
        Validations.check(user == null, "userID can not be null");

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        //Step2 - txReticlePodMaintInfoUpdateReq
        durableService.sxReticlePodMaintInfoUpdateReq(objCommon, params.getUser(), params.getReticlePodID());

        return Response.createSucc(txId, null);
    }


    @ResponseBody
    @RequestMapping(value = "/reticle_pod_transfer_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_POD_XFER_STAUES_CHANGE_RPT)
    public Response reticlePodTransferStatusChangeRpt(@RequestBody Params.ReticlePodTransferStatusChangeRptParams params) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        String txId = TransactionIDEnum.RETICLE_POD_XFER_STAUES_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), txId);
        }

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        //Step3 - txReticlePodTransferStatusChangeRpt__160
        Results.ReticlePodTransferStatusChangeRptResult result = durableService.sxReticlePodTransferStatusChangeRpt(objCommon, params, params.getClaimMemo());
        if (ObjectIdentifier.isEmpty(params.getEquipmentID()) ? false : CimStringUtils.isNotEmpty(params.getEquipmentID().getValue())) {
            boolean bFoundEi = false;
            int nLen = CimObjectUtils.isEmpty(params.getXferReticlePodList()) ? 0 : params.getXferReticlePodList().size();
            for (int i = 0; i < nLen; i++) {
                if (CimStringUtils.equals(CIMStateConst.SP_TRANS_STATE_EQUIPMENT_IN, params.getXferReticlePodList().get(i).getTransferStatus())) {
                    bFoundEi = true;
                    break;
                }
            }
            if (bFoundEi) {
                /*--------------------------------------------------*/
                /*   Put Message into Message Queue for Full-Auto   */
                /*--------------------------------------------------*/

                // Step4 - messageQueue_Put

                log.info("call messageQueue_Put()");
                ObjectIdentifier dummy = null;
                List<Infos.PortOperationMode> dmyPortOperationMode = new ArrayList<>();

                Inputs.MessageQueuePutIn messageQueuePutIn = new Inputs.MessageQueuePutIn();
                messageQueuePutIn.setEquipmentID(params.getEquipmentID());
                messageQueuePutIn.setStrPortOperationMode(dmyPortOperationMode);
                messageQueuePutIn.setEquipmentStatusCode(dummy);
                messageQueuePutIn.setLotID(dummy);
                messageQueuePutIn.setLotProcessState("");
                messageQueuePutIn.setLotHoldState("");
                messageQueuePutIn.setCassetteID(dummy);
                messageQueuePutIn.setCassetteTransferState("");
                messageQueuePutIn.setCassetteTransferReserveFlag(FALSE);
                messageQueuePutIn.setCassetteDispatchReserveFlag(FALSE);
                messageQueuePutIn.setDurableID(dummy);
                messageQueuePutIn.setDurableTransferState("");
                messageMethod.messageQueuePut(objCommon, messageQueuePutIn);

            }
        }
        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_sort/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_SORT_RPT)
    public Response reticleSortRpt(@RequestBody Params.ReticleSortRptParam param) {
        final String transactionID = TransactionIDEnum.RETICLE_SORT_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = param.getUser();
        Validations.check(null == user, "the user info is null...");

        log.info("【step2】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, privilegeCheckParams);

        log.info("【step3】call sxReticleSortRpt");
        durableService.sxReticleSortRpt(objCommon, param.getStrReticleSortInfo(), param.getClaimMemo());

        return Response.createSucc(transactionID, null);
    }


    @ResponseBody
    @RequestMapping(value = "/old/carrier_status_change/rpt", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.CASSETTE_STATUS_CHANGE_RPT)
    public Response carrierStatusChangeRptOld(@RequestBody Params.CarrierStatusChangeRptInParams carrierStatusChangeRptInParams) {
        final String transactionID = TransactionIDEnum.CASSETTE_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = carrierStatusChangeRptInParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, carrierStatusChangeRptInParams.getUser(), accessControlCheckInqParams);

        //step2 - txCarrierStatusChangeRpt
        durableService.sxCarrierStatusChangeRpt(objCommon, carrierStatusChangeRptInParams.getCassetteID(), carrierStatusChangeRptInParams.getCassetteStatus(), carrierStatusChangeRptInParams.getClaimMemo());


        // step3 -call txPostTaskRegisterReq__100
        Results.PostTaskRegisterReqResult strPostTaskRegisterReqResult = new Results.PostTaskRegisterReqResult();
        Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        List<ObjectIdentifier> cassetteIDS = new ArrayList<>();
        cassetteIDS.add(carrierStatusChangeRptInParams.getCassetteID());
        postProcessRegistrationParm.setCassetteIDs(cassetteIDS);
        Params.PostTaskRegisterReqParams strPostProcessRegistrationParm = new Params.PostTaskRegisterReqParams();
        strPostProcessRegistrationParm.setTransactionID(objCommon.getTransactionID());
        strPostProcessRegistrationParm.setPatternID(null);
        strPostProcessRegistrationParm.setKey(null);
        strPostProcessRegistrationParm.setSequenceNumber(-1);
        strPostProcessRegistrationParm.setPostProcessRegistrationParm(postProcessRegistrationParm);
        strPostProcessRegistrationParm.setClaimMemo("");
        strPostTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, strPostProcessRegistrationParm);

        // step4 -  call TxPostTaskExecuteReq__100
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams6 = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams6.setUser(carrierStatusChangeRptInParams.getUser());
        postTaskExecuteReqParams6.setKey(strPostTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams6.setSyncFlag(1);
        postTaskExecuteReqParams6.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams6.setKeyTimeStamp(null);
        postTaskExecuteReqParams6.setClaimMemo("");
        Response response = postController.postTaskExecuteReq(postTaskExecuteReqParams6);


        return Response.createSucc(transactionID, response);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/carrier_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.CASSETTE_STATUS_CHANGE_RPT)
    @EnablePostProcess
    public Response carrierStatusChangeRpt(@RequestBody Params.CarrierStatusChangeRptInParams params) {
        final String transactionID = TransactionIDEnum.CASSETTE_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        Validations.check(null == user, retCodeConfig.getInvalidParameter());

        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams controlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                controlCheckInqParams);

        //step2 - txCarrierStatusChangeRpt
        durableService.sxCarrierStatusChangeRpt(objCommon, params.getCassetteID(),
                params.getCassetteStatus(),
                params.getClaimMemo());

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/multiple_carrier_status_change/rpt", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.CASSETTE_STATUS_MULTI_CHANGE_RPT)
    public Response multipleCarrierStatusChangeRptOld(@RequestBody Params.MultipleCarrierStatusChangeRptParms multipleCarrierStatusChangeRptParms) {
        final String transactionID = TransactionIDEnum.CASSETTE_STATUS_MULTI_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = multipleCarrierStatusChangeRptParms.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }

        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2- txMultipleCarrierStatusChangeRpt
        durableService.sxMultipleCarrierStatusChangeRpt(objCommon, multipleCarrierStatusChangeRptParms.getCassetteStatus(),
                multipleCarrierStatusChangeRptParms.getCassetteID(), multipleCarrierStatusChangeRptParms.getClaimMemo());

        Results.PostTaskRegisterReqResult registReqResultRetCode = new Results.PostTaskRegisterReqResult();

        Params.PostTaskRegisterReqParams strPostProcessRegistrationParm = new Params.PostTaskRegisterReqParams();
        strPostProcessRegistrationParm.setTransactionID(objCommon.getTransactionID());
        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>(multipleCarrierStatusChangeRptParms.getCassetteID());
        postProcessRegistrationParam.setCassetteIDs(cassetteIDs);
        strPostProcessRegistrationParm.setPostProcessRegistrationParm(postProcessRegistrationParam);
        strPostProcessRegistrationParm.setSequenceNumber(-1);
        // step4 - txPostTaskRegisterReq__100
        postService.sxPostTaskRegisterReq(objCommon, strPostProcessRegistrationParm);

        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setKey(registReqResultRetCode.getDKey());
        postTaskExecuteReqParams.setUser(user);
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setClaimMemo("");
        postTaskExecuteReqParams.setKeyTimeStamp(null);

        // step5 -TxPostTaskExecuteReq__100
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        return Response.createSucc(objCommon.getTransactionID(), null);
    }


    @Override
    @ResponseBody
    @RequestMapping(value = "/multiple_carrier_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.CASSETTE_STATUS_MULTI_CHANGE_RPT)
    @EnablePostProcess
    public Response multipleCarrierStatusChangeRpt(@RequestBody Params.MultipleCarrierStatusChangeRptParms parms) {
        final String transactionID = TransactionIDEnum.CASSETTE_STATUS_MULTI_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = parms.getUser();
        Validations.check(null == user, retCodeConfig.getInvalidParameter());

        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                user, accessControlCheckInqParams);

        //step2- txMultipleCarrierStatusChangeRpt
        durableService.sxMultipleCarrierStatusChangeRpt(objCommon, parms.getCassetteStatus(),
                parms.getCassetteID(), parms.getClaimMemo());

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/carrier_usage_count_reset/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CASSETTE_USAGE_COUNT_RESET_REQ)
    public Response carrierUsageCountResetReq(@RequestBody Params.CarrierUsageCountResetReqParms carrierUsageCountResetReqParms) {
        final String transactionID = TransactionIDEnum.CASSETTE_USAGE_COUNT_RESET_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = carrierUsageCountResetReqParms.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step3 - txCarrierUsageCountResetReq
        ObjectIdentifier result = durableService.sxCarrierUsageCountResetReq(objCommon, carrierUsageCountResetReqParms.getCassetteID(), carrierUsageCountResetReqParms.getClaimMemo());

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_STATUS_CHANGE_RPT)
    public Response reticleStatusChangeRpt(@RequestBody Params.ReticleStatusChangeRptParams reticleStatusChangeRptParams) {
        final String transactionID = TransactionIDEnum.RETICLE_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = reticleStatusChangeRptParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, reticleStatusChangeRptParams.getUser(), accessControlCheckInqParams);

        //step2 - txReticleStatusChangeRpt
        Results.ReticleStatusChangeRptResult result = durableService.sxReticleStatusChangeRpt(objCommon, reticleStatusChangeRptParams.getReticleID(),
                reticleStatusChangeRptParams.getReticleStatus(), reticleStatusChangeRptParams.getClaimMemo());

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/multiple_reticle_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_STATUS_MULTI_CHANGE_RPT)
    public Response multipleReticleStatusChangeRpt(@RequestBody Params.MultipleReticleStatusChangeRptParams multipleReticleStatusChangeRptParams) {
        final String transactionID = TransactionIDEnum.RETICLE_STATUS_MULTI_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = multipleReticleStatusChangeRptParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }

        //Step1 - calendar_GetCurrentTimeDR get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step3 - txMultipleReticleStatusChangeRpt
        durableService.sxMultipleReticleStatusChangeRpt(objCommon, multipleReticleStatusChangeRptParams.getReticleStatus(),
                multipleReticleStatusChangeRptParams.getReticleID(), multipleReticleStatusChangeRptParams.getClaimMemo());

        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_usage_count_reset/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_USAGE_COUNT_RESET_REQ)
    public Response reticleUsageCountResetReq(@RequestBody Params.ReticleUsageCountResetReqParams reticleUsageCountResetReqParams) {
        final String transactionID = TransactionIDEnum.RETICLE_USAGE_COUNT_RESET_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = reticleUsageCountResetReqParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);


        //step2 - txReticleUsageCountResetReq
        ObjectIdentifier result = durableService.sxReticleUsageCountResetReq(objCommon, reticleUsageCountResetReqParams.getReticleID(), reticleUsageCountResetReqParams.getClaimMemo());

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/reticle_transfer_status_change/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RETICLE_XFER_STATUS_CHANGE_RPT)
    public Response reticleTransferStatusChangeRpt(@RequestBody Params.ReticleTransferStatusChangeRptParams reticleTransferStatusChangeRptParams) {
        final String transactionID = TransactionIDEnum.RETICLE_XFER_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = reticleTransferStatusChangeRptParams.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);

        //step1 - txAccessControlCheckInq
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        // step2 - txReticleTransferStatusChangeRpt__160
        Results.ReticleTransferStatusChangeRptResult result = durableService.sxReticleTransferStatusChangeRpt(objCommon, reticleTransferStatusChangeRptParams.getStockerID(),
                reticleTransferStatusChangeRptParams.getEquipmentID(), reticleTransferStatusChangeRptParams.getStrXferReticle(), reticleTransferStatusChangeRptParams.getClaimMemo(),true);

        // setp3 - messageQueue_Put
        for (int i = 0; i < CimArrayUtils.getSize(reticleTransferStatusChangeRptParams.getStrXferReticle()); i++) {

            Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
            msgQueuePut.setEquipmentID(reticleTransferStatusChangeRptParams.getEquipmentID());
            msgQueuePut.setCassetteDispatchReserveFlag(false);
            msgQueuePut.setCassetteTransferReserveFlag(false);
            msgQueuePut.setDurableTransferState(reticleTransferStatusChangeRptParams.getStrXferReticle().get(i).getTransferStatus());
            msgQueuePut.setDurableID(reticleTransferStatusChangeRptParams.getStrXferReticle().get(i).getReticleID());
            messageMethod.messageQueuePut(objCommon, msgQueuePut);
        }

        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params
     * @return com.fa.cim.common.support.Response
     * @throws
     * @author ho
     * @date 2020/3/19 10:58
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_pod_inv_update/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_INVENTORY_RPT)
    public Response reticlePodInvUpdateRpt(@RequestBody Params.ReticlePodInvUpdateRptParam params) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.ReticlePodInvUpdateRptResult retVal = new Results.ReticlePodInvUpdateRptResult();
        Infos.ObjCommon strObjCommonIn;

        // Initialising strObjCommonIn's first two parameters

        String transactionID = TransactionIDEnum.RETICLE_POD_INVENTORY_RPT.getValue();

        List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo = params.getStrInventoryReticlePodInfo();
        if (CimArrayUtils.getSize(params.getStrInventoryReticlePodInfo()) > 0) {
            int strReticlePodIDLen = 0;
            // Calculate the length of the string
            for (int j = 0; j < CimArrayUtils.getSize(params.getStrInventoryReticlePodInfo()); j++) {
                strReticlePodIDLen += CimStringUtils.length(strInventoryReticlePodInfo.get(j).getReticlePodID().getValue()) + 1;
            }

            StringBuilder strReticlePodID = new StringBuilder();
            for (int j = 0; j < CimArrayUtils.getSize(strInventoryReticlePodInfo); j++) {
                strReticlePodID.append(strInventoryReticlePodInfo.get(j).getReticlePodID().getValue());
                strReticlePodID.append(",");
            }
        }

        // step-1 calendar_GetCurrentTimeDR

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setStockerID(params.getStockerID());

        // step-2 txAccessControlCheckInq
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        // step-3 txReticlePodInvUpdateRpt__090
        try {
            retVal = durableService.sxReticlePodInvUpdateRpt(strObjCommonIn, params.getUser(), params.getStockerID(),
                    params.getEquipmentID(), strInventoryReticlePodInfo, params.getClaimMemo(), params.getShelfPosition());
        } catch (ServiceException ex) {
            if (!Validations.isEquals(ex.getCode(), retCodeConfigEx.getReportedNotMmdurable())) {
                throw ex;
            }
            Response.createError(retCodeConfigEx.getReportedNotMmdurable(), transactionID);
        }

        return Response.createSucc(retVal);
    }

    /*---------------------------*/
    //     DMS                    /
    /*---------------------------*/
    @ResponseBody
    @RequestMapping(value = "/drb_bank_in/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.DURABLE_BANK_IN_REQ)
    public Response drbBankInReq(@RequestBody Params.DurableBankInReqInParam durableBankInReqInParam) {
        Results.DurableBankInReqResult durableBankInReqResult = new Results.DurableBankInReqResult();
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_BANK_IN_REQ.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableBankInReqInParam.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<ObjectIdentifier> durableIDs = durableBankInReqInParam.getDurableIDs();
        durableBankInReqResult.setStrBankInDurableResults(new ArrayList<>(durableIDs.size()));
        int failCnt = 0;
        for (int i = 0; i < durableIDs.size(); i++) {
            durableBankInReqResult = durableService.sxDrbBankInReq(durableBankInReqResult, objCommonIn, i, durableBankInReqInParam, durableBankInReqInParam.getClaimMemo());
            List<Results.BankInDurableResult> strBankInDurableResults = durableBankInReqResult.getStrBankInDurableResults();
            Results.BankInDurableResult bankInDurableResult = strBankInDurableResults.get(i);
            if (bankInDurableResult.getErrorCode() != 0) {
                failCnt++;
            }
        }
        if (failCnt == 0) {
            return Response.createSucc(objCommonIn.getTransactionID(), durableBankInReqResult);
        } else {
            return Response.createSuccWithOmCode(objCommonIn.getTransactionID(), retCodeConfig.getSomeRequestsFailed(), durableBankInReqResult);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/drb_bank_in_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.DURABLE_BANK_IN_CANCEL_REQ)
    public Response drbBankInCancelReq(@RequestBody Params.DurableBankInCancelReqInParam durableBankInCancelReqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_BANK_IN_CANCEL_REQ.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableBankInCancelReqInParam.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        durableService.sxDrbBankInCancelReq(objCommonIn, durableBankInCancelReqInParam, durableBankInCancelReqInParam.getClaimMemo());
        /*------------------------------------------------------------------------*/
        /*    Post-Process task Registration                                      */
        /*------------------------------------------------------------------------*/
        Params.DurablePostProcessActionRegistReqInParam RegParam = new Params.DurablePostProcessActionRegistReqInParam();
        RegParam.setTxID((TransactionIDEnum.DURABLE_BANK_IN_CANCEL_REQ.getValue()));
        RegParam.setPatternID("");
        RegParam.setKey("");
        RegParam.setSeqNo(-1);
        Infos.DurablePostProcessRegistrationParm durablePostProcessRegistrationParm = new Infos.DurablePostProcessRegistrationParm();
        durablePostProcessRegistrationParm.setDurableCategory(durableBankInCancelReqInParam.getDurableCategory());
        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        durableIDs.add(durableBankInCancelReqInParam.getDurableID());
        durablePostProcessRegistrationParm.setDurableIDs(durableIDs);
        RegParam.setStrDurablePostProcessRegistrationParm(durablePostProcessRegistrationParm);
        Results.DurablePostProcessActionRegistReqResult durablePostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(objCommonIn, RegParam);
        /*---------------------------------------------------------------------------*/
        /*    Post-Process task Execution Section                                    */
        /*                                                                           */
        /*    TxPostProcessExecReq                                                   */
        /*    const pptUser& requestUserID     //<i>R/Request User ID                */
        /*    const char *   key               //<i>R/Key                            */
        /*    CORBA::Long    syncFlag          //<i>R/Synchronous Flag               */
        /*    CORBA::Long    prevSeqNo         //<i>R/Previous Sequence Number       */
        /*    const char *   keyTimeStamp      //<i>R/Key Time Stamp                 */
        /*    const char *   claimMemo         //<i>O/Claim Memo                     */
        /*---------------------------------------------------------------------------*/
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setKey(durablePostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setUser(durableBankInCancelReqInParam.getUser());
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp("");
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        return Response.createSucc(objCommonIn.getTransactionID(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/drb_bank_move/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_BANK_MOVE_REQ)
    public Response drbBankMoveReq(@RequestBody Params.DurableBankMoveReqParam durableBankMoveReqParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_BANK_MOVE_REQ.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableBankMoveReqParam.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableBankMoveReqResult durableBankMoveReqResult = new Results.DurableBankMoveReqResult();
        durableBankMoveReqResult.setStrBankMoveDurableResults(new ArrayList<>());
        List<ObjectIdentifier> durableIDs = durableBankMoveReqParam.getDurableIDs();
        int failCnt = 0;
        for (int i = 0; i < durableIDs.size(); i++) {
            durableBankMoveReqResult = durableService.sxDrbBankMoveReq(durableBankMoveReqResult, objCommonIn, i, durableBankMoveReqParam, durableBankMoveReqParam.getClaimMemo());
            List<Results.BankMoveDurableResult> strBankInDurableResults = durableBankMoveReqResult.getStrBankMoveDurableResults();
            Results.BankMoveDurableResult bankMoveDurableResult = strBankInDurableResults.get(i);
            if (bankMoveDurableResult.getErrorCode() != 0) {
                failCnt++;
            }
        }
        if (failCnt == 0) {
            return Response.createSucc(objCommonIn.getTransactionID(), durableBankMoveReqResult);
        } else {
            return Response.createSuccWithOmCode(objCommonIn.getTransactionID(), retCodeConfig.getSomeRequestsFailed(), durableBankMoveReqResult);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/drb_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DURABLE_BANK_DELETE_REQ)
    public Response drbDeleteReq(@RequestBody Params.DurableDeleteParam durableDeleteParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_BANK_DELETE_REQ.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableDeleteParam.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableDeleteReqResult durableDeleteReqResult = new Results.DurableDeleteReqResult();
        durableDeleteReqResult.setStrDurableDeleteResults(new ArrayList<>());
        List<ObjectIdentifier> durableIDs = durableDeleteParam.getDurableIDList();
        int failCnt = 0;
        for (int i = 0; i < CimArrayUtils.getSize(durableIDs); i++) {
            durableDeleteReqResult = durableService.sxDrbDeleteReq(durableDeleteReqResult, objCommonIn, durableIDs.get(i), durableDeleteParam.getClassName(), durableDeleteParam.getClaimMemo());
            List<Results.DurableDeleteResult> durableDeleteResults = durableDeleteReqResult.getStrDurableDeleteResults();
            Results.DurableDeleteResult durableDeleteResult = durableDeleteResults.get(i);
            if (durableDeleteResult.getErrorCode() != 0) {
                failCnt++;
            }
        }
        if (failCnt == 0) {
            return Response.createSucc(objCommonIn.getTransactionID(), durableDeleteReqResult);
        } else {
            return Response.createSuccWithOmCode(objCommonIn.getTransactionID(), retCodeConfig.getSomeRequestsFailed(), durableDeleteReqResult);
        }
    }

    @ResponseBody
    @PostMapping(value = "/drb_move_in/req")
    @CimMapping(TransactionIDEnum.DURABLE_OPERATION_START_REQ)
    @Override
    public Response drbMoveInReq(@RequestBody Params.DurableOperationStartReqInParam durableOperationStartReqInParam) {
        Validations.check(null == durableOperationStartReqInParam, retCodeConfig.getInvalidInputParam());
        String transactionID = TransactionIDEnum.DURABLE_OPERATION_START_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(durableOperationStartReqInParam.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableOperationStartReqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableOperationStartReqResult retVal = durableInqService.sxDrbMoveInReq(objCommon, durableOperationStartReqInParam);
        assert null != retVal;

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Params.DurablePostProcessActionRegistReqInParam registReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        registReqInParam.setTxID(transactionID);
        registReqInParam.setSeqNo(-1);

        Infos.DurablePostProcessRegistrationParm strDurablePostProcessRegistrationParm = new Infos.DurablePostProcessRegistrationParm();
        registReqInParam.setStrDurablePostProcessRegistrationParm(strDurablePostProcessRegistrationParm);

        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        strDurablePostProcessRegistrationParm.setDurableIDs(durableIDs);
        Optional.of(retVal).flatMap(data -> Optional.ofNullable(data.getStrStartDurables())).ifPresent(list -> list.forEach(durable -> {
            durableIDs.add(durable.getDurableId());
        }));

        strDurablePostProcessRegistrationParm.setEquipmentID(durableOperationStartReqInParam.getEquipmentID());
        strDurablePostProcessRegistrationParm.setDurableControlJobID(durableOperationStartReqInParam.getDurableControlJobID());
        strDurablePostProcessRegistrationParm.setDurableCategory(retVal.getDurableCategory());

        // Post-Processing Registration for durable
        // txDurablePostProcessActionRegistReq
        // todo:ZQI
        Results.DurablePostProcessActionRegistReqResult registReqResult = durableService.sxDrbPostTaskRegistReq(objCommon, registReqInParam);
        assert null != registReqResult;

        //----------------------------------
        // Post-Processing Update Section
        //----------------------------------
        boolean additionalInfoFlag = false;
        List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq = new ArrayList<>();
        if (registReqResult.getStrPostProcessActionInfoSeq() != null) {
            for (Infos.PostProcessActionInfo actionInfo : registReqResult.getStrPostProcessActionInfoSeq()) {
                if (CimStringUtils.equals(actionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
                    Infos.PostProcessAdditionalInfo additionalInfo = new Infos.PostProcessAdditionalInfo();
                    additionalInfo.setDKey(actionInfo.getDKey());
                    additionalInfo.setSequenceNumber(actionInfo.getSequenceNumber());
                    additionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT);
                    additionalInfo.setValue("1");
                    strPostProcessAdditionalInfoSeq.add(additionalInfo);
                    additionalInfoFlag = true;
                    break;
                }
            }
        }
        if (additionalInfoFlag) {
            postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                    registReqResult.getStrPostProcessActionInfoSeq(),
                    strPostProcessAdditionalInfoSeq,
                    durableOperationStartReqInParam.getClaimMemo());
        }

        //-----------------------------------
        //   Post Process Execution
        //-----------------------------------
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(durableOperationStartReqInParam.getUser());
        postTaskExecuteReqParams.setKey(registReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_move_in_for_ib/req")
    @CimMapping(TransactionIDEnum.DURABLE_OPERATION_START_FOR_IB_REQ)
    @Override
    public Response drbMoveInForIBReq(@RequestBody Params.DurableOperationStartReqInParam durableOperationStartReqInParam) {
        Validations.check(null == durableOperationStartReqInParam, retCodeConfig.getInvalidInputParam());
        String transactionID = TransactionIDEnum.DURABLE_OPERATION_START_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(durableOperationStartReqInParam.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableOperationStartReqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableOperationStartReqResult retVal = durableService.sxDrbMoveInForIBReq(objCommon, durableOperationStartReqInParam);
        assert null != retVal;

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Params.DurablePostProcessActionRegistReqInParam registReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        registReqInParam.setTxID(transactionID);
        registReqInParam.setSeqNo(-1);

        Infos.DurablePostProcessRegistrationParm strDurablePostProcessRegistrationParm = new Infos.DurablePostProcessRegistrationParm();
        registReqInParam.setStrDurablePostProcessRegistrationParm(strDurablePostProcessRegistrationParm);

        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        strDurablePostProcessRegistrationParm.setDurableIDs(durableIDs);
        Optional.of(retVal).flatMap(data -> Optional.ofNullable(data.getStrStartDurables())).ifPresent(list -> list.forEach(durable -> {
            durableIDs.add(durable.getDurableId());
        }));

        strDurablePostProcessRegistrationParm.setEquipmentID(durableOperationStartReqInParam.getEquipmentID());
        strDurablePostProcessRegistrationParm.setDurableControlJobID(durableOperationStartReqInParam.getDurableControlJobID());
        strDurablePostProcessRegistrationParm.setDurableCategory(retVal.getDurableCategory());

        // Post-Processing Registration for durable
        // txDurablePostProcessActionRegistReq
        // todo:ZQI
        Results.DurablePostProcessActionRegistReqResult registReqResult = durableService.sxDrbPostTaskRegistReq(objCommon, registReqInParam);
        assert null != registReqResult;

        //----------------------------------
        // Post-Processing Update Section
        //----------------------------------
        boolean additionalInfoFlag = false;
        List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq = new ArrayList<>();
        if (registReqResult.getStrPostProcessActionInfoSeq() != null) {
            for (Infos.PostProcessActionInfo actionInfo : registReqResult.getStrPostProcessActionInfoSeq()) {
                if (CimStringUtils.equals(actionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
                    Infos.PostProcessAdditionalInfo additionalInfo = new Infos.PostProcessAdditionalInfo();
                    additionalInfo.setDKey(actionInfo.getDKey());
                    additionalInfo.setSequenceNumber(actionInfo.getSequenceNumber());
                    additionalInfo.setName(BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT);
                    additionalInfo.setValue("1");
                    strPostProcessAdditionalInfoSeq.add(additionalInfo);
                    additionalInfoFlag = true;
                    break;
                }
            }
        }
        if (additionalInfoFlag) {
            postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                    registReqResult.getStrPostProcessActionInfoSeq(),
                    strPostProcessAdditionalInfoSeq,
                    durableOperationStartReqInParam.getClaimMemo());
        }

        //-----------------------------------
        //   Post Process Execution
        //-----------------------------------
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(durableOperationStartReqInParam.getUser());
        postTaskExecuteReqParams.setKey(registReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_lag_time_time_action/req")
    @CimMapping(TransactionIDEnum.NULL)
    @Override
    public Response drbLagTimeActionReq(@RequestBody Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_PROCESS_LAG_TIME_UPDATE.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableProcessLagTimeUpdateReqInParm.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableProcessLagTimeUpdateReqResult durableProcessLagTimeUpdateReqResul = durableService.sxDrbLagTimeActionReq(objCommonIn, durableProcessLagTimeUpdateReqInParm.getUser(), durableProcessLagTimeUpdateReqInParm);


        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        //todo  txDurablePostProcessActionRegistReq
        //-----------------------------------
        //   Post Process Execution
        //-----------------------------------
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        //todo  postTaskRegisterReqParams.setPostProcessRegistrationParm();
        postTaskRegisterReqParams.setClaimMemo("");
        // postTaskRegisterReqParams.setKey();
        postTaskRegisterReqParams.setUser(objCommonIn.getUser());
        // postTaskRegisterReqParams.setPatternID();
        //postTaskRegisterReqParams.setTransactionID();
        postController.postTaskRegisterReq(postTaskRegisterReqParams);


        return Response.createSucc(transactionID, durableProcessLagTimeUpdateReqResul);
    }

    @PostMapping(value = "/drb_post_task_regist/req")
    @CimMapping(TransactionIDEnum.DURABLE_POST_PROCESS_ACTION_REGIST_REQ)
    @Override
    public Response drbPostTaskRegistReq(@RequestBody Params.DurablePostProcessActionRegistReqInParam paramIn) {
        Validations.check(null == paramIn, retCodeConfig.getInvalidInputParam());
        String transactionID = TransactionIDEnum.DURABLE_POST_PROCESS_ACTION_REGIST_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(paramIn.getStrDurablePostProcessRegistrationParm().getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, paramIn.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurablePostProcessActionRegistReqResult retVal = durableService.sxDrbPostTaskRegistReq(objCommon, paramIn);
        return Response.createSucc(transactionID, retVal);
    }

    @PostMapping(value = "/drb_prf_cx_delete/req")
    @CimMapping(TransactionIDEnum.DURABLE_PFX_DELETE_REQ)
    @Override
    public Response drbPrfCxDeleteReq(@RequestBody Params.DurablePFXDeleteReqInParam paramIn) {
        Validations.check(null == paramIn, retCodeConfig.getInvalidInputParam());
        String transactionID = TransactionIDEnum.DURABLE_PFX_DELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, paramIn.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        durableService.sxDrbPrfCxDeleteReq(objCommon, paramIn);

        return Response.createSucc(transactionID);
    }

    @PostMapping(value = "/drb_prf_cx_create/req")
    @CimMapping(TransactionIDEnum.DURABLE_PFX_CREATE_REQ)
    @Override
    public Response drbPrfCxCreateReq(@RequestBody Params.DurablePFXCreateReqInParam paramIn) {
        Validations.check(null == paramIn, retCodeConfig.getInvalidInputParam());
        String transactionID = TransactionIDEnum.DURABLE_PFX_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, paramIn.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        durableService.sxDrbPrfCxCreateReq(objCommon, paramIn);

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Params.DurablePostProcessActionRegistReqInParam registReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        registReqInParam.setTxID(transactionID);
        registReqInParam.setSeqNo(-1);

        Infos.DurablePostProcessRegistrationParm registrationParm = new Infos.DurablePostProcessRegistrationParm();
        registReqInParam.setStrDurablePostProcessRegistrationParm(registrationParm);
        registrationParm.setDurableCategory(paramIn.getDurableCategory());
        registrationParm.setDurableIDs(paramIn.getDurableIDs());
        // Post-Processing Registration for durable
        Results.DurablePostProcessActionRegistReqResult registReqResult = durableService.sxDrbPostTaskRegistReq(objCommon, registReqInParam);

        //--------------------------------------
        // Post Process Execution for durable
        //--------------------------------------
        Params.PostTaskExecuteReqParams executeReqParams = new Params.PostTaskExecuteReqParams();
        executeReqParams.setKey(null == registReqResult ? null : registReqResult.getDKey());
        executeReqParams.setSyncFlag(1);
        executeReqParams.setPreviousSequenceNumber(0);
        executeReqParams.setUser(paramIn.getUser());
        executeReqParams.setClaimMemo(paramIn.getClaimMemo());
        postController.postTaskExecuteReq(executeReqParams);

        return Response.createSucc(transactionID);
    }

    @PostMapping(value = "/drb_move_in_cancel/req")
    @CimMapping(TransactionIDEnum.DURABLE_OPERATION_START_CANCEL_REQ)
    @Override
    public Response drbMoveInCancelReq(@RequestBody Params.DurableOperationStartCancelReqInParam strDurableOperationStartCancelReqInParam) {
        User requestUserID = strDurableOperationStartCancelReqInParam.getUser();
        log.info("PPTServiceManager_i::TxDurableOperationStartCancelReq");

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.DurableOperationStartCancelReqResult retVal = new Results.DurableOperationStartCancelReqResult();

        // Initialising strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.DURABLE_OPERATION_START_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("{}", "Call clearThreadSpecificDataString");
        ThreadContextHolder.clearThreadSpecificDataString();

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummyID;
        dummyID = ObjectIdentifier.buildWithValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(strDurableOperationStartCancelReqInParam.getEquipmentID());
        accessControlCheckInqParams.setStockerID(dummyID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, requestUserID, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableService.sxDrbMoveInCancelReq(
                objCommon,
                strDurableOperationStartCancelReqInParam,
                strDurableOperationStartCancelReqInParam.getClaimMemo());

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Results.DurablePostProcessActionRegistReqResult strPostProcessActionRegistReqResult;


        log.info("{}", "txDurableOperationStartCancelReq :rc == RC_OK");

        Params.DurablePostProcessActionRegistReqInParam strDurablePostProcessActionRegistReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        strDurablePostProcessActionRegistReqInParam.setTxID(objCommon.getTransactionID());
        strDurablePostProcessActionRegistReqInParam.setPatternID("");
        strDurablePostProcessActionRegistReqInParam.setKey("");
        strDurablePostProcessActionRegistReqInParam.setSeqNo(-1);
        int durableLen = CimArrayUtils.getSize(retVal.getStrStartDurables());
        Infos.DurablePostProcessRegistrationParm strDurablePostProcessRegistrationParm = new Infos.DurablePostProcessRegistrationParm();
        strDurablePostProcessActionRegistReqInParam.setStrDurablePostProcessRegistrationParm(strDurablePostProcessRegistrationParm);
        strDurablePostProcessRegistrationParm.setDurableIDs(new ArrayList<>(durableLen));
        for (int durableSeq = 0; durableSeq < durableLen; durableSeq++) {
            strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().add(
                    retVal.getStrStartDurables().get(durableSeq).getDurableId());
        }
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setEquipmentID(strDurableOperationStartCancelReqInParam.getEquipmentID());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableControlJobID(strDurableOperationStartCancelReqInParam.getDurableControlJobID());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableCategory(retVal.getDurableCategory());
        strDurablePostProcessActionRegistReqInParam.setClaimMemo(strDurableOperationStartCancelReqInParam.getClaimMemo());

        //Post-Processing Registration for durable
        strPostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(
                objCommon,
                strDurablePostProcessActionRegistReqInParam);

        //----------------------------------
        // Post-Processing Update Section
        //----------------------------------

        log.info("{}", "txDurableOperationStartCancelReq and Post Process task Registration : rc == RC_OK");

        //  Update postProcessQueue
        List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq;
        strPostProcessAdditionalInfoSeq = new ArrayList<>(1);
        boolean additionalInfoFlag = FALSE;
        for (int iCnt = 0; iCnt < CimArrayUtils.getSize(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq()); iCnt++) {
            log.info("{} {}", "loop to strPostProcessActionRegistReqResult.strPostProcessActionInfoSeq.length()", iCnt);
            if (CimStringUtils.equals(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq().get(iCnt).getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
                log.info("{}", "RunWaferInfoUpdate exists = ");
                strPostProcessAdditionalInfoSeq.add(new Infos.PostProcessAdditionalInfo());
                strPostProcessAdditionalInfoSeq.get(0).setDKey(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq().get(iCnt).getDKey());
                strPostProcessAdditionalInfoSeq.get(0).setSequenceNumber(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq().get(iCnt).getSequenceNumber());
                strPostProcessAdditionalInfoSeq.get(0).setName(BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT);
                strPostProcessAdditionalInfoSeq.get(0).setValue(("1"));
                additionalInfoFlag = true;
                break;
            }
        }
        if (additionalInfoFlag) {
            log.info("{}", "additionalInfoFlag is TRUE");
            List<Infos.PostProcessActionInfo> strPostProcessActionUpdateReqResult;
            strPostProcessActionUpdateReqResult = postService.sxPostActionModifyReq(
                    objCommon,
                    BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                    strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq(),
                    strPostProcessAdditionalInfoSeq,
                    strDurableOperationStartCancelReqInParam.getClaimMemo());
        }

        log.info("{}", "txDurableOperationStartCancelReq() == RC_OK");

        //Post Process Execution
        Response result;
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(requestUserID);
        postTaskExecuteReqParams.setKey(strPostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        result = postController.postTaskExecuteReq(postTaskExecuteReqParams);

        //-----------------------------------------------------------------------
        //   Post Process
        //-----------------------------------------------------------------------

        log.info("{}", "Call clearThreadSpecificDataString");
        ThreadContextHolder.clearThreadSpecificDataString();

        log.info("PPTServiceManager_i::TxDurableOperationStartCancelReq");
        return Response.createSucc(transactionID, retVal);
    }

    @PostMapping(value = "/drb_move_in_cancel_for_ib/req")
    @CimMapping(TransactionIDEnum.DURABLE_OPERATION_START_CANCEL_FOR_IB_REQ)
    @Override
    public Response drbMoveInCancelForIBReq(@RequestBody Params.DurableOperationStartCancelReqInParam strDurableOperationStartCancelReqInParam) {
        User requestUserID = strDurableOperationStartCancelReqInParam.getUser();
        log.info("PPTServiceManager_i::TxDurableOperationStartCancelReq");

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.DurableOperationStartCancelReqResult retVal = new Results.DurableOperationStartCancelReqResult();

        // Initialising strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.DURABLE_OPERATION_START_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("{}", "Call clearThreadSpecificDataString");
        ThreadContextHolder.clearThreadSpecificDataString();

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummyID;
        dummyID = ObjectIdentifier.buildWithValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(strDurableOperationStartCancelReqInParam.getEquipmentID());
        accessControlCheckInqParams.setStockerID(dummyID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, requestUserID, accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableService.sxDrbMoveInCancelForIBReq(
                objCommon,
                strDurableOperationStartCancelReqInParam,
                strDurableOperationStartCancelReqInParam.getClaimMemo());

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Results.DurablePostProcessActionRegistReqResult strPostProcessActionRegistReqResult;


        log.info("{}", "txDurableOperationStartCancelReq :rc == RC_OK");

        Params.DurablePostProcessActionRegistReqInParam strDurablePostProcessActionRegistReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        strDurablePostProcessActionRegistReqInParam.setTxID(objCommon.getTransactionID());
        strDurablePostProcessActionRegistReqInParam.setPatternID("");
        strDurablePostProcessActionRegistReqInParam.setKey("");
        strDurablePostProcessActionRegistReqInParam.setSeqNo(-1);
        int durableLen = CimArrayUtils.getSize(retVal.getStrStartDurables());
        Infos.DurablePostProcessRegistrationParm strDurablePostProcessRegistrationParm = new Infos.DurablePostProcessRegistrationParm();
        strDurablePostProcessActionRegistReqInParam.setStrDurablePostProcessRegistrationParm(strDurablePostProcessRegistrationParm);
        strDurablePostProcessRegistrationParm.setDurableIDs(new ArrayList<>(durableLen));
        for (int durableSeq = 0; durableSeq < durableLen; durableSeq++) {
            strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().add(
                    retVal.getStrStartDurables().get(durableSeq).getDurableId());
        }
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setEquipmentID(strDurableOperationStartCancelReqInParam.getEquipmentID());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableControlJobID(strDurableOperationStartCancelReqInParam.getDurableControlJobID());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableCategory(retVal.getDurableCategory());
        strDurablePostProcessActionRegistReqInParam.setClaimMemo(strDurableOperationStartCancelReqInParam.getClaimMemo());

        //Post-Processing Registration for durable
        strPostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(
                objCommon,
                strDurablePostProcessActionRegistReqInParam);

        //----------------------------------
        // Post-Processing Update Section
        //----------------------------------

        log.info("{}", "txDurableOperationStartCancelReq and Post Process task Registration : rc == RC_OK");

        //  Update postProcessQueue
        List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq;
        strPostProcessAdditionalInfoSeq = new ArrayList<>(1);
        boolean additionalInfoFlag = FALSE;
        for (int iCnt = 0; iCnt < CimArrayUtils.getSize(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq()); iCnt++) {
            log.info("{} {}", "loop to strPostProcessActionRegistReqResult.strPostProcessActionInfoSeq.length()", iCnt);
            if (CimStringUtils.equals(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq().get(iCnt).getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
                log.info("{}", "RunWaferInfoUpdate exists = ");
                strPostProcessAdditionalInfoSeq.add(new Infos.PostProcessAdditionalInfo());
                strPostProcessAdditionalInfoSeq.get(0).setDKey(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq().get(iCnt).getDKey());
                strPostProcessAdditionalInfoSeq.get(0).setSequenceNumber(strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq().get(iCnt).getSequenceNumber());
                strPostProcessAdditionalInfoSeq.get(0).setName(BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT);
                strPostProcessAdditionalInfoSeq.get(0).setValue(("1"));
                additionalInfoFlag = true;
                break;
            }
        }
        if (additionalInfoFlag) {
            log.info("{}", "additionalInfoFlag is TRUE");
            List<Infos.PostProcessActionInfo> strPostProcessActionUpdateReqResult;
            strPostProcessActionUpdateReqResult = postService.sxPostActionModifyReq(
                    objCommon,
                    BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO,
                    strPostProcessActionRegistReqResult.getStrPostProcessActionInfoSeq(),
                    strPostProcessAdditionalInfoSeq,
                    strDurableOperationStartCancelReqInParam.getClaimMemo());
        }

        log.info("{}", "txDurableOperationStartCancelReq() == RC_OK");

        //Post Process Execution
        Response result;
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(requestUserID);
        postTaskExecuteReqParams.setKey(strPostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        result = postController.postTaskExecuteReq(postTaskExecuteReqParams);

        //-----------------------------------------------------------------------
        //   Post Process
        //-----------------------------------------------------------------------

        log.info("{}", "Call clearThreadSpecificDataString");
        ThreadContextHolder.clearThreadSpecificDataString();

        log.info("PPTServiceManager_i::TxDurableOperationStartCancelReq");
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_cj_status_change/req")
    @CimMapping(TransactionIDEnum.DURABLE_CONTROL_JOB_MANAGE_REQ)
    @Override
    public Response drbCJStatusChangeReq(@RequestBody Params.DurableControlJobManageReqInParam durableControlJobManageReqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_GATE_PASS_REQ.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableControlJobManageReqInParam.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        ObjectIdentifier ret = durableService.sxDrbCJStatusChangeReq(objCommonIn, durableControlJobManageReqInParam);
        return Response.createSucc(objCommonIn.getTransactionID(), ret);
    }

    @ResponseBody
    @PostMapping(value = "/drb_pass_thru/req")
    @CimMapping(TransactionIDEnum.DURABLE_GATE_PASS_REQ)
    @Override
    public Response drbPassThruReq(@RequestBody Params.DurableGatePassReqInParam durableGatePassReqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        //Initialising strObjCommonIn's first two parameters
        Infos.ObjCommon objCommonIn;
        String transactionID = TransactionIDEnum.DURABLE_GATE_PASS_REQ.getValue();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        objCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableGatePassReqInParam.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableGatePassResult durableGatePassResult = new Results.DurableGatePassResult();
        List<Infos.GatePassDurableInfo> strGatePassDurableInfo = durableGatePassReqInParam.getStrGatePassDurableInfo();
        int gatePassDurableSize = strGatePassDurableInfo.size();
        durableGatePassResult.setStrGatePassDurablesResults(new ArrayList<>(gatePassDurableSize));
        int failCnt = 0;
        for (int i = 0; i < gatePassDurableSize; i++) {
            durableGatePassResult = durableService.sxDrbPassThruReq(durableGatePassResult, objCommonIn, i, durableGatePassReqInParam);
            List<Results.DurableGatePass> strGatePassDurablesResults = durableGatePassResult.getStrGatePassDurablesResults();
            Results.DurableGatePass durableGatePass = strGatePassDurablesResults.get(i);
            if (null != durableGatePass.getErrorCode() && durableGatePass.getErrorCode() != 0) {
                failCnt++;
            }
        }
        if (failCnt == 0) {
            return Response.createSucc(objCommonIn.getTransactionID(), durableGatePassResult);
        } else if (failCnt == gatePassDurableSize) {
            return Response.createError(retCodeConfigEx.getAllDurableFailed(), TransactionIDEnum.DURABLE_GATE_PASS_REQ.getValue());
        } else {
            return Response.createSuccWithOmCode(objCommonIn.getTransactionID(), retCodeConfig.getSomeRequestsFailed(), durableGatePassResult);
        }
    }

    @ResponseBody
    @PostMapping(value = "/drb_move_out/req")
    @CimMapping(TransactionIDEnum.DURABLE_OPE_COMP_REQ)
    @Override
    public Response drbMoveOutReq(@RequestBody Params.DurableOpeCompReqInParam paramIn) {
        Validations.check(null == paramIn, retCodeConfig.getInvalidInputParam());

        String transactionID = TransactionIDEnum.DURABLE_OPE_COMP_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(paramIn.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, paramIn.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableOpeCompReqResult retVal = durableService.sxDrbMoveOutReq(objCommon, paramIn);
        assert null != retVal;

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Params.DurablePostProcessActionRegistReqInParam registReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        registReqInParam.setTxID(transactionID);
        registReqInParam.setSeqNo(-1);

        Infos.DurablePostProcessRegistrationParm registrationParm = new Infos.DurablePostProcessRegistrationParm();
        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        Optional.of(retVal).flatMap(result -> Optional.ofNullable(result.getStrStartDurables())).ifPresent(list -> list.forEach(startDurable -> {
            durableIDs.add(startDurable.getDurableId());
        }));
        registrationParm.setDurableIDs(durableIDs);
        registrationParm.setEquipmentID(paramIn.getEquipmentID());
        registrationParm.setDurableCategory(retVal.getDurableCategory());
        registrationParm.setDurableControlJobID(paramIn.getDurableControlJobID());

        registReqInParam.setStrDurablePostProcessRegistrationParm(registrationParm);
        // Post-Processing Registration for durable
        Results.DurablePostProcessActionRegistReqResult registReqResult = durableService.sxDrbPostTaskRegistReq(objCommon, registReqInParam);

        //--------------------------------------
        // Post Process Execution for durable
        //--------------------------------------
        if (null != registReqResult) {
            Params.PostTaskExecuteReqParams executeReqParams = new Params.PostTaskExecuteReqParams();
            executeReqParams.setUser(paramIn.getUser());
            executeReqParams.setKey(registReqResult.getDKey());
            executeReqParams.setSyncFlag(1);
            executeReqParams.setPreviousSequenceNumber(0);
            postController.postTaskExecuteReq(executeReqParams);
        }

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_move_out_for_ib/req")
    @CimMapping(TransactionIDEnum.DURABLE_OPE_COMP_FOR_IB_REQ)
    @Override
    public Response drbMoveOutForIBReq(@RequestBody Params.DurableOpeCompReqInParam paramIn) {
        Validations.check(null == paramIn, retCodeConfig.getInvalidInputParam());

        String transactionID = TransactionIDEnum.DURABLE_OPE_COMP_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(paramIn.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, paramIn.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.DurableOpeCompReqResult retVal = durableService.sxDrbMoveOutForIBReq(objCommon, paramIn);
        assert null != retVal;

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Params.DurablePostProcessActionRegistReqInParam registReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        registReqInParam.setTxID(transactionID);
        registReqInParam.setSeqNo(-1);

        Infos.DurablePostProcessRegistrationParm registrationParm = new Infos.DurablePostProcessRegistrationParm();
        List<ObjectIdentifier> durableIDs = new ArrayList<>();
        Optional.of(retVal).flatMap(result -> Optional.ofNullable(result.getStrStartDurables())).ifPresent(list -> list.forEach(startDurable -> {
            durableIDs.add(startDurable.getDurableId());
        }));
        registrationParm.setDurableIDs(durableIDs);
        registrationParm.setEquipmentID(paramIn.getEquipmentID());
        registrationParm.setDurableCategory(retVal.getDurableCategory());
        registrationParm.setDurableControlJobID(paramIn.getDurableControlJobID());

        registReqInParam.setStrDurablePostProcessRegistrationParm(registrationParm);
        // Post-Processing Registration for durable
        Results.DurablePostProcessActionRegistReqResult registReqResult = durableService.sxDrbPostTaskRegistReq(objCommon, registReqInParam);

        //--------------------------------------
        // Post Process Execution for durable
        //--------------------------------------
        if (null != registReqResult) {
            Params.PostTaskExecuteReqParams executeReqParams = new Params.PostTaskExecuteReqParams();
            executeReqParams.setUser(paramIn.getUser());
            executeReqParams.setKey(registReqResult.getDKey());
            executeReqParams.setSyncFlag(1);
            executeReqParams.setPreviousSequenceNumber(0);
            postController.postTaskExecuteReq(executeReqParams);
        }

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/drb_skip/req")
    @CimMapping(TransactionIDEnum.DURABLE_OPE_LOCATE_REQ)
    @Override
    public Response drbSkipReq(@RequestBody Params.DurableOpeLocateReqInParam durableOpeLocateReqInParam) {
        log.info("PPTServiceManager_i:: TxDurableOpeLocateReq");

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        String transactionID = TransactionIDEnum.DURABLE_OPE_LOCATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Infos.ObjCommon strObjCommonIn;

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, durableOpeLocateReqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        durableService.sxDrbSkipReq(strObjCommonIn, durableOpeLocateReqInParam, durableOpeLocateReqInParam.getClaimMemo());

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Results.DurablePostProcessActionRegistReqResult strDurablePostProcessActionRegistReqResult;
        log.info("{}", "txDurableOpeLocateReq :rc == RC_OK");
        Params.DurablePostProcessActionRegistReqInParam strDurablePostProcessActionRegistReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        strDurablePostProcessActionRegistReqInParam.setTxID(strObjCommonIn.getTransactionID());
        strDurablePostProcessActionRegistReqInParam.setPatternID("");
        strDurablePostProcessActionRegistReqInParam.setKey("");
        strDurablePostProcessActionRegistReqInParam.setSeqNo(-1);
        strDurablePostProcessActionRegistReqInParam.setStrDurablePostProcessRegistrationParm(new Infos.DurablePostProcessRegistrationParm());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableCategory(durableOpeLocateReqInParam.getDurableCategory());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableIDs(new ArrayList<>(1));
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().add(durableOpeLocateReqInParam.getDurableID());
        log.info("{} {}", "Registration of Post-Processng", strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().get(0).getValue());
        strDurablePostProcessActionRegistReqInParam.setClaimMemo(durableOpeLocateReqInParam.getClaimMemo());

        //Post-Processing Registration for durable
        strDurablePostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(
                strObjCommonIn,
                strDurablePostProcessActionRegistReqInParam);

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        log.info("{}", "txDurableOpeLocateReq and txDurablePostProcessActionRegistReq :rc == RC_OK");

        //Post Process Execution for Durable
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(durableOpeLocateReqInParam.getUser());
        postTaskExecuteReqParams.setKey(strDurablePostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        Response result = postController.postTaskExecuteReq(postTaskExecuteReqParams);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        log.info("PPTServiceManager_i:: TxDurableOpeLocateReq");
        return Response.createSucc(transactionID, result.getBody());
    }

    @ResponseBody
    @PostMapping(value = "/hold_drb_release/req")
    @CimMapping(TransactionIDEnum.HOLD_DURABLE_RELEASE_REQ)
    @Override
    public Response holdDrbReleaseReq(@RequestBody Params.HoldDurableReleaseReqParams params) {
        String transactionID = TransactionIDEnum.HOLD_DURABLE_RELEASE_REQ.getValue();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        Infos.HoldDurableReleaseReqInParam holdDurableReleaseReqInParam = params.getHoldDurableReleaseReqInParam();
        durableService.sxHoldDrbReleaseReq(objCommon, holdDurableReleaseReqInParam, params.getClaimMemo());

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Results.DurablePostProcessActionRegistReqResult strDurablePostProcessActionRegistReqResult;
        log.info("{}", "txDurableOpeLocateReq :rc == RC_OK");
        Params.DurablePostProcessActionRegistReqInParam strDurablePostProcessActionRegistReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        strDurablePostProcessActionRegistReqInParam.setTxID(objCommon.getTransactionID());
        strDurablePostProcessActionRegistReqInParam.setPatternID("");
        strDurablePostProcessActionRegistReqInParam.setKey("");
        strDurablePostProcessActionRegistReqInParam.setSeqNo(-1);
        strDurablePostProcessActionRegistReqInParam.setStrDurablePostProcessRegistrationParm(new Infos.DurablePostProcessRegistrationParm());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableCategory(holdDurableReleaseReqInParam.getDurableCategory());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableIDs(new ArrayList<>(1));
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().add(holdDurableReleaseReqInParam.getDurableID());
        log.info("{} {}", "Registration of Post-Processng", strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().get(0).getValue());
        strDurablePostProcessActionRegistReqInParam.setClaimMemo(params.getClaimMemo());

        //Post-Processing Registration for durable
        strDurablePostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(
                objCommon,
                strDurablePostProcessActionRegistReqInParam);

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        log.info("{}", "txDurableOpeLocateReq and txDurablePostProcessActionRegistReq :rc == RC_OK");

        //Post Process Execution for Durable
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(strDurablePostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/drb_rework_cancel/req")
    @CimMapping(TransactionIDEnum.REWORK_DURABLE_CANCEL_REQ)
    @Override
    public Response drbReworkCancelReq(@RequestBody Params.ReworkDurableCancelReqParams params) {
        String transactionID = TransactionIDEnum.REWORK_DURABLE_CANCEL_REQ.getValue();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        Infos.ReworkDurableCancelReqInParam reworkDurableCancelReqInParam = params.getReworkDurableCancelReqInParam();
        durableService.sxDrbReworkCancelReq(objCommon, reworkDurableCancelReqInParam, params.getClaimMemo());

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Results.DurablePostProcessActionRegistReqResult strDurablePostProcessActionRegistReqResult;
        log.info("{}", "txDurableOpeLocateReq :rc == RC_OK");
        Params.DurablePostProcessActionRegistReqInParam strDurablePostProcessActionRegistReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        strDurablePostProcessActionRegistReqInParam.setTxID(objCommon.getTransactionID());
        strDurablePostProcessActionRegistReqInParam.setPatternID("");
        strDurablePostProcessActionRegistReqInParam.setKey("");
        strDurablePostProcessActionRegistReqInParam.setSeqNo(-1);
        strDurablePostProcessActionRegistReqInParam.setStrDurablePostProcessRegistrationParm(new Infos.DurablePostProcessRegistrationParm());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableCategory(reworkDurableCancelReqInParam.getDurableCategory());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableIDs(new ArrayList<>(1));
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().add(reworkDurableCancelReqInParam.getDurableID());
        log.info("{} {}", "Registration of Post-Processng", strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().get(0).getValue());
        strDurablePostProcessActionRegistReqInParam.setClaimMemo(params.getClaimMemo());

        //Post-Processing Registration for durable
        strDurablePostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(
                objCommon,
                strDurablePostProcessActionRegistReqInParam);

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        log.info("{}", "txDurableOpeLocateReq and txDurablePostProcessActionRegistReq :rc == RC_OK");

        //Post Process Execution for Durable
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(strDurablePostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/drb_rework/req")
    @CimMapping(TransactionIDEnum.REWORK_DURABLE_REQ)
    @Override
    public Response drbReworkReq(@RequestBody Params.ReworkDurableReqInParam strReworkDurableReqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Infos.ObjCommon strObjCommonIn;

        String txID;
        //-------------------------------------------------
        //   Decide TX_ID
        //-------------------------------------------------
        //Special Rework case
        if (CimBooleanUtils.isTrue(strReworkDurableReqInParam.getBForceRework())) {
            txID = TransactionIDEnum.FORCE_REWORK_DURABLE_REQ.getValue();
        } else {
            txID = TransactionIDEnum.REWORK_DURABLE_REQ.getValue();
        }

        ThreadContextHolder.setTransactionId(txID);

        log.info("PPTServiceManager_i:: TxReworkDurableReq", "TX ID (Use Input TX ID ) : {}", txID);

        // Initialising strObjCommonIn's first two parameters

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>(0);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(txID, strReworkDurableReqInParam.getUser(), accessControlCheckInqParams);

        //-----------------------------------------------------------------------//
        //   Main Process                                                        //
        //-----------------------------------------------------------------------//
        durableService.sxDrbReworkReq(strObjCommonIn, strReworkDurableReqInParam, strReworkDurableReqInParam.getClaimMemo());

        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Results.DurablePostProcessActionRegistReqResult strDurablePostProcessActionRegistReqResult;
        Params.DurablePostProcessActionRegistReqInParam strDurablePostProcessActionRegistReqInParam = new Params.DurablePostProcessActionRegistReqInParam();
        strDurablePostProcessActionRegistReqInParam.setTxID(strObjCommonIn.getTransactionID());
        strDurablePostProcessActionRegistReqInParam.setPatternID("");
        strDurablePostProcessActionRegistReqInParam.setKey("");
        strDurablePostProcessActionRegistReqInParam.setSeqNo(-1);
        strDurablePostProcessActionRegistReqInParam.setStrDurablePostProcessRegistrationParm(new Infos.DurablePostProcessRegistrationParm());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableCategory(strReworkDurableReqInParam.getDurableCategory());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().setDurableIDs(new ArrayList<>());
        strDurablePostProcessActionRegistReqInParam.getStrDurablePostProcessRegistrationParm().getDurableIDs().add(strReworkDurableReqInParam.getDurableID());

        //Post-Processing Registration for durable
        strDurablePostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(
                strObjCommonIn,
                strDurablePostProcessActionRegistReqInParam);

        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        log.info("{} {}", "txReworkDurableReq and txDurablePostProcessActionRegistReq :rc == RC_OK");

        //Post Process Execution for durable
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(strReworkDurableReqInParam.getUser());
        postTaskExecuteReqParams.setKey(strDurablePostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        Response result = postController.postTaskExecuteReq(postTaskExecuteReqParams);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(txID, null);
    }

    @ResponseBody
    @PostMapping(value = "/hold_drb/req")
    @CimMapping(TransactionIDEnum.HOLD_DURABLE_REQ)
    @Override
    public Response holdDrbReq(@RequestBody Params.HoldDurableReqParams params) {
        String transactionID = TransactionIDEnum.HOLD_DURABLE_REQ.getValue();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        durableService.sxHoldDrbReq(objCommon, params.getHoldDurableReqInParam(), params.getClaimMemo());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @PostMapping(value = "/drb_force_skip/req")
    @CimMapping(TransactionIDEnum.DURABLE_FORCE_OPE_LOCATE_REQ)
    @Override
    public Response drbForceSkipReq(@RequestBody Params.DurableForceOpeLocateReqInParam strDurableForceOpeLocateReqInParam) {

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        // Initialising strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.DURABLE_FORCE_OPE_LOCATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>(0);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        Infos.ObjCommon strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, strDurableForceOpeLocateReqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        durableService.sxDrbForceSkipReq(strObjCommonIn, strDurableForceOpeLocateReqInParam, strDurableForceOpeLocateReqInParam.getClaimMemo());

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/

        log.info("PPTServiceManager_i:: TxDurableForceOpeLocateReq");
        return Response.createSucc(transactionID);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strDurableBankInByPostProcReqInParam
     * @return com.fa.cim.common.support.Response
     * @throws
     * @author ho
     * @date 2020/7/23 15:45
     */
    @ResponseBody
    @PostMapping(value = "/drb_bank_in_by_post_task/req")
    @CimMapping(TransactionIDEnum.DURABLE_BANK_IN_BY_POST_PROC_REQ)
    @Override
    public Response drbBankInByPostTaskReq(Params.DurableBankInByPostProcReqInParam strDurableBankInByPostProcReqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/

        // Initialising strObjCommonIn's first two parameters
        String transactionID = TransactionIDEnum.DURABLE_BANK_IN_BY_POST_PROC_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);


        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummy = null;
        List<ObjectIdentifier> dummyIDs = new ArrayList<>();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        Infos.ObjCommon strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, strDurableBankInByPostProcReqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        boolean retVal = durableService.sxDrbBankInByPostTaskReq(strObjCommonIn, strDurableBankInByPostProcReqInParam, strDurableBankInByPostProcReqInParam.getClaimMemo());

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/drb_move_in_reserve_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.START_DURABLES_RESERVATION_CANCEL_REQ)
    public Response drbMoveInReserveCancelReq(@RequestBody Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam) {
        // Initialising strObjCommonIn's first two parameters
        String transationID = TransactionIDEnum.START_DURABLES_RESERVATION_CANCEL_REQ.getValue();

        ThreadContextHolder.setTransactionId(transationID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummyID;
        dummyID = ObjectIdentifier.buildWithValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>(0);

        Infos.ObjCommon strObjCommonIn;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(strStartDurablesReservationCancelReqInParam.getEquipmentID());
        accessControlCheckInqParams.setStockerID(dummyID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transationID, strStartDurablesReservationCancelReqInParam.getUser(), accessControlCheckInqParams);


        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.StartDurablesReservationCancelReqResult retVal = new Results.StartDurablesReservationCancelReqResult();
        // Initialising strObjCommonIn's first two parameters

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableService.sxDrbMoveInReserveCancelReq(
                strObjCommonIn,
                strStartDurablesReservationCancelReqInParam,
                strStartDurablesReservationCancelReqInParam.getClaimMemo());

        //-----------------------------------------------------------------------
        //   Post Process
        //-----------------------------------------------------------------------

        log.info("PPTServiceManager_i::TxStartDurablesReservationCancelReq");
        return Response.createSucc(transationID, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/drb_move_in_reserve_cancel_for_ib/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.START_DURABLES_RESERVATION_CANCEL_FOR_IB_REQ)
    public Response drbMoveInReserveCancelForIBReq(@RequestBody Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam) {
        // Initialising strObjCommonIn's first two parameters
        String transationID = TransactionIDEnum.START_DURABLES_RESERVATION_CANCEL_REQ.getValue();

        ThreadContextHolder.setTransactionId(transationID);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummyID;
        dummyID = ObjectIdentifier.buildWithValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>(0);

        Infos.ObjCommon strObjCommonIn;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(strStartDurablesReservationCancelReqInParam.getEquipmentID());
        accessControlCheckInqParams.setStockerID(dummyID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transationID, strStartDurablesReservationCancelReqInParam.getUser(), accessControlCheckInqParams);


        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Results.StartDurablesReservationCancelReqResult retVal = new Results.StartDurablesReservationCancelReqResult();
        // Initialising strObjCommonIn's first two parameters

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableService.sxDrbMoveInReserveCancelForIBReq(
                strObjCommonIn,
                strStartDurablesReservationCancelReqInParam,
                strStartDurablesReservationCancelReqInParam.getClaimMemo());

        //-----------------------------------------------------------------------
        //   Post Process
        //-----------------------------------------------------------------------

        log.info("PPTServiceManager_i::TxStartDurablesReservationCancelReq");
        return Response.createSucc(transationID, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/drb_move_in_reserve/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.START_DURABLES_RESERVATION_REQ)
    public Response drbMoveInReserveReq(@RequestBody Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        ObjectIdentifier retVal = null;

        // Initialising strObjCommonIn's first two parameters
        String transationID = TransactionIDEnum.START_DURABLES_RESERVATION_REQ.getValue();

        ThreadContextHolder.setTransactionId(transationID);

        int durableLen = CimArrayUtils.getSize(strStartDurablesReservationReqInParam.getStrStartDurables());
        log.info("{} {}", "durableLen", durableLen);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummyID;
        dummyID = ObjectIdentifier.buildWithValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>(0);

        Infos.ObjCommon strObjCommonIn;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(strStartDurablesReservationReqInParam.getEquipmentID());
        accessControlCheckInqParams.setStockerID(dummyID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transationID, strStartDurablesReservationReqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableService.sxDrbMoveInReserveReq(
                strObjCommonIn,
                strStartDurablesReservationReqInParam,
                strStartDurablesReservationReqInParam.getClaimMemo());

        //-----------------------------------------------------------------------
        //   Post Process
        //-----------------------------------------------------------------------
        return Response.createSucc(transationID, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/drb_move_in_reserve_for_ib/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.START_DURABLES_RESERVATION_FOR_IB_REQ)
    public Response drbMoveInReserveForIBReq(@RequestBody Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        ObjectIdentifier retVal = null;

        // Initialising strObjCommonIn's first two parameters
        String transationID = TransactionIDEnum.START_DURABLES_RESERVATION_FOR_IB_REQ.getValue();

        ThreadContextHolder.setTransactionId(transationID);

        int durableLen = CimArrayUtils.getSize(strStartDurablesReservationReqInParam.getStrStartDurables());
        log.info("{} {}", "durableLen", durableLen);

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------
        ObjectIdentifier dummyID;
        dummyID = ObjectIdentifier.buildWithValue("");
        List<ObjectIdentifier> dummyIDs;
        dummyIDs = new ArrayList<>(0);

        Infos.ObjCommon strObjCommonIn;
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(strStartDurablesReservationReqInParam.getEquipmentID());
        accessControlCheckInqParams.setStockerID(dummyID);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(dummyIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transationID, strStartDurablesReservationReqInParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = durableService.sxDrbMoveInReserveForIBReq(
                strObjCommonIn,
                strStartDurablesReservationReqInParam,
                strStartDurablesReservationReqInParam.getClaimMemo());

        //-----------------------------------------------------------------------
        //   Post Process
        //-----------------------------------------------------------------------
        return Response.createSucc(transationID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/durable_job_status_change/req")
    @CimMapping(TransactionIDEnum.DURABLE_JOB_STATUS_CHANGE_REQ)
    @Override
    public Response durableJobStatusChangeReq(@RequestBody  Params.DurableJobStatusChangeReqParams params) {
        Validations.check(null == params, retCodeConfig.getInvalidInputParam());

        String transactionID = TransactionIDEnum.START_DURABLES_RESERVATION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("【step1】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        log.info("【step2】call sxMultiDurableStatusChangeReq");
        durableService.sxDurableJobStatusChangeReq(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/durable_job_status_change/rpt")
    @CimMapping(TransactionIDEnum.DURABLE_JOB_STATUS_CHANGE_RPT)
    @Override
    public Response durableJobStatusChangeRpt(@RequestBody Params.DurableJobStatusChangeRptParams params) {
        Validations.check(null == params, retCodeConfig.getInvalidInputParam());

        String transactionID = TransactionIDEnum.DURABLE_JOB_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.info("【step1】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));


        log.info("【step2】call sxMultiDurableStatusChangeReq");
        durableService.sxDurableJobStatusChangeRpt(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/rework_drb_with_hold_release/req")
    @CimMapping(TransactionIDEnum.REWORK_DURABLE_WITH_HOLD_RELEASE_REQ)
    @Override
    public Response reworkDrbWithHoldReleaseReq(@RequestBody Params.ReworkDurableWithHoldReleaseReqInParam strReworkDurableWithHoldReleaseReqInParam) {
        Validations.check(null == strReworkDurableWithHoldReleaseReqInParam, retCodeConfig.getInvalidInputParam());

        String transactionID = TransactionIDEnum.REWORK_DURABLE_WITH_HOLD_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //-----------------------------------------------------------------------//
        //   Pre Process                                                         //
        //-----------------------------------------------------------------------//

        // Initialising strObjCommonIn's first two parameters
        //---------------------------------------------
        // Set time stamp.
        //---------------------------------------------
        //---------------------------------------------
        // Put Incoming Log.
        //---------------------------------------------

        //---------------------------------------------
        // Get calendar
        //---------------------------------------------

        //---------------------------------------------
        // Check privilege
        //---------------------------------------------

        log.info("【step1】call txAccessControlCheckInq(...)");
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, strReworkDurableWithHoldReleaseReqInParam.getUser(), new Params.AccessControlCheckInqParams(true));


        //-----------------------------------------------------------------------//
        //   Main Process                                                        //
        //-----------------------------------------------------------------------//
        String claimMemo = strReworkDurableWithHoldReleaseReqInParam.getClaimMemo();
        durableService.sxReworkDurableWithHoldReleaseReq(objCommon, strReworkDurableWithHoldReleaseReqInParam);

        Results.DurablePostProcessActionRegistReqResult strDurablePostProcessActionRegistReqResult;
        log.info("{}", "txReworkDurableWithHoldReleaseReq() : rc == RC_OK ");

        Params.DurablePostProcessActionRegistReqInParam strDurablePostProcessActionRegistReqInParam=new Params.DurablePostProcessActionRegistReqInParam();
        Infos.DurablePostProcessRegistrationParm durablePostProcessRegistrationParm = new Infos.DurablePostProcessRegistrationParm();
        List<ObjectIdentifier> durables = new ArrayList<>();
        strDurablePostProcessActionRegistReqInParam.setTxID(objCommon.getTransactionID());
        strDurablePostProcessActionRegistReqInParam.setPatternID ("");
        strDurablePostProcessActionRegistReqInParam.setKey ("");
        strDurablePostProcessActionRegistReqInParam.setSeqNo ( -1);
        durablePostProcessRegistrationParm.setDurableCategory(strReworkDurableWithHoldReleaseReqInParam.getStrReworkDurableReqInParam().getDurableCategory());
        durablePostProcessRegistrationParm.setDurableIDs(new ArrayList<>(1));
        durables.add(strReworkDurableWithHoldReleaseReqInParam.getStrReworkDurableReqInParam().getDurableID());
        durablePostProcessRegistrationParm.setDurableIDs(durables);
        strDurablePostProcessActionRegistReqInParam.setStrDurablePostProcessRegistrationParm(durablePostProcessRegistrationParm);
        strDurablePostProcessActionRegistReqInParam.setClaimMemo(strReworkDurableWithHoldReleaseReqInParam.getClaimMemo());

        //Post-Processing Registration for durable
        strDurablePostProcessActionRegistReqResult = durableService.sxDrbPostTaskRegistReq(
                objCommon,
                strDurablePostProcessActionRegistReqInParam);

        //-------------------------------------
        // Post-Processing Execution Section
        //-------------------------------------
        log.info("{}", "txReworkDurableWithHoldReleaseReq and Post Process task Registration : rc == RC_OK");

        //Post Process Execution for durable
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(strReworkDurableWithHoldReleaseReqInParam.getUser());
        postTaskExecuteReqParams.setKey(strDurablePostProcessActionRegistReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setClaimMemo("");

        Response result = postController.postTaskExecuteReq(postTaskExecuteReqParams);

        Validations.isSuccessWithException(result);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID);
    }

    @RequestMapping(value = "/reticle_pod_port_access_mode_chg/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_RSP_PORT_ACCESS_MODE_CHANGE_REQ)
    @Override
    public Response reticlePodPortAccessModeChgReq(@RequestBody Params.EqpRSPPortAccessModeChangeReqParams params) {
        String txId = TransactionIDEnum.EQP_RSP_PORT_ACCESS_MODE_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        durableService.sxEqpRSPPortAccessModeChangeReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_retrieve_by_offline/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_OFFLINE_RETRIEVE_REQ)
    @Override
    public Response reticleRetrieveByOffline(@RequestBody Params.ReticleOfflineRetrieveReqParams params) {
        String txId = TransactionIDEnum.RETICLE_OFFLINE_RETRIEVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());
        String equipmentID = null;
        String stockerID = null;
        try {
            equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            equipmentID = params.getMachineID().getValue();
        } catch (ServiceException e) {
            stockerID = ObjectIdentifier.fetchValue(params.getMachineID());
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(ObjectIdentifier.buildWithValue(equipmentID));
        accessControlCheckInqParams.setStockerID(ObjectIdentifier.buildWithValue(stockerID));
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        durableService.sxReticleOfflineRetrieveReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_pod_port_status_chg/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_RSP_PORT_STATUS_CHANGE_RPT)
    @Override
    public Response reticlePodPortStatusChgRpt(@RequestBody Params.EqpRSPPortStatusChangeRpt params) {
        String txId = TransactionIDEnum.EQP_RSP_PORT_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        durableService.sxEqpRSPPortStatusChangeRpt(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_retrieve/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_RETRIEVE_REQ)
    @Override
    public Response reticleRetrieveReq(@RequestBody Params.ReticleRetrieveReqParams params) {
        String txId = TransactionIDEnum.RETICLE_RETRIEVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        ObjectIdentifier stockerID = params.getStockerID();
        accessControlCheckInqParams.setStockerID(stockerID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        try {
            durableService.sxReticleRetrieveReq(objCommon, params);
        } catch (ServiceException ex) {
            ObjectIdentifier machineID;
            ObjectIdentifier RSPPortID;
            ObjectIdentifier reticleID;
            if (!ObjectIdentifier.isEmpty(equipmentID)) {
                machineID = equipmentID;
                RSPPortID = params.getReticlePodPortID();
            } else {
                machineID = stockerID;
                RSPPortID = params.getResourceID();
            }
            if (!CimObjectUtils.isEmpty(params.getMoveReticlesList())) {
                reticleID = params.getMoveReticlesList().get(0).getReticleID();
                if (log.isDebugEnabled()){
                    log.debug("get first reticle's ID. {}", reticleID);
                }
            } else {
                if (log.isDebugEnabled()){
                    log.debug("Not found move reticle.");
                }
                reticleID = new ObjectIdentifier("");
            }
            Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
            RetCode<Object> retCode = new RetCode<>();
            retCode.setReturnCode(new OmCode(ex.getCode(),ex.getMessage()));
            retCode.setMessageID(CimObjectUtils.toString(ex.getCode()));
            retCode.setTransactionID(ex.getTransactionID());
            retCode.setMessageText(ex.getMessage());
            retCode.setReasonText(ex.getReasonText());
            reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
            reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(true);
            reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID(params.getReticleDispatchJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID(params.getReticleComponentJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_RETRIEVE);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(reticleID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(params.getReticlePodID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(machineID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(RSPPortID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(machineID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(RSPPortID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
            arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon,reticleDispatchAndComponentJobStatusChangeReqParams);
        }
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_store_by_offline/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_OFFLINE_STORE_REQ)
    @Override
    public Response reticleStoreByOffline(@RequestBody Params.ReticleOfflineStoreReqParams params) {
        String txId = TransactionIDEnum.RETICLE_OFFLINE_STORE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, params.getUser());

        ObjectIdentifier equipmentID = null;
        ObjectIdentifier stockerID = null;
        String machineType = null;
        try {
            machineType = equipmentMethod.equipmentGetTypeDR(objCommon, params.getMachineID());
            log.debug("toMachineID is equipmentID.");
            equipmentID = params.getMachineID();
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                log.debug("toMachineID is stockerID.");
                stockerID = params.getMachineID();
            } else {
                throw e;
            }
        }

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setStockerID(stockerID);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        durableService.sxReticleOfflineStoreReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_retrieve/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_RETRIEVE_RPT)
    @Override
    public Response reticleRetrieveRpt(@RequestBody Params.ReticleRetrieveRptParams params) {
        String txId = TransactionIDEnum.RETICLE_RETRIEVE_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        ObjectIdentifier stockerID = params.getStockerID();
        accessControlCheckInqParams.setStockerID(stockerID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        try {
            durableService.sxReticleRetrieveRpt(objCommon, params);
        } catch (ServiceException ex) {
            ObjectIdentifier machineID = null;
            ObjectIdentifier portID = null;
            if (!ObjectIdentifier.isEmpty(equipmentID)) {
                machineID = equipmentID;
                portID = params.getReticlePodPortID();
            } else if (!ObjectIdentifier.isEmpty(stockerID)) {
                machineID = stockerID;
                portID = params.getResourceID();
            }
            List<Infos.ReticleRetrieveResult> strReticleRetrieveResult = params.getReticleRetrieveResultList();
            for (Infos.ReticleRetrieveResult reticleRetrieveResult : strReticleRetrieveResult) {
                Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
                RetCode<Object> retCode = new RetCode<>();
                retCode.setReturnCode(new OmCode(ex.getCode(), ex.getMessage()));
                retCode.setMessageID(CimObjectUtils.toString(ex.getCode()));
                retCode.setTransactionID(ex.getTransactionID());
                retCode.setMessageText(ex.getMessage());
                retCode.setReasonText(ex.getReasonText());
                reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
                reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(false);
                reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID("");
                reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID("");
                reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_RETRIEVE);
                reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(reticleRetrieveResult.getReticleID());
                reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(params.getReticlePodID());
                reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(machineID);
                reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(portID);
                reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(machineID);
                reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(portID);
                reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
                arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon, reticleDispatchAndComponentJobStatusChangeReqParams);
            }
        }
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_store/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_STORE_REQ)
    @Override
    public Response reticleStoreReq(@RequestBody Params.ReticleStoreReqParams params) {
        String txId = TransactionIDEnum.RETICLE_STORE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        ObjectIdentifier stockerID = params.getStockerID();
        accessControlCheckInqParams.setStockerID(stockerID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        try {
            durableService.sxReticleStoreReq(objCommon, params);
        } catch (ServiceException ex) {

            ObjectIdentifier toMachineID = null;
            ObjectIdentifier toPortID = null;
            if (!ObjectIdentifier.isEmpty(equipmentID)) {
                if (log.isDebugEnabled()){
                    log.debug("equipmentID {}", equipmentID);
                }
                toMachineID = equipmentID;
                toPortID = params.getReticlePodPortID();
            } else if (!ObjectIdentifier.isEmpty(stockerID)) {
                if (log.isDebugEnabled()){
                    log.debug("stockerID {}", stockerID);
                }
                toMachineID = stockerID;
                toPortID = params.getResourceID();
            }
            Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
            RetCode<Object> retCode = new RetCode<>();
            retCode.setReturnCode(new OmCode(ex.getCode(),ex.getMessage()));
            retCode.setMessageID(CimObjectUtils.toString(ex.getCode()));
            retCode.setTransactionID(ex.getTransactionID());
            retCode.setMessageText(ex.getMessage());
            retCode.setReasonText(ex.getReasonText());
            reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
            reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(true);
            reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID(params.getReticleDispatchJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID(params.getReticleComponentJobID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_STORE);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(params.getMoveReticleList().get(0).getReticleID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(params.getReticlePodID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(toMachineID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(toPortID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(toMachineID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(toPortID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
            arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon,reticleDispatchAndComponentJobStatusChangeReqParams);
        }
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_store/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_STORE_RPT)
    @Override
    public Response reticleStoreRpt(@RequestBody Params.ReticleStoreRptParams params) {
        String txId = TransactionIDEnum.RETICLE_STORE_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        ObjectIdentifier stockerID = params.getStockerID();
        accessControlCheckInqParams.setStockerID(stockerID);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        try {
            durableService.sxReticleStoreRpt(objCommon, params);
            List<Infos.ReticleStoreResult> strReticleStoreResultSeq = params.getReticleStoreResultList();
            if (!CimObjectUtils.isEmpty(strReticleStoreResultSeq) && CimBooleanUtils.isTrue(strReticleStoreResultSeq.get(0).getReticleStoredFlag())) {
                if (log.isDebugEnabled()){
                    log.debug("reticleStoredFlag == TRUE");
                }
                /*--------------------------------------------------------------*/
                /*   Put Message into Message Queue for Full-Auto               */
                /*--------------------------------------------------------------*/
                Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
                msgQueuePut.setEquipmentID(equipmentID);
                msgQueuePut.setCassetteDispatchReserveFlag(false);
                msgQueuePut.setCassetteTransferReserveFlag(false);
                messageMethod.messageQueuePut(objCommon, msgQueuePut);
            }
        } catch (ServiceException ex) {

            ObjectIdentifier toMachineID = null;
            ObjectIdentifier toPortID = null;
            if (!ObjectIdentifier.isEmpty(equipmentID)) {
                if (log.isDebugEnabled()){
                    log.debug("equipmentID {}", equipmentID);
                }
                toMachineID = equipmentID;
                toPortID = params.getReticlePodPortID();
            } else if (!ObjectIdentifier.isEmpty(stockerID)) {
                if (log.isDebugEnabled()){
                    log.debug("stockerID {}", stockerID);
                }
                toMachineID = stockerID;
                toPortID = params.getResourceID();
            }
            Params.ReticleDispatchAndComponentJobStatusChangeReqParams reticleDispatchAndComponentJobStatusChangeReqParams = new Params.ReticleDispatchAndComponentJobStatusChangeReqParams();
            RetCode<Object> retCode = new RetCode<>();
            retCode.setReturnCode(new OmCode(ex.getCode(),ex.getMessage()));
            retCode.setMessageID(CimObjectUtils.toString(ex.getCode()));
            retCode.setTransactionID(ex.getTransactionID());
            retCode.setMessageText(ex.getMessage());
            retCode.setReasonText(ex.getReasonText());
            reticleDispatchAndComponentJobStatusChangeReqParams.setStxResult(retCode);
            reticleDispatchAndComponentJobStatusChangeReqParams.setIsRequest(false);
            reticleDispatchAndComponentJobStatusChangeReqParams.setRdjID("");
            reticleDispatchAndComponentJobStatusChangeReqParams.setRcjID("");
            reticleDispatchAndComponentJobStatusChangeReqParams.setJobName(BizConstant.SP_RCJ_JOBNAME_STORE);
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticleID(params.getReticleStoreResultList().get(0).getReticleID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setReticlePodID(params.getReticlePodID());
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromMachineID(toMachineID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setFromPortID(toPortID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setToMachineID(toMachineID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setToPortID(toPortID);
            reticleDispatchAndComponentJobStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
            arhsService.sxReticleDispatchAndComponentJobStatusChangeReq(objCommon,reticleDispatchAndComponentJobStatusChangeReqParams);
        }
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_pod_unload/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_UNLOADING_RPT)
    @Override
    public Response reticlePodUnloadRpt (@RequestBody Params.ReticlePodUnloadingRptParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_UNLOADING_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setStockerID(params.getBareReticleStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        durableService.sxReticlePodUnloadingRpt(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_pod_load/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_LOADING_RPT)
    @Override
    public Response reticlePodLoadRpt(@RequestBody Params.ReticlePodLoadingRptParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_LOADING_RPT.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setStockerID(params.getBareReticleStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        durableService.sxReticlePodLoadingRpt(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/brs_online_mode_chg/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.BARE_RETICLE_STOCKER_ONLINE_MODE_CHANGE_REQ)
    @Override
    public Response bRSOnlineModeChgReq(@RequestBody Params.BareReticleStockerOnlineModeChangeReqParams params) {
        String txId = TransactionIDEnum.BARE_RETICLE_STOCKER_ONLINE_MODE_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setStockerID(params.getBareReticleStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        durableService.sxBareReticleStockerOnlineModeChangeReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_pod_inv_update/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_INVENTORY_REQ)
    @Override
    public Response reticlePodInvUpdateReq(@RequestBody Params.ReticlePodInventoryReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_INVENTORY_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setStockerID(params.getStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        Results.ReticlePodInventoryReqResult result = null;
        try {
            result = durableService.sxReticlePodInventoryReq(objCommon, params);
        } catch (ServiceException e) {
            result = e.getData(Results.ReticlePodInventoryReqResult.class);
            if (Validations.isEquals(retCodeConfigEx.getReportedNotMmdurable(), e.getCode())) {
                return Response.createSuccWithOmCode(txId, retCodeConfigEx.getReportedNotMmdurable(), result);
            } else if (Validations.isEquals(retCodeConfigEx.getSomertclinvDataError(), e.getCode())) {
                return Response.createSuccWithOmCode(txId, retCodeConfigEx.getSomertclinvDataError(), result);
            } else {
                throw e;
            }
        }
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/reticle_pod_load_by_offline/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_OFFLINE_LOADING_REQ)
    @Override
    public Response reticlePodLoadByOfflineReq(@RequestBody Params.ReticlePodOfflineLoadingReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_OFFLINE_LOADING_REQ.getValue();
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

        durableService.sxReticlePodOfflineLoadingReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_pod_unload_by_offline/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_OFFLINE_UNLOADING_REQ)
    @Override
    public Response reticlePodUnloadByOfflineReq(@RequestBody Params.ReticlePodOfflineUnloadingReqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_OFFLINE_UNLOADING_REQ.getValue();
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

        durableService.sxReticlePodOfflineUnloadingReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

    @RequestMapping(value = "/reticle_inv_update/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_INVENTORY_REQ)
    @Override
    public Response reticleInvUpdateReq(@RequestBody Params.ReticleInventoryReqParams params) {
        String txId = TransactionIDEnum.RETICLE_INVENTORY_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setStockerID(params.getStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        return Response.createSucc(txId, durableService.sxReticleInventoryReq(objCommon, params));
    }
}
