package com.fa.cim.controller.reticle;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ErrorCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.reticle.IReticleController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.reticle.IReticleService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IReticleController.class, confirmableKey = "ReticleConfirm", cancellableKey = "ReticleCancel")
@RequestMapping("/rtl")
@Listenable
public class ReticleController implements IReticleController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IReticleService reticleService;

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_inspection_request/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_INSPECTION_REQUEST)
    @Override
    public Response reticleInspectionRequestReq(@RequestBody Params.ReticleInspectionReuqstReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_INSPECTION_REQUEST.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2 - txReticleInspectionRequstReq
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleInspectionRequst(objCommon, params);List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }

        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_inspection_in/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_INSPECTION_IN)
    @Override
    public Response reticleInspectionInReq(@RequestBody Params.ReticleInspectionInReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_INSPECTION_IN.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2 - txReticleInspectionRequstReq
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleInspectionIn(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }

        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_inspection_out/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_INSPECTION_OUT)
    @Override
    public Response reticleInspectionOutReq(@RequestBody Params.ReticleInspectionOutReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_INSPECTION_OUT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2 - txReticleInspectionRequstReq
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleInspectionOut(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }

        return Response.createSucc(objCommon.getTransactionID(), null);
    }


    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/confirm_mask_quality/req")
    @CimMapping(TransactionIDEnum.RETICLE_CONFIRM_MASK_QUALITY)
    @Override
    public Response reticleConfirmMaskQualityReq(@RequestBody Params.ReticleConfirmMaskQualityParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_CONFIRM_MASK_QUALITY.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);

        // step2 - txConfirmMaskQuality
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxConfirmMaskQuality(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_hold/req")
    @CimMapping(TransactionIDEnum.RETICLE_HOLD)
    @Override
    public Response reticleHoldReq(@RequestBody Params.ReticleHoldReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_HOLD.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);

        // step2 - txHold
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleHold(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_hold_release/req")
    @CimMapping(TransactionIDEnum.RETICLE_HOLD_RELEASE)
    @Override
    public Response reticleHoldReleaseReq(@RequestBody Params.ReticleHoldReleaseReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_HOLD_RELEASE.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);

        // step2 - txHoldRelease
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleHoldRelease(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)) {
            return Response.createSuccWithOmCode(transactionID, retCodeConfig.getSomeRequestsFailed(), reticleOperationExceptionResult);
        }
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_request_repair/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_REQUEST_REPAIR)
    @Override
    public Response reticleRequestRepairReq(@RequestBody Params.ReticleRequestRepairReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_REQUEST_REPAIR.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2 - txReticleInspectionRequstReq
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleRequestRepair(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }

        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_repair_in/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_REPAIR_IN)
    @Override
    public Response reticleRepairInReq(@RequestBody Params.ReticleRepairInReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_REPAIR_IN.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2 - txReticleInspectionRequstReq
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleRepairIn(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }

        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/reticle_repair_out/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_REPAIR_OUT)
    @Override
    public Response reticleRepairOutReq(@RequestBody Params.ReticleRepairOutReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_REPAIR_OUT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2 - txReticleInspectionRequstReq
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleRepairOut(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }

        return Response.createSucc(objCommon.getTransactionID(), null);
    }



    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_terminate/req")
    @CimMapping(TransactionIDEnum.RETICLE_TERMINATE)
    @Override
    public Response reticleTerminateReq(@RequestBody Params.ReticleTerminateReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_TERMINATE.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);

        // step2 - txReticleTerminate
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleTerminate(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_terminate_cancel/req")
    @CimMapping(TransactionIDEnum.RETICLE_TERMINATE_CANCEL)
    @Override
    public Response reticleTerminateCancelReq(@RequestBody Params.ReticleTerminateReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_TERMINATE_CANCEL.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);

        // step2 - txReticleTerminateCancel
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleTerminateCancel(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_scrap/req")
    @CimMapping(TransactionIDEnum.RETICLE_SCRAP)
    @Override
    public Response reticleScrapReq(@RequestBody Params.ReticleScrapReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_SCRAP.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);

        // step2 - txReticleScrap
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleScrap(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_scrap_cancel/req")
    @CimMapping(TransactionIDEnum.RETICLE_SCRAP_CANCEL)
    @Override
    public Response reticleScrapCancelReq(@RequestBody Params.ReticleScrapReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_SCRAP_CANCEL.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);

        // step2 - sxReticleScrapCancel
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = reticleService.sxReticleScrapCancel(objCommon, params);
        List<Results.ReticleOperationException> reticleOperationExceptionList = reticleOperationExceptionResult.getReticleOperationExceptionList();
        if (!CimArrayUtils.isEmpty(reticleOperationExceptionList)){
            return Response.createSuccWithOmCode(transactionID,retCodeConfig.getSomeRequestsFailed(),reticleOperationExceptionResult);
        }
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_scan_request/req")
    @CimMapping(TransactionIDEnum.RETICLE_SCAN_REQUEST)
    @Override
    public Response reticleScanRequestReq(@RequestBody Params.ReticleScanRequestReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_SCAN_REQUEST.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);
        //step2  -  call sxReticleScanRequest
        reticleService.sxReticleScanRequest(objCommon,params);
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_scan_complete/req")
    @CimMapping(TransactionIDEnum.RETICLE_SCAN_COMPLETE)
    @Override
    public Response reticleScanCompleteReq(@RequestBody Params.ReticleScanCompleteReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_SCAN_COMPLETE.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);
        //step2  -  call sxReticleScanComplete
        reticleService.sxReticleScanComplete(objCommon,params);
        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/reticle_inspection_type_change/req")
    @CimMapping(TransactionIDEnum.RETICLE_INSPECTION_TYPE_CHANGE)
    @Override
    public Response reticleInspectionTypeChangeReq(@RequestBody Params.ReticleInspectionTypeChangeReqParams params) {
        final String transactionID = TransactionIDEnum.RETICLE_INSPECTION_TYPE_CHANGE.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        // step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, checkInqParams);
        //step2  -  call sxReticleInspectionTypeChange
        reticleService.sxReticleInspectionTypeChange(objCommon,params);
        return Response.createSucc(objCommon.getTransactionID(), null);
    }


}
