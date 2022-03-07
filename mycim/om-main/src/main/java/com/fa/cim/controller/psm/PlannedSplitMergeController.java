package com.fa.cim.controller.psm;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.plannedSplitMerge.IPlannedSplitMergeController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.psm.IPlannedSplitMergeService;
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
 * 2019/7/29          ********            Nyx                create file
 * 2019/9/25         #######            Neko                Refactoring
 *
 * @author Nyx
 * @since 2019/7/29 17:20
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IPlannedSplitMergeController.class, confirmableKey = "PlannedSplitMergeConfirm", cancellableKey = "PlannedSplitMergeCancel")
@RequestMapping("/psm")
@Listenable
public class PlannedSplitMergeController implements IPlannedSplitMergeController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IPlannedSplitMergeService plannedSplitMergeService;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @ResponseBody
    @RequestMapping(value = "/psm_lot_remove/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EXPERIMENTAL_LOT_DELETE_REQ)
    public Response psmLotRemoveReq(@RequestBody Params.PSMLotRemoveReqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.EXPERIMENTAL_LOT_DELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check params
        //【step1】calendar_GetCurrentTimeDR
        //【step2】 txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        routeIDs.add(params.getSplitRouteID());
        routeIDs.add(params.getOriginalRouteID());
        accessControlCheckInqParams.setRouteIDList(routeIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 txPSMLotRemoveReq
        plannedSplitMergeService.sxPSMLotRemoveReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/psm_lot_action/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EXPERIMENTAL_LOT_EXEC_REQ)
    public Response psmLotActionReq(@RequestBody Params.PSMLotActionReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.EXPERIMENTAL_LOT_EXEC_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】calendar_GetCurrentTimeDR
        //【step2】 txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> LotIds = new ArrayList<>();
        LotIds.add(params.getLotId());
        accessControlCheckInqParams.setLotIDLists(LotIds);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】 txPSMLotActionReq
        List<ObjectIdentifier> retCode = plannedSplitMergeService.sxPSMLotActionReq(objCommon, params.getLotId(), params.getClaimMemo());

        return Response.createSucc(transactionID, retCode);
    }

    @ResponseBody
    @RequestMapping(value = "/psm_lot_info_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EXPERIMENTAL_LOT_UPDATE_REQ)
    public Response psmLotInfoSetReq(@RequestBody Params.PSMLotInfoSetReqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.EXPERIMENTAL_LOT_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】 txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        routeIDs.add(params.getSplitRouteID());
        routeIDs.add(params.getOriginalRouteID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 txPSMLotInfoSetReq
        try {
            plannedSplitMergeService.sxPSMLotInfoSetReq(objCommon, params);
        } catch (ServiceException e) {
            if (Validations.isEquals(e.getCode(),retCodeConfigEx.getExplotAlreadyChanged())){
                return Response.createSuccWithOmCode(transactionID, new OmCode(retCodeConfigEx.getSucc().getCode(), retCodeConfigEx.getExplotAlreadyChanged().getMessage()),null);
            }else {
                throw e;
            }
        }

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/rc_psm_lot_info_set/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RC_PSM_ADD_REQ)
    public Response runCardPSMLotInfoSetReq(@RequestBody Params.PSMLotInfoSetReqParams params) {
        return psmLotInfoSetReq(params);
    }

    @ResponseBody
    @RequestMapping(value = "/rc_psm_lot_remove/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RC_PSM_DELETE_REQ)
    public Response runCardPSMLotRemoveReq(@RequestBody Params.PSMLotRemoveReqParams params) {
        return psmLotRemoveReq(params);
    }
}