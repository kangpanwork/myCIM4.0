package com.fa.cim.controller.pcs;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.processControlScript.IProcessControlScriptController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.pcs.IProcessControlScriptService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/30 18:01
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IProcessControlScriptController.class, confirmableKey = "ProcessControlScriptConfirm", cancellableKey = "ProcessControlScriptCancel")
@RequestMapping("/pcs")
@Listenable
public class ProcessControlScriptController implements IProcessControlScriptController {
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IProcessControlScriptService processControlScriptService;

    @ResponseBody
    @RequestMapping(value = "/pcs_parameter_value_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.USER_PARAMETER_VALUE_CHANGE_REQ)
    public Response pcsParameterValueSetReq(@RequestBody Params.PCSParameterValueSetReqParams params) {
        final String txId = TransactionIDEnum.USER_PARAMETER_VALUE_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        processControlScriptService.sxPCSParameterValueSetReq(objCommon, params);
        return Response.createSucc(txId);
    }

    @ResponseBody
    @PostMapping("/script_run/req")
    @Override
    @CimMapping(TransactionIDEnum.PCS_RUN_REQ)
    public Response runProcessControlScriptReq(@RequestBody Params.ProcessControlScriptRunReqParams params) {
        log.info("ProcessControlScriptController::runProcessControlScriptReq()");
        String txId = TransactionIDEnum.PCS_RUN_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        log.info("【step1】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        log.info("【step2】call sxProcessControlScriptRunReq(...)");
        processControlScriptService.sxProcessControlScriptRunReq(objCommon, params);
        return Response.createSucc(txId, null);
    }
}