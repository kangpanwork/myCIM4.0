package com.fa.cim.config;

import com.fa.cim.common.support.OmCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * description:
 *      This Class is Reference to the mail-code.properties, when we add one data in mail-code.properties,
 *      We must add the define in the ReCodeConfig Class, too. their reference satisfy the following rules. for example.
 *      mail-code.properties                 RetCodeConfig
 *      rc.succ = 0                                     private CimCode succ            // when we start the service, the succ's value is 0.
 *      rc.not_found_bank = 1422                        private CimCode notFoundBank;   // when we start the service, the notFoundBank's value is 1422.
 *
 *      note: The statement of constants can't be modified, only the constants value could be modified.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Bear         create file
 *
 * @author: Bear
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@PropertySource("classpath:mail-code.properties")
@ConfigurationProperties(prefix="rc")
@Setter
@Getter
public class MailRetCodeConfig {
    public static final int SUCCESS_CODE = 0;
    public static final int WARNING_CODE = 1;
    public static final int ERROR_CODE = 2;
    public static final int SYSTEM_ERROR = 2037;

    private OmCode succ;                          //succ = (0, "succ")
    private OmCode warn;                          //warn = (1, "%s")
    private OmCode error;                         //error = (2, "%s")
    private OmCode templateFileOpenError;       // (107, "Message Template File Open Error")
    private OmCode notSupportMsgdistType ;    //(109, "No User Information in Message Definition Table (FRMSGDEF_USER)")
    private OmCode emailSendError;           //rc.email_send_error = (111, "EMail Send Error")
    private OmCode programParameterError;      // rc.programparameter_error = (115, "Getting program parameter failed.")
    private OmCode notFoundMsgSent;  // rc.not_found_msgsent = (118, "No Send Message in Message Sent Table")
    private OmCode noNeedToSendMail;

}
