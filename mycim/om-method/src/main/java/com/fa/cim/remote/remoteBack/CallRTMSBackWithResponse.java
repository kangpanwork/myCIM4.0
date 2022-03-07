package com.fa.cim.remote.remoteBack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/22        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/7/22 14:21
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class CallRTMSBackWithResponse implements CallbackService<Object> {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public Object doCallback(Object result) {
        log.info("MES -> RTMS Integration response: {}", JSONObject.toJSONString(result));
        if (!CimObjectUtils.isEmpty(result)) {
            Response response = JSON.parseObject(result.toString(), Response.class);
            Validations.check(response.getCode() != 0, response.getCode(), response.getMessage());
            return response.getBody();
        }
        throw new CimIntegrationException(retCodeConfig.getTcsNoResponse().getCode(),
                retCodeConfig.getTcsNoResponse().getMessage());
    }
}