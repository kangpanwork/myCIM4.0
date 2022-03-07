package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IEstimatedTransportTimeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class EstimatedTransportTimeInqService implements IEstimatedTransportTimeInqService {
    @Autowired
    private IMCSManager mcsManager;

    public Results.EstimatedTransportTimeInqResult sxEstimatedTransportTimeInq(Infos.ObjCommon objCommon, Params.EstimatedTransportTimeInqParams estimatedTransportTimeInqParams){
        Results.EstimatedTransportTimeInqResult result = new Results.EstimatedTransportTimeInqResult();
        Infos.EstimatedTarnsportTimeInq tempEstimatedTarnsportTimeInq = new Infos.EstimatedTarnsportTimeInq();
        tempEstimatedTarnsportTimeInq.setFromMachineID(estimatedTransportTimeInqParams.getEstimatedTarnsportTime().getFromMachineID());
        tempEstimatedTarnsportTimeInq.setToMachineID(estimatedTransportTimeInqParams.getEstimatedTarnsportTime().getToMachineID());
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendEstimatedTarnsportTimeInq");
        result = mcsManager.sendEstimatedTarnsportTimeInq(objCommon, tempEstimatedTarnsportTimeInq);
        return result;
    }
}
