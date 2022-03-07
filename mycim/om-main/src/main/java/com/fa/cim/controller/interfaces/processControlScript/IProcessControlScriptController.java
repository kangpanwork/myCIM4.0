package com.fa.cim.controller.interfaces.processControlScript;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 17:59
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProcessControlScriptController {
    /**
     * This function changes the value of Script Parameter.
     * <p> The Script Parameter is defined by MDS.
     * <p> The Script Parameter for change specifies by the Parameter Class and the identifier.
     * <p> To get the recipe parameter, should be specified the parameter class and the identifier.
     * <p> The parameter class and the category of parameter class should be matched to the category of specified identifier.
     * <p> Ex. If the parameter class name is specified as 'Eqp', the identifier should be equipment ID.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/14 13:25
     */
    Response pcsParameterValueSetReq(Params.PCSParameterValueSetReqParams params);

    Response runProcessControlScriptReq(Params.ProcessControlScriptRunReqParams params);
}