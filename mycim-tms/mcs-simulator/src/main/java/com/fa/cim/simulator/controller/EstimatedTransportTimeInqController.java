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
 * @date: 2020/10/27 12:32
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class EstimatedTransportTimeInqController {


    @RequestMapping(value = "/estimated_transport_time/inq", method = RequestMethod.POST)
    public Response estimatedTransportTimeInq(@RequestBody Params.EstimatedTransportTimeInqParams estimatedTransportTimeInqParams) {
        final String transactionID = TransactionIDEnum.OM16.getValue();
        // TODO: 2020/10/27 confirm the function
        return null;
    }
}
