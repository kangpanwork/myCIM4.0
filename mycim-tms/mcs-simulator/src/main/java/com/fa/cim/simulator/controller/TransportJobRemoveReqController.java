package com.fa.cim.simulator.controller;

import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/27                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/27 13:10
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobRemoveReqController {

    @ResponseBody
    @RequestMapping(value = "/transport_job_remove/req", method = RequestMethod.POST)
    public Response transportJobRemoveReq(@RequestBody Params.TransportJobRemoveReqParams transportJobRemoveReqParams) {
        final String transactionID = TransactionIDEnum.OM07.getValue();
        // TODO: 2020/10/27 confirm the functiion
        return null;
    }
}
