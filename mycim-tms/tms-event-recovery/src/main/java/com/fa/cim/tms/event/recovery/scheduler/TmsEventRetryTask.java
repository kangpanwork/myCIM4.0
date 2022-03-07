package com.fa.cim.tms.event.recovery.scheduler;

import com.fa.cim.tms.event.recovery.service.ITmsEventRecoveryService;
import com.fa.cim.tms.event.recovery.utils.DateUtils;
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
public class TmsEventRetryTask {

    @Autowired
    private ITmsEventRecoveryService tmsEventRecoveryService;

    /**
     * description: Reticel/Reticle is same Table with Carrier. In futrue we can customesize it by carrier/reticlePod name? add some isolation logical
     * If we can not find out the difference between Carrier/ReticlPod/Reticle, we maybe add Reticle/ReticlePod table to distinguish it In futrue
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/3 12:00
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */

    @Scheduled(cron = "*/5 * * * * ?")
    public void tmsEventRecoverySchdule() {
        log.info("TMS Event Recovery Schedule StartTime: {}", DateUtils.getCurrentDateTimeWithDefault());
        tmsEventRecoveryService.tmsEventRecoveryReq();
        log.info("TMS Event Recovery Schedule EndTime: {}", DateUtils.getCurrentDateTimeWithDefault());
    }


    @Scheduled(cron = "*/5 * * * * ?")
    public void rtmsEventRecoverySchdule() {
        log.info("RTMS Event Recovery Schedule StartTime: {}", DateUtils.getCurrentDateTimeWithDefault());
        tmsEventRecoveryService.rtmsEventRecoveryReq();
        log.info("RTMS Event Recovery Schedule EndTime: {}", DateUtils.getCurrentDateTimeWithDefault());
    }

}
