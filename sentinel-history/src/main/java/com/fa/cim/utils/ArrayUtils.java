package com.fa.cim.utils;

import com.fa.cim.Custom.List;

import java.util.Arrays;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 17:40:55
 */
public class ArrayUtils {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param arr
     * @param index
     * @return T
     * @author Ho
     * @date 2019/2/26 17:50:41
     */
    public static <T> T get(Object[] arr,int index) {
        if (length(arr)<=index||arr==null){
            return null;
        }
        return (T) arr[index];
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param list
     * @param index
     * @return T
     * @author Ho
     * @date 2019/2/27 10:39:44
     */
    public static <T> T get(List<T> list,int index) {
        if (list==null) {
            return null;
        }
        return get(list.toArray(),index);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param arr
     * @return int
     * @author Ho
     * @date 2019/2/26 17:47:08
     */
    public static <T> int length(T[] arr) {
        if (arr==null) {
            return 0;
        }
        return arr.length;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param arr
     * @return int
     * @author Ho
     * @date 2019/2/26 17:48:58
     */
    public static int length(List arr) {
        return BaseUtils.length(arr);
    }

}
