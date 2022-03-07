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
 * @date: 2019/7/31 10:27
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ICustomerDefinedAttributeInqController {
    /**
     * description:
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/4                            Wind                create file
     *
     * @author: Wind
     * @date: 2019/1/4 14:20
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response cdaValueInq(Params.CDAValueInqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/26        ********            Arnold        create file
     *
     * @author: Arnold
     * @date: 2018/11/26 09:27
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response cdaInfoInq(Params.CDAInfoInqParams params);
}