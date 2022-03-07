package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ISubComponentStatusRptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 15:44
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class SubComponentStatusReportService implements ISubComponentStatusRptService {

    public Results.SubComponentStatusReportResult sxSubComponentStatusReport(Infos.ObjCommon objCommon, Params.SubComponentStatusReportParam subComponentStatusReportParam) {
        Results.SubComponentStatusReportResult result = new Results.SubComponentStatusReportResult();
        return result;
    }
}
