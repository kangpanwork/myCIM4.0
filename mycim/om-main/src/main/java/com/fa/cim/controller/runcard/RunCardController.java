package com.fa.cim.controller.runcard;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.runCard.IRunCardController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.runcard.IRunCardService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/12 15:17
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IRunCardController.class, confirmableKey = "RunCardConfirm", cancellableKey = "RunCardCancel")
@RequestMapping("/rc")
@Listenable
public class RunCardController implements IRunCardController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IRunCardService runCardService;

    @ResponseBody
    @RequestMapping(value = "/run_card_update/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RC_UPDATE_REQ)
    public Response runCardUpdateReq(@RequestBody Params.RunCardUpdateReqParams params) {
        String txId = TransactionIDEnum.RC_UPDATE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step1 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step2 - sxRunCardUpdateReq
        runCardService.sxRunCardUpdateReq(objCommon, params);

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/run_card_delete/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RC_DELETE_REQ)
    public Response runCardDeleteReq(@RequestBody Params.RunCardDeleteReqParams params) {
        String txId = TransactionIDEnum.RC_DELETE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step1 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step2 - sxRunCardDeleteReq
        runCardService.sxRunCardDeleteReq(objCommon, params.getRunCardIDs());

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/run_card_approval/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RC_APPROVAL_REQ)
    public Response runCardApprovalReq(@RequestBody Params.RunCardStateApprovalReqParams params) {
        String txId = TransactionIDEnum.RC_APPROVAL_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step1 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step2 - sxRunCardApprovalReq
        runCardService.sxRunCardApprovalReq(objCommon, params);

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/run_card_state_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RC_STATE_CHANGE_REQ)
    public Response runCardStateChangeReq(@RequestBody Params.RunCardStateChangeReqParams params) {
        String txId = TransactionIDEnum.RC_STATE_CHANGE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step1 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step2 - sxRunCardApprovalReq
        runCardService.sxRunCardStateChangeReq(objCommon, params);

        return Response.createSucc(txId, null);
    }


}
