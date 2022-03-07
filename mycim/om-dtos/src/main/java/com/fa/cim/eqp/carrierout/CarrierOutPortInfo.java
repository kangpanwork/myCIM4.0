package com.fa.cim.eqp.carrierout;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.Data;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/28                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2021/7/28 17:20
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class CarrierOutPortInfo {
    private ObjectIdentifier carrierID;
    private ObjectIdentifier portID;
}
