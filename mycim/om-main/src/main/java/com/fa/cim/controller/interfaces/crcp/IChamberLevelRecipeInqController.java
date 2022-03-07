package com.fa.cim.controller.interfaces.crcp;

import com.fa.cim.common.support.Response;
import com.fa.cim.crcp.ChamberLevelRecipeReserveParam;

/**
 * description: chamber level recipe inq controller
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/26          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/26 15:11
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IChamberLevelRecipeInqController {

    /**
     * description:  chamber level recipe
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/26 15:19                        YJ                Create
     *
     * @param chamberLevelRecipeReserveParam - chamber level recipe param
     * @return start carrier
     * @author YJ
     * @date 2021/9/26 15:19
     */
    Response chamberLevelRecipeDesignatedInq(ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam);
}
