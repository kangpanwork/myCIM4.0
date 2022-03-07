package com.fa.cim.remote.remoteBack;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * description: MES call OCAP call back service
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/8                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2021/7/8 14:51
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class CallOCAPBackWithResponse implements CallbackService<Object> {

    @Override
    public Object doCallback(Object result) {
        log.info("MES -> OCAP Integration response: {}", JSONObject.toJSONString(result));
        if (result instanceof Response){
            Response response = (Response)result;
            throw new ServiceException(response.getCode(), response.getMessage(), response.getTransactionID());
        }
        return result;
    }
}
