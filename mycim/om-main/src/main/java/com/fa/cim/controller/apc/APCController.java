package com.fa.cim.controller.apc;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.apc.IAPCController;
import com.fa.cim.controller.interfaces.autoMonitor.IAutoMonitorInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.apc.IAPCService;
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
 * 2020/5/7          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/7 11:27
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IAutoMonitorInqController.class, confirmableKey = "APCConfirm", cancellableKey = "APCCancel")
@RequestMapping("/apc")
@Listenable
public class APCController implements IAPCController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IAPCService apcService;

    @ResponseBody
    @RequestMapping(value = "/apc_interface_ops/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.APC_IF_POINT_REQ)
    @Override
    public Response APCInterfaceOpsReq(@RequestBody Params.APCIFPointReqParams apcifPointReqParams) {
        //init params
        final String transactionID = TransactionIDEnum.APC_IF_POINT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, apcifPointReqParams.getUser(), accessControlCheckInqParams);
        //【step3】call txAPCIFPointReq(...)
        apcService.sxAPCInterfaceOpsReq(objCommon, apcifPointReqParams.getOperation(), apcifPointReqParams.getApcIf());
        return Response.createSucc(transactionID);
    }
}