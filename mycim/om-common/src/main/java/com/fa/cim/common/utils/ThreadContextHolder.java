package com.fa.cim.common.utils;

import com.fa.cim.common.constant.TransactionIDEnum;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * description:
 * This ThreadContextHolder used to put some variables based thread .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/6/28 11:55
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class ThreadContextHolder {

    private static final String UN_KNOWN = "UnKnown";

    private static final ThreadLocal<String> TRANSACTION_ID_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<Timestamp> REQUEST_TIME = new ThreadLocal<>();

    private static final ThreadLocal<Map<String, String>> THREADSPECIFIC_DATA_STRING = new ThreadLocal<>();

    private ThreadContextHolder() {
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return string
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static String getTransactionId() {
        String transactionId = TRANSACTION_ID_HOLDER.get();
        if (transactionId == null || "".equals(transactionId)) {
            return UN_KNOWN;
        }
        return transactionId;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param tId
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static void setTransactionId(String tId) {
        TRANSACTION_ID_HOLDER.set(tId);
        REQUEST_TIME.set(new Timestamp(System.currentTimeMillis()));
    }

    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/11/5 11:24
     * @param tId -  
     * @return void
     */
    public static void setTransactionId(TransactionIDEnum tId) {
        if (null == tId) {
            return;
        }
        TRANSACTION_ID_HOLDER.set(tId.getValue());
        REQUEST_TIME.set(new Timestamp(System.currentTimeMillis()));
    }

    public static void setThreadSpecificDataString(String dataString, String value) {
        Map<String, String> map = THREADSPECIFIC_DATA_STRING.get();
        if (map == null){
            map = new HashMap<>();
            THREADSPECIFIC_DATA_STRING.set(map);
        }
        map.put(dataString, value);
    }

    public static String getThreadSpecificDataString(String dataString){
        Map<String, String> map = THREADSPECIFIC_DATA_STRING.get();
        if (map != null && !map.isEmpty()){
            return map.get(dataString);
        }
        return null;
    }
    public static void removeDataString(String dataString) {
        Map<String, String> map = THREADSPECIFIC_DATA_STRING.get();
        if (map != null && !map.isEmpty()){
            map.remove(dataString);
        }
    }
    public static void clearThreadSpecificDataString(){
        THREADSPECIFIC_DATA_STRING.remove();
    }

    /**
     * description:
     * clear current thread threadLocalMap element
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static void clearHolder() {
        TRANSACTION_ID_HOLDER.remove();
        THREADSPECIFIC_DATA_STRING.remove();
        REQUEST_TIME.remove();
    }

    public static Timestamp getRequestTime(){
        return REQUEST_TIME.get();
    }
}
