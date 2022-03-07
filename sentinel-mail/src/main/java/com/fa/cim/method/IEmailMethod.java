package com.fa.cim.method;

import com.fa.cim.common.support.OmCode;
import com.fa.cim.newcore.dto.msgdistribution.MessageDTO;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/13          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/13 18:08
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEmailMethod {

    void emailSend(MessageDTO.MessageRequest messageRequest);

    void emailSendSystemAdministrator(MessageDTO.MessageRequest messageRequest, OmCode omCode);

    void deleteMessageSentData(int entMessageCheckTime);

    void checkMessageSentData(MessageDTO.MessageRequest messageRequest);
}