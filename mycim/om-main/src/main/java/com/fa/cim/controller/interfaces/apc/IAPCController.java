package com.fa.cim.controller.interfaces.apc;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/7          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/5/7 11:28
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAPCController {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 11:50
     * @param apcifPointReqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response APCInterfaceOpsReq(@RequestBody Params.APCIFPointReqParams apcifPointReqParams);
}