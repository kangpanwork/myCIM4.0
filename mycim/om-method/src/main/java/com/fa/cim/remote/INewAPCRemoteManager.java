package com.fa.cim.remote;

import com.fa.cim.dto.Infos;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.remote.remoteBack.CallAPCBackWithResponse;
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
@CimRemoteManager("oms-new-apc-service")
public interface INewAPCRemoteManager extends RemoteManager {

    //APCReserveReq interface
    @Callback(CallAPCBackWithResponse.class)
    @Dispatchable("APCReserveReq")
    Object apcReserveReq(@RequestBody Infos.APCParamInfo param);

    //APCParamInq interface
    @Callback(CallAPCBackWithResponse.class)
    @Dispatchable("APCParamInq")
    Object apcParamInq(@RequestBody Infos.APCParamInfo param);
}