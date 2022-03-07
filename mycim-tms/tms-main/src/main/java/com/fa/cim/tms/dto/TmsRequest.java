package com.fa.cim.tms.dto;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class TmsRequest {

    private static final long serialVersionUID = -2220640590019721200L;
    private String functionId;
    private String sendTime;
    private String sendName;
    private String transactionRoute;
    /**
     * A Json String
     */
    private String messageBody;
    private String messageID;

    /**
     * description:
     * Create the CimRequest that can be sent with CimRequestReply
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param functionID  String
     * @param requestBody T
     * @return CimRequest
     * @author Yuri
     * @date 2019/3/25 19:38:09
     */
    public static <T> TmsRequest create(
            String functionID,
            T requestBody) {
        TmsRequest tmsRequest = new TmsRequest();
        tmsRequest.messageBody = JSONObject.toJSONString(requestBody,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse);
        tmsRequest.functionId = functionID;
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
        tmsRequest.sendTime = dateFormat.format(new Date());
        return tmsRequest;
    }

    public <T> T getRequestBody(Class<T> tClass) {
        return JSONObject.parseObject(this.messageBody, tClass);
    }


}
