package com.fa.cim.tms.status.recovery.dto;

import com.fa.cim.tms.status.recovery.pojo.ObjectIdentifier;
import lombok.*;

import java.io.Serializable;

/**
 * description:
 * This Class use to define the user information. just as userID, password,...
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Miner         create file
 *
 * @author: Miner
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


    public User(ObjectIdentifier pUserID) {
        this.userID = pUserID;
    }
}
