package com.fa.cim.simulator.remote.dispatch;

import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.middleware.standard.api.dispatch.DispatchRule;
import com.fa.cim.middleware.standard.pojo.DispatchRequest;
import com.fa.cim.middleware.standard.pojo.DispatchableBox;
import com.fa.cim.middleware.standard.pojo.meta.ChannelMeta;
import com.fa.cim.simulator.pojo.Infos;
import com.fa.cim.simulator.pojo.ObjectIdentifier;
import com.fa.cim.simulator.utils.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
        Validations.check(StringUtils.isEmpty(dispatchKey) || "null".equals(dispatchKey), this.retCodeConfig.getInvalidParameterWithMsg(), new Object[]{String.format("Dispatch Key[%s] is not found in the headers", key)});
        return dispatchKey;
    }

    @SneakyThrows
    @Override
    public DispatchableBox apply(String dispatchKeyName, String dispatchKeyValue, Object body, Map<String, Object> headers, ChannelMeta channelMeta) {
        Infos.IntegrationHeader integrationHeader = new Infos.IntegrationHeader();
        Infos.IntegrationMsgHead integrationMsgHead = new Infos.IntegrationMsgHead(true);

        Field[] declaredFields = body.getClass().getDeclaredFields();
        String equipmentID = null;
        if (declaredFields.length != 0){
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (StringUtils.equals(EUIPMENT_ID,declaredField.getName())){
                    equipmentID = null == declaredField.get(body) ? null : ((ObjectIdentifier) declaredField.get(body)).getValue();
                    break;
                }
            }
        }
        //add other header
        String requestAddr = new StringBuilder(integrationMsgHead.getSrvName()).append(".").append(equipmentID).toString();
        String hostName = InetAddress.getLocalHost().getHostName();
        String rqstID = new StringBuilder(requestAddr).append("@").append(hostName).toString();
        String traceID = new StringBuilder(rqstID).append("#").append(integrationMsgHead.getRqstTime()).append(".").append(integrationMsgHead.getTxId()).toString();

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
