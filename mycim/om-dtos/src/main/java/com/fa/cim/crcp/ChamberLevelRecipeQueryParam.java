package com.fa.cim.crcp;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * description: chamber level recipe query param
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/15          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/15 10:39
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ChamberLevelRecipeQueryParam {
    public static final String TX_ID = "ECLPQ003";

    /**
     * user
     */
    private User user;

    /**
     * 设备ID
     */
    private ObjectIdentifier equipmentId;

    /**
     * 不可用的chamber,被Chamber state / tool constraint / Tool capability 禁用的chamber
     */
    private List<String> disabledChamber;

    /**
     * 当前设备加工时的recipe
     */
    private ObjectIdentifier baseRecipeId;

}
