package com.fa.cim.simulator.controller;

import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/27                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/27 11:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class CarrierInfoChangeReqController {


    @RequestMapping(value = "/carrier_info_change/Req", method = RequestMethod.POST)
    public Response CarrierInfoChangeReq(@RequestBody Params.CarrierInfoChangeReqParam carrierInfoChangeReqParam) {
        final String transcationID = TransactionIDEnum.OM15.getValue();
        // TODO: 2020/10/27 confrim the function
        return null;
    }
}
