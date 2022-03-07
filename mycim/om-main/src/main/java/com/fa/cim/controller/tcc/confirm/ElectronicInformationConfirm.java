package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.electronicInformation.IElectronicInformationController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 17:38
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("ElectronicInformationConfirm")
@Transactional(rollbackFor = Exception.class)
public class ElectronicInformationConfirm implements IElectronicInformationController {

    @Override
    public Response eboardInfoSetReq(Params.EboardInfoSetReqParams params) {
        return null;
    }

    @Override
    public Response lotMemoAddReq(Params.LotMemoAddReqParams params) {
        return null;
    }

    @Override
    public Response lotOpeMemoAddReq(Params.LotOperationNoteInfoRegisterReqParams params) {
        return null;
    }

    @Override
    public Response eqpBoardWorkZoneBindingReq(Params.EqpBoardWorkZoneBindingParams eqpBoardWorkZoneBindingParams) {
        return null;
    }

    @Override
    public Response eqpAreaCancelReq(Params.EqpAreaCancelParams eqpAreaCancelParams) {
        return null;
    }

    @Override
    public Response eqpAreaMoveReq(Params.EqpAreaMoveParams eqpAreaMoveParams) {
        return null;
    }
}