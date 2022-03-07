package com.fa.cim.tms.controller.interfaces;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/23                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/23 9:54
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEndTimeViolationRptController {

    Response tmsEndTimeViolationRpt(Params.EndTimeViolationReportParam endTimeViolationReportParam);

    Response rtmsEndTimeViolationRpt(Params.EndTimeViolationReportParam endTimeViolationReportParam);
}
