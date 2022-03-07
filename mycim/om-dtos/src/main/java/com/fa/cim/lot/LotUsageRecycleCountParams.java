package com.fa.cim.lot;

import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/2/24        ********              Decade               create file
 * * @author: Nyx
 *
 * @date: 2021/2/24 20:19
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class LotUsageRecycleCountParams {

    private Integer usageCount;

    private Integer recycleCount;
}