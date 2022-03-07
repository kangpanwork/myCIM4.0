package com.fa.cim.tms.event.recovery.remote.remoteBack;

import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import com.fa.cim.tms.event.recovery.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/5                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/5 14:57
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class CallTmsDefaultCheckBack implements CallbackService<Response> {
    @Override
    public Response doCallback(Response response) {
        if (null == response || "null".equals(response)) {
            return Response.createError(response.getTransactionID(), "tms response is null");
        }
        return response;
    }
}
