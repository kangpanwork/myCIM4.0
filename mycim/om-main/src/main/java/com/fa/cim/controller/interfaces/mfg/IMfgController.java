package com.fa.cim.controller.interfaces.mfg;

import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Params;
import com.fa.cim.mfg.MfgInfoImportParams;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 9:56
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IMfgController {
    /**
     * description:
     * The method use to define the MfgRestrictCancelReqController.
     * transaction ID: OCONW002
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/15        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/15 16:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictCancelReq(Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams);

    /**
     * description:
     * The method use to define the MfgRestrictReqController.
     * transaction ID: OCONW001
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/11        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/11 16:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mfgRestrictReq(Params.MfgRestrictReq_110Params mfgRestrictReqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/11/011 9:57
     */
    Response mfgModifyReq(@RequestBody Params.MfgModifyMMReqParams mfgModifyMMReqParams);

    Response mfgRecipeTimeLimitSet(@RequestBody Params.RecipeTimeSetParams params);

    Response mfgRecipeTimeLimitDelete(@RequestBody Params.RecipeTimeCancelParams params);

    Response mfgRecipeUseSet(@RequestBody Params.RecipeUseSetParams params);

    Response mfgRecipeUseCheck(@RequestBody User user);

    Response mfgInfoImportReq(@Validated MfgInfoImportParams mfgInfoImportParams);
}