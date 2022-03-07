package com.fa.cim.lot;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/2/24        ********              Decade               create file
 * * @author: Nyx
 *
 * @date: 2021/2/24 15:40
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class LotNpwUsageRecycleCountUpdateParams {

    private User user;

    private ObjectIdentifier lotID;

    List<WaferUsageRecycleCountUpdateParams> waferUpdateParamsList;

    @Data
    public static class WaferUsageRecycleCountUpdateParams{

        private ObjectIdentifier waferID;

        private Integer usageCount;

        private Integer recycleCount;

    }
}