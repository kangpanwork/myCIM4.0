package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.processControl.IProcessController;
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
 * @date: 2019/7/30 15:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("ProcessControlCancel")
@Transactional(rollbackFor = Exception.class)
public class ProcessControlCancel implements IProcessController {
    @Override
    public Response futureHoldReq(Params.FutureHoldReqParams params) {
        return null;
    }

    @Override
    public Response futureHoldCancelReq(Params.FutureHoldCancelReqParams params) {
        return null;
    }

    @Override
    public Response npwUsageStateModifyReq(Params.NPWUsageStateModifyReqParams params) {
        return null;
    }

    @Override
    public Response lagTimeActionReq(Params.LagTimeActionReqParams params) {
        return null;
    }

    @Override
    public Response qtimeActionReq(Params.QtimeActionReqParam qtimeActionReqParam) {
        return null;
    }

    @Override
    public Response qtimerReq(Params.QtimerReqParams params) {
        return null;
    }

    @Override
    public Response qtimeManageActionByPostTaskReq(Params.QtimeManageActionByPostTaskReqInParm strQtimeManageActionByPostTaskReqInParm) {
        return null;
    }

    @Override
    public Response futureReworkCancelReq(Params.FutureReworkCancelReqParams futureReworkCancelReqParams) {
        return null;
    }

    @Override
    public Response processHoldCancelReq(Params.ProcessHoldCancelReq param) {
        return null;
    }

    @Override
    public Response processHoldReq(Params.ProcessHoldReq param) {
        return null;
    }

    @Override
    public Response futureReworkReq(Params.FutureReworkReqParams params) {
        return null;
    }

    @Override
    public Response futureReworkActionDoReq(Params.FutureReworkActionDoReqParams params) {
        return null;
    }

    @Override
    public Response futureHoldDepartmentChangeReq(Params.FutureHoldReqParams params) {
        return null;
    }

    @Override
    public Response processHoldDepartmentChangeReq(Params.ProcessHoldReq params) {
        return null;
    }
}