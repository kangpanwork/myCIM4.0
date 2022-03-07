package com.fa.cim.memorydata;

import com.fa.cim.bo.EmailServerSetting;
import com.fa.cim.bo.MailConfigBO;
import com.fa.cim.newcore.dto.msgdistribution.MessageDTO;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/29       ********              Nyx             create file
 *
 * @author: lightyh
 * @date: 2019/8/29 10:46
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
public class MailMemoryData {

    private MailConfigBO mailConfig;

    private Session session;

    private Map<String, EmailServerSetting> emailSettingMap = new HashMap<>();

    private ConcurrentSkipListMap<String, MessageDTO.MessageRequest> messageRequestMap;

    private int projectID;

    public void init(){
        emailSettingMap.put("ali", new EmailServerSetting("smtp.mxhichina.com", "smtp", "465"));
        emailSettingMap.put("qq", new EmailServerSetting("smtp.qq.com", "smtp", "465"));
        emailSettingMap.put("163", new EmailServerSetting("smtp.163.com", "smtp", "465"));
        messageRequestMap = new ConcurrentSkipListMap<>();
    }

    public MailConfigBO getMailConfig() {
        return mailConfig;
    }

    public void setMailConfigDO(MailConfigBO mailConfig) {
        this.mailConfig = mailConfig;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Map<String, EmailServerSetting> getEmailSettingMap(){
        return emailSettingMap;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public ConcurrentSkipListMap<String, MessageDTO.MessageRequest> getMessageRequestMap() {
        return messageRequestMap;
    }

    public void setMessageRequestMap(ConcurrentSkipListMap<String, MessageDTO.MessageRequest> messageRequestMap) {
        this.messageRequestMap = messageRequestMap;
    }
}