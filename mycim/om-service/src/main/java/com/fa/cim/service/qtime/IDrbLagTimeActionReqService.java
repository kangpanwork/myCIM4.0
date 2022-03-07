package com.fa.cim.service.qtime;

import com.fa.cim.common.support.User;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/9/2         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/9/2 9:35 上午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
public interface IDrbLagTimeActionReqService {

    Results.DurableProcessLagTimeUpdateReqResult sxDrbLagTimeActionReq(Infos.ObjCommon objCommonIn, User user, Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm);

}