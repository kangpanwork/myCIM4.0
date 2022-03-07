package com.fa.cim.controller.constraint;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.constraint.IConstraintController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
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

import javax.validation.Valid;
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
@Compensable(interfaceClass = IConstraintController.class, confirmableKey = "ConstraintConfirm", cancellableKey = "ConstraintCancel")
@RequestMapping("/constraint")
@Listenable
public class ConstraintController implements IConstraintController {
    @Autowired
    private IConstraintService mfgRestrictCancelReqService;
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IConstraintService constraintService;

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_CANCEL_REQ)
    public Response mfgRestrictCancelReq(@RequestBody Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_CANCEL_REQ;

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), mfgRestrictCancelReqParams.getUser(), accessControlCheckInqParams);

        //Step-2:txMfgRestrictReq__101;
        log.debug("【Step-4】call-txMfgRestrictCancelReq__101(...)");
        mfgRestrictCancelReqService.sxMfgRestrictCancelReq(mfgRestrictCancelReqParams, objCommon);

        // Step-3:Post Process(Generate Output Results/event log put/Set Transaction ID);
        return Response.createSucc(transactionID.getValue(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict/req", method = RequestMethod.POST)
    @Override
    @CimMapping(names = "OCONW001_EX")//OCONW001 接口和AMS 联调时/constraint/mfg_restrict/req 和/mfg/mfg_restrict/req 无法区分。更改CimMapping OCONW001_EX
    public Response mfgRestrictReq(@RequestBody Params.MfgRestrictReqParams mfgRestrictReqParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-1:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), mfgRestrictReqParams.getUser(), accessControlCheckInqParams);

        //Step-2:txMfgRestrictReq__101;
        log.debug("【Step-4】call-sxMfgRestrictReq(...)");
        Infos.EntityInhibitDetailInfo result = constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_exclusion_lot_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_EXCEPTION_LOT_CANCEL_REQ)
    public Response mfgRestrictExclusionLotCancelReq(@RequestBody Params.MfgRestrictExclusionLotReqParams cancelParams) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_EXCEPTION_LOT_CANCEL_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        int size = CimArrayUtils.getSize(cancelParams.getEntityInhibitExceptionLots());
        if (size > 0) {
            for (Infos.EntityInhibitExceptionLot item : cancelParams.getEntityInhibitExceptionLots()) {
                lotIDLists.add(item.getLotID());
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), cancelParams.getUser(), accessControlCheckInqParams);

        //Step-3:sxMfgRestrictExclusionLotCancelReq;
        log.debug("【Step-4】call-sxMfgRestrictExclusionLotCancelReq(...)");
        constraintService.sxMfgRestrictExclusionLotCancelReq(cancelParams, objCommon);

        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);

        return Response.createSucc(transactionID.getValue(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/mfg_restrict_exclusion_lot/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENTITY_INHIBIT_EXCEPTION_LOT_REQ)
    public Response mfgRestrictExclusionLotReq(@RequestBody @Valid Params.MfgRestrictExclusionLotReqParams params) {
        //Step-0:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.ENTITY_INHIBIT_EXCEPTION_LOT_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //Step-2:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        int size = CimArrayUtils.getSize(params.getEntityInhibitExceptionLots());
        if (size > 0) {
            for (Infos.EntityInhibitExceptionLot item : params.getEntityInhibitExceptionLots()) {
                lotIDLists.add(item.getLotID());
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);

        //Step-3:txMfgRestrictExclusionLotReq;
        log.debug("【Step-4】call-sxMfgRestrictExclusionLotReq(...)");
        constraintService.sxMfgRestrictExclusionLotReq(params, objCommon);

        return Response.createSucc(transactionID.getValue(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/constraint_eqp_add/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CONSTRAINT_EQP_ADD_REQ)
    public Response constraintEqpAddReq(@RequestBody Params.ConstraintEqpAddReqParams params) {
        String transactionID = TransactionIDEnum.CONSTRAINT_EQP_ADD_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step3 call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        return Response.createSucc(transactionID, constraintService.sxConstraintEqpAddReq(objCommon, params.getEntityInhibitDetailAttributes(), params.getClaimMemo()));
    }

    @ResponseBody
    @RequestMapping(value = "/constraint_eqp_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CONSTRAINT_EQP_MODIFY_REQ)
    public Response constraintEqpModifyReq(@RequestBody Params.ConstraintEqpModifyReqParams params) {
        String transactionID = TransactionIDEnum.CONSTRAINT_EQP_MODIFY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step3 call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        return Response.createSucc(transactionID, constraintService.sxConstraintEqpModifyReq(objCommon, params.getEntityInhibitions(), params.getClaimMemo()));
    }

    @ResponseBody
    @RequestMapping(value = "/constraint_eqp_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.CONSTRAINT_EQP_CANCEL_REQ)
    public Response constraintEqpCancelReq(@RequestBody Params.ConstraintEqpCancelReqParams params) {
        String transactionID = TransactionIDEnum.CONSTRAINT_EQP_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step3 call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), new Params.AccessControlCheckInqParams(true));

        constraintService.sxConstraintEqpCancelReq(objCommon, params.getEntityInhibitions(), params.getClaimMemo());
        return Response.createSucc(transactionID, null);
    }
}