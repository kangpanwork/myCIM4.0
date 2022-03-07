package com.fa.cim.remote;

import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.remote.remoteBack.CallSPCBackWithResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/18                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/12/18 19:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager(value = "oms-spc-service")
public interface ISPCRemoteManager extends RemoteManager {

    //common interface
    @Callback(CallSPCBackWithResponse.class)
    @Dispatchable("SPCCheckReq")
    List<Results.SpcCheckResult> doSPCCheckReq(@RequestBody Inputs.SpcInput param);
}
