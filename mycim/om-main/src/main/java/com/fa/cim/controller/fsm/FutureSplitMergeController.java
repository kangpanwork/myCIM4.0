package com.fa.cim.controller.fsm;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.fsm.IFutureSplitMergeController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.fsm.IFutureSplitMergeService;
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
 * @author: Nyx
 * @date: 2019/7/31 16:05
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IFutureSplitMergeController.class, confirmableKey = "FutureSplitMergeConfirm", cancellableKey = "FutureSplitMergeCancel")
@RequestMapping("/fsm")
@Listenable
public class FutureSplitMergeController  implements IFutureSplitMergeController {
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IFutureSplitMergeService futureSplitMergeService;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @ResponseBody
    @RequestMapping(value = "/fsm_lot_remove/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FSM_LOT_REMOVE_REQ)
    public Response fsmLotRemoveReq(@RequestBody com.fa.cim.fsm.Params.FSMLotRemoveReqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.FSM_LOT_REMOVE_REQ.getValue();
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
        futureSplitMergeService.sxFSMLotRemoveReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/fsm_lot_action/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FSM_LOT_ACTION_REQ)
    public Response fsmLotActionReq(@RequestBody com.fa.cim.fsm.Params.FSMLotActionReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FSM_LOT_ACTION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】calendar_GetCurrentTimeDR
        //【step2】 txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> LotIds = new ArrayList<>();
        LotIds.add(params.getLotId());
        accessControlCheckInqParams.setLotIDLists(LotIds);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】 txPSMLotActionReq
        List<ObjectIdentifier> retCode = futureSplitMergeService.sxFSMLotActionReq(objCommon, params.getLotId(), params.getClaimMemo());

        return Response.createSucc(transactionID, retCode);
    }

    @ResponseBody
    @RequestMapping(value = "/fsm_lot_info_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FSM_LOT_INFO_SET_REQ)
    public Response fsmLotInfoSetReq(@RequestBody com.fa.cim.fsm.Params.FSMLotInfoSetReqParams params) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.FSM_LOT_INFO_SET_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】 txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        routeIDs.add(params.getSplitRouteID());
        routeIDs.add(params.getOriginalRouteID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】 txPSMLotInfoSetReq
        try {
            futureSplitMergeService.sxFSMLotInfoSetReq(objCommon, params);
        } catch (ServiceException e) {
            if (Validations.isEquals(e.getCode(),retCodeConfigEx.getFsmExplotAlreadyChanged())){
                return Response.createSuccWithOmCode(transactionID, new OmCode(retCodeConfigEx.getSucc().getCode(), retCodeConfigEx.getFsmExplotAlreadyChanged().getMessage()),null);
            }else {
                throw e;
            }
        }

        return Response.createSucc(transactionID, null);
    }

}
