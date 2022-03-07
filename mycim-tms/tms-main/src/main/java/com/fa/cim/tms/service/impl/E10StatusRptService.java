package com.fa.cim.tms.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IE10StatusRptService;
import com.fa.cim.tms.utils.BooleanUtils;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class E10StatusRptService implements IE10StatusRptService {
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    public Results.E10StatusReportResult sxE10StatusRpt(@RequestBody Infos.ObjCommon objCommon, Params.E10StatusReportParmas e10StatusReportParmas) {
        Results.E10StatusReportResult result = new Results.E10StatusReportResult();

        /*--------------------------------------------*/
        /* Send TxStockerStatusChangeRpt to OMS       */
        /*--------------------------------------------*/
        Boolean castTxNoSendWarning = false;
        log.info("【step1】 - omsManager.sendStockerStatusChangeRpt");
        try {
            result = omsManager.sendStockerStatusChangeRpt(
                    objCommon,
                    e10StatusReportParmas.getMachineID(),
                    e10StatusReportParmas.getE10Status(),
                    e10StatusReportParmas.getClaimMemo());
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                log.error("oms cast no send or shutdown");
                castTxNoSendWarning = true;
            }else {
                throw e;
            }
        }

        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning),result,msgRetCodeConfig.getMsgOmsCastTxNoSend());
        return result;
    }
}

