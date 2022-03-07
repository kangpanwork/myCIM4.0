package com.fa.cim.common.support;

import lombok.*;

import java.io.Serializable;

/**
 * description:
 * This Class use to define the user information. just as userID, password,...
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Bear         create file
 *
 * @author: Bear
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class User implements Serializable {
    private static final Long serialVersionUID = 741242368963235L;
    /**
     * User ID
     */
    private ObjectIdentifier userID;
    /**
     * Password
     */
    private String password;
    /**
     * New Password
     */
    private String newPassword;
    /**
     * Function ID. For example, the Function ID of TxFutureHoldReq is "TXPC041".
     */
    private String functionID;
    /**
     * Client Node
     */
    private String clientNode;
    /**
     * Reserved for SI customization
     */
    private Object reserve;

    public User(ObjectIdentifier pUserID) {
        this.userID = pUserID;
    }

    public User duplicate() {
        User user = new User();
        user.setUserID(userID.copy());
        user.setReserve(reserve);
        user.setNewPassword(newPassword);
        user.setPassword(password);
        user.setClientNode(clientNode);
        return user;
    }
}
