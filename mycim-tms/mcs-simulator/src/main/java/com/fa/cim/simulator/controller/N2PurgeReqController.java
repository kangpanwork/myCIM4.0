package com.fa.cim.simulator.controller;

import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Results;
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
 * @date: 2020/10/27 12:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class N2PurgeReqController {

    @RequestMapping(value = "/n2_purge/req",method = RequestMethod.POST)
    public Results.N2PurgeReqResult n2PurgeReq(@RequestBody Params.N2PurgeReqParams n2PurgeReqParams){
        final String transactionID = TransactionIDEnum.OM13.getValue();
        // TODO: 2020/10/27 confrim the function
        return null;
    }
}
