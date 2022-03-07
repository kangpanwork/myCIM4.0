package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.User;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IAuthServerMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/15        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/10/15 11:25
 * Cpyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class AuthServerMethod  implements IAuthServerMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since 2018/10/15 13:13
     * @param objCommon -
     * @param user -
     * @param forceLDAPFlag -
     */
    @Override
    public void authSvrSendAuthenticate(Infos.ObjCommon objCommon, User user, Long forceLDAPFlag) {

        //【step1】get environment variable
        log.debug("【step1】get environment variable");

        // todo we don`t user auth server to check privilege.
        // todo This method has no processing logic

    }

    @Override
    public String authSvrGetAuthServer(Infos.ObjCommon objCommon, String authServerServerName, String authServerHostName) {
        //if flag is set to TRUE, MMS always bind to auth server
        Boolean execFlag = false;
        Boolean existFlag = false;

        //【joseph】todo - we don't user auth server to check privilege.
        return null;
    }
}
