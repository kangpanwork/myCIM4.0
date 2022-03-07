package com.fa.cim.service.ocap;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 13:45
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IOcapService {
    /**
     * description: Ocap Update Service
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/21       ********              Nyx             create file
     *
     * @author: hd
     * @date: 2021/1/21 10:24
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    void ocapUpdateRpt(Infos.ObjCommon objCommon,Params.OcapReqParams params);
}