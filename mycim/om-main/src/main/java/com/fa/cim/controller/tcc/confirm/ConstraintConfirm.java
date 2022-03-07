package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.constraint.IConstraintController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 9:57
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("ConstraintConfirm")
@Transactional(rollbackFor = Exception.class)
public class ConstraintConfirm implements IConstraintController {
    @Override
    public Response mfgRestrictCancelReq(Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams) {
        return null;
    }

    @Override
    public Response mfgRestrictReq(Params.MfgRestrictReqParams mfgRestrictReqParams) {
        return null;
    }

    @Override
    public Response mfgRestrictExclusionLotCancelReq(Params.MfgRestrictExclusionLotReqParams cancelParams) {
        return null;
    }

    @Override
    public Response mfgRestrictExclusionLotReq(Params.MfgRestrictExclusionLotReqParams params) {
        return null;
    }

    @Override
    public Response constraintEqpAddReq(Params.ConstraintEqpAddReqParams params) {
        return null;
    }

    @Override
    public Response constraintEqpModifyReq(Params.ConstraintEqpModifyReqParams params) {
        return null;
    }

    @Override
    public Response constraintEqpCancelReq(Params.ConstraintEqpCancelReqParams params) {
        return null;
    }
}