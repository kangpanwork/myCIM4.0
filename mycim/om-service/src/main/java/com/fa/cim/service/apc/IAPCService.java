package com.fa.cim.service.apc;

import com.fa.cim.dto.Infos;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 13:05
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAPCService {

    void sxAPCInterfaceOpsReq(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf);
}
