package com.fa.cim.newIntegration.equipment.scase;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.dto.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/9                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/9 18:01
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class GatePassCase {

    @Autowired
    private LotController lotController;

    public Response passThruReq(Params.PassThruReqParams params){
        return lotController.passThruReq(params);
    }
}
