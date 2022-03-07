package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.systemConfig.ISystemController;
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
 * @date: 2019/7/30 14:26
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("SystemConfigCancel")
@Transactional(rollbackFor = Exception.class)
public class SystemCancel implements ISystemController {
    @Override
    public Response omsEnvModifyReq(Params.OMSEnvModifyReqParams params) {
        return null;
    }

    @Override
    public Response alertMessageRpt(Params.AlertMessageRptParams alertMessageRptParams) {
        return null;
    }
}