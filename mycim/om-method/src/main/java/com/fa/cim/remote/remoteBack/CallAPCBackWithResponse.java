package com.fa.cim.remote.remoteBack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * description: MES call APC call back service
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
public class CallAPCBackWithResponse implements CallbackService<Object> {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public Object doCallback(Object result) {
        log.info("MES -> APC Integration response: {}", JSONObject.toJSONString(result));
        return Optional.ofNullable(result).map(res -> {
            Response response = JSON.parseObject(res.toString(), Response.class);
            Validations.check(response.getCode() != 0, response.getCode(), response.getMessage());
            Validations.check(null == response.getBody(), retCodeConfig.getNoResponseApc());
            return response.getBody();
        }).orElseThrow(() -> new CimIntegrationException(retCodeConfig.getNoResponseApc().getCode(),
                retCodeConfig.getNoResponseApc().getMessage()));
    }
}
