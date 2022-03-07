package com.fa.cim.controller.interfaces.runCard;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/12 15:19
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IRunCardController {

    Response runCardUpdateReq(Params.RunCardUpdateReqParams params);

    Response runCardDeleteReq(Params.RunCardDeleteReqParams param);

    Response runCardApprovalReq(Params.RunCardStateApprovalReqParams params);

    Response runCardStateChangeReq(Params.RunCardStateChangeReqParams params);
}
