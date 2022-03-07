package com.fa.cim.service.impl;

import com.fa.cim.bo.MailConfigBO;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.MailRetCodeConfig;
import com.fa.cim.memorydata.MailMemoryData;
import com.fa.cim.service.IMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;

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
@Service
@Slf4j
public class MailService implements IMailService {

    @Autowired
    private MailMemoryData mailMemoryData;

    @Autowired
    private MailRetCodeConfig mailRetCodeConfig;

    @Override
    public void mailSend(String emailNick, String reciver, String title, String content, String filePath, String messageType) {
        Session session = mailMemoryData.getSession();
        MailConfigBO mailConfig = mailMemoryData.getMailConfig();
        MimeMessage message = new MimeMessage(session);
        try {
            String nick = MimeUtility.encodeText(emailNick);
            message.setFrom(new InternetAddress(nick + "<" + mailConfig.getUsername() + ">"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(reciver));
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");
            if (CimStringUtils.equals(messageType, BizConstant.SP_MSGDF_MSGTYPE_FILETRANSFER)){
                // text
                MimeBodyPart text = new MimeBodyPart();
                text.setContent(content, "text/html;charset=UTF-8");
                // attach
                MimeBodyPart attach = new MimeBodyPart();
                DataHandler file = new DataHandler(new FileDataSource(filePath));
                attach.setDataHandler(file);
                attach.setFileName(file.getName());

                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart(text);
                multipart.addBodyPart(attach);

                message.setContent(multipart);
            }
            Transport.send(message);
        } catch (UnsupportedEncodingException | MessagingException e) {
            throw new ServiceException(new OmCode(mailRetCodeConfig.getEmailSendError()));
        }

    }
}
