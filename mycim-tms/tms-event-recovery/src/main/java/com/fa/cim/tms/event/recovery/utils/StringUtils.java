package com.fa.cim.tms.event.recovery.utils;

import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/4/16        ********              Nyx             create file
 *
 * @author: Bear
 * @date: 2018/4/16 16:00
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public class StringUtils extends org.springframework.util.StringUtils {

    public static final String EMPTY = "";
    public static final String DEFAULD_WILD_CARD_SIMBOL = "%";

    private StringUtils() {
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return
     * @author Bear
     * @date 2018/4/16
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim()) || "null".equals(str.trim());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param str
     * @return boolean
     * @author Ho
     * @date 2019/2/15 10:17:44
     */
    public static boolean isEmpty(Object str) {
        if (str instanceof String) {
            return isEmpty((String) str);
        }
        return org.springframework.util.StringUtils.isEmpty(str);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param object
     * @return java.lang.String
     * @author Ho
     * @date 2018/11/21 15:06:07
     */
    public static String toString(Object object) {
        return object == null ? EMPTY : object.toString();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return java.lang.String
     * @author Ho
     * @date 2018/11/21 15:06:07
     */
    public static String firstToString(Object[] arg) {
        if (arg == null || arg.length == 0) {
            return null;
        }
        return toString(arg[0]);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param str
     * @return int
     * @author Ho
     * @date 2018/11/23 10:34:37
     */
    public static int length(String str) {
        return isEmpty(str) ? 0 : str.length();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param identifier identifier
     * @return isEmpty
     * @author PlayBoy
     * @date 2018/11/15 10:23:36
     */
    public static boolean isEmpty(ObjectIdentifier identifier) {
        if (identifier == null) {
            return true;
        }
        return isEmpty(identifier.getValue()) && isEmpty(identifier.getReferenceKey());
    }

    public static boolean isValueEmpty(ObjectIdentifier identifier) {
        if (identifier == null) {
            return true;
        }
        return isEmpty(identifier.getValue());
    }

    public static boolean isRefKeyEmpty(ObjectIdentifier identifier) {
        if (identifier == null) {
            return true;
        }
        return isEmpty(identifier.getReferenceKey());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param identifier
     * @return boolean
     * @author Ho
     * @date 2018/12/11 09:52:55
     */
    public static boolean isEmptyID(ObjectIdentifier identifier) {
        if (identifier == null) {
            return true;
        }
        return isEmpty(identifier.getValue());
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
     * @author Bear
     * @date 2018/5/8
     */
    public static boolean equals(String source, String target) {
        if (source == null && target == null) {
            return true;
        }
        if (!isEmpty(source)) {
            return source.equals(target);
        }
        return isEmpty(target);
    }

    public static boolean equalsIgnoreCase(String source, String target) {
        if (source == null && target == null) return true;
        if (!isEmpty(source)) return source.equalsIgnoreCase(target);
        return isEmpty(target);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objectIdentifier
     * @return java.lang.String
     * @throws
     * @author Ho
     * @date 2019/3/22 17:19
     */
    public static String getValue(ObjectIdentifier objectIdentifier) {
        if (objectIdentifier == null) {
            return null;
        }
        return objectIdentifier.getValue();
    }

    /**
     * description:
     * <p>Find the position where the string appears in the last the nth time</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param source
     * @param str
     * @param nth    -
     * @return int
     * @author Bear
     * @date 2018/7/20 15:25
     */
    public static int lastNthIndexOf(String source, String str, int nth) {
        String temp = source;
        int index = -1;
        for (int i = 0; i < nth; i++) {
            index = temp.lastIndexOf(str);
            if (-1 == index) {
                break;
            }
            temp = temp.substring(0, index);
        }
        return index;
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
     * @date 2019/3/22 14:30
     */
    public static boolean equals(ObjectIdentifier src, ObjectIdentifier desc) {
        if (isEmpty(src)) {
            return isEmpty(desc);
        }
        return equals(src.getValue(), desc.getValue()) && equals(desc.getReferenceKey(), src.getReferenceKey());
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
     * @date 2019/5/29 17:16
     */
    public static boolean valueEquals(ObjectIdentifier src, ObjectIdentifier desc) {
        if (src == null) {
            return desc == null;
        }
        if (desc == null) {
            return src == null;
        }
        return equals(src.getValue(), desc.getValue());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return compare between char and int
     * @author Decade
     * @date 2020/6/30/030 17:37
     */
    public static boolean equals(char src, String desc) {
        String source = new String(new char[]{src});
        if (source == null && desc == null) {
            return true;
        }
        if (!isEmpty(source)) {
            return source.equalsIgnoreCase(desc);
        }
        return isEmpty(desc);
    }

    /**
     * description:
     * <p>same with atoi method<br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param numberStr numberStr
     * @return Long
     * @author PlayBoy
     * @date 2018/10/17 13:54:30
     */
    public static long toLongValue(String numberStr) {
        if (isEmpty(numberStr)) {
            return 0L;
        }
        long value = 0L;
        try {
            value = Float.valueOf(numberStr).longValue();
        } catch (Exception e) {
            log.error("toLongValue(): numberStr is not a number ", e);
        }
        return value;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * description:
     * <p>this function is C++ SP_SET_HOSTVARIABLE(s1,s2)</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/31                              Wind
     *
     * @param target
     * @param source
     * @author Wind
     * @date 2018/10/31 11:31
     */
    public static void setHostVariable(String target, String source) {
        if (target != null && source != null && !StringUtils.equals(target, source)) {
            if (target.length() <= source.length()) {
                target = source.substring(source.indexOf(target.length() - 1));
            } else {
                target = source;
            }
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/8                            Wind
     *
     * @param target
     * @param source
     * @return String
     * @author Wind
     * @date 2018/11/8 10:14
     */
    public static String strRrChr(String target, String source) {
        int i = target.indexOf(source);
        if (i == -1) {
            return null;
        }
        return target.substring(i);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param str -
     * @return boolean
     * @author Nyx
     * @date 2018/12/19 15:03
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strs
     * @param separator
     * @return java.lang.String
     * @throws
     * @author ho
     * @date 2020/7/28 13:41
     */
    public static String join(Set<String> strs, String separator) {
        if (strs == null) {
            return null;
        }
        Iterator<String> iterator = strs.iterator();
        if (strs.size() == 1) {
            return iterator.next();
        }
        StringBuilder str = new StringBuilder(iterator.next());
        while (iterator.hasNext()) {
            str.append(",").append(iterator.next());
        }
        return str.toString();
    }

    public static <T> String join(final T... elements) {
        return join(elements, null);
    }

    public static String join(final Object[] array, final String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }

        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
        final int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }

        final StringBuilder buf = new StringBuilder(noOfItems * 16);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * description:
     * get the count of the subString in source.
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param source    -
     * @param subString -
     * @return int
     * @author Bear
     * @date 2019/2/1 10:06
     */
    public static int count(String source, String subString) {
        int count = 0;
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(subString)) {
            return count;
        }
        int index = source.indexOf(subString);
        while (-1 != index) {
            count = count + 1;
            index = source.indexOf(subString, index + 1);
        }
        return count;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param timestamp -
     * @return java.lang.String
     * @author Bear
     * @date 2019/3/26 16:30
     */
    public static String getTimeStamp(Timestamp timestamp) {
        return null == timestamp ? null : timestamp.toString();
    }

    /**
     * description:
     * <p>
     * Counts the number of specified character in a string
     * </p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author ZQI
     * @date 2019/5/14 17:02:43
     */
    public static int countTheNumberOfSpecCharacter(String sourceStr, String character) {
        if (isEmpty(sourceStr)) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = sourceStr.indexOf(character, index)) != -1) {
            index += character.length();
            count++;
        }
        return count;
    }

    /**
     * description:
     * <p>toLowerCaseFirstOne</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param value value
     * @return value
     * @author PlayBoy
     * @date 2019-06-21 15:29:15
     */
    public static String toLowerCaseFirstOne(String value) {
        if (Character.isLowerCase(value.charAt(0))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * description:
     * <p>toUpperCaseFirstOne</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param value value
     * @return value
     * @author PlayBoy
     * @date 2019-06-21 15:29:20
     */
    public static String toUpperCaseFirstOne(String value) {
        if (Character.isUpperCase(value.charAt(0))) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * find the target value is amongst a group of strings
     *
     * @param target   target value to find
     * @param strGroup group of value to find the value within
     * @return true if the equals is found, false if otherwise
     * @author Yuri
     */
    public static boolean equalsIn(String target, String... strGroup) {
        return null != strGroup && strGroup.length > 0 &&
                Arrays.stream(strGroup).parallel().anyMatch(str -> equals(str, target));
    }

    /**
     * check if the value of the {@link ObjectIdentifier} is matching to the target value;
     * if both value is deemed as empty return true, or only either is found empty return false.
     *
     * @param id          {@link ObjectIdentifier}
     * @param targetValue {@link String}
     * @return true if it is matched
     * @author Yuri
     */
    public static boolean equals(ObjectIdentifier id, String targetValue) {
        if (ObjectIdentifier.isEmpty(id) && isEmpty(targetValue)) {
            return true;
        }
        if (ObjectIdentifier.isEmpty(id) || isEmpty(targetValue)) {
            return false;
        }
        return id.equals(targetValue);
    }

    public static String join(String seqarator, String... str) {
        if (str == null || str.length == 0) {
            return EMPTY;
        }
        int loop = str.length - 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < loop; i++) {
            if (isNotEmpty(str[i])) {
                sb.append(str[i]).append(seqarator);
            }
        }
        sb.append(str[loop]);
        return sb.toString();
    }

    public static boolean wildCardCompare(String source, String target, String wildCardSimbol) {
        int i = source.indexOf(wildCardSimbol);
        String _source = source.substring(0, i);
        String _target = target.substring(0, i);
        return equals(_source, _target);
    }

    public static boolean wildCardCompare(String source, String target) {
        return wildCardCompare(source, target, DEFAULD_WILD_CARD_SIMBOL);
    }

    public static String getFirstNotEmpty(String... str) {
        if (str == null || str.length == 0) {
            return EMPTY;
        }
        for (String s : str) {
            if (StringUtils.isNotEmpty(s)) {
                return s;
            }
        }
        return EMPTY;
    }

    public static String nonNullValue(String value) {
        return isEmpty(value) ? EMPTY : value;
    }

    /**
     * Compare str1 and str2, at most the first n bytes.
     *
     * @return Return true if equals.
     * @version 1.0
     * @author ZQI
     * @date 2020/5/20 18:09
     */
    public static boolean equals(String str1, String str2, int n) {
        if ((isEmpty(str1) || isEmpty(str2)) && n > 0) return false;
        if (length(str1) < n || length(str2) < n || n < 0) return false;
        return equals(str1.substring(0, n), str2.substring(0, n));
    }

    /**
     * Use the specified characters to cut data in the string from start position to end position.
     * <p> Note: if with out a specify end position, please set endPosition == -1
     *
     * <p> ex...
     * <p> cut("AAA.01.BBB.02",".".0,3)       result: AAA.01.BBB
     * <p> cut("AAA.01.BBB.02",".".1,3)       result: 01.BBB
     * <p> cut("AAA.01.BBB.02",".".1,-1)      result: 01.BBB.02
     * <p> cut("AAA#01#BBB#02","#".1,-1)      result: 01#BBB#02
     *
     * @param str           original string
     * @param character     specified characters
     * @param startPosition cut start position
     * @param endPosition   cut end position
     * @version 1.0
     * @author ZQI
     * @date 2020/7/22 15:58
     */
    public static String cut(String str, String character, int startPosition, int endPosition) {
        if (isEmpty(str)) return null;
        if (!str.contains(character)) return str;
        String[] split = str.split(String.format("\\%s", character));
        StringBuilder stringBuilder = new StringBuilder();
        int length = split.length;
        if (endPosition <= 0) endPosition = length;
        for (int i = 0; i < length; i++) {
            if (i >= startPosition && i < endPosition) {
                stringBuilder.append(split[i]);
                if (i < endPosition - 1) {
                    stringBuilder.append(character);
                }
            }
        }
        return stringBuilder.toString();
    }
}
