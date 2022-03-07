package com.fa.cim.remote.remoteBack;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.entity.nonruntime.CimSmEapDO;
import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CallTcsBackWithOutResponse implements CallbackService<String> {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public String doCallback(String result) {
        if (!CimStringUtils.isEmpty(result)){
            CimSmEapDO cimSmEapDO = JSON.parseObject(result, CimSmEapDO.class);
            if (Integer.parseInt(cimSmEapDO.getCode())==0){
                return result;
            } else {
                //error message
                throw new ServiceException(new OmCode(Integer.parseInt(cimSmEapDO.getCode()), cimSmEapDO.getMessage()));
            }
        }
        throw new ServiceException(retCodeConfig.getTcsNoResponse());
    }
}
