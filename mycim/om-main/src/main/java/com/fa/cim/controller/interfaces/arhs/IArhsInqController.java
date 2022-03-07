package com.fa.cim.controller.interfaces.arhs;

import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.dto.Params;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IArhsInqController {

    Response whatReticleActionListInq(Params.WhatReticleActionListInqParams params);

    Response whatReticleRetrieveInq(Params.WhatReticleRetrieveInqParams params);

    Response whereNextForRpodInq(Params.WhereNextForReticlePodInqParams params);

    Response reticleComptJobListInq(Params.ReticleComponentJobListInqParams params);

    Response reticleDispatchJobListInq(User params);

    Response rpodXferJobListInq(Params.ReticlePodXferJobListInqParams params);

    Response whatRpodForReticleXferInq(Params.WhatReticlePodForReticleXferInqParams params);
}
