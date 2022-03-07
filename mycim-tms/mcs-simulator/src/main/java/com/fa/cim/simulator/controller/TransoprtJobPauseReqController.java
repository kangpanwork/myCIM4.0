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
public class TransoprtJobPauseReqController {


    @ResponseBody
    @RequestMapping(value = "/transport_job_pause/req", method = RequestMethod.POST)
    public Response transportJobPauseReq(@RequestBody Params.TransportJobPauseReqParams transportJobPauseReqParams) {
        final String transactionID = TransactionIDEnum.OM03.getValue();
        // TODO: 2020/10/27 confrim the function 
        return null;
    }
}
