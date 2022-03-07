package com.fa.cim.controller.interfaces.accessManagement;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 13:30
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAccessController {
    /**
     * description: This function changes specific owner ID to another owner ID.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 10:41
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response ownerChangeReq(Params.OwnerChangeReqParams params);
}