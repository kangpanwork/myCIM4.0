package com.fa.cim.controller.interfaces.accessManagement;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

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
public interface IAccessInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/30          ********            Nyx                create file
     *
     * @author: Nyx
     * @date: 2019/7/30 17:46
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response basicUserInfoInq(Params.BasicUserInfoInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/3/16 17:12
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response allUserInfoInq(Params.AllUserInfoInqParams params);

    public Response loginCheckInq(@RequestBody Params.LoginCheckInqParams params);

    public Response accessControlCheckInq(@RequestBody Params.AccessControlCheckInqParams params);
}