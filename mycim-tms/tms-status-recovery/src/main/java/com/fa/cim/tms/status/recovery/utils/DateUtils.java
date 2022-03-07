package com.fa.cim.tms.status.recovery.utils;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimStringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * description:
 * DateUtils .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/5        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/9/5 17:09
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public final class DateUtils {

    public static final Timestamp zeroTimestamp = new Timestamp(0);
    private static final DateTimeFormatter DEFAULT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateUtils() {

    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return String
     * @author PlayBoy
     * @date 2018/9/5
     */
    public static String getCurrentDateTimeWithDefault() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.format(DEFAULT);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return String
     * @author PlayBoy
     * @date 2018/9/5
     */
    public static String getCurrentDateTimeWithDay() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.format(YYYY_MM_DD);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param dateStr
     * @return java.util.Date
     * @throws
     * @author Ho
     * @date 2019/5/15 17:51
     */
    public static Date convert(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").parse(dateStr);
            } catch (ParseException e1) {
                throw new RuntimeException(e1);
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
     * @param fmt
     * @param date
     * @return java.lang.String
     * @throws
     * @author Ho
     * @date 2019/4/3 17:13
     */
    public static String convert(String fmt, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(date);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param datePattern datePattern
     * @return String
     * @author PlayBoy
     * @date 2018/9/5
     */
    public static String getCurrentDateTimeByPattern(String datePattern) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * description:
     * <p>
     * any string format can parse to date.
     * like:
     * 2019-01-01 00-00-00
     * 2019-01-01-00-00-00
     * 2019-01-01-00.00.00
     * 2019-01-01-00.00-00
     * </p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @return Date
     * @author ZQI
     * @date 2019/2/19 15:57:39
     */
    private static Date parseStringToDate(String date) {
        if (CimStringUtils.isEmpty(date)) return null;
        try {
            String parse = date.replaceFirst("[0-9]{4}([^0-9]?)", "yyyy$1");
            parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}([^0-9]?)", "$1MM$2");
            parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}( ?)", "$1dd$2");
            parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}([^0-9]?)", "$1HH$2");
            parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}([^0-9]?)", "$1mm$2");
            parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}([^0-9]?)", "$1ss$2");
            return new SimpleDateFormat(parse).parse(date);
        } catch (ParseException e) {
            log.error("Parse time error.", e);
            throw new ServiceException("Parse time error.");
        }
    }

    public static Timestamp convertTo(String str) {
        if (StringUtils.isEmpty(str)) return null;
        return Timestamp.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parseStringToDate(str)));
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param timestamp -
     * @return String
     * @author ZQI
     * @date 2019/2/23 14:27:43
     */
    public static String convertToSpecString(Timestamp timestamp) {
        if (null == timestamp) return null;
        return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS").format(timestamp);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @return Double
     * @author ZQI
     * @date 2019/4/10 10:18:53
     */
    public static Double daysHoursMinutesSecondsMilliseconds(Long days, Long hours, Long minutes, Long seconds, Long milliseconds) {
        return ((days) * 86400000.00 + (hours) * (3600000.00) + (minutes) * (60000.00) + (seconds) * (1000.00) + milliseconds);
    }

    public static String getTimestampAsString(Timestamp timestamp) {
        if (null == timestamp)
            timestamp = zeroTimestamp;
        return timestamp.toString();
    }

    public static Timestamp getNonNullTimestamp(Timestamp timestamp) {
        return null == timestamp ? zeroTimestamp : timestamp;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @return Timestamp
     * @author ZQI
     * @date 2019/10/25 14:01:45
     */
    public static Timestamp makeNull() {
        return new Timestamp(0L);
    }

    /**
     * getCurrentTimeStamp
     *
     * @return Timestamp
     */
    public static Timestamp getCurrentTimeStamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Long substractTimeStamp(long lo1, long lo2) {
        return Math.abs(lo1 - lo2);
    }


    public static XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        XMLGregorianCalendar gc = null;
        try {
            gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gc;
    }


}
