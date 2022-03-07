package com.fa.cim.remote;

import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/22          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/22 14:10
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager("oms-apc-service")
public interface IAPCRemoteManager extends RemoteManager {

    Outputs.ApcOut sendInfo(@RequestBody Inputs.ApcIn in);
}