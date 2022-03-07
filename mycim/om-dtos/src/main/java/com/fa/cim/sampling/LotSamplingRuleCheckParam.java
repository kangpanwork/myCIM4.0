package com.fa.cim.sampling;

import com.fa.cim.common.support.User;

import lombok.Data;

/**
 * description: lot Sampling Rule Check param
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/10 0010        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2020/12/10 0010 13:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class LotSamplingRuleCheckParam {

    private User user;

    private String processFlowObj;

    private String productObj;

    private LotInfoParam lotInfoParam;

    private String operationNumber;

    private String processFlowRelation;

    private Boolean holdState;

    private String callFunctionId;

    private String samplingExecute;

    @Data
    public static class LotInfoParam {

        private String lotObj;
        
        private String lotId;

        private String lotType;

        private String lotSubType;

        private Integer lotPriority;
    }

}