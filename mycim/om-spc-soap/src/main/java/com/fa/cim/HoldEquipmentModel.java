package com.fa.cim;

import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/3/20          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/3/20 18:03
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class HoldEquipmentModel {
    private boolean equipmentHold;
    private String equipmentId;
    private String reason;
}