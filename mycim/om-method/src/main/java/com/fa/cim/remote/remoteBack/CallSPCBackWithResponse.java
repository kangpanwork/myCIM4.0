package com.fa.cim.remote.remoteBack;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.caller.service.CallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * description: MES call SPC call back service
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
public class CallSPCBackWithResponse implements CallbackService<List<Results.SpcCheckResult>> {

    @Autowired
    private RetCodeConfigEx retCodeConfig;

    @Override
    public List<Results.SpcCheckResult> doCallback(List<Results.SpcCheckResult> result) {
        log.info("MES -> SPC Integration response: {}", JSONObject.toJSONString(result));
        return Optional.ofNullable(result).map(res -> {
            List<Results.SpcCheckResult> response = JSONArray.parseArray(JSONObject.toJSONString(result),
                    Results.SpcCheckResult.class);
            if (CimArrayUtils.isNotEmpty(response)){
                response.forEach(spcCheckResult -> {
                    Validations.check(null == spcCheckResult,retCodeConfig.getSpcNoResponse());
                    Validations.check(!CimStringUtils.equals(spcCheckResult.getErrorCode(),BizConstant.SPC_SUCCESS_CODE)
                                    && !CimStringUtils.equals(spcCheckResult.getErrorCode(),BizConstant.SPC_NO_JOB_DEFINITION_CODE)
                                    && !CimStringUtils.equals(spcCheckResult.getErrorCode(),BizConstant.SPC_INVALID_INPUT_PARAM_ERROR_CODE)
                                    && !CimStringUtils.equals(spcCheckResult.getErrorCode(),BizConstant.SPC_JOB_IN_OCAP_FLOW_CODE)
                            , new OmCode(Integer.parseInt(spcCheckResult.getErrorCode()),spcCheckResult.getErrorMessage()));
                });
            }
            return response;
        }).orElseThrow(() -> new ServiceException(retCodeConfig.getSpcNoResponse()));
    }
}
