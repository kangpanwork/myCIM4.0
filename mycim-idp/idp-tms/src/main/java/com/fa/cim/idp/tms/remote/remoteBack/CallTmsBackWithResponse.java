package com.fa.cim.idp.tms.remote.remoteBack;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/28                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/5/28 18:04
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class CallTmsBackWithResponse implements CallbackService<Response> {



    @Override
    public Response doCallback(Response response) {
        log.info("tms reply success");
        //if response is error throw the error message
        if (response.getCode() != 0){
            log.error("tms reply response have some error");
            throw new ServiceException(response.getCode(),response.getMessage(),response.getTransactionID());
        }
        return response;
    }
}
