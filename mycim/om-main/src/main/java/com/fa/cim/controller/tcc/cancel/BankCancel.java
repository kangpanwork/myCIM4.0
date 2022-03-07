package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.bank.IBankController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/26          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/26 14:24
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("BankCancel")
@Transactional(rollbackFor = Exception.class)
public class BankCancel implements IBankController {
    @Override
    public Response bankMoveReq(Params.BankMoveReqParams bankMoveReqParams) {
        return null;
    }

    @Override
    public Response holdLotInBankReq(Params.HoldLotInBankReqParams holdLotInBankReqParams) {
        return null;
    }

    @Override
    public Response holdLotReleaseInBankReq(Params.HoldLotReleaseInBankReqParams holdLotReleaseInBankReqParams) {
        return null;
    }

    @Override
    public Response materialPrepareCancelReq(Params.MaterialPrepareCancelReqParams params) {
        return null;
    }

    @Override
    public Response lotPreparationCancelExReq(Params.LotPreparationCancelExReqParams params) {
        return null;
    }

    @Override
    public Response nonProdBankStoreReq(Params.NonProdBankStoreReqParams params) {
        return null;
    }

    @Override
    public Response nonProdBankReleaseReq(Params.NonProdBankReleaseReqParams params) {
        return null;
    }

    @Override
    public Response unshipReq(Params.BankMoveReqParams unshipReqParams) {
        return null;
    }

    @Override
    public Response shipReq(Params.BankMoveReqParams shipReqParams) {
        return null;
    }

    @Override
    public Response materialPrepareReq(Params.MaterialPrepareReqParams materialPrepareReqParams) {
        return null;
    }

    @Override
    public Response materialReceiveAndPrepareReq(Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams) {
        return null;
    }

    @Override
    public Response bankInCancelReq(Params.BankInCancelReqParams params) {
        return null;
    }

    @Override
    public Response bankInReq(Params.BankInReqParams params) {
        return null;
    }

    @Override
    public Response vendorLotReceiveReq(Params.VendorLotReceiveParams vendorLotReceiveParams) {
        return null;
    }

    @Override
    public Response vendorLotReturnReq(Params.VendorLotReturnParams vendorLotReturnParams) {
        return null;
    }
}