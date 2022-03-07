package com.fa.cim.tms.event.recovery.utils;

import java.util.Collection;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/4/19        ********             miner               create file
 *
 * @author: Miner
 * @date: 2020/4/19 10:21
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class ArrayUtils {

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/15 16:57
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public static <T> T get(Object[] arr, int index) {
        if (length(arr) <= index || arr == null) {
            return null;
        }
        return (T) arr[index];
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/15 16:57
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public static <T> int length(T[] arr) {
        if (arr == null) {
            return 0;
        }
        return arr.length;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/15 16:55
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public static boolean isNotEmpty(Collection<?> list) {
        return !isEmpty(list);
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/15                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/15 16:57
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public static boolean isEmpty(Collection<?> list) {
        return null == list || list.isEmpty();
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/16                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/16 15:45
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public static int getSize(List list) {
        if (isEmpty(list)) {
            return 0;
        }
        return list.size();
    }


}

