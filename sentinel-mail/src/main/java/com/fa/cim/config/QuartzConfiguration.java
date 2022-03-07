package com.fa.cim.config;

import com.fa.cim.job.EmailConfigSyncJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/17          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/2/17 11:24
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Configuration
public class QuartzConfiguration {
    private static final int TIME = 5;
    @Bean
    public JobDetail emailConfigSyncJobDetail(){
        return JobBuilder.newJob(EmailConfigSyncJob.class).withIdentity("EmailConfigSyncJob")
                .storeDurably().build();
    }
    @Bean
    public Trigger emailConfigSyncTrigger(){
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(TIME).repeatForever();
        return TriggerBuilder.newTrigger().forJob(emailConfigSyncJobDetail())
                .withIdentity("emailConfigSyncTrigger").withSchedule(scheduleBuilder).startNow().build();
    }
}