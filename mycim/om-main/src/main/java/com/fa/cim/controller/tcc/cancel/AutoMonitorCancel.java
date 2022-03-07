package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.autoMonitor.IAutoMonitorController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/14      ********             Neko                create file
 *
 * @author Neko
 * @since 2019/11/14 10:26
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("AutoMonitorCancel")
@Transactional(rollbackFor = Exception.class)
public class AutoMonitorCancel implements IAutoMonitorController {
    @Override
    public Response amJobStatusChangeRpt(Params.AMJobStatusChangeRptInParm param) {
        return null;
    }

    @Override
    public Response amJobLotReserveReq(Params.AMJobLotReserveReqInParams param) {
        return null;
    }

    @Override
    public Response amStatusChangeRpt(Params.AMStatusChangeRptInParm param) {
        return null;
    }

    @Override
    public Response amSetReq(Params.AMSetReqInParm param) {
        return null;
    }

    @Override
    public Response amScheduleChgReq(Params.EqpMonitorScheduleUpdateInParm param) {
        return null;
    }

    @Override
    public Response amRecoverReq(Params.AMRecoverReqParams param) {
        return null;
    }

    @Override
    public Response eqpMonitorWaferUsedCountUpdateReq(Params.EqpMonitorUsedCountUpdateReqInParam eqpMonitorUsedCountUpdateReqInParam) {
        return null;
    }
}
