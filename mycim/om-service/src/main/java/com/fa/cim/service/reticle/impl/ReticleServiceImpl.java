package com.fa.cim.service.reticle.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.method.IReticleMethod;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.service.reticle.IReticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@OmService
public class ReticleServiceImpl implements IReticleService {

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Override
    public Results.ReticleOperationExceptionResult sxReticleInspectionRequst(Infos.ObjCommon objCommon, Params.ReticleInspectionReuqstReqParams params) {
        String inspectionType = params.getInspectionType();
        Validations.check(CimStringUtils.isEmpty(inspectionType),retCodeConfig.getInvalidInputParam());
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDList = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDList) {
            //Step1 - Lock the target object - object_Lock
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
            try {
                //inspection request
                //set the reticle to target location and status
                reticleMethod.reticleInspectionRequest(objCommon, reticleID, inspectionType);
                //todo:make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_INSPECTION_REQUEST);
                reticleOperationEventMakeParams.setInspectionType(inspectionType);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            } catch (ServiceException e) {
                Results.ReticleOperationException reticleOperationException = new Results.ReticleOperationException();
                reticleOperationExceptionList.add(reticleOperationException);
                reticleOperationException.setReticleID(reticleID);
                reticleOperationException.setOmCode(e.getCode());
                reticleOperationException.setErrorMessage(e.getMessage());
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleInspectionIn(Infos.ObjCommon objCommon, Params.ReticleInspectionInReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDList = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDList) {
            //Step1 - Lock the target object - object_Lock
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
            try {
                //set the reticle to target location and status
                reticleMethod.reticleInspectionIn(objCommon, reticleID);
                //make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_INSPECTION_IN);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            } catch (ServiceException e) {
                Results.ReticleOperationException reticleOperationException = new Results.ReticleOperationException();
                reticleOperationExceptionList.add(reticleOperationException);
                reticleOperationException.setReticleID(reticleID);
                reticleOperationException.setOmCode(e.getCode());
                reticleOperationException.setErrorMessage(e.getMessage());
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleInspectionOut(Infos.ObjCommon objCommon, Params.ReticleInspectionOutReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDList = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDList) {
            //Step1 - Lock the target object - object_Lock
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
            try {
                //set the reticle to target location and status
                String inspectionOut = reticleMethod.reticleInspectionOut(objCommon, reticleID, params.isOperationFlag(), params.getClaimMemo());
                //todo:make event
                if (params.isOperationFlag()){
                    Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                    reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                    reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_INSPECTION_OUT_SUCESS);
                    reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                    reticleOperationEventMakeParams.setInspectionType(inspectionOut);
                    eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
                }else {
                    Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                    reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                    reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_INSPECTION_OUT_FAIL);
                    reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                    reticleOperationEventMakeParams.setInspectionType(inspectionOut);
                    eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
                }
            } catch (ServiceException e) {
                Results.ReticleOperationException reticleOperationException = new Results.ReticleOperationException();
                reticleOperationExceptionList.add(reticleOperationException);
                reticleOperationException.setReticleID(reticleID);
                reticleOperationException.setOmCode(e.getCode());
                reticleOperationException.setErrorMessage(e.getMessage());
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxConfirmMaskQuality(Infos.ObjCommon objCommon, Params.ReticleConfirmMaskQualityParams params) {
        //Step1 - Lock the target object - object_Lock
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDs = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDs) {
            try {
                //Step1 - Lock the target object - object_Lock
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
                //set the reticle to target location and status
                reticleMethod.confirmMaskQuality(objCommon, reticleID);
                // make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_CONFIRM_MASK_QUALITY);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            } catch (ServiceException e) {
                this.extractedException(reticleOperationExceptionList, reticleID, e);
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleHold(Infos.ObjCommon objCommon, Params.ReticleHoldReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDs = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDs) {
            try {
                //Step1 - Lock the target object - object_Lock
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
                // save holdRecord
                reticleMethod.reticleHold(objCommon, reticleID, params);
                //set the reticle to target location and status
                reticleMethod.changeRecticleState(objCommon, reticleID, BizConstant.SP_RETICLE_HOLD);
                // make event
                this.makeEventData(reticleID, objCommon, BizConstant.SP_RETICLE_HOLD, params.getHoldReqList());
            } catch (ServiceException e) {
                this.extractedException(reticleOperationExceptionList, reticleID, e);
            }
        }

        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleHoldRelease(Infos.ObjCommon objCommon, Params.ReticleHoldReleaseReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDs = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDs) {
            try {
                //Step1 - Lock the target object - object_Lock
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
                // release holdRecord
                reticleMethod.reticleHoldRelease(objCommon, reticleID, params);
                //set the reticle to target location and status
                reticleMethod.changeRecticleState(objCommon, reticleID, BizConstant.SP_RETICLE_HOLD_RELEASE);
                // make event
                Inputs.ReticleOperationEventMakeParams eventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(eventMakeParams, reticleID);
                eventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_HOLD_RELEASE);
                if (ObjectIdentifier.isNotEmpty(params.getReleaseReasonCodeID())){
                    eventMakeParams.setReasonCode(params.getReleaseReasonCodeID().getValue());
                }
                eventMakeParams.setClaimMemo(params.getClaimMemo());
                eventMethod.reticleOperationEventMake(objCommon, eventMakeParams);
            } catch (ServiceException e) {
                this.extractedException(reticleOperationExceptionList, reticleID, e);
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleRequestRepair(Infos.ObjCommon objCommon, Params.ReticleRequestRepairReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDList = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDList) {
            //Step1 - Lock the target object - object_Lock
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
            try {
                //set the reticle to target location and status
                reticleMethod.reticleRequestRepair(objCommon, reticleID);
                // make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_REQUEST_REPAIR);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            } catch (ServiceException e) {
                Results.ReticleOperationException reticleOperationException = new Results.ReticleOperationException();
                reticleOperationExceptionList.add(reticleOperationException);
                reticleOperationException.setReticleID(reticleID);
                reticleOperationException.setOmCode(e.getCode());
                reticleOperationException.setErrorMessage(e.getMessage());
            }
        }
        return reticleOperationExceptionResult;
    }


    @Override
    public Results.ReticleOperationExceptionResult sxReticleRepairIn(Infos.ObjCommon objCommon, Params.ReticleRepairInReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDList = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDList) {
            //Step1 - Lock the target object - object_Lock
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
            try {
                //set the reticle to target location and status
                reticleMethod.reticleRepairIn(objCommon, reticleID);
                // make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_REPAIR_IN);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            } catch (ServiceException e) {
                Results.ReticleOperationException reticleOperationException = new Results.ReticleOperationException();
                reticleOperationExceptionList.add(reticleOperationException);
                reticleOperationException.setReticleID(reticleID);
                reticleOperationException.setOmCode(e.getCode());
                reticleOperationException.setErrorMessage(e.getMessage());
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleRepairOut(Infos.ObjCommon objCommon, Params.ReticleRepairOutReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDList = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDList) {
            //Step1 - Lock the target object - object_Lock
            objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
            try {
                //set the reticle to target location and status
                reticleMethod.reticleRepairOut(objCommon, reticleID, params.getClaimMemo());
                // make event
                Inputs.ReticleOperationEventMakeParams reticleOperationEventMakeParams = new Inputs.ReticleOperationEventMakeParams();
                reticleMethod.reticleEventInfoSet(reticleOperationEventMakeParams, reticleID);
                reticleOperationEventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_REPAIR_OUT);
                reticleOperationEventMakeParams.setClaimMemo(params.getClaimMemo());
                eventMethod.reticleOperationEventMake(objCommon, reticleOperationEventMakeParams);
            } catch (ServiceException e) {
                Results.ReticleOperationException reticleOperationException = new Results.ReticleOperationException();
                reticleOperationExceptionList.add(reticleOperationException);
                reticleOperationException.setReticleID(reticleID);
                reticleOperationException.setOmCode(e.getCode());
                reticleOperationException.setErrorMessage(e.getMessage());
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleTerminate(Infos.ObjCommon objCommon, Params.ReticleTerminateReqParams params) {
        List<ObjectIdentifier> reticleIDs = params.getReticleIDList();
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        for (ObjectIdentifier reticleID : reticleIDs) {
            try {
                //Step1 - Lock the target object - object_Lock
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
                //set the reticle to target location and status
                reticleMethod.changeRecticleState(objCommon, reticleID, BizConstant.SP_RETICLE_TERMINATE);
                // set reticle Record
                reticleMethod.reticleTerminate(objCommon, reticleID, params);
                // make event
                this.constructEventData(reticleID, objCommon, BizConstant.SP_RETICLE_TERMINATE,
                        params.getTerminateReqList());
            } catch (ServiceException e) {
                extractedException(reticleOperationExceptionList, reticleID, e);
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleTerminateCancel(Infos.ObjCommon objCommon, Params.ReticleTerminateReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        for (ObjectIdentifier reticleID : params.getReticleIDList()) {
            try {
                //Step1 - Lock the target object - object_Lock
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
                //set the reticle to target location and status
                reticleMethod.changeRecticleState(objCommon, reticleID, BizConstant.SP_RETICLE_TERMINATE_CANCEL);
                // set reticle record
                reticleMethod.reticleTerminateCancel(objCommon, reticleID, params);
                // make event
                this.constructEventData(reticleID, objCommon, BizConstant.SP_RETICLE_TERMINATE_CANCEL,
                        params.getTerminateReqList());
            } catch (ServiceException e) {
                extractedException(reticleOperationExceptionList, reticleID, e);
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleScrap(Infos.ObjCommon objCommon, Params.ReticleScrapReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDs = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDs) {
            try {
                //Step1 - Lock the target object - object_Lock
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
                //set the reticle to target location and status
                reticleMethod.changeRecticleState(objCommon, reticleID, BizConstant.SP_RETICLE_SCRAP);
                // set reticle record
                reticleMethod.reticleScrap(objCommon, reticleID, params);
                // make event
                this.constructEventData(reticleID, objCommon, BizConstant.SP_RETICLE_SCRAP,
                       params.getReqList());
            } catch (ServiceException e) {
                this.extractedException(reticleOperationExceptionList, reticleID, e);
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public Results.ReticleOperationExceptionResult sxReticleScrapCancel(Infos.ObjCommon objCommon, Params.ReticleScrapReqParams params) {
        Results.ReticleOperationExceptionResult reticleOperationExceptionResult = new Results.ReticleOperationExceptionResult();
        List<Results.ReticleOperationException> reticleOperationExceptionList = new ArrayList<>();
        reticleOperationExceptionResult.setReticleOperationExceptionList(reticleOperationExceptionList);
        List<ObjectIdentifier> reticleIDs = params.getReticleIDList();
        for (ObjectIdentifier reticleID : reticleIDs) {
            try {
                //Step1 - Lock the target object - object_Lock
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
                //set the reticle to target location and status
                reticleMethod.changeRecticleState(objCommon, reticleID, BizConstant.SP_RETICLE_SCRAP_CANCEL);
                // set reticle record
                reticleMethod.reticleScrapCancel(objCommon, reticleID, params);
                // make event
                this.constructEventData(reticleID, objCommon, BizConstant.SP_RETICLE_SCRAP_CANCEL, params.getReqList());
            } catch (ServiceException e) {
                this.extractedException(reticleOperationExceptionList, reticleID, e);
            }
        }
        return reticleOperationExceptionResult;
    }

    @Override
    public void sxReticleScanRequest(Infos.ObjCommon objCommon, Params.ReticleScanRequestReqParams params) {
        Validations.check(CimObjectUtils.isEmpty(params), retCodeConfig.getInvalidInputParam());
        ObjectIdentifier reticleID = params.getReticleID();
        Validations.check(ObjectIdentifier.isEmpty(reticleID), retCodeConfig.getInvalidInputParam());
        //Step1 - Lock the target object - object_Lock
        objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
        // reticleScanRequest
        reticleMethod.reticleScanRequest(objCommon, reticleID);
        // make event
        Inputs.ReticleOperationEventMakeParams eventMakeParams = new Inputs.ReticleOperationEventMakeParams();
        reticleMethod.reticleEventInfoSet(eventMakeParams, reticleID);
        eventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_SCAN_REQUEST);
        eventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.reticleOperationEventMake(objCommon, eventMakeParams);
    }

    @Override
    public void sxReticleScanComplete(Infos.ObjCommon objCommon, Params.ReticleScanCompleteReqParams params) {
        Validations.check(CimObjectUtils.isEmpty(params), retCodeConfig.getInvalidInputParam());
        ObjectIdentifier reticleID = params.getReticleID();
        Validations.check(ObjectIdentifier.isEmpty(reticleID), retCodeConfig.getInvalidInputParam());
        boolean resultFlag = params.isResultFlag();
        //Step1 - Lock the target object - object_Lock
        objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
        // make event
        Inputs.ReticleOperationEventMakeParams eventMakeParams = new Inputs.ReticleOperationEventMakeParams();
        reticleMethod.reticleEventInfoSet(eventMakeParams, reticleID);
        eventMakeParams.setClaimMemo(params.getClaimMemo());
        if (resultFlag) {
            //Check ok, release all the hold and change to idle
            ObjectIdentifier releaseReasonCodeID = params.getReleaseReasonCodeID();
            reticleMethod.reticleScanCompleteSuccess(objCommon, reticleID, releaseReasonCodeID);
            if (ObjectIdentifier.isNotEmpty(releaseReasonCodeID)) {
                eventMakeParams.setReasonCode(releaseReasonCodeID.getValue());
            }
            eventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_SCAN_COMPLETE_SUCCESS);
        } else {
            //check fail, add the hold if needed and change to hold
            Infos.ReticleReasonReq reticleHoldReason = params.getReticleHoldReason();
            reticleMethod.reticleScanCompleteFail(objCommon, reticleID, reticleHoldReason);
            if (!CimObjectUtils.isEmpty(reticleHoldReason)) {
                if (ObjectIdentifier.isNotEmpty(reticleHoldReason.getHoldReasonCodeID())) {
                    eventMakeParams.setReasonCode(reticleHoldReason.getHoldReasonCodeID().getValue());
                }
            }
            eventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_SCAN_COMPLETE_FAIL);
        }
        eventMethod.reticleOperationEventMake(objCommon, eventMakeParams);
    }

    @Override
    public void sxReticleInspectionTypeChange(Infos.ObjCommon objCommon, Params.ReticleInspectionTypeChangeReqParams params) {
        Validations.check(CimObjectUtils.isEmpty(params), retCodeConfig.getInvalidInputParam());
        ObjectIdentifier reticleID = params.getReticleID();
        Validations.check(ObjectIdentifier.isEmpty(reticleID), retCodeConfig.getInvalidInputParam());
        String inspectionType = params.getInspectionType();
        Validations.check(CimStringUtils.isEmpty(inspectionType), retCodeConfig.getInvalidInputParam());
        //Step1 - Lock the target object - object_Lock
        objectLockMethod.objectLock(objCommon, CimProcessDurable.class, reticleID);
        //update the inspection type
        reticleMethod.reticleInspectionTypeChange(objCommon, reticleID, inspectionType);
        // make event
        Inputs.ReticleOperationEventMakeParams eventMakeParams = new Inputs.ReticleOperationEventMakeParams();
        reticleMethod.reticleEventInfoSet(eventMakeParams, reticleID);
        eventMakeParams.setOpeCategory(BizConstant.SP_RETICLE_INSPECTION_TYPE_CHANGE);
        eventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.reticleOperationEventMake(objCommon, eventMakeParams);
    }


    /**
     * description: construct event data
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/10 11:16
     * @param reticleID -
     * @param objCommon -
     * @param objCommon -
     * @param reqList -
     * @return
     */
    private void constructEventData(ObjectIdentifier reticleID, Infos.ObjCommon objCommon, String action,
                                    List<Infos.ReticleReasonReq> reqList) {
        Inputs.ReticleOperationEventMakeParams eventMakeParams = new Inputs.ReticleOperationEventMakeParams();
        reticleMethod.reticleEventInfoSet(eventMakeParams, reticleID);
        eventMakeParams.setOpeCategory(action);
        if (!CollectionUtils.isEmpty(reqList)) {
            Infos.ReticleReasonReq reasonReq = reqList.get(0);
            eventMakeParams.setClaimMemo(reasonReq.getClaimMemo());
            eventMakeParams.setReasonCode(reasonReq.getHoldReasonCodeID().getValue());
        }
        eventMethod.reticleOperationEventMake(objCommon, eventMakeParams);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/10 11:27
     * @param reticleID -
     * @param objCommon -
     * @param action -
     * @param holdReqList2 -
     * @return
     */
    private void makeEventData(ObjectIdentifier reticleID, Infos.ObjCommon objCommon,String action, List<Infos.ReticleHoldReq> holdReqList2) {
        Inputs.ReticleOperationEventMakeParams eventMakeParams = new Inputs.ReticleOperationEventMakeParams();
        reticleMethod.reticleEventInfoSet(eventMakeParams, reticleID);
        eventMakeParams.setOpeCategory(action);
        List<Infos.ReticleHoldReq> holdReqList = holdReqList2;
        if (!CollectionUtils.isEmpty(holdReqList)) {
            Infos.ReticleHoldReq reticleHoldReq = holdReqList.get(0);
            eventMakeParams.setClaimMemo(reticleHoldReq.getClaimMemo());
            eventMakeParams.setReasonCode(reticleHoldReq.getHoldReasonCodeID().getValue());
        }
        eventMethod.reticleOperationEventMake(objCommon, eventMakeParams);
    }

    /**
     * description: extracted Exception info
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param reticleOperationExceptionList - reticle exception info list
     * @param reticleID                     - reticle id
     * @param e                             - exception info
     * @return
     * @author salt
     * @date 2021/1/6 19:56
     */
    private void extractedException(List<Results.ReticleOperationException> reticleOperationExceptionList, ObjectIdentifier reticleID, ServiceException e) {
        Results.ReticleOperationException reticleOperationException = new Results.ReticleOperationException();

        reticleOperationException.setReticleID(reticleID);
        reticleOperationException.setOmCode(e.getCode());
        reticleOperationException.setErrorMessage(e.getMessage());

        reticleOperationExceptionList.add(reticleOperationException);
    }

}
