package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.controlJobProcessJob.IControlJobProcessJobController;
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
 * @date: 2019/7/31 10:35
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("ControlJobProcessJobCancel")
@Transactional(rollbackFor = Exception.class)
public class ControlJobProcessJobCancel implements IControlJobProcessJobController {
    @Override
    public Response cjStatusChangeReq(Params.CJStatusChangeReqParams cjStatusChangeReqParams) {
        return null;
    }

    @Override
    public Response pjInfoRpt(Params.PJInfoRptParams params) {
        return null;
    }

    @Override
    public Response pjStatusChangeReq(Params.PJStatusChangeReqParams params) {
        return null;
    }

    @Override
    public Response pjStatusChangeRpt(Params.PJStatusChangeRptInParm params) {
        return null;
    }

    @Override
    public Response processJobMapInfoRpt(Params.ProcessJobMapInfoRptParam processJobMapInfoRptParam) {
        return null;
    }
}