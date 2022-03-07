package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.IE10StatusRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IE10StatusRptService;
import com.fa.cim.tms.utils.ThreadContextHolder;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 14:22
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class E10StatusRptController implements IE10StatusRptController {

    @Autowired
    private IE10StatusRptService e10StatusRptService;
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @ResponseBody
    @RequestMapping(value = "/e10Status/rpt", method = RequestMethod.POST)
    public Response tmsE10StatusReport(@RequestBody Params.E10StatusReportParmas e10StatusReportParmas) {
        Results.E10StatusReportResult result = new Results.E10StatusReportResult();
        log.info("Request Json" + JSON.toJSONString(e10StatusReportParmas));
        final String transactionID = TransactionIDEnum.TM05.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = e10StatusReportParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - e10StatusRptService.sxE10StatusRpt");
        try {
            result = e10StatusRptService.sxE10StatusRpt(objCommon, e10StatusReportParmas);
        } catch (ServiceException e) {
            result = e.getData(Results.E10StatusReportResult.class);
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("E10StatusReportController Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
