package com.fa.cim.tms.service;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/12 15:09
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IE10StatusRptService {

    Results.E10StatusReportResult sxE10StatusRpt(@RequestBody Infos.ObjCommon objCommon, Params.E10StatusReportParmas e10StatusReportParmas);
}
