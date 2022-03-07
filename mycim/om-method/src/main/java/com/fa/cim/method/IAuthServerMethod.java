package com.fa.cim.method;

import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;

/**
 * description:
 * This file use to define the IAuthServerMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/15        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/10/15 11:21
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAuthServerMethod {
    
    /**     
     * description:
     * <p>
     *     Bind to Authserver, and then call authentication method for requested user.
     *     Specifying forceLDAPFlag, you can change the behavior of Authserver.
     *        1 : Authserver always accesses to LDAP server for authentication.
     *        else : Authserver accesses cash in memory at first,
     *              then accesses to LDAP server if requested user is not found in cash.
     *      method:AuthSvr_SendAuthenticate
     * </p>
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since 2018/10/15 11:23
     * @param objCommon - 
     * @param user - 
     * @param forceLDAPFlag -
     */
    void authSvrSendAuthenticate(Infos.ObjCommon objCommon, User user, Long forceLDAPFlag);

    /**     
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since 2018/10/15 13:55
     * @param objCommon - 
     * @param authServerServerName -
     * @param authServerHostName -  
     * @return java.lang.String
     */
    String authSvrGetAuthServer(Infos.ObjCommon objCommon, String authServerServerName, String authServerHostName);
}