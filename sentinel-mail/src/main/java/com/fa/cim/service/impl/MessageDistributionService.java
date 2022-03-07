package com.fa.cim.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.MailRetCodeConfig;
import com.fa.cim.memorydata.MailMemoryData;
import com.fa.cim.method.IEmailMethod;
import com.fa.cim.newcore.bo.msgdistribution.MessageDistributionManager;
import com.fa.cim.newcore.dto.msgdistribution.MessageDTO;
import com.fa.cim.service.IMessageDistributionService;
import com.fa.cim.service.ProgramParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/13          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/13 16:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public class MessageDistributionService implements IMessageDistributionService {

    @Autowired
    private MessageDistributionManager messageDistributionManager;

    @Autowired
    private MailMemoryData mailMemoryData;

    @Autowired
    private ProgramParameters programParameters;

    @Autowired
    private IEmailMethod emailMethod;

    @Autowired
    private MailRetCodeConfig mailRetCodeConfig;

    @Override
    public void messageDistribution() {
        programParameters.getProgramParameters();
        //-----------------------------------------
        // Server LockUp
        //-----------------------------------------
        if (mailMemoryData.getSession() == null){
            return;
        }
        //-----------------------------------------
        // first Message Request Get
        //-----------------------------------------
        MessageDTO.MessageRequest messageRequest = messageDistributionManager.firstMessageRequest();
        if (messageRequest == null || CimStringUtils.isEmpty(messageRequest.getMessageID())){
            return;
        }
        log.info("Found in firstMessageRequest{}", messageRequest.getFloorEventTimeStamp() + "\n" + messageRequest.getMessageID() + "\n" + messageRequest.getMessageText());
        //-----------------------------------------
        // message Sent Check Function use
        //-----------------------------------------
        if (programParameters.getPMsDsSentMessageCheckTime() == 0){
            //-----------------------------------------
            // email Send
            //-----------------------------------------
            try {
                emailMethod.emailSend(messageRequest);
            } catch (ServiceException e) {
                if (!CimStringUtils.isEmpty(programParameters.getAdministratorMailAddress())){
                    emailMethod.emailSendSystemAdministrator(messageRequest, new OmCode(e.getCode(),e.getMessage()));
                } else {
                    log.info("=== Message watchdog administrator e-mail address is not defined. ===");
                }
            }
        } else {
            try {
                this.sendEmailWithMessageSentData(messageRequest);
            } catch (ServiceException e) {
                log.info("send mail error");
            }
        }
        //-----------------------------------------
        // first Message Request Remove
        //-----------------------------------------
        messageDistributionManager.removeMessageRequest(messageRequest);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    void sendEmailWithMessageSentData(MessageDTO.MessageRequest messageRequest){
        //----------------------------------
        // CleanUp OSMSGDONE Table
        //----------------------------------
        int retCode = 0;
        try {
            emailMethod.deleteMessageSentData(programParameters.getPMsDsSentMessageCheckTime());
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!CimStringUtils.isEmpty(programParameters.getAdministratorMailAddress())){
                emailMethod.emailSendSystemAdministrator(messageRequest, new OmCode(e.getCode(), e.getMessage()));
            } else {
                log.info("=== Message watchdog administrator e-mail address is not defined. ===");
            }
            throw e;
        }
        //-----------------------------------------
        // Check Message Sent Data
        //-----------------------------------------
        int retCodeInner = 0;
        try {
            emailMethod.checkMessageSentData(messageRequest);
        } catch (ServiceException e) {
            retCodeInner = e.getCode();
            if (Validations.isEquals(mailRetCodeConfig.getNoNeedToSendMail(), e.getCode())){
                log.info("=== This message does not need to send. ===");
            } else {
                if (!CimStringUtils.isEmpty(programParameters.getAdministratorMailAddress())){
                    emailMethod.emailSendSystemAdministrator(messageRequest, new OmCode(e.getCode(), e.getMessage()));
                } else {
                    log.info("=== Message watchdog administrator e-mail address is not defined. ===");
                }
                throw e;
            }
        }
        if (retCodeInner == 0){
            //-----------------------------------------
            // email Send
            //-----------------------------------------
            try {
                emailMethod.emailSend(messageRequest);
            } catch (ServiceException e) {
                if (!CimStringUtils.isEmpty(programParameters.getAdministratorMailAddress())){
                    emailMethod.emailSendSystemAdministrator(messageRequest, new OmCode(e.getCode(), e.getMessage()));
                } else {
                    log.info("=== Message watchdog administrator e-mail address is not defined. ===");
                }
                throw e;
            }
        }
    }
}