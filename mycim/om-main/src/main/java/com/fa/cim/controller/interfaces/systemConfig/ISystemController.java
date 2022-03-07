package com.fa.cim.controller.interfaces.systemConfig;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 14:24
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISystemController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/5        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/12/ 14:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response omsEnvModifyReq(@RequestBody Params.OMSEnvModifyReqParams params);

    Response alertMessageRpt(Params.AlertMessageRptParams alertMessageRptParams);
}