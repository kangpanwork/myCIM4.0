package com.fa.cim.service;

import java.io.File;

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
public interface IMailService {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/19 17:00
     * @param emailNick
     * @param reciver
     * @param title
     * @param content
     * @param filePath
     * @param messageType -
     * @return void
     */
    void mailSend(String emailNick, String reciver, String title, String content, String filePath, String messageType);

}
