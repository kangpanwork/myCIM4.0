package com.fa.cim.service.plan;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 17:36
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPlanInqService {
    Results.LotPlanChangeReserveListInqResult sxLotPlanChangeReserveListInq(Infos.ObjCommon objCommon, Params.LotPlanChangeReserveListInqParams params);
}