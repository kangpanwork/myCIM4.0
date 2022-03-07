package com.fa.cim.controller.interfaces.plan;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********           lightyh                create file
 *
 * @author: lightyh
 * @date: 2019/10/18 22:33
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPlanInqController {
    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/18 22:36
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response lotPlanChangeReserveListInq(Params.LotPlanChangeReserveListInqParams params);

}
