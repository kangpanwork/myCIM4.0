package com.fa.cim.method.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.GitLabConfig;
import com.fa.cim.config.MailRetCodeConfig;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.entity.nonruntime.CimMsgSentDO;
import com.fa.cim.entity.runtime.messagedefinition.CimMessageDefinitionDO;
import com.fa.cim.entity.runtime.messagedefinition.CimMessageDefinitionUserDO;
import com.fa.cim.entity.runtime.person.CimPersonDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.memorydata.MailMemoryData;
import com.fa.cim.method.IEmailMethod;
import com.fa.cim.newcore.dto.msgdistribution.MessageDTO;
import com.fa.cim.service.IMailService;
import com.fa.cim.service.ProgramParameters;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.RepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @author light
 * @date 2020/5/19 11:31
 * @return
 */
@Service
@Slf4j
public class EmailMethod implements IEmailMethod {

    private static final String DEFAULTSENDFILENAME = "emailtxt.fil";
    private static final String ERRORMAIL = "errormail.fil";
    
    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private MailRetCodeConfig mailRetCodeConfig;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ProgramParameters programParameters;

    @Autowired
    private IMailService mailService;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private MailMemoryData mailMemoryData;

    @Autowired
    private GitLabConfig gitLabConfig;

    @Override
    public void emailSend(MessageDTO.MessageRequest messageRequest) {
        //----------------------------------
        // Read Message Definition Table
        //----------------------------------
        CimMessageDefinitionDO cimMessageDefinitionDO = this.getMessageDefinitionTable(messageRequest);
        //----------------------------------
        // if No Distribution, Return OK
        //----------------------------------
        if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_NOBODY)){
            return;
        }
        //----------------------------------
        // Set Message Address without User
        //----------------------------------
        String email_address = this.setMessageAddress(messageRequest, cimMessageDefinitionDO);
        CimPersonDO cimPersonDO = new CimPersonDO();
        if (!CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_USER)){
            //----------------------------------
            // Read User Infomation Table
            //----------------------------------
            cimPersonDO = this.getUserTableWithUserID(email_address);
            log.info("MessageDistributionServer::Sending Mail to {}", cimPersonDO.getEmailAddress());
        }
        //----------------------------------
        // Set Send File
        //----------------------------------
        String sendFileName = null;
        String resutlContent = null;
        if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageType(), BizConstant.SP_MSGDF_MSGTYPE_FILETRANSFER)
                || CimStringUtils.equals(cimMessageDefinitionDO.getMessageType(), BizConstant.SP_MSGDF_MSGTYPE_TEMPLATE)){
            // CompleteTemplateFile
            sendFileName = DEFAULTSENDFILENAME;
        } else {
            throw new ServiceException(mailRetCodeConfig.getNotSupportMsgdistType());
        }
        if (!CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_USER)){
            //----------------------------------
            // Read User Infomation Table
            //----------------------------------
            cimPersonDO = this.getUserTableWithUserID(email_address);
            if (CimStringUtils.isEmpty(cimPersonDO.getEmailAddress())){
                throw new ServiceException(mailRetCodeConfig.getEmailSendError());
            }
            try {
                resutlContent = this.completeTemplateFile(messageRequest, cimMessageDefinitionDO, cimPersonDO.getEmailAddress());
            } catch (Exception e) {
                throw new ServiceException(mailRetCodeConfig.getEmailSendError());
            }
            mailService.mailSend("MessgaeDistributionServer", cimPersonDO.getEmailAddress(), cimMessageDefinitionDO.getPrimaryMessage(), resutlContent, new File(getResourceBasePath(), sendFileName).getAbsolutePath(), cimMessageDefinitionDO.getMessageType());
        }
        if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_USER)
                || CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_LOTOWNERUSER)
                || CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_EQPOWNERUSER)
                || CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_ROUTEOWNERUSER)
                || CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_OPERATORUSER)){
            //--------------------------------------
            // Select Message Definition User Table
            //--------------------------------------
            List<CimMessageDefinitionUserDO> cimMessageDefinitionUserDOS = this.selectMessageDefinitionUserTable(cimMessageDefinitionDO);
            if (!CimArrayUtils.isEmpty(cimMessageDefinitionUserDOS)){
                for (CimMessageDefinitionUserDO cimMessageDefinitionUserDO : cimMessageDefinitionUserDOS){
                    //----------------------------------
                    // Read User Infomation Table
                    //----------------------------------
                    CimPersonDO userTable = this.getUserTable(cimMessageDefinitionUserDO);
                    if (CimStringUtils.isEmpty(cimPersonDO.getEmailAddress())){
                        throw new ServiceException(mailRetCodeConfig.getEmailSendError());
                    }
                    try {
                        resutlContent = this.completeTemplateFile(messageRequest, cimMessageDefinitionDO, cimPersonDO.getEmailAddress());
                    } catch (Exception e) {
                        throw new ServiceException(mailRetCodeConfig.getEmailSendError());
                    }
                    mailService.mailSend("MessgaeDistributionServer", userTable.getEmailAddress(),
                            cimMessageDefinitionDO.getPrimaryMessage(), resutlContent, new File(getResourceBasePath(), sendFileName).getAbsolutePath(), cimMessageDefinitionDO.getMessageType());

                }
            }
        }
    }

    private CimPersonDO getUserTable(CimMessageDefinitionUserDO cimMessageDefinitionUserDO) {
        //----------------------------------
        // Select Message Definition Table
        //----------------------------------
        String sql = "SELECT\n" +
                "            USER_ID,\n" +
                "            EMAIL_ADDRESS\n" +
                "        From OMUSER\n" +
                "        Where USER_ID = ?";
        return cimJpaRepository.queryOne(sql, CimPersonDO.class, cimMessageDefinitionUserDO.getUserID());
    }

    private List<CimMessageDefinitionUserDO> selectMessageDefinitionUserTable(CimMessageDefinitionDO cimMessageDefinitionDO) {
        //--------------------------------------------
        // Get Count For Message Definition Table User
        //--------------------------------------------
        String sql = "SELECT * FROM OMNOTIFYDEF_USER WHERE REFKEY = ?";
        return cimJpaRepository.query(sql, CimMessageDefinitionUserDO.class, cimMessageDefinitionDO.getId());

    }

    private String completeTemplateFile(MessageDTO.MessageRequest messageRequest, CimMessageDefinitionDO cimMessageDefinitionDO, String emailAddress) throws GitLabApiException {
        File file = new File(getResourceBasePath(), DEFAULTSENDFILENAME);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //----------------------------------
        // Template File Open
        //----------------------------------
        String templatefile = programParameters.getTemplateFilePath();
        if (CimStringUtils.isEmpty(cimMessageDefinitionDO.getTempFileName())){
            throw new ServiceException(mailRetCodeConfig.getTemplateFileOpenError());
        }
        templatefile = templatefile + "/" + cimMessageDefinitionDO.getTempFileName() + ".fil";
        RepositoryFile reposityFile = null;
        try {
            reposityFile = gitLabApi.getRepositoryFileApi().getFile(mailMemoryData.getProjectID(), templatefile, gitLabConfig.getProfiles());
        } catch (GitLabApiException e) {
            throw new ServiceException(mailRetCodeConfig.getEmailSendError());
        }
        String content = new String(Base64.getDecoder().decode(reposityFile.getContent()), StandardCharsets.UTF_8);
        if (CimStringUtils.isEmpty(content)){
            throw new ServiceException(mailRetCodeConfig.getTemplateFileOpenError());
        }
        if (!CimStringUtils.isEmpty(messageRequest.getFloorEventTimeStamp())){
            String floorEventTime = messageRequest.getFloorEventTimeStamp().split("\\.")[0];
            content = content.replaceAll("%A", floorEventTime);
        } else {
            content = content.replaceAll("%A", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getMessageID())){
            content = content.replaceAll("%B", messageRequest.getMessageID());
        } else {
            content = content.replaceAll("%B", " ");
        }
        if (!CimStringUtils.isEmpty(emailAddress)){
            content = content.replaceAll("%C", emailAddress);
        } else {
            content = content.replaceAll("%C", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getLotID())){
            content = content.replaceAll("%D", messageRequest.getLotID());
        } else {
            content = content.replaceAll("%D", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getLotStatus())){
            content = content.replaceAll("%E", messageRequest.getLotStatus());
        } else {
            content = content.replaceAll("%E", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getReasonCode())){
            content = content.replaceAll("%F", messageRequest.getReasonCode());
        } else {
            content = content.replaceAll("%F"," ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getUserID())){
            content = content.replaceAll("%G", messageRequest.getUserID());
        } else {
            content = content.replaceAll("%G", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getOperationNumber())){
            content = content.replaceAll("%H", messageRequest.getOperationNumber());
        } else {
            content = content.replaceAll("%H", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getOperationName())){
            content = content.replaceAll("%I", messageRequest.getOperationName());
        } else {
            content = content.replaceAll("%I", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getProcessDefinitionID())){
            content = content.replaceAll("%J", messageRequest.getProcessDefinitionID());
        } else {
            content = content.replaceAll("%J", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getEquipmentID())){
            content = content.replaceAll("%K", messageRequest.getEquipmentID());
        } else {
            content = content.replaceAll("%K", " ");
        }
        if (!CimStringUtils.isEmpty(messageRequest.getRouteID())){
            content = content.replaceAll("%L", messageRequest.getRouteID());
        } else {
            content = content.replaceAll("%L", " ");
        }
        if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageType(), BizConstant.SP_MSGDF_MSGTYPE_FILETRANSFER)
                || CimStringUtils.equals(cimMessageDefinitionDO.getMessageType(), BizConstant.SP_MSGDF_MSGTYPE_TEMPLATE)){
            if (!CimStringUtils.isEmpty(messageRequest.getMessageText())){
                content = content.replaceAll("%M", messageRequest.getMessageText());
            } else {
                content = content.replaceAll("%M", " ");
            }
        } else {
            content = content.replaceAll("%M", "");
        }
        StringBuffer resutlSb = new StringBuffer();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(cimMessageDefinitionDO.getPrimaryMessage());
            resutlSb.append(cimMessageDefinitionDO.getPrimaryMessage());
            fileWriter.write("\r\n");
            resutlSb.append("<br/>");
            fileWriter.write(cimMessageDefinitionDO.getSecondaryMessage());
            resutlSb.append(cimMessageDefinitionDO.getSecondaryMessage());
            fileWriter.write("\r\n");
            resutlSb.append("<br/>");
            fileWriter.write("\r\n");
            resutlSb.append("<br/>");
            fileWriter.write(content);
            resutlSb.append(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resutlSb.toString();
    }

    @Override
    public void emailSendSystemAdministrator(MessageDTO.MessageRequest messageRequest, OmCode omCode) {
        StringBuffer mailInfoSb = new StringBuffer();
        mailInfoSb.append(omCode.getMessage()).append("\n").append("\n")
                .append(messageRequest.getFloorEventTimeStamp()).append("\n").append("\n")
                .append(messageRequest.getMessageID()).append("\n").append("\n")
                .append(messageRequest.getMessageText()).append("\n").append("\n");
        File file= new File(getResourceBasePath(), ERRORMAIL);
        FileWriter writer = null;
        try {
            if (!file.exists()){
                file.createNewFile();
            }
            writer = new FileWriter(file);
            writer.write(mailInfoSb.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mailService.mailSend("MessgaeDistributionServer", programParameters.getAdministratorMailAddress(), "MessageDistribution Server Process Error", mailInfoSb.toString(), new File(getResourceBasePath(), ERRORMAIL).getAbsolutePath(), null);
    }

    @Override
    public void deleteMessageSentData(int sentMessageCheckTime) {
        //--------------------------------------------
        // Get Delete Mseeage Data in OSMSGDONE Table
        //--------------------------------------------
        String sql = "SELECT * FROM OSMSGDONE WHERE SENT_TIME <= to_char(sysdate - ?/24,'yyyymmddhh24miss')";
        List<CimMsgSentDO> queryResult = cimJpaRepository.query(sql, CimMsgSentDO.class, sentMessageCheckTime);
        if (!CimArrayUtils.isEmpty(queryResult)){
            for (CimMsgSentDO cimMsgSentDO : queryResult){
                cimJpaRepository.delete(cimMsgSentDO);
            }
        }

    }

    @Override
    public void checkMessageSentData(MessageDTO.MessageRequest messageRequest) {
        int sentCount = 0;
        int retCode = 0;
        String sentTime = null;
        try {
            CimMsgSentDO cimMsgSentDO = this.selectMessageSentData(messageRequest);
            sentCount = cimMsgSentDO.getSentCount();
            sentTime = cimMsgSentDO.getSentTime();
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (Validations.isEquals(mailRetCodeConfig.getNotFoundMsgSent(), e.getCode())){
                if (programParameters.getPMsDsMaxSentCount() > sentCount){
                    sentCount++;
                    this.insertMessageSentData(messageRequest);
                } else {
                    throw new ServiceException(mailRetCodeConfig.getNoNeedToSendMail());
                }
            } else {
                throw e;
            }
        }
        if (retCode == 0){
            if (programParameters.getPMsDsMaxSentCount() > sentCount){
                sentCount++;
                this.updateMessageSentData(messageRequest, sentTime, sentCount);
            } else {
                throw new ServiceException(mailRetCodeConfig.getNoNeedToSendMail());
            }
        }
    }

    private void updateMessageSentData(MessageDTO.MessageRequest messageRequest, String sentTime, int sentCount) {
        //--------------------------------------------
        // Update Sending Count to Message Sent Table
        //--------------------------------------------
        CimMsgSentDO cimMsgSentExam = new CimMsgSentDO();
        cimMsgSentExam.setMsgID(CimStringUtils.getValueOrEmptyString(messageRequest.getMessageID()));
        cimMsgSentExam.setLotOwnerID(CimStringUtils.getValueOrEmptyString(messageRequest.getLotOwner()));
        cimMsgSentExam.setEqpOwnerID(CimStringUtils.getValueOrEmptyString(messageRequest.getEquipmentOwner()));
        cimMsgSentExam.setMainPDID(CimStringUtils.getValueOrEmptyString(messageRequest.getRouteOwner()));
        cimMsgSentExam.setLotID(CimStringUtils.getValueOrEmptyString(messageRequest.getLotID()));
        cimMsgSentExam.setLotState(CimStringUtils.getValueOrEmptyString(messageRequest.getLotStatus()));
        cimMsgSentExam.setReasonCode(CimStringUtils.getValueOrEmptyString(messageRequest.getReasonCode()));
        cimMsgSentExam.setUserID(CimStringUtils.getValueOrEmptyString(messageRequest.getUserID()));
        cimMsgSentExam.setOperationNumber(CimStringUtils.getValueOrEmptyString(messageRequest.getOperationNumber()));
        cimMsgSentExam.setPdID(CimStringUtils.getValueOrEmptyString(messageRequest.getProcessDefinitionID()));
        cimMsgSentExam.setEqpID(CimStringUtils.getValueOrEmptyString(messageRequest.getEquipmentID()));
        cimMsgSentExam.setMainPDID(CimStringUtils.getValueOrEmptyString(messageRequest.getRouteID()));
        CimMsgSentDO cimMsgSentDO = cimJpaRepository.findOne(Example.of(cimMsgSentExam)).orElse(null);
        if (cimMsgSentDO != null){
            cimMsgSentDO.setSentCount(sentCount);
            cimJpaRepository.save(cimMsgSentDO);
        }
    }

    private void insertMessageSentData(MessageDTO.MessageRequest messageRequest) {
        //--------------------------------------------
        // Insert Message Sent Data into OSMSGDONE Table
        //--------------------------------------------
        CimMsgSentDO cimMsgSentDO = new CimMsgSentDO();
        cimMsgSentDO.setEventTime(messageRequest.getFloorEventTimeStamp());
        cimMsgSentDO.setMsgID(messageRequest.getMessageID());
        cimMsgSentDO.setLotOwnerID(messageRequest.getLotOwner());
        cimMsgSentDO.setEqpOwnerID(messageRequest.getEquipmentOwner());
        cimMsgSentDO.setMainpdOwnerID(messageRequest.getRouteOwner());
        cimMsgSentDO.setLotID(messageRequest.getLotID());
        cimMsgSentDO.setLotState(messageRequest.getLotStatus());
        cimMsgSentDO.setReasonCode(messageRequest.getReasonCode());
        cimMsgSentDO.setUserID(messageRequest.getUserID());
        cimMsgSentDO.setOperationNumber(messageRequest.getOperationNumber());
        cimMsgSentDO.setPdID(messageRequest.getProcessDefinitionID());
        cimMsgSentDO.setEqpID(messageRequest.getEquipmentID());
        cimMsgSentDO.setMainPDID(messageRequest.getRouteID());
        cimMsgSentDO.setSentTime(CimDateUtils.getCurrentDateTimeWithDefault());
        cimMsgSentDO.setSentCount(1);
        cimJpaRepository.save(cimMsgSentDO);
    }

    private CimMsgSentDO selectMessageSentData(MessageDTO.MessageRequest messageRequest) {
        CimMsgSentDO cimMsgSentExam = new CimMsgSentDO();
        cimMsgSentExam.setMsgID(CimStringUtils.getValueOrEmptyString(messageRequest.getMessageID()));
        cimMsgSentExam.setLotOwnerID(CimStringUtils.getValueOrEmptyString(messageRequest.getLotOwner()));
        cimMsgSentExam.setEqpOwnerID(CimStringUtils.getValueOrEmptyString(messageRequest.getEquipmentOwner()));
        cimMsgSentExam.setMainpdOwnerID(CimStringUtils.getValueOrEmptyString(messageRequest.getRouteOwner()));
        cimMsgSentExam.setLotID(CimStringUtils.getValueOrEmptyString(messageRequest.getLotID()));
        cimMsgSentExam.setLotState(CimStringUtils.getValueOrEmptyString(messageRequest.getLotStatus()));
        cimMsgSentExam.setReasonCode(CimStringUtils.getValueOrEmptyString(messageRequest.getReasonCode()));
        cimMsgSentExam.setUserID(CimStringUtils.getValueOrEmptyString(messageRequest.getUserID()));
        cimMsgSentExam.setOperationNumber(CimStringUtils.getValueOrEmptyString(messageRequest.getOperationNumber()));
        cimMsgSentExam.setPdID(CimStringUtils.getValueOrEmptyString(messageRequest.getProcessDefinitionID()));
        cimMsgSentExam.setEqpID(CimStringUtils.getValueOrEmptyString(messageRequest.getEquipmentID()));
        cimMsgSentExam.setMainPDID(CimStringUtils.getValueOrEmptyString(messageRequest.getRouteID()));
        CimMsgSentDO cimMsgSentDO = cimJpaRepository.findOne(Example.of(cimMsgSentExam)).orElse(null);
        Validations.check(cimMsgSentDO == null, mailRetCodeConfig.getNotFoundMsgSent());
        return cimMsgSentDO;
    }

    private CimMessageDefinitionDO getMessageDefinitionTable(MessageDTO.MessageRequest messageRequest) {
        //----------------------------------
        // Select Message Definition Table
        //----------------------------------
        CimMessageDefinitionDO example = new CimMessageDefinitionDO();
        example.setMessageDefinitionID(messageRequest.getMessageID());
        CimMessageDefinitionDO cimMessageDefinitionDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        Validations.check(cimMessageDefinitionDO == null, retCodeConfigEx.getNotFoundMsgDef());
        return cimMessageDefinitionDO;
    }

    private String setMessageAddress(MessageDTO.MessageRequest messageRequest, CimMessageDefinitionDO cimMessageDefinitionDO){
        String email_address = null;
        //----------------------------------
        // Set Message Address
        //----------------------------------
        if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_LOTOWNER)){
            email_address = messageRequest.getLotOwner();
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_EQPOWNER)){
            email_address = messageRequest.getEquipmentOwner();
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_ROUTEOWNER)){
            email_address = messageRequest.getRouteOwner();
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_OPERATOR)){
            email_address = messageRequest.getUserID();
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_USER)){
            email_address = " ";
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_NOBODY)){
            email_address = " ";
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_LOTOWNERUSER)){
            email_address = messageRequest.getLotOwner();
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_EQPOWNERUSER)){
            email_address = messageRequest.getEquipmentOwner();
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_ROUTEOWNERUSER)){
            email_address = messageRequest.getRouteOwner();
        } else if (CimStringUtils.equals(cimMessageDefinitionDO.getMessageDistributeType(), BizConstant.SP_MSGDF_MSGDISTYPE_OPERATORUSER)){
            email_address = messageRequest.getUserID();
        } else {
            throw new ServiceException(mailRetCodeConfig.getNotSupportMsgdistType());
        }
        log.info("Message Address Select {}", email_address);
        return email_address;
    }

    private CimPersonDO getUserTableWithUserID(String userID){
        //----------------------------------
        // Select Message Definition Table
        //----------------------------------
        String sql = "SELECT\n" +
                "            USER_ID,\n" +
                "            EMAIL_ADDRESS\n" +
                "        From OMUSER\n" +
                "        Where USER_ID = ?";
        CimPersonDO cimPersonDO = cimJpaRepository.queryOne(sql, CimPersonDO.class, userID);
        Validations.check(cimPersonDO == null, retCodeConfig.getNotFoundPerson());
        return cimPersonDO;
    }

    private static String getResourceBasePath() {
        // 获取跟目录
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            // nothing to do
        }
        if (path == null || !path.exists()) {
            path = new File("");
        }

        String pathStr = path.getAbsolutePath();
        pathStr = pathStr.replace("\\target\\classes", "");

        return pathStr;
    }


}
