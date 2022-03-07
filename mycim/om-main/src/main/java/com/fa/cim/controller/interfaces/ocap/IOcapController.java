package com.fa.cim.controller.interfaces.ocap;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: hd
 * @date: 2021/1/21 9:52
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IOcapController {
    /**
     * description: OcapUpdateRpt Transaction Interface when OCAP System decide to AddMeasure or ReMeasure
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/21      ********              hd             create file
     *
     * @author: hd
     * @date: 2021/1/21 9:52
     * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response ocapUpdateRpt(Params.OcapReqParams params);

}