package com.fa.cim.bo;

import lombok.Data;

import java.sql.Timestamp;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/29       ********              Nyx             create file
 *
 * @author: lightyh
 * @date: 2019/8/29 10:50
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class MailConfigBO {
    private String nickName;
    private String username;
    private String password;
    private String emailHost;
    private String emailProtocol;
    private String emailPort;
    private Timestamp updateTime;
}