package com.fa.cim.tms.remote.remoteBack;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/4                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/4 16:02
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
public class CallMcsDefaultCheckBack implements CallbackService<Response> {
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @Override
    public Response doCallback(Response response) {
        Validations.check(null == response, msgRetCodeConfig.getMsgMcsNoResponse());
        if (!Validations.isSuccess(response.getCode())){
            throw new ServiceException(response.getCode(), response.getMessage(), response.getTransactionID());
        }
        return response;
    }
}
