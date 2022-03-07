package com.fa.cim.tms.status.recovery.utils;

/**
 * description:
 * This ThreadContextHolder used to put some variables based thread .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             miner               create file
 *
 * @author: miner
 * @date: 2018/6/28 11:55
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class ThreadContextHolder {

    private static final String UN_KNOWN = "UnKnown";

    private static final ThreadLocal<String> TRANSACTION_ID_HOLDER = new ThreadLocal<>();

    private ThreadContextHolder() {
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return string
     * @author miner
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
     * @author miner
     * @date 2018/6/29
     */
    public static void setTransactionId(String tId) {
        TRANSACTION_ID_HOLDER.set(tId);
    }

    /**
     * description:
     * clear current thread threadLocalMap element
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author miner
     * @date 2018/6/29
     */
    public static void clearHolder() {
        TRANSACTION_ID_HOLDER.remove();
    }

}
