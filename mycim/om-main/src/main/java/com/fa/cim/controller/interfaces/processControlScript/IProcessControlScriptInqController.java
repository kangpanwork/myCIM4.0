package com.fa.cim.controller.interfaces.processControlScript;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 16:15
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProcessControlScriptInqController {
    /**
     * description:
     * <p>This function returns Script Parameter.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/7                            Wind                create file
     *
     * @author: Wind
     * @date: 2019/1/7 15:59
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response pcsParameterValueInq(Params.PCSParameterValueInqParams params);
}