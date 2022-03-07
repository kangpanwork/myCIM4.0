package com.fa.cim.job;

import com.alibaba.fastjson.JSON;
import com.fa.cim.bo.MailConfigBO;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.memorydata.MailMemoryData;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.utils.BaseUtil;
import com.fa.cim.utils.MailSessionUtil;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.mail.Session;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/17          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/2/17 11:29
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
public class EmailConfigSyncJob extends QuartzJobBean {

    @Autowired
    private MailMemoryData mailMemoryData;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        String value = StandardProperties.SP_MAIL_SENDER.getValue();
        if (CimStringUtils.isEmpty(value)){
            return;
        }
        MailConfigBO mailConfigBO = JSON.parseObject(value, MailConfigBO.class);
        MailConfigBO mailConfigOldDO = mailMemoryData.getMailConfig();
        boolean isTheSame;
        if (mailConfigOldDO != null && mailConfigBO != null){
            if(mailConfigBO.getUpdateTime().compareTo(mailConfigOldDO.getUpdateTime()) != 0){
                isTheSame = false;
            } else {
                isTheSame = BaseUtil.compareObject(mailConfigOldDO, mailConfigBO);
            }
        } else {
            isTheSame = BaseUtil.compareObject(mailConfigOldDO, mailConfigBO);
        }
        if (!isTheSame){
            Session session = MailSessionUtil.getSession(mailConfigBO);
            if (session != null){
                mailMemoryData.setMailConfigDO(mailConfigBO);
                mailMemoryData.setSession(session);
            }
        }
    }
}