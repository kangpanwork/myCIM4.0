package com.fa.cim.tms.event.recovery.dto;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/20        ********             miner               create file
 *
 * @author: Miner
 * @date: 2020/2/20 12:45
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class CimRequest {
    private static final long serialVersionUID = -2220640590019721200L;
    private User user;
    private String functionId;
    private String sendTime;
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
     * @param user        User
     * @param functionID  String
     * @param requestBody T
     * @return CimRequest
     * @author Yuri
     * @date 2019/3/25 19:38:09
     */
    public static <T> CimRequest create(User user,
                                        String functionID,
                                        T requestBody) {
        CimRequest cimRequest = new CimRequest();
        cimRequest.messageBody = JSONObject.toJSONString(requestBody,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse);
        cimRequest.functionId = functionID;
        cimRequest.user = user;
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
        cimRequest.sendTime = dateFormat.format(new Date());
        return cimRequest;
    }

    public <T> T getRequestBody(Class<T> tClass) {
        return JSONObject.parseObject(this.messageBody, tClass);
    }
}