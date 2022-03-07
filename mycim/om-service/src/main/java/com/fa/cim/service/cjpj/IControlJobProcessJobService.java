package com.fa.cim.service.cjpj;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 17:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IControlJobProcessJobService {
    void sxProcessJobMapInfoRpt(Infos.ObjCommon objCommon, Infos.ProcessJobMapInfoRptInParm processJobMapInfoRptInParm);
    void sxPJInfoRpt(Infos.ObjCommon objCommon, Params.PJInfoRptParams params) ;
    void sxPJStatusChangeReq(Infos.ObjCommon objCommon, Params.PJStatusChangeReqParams params) ;
    void sxPJStatusChangeRpt(Infos.ObjCommon objCommon, Params.PJStatusChangeRptInParm params) ;
    Results.CJStatusChangeReqResult sxCJStatusChangeReqService(Infos.ObjCommon objCommon, Params.CJStatusChangeReqParams cjStatusChangeReqParams);
}
