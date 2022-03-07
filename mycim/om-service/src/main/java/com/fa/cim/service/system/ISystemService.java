package com.fa.cim.service.system;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISystemService {

    void sxOMSEnvModifyReq(Infos.ObjCommon objCommon, Params.OMSEnvModifyReqParams params);

    Results.AlertMessageRptResult sxAlertMessageRpt(Infos.ObjCommon objCommon, Params.AlertMessageRptParams alertMessageRptParams);

}
