package com.fa.cim.utils;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/25 15:40:27
 */
public class BaseUtils {

//    private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param response
     * @return boolean
     * @author Ho
     * @date 2019/2/25 15:42:15
     */
    public static boolean isOk(Response response) {
        if (response.getCode()==null){
            return false;
        }
        return response.getCode()==0;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param bool
     * @return boolean
     * @author Ho
     * @date 2019/2/25 15:42:15
     */
    public static Boolean convert(Integer bool) {
        if (bool==null) {
            return false;
        }
        return bool!=0;
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
     * @param timestamp
     * @param fmt
     * @return java.sql.Timestamp
     * @exception
     * @author Ho
     * @date 2019/4/28 14:21
     */
    public static Timestamp convert(String timestamp,String fmt) {
        if (isNull(timestamp)) {
            return null;
        }
        SimpleDateFormat sdf=new SimpleDateFormat(fmt);
        try {
            return new Timestamp(sdf.parse(timestamp).getTime());
        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param obj
     * @return boolean
     * @exception
     * @author Ho
     * @date 2019/5/10 12:14
     */
    public static boolean isNull(Object obj){
        return obj==null||"".equals(obj)||"null".equals(obj);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param cls
     * @return java.lang.String
     * @exception
     * @author Ho
     * @date 2019/4/28 14:01
     */
    public static /*synchronized*/ String generateID(Class<?> cls) {
        String className=cls.getSimpleName();
        return String.format("%s.%s",className,System.currentTimeMillis());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param className
     * @return java.lang.String
     * @exception
     * @author Ho
     * @date 2019/4/28 14:01
     */
    public static  String generateID(String className) {
        return String.format("%s.%s",className,System.currentTimeMillis());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param bool
     * @return boolean
     * @author Ho
     * @date 2019/2/25 15:42:15
     */
    public static Integer convert(Boolean bool) {
        if (bool==null) {
            return 0;
        }
        return bool?1:0;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param list
     * @return java.lang.Integer
     * @author Ho
     * @date 2019/2/26 14:09:33
     */
    public static Integer length(List list) {
        if (list==null) {
            return 0;
        }
        return list.size();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param obj
     * @return T
     * @author Ho
     * @date 2019/2/27 14:33:48
     */
    public static <T> T convert(Object obj) {
        return (T) obj;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param identifier
     * @param stringifiedObjectReference
     * @return com.fa.cim.dto.Infos.ObjectIdentifier
     * @exception
     * @author Ho
     * @date 2019/6/4 14:03
     */
    public static Infos.ObjectIdentifier objectIdentifier(String identifier,
            String stringifiedObjectReference) {
        Infos.ObjectIdentifier objectIdentifier = new Infos.ObjectIdentifier();
        objectIdentifier.setIdentifier(identifier);
        objectIdentifier.setStringifiedObjectReference(stringifiedObjectReference);
        return objectIdentifier;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param timeStr
     * @return java.sql.Timestamp
     * @exception
     * @author Ho
     * @date 2019/6/6 14:15
     */
    public static Timestamp convert(String timeStr) {
        try{
            return convert(timeStr,"yyyy-MM-dd-HH.mm.ss.SSS");
        } catch (Exception e1){
            try{
                return convert(timeStr,"yyyy-MM-dd HH:mm:ss.SSS");
            } catch (Exception e2){
                try{
                    return convert(timeStr,"yyyy-MM-dd-HH.mm.ss");
                } catch (Exception exception) {
                    try {
                        return convert(timeStr,"yyyy-MM-dd HH:mm:ss");
                    } catch (Exception ex) {
                        try {
                            return convert(timeStr,"yyyy-MM-ddHH:mm:ss");
                        }catch (Exception ex3){
                            return convert(timeStr,"yyyyMMdd");
                        }
                    }
                }
            }
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param obj
     * @return java.lang.Boolean
     * @exception
     * @author Ho
     * @date 2019/4/22 13:22
     */
    public static Boolean convertB(Object obj) {
        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).intValue()==1;
        }
        return null;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param obj
     * @return java.lang.Boolean
     * @exception
     * @author Ho
     * @date 2019/4/22 14:51
     */
    public static Double convertD(Object obj) {
        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).doubleValue();
        }
        if (obj==null){
            return null;
        }
        return Double.valueOf(obj.toString());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param obj
     * @return java.lang.Long
     * @exception
     * @author Ho
     * @date 2019/4/22 11:27
     */
    public static Long convertL(Object obj) {
        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).longValue();
        }
        if (obj==null) {
            return null;
        }
        return Double.valueOf(obj.toString()).longValue();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param obj
     * @return java.lang.Integer
     * @exception
     * @author Ho
     * @date 2019/4/22 11:45
     */
    public static Integer convertI(Object obj) {
        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).intValue();
        }
        if (obj==null) {
            return null;
        }
        if (obj instanceof Boolean){
            return ((Boolean) obj).booleanValue()?1:0;
        }
        return Double.valueOf(obj.toString()).intValue();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param response
     * @return T
     * @author Ho
     * @date 2019/2/27 15:04:21
     */
    public static <T> T get(Response response) {
        return (T) response.getBody();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param param
     * @return T
     * @exception
     * @author Ho
     * @date 2019/5/31 18:19
     */
    public static <T> T get(Params.Param param) {
        return convert(param.getT());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param response
     * @param index
     * @return T
     * @author Ho
     * @date 2019/2/27 15:06:05
     */
    public static <T> T get(Response response,int index) {
        Object[] object=convert(response.getBody());
        return ArrayUtils.get(object,index);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/3/1 14:49
     */
    public static Response returnOK() {
        Response response=new Response();
        response.setCode(0);
        return response;
    }

    public static boolean isTrue(Boolean bool) {
        if (bool==null) {
            return false;
        }
        return bool;
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
     * @date 2019/5/11 17:54
     */
    public static int strlen(String str) {
        if (str ==null) {
            return 0;
        }
        return str.length();
    }

}
