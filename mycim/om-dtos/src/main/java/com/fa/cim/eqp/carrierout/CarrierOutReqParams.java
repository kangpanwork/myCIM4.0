package com.fa.cim.eqp.carrierout;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description: Task-461
 *              CarrierOutPortReturnReq Params
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/28                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2021/7/28 17:19
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class CarrierOutReqParams {
    private User user;
    private ObjectIdentifier equipmentID;
    private ObjectIdentifier carrierID;
    private ObjectIdentifier portID;
}
