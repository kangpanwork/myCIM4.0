package com.fa.cim.simulator.controller;

import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Results;
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
 * @date: 2020/10/27 12:58
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobAbortReqController {

    @ResponseBody
    @RequestMapping(value = "/transport_job_abort/req", method = RequestMethod.POST)
    public Results.TransportJobAbortReqResult transportJobAbortReq(@RequestBody Params.TransportJobAbortReqParams transportJobAbortReqParams) {
        final String transactionID = TransactionIDEnum.OM06.getValue();
        // TODO: 2020/10/27 confrim the function
        return null;
    }
}