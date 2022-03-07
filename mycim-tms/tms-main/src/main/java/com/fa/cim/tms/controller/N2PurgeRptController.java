package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IN2PurgeRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IN2PurgeRptService;
import com.fa.cim.tms.utils.ThreadContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * <p>N2PurgeReportController .<br/></p>
 * * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/1/21        ********             miner               create file
 *
 * @author: miner
 * @date: 2019/1/21 14:24
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class N2PurgeRptController implements IN2PurgeRptController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IN2PurgeRptService n2PurgeRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/n2_purge/rpt", method = RequestMethod.POST)
    public Response tmsN2PurgeRpt(@RequestBody Params.N2PurgeReportParams n2PurgeReportParams) {
        log.info("tmsN2PurgeRpt Request Json" + JSON.toJSONString(n2PurgeReportParams));
        Results.N2PurgeReportResult result = new Results.N2PurgeReportResult();
        final String transactionID = TransactionIDEnum.TM13.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = n2PurgeReportParams.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - n2PurgeRptService.sxN2PurgeRpt");
        result = n2PurgeRptService.sxN2PurgeRpt(objCommon, n2PurgeReportParams);
        log.info("tmsN2PurgeRpt Response Json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
