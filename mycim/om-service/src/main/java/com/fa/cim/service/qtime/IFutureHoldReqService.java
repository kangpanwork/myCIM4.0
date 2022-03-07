package com.fa.cim.service.qtime;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/9/2         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/9/2 9:40 上午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
public interface IFutureHoldReqService {

    void sxFutureHoldReq(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params);

}