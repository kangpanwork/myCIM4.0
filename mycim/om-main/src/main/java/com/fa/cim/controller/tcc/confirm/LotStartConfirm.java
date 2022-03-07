package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.lotStart.ILotStartController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 13:49
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("LotStartConfirm")
@Transactional(rollbackFor = Exception.class)
public class LotStartConfirm implements ILotStartController {
    @Override
    public Response npwLotStartReq(Params.NPWLotStartReqParams params) {
        return null;
    }

    @Override
    public Response waferAliasSetReq(Params.WaferAliasSetReqParams params) {
        return null;
    }

    @Override
    public Response waferLotStartCancelReq(Params.WaferLotStartCancelReqParams waferLotStartCancelReqParams) {
        return null;
    }

    @Override
    public Response waferLotStartReq(Params.WaferLotStartReqParams waferLotStartReqParams) {
        return null;
    }
}