package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.bond.IBondController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>BondingConfirm .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/16 10:57         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/16 10:57
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Service("BondConfirm")
@Transactional(rollbackFor = Exception.class)
public class BondConfirm implements IBondController {

    @Override
    public Response bondingGroupModifyReq(Params.BondingGroupUpdateReqInParams bondingGroupUpdateReqInParams) {
        return null;
    }

    @Override
    public Response bondingMapRpt(Params.BondingMapResultRptInParams bondingMapResultRptInParams) {
        return null;
    }

    @Override
    public Response waferBondReq(Params.WaferStackingReqInParams waferStackingReqInParams) {
        return null;
    }

    @Override
    public Response waferBondCancelReq(Params.WaferStackingCancelReqInParams waferStackingCancelReqInParams) {
        return null;
    }

    @Override
    public Response bondingGroupPartialRemoveReq(Params.BondingGroupPartialReleaseReqInParam bondingGroupPartialReleaseReqInParams) {
        return null;
    }
}
