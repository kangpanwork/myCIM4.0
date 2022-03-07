package com.fa.cim.controller.tcc.confirm;

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
 * @date: 2019/7/30 14:27
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("SystemConfigConfirm")
@Transactional(rollbackFor = Exception.class)
public class SystemConfirm implements ISystemController {
    @Override
    public Response omsEnvModifyReq(Params.OMSEnvModifyReqParams params) {
        return null;
    }

    @Override
    public Response alertMessageRpt(Params.AlertMessageRptParams alertMessageRptParams) {
        return null;
    }
}