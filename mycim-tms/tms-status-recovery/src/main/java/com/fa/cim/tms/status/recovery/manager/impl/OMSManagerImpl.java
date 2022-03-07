package com.fa.cim.tms.status.recovery.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.tms.status.recovery.dto.CimRequest;
import com.fa.cim.tms.status.recovery.dto.OMSParams;
import com.fa.cim.tms.status.recovery.dto.Results;
import com.fa.cim.tms.status.recovery.enums.TransactionIDEnum;
import com.fa.cim.tms.status.recovery.manager.IOMSManager;
import com.fa.cim.tms.status.recovery.pojo.Infos;
import com.fa.cim.tms.status.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.status.recovery.remote.IToOmsRemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/20        ********             Miner               create file
 *
 * @author: Miner
 * @date: 2020/2/20 12:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class OMSManagerImpl implements IOMSManager {

    @Autowired
    private IToOmsRemoteManager toOmsRemoteManager;


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/2 18:22
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.E10StatusReportResult sendStockerStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier sotckerStatusCode, String claimMemo) {
        Results.E10StatusReportResult e10StatusReportResult = new Results.E10StatusReportResult();
        String functionId = TransactionIDEnum.STOCKER_STATUS_CHANGE_RPT.getValue();
        OMSParams.StockerStatusChangeRptParams stockerStatusChangeRptParams = new OMSParams.StockerStatusChangeRptParams();
        objCommon.getUser().setFunctionID(functionId);
        stockerStatusChangeRptParams.setUser(objCommon.getUser());
        stockerStatusChangeRptParams.setStockerID(stockerID);
        stockerStatusChangeRptParams.setStockerStatusCode(sotckerStatusCode);
        stockerStatusChangeRptParams.setClaimMemo(claimMemo);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, stockerStatusChangeRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("ERM -> OMS mq sendStockerStatusChangeRpt request {}", request);
        Response response = null;
        try {
            response = toOmsRemoteManager.sendStockerStatusChangeRpt(stockerStatusChangeRptParams);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error("oms meet some error");
        }
        log.info("ERM -> OMS mq sendStockerStatusChangeRpt response {}", response);
        if (null != response && null != response.getBody()) {
            ObjectIdentifier objectIdentifier = JSON.parseObject(response.getBody().toString(), ObjectIdentifier.class);
            e10StatusReportResult.setMachineID(objectIdentifier);
        }
        return e10StatusReportResult;
    }


}
