package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IN2PurgeRptService;
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
 * @date: 2020/10/20 13:51
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class N2PurgeRrtService implements IN2PurgeRptService {

    public Results.N2PurgeReportResult sxN2PurgeRpt(Infos.ObjCommon objCommon, Params.N2PurgeReportParams n2PurgeReportParams) {
        Results.N2PurgeReportResult result = new Results.N2PurgeReportResult();
        return result;
    }
}
