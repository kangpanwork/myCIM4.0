package com.fa.cim.controller.qtime;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: 提供给qTime-Sentinel调用
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/9/3         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/9/3 3:03 下午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@RequestMapping("/notifier")
@Listenable
public class NotifierController {

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ISystemService systemService;

    @RequestMapping(value = "/alert_message/rpt", method = RequestMethod.POST)
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
