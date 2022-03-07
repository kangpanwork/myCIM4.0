package com.fa.cim.service;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.MailRetCodeConfig;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/13          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/13 22:26
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class ProgramParameters {

    @Autowired
    private MailRetCodeConfig mailRetCodeConfig;

    @Getter
    private int PMsDsSentMessageCheckTime;

    @Getter
    private String administratorMailAddress;

    @Getter
    private int PMsDsMaxSentCount;

    @Getter
    private String templateFilePath;

    public void getProgramParameters(){
        //------------------------------
        // Sent Message Check Time Get
        //------------------------------
        String sentMessageCheckTimeSTRING = StandardProperties.SENTMESSAGECHECKTIME_STRING.getValue();
        if (CimStringUtils.isEmpty(sentMessageCheckTimeSTRING)){
            PMsDsSentMessageCheckTime = BizConstant.DEFAULTSENTMESSAGECHECKTIME;
        } else {
            PMsDsSentMessageCheckTime = Integer.parseInt(sentMessageCheckTimeSTRING);
        }

        //------------------------------
        // Administrator Email Address
        // Though e-mail address is not retrieved, program continue (R30)
        //------------------------------
        administratorMailAddress = StandardProperties.OM_MAIL_SENTINEL_EMAIL_ID.getValue();
        if (CimStringUtils.isEmpty(administratorMailAddress)){
            log.info("MsDsSvMg AdministratorMailAddress is not defined.");
        } else {
            log.info("MsDsSvMg AdministratorMailAddress get Successful {}", administratorMailAddress);
        }

        //------------------------------
        // Sent Message Check Time Get
        //------------------------------
        String maxSentCountString = StandardProperties.OM_MAIL_SENTINEL_MAX_SEND_COUNT.getValue();
        if (CimStringUtils.isEmpty(maxSentCountString)){
            PMsDsMaxSentCount = BizConstant.DEFAULTMAXSENTCOUNT;
        } else {
            PMsDsMaxSentCount = Integer.parseInt(maxSentCountString);
            if (PMsDsMaxSentCount == 0){
                PMsDsMaxSentCount = BizConstant.DEFAULTMAXSENTCOUNT;
            }
        }
        //------------------------------
        // Template File Path
        //------------------------------
        templateFilePath = StandardProperties.TEMPLATEFILEPATH_STRING.getValue();
        if (CimStringUtils.isEmpty(templateFilePath)){
            throw new ServiceException(mailRetCodeConfig.getProgramParameterError());
        }
    }
}