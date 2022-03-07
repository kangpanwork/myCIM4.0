package com.fa.cim.utils;

import com.fa.cim.bo.MailConfigBO;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/29       ********              Nyx             create file
 *
 * @author: lightyh
 * @date: 2019/8/29 17:34
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public class MailSessionUtil {
    public static Session getSession(MailConfigBO mailConfigBO){
        Session session = null;
        Properties prop = new Properties();
        prop.setProperty("mail.host", mailConfigBO.getEmailHost());
        prop.setProperty("mail.transport.protocol", mailConfigBO.getEmailProtocol());
        prop.setProperty("mail.smtp.auth", "true");
        prop.setProperty("mail.smtp.port", mailConfigBO.getEmailPort());
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
        } catch (GeneralSecurityException e) {
            log.error("ssl encode fail", e.getMessage());
        }
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailConfigBO.getUsername(), mailConfigBO.getPassword());
            }
        });
        session.setDebug(true);
        Transport ts = null;
        try {
            ts = session.getTransport();
            ts.connect();
            return session;
        }  catch (MessagingException e) {

        }
        return null;
    }

    public static void main(String[] args) throws MessagingException {
        // tencent mail
        MailConfigBO mailConfigBO = new MailConfigBO();
//        mailConfigBO.setUsername("516554097@qq.com");
//        mailConfigBO.setEmailProtocol("smtp");
//        mailConfigBO.setEmailHost("smtp.qq.com");
//        mailConfigBO.setPassword("pusxyobmpcpabhij");
//        mailConfigBO.setEmailPort("465");
        // alimail
//        mailConfigBO.setUsername("hao.huang@fa-software.com");
//        mailConfigBO.setPassword("_fa123456");
//        mailConfigBO.setEmailProtocol("smtp");
//        mailConfigBO.setEmailHost("smtp.mxhichina.com");
//        mailConfigBO.setEmailPort("465");
        // 163邮箱
        mailConfigBO.setUsername("skylightshine@163.com");
        mailConfigBO.setPassword("shine1989");
        mailConfigBO.setEmailProtocol("smtp");
        mailConfigBO.setEmailHost("smtp.163.com");
        mailConfigBO.setEmailPort("465");
        Session session = MailSessionUtil.getSession(mailConfigBO);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailConfigBO.getUsername()));
        message.setSubject("测试");
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("516554097@qq.com"));
        message.setContent("123", "text/html;charset=UTF-8");
      //  Transport ts = session.getTransport();
       // ts.sendMessage(message, message.getAllRecipients());
        Transport.send(message);
    }
}