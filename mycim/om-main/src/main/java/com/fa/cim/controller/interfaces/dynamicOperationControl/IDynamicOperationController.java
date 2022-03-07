package com.fa.cim.controller.interfaces.dynamicOperationControl;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 16:18
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDynamicOperationController {

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 13:31
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response docLotRemoveReq(Params.DOCLotRemoveReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2019/3/25 14:39
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response docLotActionReq(Params.DOCLotActionReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2019/3/25 14:39
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response docLotInfoSetReq(Params.DOCLotInfoSetReqParams params);
}