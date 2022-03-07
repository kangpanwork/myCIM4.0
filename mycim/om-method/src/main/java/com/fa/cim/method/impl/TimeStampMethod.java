package com.fa.cim.method.impl;

import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.ITimeStampMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/24       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/24 9:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class TimeStampMethod implements ITimeStampMethod {

    @Override
    public String timeStampDoCalculation(Infos.ObjCommon objCommon, String currentTimeStamp, int days, int hours, int minutes, int seconds, int milliseconds) {
        log.info("【Method Entry】timeStampDoCalculationold()");
        Double aTempDuration = daysHoursMinutesSecondsMilliseconds(days,hours,minutes,seconds,milliseconds);
        log.info("aTempDuration = {}" ,aTempDuration );
        Timestamp anActualCompTimeStamp = Timestamp.valueOf(currentTimeStamp);
        log.info("anActualCompTimeStamp = {}" ,anActualCompTimeStamp );

        String targetTimeStamp = new Timestamp(Math.round(aTempDuration) + anActualCompTimeStamp.getTime()).toString() ;
        log.info("anActualCompTimeStamp + aDuration = {}",targetTimeStamp);
        log.info("【Method Exit】timeStampDoCalculationold()");
        return targetTimeStamp;
    }

    //region Private Methods
    @Override
    public String timeStampDoCalculationold(Infos.ObjCommon objCommon, String currentTimeStamp,
                                                     long days, long hours, long minutes, long seconds, long milliseconds) {
        log.info("【Method Entry】timeStampDoCalculationold()");

        Double aTempDuration = daysHoursMinutesSecondsMilliseconds(days,hours,minutes,seconds,milliseconds);

        log.info("aTempDuration = {}" ,aTempDuration );

        Timestamp anActualCompTimeStamp = Timestamp.valueOf(currentTimeStamp);
        //Timestamp anActualCompTimeStamp = BaseStaticMethod.getStringInTimeStamp(currentTimeStamp);

        LocalDateTime dateTime  = anActualCompTimeStamp.toLocalDateTime().plusSeconds(aTempDuration.longValue()/1000);
        String targetTimeStamp = String.format("%s", dateTime.toEpochSecond(ZoneOffset.of("+8")));

        log.info("anActualCompTimeStamp + aDuration = {}",targetTimeStamp);

        log.info("【Method Exit】timeStampDoCalculationold()");
        return targetTimeStamp;
    }

    private double daysHoursMinutesSecondsMilliseconds(long days, long hours, long minutes, long seconds, long milliseconds) {
        double theDuration = ((days)*86400000.00+(hours)*(3600000.00)+(minutes)*(60000.00)+(seconds)*(1000.00)+milliseconds);
        return theDuration ;
    }
}