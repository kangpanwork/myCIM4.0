package com.fa.cim.crcp;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

import java.util.List;

/**
 * description: 获取被禁用的chamber
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/16          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/16 15:10
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class DisabledChamberQueryParam {

    public static final String TX_ID = "ECLPQ004";
    private User user;

    /**
     * 设备ID
     */
    private ObjectIdentifier equipmentId;

    /**
     * machine recipe
     */
    private List<ObjectIdentifier> baseRecipeIds;
}
