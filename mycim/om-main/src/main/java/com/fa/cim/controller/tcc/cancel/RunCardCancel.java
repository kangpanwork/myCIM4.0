package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.runCard.IRunCardController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/12 15:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("RunCardCancel")
@Transactional(rollbackFor = Exception.class)
public class RunCardCancel implements IRunCardController {
    @Override
    public Response runCardUpdateReq(Params.RunCardUpdateReqParams params) {
        return null;
    }

    @Override
    public Response runCardDeleteReq(Params.RunCardDeleteReqParams param) {
        return null;
    }


    @Override
    public Response runCardApprovalReq(Params.RunCardStateApprovalReqParams params) {
        return null;
    }

    @Override
    public Response runCardStateChangeReq(Params.RunCardStateChangeReqParams params) {
        return null;
    }

}
