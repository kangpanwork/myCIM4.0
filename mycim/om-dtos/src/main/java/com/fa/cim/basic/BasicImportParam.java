package com.fa.cim.basic;

import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/5/8        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/5/8 14:39
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class BasicImportParam {

    private String value;


    private String referenceKey;
    /**
     * Password
     */
    private String password;
    /**
     * Function ID. For example, the Function ID of TxFutureHoldReq is "TXPC041".
     */
    private String functionID;

}