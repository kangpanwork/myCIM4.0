package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.processControlScript.IProcessControlScriptController;
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
 * @date: 2019/7/30 18:02
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("ProcessControlScriptCancel")
@Transactional(rollbackFor = Exception.class)
public class ProcessControlScriptCancel implements IProcessControlScriptController {
    @Override
    public Response pcsParameterValueSetReq(Params.PCSParameterValueSetReqParams params) {
        return null;
    }

    @Override
    public Response runProcessControlScriptReq(Params.ProcessControlScriptRunReqParams params) {
        return null;
    }
}