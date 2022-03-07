package com.fa.cim.sampling;

import java.util.List;

import com.fa.cim.common.support.ObjectIdentifier;

import lombok.Data;

/**
 * description:  advanced wafer sampling 结果
 *
 * change history:  
 * date             defect#             person             comments  
 * ---------------------------------------------------------------------------------------------------------------------  
 * 2021/6/24 0024          ********            Decade            create file
 * @author: YJ
 * @date: 2021/6/24 0024 10:49
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class AdvancedWaferSamplingResults {
    /**
     * 抽检结果
     */
    private List<WaferResult> lotWaferList;

    /**
     * 是否命中
     */
    private Boolean hitSampling;

    @Data
    public static class WaferResult{
        /**
         * wafer Id
         */
        private ObjectIdentifier waferId;

        /**
         * 是否加工
         */
        private Boolean processJobExecFlag;

        /**
         * 位置
         */
        private Integer slotNumber;
    }

}
