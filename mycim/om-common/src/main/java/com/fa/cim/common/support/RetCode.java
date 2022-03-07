package com.fa.cim.common.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * description:
 * This Class use to define the return CimCode, it include as transaction ID, return CimCode,...
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
@NoArgsConstructor
public class RetCode<T> implements Serializable {
    private static final Long serialVersionUID = -124517398664636512L;

    private String transactionID;       // Transaction ID
    private OmCode returnCode;            // Return CimCode. it include return code and return message
    private String messageID;           // Message ID
    private String messageText;         // Message Text
    private String reasonText;          // Reason Text
    private Object reserve;             // Reserved for myCIM4.0 customization
    private T object;

    public RetCode(String transactionID, OmCode returnCode, String reasonText){
        this.transactionID = transactionID;
        this.returnCode = returnCode;
        this.reasonText = reasonText;
    }
}
