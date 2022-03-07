package com.fa.cim.service.qtime;

import com.fa.cim.common.support.RetCode;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/9/2         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/9/2 9:49 上午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
public interface IQtimeActionReqService {

    RetCode<String> sxQtimeActionReq(Infos.ObjCommon objCommon, Inputs.QtimeActionReqIn qtimeActionReqIn);

}