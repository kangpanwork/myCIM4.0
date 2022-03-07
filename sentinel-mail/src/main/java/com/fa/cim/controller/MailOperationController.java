package com.fa.cim.controller;

import com.fa.cim.bo.EmailServerSetting;
import com.fa.cim.bo.MailConfigBO;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.memorydata.MailMemoryData;
import com.fa.cim.service.IMessageDistributionService;
import com.fa.cim.utils.MailSessionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.Session;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/30       ********              Nyx             create file
 *
 * @author: lightyh
 * @date: 2019/8/30 13:41
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RestController
@RequestMapping("/mail")
public class MailOperationController {

    @Autowired
    private MailMemoryData mailMemoryData;

    @Autowired
    private IMessageDistributionService messageDistributionService;

    @RequestMapping(value = "/check_email_canwork", method = RequestMethod.POST)
    public Response checkEmailCanWork(){
        Session session = mailMemoryData.getSession();
        if (session != null){
            return Response.createSucess(0, "email system runs well,you can use in");
        } else {
            return Response.createError(-1, "email system can not work, please initialize a sender");
        }
    }

    @RequestMapping(value = "/check_email_valid", method = RequestMethod.POST)
    public Response checkEmailValid(@RequestBody Params.EmailInfo emailInfo){
        if (emailInfo == null){
            return Response.createError(-1, "your email must not be null");
        }
        if (CimStringUtils.isEmpty(emailInfo.getMailType())){
            return Response.createError(-1, "mailType can not be null");
        }
        MailConfigBO mailConfigBO = new MailConfigBO();
        BeanUtils.copyProperties(emailInfo, mailConfigBO);
        EmailServerSetting emailServerSetting = mailMemoryData.getEmailSettingMap().get(emailInfo.getMailType());
        mailConfigBO.setEmailHost(emailServerSetting.getEmailHost());
        mailConfigBO.setEmailProtocol(emailServerSetting.getEmailProtocol());
        mailConfigBO.setEmailPort(emailServerSetting.getEmailPort());
        Session session = MailSessionUtil.getSession(mailConfigBO);
        if (session != null){
            return Response.createSucess(0, "your email can work, you can initialize it now");
        } else {
            return Response.createSucess(-1, "your email is invalid, please check it");
        }
    }

    @RequestMapping(value = "/update_email_info", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Response updateEmailInfo(@RequestBody Params.EmailInfo emailInfo){
        if (emailInfo == null){
            return Response.createError(-1, "your email must not be null");
        }
        if (CimStringUtils.isEmpty(emailInfo.getMailType())){
            return Response.createError(-1, "mailType can not be null");
        }
        MailConfigBO mailConfigBO = new MailConfigBO();
        BeanUtils.copyProperties(emailInfo, mailConfigBO);
        EmailServerSetting emailServerSetting = mailMemoryData.getEmailSettingMap().get(emailInfo.getMailType());
        mailConfigBO.setEmailHost(emailServerSetting.getEmailHost());
        mailConfigBO.setEmailProtocol(emailServerSetting.getEmailProtocol());
        mailConfigBO.setEmailPort(emailServerSetting.getEmailPort());
        Session session = MailSessionUtil.getSession(mailConfigBO);
        if (session == null){
            return Response.createError(-1, "your email is invalid, please check it");
        }
        try {
            mailConfigBO.setUpdateTime(CimDateUtils.getCurrentTimeStamp());
//            environmentVariableManager.createValue(EnvConst.SP_MAIL_SENDER, JSON.toJSONString(mailConfigBO), "EMAIL_CONFIG");
            return Response.createSucess(0, "update success");
        } catch (Exception e){
            e.printStackTrace();
            return Response.createSucess(-1, "system error");
        }
    }

}