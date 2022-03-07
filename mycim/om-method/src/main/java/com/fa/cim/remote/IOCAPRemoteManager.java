package com.fa.cim.remote;

import com.fa.cim.dto.Inputs;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.remote.remoteBack.CallOCAPBackWithResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description: MES call OCAP remote service
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/28                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/5/28 16:06
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager(value = "oms-ocap-service")
public interface IOCAPRemoteManager extends RemoteManager {

    //common interface
    @Dispatchable("TcHandleCompleteMessageReq")
    @Callback(CallOCAPBackWithResponse.class)
    Object tcHandleCompleteMessageReq(@RequestBody Inputs.OcapInput ocapInput);
}
