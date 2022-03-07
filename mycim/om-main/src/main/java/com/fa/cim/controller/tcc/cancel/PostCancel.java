package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.post.IPostController;
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
 * @date: 2019/7/31 9:43
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("PostCancel")
@Transactional(rollbackFor = Exception.class)
public class PostCancel implements IPostController {
    @Override
    public Response postTaskExecuteReq(Params.PostTaskExecuteReqParams postTaskExecuteReqParams) {
        return null;
    }

    @Override
    public Response postTaskRegisterReq(Params.PostTaskRegisterReqParams postTaskRegisterReqParams) {
        return null;
    }

    @Override
    public Response postActionModifyReq(Params.PostActionModifyReqParams postActionModifyReqParams) {
        return null;
    }

    @Override
    public Response postFilterCreateForExtReq(Params.PostFilterCreateForExtReqParams params) {
        return null;
    }

    @Override
    public Response postFilterRemoveForExtReq(Params.PostFilterRemoveForExtReqParams params) {
        return null;
    }

    @Override
    public Response lotCassettePostProcessForceDeleteReq(Params.StrLotCassettePostProcessForceDeleteReqInParams params) {
        return null;
    }
}