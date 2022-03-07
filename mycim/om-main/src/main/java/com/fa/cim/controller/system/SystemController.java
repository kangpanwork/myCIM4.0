package com.fa.cim.controller.system;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.systemConfig.ISystemController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.system.ISystemService;
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
 * @author: Nyx
 * @date: 2019/7/30 14:27
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = ISystemController.class, confirmableKey = "SystemConfirm", cancellableKey = "SystemCancel")
@RequestMapping("/sys")
@Listenable
public class SystemController implements ISystemController {
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private ISystemService systemService;

    @ResponseBody
    @RequestMapping(value = "/oms_env_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENV_VARIABLE_UPDATE_REQ)
    public Response omsEnvModifyReq(@RequestBody Params.OMSEnvModifyReqParams params) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        final String transactionID = TransactionIDEnum.ENV_VARIABLE_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        systemService.sxOMSEnvModifyReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @RequestMapping(value = "/alert_message/rpt", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SYSTEM_MSG_RPT)
    public Response alertMessageRpt(@RequestBody Params.AlertMessageRptParams alertMessageRptParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.SYSTEM_MSG_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setEquipmentID(alertMessageRptParams.getEquipmentID());
        privilegeCheckParams.setStockerID(alertMessageRptParams.getStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, alertMessageRptParams.getUser(), privilegeCheckParams);

        //【step3】call sxAlertMessageRpt(...)
        Results.AlertMessageRptResult result = systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        return Response.createSucc(transactionID, result);
    }
}