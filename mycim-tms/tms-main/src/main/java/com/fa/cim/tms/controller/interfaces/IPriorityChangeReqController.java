package com.fa.cim.tms.controller.interfaces;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/23                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/23 9:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPriorityChangeReqController {

    Response tmsPriorityChangeReq(Params.PriorityChangeReqParam priorityChangeReqParam);
}
