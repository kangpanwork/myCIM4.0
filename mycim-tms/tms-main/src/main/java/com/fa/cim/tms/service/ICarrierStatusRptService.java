package com.fa.cim.tms.service;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;

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
public interface ICarrierStatusRptService {

    Results.CarrierStatusReportResult sxCarrierStatusReport(Infos.ObjCommon objCommon, Params.CarrierStatusReportParam carrierStatusReportParam);

    Results.CarrierStatusReportResult sxRtmsCarrierStatusReport(Infos.ObjCommon objCommon, Params.CarrierStatusReportParam carrierStatusReportParam);
}
