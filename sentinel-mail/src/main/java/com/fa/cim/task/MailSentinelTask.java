package com.fa.cim.task;


import com.fa.cim.service.IMessageDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/20       ********              lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/8/20 0:07
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Controller
@EnableScheduling
public class MailSentinelTask {

    @Autowired
    private IMessageDistributionService messageDistributionService;

    @Scheduled(cron = "*/30 * * * * ?")
    public void senMail() {
        messageDistributionService.messageDistribution();
    }
}
