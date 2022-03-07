package com.fa.cim.bo;

import lombok.Data;

import javax.persistence.Column;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/20          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/2/20 9:07
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class EmailServerSetting {
    private String emailHost;
    private String emailProtocol;
    private String emailPort;

    public EmailServerSetting(String emailHost, String emailProtocol, String emailPort){
        this.emailHost = emailHost;
        this.emailProtocol = emailProtocol;
        this.emailPort = emailPort;
    }
}