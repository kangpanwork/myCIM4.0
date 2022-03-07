package com.fa.cim.controller.interfaces.customerDefinedAttribute;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 10:23
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ICustomerDefinedAttributeController {
    /**
     * description:
     * <p>This function carries out registration and deletion of UserData.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/7                            Wind                create file
     *
     * @author: Wind
     * @date: 2019/1/7 09:41
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response cdaValueUpdateReq(Params.CDAValueUpdateReqParams params);
}