package com.fa.cim.utils;

import com.fa.cim.dto.Params;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/25 15:26:40
 */
public class StringUtils {

//    private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param src
     * @param target
     * @return boolean
     * @author Ho
     * @date 2019/2/25 15:22:07
     */
    public static boolean equals(String src,String target,Boolean...ignoreCase){
        if (src==null){
            return src==target;
        }
        if (ArrayUtils.length(ignoreCase)==0||!ignoreCase[0]){
            return src.equals(target);
        }
        return src.equalsIgnoreCase(target);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param str
     * @param regex
     * @return java.lang.String
     * @author Ho
     * @date 2019/2/27 14:03:45
     */
    public static String strtok(String str,String regex) {
        if (str==null){
            return str;
        }
        return str.split(regex)[0];
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param formatStr
     * @param args
     * @return java.lang.String
     * @exception
     * @author Ho
     * @date 2019/7/24 14:39
     */
    public static String sprintf(String formatStr,Object...args){
        return String.format(formatStr,args);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param str
     * @param c
     * @return java.lang.String
     * @exception
     * @author Ho
     * @date 2019/7/2 15:41
     */
    public static String strchr(String str,char c){
        if (str==null){
            return null;
        }
        int i=str.indexOf(c);
        if (i==-1) {
            return null;
        }
        return str.substring(0,i);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param str
     * @param c
     * @return java.lang.String
     * @exception
     * @author Ho
     * @date 2019/7/2 15:41
     */
    public static void strchr(Params.String str, char c){
        String strchr = strchr(str.getValue(), c);
        if (strchr!=null){
            str.setValue(strchr);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param src
     * @param target
     * @return int
     * @author Ho
     * @date 2019/2/25 15:22:17
     */
    public static int variableStrCmp(String src,String target,Boolean...igonreCase){
        if (length(src)==0) {
            return length(target)==0?0:-1;
        }
        return equals(src,target,igonreCase)?0:-1;
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
     * @exception
     * @author Ho
     * @date 2019/6/5 15:59
     */
    public  static int length(String str) {
        if (str==null) {
            return 0;
        }
        return str.length();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param src
     * @param target
     * @return int
     * @exception
     * @author Ho
     * @date 2019/6/3 17:32
     */
    public static int strcmp(String src,String target){
        if (src==null) {
            return target==null?0:-1;
        }
        return src.equals(target)?0:-1;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param obj
     * @return java.lang.String
     * @exception
     * @author Ho
     * @date 2019/4/18 17:38
     */
    public static String convert(Object obj) {
        if (obj==null) {
            return null;
        }
        if (obj instanceof Date) {
            convert((Date) obj);
        }
        return obj.toString();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param date
     * @return java.lang.String
     * @exception
     * @author Ho
     * @date 2019/4/22 16:11
     */
    public static String convert(Date date) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (date==null){
            return null;
        }
        return sdf.format(date);
    }

}
