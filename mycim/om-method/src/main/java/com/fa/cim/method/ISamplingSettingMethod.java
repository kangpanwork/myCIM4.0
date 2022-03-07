package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.common.support.RetCode;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/19       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2018/12/19 14:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISamplingSettingMethod {
    /**
     * description:samplingSetting_format_Check
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param samplingPolicyName
     * @param samplingWaferAttribute -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Nyx
     * @date 2018/12/19 14:19
     */
    void samplingSettingFormatCheck(Infos.ObjCommon objCommon, String samplingPolicyName, String samplingWaferAttribute);
}
