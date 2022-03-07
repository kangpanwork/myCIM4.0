package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportRouteCheckReqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 13:03
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportRouteCheckReqService implements ITransportRouteCheckReqService {
    @Autowired
    private IMCSManager mcsManager;

    public Results.TransportRouteCheckReqResult sxTransportRouteCheckReq(Infos.ObjCommon objCommon, Params.TransportRouteCheckReqParams transportRouteCheckReqParams) {
        Results.TransportRouteCheckReqResult result = new Results.TransportRouteCheckReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        result = mcsManager.sendTransportJRouteCheckReq(objCommon, transportRouteCheckReqParams);
        return result;
    }
}

