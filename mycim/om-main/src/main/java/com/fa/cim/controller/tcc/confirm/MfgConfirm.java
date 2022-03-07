package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.controller.interfaces.mfg.IMfgController;
import com.fa.cim.dto.Params;
import com.fa.cim.mfg.MfgInfoImportParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>MfgConfirm .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/11/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/11/9/009 10:51
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("MfgConfirm")
@Transactional(rollbackFor = Exception.class)
public class MfgConfirm implements IMfgController {
    @Override
    public Response mfgRestrictCancelReq(Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams) {
        return null;
    }

    @Override
    public Response mfgRestrictReq(Params.MfgRestrictReq_110Params mfgRestrictReqParams) {
        return null;
    }

    @Override
    public Response mfgModifyReq(Params.MfgModifyMMReqParams mfgModifyMMReqParams) {
        return null;
    }

    @Override
    public Response mfgRecipeTimeLimitSet(Params.RecipeTimeSetParams params) {
        return null;
    }

    @Override
    public Response mfgRecipeTimeLimitDelete(Params.RecipeTimeCancelParams params) {
        return null;
    }

    @Override
    public Response mfgRecipeUseSet(Params.RecipeUseSetParams params) {
        return null;
    }

    @Override
    public Response mfgRecipeUseCheck(User user) {
        return null;
    }

    @Override
    public Response mfgInfoImportReq(MfgInfoImportParams mfgInfoImportParams) {
        return null;
    }
}
