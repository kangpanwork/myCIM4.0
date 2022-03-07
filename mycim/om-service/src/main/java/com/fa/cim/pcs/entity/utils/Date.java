package com.fa.cim.pcs.entity.utils;

import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * This class provides some methods of deal with time for Scripts.
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 13:37
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Component
public class Date {

    private static final DateTimeFormatter SPECIFIED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmss");

    /**
     * Calculate the duration between two specified Timestamps.
     * <p> The unit of return value is hour.</p>
     *
     * @param timeStr1 time1
     * @param timeStr2 time2
     * @return duration between two specified time
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 14:05
     */
    public double deltaTime(String timeStr1, String timeStr2) {
        Validations.check(CimStringUtils.isEmpty(timeStr1) || CimStringUtils.isEmpty(timeStr2),
                "The parameter of time is null. Please check and try again.");
        LocalDateTime dateTime1 = LocalDateTime.parse(timeStr1, SPECIFIED_FORMAT);
        LocalDateTime dateTime2 = LocalDateTime.parse(timeStr2, SPECIFIED_FORMAT);
        Duration duration = Duration.between(dateTime1, dateTime2);
        return duration.toMillis() / (double) (1000 * 60 * 60);
    }

    /**
     * Get current date like yyyyMMdd.
     *
     * @return current date
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 14:20
     */
    public String currentDate() {
        return LocalDateTime.now().format(DATE);
    }

    /**
     * Gets the day-of-week value.
     * 1: Monday
     * 2: Tuesday
     * 3: Wednesday
     * 4: Thursday
     * 5: Friday
     * 6: Saturday
     * 7: Sunday
     *
     * @return day-of-week value
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 14:27
     */
    public int currentDayOfWeek() {
        return LocalDateTime.now().getDayOfWeek().getValue();
    }

    /**
     * Get current time like HHmmss.
     *
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 14:28
     */
    public String currentTime() {
        return LocalDateTime.now().format(TIME);
    }

    /**
     * Get current time stamp of specified format. e.g. (yyyy-MM-dd-HH.mm.ss.SSS)
     *
     * @return current TimeStamp
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 14:16
     */
    public String currentTimeStamp() {
        return LocalDateTime.now().format(SPECIFIED_FORMAT);
    }
}
