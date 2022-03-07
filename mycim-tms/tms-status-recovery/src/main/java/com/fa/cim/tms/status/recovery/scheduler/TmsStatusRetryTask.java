package com.fa.cim.tms.status.recovery.scheduler;

import com.fa.cim.tms.status.recovery.service.ITmsStatusRecoveryService;
import com.fa.cim.tms.status.recovery.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 12:47
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class TmsStatusRetryTask {

    @Autowired
    private ITmsStatusRecoveryService tmsStatusRecoveryService;


    /**
     * description:rtms recovery is same as tms status recovery
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/3 10:32
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void tmsRecoverySchdule() {
        log.info("TMS/RTMS Status Recovery Schedule StartTime: {}", DateUtils.getCurrentDateTimeWithDefault());
        tmsStatusRecoveryService.tmsStatusRecoveryReq();
        log.info("TMS/RTMS Status Recovery Schedule EndTime: {}", DateUtils.getCurrentDateTimeWithDefault());
    }
}
