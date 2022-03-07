package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IN2PurgeReqService;
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
 * @date: 2020/10/20 13:38
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class N2PurgeReqService implements IN2PurgeReqService {
    @Autowired
    private IMCSManager mcsManager;

    public Results.N2PurgeReqResult sxN2PurgeReq(Infos.ObjCommon objCommon, Params.N2PurgeReqParams n2PurgeReqParams) {
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendN2PurgeReq");
        return mcsManager.sendN2PurgeReq(objCommon, n2PurgeReqParams);
    }
}
