package com.fa.cim.tms.status.recovery.utils;


import com.fa.cim.tms.status.recovery.pojo.ObjectIdentifier;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/29        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/9/29 13:11
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class ObjectUtils {
    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param identifier -
     * @return boolean
     * @author Bear
     * @date 2018/9/29 13:13
     */
    public static boolean isEmpty(ObjectIdentifier identifier) {
        return null == identifier || (StringUtils.isEmpty(identifier.getValue()) && StringUtils.isEmpty(identifier.getReferenceKey()));
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param identifier -
     * @return boolean
     * @author Bear
     * @date 2018/9/29 13:13
     */
    public static boolean isNotEmpty(ObjectIdentifier identifier) {
        return !isEmpty(identifier);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param identifier -
     * @return boolean
     * @author Bear
     * @date 2018/11/21 14:33
     */
    public static boolean isNotEmptyWithValue(ObjectIdentifier identifier) {
        return (null != identifier) && !StringUtils.isEmpty(identifier.getValue());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param src
     * @param desc
     * @return boolean
     * @throws
     * @author Ho
     * @date 2019/3/22 15:45
     */
    public static boolean equals(Object src, Object desc) {
        if (src == null) {
            return desc == null;
        }
        return src.equals(desc);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param identifier -
     * @return boolean
     * @author Bear
     * @date 2018/11/21 14:33
     */
    public static boolean isEmptyWithValue(ObjectIdentifier identifier) {
        return !isNotEmptyWithValue(identifier);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param obj -
     * @return boolean
     * @author Bear
     * @date 2018/11/4 11:54
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        } else {
            return obj instanceof Map && ((Map) obj).isEmpty();
        }
    }

    /**
     * description:
     * just compare the objectIdentifier.value.
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param source -
     * @param target -
     * @return boolean
     * @author Bear
     * @date 2018/11/5 16:59
     */
    public static boolean equalsWithValue(ObjectIdentifier source, ObjectIdentifier target) {
        if (null == source) {
            return ObjectUtils.isEmptyWithValue(target);
        }

        if (null == target) {
            return ObjectUtils.isEmptyWithValue(source);
        }
        return equalsWithValue(source, target.getValue());
    }


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param source
     * @param target -
     * @return boolean
     * @author Nyx
     * @date 2019/3/28 15:15
     */
    public static boolean equalsWithValue(String source, String target) {
        return StringUtils.equals(source, target);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return java.lang.String
     * @author Bear
     * @date 2018/12/20 16:45
     */
    public static String getObjectValue(ObjectIdentifier objectIdentifier) {
        return null == objectIdentifier || isEmpty(objectIdentifier.getValue()) ? "" : objectIdentifier.getValue();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objectIdentifier -
     * @return java.lang.String
     * @author Nyx
     * @date 2019/6/13 19:07
     */
    public static String getObjectReferenceKey(ObjectIdentifier objectIdentifier) {
        return null == objectIdentifier ? "" : objectIdentifier.getReferenceKey();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param source -
     * @param target -
     * @return boolean
     * @author Bear
     * @date 2018/11/5 17:07
     */
    public static boolean equalsWithValue(ObjectIdentifier source, String target) {
        if (null == source) {
            return (null == target);
        }
        return StringUtils.equals(source.getValue(), target);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param source -
     * @param target -
     * @return boolean
     * @author Bear
     * @date 2018/11/5 17:07
     */
    public static boolean equalsWithValue(String source, ObjectIdentifier target) {
        return equalsWithValue(target, source);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param targetList -
     * @param source     -
     * @return boolean
     * @author Bear
     * @date 2018/11/30 13:02
     */
    public static boolean containsWithValue(List<ObjectIdentifier> targetList, ObjectIdentifier source) {
        if (ObjectUtils.isEmpty(source)) {
            return false;
        }
        return containsWithValue(targetList, source.getValue());
    }

    /**
     * description:
     * judge whether the source.value exist in list object
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param targetList -
     * @param source     -
     * @return boolean
     * @author Bear
     * @date 2018/11/30 13:06
     */
    public static boolean containsWithValue(List<ObjectIdentifier> targetList, String source) {
        if (ArrayUtils.isEmpty(targetList) || StringUtils.isEmpty(source)) {
            return false;
        }
        Predicate<ObjectIdentifier> matchWithValue = p -> ObjectUtils.equalsWithValue(p, source);
        return targetList.stream().anyMatch(matchWithValue);
    }


    public static Boolean conversionIntoBoolaen(int source) {
        if (0 == source || 1 == source) {
            return 0 != source;
        }
        return null;
    }

    public static String toString(Object obj) {
        return isEmpty(obj) ? "" : " " + obj.toString();
    }
}
