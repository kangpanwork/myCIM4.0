package com.fa.cim.tms.event.recovery.utils;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/13        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/9/13 11:13
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class BooleanUtils {

    public static final String BOOLEAN_TRUE = "1";

    public static final String BOOLEAN_FALSE = "0";


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param flag -
     * @return boolean
     * @author Bear
     * @date 2018/9/13 11:15
     */
    public static boolean isTrue(Boolean flag) {
        return (null != flag) && flag;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param flag -
     * @return boolean
     * @author Bear
     * @date 2018/9/13 11:15
     */
    public static boolean isFalse(Boolean flag) {
        return (null == flag) || !flag;
    }

    /**
     * description:
     * <p>get boolean by string value.
     * support "true","false","1","0"</p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param value value
     * @return boolean
     * @author PlayBoy
     * @date 2018/12/17 15:38:43
     */
    public static boolean getBoolean(String value) {
        boolean isBoolean = Boolean.getBoolean(value);
        if (!isBoolean) {
            isBoolean = BOOLEAN_TRUE.equals(value);
        }
        return isBoolean;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param str
     * @return java.lang.Boolean
     * @throws
     * @author Ho
     * @date 2019/6/19 16:38
     */
    public static Boolean convert(String str) {
        if (str == null) {
            return null;
        }
        if ("1".equals(str)) {
            return true;
        }
        return getBoolean(str);
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/21                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/21 15:40
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public static Boolean convert(Integer intValue) {
        if (null == intValue) {
            return null;
        }
        if (1 == intValue) {
            return true;
        }
        return getBoolean(intValue.toString());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param flag
     * @return java.lang.String
     * @throws
     * @author Neyo
     * @date 2020/4/27 16:38
     */
    public static Long convertBooleanToLong(Boolean flag) {
        if (null == flag) {
            return null;
        }
        if (isTrue(flag)) {
            return 1L;
        }
        return 0L;
    }

    public static boolean isTrue(Number num) {
        return num.intValue() != 0;
    }

    public static boolean isTrue(String str) {
        return null != str && !str.equals("0");
    }

}
