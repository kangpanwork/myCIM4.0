package com.fa.cim.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * description:
 * RequestJsonUtils .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/1        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/8/1 18:03
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class RequestJsonUtils {

    public static final String POST = "POST";
    public static final String GET = "GET";

    private RequestJsonUtils() {

    }

    public static <T> T toJavaObject (byte[] jsonBytes, Class<T> type) {
        return JSONObject.parseObject(jsonBytes, type);
    }

    public static byte[] toJsonBytes (Object javaObject) {
        return JSONObject.toJSONBytes(javaObject,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse);
    }

    /**
     * description:
     * 获取 request 中 json 字符串的内容
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param request
     * @return String
     * @throws IOException
     * @author PlayBoy
     * @date 2018/8/1
     */
    public static String getRequestJsonString(HttpServletRequest request)
            throws IOException {
        String submitMethod = request.getMethod();
        // GET
        if (submitMethod.equals(GET)) {
            return new String(request.getQueryString().getBytes("iso-8859-1"), "utf-8").replaceAll("%22", "\"");
            // POST
        } else {
            return getRequestPostStr(request);
        }
    }

    /**
     * description:
     * 描述:获取 post 请求的 byte[] 数组
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param request
     * @return byte[]
     * @throws IOException
     * @author PlayBoy
     * @date 2018/8/1
     */
    public static byte[] getRequestPostBytes(HttpServletRequest request)
            throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return null;
        }
        byte[] buffer = new byte[contentLength];
        for (int i = 0; i < contentLength; ) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param request
     * @return String
     * @throws IOException
     * @author PlayBoy
     * @date 2018/8/1
     */
    public static String getRequestPostStr(HttpServletRequest request)
            throws IOException {
        byte[] buffer = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        return new String(buffer, charEncoding);
    }

}
