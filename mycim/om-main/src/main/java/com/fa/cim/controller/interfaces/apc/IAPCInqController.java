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
 * 2020/4/27          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/4/27 12:27
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IAPCInqController {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 12:35
     * @param apcRunTimeCapabilityInqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response APCRunTimeCapabilityInq(@RequestBody Params.APCRunTimeCapabilityInqParams apcRunTimeCapabilityInqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/4/27 13:03
     * @param apcRecipeParameterAdjustInqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response APCRecipeParameterAdjustInq(@RequestBody Params.APCRecipeParameterAdjustInqParams apcRecipeParameterAdjustInqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 10:03
     * @param apcifListInqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response APCInterfaceListInq(@RequestBody Params.APCIFListInqParams apcifListInqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2020/5/7 10:42
     * @param entityListInqParams -
     * @return com.fa.cim.common.support.Response
     */
    Response entityListInq(@RequestBody Params.EntityListInqParams entityListInqParams);
}