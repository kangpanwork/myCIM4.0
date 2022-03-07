package com.fa.cim.rtms;

import com.fa.cim.common.support.User;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/8/3        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/8/3 11:05
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ReticleRTMSEqpOutReqParams {

    private Integer reticleId;

    private String reticleName;

    private String podName;

    private User user;

}