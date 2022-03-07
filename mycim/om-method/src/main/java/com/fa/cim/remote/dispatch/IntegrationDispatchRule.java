package com.fa.cim.remote.dispatch;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Inputs;
import com.fa.cim.middleware.standard.api.dispatch.DispatchRule;
import com.fa.cim.middleware.standard.pojo.DispatchRequest;
import com.fa.cim.middleware.standard.pojo.DispatchableBox;
import com.fa.cim.middleware.standard.pojo.meta.ChannelMeta;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Map;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/19                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/12/19 16:24
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component("integration-dispatch-rule")
@Slf4j
public class IntegrationDispatchRule implements DispatchRule {

    @Autowired
    private RetCodeConfig retCodeConfig;

    private static final String TRACE_ID = "traceId";
    private static final String EUIPMENT_ID = "equipmentID";


    @Override
    public String parse(DispatchRequest dispatchRequest, String key) {
        Map<String, Object> headers = dispatchRequest.getHeaders();
        String dispatchKey = String.valueOf(headers.get(key));
        Validations.check(CimStringUtils.isEmpty(dispatchKey) || "null".equals(dispatchKey), this.retCodeConfig.getInvalidParameterWithMsg(), new Object[]{String.format("Dispatch Key[%s] is not found in the headers", key)});
        return dispatchKey;
    }

    @SneakyThrows
    @Override
    public DispatchableBox apply(String dispatchKeyName, String dispatchKeyValue, Object body, Map<String, Object> headers, ChannelMeta channelMeta) {
        Inputs.IntegrationHeader integrationHeader = new Inputs.IntegrationHeader();
        Inputs.IntegrationMsgHead integrationMsgHead = new Inputs.IntegrationMsgHead(true);

        Field[] declaredFields = body.getClass().getDeclaredFields();
        String equipmentID = null;
        if (declaredFields.length != 0){
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (CimStringUtils.equals(EUIPMENT_ID,declaredField.getName())){
                    equipmentID = null == declaredField.get(body) ? null : ((ObjectIdentifier) declaredField.get(body)).getValue();
                    break;
                }
            }
        }
        //add other header
        String requestAddr = new StringBuilder(integrationMsgHead.getSrvName()).append(BizConstant.DOT).append(equipmentID).toString();
        String hostName = InetAddress.getLocalHost().getHostName();
        String rqstID = new StringBuilder(requestAddr).append("@").append(hostName).toString();
        String traceID = new StringBuilder(rqstID).append("#").append(integrationMsgHead.getRqstTime()).append(BizConstant.DOT).append(integrationMsgHead.getTxId()).toString();
        //get original traceid if exsit
        if (CimStringUtils.isNotEmpty(MDC.get(TRACE_ID))) {
            String originalTraceID = MDC.get(TRACE_ID);
            log.info("exsit original traceID: {}.", originalTraceID);
            if (originalTraceID.contains(BizConstant.SP_SUBSYSTEMID_TCS) ||
                    originalTraceID.contains(BizConstant.SP_SUBSYSTEMID_SPC) ||
                    originalTraceID.contains(BizConstant.SP_SUBSYSTEMID_APC) ||
                    originalTraceID.contains(BizConstant.SP_SUBSYSTEMID_OCAP) ||
                    originalTraceID.contains(BizConstant.SP_SUBSYSTEMID_AMS) ||
                    originalTraceID.contains(BizConstant.SP_SUBSYSTEMID_XMS) ||
                    originalTraceID.contains(BizConstant.SP_SUBSYSTEMID_ADM)) {
                log.info("original traceID {} contians EAP/SPC/APC/OCAP/AMS/TMS/ADM system keep trace id continue....", originalTraceID);
                traceID = originalTraceID;
            } else {
                log.info("original traceID {} no need cover because of not contains EAP/SPC/APC/OCAP/AMS/TMS/ADM system....", originalTraceID);
            }
        }
        log.info("final integration trace id: {}",traceID);
        integrationMsgHead.setRqstAddr(requestAddr);
        integrationMsgHead.setRqstId(rqstID);
        integrationMsgHead.setTraceId(traceID);
        integrationMsgHead.setService(dispatchKeyValue);
        integrationHeader.setMsgHead(integrationMsgHead);
        integrationHeader.setMsgBody(body);
        return new DispatchableBox(channelMeta, integrationHeader, headers);
    }
}
