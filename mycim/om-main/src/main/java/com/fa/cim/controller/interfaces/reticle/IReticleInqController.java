package com.fa.cim.controller.interfaces.reticle;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

public interface IReticleInqController {

    Response holdReticleListInq(@RequestBody Params.ReticleHoldListInqParams params);

    Response reticleUpdateParamsInq(@RequestBody Params.reticleUpdateParamsInqParams params);
}
