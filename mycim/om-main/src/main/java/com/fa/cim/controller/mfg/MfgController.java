package com.fa.cim.controller.mfg;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.constraint.IConstraintController;
import com.fa.cim.controller.interfaces.mfg.IMfgController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.mfg.MfgInfoImportParams;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.constraint.IConstraintService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/31 9:58
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IConstraintController.class, confirmableKey = "MfgConfirm", cancellableKey = "MfgCancel")
@RequestMapping("/mfg")
@Listenable
public class MfgController implements IMfgController {
    @Autowired
    private IConstraintService mfgRestrictCancelReqService;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IConstraintService constraintService;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private RetCodeConfig retCodeConfig;


    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(names = "OCONW002_EX")  //OCONW002接口联调时 constraint和mfg冲突
    public Response mfgRestrictCancelReq(@RequestBody Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_CANCEL_REQ;

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), mfgRestrictCancelReqParams.getUser(), accessControlCheckInqParams);

        List<Infos.EntityInhibitDetailInfo> entityInhibitions = mfgRestrictCancelReqParams.getEntityInhibitions();
        List<Infos.ConstraintEqpDetailInfo> toolEntityInhibitions = new ArrayList<>();
        List<Infos.EntityInhibitDetailInfo> generalEntityInhibitions = new ArrayList<>();
        for (Infos.EntityInhibitDetailInfo entityInhibitDetailInfo : entityInhibitions){
            boolean specTool = entityInhibitDetailInfo.isSpecTool();
            if (specTool){
                Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo = new Infos.ConstraintEqpDetailInfo();
                constraintEqpDetailInfo.setEntityInhibitID(entityInhibitDetailInfo.getEntityInhibitID());
                toolEntityInhibitions.add(constraintEqpDetailInfo);
            }else {
                generalEntityInhibitions.add(entityInhibitDetailInfo);
            }
        }
        mfgRestrictCancelReqParams.setEntityInhibitions(generalEntityInhibitions);
        //Step-2:txMfgRestrictReq__101;
        log.debug("【Step-4】call-txMfgRestrictCancelReq__101(...)");
        mfgRestrictCancelReqService.sxMfgRestrictCancelReq(mfgRestrictCancelReqParams, objCommon);
        constraintService.sxConstraintEqpCancelReq(objCommon, toolEntityInhibitions, mfgRestrictCancelReqParams.getClaimMemo());
        // Step-3:Post Process(Generate Output Results/event log put/Set Transaction ID);
        return Response.createSucc(transactionID.getValue(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_REQ)
    public Response mfgRestrictReq(@RequestBody Params.MfgRestrictReq_110Params params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);

        //Step-2:txMfgRestrictReq__101;
        log.debug("【Step-4】call-sxMfgRestrictReq(...)");
        List<Infos.FailMfgRequest> failMfgRequestList = new ArrayList<>();
        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = params.getEntityInhibitDetailAttributeList();
        List<Infos.EntityInhibitDetailAttributes> generalEntityInhibitDetailAttributeList = new ArrayList<>();
        for (Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes : entityInhibitDetailAttributeList){
            boolean specTool = entityInhibitDetailAttributes.isSpecTool();
            if (specTool){
                Infos.ConstraintDetailAttributes constraintDetailAttributes = new Infos.ConstraintDetailAttributes();
                constraintDetailAttributes.setExceptionLotList(entityInhibitDetailAttributes.getEntityInhibitExceptionLotInfos());
                constraintDetailAttributes.setClaimedTimeStamp(entityInhibitDetailAttributes.getClaimedTimeStamp());
                constraintDetailAttributes.setEndTimeStamp(entityInhibitDetailAttributes.getEndTimeStamp());
                constraintDetailAttributes.setEntities(entityInhibitDetailAttributes.getEntities());
                constraintDetailAttributes.setExceptionEntities(entityInhibitDetailAttributes.getExceptionEntities());
                constraintDetailAttributes.setFunctionRule(entityInhibitDetailAttributes.getFunctionRule());
                constraintDetailAttributes.setMemo(entityInhibitDetailAttributes.getMemo());
                constraintDetailAttributes.setOwnerID(entityInhibitDetailAttributes.getOwnerID());
                constraintDetailAttributes.setReasonCode(entityInhibitDetailAttributes.getReasonCode());
                constraintDetailAttributes.setSpecTool(entityInhibitDetailAttributes.isSpecTool());
                constraintDetailAttributes.setStartTimeStamp(entityInhibitDetailAttributes.getStartTimeStamp());
                constraintDetailAttributes.setSubLotTypes(entityInhibitDetailAttributes.getSubLotTypes());
                try {
                    constraintService.sxConstraintEqpAddReq(objCommon, constraintDetailAttributes, params.getClaimMemo());
                } catch (ServiceException e) {
                    Infos.FailMfgRequest failMfgRequest = new Infos.FailMfgRequest();
                    failMfgRequest.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
                    failMfgRequest.setReasonCode(e.getCode());
                    failMfgRequest.setMessageText(e.getMessage());
                    failMfgRequestList.add(failMfgRequest);
                }
            }else {
                generalEntityInhibitDetailAttributeList.add(entityInhibitDetailAttributes);
            }
        }
        params.setEntityInhibitDetailAttributeList(generalEntityInhibitDetailAttributeList);
        Infos.MfgMMResponse result = constraintService.sxMfgRestrictReq_110(params, objCommon);
        List<Infos.FailMfgRequest> mfgRequestList = result.getFailMfgRequestList();
        mfgRequestList.addAll(failMfgRequestList);
        if (!CimArrayUtils.isEmpty(mfgRequestList)){
            return Response.createSuccWithOmCode(transactionID.getValue(),retCodeConfig.getSomeRequestsFailed(),result);
        }
        return Response.createSucc(transactionID.getValue(), null);
    }



    @ResponseBody
    @RequestMapping(value = "/mfg_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_MODIFY_REQ)
    public Response mfgModifyReq(@RequestBody Params.MfgModifyMMReqParams mfgModifyMMReqParams) {
        User user = mfgModifyMMReqParams.getUser();
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_MODIFY_REQ;
        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), user, accessControlCheckInqParams);

        Infos.EntityInhibitDetailInfo modifyEntityInhibitDetailInfo = mfgModifyMMReqParams.getModifyEntityInhibitDetailInfo();
        Infos.EntityInhibitDetailAttributes inhibitDetailAttributes = modifyEntityInhibitDetailInfo.getEntityInhibitDetailAttributes();
        if (!inhibitDetailAttributes.isSpecTool()) {
            Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams = new Params.MfgRestrictCancelReqParams();
            mfgRestrictCancelReqParams.setUser(user);
            mfgRestrictCancelReqParams.setReasonCode(mfgModifyMMReqParams.getReasonCode());
            mfgRestrictCancelReqParams.setClaimMemo("");
            List<Infos.EntityInhibitDetailInfo> entityInhibitDetailInfos = new ArrayList<>();
            entityInhibitDetailInfos.add(modifyEntityInhibitDetailInfo);
            mfgRestrictCancelReqParams.setEntityInhibitions(entityInhibitDetailInfos);
            constraintService.sxMfgRestrictCancelReq(mfgRestrictCancelReqParams, objCommon);

            Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
            List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
            entityInhibitDetailAttributeList.add(inhibitDetailAttributes);
            mfgRestrictReq_110Params.setClaimMemo("");
            mfgRestrictReq_110Params.setUser(user);
            mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
            Infos.MfgMMResponse mfgMMResponse = constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params, objCommon);
            List<Infos.FailMfgRequest> failMfgRequestList = mfgMMResponse.getFailMfgRequestList();
            if (CimArrayUtils.isNotEmpty(failMfgRequestList)){
                Infos.FailMfgRequest failMfgRequest = failMfgRequestList.get(0);
                if (log.isErrorEnabled()){
                    log.error(failMfgRequest.getMessageText());
                }
                Validations.check(true,failMfgRequest.getReasonCode(),failMfgRequest.getMessageText());
            }
        } else {
            Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo = new Infos.ConstraintEqpDetailInfo();
            constraintEqpDetailInfo.setEntityInhibitID(modifyEntityInhibitDetailInfo.getEntityInhibitID());
            Infos.ConstraintDetailAttributes constraintDetailAttributes = new Infos.ConstraintDetailAttributes();
            constraintDetailAttributes.setClaimedTimeStamp(inhibitDetailAttributes.getClaimedTimeStamp());
            constraintDetailAttributes.setEndTimeStamp(inhibitDetailAttributes.getEndTimeStamp());
            constraintDetailAttributes.setEntities(inhibitDetailAttributes.getEntities());
            constraintDetailAttributes.setExceptionEntities(inhibitDetailAttributes.getExceptionEntities());
            constraintDetailAttributes.setExceptionLotList(inhibitDetailAttributes.getEntityInhibitExceptionLotInfos());
            constraintDetailAttributes.setFunctionRule(inhibitDetailAttributes.getFunctionRule());
            constraintDetailAttributes.setMemo(inhibitDetailAttributes.getMemo());
            constraintDetailAttributes.setOwnerID(inhibitDetailAttributes.getOwnerID());
            constraintDetailAttributes.setReasonCode(inhibitDetailAttributes.getReasonCode());
            constraintDetailAttributes.setSpecTool(inhibitDetailAttributes.isSpecTool());
            constraintDetailAttributes.setStartTimeStamp(inhibitDetailAttributes.getStartTimeStamp());
            constraintDetailAttributes.setStatus(inhibitDetailAttributes.getStatus());
            constraintDetailAttributes.setSubLotTypes(inhibitDetailAttributes.getSubLotTypes());
            constraintEqpDetailInfo.setEntityInhibitDetailAttributes(constraintDetailAttributes);
            constraintService.sxConstraintEqpModifyReq(objCommon, constraintEqpDetailInfo, mfgModifyMMReqParams.getClaimMemo());
        }
        return Response.createSucc(transactionID.getValue(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_time_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_RECIPE_TIME_REQ)
    public Response mfgRecipeTimeLimitSet(@RequestBody Params.RecipeTimeSetParams params) {
        String txId = TransactionIDEnum.ENTITY_INHIBIT_RECIPE_TIME_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        constraintService.sxMfgRecipeTimeLimitSet(params, objCommon);
        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_time_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_RECIPE_TIME_CANCEL)
    public Response mfgRecipeTimeLimitDelete(@RequestBody Params.RecipeTimeCancelParams params) {
        String txId = TransactionIDEnum.ENTITY_INHIBIT_RECIPE_TIME_CANCEL.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //Step1 - calendar_GetCurrentTimeDR

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        constraintService.sxMfgRecipeTimeLimitDelete(params, objCommon);
        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_use_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(names = "CS_RCP_SET")
    public Response mfgRecipeUseSet(@RequestBody Params.RecipeUseSetParams params) {
        constraintService.sxMfgRecipeUseSet(params);
        return Response.createSucc("CS_RCP_SET", null);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_use_Check/req", method = RequestMethod.POST)
    @Override
    @CimMapping(names = "CS_RCP_CHECK")
    public Response mfgRecipeUseCheck(@RequestBody User user) {
        String txId = TransactionIDEnum.ENTITY_INHIBIT_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //Step1 - calendar_GetCurrentTimeDR
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);
        constraintService.sxMfgRecipeUseCheck(objCommon);
        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_info_import/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_IMPORT_REQ)
    public Response mfgInfoImportReq(MfgInfoImportParams mfgInfoImportParams) {
        String txId = TransactionIDEnum.ENTITY_INHIBIT_IMPORT_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //Step1 - calendar_GetCurrentTimeDR
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        User user = new User();
        user.setFunctionID(mfgInfoImportParams.getFunctionID());
        user.setPassword(mfgInfoImportParams.getPassword());
        user.setUserID(ObjectIdentifier.build(mfgInfoImportParams.getValue(),mfgInfoImportParams.getReferenceKey()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);
        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributesList = constraintService.sxConstraintInfoImportReq(objCommon, mfgInfoImportParams.getFile());
        return Response.createSucc(txId, entityInhibitDetailAttributesList);
    }

}