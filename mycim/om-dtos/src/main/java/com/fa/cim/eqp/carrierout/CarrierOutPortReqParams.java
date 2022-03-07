package com.fa.cim.eqp.carrierout;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

import java.util.List;

/**
 * description: Task-461
 *              CarrierOutPortReq Params
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/28                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2021/7/28 16:52
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class CarrierOutPortReqParams {
    private User user;
    private ObjectIdentifier equipmentID;
    private List<CarrierOutPortInfo> carrierOutPortInfoList;
}
